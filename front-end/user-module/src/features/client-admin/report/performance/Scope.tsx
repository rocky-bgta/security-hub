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
  reportScope: string;
  setReportScope: Dispatch<SetStateAction<string>>;
  selectedMSPs: Array<string>;
  setSelectedMSPs: Dispatch<SetStateAction<Array<string>>>;
  timeRange: string;
  setTimeRange: Dispatch<SetStateAction<string>>;
}

const Scope = ({
  reportScope,
  setReportScope,
  selectedMSPs,
  timeRange,
  setTimeRange,
  setSelectedMSPs,
}: IProps) => {
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

  return (
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
          <h3 className="mb-2 text-sm font-medium">Comparison Options</h3>
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
              <Label htmlFor="yearOverYear">Year-over-year comparison</Label>
              <Switch id="yearOverYear" />
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Scope;
