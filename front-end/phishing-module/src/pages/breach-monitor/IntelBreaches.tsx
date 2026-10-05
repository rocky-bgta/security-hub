import { AlertTriangle, CheckCircle2, Shield } from 'lucide-react';
import { useState } from 'react';

import { Card, CardContent } from 'common/Card';
import GlobalThreatMap from 'features/breach-monitor/GlobalThreatMap';
import AlertTrendChart from 'features/breach-monitor/intel/AlertTrendChart';
import IntelligenceCategoriesPanel from 'features/breach-monitor/intel/IntelligenceCategoriesPanel';
import ThreatIntelAlertsTable from 'features/breach-monitor/intel/ThreatIntelAlertsTable';
import ThreatIntelDetailDrawer from 'features/breach-monitor/intel/ThreatIntelDetailDrawer';
import ThreatTypesChart from 'features/breach-monitor/intel/ThreatTypesChart';
import type { ThreatIntelAlert } from 'models/BreachMonitor';
import {
  mockAlertTrend,
  mockThreatIntelAlerts,
  mockThreatTypesData,
} from 'utils/MockData';

const IntelBreaches = () => {
  const [selected, setSelected] = useState<ThreatIntelAlert | null>(null);

  const today = new Date().toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">
          Threat Intelligence Dashboard
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Monitor, analyze, and mitigate security threats across the
          organization
        </p>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Card>
          <CardContent className="flex items-center gap-4 p-4">
            <div className="rounded-lg bg-destructive/10 p-2.5">
              <AlertTriangle className="size-5 text-destructive" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Total Alerts</p>
              <p className="text-2xl font-bold text-destructive">300</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-4">
            <div className="rounded-lg bg-muted p-2.5">
              <CheckCircle2 className="size-5 text-muted-foreground" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Last Scan</p>
              <p className="text-lg font-semibold">{today}</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Alerts Table + Intelligence Categories */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <ThreatIntelAlertsTable
            alerts={mockThreatIntelAlerts}
            onViewDetail={setSelected}
          />
        </div>
        <IntelligenceCategoriesPanel />
      </div>

      {/* ── Recommendations Bar ── */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-wrap items-center gap-3">
            <Shield className="size-5 text-primary" />
            <h3 className="text-sm font-semibold">Recommendations:</h3>
            {[
              'Analyze Threats',
              'Enhance Monitoring',
              'Report Suspicious Domains',
              'Isolate Compromised Systems',
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

      {/* Alert Trend + Threat Types + Global Map */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <AlertTrendChart data={mockAlertTrend} />
        <ThreatTypesChart data={mockThreatTypesData} />
        <GlobalThreatMap />
      </div>

      <ThreatIntelDetailDrawer
        alert={selected}
        onClose={() => setSelected(null)}
      />
    </div>
  );
};

export default IntelBreaches;
