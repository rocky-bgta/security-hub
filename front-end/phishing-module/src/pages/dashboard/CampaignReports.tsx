import { Button } from 'common/Button';
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
import { CampaignReportDetailModal } from 'features/dashboard/CampaignReportDetailModal';
import { ExportReportModal } from 'features/dashboard/ExportReportModal';
import { useReports } from 'hooks/UseReports';
import { SearchIcon } from 'lucide-react';
import { ICampaignPerformance } from 'models/Dashboard';
import { IGetListParams } from 'models/Global';
import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  getSimulationCopy,
  getSimulationPaths,
  toCampaignChannel,
  type SimulationChannel,
} from 'utils/SimulationChannel';

/**
 * Campaign Reports page
 */
const CampaignReports = ({
  channel = 'phishing',
}: {
  channel?: SimulationChannel;
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as {
    fromDashboard?: boolean;
    fromReports?: boolean;
    channel?: SimulationChannel;
  } | null;
  const fromDashboard = Boolean(locationState?.fromDashboard);
  const fromReports = Boolean(locationState?.fromReports);
  const routeChannel = locationState?.channel || channel;
  const copy = getSimulationCopy(routeChannel);
  const {
    exporting,
    loading,
    fetchCampaignReports,
    exportCampaignReport,
    fetchCampaignReportById,
    downloadExport,
  } = useReports(toCampaignChannel(routeChannel));

  // State
  const [campaigns, setCampaigns] = useState<ICampaignPerformance[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [exportModal, setExportModal] = useState<{
    open: boolean;
    campaignId: string | null;
  }>({
    open: false,
    campaignId: null,
  });
  const [detailModal, setDetailModal] = useState<{
    open: boolean;
    campaignId: string | null;
  }>({
    open: false,
    campaignId: null,
  });
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: 10,
    search: '',
  });

  // Load campaigns
  const loadCampaigns = useCallback(async () => {
    const result = await fetchCampaignReports({
      ...queryParams,
    });

    setCampaigns(result.data);
    setTotalCount(result.totalCount);
  }, [fetchCampaignReports, queryParams]);

  useEffect(() => {
    setTimeout(() => {
      loadCampaigns();
    }, 0);
  }, [loadCampaigns]);

  // Handle export
  const handleExport = async (format: 'pdf' | 'excel' | 'csv') => {
    if (!exportModal.campaignId) return;

    const blob = await exportCampaignReport(exportModal.campaignId, format);
    if (blob) {
      const campaign = campaigns.find(
        c => c.campaignId === exportModal.campaignId,
      );
      const filename = `campaign-report-${campaign?.campaignName || exportModal.campaignId}.${format === 'excel' ? 'xlsx' : format}`;
      downloadExport(blob, filename);
    }
    setExportModal({ open: false, campaignId: null });
  };

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
        {fromReports && (
          <IconBackButton
            onClick={() => navigate(getSimulationPaths(routeChannel).reports)}
            label="Back to Reports"
          />
        )}
        {fromDashboard && !fromReports && (
          <IconBackButton
            onClick={() =>
              navigate(getSimulationPaths(routeChannel).dashboard)
            }
            label="Back to Dashboard"
          />
        )}
        <div className="space-y-1">
          <h1 className="text-3xl font-bold text-foreground">
            {copy.label} Campaign Reports
          </h1>
          <p className="text-muted-foreground">
            Detailed performance metrics for all {copy.label.toLowerCase()}{' '}
            campaigns
          </p>
        </div>
      </div>

      <div>
        {/* Search */}
        <div className="mb-6">
          <div className="relative">
            <Input
              type="text"
              value={queryParams.search || ''}
              onChange={e =>
                setQueryParams(prev => ({ ...prev, search: e.target.value }))
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
                  <TableHead>Status</TableHead>
                  <TableHead>Sent</TableHead>
                  <TableHead>{copy.openRateLabel}</TableHead>
                  <TableHead>{copy.clickRateLabel}</TableHead>
                  <TableHead>Report Rate</TableHead>
                  <TableHead>Compromised Rate</TableHead>
                  <TableHead>Start Date</TableHead>
                  <TableHead>End Date</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {campaigns.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={9} className="text-center">
                      No campaigns found
                    </TableCell>
                  </TableRow>
                ) : (
                  campaigns.map(campaign => (
                    <TableRow key={campaign.campaignId}>
                      <TableCell>{campaign.campaignName}</TableCell>
                      <TableCell>{campaign.status}</TableCell>
                      <TableCell>
                        {campaign.emailsSent.toLocaleString()}
                      </TableCell>
                      <TableCell>{campaign.openRate.toFixed(1)}%</TableCell>
                      <TableCell>{campaign.clickRate.toFixed(1)}%</TableCell>
                      <TableCell>{campaign.reportRate.toFixed(1)}%</TableCell>
                      <TableCell>
                        {campaign.compromiseRate.toFixed(1)}%
                      </TableCell>
                      <TableCell>
                        {campaign.startDate
                          ? new Date(campaign.startDate).toLocaleDateString()
                          : 'N/A'}
                      </TableCell>
                      <TableCell>
                        {campaign.endDate
                          ? new Date(campaign.endDate).toLocaleDateString()
                          : 'N/A'}
                      </TableCell>
                      <TableCell>
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
                      </TableCell>
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
      </div>

      <CampaignReportDetailModal
        isOpen={detailModal.open}
        onClose={() => setDetailModal({ open: false, campaignId: null })}
        campaignId={detailModal.campaignId}
        channel={routeChannel}
        loadReport={fetchCampaignReportById}
      />

      <ExportReportModal
        isOpen={exportModal.open}
        onClose={() => setExportModal({ open: false, campaignId: null })}
        onExport={handleExport}
        title="Export Campaign Report"
        loading={exporting}
      />
    </>
  );
};

export default CampaignReports;
