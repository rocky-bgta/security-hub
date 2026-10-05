import {
  Activity,
  Building2,
  Edit,
  Globe,
  Mail,
  MapPin,
  Package,
  Phone,
  TrendingUp,
  Users,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

import { Avatar, AvatarFallback, AvatarImage } from 'common/Avatar';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import IconBackButton from 'components/IconBackButton';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Progress } from 'common/Progress';
import { routes } from 'routes/Routes';
import { cn } from 'utils/Helper';

interface IProps {
  hostPath: typeof routes;
}

const mspProfile = {
  id: 'MSP001',
  name: 'TechSecure Solutions',
  contactPerson: 'John Anderson',
  email: 'john@techsecure.com',
  phone: '+1-555-0123',
  address: '123 Tech Street, Silicon Valley, CA 94025',
  website: 'https://techsecure.com',
  industry: 'Cybersecurity',
  status: 'Active',
  joinDate: '2023-03-15',
  lastLogin: '2024-01-15 10:30 AM',
  clients: 45,
  totalLicenses: 2500,
  usedLicenses: 1850,
  packages: ['A-SAT', 'A-Phish', 'HR'],
  revenue: '$125,000',
  commissionRate: 15,
  contractType: 'Standard',
  billingCycle: 'Monthly',
  description:
    'TechSecure Solutions is a leading cybersecurity training provider specializing in banking and financial services. With over 10 years of experience, they have successfully trained thousands of employees across multiple industries.',
  certifications: ['ISO 27001', 'SOC 2 Type II', 'CISSP Partner'],
  recentActivity: [
    {
      date: '2024-01-15',
      action: 'Added new client: First National Bank',
      type: 'client',
    },
    {
      date: '2024-01-12',
      action: 'Completed A-SAT training for 150 users',
      type: 'training',
    },
    {
      date: '2024-01-10',
      action: 'License renewal for City Bank',
      type: 'license',
    },
    {
      date: '2024-01-08',
      action: 'Generated monthly performance report',
      type: 'report',
    },
  ],
  performance: {
    clientSatisfaction: 95,
    trainingCompletion: 88,
    responseTime: 92,
    uptime: 99.8,
  },
  topClients: [
    { name: 'Bangladesh Bank', users: 1250, completion: 92 },
    { name: 'City Bank Limited', users: 850, completion: 88 },
    { name: 'BRAC Bank', users: 650, completion: 85 },
    { name: 'Dutch-Bangla Bank', users: 420, completion: 78 },
  ],
};

