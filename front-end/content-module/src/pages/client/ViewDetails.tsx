import { Avatar, AvatarFallback } from 'common/Avatar';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card } from 'common/Card';
import CustomCheckbox from 'common/CustomCheckbox';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import CreateAndAssignProduct from 'features/package/sub-package/create-sub-package/CreateAndAssignProduct';
import { Eye, Plus, Shield, Users } from 'lucide-react';
import { IAssignedLicense } from 'models/License';
import { Fragment, useEffect, useState } from 'react';
import { routes } from 'routes/Routes';

const clientData = {
  id: 'CLT001',
  name: 'Bangladesh Bank',
  msp: 'TechSecure Solutions',
  contactPerson: 'Dr. Ahmed Rahman',
  email: 'ahmed@bangladeshbank.org',
  phone: '+880-2-9530100',
  status: 'Active',
  users: 1250,
  totalLicenses: 1500,
  usedLicenses: 1180,
  packages: ['A-SAT', 'Banking', 'A-Phish'],
  joinDate: '2023-04-10',
  lastActivity: '2024-01-15 11:45 AM',
  completionRate: 85,
  activeCampaigns: 8,
  industry: 'Banking',
};

const packagesByProduct = {
  'A-SAT': [
    {
      bundleId: 'asat-1',
      bundleName: 'A-SAT Basic Pack',
      totalLicenses: 100,
      assignedLicenses: 50,
      availableLicenses: 50,
      startDate: '2024-01-01',
      expireDate: '2024-12-31',
    },
    {
      bundleId: 'asat-2',
      bundleName: 'A-SAT Advanced Pack',
      totalLicenses: 200,
      assignedLicenses: 120,
      availableLicenses: 80,
      startDate: '2024-01-01',
      expireDate: '2024-12-31',
    },
  ],
  Banking: [
    {
      bundleId: 'bank-1',
      bundleName: 'Banking Security Pack',
      totalLicenses: 150,
      assignedLicenses: 100,
      availableLicenses: 50,
      startDate: '2024-01-01',
      expireDate: '2024-12-31',
    },
  ],
  'A-Phish': [
    {
      bundleId: 'phish-1',
      bundleName: 'A-Phish Professional',
      totalLicenses: 75,
      assignedLicenses: 30,
      availableLicenses: 45,
      startDate: '2024-01-01',
      expireDate: '2024-12-31',
    },
    {
      bundleId: 'phish-2',
      bundleName: 'A-Phish Enterprise',
      totalLicenses: 100,
      assignedLicenses: 60,
      availableLicenses: 40,
      startDate: '2024-01-01',
      expireDate: '2024-12-31',
    },
  ],
};
const ClientViewDetails = () => {
  const [selectedProduct, setSelectedProduct] = useState<string>('');
  const [isUserAssignDialogOpen, setIsUserAssignDialogOpen] =
    useState<boolean>(false);

  useEffect(() => {
    setSelectedProduct(clientData.packages[0]);
  }, []);

  // Get packages for the selected product
  const selectedProductPackages = selectedProduct
    ? packagesByProduct[selectedProduct as keyof typeof packagesByProduct] || []
    : [];

  const handleProductSelect = (productName: string) => {
    setSelectedProduct(productName);
  };

  const handleAssignProduct = () => {
    setIsUserAssignDialogOpen(true);
  };

  return (
    <Fragment>
      <div>
        <h1 className="content-text-3xl content-font-bold content-text-foreground">
          Client Management
        </h1>
        <p className="content-text-muted-foreground">
          Manage and monitor all client organizations
        </p>
      </div>
      <Card className="content-mt-6 content-space-y-4 content-p-6">
        <div className="content-flex content-items-start content-justify-between">
          <div className="content-flex content-items-center content-gap-4">
            <Avatar className="content-size-12">
              <AvatarFallback className="content-bg-primary content-text-black">
                {clientData.name
                  .split(' ')
                  .map(n => n[0])
                  .join('')
                  .slice(0, 2)}
              </AvatarFallback>
            </Avatar>
            <div>
              <div className="content-flex content-items-center content-gap-2">
                <h3 className="content-text-lg content-font-semibold content-text-foreground">
                  {clientData.name}
                </h3>
                <Badge
                  variant={
                    clientData.status === 'Active' ? 'default' : 'secondary'
                  }
                  className={
                    clientData.status === 'Active'
                      ? 'content-bg-primary/10 content-text-primary'
                      : 'content-bg-orange-500/10 content-text-orange-400'
                  }
                >
                  {clientData.status}
                </Badge>
                <Badge variant="outline" className="content-text-xs">
                  {clientData.industry}
                </Badge>
              </div>
              <p className="content-text-sm content-text-muted-foreground">
                ID: {clientData.id} • MSP: {clientData.msp}
              </p>
              <p className="content-text-sm content-text-foreground">
                {clientData.contactPerson}
              </p>
              <p className="content-text-sm content-text-muted-foreground">
                {clientData.email} • {clientData.phone}
              </p>
            </div>
          </div>
        </div>

        <div className="content-grid content-grid-cols-2 content-gap-4 md:content-grid-cols-5">
          <div className="content-flex content-items-center content-gap-2">
            <Users className="content-size-4 content-text-primary" />
            <div>
              <p className="content-text-sm content-font-medium content-text-foreground">
                {clientData.users.toLocaleString()}
              </p>
              <p className="content-text-xs content-text-muted-foreground">
                Users
              </p>
            </div>
          </div>
          <div className="content-flex content-items-center content-gap-2">
            <Shield className="content-size-4 content-text-primary" />
            <div>
              <p className="content-text-sm content-font-medium content-text-foreground">
                {clientData.usedLicenses}/{clientData.totalLicenses}
              </p>
              <p className="content-text-xs content-text-muted-foreground">
                Licenses
              </p>
            </div>
          </div>
        </div>

        <div>
          <p className="content-mb-3 content-text-sm content-font-medium content-text-foreground">
            Product List
          </p>
          <div className="content-mt-3 content-flex content-flex-wrap content-gap-6">
            {clientData.packages.map(pkg => (
              <div
                key={pkg}
                className="content-relative content-flex content-cursor-pointer content-items-center content-gap-2"
                onClick={() => handleProductSelect(pkg)}
              >
                <CustomCheckbox
                  variant="exam"
                  checked={selectedProduct === pkg}
                  onChange={() => handleProductSelect(pkg)}
                />
                <Badge
                  variant="outline"
                  className={
                    selectedProduct === pkg
                      ? 'content-border-primary content-bg-primary/10 content-text-primary'
                      : ''
                  }
                >
                  {pkg}
                </Badge>
              </div>
            ))}
          </div>
        </div>

        {/* Show packages only when a product is selected */}
        {selectedProduct && (
          <div>
            <div>
              <h4 className="content-mb-3 content-font-medium content-text-ash-gray">
                {selectedProduct} Packages ({selectedProductPackages?.length})
              </h4>
              {selectedProductPackages?.length > 0 ? (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Package Name</TableHead>
                      <TableHead>Total License</TableHead>
                      <TableHead>Assigned License</TableHead>
                      <TableHead>Available License</TableHead>
                      <TableHead>Start Date</TableHead>
                      <TableHead>Expire Date</TableHead>
                      <TableHead className="content-flex content-items-center content-justify-center">
                        Action
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {selectedProductPackages?.map((pkg, pkgIndex) => (
                      <TableRow key={pkg.bundleId}>
                        <TableCell>{pkg.bundleName}</TableCell>
                        <TableCell>{pkg.totalLicenses}</TableCell>
                        <TableCell>{pkg.assignedLicenses}</TableCell>
                        <TableCell>{pkg.availableLicenses}</TableCell>
                        <TableCell>{pkg.startDate}</TableCell>
                        <TableCell>{pkg.expireDate}</TableCell>
                        <TableCell className="content-flex content-justify-center content-gap-2">
                          <Button variant="outline">
                            <Eye className="content-size-4" />
                          </Button>
                          <Button
                            onClick={() => handleAssignProduct()}
                            variant="default"
                          >
                            <Plus className="content-size-4" /> Create Sub
                            package
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : (
                <p className="content-text-sm content-text-gray-500">
                  No packages available for {selectedProduct}
                </p>
              )}
            </div>
          </div>
        )}
      </Card>
      {selectedProduct && (
        <CreateAndAssignProduct
          product={selectedProduct as unknown as IAssignedLicense}
          isOpen={isUserAssignDialogOpen}
          onClose={() => setIsUserAssignDialogOpen(false)}
        />
      )}
    </Fragment>
  );
};

export default ClientViewDetails;
