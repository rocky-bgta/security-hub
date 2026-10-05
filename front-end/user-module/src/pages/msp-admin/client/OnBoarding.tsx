import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Card, CardContent } from 'common/Card';
import { Progress } from 'common/Progress';
import { useAPI } from 'hooks/UseAPI';
import { useAuth } from 'hooks/UseAuth';
import { useStore } from 'hooks/UseStore';
import { FileType, useUploader } from 'hooks/UseUploader';
import {
  DiscountType,
  IClientOnboarding,
  IPaymentComment,
} from 'models/Client';
import {
  ICountry,
  IDropdownData,
  IIndustry,
  ILanguage,
  IOrganizationSize,
  IOrganizationType,
  IState,
  ITimeZone,
  ISubIndustry,
  ICompliance,
} from 'models/Dropdown';
import { IResponse } from 'models/Global';
import { useNavigate } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { cn, isSuccessResponse } from 'utils/Helper';
import Step5 from 'features/msp-admin/client/onboarding/Step5';
import Step4 from 'features/msp-admin/client/onboarding/Step4';
import Step3 from 'features/msp-admin/client/onboarding/Step3';
import Step2 from 'features/msp-admin/client/onboarding/Step2';
import Step1 from 'features/msp-admin/client/onboarding/Step1';

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
  { id: 5, title: 'Confirmation', description: 'Send confirmation email' },
];

