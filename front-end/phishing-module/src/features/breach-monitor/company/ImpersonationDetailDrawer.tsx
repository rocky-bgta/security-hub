import { SeverityBadge, StatusBadge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
import {
  AlertTriangle,
  Calendar,
  CheckCircle,
  Globe,
  Shield,
  X,
} from 'lucide-react';
import type { ImpersonationAlert } from 'models/BreachMonitor';
import { useState } from 'react';

interface Props {
  alert: ImpersonationAlert | null;
  onClose: () => void;
}

const ImpersonationDetailDrawer = ({ alert, onClose }: Props) => {
  const [actionTaken, setActionTaken] = useState<string[]>([]);

  if (!alert) return null;

  const recommendations = alert.recommendation.split(' | ');

  const handleAction = (action: string) => {
    setActionTaken(prev => [...prev, action]);
  };

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <div className="absolute inset-0 bg-black/50" onClick={onClose} />
      <div className="relative w-full max-w-md overflow-y-auto border-l border-card-border bg-card shadow-xl">
        <div className="flex items-center justify-between border-b border-card-border p-4">
          <h2 className="text-sm font-bold">Impersonation Alert Detail</h2>
          <button onClick={onClose} className="rounded-md p-1.5 hover:bg-muted">
            <X className="size-4" />
          </button>
        </div>

        <div className="space-y-5 p-4">
          {/* Domain & Type */}
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <Globe className="size-4 text-primary" />
              <span className="text-sm font-medium">Fake Domain</span>
            </div>
            <p className="rounded-md bg-muted/40 p-2.5 font-mono text-sm">
              {alert.fakeDomain}
            </p>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <p className="mb-1 text-xs text-muted-foreground">Type</p>
              <p className="text-sm font-medium">{alert.type}</p>
            </div>
            <div>
              <p className="mb-1 text-xs text-muted-foreground">Source</p>
              <p className="text-sm font-medium">{alert.source}</p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <p className="mb-1 text-xs text-muted-foreground">Severity</p>
              <SeverityBadge severity={alert.severity} />
            </div>
            <div>
              <p className="mb-1 text-xs text-muted-foreground">Status</p>
              <StatusBadge
                status={
                  alert.status === 'resolved' ? 'completed' : alert.status
                }
              />
            </div>
          </div>

          <div>
            <div className="mb-1 flex items-center gap-2">
              <Calendar className="size-3.5 text-muted-foreground" />
              <p className="text-xs text-muted-foreground">Date Detected</p>
            </div>
            <p className="text-sm">
              {new Date(alert.dateDetected).toLocaleDateString('en-US', {
                month: 'long',
                day: 'numeric',
                year: 'numeric',
              })}
            </p>
          </div>

          {alert.description && (
            <div>
              <p className="mb-1 text-xs text-muted-foreground">Description</p>
              <p className="rounded-lg bg-muted/30 p-3 text-sm text-muted-foreground">
                {alert.description}
              </p>
            </div>
          )}

          {/* Recommended Actions */}
          <div>
            <div className="mb-3 flex items-center gap-2">
              <Shield className="size-4 text-primary" />
              <p className="text-sm font-semibold">Recommended Actions</p>
            </div>
            <div className="space-y-2">
              {recommendations.map(rec => {
                const done = actionTaken.includes(rec);
                return (
                  <div
                    key={rec}
                    className={`flex items-center justify-between rounded-lg border p-2.5 transition-all ${
                      done
                        ? 'border-[#22c55e]/30 bg-[#22c55e]/5 opacity-60'
                        : 'border-card-border bg-muted/30'
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      {done ? (
                        <CheckCircle className="size-4 text-[#22c55e]" />
                      ) : (
                        <AlertTriangle className="size-4 text-[#f97316]" />
                      )}
                      <span className="text-sm">{rec}</span>
                    </div>
                    {!done && (
                      <Button
                        variant="default"
                        size="sm"
                        onClick={() => handleAction(rec)}
                        className="h-auto px-2.5 py-1 text-xs"
                      >
                        Apply
                      </Button>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ImpersonationDetailDrawer;
