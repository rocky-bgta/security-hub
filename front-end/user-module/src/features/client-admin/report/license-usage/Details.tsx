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

const Details = () => {
  return (
    <TabsContent value="details" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Report Details</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="breakdown">Include MSP breakdown</Label>
              <Switch id="breakdown" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="clientLevel">Client-level details</Label>
              <Switch id="clientLevel" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="userLevel">User-level details</Label>
              <Switch id="userLevel" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="historical">Historical comparison</Label>
              <Switch id="historical" defaultChecked />
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Analysis Options</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="wastage" defaultChecked />
              <Label htmlFor="wastage">License Wastage Analysis</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="optimization" />
              <Label htmlFor="optimization">Optimization Recommendations</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="cost" defaultChecked />
              <Label htmlFor="cost">Cost Analysis</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="compliance" />
              <Label htmlFor="compliance">Compliance Status</Label>
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Grouping Options</h3>
          <Select defaultValue="msp">
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Group by" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="msp">Group by MSP</SelectItem>
              <SelectItem value="package">Group by Package</SelectItem>
              <SelectItem value="region">Group by Region</SelectItem>
              <SelectItem value="date">Group by Date</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
    </TabsContent>
  );
};

export default Details;
