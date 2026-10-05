import {
  CheckCircle2,
  Download,
  FileText,
  Loader2,
  TrendingUp,
} from 'lucide-react';
import { useState } from 'react';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Switch } from 'common/Switch';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tabs';

interface IProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const PerformanceReport = ({ open, onOpenChange }: IProps) => {
  const [reportScope, setReportScope] = useState<string>('all');
  const [timeRange, setTimeRange] = useState<string>('30d');
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [isComplete, setIsComplete] = useState<boolean>(false);
  const [selectedMSPs, setSelectedMSPs] = useState<Array<string>>([]);
  const [selectedMetrics, setSelectedMetrics] = useState<Array<string>>([
    'revenue',
    'clientGrowth',
    'satisfaction',
    'licenseUtilization',
    'trainingCompletion',
  ]);

  const mspList = [
    'TechSecure Solutions',
    'CyberGuard Enterprise',
    'SecureNet Partners',
    'InfoShield Corp',
    'DataGuard Systems',
    'CyberShield Pro',
  ];

  const toggleMSP = (msp: string) => {
    if (selectedMSPs.includes(msp)) {
      setSelectedMSPs(selectedMSPs.filter(m => m !== msp));
    } else {
      setSelectedMSPs([...selectedMSPs, msp]);
    }
  };

  const toggleMetric = (metric: string) => {
    if (selectedMetrics.includes(metric)) {
      setSelectedMetrics(selectedMetrics.filter(m => m !== metric));
    } else {
      setSelectedMetrics([...selectedMetrics, metric]);
    }
  };

  const handleGenerate = () => {
    setIsGenerating(true);
    // Simulate report generation
    setTimeout(() => {
      setIsGenerating(false);
      setIsComplete(true);
    }, 3000);
  };

  const handleDownload = () => {
    // In a real implementation, this would trigger the actual download
    setTimeout(() => {
      setIsComplete(false);
      onOpenChange(false);
    }, 500);
  };

  const resetState = () => {
    if (!open) {
      setTimeout(() => {
        setIsGenerating(false);
        setIsComplete(false);
      }, 300);
    }
  };

