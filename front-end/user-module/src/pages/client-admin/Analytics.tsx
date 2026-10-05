import { Award, Download, Target, TrendingUp, Users } from 'lucide-react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Progress } from 'common/Progress';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

const analyticsData = {
  overview: {
    totalClients: 156,
    activeClients: 142,
    totalUsers: 45680,
    avgCompletionRate: 78.5,
    totalCertifications: 12450,
    riskReduction: 65,
  },
  industryBreakdown: [
    { industry: 'Banking', clients: 45, users: 18500, completion: 85 },
    {
      industry: 'Telecommunications',
      clients: 28,
      users: 12200,
      completion: 82,
    },
    { industry: 'Government', clients: 35, users: 8900, completion: 75 },
    { industry: 'Healthcare', clients: 22, users: 3800, completion: 70 },
    { industry: 'Manufacturing', clients: 18, users: 2280, completion: 68 },
    { industry: 'Education', clients: 8, users: 1200, completion: 90 },
  ],
  topPerformingClients: [
    { name: 'Bangladesh Bank', completion: 92, users: 1250, risk: 'Low' },
    { name: 'Grameenphone Ltd', completion: 89, users: 2100, risk: 'Low' },
    { name: 'BRAC Bank', completion: 85, users: 650, risk: 'Medium' },
    { name: 'City Bank Limited', completion: 82, users: 850, risk: 'Medium' },
    { name: 'Dutch-Bangla Bank', completion: 78, users: 420, risk: 'Medium' },
  ],
  packagePopularity: [
    { package: 'A-SAT', clients: 128, percentage: 82 },
    { package: 'A-Phish', clients: 95, percentage: 61 },
    { package: 'Banking', clients: 67, percentage: 43 },
    { package: 'HR', clients: 54, percentage: 35 },
    { package: 'Java', clients: 32, percentage: 21 },
    { package: 'C++', clients: 18, percentage: 12 },
  ],
  monthlyProgress: [
    { month: 'Jan', newUsers: 2800, completions: 1850, certifications: 920 },
    { month: 'Feb', newUsers: 3200, completions: 2100, certifications: 1050 },
    { month: 'Mar', newUsers: 2900, completions: 2350, certifications: 1180 },
    { month: 'Apr', newUsers: 3500, completions: 2800, certifications: 1400 },
    { month: 'May', newUsers: 3100, completions: 2650, certifications: 1320 },
    { month: 'Jun', newUsers: 3800, completions: 3200, certifications: 1600 },
  ],
};

