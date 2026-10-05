import { Download, Minus, Plus, Search } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Progress } from 'common/Progress';
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
import LicenseAllocationDialog from 'features/license-allocation/aspire-admin/LicenseAllocationDialog';
import LicenseRemoveDialog from 'features/license-allocation/aspire-admin/LicenseRemoveDialog';

interface MSP {
  id: string;
  name: string;
  tier:
    | 'Authorized Partner'
    | 'Silver Partner'
    | 'Gold Partner'
    | 'Platinum Partner'
    | 'Elite Partner';
  status: 'Active' | 'Suspended' | 'Inactive';
  email: string;
  phone: string;
  totalLicenses: number;
  usedLicenses: number;
  availableCredit: number;
  balance: number;
  totalPayments: number;
  joinDate: string;
}

const mockMSPs: MSP[] = [
  {
    id: '1',
    name: 'TechFlow Solutions',
    tier: 'Gold Partner',
    status: 'Active',
    email: 'admin@techflow.com',
    phone: '+1-555-0123',
    totalLicenses: 100,
    usedLicenses: 75,
    availableCredit: 5000,
    balance: 2500,
    totalPayments: 45000,
    joinDate: '2023-01-15',
  },
  {
    id: '2',
    name: 'DataSecure Pro',
    tier: 'Platinum Partner',
    status: 'Active',
    email: 'contact@datasecure.com',
    phone: '+1-555-0456',
    totalLicenses: 250,
    usedLicenses: 200,
    availableCredit: 10000,
    balance: 0,
    totalPayments: 120000,
    joinDate: '2022-08-22',
  },
  {
    id: '3',
    name: 'CloudMaster Inc',
    tier: 'Silver Partner',
    status: 'Suspended',
    email: 'info@cloudmaster.com',
    phone: '+1-555-0789',
    totalLicenses: 50,
    usedLicenses: 45,
    availableCredit: 1000,
    balance: 3500,
    totalPayments: 25000,
    joinDate: '2023-03-10',
  },
  {
    id: '4',
    name: 'NetGuard Systems',
    tier: 'Elite Partner',
    status: 'Active',
    email: 'support@netguard.com',
    phone: '+1-555-0321',
    totalLicenses: 500,
    usedLicenses: 450,
    availableCredit: 25000,
    balance: 1200,
    totalPayments: 300000,
    joinDate: '2021-12-05',
  },
  {
    id: '5',
    name: 'SecureIT Partners',
    tier: 'Authorized Partner',
    status: 'Active',
    email: 'admin@secureit.com',
    phone: '+1-555-0654',
    totalLicenses: 25,
    usedLicenses: 20,
    availableCredit: 500,
    balance: 800,
    totalPayments: 12000,
    joinDate: '2023-06-18',
  },
];

