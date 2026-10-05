import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/Dropdown';
import TagBadge from 'components/TagBadge';
import {
  AlertTriangle,
  Copy,
  Crown,
  Edit,
  Eye,
  Globe,
  Loader2,
  MoreHorizontal,
  Trash2,
} from 'lucide-react';
import { IEmailTemplate, TemplateType } from 'models/EmailTemplate';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface EmailTemplateCardProps {
  template: IEmailTemplate;
  templateType?: TemplateType;
  onPreview: (template: IEmailTemplate) => void;
  onEdit: (template: IEmailTemplate) => void;
  onDuplicate: (template: IEmailTemplate) => void;
  onDelete: (template: IEmailTemplate) => void;
}

/**
 * Card component for displaying email template in Grid View
 */
const EmailTemplateCard = ({
  template,
  templateType = TemplateType.EMAIL,
  onPreview,
  onEdit,
  onDuplicate,
  onDelete,
}: EmailTemplateCardProps) => {
  const maxVisibleTags = 3;
  const visibleTags = template.tags?.slice(0, maxVisibleTags) || [];
  const remainingTags = (template.tags?.length || 0) - maxVisibleTags;
  const isProcessing =
    (template as { status?: string }).status === 'PROCESSING';
  const isFailed = (template as { status?: string }).status === 'FAILED';

  const isSms = templateType === TemplateType.SMS;
  const previewText = isSms
    ? template.emailBodyPreview || template.emailSubject
    : template.emailSubject;

  return (
    <div
      className={`group relative overflow-hidden rounded-lg border transition-all hover:shadow-lg ${
        isFailed
          ? 'border-red-400/70 bg-white/20'
          : isProcessing
            ? 'border-amber-400/70 bg-amber-50/30'
            : 'border-card-border hover:border-primary/50'
      }`}
    >
      {/* Thumbnail */}
      <div
        className="relative h-40 cursor-pointer"
        onClick={() => onPreview(template)}
      >
        {template.thumbnailUrl ? (
          <img
            src={FILE_PATH_PREFIX + template.thumbnailUrl}
            alt={template.templateName}
            className="size-full object-cover"
          />
        ) : (
          <div className="flex h-full items-center justify-center">
            <span className="text-4xl text-muted-foreground">
              {isSms ? '📱' : '📧'}
            </span>
          </div>
        )}

        {/* Badges overlay */}
        <div className="absolute left-2 top-2 flex gap-1">
          {isFailed && (
            <span className="inline-flex items-center gap-1 rounded bg-red-600 px-2 py-0.5 text-xs font-semibold text-white">
              <AlertTriangle className="size-3" />
              Failed
            </span>
          )}
          {isProcessing && (
            <span className="inline-flex items-center gap-1 rounded bg-amber-500 px-2 py-0.5 text-xs font-semibold text-white">
              <Loader2 className="size-3 animate-spin" />
              Processing
            </span>
          )}
          {template.isPremium && (
            <span className="rounded bg-yellow-600 px-1.5 py-0.5 text-xs text-foreground">
              <Crown className="inline-block size-3" /> Premium
            </span>
          )}
          {template.isGlobal && (
            <span className="rounded bg-vibrant-red px-1.5 py-0.5 text-xs text-foreground">
              <Globe className="inline-block size-3" /> Global
            </span>
          )}
        </div>

        {/* Actions overlay on hover */}
        <div className="absolute inset-0 flex items-center justify-center bg-foreground/50 opacity-0 transition-opacity group-hover:opacity-100">
          <Button
            variant="secondary"
            size="sm"
            onClick={e => {
              e.stopPropagation();
              onPreview(template);
            }}
          >
            <Eye className="mr-1 size-4" />
            Preview
          </Button>
        </div>
      </div>

      <div className="p-4">
        {/* Header with title and actions */}
        <div className="mb-2 flex items-start justify-between">
          <h3
            className="line-clamp-1 cursor-pointer text-sm font-medium text-primary hover:text-primary"
            onClick={() => onPreview(template)}
            title={template.templateName}
          >
            {template.templateName}
          </h3>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => onPreview(template)}>
                <Eye className="mr-2 size-4" />
                Preview
              </DropdownMenuItem>
              {template.canEdit && (
                <DropdownMenuItem onClick={() => onEdit(template)}>
                  <Edit className="mr-2 size-4" />
                  Edit
                </DropdownMenuItem>
              )}
              <DropdownMenuItem onClick={() => onDuplicate(template)}>
                <Copy className="mr-2 size-4" />
                Duplicate
              </DropdownMenuItem>
              {template.canDelete && (
                <DropdownMenuItem
                  onClick={() => onDelete(template)}
                  className="text-vibrant-red focus:text-vibrant-red"
                >
                  <Trash2 className="mr-2 size-4" />
                  Delete
                </DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        {/* Subject line */}
        <p
          className="mb-3 line-clamp-2 text-xs text-foreground"
          title={previewText}
        >
          {previewText}
        </p>

        {/* Badges row */}
        <div className="mb-3 flex flex-wrap gap-1">
          {template.difficultyLevel?.name && (
            <Badge variant="outline" className="text-xs">
              {template.difficultyLevel.name}
            </Badge>
          )}
          {template.payloadType?.name && (
            <Badge variant="outline" className="text-xs">
              {template.payloadType.name}
            </Badge>
          )}
        </div>

        {/* Tags */}
        {visibleTags.length > 0 && (
          <div className="flex flex-wrap gap-1">
            {visibleTags.map((tag, idx) => (
              <TagBadge key={idx} tag={tag} />
            ))}
            {remainingTags > 0 && (
              <span className="text-xs text-muted-foreground">
                +{remainingTags} more
              </span>
            )}
          </div>
        )}

        {/* Footer */}
        {isFailed ? (
          <div className="mt-3 space-y-2 border-t border-red-200 pt-2">
            <p className="flex items-center gap-1 text-xs text-red-600">
              <AlertTriangle className="size-3 shrink-0" />
              Generation failed. Please delete and try again.
            </p>
            <Button
              variant="destructive"
              size="sm"
              className="w-full"
              onClick={e => {
                e.stopPropagation();
                onDelete(template);
              }}
            >
              <Trash2 className="mr-1.5 size-3.5" />
              Delete Failed Template
            </Button>
          </div>
        ) : (
          <div className="mt-3 flex items-center justify-between border-t border-primary pt-2 text-xs text-muted-foreground">
            <span>Used {template.popularity} times</span>
            <span>{template.language?.toUpperCase() || 'EN'}</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default EmailTemplateCard;
