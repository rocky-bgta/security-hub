import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Switch } from 'common/Switch';
import { TabsContent } from 'common/Tabs';

const Delivery = () => {
  return (
    <TabsContent value="delivery" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Report Name</h3>
          <Input
            defaultValue={`MSP_Performance_Report_${new Date().toISOString().split('T')[0]}`}
            className="w-full"
          />
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Delivery Options</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="download" defaultChecked />
              <Label htmlFor="download">Download immediately</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="email" />
              <Label htmlFor="email">Email when ready</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="dashboard" defaultChecked />
              <Label htmlFor="dashboard">Save to dashboard</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="schedule" />
              <Label htmlFor="schedule">Schedule recurring generation</Label>
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Security & Access</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="password">Password protect</Label>
              <Switch id="password" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="watermark">Add watermark</Label>
              <Switch id="watermark" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="expiry">Set expiry date</Label>
              <Switch id="expiry" />
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Delivery;
