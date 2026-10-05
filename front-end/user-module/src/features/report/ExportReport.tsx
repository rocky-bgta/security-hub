import {
  CheckCircle2,
  Download,
  FileImage,
  FileSpreadsheet,
  FileText,
  Loader2,
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
import { Textarea } from 'common/Textarea';

interface IProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const ExportReport = ({ open, onOpenChange }: IProps) => {
  const [exportFormat, setExportFormat] = useState<string>('pdf');
  const [timeRange, setTimeRange] = useState<string>('30d');
  const [isExporting, setIsExporting] = useState<boolean>(false);
  const [isComplete, setIsComplete] = useState<boolean>(false);
  const [selectedSections, setSelectedSections] = useState<Array<string>>([
    'summary',
    'topPerformers',
    'packageDistribution',
    'monthlyGrowth',
  ]);

  const availableSections = [
    {
      id: 'summary',
      label: 'Summary Statistics',
      description: 'Total MSPs, revenue, licenses',
    },
    {
      id: 'topPerformers',
      label: 'Top Performing MSPs',
      description: 'Best performing MSP partners',
    },
    {
      id: 'packageDistribution',
      label: 'Package Distribution',
      description: 'Package adoption across MSPs',
    },
    {
      id: 'monthlyGrowth',
      label: 'Monthly Growth Trends',
      description: 'Growth charts and trends',
    },
    {
      id: 'revenueAnalysis',
      label: 'Revenue Analysis',
      description: 'Detailed revenue breakdown',
    },
    {
      id: 'licenseUtilization',
      label: 'License Utilization',
      description: 'License usage statistics',
    },
    {
      id: 'clientMetrics',
      label: 'Client Metrics',
      description: 'Client-related analytics',
    },
    {
      id: 'performanceMetrics',
      label: 'Performance Metrics',
      description: 'KPIs and performance indicators',
    },
  ];

  const toggleSection = (sectionId: string) => {
    if (selectedSections.includes(sectionId)) {
      setSelectedSections(selectedSections.filter(s => s !== sectionId));
    } else {
      setSelectedSections([...selectedSections, sectionId]);
    }
  };

  const handleExport = () => {
    setIsExporting(true);
    // Simulate export process
    setTimeout(() => {
      setIsExporting(false);
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
        setIsExporting(false);
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
            <Download className="size-5" />
            Export MSP Reports & Analytics
          </DialogTitle>
          <DialogDescription>
            Export comprehensive MSP analytics and performance data in your
            preferred format.
          </DialogDescription>
        </DialogHeader>

        {!isExporting && !isComplete ? (
          <Tabs defaultValue="content" className="w-full">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="content">Content</TabsTrigger>
              <TabsTrigger value="format">Format</TabsTrigger>
              <TabsTrigger value="options">Options</TabsTrigger>
              <TabsTrigger value="delivery">Delivery</TabsTrigger>
            </TabsList>

            <TabsContent value="content" className="mt-4 space-y-4">
              <div className="space-y-4">
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
                  <h3 className="mb-2 text-sm font-medium">Report Sections</h3>
                  <div className="max-h-64 space-y-3 overflow-y-auto rounded-md border p-3">
                    {availableSections.map(section => (
                      <div
                        key={section.id}
                        className="flex items-start space-x-3"
                      >
                        <Checkbox
                          id={section.id}
                          checked={selectedSections.includes(section.id)}
                          onCheckedChange={() => toggleSection(section.id)}
                          className="mt-1"
                        />
                        <div className="flex-1">
                          <Label
                            htmlFor={section.id}
                            className="cursor-pointer text-sm font-medium"
                          >
                            {section.label}
                          </Label>
                          <p className="mt-1 text-xs text-muted-foreground">
                            {section.description}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                  <div className="mt-2 flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() =>
                        setSelectedSections(availableSections.map(s => s.id))
                      }
                    >
                      Select All
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setSelectedSections([])}
                    >
                      Clear All
                    </Button>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Data Filters</h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="activeMSPs">Active MSPs only</Label>
                      <Switch id="activeMSPs" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="topPerformersOnly">
                        Top performers only
                      </Label>
                      <Switch id="topPerformersOnly" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="includeCharts">
                        Include charts and graphs
                      </Label>
                      <Switch id="includeCharts" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="includeRawData">
                        Include raw data tables
                      </Label>
                      <Switch id="includeRawData" />
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="format" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">Export Format</h3>
                  <RadioGroup
                    value={exportFormat}
                    onValueChange={setExportFormat}
                    className="flex flex-col space-y-3"
                  >
                    <div className="flex items-center space-x-2 rounded-lg border p-3">
                      <RadioGroupItem value="pdf" id="pdf" />
                      <div className="flex items-center space-x-3">
                        <FileText className="size-5 text-red-500" />
                        <div>
                          <Label
                            htmlFor="pdf"
                            className="cursor-pointer font-medium"
                          >
                            PDF Report
                          </Label>
                          <p className="text-xs text-muted-foreground">
                            Professional formatted report with charts
                          </p>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2 rounded-lg border p-3">
                      <RadioGroupItem value="excel" id="excel" />
                      <div className="flex items-center space-x-3">
                        <FileSpreadsheet className="size-5 text-green-500" />
                        <div>
                          <Label
                            htmlFor="excel"
                            className="cursor-pointer font-medium"
                          >
                            Excel Workbook
                          </Label>
                          <p className="text-xs text-muted-foreground">
                            Multiple sheets with data and charts
                          </p>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2 rounded-lg border p-3">
                      <RadioGroupItem value="csv" id="csv" />
                      <div className="flex items-center space-x-3">
                        <FileSpreadsheet className="size-5 text-blue-500" />
                        <div>
                          <Label
                            htmlFor="csv"
                            className="cursor-pointer font-medium"
                          >
                            CSV Data Export
                          </Label>
                          <p className="text-xs text-muted-foreground">
                            Raw data in comma-separated format
                          </p>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2 rounded-lg border p-3">
                      <RadioGroupItem value="powerpoint" id="powerpoint" />
                      <div className="flex items-center space-x-3">
                        <FileImage className="size-5 text-orange-500" />
                        <div>
                          <Label
                            htmlFor="powerpoint"
                            className="cursor-pointer font-medium"
                          >
                            PowerPoint Presentation
                          </Label>
                          <p className="text-xs text-muted-foreground">
                            Executive presentation format
                          </p>
                        </div>
                      </div>
                    </div>
                  </RadioGroup>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Page Layout (PDF/PowerPoint)
                  </h3>
                  <Select defaultValue="portrait">
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select orientation" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="portrait">Portrait</SelectItem>
                      <SelectItem value="landscape">Landscape</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Chart Quality</h3>
                  <Select defaultValue="high">
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select quality" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="standard">
                        Standard (Faster)
                      </SelectItem>
                      <SelectItem value="high">High Quality</SelectItem>
                      <SelectItem value="print">
                        Print Quality (Slower)
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="options" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Report Customization
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="includeLogo">Include company logo</Label>
                      <Switch id="includeLogo" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="includeTimestamp">
                        Include generation timestamp
                      </Label>
                      <Switch id="includeTimestamp" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="includeFooter">Include page footer</Label>
                      <Switch id="includeFooter" defaultChecked />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="includeWatermark">
                        Add confidential watermark
                      </Label>
                      <Switch id="includeWatermark" />
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Report Title</h3>
                  <Input
                    defaultValue={`MSP Analytics Report - ${new Date().toLocaleDateString()}`}
                    className="w-full"
                  />
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Executive Summary
                  </h3>
                  <Textarea
                    placeholder="Add a custom executive summary or key insights to include in the report..."
                    className="h-24 w-full resize-none"
                  />
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Color Theme</h3>
                  <Select defaultValue="default">
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select color theme" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="default">Default (Blue)</SelectItem>
                      <SelectItem value="corporate">
                        Corporate (Gray)
                      </SelectItem>
                      <SelectItem value="professional">
                        Professional (Navy)
                      </SelectItem>
                      <SelectItem value="modern">Modern (Teal)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Language</h3>
                  <Select defaultValue="en">
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select language" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="en">English</SelectItem>
                      <SelectItem value="es">Spanish</SelectItem>
                      <SelectItem value="fr">French</SelectItem>
                      <SelectItem value="de">German</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="delivery" className="mt-4 space-y-4">
              <div className="space-y-4">
                <div>
                  <h3 className="mb-2 text-sm font-medium">File Name</h3>
                  <Input
                    defaultValue={`MSP_Analytics_${new Date().toISOString().split('T')[0]}`}
                    className="w-full"
                  />
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Delivery Method</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="downloadNow" defaultChecked />
                      <Label htmlFor="downloadNow">Download immediately</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="emailWhenReady" />
                      <Label htmlFor="emailWhenReady">Email when ready</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="saveToLibrary" defaultChecked />
                      <Label htmlFor="saveToLibrary">
                        Save to reports library
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Checkbox id="shareLink" />
                      <Label htmlFor="shareLink">Generate shareable link</Label>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Security Options</h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="passwordProtect">
                        Password protect file
                      </Label>
                      <Switch id="passwordProtect" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="setExpiry">Set expiry date</Label>
                      <Switch id="setExpiry" />
                    </div>
                    <div className="flex items-center justify-between">
                      <Label htmlFor="trackDownloads">Track downloads</Label>
                      <Switch id="trackDownloads" />
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">
                    Notification Recipients
                  </h3>
                  <Textarea
                    placeholder="Enter email addresses separated by commas (optional)..."
                    className="h-20 w-full resize-none"
                  />
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-medium">Schedule Export</h3>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox id="scheduleRecurring" />
                      <Label htmlFor="scheduleRecurring">
                        Schedule recurring exports
                      </Label>
                    </div>
                  </div>
                  <Select defaultValue="none">
                    <SelectTrigger className="mt-2 w-full">
                      <SelectValue placeholder="Select frequency" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">One-time export</SelectItem>
                      <SelectItem value="daily">Daily</SelectItem>
                      <SelectItem value="weekly">Weekly</SelectItem>
                      <SelectItem value="monthly">Monthly</SelectItem>
                      <SelectItem value="quarterly">Quarterly</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        ) : isExporting ? (
          <div className="flex flex-col items-center justify-center space-y-4 py-12">
            <Loader2 className="size-12 animate-spin text-primary" />
            <div className="text-center">
              <h3 className="text-lg font-medium">
                Exporting MSP Analytics Report
              </h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Generating {exportFormat.toUpperCase()} report with selected
                data...
              </p>
              <div className="mt-4 space-y-1 text-xs text-muted-foreground">
                <p>• Collecting analytics data</p>
                <p>• Generating charts and visualizations</p>
                <p>• Formatting report layout</p>
                <p>• Preparing download file</p>
              </div>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center space-y-4 py-12">
            <CheckCircle2 className="size-12 text-green-500" />
            <div className="text-center">
              <h3 className="text-lg font-medium">Export Complete!</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Your MSP analytics report is ready for download
              </p>
              <div className="mt-4 space-y-1 text-xs">
                <p className="text-muted-foreground">Report details:</p>
                <p>• Format: {exportFormat.toUpperCase()}</p>
                <p>• Sections: {selectedSections.length} included</p>
                <p>• Time range: {timeRange}</p>
                <p>• Generated: {new Date().toLocaleString()}</p>
              </div>
            </div>
          </div>
        )}

        <DialogFooter>
          {!isExporting && !isComplete ? (
            <>
              <Button variant="outline" onClick={() => onOpenChange(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleExport}
                disabled={selectedSections.length === 0}
              >
                <Download className="mr-2 size-4" />
                Export Report
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

export default ExportReport;
