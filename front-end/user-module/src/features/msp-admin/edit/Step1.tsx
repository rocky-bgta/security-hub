import { CreditCard, Star } from 'lucide-react';
import { ChangeEvent, useEffect, useState } from 'react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Switch } from 'common/Switch';
import CustomPhoneInput from 'components/CustomPhoneInput';
import SearchSelect from 'components/SearchSelect';
import { useAPI } from 'hooks/UseAPI';
import { IDropdownData } from 'models/Dropdown';
import { IList, IResponse } from 'models/Global';
import { IMSPCreditInfo, IMSPOrganizationDetails, IMSPTier } from 'models/MSP';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isValidEmail } from 'utils/Helper';

interface IProps {
  data: IMSPOrganizationDetails;
  creditInfo: IMSPCreditInfo;
  dropdownData: IDropdownData;
  onUpdate: (
    organizationData: IMSPOrganizationDetails,
    creditData: IMSPCreditInfo,
  ) => void;
  onNext?: () => void;
}

interface IMSPOrganizationErrors {
  organizationName?: string;
  organizationType?: string;
  contactEmail?: string;
  phoneNumber?: string;
  country?: string;
  stateProvince?: string;
  timeZone?: string;
  language?: string;
  industry?: string;
  domain?: string;
  organizationSize?: string;
  streetAddress?: string;
  tierId?: string;
  mspTypeId?: string;
  mspAdminEmail?: string;
  city?: string;
  zipPostalCode?: string;
}

interface IMSPCreditErrors {
  creditAmount?: string;
  reason?: string;
  netDaysId?: string;
  creditStartDate?: string;
}

const getCountryCodeFromId = (
  countryId: string,
  countries: IDropdownData['countries'],
): string => {
  const country = countries.find(c => c.id === countryId);
  return country?.code?.toLowerCase() || 'us';
};

const getPhoneInputValue = (phoneNumber: string, phoneCode: string): string => {
  if (!phoneNumber) return '';
  const dialCode = phoneCode.replace(/\D/g, '');
  if (!dialCode) return phoneNumber;
  if (phoneNumber.startsWith(dialCode)) return phoneNumber;
  return `${dialCode}${phoneNumber}`;
};

