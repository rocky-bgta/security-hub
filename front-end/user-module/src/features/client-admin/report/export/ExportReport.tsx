import { CheckCircle2, Download, Loader2 } from 'lucide-react';
import { useState } from 'react';

import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Tabs, TabsList, TabsTrigger } from 'common/Tabs';
import Content from 'features/report/export/Content';
import Delivery from 'features/report/export/Delivery';
import Format from 'features/report/export/Format';
import Options from 'features/report/export/Options';

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

  const handleExport = () => {
    setIsExporting(true);
    setTimeout(() => {
      setIsExporting(false);
      setIsComplete(true);
    }, 3000);
  };

  const handleDownload = () => {
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

            <Content
              selectedSections={selectedSections}
              setSelectedSections={setSelectedSections}
              timeRange={timeRange}
              setTimeRange={setTimeRange}
            />

            <Format
              exportFormat={exportFormat}
              setExportFormat={setExportFormat}
            />

            <Options />

            <Delivery />
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
