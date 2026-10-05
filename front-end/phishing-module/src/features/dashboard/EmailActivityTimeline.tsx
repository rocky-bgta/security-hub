import { Tabs, TabsList, TabsTrigger } from 'common/Tabs';
import {
  ActivityType,
  getActivityTypeLabel,
  IEmailActivity,
} from 'models/Dashboard';
import { useState } from 'react';
import { ActivityItem } from './ActivityItem';
import { Card } from 'common/Card';
import { ActivityIcon } from 'lucide-react';
import { type SimulationChannel } from 'utils/SimulationChannel';

interface EmailActivityTimelineProps {
  activities: IEmailActivity[];
  loading?: boolean;
  onLoadMore?: () => void;
  hasMore?: boolean;
  onActivityClick?: (activity: IEmailActivity) => void;
  channel?: SimulationChannel;
}

/**
 * Timeline of email activities
 */
export const EmailActivityTimeline = ({
  activities,
  loading = false,
  onLoadMore,
  hasMore = false,
  onActivityClick,
  channel = 'phishing',
}: EmailActivityTimelineProps) => {
  const [filterType, setFilterType] = useState<ActivityType | 'ALL'>('ALL');

  const filteredActivities =
    filterType === 'ALL'
      ? activities
      : activities.filter(a => a.activityType === filterType);

  const activityTypes: (ActivityType | 'ALL')[] = [
    'ALL',
    ActivityType.EMAIL_OPENED,
    ActivityType.LINK_CLICKED,
    ActivityType.EMAIL_REPORTED,
    ActivityType.DATA_SUBMITTED,
  ];

  // Loading skeleton
  if (loading && activities.length === 0) {
    return (
      <Card className="p-6">
        <h3 className="mb-4 text-lg font-medium text-foreground">
          Recent Activity
        </h3>
        <div className="space-y-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="flex animate-pulse items-start gap-3">
              <div className="size-10 rounded-full bg-gray-200" />
              <div className="flex-1">
                <div className="mb-2 h-4 w-32 rounded bg-gray-200" />
                <div className="h-3 w-48 rounded bg-gray-100" />
              </div>
            </div>
          ))}
        </div>
      </Card>
    );
  }

  return (
    <Card className="h-full p-6">
      <div className="mb-4 flex flex-col justify-start space-y-2">
        <h3 className="w-60 text-lg font-medium text-foreground">
          Recent Activity
        </h3>

        {/* Filter buttons */}
        {/* <div className="flex gap-1 overflow-x-auto"> */}
        <Tabs
          value={filterType}
          onValueChange={value => setFilterType(value as ActivityType | 'ALL')}
          className="w-auto"
        >
          <TabsList>
            {activityTypes.map(type => (
              <TabsTrigger key={type} value={type} className="text-xs">
                {type === 'ALL' ? 'All' : getActivityTypeLabel(type, channel)}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>
        {/* </div> */}
      </div>

      {/* Activity list */}
      <div className="max-h-[300px] space-y-1 overflow-y-auto">
        {filteredActivities.length === 0 ? (
          <div className="py-8 text-center text-gray-400">
            <ActivityIcon className="mx-auto mb-2 size-12 opacity-50" />
            <p>No activities to show</p>
          </div>
        ) : (
          filteredActivities.map(activity => (
            <ActivityItem
              key={activity.activityId}
              activity={activity}
              channel={channel}
              onClick={() => onActivityClick?.(activity)}
            />
          ))
        )}
      </div>

      {/* Load more button */}
      {hasMore && (
        <div className="mt-4 text-center">
          <button
            onClick={onLoadMore}
            disabled={loading}
            className="px-4 py-2 text-sm font-medium text-primary hover:text-primary/80 disabled:opacity-50"
          >
            {loading ? 'Loading...' : 'View All'}
          </button>
        </div>
      )}
    </Card>
  );
};

export default EmailActivityTimeline;
