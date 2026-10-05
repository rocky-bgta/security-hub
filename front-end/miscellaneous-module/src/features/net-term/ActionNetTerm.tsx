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
import { INetTerm, INetTermPayload } from 'models/NetTerm';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  netTerm: INetTerm | null;
  onSubmit: (data: INetTerm) => void;
}

const DefaultNetTerm = {
  netTermName: '',
  netTermInDays: 0,
  isActive: true,
};

const ActionNetTerm = ({ isOpen, onClose, netTerm, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<INetTermPayload>({
    ...DefaultNetTerm,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      netTerm
        ? {
            netTermName: netTerm.netTermName,
            netTermInDays: netTerm.netTermInDays,
            isActive: netTerm.isActive,
          }
        : { ...DefaultNetTerm },
    );
  }, [netTerm, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    const name = formData.netTermName.trim();

    if (!name) {
      newErrors.netTermName = 'Net term name is required';
    } else if (name.length < 2) {
      newErrors.netTermName = 'Net term name must be at least 2 characters';
    } else if (name.length > 50) {
      newErrors.netTermName = 'Net term name must be at most 50 characters';
    }

    const days = Number(formData.netTermInDays);
    if (
      formData.netTermInDays === undefined ||
      formData.netTermInDays === null ||
      (typeof formData.netTermInDays === 'number' && formData.netTermInDays < 1)
    ) {
      newErrors.netTermInDays = 'Net term days is required';
    } else if (isNaN(days) || days < 1 || !Number.isInteger(days)) {
      newErrors.netTermInDays = 'Net term days must be a positive whole number';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string | number) => {
    setFormData(prev => ({
      ...prev,
      [field]: field === 'isActive' ? value === 'true' : value,
    }));
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
      netTermName: formData.netTermName.trim(),
      netTermInDays: Number(formData.netTermInDays),
      isActive: formData.isActive,
    };

    let response: IResponse<INetTerm>;
    try {
      if (netTerm) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_NET_TERM.replace(':id', netTerm.id),
          { data: payload },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_NET_TERM, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success(
          netTerm
            ? 'Net term updated successfully'
            : 'Net term created successfully',
        );
        onClose();
      } else {
        toast.error(
          netTerm ? 'Failed to update net term' : 'Failed to create net term',
        );
      }
    } catch (error) {
      console.error('Error creating/updating net term:', error);
      toast.error(
        netTerm ? 'Failed to update net term' : 'Failed to create net term',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Net Term Configuration</DialogTitle>
          <DialogDescription>
            {netTerm
              ? 'Update the net term configuration below'
              : 'Fill in the details below to create a new net term configuration'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="netTermName"
              className="font-medium text-foreground"
            >
              Net Term Name *
            </Label>
            <Input
              id="netTermName"
              placeholder="e.g., Net 30, Net 60"
              value={formData.netTermName}
              onChange={e => handleInputChange('netTermName', e.target.value)}
              maxLength={50}
              className={errors.netTermName ? 'has-error' : ''}
            />
            {errors.netTermName && (
              <p className="text-sm text-destructive">{errors.netTermName}</p>
            )}
            <p className="text-xs text-muted-foreground">2–50 characters</p>
          </div>

          <div className="space-y-2">
            <Label
              htmlFor="netTermInDays"
              className="font-medium text-foreground"
            >
              Net Term (Days) *
            </Label>
            <Input
              id="netTermInDays"
              type="number"
              min={1}
              step={1}
              placeholder="e.g., 30, 60"
              value={formData.netTermInDays === 0 ? '' : formData.netTermInDays}
              onChange={e => {
                const v = e.target.value;
                handleInputChange(
                  'netTermInDays',
                  v === '' ? 0 : parseInt(v, 10) || 0,
                );
              }}
              className={errors.netTermInDays ? 'has-error' : ''}
            />
            {errors.netTermInDays && (
              <p className="text-sm text-destructive">{errors.netTermInDays}</p>
            )}
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
                : netTerm
                  ? 'Update Net Term'
                  : 'Create Net Term'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionNetTerm;
