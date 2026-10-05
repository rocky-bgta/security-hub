import React from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Button } from 'components/common/Button';
import { Badge } from 'components/common/Badge';
import { Input } from 'components/common/Input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import { Search, Edit, Eye, Trash2, Download, Plus } from 'lucide-react';

const mockTickets = [
  {
    ticketId: 'TKT-001',
    title: 'Login Authentication Issue',
    clientName: 'Acme Corp',
    assignedTo: 'John Doe',
    priority: 'High',
    status: 'Open',
    createdDate: '2024-01-15',
  },
  {
    ticketId: 'TKT-002',
    title: 'Certificate Download Problem',
    clientName: 'Tech Solutions',
    assignedTo: 'Jane Smith',
    priority: 'Medium',
    status: 'In Progress',
    createdDate: '2024-01-14',
  },
  {
    ticketId: 'TKT-003',
    title: 'Payment Processing Error',
    clientName: 'Global Industries',
    assignedTo: 'Mike Johnson',
    priority: 'High',
    status: 'Open',
    createdDate: '2024-01-13',
  },
  {
    ticketId: 'TKT-004',
    title: 'Course Content Access',
    clientName: 'Startup Inc',
    assignedTo: 'Sarah Wilson',
    priority: 'Low',
    status: 'In Progress',
    createdDate: '2024-01-12',
  },
  {
    ticketId: 'TKT-005',
    title: 'Exam Results Not Displaying',
    clientName: 'Enterprise Ltd',
    assignedTo: 'John Doe',
    priority: 'Medium',
    status: 'Closed',
    createdDate: '2024-01-11',
  },
  {
    ticketId: 'TKT-006',
    title: 'User Account Suspension',
    clientName: 'Small Business Co',
    assignedTo: 'Jane Smith',
    priority: 'High',
    status: 'Open',
    createdDate: '2024-01-10',
  },
  {
    ticketId: 'TKT-007',
    title: 'Billing Inquiry',
    clientName: 'Consulting Group',
    assignedTo: 'Mike Johnson',
    priority: 'Low',
    status: 'Closed',
    createdDate: '2024-01-09',
  },
];

const TicketLibrary = () => {
  const getPriorityColor = (priority: string) => {
    switch (priority.toLowerCase()) {
      case 'high':
        return 'destructive';
      case 'medium':
        return 'default';
      case 'low':
        return 'secondary';
      default:
        return 'secondary';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'open':
        return 'default';
      case 'in progress':
        return 'secondary';
      case 'closed':
        return 'outline';
      default:
        return 'secondary';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Ticket Library</h1>
          <p className="text-muted-foreground">
            Manage and track all support tickets from clients and users
          </p>
        </div>
        <Button>
          <Plus className="mr-2 size-4" />
          Create Ticket
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>Filter tickets by various criteria</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4">
            <div className="flex-1">
              <Input
                placeholder="Search tickets by title or ID..."
                className="w-full"
              />
            </div>
            <Select defaultValue="all">
              <SelectTrigger className="w-48">
                <SelectValue placeholder="Filter by Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="open">Open</SelectItem>
                <SelectItem value="in-progress">In Progress</SelectItem>
                <SelectItem value="closed">Closed</SelectItem>
              </SelectContent>
            </Select>
            <Select defaultValue="all">
              <SelectTrigger className="w-48">
                <SelectValue placeholder="Filter by Priority" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Priority</SelectItem>
                <SelectItem value="high">High</SelectItem>
                <SelectItem value="medium">Medium</SelectItem>
                <SelectItem value="low">Low</SelectItem>
              </SelectContent>
            </Select>
            <Button variant="outline">
              <Search className="mr-2 size-4" />
              Search
            </Button>
            <Button variant="outline">
              <Download className="mr-2 size-4" />
              Export
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Ticket Statistics */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Tickets</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">127</div>
            <p className="text-xs text-muted-foreground">+5 from yesterday</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Open Tickets</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">34</div>
            <p className="text-xs text-muted-foreground">26.8% of total</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">In Progress</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">28</div>
            <p className="text-xs text-muted-foreground">22.0% of total</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Resolved Today
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">12</div>
            <p className="text-xs text-muted-foreground">+3 from yesterday</p>
          </CardContent>
        </Card>
      </div>

      {/* Ticket List */}
      <Card>
        <CardHeader>
          <CardTitle>Support Tickets</CardTitle>
          <CardDescription>
            Complete list of all support tickets
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ticket ID</TableHead>
                <TableHead>Title</TableHead>
                <TableHead>Client Name</TableHead>
                <TableHead>Assigned To</TableHead>
                <TableHead>Priority</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created Date</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {mockTickets.map((ticket, index) => (
                <TableRow key={index}>
                  <TableCell className="font-mono text-sm">
                    {ticket.ticketId}
                  </TableCell>
                  <TableCell className="max-w-xs truncate font-medium">
                    {ticket.title}
                  </TableCell>
                  <TableCell>{ticket.clientName}</TableCell>
                  <TableCell>{ticket.assignedTo}</TableCell>
                  <TableCell>
                    <Badge variant={getPriorityColor(ticket.priority)}>
                      {ticket.priority}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant={getStatusColor(ticket.status)}>
                      {ticket.status}
                    </Badge>
                  </TableCell>
                  <TableCell>{ticket.createdDate}</TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm">
                        <Eye className="size-4" />
                      </Button>
                      <Button variant="outline" size="sm">
                        <Edit className="size-4" />
                      </Button>
                      <Button variant="outline" size="sm">
                        <Trash2 className="size-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default TicketLibrary;
