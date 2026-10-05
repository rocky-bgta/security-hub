import {
  DollarSign,
  Download,
  FileText,
  Package,
  TrendingUp,
  Users,
} from 'lucide-react';
import { useState } from 'react';

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
import ExportReport from 'features/report/export/ExportReport';
import LicenseUsageReport from 'features/report/license-usage/LicenseUsageReport';
import PerformanceReport from 'features/report/performance/PerformanceReport';
import RevenueAnalytics from 'features/report/revenue/RevenueAnalytics';

const reportData = {
  summary: {
    totalMSPs: 24,
    activeMSPs: 18,
    totalRevenue: '$2,450,000',
    avgClientsPerMSP: 28,
    totalLicensesDistributed: 45000,
    licensesUtilization: 78,
  },
  topPerformers: [
    {
      name: 'TechSecure Solutions',
      clients: 45,
      revenue: '$125,000',
      growth: '+15%',
    },
    {
      name: 'CyberGuard Enterprise',
      clients: 32,
      revenue: '$89,500',
      growth: '+12%',
    },
    {
      name: 'SecureNet Partners',
      clients: 28,
      revenue: '$76,200',
      growth: '+8%',
    },
    {
      name: 'InfoShield Corp',
      clients: 22,
      revenue: '$65,800',
      growth: '+5%',
    },
  ],
  packageDistribution: [
    { package: 'A-SAT', msps: 18, percentage: 75 },
    { package: 'A-Phish', msps: 15, percentage: 62 },
    { package: 'HR', msps: 12, percentage: 50 },
    { package: 'Banking', msps: 10, percentage: 42 },
    { package: 'Java', msps: 8, percentage: 33 },
    { package: 'C++', msps: 6, percentage: 25 },
  ],
  monthlyGrowth: [
    { month: 'Jan', newMSPs: 2, revenue: 185000 },
    { month: 'Feb', newMSPs: 1, revenue: 195000 },
    { month: 'Mar', newMSPs: 3, revenue: 220000 },
    { month: 'Apr', newMSPs: 2, revenue: 235000 },
    { month: 'May', newMSPs: 1, revenue: 245000 },
    { month: 'Jun', newMSPs: 2, revenue: 260000 },
  ],
};

