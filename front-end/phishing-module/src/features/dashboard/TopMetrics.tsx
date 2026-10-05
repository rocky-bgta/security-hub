import {
  Activity,
  ArrowDownIcon,
  ArrowRight,
  ArrowUpIcon,
  Info,
  KeyRound,
  TrendingDownIcon,
  TrendingUpIcon,
} from 'lucide-react';
import { IPhishProneDataPoint, RiskLevel } from 'models/Dashboard';
import { cn } from 'utils/Helper';
import {
  getSimulationCopy,
  type SimulationChannel,
} from 'utils/SimulationChannel';

const METRIC_INFO = {
  reportRate:
    'Percentage of simulated messages reported as suspicious. Higher is better.',
} as const;

const MetricInfoIcon = ({ text }: { text: string }) => (
  <button
    type="button"
    className="group relative inline-flex shrink-0 rounded-full outline-none focus-visible:ring-2 focus-visible:ring-primary"
    aria-label={text}
  >
    <Info className="size-4 text-muted-foreground" />
    <span
      aria-hidden="true"
      className="pointer-events-none absolute right-0 top-full z-20 mt-1 hidden w-56 rounded-md border border-card-border bg-card-background p-2.5 text-left text-xs font-normal leading-snug text-card-foreground shadow-lg group-hover:block group-focus-visible:block"
    >
      {text}
    </span>
  </button>
);

/**
 * Top 5 metrics displayed at the top of the dashboard
 */
const TopMetrics = ({
  data,
  channel = 'phishing',
}: {
  data: IPhishProneDataPoint | null;
  channel?: SimulationChannel;
}) => {
  const copy = getSimulationCopy(channel);
  const getHumanRiskScoreColor = (tier: string) => {
    switch (tier?.toLowerCase()) {
      case 'low_risk':
        return 'text-emerald-500';
      case 'medium_risk':
        return 'text-cyan-500';
      case 'high_risk':
        return 'text-orange-500';
      case 'critical_risk':
        return 'text-red-500';
      default:
        return 'text-gray-500';
    }
  };

  return (
    <div
      className={cn(
        'grid grid-cols-1 gap-4',
        channel === 'vishing' ? 'grid-cols-4' : 'md:grid-cols-2 lg:grid-cols-5',
      )}
    >
      <div className="flex flex-col justify-between rounded-lg border border-card-border p-6">
        <div className="mb-3 flex items-start justify-between gap-2">
          <h3 className="text-base font-medium text-foreground">
            Human Risk Score
          </h3>
          <MetricInfoIcon text={copy.humanRiskInfo} />
        </div>
        <div className="flex items-end justify-between">
          <div className="flex items-baseline gap-x-2">
            <p
              className={cn(
                'text-3xl font-bold',
                getHumanRiskScoreColor(data?.humanRiskScore?.tier || ''),
              )}
            >
              {data?.humanRiskScore?.score || 0}%
            </p>
            <p className="capitalize text-foreground">
              {data?.humanRiskScore?.tier?.toLowerCase().split('_')[0] ||
                'High'}
            </p>
          </div>
        </div>
      </div>

      <div className="flex flex-col justify-between rounded-lg border border-card-border p-6">
        <div className="mb-3 flex items-start justify-between gap-2">
          <h3 className="text-base font-medium text-foreground">
            {copy.proneUsersLabel}
          </h3>
          <MetricInfoIcon text={copy.proneUsersInfo} />
        </div>
        <div className="flex items-end justify-between">
          <div className="flex items-baseline gap-x-2">
            <p className="rounded-full bg-red-500 px-3 text-3xl font-bold text-white">
              {data?.phishProneUsers?.find(
                user => user.tier === RiskLevel.CRITICAL,
              )?.count || 0}
            </p>
            <p className="text-foreground">
              <span className="px-1">Critical</span> |
              <span className="px-1 text-orange-500">
                {data?.phishProneUsers?.find(
                  user => user.tier === RiskLevel.HIGH,
                )?.count || 0}
              </span>
              High
            </p>
          </div>
        </div>
      </div>

      <div className="flex flex-col justify-between rounded-lg border border-card-border p-6">
        <div className="mb-3 flex items-start justify-between gap-2">
          <h3 className="text-base font-medium text-foreground">
            Credential Submits
          </h3>
          <MetricInfoIcon text={copy.credentialSubmitsInfo} />
        </div>
        <div className="flex items-end justify-between">
          <div className="flex items-center gap-x-2">
            <p
              className={cn(
                'size-auto rounded-full p-2 font-bold text-white',
                data?.informationSubmits ? 'bg-red-500' : 'bg-green-500',
              )}
            >
              <KeyRound className="size-5" />
            </p>
            <p
              className={cn(
                'px-1 text-3xl',
                data?.informationSubmits ? 'text-orange-500' : 'text-green-500',
              )}
            >
              {data?.informationSubmits || 0}
            </p>
          </div>
        </div>
      </div>

      {channel !== 'vishing' && (
        <div className="flex flex-col justify-between rounded-lg border border-card-border p-6">
          <div className="mb-3 flex items-start justify-between gap-2">
            <h3 className="text-base font-medium text-foreground">
              Report Rate
            </h3>
            <MetricInfoIcon text={METRIC_INFO.reportRate} />
          </div>
          <div className="flex items-end justify-between">
            <div className="flex items-center gap-x-2">
              <p className="size-auto rounded-full bg-green-500 p-2 font-bold text-white">
                {data?.reportRate?.trend === 'up' ? (
                  <ArrowUpIcon className="size-6" />
                ) : data?.reportRate?.trend === 'down' ? (
                  <ArrowDownIcon className="size-6" />
                ) : (
                  <ArrowRight className="size-6" />
                )}
              </p>
              <p className="px-1 text-3xl text-foreground">
                {data?.reportRate?.current || '0%'}
              </p>
              {data?.reportRate?.trend === 'up' ? (
                <ArrowUpIcon className="size-6 text-green-500" />
              ) : data?.reportRate?.trend === 'down' ? (
                <ArrowDownIcon className="size-6 text-red-500" />
              ) : (
                <ArrowRight className="size-6 text-foreground" />
              )}
            </div>
          </div>
        </div>
      )}

      <div className="flex flex-col justify-between rounded-lg border border-card-border p-6">
        <div className="mb-3 flex items-start justify-between gap-2">
          <h3 className="text-base font-medium text-foreground">Risk Trend</h3>
          <MetricInfoIcon text={copy.riskTrendInfo} />
        </div>
        <div className="flex items-center gap-x-2">
          <p
            className={cn(
              'size-auto rounded-full p-2 font-bold text-white',
              data?.riskTrend === 'down' ? 'bg-red-500' : 'bg-green-500',
            )}
          >
            <Activity className="size-5 text-foreground" />
          </p>
          {data?.riskTrend === 'up' ? (
            <>
              <p className="px-1 text-2xl font-semibold text-green-600">
                Improving
              </p>
              <TrendingUpIcon className="size-6 text-green-500" />
            </>
          ) : data?.riskTrend === 'down' ? (
            <>
              <p className="px-1 text-2xl font-semibold text-red-600">
                Worsening
              </p>
              <TrendingDownIcon className="size-6 text-red-500" />
            </>
          ) : (
            <>
              <p className="px-1 text-2xl font-semibold text-foreground">
                Stable
              </p>
              <ArrowRight className="size-6 text-foreground" />
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default TopMetrics;
