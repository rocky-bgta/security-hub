import { Globe, Mail, Shield } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

import { Card, CardContent } from 'common/Card';
import BreachActivityChart from 'features/breach-monitor/dashboard/BreachActivityChart';
import BreachDetailDrawer from 'features/breach-monitor/dashboard/BreachDetailDrawer';
import BreachTable from 'features/breach-monitor/dashboard/BreachTable';
import ThreatIntelCard from 'features/breach-monitor/dashboard/ThreatIntelCard';
import { BreachAlert, BreachEmailSummaryResponse } from 'models/BreachMonitor';
import {
  mockBreachActivity,
  mockDashboardStats,
  mockIPBreaches,
} from 'utils/MockData';
import EmailBreachTable from 'features/breach-monitor/dashboard/EmailBreachTable';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IResponse } from 'models/Global';
import { cn } from 'utils/Helper';
import IconBackButton from 'components/IconBackButton';
import {
  getSimulationPaths,
  type SimulationChannel,
} from 'utils/SimulationChannel';

const BreachMonitorDashboard = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as {
    fromReports?: boolean;
    channel?: SimulationChannel;
  } | null;
  const fromReports = Boolean(locationState?.fromReports);
  const routeChannel = locationState?.channel || 'phishing';
  const [selectedBreach, setSelectedBreach] = useState<BreachAlert | null>(
    null,
  );
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

  const stats = mockDashboardStats;
  const vulnerabilityScore = 83;

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        {fromReports && (
          <IconBackButton
            onClick={() => navigate(getSimulationPaths(routeChannel).reports)}
            label="Back to Reports"
          />
        )}
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Dark Web Monitoring Dashboard
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Monitor breaches, track threats, and take action
          </p>
        </div>
      </div>

      {/* Top Summary Cards — matching dark screenshot design */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {/* Emails Card */}
        <Card>
          <CardContent className="relative overflow-hidden p-5">
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
              <Mail className="size-6 text-muted-foreground" />
            </div>
            <div className="mt-3 flex items-end justify-between">
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

        {/* IP Card */}
        <Card>
          <CardContent className="relative overflow-hidden p-5">
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-lg font-bold">IP</h3>
                <p className="mt-0.5 text-sm text-muted-foreground">
                  {stats.monitoredIPs} monitored
                </p>
                <p className="mt-1 text-xs text-muted-foreground">
                  Total breaches
                </p>
              </div>
              <Globe className="size-6 text-muted-foreground" />
            </div>
            <div className="mt-3 flex items-end justify-between">
              <p className="text-xs text-muted-foreground">Total breaches</p>
              <p className="text-3xl font-bold text-[#dc2626]">
                {stats.totalIPBreaches}
              </p>
            </div>
            <div className="mt-2 h-1 w-full overflow-hidden rounded-full bg-muted">
              <div
                className={cn(
                  'h-full rounded-full',
                  stats.totalIPBreaches > 0 ? 'bg-destructive' : 'bg-green-500',
                )}
                style={{
                  width: `${(stats.totalIPBreaches / stats.monitoredIPs) * 100}%`,
                }}
              />
            </div>
          </CardContent>
        </Card>

        {/* Threat Intelligence Card */}
        <Card>
          <CardContent className="flex h-full flex-col justify-between p-5">
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-lg font-bold">Threat Intelligence</h3>
                <p className="mt-1 text-xs text-muted-foreground">
                  Total breaches
                </p>
              </div>
              <Shield className="size-6 text-muted-foreground" />
            </div>
            <div className="flex items-end justify-between">
              <p className="text-xs text-muted-foreground">Total breaches</p>
              <p className="text-3xl font-bold text-[#dc2626]">
                {stats.threatIntel.open +
                  stats.threatIntel.inMitigation +
                  stats.threatIntel.completed}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Vulnerability Card */}
        <Card>
          <CardContent className="flex h-full flex-col justify-between p-5">
            <h3 className="text-lg font-bold">Vulnerability</h3>
            <div className="mt-3 flex items-center justify-between">
              <div className="flex-1">
                <div className="h-2 w-full overflow-hidden rounded-full bg-muted">
                  <div
                    className="h-full rounded-full bg-[#dc2626]"
                    style={{ width: `${vulnerabilityScore}%` }}
                  />
                </div>
              </div>
              <p className="ml-4 text-2xl font-bold text-[#dc2626]">
                {vulnerabilityScore}%
              </p>
            </div>
            <p className="mt-2 text-xs text-muted-foreground">
              High vulnerability level detected
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Chart + Threat Intel */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <BreachActivityChart data={mockBreachActivity} />
        </div>
        <ThreatIntelCard data={stats.threatIntel} />
      </div>

      {/* Recent Email Breaches */}
      <EmailBreachTable />

      {/* Recent IP Breaches */}
      <BreachTable
        title="Recent IP Breach Alerts"
        breaches={mockIPBreaches}
        type="ip"
        onViewDetail={setSelectedBreach}
      />

      <BreachDetailDrawer
        breach={selectedBreach}
        onClose={() => setSelectedBreach(null)}
      />
    </div>
  );
};

export default BreachMonitorDashboard;
