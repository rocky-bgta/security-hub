import { Dispatch, SetStateAction } from 'react';

import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { TabsContent } from 'common/Tabs';

interface IProps {
  reportType: string;
  setReportType: Dispatch<SetStateAction<string>>;
  selectedMSPs: Array<string>;
  setSelectedMSPs: Dispatch<SetStateAction<Array<string>>>;
  timeRange: string;
  setTimeRange: Dispatch<SetStateAction<string>>;
}

const Scope = ({
  reportType,
  setReportType,
  timeRange,
  setTimeRange,
}: IProps) => {
  return (
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
              <Label htmlFor="allocation">License Allocation Report</Label>
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
          <RadioGroup defaultValue="all" className="flex flex-col space-y-2">
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
  );
};

export default Scope;
