import { zodResolver } from '@hookform/resolvers/zod';
import { Fragment, lazy, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import Border from 'components/UserBorder';
import { useAPI } from 'hooks/UseAPI';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  ChangePasswordFormFields,
  ChangePasswordSchema,
  DefaultChangePasswordFormValues,
  TChangePasswordFormFields,
  TChangePasswordFormFieldsKeys,
} from 'schemas/ChangePasswordSchema';
import { isSuccessResponse } from 'utils/Helper';

const RemoteMFASettings = lazy(() => import('home-module/MFASettings'));

const ChangePassword = () => {
  const {
    control,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm<TChangePasswordFormFields>({
    resolver: zodResolver(ChangePasswordSchema),
    defaultValues: DefaultChangePasswordFormValues,
    mode: 'onChange',
  });
  const [submitting, setSubmitting] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleFormSubmit = async (fields: TChangePasswordFormFields) => {
    setSubmitting(true);

    try {
      const response = await apiClient.post(API_END_POINTS.UPDATE_PASSWORD, {
        data: {
          currentPassword: fields.OldPassword,
          newPassword: fields.NewPassword,
          confirmPassword: fields.ConfirmPassword,
        },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      reset();
      toast.success('Password updated successfully');
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : 'Failed to update password',
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Fragment>
      <h2 className="content-mb-4 content-text-lg content-text-white sm:content-mb-6 sm:content-text-xl">
        Account Security
      </h2>
      <Border>
        <div className="content-p-4 sm:content-p-6">
          <h2 className="content-mb-4 content-text-lg content-text-white sm:content-mb-6 sm:content-text-xl">
            Password Settings
          </h2>
          <form onSubmit={handleSubmit(handleFormSubmit)}>
            <div className="content-grid content-grid-cols-1 content-gap-0 sm:content-grid-cols-2">
              <div>
                {ChangePasswordFormFields.map((field, idx) => (
                  <div
                    key={idx}
                    className="content-mb-4 content-flex content-flex-col sm:content-mb-6"
                  >
                    <Controller
                      name={field.key as TChangePasswordFormFieldsKeys}
                      control={control}
                      render={({ field: { onChange, value } }) => {
                        return (
                          <>
                            <Label
                              htmlFor={field.key}
                              className="content-text-base content-font-normal content-text-cloudy-white"
                            >
                              {field.name}
                            </Label>
                            <Input
                              key={field.key}
                              id={field.key}
                              type={field.type}
                              name={field.name}
                              value={value as string}
                              placeholder={field.placeholder}
                              maxLength={65}
                              className={
                                errors[
                                  field.key as TChangePasswordFormFieldsKeys
                                ] && 'content-has-error'
                              }
                              onChange={e => onChange(e.target.value)}
                            />
                          </>
                        );
                      }}
                    />

                    {errors[field.key as TChangePasswordFormFieldsKeys] && (
                      <p className="content-text-sm content-text-red-500">
                        {
                          errors[field.key as TChangePasswordFormFieldsKeys]
                            ?.message
                        }
                      </p>
                    )}
                  </div>
                ))}
                <Button type="submit" className="content-px-8 content-py-3">
                  {submitting ? 'Updating Password...' : 'Update Password'}
                </Button>
              </div>
            </div>
          </form>
        </div>
      </Border>
      <div className="content-mt-6 sm:content-mt-8">
        <ErrorBoundaryWrapper>
          <RemoteMFASettings />
        </ErrorBoundaryWrapper>
      </div>
    </Fragment>
  );
};

export default ChangePassword;
