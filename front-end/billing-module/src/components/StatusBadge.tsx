import { Badge } from 'common/Badge';
import { cn } from 'utils/Helper';

interface StatusBadgeProps {
  status: string;
  variant?: 'default' | 'secondary' | 'destructive' | 'outline';
  className?: string;
}

export const StatusBadge = ({
  status,
  variant,
  className,
}: StatusBadgeProps) => {
  const getStatusVariant = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return 'bg-[#16a34a] text-[#f8fafc]';
      case 'completed':
        return 'bg-[#16a34a] text-[#f8fafc]';
      case 'pending':
        return 'bg-[#eab308] text-[#020617]';
      case 'overdue':
        return 'bg-[#ef4444] text-[#f8fafc]';
      case 'failed':
        return 'bg-[#ef4444] text-[#f8fafc]';
      case 'suspended':
        return 'bg-[#ef4444] text-[#f8fafc]';
      case 'inactive':
        return 'bg-muted text-muted-foreground';
      default:
        return 'bg-[#0ea5e9] text-[#f8fafc]';
    }
  };

  const statusClass = variant ? '' : getStatusVariant(status);

  return (
    <Badge variant={variant} className={cn(statusClass, className)}>
      {status}
    </Badge>
  );
};
