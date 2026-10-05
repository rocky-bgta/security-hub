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

interface RemediationPoint {
  date: string;
  remediated: number;
}

interface Props {
  data: RemediationPoint[];
}

const RemediationTrendChart = ({ data }: Props) => {
  const [range, setRange] = useState<TimeRange>('30');

  const filteredData = useMemo(() => {
    const days = parseInt(range);
    return data.slice(-days);
  }, [data, range]);

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-xl">Remediation Trend</CardTitle>
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
                labelStyle={{ color: 'white' }}
                itemStyle={{ color: 'white' }}
              />
              <Line
                type="monotone"
                dataKey="remediated"
                stroke="hsl(25 95% 53%)"
                strokeWidth={2}
                dot={{
                  r: 4,
                  fill: 'hsl(25 95% 53%)',
                  stroke: 'hsl(220 20% 10%)',
                  strokeWidth: 2,
                }}
                activeDot={{ r: 6, cursor: 'pointer' }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};

export default RemediationTrendChart;
