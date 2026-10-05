import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
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
import AssignSubPackage from 'features/package/sub-package/assign/AssignSubPackage';
import CreateAndAssignProduct from 'features/package/sub-package/create-sub-package/CreateAndAssignProduct';
import EditSubPackage from 'features/package/sub-package/edit-sub-package/EditSubPackage';
import ViewSubPackage from 'features/package/sub-package/View';
import {
  Edit,
  Eye,
  Filter,
  Package2,
  Plus,
  Search,
  Trash2,
  UserPlus,
} from 'lucide-react';
import { IAssignedLicense } from 'models/License';
import { useState } from 'react';
import { routes } from 'routes/Routes';

// Mock data for sub-packages
const mockSubPackages = [
  {
    id: '1',
    name: 'HR Onboarding Sub-Package',
    companyName: 'Acme Corp',
    product: 'Employee Onboarding Platform',
    topicCount: 12,
    createdBy: 'John Admin',
    createdOn: '2024-01-15',
    status: 'Active',
    lastUpdated: '2024-01-20',
  },
  {
    id: '2',
    name: 'Cybersecurity Basics',
    companyName: 'Aspire',
    product: 'Security Training Suite',
    topicCount: 8,
    createdBy: 'Jane Admin',
    createdOn: '2024-01-10',
    status: 'Active',
    lastUpdated: '2024-01-18',
  },
  {
    id: '3',
    name: 'Leadership Fundamentals',
    companyName: 'Ster Tech',
    product: 'Management Development Program',
    topicCount: 15,
    createdBy: 'Mike Admin',
    createdOn: '2024-01-05',
    status: 'Inactive',
    lastUpdated: '2024-01-15',
  },
  {
    id: '3',
    name: 'Leadership Two',
    companyName: 'Ster Tech',
    product: 'Management Development Program',
    topicCount: 15,
    createdBy: 'Mike Admin',
    createdOn: '2024-01-05',
    status: 'Expired',
    lastUpdated: '2024-01-15',
  },
];

enum modalTypes {
  CreateAndAssignProduct = 'create_and_assign_product',
  AssignSubPackage = 'assign_sub_package',
  ViewSubPackage = 'view_sub_package',
  EditSubPackage = 'edit_sub_package',
  None = 'none',
}

