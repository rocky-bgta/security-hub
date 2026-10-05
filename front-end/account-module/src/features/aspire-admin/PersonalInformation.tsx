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
import { Textarea } from 'components/common/Textarea';
import { User, Save } from 'lucide-react';

const AspireAdminPersonalInformation = () => {
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

  const handleSavePersonalInfo = () => {
    // toast.success('Personal Information Updated');
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

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <User className="size-5" />
            Personal Information
          </CardTitle>
          <CardDescription>
            Update your personal details and contact information
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <Label htmlFor="name">Full Name *</Label>
              <Input id="name" defaultValue={currentUser.name} />
            </div>
            <div>
              <Label htmlFor="email">Email Address *</Label>
              <Input id="email" type="email" defaultValue={currentUser.email} />
            </div>
            <div>
              <Label htmlFor="phone">Phone Number</Label>
              <Input id="phone" defaultValue={currentUser.phone} />
            </div>
            <div>
              <Label htmlFor="role">Role</Label>
              <Input id="role" defaultValue={currentUser.role} disabled />
            </div>
          </div>
          <div>
            <Label htmlFor="address">Address</Label>
            <Textarea
              id="address"
              defaultValue={currentUser.address}
              rows={3}
            />
          </div>
          <Button onClick={handleSavePersonalInfo} className="w-full md:w-auto">
            <Save className="mr-2 size-4" />
            Save Changes
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};

export default AspireAdminPersonalInformation;
