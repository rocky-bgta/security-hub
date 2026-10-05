import { Check, Loader2 } from 'lucide-react';
import { FormEvent, useState } from 'react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
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
import { Textarea } from 'common/Textarea';

interface IProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const AddNewMSP = ({ open, onOpenChange }: IProps) => {
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [isSuccess, setIsSuccess] = useState<boolean>(false);
  const [selectedPackages, setSelectedPackages] = useState<Array<string>>([]);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    // Simulate API call
    setTimeout(() => {
      setIsSubmitting(false);
      setIsSuccess(true);

      // Reset success state after showing success message
      setTimeout(() => {
        setIsSuccess(false);
        onOpenChange(false);
      }, 1500);
    }, 1500);
  };

  const togglePackage = (packageId: string) => {
    if (selectedPackages.includes(packageId)) {
      setSelectedPackages(selectedPackages.filter(id => id !== packageId));
    } else {
      setSelectedPackages([...selectedPackages, packageId]);
    }
  };

  const packageOptions = [
    { id: 'asat', name: 'A-SAT Training' },
    { id: 'aphish', name: 'A-Phish' },
    { id: 'banking', name: 'Banking Module' },
    { id: 'hr', name: 'HR Module' },
    { id: 'java', name: 'Java Security' },
    { id: 'compliance', name: 'Compliance Training' },
  ];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="border-card-border bg-card sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle className="text-xl text-foreground">
            Add New MSP
          </DialogTitle>
          <DialogDescription className="text-muted-foreground">
            Add a new Managed Service Provider to the platform.
          </DialogDescription>
        </DialogHeader>

        {isSuccess ? (
          <div className="flex flex-col items-center justify-center space-y-4 py-8">
            <div className="flex size-12 items-center justify-center rounded-full bg-green-500/20">
              <Check className="size-6 text-green-500" />
            </div>
            <h3 className="text-xl font-medium text-foreground">
              MSP Added Successfully!
            </h3>
            <p className="text-center text-muted-foreground">
              The new MSP has been added to the system. You can now manage their
              licenses and clients.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="mspName" className="text-foreground">
                  MSP Name *
                </Label>
                <Input
                  id="mspName"
                  placeholder="Enter MSP organization name"
                  className="border-card-border bg-muted text-foreground"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="industry" className="text-foreground">
                  Industry *
                </Label>
                <Select required>
                  <SelectTrigger className="border-card-border bg-muted text-foreground">
                    <SelectValue placeholder="Select industry" />
                  </SelectTrigger>
                  <SelectContent className="border-card-border bg-secondary">
                    <SelectItem value="it">IT Services</SelectItem>
                    <SelectItem value="cybersecurity">Cybersecurity</SelectItem>
                    <SelectItem value="consulting">Consulting</SelectItem>
                    <SelectItem value="telecom">Telecommunications</SelectItem>
                    <SelectItem value="cloud">Cloud Services</SelectItem>
                    <SelectItem value="other">Other</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="contactPerson" className="text-foreground">
                  Contact Person *
                </Label>
                <Input
                  id="contactPerson"
                  placeholder="Enter primary contact name"
                  className="border-card-border bg-muted text-foreground"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="email" className="text-foreground">
                  Email Address *
                </Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="Enter email address"
                  className="border-card-border bg-muted text-foreground"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="phone" className="text-foreground">
                  Phone Number *
                </Label>
                <Input
                  id="phone"
                  placeholder="Enter phone number"
                  className="border-card-border bg-muted text-foreground"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="website" className="text-foreground">
                  Website
                </Label>
                <Input
                  id="website"
                  placeholder="Enter website URL"
                  className="border-card-border bg-muted text-foreground"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-foreground">Available Packages *</Label>
              <div className="mt-1 flex flex-wrap gap-2">
                {packageOptions.map(pkg => (
                  <Badge
                    key={pkg.id}
                    variant={
                      selectedPackages.includes(pkg.id) ? 'default' : 'outline'
                    }
                    className={`cursor-pointer ${
                      selectedPackages.includes(pkg.id)
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-background text-foreground hover:bg-muted'
                    }`}
                    onClick={() => togglePackage(pkg.id)}
                  >
                    {pkg.name}
                    {selectedPackages.includes(pkg.id) && (
                      <Check className="ml-1 size-3" />
                    )}
                  </Badge>
                ))}
              </div>
              {selectedPackages.length === 0 && (
                <p className="mt-1 text-xs text-red-400">
                  Please select at least one package
                </p>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="totalLicenses" className="text-foreground">
                  Initial License Allocation *
                </Label>
                <Input
                  id="totalLicenses"
                  type="number"
                  min="1"
                  placeholder="Enter number of licenses"
                  className="border-card-border bg-muted text-foreground"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="status" className="text-foreground">
                  Initial Status
                </Label>
                <Select defaultValue="active">
                  <SelectTrigger className="border-card-border bg-muted text-foreground">
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent className="border-card-border bg-secondary">
                    <SelectItem value="active">Active</SelectItem>
                    <SelectItem value="pending">Pending</SelectItem>
                    <SelectItem value="inactive">Inactive</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="address" className="text-foreground">
                Business Address
              </Label>
              <Textarea
                id="address"
                placeholder="Enter business address"
                className="border-card-border bg-muted text-foreground"
                rows={2}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="notes" className="text-foreground">
                Additional Notes
              </Label>
              <Textarea
                id="notes"
                placeholder="Enter any additional information about this MSP"
                className="border-card-border bg-muted text-foreground"
                rows={3}
              />
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
                className="border-card-border"
              >
                Cancel
              </Button>
              <Button
                type="submit"
                className="bg-primary text-primary-foreground"
                disabled={isSubmitting || selectedPackages.length === 0}
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" />
                    Adding MSP...
                  </>
                ) : (
                  'Add MSP'
                )}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default AddNewMSP;
