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
import { IIndustries, IIndustriesPayload } from 'models/Industries';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  industries: IIndustries | null;
  onSubmit: (data: IIndustries) => void;
}

const DefaultIndustries = {
  name: '',
  code: '',
  active: true,
};

const ActionIndustries = ({
  isOpen,
  onClose,
  industries,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IIndustriesPayload>({
    ...DefaultIndustries,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      industries
        ? {
            name: industries.name,
            code: industries.code,
            active: industries.active,
          }
        : { ...DefaultIndustries },
    );
  }, [industries, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Industries name is required';
    }

    if (!formData.code.trim()) {
      newErrors.code = 'Industries code is required';
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
      code: formData.code,
      active: formData.active,
    };

    try {
      let response: IResponse<IIndustries>;
      if (industries) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_INDUSTRIES.replace(':id', industries.id),
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_INDUSTRIES, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success('Industries created successfully');
      } else {
        toast.error('Failed to create industries');
      }
    } catch (error) {
      console.error('Error creating/updating industries:', error);
      toast.error('Failed to create industries');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Industries Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new industries
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Industries Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g., Agriculture, Manufacturing, Services"
              value={formData.name}
              onChange={e => handleInputChange('name', e.target.value)}
              className={errors.name ? 'has-error' : ''}
            />
            {errors.name && (
              <p className="text-sm text-destructive">{errors.name}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="code" className="font-medium text-foreground">
              Industries Code *
            </Label>
            <Input
              id="code"
              placeholder="e.g., INDUSTRY_1, INDUSTRY_2, INDUSTRY_3"
              value={formData.code}
              onChange={e => handleInputChange('code', e.target.value)}
              className={errors.code ? 'has-error' : ''}
            />
            {errors.code && (
              <p className="text-sm text-destructive">{errors.code}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="active" className="font-medium text-foreground">
              Industries Status *
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
              {loading ? 'Creating...' : 'Create Industries'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionIndustries;
