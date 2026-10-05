import { CreditCard } from 'lucide-react';

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { TabsContent } from 'common/Tabs';
import { IMSPData } from 'models/Client';

interface IProps {
  isEditing: boolean;
  mspData: IMSPData;
  handleInputChange: (field: string, value: string | number) => void;
}

const SubscriptionInfo = ({
  isEditing,
  mspData,
  handleInputChange,
}: IProps) => {
  return (
    <TabsContent value="subscription" className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CreditCard className="size-5" />
            Subscription Details
          </CardTitle>
          <CardDescription>
            Manage subscription plan and license allocation
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="subscriptionPlan">Subscription Plan</Label>
              <Select
                value={mspData.subscriptionPlan}
                onValueChange={value =>
                  handleInputChange('subscriptionPlan', value)
                }
                disabled={!isEditing}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Basic">Basic</SelectItem>
                  <SelectItem value="Professional">Professional</SelectItem>
                  <SelectItem value="Enterprise">Enterprise</SelectItem>
                  <SelectItem value="Custom">Custom</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="contractEndDate">Contract End Date</Label>
              <Input
                id="contractEndDate"
                type="date"
                value={mspData.contractEndDate}
                onChange={e =>
                  handleInputChange('contractEndDate', e.target.value)
                }
                disabled={!isEditing}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="licenseCount">Total Licenses</Label>
              <Input
                id="licenseCount"
                type="number"
                value={mspData.licenseCount}
                onChange={e =>
                  handleInputChange(
                    'licenseCount',
                    Number.parseInt(e.target.value),
                  )
                }
                disabled={!isEditing}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="usedLicenses">Used Licenses</Label>
              <Input
                id="usedLicenses"
                type="number"
                value={mspData.usedLicenses}
                onChange={e =>
                  handleInputChange(
                    'usedLicenses',
                    Number.parseInt(e.target.value),
                  )
                }
                disabled={!isEditing}
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label>License Usage</Label>
            <div className="h-2 w-full rounded-full bg-gray-200">
              <div
                className="h-2 rounded-full bg-primary"
                style={{
                  width: `${(mspData.usedLicenses / mspData.licenseCount) * 100}%`,
                }}
              ></div>
            </div>
            <p className="text-sm text-muted-foreground">
              {mspData.usedLicenses} of {mspData.licenseCount} licenses used (
              {Math.round((mspData.usedLicenses / mspData.licenseCount) * 100)}
              %)
            </p>
          </div>
        </CardContent>
      </Card>
    </TabsContent>
  );
};

export default SubscriptionInfo;
