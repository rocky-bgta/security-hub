import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Label } from 'common/Label';
import { Textarea } from 'common/Textarea';
import { AlertTriangle } from 'lucide-react';
import { useState } from 'react';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  actionType: 'suspend' | 'reactivate' | null;
  selectedMSP: {
    name: string;
    tier: string;
    status: string;
    joinDate: string;
    email: string;
    phone: string;
    totalLicenses: number;
    usedLicenses: number;
    availableCredit: number;
    balance: number;
  } | null;
  onClose: () => void;
  onConfirm: (msp: any) => void;
}

const SuspenseDialog = ({
  isOpen,
  actionType,
  setIsOpen,
  selectedMSP,
  onClose,
  onConfirm,
}: IProps) => {
  const [suspensionReason, setSuspensionReason] = useState<string>('');

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <AlertTriangle className="size-5 text-[#eab308]" />
            {actionType === 'suspend' ? 'Suspend MSP' : 'Reactivate MSP'}
          </DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Are you sure you want to {actionType}{' '}
            <strong>{selectedMSP?.name}</strong>?
          </p>

          {actionType === 'suspend' && (
            <div>
              <Label htmlFor="reason">Reason for suspension (optional)</Label>
              <Textarea
                id="reason"
                placeholder="Enter reason for suspension..."
                value={suspensionReason}
                onChange={e => setSuspensionReason(e.target.value)}
              />
            </div>
          )}

          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button
              onClick={onConfirm}
              className={
                actionType === 'suspend' ? 'bg-red-500 hover:bg-red-600' : ''
              }
            >
              {actionType === 'suspend' ? 'Suspend' : 'Reactivate'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default SuspenseDialog;
