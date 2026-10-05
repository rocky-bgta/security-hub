import { ExportReportModal } from 'features/dashboard/ExportReportModal';
import IconBackButton from 'components/IconBackButton';
import { useReports } from 'hooks/UseReports';
import { ICampaignPerformance } from 'models/Dashboard';
import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  getSimulationCopy,
  getSimulationPaths,
  toCampaignChannel,
  type SimulationChannel,
} from 'utils/SimulationChannel';

/**
 * Campaign Detail Report page with funnel visualization
 */
const CampaignDetailReport = ({
  channel = 'phishing',
}: {
  channel?: SimulationChannel;
}) => {
  const { campaignId, id } = useParams<{ campaignId?: string; id?: string }>();
  const reportId = campaignId || id;
  const copy = getSimulationCopy(channel);
  const paths = getSimulationPaths(channel);
  const {
    loading,
    exporting,
    fetchCampaignReportById,
    exportCampaignReport,
    downloadExport,
  } = useReports(toCampaignChannel(channel));

  const [campaign, setCampaign] = useState<ICampaignPerformance | null>(null);
  const [exportModalOpen, setExportModalOpen] = useState(false);

  const loadCampaign = useCallback(async () => {
    if (!reportId) return;
    const data = await fetchCampaignReportById(reportId);
    setCampaign(data);
  }, [reportId, fetchCampaignReportById]);

  useEffect(() => {
    loadCampaign();
  }, [loadCampaign]);

  const handleExport = async (format: 'pdf' | 'excel' | 'csv') => {
    if (!reportId) return;
    const blob = await exportCampaignReport(reportId, format);
    if (blob) {
      const filename = `campaign-report-${campaign?.campaignName || reportId}.${format === 'excel' ? 'xlsx' : format}`;
      downloadExport(blob, filename);
    }
    setExportModalOpen(false);
  };

  // Funnel data
  const funnelSteps = campaign
    ? [
        { label: copy.sentLabel, value: campaign.emailsSent, color: 'bg-blue-500' },
        {
          label: copy.deliveredLabel,
          value: campaign.emailsDelivered,
          color: 'bg-green-500',
        },
        { label: copy.openedLabel, value: campaign.opened, color: 'bg-purple-500' },
        { label: copy.clickedLabel, value: campaign.clicked, color: 'bg-orange-500' },
        {
          label: 'Submitted',
          value: campaign.dataSubmitted,
          color: 'bg-red-500',
        },
      ]
    : [];

  const maxValue =
    funnelSteps.length > 0 ? Math.max(...funnelSteps.map(s => s.value)) : 1;

  if (loading && !campaign) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="size-12 animate-spin rounded-full border-b-2 border-blue-600" />
      </div>
    );
  }

  if (!campaign) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <h2 className="text-xl font-semibold text-gray-900">
            Campaign not found
          </h2>
          <div className="mt-4 flex justify-center">
            <IconBackButton
              to={paths.campaignReports}
              label="Back to Reports"
              className="text-foreground"
            />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      {/* Header */}
      <div className="border-b bg-white">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="flex min-h-16 items-center justify-between py-3">
            <div className="flex items-center gap-4">
              <IconBackButton
                to={paths.campaignReports}
                label="Back to Reports"
                className="text-gray-800"
              />
              <div>
                <h1 className="text-xl font-bold text-gray-900">
                  {campaign.campaignName}
                </h1>
                <p className="text-sm text-gray-500">
                  Campaign Performance Report
                </p>
              </div>
            </div>
            <button
              onClick={() => setExportModalOpen(true)}
              className="hover: inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm"
            >
              <svg
                className="mr-2 size-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                />
              </svg>
              Export Report
            </button>
          </div>
        </div>
      </div>

      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        {/* Summary Cards */}
        <div className="mb-8 grid grid-cols-2 gap-4 md:grid-cols-4">
          <div className="rounded-lg bg-white p-4 shadow">
            <div className="text-sm text-gray-500">Total Recipients</div>
            <div className="text-2xl font-bold text-gray-900">
              {campaign.totalRecipients.toLocaleString()}
            </div>
          </div>
          <div className="rounded-lg bg-white p-4 shadow">
            <div className="text-sm text-gray-500">Duration</div>
            <div className="text-2xl font-bold text-gray-900">
              {campaign.durationDays} days
            </div>
          </div>
          <div className="rounded-lg bg-white p-4 shadow">
            <div className="text-sm text-gray-500">{copy.proneLabel}</div>
            <div className="text-2xl font-bold text-orange-600">
              {campaign.emailsSent > 0
                ? ((campaign.clicked / campaign.emailsSent) * 100).toFixed(1)
                : 0}
              %
            </div>
          </div>
          <div className="rounded-lg bg-white p-4 shadow">
            <div className="text-sm text-gray-500">Report Rate</div>
            <div className="text-2xl font-bold text-green-600">
              {campaign.reportRate.toFixed(1)}%
            </div>
          </div>
        </div>

        {/* Funnel Visualization */}
        <div className="mb-8 rounded-lg bg-white p-6 shadow">
          <h2 className="mb-6 text-lg font-semibold text-gray-900">
            Campaign Funnel
          </h2>

          <div className="space-y-4">
            {funnelSteps.map((step, index) => {
              const percentage =
                maxValue > 0 ? (step.value / maxValue) * 100 : 0;
              const conversionRate =
                index > 0 && funnelSteps[index - 1].value > 0
                  ? ((step.value / funnelSteps[index - 1].value) * 100).toFixed(
                      1,
                    )
                  : null;

              return (
                <div key={step.label}>
                  <div className="mb-1 flex items-center justify-between">
                    <span className="text-sm font-medium text-gray-700">
                      {step.label}
                    </span>
                    <div className="flex items-center gap-4">
                      <span className="text-sm font-semibold text-gray-900">
                        {step.value.toLocaleString()}
                      </span>
                      {conversionRate && (
                        <span className="text-xs text-gray-500">
                          ({conversionRate}% conversion)
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="h-8 overflow-hidden rounded-lg bg-gray-100">
                    <div
                      className={`h-full ${step.color} transition-all duration-500`}
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Detailed Metrics */}
        <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
          {/* Delivery Metrics */}
          <div className="rounded-lg bg-white p-6 shadow">
            <h3 className="mb-4 text-lg font-semibold text-gray-900">
              {copy.metricsTitle}
            </h3>
            <dl className="space-y-3">
              <div className="flex justify-between">
                <dt className="text-sm text-gray-500">{copy.sentLabel}</dt>
                <dd className="text-sm font-medium text-gray-900">
                  {campaign.emailsSent.toLocaleString()}
                </dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-sm text-gray-500">{copy.deliveredLabel}</dt>
                <dd className="text-sm font-medium text-gray-900">
                  {campaign.emailsDelivered.toLocaleString()}
                </dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-sm text-gray-500">{copy.bouncedLabel}</dt>
                <dd className="text-sm font-medium text-gray-900">
                  {campaign.bounced.toLocaleString()}
                </dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-sm text-gray-500">Delivery Rate</dt>
                <dd className="text-sm font-medium text-gray-900">
                  {campaign.deliveryRate.toFixed(1)}%
                </dd>
              </div>
            </dl>
          </div>

          {/* Engagement Metrics */}
          <div className="rounded-lg bg-white p-6 shadow">
            <h3 className="mb-4 text-lg font-semibold text-gray-900">
              Engagement Metrics
            </h3>
            <dl className="space-y-3">
              <div className="flex justify-between">
                <dt className="text-sm text-gray-500">{copy.openedLabel}</dt>
                <dd className="text-sm font-medium text-gray-900">
                  {campaign.opened.toLocaleString()}
                </dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-sm text-gray-500">{copy.clickedLabel}</dt>
                <dd className="text-sm font-medium text-orange-600">
                  {campaign.clicked.toLocaleString()}
                </dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-sm text-gray-500">Data Submitted</dt>
                <dd className="text-sm font-medium text-red-600">
                  {campaign.dataSubmitted.toLocaleString()}
                </dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-sm text-gray-500">Reported</dt>
                <dd className="text-sm font-medium text-green-600">
                  {campaign.reported.toLocaleString()}
                </dd>
              </div>
            </dl>
          </div>
        </div>
      </div>

      {/* Export Modal */}
      <ExportReportModal
        isOpen={exportModalOpen}
        onClose={() => setExportModalOpen(false)}
        onExport={handleExport}
        title="Export Campaign Report"
        loading={exporting}
      />
    </div>
  );
};

export default CampaignDetailReport;
