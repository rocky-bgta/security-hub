import { zodResolver } from '@hookform/resolvers/zod';
import { lazy, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { useAPI } from 'hooks/UseAPI';
import { Lock } from 'lucide-react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  DefaultUpdatePasswordFormValues,
  TUpdatePasswordFormFields,
  TUpdatePasswordFormFieldsKeys,
  UpdatePasswordFormFields,
  UpdatePasswordSchema,
} from 'schemas/UpdatePasswordSchema';
import { isSuccessResponse } from 'utils/Helper';

const RemoteMFASettings = lazy(() => import('home-module/MFASettings'));

const ClientAdminUpdatePassword = () => {
  const {
    control,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm<TUpdatePasswordFormFields>({
    resolver: zodResolver(UpdatePasswordSchema),
    defaultValues: DefaultUpdatePasswordFormValues,
    mode: 'onChange',
  });
  const [submitting, setSubmitting] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleFormSubmit = async (fields: TUpdatePasswordFormFields) => {
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
      toast.error(error instanceof Error ? error.message : 'Failed to update password');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Account Management
        </h1>
        <p className="text-muted-foreground">
          Manage account settings and system configuration
        </p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Lock className="size-5" />
            Set New Password
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <form onSubmit={handleSubmit(handleFormSubmit)}>
            <div>
              <div>
                {UpdatePasswordFormFields.map((field, idx) => (
                  <div key={idx} className="mb-6 flex flex-col">
                    <Controller
                      name={field.key as TUpdatePasswordFormFieldsKeys}
                      control={control}
                      render={({ field: { onChange, value } }) => {
                        return (
                          <>
                            <Label
                              htmlFor={field.key}
                              className="text-base font-normal"
                            >
                              {field.name}
                            </Label>
                            <Input
                              key={field.key}
                              id={field.key}
                              type={field.type}
                              name={field.name}
                              value={value as string}
                              placeholder={field.name}
                              className={
                                errors[
                                field.key as TUpdatePasswordFormFieldsKeys
                                ] && 'has-error'
                              }
                              maxLength={65}
                              onChange={e => onChange(e.target.value)}
                            />
                          </>
                        );
                      }}
                    />

                    {errors[field.key as TUpdatePasswordFormFieldsKeys] && (
                      <p className="text-sm text-vibrant-red">
                        {
                          errors[field.key as TUpdatePasswordFormFieldsKeys]
                            ?.message
                        }
                      </p>
                    )}
                  </div>
                ))}
                <div className="flex justify-end">
                  <Button variant="default" type="submit" className="px-8 py-3">
                    {submitting ? 'Updating...' : 'Update Password'}
                  </Button>
                </div>
              </div>
            </div>
          </form>
        </CardContent>
      </Card>
      <ErrorBoundaryWrapper>
        <RemoteMFASettings />
      </ErrorBoundaryWrapper>
    </div>
  );
};

export default ClientAdminUpdatePassword;
