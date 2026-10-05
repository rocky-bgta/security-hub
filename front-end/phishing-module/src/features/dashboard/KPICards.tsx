import { IDashboardKpi } from 'models/Dashboard';
import { KPICardsSkeleton } from 'components/LoadingSkeleton';
import {
  AlertCircleIcon,
  CalendarIcon,
  MailIcon,
  MailOpen,
  MessageSquare,
  PhoneCall,
  Presentation,
  RefreshCcwIcon,
  UserIcon,
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Button } from 'common/Button';
import { cn } from 'utils/Helper';
import { useNavigate } from 'react-router-dom';
import { ReactNode } from 'react';
import { getSimulationPaths } from 'utils/SimulationChannel';

interface KPICardsProps {
  kpiData?: IDashboardKpi;
  dateRange: string;
  refreshing: boolean;
  onDateRangeChange: (range: string) => void;
  onRefresh: () => void;
  channel?: 'phishing' | 'smishing' | 'vishing';
}

interface KPICardProps {
  title: string;
  value: string | number;
  icon: ReactNode;
  trend?: {
    value: number;
    isPositive: boolean;
  };
  color?: 'blue' | 'green' | 'yellow' | 'red' | 'purple' | 'indigo' | 'cyan';
  onClick?: () => void;
}

const dateRanges = [
  { value: '7', label: 'Last 7 days' },
  { value: '14', label: 'Last 14 days' },
  { value: '30', label: 'Last 30 days' },
  { value: '90', label: 'Last 90 days' },
  { value: '365', label: 'Last year' },
];

const colorClasses = {
  blue: {
    iconBg: 'bg-blue-100',
    iconText: 'text-blue-600',
    valueText: 'text-blue-700',
  },
  green: {
    iconBg: 'bg-green-100',
    iconText: 'text-green-600',
    valueText: 'text-green-700',
  },
  yellow: {
    iconBg: 'bg-yellow-100',
    iconText: 'text-yellow-600',
    valueText: 'text-yellow-700',
  },
  red: {
    iconBg: 'bg-red-100',
    iconText: 'text-red-600',
    valueText: 'text-red-700',
  },
  purple: {
    bg: 'bg-purple-50',
    iconBg: 'bg-purple-100',
    iconText: 'text-purple-600',
    valueText: 'text-purple-700',
  },
  indigo: {
    iconBg: 'bg-indigo-100',
    iconText: 'text-indigo-600',
    valueText: 'text-indigo-700',
  },
  cyan: {
    iconBg: 'bg-cyan-100',
    iconText: 'text-cyan-600',
    valueText: 'text-cyan-700',
  },
};

/**
 * Container for the 7 core KPI cards
 */
