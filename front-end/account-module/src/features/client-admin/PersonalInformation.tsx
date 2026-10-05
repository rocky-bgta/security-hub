import { Loader2, Save, User } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'components/common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import CustomPhoneInput from 'components/CustomPhoneInput';
import SearchSelect from 'components/SearchSelect';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { useUploader } from 'hooks/UseUploader';
import { FileType, IDropdownData, IResponse } from 'models/Global';
import { IClientAdminInfoPayload, IClientAdminInfoResponse } from 'models/User';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  PersonalInformationSchema,
  TPersonalInformationFormFields,
} from 'schemas/PersonalInformationSchema';
import { isSuccessResponse } from 'utils/Helper';
import FileUploader, { FileUploaderHandle } from 'common/FileUploader';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { FcUpload } from 'react-icons/fc';

interface OrganizationErrors {
  organizationName?: string;
  organizationType?: string;
  contactEmail?: string;
  phoneNumber?: string;
  country?: string;
  stateProvince?: string;
  industry?: string;
  organizationSize?: string;
  streetAddress?: string;
  streetAddressLine2?: string;
  timeZone?: string;
  language?: string;
  city?: string;
  zipPostalCode?: string;
}

const ClientAdminPersonalInformation = () => {
  const [formData, setFormData] = useState<IClientAdminInfoPayload>({
    organizationName: '',
    industry: '',
    contactEmail: '',
    country: '',
    phoneNumber: '',
    organizationType: '',
    streetAddress: '',
    streetAddressLine2: '',
    city: '',
    stateProvince: '',
    zipPostalCode: '',
    timeZone: '',
    language: '',
    organizationSize: '',
    logoUrl: '',
    organizationLogo: null,
    countryCode: '',
    stateCode: '',
    sameAsOrganizationAddress: false,
    billingName: '',
    billingEmail: '',
    billingCountry: '',
    billingStreetAddress: '',
    billingStreetAddressLine2: '',
    billingCity: '',
    billingZipPostalCode: '',
    billingStateProvince: '',
  });
  const [selectedCountryCode, setSelectedCountryCode] = useState<string>('us');
  const [errors, setErrors] = useState<OrganizationErrors>({});
  const [dropdownData, setDropdownData] = useState<IDropdownData>({
    organizationTypes: [],
    countries: [],
    states: [],
    timeZones: [],
    languages: [],
    industries: [],
    organizationSizes: [],
  });
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [originalLogoUrl, setOriginalLogoUrl] = useState<string>('');
  const [logoPreview, setLogoPreview] = useState<string>('');
  const [isLogoRemoved, setIsLogoRemoved] = useState<boolean>(false);
  const fileUploaderRef = useRef<FileUploaderHandle>(null);

  const { userInfo, setUserInfo } = useStore();
  const apiClient = useAPI();
  const { uploadFile } = useUploader();

  const fetchDropdownData = async () => {
    try {
      const [
        orgTypeRes,
        countryRes,
        stateRes,
        timeZoneRes,
        languageRes,
        industryRes,
        orgSizeRes,
      ] = await Promise.all<
        [
          IResponse<Array<{ id: string; organizationType: string }>>,
          IResponse<Array<{ id: string; name: string; code?: string }>>,
          IResponse<
            Array<{
              id: string;
              name: string;
              countryId: string;
              code?: string;
            }>
          >,
          IResponse<
            Array<{
              id: string;
              displayName: string;
              countryId: string;
              stateId: string;
              timezoneId: string;
            }>
          >,
          IResponse<Array<{ id: string; displayName: string }>>,
          IResponse<Array<{ id: string; name: string }>>,
          IResponse<Array<{ id: string; name: string; range: string }>>,
        ]
      >([
        apiClient.get(API_END_POINTS.GET_ORGANIZATION_TYPE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_COUNTRY_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_STATE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_TIME_ZONE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_LANGUAGE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_INDUSTRIES_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_ORGANIZATION_SIZE_LIST),
      ]);

      setDropdownData({
        organizationTypes: orgTypeRes.data.map(item => ({
          id: item.id,
          organizationType: item.organizationType,
        })),
        countries: countryRes.data.map(item => ({
          id: item.id,
          name: item.name,
          code: item.code,
        })),
        states: stateRes.data.map(item => ({
          id: item.id,
          name: item.name,
          countryId: item.countryId,
          code: item.code,
        })),
        timeZones: timeZoneRes.data.map(item => ({
          id: item.id,
          name: item.displayName + ' (' + item.timezoneId + ')',
          countryId: item.countryId,
          stateId: item.stateId,
        })),
        languages: languageRes.data.map(item => ({
          id: item.id,
          name: item.displayName,
        })),
        industries: industryRes.data.map(item => ({
          id: item.id,
          name: item.name,
        })),
        organizationSizes: orgSizeRes.data.map(item => ({
          id: item.id,
          name: item.name + ' (' + item.range + ')',
        })),
      });
    } catch (error) {
      console.error('Error fetching dropdown data:', error);
    }
  };

  useEffect(() => {
    fetchDropdownData();
  }, []);

  useEffect(() => {
    if (dropdownData.countries.length > 0) {
      fetchFormData();
    }
  }, [userInfo, dropdownData.countries.length]);

  const fetchFormData = async () => {
    if (!userInfo?.userId) {
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    try {
      const response: IResponse<IClientAdminInfoResponse> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_DETAILS.replace(
          ':clientAdminId',
          userInfo?.userId,
        ),
      );

      const countryCode =
        dropdownData.countries
          .find(c => c.id === response.data.country?.id)
          ?.code?.toLowerCase() || 'us';

      const stateCode =
        dropdownData.states.find(s => s.id === response.data.state?.id)?.code ||
        '';

      // Set country code first so phone input can display correctly
      if (countryCode) {
        setSelectedCountryCode(countryCode);
      }

      const logoUrl = response.data.logoUrl || '';

      setOriginalLogoUrl(logoUrl);
      setLogoPreview(logoUrl ? FILE_PATH_PREFIX + logoUrl : '');
      setIsLogoRemoved(false);

      setFormData({
        organizationName: response.data.organizationName || '',
        industry: response.data.industry?.id || '',
        contactEmail: response.data.contactEmail || '',
        country: response.data.country?.id || '',
        phoneNumber: response.data.phoneNumber || '',
        organizationType: response.data.organizationType?.id || '',
        streetAddress: response.data.streetAddress || '',
        streetAddressLine2: response.data.streetAddressLine2 || '',
        city: response.data.city || '',
        stateProvince: response.data.state?.id || '',
        zipPostalCode: response.data.zipPostalCode || '',
        timeZone: response.data.timeZone?.id || '',
        language: response.data.language?.id || '',
        organizationSize: response.data.organizationSize?.id || '',
        logoUrl,
        organizationLogo: null,
        countryCode: countryCode,
        stateCode: stateCode,
        sameAsOrganizationAddress:
          response.data.billingUseSameAsOrganizationAddress || false,
        billingName: response.data.billingName || '',
        billingEmail: response.data.billingEmail || '',
        billingCountry: response.data.billingCountry || '',
        billingStreetAddress: response.data.billingStreetAddress || '',
        billingStreetAddressLine2: response.data.billingStreetAddressLine2 || '',
        billingCity: response.data.billingCity || '',
        billingZipPostalCode: response.data.billingZipPostalCode || '',
        billingStateProvince: response.data.billingStateProvince || '',
      });
    } catch (error) {
      console.error('Error fetching personal information:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSavePersonalInfo = async () => {
    setIsSubmitting(true);
    if (!validateForm()) {
      return setIsSubmitting(false);
    }

    let logoUrl = isLogoRemoved ? '' : formData.logoUrl;
    if (formData.organizationLogo) {
      const { url, error } = await uploadFile(
        formData.organizationLogo,
        FileType.LOGO as FileType,
      );
      if (error) {
        toast.error(error);
        setIsSubmitting(false);
        return;
      }
      logoUrl = url;
    }

    const countryCode =
      dropdownData.countries.find(c => c.id === formData.country)?.code || '';
    const stateCode =
      dropdownData.states.find(s => s.id === formData.stateProvince)?.code ||
      '';

    const { organizationLogo: _, ...rest } = formData;
    const payload: IClientAdminInfoPayload = {
      ...rest,
      logoUrl,
      countryCode,
      stateCode,
    };

    try {
      const response: IResponse<IClientAdminInfoResponse> = await apiClient.put(
        API_END_POINTS.UPDATE_CLIENT_ADMIN_INFO.replace(
          ':clientAdminId',
          userInfo?.userId,
        ),
        {
          data: payload,
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        const savedLogoUrl = response.data.logoUrl || '';
        setOriginalLogoUrl(savedLogoUrl);
        setLogoPreview(
          savedLogoUrl ? FILE_PATH_PREFIX + savedLogoUrl : '',
        );
        setIsLogoRemoved(false);
        setFormData(prev => ({
          ...prev,
          logoUrl: savedLogoUrl,
          organizationLogo: null,
        }));
        fileUploaderRef.current?.clearFiles();
        toast.success('Personal information saved successfully');
        setUserInfo((prev: typeof userInfo) => ({
          ...prev,
          profilePicture: savedLogoUrl ?? prev.profilePicture,
        }));
      } else {
        toast.error('Failed to save personal information');
      }
    } catch (error) {
      console.error('Error saving personal information:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleInputChange = (
    field: keyof IClientAdminInfoPayload,
    value: string,
  ) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field as keyof OrganizationErrors]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }

    if (field === 'country') {
      const countryCode =
        dropdownData.countries
          .find(item => item.id === value)
          ?.code?.toLowerCase() || 'us';
      setSelectedCountryCode(countryCode);
      setFormData(prev => ({
        ...prev,
        stateProvince: '',
        timeZone: '',
        city: '',
        zipPostalCode: '',
      }));

      setErrors(prev => ({
        ...prev,
        stateProvince: '',
        timeZone: '',
        city: '',
        zipPostalCode: '',
      }));
    }

    if (field === 'stateProvince') {
      setFormData(prev => ({
        ...prev,
        timeZone: '',
        city: '',
        zipPostalCode: '',
      }));
      setErrors(prev => ({
        ...prev,
        timeZone: '',
        city: '',
        zipPostalCode: '',
      }));
    }
  };

  const handleFileUpload = (files: FileList, _id: string) => {
    if (!files || files.length === 0) return;

    const file = files[0];
    setIsLogoRemoved(false);
    setFormData(prev => ({ ...prev, organizationLogo: file }));

    const reader = new FileReader();
    reader.onloadend = () => {
      setLogoPreview(reader.result as string);
    };
    reader.readAsDataURL(file);
  };

  const handleRemoveLogo = () => {
    setFormData(prev => ({ ...prev, organizationLogo: null }));
    setIsLogoRemoved(true);
    setLogoPreview('');
    fileUploaderRef.current?.clearFiles();
  };

  const handleDiscardLogoUpload = () => {
    setFormData(prev => ({ ...prev, organizationLogo: null }));
    setIsLogoRemoved(false);
    setLogoPreview(
      originalLogoUrl ? FILE_PATH_PREFIX + originalLogoUrl : '',
    );
    fileUploaderRef.current?.clearFiles();
  };

  const validateForm = () => {
    const result = PersonalInformationSchema.safeParse({
      organizationName: formData.organizationName,
      organizationType: formData.organizationType,
      contactEmail: formData.contactEmail,
      phoneNumber: formData.phoneNumber,
      country: formData.country,
      stateProvince: formData.stateProvince,
      industry: formData.industry,
      organizationSize: formData.organizationSize,
      streetAddress: formData.streetAddress,
      streetAddressLine2: formData.streetAddressLine2,
      city: formData.city,
      zipPostalCode: formData.zipPostalCode,
    });

    if (!result.success) {
      const fieldErrors = result.error.flatten().fieldErrors;
      const newErrors: OrganizationErrors = {};
      (
        Object.keys(fieldErrors) as Array<keyof TPersonalInformationFormFields>
      ).forEach(key => {
        const message = fieldErrors[key]?.[0];
        if (message) newErrors[key] = message;
      });
      setErrors(newErrors);
      return false;
    }

    setErrors({});
    return true;
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
            <User className="size-5" />
            Personal Information
          </CardTitle>
          <CardDescription>
            Update your personal details and contact information
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {isLoading ? (
            <div className="flex flex-col items-center justify-center gap-3 py-16">
              <Loader2 className="size-8 animate-spin text-primary" />
              <p className="text-sm text-muted-foreground">
                Loading personal information...
              </p>
            </div>
          ) : (
            <>
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="organizationName">Organization Name *</Label>
              <Input
                onChange={e =>
                  handleInputChange('organizationName', e.target.value)
                }
                id="organizationName"
                placeholder="Enter organization name"
                value={formData?.organizationName}
                className={errors.organizationName ? 'has-error' : ''}
                disabled
              />
              {errors.organizationName && (
                <p className="text-sm text-red-500">
                  {errors.organizationName}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Contact Email *</Label>
              <Input
                id="email"
                type="email"
                placeholder="contact@company.com"
                value={formData?.contactEmail}
                onChange={e =>
                  handleInputChange('contactEmail', e.target.value)
                }
                className={errors.contactEmail ? 'has-error' : ''}
                disabled
              />
              {errors.contactEmail && (
                <p className="text-sm text-red-500">{errors.contactEmail}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="country">Country *</Label>
              <SearchSelect
                value={formData?.country}
                onValueChange={value => handleInputChange('country', value)}
                placeholder="Select country"
                items={dropdownData.countries.map(country => ({
                  value: country.id,
                  label: country.name,
                }))}
                hasError={!!errors.country}
              />
              {errors.country && (
                <p className="text-sm text-red-500">{errors.country}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="stateProvince">State/Province/Division *</Label>
              <SearchSelect
                value={formData?.stateProvince}
                onValueChange={value =>
                  handleInputChange('stateProvince', value)
                }
                placeholder="Select state"
                items={dropdownData.states
                  .filter(item => item.countryId === formData.country)
                  .map(item => ({
                    value: item.id,
                    label: item.name,
                  }))}
                hasError={!!errors.stateProvince}
                disabled={formData?.country === ''}
              />
              {errors.stateProvince && (
                <p className="text-sm text-red-500">{errors.stateProvince}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="city">City *</Label>
              <Input
                id="city"
                value={formData?.city}
                onChange={e => handleInputChange('city', e.target.value)}
                placeholder="Enter city"
                className={errors.city ? 'has-error' : ''}
              />
              {errors.city && (
                <p className="text-sm text-red-500">{errors.city}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="zipPostalCode">Zip/Postal Code *</Label>
              <Input
                id="zipPostalCode"
                value={formData?.zipPostalCode}
                onChange={e =>
                  handleInputChange('zipPostalCode', e.target.value)
                }
                placeholder="Enter zip or postal code"
                className={errors.zipPostalCode ? 'has-error' : ''}
              />
              {errors.zipPostalCode && (
                <p className="text-sm text-red-500">{errors.zipPostalCode}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="timeZone">Time Zone</Label>
              <SearchSelect
                value={formData?.timeZone}
                onValueChange={value => handleInputChange('timeZone', value)}
                placeholder="Select time zone"
                items={dropdownData.timeZones
                  .filter(
                    item =>
                      item.countryId === formData?.country &&
                      item.stateId === formData?.stateProvince,
                  )
                  .map(item => ({
                    value: item.id,
                    label: item.name,
                  }))}
                hasError={!!errors.timeZone}
                disabled={
                  formData?.country === '' || formData?.stateProvince === ''
                }
              />
              {errors.timeZone && (
                <p className="text-sm text-red-500">{errors.timeZone}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="phoneNumber">Phone Number *</Label>
              <CustomPhoneInput
                key={`phone-${selectedCountryCode}`}
                inputProps={{
                  id: 'phoneNumber',
                  maxLength: 17,
                }}
                country={selectedCountryCode}
                customInputClass={
                  errors.phoneNumber ? 'input-default has-error' : ''
                }
                value={formData?.phoneNumber || ''}
                handleChange={value => handleInputChange('phoneNumber', value)}
              />
              {errors.phoneNumber && (
                <p className="text-sm text-red-500">{errors.phoneNumber}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="language">Language</Label>
              <SearchSelect
                value={formData?.language}
                onValueChange={value => handleInputChange('language', value)}
                placeholder="Select language"
                items={dropdownData.languages.map(item => ({
                  value: item.id,
                  label: item.name,
                }))}
                hasError={!!errors.language}
              />
              {errors.language && (
                <p className="text-sm text-red-500">{errors.language}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="organizationType">Organization Type *</Label>
              <SearchSelect
                value={formData?.organizationType}
                onValueChange={value =>
                  handleInputChange('organizationType', value)
                }
                placeholder="Select organization type"
                items={dropdownData.organizationTypes.map(item => ({
                  value: item.id,
                  label: item.organizationType,
                }))}
                hasError={!!errors.organizationType}
              />
              {errors.organizationType && (
                <p className="text-sm text-red-500">
                  {errors.organizationType}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="industry">Industry *</Label>
              <SearchSelect
                value={formData?.industry}
                onValueChange={value => handleInputChange('industry', value)}
                placeholder="Select industry"
                items={dropdownData.industries.map(item => ({
                  value: item.id,
                  label: item.name,
                }))}
                hasError={!!errors.industry}
              />
              {errors.industry && (
                <p className="text-sm text-red-500">{errors.industry}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="organizationSize">Organization Size *</Label>
              <SearchSelect
                value={formData?.organizationSize}
                onValueChange={value =>
                  handleInputChange('organizationSize', value)
                }
                placeholder="Select organization size"
                items={dropdownData.organizationSizes.map(item => ({
                  value: item.id,
                  label: item.name,
                }))}
                hasError={!!errors.organizationSize}
              />
              {errors.organizationSize && (
                <p className="text-sm text-red-500">
                  {errors.organizationSize}
                </p>
              )}
            </div>
          </div>

          <div className="col-span-2 space-y-2">
            <Label htmlFor="streetAddress">Street Address *</Label>
            <Input
              id="streetAddress"
              value={formData?.streetAddress}
              onChange={e => handleInputChange('streetAddress', e.target.value)}
              placeholder="123 Main St"
              className={errors.streetAddress ? 'has-error' : ''}
            />
            {errors.streetAddress && (
              <p className="text-sm text-red-500">{errors.streetAddress}</p>
            )}
          </div>

          <div className="col-span-2 space-y-2">
            <Label htmlFor="streetAddressLine2">Street Address Line 2</Label>
            <Input
              id="streetAddressLine2"
              value={formData?.streetAddressLine2}
              onChange={e =>
                handleInputChange('streetAddressLine2', e.target.value)
              }
              placeholder="Apt, Suite, Building (optional)"
              className={errors.streetAddressLine2 ? 'has-error' : ''}
            />
            {errors.streetAddressLine2 && (
              <p className="text-sm text-red-500">
                {errors.streetAddressLine2}
              </p>
            )}
          </div>

          <div className="space-y-4">
            <Label htmlFor="organizationLogo">
              Organization Logo (Optional)
            </Label>

            {logoPreview && (
              <div className="flex items-center justify-center rounded-lg border border-card-border bg-white p-8">
                <img
                  src={logoPreview}
                  alt="Organization logo preview"
                  className="max-h-32 max-w-full object-contain"
                />
              </div>
            )}

            <div className="flex flex-wrap items-center gap-3">
              <FileUploader
                ref={fileUploaderRef}
                id="organization-logo-upload"
                accept="image/png,image/jpeg,image/jpg"
                maxSize={5}
                placeholder={
                  logoPreview ? (
                    <span className="flex items-center gap-2">
                      <FcUpload className="size-4" />
                      REPLACE FILE
                    </span>
                  ) : (
                    <span className="flex items-center gap-2">
                      <FcUpload className="size-4" />
                      UPLOAD FILE
                    </span>
                  )
                }
                onUpload={handleFileUpload}
                containerClassName="inline-flex items-center rounded-md px-4 py-4 text-sm font-medium text-primary cursor-pointer"
              />
              {(logoPreview || formData.organizationLogo) && (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={handleRemoveLogo}
                >
                  Remove
                </Button>
              )}
              {formData.organizationLogo && (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={handleDiscardLogoUpload}
                >
                  Discard
                </Button>
              )}
            </div>
          </div>

          <div className="flex justify-end pt-6">
            <Button
              onClick={handleSavePersonalInfo}
              className="w-full md:w-auto"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 size-4 animate-spin" />
                  Saving...
                </>
              ) : (
                <>
                  <Save className="mr-2 size-4" />
                  Save Changes
                </>
              )}
            </Button>
          </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default ClientAdminPersonalInformation;
