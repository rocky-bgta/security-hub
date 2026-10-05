import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { useAPI } from 'hooks/UseAPI';
import { CreditCard, DollarSign } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface DepositModalProps {
  id: string;
  isOpen: boolean;
  onClose: () => void;
  customerName: string;
  onSubmit: () => void;
}

export const EditExpireDateModal = ({
  id,
  isOpen,
  onClose,
  customerName,
  onSubmit,
}: DepositModalProps) => {
  const [date, setDate] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const apiClient = useAPI();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!date) {
      toast.warning('Please enter a valid deposit date.');
      return;
    }

    setIsSubmitting(true);

    try {
      await apiClient.put(API_END_POINTS.CREDIT_UPDATE + id, {
        data: {
          expirationDate: date,
        },
      });
      toast.success('Expiration date updated successfully!');
      onSubmit();
      setDate('');
      onClose();
    } catch (error) {
      console.error('Error updating expiration date', error);
      toast.error('Failed to update expiration date');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setDate('');
      onClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <CreditCard className="size-5 text-primary" />
            Update Expiration Date
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="rounded-lg bg-secondary/50 p-4">
            <div className="mb-1 flex items-center gap-2 text-sm text-muted-foreground">
              <DollarSign className="size-4" />
              Expire Date Update for
            </div>
            <div className="font-semibold text-card-foreground">
              {customerName || 'Aspire Digital Ltd.'}
            </div>
          </div>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="date" className="text-sm font-medium">
                Date *
              </Label>
              <div>
                <Input
                  id="date"
                  type="date"
                  value={date}
                  onChange={e => setDate(e.target.value)}
                  required
                  disabled={isSubmitting}
                  min={
                    new Date(new Date().setMonth(new Date().getMonth() + 1))
                      .toISOString()
                      .split('T')[0]
                  }
                  max={
                    new Date(
                      new Date().setFullYear(new Date().getFullYear() + 1),
                    )
                      .toISOString()
                      .split('T')[0]
                  }
                />
              </div>
            </div>
          </div>

          <div className="flex gap-3 pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isSubmitting}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isSubmitting || !date || new Date(date) <= new Date()}
              className="flex-1"
            >
              {isSubmitting ? (
                <div className="flex items-center gap-2">
                  <div className="size-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                  Updating...
                </div>
              ) : (
                'Update'
              )}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
