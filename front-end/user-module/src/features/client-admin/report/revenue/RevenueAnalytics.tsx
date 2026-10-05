import {
  CheckCircle2,
  DollarSign,
  Download,
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
import Analysis from 'features/report/revenue/Analysis';
import Breakdown from 'features/report/revenue/Breakdown';
import Metrics from 'features/report/revenue/Metrics';
import Output from 'features/report/revenue/Output';

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

            <Analysis
              analysisType={analysisType}
              setAnalysisType={setAnalysisType}
              timeRange={timeRange}
              setTimeRange={setTimeRange}
            />

            <Metrics
              selectedMetrics={selectedMetrics}
              setSelectedMetrics={setSelectedMetrics}
            />

            <Breakdown />

            <Output />
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
