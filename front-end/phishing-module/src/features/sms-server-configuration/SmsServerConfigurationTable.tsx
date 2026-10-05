import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/Dropdown';
import { Skeleton } from 'components/LoadingSkeleton';
import { FileWarning, MoreHorizontal } from 'lucide-react';
import { IList } from 'models/Global';
import {
  ISmsServerConfiguration,
  getSmsProviderLabel,
} from 'models/SmsServerConfiguration';
import { formatDate } from 'utils/Helper';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import SmsServerConfigurationStatusBadge from './SmsServerConfigurationStatusBadge';

interface SmsServerConfigurationTableProps {
  configurations: IList<ISmsServerConfiguration>;
  loading?: boolean;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
  onSort?: (field: string) => void;
  onView: (configuration: ISmsServerConfiguration) => void;
  onEdit: (configuration: ISmsServerConfiguration) => void;
  onDelete: (configuration: ISmsServerConfiguration) => void;
}

export const SmsServerConfigurationTable = ({
  configurations,
  loading = false,
  sortBy,
  sortOrder,
  onSort,
  onView,
  onEdit,
  onDelete,
}: SmsServerConfigurationTableProps) => {
  const columns = [
    { key: 'name', label: 'Name', sortable: true },
    { key: 'provider', label: 'Provider', sortable: true },
    { key: 'senderId', label: 'Sender ID', sortable: true },
    { key: 'baseUrl', label: 'Base URL', sortable: false },
    { key: 'apiKeyMasked', label: 'API Key', sortable: false },
    { key: 'status', label: 'Status', sortable: true },
    { key: 'createdAt', label: 'Created', sortable: true },
    { key: 'actions', label: 'Actions', sortable: false },
  ];

  const handleSort = (key: string) => {
    if (onSort) {
      onSort(key);
    }
  };

  const getSortIcon = (key: string) => {
    if (sortBy !== key) return null;
    return sortOrder === 'asc' ? '↑' : '↓';
  };

  if (loading) {
    return (
      <Table>
        <TableHeader>
          <TableRow>
            {columns.map(col => (
              <TableHead key={col.key}>{col.label}</TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {[...Array(5)].map((_, index) => (
            <TableRow key={index}>
              {columns.map(col => (
                <TableCell key={col.key} className="px-6 py-4">
                  <Skeleton className="h-4" />
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    );
  }

  if (configurations?.items?.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <FileWarning className="mb-4 size-16 text-muted-foreground" />
        <h3 className="mb-2 text-lg font-medium text-primary">
          No SMS Server Configurations
        </h3>
        <p className="text-muted-foreground">
          Create your first SMS server configuration to get started
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <Table>
        <TableHeader>
          <TableRow>
            {columns.map(col => (
              <TableHead
                key={col.key}
                className={`px-6 py-3 text-left text-xs font-medium tracking-wider ${
                  col.sortable ? 'cursor-pointer hover:bg-white/10' : ''
                }`}
                onClick={() => col.sortable && handleSort(col.key)}
              >
                <div className="flex items-center gap-1">
                  {col.label}
                  {col.sortable && (
                    <span className="text-gray-400">{getSortIcon(col.key)}</span>
                  )}
                </div>
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {configurations?.items?.map(configuration => (
            <TableRow key={configuration.id}>
              <TableCell className="whitespace-nowrap px-6 py-4">
                <div className="flex items-center gap-2">
                  <span>{configuration.name}</span>
                  {configuration.default && (
                    <Badge variant="outline" className="text-xs">
                      Default
                    </Badge>
                  )}
                </div>
              </TableCell>
              <TableCell className="px-6 py-4">
                {getSmsProviderLabel(configuration.provider)}
              </TableCell>
              <TableCell className="px-6 py-4">{configuration.senderId}</TableCell>
              <TableCell className="max-w-[200px] truncate px-6 py-4">
                {configuration.baseUrl}
              </TableCell>
              <TableCell className="px-6 py-4 font-mono text-xs">
                {configuration.apiKeyMasked}
              </TableCell>
              <TableCell className="px-6 py-4">
                <SmsServerConfigurationStatusBadge status={configuration.status} />
              </TableCell>
              <TableCell className="whitespace-nowrap px-6 py-4">
                {formatDate(configuration.createdAt)}
              </TableCell>
              <TableCell className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon">
                      <MoreHorizontal className="size-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem onClick={() => onView(configuration)}>
                      View
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => onEdit(configuration)}>
                      Edit
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => onDelete(configuration)}>
                      Delete
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};

export default SmsServerConfigurationTable;