const MspClientOnBoarding = () => {
  const navigate = useNavigate();
  const { userInfo } = useStore();

  const { role } = useAuth();
  const [loading, setLoading] = useState<boolean>(false);
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
      domain: '',
      organizationSize: '',
      adminEmail: '',
      streetAddress: '',
      streetAddressLine2: '',
      city: '',
      zipPostalCode: '',
      organizationLogo: null,
      logoUrl: '',
      subIndustryId: '',
      subIndustryName: '',
      complianceId: '',
      complianceName: '',
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
    mspId: userInfo?.userId ?? '',
    mspName: userInfo?.fullName ?? '',
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

  const [paymentComment, setPaymentComment] = useState<IPaymentComment>({
    invoiceId: '',
    comment: '',
    actionTakenId: '',
    nextStepId: '',
    userName: userInfo?.email ?? '',
    userRole: role ?? '',
    approved: false,
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
  const [phishingProductSelected, setPhishingProductSelected] =
    useState<boolean>(false);
  const [emailSent, setEmailSent] = useState<boolean>(false);
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
    fetchDropdownData();
  }, [apiClient]);

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
    mspName?: string,
  ) => {
    if (section === 'mspId') {
      setFormData(prev => ({
        ...prev,
        mspId: data as string,
        mspName: mspName ?? '',
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [section]: data,
      }));
    }
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
        return (
          <Step4
            data={formData.invoice}
            formData={formData}
            onUpdate={(data, paymentComment) => {
              updateFormData('invoice', data);
              setPaymentComment(paymentComment);
            }}
            paymentComment={paymentComment}
            onNext={handleNext}
            onPrevious={handlePrevious}
          />
        );
      case 5:
        return (
          <Step5
            emailSent={emailSent}
            submitting={loading}
            onSubmit={handleSubmit}
            onPrevious={handlePrevious}
          />
        );
      default:
        return null;
    }
  };

  const handleSubmit = async () => {
    setCurrentStep(currentStep + 1);
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

    if (formData.invoice?.paymentStatus === 'COMPLETED') {
      if (
        formData.invoice?.completedPayment?.paymentMethod === 'BANK_TRANSFER'
      ) {
        const { url, error } = await uploadFile(
          formData.invoice?.completedPayment?.bankTransferDetails
            ?.transactionReceiptFile as unknown as File,
          FileType.CONTENT,
        );
        if (error) {
          toast.error(error);
          return { success: false };
        }
        formData.invoice.completedPayment!.bankTransferDetails!.transactionReceiptUrl =
          url;
      }
      delete formData.invoice?.completedPayment?.bankTransferDetails
        ?.transactionReceiptFile;

      if (
        formData.invoice?.completedPayment?.paymentMethod === 'CHECK_PAYMENT'
      ) {
        const { url, error } = await uploadFile(
          formData.invoice?.completedPayment?.checkPaymentDetails
            ?.checkImageFile as unknown as File,
          FileType.CONTENT,
        );
        if (error) {
          toast.error(error);
          return { success: false };
        }
        formData.invoice.completedPayment!.checkPaymentDetails!.checkImageUrl =
          url;
      }
      delete formData.invoice?.completedPayment?.checkPaymentDetails
        ?.checkImageFile;
    }

    const payload = {
      organization: {
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
        complianceId: formData.organization.complianceId,
        complianceName: formData.organization.complianceName,
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
      mspId: formData.mspId,
      mspName: formData.mspName,
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
        subtotal: formData.invoice.subtotal,
        discountType: formData.invoice.discountType,
        discountPercentage: formData.invoice.discountPercentage,
        discountAmount: formData.invoice.discountAmount,
        couponCode: formData.invoice.couponCode,
        vatRate: formData.invoice.vatRate,
        vatAmount: formData.invoice.vatAmount,
        totalAmount: formData.invoice.totalAmount,
        paymentStatus: formData.invoice.paymentStatus,
        completedPayment:
          formData.invoice?.paymentStatus === 'COMPLETED'
            ? {
                paymentMethod: formData.invoice.completedPayment?.paymentMethod,
                commentLog: {
                  comment: paymentComment.comment,
                  actionTakenId: paymentComment.actionTakenId,
                  nextStepId: paymentComment.nextStepId,
                },
                ...(formData.invoice.completedPayment?.paymentMethod ===
                  'BANK_TRANSFER' && {
                  bankTransferDetails: {
                    bankName:
                      formData.invoice.completedPayment?.bankTransferDetails
                        ?.bankName,
                    accountNumber:
                      formData.invoice.completedPayment?.bankTransferDetails
                        ?.accountNumber,
                    bankBranchName:
                      formData.invoice.completedPayment?.bankTransferDetails
                        ?.bankBranchName,
                    transactionNumber:
                      formData.invoice.completedPayment?.bankTransferDetails
                        ?.transactionNumber,
                    paymentDate:
                      formData.invoice.completedPayment?.bankTransferDetails
                        ?.paymentDate,
                    paymentAmount:
                      formData.invoice.completedPayment?.bankTransferDetails
                        ?.paymentAmount,
                    transactionReceiptUrl:
                      formData.invoice.completedPayment?.bankTransferDetails
                        ?.transactionReceiptUrl,
                  },
                }),
                ...(formData.invoice.completedPayment?.paymentMethod ===
                  'CHECK_PAYMENT' && {
                  checkPaymentDetails: {
                    checkNumber:
                      formData.invoice.completedPayment?.checkPaymentDetails
                        ?.checkNumber,
                    bankName:
                      formData.invoice.completedPayment?.checkPaymentDetails
                        ?.bankName,
                    branchName:
                      formData.invoice.completedPayment?.checkPaymentDetails
                        ?.branchName,
                    paymentDate:
                      formData.invoice.completedPayment?.checkPaymentDetails
                        ?.paymentDate,
                    paymentAmount:
                      formData.invoice.completedPayment?.checkPaymentDetails
                        ?.paymentAmount,
                    checkImageUrl:
                      formData.invoice.completedPayment?.checkPaymentDetails
                        ?.checkImageUrl,
                  },
                }),
              }
            : null,
      },
    };

    try {
      const response = await apiClient.post(
        API_END_POINTS.CREATE_CLIENT_ADMIN,
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

      setEmailSent(true);
      toast.success(
        'Email sent successfully to the Client Admin with login credentials.',
      );
      navigate(routes.clientList.path);
    } catch (error: unknown) {
      console.error('Error creating client admin:', error);
      toast.error(
        error instanceof Error ? error.message : 'Error creating client admin',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2>MSP&apos;s Client Onboarding</h2>
        <p className="text-muted-foreground">
          Streamlined process to onboard new clients for the MSP
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
          <div className="grid grid-cols-5 gap-2">
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

export default MspClientOnBoarding;
