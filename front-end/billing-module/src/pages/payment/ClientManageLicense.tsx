import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'common/Dialog';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import {
  Download,
  Edit,
  Eye,
  Filter,
  Minus,
  Plus,
  Search,
  Shield,
} from 'lucide-react';
import { useState } from 'react';

export const clientLicenses = [
  {
    id: 1,
    clientName: 'Alpha Tech',
    totalLicenses: 150,
    usedLicenses: 120,
    remainingLicenses: 30,
    licenseType: 'Premium',
    expiryDate: '2024-12-31',
    status: 'Active',
  },
  {
    id: 2,
    clientName: 'Beta Systems',
    totalLicenses: 100,
    usedLicenses: 85,
    remainingLicenses: 15,
    licenseType: 'Basic',
    expiryDate: '2024-06-30',
    status: 'Active',
  },
  {
    id: 3,
    clientName: 'Gamma Corp',
    totalLicenses: 200,
    usedLicenses: 180,
    remainingLicenses: 20,
    licenseType: 'Enterprise',
    expiryDate: '2024-09-30',
    status: 'Active',
  },
  {
    id: 4,
    clientName: 'Delta Inc',
    totalLicenses: 75,
    usedLicenses: 50,
    remainingLicenses: 25,
    licenseType: 'Standard',
    expiryDate: '2024-03-31',
    status: 'Expiring Soon',
  },
];

const ClientManageLicense = () => {
  const [licenseFilter, setLicenseFilter] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');

  const filteredLicenses = clientLicenses.filter(license => {
    const matchesSearch = license.clientName
      .toLowerCase()
      .includes(searchQuery.toLowerCase());
    const matchesFilter =
      licenseFilter === 'all' ||
      license.status.toLowerCase().includes(licenseFilter.toLowerCase());
    return matchesSearch && matchesFilter;
  });

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return 'bg-green-100 text-green-800 border-green-300';
      case 'expiring soon':
        return 'bg-orange-100 text-orange-800 border-orange-300';
      case 'expired':
        return 'bg-red-100 text-red-800 border-red-300';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-300';
    }
  };

  const handleAllocateLicenses = (
    clientId: number,
    additionalLicenses: number,
  ) => {
    // Handle license allocation logic here
    console.log(
      `Allocating ${additionalLicenses} licenses to client ID: ${clientId}`,
    );
  };

  const handleRemoveLicenses = (clientId: number, licensesToRemove: number) => {
    // Handle license removal logic here
    console.log(
      `Removing ${licensesToRemove} licenses from client ID: ${clientId}`,
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          Manage Client Licenses
        </h1>
        <Button variant="outline">
          <Download className="mr-2 size-4" />
          Export Report
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Shield className="size-5" />
            License Management Overview
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
                <Input
                  placeholder="Search by client name..."
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <Select value={licenseFilter} onValueChange={setLicenseFilter}>
              <SelectTrigger className="h-10 w-[280px] rounded-md">
                <div className="flex items-center gap-2">
                  <Filter className="mr-2 size-4" />
                  <SelectValue placeholder="Filter by status" />
                </div>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Licenses</SelectItem>
                <SelectItem value="active">Active</SelectItem>
                <SelectItem value="expiring">Expiring Soon</SelectItem>
                <SelectItem value="expired">Expired</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Client Name</TableHead>
                  <TableHead>Total Licenses</TableHead>
                  <TableHead>Used Licenses</TableHead>
                  <TableHead>Remaining</TableHead>
                  <TableHead>License Type</TableHead>
                  <TableHead>Expiry Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredLicenses.map(license => (
                  <TableRow key={license.id}>
                    <TableCell className="font-medium">
                      {license.clientName}
                    </TableCell>
                    <TableCell className="font-semibold">
                      {license.totalLicenses}
                    </TableCell>
                    <TableCell>{license.usedLicenses}</TableCell>
                    <TableCell className="font-semibold text-green-600">
                      {license.remainingLicenses}
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">{license.licenseType}</Badge>
                    </TableCell>
                    <TableCell>{license.expiryDate}</TableCell>
                    <TableCell>
                      <Badge className={getStatusColor(license.status)}>
                        {license.status}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Dialog>
                          <DialogTrigger asChild>
                            <Button variant="ghost" size="sm">
                              <Eye className="mr-2 size-4" />
                              View
                            </Button>
                          </DialogTrigger>
                          <DialogContent>
                            <DialogHeader>
                              <DialogTitle>
                                License Details - {license.clientName}
                              </DialogTitle>
                            </DialogHeader>
                            <div className="space-y-4">
                              <div className="grid grid-cols-2 gap-4">
                                <div>
                                  <label className="text-sm font-medium">
                                    Client Name
                                  </label>
                                  <p className="text-sm text-muted-foreground">
                                    {license.clientName}
                                  </p>
                                </div>
                                <div>
                                  <label className="text-sm font-medium">
                                    License Type
                                  </label>
                                  <p className="text-sm text-muted-foreground">
                                    {license.licenseType}
                                  </p>
                                </div>
                                <div>
                                  <label className="text-sm font-medium">
                                    Total Allocated
                                  </label>
                                  <p className="text-sm text-muted-foreground">
                                    {license.totalLicenses}
                                  </p>
                                </div>
                                <div>
                                  <label className="text-sm font-medium">
                                    Currently Used
                                  </label>
                                  <p className="text-sm text-muted-foreground">
                                    {license.usedLicenses}
                                  </p>
                                </div>
                                <div>
                                  <label className="text-sm font-medium">
                                    Remaining
                                  </label>
                                  <p className="text-sm text-green-600">
                                    {license.remainingLicenses}
                                  </p>
                                </div>
                                <div>
                                  <label className="text-sm font-medium">
                                    Expiry Date
                                  </label>
                                  <p className="text-sm text-muted-foreground">
                                    {license.expiryDate}
                                  </p>
                                </div>
                              </div>
                              <div>
                                <label className="text-sm font-medium">
                                  Status
                                </label>
                                <div className="mt-1">
                                  <Badge
                                    className={getStatusColor(license.status)}
                                  >
                                    {license.status}
                                  </Badge>
                                </div>
                              </div>
                              <div className="flex gap-2 pt-4">
                                <Button
                                  onClick={() =>
                                    handleAllocateLicenses(license.id, 10)
                                  }
                                  className="bg-green-600 hover:bg-green-700"
                                >
                                  <Plus className="mr-2 size-4" />
                                  Allocate Licenses
                                </Button>
                                <Button
                                  onClick={() =>
                                    handleRemoveLicenses(license.id, 5)
                                  }
                                  variant="destructive"
                                >
                                  <Minus className="mr-2 size-4" />
                                  Remove Licenses
                                </Button>
                                <Button variant="outline">
                                  <Edit className="mr-2 size-4" />
                                  Edit Details
                                </Button>
                              </div>
                            </div>
                          </DialogContent>
                        </Dialog>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ClientManageLicense;
