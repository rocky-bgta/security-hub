import { openHtmlInNewTab } from 'utils/OpenHtmlPreview';
import { Button } from 'common/Button';
import { Dialog, DialogContent, DialogTitle } from 'common/Dialog';
import { Skeleton } from 'components/LoadingSkeleton';
import TagBadge from 'components/TagBadge';
import {
  DEVICE_SIZES,
  DeviceType,
  ILandingPagePreview,
} from 'models/LandingPage';
import { useEffect, useState } from 'react';
import DevicePreviewToggle from './DevicePreviewToggle';
import {
  ChartNoAxesColumn,
  ExternalLink,
  FileType2,
  FileWarning,
  Layers,
  Send,
  ShieldCheck,
  Sparkles,
  Star,
} from 'lucide-react';

type ExtendedLandingPagePreview = ILandingPagePreview & {
  description?: string;
  pageType?: string;
  category?: string;
  difficultyLevel?: string;
  status?: string;
  thumbnailUrl?: string | null;
  websiteUrl?: string | null;
  tags?: string[];
  popularity?: number;
  templateGenerationType?: string | null;
  premium?: boolean;
};

interface LandingPagePreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  preview: ILandingPagePreview | null;
  loading?: boolean;
  onUseInCampaign?: () => void;
}

/**
 * Modal component for previewing landing page HTML content
 * Based on Task-04 Landing Page Library (BR-05: Preview renders in sandboxed iframe)
 */
