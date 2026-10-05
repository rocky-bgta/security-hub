import { safeRedirect } from 'home-module/security';
import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Card, CardContent } from 'common/Card';
import { Progress } from 'common/Progress';
import Step1 from 'features/client-admin/self-onboarding/Step1';
import Step2 from 'features/client-admin/self-onboarding/Step2';
import Step3 from 'features/client-admin/self-onboarding/Step3';
import Step4 from 'features/client-admin/self-onboarding/Step4';
import { useAPI } from 'hooks/UseAPI';
import { FileType, useUploader } from 'hooks/UseUploader';
import { DiscountType, IClientOnboarding, IInvoice } from 'models/Client';
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
import { cn, isSuccessResponse } from 'utils/Helper';
import { useStore } from 'hooks/UseStore';
import { IClientAdminDetails } from './Edit';

const steps = [
  {
    id: 1,
    title: 'Organization Details',
    description: 'Basic company information',
  },
  {
    id: 2,
    title: 'Billing Details',
    description: 'Billing and contact information',
  },
  {
    id: 3,
    title: 'Product Selection',
    description: 'Choose products and packages',
  },
  {
    id: 4,
    title: 'Invoice Generation',
    description: 'Generate and review invoice',
  },
];

interface IPreselectedProduct {
  productId: string;
  productName: string;
  productDescription: string;
}

