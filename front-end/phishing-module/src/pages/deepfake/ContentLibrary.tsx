import { Download, Loader2, Pencil, Play, Plus, Trash2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import { Link } from 'react-router-dom';

import { Card } from 'components/common/Card';
import { Button } from 'components/common/Button';
import { Badge } from 'components/common/Badge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import ConfirmDialog from 'components/ConfirmDialog';
import Pagination from 'common/Pagination';
import { routes } from 'routes/Routes';
import { useDeepfake } from 'hooks/UseDeepfake';
import { IDeepfakeVideo, UploadToContentStatus } from 'models/Deepfake';

const resolveVideoId = (video: IDeepfakeVideo): string =>
  video.videoId ?? video.id ?? '';

const resolveThumbnail = (video: IDeepfakeVideo): string =>
  video.thumbnailUrl ??
  'linear-gradient(135deg, oklch(0.35 0.04 260), oklch(0.22 0.03 265))';

const LIBRARY_FETCH_SIZE = 500;
const SECTION_PAGE_SIZE = 20;
const POLLING_INTERVAL_MS = 10_000;

const resolveCreatedAt = (video: IDeepfakeVideo): string => {
  const raw = video.createdAt ?? video.uploadDate;
  if (!raw) return '—';
  const parsed = Date.parse(raw);
  return Number.isNaN(parsed) ? raw : new Date(parsed).toLocaleString();
};

const editDeepFakePath = (id: string) =>
  routes.deepfakeEdit.path.replace(':id', encodeURIComponent(id));

const normalizeStatus = (status?: string): string =>
  (status || 'DRAFT').toUpperCase();

const isVideoComplete = (video: IDeepfakeVideo): boolean =>
  normalizeStatus(video.status) === 'COMPLETED';

const isProcessingStatus = (status?: string): boolean => {
  const normalized = normalizeStatus(status);
  return normalized === 'PROCESSING' || normalized === 'PENDING';
};

type ContentButtonState = 'hidden' | 'add' | 'processing' | 'remove';

const normalizeContentAddStatus = (status?: string): string =>
  (status || UploadToContentStatus.PENDING).toUpperCase();

const isContentAddProcessing = (status?: string): boolean =>
  normalizeContentAddStatus(status) === UploadToContentStatus.PROCESSING;

const resolveContentButtonState = (
  status?: string,
  isBusy = false,
): ContentButtonState => {
  const normalized = normalizeContentAddStatus(status);

  if (normalized === UploadToContentStatus.DELETED) {
    return 'hidden';
  }

  if (
    normalized === UploadToContentStatus.PROCESSING ||
    (isBusy && normalized === UploadToContentStatus.PENDING)
  ) {
    return 'processing';
  }

  if (normalized === UploadToContentStatus.PENDING) {
    return 'add';
  }

  if (normalized === UploadToContentStatus.DONE) {
    return 'remove';
  }

  return 'hidden';
};

const STATUS_BADGE_CONFIG: Record<
  string,
  {
    label: string;
    variant: 'default' | 'secondary' | 'destructive' | 'warning' | 'outline';
  }
> = {
  COMPLETED: { label: 'Completed', variant: 'default' },
  PROCESSING: { label: 'Processing', variant: 'warning' },
  PENDING: { label: 'Pending', variant: 'warning' },
  FAILED: { label: 'Failed', variant: 'destructive' },
  DRAFT: { label: 'Draft', variant: 'secondary' },
};

const resolveStatusBadge = (status?: string) => {
  const normalized = normalizeStatus(status);
  return (
    STATUS_BADGE_CONFIG[normalized] || {
      label: normalized
        .replace(/_/g, ' ')
        .replace(/\b\w/g, char => char.toUpperCase()),
      variant: 'outline' as const,
    }
  );
};

type LibrarySectionKey = 'processing' | 'library';

const LIBRARY_SECTIONS: {
  key: LibrarySectionKey;
  title: string;
  emptyMessage: string;
}[] = [
  {
    key: 'processing',
    title: 'Processing',
    emptyMessage: 'No videos are currently pending or processing.',
  },
  {
    key: 'library',
    title: 'All videos',
    emptyMessage: 'No other videos yet.',
  },
];

interface VideoCardProps {
  video: IDeepfakeVideo;
  contentActionId: string | null;
  onPlay: (video: IDeepfakeVideo) => void;
  onDownload: (video: IDeepfakeVideo) => void;
  onDelete: (video: IDeepfakeVideo) => void;
  onAddAsContent: (video: IDeepfakeVideo) => void;
  onRemoveAsContent: (video: IDeepfakeVideo) => void;
}

const VideoCard = ({
  video,
  contentActionId,
  onPlay,
  onDownload,
  onDelete,
  onAddAsContent,
  onRemoveAsContent,
}: VideoCardProps) => {
  const id = resolveVideoId(video);
  const thumbnail = resolveThumbnail(video);
  const isBusy = contentActionId === id;
  const contentButtonState = resolveContentButtonState(
    video.uploadToContent,
    isBusy,
  );
  const status = normalizeStatus(video.status);
  const statusBadge = resolveStatusBadge(status);
  const complete = isVideoComplete(video);

  return (
    <div className="overflow-hidden rounded-xl border border-card-border">
      <div className="group relative aspect-video w-full overflow-hidden">
        <div
          className="absolute inset-0 bg-cover bg-center transition duration-300 group-hover:scale-105 group-hover:blur-sm"
          style={{
            background: thumbnail.startsWith('linear-gradient')
              ? thumbnail
              : `center / cover no-repeat url(${thumbnail})`,
          }}
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background/90 via-background/10 to-transparent transition duration-300 group-hover:bg-background/20" />

        {contentButtonState !== 'hidden' ? (
          <Button
            size="sm"
            variant={
              contentButtonState === 'processing'
                ? 'secondary'
                : contentButtonState === 'add'
                  ? 'default'
                  : 'destructive'
            }
            className="absolute left-3 top-3 z-20 h-8 px-2 text-xs shadow-md disabled:opacity-50"
            disabled={
              contentButtonState === 'processing' ||
              (contentButtonState === 'add' && !complete) ||
              (contentButtonState === 'remove' && isBusy)
            }
            onClick={event => {
              event.stopPropagation();
              if (contentButtonState === 'processing') return;
              if (contentButtonState === 'add') {
                void onAddAsContent(video);
                return;
              }
              void onRemoveAsContent(video);
            }}
          >
            {contentButtonState === 'processing' ? (
              <>
                <Loader2 className="size-3.5 animate-spin" />
                Processing to add
              </>
            ) : contentButtonState === 'add' ? (
              'Add Content'
            ) : (
              <>
                {isBusy ? <Loader2 className="size-3.5 animate-spin" /> : null}
                Remove Content
              </>
            )}
          </Button>
        ) : null}
        {complete ? (
          <button
            type="button"
            aria-label={`Play ${video.title || 'video'}`}
            className="absolute inset-0 z-10 grid place-items-center"
            onClick={() => onPlay(video)}
          >
            <span className="grid size-12 place-items-center rounded-full bg-background/80 text-foreground shadow-md backdrop-blur transition group-hover:scale-105 group-hover:bg-background">
              <Play className="size-5 fill-current" />
            </span>
          </button>
        ) : (
          <div className="absolute inset-0 z-10 grid place-items-center">
            <span className="rounded-md bg-background/80 px-3 py-1.5 text-xs font-medium text-muted-foreground backdrop-blur">
              {status === 'FAILED' ? 'Generation failed' : 'Not ready to play'}
            </span>
          </div>
        )}
      </div>
      <div className="space-y-3 p-4">
        <div className="space-y-2">
          <h3 className="truncate font-medium">
            {video.title || 'Untitled video'}
          </h3>
          <Badge variant={statusBadge.variant}>{statusBadge.label}</Badge>
          <p className="line-clamp-2 text-xs text-muted-foreground">
            {video.description || video.script}
          </p>
        </div>
        <div className="flex flex-wrap gap-1.5 text-[10px]">
          {video.provider ? (
            <Badge variant="outline">{video.provider}</Badge>
          ) : null}
          {video.model ? <Badge variant="outline">{video.model}</Badge> : null}
          {video.language ? (
            <Badge variant="outline">{video.language}</Badge>
          ) : null}
        </div>
        <div className="flex items-center justify-between gap-2 pt-1">
          <span className="text-xs text-muted-foreground">
            {resolveCreatedAt(video)}
          </span>
          <div className="flex items-center gap-0.5">
            <Button
              size="icon"
              variant="ghost"
              className="size-8"
              aria-label={`Edit ${video.title || 'video'}`}
              asChild
            >
              <Link to={editDeepFakePath(id)}>
                <Pencil className="size-4" />
              </Link>
            </Button>
            <Button
              size="icon"
              variant="ghost"
              className="size-8"
              aria-label={`Download ${video.title || 'video'}`}
              disabled={!complete || !video.videoUrl}
              onClick={() => onDownload(video)}
            >
              <Download className="size-4" />
            </Button>
            <Button
              size="icon"
              variant="ghost"
              className="size-8 text-destructive hover:text-destructive"
              aria-label={`Delete ${video.title || 'video'}`}
              onClick={() => onDelete(video)}
            >
              <Trash2 className="size-4" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

interface LibrarySectionProps {
  title: string;
  emptyMessage: string;
  videos: IDeepfakeVideo[];
  currentPage: number;
  onPageChange: (page: number) => void;
  polling?: boolean;
  contentActionId: string | null;
  onPlay: (video: IDeepfakeVideo) => void;
  onDownload: (video: IDeepfakeVideo) => void;
  onDelete: (video: IDeepfakeVideo) => void;
  onAddAsContent: (video: IDeepfakeVideo) => void;
  onRemoveAsContent: (video: IDeepfakeVideo) => void;
}

const LibrarySection = ({
  title,
  emptyMessage,
  videos,
  currentPage,
  onPageChange,
  polling,
  contentActionId,
  onPlay,
  onDownload,
  onDelete,
  onAddAsContent,
  onRemoveAsContent,
}: LibrarySectionProps) => (
  <section className="space-y-4">
    <div className="flex items-center gap-2">
      {polling ? (
        <Loader2 className="size-4 animate-spin text-primary" />
      ) : null}
      <h2 className="text-lg font-semibold">{title}</h2>
      <span className="text-sm text-muted-foreground">({videos.length})</span>
    </div>
    {videos.length === 0 ? (
      <div className="rounded-xl border border-dashed border-card-border p-8 text-sm text-muted-foreground">
        {emptyMessage}
      </div>
    ) : (
      <>
        <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
          {videos
            .slice(
              (currentPage - 1) * SECTION_PAGE_SIZE,
              currentPage * SECTION_PAGE_SIZE,
            )
            .map(video => (
              <VideoCard
                key={resolveVideoId(video)}
                video={video}
                contentActionId={contentActionId}
                onPlay={onPlay}
                onDownload={onDownload}
                onDelete={onDelete}
                onAddAsContent={onAddAsContent}
                onRemoveAsContent={onRemoveAsContent}
              />
            ))}
        </div>

        {videos.length > SECTION_PAGE_SIZE ? (
          <div className="flex justify-end">
            <Pagination
              total={videos.length}
              perPage={SECTION_PAGE_SIZE}
              currentPage={currentPage}
              onPageChange={onPageChange}
            />
          </div>
        ) : null}
      </>
    )}
  </section>
);

const ContentLibrary = () => {
  const {
    videos,
    fetchVideos,
    deleteVideo,
    addAsMicroContent,
    removeMicroContent,
    loading,
  } = useDeepfake();
  const [playingVideo, setPlayingVideo] = useState<IDeepfakeVideo | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<IDeepfakeVideo | null>(
    null,
  );
  const [deleting, setDeleting] = useState(false);
  const [contentActionId, setContentActionId] = useState<string | null>(null);
  const [sectionPages, setSectionPages] = useState<
    Record<LibrarySectionKey, number>
  >({
    processing: 1,
    library: 1,
  });

  const listParams = useMemo(
    () => ({
      offset: 0,
      pageSize: LIBRARY_FETCH_SIZE,
    }),
    [],
  );

  const sections = useMemo(() => {
    const grouped: Record<LibrarySectionKey, IDeepfakeVideo[]> = {
      processing: [],
      library: [],
    };

    for (const video of videos.items) {
      if (isProcessingStatus(video.status)) {
        grouped.processing.push(video);
      } else {
        grouped.library.push(video);
      }
    }

    return grouped;
  }, [videos.items]);

  const hasProcessing = sections.processing.length > 0;
  const hasContentAddProcessing = videos.items.some(video =>
    isContentAddProcessing(video.uploadToContent),
  );
  const shouldPoll = hasProcessing || hasContentAddProcessing;

  const getSectionPage = (key: LibrarySectionKey): number => {
    const totalPages = Math.max(
      1,
      Math.ceil(sections[key].length / SECTION_PAGE_SIZE),
    );
    return Math.min(sectionPages[key], totalPages);
  };

  const handleSectionPageChange = (key: LibrarySectionKey, page: number) => {
    setSectionPages(prev => ({ ...prev, [key]: page }));
  };

  useEffect(() => {
    void fetchVideos(listParams);
  }, [fetchVideos, listParams]);

  useEffect(() => {
    if (!shouldPoll) return;

    let requestInProgress = false;
    const pollingInterval = window.setInterval(() => {
      if (requestInProgress) return;
      requestInProgress = true;
      void fetchVideos(listParams, { silent: true }).finally(() => {
        requestInProgress = false;
      });
    }, POLLING_INTERVAL_MS);

    return () => window.clearInterval(pollingInterval);
  }, [fetchVideos, shouldPoll, listParams]);

  const handlePlay = (video: IDeepfakeVideo) => {
    if (!isVideoComplete(video)) {
      toast.info('Video is not ready to play yet');
      return;
    }
    if (!video.videoUrl) {
      toast.info('Video file is not available yet');
      return;
    }
    setPlayingVideo(video);
  };

  const handleDownload = (video: IDeepfakeVideo) => {
    if (!isVideoComplete(video) || !video.videoUrl) {
      toast.info('Video file is not available yet');
      return;
    }
    window.open(video.videoUrl, '_blank', 'noopener,noreferrer');
    toast.success('Opening video download');
  };

  const handleDelete = async (video: IDeepfakeVideo) => {
    const videoId = resolveVideoId(video);
    if (!videoId) {
      toast.error('Unable to delete video: missing id');
      setDeleteConfirm(null);
      return;
    }

    setDeleting(true);
    const success = await deleteVideo(videoId);
    setDeleting(false);
    setDeleteConfirm(null);

    if (success) {
      toast.success('Video deleted successfully');
      await fetchVideos(listParams, { silent: true });
      return;
    }

    toast.error('Failed to delete video');
  };

  const handleAddAsContent = async (video: IDeepfakeVideo) => {
    const videoId = resolveVideoId(video);
    if (!videoId) {
      toast.error('Unable to add as content: missing id');
      return;
    }

    if (!isVideoComplete(video)) {
      toast.info('Only completed videos can be added as content');
      return;
    }

    setContentActionId(videoId);
    const result = await addAsMicroContent([videoId]);
    setContentActionId(null);

    if (!result.ok) {
      toast.error(result.message);
      return;
    }

    toast.success(result.message);
    await fetchVideos(listParams, { silent: true });
  };

  const handleRemoveAsContent = async (video: IDeepfakeVideo) => {
    const videoId = resolveVideoId(video);
    if (!videoId) {
      toast.error('Unable to remove content: missing id');
      return;
    }

    setContentActionId(videoId);
    const result = await removeMicroContent(videoId);
    setContentActionId(null);

    if (!result.ok) {
      toast.error(result.message);
      return;
    }

    toast.success(result.message);
    await fetchVideos(listParams, { silent: true });
  };

  return (
    <div className="w-full space-y-8">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-xs uppercase text-muted-foreground">
            Phishing Simulation
          </p>
          <h1 className="mt-1 text-3xl font-semibold">Content Library</h1>
          <p className="mt-2 text-sm text-muted-foreground">
            Awareness videos generated by your team. Use these in training
            modules to teach employees how to spot synthetic media.
          </p>
        </div>
        <Button asChild>
          <Link to={routes.deepfakeCreate.path}>Generate deepfake video</Link>
        </Button>
      </header>

      {loading ? (
        <Card className="flex items-center justify-center gap-2 p-16 text-muted-foreground">
          <Loader2 className="size-5 animate-spin" />
          Loading library…
        </Card>
      ) : videos.total === 0 ? (
        <Card className="flex flex-col items-center justify-center gap-3 p-16 text-center">
          <div className="grid size-12 place-items-center rounded-full bg-primary/10 text-primary">
            <Plus />
          </div>
          <h3 className="text-lg font-semibold">Library is empty</h3>
          <p className="max-w-sm text-sm text-muted-foreground">
            Generate your first deepfake training video to populate the library.
          </p>
          <Button asChild>
            <Link to={routes.deepfakeCreate.path}>Generate deepfake video</Link>
          </Button>
        </Card>
      ) : (
        <div className="space-y-10">
          {LIBRARY_SECTIONS.map(section => (
            <LibrarySection
              key={section.key}
              title={section.title}
              emptyMessage={section.emptyMessage}
              videos={sections[section.key]}
              currentPage={getSectionPage(section.key)}
              onPageChange={page => handleSectionPageChange(section.key, page)}
              polling={
                (section.key === 'processing' && hasProcessing) ||
                (section.key === 'library' && hasContentAddProcessing)
              }
              contentActionId={contentActionId}
              onPlay={handlePlay}
              onDownload={handleDownload}
              onDelete={setDeleteConfirm}
              onAddAsContent={handleAddAsContent}
              onRemoveAsContent={handleRemoveAsContent}
            />
          ))}
        </div>
      )}

      <Dialog
        open={!!playingVideo}
        onOpenChange={open => !open && setPlayingVideo(null)}
      >
        <DialogContent className="w-[95vw] max-w-6xl overflow-hidden p-0 sm:rounded-lg">
          <DialogHeader className="px-6 pr-12 pt-6">
            <DialogTitle>{playingVideo?.title || 'Untitled video'}</DialogTitle>
          </DialogHeader>
          {playingVideo?.videoUrl ? (
            <video
              key={playingVideo.videoUrl}
              src={playingVideo.videoUrl}
              controls
              autoPlay
              className="aspect-video w-full bg-black"
            />
          ) : null}
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        isOpen={!!deleteConfirm}
        onClose={() => !deleting && setDeleteConfirm(null)}
        onConfirm={() => deleteConfirm && handleDelete(deleteConfirm)}
        message={`Are you sure you want to delete "${deleteConfirm?.title || 'Untitled video'}"? This action cannot be undone.`}
        loading={deleting}
        loadingText="Deleting..."
        buttonText="Delete"
      />
    </div>
  );
};

export default ContentLibrary;
