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
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IBillingAction } from 'models/Billing';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  billingAction: IBillingAction | null;
  onSubmit: (data: IBillingAction) => void;
}

const DefaultBillingAction = {
  id: '',
  name: '',
};

const ActionBillings = ({
  isOpen,
  onClose,
  billingAction,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IBillingAction>({
    id: '',
    name: '',
  } as IBillingAction);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      billingAction
        ? {
            id: billingAction.id,
            name: billingAction.name,
          }
        : { ...DefaultBillingAction },
    );
  }, [billingAction, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Billing action name is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
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
      name: formData.name,
    };

    try {
      let response: IResponse<IBillingAction>;
      if (billingAction) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_BILLING_ACTION.replace(':id', billingAction.id),
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_BILLING_ACTION, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success(
          `Billing action ${billingAction ? 'updated' : 'created'} successfully`,
        );
      } else {
        toast.error(
          `Failed to ${billingAction ? 'update' : 'create'} billing action`,
        );
      }
    } catch (error) {
      console.error('Error creating/updating billing:', error);
      toast.error(
        `Failed to ${billingAction ? 'update' : 'create'} billing action`,
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Billing Action Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to{' '}
            {billingAction ? 'update' : 'create a new'} billing action
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Billing Action Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g., Payment in review"
              value={formData.name}
              onChange={e => handleInputChange('name', e.target.value)}
              className={errors.name ? 'has-error' : ''}
            />
            {errors.name && (
              <p className="text-sm text-destructive">{errors.name}</p>
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
                ? billingAction
                  ? 'Updating...'
                  : 'Creating...'
                : billingAction
                  ? 'Update Billing Action'
                  : 'Create Billing Action'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionBillings;
