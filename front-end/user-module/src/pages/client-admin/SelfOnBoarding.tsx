import { safeRedirect } from 'home-module/security';
import { useEffect, useState } from 'react';
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
import { IResponse, ValidityUnit } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn, isSuccessResponse } from 'utils/Helper';
import { useStore } from 'hooks/UseStore';
import { IClientAdminDetails } from './Edit';
import Loader from 'common/loader/Loader';

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

const SelfOnBoarding = () => {
  const { userInfo } = useStore();
  const [loading, setLoading] = useState<boolean>(true);
  const [currentStep, setCurrentStep] = useState<number>(1);
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
  const [formSubmitting, setFormSubmitting] = useState<boolean>(false);

  const [phishingProductSelected, setPhishingProductSelected] =
    useState<boolean>(false);
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
  const [pendingInvoice, setPendingInvoice] = useState<{
    id: string;
    couponCode: string;
    couponDiscountAmount: number;
    couponId: string;
    subtotal: number;
    totalAmount: number;
    vatAmount: number;
    discountAmount: number;
    discountPercentage: number;
    discountType: DiscountType;
  }>({
    id: '',
    couponCode: '',
    couponDiscountAmount: 0,
    couponId: '',
    subtotal: 0,
    totalAmount: 0,
    vatAmount: 0,
    discountAmount: 0,
    discountPercentage: 0,
    discountType: DiscountType.PERCENTAGE,
  });

  const apiClient = useAPI();
  const { uploadFile } = useUploader();

  useEffect(() => {
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

        const countries = countryRes.data.map(item => ({
          id: item.id,
          name: item.name,
          code: item.code,
          phoneCode: item.phoneCode,
        }));

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

        return countries;
      } catch (error) {
        console.error('Error fetching dropdown data:', error);
        return [] as Array<ICountry>;
      }
    };

    const fetchClientAdminData = async (countries: Array<ICountry>) => {
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

        const organizationCountryId =
          countries.find(item => item.phoneCode === response.data.phoneCode)
            ?.id ?? response.data.country?.id;

        setFormData(prev => ({
          ...prev,
          organization: {
            organizationName: response.data.organizationName ?? '',
            organizationType: response.data.organizationType?.id ?? '',
            contactEmail: response.data.contactEmail ?? '',
            phoneNumber: response.data.phoneNumber ?? '',
            phoneCode: response.data.phoneCode ?? '',
            country: organizationCountryId,
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
            stateProvince: (response.data as any).billingStateProvince ?? '',
            country: (response.data as any).billingCountry ?? '',
            zipPostalCode: response.data.billingZipPostalCode ?? '',
          },
          mspId: response.data.mspId ?? '',
          mspName: response.data.mspAdminEmail ?? '',
          productSelections:
            response.data.clientProducts?.map(p => ({
              productId: p.productId,
              packageId: p.packageId,
              packageName: p.packageDetails.packageName,
              productName: p.product.productName,
              licenseCount: p.licenseCount ?? 0,
              pricePerLicense: p.pricePerLicense ?? 0,
              validityPeriod: p.validityPeriod ?? 1,
              validityUnit: p.validityUnit ?? ValidityUnit.YEAR,
              userRangeId: p.packageDetails.userRangeId ?? '',
            })) ?? [],
        }));
      } catch (error) {
        console.error('Error fetching client admin data:', error);
      }
    };

    const fetchInvoiceData = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.INVOICE_LIST +
            '&clientId=' +
            userInfo?.userId +
            '&status=ON_PROGRESS&status=PENDING',
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }

        if (response.data.total > 0) {
          const invoiceData = response.data.items[0];

          setPendingInvoice({
            id: invoiceData?.id ?? '',
            couponCode: invoiceData?.couponCode ?? '',
            couponDiscountAmount: invoiceData?.couponDiscountAmount ?? 0,
            couponId: invoiceData?.couponId ?? '',
            subtotal: invoiceData?.subtotal ?? 0,
            totalAmount: invoiceData?.totalAmount ?? 0,
            vatAmount: invoiceData?.vatAmount ?? 0,
            discountAmount: invoiceData?.discountAmount ?? 0,
            discountPercentage: invoiceData?.discountPercentage ?? 0,
            discountType: invoiceData?.discountType ?? DiscountType.PERCENTAGE,
          });

          setFormData(prev => ({
            ...prev,
            productSelections: [
              ...invoiceData.productSelections.map((p: any) => ({
                productId: p.productId,
                packageId: p.packageId,
                packageName: p.packageName,
                productName: p.productName,
                licenseCount: p.licenseCount ?? 0,
                pricePerLicense: p.pricePerLicense ?? 0,
                validityPeriod: p.validityPeriod ?? 1,
                validityUnit: p.validityUnit ?? ValidityUnit.YEAR,
                userRangeId: p.userRangeId ?? '',
              })),
            ],
            invoice: {
              ...prev.invoice,
              couponCode: invoiceData?.couponCode ?? '',
              couponDiscountAmount: invoiceData?.couponDiscountAmount ?? 0,
              subtotal: invoiceData?.subtotal ?? 0,
              totalAmount: invoiceData?.totalAmount ?? 0,
              vatRate: invoiceData?.vatPercentage ?? invoiceData?.vatRate ?? 0,
              vatAmount: invoiceData?.vatAmount ?? 0,
              discountAmount: invoiceData?.discountAmount ?? 0,
              discountPercentage: invoiceData?.discountPercentage ?? 0,
              discountType:
                invoiceData?.discountType ?? DiscountType.PERCENTAGE,
            },
          }));
        }
      } catch (error) {
        console.error((error as Error).message);
      }
    };

    const initialize = async () => {
      const countries = await fetchDropdownData();
      await fetchClientAdminData(countries);

      if (userInfo.onboardBy === 'BUY_NOW' && userInfo.pendingPayment) {
        await fetchInvoiceData();
        setCurrentStep(4);
      }

      setLoading(false);
    };

    initialize();
  }, [apiClient, userInfo]);

  const progress = (currentStep / steps.length) * 100;

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

  const updateFormData = (
    section: keyof IClientOnboarding,
    data: IClientOnboarding[keyof IClientOnboarding],
  ) => {
    setFormData(prev => ({
      ...prev,
      [section]: data,
    }));
  };

  const renderStep = () => {
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
            onUpdate={(data, hasPhishingProduct) => {
              updateFormData('productSelections', data);
              setPhishingProductSelected(hasPhishingProduct);
            }}
            onNext={handleNext}
            onPrevious={handlePrevious}
          />
        );
      case 4:
      case 5:
        return (
          <Step4
            data={formData.invoice}
            formData={formData}
            couponId={pendingInvoice.couponId}
            loading={loading || formSubmitting}
            pendingPayment={userInfo.pendingPayment && pendingInvoice.id !== ''}
            onUpdate={data => updateFormData('invoice', data)}
            onNext={handleSubmit}
            onPrevious={handlePrevious}
          />
        );
      default:
        return null;
    }
  };

  const handleSubmit = async (invoiceOverride?: IInvoice) => {
    const invoice = invoiceOverride ?? formData.invoice;

    setCurrentStep(Math.min(currentStep + 1, 5));
    setFormSubmitting(true);

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
        phoneCode:
          formData.organization.phoneCode[0] === '+'
            ? formData.organization.phoneCode
            : '+' + formData.organization.phoneCode,
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
      let invoiceId = pendingInvoice.id,
        amount = pendingInvoice.totalAmount;
      if (pendingInvoice.id.trim() === '' || !userInfo.pendingPayment) {
        const response = await apiClient.post(
          API_END_POINTS.ONBOARD_CLIENT_ADMIN,
          {
            data: payload,
          },
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }

        if (phishingProductSelected) {
          const configResults = await Promise.allSettled([
            apiClient.post(API_END_POINTS.CREATE_INSECURE_WEB_CONFIG, {
              data: {
                collectBreachData: true,
                monitoredDomains: [payload.organization.domain],
                autoNotifyUsers: true,
                requirePasswordReset: true,
                syncIntervalHours: 6,
              },
            }),
            apiClient.post(API_END_POINTS.CREATE_SHODAN_CONFIG, {
              data: {
                subjectType: 'IP',
                subject: 'IP',
                notes: 'IP monitoring for ' + payload.organization.domain,
              },
            }),
          ]);

          configResults.forEach((result, index) => {
            if (result.status === 'rejected') {
              const configName =
                index === 0
                  ? 'CREATE_INSECURE_WEB_CONFIG'
                  : 'CREATE_SHODAN_CONFIG';
              console.error(`Failed to create ${configName}:`, result.reason);
            }
          });
        }

        toast.success(
          'Client admin onboarded successfully. Redirecting to payment...',
        );

        invoiceId = response.data.invoiceResponse.id;
        amount = response.data.invoiceResponse.totalAmount;
      }

      try {
        const resp = await apiClient.post(
          API_END_POINTS.INITIATE_ONLINE_PAYMENT,
          {
            data: {
              invoiceId: invoiceId,
              amount: amount,
              currency: 'USD',
              date: new Date().toISOString(),
              couponId: pendingInvoice.couponId || '',
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
          throw new Error(resp.message);
        }

        safeRedirect(resp.data.checkoutUrl);
      } catch (error: unknown) {
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to initialize payment',
        );
      }
    } catch (error: unknown) {
      toast.error(
        error instanceof Error
          ? error.message
          : 'Failed to onboard client admin',
      );

      setFormSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2>Onboarding</h2>
        <p className="text-muted-foreground">
          Streamlined process to onboard new administrators
        </p>
      </div>

      <Card>
        <CardContent className="p-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl">Progress</h2>
            <Badge variant="secondary" className="text-sm">
              Step {Math.min(currentStep, steps.length)} of {steps.length}
            </Badge>
          </div>
          <Progress value={progress} className="mb-4" />
          <div className="grid grid-cols-4 gap-2">
            {steps.map(step => (
              <div
                key={step.id}
                className={cn(
                  'rounded-lg border p-2 text-center transition-all duration-200',
                  step.id === currentStep
                    ? 'border-2 border-primary bg-transparent'
                    : step.id < currentStep
                      ? 'border-primary bg-[#00d4aa]/10'
                      : 'border-gray-400 bg-transparent',
                )}
              >
                <div
                  className={cn(
                    'text-xs font-medium',
                    step.id === currentStep
                      ? 'text-primary'
                      : step.id < currentStep
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

export default SelfOnBoarding;
