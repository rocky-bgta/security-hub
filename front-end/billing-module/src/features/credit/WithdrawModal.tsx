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
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface WithdrawModalProps {
  id: string;
  amount: number;
  isOpen: boolean;
  onClose: () => void;
  customerName: string;
  onSubmit: () => void;
}

export const WithdrawModal = ({
  id,
  amount,
  isOpen,
  onClose,
  customerName,
  onSubmit,
}: WithdrawModalProps) => {
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

  useEffect(() => {
    const numAmount = parseFloat(data.amount);
    if (numAmount > amount) {
      setErrors(prev => ({
        ...prev,
        amount: 'Amount cannot exceed available credit',
      }));
    }
  }, [data.amount, amount]);

  const validateForm = () => {
    const newErrors = {
      amount: '',
      description: '',
    };
    let isValid = true;

    const numAmount = parseFloat(data.amount);
    if (!data.amount || numAmount <= 0) {
      newErrors.amount = 'Amount must be greater than 0';
      isValid = false;
    } else if (numAmount > amount) {
      newErrors.amount = 'Amount cannot exceed available credit';
      isValid = false;
    }

    if (!data.description || data.description.trim() === '') {
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
      await apiClient.post(API_END_POINTS.CREDIT_WITHDRAW, {
        data: {
          clientId: id,
          amount: numAmount,
          reason: data.description,
          referenceId: 'b2c2965f-f071-4a39-a2c2-9d3e5b325588',
        },
      });
      toast.success('Credit withdrawal successful!');
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
      console.error('Error withdrawing credit:', error);
      toast.error('Failed to withdraw credit');
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
            Withdraw Credit
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="rounded-lg bg-secondary/50 p-4">
            <div className="mb-1 flex items-center gap-2 text-sm text-muted-foreground">
              <DollarSign className="size-4" />
              Withdrawing credit for
            </div>
            <div className="font-semibold text-card-foreground">
              {customerName || 'Aspire Digital Ltd.'}
            </div>
          </div>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="amount" className="text-sm font-medium">
                Withdraw Amount *
              </Label>
              <div className="relative">
                <DollarSign className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="amount"
                  type="number"
                  placeholder="0.00"
                  value={data.amount}
                  onChange={e => {
                    setData({ ...data, amount: e.target.value });
                    if (parseFloat(e.target.value) <= amount) {
                      setErrors({ ...errors, amount: '' });
                    }
                  }}
                  className={`pl-9 ${errors.amount && data.amount ? 'border-red-500' : ''}`}
                  disabled={isSubmitting}
                />
              </div>
              {errors.amount && data.amount && (
                <div className="text-sm text-red-500">{errors.amount}</div>
              )}
              {data.amount && parseFloat(data.amount) > 0 && (
                <div className="text-sm text-muted-foreground">
                  Total Credit:{' '}
                  {formatCurrency(String(amount - Number(data.amount)))}
                </div>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="description" className="text-sm font-medium">
                Description *
                <span className="ml-1 font-normal text-muted-foreground">
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
              <div className="text-sm text-muted-foreground">
                {data.description.length} / 3000 characters
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
              disabled={
                isSubmitting || !data.amount || parseFloat(data.amount) <= 0
              }
              className="flex-1"
            >
              {isSubmitting ? (
                <div className="flex items-center gap-2">
                  <div className="size-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                  Processing...
                </div>
              ) : (
                'Withdraw Credit'
              )}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