export const KPICards = ({
  kpiData,
  dateRange,
  refreshing,
  onDateRangeChange,
  onRefresh,
  channel = 'phishing',
}: KPICardsProps) => {
  const navigate = useNavigate();

  // Loading skeleton
  if (!kpiData) {
    return <KPICardsSkeleton />;
  }

  const handleRefresh = () => {
    if (!refreshing) {
      onRefresh();
    }
  };

  const handleCardClick = (cardType: string) => {
    const paths = getSimulationPaths(channel);
    switch (cardType) {
      case 'campaigns':
        navigate(paths.campaigns);
        break;
      case 'templates':
        navigate(paths.templates);
        break;
      case 'landingPages':
        navigate(paths.landingPages);
        break;
      case 'reports':
        navigate(paths.userRiskReport);
        break;
      case 'attacks':
      case 'hacks':
        navigate(paths.campaignReports, {
          state: { fromDashboard: true, channel },
        });
        break;
      default:
        break;
    }
  };

  return (
    <Card>
      <CardHeader className="flex flex-row justify-between">
        <CardTitle className="text-xl">Key Performance Indicators</CardTitle>
        <div className="flex shrink-0 flex-wrap items-center gap-3 sm:gap-4">
          <Select value={dateRange} onValueChange={onDateRangeChange}>
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
          <Button onClick={handleRefresh} disabled={refreshing}>
            <RefreshCcwIcon
              className={cn('size-4', { 'animate-spin': refreshing })}
            />
            Refresh
          </Button>
        </div>
      </CardHeader>
      <CardContent className="grid grid-cols-2 gap-4 p-6 md:grid-cols-3">
        {/* Attacks (Phishing Emails Sent) */}
        <KPICard
          title={
            channel === 'phishing'
              ? 'Emails Sent'
              : channel === 'smishing'
                ? 'SMS Sent'
                : 'Voice Calls Sent'
          }
          value={kpiData?.attacks?.toLocaleString() || 0}
          color="blue"
          icon={
            channel === 'phishing' ? (
              <MailOpen />
            ) : channel === 'smishing' ? (
              <MessageSquare />
            ) : (
              <PhoneCall />
            )
          }
          onClick={() => handleCardClick('attacks')}
        />

        {/* Hacks (Successful Compromises) */}
        <KPICard
          title="Compromises"
          value={kpiData?.hacks?.toLocaleString() || 0}
          color="red"
          icon={<UserIcon />}
          onClick={() => handleCardClick('hacks')}
        />

        {/* Reports (Reported by Users) */}
        <KPICard
          title="User Reports"
          value={kpiData?.reports?.toLocaleString() || 0}
          color="yellow"
          icon={<AlertCircleIcon />}
          onClick={() => handleCardClick('reports')}
        />

        {/* Campaigns */}
        <KPICard
          title="Campaigns"
          value={kpiData?.campaigns || 0}
          color="indigo"
          icon={<CalendarIcon />}
          onClick={() => handleCardClick('campaigns')}
        />

        {/* Templates */}
        <KPICard
          title="Templates"
          value={kpiData?.templates || 0}
          color="purple"
          icon={<MailIcon />}
          onClick={() => handleCardClick('templates')}
        />

        {/* Groups */}
        {/* <KPICard
        title="Groups"
        value={kpiData?.groups || 0}
        subtitle="User groups"
        color="cyan"
        icon={
          <svg
            className="size-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
            />
          </svg>
        }
        onClick={() => handleCardClick('groups')}
      /> */}

        {/* Landing Pages */}
        {channel !== 'vishing' && (
          <KPICard
            title="Landing Pages"
            value={kpiData?.landingPages || 0}
            color="green"
            icon={<Presentation />}
            onClick={() => handleCardClick('landingPages')}
          />
        )}
      </CardContent>
    </Card>
  );
};

const KPICard = ({
  title,
  value,
  icon,
  trend,
  color = 'blue',
  onClick,
}: KPICardProps) => {
  const colors = colorClasses[color];

  return (
    <div
      className={cn('rounded-lg border border-card-border p-6', {
        'cursor-pointer': onClick,
      })}
      onClick={onClick}
    >
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-foreground">{title}</p>
          <p className={cn('mt-2 text-3xl font-bold', colors.valueText)}>
            {value}
          </p>
          {trend && (
            <div className="mt-2 flex items-center">
              <span
                className={cn(`inline-flex items-center text-sm font-medium`, {
                  'text-green-600': trend.isPositive,
                  'text-red-600': !trend.isPositive,
                })}
              >
                {trend.isPositive ? (
                  <svg
                    className="mr-1 size-4"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 10l7-7m0 0l7 7m-7-7v18"
                    />
                  </svg>
                ) : (
                  <svg
                    className="mr-1 size-4"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 14l-7 7m0 0l-7-7m7 7V3"
                    />
                  </svg>
                )}
                {Math.abs(trend.value)}%
              </span>
              <span className="ml-1 text-xs text-foreground">
                vs last period
              </span>
            </div>
          )}
        </div>
        <div className={cn('rounded-full p-3', colors.iconBg)}>
          <div className={colors.iconText}>{icon}</div>
        </div>
      </div>
    </div>
  );
};

export default KPICards;
