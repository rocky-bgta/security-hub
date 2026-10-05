import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { ArrowRightLeft, Loader2 } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface CreditTransferModalProps {
  id?: string;
  amount: number;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
  fromClientId: string;
}

export interface CreditTransferData {
  toClientId: string;
  amount: number;
  reason: string;
}

interface ValidationErrors {
  toClientId: string;
  amount: string;
  reason: string;
}

export const CreditTransferModal = ({
  id,
  amount,
  isOpen,
  onClose,
  onSubmit,
  fromClientId,
}: CreditTransferModalProps) => {
  const [data, setData] = useState<CreditTransferData>({
    toClientId: '',
    amount: 0,
    reason: '',
  });

  const [errors, setErrors] = useState<ValidationErrors>({
    toClientId: '',
    amount: '',
    reason: '',
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [hasSubmitAttempt, setHasSubmitAttempt] = useState(false);

  const apiClient = useAPI();

  const validateClientId = (value: string) => {
    if (!value) {
      return 'Client ID is required';
    }
    if (value === fromClientId) {
      return 'Cannot transfer to same client';
    }
    return '';
  };

  const validateAmount = (value: number) => {
    if (value <= 0) {
      return 'Amount must be greater than 0';
    }
    if (value > amount * 0.5) {
      return 'Amount cannot exceed 50% of available credit';
    }
    return '';
  };

  const validateReason = (value: string) => {
    if (!value.trim()) {
      return 'Description is required';
    }
    if (value.length < 100) {
      return `Description must be at least 100 characters (currently ${value.length} characters)`;
    }
    if (value.length > 3000) {
      return `Description cannot exceed 3000 characters (currently ${value.length} characters)`;
    }
    return '';
  };

  const handleClientIdChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newClientId = e.target.value;
    setData({ ...data, toClientId: newClientId });
    setErrors({ ...errors, toClientId: validateClientId(newClientId) });
  };

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newAmount = parseFloat(e.target.value);
    setData({ ...data, amount: newAmount });
    setErrors({ ...errors, amount: validateAmount(newAmount) });
  };

  const handleReasonChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newReason = e.target.value;
    setData({ ...data, reason: newReason });
    if (hasSubmitAttempt) {
      setErrors({ ...errors, reason: validateReason(newReason) });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setHasSubmitAttempt(true);

    // Validate all fields before submission
    const newErrors = {
      toClientId: validateClientId(data.toClientId),
      amount: validateAmount(data.amount),
      reason: validateReason(data.reason),
    };

    setErrors(newErrors);

    // Check if there are any validation errors
    if (Object.values(newErrors).some(error => error)) {
      return;
    }

    setIsSubmitting(true);

    try {
      await apiClient.post(API_END_POINTS.CREDIT_TRANSFER, {
        data: {
          fromClientId: id,
          toClientId: data.toClientId,
          amount: data.amount,
          reason: data.reason,
        },
      });

      toast.success('Credit transfer successful');
      onSubmit();
      onClose();
    } catch (error) {
      console.error('Error:', error);
      toast.error('An error occurred while transferring credit');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setData({
        toClientId: '',
        amount: 0,
        reason: '',
      });
      setErrors({
        toClientId: '',
        amount: '',
        reason: '',
      });
      setHasSubmitAttempt(false);
      onClose();
    }
  };

  const formatCurrency = (value: string) => {
    const num = parseFloat(value);
    if (isNaN(num)) return '';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(num);
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <ArrowRightLeft className="size-5 text-primary" />
            Transfer Credit
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="toClientId">Client ID *</Label>
            <Input
              id="toClientId"
              placeholder="Enter destination client ID"
              value={data.toClientId}
              onChange={handleClientIdChange}
              disabled={isSubmitting}
            />
            {errors.toClientId && (
              <div className="text-sm text-red-500">{errors.toClientId}</div>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="amount">Amount *</Label>
            <Input
              id="amount"
              type="number"
              placeholder="0.00"
              value={data.amount}
              onChange={handleAmountChange}
              disabled={isSubmitting}
            />
            {data.amount > 0 && (
              <div className="text-sm text-muted-foreground">
                Available Credit:{' '}
                {formatCurrency(String(amount - Number(data.amount)))}
              </div>
            )}
            {errors.amount && (
              <div className="text-sm text-red-500">{errors.amount}</div>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="reason">Details * </Label>
            <Textarea
              id="reason"
              placeholder="Enter details for credit transfer (minimum 100 characters)"
              value={data.reason}
              onChange={handleReasonChange}
              disabled={isSubmitting}
              rows={3}
              maxLength={3000}
            />
            <div className="text-sm text-muted-foreground">
              Character count: {data.reason.length}/3000
            </div>
            {hasSubmitAttempt && errors.reason && (
              <div className="text-sm text-red-500">{errors.reason}</div>
            )}
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isSubmitting}
              className="bg-primary hover:bg-primary/90"
            >
              {isSubmitting && <Loader2 className="mr-2 size-4 animate-spin" />}
              Transfer Credit
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