const MSPManageLicenses = () => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [tierFilter, setTierFilter] = useState<string>('all');
  const [selectedMSP, setSelectedMSP] = useState<MSP | null>(null);
  const [licenseAction, setLicenseAction] = useState<
    'allocate' | 'remove' | null
  >(null);
  const [licenseAmount, setLicenseAmount] = useState<number>(0);

  const filteredMSPs = mockMSPs.filter(msp => {
    const matchesSearch = msp.name
      .toLowerCase()
      .includes(searchTerm.toLowerCase());
    const matchesTier = tierFilter === 'all' || msp.tier === tierFilter;
    return matchesSearch && matchesTier;
  });

  const handleAllocateLicenses = (msp: MSP) => {
    setSelectedMSP(msp);
    setLicenseAction('allocate');
    setLicenseAmount(0);
  };

  const handleRemoveLicenses = (msp: MSP) => {
    setSelectedMSP(msp);
    setLicenseAction('remove');
    setLicenseAmount(0);
  };

  const handleSubmitLicenseAction = () => {
    if (!selectedMSP || !licenseAction || licenseAmount <= 0) return;

    const actionText =
      licenseAction === 'allocate' ? 'allocated to' : 'removed from';
    toast.success(
      `Licenses ${licenseAction === 'allocate' ? 'Allocated' : 'Removed'}: ${licenseAmount} licenses ${actionText} ${selectedMSP.name} successfully.`,
    );

    // Reset form
    setSelectedMSP(null);
    setLicenseAction(null);
    setLicenseAmount(0);
  };

  const handleGenerateReport = () => {
    toast.info('License allocation report has been generated successfully.');
  };

  const getTotalLicenses = () => {
    return filteredMSPs.reduce((total, msp) => total + msp.totalLicenses, 0);
  };

  const getTotalUsedLicenses = () => {
    return filteredMSPs.reduce((total, msp) => total + msp.usedLicenses, 0);
  };

  const getLicenseUtilization = (msp: MSP) => {
    return msp.totalLicenses > 0
      ? (msp.usedLicenses / msp.totalLicenses) * 100
      : 0;
  };

  const getUtilizationColor = (utilization: number) => {
    if (utilization >= 90) return 'text-[#ef4444]';
    if (utilization >= 70) return 'text-[#facc15]';
    return 'text-[#22c55e]';
  };

  const getStatusVariant = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return 'bg-[#22c55e] text-[#f8fafc]';
      case 'completed':
        return 'bg-[#22c55e] text-[#f8fafc]';
      case 'pending':
        return 'bg-[#facc15] text-[#0f172a]';
      case 'overdue':
        return 'bg-[#ef4444] text-[#f8fafc]';
      case 'failed':
        return 'bg-[#ef4444] text-[#f8fafc]';
      case 'suspended':
        return 'bg-[#ef4444] text-[#f8fafc]';
      case 'inactive':
        return 'bg-muted text-muted-foreground';
      default:
        return 'bg-[#0ea5e9] text-[#f8fafc]';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2>Manage Licenses</h2>
          <p className="text-muted-foreground">
            Allocate, remove, and track license usage for all MSPs
          </p>
        </div>
        <Button onClick={handleGenerateReport}>
          <Download className="mr-2 size-4" />
          Generate Report
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Total Licenses
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {getTotalLicenses().toLocaleString()}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Used Licenses</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-[#facc15]">
              {getTotalUsedLicenses().toLocaleString()}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Available Licenses
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-[#22c55e]">
              {(getTotalLicenses() - getTotalUsedLicenses()).toLocaleString()}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Utilization Rate
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {getTotalLicenses() > 0
                ? Math.round(
                    (getTotalUsedLicenses() / getTotalLicenses()) * 100,
                  )
                : 0}
              %
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>MSP License Management</CardTitle>
            <div className="flex items-center gap-2">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search MSPs..."
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                  className="w-64 pl-9"
                />
              </div>
              <Select value={tierFilter} onValueChange={setTierFilter}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Filter by tier" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Tiers</SelectItem>
                  <SelectItem value="Authorized Partner">
                    Authorized Partner
                  </SelectItem>
                  <SelectItem value="Silver Partner">Silver Partner</SelectItem>
                  <SelectItem value="Gold Partner">Gold Partner</SelectItem>
                  <SelectItem value="Platinum Partner">
                    Platinum Partner
                  </SelectItem>
                  <SelectItem value="Elite Partner">Elite Partner</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>MSP Name</TableHead>
                <TableHead>Tier</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Total Licenses</TableHead>
                <TableHead>Used Licenses</TableHead>
                <TableHead>Available</TableHead>
                <TableHead>Utilization</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredMSPs.map(msp => {
                const utilization = getLicenseUtilization(msp);

                return (
                  <TableRow key={msp.id}>
                    <TableCell className="font-medium">{msp.name}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{msp.tier}</Badge>
                    </TableCell>
                    <TableCell>
                      <Badge className={getStatusVariant(msp.status)}>
                        {msp.status}
                      </Badge>
                    </TableCell>
                    <TableCell>{msp.totalLicenses.toLocaleString()}</TableCell>
                    <TableCell>{msp.usedLicenses.toLocaleString()}</TableCell>
                    <TableCell className="font-medium text-[#22c55e]">
                      {(msp.totalLicenses - msp.usedLicenses).toLocaleString()}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <Progress value={utilization} className="w-16" />
                        <span
                          className={`text-sm font-medium ${getUtilizationColor(utilization)}`}
                        >
                          {Math.round(utilization)}%
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleAllocateLicenses(msp)}
                          className="text-[#22c55e] hover:text-[#22c55e]"
                        >
                          <Plus className="mr-1 size-4" />
                          Allocate
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleRemoveLicenses(msp)}
                          className="text-[#ef4444] hover:text-[#ef4444]"
                        >
                          <Minus className="mr-1 size-4" />
                          Remove
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <LicenseAllocationDialog
        isOpen={licenseAction === 'allocate'}
        onClose={() => setLicenseAction(null)}
        selectedMSP={selectedMSP}
        licenseAmount={licenseAmount}
        setLicenseAmount={setLicenseAmount}
        onSubmit={handleSubmitLicenseAction}
      />

      <LicenseRemoveDialog
        isOpen={licenseAction === 'remove'}
        onClose={() => setLicenseAction(null)}
        selectedMSP={selectedMSP}
        licenseAmount={licenseAmount}
        setLicenseAmount={setLicenseAmount}
        onSubmit={handleSubmitLicenseAction}
      />
    </div>
  );
};

export default MSPManageLicenses;
