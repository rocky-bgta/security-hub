import { Card, CardContent } from 'common/Card';
import {
  AlertTriangle,
  CheckCircle2,
  Flame,
  Shield,
  ShieldAlert,
} from 'lucide-react';
import { useMemo, useState } from 'react';

import { type FilterState } from 'features/breach-monitor/BreachFilters';
import RemediationTrendChart from 'features/breach-monitor/vulnerabilities/RemediationTrendChart';
import RiskDistributionChart from 'features/breach-monitor/vulnerabilities/RiskDistributionChart';
import VulnerabilityAlertsTable from 'features/breach-monitor/vulnerabilities/VulnerabilityAlertsTable';
import VulnerabilityDetailDrawer from 'features/breach-monitor/vulnerabilities/VulnerabilityDetailDrawer';
import VulnerabilityInsightsPanel from 'features/breach-monitor/vulnerabilities/VulnerabilityInsightsPanel';
import type { VulnerabilityAlert } from 'models/BreachMonitor';
import {
  mockRemediationTrend,
  mockRiskDistribution,
  mockVulnerabilityAlerts,
} from 'utils/MockData';

const BreachVulnerabilities = () => {
  const [selected, setSelected] = useState<VulnerabilityAlert | null>(null);
  const [filters, _setFilters] = useState<FilterState>({
    search: '',
    severity: 'all',
    type: 'all',
    status: 'all',
  });

  const filtered = useMemo(() => {
    return mockVulnerabilityAlerts.filter(a => {
      if (filters.search) {
        const s = filters.search.toLowerCase();
        if (
          !a.cveId.toLowerCase().includes(s) &&
          !a.vulnerability.toLowerCase().includes(s)
        )
          return false;
      }
      if (filters.severity !== 'all' && a.riskLevel !== filters.severity)
        return false;
      return true;
    });
  }, [filters]);

  const today = new Date().toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
  const totalVulns = 25;
  const criticalThreats = 10;
  const highThreats = 2;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">
          Vulnerability Monitoring Dashboard
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Track vulnerabilities, remediation progress, and risk distribution in
          real time
        </p>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardContent className="flex items-center gap-4 p-4">
            <div className="rounded-lg bg-muted p-2.5">
              <ShieldAlert className="size-5 text-muted-foreground" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">
                Vulnerabilities Detected
              </p>
              <p className="text-2xl font-bold">{totalVulns}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-4">
            <div className="rounded-lg bg-destructive/10 p-2.5">
              <AlertTriangle className="size-5 text-destructive" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Critical Threats</p>
              <p className="text-2xl font-bold text-[#dc2626]">
                {criticalThreats}
              </p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-4">
            <div className="rounded-lg bg-[#f97316]/10 p-2.5">
              <Flame className="size-5 text-[#f97316]" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground">High Threat</p>
              <p className="text-2xl font-bold text-[#f97316]">{highThreats}</p>
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

      {/* Alerts Table + Insights */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <VulnerabilityAlertsTable
            alerts={filtered}
            onViewDetail={setSelected}
          />
        </div>
        <VulnerabilityInsightsPanel />
      </div>

      {/* ── Recommendations Bar ── */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-wrap items-center gap-3">
            <Shield className="size-5 text-primary" />
            <h3 className="text-sm font-semibold">Recommendations:</h3>
            {[
              'Apply Updates',
              'Conduct Scans',
              'Review Patch Status',
              'Isolate Vulnerable Systems',
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

      {/* Remediation Trend + Risk Distribution */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <RemediationTrendChart data={mockRemediationTrend} />
        <RiskDistributionChart data={mockRiskDistribution} />
      </div>

      <VulnerabilityDetailDrawer
        alert={selected}
        onClose={() => setSelected(null)}
      />
    </div>
  );
};

export default BreachVulnerabilities;
