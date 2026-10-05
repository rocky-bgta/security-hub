import { X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Avatar, AvatarFallback } from 'common/Avatar';
import { Button } from 'common/Button';
import { Card, CardHeader } from 'common/Card';
import { Dialog, DialogContent, DialogTitle } from 'common/Dialog';
import Step1 from 'features/msp-admin/edit/Step1';
import Step2 from 'features/msp-admin/edit/Step2';
import { useAPI } from 'hooks/UseAPI';
import { FileType, useUploader } from 'hooks/UseUploader';
import {
  IMSPOnboarding,
  IMSPEditPayload,
  IMSPProductUpdate,
  IMSPProfile,
} from 'models/MSP';
import {
  ICountry,
  IDropdownData,
  IIndustry,
  ILanguage,
  IMspType,
  IOrganizationSize,
  IOrganizationType,
  IState,
  ITimeZone,
} from 'models/Dropdown';
import { IList, IResponse } from 'models/Global';
import { IMSPTier } from 'models/MSP';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { cn } from 'utils/Helper';
import { DiscountType } from 'models/Client';

interface IProps {
  hostPath: typeof routes;
}

const steps = [
  {
    id: 1,
    value: 'organization-details',
    title: 'Organization Details',
    description: 'Basic company information',
  },
  {
    id: 2,
    value: 'billing-details',
    title: 'Billing Details',
    description: 'Billing and contact information',
  },
];

