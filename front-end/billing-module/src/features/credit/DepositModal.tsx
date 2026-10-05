import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { CreditCard, DollarSign } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface DepositModalProps {
  id: string;
  amount: number;
  isOpen: boolean;
  onClose: () => void;
  customerName: string;
  onSubmit: () => void;
}

export const DepositModal = ({
  id,
  amount,
  isOpen,
  onClose,
  customerName,
  onSubmit,
}: DepositModalProps) => {
  const [data, setData] = useState({
    amount: '',
    description: '',
  });
  const [errors, setErrors] = useState({
    amount: '',
    description: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const apiClient = useAPI();

  const validateAmount = (value: string) => {
    const numAmount = parseFloat(value);
    let error = '';

    if (!value) {
      error = 'Amount is required';
    } else if (isNaN(numAmount)) {
      error = 'Please enter a valid number';
    } else if (numAmount <= 0) {
      error = 'Amount must be greater than 0';
    } else if (numAmount > 5000) {
      error = 'Amount cannot exceed 5000';
    }

    return error;
  };

  const validateForm = () => {
    let isValid = true;
    const newErrors = {
      amount: '',
      description: '',
    };

    // Amount validation
    newErrors.amount = validateAmount(data.amount);
    if (newErrors.amount) isValid = false;

    // Description validation
    if (!data.description) {
      newErrors.description = 'Description is required';
      isValid = false;
    } else if (data.description.length < 300) {
      newErrors.description = 'Description must be at least 300 characters';
      isValid = false;
    } else if (data.description.length > 3000) {
      newErrors.description = 'Description cannot exceed 3000 characters';
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const numAmount = parseFloat(data.amount);
    setIsSubmitting(true);

    try {
      await apiClient.post(API_END_POINTS.CREDIT_DEPOSIT, {
        data: {
          clientId: id,
          amount: numAmount,
          reason: data.description,
          referenceId: 'b2c2965f-f071-4a39-a2c2-9d3e5b325890',
        },
      });
      toast.success('Credit deposit successful!');
      onSubmit();
      setData({
        amount: '',
        description: '',
      });
      setErrors({
        amount: '',
        description: '',
      });
      onClose();
    } catch (error) {
      console.error('Error:', error);
      toast.error('Failed to process deposit');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setData({
        amount: '',
        description: '',
      });
      setErrors({
        amount: '',
        description: '',
      });
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
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <CreditCard className="size-5 text-primary" />
            Deposit Credit
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="rounded-lg bg-secondary/50 p-4">
            <div className="mb-1 flex items-center gap-2 text-sm text-muted-foreground">
              <DollarSign className="size-4" />
              Depositing credit for
            </div>
            <div className="font-semibold text-card-foreground">
              {customerName || 'Aspire Digital Ltd.'}
            </div>
          </div>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="amount" className="text-sm font-medium">
                Deposit Amount *
              </Label>
              <div className="relative">
                <DollarSign className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="amount"
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  value={data.amount}
                  onChange={e => {
                    const value = e.target.value;
                    if (!isNaN(parseFloat(value)) || value === '') {
                      setData({ ...data, amount: value });
                      const amountError = validateAmount(value);
                      setErrors({ ...errors, amount: amountError });
                    }
                  }}
                  className={`pl-9 ${errors.amount ? 'border-red-500' : ''}`}
                  disabled={isSubmitting}
                />
              </div>
              {errors.amount && (
                <div className="text-sm text-red-500">{errors.amount}</div>
              )}
              {data.amount &&
                !isNaN(parseFloat(data.amount)) &&
                parseFloat(data.amount) > 0 && (
                  <div className="text-sm text-muted-foreground">
                    Total Credit:{' '}
                    {formatCurrency(String(amount + Number(data.amount)))}
                  </div>
                )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="description" className="text-sm font-medium">
                Description *{' '}
                <span className="text-xs text-muted-foreground">
                  (300-3000 characters)
                </span>
              </Label>
              <Textarea
                id="description"
                placeholder="Enter a description for this deposit..."
                value={data.description}
                onChange={e => {
                  setData({ ...data, description: e.target.value });
                  setErrors({ ...errors, description: '' });
                }}
                rows={3}
                disabled={isSubmitting}
                className={`resize-none ${errors.description ? 'border-red-500' : ''}`}
              />
              {errors.description && (
                <div className="text-sm text-red-500">{errors.description}</div>
              )}
              <div className="text-xs text-muted-foreground">
                Characters: {data.description.length}/3000
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
            <Button type="submit" disabled={isSubmitting} className="flex-1">
              {isSubmitting ? (
                <div className="flex items-center gap-2">
                  <div className="size-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                  Processing...
                </div>
              ) : (
                'Deposit Credit'
              )}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
