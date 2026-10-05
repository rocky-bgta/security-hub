import {
  ActivityType,
  getActivityTypeIcon,
  getActivityTypeLabel,
  IEmailActivity,
} from 'models/Dashboard';
import { type SimulationChannel } from 'utils/SimulationChannel';

interface ActivityItemProps {
  activity: IEmailActivity;
  onClick?: () => void;
  channel?: SimulationChannel;
}

const getActivityColor = (type: ActivityType): string => {
  switch (type) {
    case ActivityType.EMAIL_SENT:
      return 'bg-blue-100 text-blue-600';
    case ActivityType.EMAIL_DELIVERED:
      return 'bg-green-100 text-green-600';
    case ActivityType.EMAIL_BOUNCED:
      return 'bg-gray-100 text-gray-600';
    case ActivityType.EMAIL_NOT_OPENED_BUT_REPORTED:
      return 'bg-cyan-100 text-cyan-600';
    case ActivityType.EMAIL_OPENED:
      return 'bg-purple-100 text-purple-600';
    case ActivityType.LINK_CLICKED:
      return 'bg-orange-100 text-orange-600';
    case ActivityType.ATTACHMENT_OPENED:
      return 'bg-yellow-100 text-yellow-600';
    case ActivityType.DATA_SUBMITTED:
      return 'bg-red-100 text-red-600';
    case ActivityType.EMAIL_REPORTED:
      return 'bg-emerald-100 text-emerald-600';
    default:
      return 'bg-gray-100 text-gray-600';
  }
};

/**
 * Individual activity item in the timeline
 */
export const ActivityItem = ({
  activity,
  onClick,
  channel = 'phishing',
}: ActivityItemProps) => {
  const formatTime = (timestamp: string): string => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <div
      className={`flex items-start gap-3 rounded-lg p-3 transition-colors ${
        onClick ? 'hover: cursor-pointer' : ''
      }`}
      onClick={onClick}
    >
      {/* Activity icon */}
      <div
        className={`flex size-10 shrink-0 items-center justify-center rounded-full text-lg ${getActivityColor(activity.activityType)}`}
      >
        {getActivityTypeIcon(activity.activityType)}
      </div>

      {/* Activity details */}
      <div className="min-w-0 flex-1">
        <div className="flex items-center justify-between gap-2">
          <span className="truncate font-medium text-foreground">
            {activity.recipientName || 'Unknown User'}
          </span>
          <span className="shrink-0 text-xs text-muted-foreground">
            {formatTime(activity.timestamp)}
          </span>
        </div>
        <p className="text-sm text-muted-foreground">
          {activity.activityLabel ||
            getActivityTypeLabel(activity.activityType, channel)}
        </p>
        {activity.campaignName && (
          <p className="truncate text-xs text-muted-foreground">
            Campaign Name: {activity.campaignName}
          </p>
        )}
        {/* {activity.geoLocation && (
          <p className="text-xs text-muted-foreground">
            {activity.geoLocation}
          </p>
        )} */}
      </div>
    </div>
  );
};

export default ActivityItem;
