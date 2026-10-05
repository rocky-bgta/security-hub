import { Package } from 'lucide-react';
import { useState } from 'react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';

const availablePackages = [
  {
    id: 'asat',
    name: 'A-SAT',
    description: 'Advanced Security Awareness Training',
    price: '$15',
    available: 250,
    features: ['Interactive modules', 'Progress tracking', 'Certificates'],
  },
  {
    id: 'aphish',
    name: 'A-Phish',
    description: 'Advanced Phishing Simulation',
    price: '$12',
    available: 200,
    features: ['Email templates', 'Landing pages', 'Analytics'],
  },
  {
    id: 'hr',
    name: 'HR Training',
    description: 'Human Resources Security Training',
    price: '$10',
    available: 150,
    features: ['Policy training', 'Compliance modules', 'Assessments'],
  },
  {
    id: 'banking',
    name: 'Banking Module',
    description: 'Banking Industry Specific Training',
    price: '$20',
    available: 50,
    features: [
      'Regulatory compliance',
      'Industry scenarios',
      'Risk assessment',
    ],
  },
  {
    id: 'java',
    name: 'Java Security',
    description: 'Java Application Security Training',
    price: '$18',
    available: 100,
    features: ['Code security', 'Vulnerability detection', 'Best practices'],
  },
  {
    id: 'cpp',
    name: 'C++ Security',
    description: 'C++ Application Security Training',
    price: '$18',
    available: 75,
    features: ['Memory management', 'Secure coding', 'Code review'],
  },
];

interface AddPackageDialogProps {
  clientName: string;
  currentPackages: string[];
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
}

