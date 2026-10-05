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
import { Switch } from 'common/Switch';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { ISupportTicketType } from 'models/Billing';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  supportTicketType: ISupportTicketType | null;
  onSubmit: (data: ISupportTicketType) => void;
}

const DefaultSupportTicketType = {
  id: '',
  name: '',
  description: '',
  active: true,
};

const ActionSupportTicketType = ({
  isOpen,
  onClose,
  supportTicketType,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<{
    id: string;
    name: string;
    description: string;
    active: boolean;
  }>({
    id: '',
    name: '',
    description: '',
    active: true,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    if (isOpen) {
      if (supportTicketType) {
        // Edit mode - use the data from the list
        setFormData({
          id: supportTicketType.id,
          name: supportTicketType.name,
          description: supportTicketType.description || '',
          active: supportTicketType.active,
        });
      } else {
        // Create mode - reset to default
        setFormData({ ...DefaultSupportTicketType });
      }
    }
  }, [supportTicketType, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Support ticket type name is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string | boolean) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
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
      name: formData.name,
      description: formData.description,
      active: formData.active,
    };

    try {
      if (supportTicketType) {
        const response = await apiClient.put(
          API_END_POINTS.UPDATE_SUPPORT_TICKET_TYPE.replace(
            ':id',
            supportTicketType.id,
          ),
          {
            data: payload,
          },
        );
        if (isSuccessResponse(response?.statusCode)) {
          onSubmit?.(response.data);
          toast.success(`Support ticket type updated successfully`);
        } else {
          toast.error(`Failed to update support ticket type`);
        }
      } else {
        const response = await apiClient.post(
          API_END_POINTS.CREATE_SUPPORT_TICKET_TYPE,
          {
            data: payload,
          },
        );
        if (isSuccessResponse(response?.statusCode)) {
          onSubmit?.(response.data);
          toast.success(`Support ticket type created successfully`);
        } else {
          toast.error(`Failed to create support ticket type`);
        }
      }
    } catch (error) {
      console.error('Error creating/updating support ticket type:', error);
      toast.error(
        `Failed to ${supportTicketType ? 'update' : 'create'} support ticket type`,
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Support Ticket Type Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to{' '}
            {supportTicketType ? 'update' : 'create a new'} support ticket type
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Support Ticket Type Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g., Technical Support"
              value={formData.name}
              onChange={e => handleInputChange('name', e.target.value)}
              className={errors.name ? 'has-error' : ''}
              disabled={loading}
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
              placeholder="Enter description..."
              value={formData.description}
              onChange={e => handleInputChange('description', e.target.value)}
              className="min-h-[100px]"
              disabled={loading}
            />
          </div>

          <div className="flex items-center justify-between space-x-2">
            <Label htmlFor="active" className="font-medium text-foreground">
              Active
            </Label>
            <Switch
              id="active"
              checked={formData.active}
              onCheckedChange={checked => handleInputChange('active', checked)}
              disabled={loading}
            />
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
                ? supportTicketType
                  ? 'Updating...'
                  : 'Creating...'
                : supportTicketType
                  ? 'Update Support Ticket Type'
                  : 'Create Support Ticket Type'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionSupportTicketType;
