import { Heart, RefreshCw, Server, Shield } from 'lucide-react';
import { useState } from 'react';

import { Card, CardContent } from 'common/Card';
import IPBreachAlertsTable from 'features/breach-monitor/ip/IPBreachAlertsTable';
import IPBreachDetailDrawer from 'features/breach-monitor/ip/IPBreachDetailDrawer';
import ImpersonationTacticsPanel from 'features/breach-monitor/ip/ImpersonationTacticsPanel';
import RiskTrendChart from 'features/breach-monitor/ip/RiskTrendChart';
import TacticDistributionChart from 'features/breach-monitor/ip/TacticDistributionChart';
import type { IPBreachAlert } from 'models/BreachMonitor';
import {
  mockIPBreachAlerts,
  mockRiskTrend,
  mockTacticDistribution,
} from 'utils/MockData';

const IPBreaches = () => {
  const [selected, setSelected] = useState<IPBreachAlert | null>(null);

  const today = new Date().toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">
          IP Breach Detection & Mitigation
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Monitor IP addresses for breaches and vulnerabilities from dark web
          sources
        </p>
      </div>

      {/* IP Summary Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Card>
          <CardContent className="flex items-center gap-4 p-4">
            <div className="rounded-lg bg-muted p-2.5">
              <Server className="size-5 text-muted-foreground" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Number of IPs</p>
              <p className="text-2xl font-bold">9</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-4">
            <div className="rounded-lg bg-destructive/10 p-2.5">
              <Heart className="size-5 text-destructive" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Total Breaches</p>
              <p className="text-2xl font-bold">3</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-4">
            <div className="rounded-lg bg-muted p-2.5">
              <RefreshCw className="size-5 text-muted-foreground" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Last Scan</p>
              <p className="text-lg font-semibold">{today}</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Alerts Table + Impersonation Tactics */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <IPBreachAlertsTable
            alerts={mockIPBreachAlerts}
            onViewDetail={setSelected}
          />
        </div>
        <ImpersonationTacticsPanel />
      </div>

      {/* ── Recommendations Bar ── */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-wrap items-center gap-3">
            <Shield className="size-5 text-primary" />
            <h3 className="text-sm font-semibold">Recommendations:</h3>
            {[
              'Report to Registrar',
              'Enable DMARC',
              'Block Malicious IPs',
              'Apply Patches',
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

      {/* Risk Trend + Tactic Distribution */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <RiskTrendChart data={mockRiskTrend} />
        <TacticDistributionChart data={mockTacticDistribution} />
      </div>

      <IPBreachDetailDrawer
        alert={selected}
        onClose={() => setSelected(null)}
      />
    </div>
  );
};

export default IPBreaches;
