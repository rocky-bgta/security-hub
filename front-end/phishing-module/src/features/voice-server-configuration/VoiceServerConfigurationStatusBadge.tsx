import { Badge } from 'common/Badge';
import {
  VoiceServerConfigurationStatus,
  VOICE_SERVER_CONFIGURATION_STATUS_LABELS,
} from 'models/VoiceServerConfiguration';

interface VoiceServerConfigurationStatusBadgeProps {
  status?: VoiceServerConfigurationStatus;
}

const statusVariantMap: Record<
  VoiceServerConfigurationStatus,
  'default' | 'secondary' | 'destructive'
> = {
  [VoiceServerConfigurationStatus.ACTIVE]: 'default',
  [VoiceServerConfigurationStatus.INACTIVE]: 'secondary',
};

export const VoiceServerConfigurationStatusBadge = ({
  status = VoiceServerConfigurationStatus.INACTIVE,
}: VoiceServerConfigurationStatusBadgeProps) => (
  <Badge variant={statusVariantMap[status]}>
    {VOICE_SERVER_CONFIGURATION_STATUS_LABELS[status]}
  </Badge>
);

export default VoiceServerConfigurationStatusBadge;
