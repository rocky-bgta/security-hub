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
import { useDashboard } from 'hooks/UseDashboard';
import { toCampaignChannel } from 'utils/SimulationChannel';
import { SearchIcon } from 'lucide-react';
import {
  getRiskLevelLabel,
  IDashboardRiskImpact,
  RISK_COLORS,
  RiskLevel,
} from 'models/Dashboard';
import { IGetListParams } from 'models/Global';
import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { cn } from 'utils/Helper';
import {
  getSimulationPaths,
  type SimulationChannel,
} from 'utils/SimulationChannel';

/**
 * Campaign Reports page
 */
const CampaignRiskImpacts = ({
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
  const { fetchRiskImpact } = useDashboard();

  // State
  const [loading, setLoading] = useState(true);
  const [campaigns, setCampaigns] = useState<IDashboardRiskImpact[]>([]);
  const [totalCount, setTotalCount] = useState(0);

  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: 10,
    searchParam: '',
  });

  // Load campaigns
  const loadCampaigns = useCallback(async () => {
    setLoading(true);
    const result = await fetchRiskImpact({
      ...queryParams,
      channel: toCampaignChannel(routeChannel),
    });

    setLoading(false);
    if (!result) return;

    setCampaigns(result.items);
    setTotalCount(result.total);
  }, [fetchRiskImpact, queryParams, routeChannel]);

  useEffect(() => {
    setTimeout(() => {
      loadCampaigns();
    }, 0);
  }, [loadCampaigns]);

  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  const getRiskColor = (level: RiskLevel) => {
    return RISK_COLORS[level] || '#667eea';
  };

  const getAiRatingColor = (rating: string) => {
    switch (rating?.toLowerCase()) {
      case 'too effective':
        return 'text-emerald-400';
      case 'effective':
        return 'text-cyan-400';
      case 'balanced':
        return 'text-orange-400';
      case 'ineffective':
        return 'text-red-400';
      default:
        return 'text-gray-400';
    }
  };

  const getAiRatingBg = (rating: string) => {
    switch (rating?.toLowerCase()) {
      case 'too effective':
        return 'bg-emerald-500/10';
      case 'effective':
        return 'bg-cyan-500/10';
      case 'balanced':
        return 'bg-orange-500/10';
      case 'ineffective':
        return 'bg-red-500/10';
      default:
        return 'bg-gray-500/10';
    }
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
            Campaign Risk Impacts
          </h1>
          <p className="text-muted-foreground">
            Detailed risk impacts for all campaigns
          </p>
        </div>
      </div>

      {/* Search */}
      <div className="mb-6">
        <div className="relative">
          <Input
            type="text"
            value={queryParams.searchParam || ''}
            onChange={e =>
              setQueryParams(prev => ({ ...prev, searchParam: e.target.value }))
            }
            placeholder="Search campaigns..."
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
                <TableHead>Type</TableHead>
                <TableHead className="text-center">Target Group</TableHead>
                <TableHead className="text-center">Risk Impact</TableHead>
                <TableHead className="text-center">AI Rating</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Start Date</TableHead>
                <TableHead>End Date</TableHead>
                {/* <TableHead>Actions</TableHead> */}
              </TableRow>
            </TableHeader>
            <TableBody>
              {campaigns.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="text-center">
                    No campaigns found
                  </TableCell>
                </TableRow>
              ) : (
                campaigns.map(campaign => (
                  <TableRow key={campaign.campaignId}>
                    <TableCell>{campaign.campaignName}</TableCell>
                    <TableCell className="capitalize">
                      {campaign.type?.toLowerCase().replace(/_/g, ' ') || '-'}
                    </TableCell>
                    <TableCell className="text-center capitalize">
                      {campaign.targetGroup?.toLowerCase().replace(/_/g, ' ') ||
                        '-'}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <span
                          className="size-2.5 rounded-full"
                          style={{
                            backgroundColor: getRiskColor(campaign.riskImpact),
                          }}
                        />
                        <span>
                          {getRiskLevelLabel(campaign.riskImpact) || 'N/A'}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell className="flex justify-center">
                      <div className="flex w-28 items-center justify-center text-xs font-medium">
                        <span
                          className={cn(
                            'px-3 py-1 rounded',
                            getAiRatingBg(campaign.aiRating),
                            getAiRatingColor(campaign.aiRating),
                          )}
                        >
                          {campaign.aiRating || 'N/A'}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>{campaign.status || '-'}</TableCell>
                    <TableCell>
                      {campaign.startDate
                        ? new Date(campaign.startDate).toLocaleDateString()
                        : '-'}
                    </TableCell>
                    <TableCell>
                      {campaign.endDate
                        ? new Date(campaign.endDate).toLocaleDateString()
                        : '-'}
                    </TableCell>
                    {/* <TableCell>
                      <Button
                        type="button"
                        variant="outline"
                        onClick={() =>
                          setDetailModal({
                            open: true,
                            campaignId: campaign.campaignId,
                          })
                        }
                      >
                        View Details
                      </Button>
                    </TableCell> */}
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}

        {/* Pagination */}
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
    </>
  );
};

export default CampaignRiskImpacts;
