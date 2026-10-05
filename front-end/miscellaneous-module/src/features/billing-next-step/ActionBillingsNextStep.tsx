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
  billingNextStep: IBillingAction | null;
  onSubmit: (data: IBillingAction) => void;
}

const DefaultBillingNextStep = {
  id: '',
  name: '',
};

const ActionBillingsNextStep = ({
  isOpen,
  onClose,
  billingNextStep,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IBillingAction>({
    ...DefaultBillingNextStep,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      billingNextStep
        ? {
            id: billingNextStep?.id,
            name: billingNextStep?.name,
          }
        : { ...DefaultBillingNextStep },
    );
  }, [billingNextStep, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Billing next step name is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev: IBillingAction) => ({
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
      if (billingNextStep) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_BILLING_NEXT_STEP.replace(
            ':id',
            billingNextStep.id,
          ),
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(
          API_END_POINTS.CREATE_BILLING_NEXT_STEP,
          {
            data: payload,
          },
        );
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success(
          `Billing next step ${billingNextStep ? 'updated' : 'created'} successfully`,
        );
      } else {
        toast.error(
          `Failed to ${billingNextStep ? 'update' : 'create'} billing next step`,
        );
      }
    } catch (error) {
      console.error('Error creating/updating billing next step:', error);
      toast.error(
        `Failed to ${billingNextStep ? 'update' : 'create'} billing next step`,
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Billing Next Step Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to{' '}
            {billingNextStep ? 'update' : 'create a new'} billing next step
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Billing Next Step Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g., Follow up with Finance"
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
                ? billingNextStep
                  ? 'Updating...'
                  : 'Creating...'
                : billingNextStep
                  ? 'Update Billing Next Step'
                  : 'Create Billing Next Step'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionBillingsNextStep;
