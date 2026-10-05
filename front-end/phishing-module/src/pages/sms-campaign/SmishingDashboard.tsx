import {
  BookTemplateIcon,
  PlusCircleIcon,
  RefreshCcwIcon,
  UserIcon,
  ViewIcon,
} from 'lucide-react';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { Button } from 'common/Button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { EmailActivityTimeline } from 'features/dashboard/EmailActivityTimeline';
import { KPICards } from 'features/dashboard/KPICards';
import TopMetrics from 'features/dashboard/TopMetrics';
import { UserRiskDistribution } from 'features/dashboard/UserRiskDistribution';
import { useDashboard } from 'hooks/UseDashboard';
import { useReports } from 'hooks/UseReports';
import { useAuth } from 'hooks/UseAuth';
import {
  ICampaignPerformance,
  IDashboardKpi,
  IDashboardRiskImpact,
  IEmailActivity,
  IPhishingCourseStatistics,
  IPhishProneDataPoint,
  ITrendData,
  ITrendDataPoint,
  IUserRiskDistribution,
} from 'models/Dashboard';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';
import { cn } from 'utils/Helper';
import { CampaignChannel } from 'models/Campaign';
import CampaignPerformanceChart from 'features/dashboard/CampaignPerformanceChart';
import CampaignRiskImpact from 'features/dashboard/CampaignRiskImpact';
import UsersByRisk from 'features/dashboard/UsersByRisk';
import { IList } from 'models/Global';
import DashboardCourseStatistics from 'pages/course-statistics/DashboardCourseStatistics';
import { useCourseStatistics } from 'hooks/UseCourseStatistics';
import CampaignMetric from 'features/dashboard/CampaignMetric';

const dateRanges = [
  { value: '7', label: 'Last 7 days' },
  { value: '14', label: 'Last 14 days' },
  { value: '30', label: 'Last 30 days' },
  { value: '90', label: 'Last 90 days' },
  { value: '365', label: 'Last year' },
];

/**
 * Main Smishing Dashboard page
 */
