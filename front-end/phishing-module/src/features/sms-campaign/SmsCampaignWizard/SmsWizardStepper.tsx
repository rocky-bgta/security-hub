import { CheckIcon } from 'lucide-react';
import { CampaignType } from 'models/Campaign';
import { SMS_WIZARD_STEPS } from 'models/SmsCampaign';
import { cn } from 'utils/Helper';

interface SmsWizardStepperProps {
  currentStep: number;
  completedStep: number;
  campaignType: CampaignType;
  onStepClick: (step: number) => void;
}

export const SmsWizardStepper = ({
  currentStep,
  completedStep,
  campaignType,
  onStepClick,
}: SmsWizardStepperProps) => {
  const visibleSteps = SMS_WIZARD_STEPS.filter(
    step =>
      !(step.step === 7 && campaignType === CampaignType.SMISHING_SIMULATION),
  );

  return (
    <nav className="my-6">
      <ol className="flex items-center justify-between">
        {visibleSteps.map((step, index) => {
          const displayStepNumber = index + 1;
          const isCompleted = step.step <= completedStep;
          const isCurrent = step.step === currentStep;
          const isClickable = step.step <= completedStep + 1;

          return (
            <li
              key={step.step}
              className="relative flex w-full items-center justify-center"
            >
              {index < visibleSteps.length - 1 && (
                <div
                  className={cn(
                    'absolute left-1/2 top-4 h-0.5 w-full flex-1 bg-card-border',
                    step.step < completedStep ? 'bg-primary' : '',
                  )}
                />
              )}

              <button
                onClick={() => isClickable && onStepClick(step.step)}
                disabled={!isClickable}
                className={cn(
                  'group relative flex flex-col items-center',
                  isClickable ? 'cursor-pointer' : 'cursor-not-allowed',
                )}
              >
                <span
                  className={cn(
                    'z-10 flex size-8 items-center justify-center rounded-full text-sm font-medium text-primary-foreground transition-colors',
                    isCompleted
                      ? 'bg-primary'
                      : isCurrent
                        ? 'bg-primary ring-4 ring-primary/20'
                        : 'bg-card-border text-muted-foreground',
                    isClickable && !isCompleted && !isCurrent
                      ? 'group-hover:bg-primary/10'
                      : '',
                  )}
                >
                  {isCompleted && step.step !== currentStep ? (
                    <CheckIcon className="size-4" />
                  ) : (
                    displayStepNumber
                  )}
                </span>

                <span
                  className={cn(
                    'mt-2 text-xs font-medium',
                    isCurrent
                      ? 'text-primary'
                      : isCompleted
                        ? 'text-foreground'
                        : 'text-muted-foreground',
                  )}
                >
                  {step.name}
                </span>
              </button>
            </li>
          );
        })}
      </ol>
    </nav>
  );
};
