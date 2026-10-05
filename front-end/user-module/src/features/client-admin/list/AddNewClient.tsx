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
import { cn } from 'utils/Helper';

interface IProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const packageOptions = [
  { id: 'asat', name: 'A-SAT Training' },
  { id: 'aphish', name: 'A-Phish' },
  { id: 'banking', name: 'Banking Module' },
  { id: 'hr', name: 'HR Module' },
  { id: 'java', name: 'Java Security' },
  { id: 'compliance', name: 'Compliance Training' },
];

const mspOptions = [
  { id: 'msp1', name: 'TechSecure Solutions' },
  { id: 'msp2', name: 'CyberGuard Enterprise' },
  { id: 'msp3', name: 'SecureNet Partners' },
  { id: 'msp4', name: 'InfoShield Corp' },
];

const AddNewClient = ({ open, onOpenChange }: IProps) => {
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

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="border-card-border bg-card sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle className="text-xl text-foreground">
            Add New Client
          </DialogTitle>
          <DialogDescription className="text-muted-foreground">
            Quickly add a new client organization to the platform.
          </DialogDescription>
        </DialogHeader>

        {isSuccess ? (
          <div className="flex flex-col items-center justify-center space-y-4 py-8">
            <div className="flex size-12 items-center justify-center rounded-full bg-green-500/20">
              <Check className="size-6 text-green-500" />
            </div>
            <h3 className="text-xl font-medium text-foreground">
              Client Added Successfully!
            </h3>
            <p className="text-center text-muted-foreground">
              The new client has been added to the system. You can now manage
              their licenses and users.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="clientName" className="text-foreground">
                  Client Name *
                </Label>
                <Input
                  id="clientName"
                  placeholder="Enter client organization name"
                  className="border-card-border text-foreground"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="industry" className="text-foreground">
                  Industry *
                </Label>
                <Select required>
                  <SelectTrigger className="border-card-border text-foreground">
                    <SelectValue placeholder="Select industry" />
                  </SelectTrigger>
                  <SelectContent className="border-card-border bg-secondary">
                    <SelectItem value="banking">Banking & Finance</SelectItem>
                    <SelectItem value="healthcare">Healthcare</SelectItem>
                    <SelectItem value="government">Government</SelectItem>
                    <SelectItem value="education">Education</SelectItem>
                    <SelectItem value="technology">Technology</SelectItem>
                    <SelectItem value="manufacturing">Manufacturing</SelectItem>
                    <SelectItem value="retail">Retail</SelectItem>
                    <SelectItem value="telecom">Telecommunications</SelectItem>
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
                  className="border-card-border text-foreground"
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
                  className="border-card-border text-foreground"
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
                  className="border-card-border text-foreground"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="msp" className="text-foreground">
                  Assign MSP *
                </Label>
                <Select required>
                  <SelectTrigger className="border-card-border text-foreground">
                    <SelectValue placeholder="Select MSP" />
                  </SelectTrigger>
                  <SelectContent className="border-card-border bg-secondary">
                    {mspOptions.map(msp => (
                      <SelectItem key={msp.id} value={msp.id}>
                        {msp.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-foreground">Assign Packages *</Label>
              <div className="mt-1 flex flex-wrap gap-2">
                {packageOptions.map(pkg => (
                  <Badge
                    key={pkg.id}
                    variant={
                      selectedPackages.includes(pkg.id) ? 'default' : 'outline'
                    }
                    className={cn(
                      'cursor-pointer',
                      selectedPackages.includes(pkg.id)
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-background text-foreground',
                    )}
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
                  Total Licenses *
                </Label>
                <Input
                  id="totalLicenses"
                  type="number"
                  min="1"
                  placeholder="Enter number of licenses"
                  className="border-card-border text-foreground"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="status" className="text-foreground">
                  Initial Status
                </Label>
                <Select defaultValue="active">
                  <SelectTrigger className="border-card-border text-foreground">
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
              <Label htmlFor="notes" className="text-foreground">
                Additional Notes
              </Label>
              <Textarea
                id="notes"
                placeholder="Enter any additional information about this client"
                className="border-card-border text-foreground"
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
                    Adding Client...
                  </>
                ) : (
                  'Add Client'
                )}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default AddNewClient;
