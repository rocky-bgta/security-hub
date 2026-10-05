import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/Dropdown';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { TableSkeleton } from 'components/LoadingSkeleton';
import TagBadge from 'components/TagBadge';
import {
  AlertTriangle,
  ArrowUpDown,
  Copy,
  Edit,
  Eye,
  Loader2,
  MoreHorizontal,
  Trash2,
} from 'lucide-react';
import { IEmailTemplate, TemplateType } from 'models/EmailTemplate';
import { ReactNode } from 'react';
import { formatDate } from 'utils/Helper';

interface EmailTemplateTableProps {
  templates: IEmailTemplate[];
  loading: boolean;
  templateType?: TemplateType;
  sortBy: string;
  sortOrder: 'asc' | 'desc';
  onSort: (field: string) => void;
  onPreview: (template: IEmailTemplate) => void;
  onEdit: (template: IEmailTemplate) => void;
  onDuplicate: (template: IEmailTemplate) => void;
  onDelete: (template: IEmailTemplate) => void;
}

const SortableHeader = ({
  field,
  children,
  sortBy,
  onSort,
}: {
  field: string;
  children: ReactNode;
  sortBy: string;
  onSort: (field: string) => void;
}) => (
  <div
    className="flex cursor-pointer items-center gap-1 hover:text-primary"
    onClick={() => onSort(field)}
  >
    {children}
    <ArrowUpDown
      className={`size-4 ${sortBy === field ? 'text-primary' : 'text-muted-foreground'}`}
    />
  </div>
);

/**
 * Table component for displaying email templates
 */
const EmailTemplateTable = ({
  templates,
  loading,
  templateType = TemplateType.EMAIL,
  sortBy,
  sortOrder: _sortOrder,
  onSort,
  onPreview,
  onEdit,
  onDuplicate,
  onDelete,
}: EmailTemplateTableProps) => {
  const isSms = templateType === TemplateType.SMS;

  if (loading) {
    return <TableSkeleton count={12} />;
  }

  if (templates.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <span className="mb-4 text-6xl">{isSms ? '📱' : '📧'}</span>
        <h3 className="mb-2 text-lg font-medium text-primary">
          No templates found
        </h3>
        <p className="text-center text-muted-foreground">
          No {isSms ? 'SMS' : 'email'} templates match your current filters.
          <br />
          Try adjusting your search or create a new template.
        </p>
      </div>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>
            <SortableHeader
              field="templateName"
              sortBy={sortBy}
              onSort={onSort}
            >
              Template Name
            </SortableHeader>
          </TableHead>
          <TableHead>{isSms ? 'Message' : 'Email Subject'}</TableHead>
          <TableHead>Difficulty</TableHead>
          <TableHead>Payload Type</TableHead>
          <TableHead>Tags</TableHead>
          <TableHead>
            <SortableHeader field="popularity" sortBy={sortBy} onSort={onSort}>
              Popularity
            </SortableHeader>
          </TableHead>
          <TableHead>
            <SortableHeader field="createdAt" sortBy={sortBy} onSort={onSort}>
              Created
            </SortableHeader>
          </TableHead>
          <TableHead className="text-center">Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {templates.map(template => {
          const isFailed =
            (template as { status?: string }).status === 'FAILED';
          const isProcessing =
            (template as { status?: string }).status === 'PROCESSING';

          return (
            <TableRow
              key={template.templateId}
              className={
                isFailed ? 'bg-white/20' : isProcessing ? 'bg-amber-50/40' : ''
              }
            >
              <TableCell className="text-left">
                <div className="flex items-center gap-2">
                  <Button
                    variant="link"
                    className="pl-0 font-medium hover:underline"
                    onClick={() => onPreview(template)}
                  >
                    {template.templateName}
                  </Button>
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
                </div>
              </TableCell>
              <TableCell className="max-w-[200px] truncate text-foreground">
                {isSms
                  ? template.emailBodyPreview || template.emailSubject
                  : template.emailSubject}
              </TableCell>
              <TableCell>
                {template.difficultyLevel?.name ? (
                  <Badge variant="outline" className="text-xs">
                    {template.difficultyLevel.name}
                  </Badge>
                ) : (
                  '—'
                )}
              </TableCell>
              <TableCell>
                {template.payloadType?.name ? (
                  <Badge variant="outline" className="text-xs">
                    {template.payloadType.name}
                  </Badge>
                ) : (
                  '—'
                )}
              </TableCell>
              <TableCell>
                <div className="flex flex-wrap gap-1">
                  {template.tags?.slice(0, 2).map((tag, idx) => (
                    <TagBadge key={idx} tag={tag} />
                  ))}
                  {(template.tags?.length || 0) > 2 && (
                    <span className="text-xs text-muted-foreground">
                      +{template.tags!.length - 2}
                    </span>
                  )}
                </div>
              </TableCell>
              <TableCell className="text-center">
                {template.popularity}
              </TableCell>
              <TableCell className="text-foreground">
                {formatDate(template.createdAt)}
              </TableCell>
              <TableCell>
                <div className="flex items-center justify-center gap-2">
                  {isFailed && (
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => onDelete(template)}
                    >
                      <Trash2 className="mr-1.5 size-3.5" />
                      Delete
                    </Button>
                  )}
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon">
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
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
};

export default EmailTemplateTable;
