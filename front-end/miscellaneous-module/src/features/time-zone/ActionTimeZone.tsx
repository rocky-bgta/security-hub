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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { ICountry } from 'models/Country';
import { ITimeZone, ITimeZonePayload } from 'models/TimeZone';
import { IState } from 'models/States';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  timeZone: ITimeZone | null;
  onSubmit: (data: ITimeZone) => void;
}

const DefaultTimeZone = {
  countryId: '',
  stateId: '',
  timezoneId: '',
  displayName: '',
  displayOrder: 0,
  active: true,
};

const ActionTimeZone = ({ isOpen, onClose, timeZone, onSubmit }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<ITimeZonePayload>({
    ...DefaultTimeZone,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [countries, setCountries] = useState<ICountry[]>([]);
  const [states, setStates] = useState<IState[]>([]);
  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      timeZone
        ? {
            displayName: timeZone.displayName,
            countryId: timeZone.countryId,
            stateId: timeZone.stateId,
            displayOrder: timeZone.displayOrder,
            timezoneId: timeZone.timezoneId,
            active: timeZone.active,
          }
        : { ...DefaultTimeZone },
    );
    if (isOpen) {
      getCountries();
    }
  }, [timeZone, isOpen]);

  useEffect(() => {
    if (formData.countryId && isOpen) {
      getStates();
    }
  }, [formData.countryId, isOpen]);

  const getStates = async () => {
    const response = await apiClient.get(
      API_END_POINTS.GET_STATE_LIST_BY_COUNTRY.replace(
        ':id',
        formData.countryId,
      ),
    );
    setStates(response.data);
  };

  const getCountries = async () => {
    const response = await apiClient.get(API_END_POINTS.GET_COUNTRY_LIST);
    setCountries(response.data);
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.displayName.trim()) {
      newErrors.displayName = 'Time zone display name is required';
    }

    if (!formData.countryId.trim()) {
      newErrors.countryId = 'Country id is required';
    }

    if (!formData.stateId.trim()) {
      newErrors.stateId = 'State id is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }) as ITimeZonePayload);

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
      countryId: formData.countryId,
      stateId: formData.stateId,
      displayOrder: formData.displayOrder,
      timezoneId: formData.timezoneId,
      active: formData.active,
    };

    try {
      let response: IResponse<ITimeZone>;
      if (timeZone) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_TIME_ZONE.replace(':id', timeZone.id),
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_TIME_ZONE, {
          data: payload,
        });
      }
      if ([200, 201].includes(response.statusCode)) {
        onSubmit?.(response.data);
        toast.success('Time zone created successfully');
      } else {
        toast.error('Time zone already exists');
      }
    } catch (error) {
      console.error('Error creating/updating time zone:', error);
      toast.error('Time zone already exists');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Time Zone Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new time zone
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label
              htmlFor="displayName"
              className="font-medium text-foreground"
            >
              Time Zone Name *
            </Label>
            <Input
              id="displayName"
              placeholder="e.g., America/New_York"
              value={formData.displayName}
              onChange={e => handleInputChange('displayName', e.target.value)}
              className={errors.displayName ? 'has-error' : ''}
            />
            {errors.displayName && (
              <p className="text-sm text-destructive">{errors.displayName}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="timezoneId" className="font-medium text-foreground">
              Time Zone Code *
            </Label>
            <Input
              id="timezoneId"
              placeholder="e.g., America/New_York"
              value={formData.timezoneId}
              onChange={e => handleInputChange('timezoneId', e.target.value)}
              className={errors.timezoneId ? 'has-error' : ''}
            />
            {errors.timezoneId && (
              <p className="text-sm text-destructive">{errors.timezoneId}</p>
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
            <Label htmlFor="countryId" className="font-medium text-foreground">
              Country *
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
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="stateId" className="font-medium text-foreground">
              State *
            </Label>
            <Select
              value={formData.stateId.toString()}
              onValueChange={e => handleInputChange('stateId', e)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select State" />
              </SelectTrigger>
              <SelectContent>
                {states?.length > 0 &&
                  states.map((state: IState) => (
                    <SelectItem key={state.id} value={state.id}>
                      {state.name}
                    </SelectItem>
                  ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="active" className="font-medium text-foreground">
              Time Zone Status
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
              {loading ? 'Creating...' : 'Create Time Zone'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionTimeZone;
