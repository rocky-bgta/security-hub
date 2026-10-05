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
import { IResponse } from 'models/Global';
import { ITag, ITagPayload } from 'models/Tag';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  tag: ITag | null;
  onSubmit: (data: ITag) => void;
}

const DefaultTag: ITagPayload = {
  name: '',
  description: '',
};

const ActionTag = ({ isOpen, onClose, tag, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<ITagPayload>({ ...DefaultTag });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const apiClient = useAPI();

  useEffect(() => {
    setErrors({});
    setFormData(
      tag
        ? {
            name: tag.name,
            description: tag.description ?? '',
          }
        : { ...DefaultTag },
    );
  }, [tag, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Product tag name is required';
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

    const payload: ITagPayload = {
      name: formData.name.trim(),
      description: formData.description.trim(),
    };

    try {
      let response: IResponse<ITag>;
      if (tag) {
        response = await apiClient.post(
          API_END_POINTS.UPDATE_TAG.replace(':id', tag.id),
          { data: payload },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_TAG, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.({
          ...tag,
          ...payload,
          ...response.data,
        } as ITag);
        toast.success(
          tag
            ? 'Product tag updated successfully'
            : 'Product tag created successfully',
        );
      } else {
        toast.error(tag ? response.message : response.message);
      }
    } catch (error) {
      console.error('Error creating/updating tag:', error);
      toast.error(
        tag ? 'Failed to update product tag' : 'Failed to create product tag',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Product Tag Information</DialogTitle>
          <DialogDescription>
            {tag
              ? 'Update the product tag details below'
              : 'Fill in the details below to create a new product tag'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Product Tag Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g., Security, Phishing"
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
              htmlFor="description"
              className="font-medium text-foreground"
            >
              Description
            </Label>
            <Textarea
              id="description"
              placeholder="Brief description of the product tag"
              value={formData.description}
              onChange={e => handleInputChange('description', e.target.value)}
              className="min-h-[80px]"
              maxLength={255}
            />
            <p className="text-xs text-muted-foreground">
              {formData.description.length}/255 characters
            </p>
          </div>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button variant="outline" onClick={onClose} type="button">
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              <Save className="mr-2 size-4" />
              {loading
                ? 'Saving...'
                : tag
                  ? 'Update Product Tag'
                  : 'Create Product Tag'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionTag;
