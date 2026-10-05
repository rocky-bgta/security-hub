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
import { ICountry, ICountryPayload } from 'models/Country';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  country: ICountry | null;
  onSubmit: (data: ICountry) => void;
}

const DefaultCountry = {
  name: '',
  code: '',
  phoneCode: '',
  displayOrder: 0,
  active: true,
};

const ActionCountry = ({ isOpen, onClose, country, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<ICountryPayload>({
    ...DefaultCountry,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      country
        ? {
            name: country.name,
            code: country.code,
            phoneCode: country.phoneCode,
            displayOrder: country.displayOrder,
            active: country.active,
          }
        : { ...DefaultCountry },
    );
    setErrors({});
  }, [country, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Country name is required';
    }

    if (!formData.code.trim()) {
      newErrors.code = 'Country code is required';
    }

    if (!formData.phoneCode.trim()) {
      newErrors.phoneCode = 'Phone code is required';
    }

    if (!formData.displayOrder) {
      newErrors.displayOrder = 'Display order is required';
    } else if (
      isNaN(Number(formData.displayOrder)) ||
      Number(formData.displayOrder) < 0
    ) {
      newErrors.displayOrder = 'Display order must be a number greater than 0';
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

    let response: IResponse<ICountry>;
    try {
      if (country) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_COUNTRY.replace(':id', country.id),
          { data: formData },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_COUNTRY, {
          data: formData,
        });
      }
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'API error');
      }
      onSubmit?.(response.data);
      toast.success(
        response.message ||
          `Country has been successfully ${country ? 'updated' : 'created'}.`,
      );
    } catch (error) {
      console.error('Error creating/updating country data:', error);
      toast.error(
        (error as Error).message ||
          `Failed to ${country ? 'update' : 'create'} country`,
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Country Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to {country ? 'update' : 'create'} a
            country
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="countryName"
              className="font-medium text-foreground"
            >
              Country Name *
            </Label>
            <Input
              id="countryName"
              placeholder="e.g., United States, Canada"
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
              htmlFor="countryCode"
              className="font-medium text-foreground"
            >
              Country Code *
            </Label>
            <Input
              id="countryCode"
              placeholder="e.g., US, CA"
              value={formData.code}
              onChange={e => handleInputChange('code', e.target.value)}
              className={errors.code ? 'has-error' : ''}
            />
            {errors.code && (
              <p className="text-sm text-destructive">{errors.code}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="phoneCode" className="font-medium text-foreground">
              Phone Code *
            </Label>
            <Input
              id="phoneCode"
              placeholder="e.g., +1, +44"
              value={formData.phoneCode}
              onChange={e => handleInputChange('phoneCode', e.target.value)}
              className={errors.phoneCode ? 'has-error' : ''}
            />
            {errors.phoneCode && (
              <p className="text-sm text-destructive">{errors.phoneCode}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label
              htmlFor="displayOrder"
              className="font-medium text-foreground"
            >
              Display Order *
            </Label>
            <Input
              id="displayOrder"
              type="number"
              min="0"
              placeholder=""
              value={formData.displayOrder}
              onChange={e => handleInputChange('displayOrder', e.target.value)}
              className={errors.displayOrder ? 'has-error' : ''}
            />
            {errors.displayOrder && (
              <p className="text-sm text-destructive">{errors.displayOrder}</p>
            )}
            <p className="text-xs text-muted-foreground">
              Higher numbers indicate higher display orders
            </p>

            <div className="space-y-2">
              <Label htmlFor="active" className="font-medium text-foreground">
                Country Status
              </Label>
              <Select
                value={formData.active.toString()}
                onValueChange={e => handleInputChange('active', e)}
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
          </div>
          <div className="flex items-center justify-end gap-4 pt-4">
            <Button variant="outline" onClick={onClose}>
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              <Save className="mr-2 size-4" />
              {loading
                ? country
                  ? 'Updating...'
                  : 'Creating...'
                : country
                  ? 'Update Country'
                  : 'Create Country'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionCountry;
