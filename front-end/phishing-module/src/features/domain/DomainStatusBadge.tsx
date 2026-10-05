import { Badge } from 'common/Badge';
import { Lock, LockOpen, ShieldAlert } from 'lucide-react';
import { DomainStatus, getDomainStatusLabel } from 'models/Domain';
import { cn } from 'utils/Helper';

interface DomainStatusBadgeProps {
  status: DomainStatus;
  onClick?: () => void;
  className?: string;
}

/**
 * Badge component for displaying domain verification status
 * - UNVERIFIED: Gray badge (clickable to open verify modal)
 * - VERIFIED: Green badge
 * - VERIFIED_AND_LOCKED: Blue badge with lock icon
 */
const DomainStatusBadge = ({
  status,
  onClick,
  className,
}: DomainStatusBadgeProps) => {
  const getVariant = () => {
    switch (status) {
      case DomainStatus.UNVERIFIED:
        return 'secondary';
      case DomainStatus.VERIFIED:
        return 'default';
      case DomainStatus.VERIFIED_AND_LOCKED:
        return 'outline';
      default:
        return 'secondary';
    }
  };

  const getIcon = () => {
    switch (status) {
      case DomainStatus.UNVERIFIED:
        return <ShieldAlert className="mr-1 size-3" />;
      case DomainStatus.VERIFIED:
        return <LockOpen className="mr-1 size-3" />;
      case DomainStatus.VERIFIED_AND_LOCKED:
        return <Lock className="mr-1 size-3" />;
      default:
        return null;
    }
  };

  const isClickable = status === DomainStatus.UNVERIFIED && onClick;

  return (
    <Badge
      variant={getVariant()}
      className={cn(
        'inline-flex items-center',
        isClickable && 'cursor-pointer hover:opacity-80',
        className,
      )}
      onClick={isClickable ? onClick : undefined}
      title={
        isClickable
          ? 'Click to verify this domain'
          : getDomainStatusLabel(status)
      }
    >
      {getIcon()}
      {getDomainStatusLabel(status)}
    </Badge>
  );
};

export default DomainStatusBadge;
