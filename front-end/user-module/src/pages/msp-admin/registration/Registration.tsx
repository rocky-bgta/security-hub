import { useState } from 'react';
import OnboardingLayout from './RegistrationLayout';
import EmailCaptureStep from './EmailCaptureStep';
import PasswordSetupStep from './PasswordSetupStep';
import MFASetupStep from './MFASetupStep';
import { MSPDetailsStep, MSPDetailsData } from './MSPDetailsStep';
import { BillingInfoStep, BillingInfoData } from './BillingInfoStep';
import {
  ProductSelectionStep,
  ProductSelectionData,
} from './ProductSelectionStep';
import { PaymentOptionsStep, PaymentData } from './PaymentOptionsStep';
import { FinalConfirmationStep } from './FinalConfirmationStep';

const Registration = ({ userType = 'msp' }: { userType: 'msp' | 'admin' }) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [mspDetails, setMspDetails] = useState<MSPDetailsData | null>(null);
  const [billingInfo, setBillingInfo] = useState<BillingInfoData | null>(null);
  const [productData, setProductData] = useState<ProductSelectionData | null>(
    null,
  );
  const [paymentData, setPaymentData] = useState<PaymentData | null>(null);

  const steps = [
    'Email Verification',
    'Set Password',
    'MFA Setup',
    'MSP Details',
    'Billing Info',
    'Product Selection',
    'Payment',
    'Confirmation',
  ];

  const stepTitles = {
    0: 'Email Verification',
    1: 'Create Password',
    2: 'Multi-Factor Authentication',
    3: 'MSP Organization Details',
    4: 'Billing Information',
    5: 'Product Selection',
    6: 'Payment Options',
    7: 'Final Confirmation',
  };

  const stepSubtitles = {
    0: 'Verify your email to continue',
    1: 'Create a secure password for your account',
    2: 'Set up multi-factor authentication',
    3: 'Enter your organization information',
    4: 'Provide billing information',
    5: 'Select products and packages',
    6: 'Choose payment method',
    7: 'Review and confirm all details',
  };

  const handleNext = (data?: any) => {
    if (currentStep === 0 && userType === 'msp') {
      setEmail(data);
    } else if (currentStep === 1 && userType === 'msp') {
      setPassword(data);
    } else if (
      (currentStep === 3 && userType === 'msp') ||
      (currentStep === 0 && userType === 'admin')
    ) {
      setMspDetails(data);
    } else if (
      (currentStep === 4 && userType === 'msp') ||
      (currentStep === 1 && userType === 'admin')
    ) {
      setBillingInfo(data);
    } else if (
      (currentStep === 5 && userType === 'msp') ||
      (currentStep === 2 && userType === 'admin')
    ) {
      setProductData(data);
    } else if (
      (currentStep === 6 && userType === 'msp') ||
      (currentStep === 3 && userType === 'admin')
    ) {
      setPaymentData(data);
    }

    setCurrentStep(prev => prev + 1);
  };

  const handleSkip = () => {
    setCurrentStep(prev => prev + 1);
  };

  const handleComplete = () => {
    // Handle completion - could redirect to dashboard or show success message
    console.log('Onboarding completed successfully!');
  };

  const handlePrevious = () => {
    setCurrentStep(prev => Math.max(0, prev - 1));
  };

  const renderStep = () => {
    switch (currentStep) {
      case 0:
        return <EmailCaptureStep onNext={handleNext} />;
      case 1:
        return <PasswordSetupStep onNext={handleNext} email={email} />;
      case 2:
        return <MFASetupStep onNext={handleNext} userType={userType} />;
      case 3:
        return <MSPDetailsStep onNext={handleNext} userType={userType} />;
      case 4:
        return <BillingInfoStep onNext={handleNext} />;
      case 5:
        return <ProductSelectionStep onNext={handleNext} onSkip={handleSkip} />;
      case 6:
        return (
          <PaymentOptionsStep
            onNext={handleNext}
            onPayLater={handleSkip}
            productData={productData as ProductSelectionData}
          />
        );
      case 7:
        return (
          <FinalConfirmationStep
            onComplete={handleComplete}
            mspDetails={mspDetails as MSPDetailsData}
            billingInfo={billingInfo as BillingInfoData}
            productData={productData as ProductSelectionData}
            paymentData={paymentData as PaymentData}
          />
        );
      default:
        return <div>Step not found</div>;
    }
  };

  return (
    <OnboardingLayout
      title={stepTitles[currentStep as keyof typeof stepTitles] || 'Onboarding'}
      subtitle={stepSubtitles[currentStep as keyof typeof stepSubtitles]}
      currentStep={currentStep}
      totalSteps={steps.length}
      steps={steps}
      userType={userType}
    >
      {renderStep()}
    </OnboardingLayout>
  );
};

export default Registration;