const AddPackage = ({
  clientName,
  currentPackages,
  isOpen,
  onOpenChange,
}: AddPackageDialogProps) => {
  const [selectedPackages, setSelectedPackages] = useState<string[]>([]);
  const [packageAllocations, setPackageAllocations] = useState<
    Record<string, number>
  >({});
  const [isLoading, setIsLoading] = useState(false);

  const availableToAdd = availablePackages.filter(
    pkg => !currentPackages.includes(pkg.name),
  );

  const handlePackageToggle = (packageId: string, checked: boolean) => {
    if (checked) {
      setSelectedPackages([...selectedPackages, packageId]);
      setPackageAllocations(prev => ({ ...prev, [packageId]: 10 })); // Default allocation
    } else {
      setSelectedPackages(selectedPackages.filter(id => id !== packageId));
      setPackageAllocations(prev => {
        const newAllocations = { ...prev };
        delete newAllocations[packageId];
        return newAllocations;
      });
    }
  };

  const handleAllocationChange = (packageId: string, value: number) => {
    setPackageAllocations(prev => ({ ...prev, [packageId]: value }));
  };

  const handleAddPackages = async () => {
    setIsLoading(true);
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    setIsLoading(false);

    // Reset form
    setSelectedPackages([]);
    setPackageAllocations({});
    onOpenChange(false);
  };

  const totalCost = selectedPackages.reduce((total, packageId) => {
    const pkg = availablePackages.find(p => p.id === packageId);
    const allocation = packageAllocations[packageId] || 0;
    return (
      total + allocation * Number.parseInt(pkg?.price.replace('$', '') || '0')
    );
  }, 0);

  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[80vh] max-w-4xl overflow-y-auto border-card-border bg-card">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-foreground">
            <Package className="size-5" />
            Add Packages to {clientName}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-6">
          {availableToAdd.length === 0 ? (
            <div className="py-8 text-center">
              <Package className="mx-auto mb-4 size-12 text-muted-foreground" />
              <p className="font-medium text-foreground">
                All Available Packages Already Added
              </p>
              <p className="text-muted-foreground">
                This client has access to all available packages.
              </p>
            </div>
          ) : (
            <>
              <div className="grid gap-4">
                {availableToAdd.map(pkg => (
                  <div
                    key={pkg.id}
                    className={`rounded-lg border p-4 transition-all ${
                      selectedPackages.includes(pkg.id)
                        ? 'border-primary bg-primary/5'
                        : 'border-card-border bg-card'
                    }`}
                  >
                    <div className="flex items-start gap-4">
                      <Checkbox
                        id={pkg.id}
                        checked={selectedPackages.includes(pkg.id)}
                        onCheckedChange={checked =>
                          handlePackageToggle(pkg.id, checked as boolean)
                        }
                        className="mt-1"
                      />

                      <div className="flex-1">
                        <div className="mb-2 flex items-center justify-between">
                          <div>
                            <h4 className="font-medium text-foreground">
                              {pkg.name}
                            </h4>
                            <p className="text-sm text-muted-foreground">
                              {pkg.description}
                            </p>
                          </div>
                          <div className="text-right">
                            <Badge variant="outline" className="mb-1">
                              {pkg.price}/license
                            </Badge>
                            <p className="text-xs text-muted-foreground">
                              {pkg.available} available
                            </p>
                          </div>
                        </div>

                        <div className="mb-3 flex flex-wrap gap-1">
                          {pkg.features.map(feature => (
                            <Badge
                              key={feature}
                              variant="secondary"
                              className="text-xs"
                            >
                              {feature}
                            </Badge>
                          ))}
                        </div>

                        {selectedPackages.includes(pkg.id) && (
                          <div className="flex items-center gap-4 rounded-lg bg-muted p-3">
                            <Label
                              htmlFor={`allocation-${pkg.id}`}
                              className="text-foreground"
                            >
                              License Allocation:
                            </Label>
                            <Input
                              id={`allocation-${pkg.id}`}
                              type="number"
                              min="1"
                              max={pkg.available}
                              value={packageAllocations[pkg.id] || 0}
                              onChange={e =>
                                handleAllocationChange(
                                  pkg.id,
                                  Number.parseInt(e.target.value) || 0,
                                )
                              }
                              className="w-24 border-card-border bg-background text-foreground"
                            />
                            <span className="text-sm text-muted-foreground">
                              Cost: $
                              {(
                                (packageAllocations[pkg.id] || 0) *
                                Number.parseInt(pkg.price.replace('$', ''))
                              ).toLocaleString()}
                            </span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {selectedPackages.length > 0 && (
                <div className="rounded-lg bg-muted p-4">
                  <h4 className="mb-3 font-medium text-foreground">
                    Order Summary
                  </h4>
                  <div className="space-y-2">
                    {selectedPackages.map(packageId => {
                      const pkg = availablePackages.find(
                        p => p.id === packageId,
                      );
                      const allocation = packageAllocations[packageId] || 0;
                      const cost =
                        allocation *
                        Number.parseInt(pkg?.price.replace('$', '') || '0');
                      return (
                        <div
                          key={packageId}
                          className="flex justify-between text-sm"
                        >
                          <span className="text-foreground">
                            {pkg?.name} × {allocation}
                          </span>
                          <span className="text-foreground">
                            ${cost.toLocaleString()}
                          </span>
                        </div>
                      );
                    })}
                    <hr className="border-card-border" />
                    <div className="flex justify-between font-medium">
                      <span className="text-foreground">Total Cost:</span>
                      <span className="text-primary">
                        ${totalCost.toLocaleString()}
                      </span>
                    </div>
                  </div>
                </div>
              )}

              <div className="flex gap-2">
                <Button
                  onClick={handleAddPackages}
                  disabled={selectedPackages.length === 0 || isLoading}
                  className="flex-1 bg-primary text-primary-foreground"
                >
                  {isLoading
                    ? 'Adding Packages...'
                    : `Add ${selectedPackages.length} Package${selectedPackages.length !== 1 ? 's' : ''}`}
                </Button>
                <Button variant="outline" onClick={() => onOpenChange(false)}>
                  Cancel
                </Button>
              </div>
            </>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default AddPackage;
