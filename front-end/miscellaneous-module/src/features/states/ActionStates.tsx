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
import { IState, IStatePayload } from 'models/States';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { ICountry } from 'models/Country';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  state: IState | null;
  onSubmit: (data: IState) => void;
}

const DefaultState = {
  name: '',
  countryId: '',
  displayOrder: 0,
  code: '',
  active: true,
};

const ActionStates = ({ isOpen, onClose, state, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IStatePayload>({
    ...DefaultState,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [countries, setCountries] = useState<ICountry[]>([]);
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      state
        ? {
            name: state.name,
            countryId: state.countryId,
            displayOrder: state.displayOrder,
            code: state.code,
            active: state.active,
          }
        : { ...DefaultState },
    );
    getCountries();
  }, [state, isOpen]);

  const getCountries = async () => {
    const response = await apiClient.get(API_END_POINTS.GET_COUNTRY_LIST);
    setCountries(response.data);
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'State name is required';
    }

    if (!formData.code.trim()) {
      newErrors.code = 'State code is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }) as IStatePayload);

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
      countryId: formData.countryId,
      displayOrder: formData.displayOrder,
      code: formData.code,
      active: formData.active,
    };

    try {
      let response: IResponse<IState>;
      if (state) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_STATE.replace(':id', state.id),
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_STATE, {
          data: payload,
        });
      }
      if (isSuccessResponse(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success('State created successfully');
      } else {
        toast.error('Failed to create state');
      }
    } catch (error) {
      console.error('Error creating/updating state:', error);
      toast.error('Failed to create state');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>State Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new state
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              State Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g., New York, California"
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
              State Code *
            </Label>
            <Input
              id="code"
              placeholder="e.g., NY, CA"
              value={formData.code}
              onChange={e => handleInputChange('code', e.target.value)}
              className={errors.code ? 'has-error' : ''}
            />
            {errors.code && (
              <p className="text-sm text-destructive">{errors.code}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label
              htmlFor="displayOrder"
              className="font-medium text-foreground"
            >
              Display Order *
            </Label>
            <Input
              id="displayOrder"
              type="number"
              min="0"
              placeholder=""
              value={formData.displayOrder}
              onChange={e => handleInputChange('displayOrder', e.target.value)}
              className={errors.displayOrder ? 'has-error' : ''}
            />
            {errors.displayOrder && (
              <p className="text-sm text-destructive">{errors.displayOrder}</p>
            )}
            <p className="text-xs text-muted-foreground">
              Higher numbers indicate higher display orders
            </p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="active" className="font-medium text-foreground">
              Country
            </Label>
            <Select
              value={formData.countryId.toString()}
              onValueChange={e => handleInputChange('countryId', e)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select Country" />
              </SelectTrigger>
              <SelectContent>
                {countries?.length > 0 &&
                  countries.map((country: ICountry) => (
                    <SelectItem key={country.id} value={country.id}>
                      {country.name}
                    </SelectItem>
                  ))}
                {countries.length < 1 && (
                  <SelectItem value="">No country found</SelectItem>
                )}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="active" className="font-medium text-foreground">
              State Status
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
              {loading ? 'Creating...' : 'Create State'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionStates;
