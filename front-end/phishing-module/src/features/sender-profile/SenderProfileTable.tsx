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
import { ISenderProfile } from 'models/SenderProfile';
import ProfileTypeBadge from './ProfileTypeBadge';
import VerificationStatusIcon from './VerificationStatusIcon';
import {
  Table,
  TableBody,
  TableCell,
  TableRow,
  TableHead,
  TableHeader,
} from 'common/Table';

interface SenderProfileTableProps {
  profiles: IList<ISenderProfile>;
  loading?: boolean;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
  onSort?: (field: string) => void;
  onEdit: (profile: ISenderProfile) => void;
  onDuplicate: (profile: ISenderProfile) => void;
  onDelete: (profile: ISenderProfile) => void;
  onTest: (profile: ISenderProfile) => void;
}

/**
 * Table component for displaying sender profiles
 * Based on Task-06 Sender Profile Management (AC-02)
 */
export const SenderProfileTable = ({
  profiles,
  loading = false,
  sortBy,
  sortOrder,
  onSort,
  onEdit,
  onDuplicate,
  onDelete,
  onTest,
}: SenderProfileTableProps) => {
  const columns = [
    { key: 'profileName', label: 'Profile Name', sortable: true },
    { key: 'fromAddress', label: 'From Address', sortable: true },
    { key: 'host', label: 'Host:Port', sortable: true },
    { key: 'profileType', label: 'Type', sortable: true },
    { key: 'verified', label: 'Status', sortable: true },
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

  if (profiles?.items?.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <FileWarning className="mb-4 size-16 text-muted-foreground" />
        <h3 className="mb-2 text-lg font-medium text-primary">
          No Sender Profiles
        </h3>
        <p className="text-muted-foreground">
          Create your first sender profile to get started
        </p>
      </div>
    );
  }

  return (
    <div>
      <div className="overflow-x-auto">
        <Table>
          <TableHeader className="">
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
                      <span className="text-gray-400">
                        {getSortIcon(col.key)}
                      </span>
                    )}
                  </div>
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {profiles?.items?.map(profile => {
              const actions = [
                {
                  label: 'Test Connection',
                  onClick: () => onTest(profile),
                  icon: 'test',
                },
                {
                  label: 'Delete',
                  onClick: () => onDelete(profile),
                  icon: 'delete',
                  danger: true,
                },
                ...(profile.canEdit
                  ? [
                      {
                        label: 'Edit',
                        onClick: () => onEdit(profile),
                        icon: 'edit',
                      },
                    ]
                  : []),
                {
                  label: 'Duplicate',
                  onClick: () => onDuplicate(profile),
                  icon: 'duplicate',
                },
              ];

              return (
                <TableRow key={profile.profileId}>
                  <TableCell className="whitespace-nowrap px-6 py-4">
                    <div className="flex flex-col">
                      <span>{profile.profileName}</span>
                      {profile.displayName && (
                        <span className="text-xs text-gray-500">
                          {profile.displayName}
                        </span>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>{profile.fromAddress}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1">
                      <span>
                        {profile.host}:{profile.port}
                      </span>
                      {profile.useTls && (
                        <span className="text-xs font-medium text-green-600">
                          (TLS)
                        </span>
                      )}
                    </div>
                  </TableCell>
                  <TableCell className="whitespace-nowrap px-6 py-4">
                    <ProfileTypeBadge type={profile.profileType} />
                  </TableCell>
                  <TableCell className="whitespace-nowrap px-6 py-4">
                    <VerificationStatusIcon profile={profile} showLabel />
                  </TableCell>
                  <TableCell className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreHorizontal className="size-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        {actions?.map(
                          (action: { label: string; onClick: () => void }) => (
                            <DropdownMenuItem
                              key={action?.label}
                              onClick={action?.onClick}
                            >
                              {action?.label}
                            </DropdownMenuItem>
                          ),
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};

export default SenderProfileTable;
