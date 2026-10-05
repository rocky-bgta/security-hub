import { SeverityBadge, StatusBadge } from 'components/common/Badge';
import {
  Calendar,
  Check,
  Database,
  Globe,
  KeyRound,
  Search,
  Shield,
  ShieldCheck,
  X,
} from 'lucide-react';
import type { BreachAlert } from 'models/BreachMonitor';
import { useState } from 'react';
import { formateDateAndTime } from 'utils/Helper';

interface Props {
  breach: BreachAlert | null;
  onClose: () => void;
}

const BreachDetailDrawer = ({ breach, onClose }: Props) => {
  const [actioned, setActioned] = useState<Set<string>>(new Set());

  if (!breach) return null;

  const handleAction = (action: string) => {
    setActioned(prev => new Set(prev).add(action));
  };

  return (
    <>
      <div className="fixed inset-0 z-50 bg-black/50" onClick={onClose} />
      <div className="fixed inset-y-0 right-0 z-50 w-full max-w-md overflow-y-auto border-l border-card-border bg-card shadow-xl">
        <div className="flex items-center justify-between border-b border-card-border p-6">
          <h2 className="text-lg font-semibold">Breach Details</h2>
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
            <SeverityBadge severity={breach.severity} />
            <StatusBadge status={breach.status} />
            {breach.echoes !== undefined && breach.echoes > 0 && (
              <span className="inline-flex items-center rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
                {breach.echoes} {breach.echoes === 1 ? 'Echo' : 'Echoes'}
              </span>
            )}
          </div>

          <div className="space-y-4">
            <DetailRow
              icon={<Shield className="size-4" />}
              label={breach.type === 'email' ? 'Email' : 'IP Address'}
              value={breach.email || breach.ip || 'N/A'}
            />
            <DetailRow
              icon={<Globe className="size-4" />}
              label="Domain"
              value={breach.domain}
            />
            <DetailRow
              icon={<Database className="size-4" />}
              label="Database Name"
              value={breach.databaseName}
            />
            {breach.foundIn && (
              <DetailRow
                icon={<Search className="size-4" />}
                label="Found In"
                value={breach.foundIn}
              />
            )}
            <DetailRow
              icon={<Calendar className="size-4" />}
              label="Breach Date"
              value={formateDateAndTime(breach.breachDate)}
            />
            <DetailRow
              icon={<Calendar className="size-4" />}
              label="Ingestion Date"
              value={formateDateAndTime(
                breach.ingestionDate || breach.detectedDate,
              )}
            />
          </div>

          <div>
            <p className="mb-2 text-xs font-medium uppercase tracking-wider text-muted-foreground">
              Source
            </p>
            <div className="rounded-lg bg-muted p-3 text-sm">
              {breach.source}
            </div>
          </div>

          <div>
            <p className="mb-2 text-xs font-medium uppercase tracking-wider text-muted-foreground">
              Password / Hash
            </p>
            <div className="rounded-lg bg-muted p-3 font-mono text-sm">
              {breach.passwordHash}
            </div>
          </div>

          <div>
            <p className="mb-3 text-xs font-medium uppercase tracking-wider text-muted-foreground">
              Recommended Actions
            </p>
            <div className="space-y-2">
              {[
                {
                  key: 'change_password',
                  label: 'Change Password',
                  icon: <KeyRound className="size-3.5" />,
                },
                {
                  key: 'enable_2fa',
                  label: 'Enable 2FA',
                  icon: <ShieldCheck className="size-3.5" />,
                },
              ].map(action => {
                const done = actioned.has(action.key);
                return (
                  <button
                    key={action.key}
                    onClick={() => handleAction(action.key)}
                    disabled={done}
                    className={`flex w-full items-center gap-2 rounded-lg px-4 py-2.5 text-sm font-medium transition-colors ${
                      done
                        ? 'border border-green-500/20 bg-green-500/10 text-green-400'
                        : 'border border-primary/20 bg-primary/10 text-primary hover:bg-primary/20'
                    }`}
                  >
                    {done ? <Check className="size-3.5" /> : action.icon}
                    {done ? `${action.label} — Applied` : action.label}
                  </button>
                );
              })}
            </div>
          </div>
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

export default BreachDetailDrawer;
