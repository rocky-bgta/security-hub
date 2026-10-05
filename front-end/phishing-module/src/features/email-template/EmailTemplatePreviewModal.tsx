import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import TagBadge from 'components/TagBadge';
import {
  ChartNoAxesColumn,
  Eye,
  FileType,
  Globe2,
  Loader2,
  MapPin,
  Paperclip,
  ShieldAlert,
  Sparkles,
  Star,
} from 'lucide-react';
import { IEmailTemplatePreview, IIdName, TemplateType } from 'models/EmailTemplate';
import { useCallback, useEffect, useState } from 'react';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { getSmsBodyFromPreview } from 'utils/smsSegments';

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

type ExtendedTemplatePreview = IEmailTemplatePreview & {
  description?: string;
  emailType?: string;
  payloadType?: string | IIdName;
  difficultyLevel?: string | IIdName;
  serviceLocation?: string;
  tags?: string[];
  language?: string;
  popularity?: number;
  templateGenerationType?: string | null;
  status?: string;
  thumbnailUrl?: string;
  premium?: boolean;
};

interface EmailTemplatePreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  templateId: string | null;
  templateName: string;
  templateType?: TemplateType;
  getPreview: (id: string) => Promise<IEmailTemplatePreview | null>;
}

/**
 * Modal for previewing full email template content
 */
const EmailTemplatePreviewModal = ({
  isOpen,
  onClose,
  templateId,
  templateName,
  templateType,
  getPreview,
}: EmailTemplatePreviewModalProps) => {
  const [preview, setPreview] = useState<ExtendedTemplatePreview | null>(null);
  const [loading, setLoading] = useState(false);

  const loadPreview = useCallback(async () => {
    if (!templateId) return;
    setLoading(true);
    const data = await getPreview(templateId);
    setPreview(data as ExtendedTemplatePreview | null);
    setLoading(false);
  }, [getPreview, templateId]);

  useEffect(() => {
    if (isOpen && templateId) {
      const timer = window.setTimeout(() => {
        void loadPreview();
      }, 0);
      return () => window.clearTimeout(timer);
    }
  }, [isOpen, templateId, loadPreview]);

  const formatEnumLabel = (value?: string | IIdName | null): string => {
    if (!value) return 'Not provided';
    if (typeof value === 'object') return value.name;
    return value
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  };

  const getLanguageLabel = (language?: string): string => {
    if (!language) return 'Not provided';
    return language.length === 2
      ? `${language.toUpperCase()} (${language})`
      : language.toUpperCase();
  };

  const metadataItems = preview
    ? [
        {
          label: 'Email Type',
          value: formatEnumLabel(preview.emailType),
          icon: <ShieldAlert className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Payload',
          value: formatEnumLabel(preview.payloadType),
          icon: <FileType className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Difficulty',
          value: formatEnumLabel(preview.difficultyLevel),
          icon: <Sparkles className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Location',
          value: preview.serviceLocation || 'Not provided',
          icon: <MapPin className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Language',
          value: getLanguageLabel(preview.language),
          icon: <Globe2 className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Status',
          value: formatEnumLabel(preview.status),
          icon: <Star className="size-4 text-muted-foreground" />,
        },
        {
          label: 'Popularity',
          value:
            typeof preview.popularity === 'number'
              ? `${preview.popularity} uses`
              : 'Not available',
          icon: <ChartNoAxesColumn className="size-4 text-muted-foreground" />,
        },
      ]
    : [];

  const isSms =
    templateType === TemplateType.SMS ||
    preview?.templateType === TemplateType.SMS;

  const bodySrcDoc = preview
    ? isSms
      ? (() => {
          const smsBody = getSmsBodyFromPreview(preview);
          return smsBody.trim()
            ? `<div style="font-family: monospace; white-space: pre-wrap; padding: 16px;">${escapeHtml(smsBody)}</div>`
            : '<div style="font-family: Arial, sans-serif; color: #6b7280; padding: 16px;">No SMS message available.</div>';
        })()
      : preview.emailBody?.trim() ||
        '<div style="font-family: Arial, sans-serif; color: #6b7280; padding: 16px;">No HTML email body available.</div>'
    : '';

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] max-w-5xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Eye className="size-5 text-primary" />
            {templateName || 'Template Preview'}
          </DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="flex h-64 items-center justify-center">
            <Loader2 className="size-8 animate-spin text-primary" />
          </div>
        ) : preview ? (
          <div className="flex flex-col gap-5">
            <div className="rounded-lg border border-primary/25 bg-background p-4">
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                Subject
              </span>
              <p className="mt-2 text-base font-semibold text-primary">
                {preview.emailSubject || 'No subject provided'}
              </p>
              <p className="mt-3 text-sm text-muted-foreground">
                {preview.description?.trim() ||
                  'No description has been added for this template.'}
              </p>
            </div>

            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
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

            {preview.tags && preview.tags.length > 0 && (
              <div className="rounded-lg border border-primary/20 bg-background p-4">
                <h4 className="mb-3 text-sm font-medium text-muted-foreground">
                  Tags
                </h4>
                <div className="flex flex-wrap gap-2">
                  {preview.tags.map((tag, idx) => (
                    <TagBadge key={`${tag}-${idx}`} tag={tag} />
                  ))}
                </div>
              </div>
            )}

            {/* Email body */}
            <div className="rounded-lg border border-primary/25 p-3">
              <h4 className="mb-3 text-sm font-medium text-muted-foreground">
                {isSms ? 'Message Preview' : 'Email Body Preview'}
              </h4>
              <iframe
                srcDoc={bodySrcDoc}
                style={{
                  width: '100%',
                  height: '400px',
                  border: '1px solid #ddd',
                  borderRadius: '8px',
                  background: 'white',
                }}
              />
            </div>

            {/* Attachments */}
            {preview.attachments && preview.attachments.length > 0 && (
              <div className="rounded-lg border border-primary/20 bg-background p-4">
                <h4 className="mb-2 flex items-center gap-1 text-sm font-medium text-foreground">
                  <Paperclip className="size-4" />
                  Attachments ({preview.attachments.length})
                </h4>
                <div className="flex flex-wrap gap-2">
                  {preview.attachments.map((att, idx) => (
                    <div
                      key={idx}
                      className="flex items-center gap-2 rounded-md border border-primary px-3 py-2 text-sm"
                    >
                      <Paperclip className="size-4 text-muted-foreground" />
                      <span className="text-primary">
                        {FILE_PATH_PREFIX + att}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ) : (
          <div className="flex h-64 items-center justify-center">
            <p className="text-muted-foreground">Failed to load preview</p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default EmailTemplatePreviewModal;
