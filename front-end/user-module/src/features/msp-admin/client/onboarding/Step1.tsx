import { ChangeEvent, useCallback, useEffect, useRef, useState } from 'react';

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import CustomPhoneInput from 'components/CustomPhoneInput';
import SearchSelect from 'components/SearchSelect';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IOrganizationDetails } from 'models/Client';
import { IDropdownData } from 'models/Dropdown';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isValidEmail } from 'utils/Helper';

interface IProps {
  data: IOrganizationDetails;
  dropdownData: IDropdownData;
  onUpdate: (data: IOrganizationDetails) => void;
  onNext?: () => void;
}

interface OrganizationErrors {
  organizationName?: string;
  organizationType?: string;
  contactEmail?: string;
  phoneNumber?: string;
  country?: string;
  stateProvince?: string;
  industry?: string;
  organizationSize?: string;
  adminEmail?: string;
  streetAddress?: string;
  timeZone?: string;
  language?: string;
  city?: string;
  zipPostalCode?: string;
  organizationLogo?: string;
  subIndustryId?: string;
  complianceId?: string;
}

const Step1 = ({ data, dropdownData, onUpdate, onNext }: IProps) => {
  const [formData, setFormData] = useState<IOrganizationDetails>({
    ...data,
  });
  const [selectedCountryCode, setSelectedCountryCode] = useState<string>('us');
  const [hasEmailUpdated, setHasEmailUpdated] = useState<boolean>(false);
  const [errors, setErrors] = useState<OrganizationErrors>({});
  const organizationLogoInputRef = useRef<HTMLInputElement | null>(null);

  const apiClient = useAPI();

  const debouncedEmail = useDebounce(formData.adminEmail, 500);

  const validateEmail = useCallback(async () => {
    if (!hasEmailUpdated) return true;

    try {
      const response = await apiClient.get(
        API_END_POINTS.VALIDATE_EMAIL + encodeURIComponent(debouncedEmail),
      );
      if (response.data.exists) {
        setErrors(prev => ({
          ...prev,
          adminEmail: 'This email is already registered',
        }));
        return false;
      } else {
        setErrors(prev => ({ ...prev, adminEmail: '' }));
        return true;
      }
    } catch (error) {
      console.error('Error validating email:', error);
      setErrors(prev => ({
        ...prev,
        adminEmail: 'Error validating email',
      }));
      return false;
    }
  }, [hasEmailUpdated, debouncedEmail, apiClient]);

  useEffect(() => {
    if (!debouncedEmail || !isValidEmail(debouncedEmail)) return;
    if (!hasEmailUpdated) return;

    let cancelled = false;
    queueMicrotask(() => {
      if (!cancelled) validateEmail();
    });
    return () => {
      cancelled = true;
    };
  }, [debouncedEmail, hasEmailUpdated, validateEmail]);

  useEffect(() => {
    if (!formData.adminEmail) return;
    const domain = formData.adminEmail.split('@')[1];
    if (!domain) return;

    let cancelled = false;
    queueMicrotask(() => {
      if (!cancelled) {
        setFormData(prev =>
          prev.domain === domain ? prev : { ...prev, domain },
        );
      }
    });
    return () => {
      cancelled = true;
    };
  }, [formData.adminEmail]);

  const handleInputChange = (
    field: keyof IOrganizationDetails,
    value: string,
  ) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field as keyof OrganizationErrors]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }

    if (field === 'country') {
      setSelectedCountryCode(
        dropdownData.countries
          .find(item => item.id === value)
          ?.code.toLocaleLowerCase() as string,
      );
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

    if (field === 'adminEmail') {
      if (value !== data.adminEmail) {
        setHasEmailUpdated(true);
      } else {
        setHasEmailUpdated(false);
      }
    }
  };

  const validateForm = () => {
    const newErrors: OrganizationErrors = {};
    const requiredFields: Array<keyof IOrganizationDetails> = [
      'organizationName',
      'organizationType',
      'contactEmail',
      'phoneNumber',
      'country',
      'stateProvince',
      'industry',
      'organizationSize',
      'adminEmail',
      'streetAddress',
      'city',
      'zipPostalCode',
      'subIndustryId',
      'complianceId',
    ];

    requiredFields.forEach(field => {
      if (!formData[field]) {
        newErrors[field as keyof OrganizationErrors] = 'This field is required';
      }
    });

    if (!isValidEmail(formData.contactEmail)) {
      newErrors.contactEmail = 'Please enter a valid email address';
    }
    if (!isValidEmail(formData.adminEmail)) {
      newErrors.adminEmail = 'Please enter a valid email address';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = async () => {
    if (validateForm() && (await validateEmail())) {
      onUpdate(formData);
      onNext?.();
    }
  };

  const handleFileUpload = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const allowedImageTypes = ['image/jpeg', 'image/jpg', 'image/png'];
      if (!allowedImageTypes.includes(file.type)) {
        setErrors(prev => ({
          ...prev,
          organizationLogo: 'Only JPG, JPEG, and PNG images are allowed',
        }));
        event.target.value = '';
        return;
      }

      setErrors(prev => ({ ...prev, organizationLogo: '' }));
      setFormData(prev => ({ ...prev, organizationLogo: file }));
    }
  };

  const handleRemoveLogo = () => {
    setFormData(prev => ({ ...prev, organizationLogo: null }));
    setErrors(prev => ({ ...prev, organizationLogo: '' }));
    if (organizationLogoInputRef.current) {
      organizationLogoInputRef.current.value = '';
    }
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl font-bold">
          Organization Details
        </CardTitle>
        <p className="text-gray-400">
          Please provide your organization&apos;s basic information
        </p>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="organizationName">Organization Name *</Label>
            <Input
              id="organizationName"
              value={formData.organizationName}
              onChange={e =>
                handleInputChange('organizationName', e.target.value)
              }
              placeholder="Enter organization name"
              className={errors.organizationName ? 'has-error' : ''}
            />
            {errors.organizationName && (
              <p className="text-sm text-red-500">{errors.organizationName}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="adminEmail">Admin Email *</Label>
            <Input
              id="adminEmail"
              type="email"
              value={formData.adminEmail}
              onChange={e => handleInputChange('adminEmail', e.target.value)}
              placeholder="admin@company.com"
              className={errors.adminEmail ? 'has-error' : ''}
            />
            {errors.adminEmail && (
              <p className="text-sm text-red-500">{errors.adminEmail}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="domain">Domain</Label>
            <Input
              id="domain"
              value={formData.domain}
              readOnly
              placeholder="Auto-filled based on email"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="contactEmail">Contact Email *</Label>
            <Input
              id="contactEmail"
              type="email"
              value={formData.contactEmail}
              onChange={e => handleInputChange('contactEmail', e.target.value)}
              placeholder="contact@company.com"
              className={errors.contactEmail ? 'has-error' : ''}
            />
            {errors.contactEmail && (
              <p className="text-sm text-red-500">{errors.contactEmail}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="country">Country *</Label>
            <SearchSelect
              value={formData.country}
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
              value={formData.stateProvince}
              onValueChange={value => handleInputChange('stateProvince', value)}
              placeholder="Select state"
              items={dropdownData.states
                .filter(item => item.countryId === formData.country)
                .map(item => ({
                  value: item.id,
                  label: item.name,
                }))}
              hasError={!!errors.stateProvince}
              disabled={formData.country === ''}
            />
            {errors.stateProvince && (
              <p className="text-sm text-red-500">{errors.stateProvince}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="city">City *</Label>
            <Input
              id="city"
              value={formData.city}
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
              value={formData.zipPostalCode}
              onChange={e => handleInputChange('zipPostalCode', e.target.value)}
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
              value={formData.timeZone}
              onValueChange={value => handleInputChange('timeZone', value)}
              placeholder="Select time zone"
              items={dropdownData.timeZones
                .filter(
                  item =>
                    item.countryId === formData.country &&
                    item.stateId === formData.stateProvince,
                )
                .map(item => ({
                  value: item.id,
                  label: item.name,
                }))}
              hasError={!!errors.timeZone}
              disabled={
                formData.country === '' || formData.stateProvince === ''
              }
            />
            {errors.timeZone && (
              <p className="text-sm text-red-500">{errors.timeZone}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="phoneNumber">Phone Number *</Label>
            <CustomPhoneInput
              inputProps={{
                id: 'phoneNumber',
                maxLength: 17,
              }}
              country={selectedCountryCode}
              customInputClass={
                errors.phoneNumber ? 'input-default has-error' : ''
              }
              value={formData.phoneNumber}
              handleChange={value => handleInputChange('phoneNumber', value)}
            />
            {errors.phoneNumber && (
              <p className="text-sm text-red-500">{errors.phoneNumber}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="language">Language</Label>
            <SearchSelect
              value={formData.language}
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
              value={formData.organizationType}
              onValueChange={value =>
                handleInputChange('organizationType', value)
              }
              placeholder="Select organization type"
              items={dropdownData.organizationTypes.map(item => ({
                value: item.id,
                label: item.name,
              }))}
              hasError={!!errors.organizationType}
            />
            {errors.organizationType && (
              <p className="text-sm text-red-500">{errors.organizationType}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="industry">Industry *</Label>
            <SearchSelect
              value={formData.industry}
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
            <Label htmlFor="subIndustry">Sub Industry *</Label>
            <SearchSelect
              value={formData.subIndustryId}
              onValueChange={value => {
                const item = (dropdownData.subIndustries ?? []).find(
                  s => s.id === value,
                );
                setFormData(prev => ({
                  ...prev,
                  subIndustryId: value,
                  subIndustryName: item?.name ?? '',
                }));
                setErrors(prev => ({ ...prev, subIndustryId: '' }));
              }}
              placeholder="Select sub industry"
              items={(dropdownData.subIndustries ?? []).map(item => ({
                value: item.id,
                label: item.name,
              }))}
              hasError={!!errors.subIndustryId}
            />
            {errors.subIndustryId && (
              <p className="text-sm text-red-500">{errors.subIndustryId}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="compliance">Compliance Standard *</Label>
            <SearchSelect
              value={formData.complianceId}
              onValueChange={value => {
                const item = (dropdownData.compliances ?? []).find(c => c.id === value);
                setFormData(prev => ({
                  ...prev,
                  complianceId: value,
                  complianceName: item?.name ?? '',
                }));
                setErrors(prev => ({ ...prev, complianceId: '' }));
              }}
              placeholder="Select compliance"
              items={(dropdownData.compliances ?? []).map(item => ({
                value: item.id,
                label: item.name,
              }))}
              hasError={!!errors.complianceId}
            />
            {errors.complianceId && (
              <p className="text-sm text-red-500">{errors.complianceId}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="organizationSize">Organization Size *</Label>
            <SearchSelect
              value={formData.organizationSize}
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
              <p className="text-sm text-red-500">{errors.organizationSize}</p>
            )}
          </div>
        </div>

        <div className="col-span-2 space-y-2">
          <Label htmlFor="streetAddress">Street Address *</Label>
          <Input
            id="streetAddress"
            value={formData.streetAddress}
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
            value={formData.streetAddressLine2}
            onChange={e =>
              handleInputChange('streetAddressLine2', e.target.value)
            }
            placeholder="Apt, Suite, Building (optional)"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="organizationLogo">
            Organization Logo (Optional - Only JPG, JPEG, and PNG images are
            allowed)
          </Label>
          <Input
            id="organizationLogo"
            type="file"
            accept=".jpg,.jpeg,.png,image/jpeg,image/png"
            ref={organizationLogoInputRef}
            onChange={handleFileUpload}
            className="cursor-pointer"
          />
          {errors.organizationLogo && (
            <p className="text-sm text-red-500">{errors.organizationLogo}</p>
          )}
          {formData.organizationLogo && (
            <div className="flex items-center gap-2 text-sm text-green-600">
              <p>Logo selected: {formData.organizationLogo.name}</p>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={handleRemoveLogo}
                className="text-red-500 hover:text-red-400"
              >
                Remove
              </Button>
            </div>
          )}
        </div>

        <div className="flex justify-end pt-6">
          <Button onClick={handleSave}>Save & Continue</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step1;
