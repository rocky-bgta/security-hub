import { TrendingUp } from 'lucide-react';
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

import { Badge } from 'common/Badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { TabsContent } from 'common/Tabs';

interface IProps {
  performanceData: Array<{
    month: string;
    revenue: number;
    clients: number;
    completion: number;
  }>;
  mspComparisonData: Array<{
    name: string;
    revenue: number;
    clients: number;
    growth: number;
    risk: string;
    rating: number;
  }>;
}

const Overview = ({ performanceData, mspComparisonData }: IProps) => {
  return (
    <TabsContent value="overview" className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Performance Trends</CardTitle>
          <CardDescription>
            Revenue, client growth, and satisfaction metrics over time
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={performanceData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis yAxisId="left" />
              <YAxis yAxisId="right" orientation="right" />
              <Tooltip />
              <Legend />
              <Line
                yAxisId="left"
                type="monotone"
                dataKey="revenue"
                stroke="#8884d8"
                name="Revenue ($)"
              />
              <Line
                yAxisId="right"
                type="monotone"
                dataKey="clients"
                stroke="#82ca9d"
                name="New Clients"
              />
              <Line
                yAxisId="right"
                type="monotone"
                dataKey="completion"
                stroke="#ffc658"
                name="Completion %"
              />
            </LineChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Top Performing MSPs</CardTitle>
          <CardDescription>
            Leading MSP partners by revenue and performance metrics
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {mspComparisonData.slice(0, 5).map((msp, index) => (
              <div
                key={msp.name}
                className="flex items-center justify-between rounded-lg border p-4"
              >
                <div className="flex items-center gap-4">
                  <div className="flex size-8 items-center justify-center rounded-full bg-primary/10">
                    <span className="text-sm font-bold text-primary">
                      #{index + 1}
                    </span>
                  </div>
                  <div>
                    <h4 className="font-medium">{msp.name}</h4>
                    <p className="text-sm text-muted-foreground">
                      {msp.clients} clients
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-6">
                  <div className="text-right">
                    <p className="font-medium">
                      ${msp.revenue.toLocaleString()}
                    </p>
                    <div className="flex items-center text-sm text-green-600">
                      <TrendingUp className="mr-1 size-3" />+{msp.growth}%
                    </div>
                  </div>
                  <Badge
                    variant={
                      msp.risk === 'Low'
                        ? 'default'
                        : msp.risk === 'Medium'
                          ? 'secondary'
                          : 'destructive'
                    }
                  >
                    {msp.risk} Risk
                  </Badge>
                  <div className="flex items-center gap-1">
                    <span className="text-sm font-medium">{msp.rating}</span>
                    <span className="text-yellow-500">★</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </TabsContent>
  );
};

export default Overview;
