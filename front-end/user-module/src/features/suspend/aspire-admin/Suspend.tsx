import { Edit, Eye, Search, UserCheck, UserX } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
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
import EditDialog from 'features/edit/EditDialog';
import SuspenseDialog from 'features/edit/SuspenseDialog';
import ViewDialog from 'features/list/ViewDialog';

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

const AspireAdminSuspend = () => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [selectedMSP, setSelectedMSP] = useState<MSP | null>(null);
  const [isViewingDetails, setIsViewingDetails] = useState<boolean>(false);
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [showSuspensionDialog, setShowSuspensionDialog] =
    useState<boolean>(false);
  const [actionType, setActionType] = useState<'suspend' | 'reactivate' | null>(
    null,
  );

  const [editingMSP, setEditingMSP] = useState<MSP | null>(null);

  const filteredMSPs = mockMSPs.filter(msp => {
    const matchesSearch =
      msp.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      msp.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus =
      statusFilter === 'all' ||
      msp.status.toLowerCase() === statusFilter.toLowerCase();
    return matchesSearch && matchesStatus;
  });

  const handleViewDetails = (msp: MSP) => {
    setSelectedMSP(msp);
    setIsViewingDetails(true);
  };

  const handleEditMSP = (msp: MSP) => {
    setEditingMSP({ ...msp });
    setIsEditing(true);
  };

  const handleSaveEdit = () => {
    if (!editingMSP) return;

    toast.success(`${editingMSP.name} profile has been updated successfully.`);

    setIsEditing(false);
    setEditingMSP(null);
  };

  const handleSuspendMSP = (msp: MSP) => {
    setSelectedMSP(msp);
    setActionType('suspend');
    setShowSuspensionDialog(true);
  };

  const handleReactivateMSP = (msp: MSP) => {
    setSelectedMSP(msp);
    setActionType('reactivate');
    setShowSuspensionDialog(true);
  };

  const handleConfirmAction = () => {
    if (!selectedMSP || !actionType) return;

    const actionText = actionType === 'suspend' ? 'suspended' : 'reactivated';
    toast.success(`${selectedMSP.name} has been ${actionText} successfully.`);

    setShowSuspensionDialog(false);
    setSelectedMSP(null);
    setActionType(null);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getStatusCounts = () => {
    const active = filteredMSPs.filter(msp => msp.status === 'Active').length;
    const suspended = filteredMSPs.filter(
      msp => msp.status === 'Suspended',
    ).length;
    const inactive = filteredMSPs.filter(
      msp => msp.status === 'Inactive',
    ).length;
    return { active, suspended, inactive };
  };

  const statusCounts = getStatusCounts();

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
          <h2>MSP Management</h2>
          <p className="text-muted-foreground">
            View, edit, suspend, and reactivate MSP accounts
          </p>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Total MSPs</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {filteredMSPs.length}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Active MSPs</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-[#16a34a]">
              {statusCounts.active}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Suspended MSPs
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-[#ef4444]">
              {statusCounts.suspended}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Inactive MSPs</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-muted-foreground">
              {statusCounts.inactive}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>MSP List</CardTitle>
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
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-32">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="active">Active</SelectItem>
                  <SelectItem value="suspended">Suspended</SelectItem>
                  <SelectItem value="inactive">Inactive</SelectItem>
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
                <TableHead>Join Date</TableHead>
                <TableHead>Contact</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredMSPs.map(msp => (
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
                  <TableCell>{formatDate(msp.joinDate)}</TableCell>
                  <TableCell>{msp.email}</TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleViewDetails(msp)}
                      >
                        <Eye className="mr-1 size-4" />
                        View
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleEditMSP(msp)}
                      >
                        <Edit className="mr-1 size-4" />
                        Edit
                      </Button>
                      {msp.status === 'Active' ? (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleSuspendMSP(msp)}
                          className="text-[#ef4444] hover:text-[#ef4444]"
                        >
                          <UserX className="mr-1 size-4" />
                          Suspend
                        </Button>
                      ) : msp.status === 'Suspended' ? (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleReactivateMSP(msp)}
                          className="text-[#16a34a] hover:text-[#16a34a]"
                        >
                          <UserCheck className="mr-1 size-4" />
                          Reactivate
                        </Button>
                      ) : null}
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <ViewDialog
        isOpen={isViewingDetails}
        setIsOpen={setIsViewingDetails}
        selectedMSP={selectedMSP}
      />

      <EditDialog
        isOpen={isEditing}
        setIsOpen={setIsEditing}
        editingMSP={editingMSP}
        setEditingMSP={setEditingMSP}
        onClose={() => setIsEditing(false)}
        onSave={handleSaveEdit}
      />

      <SuspenseDialog
        isOpen={showSuspensionDialog}
        setIsOpen={setShowSuspensionDialog}
        onClose={() => setShowSuspensionDialog(false)}
        onConfirm={handleConfirmAction}
        actionType={actionType}
        selectedMSP={selectedMSP}
      />
    </div>
  );
};

export default AspireAdminSuspend;
