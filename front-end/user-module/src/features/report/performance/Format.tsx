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

const Format = () => {
  return (
    <TabsContent value="format" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Report Format</h3>
          <RadioGroup defaultValue="pdf" className="flex flex-col space-y-2">
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="pdf" id="pdf" />
              <Label htmlFor="pdf">PDF Report</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="excel" id="excel" />
              <Label htmlFor="excel">Excel Workbook</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="both" id="both" />
              <Label htmlFor="both">Both PDF and Excel</Label>
            </div>
          </RadioGroup>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Report Style</h3>
          <Select defaultValue="executive">
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select report style" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="executive">Executive Summary</SelectItem>
              <SelectItem value="detailed">Detailed Analysis</SelectItem>
              <SelectItem value="technical">Technical Report</SelectItem>
              <SelectItem value="dashboard">Dashboard Style</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Visual Elements</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="charts">Include Charts</Label>
              <Switch id="charts" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="tables">Include Data Tables</Label>
              <Switch id="tables" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="heatmaps">Include Heatmaps</Label>
              <Switch id="heatmaps" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="infographics">Include Infographics</Label>
              <Switch id="infographics" />
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Format;
