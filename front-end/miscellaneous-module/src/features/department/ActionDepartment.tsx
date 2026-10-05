import { Save, X } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
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
import { IDepartment, IDepartmentPayload } from 'models/Department';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  department: IDepartment | null;
  onSubmit: (data: IDepartment) => void;
}

const DefaultDepartment = {
  name: '',
  description: '',
  isSystemDefined: true,
  active: true,
};

const VALIDATION_RULES = {
  name: {
    minLength: 2,
    maxLength: 100,
  },
  description: {
    minLength: 0,
    maxLength: 500,
  },
};

const ActionDepartment = ({
  isOpen,
  onClose,
  department,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IDepartmentPayload>({
    ...DefaultDepartment,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      department
        ? {
            name: department.name,
            description: department.description,
            isSystemDefined: department.isSystemDefined,
            active: department.active,
          }
        : { ...DefaultDepartment },
    );
  }, [department, isOpen]);

  const validateField = (field: string, value: string | boolean): string => {
    if (field === 'name') {
      const nameValue = String(value);
      const trimmedName = nameValue.trim();

      if (!trimmedName) {
        return 'Department name is required';
      } else if (trimmedName.length < VALIDATION_RULES.name.minLength) {
        return `Department name must be at least ${VALIDATION_RULES.name.minLength} characters`;
      } else if (nameValue.length > VALIDATION_RULES.name.maxLength) {
        return `Department name must not exceed ${VALIDATION_RULES.name.maxLength} characters`;
      }
    } else if (field === 'description') {
      const descValue = String(value);
      if (descValue.length > VALIDATION_RULES.description.maxLength) {
        return `Description must not exceed ${VALIDATION_RULES.description.maxLength} characters`;
      }
    }
    return '';
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    // Validate Department Name
    const nameError = validateField('name', formData.name);
    if (nameError) {
      newErrors.name = nameError;
    }

    // Validate Description
    const descError = validateField('description', formData.description);
    if (descError) {
      newErrors.description = descError;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }));

    const error = validateField(field, value);
    setErrors(prev => ({ ...prev, [field]: error }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);

    if (!validateForm()) {
      setLoading(false);
      return;
    }

    let response: IResponse<IDepartment>;
    try {
      if (department) {
        response = await apiClient.put(API_END_POINTS.UPDATE_DEPARTMENT, {
          data: {
            id: department.id,
            name: formData.name,
            description: formData.description,
            isSystemDefined: formData.isSystemDefined,
            active: Boolean(formData.active),
          },
        });
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_DEPARTMENT, {
          data: formData,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success(
          `Department ${department ? 'updated' : 'created'} successfully`,
        );
      } else {
        toast.error(`Failed to ${department ? 'update' : 'create'} department`);
      }
    } catch (error) {
      console.error('Error creating/updating department data:', error);
      toast.error(`Failed to ${department ? 'update' : 'create'} department`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="w-1/2">
        <DialogHeader>
          <DialogTitle>Department Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to {department ? 'update' : 'create'} a
            department
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="departmentName"
              className="font-medium text-foreground"
            >
              Department Name *
            </Label>
            <Input
              id="departmentName"
              placeholder="e.g., Human Resources, IT"
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
              htmlFor="departmentDescription"
              className="font-medium text-foreground"
            >
              Description
            </Label>
            <Textarea
              id="departmentDescription"
              placeholder="Enter department description"
              value={formData.description}
              onChange={e => handleInputChange('description', e.target.value)}
              rows={4}
            />
            {errors.description && (
              <p className="text-sm text-destructive">{errors.description}</p>
            )}
          </div>
          {department && (
            <div className="space-y-2">
              <Label htmlFor="active" className="font-medium text-foreground">
                Active
              </Label>
              <Select
                value={formData.active ? 'true' : 'false'}
                onValueChange={e => handleInputChange('active', e === 'true')}
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
          )}
          <div className="flex items-center space-x-2">
            <Checkbox
              id="isSystemDefined"
              checked={formData.isSystemDefined}
              onCheckedChange={checked =>
                handleInputChange('isSystemDefined', checked === true)
              }
            />
            <Label
              htmlFor="isSystemDefined"
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
            >
              System Defined
            </Label>
          </div>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button variant="outline" type="button" onClick={onClose}>
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              <Save className="mr-2 size-4" />
              {loading
                ? department
                  ? 'Updating...'
                  : 'Creating...'
                : department
                  ? 'Update Department'
                  : 'Create Department'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionDepartment;
