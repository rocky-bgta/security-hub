import { useEffect, useState } from 'react';

import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

import CustomPhoneInput from 'components/CustomPhoneInput';
import { UserPlus } from 'lucide-react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from 'hooks/UseAPI';
import { isSuccessResponse } from 'utils/Helper';
import { ICountry, IDropdownOption } from 'models/Dropdown';
import SearchSelect from 'components/SearchSelect';
import { IMSPDropdownData } from 'models/MSP';
import { IClientDropdownData } from 'models/Client';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { UserFormData, userSchema } from 'schemas/UserSchema';
import { Status } from 'models/Global';
import { toast } from 'react-toastify';

const Onboarding = () => {
  const apiClient = useAPI();
  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
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
  const [countryList, setCountryList] = useState<Array<ICountry>>([]);
  const [selectedCountry, setSelectedCountry] = useState('');
  const [selectedMSP, setSelectedMSP] = useState('');
  const [selectedClient, setSelectedClient] = useState('');
  const [mspList, setMspList] = useState<Array<IMSPDropdownData>>([]);
  const [clientList, setClientList] = useState<Array<IClientDropdownData>>([]);
  const [departmentList, setDepartmentList] = useState<
    Array<IDropdownOption>
  >([]);

  const fetchCountryList = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.COUNTRY_LIST);
      if (isSuccessResponse(response.statusCode)) {
        setCountryList(response.data);
      }
    } catch (error) {
      console.error('Error fetching country list:', error);
    }
  };

  useEffect(() => {
    setTimeout(() => {
      fetchCountryList();
    }, 0);
  }, []);

  const fetchDepartmentList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_DEPARTMENT_LIST +
        'active=true&isSystemDefined=true&pageSize=1000',
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
    setTimeout(() => {
      fetchDepartmentList();
    }, 0);
  }, []);

  const fetchMspList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.MSP_LIST_BY_COUNTRY +
        `country=${selectedCountry}&isClient=false`,
      );
      if (isSuccessResponse(response.statusCode)) {
        setMspList(response.data);
      }
    } catch (error) {
      console.error('Error fetching MSP list:', error);
    }
  };

  useEffect(() => {
    setTimeout(() => {
      if (selectedCountry) {
        fetchMspList();
      }
    }, 0);
  }, [selectedCountry]);

  const fetchClientList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_LIST_BY_MSP + `mspId=${selectedMSP}`,
      );
      if (isSuccessResponse(response.statusCode)) {
        setClientList(
          response.data.map((client: IClientDropdownData) => ({
            id: client.id,
            organizationName: client.organizationName,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching client list:', error);
    }
  };

  useEffect(() => {
    setTimeout(() => {
      if (selectedMSP) {
        fetchClientList();
      }
    }, 0);
  }, [selectedMSP]);

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
      clientAdminId: selectedClient,
    };

    try {
      const response = await apiClient.post(API_END_POINTS.ADD_END_USER, {
        data: createPayload,
      });

      if (response.statusCode === 409) {
        toast.error('User already exists');
        return;
      }

      if ([200, 201, 204].includes(response.statusCode)) {
        toast.success('User added successfully');
        reset();
      } else {
        toast.error('Something went wrong');
      }
    } catch (error) {
      toast.error('Something went wrong');
      console.error(error);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">
          Client User Onboarding
        </h1>
        <p className="text-muted-foreground">
          Onboard new Client Users under selected MSP and Client Admin
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <UserPlus className="size-5" />
            Onboard New Client User
          </CardTitle>
          <CardDescription>
            Select MSP and Client Admin, then provide user details to create a
            new account
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* MSP Country Selection */}
          <div className="space-y-2">
            <Label htmlFor="msp-country-select">Select MSP Country *</Label>

            <SearchSelect
              value={selectedCountry}
              onValueChange={setSelectedCountry}
              placeholder="Search and select MSP Country..."
              items={countryList.map(country => ({
                value: country.id,
                label: country.name,
              }))}
            />
          </div>
          {/* MSP Selection */}
          <div className="space-y-2">
            <Label htmlFor="msp-select">Select MSP *</Label>
            <SearchSelect
              value={selectedMSP}
              onValueChange={setSelectedMSP}
              placeholder="Search and select MSP..."
              items={mspList.map(msp => ({
                value: msp.id,
                label: msp.organizationName,
              }))}
              disabled={!selectedCountry}
            />
          </div>

          {/* Client Admin Selection */}
          <div className="space-y-2">
            <Label htmlFor="client-select">Select Client Admin *</Label>
            <SearchSelect
              value={selectedClient}
              onValueChange={setSelectedClient}
              disabled={!selectedMSP}
              placeholder="Search and select Client Admin..."
              items={clientList.map(client => ({
                value: client.id,
                label: client.organizationName,
              }))}
            />
          </div>

          {/* User Details Form */}
          {selectedClient && (
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
                    <Input
                      {...field}
                      id="lastName"
                      placeholder="Enter last name"
                    />
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
                      placeholder="user@example.com"
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
                        {departmentList.map(dept => (
                          <SelectItem key={dept.id} value={dept.name}>
                            {dept.name}
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
                        <SelectItem value={Status.INACTIVE}>
                          Inactive
                        </SelectItem>
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
                {isSubmitting ? 'Submitting...' : 'Add User'}
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default Onboarding;
