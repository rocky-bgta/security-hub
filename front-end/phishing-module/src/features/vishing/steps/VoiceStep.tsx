import { useRef, useState, useEffect } from 'react';
import {
  Check,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Mic,
  Play,
  Plus,
  RotateCcw,
  Search,
  Sparkles,
  Square,
  Trash2,
  Upload,
} from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Button } from 'components/common/Button';
import { Input } from 'components/common/Input';
import ConfirmDialog from 'components/ConfirmDialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import { cn } from 'utils/Helper';
import { toast } from 'react-toastify';
import { useVishingWizard } from '../context/VishingWizardContext';
import { useVoiceClone } from 'hooks/UseVoiceClone';
import useDebounce from 'hooks/UseDebounce';
import { VoiceCloneProvider } from 'models/Vishing';

type VoiceTag = 'New' | 'Popular' | 'Premium';
type VoiceProfile = {
  id: string;
  videoId?: string;
  name: string;
  role: string;
  gender: 'Male' | 'Female' | 'Neutral';
  tone: string;
  language: string;
  tags?: VoiceTag[];
  cloned?: boolean;
  sampleUrl?: string;
  provider?: string;
  status?: string;
};

const PROVIDER_LABELS: Record<string, string> = {
  ELEVENLABS: 'ElevenLabs',
  FISH_AUDIO: 'Fish Audio',
};

const PROVIDER_OPTIONS = [
  { value: 'ELEVENLABS', label: 'ElevenLabs' },
  { value: 'FISH_AUDIO', label: 'Fish Audio' },
] as const;

const STATUS_OPTIONS = [
  { value: 'DRAFT', label: 'Draft' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'PROCESSING', label: 'Processing' },
  { value: 'FAILED', label: 'Failed' },
] as const;

const LANGUAGE_OPTIONS = [{ value: 'en', label: 'English' }] as const;

const CONSENT_TEXT =
  'I consent to my voice and likeness being used to generate synthetic media for internal security awareness training only.';