export const LandingPagePreviewModal = ({
  isOpen,
  onClose,
  preview,
  loading = false,
  onUseInCampaign,
}: LandingPagePreviewModalProps) => {
  const [device, setDevice] = useState<DeviceType>('desktop');
  const [iframeLoaded, setIframeLoaded] = useState(false);
  const previewData = preview as ExtendedLandingPagePreview | null;

  useEffect(() => {
    if (isOpen) {
      setTimeout(() => {
        setIframeLoaded(false);
      }, 0);
    }
  }, [isOpen, preview?.pageId]);

  const getIframeWidth = () => {
    return DEVICE_SIZES[device].width;
  };

  const formatEnumLabel = (
    value?: string | { id: string; name: string } | null,
  ): string => {
    if (!value) return 'Not provided';
    if (typeof value === 'object') return value.name;
    return value
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  };

  const metadataItems = previewData
    ? [
        {
          label: 'Page Type',
          value: formatEnumLabel(previewData.pageType),
          icon: <FileType2 className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Category',
          value: formatEnumLabel(previewData.category),
          icon: <Layers className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Difficulty',
          value: formatEnumLabel(previewData.difficultyLevel),
          icon: <Sparkles className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Status',
          value: formatEnumLabel(previewData.status),
          icon: <ShieldCheck className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Popularity',
          value:
            typeof previewData.popularity === 'number'
              ? `${previewData.popularity} uses`
              : 'Not available',
          icon: <ChartNoAxesColumn className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Generation',
          value: formatEnumLabel(previewData.templateGenerationType),
          icon: <Star className="size-4 text-muted-foreground" />,
        },
      ]
    : [];

  const handleOpenInNewTab = () => {
    if (!preview?.htmlContent) return;
    openHtmlInNewTab(preview.htmlContent);
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] max-w-5xl overflow-y-auto">
        <DialogTitle>{previewData?.name || 'Landing Page Preview'}</DialogTitle>
        <div className="flex flex-col">
          {/* Summary */}
          {previewData && (
            <div className="mb-4 rounded-lg border border-card-border bg-background p-4">
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                Description
              </span>
              <p className="mt-2 text-sm text-muted-foreground">
                {previewData.description?.trim() ||
                  'No description has been added for this landing page template.'}
              </p>
            </div>
          )}

          {previewData && metadataItems.length > 0 && (
            <div className="mb-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {metadataItems.map(item => (
                <div
                  key={item.label}
                  className="rounded-lg border border-primary/20 bg-background p-3"
                >
                  <div className="mb-2 flex items-center gap-2">
                    {item.icon}
                    <span className="text-xs uppercase tracking-wide text-muted-foreground">
                      {item.label}
                    </span>
                  </div>
                  <p className="text-sm font-medium text-primary">
                    {item.value}
                  </p>
                </div>
              ))}
            </div>
          )}

          {previewData?.tags && previewData.tags.length > 0 && (
            <div className="mb-4 rounded-lg border border-primary/20 bg-background p-4">
              <h4 className="mb-2 text-sm font-medium text-muted-foreground">
                Tags
              </h4>
              <div className="flex flex-wrap gap-2">
                {previewData.tags.map((tag, idx) => (
                  <TagBadge key={`${tag}-${idx}`} tag={tag} />
                ))}
              </div>
            </div>
          )}

          {/* Header Controls */}
          <div className="flex flex-wrap items-center justify-between gap-4 border-b border-gray-200 pb-4">
            <DevicePreviewToggle
              selectedDevice={device}
              onDeviceChange={setDevice}
            />
            <div className="flex items-center gap-2">
              <Button
                variant="secondary"
                size="sm"
                onClick={handleOpenInNewTab}
                disabled={!preview?.htmlContent}
              >
                <ExternalLink className="mr-1 size-4" />
                Open in New Tab
              </Button>

              {onUseInCampaign && (
                <Button variant="default" size="sm" onClick={onUseInCampaign}>
                  <Send className="mr-1 size-4" />
                  Use in Campaign
                </Button>
              )}
            </div>
          </div>

          {/* Preview Content */}
          <div className="mt-4 flex flex-1 items-center justify-center overflow-hidden rounded-lg bg-gray-100">
            {loading ? (
              <div className="flex size-full items-center justify-center">
                <div className="text-center">
                  <Skeleton className="mx-auto mb-2 size-8 rounded-full" />
                  <p className="text-gray-500">Loading preview...</p>
                </div>
              </div>
            ) : previewData?.htmlContent ? (
              <div
                className="relative h-[500px] overflow-y-scroll bg-white shadow-lg transition-all duration-300"
                style={{
                  width: getIframeWidth(),
                  maxWidth: '100%',
                }}
              >
                <iframe
                  srcDoc={previewData.htmlContent}
                  title={`Preview: ${previewData.name}`}
                  className="size-full border-0"
                  sandbox="allow-same-origin"
                  onLoad={() => setIframeLoaded(true)}
                  style={{
                    opacity: iframeLoaded ? 1 : 0,
                    transition: 'opacity 0.3s ease',
                  }}
                />
                {!iframeLoaded && (
                  <div className="absolute inset-0 flex items-center justify-center">
                    <Skeleton className="size-full" />
                  </div>
                )}
              </div>
            ) : (
              <div className="text-center text-gray-500">
                <FileWarning className="mx-auto mb-4 size-16" />
                <p>No preview available</p>
              </div>
            )}
          </div>

          {/* Page Info Footer */}
          {previewData && (
            <div className="mt-4 rounded-lg border border-primary/20 bg-background p-4">
              <h4 className="mb-3 text-sm font-medium text-muted-foreground">
                Capture Configuration
              </h4>
              <div className="flex flex-wrap gap-4 text-sm text-gray-500">
                <div className="flex items-center gap-2">
                  <span className="font-medium">Captures Data:</span>
                  <span
                    className={
                      previewData.captureSubmittedData
                        ? 'text-green-600'
                        : 'text-gray-400'
                    }
                  >
                    {previewData.captureSubmittedData ? 'Yes' : 'No'}
                  </span>
                </div>
                {previewData.captureFields &&
                  previewData.captureFields.length > 0 && (
                    <div className="flex items-center gap-2">
                      <span className="font-medium">Fields:</span>
                      <span>{previewData.captureFields.join(', ')}</span>
                    </div>
                  )}
                {previewData.redirectUrl && (
                  <div className="flex items-center gap-2">
                    <span className="font-medium">Redirect URL:</span>
                    <a
                      href={previewData.redirectUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="max-w-xs truncate text-primary hover:underline"
                    >
                      {previewData.redirectUrl}
                    </a>
                  </div>
                )}
                {!previewData.redirectUrl && (
                  <div className="flex items-center gap-2">
                    <span className="font-medium">Redirect URL:</span>
                    <span className="text-gray-400">Not configured</span>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Footer Buttons */}
          <div className="mt-4 flex justify-end gap-3 border-t border-gray-200 pt-4">
            <Button variant="secondary" onClick={onClose}>
              Close
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default LandingPagePreviewModal;
