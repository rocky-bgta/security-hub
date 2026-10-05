import {
  CheckCircle2,
  DollarSign,
  Download,
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

const RevenueAnalytics = ({ open, onOpenChange }: IProps) => {
  const [analysisType, setAnalysisType] = useState<string>('comprehensive');
  const [timeRange, setTimeRange] = useState<string>('1y');
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [isComplete, setIsComplete] = useState<boolean>(false);
  const [selectedMetrics, setSelectedMetrics] = useState<Array<string>>([
    'totalRevenue',
    'revenueGrowth',
    'mspContribution',
    'packageRevenue',
    'profitMargins',
  ]);

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
    }, 3500);
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
            <DollarSign className="size-5" />
            Revenue Analytics Report
          </DialogTitle>
          <DialogDescription>
            Generate comprehensive revenue analysis and financial insights
            across all MSP partnerships.
          </DialogDescription>
        </DialogHeader>

        {!isGenerating && !isComplete ? (
          <Tabs defaultValue="analysis" className="w-full">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="analysis">Analysis</TabsTrigger>
              <TabsTrigger value="metrics">Metrics</TabsTrigger>
              <TabsTrigger value="breakdown">Breakdown</TabsTrigger>
              <TabsTrigger value="output">Output</TabsTrigger>
            </TabsList>

            <TabsContent value="analysis" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Analysis Type</h3>
                  <RadioGroup
                    value={analysisType}
                    onValueChange={setAnalysisType}
                    className="flex flex-col space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        value="comprehensive"
                        id="comprehensive"
                      />
                      <Label htmlFor="comprehensive">
                        Comprehensive Revenue Analysis
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="growth" id="growth" />
                      <Label htmlFor="growth">Growth Analysis</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        value="profitability"
                        id="profitability"
                      />
                      <Label htmlFor="profitability">
                        Profitability Analysis
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="forecasting" id="forecasting" />
                      <Label htmlFor="forecasting">Revenue Forecasting</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="comparison" id="comparison" />
                      <Label htmlFor="comparison">Comparative Analysis</Label>
                    </div>
                  </RadioGroup>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Time Range</h3>
                  <Select value={timeRange} onValueChange={setTimeRange}>
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select time range" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="3m">Last 3 months</SelectItem>
                      <SelectItem value="6m">Last 6 months</SelectItem>
                      <SelectItem value="1y">Last year</SelectItem>
                      <SelectItem value="2y">Last 2 years</SelectItem>
                      <SelectItem value="3y">Last 3 years</SelectItem>
                      <SelectItem value="custom">Custom range</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Analysis Scope</h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="allMSPs">Include all MSPs</Label>
                      <Switch id="allMSPs" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="allPackages">Include all packages</Label>
                      <Switch id="allPackages" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="recurring">
                        Include recurring revenue
                      </Label>
                      <Switch id="recurring" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="oneTime">Include one-time revenue</Label>
                      <Switch id="oneTime" defaultChecked />
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Comparison Options
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="previousPeriod" defaultChecked />
                      <Label htmlFor="previousPeriod">
                        Compare with previous period
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="yearOverYear" defaultChecked />
                      <Label htmlFor="yearOverYear">
                        Year-over-year comparison
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="industryBenchmark" />
                      <Label htmlFor="industryBenchmark">
                        Industry benchmarks
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="targets" />
                      <Label htmlFor="targets">Compare against targets</Label>
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="metrics" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Revenue Metrics</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="totalRevenue"
                        checked={selectedMetrics.includes('totalRevenue')}
                        onCheckedChange={() => toggleMetric('totalRevenue')}
                      />
                      <Label htmlFor="totalRevenue">Total Revenue</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="revenueGrowth"
                        checked={selectedMetrics.includes('revenueGrowth')}
                        onCheckedChange={() => toggleMetric('revenueGrowth')}
                      />
                      <Label htmlFor="revenueGrowth">Revenue Growth Rate</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="recurringRevenue"
                        checked={selectedMetrics.includes('recurringRevenue')}
                        onCheckedChange={() => toggleMetric('recurringRevenue')}
                      />
                      <Label htmlFor="recurringRevenue">
                        Monthly Recurring Revenue (MRR)
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="annualRevenue"
                        checked={selectedMetrics.includes('annualRevenue')}
                        onCheckedChange={() => toggleMetric('annualRevenue')}
                      />
                      <Label htmlFor="annualRevenue">
                        Annual Recurring Revenue (ARR)
                      </Label>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">MSP Performance</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="mspContribution"
                        checked={selectedMetrics.includes('mspContribution')}
                        onCheckedChange={() => toggleMetric('mspContribution')}
                      />
                      <Label htmlFor="mspContribution">
                        MSP Revenue Contribution
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="topPerformers"
                        checked={selectedMetrics.includes('topPerformers')}
                        onCheckedChange={() => toggleMetric('topPerformers')}
                      />
                      <Label htmlFor="topPerformers">
                        Top Revenue Performers
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="commissions"
                        checked={selectedMetrics.includes('commissions')}
                        onCheckedChange={() => toggleMetric('commissions')}
                      />
                      <Label htmlFor="commissions">Commission Payments</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="churnImpact"
                        checked={selectedMetrics.includes('churnImpact')}
                        onCheckedChange={() => toggleMetric('churnImpact')}
                      />
                      <Label htmlFor="churnImpact">Churn Impact Analysis</Label>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Product Performance
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="packageRevenue"
                        checked={selectedMetrics.includes('packageRevenue')}
                        onCheckedChange={() => toggleMetric('packageRevenue')}
                      />
                      <Label htmlFor="packageRevenue">Revenue by Package</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="packageGrowth"
                        checked={selectedMetrics.includes('packageGrowth')}
                        onCheckedChange={() => toggleMetric('packageGrowth')}
                      />
                      <Label htmlFor="packageGrowth">
                        Package Growth Rates
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="crossSelling"
                        checked={selectedMetrics.includes('crossSelling')}
                        onCheckedChange={() => toggleMetric('crossSelling')}
                      />
                      <Label htmlFor="crossSelling">
                        Cross-selling Analysis
                      </Label>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Financial Health</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="profitMargins"
                        checked={selectedMetrics.includes('profitMargins')}
                        onCheckedChange={() => toggleMetric('profitMargins')}
                      />
                      <Label htmlFor="profitMargins">Profit Margins</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="costAnalysis"
                        checked={selectedMetrics.includes('costAnalysis')}
                        onCheckedChange={() => toggleMetric('costAnalysis')}
                      />
                      <Label htmlFor="costAnalysis">Cost Analysis</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="roi"
                        checked={selectedMetrics.includes('roi')}
                        onCheckedChange={() => toggleMetric('roi')}
                      />
                      <Label htmlFor="roi">Return on Investment (ROI)</Label>
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="breakdown" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Revenue Breakdown
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="byMSP">Break down by MSP</Label>
                      <Switch id="byMSP" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="byPackage">Break down by package</Label>
                      <Switch id="byPackage" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="byRegion">Break down by region</Label>
                      <Switch id="byRegion" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="byClient">
                        Break down by client size
                      </Label>
                      <Switch id="byClient" />
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Time Granularity</h3>
                  <Select defaultValue="monthly">
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select granularity" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="daily">Daily</SelectItem>
                      <SelectItem value="weekly">Weekly</SelectItem>
                      <SelectItem value="monthly">Monthly</SelectItem>
                      <SelectItem value="quarterly">Quarterly</SelectItem>
                      <SelectItem value="yearly">Yearly</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Segmentation</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="newVsExisting" defaultChecked />
                      <Label htmlFor="newVsExisting">
                        New vs Existing MSPs
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="contractType" />
                      <Label htmlFor="contractType">By contract type</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="industryVertical" />
                      <Label htmlFor="industryVertical">
                        By industry vertical
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="partnerTier" />
                      <Label htmlFor="partnerTier">By partner tier</Label>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Advanced Analytics
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="cohortAnalysis" />
                      <Label htmlFor="cohortAnalysis">Cohort Analysis</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="seasonality" />
                      <Label htmlFor="seasonality">Seasonality Analysis</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="predictive" />
                      <Label htmlFor="predictive">Predictive Modeling</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="anomaly" />
                      <Label htmlFor="anomaly">Anomaly Detection</Label>
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="output" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Report Format</h3>
                  <RadioGroup
                    defaultValue="comprehensive"
                    className="flex flex-col space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        value="comprehensive"
                        id="comprehensiveFormat"
                      />
                      <Label htmlFor="comprehensiveFormat">
                        Comprehensive Report (PDF + Excel)
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="executive" id="executiveFormat" />
                      <Label htmlFor="executiveFormat">
                        Executive Summary (PDF)
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="dashboard" id="dashboardFormat" />
                      <Label htmlFor="dashboardFormat">
                        Interactive Dashboard
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="data" id="dataFormat" />
                      <Label htmlFor="dataFormat">
                        Data Export (Excel/CSV)
                      </Label>
                    </div>
                  </RadioGroup>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Visualization Options
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="charts">Include charts and graphs</Label>
                      <Switch id="charts" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="heatmaps">Include heatmaps</Label>
                      <Switch id="heatmaps" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="trends">Include trend lines</Label>
                      <Switch id="trends" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="forecasts">Include forecasts</Label>
                      <Switch id="forecasts" />
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Report Name</h3>
                  <Input
                    defaultValue={`Revenue_Analytics_${new Date().toISOString().split('T')[0]}`}
                    className="w-full"
                  />
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Delivery Options</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="downloadNow" defaultChecked />
                      <Label htmlFor="downloadNow">Download immediately</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="emailReport" />
                      <Label htmlFor="emailReport">Email when ready</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="saveDashboard" defaultChecked />
                      <Label htmlFor="saveDashboard">Save to dashboard</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="scheduleRecurring" />
                      <Label htmlFor="scheduleRecurring">
                        Schedule recurring reports
                      </Label>
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
                Generating Revenue Analytics
              </h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Processing financial data and calculations...
              </p>
              <div className="mt-4 space-y-1 text-xs text-muted-foreground">
                <p>• Collecting revenue data</p>
                <p>• Calculating growth metrics</p>
                <p>• Analyzing profitability trends</p>
                <p>• Generating financial insights</p>
                <p>• Creating visualizations</p>
              </div>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center space-y-4 py-12">
            <CheckCircle2 className="size-12 text-green-500" />
            <div className="text-center">
              <h3 className="text-lg font-medium">
                Revenue Analytics Generated!
              </h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Your comprehensive revenue analysis is ready
              </p>
              <div className="mt-4 space-y-1 text-xs">
                <p className="text-muted-foreground">Report includes:</p>
                <p>• {analysisType} analysis</p>
                <p>• {selectedMetrics.length} revenue metrics</p>
                <p>• {timeRange} time period</p>
                <p>• Financial forecasting and insights</p>
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
                <TrendingUp className="mr-2 size-4" />
                Generate Analytics
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

export default RevenueAnalytics;
