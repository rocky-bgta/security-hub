import { Checkbox } from 'common/Checkbox';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Switch } from 'common/Switch';
import { TabsContent } from 'common/Tabs';

const Breakdown = () => {
  return (
    <TabsContent value="breakdown" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Revenue Breakdown</h3>
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
              <Label htmlFor="byClient">Break down by client size</Label>
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
              <Label htmlFor="newVsExisting">New vs Existing MSPs</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="contractType" />
              <Label htmlFor="contractType">By contract type</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="industryVertical" />
              <Label htmlFor="industryVertical">By industry vertical</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="partnerTier" />
              <Label htmlFor="partnerTier">By partner tier</Label>
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Advanced Analytics</h3>
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
  );
};

export default Breakdown;
