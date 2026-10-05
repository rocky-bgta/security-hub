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
import { useAPI } from 'hooks/UseAPI';
import { IOrganizationSizePayload } from 'models/OrganizationSize';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IOrganizationSize } from 'models/OrganizationSize';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  organizationSize: IOrganizationSize | null;
  onSubmit: (data: IOrganizationSize) => void;
}

const DefaultOrganizationSize = {
  name: '',
  range: '',
};

const ActionOrganizationSize = ({
  isOpen,
  onClose,
  organizationSize,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IOrganizationSizePayload>({
    ...DefaultOrganizationSize,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      organizationSize
        ? {
            name: organizationSize.name,
            range: organizationSize.range,
          }
        : { ...DefaultOrganizationSize },
    );
  }, [organizationSize, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Organization size name is required';
    }

    if (!formData.range.trim()) {
      newErrors.range = 'Organization size range is required';
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
      name: formData.name,
      range: formData.range,
    };

    try {
      let response: IResponse<IOrganizationSize>;
      if (organizationSize) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_ORGANIZATION_SIZE.replace(
            ':id',
            organizationSize.id,
          ),
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(
          API_END_POINTS.CREATE_ORGANIZATION_SIZE,
          {
            data: payload,
          },
        );
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success('Organization size created successfully');
      } else {
        toast.error('Failed to create organization size');
      }
    } catch (error) {
      console.error('Error creating/updating organization size:', error);
      toast.error('Failed to create organization size');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Organization Size Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new organization size
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Organization Size Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g., Small, Medium, Large"
              value={formData.name}
              onChange={e => handleInputChange('name', e.target.value)}
              className={errors.name ? 'has-error' : ''}
            />
            {errors.name && (
              <p className="text-sm text-destructive">{errors.name}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="range" className="font-medium text-foreground">
              Organization Size Range *
            </Label>
            <Input
              id="range"
              placeholder="e.g., 1-10, 11-20, 21-30"
              value={formData.range}
              onChange={e => handleInputChange('range', e.target.value)}
              className={errors.range ? 'has-error' : ''}
            />
            {errors.range && (
              <p className="text-sm text-destructive">{errors.range}</p>
            )}
          </div>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button
              disabled={loading}
              type="button"
              variant="outline"
              onClick={onClose}
            >
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button disabled={loading} type="submit">
              <Save className="mr-2 size-4" />
              {loading ? 'Creating...' : 'Create Organization Size'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionOrganizationSize;
