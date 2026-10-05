import {
  Area,
  AreaChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart as RechartsPieChart,
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
import { Progress } from 'common/Progress';
import { TabsContent } from 'common/Tabs';

interface IProps {
  performanceData: Array<{
    month: string;
    revenue: number;
    clients: number;
    completion: number;
  }>;
}

const Services = ({ performanceData }: IProps) => {
  const serviceUsageData = [
    { name: 'Phishing Simulation', value: 35, color: '#8884d8' },
    { name: 'Security Training', value: 28, color: '#82ca9d' },
    { name: 'Compliance Tracking', value: 20, color: '#ffc658' },
    { name: 'Risk Assessment', value: 12, color: '#ff7300' },
    { name: 'Incident Response', value: 5, color: '#00ff88' },
  ];

  return (
    <TabsContent value="services" className="space-y-4">
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Service Usage Distribution</CardTitle>
            <CardDescription>
              Most popular services among MSP clients
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <RechartsPieChart>
                <Pie
                  data={serviceUsageData}
                  cx="50%"
                  cy="50%"
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                  label={({ name, value }) => `${name}: ${value}%`}
                >
                  {serviceUsageData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </RechartsPieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Service Performance Metrics</CardTitle>
            <CardDescription>Effectiveness and adoption rates</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {serviceUsageData.map(service => (
                <div key={service.name} className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-sm font-medium">{service.name}</span>
                    <span className="text-sm text-muted-foreground">
                      {service.value}%
                    </span>
                  </div>
                  <Progress value={service.value} className="h-2" />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Service Adoption Trends</CardTitle>
          <CardDescription>Monthly service usage growth</CardDescription>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={performanceData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Area
                type="monotone"
                dataKey="completion"
                stackId="1"
                stroke="#8884d8"
                fill="#8884d8"
              />
            </AreaChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>
    </TabsContent>
  );
};

export default Services;
