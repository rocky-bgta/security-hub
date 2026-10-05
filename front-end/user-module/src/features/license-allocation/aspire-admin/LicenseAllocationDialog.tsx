import { ArrowLeft, ArrowRight, FileText } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedMSP: {
    name: string;
    email: string;
    tier: string;
  } | null;
  licenseAmount: number;
  setLicenseAmount: (amount: number) => void;
  onSubmit: () => void;
}

interface Product {
  id: string;
  name: string;
  description: string;
  category: string;
}

interface Package {
  id: string;
  productId: string;
  name: string;
  description: string;
  licenseType: string;
  maxLicenses: number;
  price: number;
  defaultValidityPeriod: number;
}

const mockProducts: Product[] = [
  {
    id: '1',
    name: 'Security Suite Pro',
    description:
      'Comprehensive security solution with advanced threat detection',
    category: 'Security',
  },
  {
    id: '2',
    name: 'Compliance Manager',
    description: 'Complete compliance management and reporting platform',
    category: 'Compliance',
  },
  {
    id: '3',
    name: 'Training Platform',
    description: 'Interactive security awareness training platform',
    category: 'Training',
  },
];

const mockPackages: Package[] = [
  // Security Suite Pro packages
  {
    id: '1',
    productId: '1',
    name: 'Basic Security Package',
    description: 'Essential security features for small teams',
    licenseType: 'Basic',
    maxLicenses: 50,
    price: 25,
    defaultValidityPeriod: 12,
  },
  {
    id: '2',
    productId: '1',
    name: 'Premium Security Package',
    description: 'Advanced security features with extended monitoring',
    licenseType: 'Premium',
    maxLicenses: 200,
    price: 45,
    defaultValidityPeriod: 12,
  },
  {
    id: '3',
    productId: '1',
    name: 'Enterprise Security Package',
    description: 'Full enterprise security suite with 24/7 support',
    licenseType: 'Enterprise',
    maxLicenses: 1000,
    price: 75,
    defaultValidityPeriod: 12,
  },
  // Compliance Manager packages
  {
    id: '4',
    productId: '2',
    name: 'Standard Compliance Package',
    description: 'Basic compliance tracking and reporting',
    licenseType: 'Standard',
    maxLicenses: 100,
    price: 30,
    defaultValidityPeriod: 12,
  },
  {
    id: '5',
    productId: '2',
    name: 'Advanced Compliance Package',
    description: 'Comprehensive compliance management with automation',
    licenseType: 'Advanced',
    maxLicenses: 500,
    price: 55,
    defaultValidityPeriod: 12,
  },
  // Training Platform packages
  {
    id: '6',
    productId: '3',
    name: 'Basic Training Package',
    description: 'Essential training modules and progress tracking',
    licenseType: 'Basic',
    maxLicenses: 100,
    price: 20,
    defaultValidityPeriod: 12,
  },
  {
    id: '7',
    productId: '3',
    name: 'Premium Training Package',
    description: 'Advanced training with custom content and analytics',
    licenseType: 'Premium',
    maxLicenses: 500,
    price: 35,
    defaultValidityPeriod: 12,
  },
];

