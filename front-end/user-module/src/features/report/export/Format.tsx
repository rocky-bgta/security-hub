import { FileImage, FileSpreadsheet, FileText } from 'lucide-react';
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
  exportFormat: string;
  setExportFormat: Dispatch<SetStateAction<string>>;
}

const Format = ({ exportFormat, setExportFormat }: IProps) => {
  return (
    <TabsContent value="format" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Export Format</h3>
          <RadioGroup
            value={exportFormat}
            onValueChange={setExportFormat}
            className="flex flex-col space-y-3"
          >
            <div className="flex items-center space-x-2 rounded-lg border p-3">
              <RadioGroupItem value="pdf" id="pdf" />
              <div className="flex items-center space-x-3">
                <FileText className="size-5 text-red-500" />
                <div>
                  <Label htmlFor="pdf" className="cursor-pointer font-medium">
                    PDF Report
                  </Label>
                  <p className="text-xs text-muted-foreground">
                    Professional formatted report with charts
                  </p>
                </div>
              </div>
            </div>
            <div className="flex items-center space-x-2 rounded-lg border p-3">
              <RadioGroupItem value="excel" id="excel" />
              <div className="flex items-center space-x-3">
                <FileSpreadsheet className="size-5 text-green-500" />
                <div>
                  <Label htmlFor="excel" className="cursor-pointer font-medium">
                    Excel Workbook
                  </Label>
                  <p className="text-xs text-muted-foreground">
                    Multiple sheets with data and charts
                  </p>
                </div>
              </div>
            </div>
            <div className="flex items-center space-x-2 rounded-lg border p-3">
              <RadioGroupItem value="csv" id="csv" />
              <div className="flex items-center space-x-3">
                <FileSpreadsheet className="size-5 text-blue-500" />
                <div>
                  <Label htmlFor="csv" className="cursor-pointer font-medium">
                    CSV Data Export
                  </Label>
                  <p className="text-xs text-muted-foreground">
                    Raw data in comma-separated format
                  </p>
                </div>
              </div>
            </div>
            <div className="flex items-center space-x-2 rounded-lg border p-3">
              <RadioGroupItem value="powerpoint" id="powerpoint" />
              <div className="flex items-center space-x-3">
                <FileImage className="size-5 text-orange-500" />
                <div>
                  <Label
                    htmlFor="powerpoint"
                    className="cursor-pointer font-medium"
                  >
                    PowerPoint Presentation
                  </Label>
                  <p className="text-xs text-muted-foreground">
                    Executive presentation format
                  </p>
                </div>
              </div>
            </div>
          </RadioGroup>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">
            Page Layout (PDF/PowerPoint)
          </h3>
          <Select defaultValue="portrait">
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select orientation" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="portrait">Portrait</SelectItem>
              <SelectItem value="landscape">Landscape</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Chart Quality</h3>
          <Select defaultValue="high">
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select quality" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="standard">Standard (Faster)</SelectItem>
              <SelectItem value="high">High Quality</SelectItem>
              <SelectItem value="print">Print Quality (Slower)</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
    </TabsContent>
  );
};

export default Format;
