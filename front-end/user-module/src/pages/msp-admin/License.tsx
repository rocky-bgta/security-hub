import {
  AlertTriangle,
  Calendar,
  Download,
  Minus,
  Package,
  Plus,
  Settings,
  Users,
} from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { Avatar, AvatarFallback, AvatarImage } from 'common/Avatar';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import IconBackButton from 'components/IconBackButton';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Progress } from 'common/Progress';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

import { routes } from 'routes/Routes';

const mspLicenseData = {
  msp: {
    id: 'MSP001',
    name: 'TechSecure Solutions',
    contactPerson: 'John Anderson',
    totalLicenses: 2500,
    usedLicenses: 1850,
    availableLicenses: 650,
  },
  packages: [
    {
      id: 'asat',
      name: 'A-SAT',
      totalLicenses: 1000,
      usedLicenses: 750,
      price: '$15',
      expiryDate: '2024-12-31',
      status: 'Active',
    },
    {
      id: 'aphish',
      name: 'A-Phish',
      totalLicenses: 800,
      usedLicenses: 600,
      price: '$12',
      expiryDate: '2024-11-30',
      status: 'Active',
    },
    {
      id: 'hr',
      name: 'HR Training',
      totalLicenses: 500,
      usedLicenses: 350,
      price: '$10',
      expiryDate: '2024-10-15',
      status: 'Expiring Soon',
    },
    {
      id: 'banking',
      name: 'Banking Module',
      totalLicenses: 200,
      usedLicenses: 150,
      price: '$20',
      expiryDate: '2024-09-30',
      status: 'Expired',
    },
  ],
  clients: [
    {
      id: 'CLT001',
      name: 'Bangladesh Bank',
      assignedLicenses: 500,
      usedLicenses: 450,
      packages: ['A-SAT', 'Banking'],
      lastActivity: '2024-01-15',
    },
    {
      id: 'CLT002',
      name: 'City Bank Limited',
      assignedLicenses: 300,
      usedLicenses: 250,
      packages: ['A-SAT', 'A-Phish'],
      lastActivity: '2024-01-14',
    },
    {
      id: 'CLT003',
      name: 'BRAC Bank',
      assignedLicenses: 250,
      usedLicenses: 200,
      packages: ['A-SAT', 'HR'],
      lastActivity: '2024-01-13',
    },
  ],
  recentTransactions: [
    {
      id: 'TXN001',
      type: 'Purchase',
      package: 'A-SAT',
      quantity: 200,
      date: '2024-01-10',
      amount: '$3,000',
    },
    {
      id: 'TXN002',
      type: 'Assignment',
      client: 'Bangladesh Bank',
      quantity: 100,
      date: '2024-01-08',
      package: 'A-Phish',
    },
    {
      id: 'TXN003',
      type: 'Renewal',
      package: 'HR Training',
      quantity: 150,
      date: '2024-01-05',
      amount: '$1,500',
    },
  ],
};

interface IProps {
  hostPath: typeof routes;
}

