import { Badge } from 'common/Badge';
import {
  SmsServerConfigurationStatus,
  SMS_SERVER_CONFIGURATION_STATUS_LABELS,
} from 'models/SmsServerConfiguration';

interface SmsServerConfigurationStatusBadgeProps {
  status: SmsServerConfigurationStatus;
}

const statusVariantMap: Record<
  SmsServerConfigurationStatus,
  'default' | 'secondary' | 'destructive'
> = {
  [SmsServerConfigurationStatus.ACTIVE]: 'default',
  [SmsServerConfigurationStatus.INACTIVE]: 'secondary',
};

export const SmsServerConfigurationStatusBadge = ({
  status,
}: SmsServerConfigurationStatusBadgeProps) => (
  <Badge variant={statusVariantMap[status]}>
    {SMS_SERVER_CONFIGURATION_STATUS_LABELS[status]}
  </Badge>
);

export default SmsServerConfigurationStatusBadge;
