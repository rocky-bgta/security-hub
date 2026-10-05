import {
  Check,
  Loader2,
  Mic,
  Pause,
  Play,
  Plus,
  Square,
  Trash2,
  Upload,
} from 'lucide-react';
import { useCallback, useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'components/common/Button';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import Pagination from 'common/Pagination';
import ConfirmDialog from 'components/ConfirmDialog';
import { cn } from 'utils/Helper';
import { type IDeepfakeVoice, type VoiceSample } from 'models/Deepfake';
import { type IProviderCredential } from 'models/ProviderCredential';
import { resolveVoiceId, useDeepfake } from 'hooks/UseDeepfake';
import useProviderCredentials from 'hooks/UseProviderCredentials';

interface IVoiceCloningProps {
  voice: VoiceSample;
  setVoice: (value: VoiceSample) => void;
  onStop: () => void;
}

const DEFAULT_PAGE_SIZE = 10;
const ALL_PROVIDERS_FILTER = 'ALL';

interface IVoiceProviderOption {
  providerName: string;
  providerId: string;
}

const resolveVoiceProviderLabel = (providerName: string): string => {
  return providerName.replace(/_/g, ' ');
};

/**
 * Voice records and credentials spell the same provider differently
 * (ELEVENLABS vs ELEVEN_LABS vs "Eleven Labs"), so compare on letters only.
 */
const normalizeProviderKey = (value?: string): string =>
  value ? value.replace(/[^a-z0-9]/gi, '').toLowerCase() : '';

const resolveVoiceProviders = (
  items: IProviderCredential[],
): IVoiceProviderOption[] => {
  const voiceCreds = items
    .filter(item => item.category === 'VOICE_CLONING' && item.isActive)
    .sort((a, b) => Number(b.isDefault) - Number(a.isDefault));

  const unique: IVoiceProviderOption[] = [];
  for (const item of voiceCreds) {
    if (!item.providerName || !item.id) continue;
    if (unique.some(option => option.providerName === item.providerName)) {
      continue;
    }
    unique.push({
      providerName: item.providerName,
      providerId: item.id,
    });
  }
  return unique;
};

const resolveVoiceName = (item: IDeepfakeVoice): string => {
  if (item.voiceName?.trim()) return item.voiceName.trim();
  if (item.fileName?.trim()) {
    return item.fileName.replace(/\.[^/.]+$/, '') || item.fileName;
  }
  return 'Cloned voice';
};

const formatCreatedAt = (value?: string): string => {
  if (!value) return '';
  const parsed = Date.parse(value);
  if (Number.isNaN(parsed)) return value;

  const date = new Date(parsed);
  const day = date.getDate();
  const month = date.toLocaleDateString('en-US', { month: 'long' });
  const year = date.getFullYear();
  return `${day} ${month}, ${year}`;
};

const VoiceCloning = ({ voice, setVoice, onStop }: IVoiceCloningProps) => {
  const { voices, voicesLoading, fetchVoices, deleteVoice } = useDeepfake();
  const { getCredentialList, isLoadingList: providersLoading } =
    useProviderCredentials();
  const fileRef = useRef<HTMLInputElement>(null);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const chunksRef = useRef<Blob[]>([]);
  const streamRef = useRef<MediaStream | null>(null);
  const elapsedRef = useRef(0);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  const [voiceProviders, setVoiceProviders] = useState<IVoiceProviderOption[]>(
    [],
  );
  const [providerFilter, setProviderFilter] = useState(ALL_PROVIDERS_FILTER);
  const [offset, setOffset] = useState(0);
  const [playingVoiceId, setPlayingVoiceId] = useState<string | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<IDeepfakeVoice | null>(
    null,
  );
  const [deleting, setDeleting] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [isEditingSample, setIsEditingSample] = useState(false);
  const [recording, setRecording] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const [modalEngine, setModalEngine] = useState(voice.engine);
  const [modalProviderId, setModalProviderId] = useState(
    voice.providerId || '',
  );
  const [modalSample, setModalSample] = useState<VoiceSample>({
    duration: 0,
    engine: voice.engine,
    providerId: voice.providerId,
    audioFile: null,
  });

  const resolveProviderId = useCallback(
    (providerName?: string) => {
      const key = normalizeProviderKey(providerName);
      if (!key) return '';
      return (
        voiceProviders.find(
          option => normalizeProviderKey(option.providerName) === key,
        )?.providerId || ''
      );
    },
    [voiceProviders],
  );

  const stopStream = () => {
    streamRef.current?.getTracks().forEach(track => track.stop());
    streamRef.current = null;
  };

  const stopPreview = useCallback(() => {
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current.currentTime = 0;
      audioRef.current = null;
    }
    setPlayingVoiceId(null);
  }, []);

  const togglePreview = (item: IDeepfakeVoice) => {
    const id = resolveVoiceId(item);
    if (!item.sampleUrl) {
      toast.info('No sample available for this voice');
      return;
    }

    if (playingVoiceId === id) {
      stopPreview();
      return;
    }

    stopPreview();
    const audio = new Audio(item.sampleUrl);
    audioRef.current = audio;
    setPlayingVoiceId(id);

    audio.onended = () => {
      setPlayingVoiceId(null);
      audioRef.current = null;
    };
    audio.onerror = () => {
      toast.error('Failed to play voice sample');
      setPlayingVoiceId(null);
      audioRef.current = null;
    };

    void audio.play().catch(() => {
      toast.error('Failed to play voice sample');
      setPlayingVoiceId(null);
      audioRef.current = null;
    });
  };

  const applyVoiceSelection = useCallback(
    (item: IDeepfakeVoice) => {
      const resolvedName = resolveVoiceName(item);
      setVoice({
        duration: 1,
        engine: item.provider,
        providerId: resolveProviderId(item.provider),
        audioFile: null,
        voiceId: resolveVoiceId(item),
        voiceName: resolvedName,
        sampleUrl: item.sampleUrl,
      });
    },
    [resolveProviderId, setVoice],
  );

  const loadVoices = useCallback(
    async (
      pageOffset: number,
      selectedProviderFilter: string,
      options?: { preferredVoiceId?: string; autoSelectFirst?: boolean },
    ) => {
      const items = await fetchVoices({
        providerId:
          selectedProviderFilter === ALL_PROVIDERS_FILTER
            ? undefined
            : selectedProviderFilter,
        offset: pageOffset,
        pageSize: DEFAULT_PAGE_SIZE,
      });

      if (items.length === 0) {
        if (options?.autoSelectFirst) {
          setVoice({
            duration: 0,
            engine: voice.engine,
            providerId: voice.providerId,
            audioFile: null,
            voiceId: undefined,
            voiceName: undefined,
            sampleUrl: undefined,
          });
        }
        return;
      }

      if (options?.preferredVoiceId) {
        const preferred = items.find(
          item => resolveVoiceId(item) === options.preferredVoiceId,
        );
        if (preferred) {
          applyVoiceSelection(preferred);
          return;
        }
      }

      if (options?.autoSelectFirst) {
        applyVoiceSelection(items[0]);
      }
    },
    [
      applyVoiceSelection,
      fetchVoices,
      setVoice,
      voice.engine,
      voice.providerId,
    ],
  );

  useEffect(() => {
    let active = true;
    void getCredentialList({
      isActive: true,
      offset: 0,
      pageSize: 100,
      sortBy: 'createdAt',
      sortOrder: 'asc',
    }).then(result => {
      if (!active) return;

      if (!result.ok) {
        toast.error(result.message);
        setVoiceProviders([]);
        return;
      }

      setVoiceProviders(resolveVoiceProviders(result.data.items));
    });

    return () => {
      active = false;
    };
  }, [getCredentialList]);

  useEffect(
    () => () => {
      stopStream();
      stopPreview();
    },
    [stopPreview],
  );

  useEffect(() => {
    stopPreview();
  }, [providerFilter, offset, stopPreview]);

  useEffect(() => {
    void loadVoices(offset, providerFilter, {
      preferredVoiceId: voice.voiceId,
      autoSelectFirst: offset === 0 && !voice.audioFile,
    });
    // Refetch when page or provider filter changes
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [offset, providerFilter]);

  const handleProviderFilterChange = (value: string) => {
    setProviderFilter(value);
    setOffset(0);
  };

  useEffect(() => {
    if (!recording) return;
    const timer = setInterval(() => {
      setElapsed(value => {
        const next = value + 0.1;
        elapsedRef.current = next;
        return next;
      });
    }, 100);
    return () => clearInterval(timer);
  }, [recording]);

  const handlePageChange = (page: number) => {
    setOffset(page - 1);
  };

  const currentPage = offset + 1;

  const resolveDefaultModalProvider =
    useCallback((): IVoiceProviderOption | null => {
      const fromFilter =
        providerFilter !== ALL_PROVIDERS_FILTER
          ? voiceProviders.find(option => option.providerId === providerFilter)
          : undefined;
      if (fromFilter) return fromFilter;

      const fromVoice = voiceProviders.find(
        option =>
          option.providerId === voice.providerId ||
          option.providerName === voice.engine,
      );
      return fromVoice || voiceProviders[0] || null;
    }, [providerFilter, voice.engine, voice.providerId, voiceProviders]);

  useEffect(() => {
    if (!modalOpen || modalEngine || voiceProviders.length === 0) return;
    const next = resolveDefaultModalProvider();
    if (!next) return;
    setModalEngine(next.providerName);
    setModalProviderId(next.providerId);
    setModalSample(previous => ({
      ...previous,
      engine: next.providerName,
      providerId: next.providerId,
    }));
  }, [modalOpen, modalEngine, voiceProviders, resolveDefaultModalProvider]);

  // Keep selected voice providerId in sync once credentials load.
  useEffect(() => {
    if (!voice.engine || voice.providerId || voiceProviders.length === 0)
      return;
    const matchedId = resolveProviderId(voice.engine);
    if (!matchedId) return;
    setVoice({
      ...voice,
      providerId: matchedId,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resolveProviderId, voice.engine, voice.providerId, voiceProviders]);

  const resetModalRecording = () => {
    if (mediaRecorderRef.current?.state === 'recording') {
      mediaRecorderRef.current.stop();
    }
    setRecording(false);
    setElapsed(0);
    elapsedRef.current = 0;
    stopStream();
  };

  const openNewVoiceModal = () => {
    const defaultProvider = resolveDefaultModalProvider();
    setModalEngine(defaultProvider?.providerName || '');
    setModalProviderId(defaultProvider?.providerId || '');
    setModalSample({
      duration: 0,
      engine: defaultProvider?.providerName || '',
      providerId: defaultProvider?.providerId || '',
      audioFile: null,
    });
    resetModalRecording();
    setIsEditingSample(false);
    setModalOpen(true);
  };

  const openEditSampleModal = () => {
    if (!voice.audioFile) {
      openNewVoiceModal();
      return;
    }

    setModalEngine(voice.engine);
    setModalProviderId(voice.providerId || resolveProviderId(voice.engine));
    setModalSample({
      duration: voice.duration,
      engine: voice.engine,
      providerId: voice.providerId,
      audioFile: voice.audioFile,
      voiceName: voice.voiceName,
    });
    resetModalRecording();
    setIsEditingSample(true);
    setModalOpen(true);
  };

  const discardPendingSample = () => {
    resetModalRecording();
    setVoice({
      duration: 0,
      engine: voice.engine,
      providerId: voice.providerId,
      audioFile: null,
      voiceId: undefined,
      voiceName: undefined,
      sampleUrl: undefined,
    });
    onStop();
    toast.success('Sample cleared.');
  };

  const discardModalSample = () => {
    setModalSample(previous => ({
      ...previous,
      duration: 0,
      audioFile: null,
    }));
    toast.success('Sample cleared.');
  };

  const closeNewVoiceModal = (open: boolean) => {
    if (!open) {
      if (mediaRecorderRef.current?.state === 'recording') {
        mediaRecorderRef.current.stop();
      }
      setRecording(false);
      stopStream();
      setIsEditingSample(false);
    }
    setModalOpen(open);
  };

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;
      chunksRef.current = [];

      const recorder = new MediaRecorder(stream);
      mediaRecorderRef.current = recorder;

      recorder.ondataavailable = event => {
        if (event.data.size > 0) chunksRef.current.push(event.data);
      };

      recorder.onstop = () => {
        const blob = new Blob(chunksRef.current, { type: 'audio/webm' });
        const file = new File([blob], 'voice-sample.webm', {
          type: 'audio/webm',
        });
        const duration = Math.max(1, Math.round(elapsedRef.current));
        setModalSample(previous => ({
          duration,
          engine: modalEngine,
          providerId: modalProviderId,
          audioFile: file,
          voiceName: previous.voiceName,
        }));
        stopStream();
        setElapsed(0);
        elapsedRef.current = 0;
      };

      recorder.start();
      setRecording(true);
      setElapsed(0);
      elapsedRef.current = 0;
      setModalSample(previous => ({
        duration: 0,
        engine: modalEngine,
        providerId: modalProviderId,
        audioFile: null,
        voiceName: previous.voiceName,
      }));
    } catch {
      setModalSample(previous => ({
        duration: 0,
        engine: modalEngine,
        providerId: modalProviderId,
        audioFile: null,
        voiceName: previous.voiceName,
      }));
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current?.state === 'recording') {
      mediaRecorderRef.current.stop();
    }
    setRecording(false);
  };

  const onUpload = (file?: File) => {
    if (!file) return;
    setModalSample(previous => ({
      duration: Math.max(1, Math.round(file.size / 16000)),
      engine: modalEngine,
      providerId: modalProviderId,
      audioFile: file,
      voiceName:
        previous.voiceName?.trim() ||
        file.name.replace(/\.[^/.]+$/, '') ||
        file.name,
    }));
  };

  const confirmNewVoice = () => {
    const resolvedName = modalSample.voiceName?.trim();
    const selectedProvider = modalSample.engine;
    const selectedProviderId =
      modalSample.providerId || resolveProviderId(selectedProvider);
    if (!selectedProvider || !selectedProviderId) {
      toast.error('Select a voice provider');
      return;
    }
    if (!modalSample.audioFile || !modalSample.duration || !resolvedName)
      return;

    setVoice({
      ...modalSample,
      engine: selectedProvider,
      providerId: selectedProviderId,
      voiceId: undefined,
      voiceName: resolvedName,
    });
    onStop();
    setIsEditingSample(false);
    setModalOpen(false);
  };

  const selectVoice = (item: IDeepfakeVoice) => {
    applyVoiceSelection(item);
    onStop();
  };

  const handleDeleteVoice = async () => {
    if (!deleteConfirm) return;
    const voiceId = resolveVoiceId(deleteConfirm);
    if (!voiceId) return;

    setDeleting(true);
    const deleted = await deleteVoice(voiceId);
    setDeleting(false);
    setDeleteConfirm(null);

    if (!deleted) {
      toast.error('Failed to delete cloned voice');
      return;
    }

    stopPreview();
    if (voice.voiceId === voiceId) {
      const nextEngine = deleteConfirm.provider ?? voice.engine;
      setVoice({
        duration: 0,
        engine: nextEngine,
        providerId: resolveProviderId(nextEngine) || voice.providerId,
        audioFile: null,
      });
    }
    await fetchVoices({
      providerId:
        providerFilter === ALL_PROVIDERS_FILTER ? undefined : providerFilter,
      offset,
      pageSize: DEFAULT_PAGE_SIZE,
    });
    toast.success('Cloned voice deleted');
  };

  const selectedFilterProvider = voiceProviders.find(
    option => option.providerId === providerFilter,
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="space-y-2">
          <Label>Provider</Label>
          <Select
            value={providerFilter}
            onValueChange={handleProviderFilterChange}
          >
            <SelectTrigger className="w-[220px]">
              <SelectValue placeholder="Filter by provider" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL_PROVIDERS_FILTER}>
                All providers
              </SelectItem>
              {voiceProviders.map(option => (
                <SelectItem key={option.providerId} value={option.providerId}>
                  {resolveVoiceProviderLabel(option.providerName)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <Button onClick={openNewVoiceModal}>
          <Plus className="mr-1.5 size-4" /> New voice
        </Button>
      </div>

      {voicesLoading ? (
        <div className="flex items-center justify-center gap-2 rounded-xl border border-card-border p-10 text-sm text-muted-foreground">
          <Loader2 className="size-4 animate-spin" />
          Loading cloned voices…
        </div>
      ) : voices.items.length === 0 ? (
        <div className="rounded-xl border border-dashed border-card-border p-10 text-center">
          <p className="text-sm font-medium">No cloned voices yet</p>
          <p className="mt-1 text-xs text-muted-foreground">
            {providerFilter === ALL_PROVIDERS_FILTER
              ? 'Create a new voice sample to use in this video.'
              : `No voices found for ${resolveVoiceProviderLabel(
                  selectedFilterProvider?.providerName || 'this provider',
                )}.`}
          </p>
        </div>
      ) : (
        <>
          <div className="grid gap-3 sm:grid-cols-2">
            {voices.items.map(item => {
              const id = resolveVoiceId(item);
              const active = voice.voiceId === id && !voice.audioFile;
              const isPlaying = playingVoiceId === id;

              return (
                <div
                  key={id}
                  className={cn(
                    'relative rounded-xl border p-4 transition',
                    active
                      ? 'border-primary bg-primary/5 ring-1 ring-primary'
                      : 'border-card-border hover:border-primary/50',
                  )}
                >
                  {active ? (
                    <span className="absolute right-3 top-3 grid size-5 place-items-center rounded-full bg-primary text-primary-foreground">
                      <Check className="size-3" />
                    </span>
                  ) : null}
                  <button
                    type="button"
                    onClick={() => selectVoice(item)}
                    className="block w-full pr-10 text-left"
                  >
                    <p className="truncate text-sm font-medium">
                      {resolveVoiceName(item)}
                    </p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      {item.provider
                        ? resolveVoiceProviderLabel(item.provider)
                        : 'Unknown provider'}
                      {item.language ? ` · ${item.language}` : ''}
                      {item.status ? ` · ${item.status}` : ''}
                    </p>
                    {item.createdAt ? (
                      <p className="mt-1 text-[11px] text-muted-foreground">
                        {formatCreatedAt(item.createdAt)}
                      </p>
                    ) : null}
                  </button>
                  <div className="mt-3 flex items-center gap-2 border-t border-card-border pt-3">
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      className="h-8"
                      disabled={!item.sampleUrl}
                      onClick={() => togglePreview(item)}
                    >
                      {isPlaying ? (
                        <>
                          <Pause className="mr-1.5 size-3.5" /> Pause
                        </>
                      ) : (
                        <>
                          <Play className="mr-1.5 size-3.5" /> Play
                        </>
                      )}
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      className="ml-auto h-8 text-destructive hover:text-destructive"
                      aria-label={`Delete ${resolveVoiceName(item)}`}
                      onClick={() => setDeleteConfirm(item)}
                    >
                      <Trash2 className="size-3.5" />
                    </Button>
                  </div>
                </div>
              );
            })}
          </div>

          <div className="flex justify-end">
            <Pagination
              total={voices.total}
              perPage={DEFAULT_PAGE_SIZE}
              currentPage={currentPage}
              onPageChange={handlePageChange}
            />
          </div>
        </>
      )}

      {voice.audioFile ? (
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-primary/30 bg-primary/5 px-4 py-3 text-sm">
          <div>
            <p className="font-medium text-primary">New voice sample ready</p>
            <p className="text-xs text-muted-foreground">
              {voice.audioFile.name} · {voice.duration}s ·{' '}
              {resolveVoiceProviderLabel(voice.engine)}
              {voice.voiceName ? ` · ${voice.voiceName}` : ''} · uploads when
              you continue
            </p>
          </div>
          <div className="flex items-center gap-2">
            <Button size="sm" variant="outline" onClick={openEditSampleModal}>
              <Mic className="mr-1 size-3.5" /> Edit sample
            </Button>
            <Button size="sm" variant="ghost" onClick={discardPendingSample}>
              <Trash2 className="mr-1 size-3.5" /> Discard
            </Button>
          </div>
        </div>
      ) : null}

      <Dialog open={modalOpen} onOpenChange={closeNewVoiceModal}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>
              {isEditingSample ? 'Edit voice sample' : 'Add new voice'}
            </DialogTitle>
            <DialogDescription>
              {isEditingSample
                ? 'Re-record or replace the pending sample. It uploads when you continue.'
                : 'Choose a voice provider, then record the consent script or upload a pre-recorded sample to clone a new voice.'}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-6">
            <div className="grid gap-4 md:grid-cols-[1fr_220px]">
              <div className="rounded-xl border border-card-border p-4">
                <p className="text-xs uppercase tracking-wider text-muted-foreground">
                  Consent script
                </p>
                <p className="mt-2 text-sm italic text-foreground/90">
                  &quot;I consent to my voice and likeness being used to
                  generate synthetic media for the purposes of internal security
                  awareness training only.&quot;
                </p>
              </div>
              <div className="space-y-2">
                <Label>
                  Voice provider <span className="text-destructive">*</span>
                </Label>
                {providersLoading ? (
                  <div className="flex h-10 items-center gap-2 text-xs text-muted-foreground">
                    <Loader2 className="size-3.5 animate-spin" />
                    Loading providers…
                  </div>
                ) : voiceProviders.length === 0 ? (
                  <p className="text-xs text-muted-foreground">
                    No active voice cloning providers configured. Add one under
                    Provider Configuration.
                  </p>
                ) : (
                  <Select
                    value={modalEngine || undefined}
                    onValueChange={value => {
                      const matched = voiceProviders.find(
                        option => option.providerName === value,
                      );
                      const nextProviderId = matched?.providerId || '';
                      setModalEngine(value);
                      setModalProviderId(nextProviderId);
                      setModalSample(prev => ({
                        ...prev,
                        engine: value,
                        providerId: nextProviderId,
                      }));
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select provider" />
                    </SelectTrigger>
                    <SelectContent>
                      {voiceProviders.map(option => (
                        <SelectItem
                          key={option.providerId}
                          value={option.providerName}
                        >
                          {resolveVoiceProviderLabel(option.providerName)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="deepfake-voice-name">
                Voice name <span className="text-destructive">*</span>
              </Label>
              <Input
                id="deepfake-voice-name"
                value={modalSample.voiceName ?? ''}
                onChange={event =>
                  setModalSample(previous => ({
                    ...previous,
                    voiceName: event.target.value,
                  }))
                }
                placeholder="Enter a name for this voice"
                maxLength={100}
                disabled={recording}
              />
            </div>

            <div className="grid place-items-center gap-4 rounded-xl border border-card-border p-8">
              <div className="flex h-20 items-end gap-1">
                {Array.from({ length: 32 }).map((_, index) => {
                  const height = recording
                    ? 20 + Math.abs(Math.sin((elapsed * 10 + index) * 0.6)) * 60
                    : modalSample.duration
                      ? 10 + ((index * 13) % 60)
                      : 6;
                  return (
                    <span
                      key={index}
                      className={cn(
                        'w-1.5 rounded-full transition-all',
                        recording
                          ? 'bg-primary'
                          : modalSample.duration
                            ? 'bg-primary/70'
                            : 'bg-muted-foreground/30',
                      )}
                      style={{ height: `${height}%` }}
                    />
                  );
                })}
              </div>
              <div className="text-center">
                {recording ? (
                  <div className="font-mono text-sm text-primary">
                    ● Recording · {elapsed.toFixed(1)}s
                  </div>
                ) : modalSample.duration ? (
                  <div className="text-sm text-muted-foreground">
                    Sample captured · {modalSample.duration}s ·{' '}
                    {resolveVoiceProviderLabel(modalEngine)}
                  </div>
                ) : (
                  <div className="text-sm text-muted-foreground">
                    No sample yet
                  </div>
                )}
              </div>
              <div className="flex flex-wrap justify-center gap-2">
                {recording ? (
                  <Button onClick={stopRecording} variant="destructive">
                    <Square className="mr-1.5 size-4" /> Stop
                  </Button>
                ) : (
                  <>
                    <Button onClick={startRecording} disabled={!modalEngine}>
                      <Mic className="mr-1.5 size-4" />
                      {modalSample.duration ? 'Re-record' : 'Record'}
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => fileRef.current?.click()}
                      disabled={!modalEngine}
                    >
                      <Upload className="mr-1.5 size-4" /> Upload audio
                    </Button>
                    {modalSample.audioFile ? (
                      <Button variant="ghost" onClick={discardModalSample}>
                        <Trash2 className="mr-1 size-4" /> Discard
                      </Button>
                    ) : null}
                    <input
                      ref={fileRef}
                      type="file"
                      accept="audio/*"
                      hidden
                      onChange={event => onUpload(event.target.files?.[0])}
                    />
                  </>
                )}
              </div>
            </div>

            <div className="flex justify-end gap-2">
              <Button
                variant="secondary"
                onClick={() => closeNewVoiceModal(false)}
                disabled={recording}
              >
                Cancel
              </Button>
              <Button
                onClick={confirmNewVoice}
                disabled={
                  recording ||
                  !modalEngine ||
                  !modalProviderId ||
                  !modalSample.audioFile ||
                  !modalSample.voiceName?.trim()
                }
              >
                Use this voice
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        isOpen={!!deleteConfirm}
        onClose={() => !deleting && setDeleteConfirm(null)}
        onConfirm={() => void handleDeleteVoice()}
        message={`Are you sure you want to delete "${deleteConfirm ? resolveVoiceName(deleteConfirm) : 'this voice'}"? This action cannot be undone.`}
        loading={deleting}
        loadingText="Deleting..."
        buttonText="Delete"
      />
    </div>
  );
};

export default VoiceCloning;
