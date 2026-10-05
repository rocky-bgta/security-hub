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
          <h3 className="mb-2 text-sm font-medium">Revenue Metrics</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="totalRevenue"
                checked={selectedMetrics.includes('totalRevenue')}
                onCheckedChange={() => toggleMetric('totalRevenue')}
              />
              <Label htmlFor="totalRevenue">Total Revenue</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="revenueGrowth"
                checked={selectedMetrics.includes('revenueGrowth')}
                onCheckedChange={() => toggleMetric('revenueGrowth')}
              />
              <Label htmlFor="revenueGrowth">Revenue Growth Rate</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="recurringRevenue"
                checked={selectedMetrics.includes('recurringRevenue')}
                onCheckedChange={() => toggleMetric('recurringRevenue')}
              />
              <Label htmlFor="recurringRevenue">
                Monthly Recurring Revenue (MRR)
              </Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="annualRevenue"
                checked={selectedMetrics.includes('annualRevenue')}
                onCheckedChange={() => toggleMetric('annualRevenue')}
              />
              <Label htmlFor="annualRevenue">
                Annual Recurring Revenue (ARR)
              </Label>
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">MSP Performance</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="mspContribution"
                checked={selectedMetrics.includes('mspContribution')}
                onCheckedChange={() => toggleMetric('mspContribution')}
              />
              <Label htmlFor="mspContribution">MSP Revenue Contribution</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="topPerformers"
                checked={selectedMetrics.includes('topPerformers')}
                onCheckedChange={() => toggleMetric('topPerformers')}
              />
              <Label htmlFor="topPerformers">Top Revenue Performers</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="commissions"
                checked={selectedMetrics.includes('commissions')}
                onCheckedChange={() => toggleMetric('commissions')}
              />
              <Label htmlFor="commissions">Commission Payments</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="churnImpact"
                checked={selectedMetrics.includes('churnImpact')}
                onCheckedChange={() => toggleMetric('churnImpact')}
              />
              <Label htmlFor="churnImpact">Churn Impact Analysis</Label>
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Product Performance</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="packageRevenue"
                checked={selectedMetrics.includes('packageRevenue')}
                onCheckedChange={() => toggleMetric('packageRevenue')}
              />
              <Label htmlFor="packageRevenue">Revenue by Package</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="packageGrowth"
                checked={selectedMetrics.includes('packageGrowth')}
                onCheckedChange={() => toggleMetric('packageGrowth')}
              />
              <Label htmlFor="packageGrowth">Package Growth Rates</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="crossSelling"
                checked={selectedMetrics.includes('crossSelling')}
                onCheckedChange={() => toggleMetric('crossSelling')}
              />
              <Label htmlFor="crossSelling">Cross-selling Analysis</Label>
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Financial Health</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="profitMargins"
                checked={selectedMetrics.includes('profitMargins')}
                onCheckedChange={() => toggleMetric('profitMargins')}
              />
              <Label htmlFor="profitMargins">Profit Margins</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="costAnalysis"
                checked={selectedMetrics.includes('costAnalysis')}
                onCheckedChange={() => toggleMetric('costAnalysis')}
              />
              <Label htmlFor="costAnalysis">Cost Analysis</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="roi"
                checked={selectedMetrics.includes('roi')}
                onCheckedChange={() => toggleMetric('roi')}
              />
              <Label htmlFor="roi">Return on Investment (ROI)</Label>
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Metrics;
