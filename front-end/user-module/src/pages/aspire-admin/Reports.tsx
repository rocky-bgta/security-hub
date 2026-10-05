import { useState } from 'react';

import { Button } from 'common/Button';
import { Calendar as CalendarComponent } from 'common/Calendar';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Label } from 'common/Label';
import { Popover, PopoverContent, PopoverTrigger } from 'common/Popover';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { format } from 'date-fns';
import {
  BarChart3,
  Calendar,
  Download,
  FileText,
  Search,
  TrendingUp,
} from 'lucide-react';
import { DateRange } from 'react-day-picker';
import { cn } from 'utils/Helper';

const Reports = () => {
  const [reportType, setReportType] = useState('');
  const [selectedClient, setSelectedClient] = useState('');
  const [userStatus, setUserStatus] = useState('all');
  const [actionType, setActionType] = useState('all');
  const [dateRange, setDateRange] = useState<DateRange | undefined>();
  const [isGenerating, setIsGenerating] = useState(false);

  const handleGenerateReport = () => {
    if (!reportType) {
      //   toast({
      //     title: 'Error',
      //     description: 'Please select a report type.',
      //     variant: 'destructive',
      //   });
      return;
    }

    setIsGenerating(true);

    // Simulate report generation
    setTimeout(() => {
      setIsGenerating(false);
      //   toast({
      //     title: 'Report Generated',
      //     description: `${reportType} report has been generated successfully.`,
      //   });
    }, 2000);
  };

  const reportTypes = [
    {
      value: 'activity',
      label: 'Activity Report',
      description: 'User logins, status changes, and actions performed',
    },
    {
      value: 'engagement',
      label: 'Engagement Report',
      description: 'User interaction with system features and task completions',
    },
    {
      value: 'performance',
      label: 'Performance Report',
      description: 'User performance on KPIs and goal achievements',
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">
          Client User Reports
        </h1>
        <p className="text-muted-foreground">
          Generate comprehensive reports on Client User activity, engagement,
          and performance
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Report Configuration */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="size-5" />
                Report Configuration
              </CardTitle>
              <CardDescription>
                Configure your report parameters and filters
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Report Type Selection */}
              <div className="space-y-2">
                <Label htmlFor="report-type">Report Type *</Label>
                <Select value={reportType} onValueChange={setReportType}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select report type..." />
                  </SelectTrigger>
                  <SelectContent>
                    {reportTypes.map(type => (
                      <SelectItem key={type.value} value={type.value}>
                        <div>
                          <div className="font-medium">{type.label}</div>
                          <div className="text-sm text-muted-foreground">
                            {type.description}
                          </div>
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* Filters */}
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="client-filter">Client Admin</Label>
                  <div className="relative">
                    <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
                    <Select
                      value={selectedClient}
                      onValueChange={setSelectedClient}
                    >
                      <SelectTrigger className="pl-10">
                        <SelectValue placeholder="All Client Admins" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Client Admins</SelectItem>
                        <SelectItem value="acme-corp">
                          Acme Corporation
                        </SelectItem>
                        <SelectItem value="stellar-enterprises">
                          Stellar Enterprises
                        </SelectItem>
                        <SelectItem value="fusion-systems">
                          Fusion Systems
                        </SelectItem>
                        <SelectItem value="quantum-solutions">
                          Quantum Solutions
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="status-filter">User Status</Label>
                  <Select value={userStatus} onValueChange={setUserStatus}>
                    <SelectTrigger>
                      <SelectValue placeholder="All statuses" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Statuses</SelectItem>
                      <SelectItem value="active">Active</SelectItem>
                      <SelectItem value="inactive">Inactive</SelectItem>
                      <SelectItem value="suspended">Suspended</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="action-filter">Action Type</Label>
                  <Select value={actionType} onValueChange={setActionType}>
                    <SelectTrigger>
                      <SelectValue placeholder="All actions" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Actions</SelectItem>
                      <SelectItem value="login">Login</SelectItem>
                      <SelectItem value="profile-update">
                        Profile Update
                      </SelectItem>
                      <SelectItem value="task-completion">
                        Task Completion
                      </SelectItem>
                      <SelectItem value="feature-access">
                        Feature Access
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Date Range</Label>
                  <Popover>
                    <PopoverTrigger asChild>
                      <Button
                        variant="outline"
                        className={cn(
                          'w-full justify-start text-left font-normal',
                          !dateRange && 'text-muted-foreground',
                        )}
                      >
                        <Calendar className="mr-2 size-4" />
                        {dateRange?.from ? (
                          dateRange.to ? (
                            <>
                              {format(dateRange.from, 'LLL dd, y')} -{' '}
                              {format(dateRange.to, 'LLL dd, y')}
                            </>
                          ) : (
                            format(dateRange.from, 'LLL dd, y')
                          )
                        ) : (
                          <span>Select a date range</span>
                        )}
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="start">
                      <CalendarComponent
                        initialFocus
                        mode="range"
                        defaultMonth={dateRange?.from}
                        selected={dateRange}
                        onSelect={setDateRange}
                        numberOfMonths={2}
                        className={cn('pointer-events-auto p-3')}
                      />
                    </PopoverContent>
                  </Popover>
                </div>
              </div>

              {/* Generate Button */}
              <div className="flex justify-end space-x-4">
                <Button
                  variant="outline"
                  onClick={() => {
                    setReportType('');
                    setSelectedClient('');
                    setUserStatus('all');
                    setActionType('all');
                    setDateRange(undefined);
                  }}
                >
                  Clear Filters
                </Button>
                <Button onClick={handleGenerateReport} disabled={isGenerating}>
                  {isGenerating ? 'Generating...' : 'Generate Report'}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Report Summary */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <BarChart3 className="size-5" />
                Quick Stats
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-foreground">
                  Total Users
                </span>
                <span className="text-2xl font-bold text-muted-foreground">
                  1,247
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-foreground">
                  Active Users
                </span>
                <span className="text-2xl font-bold text-green-600">1,156</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-foreground">
                  Suspended
                </span>
                <span className="text-2xl font-bold text-red-600">23</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-foreground">
                  This Month
                </span>
                <div className="flex items-center gap-1">
                  <TrendingUp className="size-4 text-green-600" />
                  <span className="text-sm font-bold text-green-600">+12%</span>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Export Options</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <Button variant="outline" className="w-full justify-start">
                <Download className="mr-2 size-4" />
                Export as CSV
              </Button>
              <Button variant="outline" className="w-full justify-start">
                <Download className="mr-2 size-4" />
                Export as Excel
              </Button>
              <Button variant="outline" className="w-full justify-start">
                <Download className="mr-2 size-4" />
                Export as PDF
              </Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Schedule Reports</CardTitle>
            </CardHeader>
            <CardContent>
              <Button variant="outline" className="w-full">
                <Calendar className="mr-2 size-4" />
                Set up Automated Reports
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Reports;
