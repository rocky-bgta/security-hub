import { Button } from 'common/Button';
import { ExportReportModal } from 'features/dashboard/ExportReportModal';
import { useDashboard } from 'hooks/UseDashboard';
import { useReports } from 'hooks/UseReports';
import { Download } from 'lucide-react';
import {
  ICampaignPerformance,
  IDashboardOverview,
  IEmailActivity,
  IEmailStats,
  IInsecureWebFindingsSummary,
  IUserRiskDistribution,
  RISK_COLORS,
  RiskLevel,
  getActivityTypeIcon,
  getActivityTypeLabel,
} from 'models/Dashboard';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { routes } from 'routes/Routes';
import {
  getSimulationCopy,
  getSimulationPaths,
  toCampaignChannel,
  type SimulationChannel,
} from 'utils/SimulationChannel';

/**
 * Phishing Reports Page
 * Comprehensive reports hub for phishing campaign analytics
 * Based on Task-09 requirements (AC-10 to AC-14)
 */
const PhishingReports = ({
  channel = 'phishing',
}: {
  channel?: SimulationChannel;
}) => {
  const navigate = useNavigate();
  const copy = getSimulationCopy(channel);
  const paths = getSimulationPaths(channel);
  const reportsNavState = { fromReports: true, channel };
  const goToReport = (path: string) => {
    navigate(path, { state: reportsNavState });
  };
  const campaignChannel = toCampaignChannel(channel);
  const {
    loading: dashboardLoading,
    fetchOverview,
    fetchEmailStats,
    fetchUserRiskDistribution,
    fetchCampaignPerformance,
    fetchInsecureWebFindingsSummary,
  } = useDashboard(campaignChannel);
  const {
    loading: reportsLoading,
    exporting,
    fetchEmailActivity,
    exportUserRiskReport,
    downloadExport,
  } = useReports(campaignChannel);

  // State
  const [overview, setOverview] = useState<IDashboardOverview | null>(null);
  const [emailStats, setEmailStats] = useState<IEmailStats | null>(null);
  const [riskDistribution, setRiskDistribution] =
    useState<IUserRiskDistribution | null>(null);
  const [topCampaigns, setTopCampaigns] = useState<ICampaignPerformance[]>([]);
  const [insecureWebFindingsSummary, setInsecureWebFindingsSummary] =
    useState<IInsecureWebFindingsSummary | null>(null);
  const [phishingRecentActivity, setRecentActivity] = useState<
    IEmailActivity[]
  >([]);
  const [exportModal, setExportModal] = useState<{
    open: boolean;
    type: string;
  }>({
    open: false,
    type: '',
  });

  const loading = dashboardLoading || reportsLoading;

  // Load report data
  const loadReportData = useCallback(async () => {
    try {
      const [
        overviewData,
        emailStatsData,
        riskData,
        campaignData,
        insecureWebFindingsSummaryData,
        activityData,
      ] = await Promise.all([
        fetchOverview(),
        fetchEmailStats(),
        fetchUserRiskDistribution(),
        fetchCampaignPerformance(5),
        fetchInsecureWebFindingsSummary(),
        fetchEmailActivity({ offset: 0, pageSize: 10 }),
      ]);

      setOverview(overviewData);
      setEmailStats(emailStatsData);
      setRiskDistribution(riskData);
      setTopCampaigns(campaignData);
      setInsecureWebFindingsSummary(insecureWebFindingsSummaryData);
      setRecentActivity(activityData.data);
    } catch (error) {
      console.error('Error loading report data:', error);
      toast.error('Failed to load report data');
    }
  }, [
    fetchOverview,
    fetchEmailStats,
    fetchUserRiskDistribution,
    fetchCampaignPerformance,
    fetchInsecureWebFindingsSummary,
    fetchEmailActivity,
  ]);

  useEffect(() => {
    setTimeout(() => {
      loadReportData();
    }, 0);
  }, [loadReportData]);

  // Export handlers
  const handleExport = async (format: 'pdf' | 'excel' | 'csv') => {
    try {
      if (exportModal.type === 'user-risk') {
        const blob = await exportUserRiskReport(format);
        if (blob) {
          downloadExport(
            blob,
            `user-risk-report.${format === 'excel' ? 'xlsx' : format}`,
          );
          toast.success('Report exported successfully');
        }
      }
      // Add more export types as needed
    } catch (error) {
      console.error('Error exporting report:', error);
      toast.error('Failed to export report');
    }
    setExportModal({ open: false, type: '' });
  };

  // Format helpers
  const formatNumber = (num: number | undefined) => {
    if (num === undefined) return '0';
    return num.toLocaleString();
  };

  const formatPercentage = (num: number | undefined) => {
    if (num === undefined) return '0%';
    return `${num.toFixed(1)}%`;
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Report cards data
  const reportCards = [
    {
      id: 'campaigns',
      title: 'Campaign Reports',
      description: copy.campaignsDescription,
      icon: (
        <svg
          className="size-8 text-blue-600"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
      ),
      stats: [
        {
          label: 'Total Campaigns',
          value: formatNumber(overview?.totalCampaigns),
        },
        {
          label: 'Completed',
          value: formatNumber(overview?.completedCampaigns),
        },
      ],
      color: 'blue',
      path: paths.campaignReports,
    },
    {
      id: 'user-risk',
      title: 'User Risk Report',
      description:
        'Identify and track high-risk users based on behavior patterns',
      icon: (
        <svg
          className="size-8 text-orange-600"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
      ),
      stats: [
        {
          label: 'High Risk Users',
          value: formatNumber(riskDistribution?.highRiskCount),
        },
        {
          label: 'Critical',
          value: formatNumber(riskDistribution?.criticalRiskCount),
        },
      ],
      color: 'orange',
      path: paths.userRiskReport,
    },
    {
      id: 'breach',
      title: 'Breach Summary',
      description: 'Overview of detected data domains and breaches',
      icon: (
        <svg
          className="size-8 text-red-600"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
          />
        </svg>
      ),
      stats: [
        // {
        //   label: 'Total Domains',
        //   value: formatNumber(insecureWebFindingsSummary?.domainCount),
        // },
        {
          label: 'Total Breaches',
          value: formatNumber(insecureWebFindingsSummary?.breachCount),
        },
      ],
      color: 'red',
      path: routes.breachMonitorDashboard.path,
    },
    {
      id: 'email-stats',
      title: copy.statsTitle,
      description: copy.statsDescription,
      icon: (
        <svg
          className="size-8 text-green-600"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
          />
        </svg>
      ),
      stats: [
        {
          label: copy.sentLabel,
          value: formatNumber(emailStats?.totalEmailsSent),
        },
        // {
        //   label: 'Delivery Rate',
        //   value: formatPercentage(emailStats?.deliveryRate),
        // },
      ],
      color: 'green',
      path: paths.recentActivity,
    },
  ];

  return (
    <div>
      {/* Header */}
      <div className="mb-4">
        <div>
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-foreground">
                {copy.reportsTitle}
              </h1>
              <p className="text-sm text-muted-foreground">
                Analytics and reporting for your security awareness program
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="space-y-8">
        {/* Key Metrics Summary */}
        <div className="mb-8 grid grid-cols-2 gap-4 md:grid-cols-3">
          <div className="rounded-lg border border-card-border p-4 shadow-sm">
            <p className="text-sm text-muted-foreground">{copy.proneLabel}</p>
            <p className="text-2xl font-bold text-destructive">
              {formatPercentage(overview?.phishPronePercentage)}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              {copy.proneHint}
            </p>
          </div>
          <div className="rounded-lg border border-card-border p-4 shadow-sm">
            <p className="text-sm text-muted-foreground">{copy.clickRateLabel}</p>
            <p className="text-2xl font-bold text-orange-600">
              {formatPercentage(overview?.avgClickRate)}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              Average across campaigns
            </p>
          </div>
          <div className="rounded-lg border border-card-border p-4 shadow-sm">
            <p className="text-sm text-muted-foreground">{copy.openRateLabel}</p>
            <p className="text-2xl font-bold text-primary">
              {formatPercentage(overview?.avgOpenRate)}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              Average {copy.openRateLabel.toLowerCase()}
            </p>
          </div>
          {/* <div className="rounded-lg border border-card-border p-4 shadow-sm">
            <p className="text-sm text-muted-foreground">Repeat Offenders</p>
            <p className="text-2xl font-bold text-primary">
              {formatNumber(overview?.repeatOffenders)}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              Multiple failures
            </p>
          </div> */}
        </div>

        {/* Report Cards */}
        <h2 className="mb-4 text-lg font-semibold text-foreground">
          Available Reports
        </h2>
        <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2">
          {reportCards.map(card => (
            <div
              key={card.id}
              className="overflow-hidden rounded-xl border border-card-border shadow-sm transition-shadow hover:shadow-md"
            >
              <div className="p-6">
                <div className="flex items-start gap-4">
                  <div className={`rounded-lg p-3`}>{card.icon}</div>
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-foreground">
                      {card.title}
                    </h3>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {card.description}
                    </p>

                    <div className="mt-4 flex items-center gap-6">
                      {card.stats.map((stat, i) => (
                        <div key={i}>
                          <p className="text-xs text-muted-foreground">
                            {stat.label}
                          </p>
                          <p className="text-lg font-semibold text-foreground">
                            {stat.value}
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
              <div className="flex items-center justify-between border-t border-gray-200 px-6 py-3">
                <button
                  onClick={() => goToReport(card.path)}
                  className={`text-sm font-medium text-primary`}
                >
                  View Report →
                </button>
                {card.id === 'user-risk' && (
                  <button
                    onClick={() =>
                      setExportModal({ open: true, type: 'user-risk' })
                    }
                    className="flex items-center gap-1 text-sm font-medium text-muted-foreground hover:text-foreground"
                  >
                    <Download className="size-4" />
                    Export
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>

        {/* Bottom Section: Top Campaigns & Recent Activity */}
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {/* Top Campaigns */}
          <div className="rounded-xl border border-card-border shadow-sm">
            <div className="flex items-center justify-between border-b border-card-border px-6 py-4">
              <h3 className="text-lg font-semibold text-foreground">
                {copy.topCampaignsTitle}
              </h3>
              <button
                onClick={() => goToReport(paths.campaignReports)}
                className="text-sm font-medium text-primary hover:text-primary/80"
              >
                View all
              </button>
            </div>
            <div className="divide-y divide-card-border">
              {loading && topCampaigns?.length === 0 ? (
                [...Array(3)].map((_, i) => (
                  <div key={i} className="animate-pulse px-6 py-4">
                    <div className="mb-2 h-4 w-2/3 rounded bg-card-border" />
                    <div className="h-3 w-1/3 rounded" />
                  </div>
                ))
              ) : topCampaigns?.length === 0 ? (
                <div className="px-6 py-8 text-center text-muted-foreground">
                  No campaign data available
                </div>
              ) : (
                topCampaigns?.map(campaign => (
                  <div
                    key={campaign.campaignId}
                    className="px-6 py-4"
                  >
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-medium text-foreground">
                          {campaign.campaignName}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          {formatNumber(campaign.emailsSent)} {copy.sentUnit}
                        </p>
                      </div>
                      <div className="text-right">
                        <p
                          className={`text-lg font-semibold ${campaign.clickRate > 20 ? 'text-red-600' : 'text-foreground'}`}
                        >
                          {formatPercentage(campaign.clickRate)}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          click rate
                        </p>
                      </div>
                    </div>
                    <div className="mt-2 h-1.5 w-full rounded-full bg-card-border">
                      <div
                        className={`h-1.5 rounded-full ${campaign.clickRate > 20 ? 'bg-red-500' : 'bg-blue-500'}`}
                        style={{
                          width: `${Math.min(100, campaign.clickRate * 2)}%`,
                        }}
                      />
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Recent Activity */}
          <div className="rounded-xl border border-card-border shadow-sm">
            <div className="flex items-center justify-between border-b border-card-border px-6 py-4">
              <h3 className="text-lg font-semibold text-foreground">
                Recent Activity
              </h3>
              <button
                onClick={() => goToReport(paths.recentActivity)}
                className="text-sm font-medium text-primary hover:text-primary/80"
              >
                View all
              </button>
            </div>
            <div className="max-h-[400px] divide-y divide-card-border overflow-y-auto">
              {loading && phishingRecentActivity?.length === 0 ? (
                [...Array(5)].map((_, i) => (
                  <div key={i} className="animate-pulse px-6 py-3">
                    <div className="flex items-center gap-3">
                      <div className="size-8 rounded-full bg-card-border" />
                      <div className="flex-1">
                        <div className="mb-1 h-4 w-3/4 rounded bg-card-border" />
                        <div className="h-3 w-1/2 rounded" />
                      </div>
                    </div>
                  </div>
                ))
              ) : phishingRecentActivity?.length === 0 ? (
                <div className="px-6 py-8 text-center text-muted-foreground">
                  No recent activity
                </div>
              ) : (
                phishingRecentActivity?.map(activity => (
                  <div key={activity.activityId} className="hover: px-6 py-3">
                    <div className="flex items-center gap-3">
                      <div className="flex size-8 items-center justify-center rounded-full text-lg">
                        {getActivityTypeIcon(activity.activityType)}
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-medium text-foreground">
                          {getActivityTypeLabel(activity.activityType, channel)}
                        </p>
                        <p className="truncate text-xs text-muted-foreground">
                          {activity.recipientEmail} • {activity.campaignName}
                        </p>
                      </div>
                      <div className="whitespace-nowrap text-xs text-muted-foreground">
                        {formatDate(activity.timestamp)}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {/* User Risk Distribution Quick View */}
        {riskDistribution && (
          <div className="mt-8 rounded-xl border border-card-border p-6 shadow-sm">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-foreground">
                User Risk Distribution
              </h3>
              <button
                onClick={() => goToReport(paths.userRiskReport)}
                className="text-sm font-medium text-primary hover:text-primary/80"
              >
                View Details →
              </button>
            </div>
            <div className="grid grid-cols-4 gap-4">
              <div
                onClick={() =>
                  goToReport(
                    `${paths.userRiskReport}?riskLevel=${RiskLevel.LOW}`,
                  )
                }
                className="cursor-pointer rounded-lg border border-card-border p-4 transition-colors hover:bg-primary/10"
              >
                <div className="mb-2 flex items-center gap-2">
                  <div
                    className="size-3 rounded-full"
                    style={{ backgroundColor: RISK_COLORS[RiskLevel.LOW] }}
                  />
                  <span className="text-sm font-medium text-foreground">
                    Low Risk
                  </span>
                </div>
                <p className="text-2xl font-bold text-foreground">
                  {formatNumber(riskDistribution.lowRiskCount)}
                </p>
                <p className="text-xs text-muted-foreground">
                  {formatPercentage(riskDistribution.lowRiskPercentage)}
                </p>
              </div>
              <div
                onClick={() =>
                  goToReport(
                    `${paths.userRiskReport}?riskLevel=${RiskLevel.MEDIUM}`,
                  )
                }
                className="cursor-pointer rounded-lg border border-card-border p-4 transition-colors hover:bg-primary/10"
              >
                <div className="mb-2 flex items-center gap-2">
                  <div
                    className="size-3 rounded-full"
                    style={{ backgroundColor: RISK_COLORS[RiskLevel.MEDIUM] }}
                  />
                  <span className="text-sm font-medium text-foreground">
                    Medium Risk
                  </span>
                </div>
                <p className="text-2xl font-bold text-foreground">
                  {formatNumber(riskDistribution.mediumRiskCount)}
                </p>
                <p className="text-xs text-muted-foreground">
                  {formatPercentage(riskDistribution.mediumRiskPercentage)}
                </p>
              </div>
              <div
                onClick={() =>
                  goToReport(
                    `${paths.userRiskReport}?riskLevel=${RiskLevel.HIGH}`,
                  )
                }
                className="cursor-pointer rounded-lg border border-card-border p-4 transition-colors hover:bg-primary/10"
              >
                <div className="mb-2 flex items-center gap-2">
                  <div
                    className="size-3 rounded-full"
                    style={{ backgroundColor: RISK_COLORS[RiskLevel.HIGH] }}
                  />
                  <span className="text-sm font-medium text-foreground">
                    High Risk
                  </span>
                </div>
                <p className="text-2xl font-bold text-foreground">
                  {formatNumber(riskDistribution.highRiskCount)}
                </p>
                <p className="text-xs text-muted-foreground">
                  {formatPercentage(riskDistribution.highRiskPercentage)}
                </p>
              </div>
              <div
                onClick={() =>
                  goToReport(
                    `${paths.userRiskReport}?riskLevel=${RiskLevel.CRITICAL}`,
                  )
                }
                className="cursor-pointer rounded-lg border border-card-border p-4 transition-colors hover:bg-primary/10"
              >
                <div className="mb-2 flex items-center gap-2">
                  <div
                    className="size-3 rounded-full"
                    style={{ backgroundColor: RISK_COLORS[RiskLevel.CRITICAL] }}
                  />
                  <span className="text-sm font-medium text-foreground">
                    Critical
                  </span>
                </div>
                <p className="text-2xl font-bold text-foreground">
                  {formatNumber(riskDistribution.criticalRiskCount)}
                </p>
                <p className="text-xs text-muted-foreground">
                  {formatPercentage(riskDistribution.criticalRiskPercentage)}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Quick Export Section */}
        <div className="mt-8 rounded-xl border border-card-border p-6 text-white">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-xl font-semibold">Export Reports</h3>
              <p className="mt-1">
                Download comprehensive reports in PDF, Excel, or CSV format
              </p>
            </div>
            <div className="flex items-center gap-3">
              <Button
                onClick={() =>
                  setExportModal({ open: true, type: 'user-risk' })
                }
              >
                Export User Risk Report
              </Button>
              {/* <button
                onClick={() => navigate(paths.campaignReports)}
                className="rounded-lg px-4 py-2 font-medium text-primary transition-colors hover/80"
              >
                Export Campaign Reports
              </button> */}
            </div>
          </div>
        </div>
      </div>

      {/* Export Modal */}
      <ExportReportModal
        isOpen={exportModal.open}
        onClose={() => setExportModal({ open: false, type: '' })}
        onExport={handleExport}
        title={
          exportModal.type === 'user-risk'
            ? 'Export User Risk Report'
            : 'Export Report'
        }
        loading={exporting}
      />
    </div>
  );
};

export default PhishingReports;
