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
import { IUserRange, IUserRangePayload } from 'models/UserRange';
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
import { Checkbox } from 'common/Checkbox';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  userRange: IUserRange | null;
  onSubmit: (data: IUserRange) => void;
}

const DefaultUserRange = {
  rangeName: '',
  minUsers: 0,
  maxUsers: 0,
  description: '',
  isDefault: false,
};

const VALIDATION_RULES = {
  rangeName: {
    minLength: 2,
    maxLength: 100,
  },
  description: {
    minLength: 0,
    maxLength: 500,
  },
  minUsers: {
    min: 0,
  },
  maxUsers: {
    min: 1,
  },
};

const ActionUserRange = ({ isOpen, onClose, userRange, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IUserRangePayload>({
    ...DefaultUserRange,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      userRange
        ? {
            rangeName: userRange.rangeName,
            minUsers: userRange.minUsers,
            maxUsers: userRange.maxUsers,
            description: userRange.description,
            isDefault: userRange.isDefault,
          }
        : { ...DefaultUserRange },
    );
  }, [userRange, isOpen]);

  const validateField = (
    field: string,
    value: string | number | boolean,
  ): string => {
    if (field === 'rangeName') {
      const nameValue = String(value);
      const trimmedName = nameValue.trim();

      if (!trimmedName) {
        return 'Range name is required';
      } else if (trimmedName.length < VALIDATION_RULES.rangeName.minLength) {
        return `Range name must be at least ${VALIDATION_RULES.rangeName.minLength} characters`;
      } else if (nameValue.length > VALIDATION_RULES.rangeName.maxLength) {
        return `Range name must not exceed ${VALIDATION_RULES.rangeName.maxLength} characters`;
      }
    } else if (field === 'description') {
      const descValue = String(value);
      if (descValue.length > VALIDATION_RULES.description.maxLength) {
        return `Description must not exceed ${VALIDATION_RULES.description.maxLength} characters`;
      }
    } else if (field === 'minUsers') {
      const minValue = Number(value);
      if (minValue < VALIDATION_RULES.minUsers.min) {
        return `Minimum users must be at least ${VALIDATION_RULES.minUsers.min}`;
      }
      if (formData.maxUsers > 0 && minValue >= formData.maxUsers) {
        return 'Minimum users must be less than maximum users';
      }
    } else if (field === 'maxUsers') {
      const maxValue = Number(value);
      if (maxValue < VALIDATION_RULES.maxUsers.min) {
        return `Maximum users must be at least ${VALIDATION_RULES.maxUsers.min}`;
      }
      if (formData.minUsers >= maxValue) {
        return 'Maximum users must be greater than minimum users';
      }
    }
    return '';
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    // Validate Range Name
    const nameError = validateField('rangeName', formData.rangeName);
    if (nameError) {
      newErrors.rangeName = nameError;
    }

    // Validate Min Users
    const minUsersError = validateField('minUsers', formData.minUsers);
    if (minUsersError) {
      newErrors.minUsers = minUsersError;
    }

    // Validate Max Users
    const maxUsersError = validateField('maxUsers', formData.maxUsers);
    if (maxUsersError) {
      newErrors.maxUsers = maxUsersError;
    }

    // Validate Description
    const descError = validateField('description', formData.description);
    if (descError) {
      newErrors.description = descError;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (
    field: string,
    value: string | number | boolean,
  ) => {
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

    try {
      if (userRange) {
        const response = await apiClient.put(
          API_END_POINTS.UPDATE_USER_RANGE.replace(':id', userRange.id),
          {
            data: {
              rangeName: formData.rangeName,
              minUsers: formData.minUsers,
              maxUsers: formData.maxUsers,
              description: formData.description,
              isDefault: formData.isDefault,
            },
          },
        );
        if (isSuccessResponse(response.statusCode)) {
          onSubmit?.(response.data);
          toast.success(`User range updated successfully`);
        } else {
          toast.error(response.data.message);
        }
      } else {
        const response = await apiClient.post(
          API_END_POINTS.CREATE_USER_RANGE,
          {
            data: formData,
          },
        );
        if (isSuccessResponse(response.statusCode)) {
          onSubmit?.(response.data);
          toast.success(`User range created successfully`);
        } else {
          toast.error(response.data.message);
        }
      }
    } catch (error) {
      console.error('Error creating/updating user range data:', error);
      toast.error('Failed to create user range');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="w-1/2">
        <DialogHeader>
          <DialogTitle>User Range Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to {userRange ? 'update' : 'create'} a
            user range
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="rangeName" className="font-medium text-foreground">
              Range Name *
            </Label>
            <Input
              id="rangeName"
              placeholder="e.g., Small (1-50), Medium (51-200)"
              value={formData.rangeName}
              onChange={e => handleInputChange('rangeName', e.target.value)}
              className={errors.rangeName ? 'has-error' : ''}
            />
            {errors.rangeName && (
              <p className="text-sm text-destructive">{errors.rangeName}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="minUsers" className="font-medium text-foreground">
                Minimum Users *
              </Label>
              <Input
                id="minUsers"
                type="number"
                placeholder="0"
                value={formData.minUsers}
                onChange={e =>
                  handleInputChange('minUsers', parseInt(e.target.value) || 0)
                }
                className={errors.minUsers ? 'has-error' : ''}
                min={0}
              />
              {errors.minUsers && (
                <p className="text-sm text-destructive">{errors.minUsers}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="maxUsers" className="font-medium text-foreground">
                Maximum Users *
              </Label>
              <Input
                id="maxUsers"
                type="number"
                placeholder="0"
                value={formData.maxUsers}
                onChange={e =>
                  handleInputChange('maxUsers', parseInt(e.target.value) || 0)
                }
                className={errors.maxUsers ? 'has-error' : ''}
                min={1}
              />
              {errors.maxUsers && (
                <p className="text-sm text-destructive">{errors.maxUsers}</p>
              )}
            </div>
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
              placeholder="Enter user range description"
              value={formData.description}
              onChange={e => handleInputChange('description', e.target.value)}
              rows={4}
            />
            {errors.description && (
              <p className="text-sm text-destructive">{errors.description}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="flex items-center space-x-2">
              <Checkbox
                checked={formData.isDefault}
                onCheckedChange={checked =>
                  handleInputChange('isDefault', checked)
                }
              />
              <Label
                htmlFor="isDefault"
                className="font-medium text-foreground"
              >
                Default Range
              </Label>
            </div>
            {/* {userRange && (
              <div className="space-y-2">
                <Label
                  htmlFor="isActive"
                  className="font-medium text-foreground"
                >
                  Active
                </Label>
                <Select
                  value={userRange.isActive ? 'true' : 'false'}
                  onValueChange={() => {
                    // Status update can be handled separately if needed
                  }}
                  disabled
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
            )} */}
          </div>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button variant="outline" type="button" onClick={onClose}>
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              <Save className="mr-2 size-4" />
              {loading
                ? userRange
                  ? 'Updating...'
                  : 'Creating...'
                : userRange
                  ? 'Update User Range'
                  : 'Create User Range'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionUserRange;
