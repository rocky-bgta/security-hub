import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Card, CardContent } from 'common/Card';
import { Progress } from 'common/Progress';
import MSPOnboardingStep1 from 'features/onboarding/Step1';
import MSPOnboardingStep2 from 'features/onboarding/Step2';
import MSPOnboardingStep3 from 'features/onboarding/Step3';
import MSPOnboardingStep4 from 'features/onboarding/Step4';
import MSPOnboardingStep5 from 'features/onboarding/Step5';
import { useAPI } from 'hooks/UseAPI';
import { useAuth } from 'hooks/UseAuth';
import { useStore } from 'hooks/UseStore';
import { FileType, useUploader } from 'hooks/UseUploader';
import { DiscountType, IPaymentComment } from 'models/Client';
import {
  ICountry,
  IDropdownData,
  IIndustry,
  ILanguage,
  IOrganizationSize,
  IOrganizationType,
  IState,
  ITimeZone,
} from 'models/Dropdown';
import { IResponse, PaymentMethod } from 'models/Global';
import { IMSPOnboarding } from 'models/MSP';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn, isSuccessResponse } from 'utils/Helper';
import { routes } from 'routes/Routes';
import { useNavigate } from 'react-router-dom';

const steps = [
  {
    id: 1,
    title: 'MSP Details',
    description: 'Basic MSP information',
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
    description: 'Review invoice',
  },
  { id: 5, title: 'Confirmation', description: 'Send confirmation email' },
];

