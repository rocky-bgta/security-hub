import { Dispatch, SetStateAction } from 'react';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Label } from 'common/Label';
import { TabsContent } from 'common/Tabs';

interface IProps {
  selectedPackages: Array<string>;
  setSelectedPackages: Dispatch<SetStateAction<Array<string>>>;
}

const Packages = ({ selectedPackages, setSelectedPackages }: IProps) => {
  const packageList = [
    'A-SAT',
    'A-Phish',
    'HR',
    'Banking',
    'Java',
    'C++',
    'Python',
    'Healthcare',
  ];

  const togglePackage = (pkg: string) => {
    if (selectedPackages.includes(pkg)) {
      setSelectedPackages(selectedPackages.filter(p => p !== pkg));
    } else {
      setSelectedPackages([...selectedPackages, pkg]);
    }
  };

  return (
    <TabsContent value="packages" className="mt-4 space-y-4">
      <div className="space-y-4">
        <div>
          <h3 className="mb-2 text-sm font-medium">Package Selection</h3>
          <div className="max-h-48 space-y-2 overflow-y-auto rounded-md border p-3">
            {packageList.map(pkg => (
              <div key={pkg} className="flex items-center space-x-2">
                <Checkbox
                  id={pkg}
                  checked={selectedPackages.includes(pkg)}
                  onCheckedChange={() => togglePackage(pkg)}
                />
                <Label htmlFor={pkg} className="text-sm">
                  {pkg}
                </Label>
              </div>
            ))}
          </div>
          <div className="mt-2 flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setSelectedPackages(packageList)}
            >
              Select All
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setSelectedPackages([])}
            >
              Clear All
            </Button>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">License Types</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="active" defaultChecked />
              <Label htmlFor="active">Active Licenses</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="expired" />
              <Label htmlFor="expired">Expired Licenses</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="pending" defaultChecked />
              <Label htmlFor="pending">Pending Licenses</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="suspended" />
              <Label htmlFor="suspended">Suspended Licenses</Label>
            </div>
          </div>
        </div>

        <div>
          <h3 className="mb-2 text-sm font-medium">Usage Metrics</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="allocated" defaultChecked />
              <Label htmlFor="allocated">Allocated vs Available</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="utilized" defaultChecked />
              <Label htmlFor="utilized">Utilization Rates</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="trends" defaultChecked />
              <Label htmlFor="trends">Usage Trends</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="efficiency" />
              <Label htmlFor="efficiency">Efficiency Metrics</Label>
            </div>
          </div>
        </div>
      </div>
    </TabsContent>
  );
};

export default Packages;
