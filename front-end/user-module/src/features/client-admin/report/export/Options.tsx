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

const Options = () => {
  return (
    <TabsContent value="options" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Report Customization</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="includeLogo">Include company logo</Label>
              <Switch id="includeLogo" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="includeTimestamp">
                Include generation timestamp
              </Label>
              <Switch id="includeTimestamp" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="includeFooter">Include page footer</Label>
              <Switch id="includeFooter" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="includeWatermark">
                Add confidential watermark
              </Label>
              <Switch id="includeWatermark" />
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Report Title</h3>
          <Input
            defaultValue={`MSP Analytics Report - ${new Date().toLocaleDateString()}`}
            className="w-full"
          />
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Executive Summary</h3>
          <Textarea
            placeholder="Add a custom executive summary or key insights to include in the report..."
            className="h-24 w-full resize-none"
          />
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Color Theme</h3>
          <Select defaultValue="default">
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select color theme" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="default">Default (Blue)</SelectItem>
              <SelectItem value="corporate">Corporate (Gray)</SelectItem>
              <SelectItem value="professional">Professional (Navy)</SelectItem>
              <SelectItem value="modern">Modern (Teal)</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Language</h3>
          <Select defaultValue="en">
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select language" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="en">English</SelectItem>
              <SelectItem value="es">Spanish</SelectItem>
              <SelectItem value="fr">French</SelectItem>
              <SelectItem value="de">German</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
    </TabsContent>
  );
};

export default Options;
