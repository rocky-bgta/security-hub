import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import CustomSelect from 'common/CustomSelect';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import {
  Eye,
  Filter,
  Mail,
  Phone,
  RefreshCw,
  Search,
  Shield,
  UserRound,
  Users,
} from 'lucide-react';
import { useState } from 'react';
import { cn } from 'utils/Helper';
import {
  formatDate,
  formatEnum,
  getRiskGroupBadgeVariant,
  getStatusBadgeVariant,
} from './helpers';
import { IAudienceDirectory, IEndUser } from './types';

const USER_PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

interface IndividualUserPickerProps {
  directory: IAudienceDirectory;
  userIds: string[];
  onToggleUser: (userId: string) => void;
  onToggleSelectAll: () => void;
  onClearAll: () => void;
}

export const IndividualUserPicker = ({
  directory,
  userIds,
  onToggleUser,
  onToggleSelectAll,
  onClearAll,
}: IndividualUserPickerProps) => {
  const {
    users,
    usersLoading,
    departmentList,
    departmentFilter,
    riskGroupFilter,
    searchInput,
    setSearchInput,
    queryParams,
    clientAdminId,
    fetchUsers,
    handlePageChange,
    handlePageSizeChange,
    handleRiskGroupChange,
    handleDepartmentFilterChange,
    handleResetFilters,
  } = directory;

  const [viewUser, setViewUser] = useState<IEndUser | null>(null);

  const isAllSelected =
    users.items.length > 0 && users.items.every(u => userIds.includes(u.id));

  return (
    <div className="mb-6">
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Users className="size-5 text-muted-foreground" />
          <span className="text-sm font-medium text-foreground">
            Select Users
          </span>
          {userIds.length > 0 && (
            <Badge variant="default">{userIds.length} selected</Badge>
          )}
        </div>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => fetchUsers()}
          disabled={usersLoading}
        >
          <RefreshCw
            className={cn('mr-2 size-4', usersLoading && 'animate-spin')}
          />
          Refresh
        </Button>
      </div>

      <div className="mb-4 grid grid-cols-1 gap-3 md:grid-cols-6">
        <div className="relative col-span-2">
          <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
          <Input
            placeholder="Search users..."
            value={searchInput}
            onChange={e => setSearchInput(e.target.value)}
            className="w-full pl-9"
          />
        </div>

        <div className="col-span-2">
          <CustomSelect
            data={departmentList.map(item => ({
              id: item.id,
              label: item.name,
              value: item.name,
            }))}
            isMulti
            isSearchable
            customClassName="w-full"
            name="departmentFilter"
            placeholder="Departments..."
            value={departmentFilter}
            handleChange={handleDepartmentFilterChange}
          />
        </div>

        <div className="col-span-1">
          <Select value={riskGroupFilter} onValueChange={handleRiskGroupChange}>
            <SelectTrigger className="w-full">
              <Filter className="mr-1 size-4 text-muted-foreground" />
              <SelectValue placeholder="Risk Group" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Risk</SelectItem>
              <SelectItem value="HIGH_RISK">High Risk</SelectItem>
              <SelectItem value="MEDIUM_RISK">Medium Risk</SelectItem>
              <SelectItem value="LOW_RISK">Low Risk</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="col-span-1">
          <Button
            type="button"
            variant="outline"
            className="w-full"
            onClick={handleResetFilters}
          >
            Reset
          </Button>
        </div>
      </div>

      <div>
        {usersLoading ? (
          <div className="flex items-center justify-center p-8">
            <RefreshCw className="size-6 animate-spin text-muted-foreground" />
            <span className="ml-2 text-muted-foreground">Loading users…</span>
          </div>
        ) : (
          <>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-12">
                    <Checkbox
                      checked={isAllSelected}
                      onCheckedChange={onToggleSelectAll}
                      aria-label="Select all users on this page"
                    />
                  </TableHead>
                  <TableHead>Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Phone Number</TableHead>
                  <TableHead>Department</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Risk Group</TableHead>
                  <TableHead className="w-12 text-right">View</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {users.items.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={7}
                      className="p-8 text-center text-muted-foreground"
                    >
                      {clientAdminId
                        ? 'No users found matching your filters.'
                        : 'Please provide a clientAdminId to load users.'}
                    </TableCell>
                  </TableRow>
                ) : (
                  users.items.map(user => {
                    const isSelected = userIds.includes(user.id);
                    return (
                      <TableRow
                        key={user.id}
                        onClick={() => onToggleUser(user.id)}
                        className={cn(
                          'cursor-pointer transition-colors',
                          isSelected
                            ? 'bg-primary/10 hover:bg-primary/15'
                            : 'hover:bg-card-border/30',
                        )}
                      >
                        <TableCell onClick={e => e.stopPropagation()}>
                          <Checkbox
                            checked={isSelected}
                            onCheckedChange={() => onToggleUser(user.id)}
                            aria-label={`Select ${user.fullName}`}
                          />
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            {isSelected && (
                              <div className="size-2 shrink-0 rounded-full bg-primary" />
                            )}
                            <span
                              className={cn(
                                'font-medium',
                                isSelected && 'text-primary',
                              )}
                            >
                              {user.fullName}
                            </span>
                          </div>
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {user.email}
                        </TableCell>
                        <TableCell>{user.phoneNumber || '—'}</TableCell>
                        <TableCell>{user.department || '—'}</TableCell>
                        <TableCell>
                          <Badge variant={getStatusBadgeVariant(user.status)}>
                            {formatEnum(user.status || '')}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={getRiskGroupBadgeVariant(user.riskGroup)}
                          >
                            {formatEnum(user.riskGroup || '')}
                          </Badge>
                        </TableCell>
                        <TableCell
                          className="text-right"
                          onClick={e => e.stopPropagation()}
                        >
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            className="size-8"
                            onClick={() => setViewUser(user)}
                            aria-label={`View ${user.fullName}`}
                          >
                            <Eye className="size-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    );
                  })
                )}
              </TableBody>
            </Table>

            {users.total > 0 && (
              <div className="flex items-center justify-between border-t border-card-border p-4">
                <span className="text-sm text-muted-foreground">
                  Showing {users.items.length} of {users.total} users
                </span>
                <div className="flex items-center gap-3">
                  <Select
                    value={String(queryParams.pageSize)}
                    onValueChange={value => handlePageSizeChange(Number(value))}
                  >
                    <SelectTrigger className="w-[90px]" aria-label="Page size">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {USER_PAGE_SIZE_OPTIONS.map(size => (
                        <SelectItem key={size} value={String(size)}>
                          {size}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Pagination
                    total={users.total}
                    perPage={users.pageSize}
                    currentPage={queryParams.offset + 1}
                    onPageChange={handlePageChange}
                  />
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {userIds.length > 0 && (
        <div className="mt-4 rounded-lg border border-primary/30 bg-primary/10 p-3">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-foreground">
              {userIds.length} user{userIds.length !== 1 ? 's' : ''} selected
            </span>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={onClearAll}
            >
              Clear all
            </Button>
          </div>
        </div>
      )}

      <Dialog open={!!viewUser} onOpenChange={() => setViewUser(null)}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <UserRound className="size-5 text-primary" />
              User Details
            </DialogTitle>
          </DialogHeader>

          {viewUser && (
            <div className="space-y-4">
              <div className="rounded-lg border border-primary/20 bg-background p-4">
                <p className="text-lg font-semibold text-foreground">
                  {viewUser.fullName}
                </p>
                <div className="mt-2 flex flex-col gap-1.5 text-sm text-muted-foreground">
                  <span className="flex items-center gap-2">
                    <Mail className="size-4 shrink-0" />
                    {viewUser.email}
                  </span>
                  {viewUser.phoneNumber && (
                    <span className="flex items-center gap-2">
                      <Phone className="size-4 shrink-0" />
                      {viewUser.phoneNumber}
                    </span>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="rounded-lg border border-card-border p-3">
                  <p className="mb-1 text-xs uppercase tracking-wide text-muted-foreground">
                    Department
                  </p>
                  <p className="text-sm font-medium text-foreground">
                    {viewUser.department || '—'}
                  </p>
                </div>

                <div className="rounded-lg border border-card-border p-3">
                  <p className="mb-1 text-xs uppercase tracking-wide text-muted-foreground">
                    Status
                  </p>
                  <Badge variant={getStatusBadgeVariant(viewUser.status)}>
                    {formatEnum(viewUser.status || '')}
                  </Badge>
                </div>

                <div className="rounded-lg border border-card-border p-3">
                  <p className="mb-1 flex items-center gap-1 text-xs uppercase tracking-wide text-muted-foreground">
                    <Shield className="size-3" /> Risk Group
                  </p>
                  <Badge variant={getRiskGroupBadgeVariant(viewUser.riskGroup)}>
                    {formatEnum(viewUser.riskGroup || '')}
                  </Badge>
                </div>

                <div className="rounded-lg border border-card-border p-3">
                  <p className="mb-1 text-xs uppercase tracking-wide text-muted-foreground">
                    Last Login
                  </p>
                  <p className="text-sm font-medium text-foreground">
                    {formatDate(viewUser.lastLoginAt)}
                  </p>
                </div>
              </div>

              <div className="flex justify-end gap-2 border-t border-card-border pt-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setViewUser(null)}
                >
                  Close
                </Button>
                <Button
                  type="button"
                  variant={
                    userIds.includes(viewUser.id) ? 'destructive' : 'default'
                  }
                  onClick={() => {
                    onToggleUser(viewUser.id);
                    setViewUser(null);
                  }}
                >
                  {userIds.includes(viewUser.id)
                    ? 'Deselect User'
                    : 'Select User'}
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};
