import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogTrigger,
} from 'common/Dialog';
import { Button } from 'common/Button';
import { UserPlus } from 'lucide-react';
import { Label } from 'common/Label';
import { Input } from 'common/Input';
import CustomPhoneInput from 'components/CustomPhoneInput';
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from 'common/Select';
import { Status } from 'models/Global';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { EMAIL_ADDRESS_MAX_LENGTH, isSuccessResponse } from 'utils/Helper';
import { useStore } from 'hooks/UseStore';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { userSchema, UserFormData } from 'schemas/UserSchema';
import { useParams } from 'react-router-dom';
import { IDropdownOption } from 'models/Dropdown';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
  selectedUserId: string;
}

const ActionModal = ({ isOpen, onClose, onSubmit, selectedUserId }: IProps) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();
  const { id } = useParams();
  const [departmentList, setDepartmentList] = useState<Array<IDropdownOption>>(
    [],
  );
  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
    reset,
  } = useForm<UserFormData>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: '',
      department: '',
      status: Status.ACTIVE,
    },
  });

  useEffect(() => {
    if (selectedUserId && isOpen) {
      fetchUser();
    } else if (isOpen && !selectedUserId) {
      reset({
        firstName: '',
        lastName: '',
        email: '',
        phoneNumber: '',
        department: '',
        status: Status.ACTIVE,
      });
    }
  }, [selectedUserId, isOpen]);

  const fetchDepartmentList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_DEPARTMENT_LIST +
        'active=true' +
        '&clientAdminId=' +
        userInfo.userId +
        '&isSystemDefined=true&pageSize=1000',
      );
      if (isSuccessResponse(response.statusCode)) {
        setDepartmentList(
          response.data.items.map((department: IDropdownOption) => ({
            id: department.name,
            name: department.name,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching department list:', error);
    }
  };

  useEffect(() => {
    fetchDepartmentList();
  }, []);

  const fetchUser = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.VIEW_END_USER + selectedUserId,
      );
      if (isSuccessResponse(response.statusCode)) {
        const userData = response.data;
        setValue('firstName', userData.firstName);
        setValue('lastName', userData.lastName);
        setValue('email', userData.email);
        setValue(
          'phoneNumber',
          (userData.phoneNumber || '').replace(/^\+/, ''),
        );
        setValue('department', userData.department);
        setValue('status', userData.status);
      }
    } catch (error) {
      console.error(error);
      toast.error('Failed to fetch user details');
    }
  };

  const onFormSubmit = async (data: UserFormData) => {
    const phoneNumber = data.phoneNumber.startsWith('+')
      ? data.phoneNumber
      : '+' + data.phoneNumber;

    const createPayload = {
      firstName: data.firstName.trim(),
      lastName: data.lastName.trim(),
      email: data.email,
      phoneNumber: phoneNumber.trim(),
      department: data.department,
      status: data.status,
      clientAdminId: id,
    };

    const editPayload = {
      ...createPayload,
      id: selectedUserId,
    };

    try {
      let response;
      if (selectedUserId) {
        response = await apiClient.put(API_END_POINTS.ADD_END_USER, {
          data: editPayload,
        });
      } else {
        response = await apiClient.post(API_END_POINTS.ADD_END_USER, {
          data: createPayload,
        });
      }

      if (!isSuccessResponse(response.statusCode)) {
        toast.error(response.message);
        return;
      }
      onClose();
      onSubmit();
      toast.success(response.message);
      reset();
    } catch (error) {
      console.error('Error submitting form:', error);
    }
  };

  const handleDialogClose = () => {
    reset();
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleDialogClose}>
      <DialogTrigger asChild>
        <Button>
          <UserPlus className="mr-2 size-4" />
          {selectedUserId ? 'Edit User' : 'Add User'}
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{selectedUserId ? 'Edit User' : 'Add User'}</DialogTitle>
          <DialogDescription>
            {selectedUserId
              ? 'Edit user information'
              : 'Add a new user account'}
          </DialogDescription>
        </DialogHeader>
        <div className="grid grid-cols-2 gap-4">
          <div className="col-span-1">
            <Label htmlFor="firstName">First Name *</Label>
            <Controller
              name="firstName"
              control={control}
              render={({ field }) => (
                <Input
                  {...field}
                  id="firstName"
                  placeholder="Enter first name"
                />
              )}
            />
            {errors.firstName && (
              <p className="mt-1 text-sm text-red-500">
                {errors.firstName.message}
              </p>
            )}
          </div>

          <div className="col-span-1">
            <Label htmlFor="lastName">Last Name *</Label>
            <Controller
              name="lastName"
              control={control}
              render={({ field }) => (
                <Input {...field} id="lastName" placeholder="Enter last name" />
              )}
            />
            {errors.lastName && (
              <p className="mt-1 text-sm text-red-500">
                {errors.lastName.message}
              </p>
            )}
          </div>
          <div className="col-span-1">
            <Label htmlFor="email">Email *</Label>
            <Controller
              name="email"
              control={control}
              render={({ field }) => (
                <Input
                  {...field}
                  id="email"
                  type="email"
                  disabled={!!selectedUserId}
                  placeholder="name@domain.com"
                  maxLength={EMAIL_ADDRESS_MAX_LENGTH}
                />
              )}
            />
            {errors.email && (
              <p className="mt-1 text-sm text-red-500">
                {errors.email.message}
              </p>
            )}
          </div>

          <div>
            <Label htmlFor="phone">Phone Number *</Label>
            <Controller
              name="phoneNumber"
              control={control}
              render={({ field }) => (
                <CustomPhoneInput
                  value={field.value}
                  handleChange={field.onChange}
                />
              )}
            />
            {errors.phoneNumber && (
              <p className="mt-1 text-sm text-red-500">
                {errors.phoneNumber.message}
              </p>
            )}
          </div>

          <div>
            <Label htmlFor="department">Department *</Label>
            <Controller
              name="department"
              control={control}
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select department" />
                  </SelectTrigger>
                  <SelectContent>
                    {departmentList.map(department => (
                      <SelectItem key={department.id} value={department.id}>
                        {department.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.department && (
              <p className="mt-1 text-sm text-red-500">
                {errors.department.message}
              </p>
            )}
          </div>

          <div>
            <Label htmlFor="status">Status</Label>
            <Controller
              name="status"
              control={control}
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={Status.ACTIVE}>Active</SelectItem>
                    <SelectItem value={Status.INACTIVE}>Inactive</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>

          <Button
            type="button"
            onClick={handleSubmit(onFormSubmit)}
            disabled={isSubmitting}
            className="col-span-2 mt-10 w-full"
          >
            {isSubmitting
              ? 'Submitting...'
              : selectedUserId
                ? 'Update User'
                : 'Add User'}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ActionModal;
