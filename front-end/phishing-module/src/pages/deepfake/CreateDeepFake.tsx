import {
  Check,
  ChevronLeft,
  ChevronRight,
  Loader2,
  Sparkles,
} from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';

import { Button } from 'components/common/Button';
import { Card, CardContent, CardHeader } from 'components/common/Card';
import FaceCapture from 'features/deepfake/create/FaceCapture';
import Onboarding from 'features/deepfake/create/Onboarding';
import PreviewGenerate from 'features/deepfake/create/PreviewGenerate';
import ScriptReading from 'features/deepfake/create/ScriptReading';
import {
  IDeepfakeStep1StartPayload,
  IDeepfakeVideo,
  resolveBackgroundDisplayUrl,
  resolveFaceDisplayUrl,
  resolveUiStepFromCompletedApiStep,
  STEP_HEADERS,
  STEPS,
  TEMPLATE_SCRIPT,
  VoiceSample,
} from 'models/Deepfake';
import VoiceCloning from 'features/deepfake/create/VoiceCloning';
import { cn, dataUrlToFile, isSuccessResponse } from 'utils/Helper';
import DeepfakeReviewDialog from 'features/deepfake/create/ReviewDialog';
import { routes } from 'routes/Routes';
import { resolveVideoId, useDeepfake } from 'hooks/UseDeepfake';

const parseUiStep = (value: string | null): number | null => {
  if (!value) return null;
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed < 1 || parsed > 5) return null;
  return parsed;
};

const editDeepFakePath = (id: string) =>
  routes.deepfakeEdit.path.replace(':id', encodeURIComponent(id));

