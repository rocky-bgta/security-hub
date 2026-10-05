import { Badge } from 'common/Badge';
import { IIdName, PayloadType } from 'models/EmailTemplate';
import { cn } from 'utils/Helper';

interface PayloadTypeBadgeProps {
  type: PayloadType | IIdName;
  className?: string;
}

/**
 * Badge component for displaying payload type with icon
 */
const PayloadTypeBadge = ({ type, className }: PayloadTypeBadgeProps) => {
  const label =
    typeof type === 'object' ? type.name : type;


  return (
    <Badge variant="outline" className={cn('text-xs', className)}>
      {label}
    </Badge>
  );
};

export default PayloadTypeBadge;
