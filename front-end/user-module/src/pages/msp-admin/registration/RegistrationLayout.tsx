import { Card, CardContent, CardHeader } from 'common/Card';
import React from 'react';
import OnboardingStepper from './RegistrationStepper';

interface OnboardingLayoutProps {
  children: React.ReactNode;
  title: string;
  subtitle?: string;
  currentStep: number;
  totalSteps: number;
  steps: string[];
  userType: 'msp' | 'admin';
}

const OnboardingLayout = ({
  children,
  title,
  subtitle,
  currentStep,
  steps,
  userType,
}: OnboardingLayoutProps) => {
  return (
    <div className="min-h-screen">
      <div>
        {/* Header */}
        <div className="mb-8 text-center">
          <h1 className="mb-2 text-3xl font-bold text-foreground">
            {userType === 'admin'
              ? 'MSP Admin Onboarding'
              : 'Welcome to Aspire ASAT'}
          </h1>
          <p className="text-muted-foreground">
            {userType === 'admin'
              ? 'Complete the MSP registration process'
              : "Let's get your MSP account set up"}
          </p>
        </div>

        {/* Stepper */}
        <div className="mb-8">
          <OnboardingStepper steps={steps} currentStep={currentStep} />
        </div>

        {/* Main Content */}
        <div className="mx-auto max-w-5xl">
          <Card>
            <CardHeader className="pb-6 text-center">
              <h2 className="text-2xl font-semibold text-card-foreground">
                {title}
              </h2>
              {subtitle && (
                <p className="mt-2 text-muted-foreground">{subtitle}</p>
              )}
            </CardHeader>
            <CardContent className="px-8 pb-8">{children}</CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default OnboardingLayout;
