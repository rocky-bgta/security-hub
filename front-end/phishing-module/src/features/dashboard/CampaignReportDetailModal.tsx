import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { ICampaignPerformance } from 'models/Dashboard';
import { BarChart3, Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import {
  getSimulationCopy,
  type SimulationChannel,
} from 'utils/SimulationChannel';

interface CampaignReportDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  campaignId: string | null;
  channel?: SimulationChannel;
  loadReport: (id: string) => Promise<ICampaignPerformance | null>;
}

const formatDate = (value: string | null | undefined) => {
  if (!value) return 'N/A';
  try {
    return new Date(value).toLocaleString();
  } catch {
    return 'N/A';
  }
};

const StatRow = ({
  label,
  value,
}: {
  label: string;
  value: string | number;
}) => (
  <div className="flex items-center justify-between gap-4 border-b border-card-border py-2 last:border-0">
    <span className="text-sm text-muted-foreground">{label}</span>
    <span className="text-sm font-medium text-foreground">{value}</span>
  </div>
);

/**
 * Modal showing full campaign report metrics from GET reports/campaigns/:id
 */
export const CampaignReportDetailModal = ({
  isOpen,
  onClose,
  campaignId,
  channel = 'phishing',
  loadReport,
}: CampaignReportDetailModalProps) => {
  const copy = getSimulationCopy(channel);
  const [report, setReport] = useState<ICampaignPerformance | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isOpen || !campaignId) {
      setTimeout(() => {
        setReport(null);
      }, 0);
      return;
    }

    let cancelled = false;

    (async () => {
      setLoading(true);
      const data = await loadReport(campaignId);
      if (!cancelled) {
        setReport(data);
        setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [isOpen, campaignId, loadReport]);

  return (
    <Dialog open={isOpen} onOpenChange={open => !open && onClose()}>
      <DialogContent className="max-h-[90vh] max-w-3xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <BarChart3 className="size-5 text-primary" />
            Campaign report details
          </DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="flex h-48 items-center justify-center">
            <Loader2 className="size-8 animate-spin text-primary" />
          </div>
        ) : report ? (
          <div className="flex flex-col gap-6">
            <div>
              <h3 className="text-base font-semibold text-foreground">
                {report.campaignName}
              </h3>
              <p className="text-xs text-muted-foreground">
                ID: {report.campaignId}
              </p>
            </div>

            <div>
              <h4 className="mb-2 text-sm font-medium text-foreground">
                Overview
              </h4>
              <div className="rounded-lg border border-card-border px-3">
                <StatRow label="Status" value={report.status} />
                <StatRow
                  label="Start date"
                  value={formatDate(report.startDate)}
                />
                <StatRow label="End date" value={formatDate(report.endDate)} />
                {/* <StatRow
                  label="Duration (days)"
                  value={report.durationDays ?? '—'}
                />
                <StatRow
                  label="Risk ranking"
                  value={report.riskRanking != null ? report.riskRanking : '—'}
                /> */}
              </div>
            </div>

            <div>
              <h4 className="mb-2 text-sm font-medium text-foreground">
                Volume
              </h4>
              <div className="rounded-lg border border-card-border px-3">
                <StatRow
                  label="Total recipients"
                  value={report.totalRecipients.toLocaleString()}
                />
                <StatRow
                  label={copy.sentLabel}
                  value={report.emailsSent.toLocaleString()}
                />
                {/* <StatRow
                  label="Emails delivered"
                  value={report.emailsDelivered.toLocaleString()}
                /> */}
                <StatRow
                  label={copy.openedLabel}
                  value={report.opened.toLocaleString()}
                />
                <StatRow
                  label={copy.clickedLabel}
                  value={report.clicked.toLocaleString()}
                />
                <StatRow
                  label="Compromised"
                  value={report.dataSubmitted.toLocaleString()}
                />
                <StatRow
                  label="Reported"
                  value={report.reported.toLocaleString()}
                />
                <StatRow
                  label={copy.bouncedLabel}
                  value={report.bounced.toLocaleString()}
                />
              </div>
            </div>

            <div>
              <h4 className="mb-2 text-sm font-medium text-foreground">
                Rates
              </h4>
              <div className="rounded-lg border border-card-border px-3">
                <StatRow
                  label="Delivery rate"
                  value={`${Number(report.deliveryRate).toFixed(1)}%`}
                />
                <StatRow
                  label={copy.openRateLabel}
                  value={`${Number(report.openRate).toFixed(1)}%`}
                />
                <StatRow
                  label={copy.clickRateLabel}
                  value={`${Number(report.clickRate).toFixed(1)}%`}
                />
                <StatRow
                  label="Compromise rate"
                  value={`${Number(report.compromiseRate).toFixed(1)}%`}
                />
                <StatRow
                  label="Report rate"
                  value={`${Number(report.reportRate).toFixed(1)}%`}
                />
              </div>
            </div>
          </div>
        ) : (
          <div className="py-8 text-center text-sm text-muted-foreground">
            Could not load campaign report.
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default CampaignReportDetailModal;
