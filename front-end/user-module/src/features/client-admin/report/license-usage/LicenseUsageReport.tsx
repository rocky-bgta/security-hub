import {
  CheckCircle2,
  Download,
  FileSpreadsheet,
  Loader2,
  Package,
} from 'lucide-react';
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
import Details from 'features/report/license-usage/Details';
import Export from 'features/report/license-usage/Export';
import Packages from 'features/report/license-usage/Packages';
import Scope from 'features/report/license-usage/Scope';

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

  const handleGenerate = () => {
    setIsGenerating(true);
    setTimeout(() => {
      setIsGenerating(false);
      setIsComplete(true);
    }, 2500);
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

            <Scope
              reportType={reportType}
              setReportType={setReportType}
              selectedMSPs={selectedMSPs}
              setSelectedMSPs={setSelectedMSPs}
              timeRange={timeRange}
              setTimeRange={setTimeRange}
            />

            <Packages
              selectedPackages={selectedPackages}
              setSelectedPackages={setSelectedPackages}
            />

            <Details />

            <Export />
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
