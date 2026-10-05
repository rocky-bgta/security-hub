import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import { Switch } from 'common/Switch';
import { TabsContent } from 'common/Tabs';

const Output = () => {
  return (
    <TabsContent value="output" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Report Format</h3>
          <RadioGroup
            defaultValue="comprehensive"
            className="flex flex-col space-y-2"
          >
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="comprehensive" id="comprehensiveFormat" />
              <Label htmlFor="comprehensiveFormat">
                Comprehensive Report (PDF + Excel)
              </Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="executive" id="executiveFormat" />
              <Label htmlFor="executiveFormat">Executive Summary (PDF)</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="dashboard" id="dashboardFormat" />
              <Label htmlFor="dashboardFormat">Interactive Dashboard</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="data" id="dataFormat" />
              <Label htmlFor="dataFormat">Data Export (Excel/CSV)</Label>
            </div>
          </RadioGroup>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Visualization Options</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="charts">Include charts and graphs</Label>
              <Switch id="charts" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="heatmaps">Include heatmaps</Label>
              <Switch id="heatmaps" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="trends">Include trend lines</Label>
              <Switch id="trends" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="forecasts">Include forecasts</Label>
              <Switch id="forecasts" />
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Report Name</h3>
          <Input
            defaultValue={`Revenue_Analytics_${new Date().toISOString().split('T')[0]}`}
            className="w-full"
          />
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Delivery Options</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="downloadNow" defaultChecked />
              <Label htmlFor="downloadNow">Download immediately</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="emailReport" />
              <Label htmlFor="emailReport">Email when ready</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="saveDashboard" defaultChecked />
              <Label htmlFor="saveDashboard">Save to dashboard</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="scheduleRecurring" />
              <Label htmlFor="scheduleRecurring">
                Schedule recurring reports
              </Label>
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Output;
