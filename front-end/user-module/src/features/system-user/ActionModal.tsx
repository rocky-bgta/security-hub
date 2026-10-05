import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from 'common/Dialog';
import { Button } from 'common/Button';
import { Label } from 'common/Label';
import { Input } from 'common/Input';
import SearchSelect from 'components/SearchSelect';
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from 'common/Select';
import { IResponse, Status } from 'models/Global';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse, isValidEmail } from 'utils/Helper';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  SystemUserFormSchema,
  DefaultSystemUserFormValues,
  SystemUserFormFields,
  TSystemUserFormFields,
  TSystemUserFormFieldKeys,
} from 'schemas/UserSchema';
import { IDropdownOption } from 'models/Dropdown';
import { ISystemUser } from 'models/User';
import Loader from 'common/loader/Loader';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
  selectedUserId?: string;
}

interface IRole {
  id: string;
  roleName: string;
  description: string;
  accessLevel: number;
  colorTheme: string;
  status: string;
  userCount: number;
  createdAt?: string;
  updatedAt?: string;
}

const ActionModal = ({
  isOpen,
  onClose,
  onSubmit,
  selectedUserId = '',
}: IProps) => {
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [departmentList, setDepartmentList] = useState<Array<IDropdownOption>>(
    [],
  );
  const [countryList, setCountryList] = useState<Array<IDropdownOption>>([]);
  const [roleList, setRoleList] = useState<Array<IDropdownOption>>([]);
  const [userDetails, setUserDetails] = useState<ISystemUser | null>(null);
  const [isCheckingEmail, setIsCheckingEmail] = useState<boolean>(false);

  const apiClient = useAPI();

  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting, isValid },
    setValue,
    reset,
    watch,
    setError,
    clearErrors,
  } = useForm<TSystemUserFormFields>({
    resolver: zodResolver(SystemUserFormSchema),
    defaultValues: DefaultSystemUserFormValues,
    mode: 'onChange',
  });

  const email = watch('email');
  const debouncedEmail = useDebounce(email, 500);

  const fetchUser = useCallback(async () => {
    try {
      const response: IResponse<ISystemUser> = await apiClient.get(
        API_END_POINTS.GET_SYSTEM_USER.replace(':id', selectedUserId),
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to fetch user details');
      }

      setUserDetails(response.data);
      reset({
        ...DefaultSystemUserFormValues,
        ...(response.data as unknown as Partial<TSystemUserFormFields>),
      });
    } catch (error) {
      console.error(error);
      toast.error((error as Error).message);
    }
  }, [apiClient, selectedUserId, reset]);

  const validateEmailExists = useCallback(
    async (emailToCheck: string) => {
      setIsCheckingEmail(true);

      try {
        const response = await apiClient.get(
          API_END_POINTS.CHECK_USER_EXISTS +
            encodeURIComponent(emailToCheck.trim()),
        );

        if (response.data) {
          setError('email', {
            type: 'manual',
            message: 'A user with this email already exists',
          });
          return false;
        }

        clearErrors('email');
        return true;
      } catch (error) {
        console.error('Error validating email:', error);
        setError('email', {
          type: 'manual',
          message: 'Unable to validate email. Please try again.',
        });
        return false;
      } finally {
        setIsCheckingEmail(false);
      }
    },
    [apiClient, clearErrors, setError],
  );

  useEffect(() => {
    if (selectedUserId || !isOpen) return;
    if (!debouncedEmail || !isValidEmail(debouncedEmail)) return;

    let cancelled = false;
    queueMicrotask(async () => {
      if (cancelled) return;
      await validateEmailExists(debouncedEmail);
    });

    return () => {
      cancelled = true;
    };
  }, [debouncedEmail, isOpen, selectedUserId, validateEmailExists]);

  useEffect(() => {
    if (selectedUserId && isOpen) {
      fetchUser();
    }
  }, [selectedUserId, isOpen, fetchUser]);

  useEffect(() => {
    const fetchDropdownLists = async () => {
      const [departmentResult, countryResult, roleResult] =
        await Promise.allSettled([
          apiClient.get(
            API_END_POINTS.GET_DEPARTMENT_LIST +
              'active=true&isSystemDefined=true&pageSize=1000',
          ) as Promise<IResponse<{ items: Array<IDropdownOption> }>>,
          apiClient.get(API_END_POINTS.GET_ACTIVE_COUNTRY_LIST) as Promise<
            IResponse<Array<IDropdownOption>>
          >,
          apiClient.get(
            API_END_POINTS.GET_ROLE_LIST + 'sortBy=accessLevel&order=desc',
          ) as Promise<IResponse<Array<IRole>>>,
        ]);

      if (departmentResult.status === 'fulfilled') {
        if (isSuccessResponse(departmentResult.value.statusCode)) {
          setDepartmentList(
            departmentResult.value.data.items.map(
              (department: IDropdownOption) => ({
                id: department.name,
                name: department.name,
              }),
            ),
          );
        } else {
          console.error(
            'Failed to fetch department list:',
            departmentResult.value.message,
          );
        }
      } else {
        console.error(
          'Error fetching department list:',
          departmentResult.reason,
        );
      }

      if (countryResult.status === 'fulfilled') {
        if (isSuccessResponse(countryResult.value.statusCode)) {
          setCountryList(
            countryResult.value.data.map((country: IDropdownOption) => ({
              id: country.name,
              name: country.name,
            })),
          );
        } else {
          console.error(
            'Failed to fetch country list:',
            countryResult.value.message,
          );
        }
      } else {
        console.error('Error fetching country list:', countryResult.reason);
      }

      if (roleResult.status === 'fulfilled') {
        if (isSuccessResponse(roleResult.value.statusCode)) {
          setRoleList(
            roleResult.value.data.map(role => ({
              id: role.id,
              name: role.roleName,
            })),
          );
          if (selectedUserId) {
            setValue('roleIds', [
              roleResult.value.data.find(
                role => role.roleName === userDetails?.roles?.[0],
              )?.id || '',
            ]);
          }
        } else {
          console.error('Failed to fetch role list:', roleResult.value.message);
        }
      } else {
        console.error('Error fetching role list:', roleResult.reason);
      }

      setIsLoading(false);
    };

    fetchDropdownLists();
  }, [apiClient, setValue, selectedUserId, userDetails]);

  const onFormSubmit = async (data: TSystemUserFormFields) => {
    const createPayload = {
      firstName: data.firstName.trim(),
      lastName: data.lastName.trim(),
      email: data.email.trim(),
      companyName: data.companyName.trim(),
      designation: data.designation.trim(),
      department: data.department,
      country: data.country,
      supervisorName: data.supervisorName.trim(),
      roleIds: data.roleIds,
      status: data.status,
    };

    const editPayload = {
      ...createPayload,
      id: selectedUserId,
    };

    try {
      let response;
      if (selectedUserId) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_SYSTEM_USER.replace(':id', selectedUserId),
          {
            data: editPayload,
          },
        );
      } else {
        const isEmailAvailable = await validateEmailExists(data.email.trim());
        if (!isEmailAvailable) throw new Error('Email already exists');

        response = await apiClient.post(API_END_POINTS.ADD_SYSTEM_USER, {
          data: createPayload,
        });
      }

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to submit system user');
      }

      toast.success(response.message);
      onSubmit();
      handleDialogClose();
    } catch (error) {
      console.error('Error submitting system user:', error);
      toast.error((error as Error).message || 'Failed to submit system user');
    }
  };

  const handleDialogClose = () => {
    reset();
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleDialogClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {selectedUserId ? 'Edit System User' : 'Add System User'}
          </DialogTitle>
          <DialogDescription>
            {selectedUserId
              ? 'Edit system user information'
              : 'Add a new system user account'}
          </DialogDescription>
        </DialogHeader>
        {isLoading ? (
          <div className="relative flex h-40 items-center justify-center">
            <Loader mode="container" />
          </div>
        ) : (
          <form
            className="grid grid-cols-2 gap-4"
            onSubmit={handleSubmit(onFormSubmit)}
          >
            {SystemUserFormFields.map(formField => {
              const fieldKey = formField.key as TSystemUserFormFieldKeys;
              const isSearchableDropdown = ['department', 'country'].includes(
                formField.key,
              );
              const isDropdown = [
                'department',
                'country',
                'roleIds',
                'status',
              ].includes(formField.key);

              const dropdownOptions: Array<IDropdownOption> =
                formField.key === 'department'
                  ? departmentList
                  : formField.key === 'country'
                    ? countryList
                    : formField.key === 'roleIds'
                      ? roleList
                      : [
                          { id: Status.ACTIVE, name: 'Active' },
                          { id: Status.INACTIVE, name: 'Inactive' },
                        ];

              return (
                <div key={formField.key} className="col-span-1 space-y-2">
                  <Label htmlFor={formField.key} required={formField.required}>
                    {formField.name}
                  </Label>

                  <Controller
                    name={fieldKey}
                    control={control}
                    render={({ field }) => {
                      if (isSearchableDropdown) {
                        return (
                          <SearchSelect
                            value={field.value as string}
                            onValueChange={field.onChange}
                            placeholder={formField.placeholder}
                            items={dropdownOptions.map(option => ({
                              value: option.id,
                              label: option.name,
                            }))}
                            hasError={!!errors[fieldKey]}
                          />
                        );
                      }

                      if (isDropdown) {
                        const value =
                          formField.key === 'roleIds'
                            ? field.value[0]
                            : (field.value as string);

                        return (
                          <Select
                            value={value}
                            onValueChange={value => {
                              if (formField.key === 'roleIds') {
                                field.onChange(value ? [value] : []);
                                return;
                              }
                              field.onChange(value);
                            }}
                          >
                            <SelectTrigger>
                              <SelectValue
                                placeholder={formField.placeholder}
                              />
                            </SelectTrigger>
                            <SelectContent>
                              {dropdownOptions.map(option => (
                                <SelectItem key={option.id} value={option.id}>
                                  {option.name}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        );
                      }

                      return (
                        <Input
                          {...field}
                          id={formField.key}
                          type={formField.key === 'email' ? 'email' : 'text'}
                          value={String(field.value || '')}
                          disabled={
                            formField.key === 'email' && !!selectedUserId
                          }
                          placeholder={`Enter ${formField.placeholder.toLowerCase()}`}
                          onChange={e => {
                            field.onChange(e);
                            if (
                              formField.key === 'email' &&
                              !selectedUserId &&
                              errors.email?.type === 'manual'
                            ) {
                              clearErrors('email');
                            }
                          }}
                        />
                      );
                    }}
                  />

                  {errors[fieldKey] && (
                    <p className="mt-1 text-sm text-red-500">
                      {String(errors[fieldKey]?.message || '')}
                    </p>
                  )}
                </div>
              );
            })}

            <Button
              type="submit"
              disabled={isSubmitting || isCheckingEmail || !isValid}
              className="col-span-2 mt-10 w-full"
            >
              {isSubmitting
                ? 'Submitting...'
                : selectedUserId
                  ? 'Update User'
                  : 'Add User'}
            </Button>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ActionModal;