const SmishingDashboard = () => {
  const navigate = useNavigate();
  const { role } = useAuth();
  const {
    loading,
    fetchPhishProneData,
    fetchRiskImpact,
    fetchKpiMetrics,
    fetchCampaignPerformance,
    fetchUserRiskDistribution,
    refreshStats,
    fetchAllTrends,
  } = useDashboard(CampaignChannel.SMS);

  const { fetchPhishingCourseStatistics } = useCourseStatistics(
    CampaignChannel.SMS,
  );
  const { fetchEmailActivity } = useReports();

  const [phishProneData, setPhishProneData] =
    useState<IPhishProneDataPoint | null>(null);
  const [riskImpact, setRiskImpact] =
    useState<IList<IDashboardRiskImpact> | null>(null);
  const [kpiMetrics, setKpiMetrics] = useState<IDashboardKpi | null>(null);
  const [campaignPerformance, setCampaignPerformance] = useState<
    ICampaignPerformance[]
  >([]);
  const [riskDistribution, setRiskDistribution] =
    useState<IUserRiskDistribution | null>(null);
  const [activities, setActivities] = useState<IEmailActivity[]>([]);
  const [trends, setTrends] = useState<ITrendData | null>(null);

  const [proneDateRange, setProneDateRange] = useState('30');
  const [tradeDateRange, setTradeDateRange] = useState('30');
  const [kpiDateRange, setKpiDateRange] = useState('30');
  const [proneRefreshing, setProneRefreshing] = useState(false);
  const [kpiRefreshing, setKpiRefreshing] = useState(false);
  const [courseStatistics, setCourseStatistics] =
    useState<IPhishingCourseStatistics | null>(null);
  const isAutoRefreshingRef = useRef(false);
  const isClientAdmin = role === ROLE.CLIENT_ADMIN;

  const engagementTrendData = useMemo(() => {
    const trendByDate = new Map<
      string,
      {
        date: string;
        label?: string;
        openRate: number | null;
        clickRate: number | null;
        reportRate: number | null;
        compromiseRate: number | null;
      }
    >();

    const ensureEntry = (point: ITrendDataPoint) => {
      if (!trendByDate.has(point.date)) {
        trendByDate.set(point.date, {
          date: point.date,
          label: point.label,
          openRate: null,
          clickRate: null,
          reportRate: null,
          compromiseRate: null,
        });
      }
      return trendByDate.get(point.date)!;
    };

    trends?.openRateTrend.forEach(point => {
      const entry = ensureEntry(point);
      entry.openRate = point.value;
    });

    trends?.clickRateTrend.forEach(point => {
      const entry = ensureEntry(point);
      entry.clickRate = point.value;
    });

    trends?.reportRateTrend.forEach(point => {
      const entry = ensureEntry(point);
      entry.reportRate = point.value;
    });

    trends?.submissionRateTrend.forEach(point => {
      const entry = ensureEntry(point);
      entry.compromiseRate = point.value;
    });

    return Array.from(trendByDate.values()).sort((a, b) =>
      a.date.localeCompare(b.date),
    );
  }, [trends]);

  const loadDashboardData = useCallback(async () => {
    const [
      phishProneData,
      riskImpactData,
      riskDistributionData,
      allTrendsData,
      courseStatisticsData,
      activityData,
      campaignData,
      kpiData,
    ] = await Promise.all([
      fetchPhishProneData(),
      fetchRiskImpact({
        offset: 0,
        pageSize: 4,
      }),
      fetchUserRiskDistribution(),
      fetchAllTrends(),
      fetchPhishingCourseStatistics(),
      fetchEmailActivity({
        offset: 0,
        pageSize: 20,
        channel: CampaignChannel.SMS,
      }),
      fetchCampaignPerformance(10),
      fetchKpiMetrics(),
    ]);

    setPhishProneData(phishProneData);
    setRiskImpact(riskImpactData);
    setRiskDistribution(riskDistributionData);
    setTrends(allTrendsData);
    setCourseStatistics(courseStatisticsData);
    setActivities(activityData.data);
    setCampaignPerformance(campaignData);
    setKpiMetrics(kpiData);
  }, [
    fetchPhishProneData,
    fetchRiskImpact,
    fetchUserRiskDistribution,
    fetchAllTrends,
    fetchPhishingCourseStatistics,
    fetchEmailActivity,
    fetchCampaignPerformance,
    fetchKpiMetrics,
  ]);

  const loadPhishProneData = useCallback(async () => {
    const phishProneData = await fetchPhishProneData(proneDateRange);
    setPhishProneData(phishProneData);
  }, [fetchPhishProneData, proneDateRange]);

  useEffect(() => {
    loadPhishProneData();
  }, [proneDateRange, loadPhishProneData]);

  useEffect(() => {
    const loadAllTrendsData = async () => {
      const allTrendsData = await fetchAllTrends(tradeDateRange);
      setTrends(allTrendsData);
    };

    loadAllTrendsData();
  }, [fetchAllTrends, tradeDateRange]);

  const loadKPIData = useCallback(async () => {
    const kpiData = await fetchKpiMetrics(kpiDateRange);
    setKpiMetrics(kpiData);
  }, [fetchKpiMetrics, kpiDateRange]);

  useEffect(() => {
    loadKPIData();
  }, [kpiDateRange, loadKPIData]);

  useEffect(() => {
    setTimeout(() => {
      loadDashboardData();
    }, 0);
  }, [loadDashboardData]);

  const handleRefresh = useCallback(async () => {
    if (isAutoRefreshingRef.current) return;

    isAutoRefreshingRef.current = true;
    try {
      await refreshStats();
      await loadDashboardData();
    } finally {
      isAutoRefreshingRef.current = false;
    }
  }, [refreshStats, loadDashboardData]);

  useEffect(() => {
    if (!isClientAdmin) return;

    const intervalId = window.setInterval(() => {
      void handleRefresh();
    }, 30000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [isClientAdmin, handleRefresh]);

  const handleProneDataRefresh = async () => {
    setProneRefreshing(true);
    await loadPhishProneData();
    setProneRefreshing(false);
  };

  return (
    <div className="flex w-full flex-col gap-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0">
          <h1 className="text-3xl font-bold text-foreground">
            Smishing Dashboard
          </h1>
          <p className="text-muted-foreground">
            Monitor your SMS Security Awareness Program
          </p>
        </div>
        <div className="flex shrink-0 flex-wrap items-center gap-3 sm:gap-4">
          <Select value={proneDateRange} onValueChange={setProneDateRange}>
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {dateRanges.map(range => (
                <SelectItem key={range.value} value={range.value}>
                  {range.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button onClick={handleProneDataRefresh} disabled={proneRefreshing}>
            <RefreshCcwIcon
              className={cn('size-4', { 'animate-spin': proneRefreshing })}
            />
            Refresh
          </Button>
        </div>
      </header>

      <div className="flex flex-col gap-6">
        <TopMetrics data={phishProneData} channel="smishing" />

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
          <div className="lg:col-span-3">
            <CampaignRiskImpact data={riskImpact?.items} channel="smishing" />
          </div>
          <div className="lg:col-span-2">
            <UsersByRisk data={riskDistribution} />
          </div>
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
          <div className="lg:col-span-3">
            <CampaignMetric
              channel="smishing"
              data={engagementTrendData}
              dateRange={tradeDateRange}
              onDateRangeChange={setTradeDateRange}
            />
          </div>
          <div className="lg:col-span-2">
            <DashboardCourseStatistics
              data={courseStatistics}
              channel="smishing"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2 lg:items-start">
          <UserRiskDistribution
            data={riskDistribution || undefined}
            channel="smishing"
          />
          <div className="h-full">
            <EmailActivityTimeline
              activities={activities}
              loading={loading}
              hasMore={activities.length >= 20}
              channel="smishing"
              onLoadMore={() => {
                navigate(routes.smishingRecentActivity.path);
              }}
            />
          </div>
        </div>

        <CampaignPerformanceChart
          data={campaignPerformance}
          channel="smishing"
        />

        <KPICards
          kpiData={kpiMetrics || undefined}
          dateRange={kpiDateRange}
          refreshing={kpiRefreshing}
          onDateRangeChange={setKpiDateRange}
          onRefresh={async () => {
            setKpiRefreshing(true);
            await loadKPIData();
            setKpiRefreshing(false);
          }}
          channel="smishing"
        />

        <section>
          <h2 className="mb-4 text-lg font-semibold text-foreground">
            Quick Actions
          </h2>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <Button
              variant="outline"
              onClick={() => navigate(routes.smishingSimulationCreate.path)}
              className="flex items-center gap-5 px-8 py-10"
            >
              <div className="flex size-10 items-center justify-center rounded-lg bg-blue-100">
                <PlusCircleIcon className="size-5 text-blue-600" />
              </div>
              <span className="font-medium text-white">New Campaign</span>
            </Button>

            <Button
              variant="outline"
              onClick={() => navigate(routes.smsTemplateLibraryCreate.path)}
              className="flex items-center gap-5 px-8 py-10"
            >
              <div className="flex size-10 items-center justify-center rounded-lg bg-purple-100">
                <BookTemplateIcon className="size-5 text-purple-600" />
              </div>
              <span className="font-medium text-white">New SMS Template</span>
            </Button>

            <Button
              variant="outline"
              onClick={() => navigate(routes.smishingReports.path)}
              className="flex items-center gap-5 px-8 py-10"
            >
              <div className="flex size-10 items-center justify-center rounded-lg bg-green-100">
                <ViewIcon className="size-5 text-green-600" />
              </div>
              <span className="font-medium text-white">View Reports</span>
            </Button>

            <Button
              variant="outline"
              onClick={() => navigate(routes.smishingCampaignRiskImpacts.path)}
              className="flex items-center gap-5 px-8 py-10"
            >
              <div className="flex size-10 items-center justify-center rounded-lg bg-red-100">
                <UserIcon className="size-5 text-red-600" />
              </div>
              <span className="font-medium text-white">User Risk</span>
            </Button>
          </div>
        </section>
      </div>
    </div>
  );
};

export default SmishingDashboard;
