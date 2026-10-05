import { Save, X } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';

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
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IRole, IRolePayload } from 'models/Role';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  role: IRole | null;
  onSubmit: (data: IRole) => void;
}

const ColorThemes = [
  { value: 'blue', label: 'Blue', class: 'bg-blue-500' },
  { value: 'purple', label: 'Purple', class: 'bg-purple-500' },
  { value: 'green', label: 'Green', class: 'bg-green-500' },
  { value: 'orange', label: 'Orange', class: 'bg-orange-500' },
  { value: 'red', label: 'Red', class: 'bg-red-500' },
  { value: 'pink', label: 'Pink', class: 'bg-pink-500' },
  { value: 'indigo', label: 'Indigo', class: 'bg-indigo-500' },
  { value: 'gray', label: 'Gray', class: 'bg-gray-500' },
];

const DefaultRole = {
  roleName: '',
  description: '',
  accessLevel: 1,
  colorTheme: 'gray',
  status: 'active',
};

const ActionRole = ({ isOpen, onClose, role, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IRolePayload>({ ...DefaultRole });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(role ? { ...role } : { ...DefaultRole });
  }, [role, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.roleName.trim()) {
      newErrors.roleName = 'Role name is required';
    }

    if (!formData.accessLevel) {
      newErrors.accessLevel = 'Access level is required';
    } else if (
      isNaN(Number(formData.accessLevel)) ||
      Number(formData.accessLevel) < 1 ||
      Number(formData.accessLevel) > 6
    ) {
      newErrors.accessLevel = 'Access level must be a number between 1 and 6';
    }

    if (!formData.colorTheme) {
      newErrors.colorTheme = 'Color theme is required';
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

    let response: IResponse<IRole>;
    try {
      if (role) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_ROLE.replace(':id', role.id),
          { data: formData },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_ROLE, {
          data: formData,
        });
      }
      onSubmit?.(response.data);
      onClose();
      toast.success(
        `Role ${role?.roleName ? 'updated' : 'created'} successfully`,
      );
    } catch (error) {
      console.error('Error creating/updating role data:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Role Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new role
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Role Name *
            </Label>
            <Input
              id="roleName"
              placeholder="e.g., Client Admin, Phishing Admin"
              value={formData.roleName}
              onChange={e => handleInputChange('roleName', e.target.value)}
              className={errors.roleName ? 'has-error' : ''}
            />
            {errors.roleName && (
              <p className="text-sm text-destructive">{errors.roleName}</p>
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
              placeholder="Brief description of the role's responsibilities and access"
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
            <Label
              htmlFor="accessLevel"
              className="font-medium text-foreground"
            >
              Access Level *
            </Label>
            <Input
              id="accessLevel"
              type="number"
              min="1"
              max="6"
              placeholder="1-6 (1 = lowest, 6 = highest)"
              value={formData.accessLevel}
              onChange={e => handleInputChange('accessLevel', e.target.value)}
              className={errors.accessLevel ? 'has-error' : ''}
            />
            {errors.accessLevel && (
              <p className="text-sm text-destructive">{errors.accessLevel}</p>
            )}
            <p className="text-xs text-muted-foreground">
              Higher numbers indicate higher access levels (1-6)
            </p>
          </div>

          <div className="space-y-2">
            <Label htmlFor="colorTheme" className="font-medium text-foreground">
              Color Theme *
            </Label>
            <Select
              value={formData.colorTheme}
              onValueChange={value => handleInputChange('colorTheme', value)}
            >
              <SelectTrigger className={errors.colorTheme ? 'has-error' : ''}>
                <SelectValue placeholder="Select a color theme" />
              </SelectTrigger>
              <SelectContent>
                {ColorThemes.map(theme => (
                  <SelectItem key={theme.value} value={theme.value}>
                    <div className="flex items-center gap-2">
                      <div
                        className={cn('h-4 w-4 rounded-full', theme.class)}
                      />
                      {theme.label}
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.colorTheme && (
              <p className="text-sm text-destructive">{errors.colorTheme}</p>
            )}
          </div>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button variant="outline" onClick={onClose}>
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              <Save className="mr-2 size-4" />
              {loading ? 'Creating...' : 'Create Role'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionRole;
