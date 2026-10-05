import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Button } from 'components/common/Button';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import { Switch } from 'components/common/Switch';
import { Settings, Save } from 'lucide-react';

const GlobalSettings = () => {
  const handleSaveGlobalSettings = () => {
    // toast({
    //   title: 'Global Settings Updated',
    //   description: 'System-wide settings have been applied successfully.',
    // });
  };

  return (
    <div className="space-y-6 p-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Account Management
        </h1>
        <p className="text-muted-foreground">
          Manage account settings and system configuration
        </p>
      </div>

      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Settings className="size-5" />
              Global Security Policies
            </CardTitle>
            <CardDescription>
              Configure system-wide security and account policies
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <Label htmlFor="min-password-length">
                  Minimum Password Length
                </Label>
                <Input
                  id="min-password-length"
                  type="number"
                  defaultValue="8"
                />
              </div>
              <div>
                <Label htmlFor="session-timeout">
                  Session Timeout (minutes)
                </Label>
                <Input id="session-timeout" type="number" defaultValue="30" />
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <Label>Enforce Two-Factor Authentication</Label>
                <Switch defaultChecked />
              </div>
              <div className="flex items-center justify-between">
                <Label>Require Password Change Every 90 Days</Label>
                <Switch />
              </div>
              <div className="flex items-center justify-between">
                <Label>Lock Account After Failed Login Attempts</Label>
                <Switch defaultChecked />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Global Notification Settings</CardTitle>
            <CardDescription>
              Configure default notification settings for new users
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <Label>Enable Email Notifications by Default</Label>
                <Switch defaultChecked />
              </div>
              <div className="flex items-center justify-between">
                <Label>Enable SMS Notifications by Default</Label>
                <Switch />
              </div>
              <div className="flex items-center justify-between">
                <Label>Enable Push Notifications by Default</Label>
                <Switch defaultChecked />
              </div>
            </div>
          </CardContent>
        </Card>

        <Button onClick={handleSaveGlobalSettings} className="w-full md:w-auto">
          <Save className="mr-2 size-4" />
          Save Global Settings
        </Button>
      </div>
    </div>
  );
};

export default GlobalSettings;