const MSPEdit = ({ hostPath }: IProps) => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState<boolean>(true);
  const [showAlert, setShowAlert] = useState<boolean>(false);
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [mspData, setMspData] = useState<Partial<IMSPOnboarding>>();
  const [formData, setFormData] = useState<Partial<IMSPOnboarding>>({
    organization: {
      organizationName: '',
      organizationType: '',
      contactEmail: '',
      phoneNumber: '',
      phoneCode: '',
      country: '',
      stateProvince: '',
      timeZone: '',
      language: '',
      industry: '',
      domain: '',
      organizationSize: '',
      streetAddress: '',
      streetAddressLine2: '',
      city: '',
      zipPostalCode: '',
      logoUrl: '',
      tierId: '',
      mspAdminEmail: '',
      organizationLogo: null,
      mspTypeId: '',
    },
    billing: {
      billingEmail: '',
      billingName: '',
      useSameAsOrganizationAddress: false,
      streetAddress: '',
      streetAddressLine2: '',
      city: '',
      stateProvince: '',
      country: '',
      zipPostalCode: '',
    },
    creditInfo: {
      enableCredit: false,
      creditAmount: 0,
      reason: '',
      netDaysId: '',
      autoSuspendOnOverdue: false,
      creditStartDate: '',
      creditEndDate: '',
      netDays: { id: '', name: '' },
    },
    productSelections: [],
    invoice: {
      subtotal: 0,
      discountType: DiscountType.PERCENTAGE,
      discountValue: 0,
      discountPercentage: 0,
      discountAmount: 0,
      couponCode: '',
      vatRate: 0,
      vatAmount: 0,
      totalAmount: 0,
      paymentStatus: 'PENDING',
      completedPayment: {
        paymentMethod: 'BANK_TRANSFER',
        bankTransferDetails: {
          bankName: '',
          accountNumber: '',
          bankBranchName: '',
          transactionNumber: '',
          paymentDate: '',
          paymentAmount: 0,
          transactionReceiptUrl: '',
          transactionReceiptFile: null,
        },
        checkPaymentDetails: {
          checkNumber: '',
          bankName: '',
          branchName: '',
          paymentDate: '',
          paymentAmount: 0,
          checkImageUrl: '',
          checkImageFile: null,
        },
      },
    },
  });
  const [dropdownData, setDropdownData] = useState<IDropdownData>({
    organizationTypes: [],
    countries: [],
    states: [],
    timeZones: [],
    languages: [],
    industries: [],
    organizationSizes: [],
    tier: [],
    mspTypes: [],
  });
  const [notes, setNotes] = useState<string>('');
  const [productUpdates, setProductUpdates] = useState<Array<IMSPProductUpdate>>(
    [],
  );
  const [assignedClientIds, setAssignedClientIds] = useState<Array<string>>([]);
  const [originalAssignedClientIds, setOriginalAssignedClientIds] = useState<
    Array<string>
  >([]);

  const apiClient = useAPI();
  const { uploadFile } = useUploader();

  useEffect(() => {
    const loadData = async () => {
      // First load dropdown data, then fetch MSP data
      await fetchDropdownData();
      if (id) {
        await fetchData();
      }
    };
    loadData();
  }, [id]);

  type DropdownRef =
    | string
    | { id?: string; name?: string }
    | null
    | undefined;

  const getDropdownLabel = (value: DropdownRef): string => {
    if (!value) return '';
    if (typeof value === 'string') return value;
    if (typeof value === 'object') {
      return value.name || value.id || '';
    }
    return '';
  };

  const findIdFromName = (
    value: DropdownRef,
    items: Array<{ id: string; name: string }>,
  ): string => {
    if (!value) return '';
    if (typeof value === 'object' && value.id) return value.id;

    const name = getDropdownLabel(value);
    if (!name) return '';

    const foundById = items.find(item => item.id === name);
    if (foundById) return foundById.id;

    const foundByName = items.find(
      item => item.name.toLowerCase() === name.toLowerCase(),
    );
    return foundByName?.id || name;
  };

  const fetchData = async () => {
    try {
      setLoading(true);

      // Fetch dropdown data first if not already loaded
      let currentDropdownData = dropdownData;
      if (currentDropdownData.countries.length === 0) {
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
            IResponse<Array<IOrganizationType>>,
            IResponse<Array<ICountry>>,
            IResponse<Array<IState>>,
            IResponse<Array<ITimeZone>>,
            IResponse<Array<ILanguage>>,
            IResponse<Array<IIndustry>>,
            IResponse<Array<IOrganizationSize>>,
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

        currentDropdownData = {
          organizationTypes: orgTypeRes.data.map(item => ({
            id: item.id,
            name: item.organizationType!,
          })),
          countries: countryRes.data.map(item => ({
            id: item.id,
            name: item.name,
            code: item.code,
            phoneCode: item.phoneCode,
          })),
          states: stateRes.data.map(item => ({
            id: item.id,
            name: item.name,
            countryId: item.countryId,
          })),
          timeZones: timeZoneRes.data.map(item => ({
            id: item.id,
            name: item.displayName + ' (' + item.timezoneId + ')',
            countryId: item.countryId,
            stateId: item.stateId,
          })),
          languages: languageRes.data.map(item => ({
            id: item.id,
            name: item.displayName!,
            code: item.code,
          })),
          industries: industryRes.data.map(item => ({
            id: item.id,
            name: item.name,
          })),
          organizationSizes: orgSizeRes.data.map(item => ({
            id: item.id,
            name: item.name + ' (' + item.range + ')',
          })),
        };
        setDropdownData(currentDropdownData);
        currentDropdownData = await fetchDropdownData();
      }

      const response: IResponse<IMSPProfile> = await apiClient.get(
        API_END_POINTS.MSP_VIEW.replace(':id', id as string),
      );

      const findCountryId = (value: DropdownRef) =>
        findIdFromName(value, currentDropdownData.countries);
      const findStateId = (value: DropdownRef, countryId: string) => {
        if (!value) return '';
        if (typeof value === 'object' && value.id) return value.id;

        const name = getDropdownLabel(value);
        if (!name) return '';

        const foundById = currentDropdownData.states.find(
          s => s.id === name && s.countryId === countryId,
        );
        if (foundById) return foundById.id;

        const foundByName = currentDropdownData.states.find(
          s =>
            s.name.toLowerCase() === name.toLowerCase() &&
            s.countryId === countryId,
        );
        return foundByName?.id || name;
      };
      const findTimeZoneId = (
        value: DropdownRef,
        countryId: string,
        stateId: string,
      ) => {
        if (!value || !countryId) return '';
        if (typeof value === 'object' && value.id) return value.id;

        const name = getDropdownLabel(value);
        if (!name) return '';

        const item = currentDropdownData.timeZones.find(
          tz =>
            (tz.name.toLowerCase().includes(name.toLowerCase()) ||
              name.toLowerCase().includes(tz.name.toLowerCase())) &&
            tz.countryId === countryId &&
            (!stateId || tz.stateId === stateId),
        );
        return item?.id || '';
      };
      const findLanguageId = (value: DropdownRef) =>
        findIdFromName(value, currentDropdownData.languages);
      const findIndustryId = (value: DropdownRef) =>
        findIdFromName(value, currentDropdownData.industries);
      const findOrgSizeId = (value: DropdownRef) =>
        findIdFromName(value, currentDropdownData.organizationSizes);
      const findOrgTypeId = (value: DropdownRef) =>
        findIdFromName(value, currentDropdownData.organizationTypes);

      const orgCountryId = findCountryId(response.data.organization.country);
      const orgStateId = findStateId(
        response.data.organization.stateProvince,
        orgCountryId,
      );
      const orgTimeZoneId = findTimeZoneId(
        response.data.organization.timeZone,
        orgCountryId,
        orgStateId,
      );

      const orgCountryPhoneCode =
        currentDropdownData.countries.find(item => item.id === orgCountryId)
          ?.phoneCode || '';

      const orgData: IMSPOnboarding['organization'] = {
        organizationName: response.data.organization.organizationName || '',
        organizationType:
          findOrgTypeId(response.data.organization.organizationType) || '',
        contactEmail: response.data.organization.contactEmail || '',
        phoneNumber: response.data.organization.phoneNumber || '',
        phoneCode: orgCountryPhoneCode,
        country: orgCountryId,
        stateProvince: orgStateId,
        timeZone: orgTimeZoneId,
        language: findLanguageId(response.data.organization.language) || '',
        industry: findIndustryId(response.data.organization.industry) || '',
        domain: response.data.organization.domain || '',
        organizationSize:
          findOrgSizeId(response.data.organization.organizationSize) || '',
        streetAddress: response.data.organization.streetAddress || '',
        streetAddressLine2: response.data.organization.streetAddressLine2 || '',
        city: response.data.organization.city || '',
        zipPostalCode: response.data.organization.zipPostalCode || '',
        logoUrl: response.data.organization.logoUrl || '',
        tierId: response.data.organization.tier?.id || '',
        mspAdminEmail: response.data.organization.mspAdminEmail || '',
        organizationLogo: null,
        mspTypeId: response.data.organization.mspType?.id || '',
      };

      const billingCountryId = findCountryId(response.data.billingCountry);
      const billingStateId = findStateId(
        response.data.billingStateProvince,
        billingCountryId,
      );

      const creditData: IMSPOnboarding['creditInfo'] = {
        enableCredit: response.data.creditInfo?.enableCredit || false,
        creditAmount: response.data.creditInfo?.creditAmount || 0,
        reason: response.data.creditInfo?.reason || '',
        netDaysId: response.data.creditInfo?.netDays?.id || '',
        autoSuspendOnOverdue:
          response.data.creditInfo?.autoSuspendOnOverdue || false,
        creditStartDate: response.data.creditInfo?.creditStartDate || '',
        creditEndDate: response.data.creditInfo?.creditEndDate || '',
        netDays: response.data.creditInfo?.netDays || null,
      };

      const customizedData: Partial<IMSPOnboarding> = {
        organization: orgData,
        billing: {
          billingEmail: response.data.billingEmail || '',
          billingName: response.data.billingName || '',
          useSameAsOrganizationAddress: false,
          streetAddress: response.data.billingStreetAddress || '',
          streetAddressLine2: response.data.billingStreetAddressLine2 || '',
          city: response.data.billingCity || '',
          stateProvince: billingStateId,
          country: billingCountryId,
          zipPostalCode: response.data.billingZipPostalCode || '',
        },
        creditInfo: creditData,
        productSelections: [],
        invoice: {
          subtotal: 0,
          discountType: DiscountType.PERCENTAGE,
          discountValue: 0,
          discountPercentage: 0,
          discountAmount: 0,
          couponCode: '',
          vatRate: 0,
          vatAmount: 0,
          totalAmount: 0,
          paymentStatus: 'PENDING',
          completedPayment: {
            paymentMethod: 'BANK_TRANSFER',
            bankTransferDetails: {
              bankName: '',
              accountNumber: '',
              bankBranchName: '',
              transactionNumber: '',
              paymentDate: '',
              paymentAmount: 0,
              transactionReceiptUrl: '',
              transactionReceiptFile: null,
            },
            checkPaymentDetails: {
              checkNumber: '',
              bankName: '',
              branchName: '',
              paymentDate: '',
              paymentAmount: 0,
              checkImageUrl: '',
              checkImageFile: null,
            },
          },
        },
      };
      setFormData(customizedData);
      setMspData(customizedData);
      setNotes(response.data.notes || '');
      setProductUpdates([]);
      const clientIds =
        response.data.assignedClients?.map(client => client.clientId) || [];
      setAssignedClientIds(clientIds);
      setOriginalAssignedClientIds(clientIds);
    } catch (error) {
      console.error('Error fetching MSP data:', error);
      toast.error('Failed to load MSP data');
    } finally {
      setLoading(false);
    }
  };

  const fetchDropdownData = async (): Promise<IDropdownData> => {
    try {
      const [
        orgTypeRes,
        countryRes,
        stateRes,
        timeZoneRes,
        languageRes,
        industryRes,
        orgSizeRes,
        tierRes,
        mspTypeRes,
      ] = await Promise.all<
        [
          IResponse<Array<IOrganizationType>>,
          IResponse<Array<ICountry>>,
          IResponse<Array<IState>>,
          IResponse<Array<ITimeZone>>,
          IResponse<Array<ILanguage>>,
          IResponse<Array<IIndustry>>,
          IResponse<Array<IOrganizationSize>>,
          IResponse<IList<IMSPTier>>,
          IResponse<Array<IMspType>>,
        ]
      >([
        apiClient.get(API_END_POINTS.GET_ORGANIZATION_TYPE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_COUNTRY_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_STATE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_TIME_ZONE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_LANGUAGE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_INDUSTRIES_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_ORGANIZATION_SIZE_LIST),
        apiClient.get(
          API_END_POINTS.GET_TIER_LIST + '?status=true&offset=0&limit=100',
        ),
        apiClient.get(API_END_POINTS.GET_ACTIVE_MSP_TYPE_LIST),
      ]);

      const data: IDropdownData = {
        organizationTypes: orgTypeRes.data.map(item => ({
          id: item.id,
          name: item.organizationType!,
        })),
        countries: countryRes.data.map(item => ({
          id: item.id,
          name: item.name,
          code: item.code,
          phoneCode: item.phoneCode,
        })),
        states: stateRes.data.map(item => ({
          id: item.id,
          name: item.name,
          countryId: item.countryId,
        })),
        timeZones: timeZoneRes.data.map(item => ({
          id: item.id,
          name: item.displayName + ' (' + item.timezoneId + ')',
          countryId: item.countryId,
          stateId: item.stateId,
        })),
        languages: languageRes.data.map(item => ({
          id: item.id,
          name: item.displayName!,
          code: item.code,
        })),
        industries: industryRes.data.map(item => ({
          id: item.id,
          name: item.name,
        })),
        organizationSizes: orgSizeRes.data.map(item => ({
          id: item.id,
          name: item.name + ' (' + item.range + ')',
        })),
        tier: tierRes.data.items.map(item => ({
          id: item.id,
          name: item.tierName,
        })),
        mspTypes: Array.isArray(mspTypeRes.data)
          ? mspTypeRes.data.map(item => ({
              id: item.id,
              name: item.name,
            }))
          : [],
      };

      setDropdownData(data);
      return data;
    } catch (error) {
      console.error('Error fetching dropdown data:', error);
      return dropdownData;
    }
  };

  const buildEditPayload = (
    organization: IMSPOnboarding['organization'],
    billing: IMSPOnboarding['billing'],
    creditInfo?: IMSPOnboarding['creditInfo'],
  ): IMSPEditPayload => {
    const creditStartDate = creditInfo?.creditStartDate
      ? new Date(creditInfo.creditStartDate).toISOString()
      : '';

    return {
      organizationName: organization.organizationName,
      mspTier: organization.tierId,
      mspTypeId: organization.mspTypeId || '',
      contactEmail: organization.contactEmail,
      mspAdminEmail: organization.mspAdminEmail,
      phoneNumber: organization.phoneNumber,
      phoneCode: organization.phoneCode || '',
      country: organization.country,
      stateProvince: organization.stateProvince,
      timeZone: organization.timeZone,
      language: organization.language,
      industry: organization.industry,
      domain: organization.domain,
      organizationType: organization.organizationType,
      organizationSize: organization.organizationSize,
      organizationStreetAddress: organization.streetAddress,
      organizationStreetAddressLine2: organization.streetAddressLine2 || '',
      organizationCity: organization.city,
      organizationStateProvince: organization.stateProvince,
      organizationCountry: organization.country,
      organizationZipPostalCode: organization.zipPostalCode,
      logoUrl: organization.logoUrl || '',

      billingEmail: billing.billingEmail,
      billingName: billing.billingName,
      billingStreetAddress: billing.streetAddress,
      billingStreetAddressLine2: billing.streetAddressLine2 || '',
      billingCity: billing.city,
      billingStateProvince: billing.stateProvince,
      billingCountry: billing.country,
      billingZipPostalCode: billing.zipPostalCode,

      productUpdates,
      clientIdsToAssign: assignedClientIds.filter(
        clientId => !originalAssignedClientIds.includes(clientId),
      ),
      clientIdsToRemove: originalAssignedClientIds.filter(
        clientId => !assignedClientIds.includes(clientId),
      ),

      creditUpdate: {
        enableCredit: creditInfo?.enableCredit || false,
        reason: creditInfo?.reason || '',
        creditAmount: creditInfo?.creditAmount || 0,
        netDaysId: creditInfo?.netDaysId || '',
        creditStartDate,
        autoSuspendOnOverdue: creditInfo?.autoSuspendOnOverdue || false,
      },
      notes,
    };
  };

  const handleSave = async () => {
    if (formData.organization == null || formData.billing == null) return;

    setLoading(true);

    const organization = { ...formData.organization };

    if (organization.organizationLogo) {
      const { url, error } = await uploadFile(
        organization.organizationLogo,
        FileType.LOGO,
      );
      if (error) {
        toast.error(error);
        setLoading(false);
        return;
      }

      organization.logoUrl = url;
    }
    delete organization.organizationLogo;

    const payload = buildEditPayload(
      organization,
      formData.billing,
      formData.creditInfo,
    );

    try {
      await apiClient.put(
        API_END_POINTS.EDIT_MSP_ADMIN.replace(':id', id as string),
        {
          data: payload,
        },
      );

      toast.success('MSP Admin has been updated successfully.');
      navigate(hostPath.mspList.path);
    } catch (error) {
      console.error('Error updating MSP admin:', error);
      toast.error('Failed to update MSP admin. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    let hasChanges = false;
    if (formData.organization && mspData?.organization) {
      for (const item in formData.organization) {
        const key = item as keyof typeof formData.organization;
        if (formData.organization?.[key] !== mspData?.organization?.[key]) {
          hasChanges = true;
          break;
        }
      }
    }

    if (hasChanges) {
      setShowAlert(true);
    } else {
      navigate(hostPath.mspList.path);
    }
  };

  const updateFormData = (
    section: keyof IMSPOnboarding,
    data: IMSPOnboarding[keyof IMSPOnboarding],
  ) => {
    setFormData(prev => ({
      ...prev,
      [section]: data,
    }));
  };

  const handleNext = () => {
    if (currentStep < steps.length) {
      setCurrentStep(currentStep + 1);
    } else {
      handleSave();
    }
  };

  const handlePrevious = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <Step1
            data={
              formData.organization ||
              ({
                organizationName: '',
                organizationType: '',
                contactEmail: '',
                phoneNumber: '',
                phoneCode: '',
                country: '',
                stateProvince: '',
                timeZone: '',
                language: '',
                industry: '',
                domain: '',
                organizationSize: '',
                streetAddress: '',
                streetAddressLine2: '',
                city: '',
                zipPostalCode: '',
                logoUrl: '',
                tierId: '',
                mspAdminEmail: '',
                organizationLogo: null,
              } as IMSPOnboarding['organization'])
            }
            creditInfo={
              formData.creditInfo ||
              ({
                enableCredit: false,
                creditAmount: 0,
                reason: '',
                netDaysId: '',
                autoSuspendOnOverdue: false,
                creditStartDate: '',
              } as IMSPOnboarding['creditInfo'])
            }
            dropdownData={dropdownData}
            onUpdate={(organizationData, creditData) => {
              updateFormData('organization', organizationData);
              updateFormData('creditInfo', creditData);
            }}
            onNext={handleNext}
          />
        );
      case 2:
        return (
          <Step2
            data={
              formData.billing || {
                billingEmail: '',
                billingName: '',
                useSameAsOrganizationAddress: false,
                streetAddress: '',
                streetAddressLine2: '',
                city: '',
                stateProvince: '',
                country: '',
                zipPostalCode: '',
              }
            }
            prevStepData={
              formData.organization ||
              ({
                organizationName: '',
                organizationType: '',
                contactEmail: '',
                phoneNumber: '',
                country: '',
                stateProvince: '',
                timeZone: '',
                language: '',
                industry: '',
                domain: '',
                organizationSize: '',
                streetAddress: '',
                streetAddressLine2: '',
                city: '',
                zipPostalCode: '',
                logoUrl: '',
                tierId: '',
                mspAdminEmail: '',
                organizationLogo: null,
              } as IMSPOnboarding['organization'])
            }
            dropdownData={dropdownData}
            onUpdate={data => updateFormData('billing', data)}
            onNext={handleNext}
            onPrevious={handlePrevious}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex flex-col gap-4">
          <h2>Edit MSP</h2>
          <p className="text-muted-foreground">
            Modify MSP partner details and settings
          </p>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex gap-2">
            <Button variant="outline" onClick={handleCancel}>
              <X className="mr-2 size-4" />
              Cancel
            </Button>
          </div>
        </div>
      </div>

      {loading && !mspData ? (
        <p className="text-center text-sm text-muted-foreground">Loading...</p>
      ) : (
        <>
          <Card>
            <CardHeader>
              <div className="flex items-center space-x-4">
                <Avatar className="size-16">
                  <AvatarFallback className="text-lg">
                    {formData.organization?.organizationName
                      ? formData.organization.organizationName
                        .split(' ')
                        .map(n => n[0])
                        .join('')
                        .toUpperCase()
                      : 'MSP'}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1">
                  <h2 className="text-2xl font-bold">
                    {formData.organization?.organizationName || 'Edit MSP'}
                  </h2>
                  <p className="text-muted-foreground">
                    {formData.organization?.mspAdminEmail || ''}
                  </p>
                </div>
              </div>
            </CardHeader>
          </Card>
          <div className="grid grid-cols-2 gap-2">
            {steps.map(step => (
              <div
                key={step.id}
                className={cn(
                  'rounded-lg border border-primary p-2 text-center transition-all duration-200',
                  step.id === currentStep
                    ? 'border-2 bg-primary/10'
                    : step.id < currentStep
                      ? 'bg-[#00d4aa]/10'
                      : 'bg-transparent',
                )}
              >
                <div className="text-xs font-medium text-primary">
                  {step.title}
                </div>
              </div>
            ))}
          </div>
          {renderStep()}
        </>
      )}

      {showAlert && (
        <Dialog open={showAlert} onOpenChange={setShowAlert}>
          <DialogContent className="!w-1/3 !px-6 py-6">
            <DialogTitle>Confirm Cancel</DialogTitle>

            <h4 className="mb-4 text-base text-white">
              Are you sure you want to leave without saving changes?
            </h4>
            <div className="flex justify-end gap-3">
              <Button variant="destructive" onClick={() => setShowAlert(false)}>
                Cancel
              </Button>
              <Button onClick={() => navigate(hostPath.mspList.path)}>
                Confirm
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
};

export default MSPEdit;
