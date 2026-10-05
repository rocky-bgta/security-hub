import {
  CheckCircle2,
  Download,
  FileSpreadsheet,
  Loader2,
  Package,
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

const LicenseUsageReport = ({ open, onOpenChange }: IProps) => {
  const [reportType, setReportType] = useState<string>('summary');
  const [timeRange, setTimeRange] = useState<string>('30d');
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [isComplete, setIsComplete] = useState<boolean>(false);
  const [selectedPackages, setSelectedPackages] = useState<Array<string>>([
    'A-SAT',
    'A-Phish',
    'HR',
  ]);
  const [selectedMSPs, setSelectedMSPs] = useState<Array<string>>([]);

  const packageList = [
    'A-SAT',
    'A-Phish',
    'HR',
    'Banking',
    'Java',
    'C++',
    'Python',
    'Healthcare',
  ];
  const mspList = [
    'TechSecure Solutions',
    'CyberGuard Enterprise',
    'SecureNet Partners',
    'InfoShield Corp',
    'DataGuard Systems',
  ];

  const togglePackage = (pkg: string) => {
    if (selectedPackages.includes(pkg)) {
      setSelectedPackages(selectedPackages.filter(p => p !== pkg));
    } else {
      setSelectedPackages([...selectedPackages, pkg]);
    }
  };

  const toggleMSP = (msp: string) => {
    if (selectedMSPs.includes(msp)) {
      setSelectedMSPs(selectedMSPs.filter(m => m !== msp));
    } else {
      setSelectedMSPs([...selectedMSPs, msp]);
    }
  };

  const handleGenerate = () => {
    setIsGenerating(true);
    // Simulate report generation
    setTimeout(() => {
      setIsGenerating(false);
      setIsComplete(true);
    }, 2500);
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
            <Package className="size-5" />
            Export License Usage Report
          </DialogTitle>
          <DialogDescription>
            Generate detailed license usage and allocation reports across all
            MSPs and packages.
          </DialogDescription>
        </DialogHeader>

        {!isGenerating && !isComplete ? (
          <Tabs defaultValue="scope" className="w-full">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="scope">Scope</TabsTrigger>
              <TabsTrigger value="packages">Packages</TabsTrigger>
              <TabsTrigger value="details">Details</TabsTrigger>
              <TabsTrigger value="export">Export</TabsTrigger>
            </TabsList>

            <TabsContent value="scope" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Report Type</h3>
                  <RadioGroup
                    value={reportType}
                    onValueChange={setReportType}
                    className="flex flex-col space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="summary" id="summary" />
                      <Label htmlFor="summary">License Usage Summary</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="detailed" id="detailed" />
                      <Label htmlFor="detailed">Detailed Usage Report</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="allocation" id="allocation" />
                      <Label htmlFor="allocation">
                        License Allocation Report
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="utilization" id="utilization" />
                      <Label htmlFor="utilization">Utilization Analysis</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="forecast" id="forecast" />
                      <Label htmlFor="forecast">Usage Forecast</Label>
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
                  <h3 className="mb-2 text-sm font-medium">MSP Selection</h3>
                  <RadioGroup
                    defaultValue="all"
                    className="flex flex-col space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="all" id="allMSPs" />
                      <Label htmlFor="allMSPs">All MSPs</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="active" id="activeMSPs" />
                      <Label htmlFor="activeMSPs">Active MSPs Only</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="custom" id="customMSPs" />
                      <Label htmlFor="customMSPs">Custom Selection</Label>
                    </div>
                  </RadioGroup>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="packages" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Package Selection
                  </h3>
                  <div className="max-h-48 space-y-2 overflow-y-auto rounded-md border p-3">
                    {packageList.map(pkg => (
                      <div key={pkg} className="flex items-center space-x-2">
                        <Checkbox
                          id={pkg}
                          checked={selectedPackages.includes(pkg)}
                          onCheckedChange={() => togglePackage(pkg)}
                        />
                        <Label htmlFor={pkg} className="text-sm">
                          {pkg}
                        </Label>
                      </div>
                    ))}
                  </div>
                  <div className="mt-2 flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setSelectedPackages(packageList)}
                    >
                      Select All
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setSelectedPackages([])}
                    >
                      Clear All
                    </Button>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">License Types</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="active" defaultChecked />
                      <Label htmlFor="active">Active Licenses</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="expired" />
                      <Label htmlFor="expired">Expired Licenses</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="pending" defaultChecked />
                      <Label htmlFor="pending">Pending Licenses</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="suspended" />
                      <Label htmlFor="suspended">Suspended Licenses</Label>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Usage Metrics</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="allocated" defaultChecked />
                      <Label htmlFor="allocated">Allocated vs Available</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="utilized" defaultChecked />
                      <Label htmlFor="utilized">Utilization Rates</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="trends" defaultChecked />
                      <Label htmlFor="trends">Usage Trends</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="efficiency" />
                      <Label htmlFor="efficiency">Efficiency Metrics</Label>
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="details" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Report Details</h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="breakdown">Include MSP breakdown</Label>
                      <Switch id="breakdown" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="clientLevel">Client-level details</Label>
                      <Switch id="clientLevel" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="userLevel">User-level details</Label>
                      <Switch id="userLevel" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="historical">Historical comparison</Label>
                      <Switch id="historical" defaultChecked />
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Analysis Options</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="wastage" defaultChecked />
                      <Label htmlFor="wastage">License Wastage Analysis</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="optimization" />
                      <Label htmlFor="optimization">
                        Optimization Recommendations
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="cost" defaultChecked />
                      <Label htmlFor="cost">Cost Analysis</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="compliance" />
                      <Label htmlFor="compliance">Compliance Status</Label>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Grouping Options</h3>
                  <Select defaultValue="msp">
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Group by" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="msp">Group by MSP</SelectItem>
                      <SelectItem value="package">Group by Package</SelectItem>
                      <SelectItem value="region">Group by Region</SelectItem>
                      <SelectItem value="date">Group by Date</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="export" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Export Format</h3>
                  <RadioGroup
                    defaultValue="excel"
                    className="flex flex-col space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="excel" id="excel" />
                      <Label htmlFor="excel">Excel Workbook (.xlsx)</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="csv" id="csv" />
                      <Label htmlFor="csv">CSV Files (.csv)</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="pdf" id="pdf" />
                      <Label htmlFor="pdf">PDF Report (.pdf)</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="json" id="json" />
                      <Label htmlFor="json">JSON Data (.json)</Label>
                    </div>
                  </RadioGroup>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">File Options</h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="compress">Compress files (ZIP)</Label>
                      <Switch id="compress" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="password">Password protect</Label>
                      <Switch id="password" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="metadata">Include metadata</Label>
                      <Switch id="metadata" defaultChecked />
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">File Name</h3>
                  <Input
                    defaultValue={`License_Usage_Report_${new Date().toISOString().split('T')[0]}`}
                    className="w-full"
                  />
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Delivery</h3>
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
                      <Checkbox id="save" defaultChecked />
                      <Label htmlFor="save">Save to reports library</Label>
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
                Generating License Usage Report
              </h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Processing license data...
              </p>
              <div className="mt-4 space-y-1 text-xs text-muted-foreground">
                <p>• Collecting license allocation data</p>
                <p>• Calculating utilization rates</p>
                <p>• Analyzing usage patterns</p>
                <p>• Generating export file</p>
              </div>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center space-y-4 py-12">
            <CheckCircle2 className="size-12 text-green-500" />
            <div className="text-center">
              <h3 className="text-lg font-medium">
                License Usage Report Generated!
              </h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Your license usage analysis is ready
              </p>
              <div className="mt-4 space-y-1 text-xs">
                <p className="text-muted-foreground">Report includes:</p>
                <p>• {selectedPackages.length} package types</p>
                <p>• {reportType} analysis</p>
                <p>• {timeRange} time period</p>
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
                disabled={selectedPackages.length === 0}
              >
                <FileSpreadsheet className="mr-2 size-4" />
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
export default LicenseUsageReport;
