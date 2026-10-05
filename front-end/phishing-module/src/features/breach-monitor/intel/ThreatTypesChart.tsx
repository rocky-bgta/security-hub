import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import type { ThreatTypeData } from 'models/BreachMonitor';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

interface Props {
  data: ThreatTypeData[];
}

const ThreatTypesChart = ({ data }: Props) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Threat Types</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="h-56">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(220 20% 18%)" />
              <XAxis
                dataKey="name"
                tick={{ fontSize: 10, fill: 'hsl(220 10% 55%)' }}
              />
              <YAxis tick={{ fontSize: 10, fill: 'hsl(220 10% 55%)' }} />
              <Tooltip
                contentStyle={{
                  background: 'hsl(220 20% 12%)',
                  border: '1px solid hsl(220 20% 18%)',
                  borderRadius: 8,
                  fontSize: 12,
                  color: 'white',
                }}
                labelStyle={{ color: 'white' }}
                itemStyle={{ color: 'white' }}
              />
              <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                {data.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="mt-3 flex flex-wrap items-center justify-center gap-3 text-xs text-muted-foreground">
          {data.map(d => (
            <span key={d.name} className="flex items-center gap-1.5">
              <span
                className="size-2.5 rounded-sm"
                style={{ background: d.color }}
              />
              {d.name}
            </span>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default ThreatTypesChart;
