import { useEffect, useState } from 'react';

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { IBillingDetails, IOrganizationDetails } from 'models/Client';
import { isValidEmail } from 'utils/Helper';
import { Switch } from 'common/Switch';
import SearchSelect from 'components/SearchSelect';
import { IDropdownData } from 'models/Dropdown';

interface IProps {
  data: IBillingDetails;
  prevStepData: IOrganizationDetails;
  dropdownData: IDropdownData;
  onUpdate: (data: IBillingDetails) => void;
  onNext: () => void;
  onPrevious: () => void;
}

const Step2 = ({
  data,
  prevStepData,
  dropdownData,
  onUpdate,
  onNext,
  onPrevious,
}: IProps) => {
  const [formData, setFormData] = useState<IBillingDetails>({
    ...data,
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof IBillingDetails, string>>
  >({});

  const handleInputChange = (
    field: keyof IBillingDetails,
    value: string | boolean,
  ) => {
    setFormData(prev => ({ ...prev, [field]: value }));

    if (field === 'useSameAsOrganizationAddress' && value === true) {
      updateAddress()
    }

    if (field === 'country') {
      setFormData(prev => ({
        ...prev,
        stateProvince: '',
        city: '',
        zipPostalCode: '',
      }));

      setErrors(prev => ({
        ...prev,
        stateProvince: '',
        city: '',
        zipPostalCode: '',
      }));
    }

    if (field === 'stateProvince') {
      setFormData(prev => ({
        ...prev,
        city: '',
        zipPostalCode: '',
      }));
      setErrors(prev => ({
        ...prev,
        city: '',
        zipPostalCode: '',
      }));
    }

    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const updateAddress = () => {
    setFormData(prev => ({
      ...prev,
      streetAddress: prevStepData.streetAddress,
      streetAddressLine2: prevStepData.streetAddressLine2,
      stateProvince: prevStepData.stateProvince,
      country: prevStepData.country,
      city: prevStepData.city,
      zipPostalCode: prevStepData.zipPostalCode,
    }));

    setErrors(prev => ({
      ...prev,
      streetAddress: '',
      country: '',
      stateProvince: '',
      city: '',
      zipPostalCode: '',
    }));
  }

  useEffect(() => {
    updateAddress()
  }, [])

  const validateForm = () => {
    const newErrors: Partial<Record<keyof IBillingDetails, string>> = {};
    const requiredFields: Array<keyof Partial<IBillingDetails>> = [
      'billingEmail',
      'billingName',
      'streetAddress',
      'city',
      'stateProvince',
      'country',
      'zipPostalCode',
    ];

    requiredFields.forEach(field => {
      if (!formData[field]) {
        newErrors[field as keyof Partial<IBillingDetails>] =
          'This field is required';
      }
    });

    if (!formData.billingEmail) {
      newErrors.billingEmail = 'Billing email is required';
    } else if (!isValidEmail(formData.billingEmail)) {
      newErrors.billingEmail = 'Please enter a valid email address';
    } else {
      const billingDomain = formData.billingEmail.split('@')[1]?.toLowerCase();
      const organizationDomain = prevStepData.domain?.toLowerCase();
      if (organizationDomain && billingDomain !== organizationDomain) {
        newErrors.billingEmail = `Billing email domain must match the organization domain (${prevStepData.domain})`;
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (validateForm()) {
      onUpdate(formData);
      onNext();
    }
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl font-bold">Billing Details</CardTitle>
        <p className="text-gray-400">
          Provide billing information for your organization
        </p>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="flex items-center space-x-2">
          <Switch
            id="useSameAsOrganizationAddress"
            checked={formData.useSameAsOrganizationAddress}
            onCheckedChange={checked =>
              handleInputChange('useSameAsOrganizationAddress', checked)
            }
          />
          <Label htmlFor="useSameAsOrganizationAddress">
            Same as Organization Address
          </Label>
        </div>

        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="billingEmail">Billing Email *</Label>
            <Input
              id="billingEmail"
              type="email"
              value={formData.billingEmail}
              onChange={e => handleInputChange('billingEmail', e.target.value)}
              placeholder={
                prevStepData.domain
                  ? `billing@${prevStepData.domain}`
                  : 'billing@company.com'
              }
              className={errors.billingEmail ? 'has-error' : ''}
            />
            {errors.billingEmail && (
              <p className="text-sm text-red-500">{errors.billingEmail}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="billingName">Billing Name *</Label>
            <Input
              id="billingName"
              value={formData.billingName}
              onChange={e => handleInputChange('billingName', e.target.value)}
              placeholder="Enter billing contact name"
              className={errors.billingName ? 'has-error' : ''}
            />
            {errors.billingName && (
              <p className="text-sm text-red-500">{errors.billingName}</p>
            )}
          </div>

          <div className="col-span-2 space-y-2">
            <Label htmlFor="streetAddress">Street Address *</Label>
            <Input
              id="streetAddress"
              value={formData.streetAddress}
              onChange={e => handleInputChange('streetAddress', e.target.value)}
              placeholder="123 Main St"
              className={errors.streetAddress ? 'has-error' : ''}
              disabled={formData.useSameAsOrganizationAddress}
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
              disabled={formData.useSameAsOrganizationAddress}
            />
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
              disabled={formData.useSameAsOrganizationAddress}
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
              disabled={
                formData.country === '' || formData.useSameAsOrganizationAddress
              }
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
              disabled={formData.useSameAsOrganizationAddress}
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
              disabled={formData.useSameAsOrganizationAddress}
            />
            {errors.zipPostalCode && (
              <p className="text-sm text-red-500">{errors.zipPostalCode}</p>
            )}
          </div>
        </div>

        <div className="flex justify-between pt-6">
          <Button variant="outline" onClick={onPrevious} className="px-8 py-2">
            Previous
          </Button>
          <Button onClick={handleSave}>Save & Continue</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step2;
