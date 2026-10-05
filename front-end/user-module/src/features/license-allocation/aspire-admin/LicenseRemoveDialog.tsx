import { Minus } from 'lucide-react';

import { Button } from 'common/Button';
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
  selectedMSP: { name: string; totalLicenses: number } | null;
  licenseAmount: number;
  setLicenseAmount: (amount: number) => void;
  onSubmit: () => void;
}

const LicenseRemoveDialog = ({
  isOpen,
  onClose,
  selectedMSP,
  licenseAmount,
  setLicenseAmount,
  onSubmit,
}: IProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Remove Licenses - {selectedMSP?.name}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <Label htmlFor="removeLicenseAmount">
              Number of Licenses to Remove
            </Label>
            <Input
              id="removeLicenseAmount"
              type="number"
              value={licenseAmount}
              onChange={e => setLicenseAmount(Number(e.target.value))}
              min="1"
              max={selectedMSP?.totalLicenses}
              placeholder="Enter number of licenses"
            />
            <div className="mt-1 text-sm text-muted-foreground">
              Current total: {selectedMSP?.totalLicenses} licenses
            </div>
          </div>

          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button
              onClick={onSubmit}
              variant="destructive"
              disabled={licenseAmount <= 0}
            >
              <Minus className="mr-1 size-4" />
              Remove Licenses
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default LicenseRemoveDialog;
