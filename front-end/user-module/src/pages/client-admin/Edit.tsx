import { X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Avatar, AvatarFallback } from 'common/Avatar';
import { Button } from 'common/Button';
import { Card, CardHeader } from 'common/Card';
import { Dialog, DialogContent, DialogTitle } from 'common/Dialog';
import Step1 from 'features/client-admin/onboarding/Step1';
import Step2 from 'features/client-admin/onboarding/Step2';
import Step3 from 'features/client-admin/onboarding/Step3';
import { useAPI } from 'hooks/UseAPI';
import { FileType, useUploader } from 'hooks/UseUploader';
import { IClientOnboarding } from 'models/Client';
import {
  ICountry,
  IDropdownData,
  IIndustry,
  ILanguage,
  IOrganizationSize,
  IOrganizationType,
  IState,
  ISubIndustry,
  ICompliance,
  ITimeZone,
} from 'models/Dropdown';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { cn } from 'utils/Helper';
import { IClientProduct } from './List';

interface IProps {
  hostPath: typeof routes;
}

export interface IClientAdminDetails {
  id: string;
  email: string;
  organizationName: string;
  contactEmail: string;
  phoneNumber: string;
  phoneCode: string;
  billingName: string;
  billingEmail: string;
  billingUseSameAsOrganizationAddress: boolean;
  billingStreetAddress: string;
  billingStreetAddressLine2: string;
  billingCity: string;
  billingZipPostalCode: string;
  billingState: {
    id: string;
    code: string;
    name: string;
    active: boolean;
  } | null;
  billingCountry: {
    id: string;
    code: string;
    name: string;
    active: boolean;
  } | null;
  mspId: string;
  countryCode: string | null;
  stateCode: string | null;
  domain: string;
  streetAddress: string;
  streetAddressLine2: string;
  city: string;
  zipPostalCode: string;
  logoUrl: string;
  status: string;
  createdAt: string;
  clientAdminId: string | null;
  creditId: string | null;
  tierId: string | null;
  mspType: string | null;
  mspAdminEmail: string | null;
  netDays: number | null;
  department: string | null;
  roleIds: string[] | null;
  clientProducts: IClientProduct[];
  country: {
    id: string;
    code: string;
    name: string;
    active: boolean;
  };
  state: {
    id: string;
    code: string;
    name: string;
    active: boolean;
  };
  timeZone: {
    id: string;
    code: string;
    name: string;
    displayName: string;
    active: boolean;
  };
  language: {
    id: string;
    code: string;
    name: string;
    displayName: string;
    active: boolean;
  };
  industry: {
    id: string;
    code: string;
    name: string;
    active: boolean;
  };
  subIndustry?: {
    id: string;
    name: string;
  } | null;
  complianceId: string | null;
  complianceName: string | null;
  organizationSize: {
    id: string;
    name: string;
    range: string;
  };
  organizationType: {
    id: string;
    name: string;
  };
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
  {
    id: 3,
    value: 'msp-partner',
    title: 'MSP Partner',
    description: 'Select managed service provider',
  },
];

