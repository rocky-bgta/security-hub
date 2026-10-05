import { Badge } from 'common/Badge';
import { formatEnum, getRiskGroupBadgeVariant } from './helpers';
import { IRiskGroupUserCount } from './types';
import { cn } from 'utils/Helper';

interface RiskGroupAudienceListProps {
  riskGroupUserCounts: IRiskGroupUserCount[];
  selectedIds: string[];
  onToggle: (id: string) => void;
  onToggleAll: () => void;
}

export const RiskGroupAudienceList = ({
  riskGroupUserCounts,
  selectedIds,
  onToggle,
  onToggleAll,
}: RiskGroupAudienceListProps) => {
  const isAllSelected =
    riskGroupUserCounts.length > 0 &&
    riskGroupUserCounts.every(group => selectedIds.includes(group.riskGroup));
  const totalUsers = riskGroupUserCounts.reduce(
    (sum, group) => sum + group.userCount,
    0,
  );

  return (
    <div className="mb-6">
      <label className="mb-3 block text-sm font-medium text-foreground">
        Select Risk Groups
      </label>
      <div className="space-y-1">
        {riskGroupUserCounts.length > 0 && (
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
        {riskGroupUserCounts.map(group => (
          <label
            key={group.riskGroup}
            className={cn(
              'flex cursor-pointer items-center justify-between rounded-lg border p-3 transition-colors',
              selectedIds.includes(group.riskGroup)
                ? 'border-primary bg-primary/10'
                : 'border-card-border hover:border-primary/40',
            )}
          >
            <div className="flex items-center gap-3">
              <input
                type="checkbox"
                checked={selectedIds.includes(group.riskGroup)}
                onChange={() => onToggle(group.riskGroup)}
                className="size-4 rounded border-gray-300 text-primary"
              />
              <span className="font-medium text-foreground">
                {formatEnum(group.riskGroup)}
              </span>
            </div>
            <Badge variant={getRiskGroupBadgeVariant(group.riskGroup)}>
              {group.userCount} users
            </Badge>
          </label>
        ))}
      </div>
    </div>
  );
};
