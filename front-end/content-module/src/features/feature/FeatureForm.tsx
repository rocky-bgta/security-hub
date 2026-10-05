import { zodResolver } from '@hookform/resolvers/zod';
import { Fragment, useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { IFeatureDetails } from 'models/Feature';
import { Status } from 'models/Global';
import { ISelectOption } from 'models/Input';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  DefaultFeatureFormValues,
  FeatureFormSchema,
  TFeatureFormFields,
} from 'schemas/FeatureSchema';

type TFeatureFormProps = {
  formData?: TFeatureFormFields;
  onClose?: any;
  data?: IFeatureDetails;
  featureId?: string;
  onSubmit: () => void;
};

const FeatureForm = ({
  formData,
  onClose,
  featureId,
  onSubmit,
}: TFeatureFormProps) => {
  const {
    control,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm<TFeatureFormFields>({
    resolver: zodResolver(FeatureFormSchema),
    defaultValues: DefaultFeatureFormValues,
    mode: 'onChange',
  });
  const [submitting, setSubmitting] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    if (formData && featureId) {
      reset(formData);
    } else {
      reset(DefaultFeatureFormValues);
    }
  }, [formData, featureId]);

  const dropdowns: {
    [x: string]: Array<ISelectOption>;
  } = {
    status: [
      { id: '1', label: 'Enable', value: Status.ENABLED },
      { id: '2', label: 'Disable', value: Status.DISABLED },
    ],
    availability: [
      { id: 'Free', label: 'Free', value: 'FREE' },
      { id: 'Premium', label: 'Premium', value: 'PREMIUM' },
      { id: 'Restricted', label: 'Restricted', value: 'RESTRICTED' },
    ],
  };

  const handleSubmitForm = async (fields: TFeatureFormFields) => {
    setSubmitting(true);

    const payload = {
      featureName: fields.featureName,
      featureDescription: fields.featureDescription,
      packageIds: [],
      featureStatus: fields.featureStatus,
      availability: fields.availability,
    };

    let response;
    try {
      if (featureId) {
        response = await apiClient.put(
          API_END_POINTS.FEATURE_UPDATE + featureId,
          {
            data: payload,
          },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.FEATURE_CREATE, {
          data: payload,
        });
      }
      if ([200, 201].includes(response.statusCode)) {
        onSubmit();
        reset();
        onClose();
        toast.success(response.message);
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error create/update feature:', error);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form
      onSubmit={handleSubmit(handleSubmitForm)}
      className="content-my-6 content-grid content-grid-cols-1 content-gap-3 lg:content-grid-cols-2 lg:content-gap-x-10 lg:content-gap-y-6"
    >
      <div className="content-flex content-flex-col">
        <Controller
          name="featureName"
          control={control}
          render={({ field: { onChange, value } }) => (
            <Fragment>
              <Label htmlFor="title">Feature Name</Label>
              <Input
                key="title"
                id="title"
                name="Feature Title"
                value={value}
                placeholder="Feature Title"
                className={errors['featureName'] && 'content-has-error'}
                onChange={e => onChange(e.target.value)}
              />
            </Fragment>
          )}
        />
        {errors['featureName'] && (
          <p className="content-text-sm content-text-vibrant-red">
            {errors['featureName']?.message}
          </p>
        )}
      </div>

      <div className="content-flex content-flex-col">
        <Controller
          name="featureStatus"
          control={control}
          render={({ field: { onChange, value } }) => (
            <Fragment>
              <Label htmlFor="status">Feature Status</Label>{' '}
              <Select value={value} onValueChange={value => onChange(value)}>
                <SelectTrigger disabled={false}>
                  <SelectValue placeholder="Select a Status" />
                </SelectTrigger>
                <SelectContent>
                  {dropdowns['status'].map(option => (
                    <SelectItem key={option.id} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Fragment>
          )}
        />
        {errors['featureStatus'] && (
          <p className="content-text-sm content-text-vibrant-red">
            {errors['featureStatus']?.message}
          </p>
        )}
      </div>

      <div className="content-flex content-flex-col">
        <Controller
          name="featureDescription"
          control={control}
          render={({ field: { onChange, value } }) => (
            <Fragment>
              <Label htmlFor="description">
                Feature Description (optional)
              </Label>
              <textarea
                key="description"
                id="description"
                name="Feature Title"
                value={value}
                placeholder="Feature Description"
                className="content-h-56 content-rounded content-border content-border-card-border content-bg-transparent content-px-2.5 content-py-2 content-text-cloudy-white placeholder:content-text-sm"
                onChange={e => onChange(e.target.value)}
              />
            </Fragment>
          )}
        />
      </div>

      <div className="content-flex content-flex-col">
        <Controller
          name="availability"
          control={control}
          render={({ field: { onChange, value } }) => (
            <Fragment>
              <Label htmlFor="availability">Availability Settings</Label>
              <Select value={value} onValueChange={value => onChange(value)}>
                <SelectTrigger disabled={false}>
                  <SelectValue placeholder="Select Availability" />
                </SelectTrigger>
                <SelectContent>
                  {dropdowns['availability'].map(option => (
                    <SelectItem key={option.id} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Fragment>
          )}
        />
        {errors['availability'] && (
          <p className="content-text-sm content-text-vibrant-red">
            {errors['availability']?.message}
          </p>
        )}
      </div>

      <div className="content-my-10 content-flex content-justify-center content-gap-x-20 lg:content-col-span-2">
        <Button
          className="content-w-32"
          type="button"
          onClick={onClose}
          variant="destructive"
        >
          Discard
        </Button>

        <Button className="content-w-32" type="submit" disabled={submitting}>
          {submitting ? 'Saving...' : 'Save'}
        </Button>
      </div>
    </form>
  );
};

export default FeatureForm;
