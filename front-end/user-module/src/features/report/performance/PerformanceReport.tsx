import {
  CheckCircle2,
  Download,
  FileText,
  Loader2,
  TrendingUp,
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
import Delivery from 'features/report/performance/Delivery';
import Format from 'features/report/performance/Format';
import Metrics from 'features/report/performance/Metrics';
import Scope from 'features/report/performance/Scope';

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

  const handleGenerate = () => {
    setIsGenerating(true);
    setTimeout(() => {
      setIsGenerating(false);
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

            <Scope
              reportScope={reportScope}
              setReportScope={setReportScope}
              selectedMSPs={selectedMSPs}
              setSelectedMSPs={setSelectedMSPs}
              timeRange={timeRange}
              setTimeRange={setTimeRange}
            />

            <Metrics
              selectedMetrics={selectedMetrics}
              setSelectedMetrics={setSelectedMetrics}
            />

            <Format />

            <Delivery />
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
