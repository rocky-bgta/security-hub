import { BarChart, Calendar } from 'lucide-react';
import { useState } from 'react';

import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

const reportData = {
  totalLogins: 1250,
  totalEnrollments: 456,
  totalCertifications: 285,
  avgTimeSpent: '2.5 hours',
};

const UserActivityReport = () => {
  const [exportFormat, setExportFormat] = useState<string>('csv');
  const [dateRange, setDateRange] = useState<string>('30');

  const handleGenerateReport = () => {
    // toast({
    //   title: 'Report Generated',
    //   description: `${reportTypes.find(r => r.id === selectedReport)?.name} has been generated successfully.`,
    // });
  };

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Report Management
          </h1>
          <p className="text-muted-foreground">
            Generate and export detailed reports
          </p>
        </div>
        <div className="flex gap-2">
          <Select value={dateRange} onValueChange={setDateRange}>
            <SelectTrigger>
              <Calendar className="mr-2 size-4" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">Last 7 Days</SelectItem>
              <SelectItem value="30">Last 30 Days</SelectItem>
              <SelectItem value="90">Last 3 Months</SelectItem>
              <SelectItem value="365">Last Year</SelectItem>
            </SelectContent>
          </Select>

          <Select value={exportFormat} onValueChange={setExportFormat}>
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="csv">CSV</SelectItem>
              <SelectItem value="pdf">PDF</SelectItem>
              <SelectItem value="excel">Excel</SelectItem>
            </SelectContent>
          </Select>

          <Button onClick={handleGenerateReport}>
            <BarChart className="mr-2 size-4" />
            Generate & Export Report
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>User Activity Report</CardTitle>
          <CardDescription>
            User engagement and training progress
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-6">
            <div className="grid gap-4 md:grid-cols-4">
              <Card>
                <CardContent className="p-4">
                  <div className="text-2xl font-bold">
                    {reportData.totalLogins}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Total Logins
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-4">
                  <div className="text-2xl font-bold">
                    {reportData.totalEnrollments}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Total Enrollments
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-4">
                  <div className="text-2xl font-bold">
                    {reportData.totalCertifications}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Total Certifications
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-4">
                  <div className="text-2xl font-bold">
                    {reportData.avgTimeSpent}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Average Time Spent
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default UserActivityReport;
