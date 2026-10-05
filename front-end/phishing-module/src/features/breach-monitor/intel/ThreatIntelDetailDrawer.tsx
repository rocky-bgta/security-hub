import { SeverityBadge } from 'components/common/Badge';
import {
  Calendar,
  FileWarning,
  Globe,
  Lightbulb,
  Shield,
  X,
} from 'lucide-react';
import type { ThreatIntelAlert } from 'models/BreachMonitor';
import { useState } from 'react';
import { formatDate } from 'utils/Helper';

interface Props {
  alert: ThreatIntelAlert | null;
  onClose: () => void;
}

const ThreatIntelDetailDrawer = ({ alert, onClose }: Props) => {
  const [actionTaken, setActionTaken] = useState<string | null>(null);

  if (!alert) return null;

  const handleAction = (action: string) => {
    setActionTaken(action);
    setTimeout(() => setActionTaken(null), 2000);
  };

  return (
    <>
      <div className="fixed inset-0 z-50 bg-black/50" onClick={onClose} />
      <div className="fixed inset-y-0 right-0 z-50 w-full max-w-md overflow-y-auto border-l border-card-border bg-card shadow-xl">
        <div className="flex items-center justify-between border-b border-card-border p-6">
          <h2 className="text-lg font-semibold">Threat Intelligence Details</h2>
          <button
            onClick={onClose}
            className="rounded-md p-1.5 transition-colors hover:bg-muted"
            aria-label="Close"
          >
            <X className="size-5" />
          </button>
        </div>
        <div className="space-y-6 p-6">
          <div className="flex gap-2">
            <SeverityBadge severity={alert.severity} />
            <span
              className={`inline-flex items-center rounded px-2 py-0.5 text-xs font-semibold ${
                alert.status === 'completed'
                  ? 'bg-status-completed/15 text-status-completed'
                  : alert.status === 'in_mitigation'
                    ? 'bg-status-mitigation/15 text-status-mitigation'
                    : 'bg-status-open/15 text-status-open'
              }`}
            >
              {alert.status === 'completed'
                ? 'Resolved'
                : alert.status === 'in_mitigation'
                  ? 'In Mitigation'
                  : 'Open'}
            </span>
          </div>

          <div className="space-y-4">
            <DetailRow
              icon={<Globe className="size-4" />}
              label="Threat Domain"
              value={alert.threatDomain}
            />
            <DetailRow
              icon={<FileWarning className="size-4" />}
              label="Description"
              value={alert.description}
            />
            <DetailRow
              icon={<Calendar className="size-4" />}
              label="Date Found"
              value={formatDate(alert.dateFound)}
            />
            <DetailRow
              icon={<Shield className="size-4" />}
              label="Source"
              value={alert.source}
            />
            <DetailRow
              icon={<Lightbulb className="size-4" />}
              label="Category"
              value={alert.category
                .replace('_', ' ')
                .replace(/\b\w/g, l => l.toUpperCase())}
            />
          </div>

          <div>
            <p className="mb-2 text-xs font-medium uppercase tracking-wider text-muted-foreground">
              Recommendation
            </p>
            <div className="rounded-lg bg-muted p-3 text-sm">
              {alert.recommendation}
            </div>
          </div>

          {actionTaken && (
            <div className="bg-status-completed/10 border-status-completed/30 text-status-completed rounded-lg border p-3 text-sm">
              ✓ Action "{actionTaken}" has been initiated
            </div>
          )}

          <div className="flex gap-2">
            <button
              onClick={() => handleAction('Analyze Threats')}
              className="flex-1 rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
            >
              Analyze Threats
            </button>
            <button
              onClick={() => handleAction('Enhance Monitoring')}
              className="flex-1 rounded-md bg-secondary px-4 py-2 text-sm font-medium text-secondary-foreground transition-colors hover:bg-secondary/80"
            >
              Enhance Monitoring
            </button>
          </div>
          <button
            onClick={() => handleAction('Escalate')}
            className="w-full rounded-md bg-destructive px-4 py-2 text-sm font-medium text-destructive-foreground transition-colors hover:bg-destructive/90"
          >
            Escalate
          </button>
        </div>
      </div>
    </>
  );
};

function DetailRow({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-start gap-3">
      <div className="mt-0.5 text-muted-foreground">{icon}</div>
      <div>
        <p className="text-xs text-muted-foreground">{label}</p>
        <p className="text-sm font-medium">{value}</p>
      </div>
    </div>
  );
}

export default ThreatIntelDetailDrawer;
