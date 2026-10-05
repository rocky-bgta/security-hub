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
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { ICompliance, ICompliancePayload } from 'models/Compliance';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  compliance: ICompliance | null;
  onSubmit: (data: ICompliance) => void;
}

const DefaultCompliance = {
  complianceName: '',
  description: '',
  acronym: '',
  sortOrder: 1,
};

const ActionCompliance = ({
  isOpen,
  onClose,
  compliance,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<ICompliancePayload>({
    ...DefaultCompliance,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      compliance
        ? {
            complianceName: compliance.complianceName,
            description: compliance.description,
            acronym: compliance.acronym,
            sortOrder: compliance.sortOrder,
          }
        : { ...DefaultCompliance },
    );
  }, [compliance, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.complianceName.trim()) {
      newErrors.complianceName = 'Compliance name is required';
    }

    if (!formData.acronym.trim()) {
      newErrors.acronym = 'Acronym is required';
    }

    if (!formData.sortOrder) {
      newErrors.sortOrder = 'Sort order is required';
    } else if (
      isNaN(Number(formData.sortOrder)) ||
      Number(formData.sortOrder) < 1
    ) {
      newErrors.sortOrder = 'Sort order must be a number greater than 0';
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

    let response: IResponse<ICompliance>;
    try {
      if (compliance) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_COMPLIANCE.replace(':id', compliance.id),
          { data: formData },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_COMPLIANCE, {
          data: formData,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success('Compliance created successfully');
      } else {
        toast.error('Failed to create compliance');
      }
    } catch (error) {
      console.error('Error creating/updating compliance data:', error);
      toast.error('Failed to create compliance');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Compliance Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new compliance
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="complianceName"
              className="font-medium text-foreground"
            >
              Compliance Name *
            </Label>
            <Input
              id="complianceName"
              placeholder="e.g., PCI, HIPAA"
              value={formData.complianceName}
              onChange={e =>
                handleInputChange('complianceName', e.target.value)
              }
              className={errors.complianceName ? 'has-error' : ''}
            />
            {errors.complianceName && (
              <p className="text-sm text-destructive">
                {errors.complianceName}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="acronym" className="font-medium text-foreground">
              Acronym *
            </Label>
            <Input
              id="acronym"
              placeholder="e.g., PCI, HIPAA"
              value={formData.acronym}
              onChange={e => handleInputChange('acronym', e.target.value)}
              className={errors.acronym ? 'has-error' : ''}
            />
            {errors.acronym && (
              <p className="text-sm text-destructive">{errors.acronym}</p>
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
              placeholder="Brief description of the category"
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
            <Label htmlFor="sortOrder" className="font-medium text-foreground">
              Sort Order *
            </Label>
            <Input
              id="sortOrder"
              type="number"
              min="1"
              placeholder=""
              value={formData.sortOrder}
              onChange={e => handleInputChange('sortOrder', e.target.value)}
              className={errors.sortOrder ? 'has-error' : ''}
            />
            {errors.sortOrder && (
              <p className="text-sm text-destructive">{errors.sortOrder}</p>
            )}
            <p className="text-xs text-muted-foreground">
              Higher numbers indicate higher sort orders
            </p>
          </div>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button variant="outline" onClick={onClose}>
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              <Save className="mr-2 size-4" />
              {loading ? 'Creating...' : 'Create Compliance'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionCompliance;