const Step1 = ({
  data,
  creditInfo,
  dropdownData,
  onUpdate,
  onNext,
}: IProps) => {
  const [formData, setFormData] = useState<IMSPOrganizationDetails>({
    ...data,
  });
  const [creditData, setCreditData] = useState<IMSPCreditInfo>({
    ...creditInfo,
  });
  const [selectedCountryCode, setSelectedCountryCode] = useState<string>(() =>
    getCountryCodeFromId(data.country, dropdownData.countries),
  );

  // Update form data when props change
  useEffect(() => {
    if (data) {
      setFormData({ ...data });
      if (data.country && dropdownData.countries.length > 0) {
        setSelectedCountryCode(
          getCountryCodeFromId(data.country, dropdownData.countries),
        );
      }
    }
  }, [data, dropdownData.countries]);

  useEffect(() => {
    if (creditInfo) {
      setCreditData({ ...creditInfo });
    }
  }, [creditInfo]);
  const [localDropdownData, setLocalDropdownData] = useState<{
    tierCommissionList: Array<IMSPTier>;
    creditReasonList: Array<{ id: string; reasonName: string }>;
    netDaysList: Array<{
      id: string;
      netTermName: string;
      netTermInDays: number;
    }>;
  }>({
    tierCommissionList: [],
    creditReasonList: [],
    netDaysList: [],
  });

  const [errors, setErrors] = useState<
    IMSPOrganizationErrors & IMSPCreditErrors
  >({});

  const apiClient = useAPI();

  useEffect(() => {
    if (formData.mspAdminEmail) {
      const domain = formData.mspAdminEmail.split('@')[1];
      if (domain) {
        setFormData(prev => ({
          ...prev,
          domain: domain,
        }));
      }
    }
  }, [formData.mspAdminEmail]);

  useEffect(() => {
    if (formData.country && dropdownData.countries.length > 0) {
      const country = dropdownData.countries.find(
        c => c.id === formData.country,
      );
      if (country?.code) {
        setSelectedCountryCode(country.code.toLowerCase());
      }
    }
  }, [formData.country, dropdownData.countries]);

  useEffect(() => {
    setTimeout(() => {
      fetchLocalDropdownData();
    }, 1000);
  }, []);

  const fetchLocalDropdownData = async () => {
    try {
      const [tierRes, creditReasonRes, netDaysRes] = await Promise.all<
        [
          IResponse<IList<IMSPTier>>,
          IResponse<IList<{ id: string; reasonName: string }>>,
          IResponse<
            IList<{ id: string; netTermName: string; netTermInDays: number }>
          >,
        ]
      >([
        apiClient.get(
          API_END_POINTS.GET_TIER_LIST + '?status=true&offset=0&limit=100',
        ),
        apiClient.get(
          API_END_POINTS.GET_CREDIT_ALLOCATION_REASON_LIST +
          '?isActive=true&offset=0&limit=100',
        ),
        apiClient.get(
          API_END_POINTS.GET_NET_DAYS_LIST + '?isActive=true&offset=0&limit=10',
        ),
      ]);

      setLocalDropdownData(prev => ({
        ...prev,
        tierCommissionList: tierRes.data.items,
        creditReasonList: creditReasonRes.data.items,
        netDaysList: netDaysRes.data.items,
      }));
    } catch (error) {
      console.error('Error fetching local dropdown data:', error);
    }
  };

  const handleInputChange = (
    field: keyof IMSPOrganizationDetails | keyof IMSPCreditInfo | 'mspTypeId',
    value: string | number | boolean,
  ) => {
    if (field in formData) {
      setFormData(prev => ({ ...prev, [field]: value }));
    } else {
      setCreditData(prev => ({ ...prev, [field]: value }));
    }

    if (
      errors[field as keyof IMSPOrganizationErrors | keyof IMSPCreditErrors]
    ) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }

    if (field === 'country') {
      const selectedCountry = dropdownData.countries.find(
        item => item.id === value,
      );
      setSelectedCountryCode(
        selectedCountry?.code.toLocaleLowerCase() as string,
      );
      setFormData(prev => ({
        ...prev,
        phoneCode: selectedCountry?.phoneCode || '',
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

  const validateForm = () => {
    const newErrors: IMSPOrganizationErrors & IMSPCreditErrors = {};
    const formDataRequiredFields: Array<keyof IMSPOrganizationDetails> = [
      'organizationName',
      'organizationType',
      'contactEmail',
      'phoneNumber',
      'country',
      'stateProvince',
      'industry',
      'domain',
      'organizationSize',
      'streetAddress',
      'city',
      'zipPostalCode',
      'tierId',
      'mspAdminEmail',
    ];
    const creditDataRequiredFields: Array<keyof IMSPCreditInfo> = [
      'creditAmount',
      'netDaysId',
      'reason',
      'creditStartDate',
    ];

    formDataRequiredFields.forEach(field => {
      if (!formData[field]) {
        newErrors[field as keyof IMSPOrganizationErrors] =
          'This field is required';
      }
    });

    if (creditData.enableCredit) {
      creditDataRequiredFields.forEach(field => {
        if (!creditData[field]) {
          newErrors[field as keyof IMSPCreditErrors] = 'This field is required';
        }
      });
    }

    if (!isValidEmail(formData.contactEmail)) {
      newErrors.contactEmail = 'Please enter a valid email address';
    }
    if (!isValidEmail(formData.mspAdminEmail)) {
      newErrors.mspAdminEmail = 'Please enter a valid email address';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (validateForm()) {
      onUpdate(formData, creditData);
      onNext?.();
    }
  };

  const handleFileUpload = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setFormData(prev => ({ ...prev, organizationLogo: file }));
    }
  };

  const selectedTier = localDropdownData.tierCommissionList.find(
    tier => tier.id === formData.tierId,
  );

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl font-bold">MSP Details</CardTitle>
        <p className="text-gray-400">
          Please provide your MSP organization&apos;s basic information
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
              placeholder="Enter MSP name"
              className={errors.organizationName ? 'has-error' : ''}
            />
            {errors.organizationName && (
              <p className="text-sm text-red-500">{errors.organizationName}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="mspAdminEmail">MSP Admin Email *</Label>
            <Input
              id="mspAdminEmail"
              type="email"
              value={formData.mspAdminEmail}
              onChange={e => handleInputChange('mspAdminEmail', e.target.value)}
              placeholder="admin@company.com"
              className={errors.mspAdminEmail ? 'has-error' : ''}
              disabled={true}
            />
            {errors.mspAdminEmail && (
              <p className="text-sm text-red-500">{errors.mspAdminEmail}</p>
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
              key={formData.country || selectedCountryCode}
              inputProps={{
                id: 'phoneNumber',
                maxLength: 17,
              }}
              country={selectedCountryCode}
              customInputClass={
                errors.phoneNumber ? 'input-default has-error' : ''
              }
              value={getPhoneInputValue(
                formData.phoneNumber,
                formData.phoneCode,
              )}
              handleChange={(value, data) => {
                handleInputChange('phoneNumber', value);
                setFormData(prev => ({
                  ...prev,
                  phoneCode: '+' + data.dialCode,
                }));
              }}
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

          <div className="space-y-2">
            <Label htmlFor="tierId">MSP Tier *</Label>
            <SearchSelect
              value={formData.tierId}
              onValueChange={value => handleInputChange('tierId', value)}
              placeholder="Select MSP tier"
              items={localDropdownData.tierCommissionList.map(tier => ({
                value: tier.id,
                label: tier.tierName,
              }))}
              hasError={!!errors.tierId}
            />
            {errors.tierId && (
              <p className="text-sm text-red-500">{errors.tierId}</p>
            )}
            {selectedTier && (
              <div className="mt-2 flex items-center gap-2">
                <Badge variant="outline">
                  <Star className="mr-1 size-3" />
                  {selectedTier.commissionPercentage}% Commission
                </Badge>
              </div>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="mspTypeId">MSP Type *</Label>
            <SearchSelect
              value={formData.mspTypeId}
              onValueChange={value => handleInputChange('mspTypeId', value)}
              placeholder="Select MSP type"
              items={dropdownData.mspTypes?.map(item => ({
                value: item.id,
                label: item.name,
              })) || []
              }
              hasError={!!errors.mspTypeId}
            />
            {errors.mspTypeId && (
              <p className="text-sm text-red-500">{errors.mspTypeId}</p>
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
          <Label htmlFor="organizationLogo">Organization Logo (Optional)</Label>
          <Input
            id="organizationLogo"
            type="file"
            accept="image/*"
            onChange={handleFileUpload}
            className="cursor-pointer"
          />
          {formData.organizationLogo && (
            <p className="text-sm text-green-600">
              Logo uploaded: {formData.organizationLogo.name}
            </p>
          )}
          {formData.logoUrl && !formData.organizationLogo && (
            <p className="text-sm text-muted-foreground">
              Current logo: {formData.logoUrl}
            </p>
          )}
        </div>

        {/* Credit Information Section */}
        <div className="col-span-2 mt-6 space-y-4 rounded-lg border border-card-border p-4">
          <div className="flex items-center gap-2">
            <CreditCard className="size-5 text-primary" />
            <h3 className="text-lg font-semibold">Credit Information</h3>
          </div>

          <div className="flex items-center space-x-2">
            <Switch
              id="enableCredit"
              checked={creditData.enableCredit}
              onCheckedChange={checked =>
                handleInputChange('enableCredit', checked)
              }
            />
            <Label htmlFor="enableCredit">Enable Credit</Label>
          </div>

          {creditData.enableCredit && (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="creditAmount">Credit Amount *</Label>
                <Input
                  id="creditAmount"
                  type="number"
                  value={creditData.creditAmount || ''}
                  onChange={e =>
                    handleInputChange(
                      'creditAmount',
                      parseFloat(e.target.value) || 0,
                    )
                  }
                  placeholder="Enter credit amount"
                  className={errors.creditAmount ? 'has-error' : ''}
                />
                {errors.creditAmount && (
                  <p className="text-sm text-red-500">{errors.creditAmount}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="netDaysId">Net Days *</Label>
                <SearchSelect
                  value={creditData.netDaysId}
                  onValueChange={value => handleInputChange('netDaysId', value)}
                  placeholder="Select net days"
                  items={localDropdownData.netDaysList.map(item => ({
                    value: item.id,
                    label: `${item.netTermName} (${item.netTermInDays} days)`,
                  }))}
                  hasError={!!errors.netDaysId}
                />
                {errors.netDaysId && (
                  <p className="text-sm text-red-500">{errors.netDaysId}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="reason">Reason *</Label>
                <SearchSelect
                  value={creditData.reason}
                  onValueChange={value => handleInputChange('reason', value)}
                  placeholder="Select reason"
                  items={localDropdownData.creditReasonList.map(item => ({
                    value: item.id,
                    label: item.reasonName,
                  }))}
                  hasError={!!errors.reason}
                />
                {errors.reason && (
                  <p className="text-sm text-red-500">{errors.reason}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="creditStartDate">Credit Start Date *</Label>
                <Input
                  id="creditStartDate"
                  type="date"
                  value={creditData.creditStartDate ? creditData.creditStartDate.split('T')[0] : ''}
                  onChange={e =>
                    handleInputChange('creditStartDate', e.target.value)
                  }
                  className={errors.creditStartDate ? 'has-error' : ''}
                />
                {errors.creditStartDate && (
                  <p className="text-sm text-red-500">
                    {errors.creditStartDate}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <div className="flex items-center space-x-2">
                  <Switch
                    id="autoSuspendOnOverdue"
                    checked={creditData.autoSuspendOnOverdue}
                    onCheckedChange={checked =>
                      handleInputChange('autoSuspendOnOverdue', checked)
                    }
                  />
                  <Label htmlFor="autoSuspendOnOverdue">
                    Auto Suspend on Overdue
                  </Label>
                </div>
              </div>
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