const MSPOnBoarding = () => {
  const navigate = useNavigate();
  const { userInfo } = useStore();
  const { role } = useAuth();
  const [loading, setLoading] = useState<boolean>(false);
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [formData, setFormData] = useState<IMSPOnboarding>({
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
      mspTypeId: '',
      mspAdminEmail: '',
      organizationLogo: null,
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
        paymentMethod: PaymentMethod.BANK_TRANSFER,
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
    creditInfo: {
      netDays: {
        id: '',
        name: '',
      },
      creditEndDate: '',
      enableCredit: false,
      creditAmount: 0,
      reason: '',
      netDaysId: '',
      autoSuspendOnOverdue: false,
      creditStartDate: '',
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
  const [emailSent, setEmailSent] = useState<boolean>(false);
  const [paymentComment, setPaymentComment] = useState<IPaymentComment>({
    invoiceId: '',
    comment: '',
    actionTakenId: '',
    nextStepId: '',
    userName: userInfo?.email ?? '',
    userRole: role ?? '',
    approved: false,
  });
  const apiClient = useAPI();
  const { uploadFile } = useUploader();

  useEffect(() => {
    fetchDropdownData();
  }, []);

  const progress = (currentStep / steps.length) * 100;

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
      });
    } catch (error) {
      console.error('Error fetching dropdown data:', error);
    }
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

  const updateFormData = (
    section: keyof IMSPOnboarding,
    data: IMSPOnboarding[keyof IMSPOnboarding],
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
          <MSPOnboardingStep1
            data={formData.organization}
            creditInfo={formData.creditInfo}
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
          <MSPOnboardingStep2
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
          <MSPOnboardingStep3
            data={formData.productSelections}
            onUpdate={data => updateFormData('productSelections', data)}
            onNext={handleNext}
            onPrevious={handlePrevious}
          />
        );
      case 4:
        return (
          <MSPOnboardingStep4
            data={formData.invoice}
            formData={formData}
            onUpdate={(data, comment) => {
              updateFormData('invoice', data);
              setPaymentComment(comment);
            }}
            paymentComment={paymentComment}
            onNext={handleNext}
            onPrevious={handlePrevious}
          />
        );
      case 5:
      case 6:
        return (
          <MSPOnboardingStep5
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
        formData.invoice?.completedPayment?.paymentMethod ===
        PaymentMethod.BANK_TRANSFER
      ) {
        const { url, error } = await uploadFile(
          formData.invoice?.completedPayment?.bankTransferDetails
            ?.transactionReceiptFile as unknown as File,
          FileType.CONTENT,
        );
        if (error) {
          toast.error(error);
          setLoading(false);
          return;
        }
        formData.invoice.completedPayment!.bankTransferDetails!.transactionReceiptUrl =
          url;
      }
      delete formData.invoice?.completedPayment?.bankTransferDetails
        ?.transactionReceiptFile;

      if (
        formData.invoice?.completedPayment?.paymentMethod ===
        PaymentMethod.CHECK_PAYMENT
      ) {
        const { url, error } = await uploadFile(
          formData.invoice?.completedPayment?.checkPaymentDetails
            ?.checkImageFile as unknown as File,
          FileType.CONTENT,
        );
        if (error) {
          toast.error(error);
          setLoading(false);
          return;
        }
        formData.invoice.completedPayment!.checkPaymentDetails!.checkImageUrl =
          url;
      }
      delete formData.invoice?.completedPayment?.checkPaymentDetails
        ?.checkImageFile;
    }

    const payload = {
      organization: formData.organization,
      billing: formData.billing,
      productSelections: formData.productSelections.map(product => ({
        productId: product.productId,
        packageId: product.packageId,
        userRangeId: product.userRangeId,
        licenseCount: product.licenseCount,
        pricePerLicense: product.pricePerLicense,
        validityPeriod: product.validityPeriod,
        validityUnit: product.validityUnit,
        productName: product.productName,
        packageName: product.packageName,
      })),
      invoice: {
        subtotal: formData.invoice.subtotal,
        discountType: formData.invoice.discountType,
        discountValue: formData.invoice.discountValue,
        discountPercentage: formData.invoice.discountPercentage,
        discountAmount: formData.invoice.discountAmount,
        couponCode: formData.invoice.couponCode,
        vatRate: formData.invoice.vatRate,
        vatAmount: formData.invoice.vatAmount,
        totalAmount: formData.invoice.totalAmount,
        paymentStatus: formData.invoice.paymentStatus,
        completedPayment:
          formData.invoice.paymentStatus === 'COMPLETED' &&
          formData.invoice.completedPayment?.paymentMethod
            ? {
                paymentMethod: formData.invoice.completedPayment.paymentMethod,
                commentLog: {
                  invoiceId: paymentComment.invoiceId,
                  comment: paymentComment.comment,
                  actionTakenId: paymentComment.actionTakenId,
                  nextStepId: paymentComment.nextStepId,
                  userName: paymentComment.userName,
                  userRole: paymentComment.userRole,
                  approved: paymentComment.approved,
                },
                ...(formData.invoice.completedPayment.paymentMethod ===
                  PaymentMethod.BANK_TRANSFER && {
                  bankTransferDetails: {
                    bankName:
                      formData.invoice.completedPayment.bankTransferDetails
                        ?.bankName ?? '',
                    accountNumber:
                      formData.invoice.completedPayment.bankTransferDetails
                        ?.accountNumber ?? '',
                    bankBranchName:
                      formData.invoice.completedPayment.bankTransferDetails
                        ?.bankBranchName ?? '',
                    transactionNumber:
                      formData.invoice.completedPayment.bankTransferDetails
                        ?.transactionNumber ?? '',
                    paymentDate:
                      formData.invoice.completedPayment.bankTransferDetails
                        ?.paymentDate ?? '',
                    paymentAmount:
                      formData.invoice.completedPayment.bankTransferDetails
                        ?.paymentAmount ?? 0,
                    transactionReceiptUrl:
                      formData.invoice.completedPayment.bankTransferDetails
                        ?.transactionReceiptUrl ?? '',
                  },
                }),
                ...(formData.invoice.completedPayment.paymentMethod ===
                  PaymentMethod.CHECK_PAYMENT && {
                  checkPaymentDetails: {
                    checkNumber:
                      formData.invoice.completedPayment.checkPaymentDetails
                        ?.checkNumber ?? '',
                    bankName:
                      formData.invoice.completedPayment.checkPaymentDetails
                        ?.bankName ?? '',
                    branchName:
                      formData.invoice.completedPayment.checkPaymentDetails
                        ?.branchName ?? '',
                    paymentDate:
                      formData.invoice.completedPayment.checkPaymentDetails
                        ?.paymentDate ?? '',
                    paymentAmount:
                      formData.invoice.completedPayment.checkPaymentDetails
                        ?.paymentAmount ?? 0,
                    checkImageUrl:
                      formData.invoice.completedPayment.checkPaymentDetails
                        ?.checkImageUrl ?? '',
                  },
                }),
              }
            : null,
      },
      creditInfo: formData.creditInfo,
    };

    try {
      const response = await apiClient.post(API_END_POINTS.CREATE_MSP_ADMIN, {
        data: payload,
      });

      if (!isSuccessResponse(response.statusCode)) {
        toast.error(
          response.message || 'Failed to create msp admin. Please try again.',
        );
        return;
      }

      setEmailSent(true);
      toast.success(
        'Email sent successfully to the MSP Admin with login credentials.',
      );
      navigate(routes.mspList.path);
    } catch (error) {
      console.error('Error creating msp admin:', error);
      toast.error('Failed to create msp admin. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2>MSP Admin Onboarding</h2>
        <p className="text-muted-foreground">
          Complete the onboarding process to set up your MSP Admin account with
          commission tiers and product selection.
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

export default MSPOnBoarding;
