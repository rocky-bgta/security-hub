import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { DashboardLineChart } from 'features/dashboard/DashboardLineChart';
import { CHART_COLORS } from 'models/Dashboard';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { getSimulationCopy, type SimulationChannel } from 'utils/SimulationChannel';

const dateRanges = [
  { value: '7', label: 'Last 7 days' },
  { value: '14', label: 'Last 14 days' },
  { value: '30', label: 'Last 30 days' },
  { value: '90', label: 'Last 90 days' },
  { value: '365', label: 'Last year' },
];

interface ICampaignMetricProps {
  data: Array<{
    date: string;
    label?: string;
    value?: number | null;
    [key: string]: string | number | null | undefined;
  }>;
  dateRange: string;
  onDateRangeChange: (range: string) => void;
  channel?: SimulationChannel;
}

const CampaignMetric = ({
  data,
  dateRange,
  onDateRangeChange,
  channel = 'phishing',
}: ICampaignMetricProps) => {
  const copy = getSimulationCopy(channel);
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>
          {channel === 'phishing'
            ? 'Phishing Campaign Metrics and Insights'
            : channel === 'smishing'
              ? 'Smishing Campaign Metrics and Insights'
              : 'Vishing Campaign Metrics and Insights'}
        </CardTitle>

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
      </CardHeader>

      <CardContent>
        <DashboardLineChart
          data={data}
          title="Engagement Rate Trends"
          height={240}
          valueSuffix="%"
          series={[
            {
              key: 'openRate',
              name: copy.openRateLabel,
              color: CHART_COLORS.primary,
            },
            {
              key: 'clickRate',
              name: copy.clickRateLabel,
              color: CHART_COLORS.warning,
            },
            {
              key: 'compromiseRate',
              name: 'Compromise Rate',
              color: CHART_COLORS.danger,
            },
            {
              key: 'reportRate',
              name: 'Report Rate',
              color: CHART_COLORS.success,
            },
          ]}
        />
      </CardContent>
    </Card>
  );
};

export default CampaignMetric;
