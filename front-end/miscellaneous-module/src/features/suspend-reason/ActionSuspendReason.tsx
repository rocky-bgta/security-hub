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
import { IResponse } from 'models/Global';
import { ISuspendReason, ISuspendReasonPayload } from 'models/SuspendReason';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  suspendReason: ISuspendReason | null;
  onSubmit: (data: ISuspendReason) => void;
}

const DefaultSuspendReason: ISuspendReasonPayload = {
  name: '',
  description: '',
  active: true,
};

const ActionSuspendReason = ({
  isOpen,
  onClose,
  suspendReason,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<ISuspendReasonPayload>({
    ...DefaultSuspendReason,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      suspendReason
        ? {
            name: suspendReason.name,
            description: suspendReason.description ?? '',
            active: suspendReason.active,
          }
        : { ...DefaultSuspendReason },
    );
  }, [suspendReason, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Suspend reason name is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string | boolean) => {
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

    const payload: ISuspendReasonPayload = {
      name: formData.name.trim(),
      description: formData.description.trim(),
      active: formData.active,
    };

    try {
      let response: IResponse<ISuspendReason>;
      if (suspendReason) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_SUSPEND_REASON.replace(':id', suspendReason.id),
          { data: payload },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_SUSPEND_REASON, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success(
          suspendReason
            ? 'Suspend reason updated successfully'
            : 'Suspend reason created successfully',
        );
      } else {
        toast.error(
          suspendReason
            ? 'Failed to update suspend reason'
            : 'Failed to create suspend reason',
        );
      }
    } catch (error) {
      console.error('Error creating/updating suspend reason:', error);
      toast.error(
        suspendReason
          ? 'Failed to update suspend reason'
          : 'Failed to create suspend reason',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Suspend Reason Information</DialogTitle>
          <DialogDescription>
            {suspendReason
              ? 'Update the suspend reason details below'
              : 'Fill in the details below to create a new suspend reason'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g., Policy violation, Non-payment"
              value={formData.name}
              onChange={e => handleInputChange('name', e.target.value)}
              className={errors.name ? 'has-error' : ''}
            />
            {errors.name && (
              <p className="text-sm text-destructive">{errors.name}</p>
            )}
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
              placeholder="Brief description of the suspend reason"
              value={formData.description}
              onChange={e => handleInputChange('description', e.target.value)}
              className="min-h-[80px]"
              maxLength={255}
            />
            <p className="text-xs text-muted-foreground">
              {formData.description.length}/255 characters
            </p>
          </div>

          <div className="space-y-2">
            <Label htmlFor="active" className="font-medium text-foreground">
              Status
            </Label>
            <Select
              value={formData.active.toString()}
              onValueChange={value =>
                handleInputChange('active', value === 'true')
              }
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
                : suspendReason
                  ? 'Update Suspend Reason'
                  : 'Create Suspend Reason'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionSuspendReason;