const ClientAnalytics = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2>Client Analytics</h2>
          <p className="text-muted-foreground">
            Comprehensive analytics and insights for client performance
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Select>
            <SelectTrigger className="w-32 border-card-border">
              <SelectValue placeholder="Period" />
            </SelectTrigger>
            <SelectContent className="border-card-border bg-secondary">
              <SelectItem value="7d">Last 7 days</SelectItem>
              <SelectItem value="30d">Last 30 days</SelectItem>
              <SelectItem value="90d">Last 90 days</SelectItem>
              <SelectItem value="1y">Last year</SelectItem>
            </SelectContent>
          </Select>
          <Button className="bg-primary text-primary-foreground">
            <Download className="mr-2 size-4" />
            Export Analytics
          </Button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center space-x-4">
              <div className="rounded-lg bg-blue-500/20 p-3">
                <Users className="size-6 text-blue-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-foreground">
                  {analyticsData.overview.totalClients}
                </p>
                <p className="text-sm text-muted-foreground">Total Clients</p>
                <p className="text-xs text-green-400">
                  {analyticsData.overview.activeClients} Active
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center space-x-4">
              <div className="rounded-lg bg-green-500/20 p-3">
                <Target className="size-6 text-green-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-foreground">
                  {analyticsData.overview.avgCompletionRate}%
                </p>
                <p className="text-sm text-muted-foreground">
                  Avg Completion Rate
                </p>
                <p className="text-xs text-green-400">+5.2% from last month</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center space-x-4">
              <div className="rounded-lg bg-purple-500/20 p-3">
                <Award className="size-6 text-purple-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-foreground">
                  {analyticsData.overview.totalCertifications.toLocaleString()}
                </p>
                <p className="text-sm text-muted-foreground">
                  Certifications Earned
                </p>
                <p className="text-xs text-green-400">+12% this quarter</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-foreground">
              Industry Performance
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {analyticsData.industryBreakdown.map(industry => (
                <div key={industry.industry} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-foreground">
                      {industry.industry}
                    </span>
                    <div className="text-right">
                      <span className="text-sm font-medium text-foreground">
                        {industry.clients} clients
                      </span>
                      <span className="ml-2 text-xs text-muted-foreground">
                        ({industry.users.toLocaleString()} users)
                      </span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-muted-foreground">
                      Completion Rate
                    </span>
                    <span className="font-medium text-foreground">
                      {industry.completion}%
                    </span>
                  </div>
                  <Progress value={industry.completion} className="h-2" />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-foreground">
              <TrendingUp className="size-5" />
              Top Performing Clients
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {analyticsData.topPerformingClients.map((client, index) => (
                <Card
                  key={client.name}
                  className="flex items-center justify-between p-3"
                >
                  <div className="flex items-center gap-3">
                    <div className="flex size-8 items-center justify-center rounded-full bg-primary/20 text-sm font-bold text-primary">
                      {index + 1}
                    </div>
                    <div>
                      <p className="font-medium text-foreground">
                        {client.name}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {client.users.toLocaleString()} users
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-medium text-foreground">
                      {client.completion}%
                    </p>
                    <Badge
                      variant="outline"
                      className={
                        client.risk === 'Low'
                          ? 'border-green-400 text-green-400'
                          : 'border-orange-400 text-orange-400'
                      }
                    >
                      {client.risk} Risk
                    </Badge>
                  </div>
                </Card>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-foreground">Package Adoption</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {analyticsData.packagePopularity.map(pkg => (
              <Card key={pkg.package} className="p-4">
                <div className="mb-2 flex items-center justify-between">
                  <h4 className="font-medium text-foreground">{pkg.package}</h4>
                  <span className="text-sm text-muted-foreground">
                    {pkg.percentage}%
                  </span>
                </div>
                <p className="mb-2 text-sm text-muted-foreground">
                  {pkg.clients} clients
                </p>
                <Progress value={pkg.percentage} className="h-2" />
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-foreground">
            Monthly Progress Trends
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="relative flex h-64 items-center justify-center overflow-hidden rounded-lg bg-muted/20">
            <svg className="size-full" viewBox="0 0 600 200">
              <path
                d="M 50 120 L 150 110 L 250 115 L 350 100 L 450 105 L 550 90"
                stroke="#3b82f6"
                strokeWidth="2"
                fill="none"
                className="drop-shadow-sm"
              />

              <path
                d="M 50 140 L 150 130 L 250 125 L 350 115 L 450 120 L 550 105"
                stroke="#00d4aa"
                strokeWidth="2"
                fill="none"
                className="drop-shadow-sm"
              />

              <path
                d="M 50 160 L 150 150 L 250 145 L 350 135 L 450 140 L 550 125"
                stroke="#f59e0b"
                strokeWidth="2"
                fill="none"
                className="drop-shadow-sm"
              />
            </svg>

            <div className="absolute right-4 top-4 space-y-1 text-xs">
              <div className="flex items-center gap-2">
                <div className="h-0.5 w-3 bg-blue-500" />
                <span className="text-muted-foreground">New Users</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-0.5 w-3 bg-primary" />
                <span className="text-muted-foreground">Completions</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-0.5 w-3 bg-yellow-500" />
                <span className="text-muted-foreground">Certifications</span>
              </div>
            </div>

            <div className="absolute inset-x-0 bottom-2 flex justify-between px-12 text-xs text-muted-foreground">
              {analyticsData.monthlyProgress.map(data => (
                <span key={data.month}>{data.month}</span>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ClientAnalytics;
