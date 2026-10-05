import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import { Switch } from 'common/Switch';
import { TabsContent } from 'common/Tabs';

const Export = () => {
  return (
    <TabsContent value="export" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Export Format</h3>
          <RadioGroup defaultValue="excel" className="flex flex-col space-y-2">
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="excel" id="excel" />
              <Label htmlFor="excel">Excel Workbook (.xlsx)</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="csv" id="csv" />
              <Label htmlFor="csv">CSV Files (.csv)</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="pdf" id="pdf" />
              <Label htmlFor="pdf">PDF Report (.pdf)</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="json" id="json" />
              <Label htmlFor="json">JSON Data (.json)</Label>
            </div>
          </RadioGroup>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">File Options</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="compress">Compress files (ZIP)</Label>
              <Switch id="compress" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="password">Password protect</Label>
              <Switch id="password" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="metadata">Include metadata</Label>
              <Switch id="metadata" defaultChecked />
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">File Name</h3>
          <Input
            defaultValue={`License_Usage_Report_${new Date().toISOString().split('T')[0]}`}
            className="w-full"
          />
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Delivery</h3>
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
              <Checkbox id="save" defaultChecked />
              <Label htmlFor="save">Save to reports library</Label>
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Export;
