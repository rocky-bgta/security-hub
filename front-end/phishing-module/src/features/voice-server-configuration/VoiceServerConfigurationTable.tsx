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
import { Skeleton } from 'components/LoadingSkeleton';
import { FileWarning, MoreHorizontal } from 'lucide-react';
import { IList } from 'models/Global';
import {
  canDeleteVoiceServerConfiguration,
  canEditVoiceServerConfiguration,
  IVoiceServerConfiguration,
  resolveVoiceProviderLabel,
} from 'models/VoiceServerConfiguration';
import { formatDate } from 'utils/Helper';
import VoiceServerConfigurationStatusBadge from './VoiceServerConfigurationStatusBadge';

interface VoiceServerConfigurationTableProps {
  configurations: IList<IVoiceServerConfiguration>;
  loading?: boolean;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
  onSort?: (field: string) => void;
  onView: (configuration: IVoiceServerConfiguration) => void;
  onEdit: (configuration: IVoiceServerConfiguration) => void;
  onDelete: (configuration: IVoiceServerConfiguration) => void;
}

export const VoiceServerConfigurationTable = ({
  configurations,
  loading = false,
  sortBy,
  sortOrder,
  onSort,
  onView,
  onEdit,
  onDelete,
}: VoiceServerConfigurationTableProps) => {
  const columns = [
    { key: 'name', label: 'Name', sortable: true },
    { key: 'provider', label: 'Provider', sortable: true },
    { key: 'callerId', label: 'Caller ID', sortable: true },
    { key: 'countryCode', label: 'Country', sortable: true },
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
          No Voice Server Configurations
        </h3>
        <p className="text-muted-foreground">
          Create your first voice server configuration to get started
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
                {resolveVoiceProviderLabel(configuration.provider)}
              </TableCell>
              <TableCell className="px-6 py-4">
                {configuration.callerId || '—'}
              </TableCell>
              <TableCell className="px-6 py-4">
                {configuration.countryCode || configuration.region || '—'}
              </TableCell>
              <TableCell className="px-6 py-4 font-mono text-xs">
                {configuration.apiKeyMasked || '—'}
              </TableCell>
              <TableCell className="px-6 py-4">
                <VoiceServerConfigurationStatusBadge
                  status={configuration.status}
                />
              </TableCell>
              <TableCell className="whitespace-nowrap px-6 py-4">
                {configuration.createdAt
                  ? formatDate(configuration.createdAt)
                  : '—'}
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
                    {canEditVoiceServerConfiguration(configuration) ? (
                      <DropdownMenuItem onClick={() => onEdit(configuration)}>
                        Edit
                      </DropdownMenuItem>
                    ) : null}
                    {canDeleteVoiceServerConfiguration(configuration) ? (
                      <DropdownMenuItem
                        onClick={() => onDelete(configuration)}
                      >
                        Delete
                      </DropdownMenuItem>
                    ) : null}
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

export default VoiceServerConfigurationTable;
