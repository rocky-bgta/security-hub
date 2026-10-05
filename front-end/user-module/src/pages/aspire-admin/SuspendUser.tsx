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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { StatusBadge } from 'common/StatusBadge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { Textarea } from 'common/Textarea';
import {
  AlertTriangle,
  Download,
  Edit,
  Eye,
  Search,
  UserCheck,
  UserX,
} from 'lucide-react';
import { useState } from 'react';

interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  status: 'Active' | 'Inactive' | 'Suspended';
  department: string;
  lastLogin: string;
  msp: string;
  client: string;
  phone: string;
  permissions: string[];
}

const mockUsers: User[] = [
  {
    id: '1',
    name: 'John Doe',
    email: 'john.doe@acme.com',
    role: 'Manager',
    status: 'Active',
    department: 'Sales',
    lastLogin: '2024-01-15 09:30:00',
    msp: 'TechFlow Solutions',
    client: 'Acme Corporation',
    phone: '+1 (555) 123-4567',
    permissions: ['Dashboard Access', 'Report Generation'],
  },
  {
    id: '2',
    name: 'Jane Smith',
    email: 'jane.smith@stellar.com',
    role: 'User',
    status: 'Active',
    department: 'Support',
    lastLogin: '2024-01-14 16:45:00',
    msp: 'Digital Nexus Inc',
    client: 'Stellar Enterprises',
    phone: '+1 (555) 234-5678',
    permissions: ['Dashboard Access'],
  },
  {
    id: '3',
    name: 'Bob Johnson',
    email: 'bob.johnson@fusion.com',
    role: 'Admin',
    status: 'Suspended',
    department: 'Operations',
    lastLogin: '2024-01-10 11:20:00',
    msp: 'CloudTech Masters',
    client: 'Fusion Systems',
    phone: '+1 (555) 345-6789',
    permissions: ['Full Access', 'User Management'],
  },
  {
    id: '4',
    name: 'Alice Brown',
    email: 'alice.brown@quantum.com',
    role: 'User',
    status: 'Inactive',
    department: 'Engineering',
    lastLogin: '2024-01-12 14:15:00',
    msp: 'Data Dynamics LLC',
    client: 'Quantum Solutions',
    phone: '+1 (555) 456-7890',
    permissions: ['Dashboard Access', 'Task Management'],
  },
];

