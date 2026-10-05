import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
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
import { Textarea } from 'common/Textarea';

const Delivery = () => {
  return (
    <TabsContent value="delivery" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">File Name</h3>
          <Input
            defaultValue={`MSP_Analytics_${new Date().toISOString().split('T')[0]}`}
            className="w-full"
          />
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Delivery Method</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="downloadNow" defaultChecked />
              <Label htmlFor="downloadNow">Download immediately</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="emailWhenReady" />
              <Label htmlFor="emailWhenReady">Email when ready</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="saveToLibrary" defaultChecked />
              <Label htmlFor="saveToLibrary">Save to reports library</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="shareLink" />
              <Label htmlFor="shareLink">Generate shareable link</Label>
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Security Options</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="passwordProtect">Password protect file</Label>
              <Switch id="passwordProtect" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="setExpiry">Set expiry date</Label>
              <Switch id="setExpiry" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="trackDownloads">Track downloads</Label>
              <Switch id="trackDownloads" />
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Notification Recipients</h3>
          <Textarea
            placeholder="Enter email addresses separated by commas (optional)..."
            className="h-20 w-full resize-none"
          />
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Schedule Export</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="scheduleRecurring" />
              <Label htmlFor="scheduleRecurring">
                Schedule recurring exports
              </Label>
            </div>
          </div>
          <Select defaultValue="none">
            <SelectTrigger className="mt-2 w-full">
              <SelectValue placeholder="Select frequency" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="none">One-time export</SelectItem>
              <SelectItem value="daily">Daily</SelectItem>
              <SelectItem value="weekly">Weekly</SelectItem>
              <SelectItem value="monthly">Monthly</SelectItem>
              <SelectItem value="quarterly">Quarterly</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
    </TabsContent>
  );
};

export default Delivery;
