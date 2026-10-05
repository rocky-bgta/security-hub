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
import { useAPI } from 'hooks/UseAPI';
import { IMSPType, IMSPTypePayload } from 'models/MSPType';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  mspType: IMSPType | null;
  onSubmit: (data: IMSPType) => void;
}

const DefaultMSPType = {
  name: '',
  isActive: true,
};

const ActionMSPType = ({ isOpen, onClose, mspType, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IMSPTypePayload>({
    ...DefaultMSPType,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      mspType
        ? {
            name: mspType.name,
            isActive: mspType.isActive,
          }
        : { ...DefaultMSPType },
    );
  }, [mspType, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    const name = formData.name.trim();

    if (!name) {
      newErrors.name = 'MSP Type name is required';
    } else if (name.length < 2) {
      newErrors.name = 'MSP Type name must be at least 2 characters';
    } else if (name.length > 50) {
      newErrors.name = 'MSP Type name must be at most 50 characters';
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
      name: formData.name.trim(),
      isActive: formData.isActive,
    };

    let response: IResponse<IMSPType>;
    try {
      if (mspType) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_MSP_TYPE.replace(':id', mspType.id),
          { data: payload },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_MSP_TYPE, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success(
          mspType
            ? 'MSP Type updated successfully'
            : 'MSP Type created successfully',
        );
        onClose();
      } else {
        toast.error(
          mspType ? 'Failed to update MSP Type' : 'Failed to create MSP Type',
        );
      }
    } catch (error) {
      console.error('Error creating/updating MSP Type:', error);
      toast.error(
        mspType ? 'Failed to update MSP Type' : 'Failed to create MSP Type',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>MSP Type Information</DialogTitle>
          <DialogDescription>
            {mspType
              ? 'Update the MSP Type details below'
              : 'Fill in the details below to create a new MSP Type'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="mspTypeName"
              className="font-medium text-foreground"
            >
              MSP Type Name *
            </Label>
            <Input
              id="mspTypeName"
              placeholder="e.g., Managed Service Provider, Reseller"
              value={formData.name}
              onChange={e => handleInputChange('name', e.target.value)}
              maxLength={50}
              className={errors.name ? 'has-error' : ''}
            />
            {errors.name && (
              <p className="text-sm text-destructive">{errors.name}</p>
            )}
            <p className="text-xs text-muted-foreground">2–50 characters</p>
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
                : mspType
                  ? 'Update MSP Type'
                  : 'Create MSP Type'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionMSPType;
