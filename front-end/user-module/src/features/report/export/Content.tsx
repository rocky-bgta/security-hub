import { Dispatch, SetStateAction } from 'react';

import { Button } from 'common/Button';
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

interface IProps {
  selectedSections: Array<string>;
  setSelectedSections: Dispatch<SetStateAction<Array<string>>>;
  timeRange: string;
  setTimeRange: Dispatch<SetStateAction<string>>;
}

const availableSections = [
  {
    id: 'summary',
    label: 'Summary Statistics',
    description: 'Total MSPs, revenue, licenses',
  },
  {
    id: 'topPerformers',
    label: 'Top Performing MSPs',
    description: 'Best performing MSP partners',
  },
  {
    id: 'packageDistribution',
    label: 'Package Distribution',
    description: 'Package adoption across MSPs',
  },
  {
    id: 'monthlyGrowth',
    label: 'Monthly Growth Trends',
    description: 'Growth charts and trends',
  },
  {
    id: 'revenueAnalysis',
    label: 'Revenue Analysis',
    description: 'Detailed revenue breakdown',
  },
  {
    id: 'licenseUtilization',
    label: 'License Utilization',
    description: 'License usage statistics',
  },
  {
    id: 'clientMetrics',
    label: 'Client Metrics',
    description: 'Client-related analytics',
  },
  {
    id: 'performanceMetrics',
    label: 'Performance Metrics',
    description: 'KPIs and performance indicators',
  },
];

const Content = ({
  selectedSections,
  timeRange,
  setTimeRange,
  setSelectedSections,
}: IProps) => {
  const toggleSection = (sectionId: string) => {
    if (selectedSections.includes(sectionId)) {
      setSelectedSections(selectedSections.filter(s => s !== sectionId));
    } else {
      setSelectedSections([...selectedSections, sectionId]);
    }
  };
  return (
    <TabsContent value="content" className="mt-4 space-y-4">
      <div className="space-y-4">
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
          <h3 className="mb-2 text-sm font-medium">Report Sections</h3>
          <div className="max-h-64 space-y-3 overflow-y-auto rounded-md border p-3">
            {availableSections.map(section => (
              <div key={section.id} className="flex items-start space-x-3">
                <Checkbox
                  id={section.id}
                  checked={selectedSections.includes(section.id)}
                  onCheckedChange={() => toggleSection(section.id)}
                  className="mt-1"
                />
                <div className="flex-1">
                  <Label
                    htmlFor={section.id}
                    className="cursor-pointer text-sm font-medium"
                  >
                    {section.label}
                  </Label>
                  <p className="mt-1 text-xs text-muted-foreground">
                    {section.description}
                  </p>
                </div>
              </div>
            ))}
          </div>
          <div className="mt-2 flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() =>
                setSelectedSections(availableSections.map(s => s.id))
              }
            >
              Select All
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setSelectedSections([])}
            >
              Clear All
            </Button>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Data Filters</h3>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="activeMSPs">Active MSPs only</Label>
              <Switch id="activeMSPs" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="topPerformersOnly">Top performers only</Label>
              <Switch id="topPerformersOnly" />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="includeCharts">Include charts and graphs</Label>
              <Switch id="includeCharts" defaultChecked />
            </div>
            <div className="flex items-center justify-between">
              <Label htmlFor="includeRawData">Include raw data tables</Label>
              <Switch id="includeRawData" />
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Content;
