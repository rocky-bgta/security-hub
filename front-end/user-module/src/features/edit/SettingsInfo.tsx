import { Settings } from 'lucide-react';

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Label } from 'common/Label';
import { Separator } from 'common/Separator';
import { Switch } from 'common/Switch';
import { TabsContent } from 'common/Tabs';
import { IMSPData } from 'models/MSP';

interface IProps {
  isEditing: boolean;
  mspData: IMSPData;
  handleSettingChange: (setting: string, value: boolean) => void;
}

const SettingsInfo = ({ isEditing, mspData, handleSettingChange }: IProps) => {
  return (
    <TabsContent value="settings" className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Settings className="size-5" />
            Platform Settings
          </CardTitle>
          <CardDescription>
            Configure MSP access and feature permissions
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-4">
            <h4 className="font-medium">Notifications</h4>
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <Label htmlFor="emailNotifications">
                    Email Notifications
                  </Label>
                  <p className="text-sm text-muted-foreground">
                    Receive email notifications for important updates
                  </p>
                </div>
                <Switch
                  id="emailNotifications"
                  checked={mspData.settings.emailNotifications}
                  onCheckedChange={checked =>
                    handleSettingChange('emailNotifications', checked)
                  }
                  disabled={!isEditing}
                />
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <Label htmlFor="smsNotifications">SMS Notifications</Label>
                  <p className="text-sm text-muted-foreground">
                    Receive SMS alerts for critical issues
                  </p>
                </div>
                <Switch
                  id="smsNotifications"
                  checked={mspData.settings.smsNotifications}
                  onCheckedChange={checked =>
                    handleSettingChange('smsNotifications', checked)
                  }
                  disabled={!isEditing}
                />
              </div>
            </div>
          </div>

          <Separator />

          <div className="space-y-4">
            <h4 className="font-medium">Account Features</h4>
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <Label htmlFor="autoRenewal">Auto Renewal</Label>
                  <p className="text-sm text-muted-foreground">
                    Automatically renew subscription
                  </p>
                </div>
                <Switch
                  id="autoRenewal"
                  checked={mspData.settings.autoRenewal}
                  onCheckedChange={checked =>
                    handleSettingChange('autoRenewal', checked)
                  }
                  disabled={!isEditing}
                />
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <Label htmlFor="apiAccess">API Access</Label>
                  <p className="text-sm text-muted-foreground">
                    Enable API access for integrations
                  </p>
                </div>
                <Switch
                  id="apiAccess"
                  checked={mspData.settings.apiAccess}
                  onCheckedChange={checked =>
                    handleSettingChange('apiAccess', checked)
                  }
                  disabled={!isEditing}
                />
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <Label htmlFor="customBranding">Custom Branding</Label>
                  <p className="text-sm text-muted-foreground">
                    Allow custom branding and white-labeling
                  </p>
                </div>
                <Switch
                  id="customBranding"
                  checked={mspData.settings.customBranding}
                  onCheckedChange={checked =>
                    handleSettingChange('customBranding', checked)
                  }
                  disabled={!isEditing}
                />
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <Label htmlFor="reportingAccess">Advanced Reporting</Label>
                  <p className="text-sm text-muted-foreground">
                    Access to detailed analytics and reports
                  </p>
                </div>
                <Switch
                  id="reportingAccess"
                  checked={mspData.settings.reportingAccess}
                  onCheckedChange={checked =>
                    handleSettingChange('reportingAccess', checked)
                  }
                  disabled={!isEditing}
                />
              </div>
            </div>
          </div>

          <Separator />

          <div className="space-y-4">
            <h4 className="font-medium">Security</h4>
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <Label htmlFor="multiFactorAuth">
                    Multi-Factor Authentication
                  </Label>
                  <p className="text-sm text-muted-foreground">
                    Require MFA for account access
                  </p>
                </div>
                <Switch
                  id="multiFactorAuth"
                  checked={mspData.settings.multiFactorAuth}
                  onCheckedChange={checked =>
                    handleSettingChange('multiFactorAuth', checked)
                  }
                  disabled={!isEditing}
                />
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <Label htmlFor="ssoEnabled">Single Sign-On (SSO)</Label>
                  <p className="text-sm text-muted-foreground">
                    Enable SSO integration
                  </p>
                </div>
                <Switch
                  id="ssoEnabled"
                  checked={mspData.settings.ssoEnabled}
                  onCheckedChange={checked =>
                    handleSettingChange('ssoEnabled', checked)
                  }
                  disabled={!isEditing}
                />
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </TabsContent>
  );
};

export default SettingsInfo;
