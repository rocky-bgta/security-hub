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
  ArrowUpDown,
  Loader2,
  MoreHorizontal,
  Trash2,
} from 'lucide-react';
import {
  DifficultyLevel,
  getDifficultyColor,
  getDifficultyLevelLabel,
} from 'models/EmailTemplate';
import { ILandingPage, getLandingPageTypeLabel } from 'models/LandingPage';
import { useState } from 'react';
import CategoryBadge from './CategoryBadge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { TableSkeleton } from 'components/LoadingSkeleton';

interface LandingPageTableProps {
  pages: ILandingPage[];
  loading?: boolean;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
  onSort: (field: string) => void;
  onPreview: (page: ILandingPage) => void;
  onEdit: (page: ILandingPage) => void;
  onDuplicate: (page: ILandingPage) => void;
  onDelete: (page: ILandingPage) => void;
}

/**
 * Table component for displaying landing pages in list view
 * Based on Task-04 Landing Page Library
 */
export const LandingPageTable = ({
  pages,
  loading = false,
  sortBy,
  sortOrder,
  onSort,
  onPreview,
  onEdit,
  onDuplicate,
  onDelete,
}: LandingPageTableProps) => {
  const [_openMenuId, setOpenMenuId] = useState<string | null>(null);

  const columns = [
    { key: 'name', label: 'Name', sortable: true },
    { key: 'category', label: 'Category', sortable: true },
    { key: 'difficultyLevel', label: 'Difficulty', sortable: true },
    { key: 'pageType', label: 'Type', sortable: false },
    { key: 'tags', label: 'Tags', sortable: false },
    { key: 'popularity', label: 'Popularity', sortable: true },
    { key: 'actions', label: 'Actions', sortable: false },
  ];

  const getSortIcon = (field: string) => {
    if (sortBy !== field) {
      return <ArrowUpDown className="size-4 text-muted-foreground" />;
    }
    return sortOrder === 'asc' ? (
      <ArrowUpDown className="size-4 text-primary" />
    ) : (
      <ArrowUpDown className="size-4 text-primary" />
    );
  };

  const getMenuItems = (page: ILandingPage) => [
    {
      label: 'Preview',
      onClick: () => {
        onPreview(page);
        setOpenMenuId(null);
      },
      icon: (
        <svg
          className="size-4"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
          />
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
          />
        </svg>
      ),
    },
    ...(page.canEdit
      ? [
          {
            label: 'Edit',
            onClick: () => {
              onEdit(page);
              setOpenMenuId(null);
            },
            icon: (
              <svg
                className="size-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                />
              </svg>
            ),
          },
        ]
      : []),
    {
      label: 'Duplicate',
      onClick: () => {
        onDuplicate(page);
        setOpenMenuId(null);
      },
      icon: (
        <svg
          className="size-4"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
          />
        </svg>
      ),
    },
    ...(page.canDelete
      ? [
          {
            label: 'Delete',
            onClick: () => {
              onDelete(page);
              setOpenMenuId(null);
            },
            icon: (
              <svg
                className="size-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                />
              </svg>
            ),
            className: 'text-red-600 hover:bg-red-50',
          },
        ]
      : []),
  ];

  if (loading) {
    return <TableSkeleton count={12} />;
  }

  if (pages.length === 0) {
    return (
      <div className="rounded-lg border border-gray-200 bg-white p-16 text-center text-gray-500 shadow-sm">
        <svg
          className="mx-auto mb-4 size-16"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1}
            d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
          />
        </svg>
        <p className="mb-2 text-lg font-medium">No landing pages found</p>
        <p className="text-sm">
          Create a new landing page or adjust your filters
        </p>
      </div>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          {columns.map(col => (
            <TableHead
              key={col.key}
              className={`px-6 py-3 text-left text-xs font-medium tracking-wider ${
                col.sortable ? 'cursor-pointer hover:bg-white/10' : ''
              }`}
              onClick={() => col.sortable && onSort(col.key)}
            >
              <div className="flex items-center gap-1">
                {col.label}
                {col.sortable && getSortIcon(col.key)}
              </div>
            </TableHead>
          ))}
        </TableRow>
      </TableHeader>
      <TableBody>
        {pages.map(page => {
          const isFailed = (page as { status?: string }).status === 'FAILED';
          const isProcessing =
            (page as { status?: string }).status === 'PROCESSING';

          return (
            <TableRow
              key={page.pageId}
              className={`cursor-pointer ${
                isFailed ? 'bg-white/20' : isProcessing ? 'bg-amber-50/40' : ''
              }`}
              onClick={() => onPreview(page)}
            >
              <TableCell>
                <div className="flex items-center">
                  <div className="size-10 shrink-0">
                    {page.thumbnailUrl ? (
                      <img
                        className="size-10 rounded object-cover"
                        src={FILE_PATH_PREFIX + page.thumbnailUrl}
                        alt={page.name}
                      />
                    ) : (
                      <div className="flex size-10 items-center justify-center rounded bg-card-border">
                        <svg
                          className="size-5 text-gray-400"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={1}
                            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                          />
                        </svg>
                      </div>
                    )}
                  </div>
                  <div className="ml-4">
                    <div className="flex items-center gap-2 text-sm font-medium text-card-foreground">
                      {page.name}
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
                        <span className="rounded bg-yellow-100 px-1.5 py-0.5 text-xs text-secondary-foreground">
                          Premium
                        </span>
                      )}
                      {page.isGlobal && (
                        <span className="rounded bg-blue-100 px-1.5 py-0.5 text-xs text-secondary-foreground">
                          Global
                        </span>
                      )}
                    </div>
                    {page.description && (
                      <div className="max-w-xs truncate text-sm text-muted-foreground">
                        {page.description}
                      </div>
                    )}
                  </div>
                </div>
              </TableCell>
              <TableCell>
                <CategoryBadge category={page.category} />
              </TableCell>
              <TableCell>
                <span
                  className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${getDifficultyColor(
                    (typeof page.difficultyLevel === 'object'
                      ? page.difficultyLevel.id
                      : page.difficultyLevel) as DifficultyLevel,
                  )}`}
                >
                  {typeof page.difficultyLevel === 'object'
                    ? page.difficultyLevel.name
                    : getDifficultyLevelLabel(
                        page.difficultyLevel as DifficultyLevel,
                      )}
                </span>
              </TableCell>
              <TableCell>{getLandingPageTypeLabel(page.pageType)}</TableCell>
              <TableCell>
                <div className="flex max-w-xs flex-wrap gap-1">
                  {page.tags?.slice(0, 2).map((tag, i) => (
                    <TagBadge key={i} tag={tag} />
                  ))}
                  {page.tags && page.tags.length > 2 && (
                    <span className="text-xs text-gray-500">
                      +{page.tags.length - 2}
                    </span>
                  )}
                </div>
              </TableCell>
              <TableCell className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                <div className="flex items-center gap-1">
                  <svg
                    className="size-4"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                    <path
                      fillRule="evenodd"
                      d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z"
                      clipRule="evenodd"
                    />
                  </svg>
                  {page.popularity}
                </div>
              </TableCell>
              <TableCell
                className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium"
                onClick={e => e.stopPropagation()}
              >
                <div className="flex items-center justify-end gap-2">
                  {isFailed && (
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => onDelete(page)}
                    >
                      <Trash2 className="mr-1.5 size-3.5" />
                      Delete
                    </Button>
                  )}
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <button className="rounded p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600">
                        <MoreHorizontal className="size-5" />
                      </button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent>
                      {getMenuItems(page).map(item => (
                        <DropdownMenuItem
                          key={item.label}
                          onClick={item.onClick}
                        >
                          {item.icon}
                          {item.label}
                        </DropdownMenuItem>
                      ))}
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
};

export default LandingPageTable;
