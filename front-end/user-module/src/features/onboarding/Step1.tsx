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
import useDebounce from 'hooks/UseDebounce';
import { IDropdownData } from 'models/Dropdown';
import { IList, IResponse } from 'models/Global';
import { IMspType } from 'models/Dropdown';
import {
  IMSPCreditInfo,
  IMSPOrganizationDetails,
  IMSPTier,
} from 'models/MSP';
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
  onNext: () => void;
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
  netDays?: string;
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

const MSPOnboardingStep1 = ({
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
  const [selectedCountryCode, setSelectedCountryCode] = useState<string>('us');
  const [localDropdownData, setLocalDropdownData] = useState<{
    tierCommissionList: Array<IMSPTier>;
    creditReasonList: Array<{ id: string; reasonName: string }>;
    netDaysList: Array<{
      id: string;
      netTermName: string;
      netTermInDays: number;
    }>;
    mspTypeList: Array<IMspType>;
  }>({
    tierCommissionList: [],
    creditReasonList: [],
    netDaysList: [],
    mspTypeList: [],
  });

  const [hasEmailUpdated, setHasEmailUpdated] = useState<boolean>(false);
  const [errors, setErrors] = useState<
    IMSPOrganizationErrors & IMSPCreditErrors
  >({});

  const apiClient = useAPI();

  const debouncedEmail = useDebounce(formData.mspAdminEmail, 500);

  useEffect(() => {
    if (!debouncedEmail || !isValidEmail(debouncedEmail)) return;

    if (hasEmailUpdated) {
      validateEmail();
    }
  }, [debouncedEmail]);

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

  const fetchLocalDropdownData = async () => {
    try {
      const [tierRes, creditReasonRes, netDaysRes, mspTypeRes] =
        await Promise.all<
          [
            IResponse<IList<IMSPTier>>,
            IResponse<IList<{ id: string; reasonName: string }>>,
            IResponse<
              IList<{ id: string; netTermName: string; netTermInDays: number }>
            >,
            IResponse<Array<IMspType>>,
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
            API_END_POINTS.GET_NET_DAYS_LIST +
            '?isActive=true&offset=0&limit=10',
          ),
          apiClient.get(API_END_POINTS.GET_ACTIVE_MSP_TYPE_LIST),
        ]);

      setLocalDropdownData(prev => ({
        ...prev,
        tierCommissionList: tierRes.data.items,
        creditReasonList: creditReasonRes.data.items,
        netDaysList: netDaysRes.data.items,
        mspTypeList: Array.isArray(mspTypeRes.data) ? mspTypeRes.data : [],
      }));
    } catch (error) {
      console.error('Error fetching local dropdown data:', error);
    }
  };

  useEffect(() => {
    fetchLocalDropdownData();
  }, []);

  const validateEmail = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.VALIDATE_EMAIL + encodeURIComponent(debouncedEmail),
      );
      if (response.data.exists) {
        setErrors(prev => ({
          ...prev,
          mspAdminEmail: 'This email is already registered',
        }));
      } else {
        setErrors(prev => ({ ...prev, mspAdminEmail: '' }));
      }

      setHasEmailUpdated(false);
    } catch (error) {
      console.error('Error validating email:', error);
      setErrors(prev => ({
        ...prev,
        mspAdminEmail: 'Error validating email',
      }));
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

    if (field === 'mspAdminEmail') {
      if (value !== data.mspAdminEmail) setHasEmailUpdated(true);
    }
  };

  const validateForm = () => {
    const newErrors: IMSPOrganizationErrors & IMSPCreditErrors = {};
    const formDataRequiredFields: Array<keyof IMSPOrganizationDetails> = [
      'organizationName',
      'mspTypeId',
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

          <div className="col-span-1 space-y-2">
            <Label htmlFor="mspAdminEmail">MSP Admin Email *</Label>
            <Input
              id="mspAdminEmail"
              type="email"
              value={formData.mspAdminEmail}
              onChange={e => handleInputChange('mspAdminEmail', e.target.value)}
              placeholder="admin@company.com"
              className={errors.mspAdminEmail ? 'has-error' : ''}
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
              className=""
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
            <Label htmlFor="mspTypeId">MSP Type *</Label>
            <SearchSelect
              value={formData.mspTypeId || ''}
              onValueChange={value => handleInputChange('mspTypeId', value)}
              placeholder="Select MSP type"
              items={localDropdownData.mspTypeList.map(item => ({
                value: item.id,
                label: item.name,
              }))}
              hasError={!!errors.mspTypeId}
            />
            {errors.mspTypeId && (
              <p className="text-sm text-red-500">{errors.mspTypeId}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="industry">Industry *</Label>
            <SearchSelect
              value={formData.industry}
              onValueChange={value => handleInputChange('industry', value)}
              placeholder="Select MSP industry"
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
            <Label htmlFor="tierId">MSP tier *</Label>
            <SearchSelect
              value={formData.tierId}
              onValueChange={value => handleInputChange('tierId', value)}
              placeholder="Select MSP tier"
              items={localDropdownData.tierCommissionList.map(item => ({
                value: item.id,
                label: item.tierName,
              }))}
              hasError={!!errors.tierId}
            />
            {errors.tierId && (
              <p className="text-sm text-red-500">{errors.tierId}</p>
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
        </div>

        {selectedTier && (
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Star className="size-5 text-blue-600" />
                  <span className="font-medium">Commission Rate</span>
                </div>
                <Badge className="bg-blue-600 text-white">
                  {selectedTier.commissionPercentage}% for{' '}
                  {selectedTier.tierName}
                </Badge>
              </div>
            </CardContent>
          </Card>
        )}

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-lg">
              <CreditCard className="size-5 text-amber-600" />
              Credit Allocation (Optional)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <Label htmlFor="creditToggle">Enable Credit Allocation</Label>
                <p className="text-sm text-gray-400">
                  Onboard with allocated credit
                </p>
              </div>
              <Switch
                id="creditToggle"
                checked={creditData.enableCredit}
                onCheckedChange={value =>
                  handleInputChange('enableCredit', value)
                }
              />
            </div>

            {creditData.enableCredit && (
              <div className="grid grid-cols-1 items-center gap-4 border-t pt-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="creditAmount">Credit Amount (USD) *</Label>
                  <Input
                    id="creditAmount"
                    type="number"
                    value={creditData.creditAmount}
                    onChange={e =>
                      handleInputChange('creditAmount', +e.target.value)
                    }
                    placeholder="Enter credit amount"
                    min="0"
                    step="0.01"
                  />
                  {errors.creditAmount && (
                    <p className="text-sm text-red-500">
                      {errors.creditAmount}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="reason">Reason for Credit</Label>
                  <SearchSelect
                    value={creditData.reason}
                    onValueChange={value => handleInputChange('reason', value)}
                    placeholder="Select reason"
                    items={localDropdownData.creditReasonList.map(reason => ({
                      value: reason.id,
                      label: reason.reasonName,
                    }))}
                  />
                  {errors.reason && (
                    <p className="text-sm text-red-500">{errors.reason}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="netTerms">Net Terms *</Label>
                  <SearchSelect
                    value={creditData.netDaysId}
                    onValueChange={value =>
                      handleInputChange('netDaysId', value)
                    }
                    placeholder="Select net terms"
                    items={localDropdownData.netDaysList.map(term => ({
                      value: term.id,
                      label: term.netTermName,
                    }))}
                  />
                  {errors.netDaysId && (
                    <p className="text-sm text-red-500">{errors.netDaysId}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="creditStartDate">Credit Start Date *</Label>
                  <Input
                    id="creditStartDate"
                    type="date"
                    value={creditData.creditStartDate}
                    onChange={e =>
                      handleInputChange('creditStartDate', e.target.value)
                    }
                    className={errors.creditStartDate ? 'has-error' : ''}
                  />
                  {errors.creditStartDate && (
                    <p className="text-sm text-red-500">{errors.creditStartDate}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="autoSuspendOnOverdue">
                      Auto Suspend on Overdue
                    </Label>
                    <Switch
                      id="autoSuspendOnOverdue"
                      checked={creditData.autoSuspendOnOverdue}
                      onCheckedChange={checked =>
                        handleInputChange('autoSuspendOnOverdue', checked)
                      }
                    />
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        <div className="flex justify-end pt-6">
          <Button onClick={handleSave}>Save & Continue</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default MSPOnboardingStep1;