const IndividualClientSubPackages = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [productFilter, setProductFilter] = useState('all');
  const [isOpenModal, setIsOpenModal] = useState<modalTypes>(modalTypes.None);

  const filteredPackages = mockSubPackages.filter(pkg => {
    const matchesSearch = pkg.name
      .toLowerCase()
      .includes(searchTerm.toLowerCase());
    const matchesStatus =
      statusFilter === 'all' || pkg.status.toLowerCase() === statusFilter;
    const matchesProduct =
      productFilter === 'all' || pkg.product === productFilter;
    return matchesSearch && matchesStatus && matchesProduct;
  });

  const products = [...new Set(mockSubPackages.map(pkg => pkg.product))];

  const handleView = (id: string) => {
    setIsOpenModal(modalTypes.ViewSubPackage);
  };

  const handleEdit = (id: string) => {
    setIsOpenModal(modalTypes.EditSubPackage);
  };

  const handleDelete = (id: string, name: string) => {};

  const handleDuplicate = (id: string, name: string) => {};

  const handleToggleStatus = (
    id: string,
    currentStatus: string,
    name: string,
  ) => {
    const newStatus = currentStatus === 'Active' ? 'Inactive' : 'Active';

    // In a real app, this would make an API call to update the status
  };

  const handleAssignProduct = (id: string) => {
    setIsOpenModal(modalTypes.AssignSubPackage);
  };

  return (
    <div className="content-space-y-6">
      {/* Header */}
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-text-white dark:content-text-white">
            Sub Packages
          </h1>
          <p className="content-mt-2 content-text-gray-600 dark:content-text-gray-400">
            Manage and organize training content packages for your organization
          </p>
        </div>

        <Button
          onClick={() => setIsOpenModal(modalTypes.CreateAndAssignProduct)}
        >
          <Plus className="content-size-4" />
          Create Sub Package
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <Filter className="content-size-5" />
            Filters & Search
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="content-grid content-grid-cols-4 content-gap-4">
            <div className="content-col-span-2">
              <div className="content-relative">
                <Search className="content-absolute content-left-3 content-top-1/2 content-size-4 -content-translate-y-1/2 content-transform content-text-gray-400" />
                <Input
                  placeholder="Search sub-packages by name..."
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                  className="content-pl-10"
                />
              </div>
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger>
                <SelectValue placeholder="Filter by Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="active">Active</SelectItem>
                <SelectItem value="inactive">Inactive</SelectItem>
                <SelectItem value="expired">Expired</SelectItem>
              </SelectContent>
            </Select>
            <Select value={productFilter} onValueChange={setProductFilter}>
              <SelectTrigger>
                <SelectValue placeholder="Filter by Product" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Products</SelectItem>
                {products.map(product => (
                  <SelectItem key={product} value={product}>
                    {product}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Sub-Packages Table */}
      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <Package2 className="content-size-5" />
            Total Sub Packages ({filteredPackages.length})
          </CardTitle>
          <CardDescription>
            Manage your training sub-packages and assign them to users
          </CardDescription>
        </CardHeader>
        <CardContent className="content-p-0">
          <div className="content-overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>SL</TableHead>
                  <TableHead>Sub-Package Name</TableHead>
                  <TableHead>Product</TableHead>
                  <TableHead>Company</TableHead>
                  <TableHead>Topics</TableHead>
                  <TableHead>Created By</TableHead>
                  <TableHead>Created On</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Last Updated</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredPackages.map((pkg, index) => (
                  <TableRow key={pkg.id}>
                    <TableCell>{index + 1}</TableCell>
                    <TableCell>{pkg.name}</TableCell>
                    <TableCell>{pkg.product}</TableCell>
                    <TableCell>{pkg.companyName}</TableCell>
                    <TableCell>
                      <Badge variant="secondary">{pkg.topicCount} topics</Badge>
                    </TableCell>
                    <TableCell>{pkg.createdBy}</TableCell>
                    <TableCell>{pkg.createdOn}</TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          pkg.status === 'Active'
                            ? 'default'
                            : pkg.status === 'Expired'
                              ? 'destructive'
                              : 'secondary'
                        }
                      >
                        {pkg.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="content-text-gray-700 dark:content-text-gray-300">
                      {pkg.lastUpdated}
                    </TableCell>
                    <TableCell className="content-text-right">
                      <div className="content-flex content-items-center content-justify-end content-gap-2">
                        <Button
                          size="sm"
                          onClick={() => handleAssignProduct(pkg.id)}
                          className="content-text-white"
                        >
                          <UserPlus className="content-size-3" />
                          Assign Product
                        </Button>
                        {/* <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleDuplicate(pkg.id, pkg.name)}
                          title="Duplicate Sub-Package"
                        >
                          <Copy className="content-h-3 content-w-3" />
                        </Button> */}
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleView(pkg.id)}
                          title="View Details"
                        >
                          <Eye className="content-size-3" />
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleEdit(pkg.id)}
                          title="Edit Sub-Package"
                        >
                          <Edit className="content-size-3" />
                        </Button>

                        {/* <Button
                          size="sm"
                          variant="outline"
                          onClick={() =>
                            handleToggleStatus(pkg.id, pkg.status, pkg.name)
                          }
                          title={
                            pkg.status === 'Active' ? 'Deactivate' : 'Activate'
                          }
                        >
                          {pkg.status === 'Active' ? (
                            <ToggleLeft className="content-h-3 content-w-3" />
                          ) : (
                            <ToggleRight className="content-h-3 content-w-3" />
                          )}
                        </Button> */}
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleDelete(pkg.id, pkg.name)}
                          title="Delete Sub-Package"
                        >
                          <Trash2 className="content-size-3" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      {filteredPackages.length === 0 && (
        <Card>
          <CardContent className="!content-py-12 content-text-center">
            <Package2 className="content-mx-auto content-mb-4 content-size-12 content-text-primary" />
            <h3 className="content-mb-2 content-text-lg content-font-semibold content-text-white dark:content-text-white">
              No sub-packages found
            </h3>
            <p className="content-text-ash-gray">
              Try adjusting your search criteria or create a new sub-package.
            </p>

            <Button>
              <Plus className="content-size-4" />
              Create Sub-Package
            </Button>
          </CardContent>
        </Card>
      )}

      <CreateAndAssignProduct
        product={{} as IAssignedLicense}
        isOpen={isOpenModal === modalTypes.CreateAndAssignProduct}
        onClose={() => setIsOpenModal(modalTypes.None)}
      />

      <AssignSubPackage
        selectedSubPackage={''}
        refetch={() => {}}
        isOpen={isOpenModal === modalTypes.AssignSubPackage}
        onClose={() => setIsOpenModal(modalTypes.None)}
      />

      <ViewSubPackage
        selectedSubPackage={''}
        isOpen={isOpenModal === modalTypes.ViewSubPackage}
        onClose={() => setIsOpenModal(modalTypes.None)}
      />
      <EditSubPackage
        selectedSubPackage={''}
        isOpen={isOpenModal === modalTypes.EditSubPackage}
        onClose={() => setIsOpenModal(modalTypes.None)}
        refetch={() => {}}
      />
    </div>
  );
};

export default IndividualClientSubPackages;
