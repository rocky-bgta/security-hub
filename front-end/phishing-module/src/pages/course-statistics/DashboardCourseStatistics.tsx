import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { IPhishingCourseStatistics } from 'models/Dashboard';
import { Link } from 'react-router-dom';
import {
  getSimulationLabel,
  getSimulationPaths,
  type SimulationChannel,
} from 'utils/SimulationChannel';

interface DashboardCourseStatisticsProps {
  data: IPhishingCourseStatistics | null;
  channel?: SimulationChannel;
}

/**
 * Phishing Course Statistics component
 */

const getLeftPosition = (percentage: number) => {
  if (percentage >= 90) return '65%';
  return `calc(${percentage}% + 5px)`;
};

const DashboardCourseStatistics = ({
  data,
  channel = 'phishing',
}: DashboardCourseStatisticsProps) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">
          {getSimulationLabel(channel)} Course Statistics
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="mb-6 flex items-center justify-between">
          <p>Total Assigned Courses: {data?.totalUsers || 0}</p>
          <p>Completion Rate: {data?.completePercentage || 0}%</p>
        </div>

        <div className="flex flex-col gap-y-5">
          <div className="flex items-center gap-x-4">
            <p className="w-3/12">Complete:</p>
            <p className="relative h-8 w-8/12">
              <p
                className="h-full rounded-md bg-primary"
                style={{
                  width: `${data?.completePercentage || 0}%`,
                }}
              ></p>
              <span
                className="absolute top-1/2 w-max -translate-y-1/2 pr-1"
                style={{
                  left: getLeftPosition(data?.completePercentage || 0),
                }}
              >
                {data?.completedUsers || 0}{' '}
                <span>({data?.completePercentage || 0}%)</span>
              </span>
            </p>
          </div>
          <div className="flex items-center gap-x-4">
            <p className="w-3/12">In Progress:</p>
            <p className="relative h-8 w-8/12">
              <p
                className="h-full rounded-md bg-primary"
                style={{
                  width: `${data?.inProgressPercentage || 0}%`,
                }}
              ></p>
              <span
                className="absolute top-1/2 w-max -translate-y-1/2"
                style={{
                  left: getLeftPosition(data?.inProgressPercentage || 0),
                }}
              >
                {data?.inProgressUsers || 0}{' '}
                <span>({data?.inProgressPercentage || 0}%)</span>
              </span>
            </p>
          </div>
          <div className="flex items-center gap-x-4">
            <p className="w-3/12">Pending:</p>
            <p className="relative h-8 w-8/12">
              <p
                className="h-full rounded-md bg-primary"
                style={{
                  width: `${data?.pendingPercentage || 0}%`,
                }}
              ></p>
              <span
                className="absolute top-1/2 w-max -translate-y-1/2"
                style={{
                  left: getLeftPosition(data?.pendingPercentage || 0),
                }}
              >
                {data?.pendingUsers || 0}{' '}
                <span>({data?.pendingPercentage || 0}%)</span>
              </span>
            </p>
          </div>
          <div className="flex items-center gap-x-4">
            <p className="w-3/12">Expired:</p>
            <p className="relative h-8 w-8/12">
              <p
                className="h-full rounded-md bg-primary"
                style={{
                  width: `${data?.expiredPercentage || 0}%`,
                }}
              ></p>
              <span
                className="absolute top-1/2 w-max -translate-y-1/2"
                style={{
                  left: getLeftPosition(data?.expiredPercentage || 0),
                }}
              >
                {data?.expiredUsers || 0}{' '}
                <span>({data?.expiredPercentage || 0}%)</span>
              </span>
            </p>
          </div>
        </div>

        <div className="mt-8 text-right">
          <Link
            to={getSimulationPaths(channel).courseStatistics}
            state={{ fromDashboard: true, channel }}
            className="text-sm font-medium text-primary"
          >
            More Details &gt;
          </Link>
        </div>
      </CardContent>
    </Card>
  );
};

export default DashboardCourseStatistics;
