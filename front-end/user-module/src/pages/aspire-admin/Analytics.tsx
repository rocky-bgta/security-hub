import { useState } from 'react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Activity,
  Clock,
  Download,
  Target,
  TrendingUp,
  Users,
} from 'lucide-react';

const Analytics = () => {
  const [timeRange, setTimeRange] = useState('30');
  const [clientFilter, setClientFilter] = useState('all');

  const analyticsData = {
    totalLogins: 8564,
    activeUsers: 1156,
    avgSessionDuration: '24m 32s',
    engagementScore: 87.3,
    taskCompletionRate: 94.2,
    goalAchievementRate: 78.5,
    trainingCompletion: 89.7,
  };

  const activityMetrics = [
    { label: 'Total Logins', value: '8,564', change: '+12.3%', trending: 'up' },
    { label: 'Active Users', value: '1,156', change: '+8.7%', trending: 'up' },
    {
      label: 'Session Duration',
      value: '24m 32s',
      change: '+5.2%',
      trending: 'up',
    },
    {
      label: 'Feature Usage',
      value: '92.4%',
      change: '-2.1%',
      trending: 'down',
    },
  ];

  const performanceMetrics = [
    {
      label: 'Task Completion',
      value: '94.2%',
      change: '+3.1%',
      trending: 'up',
    },
    {
      label: 'Goal Achievement',
      value: '78.5%',
      change: '+6.8%',
      trending: 'up',
    },
    {
      label: 'Training Completion',
      value: '89.7%',
      change: '+2.4%',
      trending: 'up',
    },
    {
      label: 'Engagement Score',
      value: '87.3',
      change: '+4.2%',
      trending: 'up',
    },
  ];

  const topFeatures = [
    { name: 'Dashboard', usage: 98.2, sessions: 2847 },
    { name: 'Reporting Tools', usage: 85.7, sessions: 1923 },
    { name: 'Profile Management', usage: 76.3, sessions: 1456 },
    { name: 'Task Management', usage: 68.9, sessions: 1234 },
    { name: 'Communication', usage: 54.2, sessions: 987 },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Client User Analytics
          </h1>
          <p className="text-muted-foreground">
            Monitor Client User engagement, activity, and performance metrics
          </p>
        </div>
        <Button variant="outline">
          <Download className="mr-2 size-4" />
          Export Analytics
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="pt-6">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Label htmlFor="time-range">Time Range</Label>
              <Select value={timeRange} onValueChange={setTimeRange}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="24">Last 24 hours</SelectItem>
                  <SelectItem value="7">Last 7 days</SelectItem>
                  <SelectItem value="30">Last 30 days</SelectItem>
                  <SelectItem value="custom">Custom Range</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="client-filter">Client Admin</Label>
              <Select value={clientFilter} onValueChange={setClientFilter}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Client Admins</SelectItem>
                  <SelectItem value="acme">Acme Corporation</SelectItem>
                  <SelectItem value="stellar">Stellar Enterprises</SelectItem>
                  <SelectItem value="fusion">Fusion Systems</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="status-filter">User Status</Label>
              <Select defaultValue="all">
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Users</SelectItem>
                  <SelectItem value="active">Active Only</SelectItem>
                  <SelectItem value="inactive">Inactive Only</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Key Metrics Overview */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Logins</CardTitle>
            <Activity className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {analyticsData.totalLogins.toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground">
              <span className="text-green-600">+12.3%</span> from last period
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Users</CardTitle>
            <Users className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {analyticsData.activeUsers.toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground">
              <span className="text-green-600">+8.7%</span> from last period
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Session</CardTitle>
            <Clock className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {analyticsData.avgSessionDuration}
            </div>
            <p className="text-xs text-muted-foreground">
              <span className="text-green-600">+5.2%</span> from last period
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Engagement Score
            </CardTitle>
            <TrendingUp className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {analyticsData.engagementScore}%
            </div>
            <p className="text-xs text-muted-foreground">
              <span className="text-green-600">+4.2%</span> from last period
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Activity and Performance Metrics */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="size-5" />
              Activity Metrics
            </CardTitle>
            <CardDescription>User activity and engagement data</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {activityMetrics.map((metric, index) => (
              <div key={index} className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {metric.label}
                  </p>
                  <p className="text-2xl font-bold text-foreground">
                    {metric.value}
                  </p>
                </div>
                <Badge
                  variant={metric.trending === 'up' ? 'default' : 'destructive'}
                >
                  {metric.change}
                </Badge>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Target className="size-5" />
              Performance Metrics
            </CardTitle>
            <CardDescription>
              User performance and achievement data
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {performanceMetrics.map((metric, index) => (
              <div key={index} className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {metric.label}
                  </p>
                  <p className="text-2xl font-bold text-foreground">
                    {metric.value}
                  </p>
                </div>
                <Badge
                  variant={metric.trending === 'up' ? 'default' : 'destructive'}
                >
                  {metric.change}
                </Badge>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>

      {/* Feature Usage and User Trends */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Top Features</CardTitle>
            <CardDescription>Most accessed features by users</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {topFeatures.map((feature, index) => (
                <div key={index} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-foreground">
                      {feature.name}
                    </span>
                    <span className="text-sm text-muted-foreground">
                      {feature.usage}%
                    </span>
                  </div>
                  <div className="h-2 w-full rounded-full bg-white">
                    <div
                      className="h-2 rounded-full bg-primary transition-all duration-300"
                      style={{ width: `${feature.usage}%` }}
                    />
                  </div>
                  <div className="text-xs text-muted-foreground">
                    {feature.sessions.toLocaleString()} sessions
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>User Status Distribution</CardTitle>
            <CardDescription>
              Current status breakdown of all users
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="size-3 rounded-full bg-green-500"></div>
                  <span className="text-sm text-foreground">Active Users</span>
                </div>
                <span className="font-bold text-foreground">1,156 (92.7%)</span>
              </div>

              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="size-3 rounded-full bg-yellow-500"></div>
                  <span className="text-sm text-foreground">
                    Inactive Users
                  </span>
                </div>
                <span className="font-bold text-foreground">68 (5.5%)</span>
              </div>

              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="size-3 rounded-full bg-red-500"></div>
                  <span className="text-sm text-foreground">
                    Suspended Users
                  </span>
                </div>
                <span className="font-bold text-foreground">23 (1.8%)</span>
              </div>
            </div>

            <div className="border-t pt-4">
              <div className="mb-2 text-2xl font-bold text-foreground">
                1,247
              </div>
              <p className="text-sm text-muted-foreground">Total Users</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Analytics;