const ClientEdit = ({ hostPath }: IProps) => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState<boolean>(false);
  const [showAlert, setShowAlert] = useState<boolean>(false);
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [clientAdminData, setClientAdminData] =
    useState<Partial<IClientOnboarding>>();
  const [formData, setFormData] = useState<Partial<IClientOnboarding>>({
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
      subIndustryId: '',
      subIndustryName: '',
      complianceId: '',
      complianceName: '',
      domain: '',
      organizationSize: '',
      adminEmail: '',
      streetAddress: '',
      streetAddressLine2: '',
      city: '',
      zipPostalCode: '',
      organizationLogo: null,
      logoUrl: '',
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
    mspId: '',
  });
  const [dropdownData, setDropdownData] = useState<IDropdownData>({
    organizationTypes: [],
    countries: [],
    states: [],
    timeZones: [],
    languages: [],
    industries: [],
    organizationSizes: [],
    subIndustries: [],
    compliances: [],
  });

  const apiClient = useAPI();
  const { uploadFile } = useUploader();

  useEffect(() => {
    if (id) {
      fetchData();
    }
  }, [id]);

  useEffect(() => {
    fetchDropdownData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const response: IResponse<IClientAdminDetails> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_DETAILS.replace(':id', id as string),
      );

      const customizedData = {
        organization: {
          organizationName: response.data.organizationName,
          organizationType: response.data.organizationType?.id,
          contactEmail: response.data.contactEmail,
          phoneNumber: response.data.phoneNumber,
          phoneCode: response.data.phoneCode ?? '',
          country: response.data.country?.id,
          stateProvince: response.data.state?.id,
          timeZone: response.data.timeZone?.id,
          language: response.data.language?.id,
          industry: response.data.industry?.id,
          subIndustryId: response.data.subIndustry?.id ?? '',
          subIndustryName: response.data.subIndustry?.name ?? '',
          complianceId: response.data.complianceId ?? '',
          complianceName: response.data.complianceName ?? '',
          domain: response.data.domain,
          organizationSize: response.data.organizationSize?.id,
          adminEmail: response.data.email,
          streetAddress: response.data.streetAddress,
          streetAddressLine2: response.data.streetAddressLine2,
          city: response.data.city,
          zipPostalCode: response.data.zipPostalCode,
          logoUrl: response.data.logoUrl,
        },
        billing: {
          billingEmail: response.data.billingEmail,
          billingName: response.data.billingName,
          useSameAsOrganizationAddress:
            response.data.billingUseSameAsOrganizationAddress,
          streetAddress: response.data.billingStreetAddress,
          streetAddressLine2: response.data.billingStreetAddressLine2,
          city: response.data.billingCity,
          zipPostalCode: response.data.billingZipPostalCode,
          stateProvince: response.data.billingState?.id ?? '',
          country: response.data.billingCountry?.id ?? '',
        },
        mspId: response.data.mspId,
      };
      setFormData(customizedData);
      setClientAdminData(customizedData);
    } catch (error) {
      console.error('Error fetching client admin data:', error);
    } finally {
      setLoading(false);
    }
  };

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
        subIndustryRes,
        complianceRes,
      ] = await Promise.all<
        [
          IResponse<Array<IOrganizationType>>,
          IResponse<Array<ICountry>>,
          IResponse<Array<IState>>,
          IResponse<Array<ITimeZone>>,
          IResponse<Array<ILanguage>>,
          IResponse<Array<IIndustry>>,
          IResponse<Array<IOrganizationSize>>,
          IResponse<Array<ISubIndustry>>,
          IResponse<Array<ICompliance>>,
        ]
      >([
        apiClient.get(API_END_POINTS.GET_ORGANIZATION_TYPE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_COUNTRY_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_STATE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_TIME_ZONE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_LANGUAGE_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_INDUSTRIES_LIST),
        apiClient.get(API_END_POINTS.GET_ACTIVE_ORGANIZATION_SIZE_LIST),
        apiClient.get(API_END_POINTS.GET_SUB_INDUSTRIES_LIST),
        apiClient.get(API_END_POINTS.GET_COMPLIANCE_LIST),
      ]);

      setDropdownData({
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
        subIndustries: subIndustryRes.data.map(item => ({
          id: item.id,
          name: item.name,
          industryId: item.industryId,
        })),
        compliances: complianceRes.data.map(item => ({
          id: item.id,
          name: item.complianceName ?? '',
        })),
      });
    } catch (error) {
      console.error('Error fetching dropdown data:', error);
    }
  };

  const handleSave = async () => {
    if (formData.organization == null || formData.billing == null) return;

    setLoading(true);

    if (formData.organization?.organizationLogo) {
      const { url, error } = await uploadFile(
        formData.organization.organizationLogo,
        FileType.LOGO,
      );
      if (error) {
        toast.error(error);
        return { success: false };
      }

      formData.organization.logoUrl = url;
    }
    delete formData.organization.organizationLogo;

    const payload = {
      ...formData.organization,
      organizationName: formData.organization.organizationName,
      organizationType: formData.organization.organizationType,
      contactEmail: formData.organization.contactEmail,
      phoneNumber: formData.organization.phoneNumber,
      country: formData.organization.country,
      stateProvince: formData.organization.stateProvince,
      timeZone: formData.organization.timeZone,
      language: formData.organization.language,
      industry: formData.organization.industry,
      subIndustryId: formData.organization.subIndustryId,
      subIndustryName: formData.organization.subIndustryName,
      complianceId: formData.organization.complianceId,
      complianceName: formData.organization.complianceName,
      domain: formData.organization.domain,
      organizationSize: formData.organization.organizationSize,
      adminEmail: formData.organization.adminEmail,
      streetAddress: formData.organization.streetAddress,
      streetAddressLine2: formData.organization.streetAddressLine2,
      city: formData.organization.city,
      zipPostalCode: formData.organization.zipPostalCode,

      billingEmail: formData.billing.billingEmail,
      billingName: formData.billing.billingName,
      sameAsOrganizationAddress: formData.billing.useSameAsOrganizationAddress,
      billingStreetAddress: formData.billing.streetAddress,
      billingStreetAddressLine2: formData.billing.streetAddressLine2,
      billingCity: formData.billing.city,
      billingStateProvince: formData.billing.stateProvince,
      billingCountry: formData.billing.country,
      billingZipPostalCode: formData.billing.zipPostalCode,

      mspId: formData.mspId,
    };

    try {
      await apiClient.put(
        API_END_POINTS.EDIT_CLIENT_ADMIN.replace(':id', id as string),
        {
          data: payload,
        },
      );

      toast.success('Client Admin has been updated successfully.');
      navigate(hostPath.clientList.path);
    } catch (error) {
      console.error('Error updating client admin:', error);
      toast.error('Failed to update client admin. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    let hasChanges = false;
    for (const item in formData.organization) {
      const key = item as keyof typeof formData.organization;
      if (
        formData.organization?.[key] !== clientAdminData?.organization?.[key]
      ) {
        hasChanges = true;
        break;
      }
    }

    if (hasChanges) {
      setShowAlert(true);
    } else {
      navigate(hostPath.clientList.path);
    }
  };

  const updateFormData = (
    section: keyof IClientOnboarding,
    data: IClientOnboarding[keyof IClientOnboarding],
  ) => {
    setFormData(prev => ({
      ...prev,
      [section]: data,
    }));

    if (section === 'mspId') handleSave();
  };

  const handleNext = () => {
    if (currentStep < steps.length + 1) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrevious = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - (currentStep === steps.length + 1 ? 2 : 1));
    }
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <Step1
            data={formData.organization!}
            dropdownData={dropdownData}
            onUpdate={data => updateFormData('organization', data)}
            onNext={handleNext}
          />
        );
      case 2:
        return (
          <Step2
            data={formData.billing!}
            prevStepData={formData.organization!}
            dropdownData={dropdownData}
            onUpdate={data => updateFormData('billing', data)}
            onNext={handleNext}
            onPrevious={handlePrevious}
          />
        );
      case 3:
        return (
          <Step3
            data={formData.mspId!}
            dropdownData={dropdownData}
            onUpdate={data => updateFormData('mspId', data)}
            onPrevious={handlePrevious}
            isEditing={true}
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
          <h2>Edit Client</h2>
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

      {loading && (
        <p className="text-center text-sm text-muted-foreground">Loading...</p>
      )}

      {!loading && clientAdminData && (
        <>
          <Card>
            <CardHeader>
              <div className="flex items-center space-x-4">
                <Avatar className="size-16">
                  <AvatarFallback className="text-lg">
                    {formData.organization?.organizationName
                      .split(' ')
                      .map(n => n[0])
                      .join('')
                      .toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1">
                  <h2 className="text-2xl font-bold">
                    {formData.organization?.organizationName}
                  </h2>
                  <p className="text-muted-foreground">
                    {formData.organization?.adminEmail}
                  </p>
                  {/* <div className="mt-2 flex items-center gap-4 text-sm text-muted-foreground">
                <span className="flex items-center gap-1">
                  <Users className="h-4 w-4" />
                  {mspData.usedLicenses}/{mspData.licenseCount} licenses
                </span>
                <span className="flex items-center gap-1">
                  <Calendar className="h-4 w-4" />
                  Member since {new Date(mspData.createdAt).getFullYear()}
                </span>
              </div> */}
                </div>
              </div>
            </CardHeader>
          </Card>
          <div className="grid grid-cols-3 gap-2">
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
              <Button onClick={() => navigate(hostPath.clientList.path)}>
                Confirm
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
};

export default ClientEdit;
