import {
  BarChart,
  Bar,
  Rectangle,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { CHART_COLORS, ICampaignPerformance } from 'models/Dashboard';
import { ActivityIcon } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import {
  getSimulationCopy,
  getSimulationPaths,
  type SimulationChannel,
} from 'utils/SimulationChannel';

interface CampaignPerformanceChartProps {
  data?: ICampaignPerformance[];
  channel?: SimulationChannel;
}

type SegmentKey =
  | 'openRate'
  | 'clickRate'
  | 'reportRate'
  | 'compromiseRate'
  | 'remaining';

type CampaignChartRow = {
  campaignId: string;
  name: string;
  sent: number;
  opened: number;
  clicked: number;
  report: number;
  compromise: number;
  remainingUsers: number;
  openRate: number;
  clickRate: number;
  reportRate: number;
  compromiseRate: number;
  remaining: number;
  firstVisibleSegment: SegmentKey;
  lastVisibleSegment: SegmentKey;
};

const COLORS = {
  opened: CHART_COLORS.secondary,
  clicked: CHART_COLORS.warning,
  report: CHART_COLORS.success,
  compromise: CHART_COLORS.danger,
  remaining: '#334155',
};

const getTooltipLabels = (channel: SimulationChannel) => {
  const copy = getSimulationCopy(channel);
  return {
    openRate: copy.openedLabel,
    clickRate: copy.clickedLabel,
    reportRate: 'Reported',
    compromiseRate: 'Compromised',
    remaining: 'Remaining',
  };
};

type TooltipMetricKey = keyof ReturnType<typeof getTooltipLabels>;

type CustomTooltipProps = {
  active?: boolean;
  payload?: Array<{
    dataKey: TooltipMetricKey;
    value: number | string;
    payload: CampaignChartRow;
  }>;
  labels?: ReturnType<typeof getTooltipLabels>;
};

type CustomYAxisTickProps = {
  x?: number;
  y?: number;
  payload?: {
    value: string;
  };
};

const CustomTooltip = ({
  active,
  payload,
  labels = getTooltipLabels('phishing'),
}: CustomTooltipProps) => {
  if (!active || !payload || !payload.length) return null;

  const hoveredItem = payload[0];
  if (hoveredItem.dataKey === 'remaining') {
    return (
      <div className="rounded bg-[#1f2937] px-3 py-2 text-xs text-white">
        <div>
          <span>{labels.remaining}</span>:{' '}
          {hoveredItem.payload.remainingUsers} users (
          {hoveredItem.payload.remaining.toFixed(1)}%)
        </div>
      </div>
    );
  }

  const countByMetric: Record<
    Exclude<TooltipMetricKey, 'remaining'>,
    number
  > = {
    openRate: hoveredItem.payload.opened,
    clickRate: hoveredItem.payload.clicked,
    reportRate: hoveredItem.payload.report,
    compromiseRate: hoveredItem.payload.compromise,
  };
  const percentageByMetric: Record<
    Exclude<TooltipMetricKey, 'remaining'>,
    number
  > = {
    openRate: hoveredItem.payload.openRate,
    clickRate: hoveredItem.payload.clickRate,
    reportRate: hoveredItem.payload.reportRate,
    compromiseRate: hoveredItem.payload.compromiseRate,
  };
  const count = countByMetric[hoveredItem.dataKey];
  const percentage = percentageByMetric[hoveredItem.dataKey];

  return (
    <div className="rounded bg-[#1f2937] px-3 py-2 text-xs text-white">
      <div>
        <span>
          {labels[hoveredItem.dataKey] ?? hoveredItem.dataKey}
        </span>
        : {count} users ({percentage.toFixed(1)}%)
      </div>
    </div>
  );
};

const LegendItem = ({ color, label }: { color: string; label: string }) => (
  <div className="flex items-center gap-2">
    <div className="size-3 rounded-sm" style={{ backgroundColor: color }} />
    <span>{label}</span>
  </div>
);

const CustomYAxisTick = ({
  x = 0,
  y = 0,
  payload,
  onCampaignClick,
}: CustomYAxisTickProps & {
  onCampaignClick?: (campaignId: string) => void;
}) => {
  if (!payload) return null;

  const { value } = payload;

  return (
    <g transform={`translate(${x},${y})`}>
      <text textAnchor="end" dominantBaseline="middle" fill="#cbd5f5">
        <tspan
          x={0}
          dy="0"
          fontSize={14}
          onMouseDown={event => event.preventDefault()}
          onPointerDown={event => event.preventDefault()}
          onClick={() => onCampaignClick?.(value)}
          style={{ cursor: onCampaignClick ? 'pointer' : 'default' }}
        >
          {value}
        </tspan>
      </text>
    </g>
  );
};

type SegmentShapeProps = {
  x?: number;
  y?: number;
  width?: number;
  height?: number;
  fill?: string;
  payload?: CampaignChartRow;
};

const createSegmentShape = (segment: SegmentKey, displayName: string) => {
  const SegmentShape = ({
    x = 0,
    y = 0,
    width = 0,
    height = 0,
    fill,
    payload,
  }: SegmentShapeProps) => {
    const row = payload;
    const isFirst = row?.firstVisibleSegment === segment;
    const isLast = row?.lastVisibleSegment === segment;

    const radius: [number, number, number, number] =
      isFirst && isLast
        ? [10, 10, 10, 10]
        : isFirst
          ? [10, 0, 0, 10]
          : isLast
            ? [0, 10, 10, 0]
            : [0, 0, 0, 0];

    return (
      <Rectangle
        x={x}
        y={y}
        width={width}
        height={height}
        fill={fill}
        radius={radius}
      />
    );
  };

  SegmentShape.displayName = displayName;
  return SegmentShape;
};

const CampaignPerformanceChart = ({
  data,
  channel = 'phishing',
}: CampaignPerformanceChartProps) => {
  const navigate = useNavigate();
  const copy = getSimulationCopy(channel);
  const tooltipLabels = getTooltipLabels(channel);
  const sourceData = data;

  const chartData: CampaignChartRow[] =
    sourceData?.slice(0, 7).map(campaign => {
      const openRate = campaign.openRate;
      const clickRate = campaign.clickRate;
      const reportRate = campaign.reportRate;
      const compromiseRate = campaign.compromiseRate;
      const totalVisibleRate =
        openRate + clickRate + reportRate + compromiseRate;
      const remaining = Math.max(0, 100 - totalVisibleRate);

      const segmentOrder: SegmentKey[] = [
        'openRate',
        'clickRate',
        'reportRate',
        'compromiseRate',
        'remaining',
      ];
      const segmentValues: Record<SegmentKey, number> = {
        openRate,
        clickRate,
        reportRate,
        compromiseRate,
        remaining,
      };
      const firstVisibleSegment =
        segmentOrder.find(key => segmentValues[key] > 0) ?? 'remaining';
      const lastVisibleSegment =
        [...segmentOrder].reverse().find(key => segmentValues[key] > 0) ??
        'remaining';

      return {
        campaignId: campaign.campaignId,
        name: campaign.campaignName,
        sent: campaign.emailsSent,
        opened: campaign.opened,
        clicked: campaign.clicked,
        report: campaign.reported,
        compromise: campaign.dataSubmitted,
        remainingUsers: Math.round(
          campaign.emailsSent * (Math.max(0, 100 - totalVisibleRate) / 100),
        ),
        openRate,
        clickRate,
        reportRate,
        compromiseRate,
        remaining,
        firstVisibleSegment,
        lastVisibleSegment,
      };
    }) ?? [];

  const onCampaignClick = (campaignId: string) => {
    navigate(
      getSimulationPaths(channel).campaignDetails.replace(':id', campaignId),
    );
  };

  return (
    <Card>
      <CardHeader>
        <div className="mb-4 flex items-start justify-between gap-4">
          <CardTitle className="text-lg font-semibold">
            Campaign Performance
          </CardTitle>

          <div className="flex flex-wrap justify-end gap-4 text-xs">
            <LegendItem color={COLORS.opened} label={copy.openedLabel} />
            <LegendItem color={COLORS.clicked} label={copy.clickedLabel} />
            <LegendItem color={COLORS.report} label="Report" />
            <LegendItem color={COLORS.compromise} label="Compromise" />
          </div>
        </div>
      </CardHeader>

      <CardContent>
        {chartData.length === 0 ? (
          <div className="py-8 text-center text-gray-400">
            <ActivityIcon className="mx-auto mb-2 size-12 opacity-50" />
            <p>No data to show</p>
          </div>
        ) : (
          <ResponsiveContainer
            width="100%"
            height={Math.max(120, chartData.length * 64)}
          >
            <BarChart
              data={chartData}
              layout="vertical"
              barCategoryGap={24}
              barSize={18}
              maxBarSize={18}
              accessibilityLayer={false}
            >
              <XAxis type="number" domain={[0, 100]} hide />
              <YAxis
                type="category"
                dataKey="name"
                width={300}
                tick={<CustomYAxisTick onCampaignClick={onCampaignClick} />}
                interval={0}
              />

              <Tooltip
                shared={false}
                isAnimationActive={false}
                content={<CustomTooltip labels={tooltipLabels} />}
              />

              <Bar
                dataKey="openRate"
                stackId="a"
                fill={COLORS.opened}
                shape={createSegmentShape('openRate', 'OpenRateSegmentShape')}
              />
              <Bar
                dataKey="clickRate"
                stackId="a"
                fill={COLORS.clicked}
                shape={createSegmentShape('clickRate', 'ClickRateSegmentShape')}
              />
              <Bar
                dataKey="reportRate"
                stackId="a"
                fill={COLORS.report}
                shape={createSegmentShape(
                  'reportRate',
                  'ReportRateSegmentShape',
                )}
              />
              <Bar
                dataKey="compromiseRate"
                stackId="a"
                fill={COLORS.compromise}
                shape={createSegmentShape(
                  'compromiseRate',
                  'CompromiseRateSegmentShape',
                )}
              />
              <Bar
                dataKey="remaining"
                stackId="a"
                fill={COLORS.remaining}
                shape={createSegmentShape('remaining', 'RemainingSegmentShape')}
              />
            </BarChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
};

export default CampaignPerformanceChart;
