import {
  Building2,
  CheckCircle,
  DollarSign,
  Download,
  Target,
  TrendingUp,
} from 'lucide-react';
import { useState } from 'react';

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Tabs, TabsList, TabsTrigger } from 'common/Tabs';
import Overview from 'features/analytics/Overview';
import Performance from 'features/analytics/Performance';
import Regional from 'features/analytics/Regional';
import Services from 'features/analytics/Services';

const performanceData = [
  {
    month: 'Jan',
    revenue: 45000,
    clients: 12,
    completion: 78,
    satisfaction: 4.2,
  },
  {
    month: 'Feb',
    revenue: 52000,
    clients: 15,
    completion: 82,
    satisfaction: 4.3,
  },
  {
    month: 'Mar',
    revenue: 48000,
    clients: 14,
    completion: 85,
    satisfaction: 4.1,
  },
  {
    month: 'Apr',
    revenue: 61000,
    clients: 18,
    completion: 88,
    satisfaction: 4.4,
  },
  {
    month: 'May',
    revenue: 58000,
    clients: 17,
    completion: 91,
    satisfaction: 4.5,
  },
  {
    month: 'Jun',
    revenue: 67000,
    clients: 21,
    completion: 89,
    satisfaction: 4.6,
  },
];

const mspComparisonData = [
  {
    name: 'TechSecure Pro',
    revenue: 125000,
    clients: 45,
    growth: 23,
    rating: 4.8,
    risk: 'Low',
  },
  {
    name: 'CyberGuard Solutions',
    revenue: 98000,
    clients: 38,
    growth: 18,
    rating: 4.6,
    risk: 'Low',
  },
  {
    name: 'SecureIT Partners',
    revenue: 87000,
    clients: 32,
    growth: 15,
    rating: 4.4,
    risk: 'Medium',
  },
  {
    name: 'DataShield MSP',
    revenue: 76000,
    clients: 28,
    growth: 12,
    rating: 4.2,
    risk: 'Medium',
  },
  {
    name: 'InfoSec Experts',
    revenue: 65000,
    clients: 24,
    growth: 8,
    rating: 4.0,
    risk: 'High',
  },
  {
    name: 'CyberDefense Co',
    revenue: 54000,
    clients: 19,
    growth: 5,
    rating: 3.8,
    risk: 'High',
  },
];

const MSPAnalytics = () => {
  const [timeRange, setTimeRange] = useState<string>('6months');

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2>MSP Analytics</h2>
          <p className="text-muted-foreground">
            Comprehensive analytics and performance insights for all MSP
            partners
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Select value={timeRange} onValueChange={setTimeRange}>
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="1month">Last Month</SelectItem>
              <SelectItem value="3months">Last 3 Months</SelectItem>
              <SelectItem value="6months">Last 6 Months</SelectItem>
              <SelectItem value="1year">Last Year</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" size="sm">
            <Download className="mr-2 size-4" />
            Export
          </Button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total MSPs</CardTitle>
            <Building2 className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">132</div>
            <div className="flex items-center text-xs text-green-600">
              <TrendingUp className="mr-1 size-3" />
              +12% from last month
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <DollarSign className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">$1.32M</div>
            <div className="flex items-center text-xs text-green-600">
              <TrendingUp className="mr-1 size-3" />
              +18% from last month
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Avg Completion Rate
            </CardTitle>
            <Target className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">87%</div>
            <div className="flex items-center text-xs text-green-600">
              <TrendingUp className="mr-1 size-3" />
              +5% from last month
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Avg Satisfaction
            </CardTitle>
            <CheckCircle className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">4.4/5</div>
            <div className="flex items-center text-xs text-green-600">
              <TrendingUp className="mr-1 size-3" />
              +0.2 from last month
            </div>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="performance">Performance</TabsTrigger>
          <TabsTrigger value="regional">Regional Analysis</TabsTrigger>
          <TabsTrigger value="services">Service Usage</TabsTrigger>
        </TabsList>

        <Overview
          performanceData={performanceData}
          mspComparisonData={mspComparisonData}
        />

        <Performance mspComparisonData={mspComparisonData} />

        <Regional />

        <Services performanceData={performanceData} />
      </Tabs>
    </div>
  );
};

export default MSPAnalytics;
