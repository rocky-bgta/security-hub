import { Badge } from 'common/Badge';
import { IDepartmentUserCount } from './types';
import { cn } from 'utils/Helper';

interface DepartmentAudienceListProps {
  departmentUserCounts: IDepartmentUserCount[];
  selectedIds: string[];
  onToggle: (id: string) => void;
  onToggleAll: () => void;
}

export const DepartmentAudienceList = ({
  departmentUserCounts,
  selectedIds,
  onToggle,
  onToggleAll,
}: DepartmentAudienceListProps) => {
  const isAllSelected =
    departmentUserCounts.length > 0 &&
    departmentUserCounts.every(dept =>
      selectedIds.includes(dept.departmentName),
    );
  const totalUsers = departmentUserCounts.reduce(
    (sum, dept) => sum + dept.userCount,
    0,
  );

  return (
    <div className="mb-6">
      <label className="mb-3 block text-sm font-medium text-foreground">
        Select Departments
      </label>
      <div className="space-y-1">
        {departmentUserCounts.length > 0 && (
          <label
            className={cn(
              'flex cursor-pointer items-center justify-between rounded-lg border p-3 transition-colors',
              isAllSelected
                ? 'border-primary bg-primary/10'
                : 'border-card-border hover:border-primary/40',
            )}
          >
            <div className="flex items-center gap-3">
              <input
                type="checkbox"
                checked={isAllSelected}
                onChange={onToggleAll}
                className="size-4 rounded border-gray-300 text-primary"
              />
              <span className="font-medium text-foreground">Select All</span>
            </div>
            <Badge variant="secondary">{totalUsers} users</Badge>
          </label>
        )}
        {departmentUserCounts.map(dept => (
          <label
            key={dept.departmentName}
            className={cn(
              'flex cursor-pointer items-center justify-between rounded-lg border p-3 transition-colors',
              selectedIds.includes(dept.departmentName)
                ? 'border-primary bg-primary/10'
                : 'border-card-border hover:border-primary/40',
            )}
          >
            <div className="flex items-center gap-3">
              <input
                type="checkbox"
                checked={selectedIds.includes(dept.departmentName)}
                onChange={() => onToggle(dept.departmentName)}
                className="size-4 rounded border-gray-300 text-primary"
              />
              <span className="font-medium text-foreground">
                {dept.departmentName}
              </span>
            </div>
            <Badge variant="secondary">{dept.userCount} users</Badge>
          </label>
        ))}
      </div>
    </div>
  );
};
