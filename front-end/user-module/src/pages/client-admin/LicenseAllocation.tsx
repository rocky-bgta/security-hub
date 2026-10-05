import {
  Edit,
  Minus,
  Package,
  Plus,
  Save,
  Users,
  X,
} from 'lucide-react';
import { useState } from 'react';

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
import AddPackage from 'features/client-admin/license-allocation/AddPackage';

const clientAllocationData = {
  msp: {
    id: 'MSP001',
    name: 'TechSecure Solutions',
    totalLicenses: 2500,
    usedLicenses: 1850,
    availableLicenses: 650,
  },
  packages: [
    { id: 'asat', name: 'A-SAT', available: 250, price: '$15' },
    { id: 'aphish', name: 'A-Phish', available: 200, price: '$12' },
    { id: 'hr', name: 'HR Training', available: 150, price: '$10' },
    { id: 'banking', name: 'Banking Module', available: 50, price: '$20' },
  ],
  clients: [
    {
      id: 'CLT001',
      name: 'Bangladesh Bank',
      contactPerson: 'Dr. Ahmed Rahman',
      email: 'ahmed@bangladeshbank.org',
      assignedLicenses: 500,
      usedLicenses: 450,
      availableLicenses: 50,
      packages: [
        { name: 'A-SAT', assigned: 200, used: 180 },
        { name: 'Banking', assigned: 200, used: 190 },
        { name: 'A-Phish', assigned: 100, used: 80 },
      ],
      lastActivity: '2024-01-15',
      status: 'Active',
    },
    {
      id: 'CLT002',
      name: 'City Bank Limited',
      contactPerson: 'Ms. Fatima Khan',
      email: 'fatima@citybank.com.bd',
      assignedLicenses: 300,
      usedLicenses: 250,
      availableLicenses: 50,
      packages: [
        { name: 'A-SAT', assigned: 150, used: 130 },
        { name: 'A-Phish', assigned: 100, used: 85 },
        { name: 'HR Training', assigned: 50, used: 35 },
      ],
      lastActivity: '2024-01-14',
      status: 'Active',
    },
    {
      id: 'CLT003',
      name: 'BRAC Bank',
      contactPerson: 'Mr. Karim Hassan',
      email: 'karim@bracbank.com',
      assignedLicenses: 250,
      usedLicenses: 200,
      availableLicenses: 50,
      packages: [
        { name: 'A-SAT', assigned: 150, used: 120 },
        { name: 'HR Training', assigned: 100, used: 80 },
      ],
      lastActivity: '2024-01-13',
      status: 'Active',
    },
    {
      id: 'CLT004',
      name: 'Dutch-Bangla Bank',
      contactPerson: 'Ms. Rashida Begum',
      email: 'rashida@dutchbanglabank.com',
      assignedLicenses: 200,
      usedLicenses: 120,
      availableLicenses: 80,
      packages: [
        { name: 'A-SAT', assigned: 100, used: 70 },
        { name: 'HR Training', assigned: 100, used: 50 },
      ],
      lastActivity: '2024-01-10',
      status: 'Pending',
    },
  ],
};

