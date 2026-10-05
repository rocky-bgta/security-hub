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
import { IResponse } from 'models/Global';
import {
  IOrganizationType,
  IOrganizationTypePayload,
} from 'models/OrganizationType';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  organizationType: IOrganizationType | null;
  onSubmit: (data: IOrganizationType) => void;
}

const DefaultOrganizationType = {
  organizationType: '',
};

const ActionOrganizationType = ({
  isOpen,
  onClose,
  organizationType,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IOrganizationTypePayload>({
    ...DefaultOrganizationType,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      organizationType
        ? {
            organizationType: organizationType.organizationType,
          }
        : { ...DefaultOrganizationType },
    );
  }, [organizationType, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.organizationType.trim()) {
      newErrors.organizationType = 'Organization type name is required';
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

    const payload: IOrganizationTypePayload = {
      organizationType: formData.organizationType,
    };

    try {
      let response: IResponse<IOrganizationType>;
      if (organizationType) {
        payload.id = organizationType.id;
        response = await apiClient.put(
          API_END_POINTS.UPDATE_ORGANIZATION_TYPE,
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(
          API_END_POINTS.CREATE_ORGANIZATION_TYPE,
          {
            data: payload,
          },
        );
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success('Organization type created successfully');
      } else {
        toast.error('Failed to create organization type');
      }
    } catch (error) {
      console.error('Error creating/updating organization type:', error);
      toast.error('Failed to create organization type');
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Organization Type Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new organization type
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="organizationType"
              className="font-medium text-foreground"
            >
              Organization Type Name *
            </Label>
            <Input
              id="organizationType"
              placeholder="e.g., Technology, Education, Healthcare"
              value={formData.organizationType}
              onChange={e =>
                handleInputChange('organizationType', e.target.value)
              }
              className={errors.organizationType ? 'has-error' : ''}
            />
            {errors.organizationType && (
              <p className="text-sm text-destructive">
                {errors.organizationType}
              </p>
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
              {loading
                ? organizationType
                  ? 'Updating...'
                  : 'Creating...'
                : organizationType
                  ? 'Update Organization Type'
                  : 'Create Organization Type'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionOrganizationType;
