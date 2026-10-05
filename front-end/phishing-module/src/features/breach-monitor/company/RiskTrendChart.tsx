import { useState, useMemo } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import TimeRangeSelector, {
  type TimeRange,
} from 'components/common/TimeRangeSelector';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';

interface RiskPoint {
  date: string;
  riskLevel: string;
  value: number;
}

interface Props {
  data: RiskPoint[];
}

const riskColors: Record<string, string> = {
  low: 'hsl(142 71% 45%)',
  medium: 'hsl(45 93% 47%)',
  high: 'hsl(0 72% 51%)',
};

interface CustomDotProps {
  cx?: number;
  cy?: number;
  payload?: RiskPoint;
}

function CustomDot({ cx, cy, payload }: CustomDotProps) {
  return (
    <circle
      cx={cx}
      cy={cy}
      r={5}
      fill={riskColors[payload?.riskLevel ?? 'medium'] || riskColors.medium}
      stroke="hsl(220 20% 10%)"
      strokeWidth={2}
    />
  );
}

const RiskTrendChart = ({ data }: Props) => {
  const [range, setRange] = useState<TimeRange>('30');

  const filteredData = useMemo(() => {
    const days = parseInt(range);
    return data.slice(-days);
  }, [data, range]);

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-xl">Risk Trend</CardTitle>
          <TimeRangeSelector value={range} onChange={setRange} />
        </div>
      </CardHeader>
      <CardContent>
        <div className="h-56">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={filteredData}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(220 20% 18%)" />
              <XAxis
                dataKey="date"
                tick={{ fontSize: 10, fill: 'hsl(220 10% 55%)' }}
                tickFormatter={(v: string) => v.slice(5)}
              />
              <YAxis
                tick={{ fontSize: 10, fill: 'hsl(220 10% 55%)' }}
                domain={[0, 10]}
                tickFormatter={(v: number) =>
                  v <= 3 ? 'LO' : v <= 6 ? 'MO' : 'HI'
                }
              />
              <Tooltip
                contentStyle={{
                  background: 'hsl(220 20% 12%)',
                  border: '1px solid hsl(220 20% 18%)',
                  borderRadius: 8,
                  fontSize: 12,
                }}
                labelFormatter={label => `Date: ${label}`}
                formatter={(value, _name, item) => [
                  `Risk: ${(item.payload as RiskPoint).riskLevel.toUpperCase()} (${value ?? ''})`,
                  '',
                ]}
                labelStyle={{ color: 'white' }}
                itemStyle={{ color: 'white' }}
              />
              <Line
                type="monotone"
                dataKey="value"
                stroke="hsl(45 93% 47%)"
                strokeWidth={2}
                dot={<CustomDot />}
                activeDot={{ r: 7, cursor: 'pointer' }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
        <div className="mt-3 flex items-center justify-center gap-4 text-xs text-muted-foreground">
          <span className="flex items-center gap-1.5">
            <span
              className="size-2.5 rounded-full"
              style={{ background: riskColors.low }}
            />{' '}
            Low
          </span>
          <span className="flex items-center gap-1.5">
            <span
              className="size-2.5 rounded-full"
              style={{ background: riskColors.medium }}
            />{' '}
            Medium
          </span>
          <span className="flex items-center gap-1.5">
            <span
              className="size-2.5 rounded-full"
              style={{ background: riskColors.high }}
            />{' '}
            High
          </span>
        </div>
      </CardContent>
    </Card>
  );
};

export default RiskTrendChart;
