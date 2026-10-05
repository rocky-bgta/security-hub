import { TrendingUp } from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { TabsContent } from 'common/Tabs';

const Regional = () => {
  const regionData = [
    { region: 'North America', msps: 45, revenue: 450000, growth: 15 },
    { region: 'Europe', msps: 32, revenue: 320000, growth: 12 },
    { region: 'Asia Pacific', msps: 28, revenue: 280000, growth: 22 },
    { region: 'Latin America', msps: 15, revenue: 150000, growth: 18 },
    { region: 'Middle East', msps: 12, revenue: 120000, growth: 25 },
  ];

  return (
    <TabsContent value="regional" className="space-y-4">
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Regional Distribution</CardTitle>
            <CardDescription>
              MSP distribution and revenue by region
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={regionData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="region" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="msps" fill="#8884d8" name="MSPs" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Regional Growth</CardTitle>
            <CardDescription>Growth rates by geographic region</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={regionData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="region" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="growth" fill="#82ca9d" name="Growth %" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Regional Performance Details</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {regionData.map(region => (
              <div
                key={region.region}
                className="flex items-center justify-between rounded-lg border p-4"
              >
                <div>
                  <h4 className="font-medium">{region.region}</h4>
                  <p className="text-sm text-muted-foreground">
                    {region.msps} MSPs
                  </p>
                </div>
                <div className="flex items-center gap-6">
                  <div className="text-right">
                    <p className="font-medium">
                      ${region.revenue.toLocaleString()}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      Total Revenue
                    </p>
                  </div>
                  <div className="flex items-center">
                    <TrendingUp className="mr-1 size-4 text-green-600" />
                    <span className="font-medium text-green-600">
                      +{region.growth}%
                    </span>
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

export default Regional;
