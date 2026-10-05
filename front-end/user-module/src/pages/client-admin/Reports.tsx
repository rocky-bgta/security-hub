import { Award, Download, Target, TrendingUp, Users } from 'lucide-react';
import { useState } from 'react';

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

const usageReportData = {
  client: {
    id: 'CLT001',
    name: 'Bangladesh Bank',
    reportPeriod: 'Last 30 Days',
    generatedDate: '2024-01-15',
  },
  summary: {
    totalUsers: 1250,
    activeUsers: 1180,
    coursesCompleted: 2850,
    certificationsEarned: 420,
    phishingTestsPassed: 1680,
    averageCompletionRate: 78.5,
    riskReduction: 35,
  },
  packageUsage: [
    {
      name: 'A-SAT',
      assignedLicenses: 200,
      usedLicenses: 180,
      completedCourses: 165,
      averageScore: 85,
      timeSpent: '1,250 hours',
      certificationsEarned: 165,
    },
    {
      name: 'Banking Module',
      assignedLicenses: 200,
      usedLicenses: 190,
      completedCourses: 175,
      averageScore: 88,
      timeSpent: '1,400 hours',
      certificationsEarned: 175,
    },
    {
      name: 'A-Phish',
      assignedLicenses: 100,
      usedLicenses: 80,
      completedCourses: 70,
      averageScore: 82,
      timeSpent: '320 hours',
      certificationsEarned: 70,
    },
  ],
  departmentBreakdown: [
    { department: 'IT Security', users: 45, completion: 92, avgScore: 89 },
    { department: 'Operations', users: 350, completion: 85, avgScore: 84 },
    { department: 'Finance', users: 280, completion: 78, avgScore: 81 },
    { department: 'Human Resources', users: 120, completion: 75, avgScore: 79 },
    { department: 'Compliance', users: 85, completion: 88, avgScore: 87 },
    { department: 'Branch Network', users: 370, completion: 72, avgScore: 76 },
  ],
  monthlyProgress: [
    { month: 'Oct', completions: 180, certifications: 25, phishingPassed: 120 },
    { month: 'Nov', completions: 220, certifications: 35, phishingPassed: 150 },
    { month: 'Dec', completions: 280, certifications: 45, phishingPassed: 180 },
    { month: 'Jan', completions: 320, certifications: 55, phishingPassed: 210 },
  ],
  topPerformers: [
    {
      name: 'Karim Hassan',
      department: 'IT Security',
      completion: 100,
      score: 95,
    },
    {
      name: 'Fatima Khatun',
      department: 'Operations',
      completion: 95,
      score: 92,
    },
    {
      name: 'Ahmed Rahman',
      department: 'IT Security',
      completion: 90,
      score: 90,
    },
    { name: 'Rashida Begum', department: 'HR', completion: 88, score: 88 },
    { name: 'Mohammad Ali', department: 'IT', completion: 85, score: 87 },
  ],
  riskAssessment: {
    highRisk: 45,
    mediumRisk: 180,
    lowRisk: 1025,
    riskReduction: 35,
    vulnerabilities: [
      {
        type: 'Phishing Susceptibility',
        before: 65,
        after: 25,
        improvement: 62,
      },
      { type: 'Password Security', before: 45, after: 15, improvement: 67 },
      { type: 'Social Engineering', before: 55, after: 20, improvement: 64 },
      { type: 'Data Handling', before: 40, after: 12, improvement: 70 },
    ],
  },
};

