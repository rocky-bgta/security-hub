import { Badge } from 'common/Badge';
import { cn } from 'utils/Helper';

interface StatusBadgeProps {
  status: string;
  variant?: 'default' | 'secondary' | 'destructive' | 'outline';
  className?: string;
}

const StatusBadge = ({ status, variant, className }: StatusBadgeProps) => {
  const getStatusVariant = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
      case 'completed':
        return 'bg-green-500 text-white';
      case 'pending':
        return 'bg-yellow-400 text-black';
      case 'overdue':
      case 'failed':
      case 'suspended':
        return 'bg-red-500 text-white';
      case 'inactive':
        return 'bg-gray-300 text-gray-700 hover:text-white';
      default:
        return 'bg-blue-500 text-white';
    }
  };

  const statusClass = variant ? '' : getStatusVariant(status);

  return (
    <Badge variant={variant} className={cn(statusClass, className)}>
      {status}
    </Badge>
  );
};

export { StatusBadge };
