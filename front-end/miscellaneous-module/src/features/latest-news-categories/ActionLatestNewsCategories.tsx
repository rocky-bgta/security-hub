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
import { ILatestNewsCategories } from 'models/LatestNewsCategories';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { isSuccessResponse } from 'utils/Helper';
import { Textarea } from 'common/Textarea';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  latestNewsCategories: ILatestNewsCategories | null;
  onSubmit: (data: ILatestNewsCategories) => void;
}

type FormPayload = {
  name: string;
  description: string;
  status: string; // string like "DRAFT", "ACTIVE", etc.
};

const DefaultLatestNewsCategories: FormPayload = {
  name: '',
  description: '',
  status: 'DRAFT',
};

const ActionLatestNewsCategories = ({
  isOpen,
  onClose,
  latestNewsCategories,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<FormPayload>({
    ...DefaultLatestNewsCategories,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      latestNewsCategories
        ? {
            name: latestNewsCategories.name,
            description: latestNewsCategories.description,
            // support both a possible string status from backend or legacy boolean active
            status:
              // if backend provides status string use it
              (latestNewsCategories as any).status
                ? (latestNewsCategories as any).status
                : // otherwise fallback to active boolean
                  (latestNewsCategories as any).active
                  ? 'ACTIVE'
                  : 'INACTIVE',
          }
        : { ...DefaultLatestNewsCategories },
    );
  }, [latestNewsCategories, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Latest news categories name is required';
    }

    if (!formData.description.trim()) {
      newErrors.code = 'Latest news categories is required';
    }

    // validate status
    const allowedStatuses = ['DRAFT', 'ACTIVE', 'INACTIVE'];
    if (!allowedStatuses.includes(formData.status)) {
      newErrors.status = `Invalid status. Allowed: ${allowedStatuses.join(', ')}`;
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

    // Backend expects a "status" string (DRAFT/ACTIVE/INACTIVE)
    const requestPayload = {
      name: formData.name,
      description: formData.description,
      status: formData.status,
    };

    // keep status locally for parent
    const localPayload = {
      name: formData.name,
      code: formData.description,
      status: formData.status,
    };

    try {
      let response: IResponse<ILatestNewsCategories>;
      if (latestNewsCategories) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_LATEST_NEWS_CATEGORY.replace(
            ':id',
            latestNewsCategories.id,
          ),
          {
            data: requestPayload,
          },
        );
      } else {
        response = await apiClient.post(
          API_END_POINTS.CREATE_LATEST_NEWS_CATEGORY,
          {
            data: requestPayload,
          },
        );
      }

      if (isSuccessResponse(response.statusCode)) {
        // merge backend response with submitted local payload so parent receives name/code/status
        const result = { ...(response.data as any), ...localPayload };
        onSubmit?.(result as ILatestNewsCategories);
        toast.success(
          latestNewsCategories
            ? 'Latest news category updated successfully'
            : 'Latest news category created successfully',
        );
      } else {
        toast.error('Failed to save latest news category');
      }
    } catch (error) {
      console.error('Error creating/updating latest news category:', error);
      toast.error('Failed to save latest news category');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle> Latest News Category Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new latest news category
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Category Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g. IT, Security, Finance"
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
              Category Description *
            </Label>
            <Textarea
              id="description"
              placeholder="Describe the category requirements"
              value={formData.description}
              onChange={e => handleInputChange('description', e.target.value)}
            />
            {errors.code && (
              <p className="text-sm text-destructive">{errors.code}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="status" className="font-medium text-foreground">
              Category Status *
            </Label>
            <Select
              value={formData.status}
              onValueChange={e => handleInputChange('status', e)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="DRAFT">DRAFT</SelectItem>
                <SelectItem value="ACTIVE">ACTIVE</SelectItem>
                <SelectItem value="INACTIVE">INACTIVE</SelectItem>
              </SelectContent>
            </Select>
            {errors.status && (
              <p className="text-sm text-destructive">{errors.status}</p>
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
              {loading ? 'Saving...' : 'Save Category'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionLatestNewsCategories;
