import { Button } from 'common/Button';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { TableSkeleton } from 'components/LoadingSkeleton';
import IconBackButton from 'components/IconBackButton';
import { useReports } from 'hooks/UseReports';
import { RotateCcw, Search } from 'lucide-react';
import {
  ActivityType,
  getActivityTypeLabel,
  IEmailActivity,
} from 'models/Dashboard';
import { IGetListParams } from 'models/Global';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { formatDate } from 'utils/Helper';
import {
  getSimulationCopy,
  getSimulationPaths,
  toCampaignChannel,
  type SimulationChannel,
} from 'utils/SimulationChannel';

const ACTIVITY_TYPE_VALUES: ActivityType[] = [
  ActivityType.EMAIL_SENT,
  ActivityType.EMAIL_DELIVERED,
  ActivityType.EMAIL_BOUNCED,
  ActivityType.EMAIL_NOT_OPENED_BUT_REPORTED,
  ActivityType.EMAIL_OPENED,
  ActivityType.LINK_CLICKED,
  ActivityType.ATTACHMENT_OPENED,
  ActivityType.DATA_SUBMITTED,
  ActivityType.EMAIL_REPORTED,
];

const RecentActivity = ({
  channel = 'phishing',
}: {
  channel?: SimulationChannel;
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as {
    fromReports?: boolean;
    channel?: SimulationChannel;
  } | null;
  const fromReports = Boolean(locationState?.fromReports);
  const routeChannel = locationState?.channel || channel;
  const copy = getSimulationCopy(routeChannel);
  const activityTypeOptions = useMemo(
    () => [
      { label: 'All Activity', value: 'ALL' as const },
      ...ACTIVITY_TYPE_VALUES.map(value => ({
        label: getActivityTypeLabel(value, routeChannel),
        value,
      })),
    ],
    [routeChannel],
  );
  const { loading, fetchEmailActivity } = useReports(
    toCampaignChannel(routeChannel),
  );
  const [activities, setActivities] = useState<IEmailActivity[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: 10,
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [activityTypeFilter, setActivityTypeFilter] = useState<
    ActivityType | 'ALL'
  >('ALL');

  const loadActivities = useCallback(async () => {
    const result = await fetchEmailActivity({
      offset: queryParams.offset || 0,
      pageSize: queryParams.pageSize || 10,
      search: searchTerm || undefined,
      activityType:
        activityTypeFilter === 'ALL' ? undefined : activityTypeFilter,
    });

    setActivities(result.data);
    setTotalCount(result.totalCount);
  }, [
    fetchEmailActivity,
    queryParams.offset,
    queryParams.pageSize,
    searchTerm,
    activityTypeFilter,
  ]);

  useEffect(() => {
    const timer = setTimeout(() => {
      void loadActivities();
    }, 0);
    return () => clearTimeout(timer);
  }, [loadActivities]);

  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    setSearchTerm('');
    setActivityTypeFilter('ALL');
    setQueryParams(prev => ({ ...prev, offset: 0 }));
  };

  return (
    <div className="space-y-4">
      <div className="space-y-3">
        {fromReports && (
          <IconBackButton
            onClick={() => navigate(getSimulationPaths(routeChannel).reports)}
            label="Back to Reports"
          />
        )}
        <div>
          <h1 className="text-2xl font-bold text-foreground">
            Recent Activity
          </h1>
          <p className="text-sm text-muted-foreground">
            Track user interaction events across {copy.label.toLowerCase()}{' '}
            campaigns
          </p>
        </div>
      </div>

      <div>
        <div className="mb-6 grid grid-cols-1 gap-4 lg:grid-cols-7">
          <div className="relative lg:col-span-3">
            <Input
              type="text"
              value={searchTerm}
              onChange={e => {
                setSearchTerm(e.target.value);
                setQueryParams(prev => ({ ...prev, offset: 0 }));
              }}
              placeholder="Search by recipient or campaign..."
              className="w-full pl-10"
            />
            <Search className="absolute left-3 top-2.5 size-5 text-gray-400" />
          </div>

          <Select
            value={activityTypeFilter}
            onValueChange={value => {
              setActivityTypeFilter(value as ActivityType | 'ALL');
              setQueryParams(prev => ({ ...prev, offset: 0 }));
            }}
          >
            <SelectTrigger className="w-full lg:col-span-3">
              <SelectValue placeholder="Select activity type" />
            </SelectTrigger>
            <SelectContent>
              {activityTypeOptions.map(option => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <div className="col-span-1 w-full">
            <Button variant="outline" onClick={handleReset} className="w-full">
              <RotateCcw className="size-4" />
              Reset Filters
            </Button>
          </div>
        </div>

        <div>
          {loading ? (
            <TableSkeleton />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Activity</TableHead>
                  <TableHead>Recipient Name</TableHead>
                  <TableHead>Recipient Email</TableHead>
                  <TableHead>Campaign Name</TableHead>
                  <TableHead>Timestamp</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {activities.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center">
                      No activity found
                    </TableCell>
                  </TableRow>
                ) : (
                  activities.map(activity => (
                    <TableRow key={activity.activityId}>
                      <TableCell>
                        {getActivityTypeLabel(activity.activityType, routeChannel)}
                      </TableCell>
                      <TableCell>{activity.recipientName || '-'}</TableCell>
                      <TableCell>{activity.recipientEmail || '-'}</TableCell>
                      <TableCell>{activity.campaignName || '-'}</TableCell>
                      <TableCell>{formatDate(activity.timestamp)}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}

          {totalCount > (queryParams.pageSize || 10) && (
            <div className="mt-6 flex justify-end">
              <Pagination
                total={totalCount}
                perPage={queryParams.pageSize || 10}
                currentPage={(queryParams.offset || 0) + 1}
                onPageChange={handlePageChange}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default RecentActivity;