const ClientProfile = ({ hostPath }: IProps) => {
  const navigate = useNavigate();

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        <IconBackButton
          onClick={() => navigate(hostPath.clientList.path)}
          label="Back to Client List"
        />
        <div className="flex items-center justify-between">
          <div>
            <h2>MSP Profile</h2>
            <p className="text-muted-foreground">
              Detailed view of MSP partner information and performance
            </p>
          </div>
          <Button
            className="bg-primary text-primary-foreground"
            onClick={() => navigate(hostPath.clientEdit.path)}
          >
            <Edit className="mr-2 size-4" />
            Edit Profile
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-6 lg:col-span-2">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-start gap-6">
                <Avatar className="size-20">
                  <AvatarImage src="/placeholder.svg?height=80&width=80" />
                  <AvatarFallback className="bg-primary text-2xl text-primary-foreground">
                    {mspProfile.name
                      .split(' ')
                      .map(n => n[0])
                      .join('')}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1">
                  <div className="mb-2 flex items-center gap-3">
                    <h2 className="text-2xl font-bold text-foreground">
                      {mspProfile.name}
                    </h2>
                    <Badge
                      className={
                        mspProfile.status === 'Active'
                          ? 'bg-green-500/20 text-green-400'
                          : 'bg-orange-500/20 text-orange-400'
                      }
                    >
                      {mspProfile.status}
                    </Badge>
                  </div>
                  <p className="mb-2 text-lg text-muted-foreground">
                    {mspProfile.contactPerson}
                  </p>
                  <p className="mb-4 text-sm text-muted-foreground">
                    {mspProfile.description}
                  </p>
                  <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
                    <div className="text-center">
                      <p className="text-2xl font-bold text-primary">
                        {mspProfile.clients}
                      </p>
                      <p className="text-xs text-muted-foreground">Clients</p>
                    </div>
                    <div className="text-center">
                      <p className="text-2xl font-bold text-primary">
                        {Math.round(
                          (mspProfile.usedLicenses / mspProfile.totalLicenses) *
                            100,
                        )}
                        %
                      </p>
                      <p className="text-xs text-muted-foreground">
                        License Usage
                      </p>
                    </div>
                    <div className="text-center">
                      <p className="text-2xl font-bold text-primary">
                        {mspProfile.revenue}
                      </p>
                      <p className="text-xs text-muted-foreground">Revenue</p>
                    </div>
                    <div className="text-center">
                      <p className="text-2xl font-bold text-primary">
                        {mspProfile.commissionRate}%
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Commission
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-foreground">
                <TrendingUp className="size-5" />
                Performance Metrics
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid gap-6 md:grid-cols-2">
                <div className="space-y-4">
                  <div>
                    <div className="mb-2 flex justify-between">
                      <span className="text-sm font-medium text-foreground">
                        Client Satisfaction
                      </span>
                      <span className="text-sm text-foreground">
                        {mspProfile.performance.clientSatisfaction}%
                      </span>
                    </div>
                    <Progress
                      value={mspProfile.performance.clientSatisfaction}
                      className="h-2"
                    />
                  </div>
                  <div>
                    <div className="mb-2 flex justify-between">
                      <span className="text-sm font-medium text-foreground">
                        Training Completion
                      </span>
                      <span className="text-sm text-foreground">
                        {mspProfile.performance.trainingCompletion}%
                      </span>
                    </div>
                    <Progress
                      value={mspProfile.performance.trainingCompletion}
                      className="h-2"
                    />
                  </div>
                </div>
                <div className="space-y-4">
                  <div>
                    <div className="mb-2 flex justify-between">
                      <span className="text-sm font-medium text-foreground">
                        Response Time
                      </span>
                      <span className="text-sm text-foreground">
                        {mspProfile.performance.responseTime}%
                      </span>
                    </div>
                    <Progress
                      value={mspProfile.performance.responseTime}
                      className="h-2"
                    />
                  </div>
                  <div>
                    <div className="mb-2 flex justify-between">
                      <span className="text-sm font-medium text-foreground">
                        System Uptime
                      </span>
                      <span className="text-sm text-foreground">
                        {mspProfile.performance.uptime}%
                      </span>
                    </div>
                    <Progress
                      value={mspProfile.performance.uptime}
                      className="h-2"
                    />
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-foreground">
                <Building2 className="size-5" />
                Top Clients
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {mspProfile.topClients.map((client, index) => (
                  <div
                    key={client.name}
                    className="flex items-center justify-between rounded-lg border border-card-border p-3"
                  >
                    <div className="flex items-center gap-3">
                      <div className="flex size-8 items-center justify-center rounded-full bg-primary/20 text-sm font-bold text-primary">
                        {index + 1}
                      </div>
                      <div>
                        <p className="font-medium text-foreground">
                          {client.name}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          {client.users.toLocaleString()} users
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="font-medium text-foreground">
                        {client.completion}%
                      </p>
                      <p className="text-sm text-muted-foreground">
                        Completion
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-foreground">
                <Activity className="size-5" />
                Recent Activity
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {mspProfile.recentActivity.map((activity, index) => (
                  <div
                    key={index}
                    className="flex items-start gap-3 rounded-lg border border-card-border p-3"
                  >
                    <div
                      className={cn(
                        'rounded-lg p-2',
                        activity.type === 'client'
                          ? 'bg-blue-500/20'
                          : activity.type === 'training'
                            ? 'bg-green-500/20'
                            : activity.type === 'license'
                              ? 'bg-purple-500/20'
                              : 'bg-orange-500/20',
                      )}
                    >
                      {activity.type === 'client' && (
                        <Building2 className="size-4 text-blue-400" />
                      )}
                      {activity.type === 'training' && (
                        <Users className="size-4 text-green-400" />
                      )}
                      {activity.type === 'license' && (
                        <Package className="size-4 text-purple-400" />
                      )}
                      {activity.type === 'report' && (
                        <TrendingUp className="size-4 text-orange-400" />
                      )}
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium text-foreground">
                        {activity.action}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {activity.date}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-foreground">
                Contact Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-3">
                <Mail className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.email}
                  </p>
                  <p className="text-xs text-muted-foreground">Primary Email</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Phone className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.phone}
                  </p>
                  <p className="text-xs text-muted-foreground">Phone Number</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <MapPin className="mt-1 size-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.address}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    Business Address
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Globe className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.website}
                  </p>
                  <p className="text-xs text-muted-foreground">Website</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-foreground">
                Business Details
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-xs text-muted-foreground">Industry</p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.industry}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Contract Type</p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.contractType}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Billing Cycle</p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.billingCycle}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Join Date</p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.joinDate}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Last Login</p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.lastLogin}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-foreground">Certifications</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                {mspProfile.certifications.map(cert => (
                  <Badge
                    key={cert}
                    variant="outline"
                    className="w-full justify-center"
                  >
                    {cert}
                  </Badge>
                ))}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-foreground">Active Packages</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {mspProfile.packages.map(pkg => (
                  <Badge key={pkg} className="bg-primary/20 text-primary">
                    {pkg}
                  </Badge>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default ClientProfile;
