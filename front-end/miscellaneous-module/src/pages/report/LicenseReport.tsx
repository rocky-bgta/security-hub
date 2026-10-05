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
import { Progress } from 'common/Progress';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

const reportData = {
  totalLicenses: 324,
  assignedLicenses: 124,
  availableLicenses: 200,
  expiredLicenses: 24,
  breakdown: [
    { category: 'Premium', total: 150, assigned: 89, available: 61 },
    { category: 'Basic', total: 124, assigned: 35, available: 89 },
    { category: 'Enterprise', total: 50, assigned: 0, available: 50 },
  ],
};

const LicenseReport = () => {
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
          <CardTitle>License Report</CardTitle>
          <CardDescription>
            License allocation, usage, and availability
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-6">
            <div className="grid gap-4 md:grid-cols-4">
              <Card>
                <CardContent className="p-4">
                  <div className="text-2xl font-bold">
                    {reportData.totalLicenses}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Total Licenses
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-4">
                  <div className="text-2xl font-bold text-yellow-600">
                    {reportData.assignedLicenses}
                  </div>
                  <div className="text-sm text-muted-foreground">Assigned</div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-4">
                  <div className="text-2xl font-bold text-green-600">
                    {reportData.availableLicenses}
                  </div>
                  <div className="text-sm text-muted-foreground">Available</div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-4">
                  <div className="text-2xl font-bold text-red-600">
                    {reportData.expiredLicenses}
                  </div>
                  <div className="text-sm text-muted-foreground">Expired</div>
                </CardContent>
              </Card>
            </div>
            <Card>
              <CardHeader>
                <CardTitle>License Breakdown by Category</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {reportData.breakdown.map((item, index) => (
                    <div
                      key={index}
                      className="flex items-center justify-between"
                    >
                      <div className="flex-1">
                        <div className="font-medium">{item.category}</div>
                        <div className="text-sm text-muted-foreground">
                          {item.assigned}/{item.total} assigned
                        </div>
                      </div>
                      <div className="w-32">
                        <Progress value={(item.assigned / item.total) * 100} />
                      </div>
                      <div className="ml-4 text-sm">
                        {Math.round((item.assigned / item.total) * 100)}%
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default LicenseReport;
