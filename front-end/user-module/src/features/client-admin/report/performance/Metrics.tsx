import { Dispatch, SetStateAction } from 'react';

import { Checkbox } from 'common/Checkbox';
import { Label } from 'common/Label';
import { TabsContent } from 'common/Tabs';

interface IProps {
  selectedMetrics: Array<string>;
  setSelectedMetrics: Dispatch<SetStateAction<Array<string>>>;
}

const Metrics = ({ selectedMetrics, setSelectedMetrics }: IProps) => {
  const toggleMetric = (metric: string) => {
    if (selectedMetrics.includes(metric)) {
      setSelectedMetrics(selectedMetrics.filter(m => m !== metric));
    } else {
      setSelectedMetrics([...selectedMetrics, metric]);
    }
  };

  return (
    <TabsContent value="metrics" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Performance Metrics</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="revenue"
                checked={selectedMetrics.includes('revenue')}
                onCheckedChange={() => toggleMetric('revenue')}
              />
              <Label htmlFor="revenue">Revenue Performance</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="clientGrowth"
                checked={selectedMetrics.includes('clientGrowth')}
                onCheckedChange={() => toggleMetric('clientGrowth')}
              />
              <Label htmlFor="clientGrowth">Client Growth Rate</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="satisfaction"
                checked={selectedMetrics.includes('satisfaction')}
                onCheckedChange={() => toggleMetric('satisfaction')}
              />
              <Label htmlFor="satisfaction">Client Satisfaction Scores</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="licenseUtilization"
                checked={selectedMetrics.includes('licenseUtilization')}
                onCheckedChange={() => toggleMetric('licenseUtilization')}
              />
              <Label htmlFor="licenseUtilization">License Utilization</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="trainingCompletion"
                checked={selectedMetrics.includes('trainingCompletion')}
                onCheckedChange={() => toggleMetric('trainingCompletion')}
              />
              <Label htmlFor="trainingCompletion">
                Training Completion Rates
              </Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="responseTime"
                checked={selectedMetrics.includes('responseTime')}
                onCheckedChange={() => toggleMetric('responseTime')}
              />
              <Label htmlFor="responseTime">Response Time</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="uptime"
                checked={selectedMetrics.includes('uptime')}
                onCheckedChange={() => toggleMetric('uptime')}
              />
              <Label htmlFor="uptime">System Uptime</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="riskAssessment"
                checked={selectedMetrics.includes('riskAssessment')}
                onCheckedChange={() => toggleMetric('riskAssessment')}
              />
              <Label htmlFor="riskAssessment">Risk Assessment</Label>
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Additional Analysis</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="trends" defaultChecked />
              <Label htmlFor="trends">Trend Analysis</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="forecasting" />
              <Label htmlFor="forecasting">Performance Forecasting</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="recommendations" defaultChecked />
              <Label htmlFor="recommendations">
                Improvement Recommendations
              </Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="alerts" />
              <Label htmlFor="alerts">Performance Alerts</Label>
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Metrics;