const MSPReports = () => {
  const [performanceDialogOpen, setPerformanceDialogOpen] =
    useState<boolean>(false);
  const [licenseDialogOpen, setLicenseDialogOpen] = useState<boolean>(false);
  const [revenueDialogOpen, setRevenueDialogOpen] = useState<boolean>(false);
  const [exportDialogOpen, setExportDialogOpen] = useState<boolean>(false);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2>MSP Reports</h2>
          <p className="text-muted-foreground">
            Comprehensive analytics and performance reports for MSP partners
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Select>
            <SelectTrigger className="w-32">
              <SelectValue placeholder="Period" />
            </SelectTrigger>
            <SelectContent className="border-card-border bg-secondary">
              <SelectItem value="7d">Last 7 days</SelectItem>
              <SelectItem value="30d">Last 30 days</SelectItem>
              <SelectItem value="90d">Last 90 days</SelectItem>
              <SelectItem value="1y">Last year</SelectItem>
            </SelectContent>
          </Select>
          <Button
            className="bg-primary text-primary-foreground"
            onClick={() => setExportDialogOpen(true)}
          >
            <Download className="mr-2 size-4" />
            Export Report
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
                  {reportData.summary.totalMSPs}
                </p>
                <p className="text-sm text-muted-foreground">Total MSPs</p>
                <p className="text-xs text-green-400">
                  {reportData.summary.activeMSPs} Active
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center space-x-4">
              <div className="rounded-lg bg-green-500/20 p-3">
                <DollarSign className="size-6 text-green-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-foreground">
                  {reportData.summary.totalRevenue}
                </p>
                <p className="text-sm text-muted-foreground">Total Revenue</p>
                <p className="text-xs text-green-400">+18% from last month</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center space-x-4">
              <div className="rounded-lg bg-purple-500/20 p-3">
                <Package className="size-6 text-purple-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-foreground">
                  {reportData.summary.totalLicensesDistributed.toLocaleString()}
                </p>
                <p className="text-sm text-muted-foreground">
                  Licenses Distributed
                </p>
                <p className="text-xs text-green-400">
                  {reportData.summary.licensesUtilization}% Utilization
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-foreground">
              <TrendingUp className="size-5" />
              Top Performing MSPs
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {reportData.topPerformers.map((msp, index) => (
                <div
                  key={msp.name}
                  className="flex items-center justify-between rounded-lg border border-card-border p-3"
                >
                  <div className="flex items-center gap-3">
                    <div className="flex size-8 items-center justify-center rounded-full bg-primary/20 text-sm font-bold text-primary">
                      {index + 1}
                    </div>
                    <div>
                      <p className="font-medium text-foreground">{msp.name}</p>
                      <p className="text-sm text-muted-foreground">
                        {msp.clients} clients
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-medium text-foreground">{msp.revenue}</p>
                    <p className="text-sm text-green-400">{msp.growth}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-foreground">
              <Package className="size-5" />
              Package Distribution
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {reportData.packageDistribution.map(pkg => (
                <div key={pkg.package} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-foreground">
                      {pkg.package}
                    </span>
                    <div className="text-right">
                      <span className="text-sm font-medium text-foreground">
                        {pkg.msps} MSPs
                      </span>
                      <span className="ml-2 text-xs text-muted-foreground">
                        ({pkg.percentage}%)
                      </span>
                    </div>
                  </div>
                  <Progress value={pkg.percentage} className="h-2" />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-foreground">
            Monthly Growth Trends
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="relative flex h-64 items-center justify-center overflow-hidden rounded-lg bg-muted/20">
            <svg className="size-full" viewBox="0 0 600 200">
              <path
                d="M 50 150 L 150 140 L 250 120 L 350 110 L 450 105 L 550 95"
                stroke="#00d4aa"
                strokeWidth="3"
                fill="none"
                className="drop-shadow-sm"
              />

              <path
                d="M 50 160 L 150 155 L 250 145 L 350 150 L 450 155 L 550 145"
                stroke="#3b82f6"
                strokeWidth="2"
                fill="none"
                className="drop-shadow-sm"
              />

              {reportData.monthlyGrowth.map((data, index) => (
                <g key={data.month}>
                  <circle
                    cx={50 + index * 100}
                    cy={150 - (data.revenue - 180000) / 1000}
                    r="4"
                    fill="#00d4aa"
                  />
                  <circle
                    cx={50 + index * 100}
                    cy={160 - data.newMSPs * 5}
                    r="3"
                    fill="#3b82f6"
                  />
                </g>
              ))}
            </svg>

            <div className="absolute right-4 top-4 space-y-1 text-xs">
              <div className="flex items-center gap-2">
                <div className="h-0.5 w-3 bg-primary" />
                <span className="text-muted-foreground">Revenue</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-0.5 w-3 bg-blue-500" />
                <span className="text-muted-foreground">New MSPs</span>
              </div>
            </div>

            <div className="absolute inset-x-0 bottom-2 flex justify-between px-12 text-xs text-muted-foreground">
              {reportData.monthlyGrowth.map(data => (
                <span key={data.month}>{data.month}</span>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-foreground">
            Quick Report Actions
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-3">
            <Button
              variant="outline"
              className="flex h-20 flex-col gap-2"
              onClick={() => setPerformanceDialogOpen(true)}
            >
              <FileText className="size-6" />
              <span>Generate MSP Performance Report</span>
            </Button>
            <Button
              variant="outline"
              className="flex h-20 flex-col gap-2"
              onClick={() => setLicenseDialogOpen(true)}
            >
              <Download className="size-6" />
              <span>Export License Usage Report</span>
            </Button>
            <Button
              variant="outline"
              className="flex h-20 flex-col gap-2"
              onClick={() => setRevenueDialogOpen(true)}
            >
              <TrendingUp className="size-6" />
              <span>Revenue Analytics Report</span>
            </Button>
          </div>
        </CardContent>
      </Card>

      <PerformanceReport
        open={performanceDialogOpen}
        onOpenChange={setPerformanceDialogOpen}
      />

      <LicenseUsageReport
        open={licenseDialogOpen}
        onOpenChange={setLicenseDialogOpen}
      />

      <RevenueAnalytics
        open={revenueDialogOpen}
        onOpenChange={setRevenueDialogOpen}
      />

      <ExportReport
        open={exportDialogOpen}
        onOpenChange={setExportDialogOpen}
      />
    </div>
  );
};

export default MSPReports;
