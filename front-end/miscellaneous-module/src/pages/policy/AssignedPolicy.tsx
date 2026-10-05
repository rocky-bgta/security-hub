import { Calendar, Edit, Eye, FileText, Filter } from 'lucide-react';
import { ChangeEvent, useState } from 'react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
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
import EditPolicy from 'features/policy/EditPolicy';

const assignedPolicies = [
  {
    id: '1',
    name: 'Data Protection Policy',
    type: 'Compliance',
    status: 'active',
    effectiveDate: '2024-01-01',
    expiryDate: '2025-01-01',
    assignedUsers: 'All Users',
    description: 'Comprehensive data protection and privacy policy',
  },
  {
    id: '2',
    name: 'Password Security Policy',
    type: 'Security',
    status: 'active',
    effectiveDate: '2024-01-15',
    expiryDate: '2024-12-31',
    assignedUsers: 'IT Department',
    description: 'Password requirements and security guidelines',
  },
  {
    id: '3',
    name: 'Remote Work Policy',
    type: 'Operational',
    status: 'inactive',
    effectiveDate: '2023-03-01',
    expiryDate: '2024-03-01',
    assignedUsers: 'Remote Workers',
    description: 'Guidelines for remote work and security practices',
  },
  {
    id: '4',
    name: 'Incident Response Policy',
    type: 'Security',
    status: 'active',
    effectiveDate: '2024-02-01',
    expiryDate: '2025-02-01',
    assignedUsers: 'Security Team',
    description: 'Procedures for handling security incidents',
  },
];

const AssignedPolicy = () => {
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [isEditPolicyOpen, setIsEditPolicyOpen] = useState<boolean>(false);
  const [selectedPolicy, setSelectedPolicy] = useState<any>(null);

  const filteredPolicies = assignedPolicies.filter(policy => {
    return statusFilter === 'all' || policy.status === statusFilter;
  });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'active':
        return <Badge variant="default">Active</Badge>;
      case 'inactive':
        return <Badge variant="secondary">Inactive</Badge>;
      case 'pending':
        return <Badge variant="secondary">Pending</Badge>;
      case 'approved':
        return <Badge variant="default">Approved</Badge>;
      case 'rejected':
        return <Badge variant="destructive">Rejected</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  const updateSelectedPolicy = (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => {
    const { id, value } = e.target;
    setSelectedPolicy({
      ...selectedPolicy,
      [id]: value,
    });
  };

  const handleEditPolicyRequest = () => {
    setIsEditPolicyOpen(false);
    // toast({
    //   title: 'Update Request Submitted',
    //   description:
    //     'Your policy update request has been sent to Super Admin for approval.',
    // });
  };

  const handleStatusChangeRequest = (policyId: string, newStatus: string) => {
    // toast({
    //   title: 'Status Change Requested',
    //   description: `Request to ${newStatus} policy has been sent to Super Admin for approval.`,
    // });
  };

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Policy Management
          </h1>
          <p className="text-muted-foreground">
            View assigned policies and manage policy requests
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="mb-2 flex items-center gap-2">
                <FileText className="size-5" />
                Assigned Policies ({filteredPolicies.length})
              </CardTitle>
              <CardDescription>
                Policies currently assigned to your organization
              </CardDescription>
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-40">
                <Filter className="mr-2 size-4" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="active">Active</SelectItem>
                <SelectItem value="inactive">Inactive</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Policy Name</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Effective Date</TableHead>
                <TableHead>Expiry Date</TableHead>
                <TableHead>Assigned To</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredPolicies.map(policy => (
                <TableRow key={policy.id}>
                  <TableCell>
                    <div>
                      <div className="font-medium">{policy.name}</div>
                      <div className="text-sm text-muted-foreground">
                        {policy.description}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>{policy.type}</TableCell>
                  <TableCell>{getStatusBadge(policy.status)}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1">
                      <Calendar className="size-3" />
                      {policy.effectiveDate}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1">
                      <Calendar className="size-3" />
                      {policy.expiryDate}
                    </div>
                  </TableCell>
                  <TableCell>{policy.assignedUsers}</TableCell>
                  <TableCell>
                    <div className="flex gap-1">
                      <Button
                        size="sm"
                        variant="outline"
                        className="border-transparent"
                      >
                        <Eye className="size-3" />
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="border-transparent"
                        onClick={() => {
                          setSelectedPolicy(policy);
                          setIsEditPolicyOpen(true);
                        }}
                      >
                        <Edit className="size-3" />
                      </Button>
                      <Button
                        size="sm"
                        variant={
                          policy.status === 'active' ? 'destructive' : 'default'
                        }
                        onClick={() =>
                          handleStatusChangeRequest(
                            policy.id,
                            policy.status === 'active'
                              ? 'deactivate'
                              : 'activate',
                          )
                        }
                      >
                        {policy.status === 'active' ? 'Deactivate' : 'Activate'}
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <EditPolicy
        isEditPolicyOpen={isEditPolicyOpen}
        setIsEditPolicyOpen={setIsEditPolicyOpen}
        selectedPolicy={selectedPolicy}
        handleEditPolicyRequest={handleEditPolicyRequest}
        handleChangeValue={updateSelectedPolicy}
      />
    </div>
  );
};

export default AssignedPolicy;