const ClientReports = () => {
  const [reportPeriod, setReportPeriod] = useState<string>('30d');
  const [pdfDialogOpen, setPdfDialogOpen] = useState<boolean>(false);
  const [excelDialogOpen, setExcelDialogOpen] = useState<boolean>(false);
  const [scheduleDialogOpen, setScheduleDialogOpen] = useState<boolean>(false);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2>Usage Report</h2>
          <p className="text-muted-foreground">
            Detailed usage analytics for {usageReportData.client.name}
          </p>
        </div>

        <div className="flex gap-2">
          <Select value={reportPeriod} onValueChange={setReportPeriod}>
            <SelectTrigger className="w-32 border-card-border">
              <SelectValue />
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
            Export Report
          </Button>
        </div>
      </div>

      <Card>
        <CardContent className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold text-foreground">
                {usageReportData.client.name}
              </h2>
              <p className="text-muted-foreground">
                Usage Report - {usageReportData.client.reportPeriod}
              </p>
            </div>
            <div className="text-right">
              <p className="text-sm text-muted-foreground">Generated on</p>
              <p className="text-sm font-medium text-foreground">
                {usageReportData.client.generatedDate}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center space-x-4">
              <div className="rounded-lg bg-blue-500/20 p-3">
                <Users className="size-6 text-blue-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-foreground">
                  {usageReportData.summary.activeUsers}
                </p>
                <p className="text-sm text-muted-foreground">Active Users</p>
                <p className="text-xs text-green-400">
                  {Math.round(
                    (usageReportData.summary.activeUsers /
                      usageReportData.summary.totalUsers) *
                      100,
                  )}
                  % of total
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
                  {usageReportData.summary.coursesCompleted}
                </p>
                <p className="text-sm text-muted-foreground">
                  Courses Completed
                </p>
                <p className="text-xs text-green-400">+15% from last period</p>
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
                  {usageReportData.summary.certificationsEarned}
                </p>
                <p className="text-sm text-muted-foreground">
                  Certifications Earned
                </p>
                <p className="text-xs text-green-400">+22% from last period</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center space-x-4">
              <div className="rounded-lg bg-orange-500/20 p-3">
                <TrendingUp className="size-6 text-orange-400" />
              </div>
              <div>
                <p className="text-2xl font-bold text-foreground">
                  {usageReportData.summary.averageCompletionRate}%
                </p>
                <p className="text-sm text-muted-foreground">
                  Avg Completion Rate
                </p>
                <p className="text-xs text-green-400">+8% from last period</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-foreground">
              Package Usage Breakdown
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              {usageReportData.packageUsage.map(pkg => (
                <div key={pkg.name} className="space-y-3">
                  <div className="flex items-center justify-between">
                    <h4 className="font-medium text-foreground">{pkg.name}</h4>
                    <Badge variant="outline" className="text-xs">
                      {Math.round(
                        (pkg.usedLicenses / pkg.assignedLicenses) * 100,
                      )}
                      % utilized
                    </Badge>
                  </div>

                  <div className="grid grid-cols-3 gap-4 text-sm">
                    <div>
                      <p className="text-muted-foreground">Completed</p>
                      <p className="font-medium text-foreground">
                        {pkg.completedCourses}
                      </p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">Avg Score</p>
                      <p className="font-medium text-foreground">
                        {pkg.averageScore}%
                      </p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">Time Spent</p>
                      <p className="font-medium text-foreground">
                        {pkg.timeSpent}
                      </p>
                    </div>
                  </div>

                  <Progress
                    value={(pkg.usedLicenses / pkg.assignedLicenses) * 100}
                    className="h-2"
                  />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-foreground">
              Department Performance
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {usageReportData.departmentBreakdown.map(dept => (
                <div key={dept.department} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-foreground">
                      {dept.department}
                    </span>
                    <div className="text-right">
                      <span className="text-sm font-medium text-foreground">
                        {dept.completion}%
                      </span>
                      <span className="ml-2 text-xs text-muted-foreground">
                        ({dept.users} users)
                      </span>
                    </div>
                  </div>
                  <Progress value={dept.completion} className="h-2" />
                  <div className="flex justify-between text-xs text-muted-foreground">
                    <span>Avg Score: {dept.avgScore}%</span>
                    <span>Completion: {dept.completion}%</span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-foreground">
            Monthly Progress Trends
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="relative flex h-64 items-center justify-center overflow-hidden rounded-lg">
            <svg className="size-full" viewBox="0 0 600 200">
              <path
                d="M 50 150 L 200 130 L 350 110 L 500 90"
                stroke="#00d4aa"
                strokeWidth="3"
                fill="none"
                className="drop-shadow-sm"
              />

              <path
                d="M 50 170 L 200 160 L 350 145 L 500 125"
                stroke="#3b82f6"
                strokeWidth="2"
                fill="none"
                className="drop-shadow-sm"
              />

              <path
                d="M 50 160 L 200 145 L 350 125 L 500 105"
                stroke="#f59e0b"
                strokeWidth="2"
                fill="none"
                className="drop-shadow-sm"
              />
            </svg>

            <div className="absolute right-4 top-4 space-y-1 text-xs">
              <div className="flex items-center gap-2">
                <div className="h-0.5 w-3 bg-primary" />
                <span className="text-muted-foreground">
                  Course Completions
                </span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-0.5 w-3 bg-blue-500" />
                <span className="text-muted-foreground">Certifications</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-0.5 w-3 bg-yellow-500" />
                <span className="text-muted-foreground">Phishing Tests</span>
              </div>
            </div>

            <div className="absolute inset-x-0 bottom-2 flex justify-between px-12 text-xs text-muted-foreground">
              {usageReportData.monthlyProgress.map(data => (
                <span key={data.month}>{data.month}</span>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ClientReports;
