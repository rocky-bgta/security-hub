import { CHART_COLORS, ITrendDataPoint } from 'models/Dashboard';
import {
  Area,
  AreaChart,
  CartesianGrid,
  Legend,
  Line,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { ValueType } from 'recharts/types/component/DefaultTooltipContent';
import { NameType } from 'recharts/types/component/DefaultTooltipContent';

export interface DashboardLineChartSeries {
  key: string;
  name: string;
  color: string;
}

type DashboardLineChartDataPoint = {
  date: string;
  label?: string;
  value?: number | null;
  [key: string]: string | number | null | undefined;
};

export interface DashboardLineChartProps {
  data: DashboardLineChartDataPoint[] | ITrendDataPoint[];
  title?: string;
  color?: string;
  height?: number;
  valueSuffix?: string;
  series?: DashboardLineChartSeries[];
}

const formatDateTick = (date: string) =>
  date && date.length >= 10 ? date.slice(5) : date;

function formatTickValue(v: number, suffix?: string): string {
  const n = Number(v);
  const base = Number.isInteger(n) ? String(n) : n.toFixed(1);
  return suffix ? `${base}${suffix}` : base;
}

export const DashboardLineChart = ({
  data,
  title,
  color = CHART_COLORS.primary,
  height = 220,
  valueSuffix,
  series,
}: DashboardLineChartProps) => {
  if (!data?.length) {
    return (
      <div
        className="flex items-center justify-center rounded-xl"
        style={{ height }}
      >
        <p className="text-sm text-muted-foreground">No trend data available</p>
      </div>
    );
  }

  const seriesName = title?.replace(/\s*Trend\s*$/i, '').trim() || 'Value';
  const resolvedSeries: DashboardLineChartSeries[] = series?.length
    ? series
    : [{ key: 'value', name: seriesName, color }];
  const isSinglePoint = data.length === 1;

  return (
    <div className="w-full">
      {title && (
        <h3 className="mb-4 text-sm font-semibold tracking-tight text-foreground">
          {title}
        </h3>
      )}

      <div style={{ height }}>
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart
            data={data as ITrendDataPoint[]}
            margin={{ top: 10, right: 16, left: 0, bottom: 0 }}
          >
            <defs>
              {resolvedSeries.map(lineSeries => (
                <linearGradient
                  key={lineSeries.key}
                  id={`lineGradient-${lineSeries.key}`}
                  x1="0"
                  y1="0"
                  x2="0"
                  y2="1"
                >
                  <stop
                    offset="5%"
                    stopColor={lineSeries.color}
                    stopOpacity={0.4}
                  />
                  <stop
                    offset="95%"
                    stopColor={lineSeries.color}
                    stopOpacity={0}
                  />
                </linearGradient>
              ))}
            </defs>

            <CartesianGrid
              strokeDasharray="2 4"
              stroke="hsl(var(--border))"
              vertical={false}
              opacity={0.4}
            />

            <XAxis
              dataKey="date"
              tickFormatter={formatDateTick}
              tick={{ fontSize: 11, fill: 'white' }}
              tickMargin={10}
              axisLine={false}
              tickLine={false}
              fill="var(--card-background)"
            />

            <YAxis
              tickFormatter={v => formatTickValue(Number(v), valueSuffix)}
              tick={{ fontSize: 11, fill: 'white' }}
              width={40}
              fill="var(--card-background)"
              axisLine={false}
              tickLine={false}
            />

            <Tooltip
              cursor={{
                stroke: resolvedSeries[0].color,
                strokeDasharray: '3 3',
              }}
              itemSorter={item => {
                const dataKey = String(item.dataKey ?? '');
                const seriesIndex = resolvedSeries.findIndex(
                  lineSeries => lineSeries.key === dataKey,
                );
                return seriesIndex === -1
                  ? Number.MAX_SAFE_INTEGER
                  : seriesIndex;
              }}
              formatter={(
                value: ValueType | undefined,
                name: NameType | undefined,
              ) => [
                formatTickValue(Number(value), valueSuffix),
                String(name || seriesName),
              ]}
              labelFormatter={(_, payload) =>
                payload?.[0]?.payload?.label || payload?.[0]?.payload?.date
              }
              contentStyle={{
                borderRadius: 10,
                border: '1px solid hsl(var(--border))',
                background: 'hsl(var(--card))',
                fontSize: 12,
              }}
            />

            {resolvedSeries.length > 1 && (
              <Legend
                iconType="line"
                wrapperStyle={{ fontSize: 12 }}
                itemSorter={item => {
                  const dataKey = String(item.dataKey ?? '');
                  const seriesIndex = resolvedSeries.findIndex(
                    lineSeries => lineSeries.key === dataKey,
                  );
                  return seriesIndex === -1
                    ? Number.MAX_SAFE_INTEGER
                    : seriesIndex;
                }}
              />
            )}

            {resolvedSeries.map(lineSeries => (
              <Area
                key={`area-${lineSeries.key}`}
                type="monotone"
                dataKey={lineSeries.key}
                stroke="none"
                fill={`url(#lineGradient-${lineSeries.key})`}
                fillOpacity={0.4}
                legendType="none"
                tooltipType="none"
              />
            ))}

            {isSinglePoint &&
              resolvedSeries.map(lineSeries => {
                const firstPoint = data[0] as DashboardLineChartDataPoint;
                const yValue = Number(firstPoint?.[lineSeries.key]);
                if (!Number.isFinite(yValue)) return null;

                return (
                  <ReferenceLine
                    key={`single-point-reference-${lineSeries.key}`}
                    y={yValue}
                    stroke={lineSeries.color}
                    strokeWidth={2}
                    fill={lineSeries.color}
                    fillOpacity={0.4}
                    strokeOpacity={0.7}
                    ifOverflow="extendDomain"
                  />
                );
              })}

            {resolvedSeries.map(lineSeries => (
              <Line
                key={lineSeries.key}
                type="monotone"
                dataKey={lineSeries.key}
                name={lineSeries.name}
                stroke={lineSeries.color}
                strokeWidth={3}
                dot={true}
                activeDot={{
                  r: 6,
                  stroke: lineSeries.color,
                  strokeWidth: 2,
                  fill: '#fff',
                }}
              />
            ))}
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};