  return (
    <Dialog
      open={open}
      onOpenChange={newOpen => {
        onOpenChange(newOpen);
        resetState();
      }}
    >
      <DialogContent className="sm:max-w-[700px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <TrendingUp className="size-5" />
            Generate MSP Performance Report
          </DialogTitle>
          <DialogDescription>
            Create a comprehensive performance analysis report for your MSP
            partners.
          </DialogDescription>
        </DialogHeader>

        {!isGenerating && !isComplete ? (
          <Tabs defaultValue="scope" className="w-full">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="scope">Scope</TabsTrigger>
              <TabsTrigger value="metrics">Metrics</TabsTrigger>
              <TabsTrigger value="format">Format</TabsTrigger>
              <TabsTrigger value="delivery">Delivery</TabsTrigger>
            </TabsList>

            <TabsContent value="scope" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Report Scope</h3>
                  <RadioGroup
                    value={reportScope}
                    onValueChange={setReportScope}
                    className="flex flex-col space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="all" id="all" />
                      <Label htmlFor="all">All MSPs</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="top" id="top" />
                      <Label htmlFor="top">Top 10 Performers</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="bottom" id="bottom" />
                      <Label htmlFor="bottom">Bottom 10 Performers</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="custom" id="custom" />
                      <Label htmlFor="custom">Custom Selection</Label>
                    </div>
                  </RadioGroup>
                </div>

                {reportScope === 'custom' && (
                  <div>
                    <h3 className="mb-2 text-sm font-medium">Select MSPs</h3>
                    <div className="max-h-40 space-y-2 overflow-y-auto rounded-md border p-3">
                      {mspList.map(msp => (
                        <div key={msp} className="flex items-center space-x-2">
                          <Checkbox
                            id={msp}
                            checked={selectedMSPs.includes(msp)}
                            onCheckedChange={() => toggleMSP(msp)}
                          />
                          <Label htmlFor={msp} className="text-sm">
                            {msp}
                          </Label>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                <div>
                  <h3 className="mb-2 text-sm font-medium">Time Range</h3>
                  <Select value={timeRange} onValueChange={setTimeRange}>
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select time range" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="7d">Last 7 days</SelectItem>
                      <SelectItem value="30d">Last 30 days</SelectItem>
                      <SelectItem value="90d">Last 90 days</SelectItem>
                      <SelectItem value="6m">Last 6 months</SelectItem>
                      <SelectItem value="1y">Last year</SelectItem>
                      <SelectItem value="custom">Custom range</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Comparison Options
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="previousPeriod">
                        Compare with previous period
                      </Label>
                      <Switch id="previousPeriod" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="industryBenchmark">
                        Include industry benchmarks
                      </Label>
                      <Switch id="industryBenchmark" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="yearOverYear">
                        Year-over-year comparison
                      </Label>
                      <Switch id="yearOverYear" />
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="metrics" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Performance Metrics
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="revenue"
                        checked={selectedMetrics.includes('revenue')}
                        onCheckedChange={() => toggleMetric('revenue')}
                      />
                      <Label htmlFor="revenue">Revenue Performance</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="clientGrowth"
                        checked={selectedMetrics.includes('clientGrowth')}
                        onCheckedChange={() => toggleMetric('clientGrowth')}
                      />
                      <Label htmlFor="clientGrowth">Client Growth Rate</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="satisfaction"
                        checked={selectedMetrics.includes('satisfaction')}
                        onCheckedChange={() => toggleMetric('satisfaction')}
                      />
                      <Label htmlFor="satisfaction">
                        Client Satisfaction Scores
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="licenseUtilization"
                        checked={selectedMetrics.includes('licenseUtilization')}
                        onCheckedChange={() =>
                          toggleMetric('licenseUtilization')
                        }
                      />
                      <Label htmlFor="licenseUtilization">
                        License Utilization
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="trainingCompletion"
                        checked={selectedMetrics.includes('trainingCompletion')}
                        onCheckedChange={() =>
                          toggleMetric('trainingCompletion')
                        }
                      />
                      <Label htmlFor="trainingCompletion">
                        Training Completion Rates
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="responseTime"
                        checked={selectedMetrics.includes('responseTime')}
                        onCheckedChange={() => toggleMetric('responseTime')}
                      />
                      <Label htmlFor="responseTime">Response Time</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="uptime"
                        checked={selectedMetrics.includes('uptime')}
                        onCheckedChange={() => toggleMetric('uptime')}
                      />
                      <Label htmlFor="uptime">System Uptime</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="riskAssessment"
                        checked={selectedMetrics.includes('riskAssessment')}
                        onCheckedChange={() => toggleMetric('riskAssessment')}
                      />
                      <Label htmlFor="riskAssessment">Risk Assessment</Label>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Additional Analysis
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="trends" defaultChecked />
                      <Label htmlFor="trends">Trend Analysis</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="forecasting" />
                      <Label htmlFor="forecasting">
                        Performance Forecasting
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="recommendations" defaultChecked />
                      <Label htmlFor="recommendations">
                        Improvement Recommendations
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="alerts" />
                      <Label htmlFor="alerts">Performance Alerts</Label>
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="format" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Report Format</h3>
                  <RadioGroup
                    defaultValue="pdf"
                    className="flex flex-col space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="pdf" id="pdf" />
                      <Label htmlFor="pdf">PDF Report</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="excel" id="excel" />
                      <Label htmlFor="excel">Excel Workbook</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="both" id="both" />
                      <Label htmlFor="both">Both PDF and Excel</Label>
                    </div>
                  </RadioGroup>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Report Style</h3>
                  <Select defaultValue="executive">
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select report style" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="executive">
                        Executive Summary
                      </SelectItem>
                      <SelectItem value="detailed">
                        Detailed Analysis
                      </SelectItem>
                      <SelectItem value="technical">
                        Technical Report
                      </SelectItem>
                      <SelectItem value="dashboard">Dashboard Style</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Visual Elements</h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="charts">Include Charts</Label>
                      <Switch id="charts" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="tables">Include Data Tables</Label>
                      <Switch id="tables" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="heatmaps">Include Heatmaps</Label>
                      <Switch id="heatmaps" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="infographics">Include Infographics</Label>
                      <Switch id="infographics" />
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="delivery" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Report Name</h3>
                  <Input
                    defaultValue={`MSP_Performance_Report_${new Date().toISOString().split('T')[0]}`}
                    className="w-full"
                  />
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Delivery Options</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="download" defaultChecked />
                      <Label htmlFor="download">Download immediately</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="email" />
                      <Label htmlFor="email">Email when ready</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="dashboard" defaultChecked />
                      <Label htmlFor="dashboard">Save to dashboard</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="schedule" />
                      <Label htmlFor="schedule">
                        Schedule recurring generation
                      </Label>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Security & Access
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="password">Password protect</Label>
                      <Switch id="password" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="watermark">Add watermark</Label>
                      <Switch id="watermark" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="expiry">Set expiry date</Label>
                      <Switch id="expiry" />
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        ) : isGenerating ? (
          <div className="flex flex-col items-center justify-center space-y-4 py-12">
            <Loader2 className="size-12 animate-spin text-primary" />
            <div className="text-center">
              <h3 className="text-lg font-medium">
                Generating Performance Report
              </h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Analyzing MSP performance data...
              </p>
              <div className="mt-4 space-y-1 text-xs text-muted-foreground">
                <p>• Collecting performance metrics</p>
                <p>• Calculating growth rates</p>
                <p>• Generating visualizations</p>
                <p>• Compiling final report</p>
              </div>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center space-y-4 py-12">
            <CheckCircle2 className="size-12 text-green-500" />
            <div className="text-center">
              <h3 className="text-lg font-medium">
                Performance Report Generated!
              </h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Your MSP performance analysis is ready
              </p>
              <div className="mt-4 space-y-1 text-xs">
                <p className="text-muted-foreground">Report includes:</p>
                <p>
                  •{' '}
                  {reportScope === 'all'
                    ? 'All MSPs'
                    : reportScope === 'custom'
                      ? `${selectedMSPs.length} selected MSPs`
                      : `${reportScope} performers`}
                </p>
                <p>• {selectedMetrics.length} performance metrics</p>
                <p>• {timeRange} time period analysis</p>
              </div>
            </div>
          </div>
        )}

        <DialogFooter>
          {!isGenerating && !isComplete ? (
            <>
              <Button variant="outline" onClick={() => onOpenChange(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleGenerate}
                disabled={selectedMetrics.length === 0}
              >
                <FileText className="mr-2 size-4" />
                Generate Report
              </Button>
            </>
          ) : isComplete ? (
            <Button onClick={handleDownload} className="w-full sm:w-auto">
              <Download className="mr-2 size-4" />
              Download Report
            </Button>
          ) : null}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default PerformanceReport;
