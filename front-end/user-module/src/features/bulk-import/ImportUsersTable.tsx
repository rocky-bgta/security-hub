import { Badge } from 'common/Badge';
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
  getDisplayName,
  getFailureReasonLabel,
} from 'features/bulk-import/utils';
import { AlertCircle } from 'lucide-react';
import { IBulkImportUser, IBulkImportUserDraft } from 'models/BulkImport';
import { IDropdownOption } from 'models/Dropdown';
import { IList } from 'models/Global';
import { EMAIL_ADDRESS_MAX_LENGTH } from 'utils/Helper';

interface IProps {
  title: string;
  titleClassName?: string;
  users: IList<IBulkImportUser>;
  variant: 'valid' | 'invalid';
  editable?: boolean;
  loading?: boolean;
  drafts?: Record<number, IBulkImportUserDraft>;
  departmentOptions?: Array<IDropdownOption>;
  showPhone?: boolean;
  paginationKey: string;
  onDraftChange?: (
    rowIndex: number,
    patch: Partial<IBulkImportUserDraft>,
  ) => void;
  onPageChange: (page: number) => void;
}

const FailureReasonTooltip = ({ reason }: { reason?: string }) => {
  if (!reason) return <span className="text-muted-foreground">—</span>;

  return (
    <div className="group relative inline-flex items-center">
      <AlertCircle className="size-4 text-yellow-400" />
      <span className="pointer-events-none absolute bottom-full left-1/2 z-20 mb-2 hidden -translate-x-1/2 whitespace-nowrap rounded bg-black/90 px-2 py-1 text-xs text-white group-hover:block">
        {getFailureReasonLabel(reason)}
      </span>
    </div>
  );
};

const ImportUsersTable = ({
  title,
  titleClassName,
  users,
  variant,
  editable = false,
  loading = false,
  drafts = {},
  departmentOptions = [],
  showPhone = false,
  paginationKey,
  onDraftChange,
  onPageChange,
}: IProps) => {
  const items = users.items ?? [];
  const extraDepartments = Array.from(
    new Set(
      items
        .map(user => drafts[user.rowIndex]?.department ?? user.department ?? '')
        .filter(
          name =>
            Boolean(name) &&
            !departmentOptions.some(option => option.name === name),
        ),
    ),
  );
  const selectOptions = [
    ...departmentOptions,
    ...extraDepartments.map(name => ({ id: name, name })),
  ];

  return (
    <div className="space-y-3">
      <h3 className={titleClassName ?? 'text-lg font-semibold text-foreground'}>
        {title}
      </h3>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-12">#</TableHead>
            <TableHead>Full Name</TableHead>
            <TableHead>Email</TableHead>
            {showPhone && <TableHead>Phone</TableHead>}
            <TableHead>Department</TableHead>
            <TableHead>{variant === 'valid' ? 'Status' : 'Reason'}</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading && items.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={showPhone ? 6 : 5}
                className="py-8 text-center text-muted-foreground"
              >
                Loading...
              </TableCell>
            </TableRow>
          ) : items.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={showPhone ? 6 : 5}
                className="py-8 text-center text-muted-foreground"
              >
                {variant === 'valid' ? 'No valid users' : 'No invalid users'}
              </TableCell>
            </TableRow>
          ) : (
            items.map((user, index) => {
              const draft = drafts[user.rowIndex];
              const fullName = draft?.fullName ?? getDisplayName(user);
              const email = draft?.email ?? user.email ?? '';
              const phoneNumber = draft?.phoneNumber ?? user.phoneNumber ?? '';
              const department = draft?.department ?? user.department ?? '';
              const rowNumber = (users.offset ?? 0) + index + 1;

              return (
                <TableRow key={user.rowIndex}>
                  <TableCell>{rowNumber}</TableCell>
                  <TableCell>
                    {editable ? (
                      <Input
                        className="h-9"
                        value={fullName}
                        maxLength={200}
                        onChange={event =>
                          onDraftChange?.(user.rowIndex, {
                            fullName: event.target.value,
                          })
                        }
                      />
                    ) : (
                      fullName || '—'
                    )}
                  </TableCell>
                  <TableCell>
                    {editable ? (
                      <Input
                        className="h-9"
                        type="email"
                        value={email}
                        maxLength={EMAIL_ADDRESS_MAX_LENGTH}
                        onChange={event =>
                          onDraftChange?.(user.rowIndex, {
                            email: event.target.value,
                          })
                        }
                      />
                    ) : (
                      email || '—'
                    )}
                  </TableCell>
                  {showPhone && (
                    <TableCell>
                      {editable ? (
                        <Input
                          className="h-9"
                          value={phoneNumber}
                          maxLength={20}
                          onChange={event =>
                            onDraftChange?.(user.rowIndex, {
                              phoneNumber: event.target.value,
                            })
                          }
                        />
                      ) : (
                        phoneNumber || '—'
                      )}
                    </TableCell>
                  )}
                  <TableCell>
                    {editable ? (
                      <Select
                        value={department || undefined}
                        onValueChange={value =>
                          onDraftChange?.(user.rowIndex, {
                            department: value,
                          })
                        }
                      >
                        <SelectTrigger className="h-9">
                          <SelectValue placeholder="Select" />
                        </SelectTrigger>
                        <SelectContent>
                          {selectOptions.map(option => (
                            <SelectItem key={option.id} value={option.name}>
                              {option.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    ) : (
                      department || '—'
                    )}
                  </TableCell>
                  <TableCell>
                    {variant === 'valid' ? (
                      <Badge className="bg-green-500 text-white hover:bg-green-500">
                        Valid
                      </Badge>
                    ) : (
                      <FailureReasonTooltip reason={user.failureReason} />
                    )}
                  </TableCell>
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
      <div className="flex justify-end">
        <Pagination
          key={paginationKey}
          total={users.total}
          perPage={users.pageSize || 10}
          onPageChange={onPageChange}
        />
      </div>
    </div>
  );
};

export default ImportUsersTable;
