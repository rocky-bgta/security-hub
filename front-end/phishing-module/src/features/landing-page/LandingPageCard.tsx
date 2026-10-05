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
  CheckCircle,
  CopyIcon,
  Eye,
  EyeIcon,
  ImageIcon,
  Loader2,
  MoreHorizontal,
  PencilIcon,
  Star,
  Trash2Icon,
} from 'lucide-react';
import {
  DifficultyLevel,
  getDifficultyColor,
  getDifficultyLevelLabel,
} from 'models/EmailTemplate';
import { ILandingPage, getLandingPageTypeLabel } from 'models/LandingPage';
import { useState } from 'react';
import { cn } from 'utils/Helper';
import CategoryBadge from './CategoryBadge';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface LandingPageCardProps {
  page: ILandingPage;
  onPreview: (page: ILandingPage) => void;
  onEdit: (page: ILandingPage) => void;
  onDuplicate: (page: ILandingPage) => void;
  onDelete: (page: ILandingPage) => void;
}

/**
 * Card component for displaying a single landing page in grid view
 * Based on Task-04 Landing Page Library
 */
export const LandingPageCard = ({
  page,
  onPreview,
  onEdit,
  onDuplicate,
  onDelete,
}: LandingPageCardProps) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const isProcessing = (page as { status?: string }).status === 'PROCESSING';
  const isFailed = (page as { status?: string }).status === 'FAILED';

  const menuItems = [
    {
      label: 'Preview',
      onClick: () => {
        onPreview(page);
        setIsMenuOpen(false);
      },
      icon: <EyeIcon className="size-4 text-blue-600" />,
    },
    ...(page.canEdit
      ? [
          {
            label: 'Edit',
            onClick: () => {
              onEdit(page);
              setIsMenuOpen(false);
            },
            icon: <PencilIcon className="size-4 text-blue-600" />,
          },
        ]
      : []),
    {
      label: 'Duplicate',
      onClick: () => {
        onDuplicate(page);
        setIsMenuOpen(false);
      },
      icon: <CopyIcon className="size-4 text-green-600" />,
    },
    ...(page.canDelete
      ? [
          {
            label: 'Delete',
            onClick: () => {
              onDelete(page);
              setIsMenuOpen(false);
            },
            icon: <Trash2Icon className="size-4 text-red-600" />,
            className: 'text-red-600 hover:bg-red-50',
          },
        ]
      : []),
  ];

  return (
    <div
      className={`group relative overflow-hidden rounded-lg border transition-shadow hover:shadow-md ${
        isFailed
          ? 'border-red-400/70 bg-white/20'
          : isProcessing
            ? 'border-amber-400/70 bg-amber-50/30'
            : 'border-card-border'
      }`}
    >
      {/* Thumbnail */}
      <div
        className="relative h-40 cursor-pointer"
        onClick={() => onPreview(page)}
      >
        {page.thumbnailUrl ? (
          <img
            src={FILE_PATH_PREFIX + page.thumbnailUrl}
            alt={page.name}
            className="size-full object-cover"
          />
        ) : (
          <div className="flex size-full items-center justify-center text-muted-foreground">
            <ImageIcon className="size-9" />
          </div>
        )}

        {/* Badges */}
        <div className="absolute left-2 top-2 flex flex-wrap gap-1">
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
          {page.isPremium && (
            <span className="rounded bg-yellow-400 px-2 py-0.5 text-xs font-semibold text-yellow-900">
              Premium
            </span>
          )}
          {page.isGlobal && (
            <span className="rounded bg-blue-500 px-2 py-0.5 text-xs font-semibold text-foreground">
              Global
            </span>
          )}
        </div>

        {/* Popularity */}
        <div className="absolute bottom-2 right-2 flex items-center gap-1 rounded bg-black/50 px-2 py-1 text-xs text-foreground">
          <Star className="size-3 text-yellow-500" />
          {page.popularity}
        </div>

        <div className="absolute inset-0 flex items-center justify-center bg-foreground/50 opacity-0 transition-opacity group-hover:opacity-100">
          <Button
            variant="secondary"
            size="sm"
            onClick={e => {
              e.stopPropagation();
              onPreview(page);
            }}
          >
            <Eye className="mr-1 size-4" />
            Preview
          </Button>
        </div>
      </div>

      {/* Content */}
      <div className="p-4">
        {/* Header */}
        <div className="mb-2 flex items-start justify-between">
          <h3
            className="cursor-pointer truncate font-medium text-foreground"
            onClick={() => onPreview(page)}
            title={page.name}
          >
            {page.name}
          </h3>
          <DropdownMenu open={isMenuOpen} onOpenChange={setIsMenuOpen}>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {menuItems.map(item => (
                <DropdownMenuItem
                  key={item.label}
                  onClick={item.onClick}
                  className="space-x-2"
                >
                  {item.icon}
                  <span>{item.label}</span>
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        {/* Description */}
        {page.description && (
          <p className="mb-3 line-clamp-2 text-sm text-muted-foreground">
            {page.description}
          </p>
        )}

        {/* Meta Info */}
        <div className="mb-3 flex flex-wrap gap-2">
          <CategoryBadge category={page.category} />
          <span
            className={cn(
              'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium',
              getDifficultyColor(
                (typeof page.difficultyLevel === 'object'
                  ? page.difficultyLevel.id
                  : page.difficultyLevel) as DifficultyLevel,
              ),
            )}
          >
            {typeof page.difficultyLevel === 'object'
              ? page.difficultyLevel.name
              : getDifficultyLevelLabel(
                  page.difficultyLevel as DifficultyLevel,
                )}
          </span>
          <span className="inline-flex items-center rounded-full bg-blue-600 px-2.5 py-0.5 text-xs font-medium">
            {getLandingPageTypeLabel(page.pageType)}
          </span>
        </div>

        {/* Tags */}
        {page.tags && page.tags.length > 0 && (
          <div className="flex flex-wrap gap-1">
            {page.tags.slice(0, 3).map((tag, index) => (
              <TagBadge key={index} tag={tag} />
            ))}
            {page.tags.length > 3 && (
              <span className="text-xs text-muted-foreground">
                +{page.tags.length - 3}
              </span>
            )}
          </div>
        )}

        {/* Capture Data Indicator */}
        {page.captureSubmittedData && !isFailed && (
          <div className="mt-3 flex items-center text-xs text-green-600">
            <CheckCircle className="mr-1 size-4" />
            Captures form data
          </div>
        )}

        {/* Failed state footer */}
        {isFailed && (
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
                onDelete(page);
              }}
            >
              <Trash2Icon className="mr-1.5 size-3.5" />
              Delete Failed Page
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};

export default LandingPageCard;
