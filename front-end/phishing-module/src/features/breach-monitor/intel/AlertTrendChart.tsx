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

interface AlertTrendPoint {
  date: string;
  high: number;
  medium: number;
  low: number;
}

interface Props {
  data: AlertTrendPoint[];
}

const AlertTrendChart = ({ data }: Props) => {
  const [range, setRange] = useState<TimeRange>('30');

  const filteredData = useMemo(() => {
    const days = parseInt(range);
    return data.slice(-days);
  }, [data, range]);

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-xl">Alert Trend</CardTitle>
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
              <YAxis tick={{ fontSize: 10, fill: 'hsl(220 10% 55%)' }} />
              <Tooltip
                contentStyle={{
                  background: 'hsl(220 20% 12%)',
                  border: '1px solid hsl(220 20% 18%)',
                  borderRadius: 8,
                  fontSize: 12,
                }}
              />
              <Line
                type="monotone"
                dataKey="high"
                stroke="hsl(0 72% 51%)"
                strokeWidth={2}
                dot={{ r: 4, fill: 'hsl(0 72% 51%)' }}
              />
              <Line
                type="monotone"
                dataKey="medium"
                stroke="hsl(45 93% 47%)"
                strokeWidth={2}
                dot={{ r: 4, fill: 'hsl(45 93% 47%)' }}
              />
              <Line
                type="monotone"
                dataKey="low"
                stroke="hsl(142 71% 45%)"
                strokeWidth={2}
                dot={{ r: 4, fill: 'hsl(142 71% 45%)' }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
        <div className="mt-3 flex items-center justify-center gap-4 text-xs">
          <span className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-full bg-[#dc2626]" /> High
          </span>
          <span className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-full bg-[#eab308]" /> Medium
          </span>
          <span className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-full bg-[#22c55e]" /> Low
          </span>
        </div>
      </CardContent>
    </Card>
  );
};

export default AlertTrendChart;
