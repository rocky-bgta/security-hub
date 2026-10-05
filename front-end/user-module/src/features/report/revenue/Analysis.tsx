import { Dispatch, SetStateAction } from 'react';

import { Checkbox } from 'common/Checkbox';
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
import { TabsContent } from 'common/Tabs';

interface IProps {
  analysisType: string;
  setAnalysisType: Dispatch<SetStateAction<string>>;
  timeRange: string;
  setTimeRange: Dispatch<SetStateAction<string>>;
}

const Analysis = ({
  analysisType,
  setAnalysisType,
  timeRange,
  setTimeRange,
}: IProps) => {
  return (
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
              <RadioGroupItem value="comprehensive" id="comprehensive" />
              <Label htmlFor="comprehensive">
                Comprehensive Revenue Analysis
              </Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="growth" id="growth" />
              <Label htmlFor="growth">Growth Analysis</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="profitability" id="profitability" />
              <Label htmlFor="profitability">Profitability Analysis</Label>
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
              <Label htmlFor="recurring">Include recurring revenue</Label>
              <Switch id="recurring" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="oneTime">Include one-time revenue</Label>
              <Switch id="oneTime" defaultChecked />
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Comparison Options</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="previousPeriod" defaultChecked />
              <Label htmlFor="previousPeriod">
                Compare with previous period
              </Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="yearOverYear" defaultChecked />
              <Label htmlFor="yearOverYear">Year-over-year comparison</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="industryBenchmark" />
              <Label htmlFor="industryBenchmark">Industry benchmarks</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="targets" />
              <Label htmlFor="targets">Compare against targets</Label>
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Analysis;