const IdentityVoiceTab = () => {
  const { setVoiceSetup, voiceSetup } = useVishingWizard();
  const { listVoiceClones, deleteVoiceClone } = useVoiceClone();
  const [profiles, setProfiles] = useState<VoiceProfile[]>([]);
  const [selectedId, setSelectedId] = useState(
    () => voiceSetup.voiceCloneId ?? '',
  );
  const [voiceName, setVoiceName] = useState(() => voiceSetup.voiceName ?? '');
  const [newVoiceName, setNewVoiceName] = useState<string>('');
  const [recording, setRecording] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const [sampleSeconds, setSampleSeconds] = useState(
    () => voiceSetup.sampleDuration ?? 0,
  );
  const [audioFile, setAudioFile] = useState<File | null>(
    () => voiceSetup.audioFile ?? null,
  );
  const [engine, setEngine] = useState(
    () => voiceSetup.cloningEngine ?? 'ELEVENLABS',
  );
  const [addVoiceOpen, setAddVoiceOpen] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState<VoiceProfile | null>(null);
  const [deletingVoice, setDeletingVoice] = useState(false);
  const [voiceListRevision, setVoiceListRevision] = useState(0);

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const streamRef = useRef<MediaStream | null>(null);
  const elapsedRef = useRef(0);
  const previewAudioRef = useRef<HTMLAudioElement | null>(null);
  const hasAudioDraftRef = useRef(Boolean(voiceSetup.audioFile));
  const [playingPreviewId, setPlayingPreviewId] = useState<string | null>(null);

  // Library search & filters (server-side)
  const [query, setQuery] = useState('');
  const [filterProvider, setFilterProvider] = useState('all');
  const [filterLanguage, setFilterLanguage] = useState('all');
  const [filterStatus, setFilterStatus] = useState('all');
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const PAGE_SIZE = 12;
  const debouncedSearch = useDebounce(query.trim(), 400);

  const fileRef = useRef<HTMLInputElement>(null);

  const stopPreview = () => {
    const audio = previewAudioRef.current;
    if (audio) {
      audio.pause();
      audio.currentTime = 0;
      audio.onended = null;
      audio.onerror = null;
      previewAudioRef.current = null;
    }
    setPlayingPreviewId(null);
  };

  useEffect(() => {
    const load = async () => {
      setListLoading(true);
      try {
        const result = await listVoiceClones({
          offset: (page - 1) * PAGE_SIZE,
          pageSize: PAGE_SIZE,
          provider:
            filterProvider !== 'all'
              ? (filterProvider as VoiceCloneProvider)
              : undefined,
          language: filterLanguage !== 'all' ? filterLanguage : undefined,
          status: filterStatus !== 'all' ? filterStatus : undefined,
          search: debouncedSearch || undefined,
        });
        setProfiles(result.items);
        setTotal(result.total);
        setSelectedId(prev => {
          if (prev && result.items.some(item => item.id === prev)) {
            const match = result.items.find(item => item.id === prev);
            if (match?.name) {
              setVoiceName(current => current.trim() || match.name);
            }
            return prev;
          }
          if (hasAudioDraftRef.current) return prev || '';
          const first = result.items[0];
          if (first) {
            setVoiceName(current => current.trim() || first.name);
            return prev || first.id;
          }
          return prev || '';
        });
      } finally {
        setListLoading(false);
      }
    };

    void load();
  }, [
    listVoiceClones,
    page,
    filterProvider,
    filterLanguage,
    filterStatus,
    debouncedSearch,
    voiceListRevision,
  ]);

  useEffect(() => () => stopPreview(), []);

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

  const resolveAudioVoiceName = (file: File): string => {
    const base = file.name.replace(/\.[^/.]+$/, '') || file.name;
    return base.trim() || 'Cloned Voice';
  };

  const storeAudioSample = (file: File, durationSeconds: number) => {
    const isAudio =
      /audio\/(wav|mp3|mpeg|x-wav|webm)/.test(file.type) ||
      /\.(wav|mp3|mpeg|webm)$/i.test(file.name);

    if (!isAudio) {
      toast.error('Invalid format. Upload a WAV, MP3, or WEBM file.');
      return;
    }

    setAudioFile(file);
    hasAudioDraftRef.current = true;
    setSampleSeconds(durationSeconds);
    setSelectedId('');
    setNewVoiceName(current => current.trim() || resolveAudioVoiceName(file));
    toast.success(
      'Voice sample captured. It will be uploaded when you continue.',
    );
  };

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;
      const recorder = new MediaRecorder(stream);
      audioChunksRef.current = [];
      elapsedRef.current = 0;
      setElapsed(0);

      recorder.ondataavailable = event => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      recorder.onstop = () => {
        stream.getTracks().forEach(track => track.stop());
        streamRef.current = null;

        const durationSeconds = Math.max(1, Math.round(elapsedRef.current));
        const blob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
        const file = new File([blob], 'voice-sample.webm', {
          type: 'audio/webm',
        });
        setRecording(false);
        setElapsed(0);
        elapsedRef.current = 0;
        storeAudioSample(file, durationSeconds);
      };

      mediaRecorderRef.current = recorder;
      recorder.start(1000);
      setRecording(true);
      setSampleSeconds(0);
      setAudioFile(null);
    } catch {
      toast.error('Microphone access is required to record a voice sample.');
    }
  };

  const toggleRecord = () => {
    if (recording) {
      mediaRecorderRef.current?.stop();
      return;
    }

    void startRecording();
  };

  const openAddVoiceModal = () => {
    setNewVoiceName('');
    setAddVoiceOpen(true);
  };

  const closeAddVoiceModal = (open: boolean) => {
    if (!open) {
      if (recording) {
        mediaRecorderRef.current?.stop();
      }
      setAddVoiceOpen(false);
      return;
    }
    setAddVoiceOpen(true);
  };

  const handleProfileSelect = (profile: VoiceProfile) => {
    hasAudioDraftRef.current = false;
    setSelectedId(profile.id);
    setVoiceName(profile.name);
    setAudioFile(null);
    setSampleSeconds(0);
  };

  const handleProfilePreview = async (profile: VoiceProfile) => {
    if (playingPreviewId === profile.id) {
      stopPreview();
      return;
    }

    if (!profile.sampleUrl) {
      toast.info('No preview available for this voice.');
      return;
    }

    stopPreview();

    try {
      const audio = new Audio(profile.sampleUrl);
      previewAudioRef.current = audio;
      setPlayingPreviewId(profile.id);

      audio.onended = () => {
        previewAudioRef.current = null;
        setPlayingPreviewId(null);
      };
      audio.onerror = () => {
        previewAudioRef.current = null;
        setPlayingPreviewId(null);
        toast.error('Unable to play voice sample preview.');
      };

      await audio.play();
    } catch {
      previewAudioRef.current = null;
      setPlayingPreviewId(null);
      toast.error('Unable to play voice sample preview.');
    }
  };

  const deleteProfile = async () => {
    if (!deleteConfirm) return;

    setDeletingVoice(true);
    const deleted = await deleteVoiceClone(deleteConfirm.id);
    setDeletingVoice(false);
    setDeleteConfirm(null);

    if (!deleted) {
      toast.error('Failed to delete cloned voice.');
      return;
    }

    stopPreview();
    if (selectedId === deleteConfirm.id) {
      setSelectedId('');
      setVoiceName('');
      if (!audioFile) setSampleSeconds(0);
    }

    if (profiles.length === 1 && page > 1) {
      setPage(current => current - 1);
    } else {
      setVoiceListRevision(revision => revision + 1);
    }
    toast.success('Cloned voice deleted.');
  };

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const currentPage = Math.min(page, totalPages);
  const pageStart = total === 0 ? 0 : (currentPage - 1) * PAGE_SIZE;
  const pageEnd = Math.min(pageStart + profiles.length, total);

  useEffect(() => {
    setVoiceSetup({
      consentConfirmed: true,
      consentText: CONSENT_TEXT,
      voiceCloneId: selectedId,
      voiceName,
      cloningEngine: engine as VoiceCloneProvider,
      language: 'en',
      audioFile,
      sampleDuration: sampleSeconds,
    });
  }, [selectedId, voiceName, engine, audioFile, sampleSeconds, setVoiceSetup]);

  const activeFilters: { key: string; label: string; clear: () => void }[] = [];
  if (filterProvider !== 'all')
    activeFilters.push({
      key: 'provider',
      label: `Provider: ${PROVIDER_LABELS[filterProvider] ?? filterProvider}`,
      clear: () => {
        setFilterProvider('all');
        setPage(1);
      },
    });
  if (filterLanguage !== 'all')
    activeFilters.push({
      key: 'lang',
      label: `Language: ${
        LANGUAGE_OPTIONS.find(option => option.value === filterLanguage)
          ?.label ?? filterLanguage
      }`,
      clear: () => {
        setFilterLanguage('all');
        setPage(1);
      },
    });
  if (filterStatus !== 'all')
    activeFilters.push({
      key: 'status',
      label: `Status: ${filterStatus}`,
      clear: () => {
        setFilterStatus('all');
        setPage(1);
      },
    });
  if (query.trim())
    activeFilters.push({
      key: 'q',
      label: `"${query.trim()}"`,
      clear: () => {
        setQuery('');
        setPage(1);
      },
    });

  const resetFilters = () => {
    setQuery('');
    setFilterProvider('all');
    setFilterLanguage('all');
    setFilterStatus('all');
    setPage(1);
  };

  const highlight = (text: string) => {
    const q = query.trim();
    if (!q) return text;
    const i = text.toLowerCase().indexOf(q.toLowerCase());
    if (i === -1) return text;
    return (
      <>
        {text.slice(0, i)}
        <mark className="rounded bg-primary/30 px-0.5 text-primary-foreground">
          {text.slice(i, i + q.length)}
        </mark>
        {text.slice(i + q.length)}
      </>
    );
  };

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2 text-xs uppercase tracking-[0.18em] text-primary">
        <Mic className="size-3.5" /> Step 1 · Voice — Clone Your Voice
      </div>

      <div className="grid gap-6">
        {audioFile && (
          <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#00FFA3]/30 bg-[#00FFA3]/10 px-4 py-3 text-sm">
            <div>
              <p className="font-medium text-[#00FFA3]">
                New voice sample ready
              </p>
              <p className="text-xs text-muted-foreground">
                {audioFile.name} · {sampleSeconds}s ·{' '}
                {PROVIDER_LABELS[engine] ?? engine} · uploads when you continue
              </p>
            </div>
            <div className="flex items-center gap-2">
              <Button size="sm" variant="outline" onClick={openAddVoiceModal}>
                <Mic className="mr-1 size-3.5" /> Edit sample
              </Button>
              <Button
                size="sm"
                variant="ghost"
                onClick={() => {
                  setSampleSeconds(0);
                  setAudioFile(null);
                  toast.success('Sample cleared.');
                }}
              >
                <Trash2 className="mr-1 size-3.5" /> Discard
              </Button>
            </div>
          </div>
        )}

        {/* AI Voice Library — full-width modern dark glass */}
        <Card
          className="relative overflow-hidden border-white/10 shadow-[0_8px_40px_-12px_rgba(0,255,163,0.15)] backdrop-blur-xl"
          style={{
            backgroundImage:
              'radial-gradient(circle 12rem at 0% 0%, rgba(0,255,163,0.12), transparent), radial-gradient(circle 12rem at 100% 100%, rgba(99,102,241,0.12), transparent)',
          }}
        >
          <CardHeader className="relative">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
              <div className="w-full lg:w-3/12">
                <CardTitle className="flex items-center gap-2 text-xl tracking-tight">
                  <span className="grid size-8 place-items-center rounded-lg bg-[#00FFA3]/15 ring-1 ring-[#00FFA3]/30">
                    <Sparkles className="size-4 text-[#00FFA3]" />
                  </span>
                  AI Voice Library
                </CardTitle>
                <CardDescription className="mt-1">
                  Manage cloned voices used in simulations.
                </CardDescription>
              </div>

              <div className="flex w-full items-center justify-end gap-2 lg:w-9/12">
                <div className="relative">
                  <Search className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    value={query}
                    onChange={e => {
                      setQuery(e.target.value);
                      setPage(1);
                    }}
                    placeholder="Search voices..."
                    className="h-9 w-56 rounded-lg border-white/10 bg-white/5 pl-8 text-sm placeholder:text-muted-foreground/70 focus-visible:ring-[#00FFA3]/40"
                  />
                </div>

                <Select
                  value={filterProvider}
                  onValueChange={v => {
                    setFilterProvider(v);
                    setPage(1);
                  }}
                >
                  <SelectTrigger className="h-9 w-36 rounded-lg border-white/10 bg-white/5 text-xs">
                    <SelectValue placeholder="Provider" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All providers</SelectItem>
                    {PROVIDER_OPTIONS.map(option => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <Select
                  value={filterStatus}
                  onValueChange={v => {
                    setFilterStatus(v);
                    setPage(1);
                  }}
                >
                  <SelectTrigger className="h-9 w-32 rounded-lg border-white/10 bg-white/5 text-xs">
                    <SelectValue placeholder="Status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All status</SelectItem>
                    {STATUS_OPTIONS.map(option => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <Button
                  onClick={openAddVoiceModal}
                  className="h-9 rounded-lg bg-[#00FFA3] text-black shadow-[0_0_24px_-4px_rgba(0,255,163,0.6)] hover:bg-[#00FFA3]/90"
                >
                  <Plus className="mr-1 size-4" /> Add Voice
                </Button>
              </div>
            </div>

            {/* Active filter chips */}
            {activeFilters.length > 0 && (
              <div className="mt-3 flex flex-wrap items-center gap-2">
                <span className="text-[11px] uppercase tracking-wider text-muted-foreground">
                  Active:
                </span>
                {activeFilters.map(f => (
                  <button
                    key={f.key}
                    onClick={f.clear}
                    className="group inline-flex items-center gap-1 rounded-full border border-[#00FFA3]/30 bg-[#00FFA3]/10 px-2.5 py-0.5 text-[11px] text-[#00FFA3] transition hover:bg-[#00FFA3]/20"
                  >
                    {f.label}
                    <Trash2 className="size-3 opacity-60 group-hover:opacity-100" />
                  </button>
                ))}
                <Button
                  size="sm"
                  variant="ghost"
                  className="h-6 px-2 text-[11px] text-muted-foreground hover:text-foreground"
                  onClick={resetFilters}
                >
                  <RotateCcw className="mr-1 size-3" /> Reset filters
                </Button>
              </div>
            )}
          </CardHeader>

          <CardContent className="relative">
            <div className="mb-3 flex items-center justify-between text-[11px] text-muted-foreground">
              <span>
                {listLoading
                  ? 'Loading voices...'
                  : `Showing ${total === 0 ? 0 : pageStart + 1}–${pageEnd} of ${total}`}
              </span>
              <span className="hidden sm:inline">
                Page {currentPage} / {totalPages}
              </span>
            </div>

            {profiles.length === 0 ? (
              <div className="rounded-xl border border-dashed border-white/10 bg-white/5 p-10 text-center">
                <Sparkles className="mx-auto mb-2 size-6 text-muted-foreground/60" />
                <div className="text-sm font-medium">
                  {listLoading ? 'Loading voices...' : 'No voices found'}
                </div>
                <div className="mt-1 text-xs text-muted-foreground">
                  Try clearing filters or searching another keyword.
                </div>
                <Button
                  size="sm"
                  variant="ghost"
                  className="mt-3"
                  onClick={resetFilters}
                >
                  <RotateCcw className="mr-1 size-3" /> Reset filters
                </Button>
              </div>
            ) : (
              <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                {profiles.map(p => {
                  const active = p.id === selectedId;
                  return (
                    <div
                      key={p.id}
                      className={cn(
                        'group relative overflow-hidden rounded-xl border p-4 transition-all duration-200 ease-out backdrop-blur-md',
                        'hover:-translate-y-0.5 hover:shadow-[0_10px_30px_-10px_rgba(0,255,163,0.35)]',
                        active
                          ? 'border-[#00FFA3]/60 bg-[#00FFA3]/[0.06] shadow-[0_0_0_1px_rgba(0,255,163,0.4),0_0_24px_-4px_rgba(0,255,163,0.45)]'
                          : 'border-white/10 bg-white/[0.03] hover:border-[#00FFA3]/40',
                      )}
                    >
                      {active && (
                        <span className="absolute right-3 top-3 grid size-5 place-items-center rounded-full bg-[#00FFA3] text-black">
                          <Check className="size-3" />
                        </span>
                      )}

                      <button
                        onClick={() => handleProfileSelect(p)}
                        className="block w-full text-left"
                      >
                        <div className="flex items-center gap-2 pr-6">
                          <span className="grid size-9 place-items-center rounded-full bg-gradient-to-br from-[#00FFA3]/30 to-indigo-500/30 text-sm font-semibold text-foreground ring-1 ring-white/10">
                            {p.name.charAt(0)}
                          </span>
                          <div className="min-w-0">
                            <div className="truncate text-sm font-semibold">
                              {highlight(p.name)}
                            </div>
                            <div className="mt-0.5 truncate text-[11px] text-muted-foreground">
                              {highlight(
                                PROVIDER_LABELS[p.provider ?? ''] ??
                                  p.provider ??
                                  p.role,
                              )}{' '}
                              · {highlight(p.language)}
                              {p.status ? ` · ${p.status}` : ''}
                            </div>
                          </div>
                        </div>

                        {p.tags && p.tags.length > 0 && (
                          <div className="mt-3 flex flex-wrap gap-1">
                            {p.tags.map(t => (
                              <span
                                key={t}
                                className={cn(
                                  'rounded-full px-2 py-0.5 text-[10px] font-medium ring-1',
                                  t === 'New' &&
                                    'bg-[#00FFA3]/15 text-[#00FFA3] ring-[#00FFA3]/30',
                                  t === 'Popular' &&
                                    'bg-amber-400/15 text-amber-300 ring-amber-300/30',
                                  t === 'Premium' &&
                                    'bg-fuchsia-400/15 text-fuchsia-300 ring-fuchsia-300/30',
                                )}
                              >
                                {t}
                              </span>
                            ))}
                          </div>
                        )}
                      </button>

                      <div className="mt-3 flex items-center gap-1 border-t border-white/5 pt-2">
                        <Button
                          size="sm"
                          variant="ghost"
                          className="h-7 px-2 text-xs hover:text-[#00FFA3]"
                          onClick={() => void handleProfilePreview(p)}
                          disabled={!p.sampleUrl}
                        >
                          {playingPreviewId === p.id ? (
                            <>
                              <Square className="mr-1 size-3" /> Stop
                            </>
                          ) : (
                            <>
                              <Play className="mr-1 size-3" /> Preview
                            </>
                          )}
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          className="ml-auto h-7 px-2 text-destructive hover:text-destructive"
                          aria-label={`Delete ${p.name}`}
                          onClick={() => setDeleteConfirm(p)}
                        >
                          <Trash2 className="size-3" />
                        </Button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-5 flex items-center justify-center gap-1">
                <Button
                  size="sm"
                  variant="ghost"
                  disabled={currentPage === 1}
                  onClick={() => setPage(p => Math.max(1, p - 1))}
                  className="h-8 rounded-lg border border-white/10 bg-white/5 px-3 text-xs disabled:opacity-40"
                >
                  <ChevronLeft className="mr-1 size-3" /> Previous
                </Button>
                {Array.from({ length: totalPages }, (_, i) => i + 1).map(n => (
                  <button
                    key={n}
                    onClick={() => setPage(n)}
                    className={cn(
                      'h-8 min-w-8 rounded-lg border px-2.5 text-xs transition',
                      n === currentPage
                        ? 'border-[#00FFA3]/60 bg-[#00FFA3]/15 text-[#00FFA3] shadow-[0_0_12px_-2px_rgba(0,255,163,0.5)]'
                        : 'border-white/10 bg-white/5 text-muted-foreground hover:border-[#00FFA3]/30 hover:text-foreground',
                    )}
                  >
                    {n}
                  </button>
                ))}
                <Button
                  size="sm"
                  variant="ghost"
                  disabled={currentPage === totalPages}
                  onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                  className="h-8 rounded-lg border border-white/10 bg-white/5 px-3 text-xs disabled:opacity-40"
                >
                  Next <ChevronRight className="ml-1 size-3" />
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <Dialog open={addVoiceOpen} onOpenChange={closeAddVoiceModal}>
        <DialogContent className="max-w-xl p-0">
          <div className="border-b px-6 py-5">
            <DialogHeader>
              <div className="flex items-center justify-between gap-3 pr-6">
                <div>
                  <DialogTitle className="flex items-center gap-2 text-lg">
                    <Mic className="size-5 text-primary" />
                    Add Voice
                  </DialogTitle>
                  <DialogDescription>
                    Record or upload a voice sample for cloning.
                  </DialogDescription>
                </div>
                <Select
                  value={engine}
                  onValueChange={value =>
                    setEngine(value as VoiceCloneProvider)
                  }
                >
                  <SelectTrigger className="h-8 w-44 text-xs">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {PROVIDER_OPTIONS.map(option => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </DialogHeader>
          </div>

          <div className="space-y-4 px-6 py-5">
            <div className="space-y-2">
              <label
                htmlFor="vishing-voice-name"
                className="text-sm font-medium"
              >
                Voice name <span className="text-destructive">*</span>
              </label>
              <Input
                id="vishing-voice-name"
                value={newVoiceName}
                onChange={event => setNewVoiceName(event.target.value)}
                placeholder="Enter a name for this voice"
                maxLength={100}
                disabled={recording}
              />
            </div>

            <div className="space-y-3 rounded-xl border border-primary/20 bg-primary/5 p-4">
              <p className="text-sm font-medium">Consent script</p>
              <p className="text-sm font-medium italic">{CONSENT_TEXT}</p>
            </div>

            <div className="grid place-items-center gap-4 rounded-xl border border-card-border p-8">
              <div className="flex h-20 items-end gap-1">
                {Array.from({ length: 32 }).map((_, index) => {
                  const height = recording
                    ? 20 + Math.abs(Math.sin((elapsed * 10 + index) * 0.6)) * 60
                    : audioFile
                      ? 10 + ((index * 13) % 60)
                      : 6;
                  return (
                    <span
                      key={index}
                      className={cn(
                        'w-1.5 rounded-full transition-all',
                        recording
                          ? 'bg-primary'
                          : audioFile
                            ? 'bg-[#9d68ff]/70'
                            : 'bg-white/30',
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
                ) : audioFile ? (
                  <div className="text-sm text-muted-foreground">
                    Sample captured · {sampleSeconds}s ·{' '}
                    {PROVIDER_LABELS[engine] ?? engine} · ready to upload on
                    continue
                  </div>
                ) : (
                  <div className="text-sm text-muted-foreground">
                    No sample yet
                  </div>
                )}
              </div>

              <div className="flex flex-wrap justify-center gap-2">
                {recording ? (
                  <Button onClick={toggleRecord} variant="destructive">
                    <Square className="mr-1.5 size-4" /> Stop
                  </Button>
                ) : (
                  <>
                    <Button onClick={toggleRecord}>
                      <Mic className="mr-1.5 size-4" />
                      {audioFile ? 'Re-record' : 'Record'}
                    </Button>
                    <input
                      ref={fileRef}
                      type="file"
                      accept="audio/wav,audio/mp3,audio/mpeg,audio/webm,.wav,.mp3,.webm"
                      className="hidden"
                      onChange={e => {
                        const f = e.target.files?.[0];
                        if (f) {
                          storeAudioSample(
                            f,
                            Math.max(1, Math.round(f.size / 32000)),
                          );
                        }
                        e.target.value = '';
                      }}
                    />
                    <Button
                      variant="outline"
                      onClick={() => fileRef.current?.click()}
                    >
                      <Upload className="mr-1.5 size-4" /> Upload audio
                    </Button>
                    {audioFile && (
                      <Button
                        variant="ghost"
                        onClick={() => {
                          setSampleSeconds(0);
                          setAudioFile(null);
                          toast.success('Sample cleared.');
                        }}
                      >
                        <Trash2 className="mr-1 size-4" /> Discard
                      </Button>
                    )}
                  </>
                )}
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 border-t bg-muted/30 px-6 py-4">
            <Button
              variant="ghost"
              onClick={() => closeAddVoiceModal(false)}
              disabled={recording}
            >
              Cancel
            </Button>
            <Button
              onClick={() => {
                setVoiceName(newVoiceName.trim());
                closeAddVoiceModal(false);
              }}
              disabled={recording || !audioFile || !newVoiceName.trim()}
            >
              <CheckCircle2 className="mr-1 size-4" /> Done
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        isOpen={!!deleteConfirm}
        onClose={() => !deletingVoice && setDeleteConfirm(null)}
        onConfirm={() => void deleteProfile()}
        message={`Are you sure you want to delete "${deleteConfirm?.name || 'this voice'}"? This action cannot be undone.`}
        loading={deletingVoice}
        loadingText="Deleting..."
        buttonText="Delete"
      />
    </div>
  );
};

export default IdentityVoiceTab;