const ClientLicenseAllocation = () => {
  const [isEditMode, setIsEditMode] = useState<boolean>(false);
  const [isAddDialogOpen, setIsAddDialogOpen] = useState<boolean>(false);
  const [editingAllocations, setEditingAllocations] = useState<{
    [key: string]: number;
  }>({});
  const [newAllocation, setNewAllocation] = useState<{
    clientId: string;
    packageId: string;
    quantity: number;
  }>({
    clientId: '',
    packageId: '',
    quantity: 0,
  });
  const [addPackageDialog, setAddPackageDialog] = useState<{
    isOpen: boolean;
    clientId: string;
    clientName: string;
    currentPackages: string[];
  }>({
    isOpen: false,
    clientId: '',
    clientName: '',
    currentPackages: [],
  });

  const handleEditAllocation = (
    clientId: string,
    packageName: string,
    newValue: number,
  ) => {
    setEditingAllocations(prev => ({
      ...prev,
      [`${clientId}-${packageName}`]: newValue,
    }));
  };

  const handleSaveAllocations = () => {
    setIsEditMode(false);
    setEditingAllocations({});
  };

  const handleAddAllocation = () => {
    setIsAddDialogOpen(false);
    setNewAllocation({ clientId: '', packageId: '', quantity: 0 });
  };

  const handleAddPackage = (
    clientId: string,
    clientName: string,
    currentPackages: string[],
  ) => {
    setAddPackageDialog({
      isOpen: true,
      clientId,
      clientName,
      currentPackages,
    });
  };

  const handleViewUsers = (
    clientId: string,
    clientName: string | number | boolean,
  ) => {
    // window.location.hash = `client-users?clientId=${clientId}&clientName=${encodeURIComponent(clientName)}`;
  };

  const handleUsageReport = (
    clientId: string,
    clientName: string | number | boolean,
  ) => {
    // window.location.hash = `client-usage-report?clientId=${clientId}&clientName=${encodeURIComponent(clientName)}`;
  };

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        <IconBackButton
          label="Back"
          onClick={() => window.history.back()}
        />
        <div className="flex items-center justify-between">
          <div>
            <h2>Client License Allocation</h2>
            <p className="text-muted-foreground">
              Manage license distribution across client organizations
            </p>
          </div>
          <div className="flex gap-2">
          <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
            <DialogTrigger asChild>
              <Button className="bg-primary text-primary-foreground">
                <Plus className="mr-2 size-4" />
                Allocate Licenses
              </Button>
            </DialogTrigger>
            <DialogContent className="border-card-border bg-card">
              <DialogHeader>
                <DialogTitle className="text-foreground">
                  Allocate New Licenses
                </DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="client" className="text-foreground">
                    Client
                  </Label>
                  <Select
                    onValueChange={value =>
                      setNewAllocation(prev => ({ ...prev, clientId: value }))
                    }
                  >
                    <SelectTrigger className="border-card-border bg-muted">
                      <SelectValue placeholder="Select client" />
                    </SelectTrigger>
                    <SelectContent className="border-card-border bg-secondary">
                      {clientAllocationData.clients.map(client => (
                        <SelectItem key={client.id} value={client.id}>
                          {client.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="package" className="text-foreground">
                    Package
                  </Label>
                  <Select
                    onValueChange={value =>
                      setNewAllocation(prev => ({ ...prev, packageId: value }))
                    }
                  >
                    <SelectTrigger className="border-card-border bg-muted">
                      <SelectValue placeholder="Select package" />
                    </SelectTrigger>
                    <SelectContent className="border-card-border bg-secondary">
                      {clientAllocationData.packages.map(pkg => (
                        <SelectItem key={pkg.id} value={pkg.id}>
                          {pkg.name} ({pkg.available} available)
                        </SelectItem>
                      ))}
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
                    value={newAllocation.quantity}
                    onChange={e =>
                      setNewAllocation(prev => ({
                        ...prev,
                        quantity: Number.parseInt(e.target.value) || 0,
                      }))
                    }
                    className="border-card-border bg-muted text-foreground"
                    placeholder="Enter number of licenses"
                  />
                </div>
                <div className="flex gap-2">
                  <Button
                    onClick={handleAddAllocation}
                    className="flex-1 bg-primary text-primary-foreground"
                  >
                    Allocate Licenses
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
          {isEditMode ? (
            <div className="flex gap-2">
              <Button
                onClick={handleSaveAllocations}
                className="bg-green-600 text-white"
              >
                <Save className="mr-2 size-4" />
                Save Changes
              </Button>
              <Button variant="outline" onClick={() => setIsEditMode(false)}>
                <X className="mr-2 size-4" />
                Cancel
              </Button>
            </div>
          ) : (
            <Button variant="outline" onClick={() => setIsEditMode(true)}>
              <Edit className="mr-2 size-4" />
              Edit Allocations
            </Button>
          )}
        </div>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-foreground">
            <Package className="size-5" />
            MSP License Pool
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-4">
            <Card className="p-4 text-center">
              <p className="text-2xl font-bold text-primary">
                {clientAllocationData.msp.totalLicenses}
              </p>
              <p className="text-sm text-muted-foreground">Total Licenses</p>
            </Card>
            <Card className="p-4 text-center">
              <p className="text-2xl font-bold text-green-400">
                {clientAllocationData.msp.usedLicenses}
              </p>
              <p className="text-sm text-muted-foreground">Allocated</p>
            </Card>
            <Card className="p-4 text-center">
              <p className="text-2xl font-bold text-blue-400">
                {clientAllocationData.msp.availableLicenses}
              </p>
              <p className="text-sm text-muted-foreground">Available</p>
            </Card>
            <Card className="p-4 text-center">
              <p className="text-2xl font-bold text-orange-400">
                {Math.round(
                  (clientAllocationData.msp.usedLicenses /
                    clientAllocationData.msp.totalLicenses) *
                    100,
                )}
                %
              </p>
              <p className="text-sm text-muted-foreground">Utilization</p>
            </Card>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-foreground">
            Available Package Licenses
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-4">
            {clientAllocationData.packages.map(pkg => (
              <Card key={pkg.id} className="p-4 text-center">
                <h4 className="mb-2 font-medium text-foreground">{pkg.name}</h4>
                <p className="mb-1 text-2xl font-bold text-primary">
                  {pkg.available}
                </p>
                <p className="mb-2 text-sm text-muted-foreground">Available</p>
                <Badge variant="outline" className="text-xs">
                  {pkg.price}/license
                </Badge>
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-foreground">
            <Users className="size-5" />
            Client License Allocations
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-6">
            {clientAllocationData.clients.map(client => (
              <Card key={client.id} className="space-y-4 p-6">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <Avatar className="size-12">
                      <AvatarImage src="/placeholder.svg?height=48&width=48" />
                      <AvatarFallback className="bg-primary text-primary-foreground">
                        {client.name
                          .split(' ')
                          .map(n => n[0])
                          .join('')
                          .slice(0, 2)}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="text-lg font-semibold text-foreground">
                          {client.name}
                        </h3>
                        <Badge
                          className={
                            client.status === 'Active'
                              ? 'bg-green-500/20 text-green-400'
                              : 'bg-orange-500/20 text-orange-400'
                          }
                        >
                          {client.status}
                        </Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {client.contactPerson}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {client.email}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-sm text-muted-foreground">
                      Last Activity
                    </p>
                    <p className="text-sm font-medium text-foreground">
                      {client.lastActivity}
                    </p>
                  </div>
                </div>

                <div className="grid gap-4 md:grid-cols-3">
                  <Card className="p-3 text-center">
                    <p className="text-lg font-bold text-primary">
                      {client.assignedLicenses}
                    </p>
                    <p className="text-xs text-muted-foreground">Assigned</p>
                  </Card>
                  <Card className="p-3 text-center">
                    <p className="text-lg font-bold text-green-400">
                      {client.usedLicenses}
                    </p>
                    <p className="text-xs text-muted-foreground">Used</p>
                  </Card>
                  <Card className="p-3 text-center">
                    <p className="text-lg font-bold text-blue-400">
                      {client.availableLicenses}
                    </p>
                    <p className="text-xs text-muted-foreground">Available</p>
                  </Card>
                </div>

                <div>
                  <div className="mb-2 flex justify-between">
                    <span className="text-sm font-medium text-foreground">
                      Overall Utilization
                    </span>
                    <span className="text-sm text-foreground">
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
                    className="h-3"
                  />
                </div>

                <div className="space-y-3">
                  <h4 className="font-medium text-foreground">
                    Package Allocations
                  </h4>
                  <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
                    {client.packages.map(pkg => (
                      <Card key={pkg.name} className="p-3">
                        <div className="mb-2 flex items-center justify-between">
                          <span className="text-sm font-medium text-foreground">
                            {pkg.name}
                          </span>
                          {isEditMode ? (
                            <div className="flex items-center gap-1">
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() =>
                                  handleEditAllocation(
                                    client.id,
                                    pkg.name,
                                    (editingAllocations[
                                      `${client.id}-${pkg.name}`
                                    ] || pkg.assigned) - 1,
                                  )
                                }
                              >
                                <Minus className="size-3" />
                              </Button>
                              <Input
                                type="number"
                                value={
                                  editingAllocations[
                                    `${client.id}-${pkg.name}`
                                  ] || pkg.assigned
                                }
                                onChange={e =>
                                  handleEditAllocation(
                                    client.id,
                                    pkg.name,
                                    Number.parseInt(e.target.value) || 0,
                                  )
                                }
                                className="h-8 w-16 border-card-border bg-muted text-center text-foreground"
                              />
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() =>
                                  handleEditAllocation(
                                    client.id,
                                    pkg.name,
                                    (editingAllocations[
                                      `${client.id}-${pkg.name}`
                                    ] || pkg.assigned) + 1,
                                  )
                                }
                              >
                                <Plus className="size-3" />
                              </Button>
                            </div>
                          ) : (
                            <span className="text-sm text-muted-foreground">
                              {pkg.used}/{pkg.assigned}
                            </span>
                          )}
                        </div>
                        <Progress
                          value={(pkg.used / pkg.assigned) * 100}
                          className="h-2"
                        />
                        <div className="mt-1 flex justify-between text-xs text-muted-foreground">
                          <span>Used: {pkg.used}</span>
                          <span>
                            {Math.round((pkg.used / pkg.assigned) * 100)}%
                          </span>
                        </div>
                      </Card>
                    ))}
                  </div>
                </div>

                <div className="flex gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() =>
                      handleAddPackage(
                        client.id,
                        client.name,
                        client.packages.map(p => p.name),
                      )
                    }
                  >
                    <Plus className="mr-1 size-3" />
                    Add Package
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleViewUsers(client.id, client.name)}
                  >
                    <Users className="mr-1 size-3" />
                    View Users
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleUsageReport(client.id, client.name)}
                  >
                    <Package className="mr-1 size-3" />
                    Usage Report
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>

      <AddPackage
        clientName={addPackageDialog.clientName}
        currentPackages={addPackageDialog.currentPackages}
        isOpen={addPackageDialog.isOpen}
        onOpenChange={open =>
          setAddPackageDialog(prev => ({ ...prev, isOpen: open }))
        }
      />
    </div>
  );
};

export default ClientLicenseAllocation;
