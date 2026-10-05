import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2 } from 'lucide-react';
import { Fragment, useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';

import { Button } from 'common/Button';
import { Label } from 'common/Label';
import Border from 'components/UserBorder';

import { UploadIcon } from 'assets/icons';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import CustomPhoneInput from 'components/CustomPhoneInput';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { useUploader } from 'hooks/UseUploader';
import { IDropdownOption } from 'models/DropDown';
import { FileType, IResponse } from 'models/Global';
import { IUserInfo } from 'models/Users';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  DefaultProfileSettingsValues,
  normalizePhoneValue,
  ProfileSettingsFormFields,
  ProfileSettingsSchema,
  TProfileSettingsFormFields,
} from 'schemas/ProfileSettingsSchema';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { isSuccessResponse } from 'utils/Helper';

const ProfileSettings = () => {
  const { userInfo, setUserInfo } = useStore();
  const apiClient = useAPI();

  const {
    control,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm<TProfileSettingsFormFields>({
    resolver: zodResolver(ProfileSettingsSchema),
    defaultValues: DefaultProfileSettingsValues,
    mode: 'onChange',
  });

  const [submitting, setSubmitting] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [userDetails, setUserDetails] = useState<IUserInfo>();
  const [profileImage, setProfileImage] = useState<{
    link: string;
    selectedFile: File | null;
  }>({ link: '', selectedFile: null });
  const { uploadFile } = useUploader();
  const [departmentList, setDepartmentList] = useState<Array<IDropdownOption>>(
    [],
  );

  const fetchDepartmentList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_DEPARTMENT_LIST +
          'active=true' +
          '&clientAdminId=' +
          userInfo?.clientAdminId +
          '&isSystemDefined=true&pageSize=1000',
      );
      if (isSuccessResponse(response.statusCode)) {
        setDepartmentList(
          response.data?.items?.map((item: IDropdownOption) => ({
            id: item.name,
            name: item.name,
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

  useEffect(() => {
    fetchUserInfo(true);
  }, []);

  const fetchUserInfo = async (showLoader = false) => {
    if (showLoader) setIsLoading(true);
    try {
      const response: IResponse<IUserInfo> = await apiClient.get(
        API_END_POINTS.GET_USER_INFO.replace(':userId', userInfo?.userId),
      );
      reset({
        FirstName: response.data?.firstName,
        LastName: response.data?.lastName,
        Email: response.data?.email,
        Phone: normalizePhoneValue(response.data?.phoneNumber),
        Department: response.data?.department,
      });
      setProfileImage({
        link: response.data?.profilePicture || '',
        selectedFile: null,
      });
      setUserDetails(response.data);
    } catch (error) {
      console.error('Failed to fetch user info:', error);
    } finally {
      if (showLoader) setIsLoading(false);
    }
  };

  const handleFormSubmit = async (fields: TProfileSettingsFormFields) => {
    setSubmitting(true);

    let profileImageLink = profileImage?.link;

    if (profileImage.selectedFile) {
      const { url, error } = await uploadFile(
        profileImage.selectedFile,
        FileType.THUMBNAIL as FileType,
      );
      if (error) {
        toast.error(error);
        setSubmitting(false);
        return;
      }
      profileImageLink = url;
    }

    const payload = {
      id: userDetails?.id,
      firstName: fields.FirstName,
      lastName: fields.LastName,
      phoneNumber: fields.Phone,
      department: fields.Department,
      profilePicture: profileImageLink,
    };

    try {
      const response: IResponse<IUserInfo> = await apiClient.put(
        API_END_POINTS.UPDATE_USER_INFO,
        {
          data: payload,
        },
      );
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Profile updated successfully');
        setUserInfo((prev: typeof userInfo) => ({
          ...prev,
          profilePicture: profileImageLink ?? prev.profilePicture,
          fullName: `${fields.FirstName} ${fields.LastName}`.trim(),
          phoneNumber: fields.Phone,
        }));
        fetchUserInfo();
      } else {
        toast.error('Failed to update profile');
      }
    } catch (error) {
      console.error('Error updating profile', error);
      toast.error('Failed to update profile');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Fragment>
      <h2 className="content-mb-4 content-text-lg content-text-white sm:content-mb-6 sm:content-text-xl">
        Profile Settings
      </h2>
      {isLoading ? (
        <Border>
          <div className="content-flex content-flex-col content-items-center content-justify-center content-gap-3 content-py-12 sm:content-py-16">
            <Loader2 className="content-size-8 content-animate-spin content-text-primary" />
            <p className="content-text-sm content-text-cloudy-white">
              Loading profile settings...
            </p>
          </div>
        </Border>
      ) : (
        <>
          <div className="content-mb-6 sm:content-mb-8">
            <Border>
              <div className="content-flex content-flex-col content-items-start content-gap-4 content-px-4 content-py-4 sm:content-flex-row sm:content-items-center sm:content-gap-6 sm:content-px-8 sm:content-py-6">
                <img
                  src={
                    profileImage.selectedFile || profileImage.link
                      ? profileImage.selectedFile
                        ? URL.createObjectURL(profileImage.selectedFile)
                        : FILE_PATH_PREFIX + profileImage.link
                      : 'https://placehold.co/80x80'
                  }
                  alt="Profile-img"
                  className="content-size-16 content-rounded-full content-object-cover sm:content-size-20"
                />
                <label
                  htmlFor="profile-image"
                  className="content-flex content-cursor-pointer content-items-center content-gap-2 content-rounded-md content-border content-border-card-border content-bg-dark-blue content-px-3 content-py-2 content-text-sm content-font-normal content-text-cloudy-white hover:content-bg-[#EFF4FB40] sm:content-px-4 sm:content-text-base"
                >
                  <UploadIcon />
                  Upload Photo
                  <Input
                    type="file"
                    id="profile-image"
                    name="profile-image"
                    className="content-hidden"
                    accept="image/png, image/jpeg, image/jpg"
                    onChange={e => {
                      setProfileImage({
                        link: '',
                        selectedFile: e.target.files?.[0] ?? null,
                      });
                    }}
                  />
                </label>
              </div>
            </Border>
          </div>
          <Border>
            <div className="content-p-4 sm:content-p-6">
              <form onSubmit={handleSubmit(handleFormSubmit)}>
                {ProfileSettingsFormFields.map((item: any, idx: number) => (
                  <div key={idx} className="content-mb-6 sm:content-mb-8">
                    <Border>
                      <div className="content-p-4 sm:content-p-6">
                        <h2 className="content-mb-4 content-text-lg content-text-white sm:content-mb-6 sm:content-text-xl">
                          {item?.section}
                        </h2>
                        <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-grid-cols-2 sm:content-gap-6 lg:content-grid-cols-3">
                          {item?.fields.map((field: any, fieldIdx: number) => (
                            <div
                              key={fieldIdx}
                              className="content-flex content-flex-col"
                            >
                              <Controller
                                name={
                                  field.key as keyof TProfileSettingsFormFields
                                }
                                control={control}
                                render={({ field: { onChange, value } }) => {
                                  if (
                                    [
                                      'Country',
                                      'TimeZone',
                                      'Language',
                                      'Department',
                                    ].includes(field.key)
                                  ) {
                                    return (
                                      <>
                                        <Label
                                          htmlFor={field.key}
                                          className="content-text-base content-font-normal content-text-cloudy-white"
                                        >
                                          {field.name}
                                          {field.required ? ' *' : ''}
                                        </Label>
                                        <Select
                                          value={value as string}
                                          onValueChange={onChange}
                                        >
                                          <SelectTrigger>
                                            <SelectValue
                                              placeholder={field.placeholder}
                                            />
                                          </SelectTrigger>
                                          <SelectContent>
                                            {departmentList.map(item => (
                                              <SelectItem
                                                key={item.name}
                                                value={item.name}
                                              >
                                                {item.name}
                                              </SelectItem>
                                            ))}
                                          </SelectContent>
                                        </Select>
                                      </>
                                    );
                                  }
                                  if (['Phone'].includes(field.key)) {
                                    return (
                                      <>
                                        <Label
                                          htmlFor={field.key}
                                          className="content-text-base content-font-normal content-text-cloudy-white"
                                        >
                                          {field.name}
                                          {field.required ? ' *' : ''}
                                        </Label>
                                        <CustomPhoneInput
                                          key={field.key}
                                          // id={field.key}
                                          // name={field.name}
                                          value={value as string}
                                          placeholder={field.placeholder}
                                          handleChange={onChange}
                                        />
                                      </>
                                    );
                                  }
                                  return (
                                    <>
                                      <Label
                                        htmlFor={field.key}
                                        className="content-text-base content-font-normal content-text-cloudy-white"
                                      >
                                        {field.name}
                                        {field.required ? ' *' : ''}
                                      </Label>
                                      <Input
                                        disabled={[
                                          'Email',
                                          'OrganizationName',
                                          'TimeZone',
                                        ].includes(field.key)}
                                        id={field.key}
                                        type={field.type}
                                        name={field.name}
                                        value={value || ''}
                                        placeholder={field.name}
                                        maxLength={
                                          field.key === 'FirstName' ||
                                          field.key === 'LastName'
                                            ? 50
                                            : undefined
                                        }
                                        className={
                                          errors[
                                            field.key as keyof TProfileSettingsFormFields
                                          ]
                                            ? 'content-has-error'
                                            : ''
                                        }
                                        onChange={e => onChange(e.target.value)}
                                      />
                                    </>
                                  );
                                }}
                              />

                              {errors[
                                field.key as keyof TProfileSettingsFormFields
                              ] && (
                                <p className="content-text-sm content-text-red-500">
                                  {
                                    errors[
                                      field.key as keyof TProfileSettingsFormFields
                                    ]?.message
                                  }
                                </p>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    </Border>
                  </div>
                ))}
                <Button type="submit" disabled={submitting}>
                  {submitting ? (
                    <>
                      <Loader2 className="content-mr-2 content-size-4 content-animate-spin" />
                      Saving Changes...
                    </>
                  ) : (
                    'Save Changes'
                  )}
                </Button>
              </form>
            </div>
          </Border>
        </>
      )}
    </Fragment>
  );
};

export default ProfileSettings;