const SuspendUser = () => {
  const [users, setUsers] = useState(mockUsers);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [editUser, setEditUser] = useState<User | null>(null);
  const [suspensionReason, setSuspensionReason] = useState('');

  const filteredUsers = users.filter(user => {
    const matchesSearch =
      user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.department.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus =
      statusFilter === 'all' || user.status.toLowerCase() === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleSuspendUser = (userId: string, reason: string) => {
    setUsers(
      users.map(user =>
        user.id === userId ? { ...user, status: 'Suspended' as const } : user,
      ),
    );

    // toast({
    //   title: 'User Suspended',
    //   description:
    //     'User has been suspended successfully. They will no longer have access to the system.',
    // });

    setSuspensionReason('');
  };

  const handleReactivateUser = (userId: string) => {
    setUsers(
      users.map(user =>
        user.id === userId ? { ...user, status: 'Active' as const } : user,
      ),
    );

    // toast({
    //   title: 'User Reactivated',
    //   description: 'User has been reactivated and can now access the system.',
    // });
  };

  const handleSaveEdit = () => {
    if (editUser) {
      setUsers(users.map(user => (user.id === editUser.id ? editUser : user)));

      //   toast({
      //     title: 'User Updated',
      //     description: 'User details have been successfully updated.',
      //   });

      setEditUser(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Suspend Client User
          </h1>
          <p className="text-muted-foreground">
            Manage Client User access and suspension status
          </p>
        </div>
        <Button variant="outline">
          <Download className="mr-2 size-4" />
          Export Users
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <UserX className="size-5" />
            Client User Management
          </CardTitle>
          <CardDescription>
            View, edit, suspend, and reactivate Client Users
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Filters */}
          <div className="mb-6 flex flex-col gap-4 md:flex-row">
            <div className="flex-1">
              <Label htmlFor="search">Search Users</Label>
              <div className="relative mt-1">
                <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
                <Input
                  id="search"
                  placeholder="Search by name, email, or department..."
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>

            <div className="md:w-48">
              <Label htmlFor="status-filter">Filter by Status</Label>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="mt-1">
                  <SelectValue placeholder="All statuses" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Statuses</SelectItem>
                  <SelectItem value="active">Active</SelectItem>
                  <SelectItem value="inactive">Inactive</SelectItem>
                  <SelectItem value="suspended">Suspended</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Users Table */}

          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>User</TableHead>
                <TableHead>Role</TableHead>
                <TableHead>Department</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>MSP/Client</TableHead>
                <TableHead>Last Login</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredUsers.map(user => (
                <TableRow key={user.id}>
                  <TableCell>
                    <div>
                      <div className="font-medium">{user.name}</div>
                      <div className="text-sm text-muted-foreground">
                        {user.email}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">{user.role}</Badge>
                  </TableCell>
                  <TableCell>{user.department}</TableCell>
                  <TableCell>
                    <StatusBadge status={user.status} />
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">
                      <div className="font-medium">{user.msp}</div>
                      <div className="text-muted-foreground">{user.client}</div>
                    </div>
                  </TableCell>
                  <TableCell className="text-sm">{user.lastLogin}</TableCell>
                  <TableCell>
                    <div className="flex space-x-1">
                      {/* View Profile */}
                      <Dialog>
                        <DialogTrigger asChild>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setSelectedUser(user)}
                          >
                            <Eye className="size-4" />
                          </Button>
                        </DialogTrigger>
                        <DialogContent>
                          <DialogHeader>
                            <DialogTitle>User Profile</DialogTitle>
                            <DialogDescription>
                              View detailed user information
                            </DialogDescription>
                          </DialogHeader>
                          {selectedUser && (
                            <div className="space-y-4">
                              <div className="grid grid-cols-2 gap-4">
                                <div>
                                  <Label>Name</Label>
                                  <p className="text-sm font-medium text-foreground">
                                    {selectedUser.name}
                                  </p>
                                </div>
                                <div>
                                  <Label>Email</Label>
                                  <p className="text-sm font-medium text-foreground">
                                    {selectedUser.email}
                                  </p>
                                </div>
                                <div>
                                  <Label>Phone</Label>
                                  <p className="text-sm font-medium text-foreground">
                                    {selectedUser.phone}
                                  </p>
                                </div>
                                <div>
                                  <Label>Role</Label>
                                  <p className="text-sm font-medium text-foreground">
                                    {selectedUser.role}
                                  </p>
                                </div>
                                <div>
                                  <Label>Department</Label>
                                  <p className="text-sm font-medium text-foreground">
                                    {selectedUser.department}
                                  </p>
                                </div>
                                <div>
                                  <Label className="mr-2">Status</Label>
                                  <StatusBadge status={selectedUser.status} />
                                </div>
                                <div>
                                  <Label>Last Login</Label>
                                  <p className="text-sm font-medium text-foreground">
                                    {selectedUser.lastLogin}
                                  </p>
                                </div>
                                <div>
                                  <Label>MSP</Label>
                                  <p className="text-sm font-medium text-foreground">
                                    {selectedUser.msp}
                                  </p>
                                </div>
                              </div>
                              <div>
                                <Label>Permissions</Label>
                                <div className="mt-1 flex flex-wrap gap-1">
                                  {selectedUser.permissions.map(
                                    (permission, index) => (
                                      <Badge
                                        key={index}
                                        variant="secondary"
                                        className="text-xs"
                                      >
                                        {permission}
                                      </Badge>
                                    ),
                                  )}
                                </div>
                              </div>
                            </div>
                          )}
                        </DialogContent>
                      </Dialog>

                      {/* Edit User */}
                      <Dialog>
                        <DialogTrigger asChild>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setEditUser(user)}
                          >
                            <Edit className="size-4" />
                          </Button>
                        </DialogTrigger>
                        <DialogContent>
                          <DialogHeader>
                            <DialogTitle>Edit User</DialogTitle>
                            <DialogDescription>
                              Update user information and settings
                            </DialogDescription>
                          </DialogHeader>
                          {editUser && (
                            <div className="space-y-4">
                              <div className="grid grid-cols-2 gap-4">
                                <div>
                                  <Label htmlFor="edit-name">Name</Label>
                                  <Input
                                    id="edit-name"
                                    value={editUser.name}
                                    onChange={e =>
                                      setEditUser({
                                        ...editUser,
                                        name: e.target.value,
                                      })
                                    }
                                  />
                                </div>
                                <div>
                                  <Label htmlFor="edit-email">Email</Label>
                                  <Input
                                    id="edit-email"
                                    type="email"
                                    value={editUser.email}
                                    onChange={e =>
                                      setEditUser({
                                        ...editUser,
                                        email: e.target.value,
                                      })
                                    }
                                  />
                                </div>
                                <div>
                                  <Label htmlFor="edit-phone">Phone</Label>
                                  <Input
                                    id="edit-phone"
                                    value={editUser.phone}
                                    onChange={e =>
                                      setEditUser({
                                        ...editUser,
                                        phone: e.target.value,
                                      })
                                    }
                                  />
                                </div>
                                <div>
                                  <Label htmlFor="edit-role">Role</Label>
                                  <Select
                                    value={editUser.role}
                                    onValueChange={value =>
                                      setEditUser({
                                        ...editUser,
                                        role: value,
                                      })
                                    }
                                  >
                                    <SelectTrigger>
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="Admin">
                                        Admin
                                      </SelectItem>
                                      <SelectItem value="Manager">
                                        Manager
                                      </SelectItem>
                                      <SelectItem value="User">User</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                                <div>
                                  <Label htmlFor="edit-department">
                                    Department
                                  </Label>
                                  <Select
                                    value={editUser.department}
                                    onValueChange={value =>
                                      setEditUser({
                                        ...editUser,
                                        department: value,
                                      })
                                    }
                                  >
                                    <SelectTrigger>
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="Sales">
                                        Sales
                                      </SelectItem>
                                      <SelectItem value="Support">
                                        Support
                                      </SelectItem>
                                      <SelectItem value="Operations">
                                        Operations
                                      </SelectItem>
                                      <SelectItem value="Engineering">
                                        Engineering
                                      </SelectItem>
                                      <SelectItem value="Marketing">
                                        Marketing
                                      </SelectItem>
                                      <SelectItem value="Finance">
                                        Finance
                                      </SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                                <div>
                                  <Label htmlFor="edit-status">Status</Label>
                                  <Select
                                    value={editUser.status}
                                    onValueChange={value =>
                                      setEditUser({
                                        ...editUser,
                                        status: value as
                                          | 'Active'
                                          | 'Inactive'
                                          | 'Suspended',
                                      })
                                    }
                                  >
                                    <SelectTrigger>
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="Active">
                                        Active
                                      </SelectItem>
                                      <SelectItem value="Inactive">
                                        Inactive
                                      </SelectItem>
                                      <SelectItem value="Suspended">
                                        Suspended
                                      </SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                              </div>
                              <div className="flex justify-end space-x-2">
                                <Button
                                  variant="outline"
                                  onClick={() => setEditUser(null)}
                                >
                                  Cancel
                                </Button>
                                <Button onClick={handleSaveEdit}>
                                  Save Changes
                                </Button>
                              </div>
                            </div>
                          )}
                        </DialogContent>
                      </Dialog>

                      {/* Suspend/Reactivate User */}
                      {user.status === 'Active' ? (
                        <Dialog>
                          <DialogTrigger asChild>
                            <Button variant="ghost" size="sm">
                              <UserX className="size-4" />
                            </Button>
                          </DialogTrigger>
                          <DialogContent>
                            <DialogHeader>
                              <DialogTitle className="flex items-center gap-2">
                                <AlertTriangle className="size-5 text-orange-500" />
                                Suspend User
                              </DialogTitle>
                              <DialogDescription>
                                Are you sure you want to suspend {user.name}?
                                They will no longer have access to the system.
                              </DialogDescription>
                            </DialogHeader>
                            <div className="space-y-4">
                              <div>
                                <Label htmlFor="suspension-reason">
                                  Reason for Suspension (Optional)
                                </Label>
                                <Textarea
                                  id="suspension-reason"
                                  placeholder="Enter reason for suspension..."
                                  value={suspensionReason}
                                  onChange={e =>
                                    setSuspensionReason(e.target.value)
                                  }
                                />
                              </div>
                              <div className="flex justify-end space-x-2">
                                <Button variant="outline">Cancel</Button>
                                <Button
                                  variant="destructive"
                                  onClick={() =>
                                    handleSuspendUser(user.id, suspensionReason)
                                  }
                                >
                                  Suspend User
                                </Button>
                              </div>
                            </div>
                          </DialogContent>
                        </Dialog>
                      ) : user.status === 'Suspended' ? (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleReactivateUser(user.id)}
                        >
                          <UserCheck className="size-4" />
                        </Button>
                      ) : (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleReactivateUser(user.id)}
                        >
                          <UserCheck className="size-4" />
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          {filteredUsers.length === 0 && (
            <div className="py-8 text-center text-muted-foreground">
              No users found matching your criteria.
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default SuspendUser;
