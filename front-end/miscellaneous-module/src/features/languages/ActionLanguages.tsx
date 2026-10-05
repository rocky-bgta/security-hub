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
import { ILanguage, ILanguagePayload } from 'models/Languages';
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
  language: ILanguage | null;
  onSubmit: (data: ILanguage) => void;
}

const DefaultLanguage = {
  displayName: '',
  code: '',
  active: true,
};

const ActionLanguages = ({ isOpen, onClose, language, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<ILanguagePayload>({
    ...DefaultLanguage,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      language
        ? {
            displayName: language.displayName,
            code: language.code,
            active: language.active,
          }
        : { ...DefaultLanguage },
    );
  }, [language, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.displayName.trim()) {
      newErrors.displayName = 'Language display name is required';
    }

    if (!formData.code.trim()) {
      newErrors.code = 'Language code is required';
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
      displayName: formData.displayName,
      code: formData.code,
      active: formData.active,
    };

    try {
      let response: IResponse<ILanguage>;
      if (language) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_LANGUAGE.replace(':id', language.id),
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_LANGUAGE, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success('Language created successfully');
      } else {
        toast.error('Failed to create language');
      }
    } catch (error) {
      console.error('Error creating/updating language:', error);
      toast.error('Failed to create language');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Language Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new language
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="displayName"
              className="font-medium text-foreground"
            >
              Language Display Name *
            </Label>
            <Input
              id="displayName"
              placeholder="e.g., English, Spanish, French"
              value={formData.displayName}
              onChange={e => handleInputChange('displayName', e.target.value)}
              className={errors.displayName ? 'has-error' : ''}
            />
            {errors.displayName && (
              <p className="text-sm text-destructive">{errors.displayName}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="code" className="font-medium text-foreground">
              Language Code *
            </Label>
            <Input
              id="code"
              placeholder="e.g., en, es, fr"
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
              Language Status
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
              {loading ? 'Creating...' : 'Create Language'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionLanguages;
