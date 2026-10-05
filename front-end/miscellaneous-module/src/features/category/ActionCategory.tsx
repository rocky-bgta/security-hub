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
import { ICategory, ICategoryPayload } from 'models/Category';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  category: ICategory | null;
  onSubmit: (data: ICategory) => void;
}

const DefaultCategory = {
  categoryName: '',
  description: '',
  sortOrder: 1,
};

const ActionCategory = ({ isOpen, onClose, category, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<ICategoryPayload>({
    ...DefaultCategory,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      category
        ? {
            categoryName: category.categoryName,
            description: category.description,
            sortOrder: category.sortOrder,
          }
        : { ...DefaultCategory },
    );
  }, [category, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.categoryName.trim()) {
      newErrors.categoryName = 'Category name is required';
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

    try {
      let response: IResponse<ICategory>;
      if (category) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_CATEGORY.replace(':id', category.id),
          {
            data: formData,
          },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_CATEGORY, {
          data: formData,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success('Category created successfully');
      } else {
        toast.error('Failed to create category');
      }
    } catch (error) {
      console.error('Error creating/updating category:', error);
      toast.error('Failed to create category');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Category Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new category
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="categoryName"
              className="font-medium text-foreground"
            >
              Category Name *
            </Label>
            <Input
              id="categoryName"
              placeholder="e.g., Security, Marketing"
              value={formData.categoryName}
              onChange={e => handleInputChange('categoryName', e.target.value)}
              className={errors.categoryName ? 'has-error' : ''}
            />
            {errors.categoryName && (
              <p className="text-sm text-destructive">{errors.categoryName}</p>
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
              {loading ? 'Creating...' : 'Create Category'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionCategory;
