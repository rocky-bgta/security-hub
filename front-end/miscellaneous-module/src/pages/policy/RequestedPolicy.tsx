import { Clock, Filter, Plus } from 'lucide-react';
import { useState } from 'react';

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
import RequestNewPolicy from 'features/policy/RequestNewPolicy';

const policyRequests = [
  {
    id: '1',
    type: 'Add New Policy',
    policyName: 'Social Media Policy',
    description: 'Guidelines for social media usage by employees',
    requestDate: '2024-01-10',
    status: 'pending',
    requestedBy: 'John Doe',
  },
  {
    id: '2',
    type: 'Update Policy',
    policyName: 'Password Security Policy',
    description: 'Update password complexity requirements',
    requestDate: '2024-01-08',
    status: 'approved',
    requestedBy: 'Jane Smith',
  },
  {
    id: '3',
    type: 'Status Change',
    policyName: 'Remote Work Policy',
    description: 'Request to reactivate remote work policy',
    requestDate: '2024-01-05',
    status: 'rejected',
    requestedBy: 'Bob Johnson',
  },
];

const RequestedPolicy = () => {
  const [requestStatus, setRequestStatus] = useState<string>('all');
  const [isAddPolicyOpen, setIsAddPolicyOpen] = useState<boolean>(false);

  const filteredRequests = policyRequests.filter(request => {
    return requestStatus === 'all' || request.status === requestStatus;
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

  const handleAddPolicyRequest = () => {
    setIsAddPolicyOpen(false);
    // toast({
    //   title: 'Policy Request Submitted',
    //   description:
    //     'Your request for a new policy has been sent to Super Admin for approval.',
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
        <Button onClick={() => setIsAddPolicyOpen(true)}>
          <Plus className="mr-2 size-4" />
          Request New Policy
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="mb-2 flex items-center gap-2">
                <Clock className="size-5" />
                Policy Requests ({filteredRequests.length})
              </CardTitle>
              <CardDescription>Track status of policy requests</CardDescription>
            </div>
            <Select value={requestStatus} onValueChange={setRequestStatus}>
              <SelectTrigger className="w-40">
                <Filter className="mr-2 size-4" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="pending">Pending</SelectItem>
                <SelectItem value="approved">Approved</SelectItem>
                <SelectItem value="rejected">Rejected</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Request Type</TableHead>
                <TableHead>Policy Name</TableHead>
                <TableHead>Description</TableHead>
                <TableHead>Request Date</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Requested By</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredRequests.map(request => (
                <TableRow key={request.id}>
                  <TableCell className="font-medium">{request.type}</TableCell>
                  <TableCell>{request.policyName}</TableCell>
                  <TableCell className="max-w-64 truncate text-sm text-muted-foreground">
                    {request.description}
                  </TableCell>
                  <TableCell>{request.requestDate}</TableCell>
                  <TableCell>{getStatusBadge(request.status)}</TableCell>
                  <TableCell>{request.requestedBy}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <RequestNewPolicy
        isAddPolicyOpen={isAddPolicyOpen}
        setIsAddPolicyOpen={setIsAddPolicyOpen}
        handleAddPolicyRequest={handleAddPolicyRequest}
      />
    </div>
  );
};

export default RequestedPolicy;
