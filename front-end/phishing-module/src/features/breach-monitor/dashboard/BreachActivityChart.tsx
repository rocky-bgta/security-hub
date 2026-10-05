import { useMemo, useState } from 'react';
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import TimeRangeSelector, {
  TimeRange,
} from 'components/common/TimeRangeSelector';
import { BreachActivityPoint } from 'models/BreachMonitor';

interface Props {
  data: BreachActivityPoint[];
  onPointClick?: (point: BreachActivityPoint) => void;
}

const BreachActivityChart = ({ data, onPointClick }: Props) => {
  const [range, setRange] = useState<TimeRange>('30');

  const filteredData = useMemo(() => {
    const days = parseInt(range);
    return data.slice(-days);
  }, [data, range]);

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-xl">Breach Activity</CardTitle>
          <TimeRangeSelector value={range} onChange={setRange} />
        </div>
      </CardHeader>
      <CardContent>
        <div className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart
              data={filteredData}
              onClick={state => {
                const index = state?.activeTooltipIndex;
                if (
                  typeof index === 'number' &&
                  index >= 0 &&
                  filteredData[index] &&
                  onPointClick
                ) {
                  onPointClick(filteredData[index]);
                }
              }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(220 20% 18%)" />
              <XAxis
                dataKey="date"
                tick={{ fontSize: 11, fill: 'hsl(220 10% 55%)' }}
                tickFormatter={(v: string) => v.slice(5)}
              />
              <YAxis tick={{ fontSize: 11, fill: 'hsl(220 10% 55%)' }} />
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
              <Legend wrapperStyle={{ fontSize: 12 }} />
              <Line
                type="monotone"
                dataKey="critical"
                stroke="hsl(0 72% 51%)"
                strokeWidth={2}
                dot={{ r: 3 }}
                activeDot={{ r: 5, cursor: 'pointer' }}
              />
              <Line
                type="monotone"
                dataKey="high"
                stroke="hsl(25 95% 53%)"
                strokeWidth={2}
                dot={{ r: 3 }}
                activeDot={{ r: 5, cursor: 'pointer' }}
              />
              <Line
                type="monotone"
                dataKey="medium"
                stroke="hsl(45 93% 47%)"
                strokeWidth={2}
                dot={{ r: 3 }}
                activeDot={{ r: 5, cursor: 'pointer' }}
              />
              <Line
                type="monotone"
                dataKey="low"
                stroke="hsl(142 71% 45%)"
                strokeWidth={2}
                dot={{ r: 3 }}
                activeDot={{ r: 5, cursor: 'pointer' }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};

export default BreachActivityChart;
