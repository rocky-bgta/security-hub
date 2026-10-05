import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Progress } from 'common/Progress';

import {
  Award,
  Download,
  FileText,
  GraduationCap,
  LucideIcon,
  RefreshCw,
  UserCheck,
  UserPlus,
} from 'lucide-react';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';

import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IProductLicenseOnboardingReport } from 'models/ProductLicenseOnboardingReport';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

const EMPTY_REPORT: IProductLicenseOnboardingReport = {
  totalLicenses: 0,
  activeUsers: 0,
  pendingActivation: 0,
  assignedLicenses: 0,
  unusedLicenses: 0,
  suspendedAccounts: 0,
  accountCreated: 0,
  accountActivated: 0,
  trainingAssigned: 0,
  trainingCompleted: 0,
  certificateEarned: 0,
};

const StatCard = ({
  label,
  value,
  description,
  color,
}: {
  label: string;
  value: string | number;
  description?: string;
  color?: string;
}) => (
  <Card>
    <CardContent className="p-4">
      <div className={`text-2xl font-bold ${color || ''}`}>{value}</div>
      <div className="mt-1 text-xs text-muted-foreground">{label}</div>
      {description ? (
        <div className="text-[11px] text-muted-foreground/70">
          {description}
        </div>
      ) : null}
    </CardContent>
  </Card>
);

const getFunnelPercent = (count: number, total: number) => {
  console.log('count', count);
  console.log('total', total);
  if (total <= 0 || count <= 0) return 0;
  return Math.min(100, Math.round((count / total) * 100));
};

const OnboardingReport = () => {
  const apiClient = useAPI();
  const [reportData, setReportData] =
    useState<IProductLicenseOnboardingReport>(EMPTY_REPORT);
  const [isLoading, setIsLoading] = useState(false);

  const fetchOnboardingReport = useCallback(async () => {
    try {
      setIsLoading(true);
      const response: IResponse<IProductLicenseOnboardingReport> =
        await apiClient.get(
          API_END_POINTS.GET_PRODUCT_LICENSE_ONBOARDING_REPORT,
        );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'API error');
      }

      setReportData({ ...EMPTY_REPORT, ...response.data });
    } catch (error) {
      console.error('Error fetching onboarding report:', error);
      toast.error('Failed to load onboarding report');
    } finally {
      setIsLoading(false);
    }
  }, [apiClient]);

  useEffect(() => {
    fetchOnboardingReport();
  }, [fetchOnboardingReport]);

  const funnelStages: Array<{
    label: string;
    count: number;
    icon: LucideIcon;
  }> = useMemo(
    () => [
      {
        label: 'Account Created',
        count: reportData.accountCreated,
        icon: UserPlus,
      },
      {
        label: 'Account Activated',
        count: reportData.accountActivated,
        icon: UserCheck,
      },
      {
        label: 'Training Assigned',
        count: reportData.trainingAssigned,
        icon: FileText,
      },
      {
        label: 'Training Completed',
        count: reportData.trainingCompleted,
        icon: GraduationCap,
      },
      {
        label: 'Certificate Earned',
        count: reportData.certificateEarned,
        icon: Award,
      },
    ],
    [reportData],
  );

  const handleDownload = () => {
    const rows = [
      ['Metric', 'Value'],
      ['Total Licenses', reportData.totalLicenses],
      ['Active Users', reportData.activeUsers],
      ['Pending Activation', reportData.pendingActivation],
      ['Assigned Licenses', reportData.assignedLicenses],
      ['Unused Licenses', reportData.unusedLicenses],
      ['Suspended Accounts', reportData.suspendedAccounts],
      ['Account Created', reportData.accountCreated],
      ['Account Activated', reportData.accountActivated],
      ['Training Assigned', reportData.trainingAssigned],
      ['Training Completed', reportData.trainingCompleted],
      ['Certificate Earned', reportData.certificateEarned],
    ];
    const csv = rows.map(row => row.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const objectUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = objectUrl;
    link.download = `onboarding-report-${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(objectUrl);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-end gap-2">
        <Button
          variant="outline"
          size="icon"
          onClick={fetchOnboardingReport}
          disabled={isLoading}
          aria-label="Refresh onboarding report"
        >
          <RefreshCw className={isLoading ? 'animate-spin' : ''} />
        </Button>
        <Button
          variant="outline"
          size="icon"
          onClick={handleDownload}
          aria-label="Download onboarding report"
        >
          <Download />
        </Button>
      </div>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-5">
        <StatCard
          label="Total Licenses"
          value={reportData.totalLicenses}
          description="Purchased by Client"
        />
        <StatCard
          label="Active Users"
          value={reportData.activeUsers}
          description="Training Assigned"
          color="text-emerald-400"
        />
        <StatCard
          label="Pending Activation"
          value={reportData.pendingActivation}
          description="Activation Pending"
          color="text-yellow-400"
        />
        <StatCard
          label="Unused Licenses"
          value={reportData.unusedLicenses}
          description="Available Licenses"
          color="text-blue-400"
        />
        <StatCard
          label="Suspended Accounts"
          value={reportData.suspendedAccounts}
          description="Inactive / Suspended"
          color="text-destructive"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">
            Employee Security Readiness Funnel
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {funnelStages.map(stage => {
              const Icon = stage.icon;
              const percent = getFunnelPercent(
                stage.count,
                reportData.assignedLicenses || reportData.totalLicenses,
              );

              return (
                <div key={stage.label} className="flex items-center gap-4">
                  <Icon className="size-4 shrink-0 text-primary" />
                  <div className="w-44 text-sm font-medium">{stage.label}</div>
                  <div className="flex-1">
                    <Progress value={percent} className="h-3" />
                  </div>
                  <div className="w-12 text-right text-sm">{percent}%</div>
                  <div className="w-10 text-right text-sm text-muted-foreground">
                    {stage.count}
                  </div>
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default OnboardingReport;
