import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
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
import { useCourseStatistics } from 'hooks/UseCourseStatistics';
import { SearchIcon } from 'lucide-react';
import { IPhishingCourseDetails } from 'models/Dashboard';
import { IGetListParams } from 'models/Global';
import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  getSimulationLabel,
  getSimulationPaths,
  toCampaignChannel,
  type SimulationChannel,
} from 'utils/SimulationChannel';

/**
 * Campaign Reports page
 */
const CourseStatisticsDetails = ({
  channel = 'phishing',
}: {
  channel?: SimulationChannel;
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const fromDashboard = Boolean(
    (location.state as { fromDashboard?: boolean } | null)?.fromDashboard,
  );
  const routeChannel =
    (location.state as { channel?: SimulationChannel } | null)?.channel ||
    channel;
  const { fetchPhishingCourseDetails } = useCourseStatistics();

  // State
  const [loading, setLoading] = useState(true);
  const [courseDetails, setCourseDetails] = useState<IPhishingCourseDetails[]>(
    [],
  );
  const [totalCount, setTotalCount] = useState(0);

  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: 10,
    search: '',
  });

  // Load courseDetails
  const loadCourseDetails = useCallback(async () => {
    setLoading(true);
    const result = await fetchPhishingCourseDetails({
      ...queryParams,
      channel: toCampaignChannel(routeChannel),
    });

    setLoading(false);
    if (!result) return;

    setCourseDetails(result.items);
    setTotalCount(result.total);
  }, [fetchPhishingCourseDetails, queryParams, routeChannel]);

  useEffect(() => {
    setTimeout(() => {
      loadCourseDetails();
    }, 0);
  }, [loadCourseDetails]);

  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  return (
    <>
      {/* Header */}
      <div className="mb-6 space-y-3">
        {fromDashboard && (
          <IconBackButton
            onClick={() =>
              navigate(getSimulationPaths(routeChannel).dashboard)
            }
            label="Back to Dashboard"
          />
        )}
        <div className="space-y-1">
          <h1 className="text-3xl font-bold text-foreground">
            {getSimulationLabel(routeChannel)} Campaign Course Statistics
          </h1>
          <p className="text-muted-foreground">
            Detailed course statistics for all campaigns
          </p>
        </div>
      </div>

      {/* Search */}
      <div className="mb-6">
        <div className="relative">
          <Input
            type="text"
            value={queryParams.search || ''}
            onChange={e =>
              setQueryParams(prev => ({ ...prev, search: e.target.value }))
            }
            placeholder="Search user..."
            className="pl-10"
          />
          <SearchIcon className="absolute left-3 top-2.5 size-5 text-muted-foreground" />
        </div>
      </div>

      {/* Campaign Table */}
      <div>
        {loading ? (
          <TableSkeleton />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Campaign</TableHead>
                <TableHead className="text-center">User Email</TableHead>
                <TableHead className="text-center">User Name</TableHead>
                <TableHead className="text-center">Department</TableHead>
                <TableHead className="text-center">Assigned Date</TableHead>
                <TableHead>Expired Date</TableHead>
                <TableHead>Progress Status</TableHead>
                <TableHead>Completion</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {courseDetails?.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="text-center">
                    No details found
                  </TableCell>
                </TableRow>
              ) : (
                courseDetails.map((details, idx) => (
                  <TableRow key={idx}>
                    <TableCell>{details.campaignName || '-'}</TableCell>
                    <TableCell>{details.email || '-'}</TableCell>
                    <TableCell>{details.userName || '-'}</TableCell>
                    <TableCell className="text-center">
                      {details.department || '-'}
                    </TableCell>
                    <TableCell className="text-center">
                      {details.assignedDate
                        ? new Date(details.assignedDate).toLocaleDateString()
                        : '-'}
                    </TableCell>
                    <TableCell>
                      {details.expireDate
                        ? new Date(details.expireDate).toLocaleDateString()
                        : '-'}
                    </TableCell>
                    <TableCell>{details.status || '-'}</TableCell>
                    <TableCell>{details.progress || '-'}%</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}

        {/* Pagination */}
        <div className="mt-6 flex justify-end">
          <Pagination
            total={totalCount}
            perPage={queryParams.pageSize || 10}
            currentPage={(queryParams.offset || 0) + 1}
            onPageChange={handlePageChange}
          />
        </div>
      </div>
    </>
  );
};

export default CourseStatisticsDetails;
