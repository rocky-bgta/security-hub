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
import { IResponse } from 'models/Global';
import { IIndustries } from 'models/Industries';
import { ISubIndustries, ISubIndustriesPayload } from 'models/SubIndustries';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  subIndustries: ISubIndustries | null;
  onSubmit: (data: ISubIndustries) => void;
}

const DefaultSubIndustries: ISubIndustriesPayload = {
  industryId: '',
  name: '',
  code: '',
  active: true,
};

const ActionSubIndustries = ({
  isOpen,
  onClose,
  subIndustries,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<ISubIndustriesPayload>({
    ...DefaultSubIndustries,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [industries, setIndustries] = useState<Array<IIndustries>>([]);

  const apiClient = useAPI();

  useEffect(() => {
    if (!isOpen) return;

    const fetchIndustries = async () => {
      try {
        const response: IResponse<Array<IIndustries>> = await apiClient.get(
          API_END_POINTS.GET_INDUSTRIES_LIST,
        );
        setIndustries(Array.isArray(response.data) ? response.data : []);
      } catch (error) {
        console.error('Error fetching industries:', error);
        toast.error('Failed to fetch industries');
      }
    };

    fetchIndustries();
  }, [isOpen, apiClient]);

  useEffect(() => {
    setFormData(
      subIndustries
        ? {
            industryId: subIndustries.industryId,
            name: subIndustries.name,
            code: subIndustries.code,
            active: subIndustries.active,
          }
        : { ...DefaultSubIndustries },
    );
    setErrors({});
  }, [subIndustries, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.industryId) {
      newErrors.industryId = 'Industry is required';
    }

    if (!formData.name.trim()) {
      newErrors.name = 'Sub-industry name is required';
    }

    if (!formData.code.trim()) {
      newErrors.code = 'Sub-industry code is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: field === 'active' ? value === 'true' : value,
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
      industryId: formData.industryId,
      name: formData.name,
      code: formData.code,
      active: formData.active,
    };

    try {
      let response: IResponse<ISubIndustries>;
      if (subIndustries) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_SUB_INDUSTRIES.replace(':id', subIndustries.id),
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_SUB_INDUSTRIES, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success(
          subIndustries
            ? 'Sub-industry updated successfully'
            : 'Sub-industry created successfully',
        );
      } else {
        toast.error('Failed to save sub-industry');
      }
    } catch (error) {
      console.error('Error creating/updating sub-industry:', error);
      toast.error('Failed to save sub-industry');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Sub-Industry Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to {subIndustries ? 'update' : 'create'} a
            sub-industry
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="industryId" className="font-medium text-foreground">
              Industry *
            </Label>
            <Select
              value={formData.industryId || undefined}
              onValueChange={value => handleInputChange('industryId', value)}
            >
              <SelectTrigger className={errors.industryId ? 'has-error' : ''}>
                <SelectValue placeholder="Select industry" />
              </SelectTrigger>
              <SelectContent>
                {industries.map(industry => (
                  <SelectItem key={industry.id} value={industry.id}>
                    {industry.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.industryId && (
              <p className="text-sm text-destructive">{errors.industryId}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Sub-Industry Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g., Software Development, Retail Banking"
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
              Sub-Industry Code *
            </Label>
            <Input
              id="code"
              placeholder="e.g., SUB_INDUSTRY_1, SUB_INDUSTRY_2"
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
              Sub-Industry Status *
            </Label>
            <Select
              value={formData.active.toString()}
              onValueChange={value => handleInputChange('active', value)}
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
              {loading
                ? 'Saving...'
                : subIndustries
                  ? 'Update Sub-Industry'
                  : 'Create Sub-Industry'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionSubIndustries;
