import { Mail, Shield, ShieldAlert } from 'lucide-react';
import { useEffect, useState } from 'react';

import { Card, CardContent, CardHeader } from 'common/Card';
import BreachActivityChart from 'features/breach-monitor/email/BreachActivityChart';
import BreachDetailDrawer from 'features/breach-monitor/email/BreachDetailDrawer';
import BreachTable from 'features/breach-monitor/email/BreachTable';
import FoundInSourcesPanel from 'features/breach-monitor/email/FoundInSourcesPanel';
import { BreachAlert, BreachEmailSummaryResponse } from 'models/BreachMonitor';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn } from 'utils/Helper';

const credentialExposureTypes = [
  { label: 'Token Leaks', icon: '🔑' },
  { label: 'Passwords Leaked', icon: '🔓' },
  { label: 'Hashed Passwords', icon: '🔒' },
  { label: 'MFA Bypass Attempts', icon: '🛡️' },
  { label: 'Reused Credentials', icon: '♻️' },
];

const EmailBreaches = () => {
  const [selected, setSelected] = useState<BreachAlert | null>(null);

  const [breachEmailSummary, setBreachEmailSummary] =
    useState<BreachEmailSummaryResponse>({
      domainCount: 0,
      breachCount: 0,
    });

  const apiClient = useAPI();

  useEffect(() => {
    const fetchEmailSummary = async () => {
      try {
        const response: IResponse<BreachEmailSummaryResponse> =
          await apiClient.get(API_END_POINTS.GET_BREACH_MONITOR_EMAIL_SUMMARY);
        setBreachEmailSummary(response.data);
      } catch (error) {
        console.error('Failed to fetch email breach summary:', error);
      }
    };
    fetchEmailSummary();
  }, [apiClient]);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-foreground">Email Breaches</h1>
      {/* ── Top Summary Cards ── */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader>
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-lg font-bold">Emails</h3>
                <p className="mt-0.5 text-sm text-muted-foreground">
                  {breachEmailSummary.domainCount} monitored
                </p>
                <p className="mt-1 text-xs text-muted-foreground">
                  Total breaches
                </p>
              </div>
              <Mail className="size-6" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="flex items-end justify-between">
              <p className="text-xs text-muted-foreground">Total breaches</p>
              <p
                className={cn(
                  'text-3xl font-bold',
                  breachEmailSummary.breachCount > 0
                    ? 'text-destructive'
                    : 'text-green-500',
                )}
              >
                {breachEmailSummary.breachCount}
              </p>
            </div>
            <div className="mt-2 h-1 w-full overflow-hidden rounded-full bg-muted">
              <div
                className={cn(
                  'h-full rounded-full',
                  breachEmailSummary.breachCount > 0
                    ? 'bg-destructive'
                    : 'bg-green-500',
                )}
                style={{
                  width: `${(breachEmailSummary.breachCount / breachEmailSummary.domainCount) * 100}%`,
                }}
              />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* ── Breached Emails Count + Credential Exposure Type Chips ── */}
      <div className="flex flex-wrap items-center gap-3">
        <div
          className={cn(
            'flex items-center gap-2 rounded-lg border px-4 py-2',
            breachEmailSummary.breachCount > 0
              ? 'border-destructive/20 bg-destructive/10'
              : 'border-green-500/20 bg-green-500/10',
          )}
        >
          <ShieldAlert
            className={cn(
              'size-5',
              breachEmailSummary.breachCount > 0
                ? 'text-destructive'
                : 'text-green-500',
            )}
          />
          <div>
            <p className="text-xs text-muted-foreground">Breached Emails</p>
            <p
              className={cn(
                'text-lg font-bold',
                breachEmailSummary.breachCount > 0
                  ? 'text-destructive'
                  : 'text-green-500',
              )}
            >
              {breachEmailSummary.breachCount}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-1 text-xs font-medium text-muted-foreground">
          Credential Exposure Type:
        </div>
        {credentialExposureTypes.map(t => (
          <span
            key={t.label}
            className="inline-flex items-center gap-1.5 rounded-full border border-card-border px-3 py-1.5 text-xs font-medium text-secondary-foreground"
          >
            <span>{t.icon}</span>
            {t.label}
          </span>
        ))}
      </div>

      {/* ── Recent Breach Alerts Table ── */}
      <BreachTable />

      {/* ── Recommendations Bar ── */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-wrap items-center gap-3">
            <Shield className="size-5 text-primary" />
            <h3 className="text-sm font-semibold">Recommendations:</h3>
            {[
              'Change Passwords',
              'Enable 2FA',
              'Check Recovery Options',
              'Review Account Activity',
              'Avoid Reusing Passwords',
            ].map((rec, i, arr) => (
              <span key={rec} className="flex items-center gap-3">
                <span className="text-sm font-medium text-primary">{rec}</span>
                {i < arr.length - 1 && (
                  <span className="text-muted-foreground">|</span>
                )}
              </span>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* ── Breach Activity Chart + Developer Query Box ── */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <BreachActivityChart />
        <FoundInSourcesPanel />
      </div>

      {/* ── Detail Drawer ── */}
      <BreachDetailDrawer breach={selected} onClose={() => setSelected(null)} />
    </div>
  );
};

export default EmailBreaches;