const CreateDeepFake = () => {
  const navigate = useNavigate();
  const { id: routeVideoId } = useParams<{ id: string }>();
  const [searchParams, setSearchParams] = useSearchParams();
  const isEditMode = !!routeVideoId?.trim();
  const loadVideoId =
    routeVideoId?.trim() || searchParams.get('id')?.trim() || '';
  const urlStep = parseUiStep(searchParams.get('step'));
  const {
    loading: loadingVideo,
    saving,
    generating,
    getVideoById,
    startWizard,
    uploadBackground,
    updateStep1,
    updateStep2,
    updateStep3,
    updateStep4,
    updateStep5,
    updateStep6,
  } = useDeepfake();

  const [step, setStep] = useState(urlStep ?? 1);
  const [videoId, setVideoId] = useState(loadVideoId);
  const [name, setName] = useState('');
  const [language, setLanguage] = useState('');
  const [background, setBackground] = useState({
    name: '',
    value: '',
    url: '',
  });
  const [customBackgroundFile, setCustomBackgroundFile] = useState<File | null>(
    null,
  );
  const [backgroundKey, setBackgroundKey] = useState('');
  const [backgroundPreviewUrl, setBackgroundPreviewUrl] = useState('');
  const [face, setFace] = useState<string | null>(null);
  const [faceImageId, setFaceImageId] = useState('');
  const [faceConfirmed, setFaceConfirmed] = useState(false);
  const [voice, setVoice] = useState<VoiceSample>({
    duration: 0,
    engine: '',
    audioFile: null,
  });
  const [script, setScript] = useState(TEMPLATE_SCRIPT);
  const [provider, setProvider] = useState('');
  const [providerId, setProviderId] = useState('');
  const [model, setModel] = useState('');
  const [progress, setProgress] = useState(0);
  const [generated, setGenerated] = useState(false);
  const [approved, setApproved] = useState(false);
  const [reviewOpen, setReviewOpen] = useState(false);
  const [stepLoading, setStepLoading] = useState(false);
  const [hydrated, setHydrated] = useState(!loadVideoId);
  const [maxReachedStep, setMaxReachedStep] = useState(urlStep ?? 1);

  const syncWizardUrl = (nextVideoId: string, nextStep: number) => {
    const params = new URLSearchParams();
    params.set('step', String(nextStep));

    // Edit route: keep /edit-deepfake/:id?step=
    if (isEditMode && nextVideoId) {
      navigate(`${editDeepFakePath(nextVideoId)}?${params.toString()}`, {
        replace: true,
      });
      return;
    }

    // Create flow stays on create; track wizard id in query only
    if (nextVideoId) params.set('id', nextVideoId);
    setSearchParams(params, { replace: true });
  };

  const goToStep = (nextStep: number, nextVideoId = videoId) => {
    const clamped = Math.min(5, Math.max(1, nextStep));
    setStep(clamped);
    setMaxReachedStep(prev => Math.max(prev, clamped));
    if (nextVideoId || clamped > 1) {
      syncWizardUrl(nextVideoId, clamped);
    }
  };

  const back = () => goToStep(step - 1);

  const hydrateFromVideo = (video: IDeepfakeVideo) => {
    const id = resolveVideoId(video);
    if (id) setVideoId(id);

    if (video.title) setName(video.title);
    if (video.language) setLanguage(video.language);

    const backgroundDisplayUrl = resolveBackgroundDisplayUrl(video);
    setBackground({
      name: video.backgroundPreset || 'Custom',
      value: `center / cover no-repeat url(${backgroundDisplayUrl})`,
      url: backgroundDisplayUrl,
    });
    setBackgroundKey(video.backgroundKey || backgroundDisplayUrl);
    setBackgroundPreviewUrl(backgroundDisplayUrl);
    setCustomBackgroundFile(null);

    const faceUrl = resolveFaceDisplayUrl(video);
    if (faceUrl) {
      setFace(faceUrl);
      setFaceImageId('');
      setFaceConfirmed(
        video.faceConfirmed === true || (video.currentStep ?? 0) >= 3,
      );
    } else {
      setFace(null);
      setFaceImageId('');
      setFaceConfirmed(false);
    }

    const engine = (video.voiceProvider || video.provider || '')
      .trim()
      .toUpperCase();

    setVoice({
      duration: 0,
      engine,
      audioFile: null,
      voiceId: video.voiceCloneId,
      voiceName: video.voiceName,
      sampleUrl: video.audioPreviewUrl || video.audioUrl,
    });

    if (video.script) setScript(video.script);

    const providerRaw = video.videoProvider || video.provider || '';
    setProvider(providerRaw);
    setProviderId(video.providerId || '');
    setModel(video.model || '');
    setGenerated(
      (video.currentStep ?? 0) >= 6 ||
        video.status === 'COMPLETED' ||
        !!video.videoUrl,
    );
    setProgress(
      video.status === 'COMPLETED' || !!video.videoUrl
        ? 100
        : (video.renderProgress ?? 0),
    );
  };

  useEffect(() => {
    if (!loadVideoId) {
      setHydrated(true);
      return;
    }

    // Create flow just received an id — don't remount/reset back to step 1
    if (videoId === loadVideoId && hydrated) {
      return;
    }

    let cancelled = false;
    const load = async () => {
      setHydrated(false);
      const video = await getVideoById(loadVideoId);
      if (cancelled) return;

      if (!video) {
        toast.error('Unable to load deepfake video');
        setHydrated(true);
        navigate(routes.deepFakeContentLibrary.path, { replace: true });
        return;
      }

      hydrateFromVideo(video);
      const resumeStep = resolveUiStepFromCompletedApiStep(video.currentStep);
      const stepToUse =
        urlStep && urlStep >= 1 && urlStep <= resumeStep ? urlStep : resumeStep;

      setMaxReachedStep(Math.max(resumeStep, stepToUse));
      setStep(stepToUse);
      syncWizardUrl(resolveVideoId(video) || loadVideoId, stepToUse);
      setHydrated(true);
    };

    void load();
    return () => {
      cancelled = true;
    };
    // Intentionally depend on video id only so step changes don't refetch.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadVideoId, getVideoById, navigate]);

  useEffect(() => {
    if (!hydrated) return;
    if (!videoId && step === 1) return;

    const currentUrlStep = parseUiStep(searchParams.get('step'));
    const currentUrlId = searchParams.get('id')?.trim() || '';

    if (isEditMode) {
      if (currentUrlStep === step && routeVideoId === videoId) return;
    } else if (currentUrlStep === step && currentUrlId === videoId) {
      return;
    }

    syncWizardUrl(videoId, step);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [step, videoId, hydrated, isEditMode, routeVideoId]);

  const canNext = useMemo(() => {
    if (step === 1) return name.trim().length > 0 && !!language;
    if (step === 2) return !!face;
    if (step === 3) {
      // A new sample has to be cloned, so it needs a name and a credential.
      if (voice.audioFile)
        return (
          voice.duration > 0 && !!voice.providerId && !!voice.voiceName?.trim()
        );
      return !!voice.voiceId;
    }
    if (step === 4) return script.trim().length > 10;
    if (step === 5) return !!provider && !!providerId && !!model;
    return true;
  }, [step, name, language, face, voice, script, provider, providerId, model]);

  const previewBackground = backgroundPreviewUrl
    ? `center / cover no-repeat url(${backgroundPreviewUrl})`
    : background.value;

  const resolveBackgroundImageUrl = async (): Promise<string> => {
    if (customBackgroundFile) {
      if (backgroundPreviewUrl) return backgroundPreviewUrl;
      if (backgroundKey) return backgroundKey;

      const response = await uploadBackground(customBackgroundFile);
      if (!response || !isSuccessResponse(response.statusCode)) {
        throw new Error(response?.message || 'Failed to upload background');
      }

      const uploadData = response.data;
      const imageUrl =
        uploadData?.backgroundPreviewUrl || uploadData?.backgroundKey;
      if (!imageUrl) {
        throw new Error('Background uploaded but no image URL was returned');
      }

      setBackgroundKey(imageUrl);
      setBackgroundPreviewUrl(uploadData.backgroundPreviewUrl || imageUrl);
      return imageUrl;
    }

    if (backgroundPreviewUrl) return backgroundPreviewUrl;
    if (background.url) return background.url;
    if (backgroundKey) return backgroundKey;
    throw new Error('Background image is required');
  };

  const buildStep1Payload = async (): Promise<IDeepfakeStep1StartPayload> => {
    const imageUrl = await resolveBackgroundImageUrl();
    return {
      title: name.trim(),
      language,
      backgroundType: 'CUSTOM',
      backgroundKey: imageUrl,
    };
  };

  const handleStep1Continue = async () => {
    setStepLoading(true);
    try {
      const step1Payload = await buildStep1Payload();

      let currentVideoId = videoId;
      if (!currentVideoId) {
        const startResponse = await startWizard(step1Payload);
        if (!startResponse || !isSuccessResponse(startResponse.statusCode)) {
          throw new Error(startResponse?.message || 'Failed to start wizard');
        }

        currentVideoId = resolveVideoId(startResponse.data);
        if (!currentVideoId) {
          toast.error('Wizard started but no video id was returned');
          return;
        }
        setVideoId(currentVideoId);
      }

      const updateResponse = await updateStep1(currentVideoId, step1Payload);
      if (!updateResponse || !isSuccessResponse(updateResponse.statusCode)) {
        toast.error(updateResponse?.message || 'Failed to save onboarding');
        return;
      }

      goToStep(2, currentVideoId);
    } catch (error) {
      toast.error((error as Error).message || 'Failed to save onboarding');
    } finally {
      setStepLoading(false);
    }
  };

  const handleStep2Continue = async () => {
    if (!face || !videoId) return;

    setStepLoading(true);
    try {
      const isRemoteFace = /^https?:\/\//i.test(face);

      if (!isRemoteFace) {
        const faceFile = face.startsWith('data:')
          ? dataUrlToFile(face, 'face-capture.jpg')
          : null;

        if (!faceFile) {
          toast.error('Invalid face image');
          return;
        }

        const step2Response = await updateStep2(videoId, { file: faceFile });
        if (!step2Response || !isSuccessResponse(step2Response.statusCode)) {
          throw new Error(
            step2Response?.message || 'Failed to save face capture',
          );
        }

        if (step2Response.data?.faceImageUrl) {
          setFace(step2Response.data.faceImageUrl);
        }
      } else if (faceImageId) {
        const step2Response = await updateStep2(videoId, { faceImageId });
        if (!step2Response || !isSuccessResponse(step2Response.statusCode)) {
          throw new Error(
            step2Response?.message || 'Failed to select face capture',
          );
        }
      }

      const step3Response = await updateStep3(videoId, { confirmed: true });
      if (!step3Response || !isSuccessResponse(step3Response.statusCode)) {
        throw new Error(
          step3Response?.message || 'Failed to confirm face preview',
        );
      }

      setFaceConfirmed(true);
      toast.success('Face model confirmed');
      goToStep(3);
    } catch (error) {
      toast.error((error as Error).message || 'Failed to save face capture');
    } finally {
      setStepLoading(false);
    }
  };

  const handleStep3Continue = async () => {
    if (!videoId) return;
    if (!voice.audioFile && !voice.voiceId) return;

    const resolvedVoiceName = voice.voiceName?.trim() || undefined;

    if (voice.audioFile) {
      if (!voice.providerId) {
        toast.error('Select a voice provider');
        return;
      }
      if (!resolvedVoiceName) {
        toast.error('Enter a name for the new voice');
        return;
      }
    }

    setStepLoading(true);
    try {
      const response = voice.audioFile
        ? await updateStep4(videoId, {
            file: voice.audioFile,
            provider: voice.engine.toUpperCase(),
            providerId: voice.providerId,
            language,
            voiceName: resolvedVoiceName,
          })
        : await updateStep4(videoId, { voiceCloneId: voice.voiceId });
      if (!response || !isSuccessResponse(response.statusCode)) {
        throw new Error(response?.message || 'Failed to save voice cloning');
      }

      toast.success(
        voice.audioFile
          ? `Voice cloned with ${voice.engine}, re-usable for any future script`
          : `Using cloned voice${resolvedVoiceName ? ` “${resolvedVoiceName}”` : ''}`,
      );
      goToStep(4);
    } catch (error) {
      toast.error((error as Error).message || 'Failed to save voice cloning');
    } finally {
      setStepLoading(false);
    }
  };

  const handleStep4Continue = async () => {
    if (!videoId) return;

    setStepLoading(true);
    try {
      const response = await updateStep5(videoId, { script });
      if (!response || !isSuccessResponse(response.statusCode)) {
        throw new Error(response?.message || 'Failed to save script');
      }

      toast.success('Script saved successfully');
      goToStep(5);
    } catch (error) {
      toast.error((error as Error).message || 'Failed to save script');
    } finally {
      setStepLoading(false);
    }
  };

  const handleGenerate = async () => {
    if (!approved) {
      toast.error('Review and approve before generating');
      return;
    }
    if (!provider || !providerId || !model || !videoId) {
      toast.error('Pick a provider and model first');
      return;
    }

    setGenerated(false);

    try {
      const response = await updateStep6(videoId, {
        providerId,
      });
      if (!response || !isSuccessResponse(response.statusCode)) {
        throw new Error(response?.message || 'Failed to generate video');
      }

      setGenerated(true);
      setMaxReachedStep(5);
      toast.success('Deepfake generation initiated successfully');
      navigate(routes.deepFakeContentLibrary.path);
    } catch (error) {
      toast.error((error as Error).message || 'Failed to generate video');
    }
  };

  const handleContinue = async () => {
    if (step === 1) {
      await handleStep1Continue();
      return;
    }
    if (step === 2) {
      await handleStep2Continue();
      return;
    }
    if (step === 3) {
      await handleStep3Continue();
      return;
    }
    if (step === 4) {
      await handleStep4Continue();
    }
  };

  const isBusy =
    stepLoading || saving || generating || loadingVideo || !hydrated;

  if (!hydrated) {
    return (
      <div className="flex min-h-[420px] w-full items-center justify-center gap-2 text-muted-foreground">
        <Loader2 className="size-5 animate-spin" />
        Loading deepfake wizard…
      </div>
    );
  }

  return (
    <div className="w-full space-y-6">
      <header>
        <p className="text-xs uppercase text-muted-foreground">
          Phishing Simulation
        </p>
        <h1 className="mt-1 text-3xl font-semibold">
          {isEditMode ? 'Edit Deepfake Video' : 'Generate Deepfake Video'}
        </h1>
        <p className="mt-2 text-sm text-muted-foreground">
          {isEditMode
            ? 'Update your deepfake training video settings, assets, and script, then re-render when ready.'
            : 'Choose to upload a photo or capture your face in real-time, then record your voice for the consent script or upload a pre-recorded audio file. Customise the script, render and deliver.'}
        </p>
      </header>

      <Card>
        <CardContent className="p-4">
          <div className="grid grid-cols-5 gap-2">
            {STEPS.map((item, index) => {
              const active = step === item.id;
              const done = step > item.id;
              const reachable = item.id <= maxReachedStep;

              return (
                <div key={item.id} className="flex items-center gap-2">
                  <button
                    type="button"
                    disabled={!reachable || isBusy}
                    onClick={() => reachable && goToStep(item.id)}
                    className={cn(
                      'flex w-full items-center gap-3 rounded-lg border px-3 py-2 text-left transition-all duration-200',
                      active &&
                        'border-primary bg-transparent ring-1 ring-primary',
                      done && 'border-primary bg-primary/10',
                      !active &&
                        !done &&
                        'border-card-border bg-transparent text-muted-foreground',
                      reachable &&
                        !isBusy &&
                        'cursor-pointer hover:bg-primary/5',
                      (!reachable || isBusy) && 'cursor-default',
                    )}
                  >
                    <span
                      className={cn(
                        'grid size-6 place-items-center rounded-full text-xs font-semibold',
                        active && 'bg-primary text-primary-foreground',
                        done && 'bg-primary text-primary-foreground',
                        !active && !done && 'bg-muted text-muted-foreground',
                      )}
                    >
                      {done ? <Check className="size-3.5" /> : item.id}
                    </span>
                    <span className="space-y-1">
                      <span
                        className={cn(
                          'block text-xs font-medium',
                          active && 'text-primary',
                          done && 'text-primary',
                          !active && !done && 'text-muted-foreground',
                        )}
                      >
                        {item.title}
                      </span>
                      <span
                        className={cn(
                          'block text-[10px] text-muted-foreground',
                          active && 'text-primary',
                          done && 'text-primary',
                          !active && !done && 'text-muted-foreground',
                        )}
                      >
                        {item.desc}
                      </span>
                    </span>
                  </button>
                  {index < STEPS.length - 1 && (
                    <ChevronRight className="size-4 shrink-0 text-muted-foreground" />
                  )}
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <Card className="min-h-[460px]">
          <CardHeader className="md:p-8">
            <StepHeader
              eyebrow={
                STEP_HEADERS[step !== 2 ? step : face ? '2_2' : '2_1'].eyebrow
              }
              title={
                STEP_HEADERS[step !== 2 ? step : face ? '2_2' : '2_1'].title
              }
              desc={STEP_HEADERS[step !== 2 ? step : face ? '2_2' : '2_1'].desc}
            />
          </CardHeader>

          <CardContent className="space-y-6 md:p-8 md:pt-0">
            {step === 1 && (
              <Onboarding
                name={name}
                setName={setName}
                language={language}
                setLanguage={setLanguage}
                background={background}
                setBackground={setBackground}
                onCustomBackgroundFile={file => {
                  setCustomBackgroundFile(file);
                  setBackgroundKey('');
                  setBackgroundPreviewUrl('');
                }}
              />
            )}
            {step === 2 && (
              <FaceCapture
                face={face}
                setFace={value => {
                  setFace(value);
                  setFaceImageId('');
                  setFaceConfirmed(false);
                  setGenerated(false);
                  setApproved(false);
                }}
                confirmed={faceConfirmed}
                selectedFaceImageId={faceImageId}
                onRetake={() => {
                  setFace(null);
                  setFaceImageId('');
                  setFaceConfirmed(false);
                  setApproved(false);
                }}
                onCaptured={() => {
                  setGenerated(false);
                  setApproved(false);
                }}
                onSelectExisting={image => {
                  setFace(image.url);
                  setFaceImageId(image.id);
                  setFaceConfirmed(false);
                  setGenerated(false);
                  setApproved(false);
                }}
              />
            )}
            {step === 3 && (
              <VoiceCloning
                voice={voice}
                setVoice={setVoice}
                onStop={() => {
                  setGenerated(false);
                  setApproved(false);
                }}
              />
            )}
            {step === 4 && (
              <ScriptReading
                script={script}
                setScript={value => {
                  setScript(value);
                  setGenerated(false);
                  setApproved(false);
                }}
              />
            )}
            {step === 5 && (
              <PreviewGenerate
                provider={provider}
                setProvider={value => {
                  setProvider(value);
                  setModel('');
                  setProviderId('');
                  setGenerated(false);
                  setApproved(false);
                }}
                providerId={providerId}
                setProviderId={setProviderId}
                model={model}
                setModel={value => {
                  setModel(value);
                  setGenerated(false);
                  setApproved(false);
                }}
                generating={saving || generating}
                progress={progress}
                approved={approved}
                background={previewBackground}
                face={face}
              />
            )}

            <div className="mt-8 flex items-center justify-between border-t border-card-border pt-5">
              <Button
                variant="secondary"
                onClick={back}
                disabled={step === 1 || isBusy}
              >
                <ChevronLeft className="mr-1 size-4" /> Back
              </Button>
              {step < 5 ? (
                <Button onClick={handleContinue} disabled={!canNext || isBusy}>
                  {isBusy ? (
                    <Loader2 className="mr-1 size-4 animate-spin" />
                  ) : null}
                  Save & Continue Later
                  <ChevronRight className="size-4" />
                </Button>
              ) : (
                <div className="flex flex-wrap items-center justify-end gap-2">
                  <Button
                    onClick={() => setReviewOpen(true)}
                    disabled={!canNext || isBusy}
                  >
                    <Check className="mr-1 size-4" />
                    {approved ? 'Re-review ' : 'Review '} & Approve
                  </Button>
                  <Button
                    size="lg"
                    onClick={handleGenerate}
                    disabled={
                      isBusy || !provider || !providerId || !model || !approved
                    }
                    className="bg-primary text-primary-foreground"
                  >
                    <Sparkles className="mr-2 size-4" />
                    {generating
                      ? 'Generating…'
                      : generated
                        ? 'Re-generate'
                        : 'Generate video'}
                  </Button>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        <Card className="h-fit p-5">
          <h3 className="text-sm font-semibold uppercase text-muted-foreground">
            Live preview
          </h3>
          <div
            className="relative mt-3 aspect-video overflow-hidden rounded-lg border border-card-border"
            style={{ background: previewBackground }}
          >
            {face ? (
              <img
                src={face}
                alt="Avatar"
                className="absolute bottom-0 left-1/2 h-[85%] -translate-x-1/2 object-contain"
              />
            ) : (
              <div className="absolute inset-0 grid place-items-center text-xs text-muted-foreground">
                Avatar preview
              </div>
            )}
            <div className="absolute inset-x-3 bottom-3 rounded-md bg-background/70 px-3 py-2 text-xs backdrop-blur">
              <div className="truncate font-medium">
                {name || 'Untitled video'}
              </div>
              <div className="truncate text-[10px] text-muted-foreground">
                {provider || 'No provider'} · {model || 'No model'}
              </div>
            </div>
          </div>
          <dl className="mt-4 space-y-2 text-xs">
            <SummaryRow label="Language" value={language} />
            <SummaryRow
              label="Voice"
              value={
                voice.audioFile
                  ? `${voice.duration}s · ${voice.engine}`
                  : voice.voiceId
                    ? voice.voiceName || voice.engine
                    : '—'
              }
            />
            <SummaryRow
              label="Script"
              value={
                maxReachedStep >= 4 && script.trim()
                  ? `${script.split(/\s+/).filter(Boolean).length} words`
                  : '—'
              }
            />
            <SummaryRow label="Background" value={background.name || '—'} />
            <SummaryRow
              label="Status"
              value={generated ? 'Rendered' : approved ? 'Approved' : 'Draft'}
            />
          </dl>
        </Card>
      </div>

      <DeepfakeReviewDialog
        open={reviewOpen}
        onOpenChange={setReviewOpen}
        background={previewBackground}
        face={face}
        provider={provider}
        name={name}
        script={script}
        voiceEngine={voice.engine}
        language={language}
        model={model}
        onEditScript={() => {
          setReviewOpen(false);
          goToStep(4);
        }}
        onEditVoice={() => {
          setReviewOpen(false);
          goToStep(3);
        }}
        onEditFace={() => {
          setReviewOpen(false);
          goToStep(2);
        }}
        onConfirm={() => {
          setReviewOpen(false);
          setApproved(true);
          toast.success('Approved — you can generate the video now');
        }}
      />
    </div>
  );
};

const StepHeader = ({
  eyebrow,
  title,
  desc,
}: {
  eyebrow: string;
  title: string;
  desc: string;
}) => {
  return (
    <div>
      <p className="text-xs uppercase text-primary">{eyebrow}</p>
      <h2 className="mt-1 text-2xl font-semibold">{title}</h2>
      <p className="mt-1 text-sm text-muted-foreground">{desc}</p>
    </div>
  );
};

const SummaryRow = ({ label, value }: { label: string; value: string }) => {
  return (
    <div className="flex items-center justify-between gap-4">
      <dt className="text-muted-foreground">{label}</dt>
      <dd className="truncate font-medium">{value}</dd>
    </div>
  );
};

export default CreateDeepFake;