const BuyProduct = () => {
  const { userInfo } = useStore();
  const location = useLocation();
  const preselectedProduct = (
    location.state as { preselectedProduct?: IPreselectedProduct } | null
  )?.preselectedProduct;
  const [loading, setLoading] = useState<boolean>(true);
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [showingSteps, setShowingSteps] = useState<
    Array<{ id: number; title: string; description: string }>
  >([]);
  const [formData, setFormData] = useState<IClientOnboarding>({
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
      domain: userInfo?.email?.split('@')[1],
      organizationSize: '',
      adminEmail: userInfo?.email,
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
    mspName: '',
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
          checkImageUrl: '',
          paymentAmount: 0,
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
    subIndustries: [],
    compliances: [],
  });

  const apiClient = useAPI();
  const { uploadFile } = useUploader();

  useEffect(() => {
    fetchDropdownData();
    fetchClientAdminData();
  }, []);

  useEffect(() => {
    if (userInfo.onboardBy === 'TRIAL') {
      setShowingSteps(steps);
    } else {
      setShowingSteps(steps.slice(2));
    }
  }, [userInfo]);

  useEffect(() => {
    if (!preselectedProduct || showingSteps.length === 0) return;

    const productSelectionStep = showingSteps.length === 4 ? 3 : 1;
    setCurrentStep(productSelectionStep);
  }, [preselectedProduct, showingSteps]);

  const progress = (currentStep / showingSteps.length) * 100;

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

  const fetchClientAdminData = async () => {
    try {
      const response: IResponse<IClientAdminDetails> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_DETAILS.replace(
          ':id',
          userInfo.userId as string,
        ),
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(
          response.message || 'Failed to fetch client admin data',
        );
      }

      setFormData(prev => ({
        ...prev,
        organization: {
          organizationName: response.data.organizationName ?? '',
          organizationType: response.data.organizationType?.id ?? '',
          contactEmail: response.data.contactEmail ?? '',
          phoneNumber: response.data.phoneNumber ?? '',
          phoneCode: response.data.phoneCode ?? '',
          country: response.data.country?.id ?? '',
          stateProvince: response.data.state?.id ?? '',
          timeZone: response.data.timeZone?.id ?? '',
          language: response.data.language?.id ?? '',
          industry: response.data.industry?.id ?? '',
          subIndustryId: response.data.subIndustry?.id ?? '',
          subIndustryName: response.data.subIndustry?.name ?? '',
          complianceId: response.data.complianceId ?? '',
          complianceName: response.data.complianceName ?? '',
          domain: response.data.domain ?? userInfo?.email?.split('@')[1],
          organizationSize: response.data.organizationSize?.id ?? '',
          adminEmail: response.data.email ?? userInfo?.email,
          streetAddress: response.data.streetAddress ?? '',
          streetAddressLine2: response.data.streetAddressLine2 ?? '',
          city: response.data.city ?? '',
          zipPostalCode: response.data.zipPostalCode ?? '',
          logoUrl: response.data.logoUrl ?? '',
          organizationLogo: null,
        },
        billing: {
          billingName: response.data.billingName ?? '',
          billingEmail: response.data.billingEmail ?? '',
          useSameAsOrganizationAddress:
            response.data.billingUseSameAsOrganizationAddress ?? false,
          streetAddress: response.data.billingStreetAddress ?? '',
          streetAddressLine2: response.data.billingStreetAddressLine2 ?? '',
          city: response.data.billingCity ?? '',
          stateProvince: (response as any).data.billingStateProvince ?? '',
          country: (response as any).data.billingCountry ?? '',
          zipPostalCode: response.data.billingZipPostalCode ?? '',
        },
        mspId: response.data.mspId ?? '',
        mspName: response.data.mspAdminEmail ?? '',
      }));
    } catch (error) {
      console.error('Error fetching client admin data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleNext = () => {
    if (currentStep < showingSteps.length + 1) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrevious = () => {
    if (currentStep > 1) {
      setCurrentStep(
        currentStep - (currentStep === showingSteps.length + 1 ? 2 : 1),
      );
    }
  };

  const updateFormData = <T extends keyof IClientOnboarding>(
    section: T,
    data: IClientOnboarding[T],
  ) => {
    setFormData(prev => ({
      ...prev,
      [section]: data,
    }));
  };

  const renderStep = () => {
    if (showingSteps.length === 4) {
      switch (currentStep) {
        case 1:
          return (
            <Step1
              data={formData.organization}
              dropdownData={dropdownData}
              onUpdate={data => updateFormData('organization', data)}
              onNext={handleNext}
            />
          );
        case 2:
          return (
            <Step2
              data={formData.billing}
              prevStepData={formData.organization}
              dropdownData={dropdownData}
              onUpdate={data => updateFormData('billing', data)}
              onNext={handleNext}
              onPrevious={handlePrevious}
            />
          );
        case 3:
          return (
            <Step3
              data={formData.productSelections}
              onUpdate={data => updateFormData('productSelections', data)}
              onNext={handleNext}
              onPrevious={handlePrevious}
              preselectedProductId={preselectedProduct?.productId}
            />
          );
        case 4:
        case 5:
          return (
            <Step4
              data={formData.invoice}
              formData={formData}
              loading={loading}
              onUpdate={data => updateFormData('invoice', data)}
              onNext={handleSubmit}
              onPrevious={handlePrevious}
            />
          );
        default:
          return null;
      }
    } else {
      switch (currentStep) {
        case 1:
          return (
            <Step3
              data={formData.productSelections}
              onUpdate={data => updateFormData('productSelections', data)}
              onNext={handleNext}
              onPrevious={handlePrevious}
              preselectedProductId={preselectedProduct?.productId}
            />
          );
        case 2:
        case 3:
          return (
            <Step4
              data={formData.invoice}
              formData={formData}
              loading={loading}
              onUpdate={data => updateFormData('invoice', data)}
              onNext={handleSubmit}
              onPrevious={handlePrevious}
            />
          );
        default:
          return null;
      }
    }
  };

  const handleSubmit = async (invoiceOverride?: IInvoice) => {
    const invoice = invoiceOverride ?? formData.invoice;

    setCurrentStep(Math.min(currentStep + 1, showingSteps.length));
    setLoading(true);

    if (formData.organization.organizationLogo) {
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
      clientAdminId: userInfo?.userId,
      organization: {
        ...formData.organization,
        organizationName: formData.organization.organizationName,
        organizationType: formData.organization.organizationType,
        contactEmail: formData.organization.contactEmail,
        phoneNumber:
          formData.organization.phoneNumber[0] === '+'
            ? formData.organization.phoneNumber
            : '+' + formData.organization.phoneNumber,
        country: formData.organization.country,
        stateProvince: formData.organization.stateProvince,
        timeZone: formData.organization.timeZone,
        language: formData.organization.language,
        industry: formData.organization.industry,
        domain: formData.organization.domain,
        organizationSize: formData.organization.organizationSize,
        adminEmail: formData.organization.adminEmail,
        streetAddress: formData.organization.streetAddress,
        streetAddressLine2: formData.organization.streetAddressLine2,
        city: formData.organization.city,
        zipPostalCode: formData.organization.zipPostalCode,
      },
      billing: {
        billingEmail: formData.billing.billingEmail,
        billingName: formData.billing.billingName,
        useSameAsOrganizationAddress:
          formData.billing.useSameAsOrganizationAddress,
        streetAddress: formData.billing.streetAddress,
        streetAddressLine2: formData.billing.streetAddressLine2,
        city: formData.billing.city,
        stateProvince: formData.billing.stateProvince,
        country: formData.billing.country,
        zipPostalCode: formData.billing.zipPostalCode,
      },
      productSelections: [
        ...formData.productSelections.map(product => ({
          productId: product.productId,
          productName: product.productName,
          packageId: product.packageId,
          packageName: product.packageName,
          licenseCount: product.licenseCount,
          pricePerLicense: product.pricePerLicense,
          validityPeriod: product.validityPeriod,
          validityUnit: product.validityUnit,
          userRangeId: product.userRangeId,
        })),
      ],
      invoice: {
        subtotal: invoice.subtotal,
        discountType: invoice.discountType,
        discountPercentage: invoice.discountPercentage,
        discountAmount: invoice.discountAmount,
        couponCode: invoice.couponCode,
        vatRate: invoice.vatRate,
        vatAmount: invoice.vatAmount,
        totalAmount: invoice.totalAmount,
        paymentStatus: invoice.paymentStatus,
      },
    };

    try {
      const response = await apiClient.post(
        API_END_POINTS.ONBOARD_CLIENT_ADMIN,
        {
          data: payload,
        },
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to generate invoice');
      }

      toast.success(response.message || 'Invoice generated successfully');

      const invoiceId = response.data.invoiceResponse.id;
      const amount = response.data.invoiceResponse.totalAmount;

      try {
        const resp = await apiClient.post(
          API_END_POINTS.INITIATE_ONLINE_PAYMENT,
          {
            data: {
              invoiceId: invoiceId,
              amount: amount,
              currency: 'USD',
              date: new Date().toISOString(),
              couponId: '',
              couponCode: payload.invoice.couponCode,
              paymentSources: [
                {
                  method: 'STRIPE',
                  amount: amount,
                  online: true,
                },
              ],
            },
          },
        );

        if (!isSuccessResponse(resp.statusCode)) {
          throw new Error(resp.message || 'Failed to initialize payment');
        }

        safeRedirect(resp.data.checkoutUrl);
      } catch (error) {
        toast.error((error as Error).message);
      }
    } catch (error) {
      toast.error((error as Error).message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardContent className="p-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl">Progress</h2>
            <Badge variant="secondary" className="text-sm">
              Step {Math.min(currentStep, showingSteps.length)} of{' '}
              {showingSteps.length}
            </Badge>
          </div>
          <Progress value={progress} className="mb-4" />
          <div
            className={cn(
              'grid gap-2',
              showingSteps.length > 2 ? 'grid-cols-4' : 'grid-cols-2',
            )}
          >
            {showingSteps.map((step, idx) => (
              <div
                key={step.id}
                className={cn(
                  'rounded-lg border p-2 text-center transition-all duration-200',
                  idx + 1 === currentStep
                    ? 'border-2 border-primary bg-transparent'
                    : idx + 1 < currentStep
                      ? 'border-primary bg-[#00d4aa]/10'
                      : 'border-gray-400 bg-transparent',
                )}
              >
                <div
                  className={cn(
                    'text-xs font-medium',
                    idx + 1 === currentStep
                      ? 'text-primary'
                      : idx + 1 < currentStep
                        ? 'text-primary'
                        : 'text-gray-400',
                  )}
                >
                  {step.title}
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {renderStep()}
    </div>
  );
};

export default BuyProduct;
