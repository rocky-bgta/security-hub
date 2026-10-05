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

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';

import { Alert, AlertDescription } from 'components/common/Alert';
import { Shield, Save, Key, AlertTriangle, CheckCircle } from 'lucide-react';

const SecuritySettings = () => {
  // Mock user data
  const currentUser = {
    id: '1',
    name: 'John Admin',
    email: 'admin@company.com',
    phone: '+1-555-0123',
    address: '123 Main St, City, State 12345',
    role: 'Admin',
    twoFactorEnabled: true,
    emailNotifications: true,
    smsNotifications: false,
    pushNotifications: true,
  };

  const handleSaveSecuritySettings = () => {
    // toast({
    //   title: 'Security Settings Updated',
    //   description: 'Your security settings have been updated successfully.',
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
              <Key className="size-5" />
              Password Settings
            </CardTitle>
            <CardDescription>
              Update your password and security preferences
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="current-password">Current Password</Label>
              <Input
                id="current-password"
                type="password"
                placeholder="Enter current password"
              />
            </div>
            <div>
              <Label htmlFor="new-password">New Password</Label>
              <Input
                id="new-password"
                type="password"
                placeholder="Enter new password"
              />
            </div>
            <div>
              <Label htmlFor="confirm-password">Confirm New Password</Label>
              <Input
                id="confirm-password"
                type="password"
                placeholder="Confirm new password"
              />
            </div>
            <Alert>
              <AlertTriangle className="size-4" />
              <AlertDescription>
                Password must be at least 8 characters with numbers and special
                characters.
              </AlertDescription>
            </Alert>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="size-5" />
              Two-Factor Authentication
            </CardTitle>
            <CardDescription>
              Add an extra layer of security to your account
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <Label>Enable Two-Factor Authentication</Label>
                <p className="text-sm text-muted-foreground">
                  Secure your account with 2FA
                </p>
              </div>
              <Switch defaultChecked={currentUser.twoFactorEnabled} />
            </div>
            {currentUser.twoFactorEnabled && (
              <Alert>
                <CheckCircle className="size-4" />
                <AlertDescription>
                  Two-factor authentication is enabled. Use your authenticator
                  app to generate codes.
                </AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Security Questions</CardTitle>
            <CardDescription>
              Set up security questions for account recovery
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="question1">Security Question 1</Label>
              <Select>
                <SelectTrigger>
                  <SelectValue placeholder="Select a security question" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="pet">
                    What was the name of your first pet?
                  </SelectItem>
                  <SelectItem value="school">
                    What elementary school did you attend?
                  </SelectItem>
                  <SelectItem value="city">
                    In what city were you born?
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="answer1">Answer</Label>
              <Input id="answer1" placeholder="Enter your answer" />
            </div>
          </CardContent>
        </Card>

        <Button
          onClick={handleSaveSecuritySettings}
          className="w-full md:w-auto"
        >
          <Save className="mr-2 size-4" />
          Save Security Settings
        </Button>
      </div>
    </div>
  );
};

export default SecuritySettings;
