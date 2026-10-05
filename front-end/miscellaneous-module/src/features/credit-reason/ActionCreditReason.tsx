import { Save, X } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
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
import { useAPI } from 'hooks/UseAPI';
import { ICreditReason, ICreditReasonPayload } from 'models/CreditReason';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  creditReason: ICreditReason | null;
  onSubmit: (data: ICreditReason) => void;
}

const DefaultCreditReason = {
  reasonName: '',
  description: '',
  isActive: true,
};

const ActionCreditReason = ({
  isOpen,
  onClose,
  creditReason,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<ICreditReasonPayload>({
    ...DefaultCreditReason,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      creditReason
        ? {
            reasonName: creditReason.reasonName,
            description: creditReason.description ?? '',
            isActive: creditReason.isActive,
          }
        : { ...DefaultCreditReason },
    );
  }, [creditReason, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    const name = formData.reasonName.trim();

    if (!name) {
      newErrors.reasonName = 'Reason name is required';
    } else if (name.length < 2) {
      newErrors.reasonName = 'Reason name must be at least 2 characters';
    } else if (name.length > 50) {
      newErrors.reasonName = 'Reason name must be at most 50 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));

    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);

    if (!validateForm()) {
      setLoading(false);
      return;
    }

    const payload = {
      reasonName: formData.reasonName.trim(),
      description: formData.description.trim(),
      isActive: formData.isActive,
    };

    let response: IResponse<ICreditReason>;
    try {
      if (creditReason) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_CREDIT_REASON.replace(':id', creditReason.id),
          { data: payload },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_CREDIT_REASON, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success(
          creditReason
            ? 'Credit reason updated successfully'
            : 'Credit reason created successfully',
        );
        onClose();
      } else {
        toast.error(
          creditReason
            ? 'Failed to update credit reason'
            : 'Failed to create credit reason',
        );
      }
    } catch (error) {
      console.error('Error creating/updating credit reason:', error);
      toast.error(
        creditReason
          ? 'Failed to update credit reason'
          : 'Failed to create credit reason',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Credit Allocation Reason</DialogTitle>
          <DialogDescription>
            {creditReason
              ? 'Update the credit allocation reason details below'
              : 'Fill in the details below to create a new credit allocation reason'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="reasonName" className="font-medium text-foreground">
              Reason Name *
            </Label>
            <Input
              id="reasonName"
              placeholder="e.g., Bonus credits, Referral reward"
              value={formData.reasonName}
              onChange={e => handleInputChange('reasonName', e.target.value)}
              maxLength={50}
              className={errors.reasonName ? 'has-error' : ''}
            />
            {errors.reasonName && (
              <p className="text-sm text-destructive">{errors.reasonName}</p>
            )}
            <p className="text-xs text-muted-foreground">2–50 characters</p>
          </div>

          <div className="space-y-2">
            <Label
              htmlFor="description"
              className="font-medium text-foreground"
            >
              Description
            </Label>
            <Textarea
              id="description"
              placeholder="Optional description"
              value={formData.description}
              onChange={e => handleInputChange('description', e.target.value)}
              rows={3}
              className="resize-none"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="isActive" className="font-medium text-foreground">
              Status
            </Label>
            <Select
              value={formData.isActive.toString()}
              onValueChange={e => handleInputChange('isActive', e)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="true">Active</SelectItem>
                <SelectItem value="false">Inactive</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button variant="outline" onClick={onClose} type="button">
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              <Save className="mr-2 size-4" />
              {loading
                ? 'Saving...'
                : creditReason
                  ? 'Update Credit Reason'
                  : 'Create Credit Reason'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionCreditReason;