const MSPLicense = ({ hostPath }: IProps) => {
  const navigate = useNavigate();
  const [selectedPackage, setSelectedPackage] = useState<string>('');
  const [licenseQuantity, setLicenseQuantity] = useState<number>(0);
  const [isAddDialogOpen, setIsAddDialogOpen] = useState<boolean>(false);

  const handleAddLicenses = () => {
    setIsAddDialogOpen(false);
    setLicenseQuantity(0);
    setSelectedPackage('');
  };

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        <IconBackButton
          onClick={() => navigate(hostPath.mspList.path)}
          label="Back to MSP List"
        />
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-foreground">
              Manage Licenses
            </h1>
            <p className="text-muted-foreground">
              Manage license allocation for {mspLicenseData.msp.name}
            </p>
          </div>
          <div className="flex gap-2">
          <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
            <DialogTrigger asChild>
              <Button className="bg-primary text-primary-foreground">
                <Plus className="mr-2 size-4" />
                Add Licenses
              </Button>
            </DialogTrigger>
            <DialogContent className="border-card-border bg-card">
              <DialogHeader>
                <DialogTitle className="text-foreground">
                  Add New Licenses
                </DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="package" className="text-foreground">
                    Package
                  </Label>
                  <Select onValueChange={setSelectedPackage}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select package" />
                    </SelectTrigger>
                    <SelectContent className="border-card-border bg-secondary">
                      <SelectItem value="asat">A-SAT ($15/license)</SelectItem>
                      <SelectItem value="aphish">
                        A-Phish ($12/license)
                      </SelectItem>
                      <SelectItem value="hr">
                        HR Training ($10/license)
                      </SelectItem>
                      <SelectItem value="banking">
                        Banking Module ($20/license)
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="quantity" className="text-foreground">
                    Quantity
                  </Label>
                  <Input
                    id="quantity"
                    type="number"
                    value={licenseQuantity}
                    onChange={e =>
                      setLicenseQuantity(Number.parseInt(e.target.value) || 0)
                    }
                    className="border-card-border bg-muted text-foreground"
                    placeholder="Enter number of licenses"
                  />
                </div>
                {selectedPackage && licenseQuantity > 0 && (
                  <div className="rounded-lg bg-muted p-3">
                    <p className="text-sm text-foreground">
                      Total Cost:{' '}
                      <span className="font-bold">${licenseQuantity * 15}</span>
                    </p>
                  </div>
                )}
                <div className="flex gap-2">
                  <Button
                    onClick={handleAddLicenses}
                    className="flex-1 bg-primary text-primary-foreground"
                  >
                    Add Licenses
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => setIsAddDialogOpen(false)}
                  >
                    Cancel
                  </Button>
                </div>
              </div>
            </DialogContent>
          </Dialog>
          <Button
            variant="outline"
            onClick={() => navigate(hostPath.mspLicenseAllocation.path)}
          >
            <Settings className="mr-2 size-4" />
            Manage Client Allocation
          </Button>
          <Button variant="outline">
            <Download className="mr-2 size-4" />
            Export Report
          </Button>
        </div>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-foreground">
            <Package className="size-5" />
            License Overview
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex items-center gap-6">
            <Avatar className="size-16">
              <AvatarImage src="/placeholder.svg?height=64&width=64" />
              <AvatarFallback className="bg-primary text-lg text-primary-foreground">
                {mspLicenseData.msp.name
                  .split(' ')
                  .map(n => n[0])
                  .join('')}
              </AvatarFallback>
            </Avatar>
            <div>
              <h3 className="text-xl font-semibold text-foreground">
                {mspLicenseData.msp.name}
              </h3>
              <p className="text-muted-foreground">
                {mspLicenseData.msp.contactPerson}
              </p>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-3">
            <div className="rounded-lg border border-card-border p-4 text-center">
              <p className="text-2xl font-bold text-primary">
                {mspLicenseData.msp.totalLicenses}
              </p>
              <p className="text-sm text-muted-foreground">Total Licenses</p>
            </div>
            <div className="rounded-lg border border-card-border p-4 text-center">
              <p className="text-2xl font-bold text-green-400">
                {mspLicenseData.msp.usedLicenses}
              </p>
              <p className="text-sm text-muted-foreground">Used Licenses</p>
            </div>
            <div className="rounded-lg border border-card-border p-4 text-center">
              <p className="text-2xl font-bold text-blue-400">
                {mspLicenseData.msp.availableLicenses}
              </p>
              <p className="text-sm text-muted-foreground">
                Available Licenses
              </p>
            </div>
          </div>

          <div className="mt-4">
            <div className="mb-2 flex justify-between">
              <span className="text-sm font-medium text-foreground">
                License Utilization
              </span>
              <span className="text-sm text-foreground">
                {Math.round(
                  (mspLicenseData.msp.usedLicenses /
                    mspLicenseData.msp.totalLicenses) *
                    100,
                )}
                %
              </span>
            </div>
            <Progress
              value={
                (mspLicenseData.msp.usedLicenses /
                  mspLicenseData.msp.totalLicenses) *
                100
              }
              className="h-3"
            />
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-foreground">Package Licenses</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {mspLicenseData.packages.map(pkg => (
                <div
                  key={pkg.id}
                  className="rounded-lg border border-card-border p-4"
                >
                  <div className="mb-3 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <h4 className="font-medium text-foreground">
                        {pkg.name}
                      </h4>
                      <Badge
                        variant={
                          pkg.status === 'Active'
                            ? 'default'
                            : pkg.status === 'Expiring Soon'
                              ? 'secondary'
                              : 'destructive'
                        }
                        className={
                          pkg.status === 'Active'
                            ? 'bg-green-500/20 text-green-400'
                            : pkg.status === 'Expiring Soon'
                              ? 'bg-orange-500/20 text-orange-400'
                              : 'bg-red-500/20 text-red-400'
                        }
                      >
                        {pkg.status}
                      </Badge>
                    </div>
                    <div className="flex gap-2">
                      <Button size="sm" variant="outline">
                        <Plus className="size-3" />
                      </Button>
                      <Button size="sm" variant="outline">
                        <Minus className="size-3" />
                      </Button>
                    </div>
                  </div>

                  <div className="mb-3 grid grid-cols-3 gap-4 text-sm">
                    <div>
                      <p className="text-muted-foreground">Total</p>
                      <p className="font-medium text-foreground">
                        {pkg.totalLicenses}
                      </p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">Used</p>
                      <p className="font-medium text-foreground">
                        {pkg.usedLicenses}
                      </p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">Price</p>
                      <p className="font-medium text-foreground">{pkg.price}</p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <div className="flex justify-between text-xs">
                      <span className="text-muted-foreground">Usage</span>
                      <span className="text-foreground">
                        {Math.round(
                          (pkg.usedLicenses / pkg.totalLicenses) * 100,
                        )}
                        %
                      </span>
                    </div>
                    <Progress
                      value={(pkg.usedLicenses / pkg.totalLicenses) * 100}
                      className="h-2"
                    />
                  </div>

                  <div className="mt-3 flex items-center justify-between text-xs">
                    <span className="text-muted-foreground">
                      Expires: {pkg.expiryDate}
                    </span>
                    {pkg.status === 'Expiring Soon' && (
                      <AlertTriangle className="size-4 text-orange-400" />
                    )}
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2 text-foreground">
                <Users className="size-5" />
                Client License Allocation
              </CardTitle>
              <Button
                size="sm"
                variant="outline"
                onClick={() => navigate(hostPath.mspLicenseAllocation.path)}
              >
                <Settings className="mr-2 size-4" />
                Manage All
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {mspLicenseData.clients.map(client => (
                <div
                  key={client.id}
                  className="rounded-lg border border-card-border p-4"
                >
                  <div className="mb-3 flex items-center justify-between">
                    <h4 className="font-medium text-foreground">
                      {client.name}
                    </h4>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() =>
                        navigate(hostPath.mspLicenseAllocation.path)
                      }
                    >
                      Manage
                    </Button>
                  </div>

                  <div className="mb-3 grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-muted-foreground">Assigned</p>
                      <p className="font-medium text-foreground">
                        {client.assignedLicenses}
                      </p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">Used</p>
                      <p className="font-medium text-foreground">
                        {client.usedLicenses}
                      </p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <div className="flex justify-between text-xs">
                      <span className="text-muted-foreground">Utilization</span>
                      <span className="text-foreground">
                        {Math.round(
                          (client.usedLicenses / client.assignedLicenses) * 100,
                        )}
                        %
                      </span>
                    </div>
                    <Progress
                      value={
                        (client.usedLicenses / client.assignedLicenses) * 100
                      }
                      className="h-2"
                    />
                  </div>

                  <div className="mt-3 flex items-center justify-between">
                    <div className="flex flex-wrap gap-1">
                      {client.packages.map(pkg => (
                        <Badge key={pkg} variant="outline" className="text-xs">
                          {pkg}
                        </Badge>
                      ))}
                    </div>
                    <span className="text-xs text-muted-foreground">
                      Last: {client.lastActivity}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-foreground">
            <Calendar className="size-5" />
            Recent License Transactions
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="grid grid-cols-6 gap-4 border-b border-card-border pb-2 text-sm font-medium text-muted-foreground">
              <div>TYPE</div>
              <div>PACKAGE/CLIENT</div>
              <div>QUANTITY</div>
              <div>DATE</div>
              <div>AMOUNT</div>
              <div>STATUS</div>
            </div>
            {mspLicenseData.recentTransactions.map(transaction => (
              <div
                key={transaction.id}
                className="grid grid-cols-6 items-center gap-4 border-b border-card-border/50 py-3 last:border-b-0"
              >
                <div>
                  <Badge
                    variant="outline"
                    className={
                      transaction.type === 'Purchase'
                        ? 'border-green-400 text-green-400'
                        : transaction.type === 'Assignment'
                          ? 'border-blue-400 text-blue-400'
                          : 'border-orange-400 text-orange-400'
                    }
                  >
                    {transaction.type}
                  </Badge>
                </div>
                <div className="text-sm text-foreground">
                  {transaction.package || transaction.client}
                </div>
                <div className="text-sm text-foreground">
                  {transaction.quantity}
                </div>
                <div className="text-sm text-muted-foreground">
                  {transaction.date}
                </div>
                <div className="text-sm text-foreground">
                  {transaction.amount || '-'}
                </div>
                <div>
                  <Badge className="bg-green-500/20 text-green-400">
                    Completed
                  </Badge>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default MSPLicense;