const LicenseAllocationDialog = ({
  isOpen,
  onClose,
  selectedMSP,
  licenseAmount,
  setLicenseAmount,
  onSubmit,
}: IProps) => {
  // New state for allocation flow
  const [allocationStep, setAllocationStep] = useState<
    'product' | 'package' | 'details' | 'invoice'
  >('product');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [selectedPackage, setSelectedPackage] = useState<Package | null>(null);
  const [expirationDate, setExpirationDate] = useState<string>('');
  const [customValidityPeriod, setCustomValidityPeriod] = useState<string>('');

  // New handlers for allocation flow
  const handleProductSelect = (product: Product) => {
    setSelectedProduct(product);
    setSelectedPackage(null);
    setAllocationStep('package');
  };

  const handlePackageSelect = (pkg: Package) => {
    setSelectedPackage(pkg);
    setCustomValidityPeriod(pkg.defaultValidityPeriod.toString());
    setAllocationStep('details');

    // Calculate expiration date based on validity period
    const currentDate = new Date();
    currentDate.setMonth(currentDate.getMonth() + pkg.defaultValidityPeriod);
    setExpirationDate(currentDate.toISOString().split('T')[0]);
  };

  const handleNextStep = () => {
    if (allocationStep === 'details') {
      setAllocationStep('invoice');
    }
  };

  const handlePreviousStep = () => {
    if (allocationStep === 'package') {
      setAllocationStep('product');
      setSelectedProduct(null);
    } else if (allocationStep === 'details') {
      setAllocationStep('package');
      setSelectedPackage(null);
    } else if (allocationStep === 'invoice') {
      setAllocationStep('details');
    }
  };

  const handleFinalAllocate = () => {
    if (!selectedMSP || !selectedPackage || licenseAmount <= 0) return;

    const totalCost = licenseAmount * selectedPackage.price;

    toast.success(
      `Licenses Successfully Allocated: ${licenseAmount} ${selectedPackage.name} licenses allocated to ${selectedMSP.name}. Invoice generated for $${totalCost.toFixed(2)}.`,
    );

    setAllocationStep('product');
    setSelectedProduct(null);
    setSelectedPackage(null);
    setLicenseAmount(0);
    setCustomValidityPeriod('');
    setExpirationDate('');

    onSubmit();
  };

  const getAvailablePackages = () => {
    return selectedProduct
      ? mockPackages.filter(pkg => pkg.productId === selectedProduct.id)
      : [];
  };

  const handleClose = () => {
    setAllocationStep('product');
    setSelectedProduct(null);
    setSelectedPackage(null);
    setLicenseAmount(0);
    setCustomValidityPeriod('');
    setExpirationDate('');

    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="max-w-4xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            Allocate Licenses - {selectedMSP?.name}
            <Badge variant="outline" className="ml-2">
              Step{' '}
              {allocationStep === 'product'
                ? '1'
                : allocationStep === 'package'
                  ? '2'
                  : allocationStep === 'details'
                    ? '3'
                    : '4'}{' '}
              of 4
            </Badge>
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-6">
          {/* Step 1: Product Selection */}
          {allocationStep === 'product' && (
            <div className="space-y-4">
              <h3 className="text-lg font-medium">Select Product</h3>
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {mockProducts.map(product => (
                  <Card
                    key={product.id}
                    className={`cursor-pointer transition-all hover:shadow-md ${
                      selectedProduct?.id === product.id
                        ? 'ring-2 ring-primary'
                        : ''
                    }`}
                    onClick={() => handleProductSelect(product)}
                  >
                    <CardContent className="p-4">
                      <div className="space-y-2">
                        <Badge variant="secondary">{product.category}</Badge>
                        <h4 className="font-medium">{product.name}</h4>
                        <p className="text-sm text-muted-foreground">
                          {product.description}
                        </p>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          )}

          {/* Step 2: Package Selection */}
          {allocationStep === 'package' && selectedProduct && (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handlePreviousStep}
                >
                  <ArrowLeft className="mr-1 size-4" />
                  Back
                </Button>
                <h3 className="text-lg font-medium">
                  Select Package for {selectedProduct.name}
                </h3>
              </div>
              <div className="grid gap-4">
                {getAvailablePackages().map(pkg => (
                  <Card
                    key={pkg.id}
                    className={`cursor-pointer transition-all hover:shadow-md ${
                      selectedPackage?.id === pkg.id
                        ? 'ring-2 ring-primary'
                        : ''
                    }`}
                    onClick={() => handlePackageSelect(pkg)}
                  >
                    <CardContent className="p-4">
                      <div className="flex items-start justify-between">
                        <div className="space-y-2">
                          <div className="flex items-center gap-2">
                            <h4 className="font-medium">{pkg.name}</h4>
                            <Badge variant="outline">{pkg.licenseType}</Badge>
                          </div>
                          <p className="text-sm text-muted-foreground">
                            {pkg.description}
                          </p>
                          <div className="flex gap-4 text-sm">
                            <span>Max Licenses: {pkg.maxLicenses}</span>
                            <span>
                              Validity: {pkg.defaultValidityPeriod} months
                            </span>
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="text-lg font-bold text-primary">
                            ${pkg.price}
                          </div>
                          <div className="text-sm text-muted-foreground">
                            per license
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          )}

          {/* Step 3: License Details */}
          {allocationStep === 'details' && selectedPackage && (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handlePreviousStep}
                >
                  <ArrowLeft className="mr-1 size-4" />
                  Back
                </Button>
                <h3 className="text-lg font-medium">
                  License Allocation Details
                </h3>
              </div>

              <Card>
                <CardContent className="p-4">
                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <Label>Selected Package</Label>
                      <div className="mt-1 rounded bg-muted p-2">
                        <div className="font-medium">
                          {selectedPackage.name}
                        </div>
                        <div className="text-sm text-muted-foreground">
                          {selectedPackage.description}
                        </div>
                      </div>
                    </div>

                    <div>
                      <Label htmlFor="licenseAmount">Number of Licenses</Label>
                      <Input
                        id="licenseAmount"
                        type="number"
                        value={licenseAmount}
                        onChange={e => setLicenseAmount(Number(e.target.value))}
                        min="1"
                        max={selectedPackage.maxLicenses}
                        placeholder="Enter number of licenses"
                      />
                    </div>

                    <div>
                      <Label htmlFor="validityPeriod">
                        Validity Period (months)
                      </Label>
                      <Input
                        id="validityPeriod"
                        type="number"
                        value={customValidityPeriod}
                        onChange={e => {
                          setCustomValidityPeriod(e.target.value);
                          // Update expiration date when validity period changes
                          const currentDate = new Date();
                          currentDate.setMonth(
                            currentDate.getMonth() + Number(e.target.value),
                          );
                          setExpirationDate(
                            currentDate.toISOString().split('T')[0],
                          );
                        }}
                        min="1"
                        max="60"
                        placeholder="Enter validity period"
                      />
                      <div className="mt-1 text-sm text-muted-foreground">
                        Default: {selectedPackage.defaultValidityPeriod} months
                      </div>
                    </div>

                    <div>
                      <Label htmlFor="expirationDate">Expiration Date</Label>
                      <Input
                        id="expirationDate"
                        type="date"
                        value={expirationDate}
                        onChange={e => setExpirationDate(e.target.value)}
                      />
                    </div>
                  </div>
                </CardContent>
              </Card>

              <div className="flex justify-end">
                <Button
                  onClick={handleNextStep}
                  disabled={
                    licenseAmount <= 0 ||
                    !customValidityPeriod ||
                    !expirationDate
                  }
                >
                  Generate Invoice
                  <ArrowRight className="ml-1 size-4" />
                </Button>
              </div>
            </div>
          )}

          {/* Step 4: Invoice Generation */}
          {allocationStep === 'invoice' &&
            selectedPackage &&
            licenseAmount > 0 && (
              <div className="space-y-4">
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={handlePreviousStep}
                  >
                    <ArrowLeft className="mr-1 size-4" />
                    Back
                  </Button>
                  <h3 className="text-lg font-medium">Invoice Summary</h3>
                </div>

                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <FileText className="size-5" />
                      License Allocation Invoice
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid gap-4 md:grid-cols-2">
                      <div>
                        <Label>MSP Details</Label>
                        <div className="mt-1 space-y-1">
                          <div className="font-medium">{selectedMSP?.name}</div>
                          <div className="text-sm text-muted-foreground">
                            {selectedMSP?.email}
                          </div>
                          <div className="text-sm text-muted-foreground">
                            Tier: {selectedMSP?.tier}
                          </div>
                        </div>
                      </div>

                      <div>
                        <Label>License Details</Label>
                        <div className="mt-1 space-y-1">
                          <div className="font-medium">
                            {selectedPackage.name}
                          </div>
                          <div className="text-sm text-muted-foreground">
                            Type: {selectedPackage.licenseType}
                          </div>
                          <div className="text-sm text-muted-foreground">
                            Quantity: {licenseAmount} licenses
                          </div>
                          <div className="text-sm text-muted-foreground">
                            Validity: {customValidityPeriod} months
                          </div>
                          <div className="text-sm text-muted-foreground">
                            Expires: {expirationDate}
                          </div>
                        </div>
                      </div>
                    </div>

                    <div className="border-t pt-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <div className="text-sm text-muted-foreground">
                            Unit Price
                          </div>
                          <div className="font-medium">
                            ${selectedPackage.price} per license
                          </div>
                        </div>
                        <div>
                          <div className="text-sm text-muted-foreground">
                            Quantity
                          </div>
                          <div className="font-medium">
                            {licenseAmount} licenses
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="text-sm text-muted-foreground">
                            Total Cost
                          </div>
                          <div className="text-2xl font-bold text-primary">
                            $
                            {(licenseAmount * selectedPackage.price).toFixed(2)}
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                <div className="flex justify-end gap-2">
                  <Button variant="outline" onClick={handleClose}>
                    Cancel
                  </Button>
                  <Button onClick={handleFinalAllocate}>
                    <FileText className="mr-1 size-4" />
                    Allocate Licenses & Generate Invoice
                  </Button>
                </div>
              </div>
            )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default LicenseAllocationDialog;
