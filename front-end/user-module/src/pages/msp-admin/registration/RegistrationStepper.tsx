import { Check } from 'lucide-react';
import { cn } from 'utils/Helper';

interface StepperProps {
  steps: string[];
  currentStep: number;
  className?: string;
}

const OnboardingStepper = ({ steps, currentStep, className }: StepperProps) => {
  return (
    <div className={cn('w-full py-5', className)}>
      <div className="flex items-center justify-between">
        {steps.map((step, index) => (
          <div key={step} className="flex items-center">
            <div className="flex flex-col items-center">
              <div
                className={cn(
                  'flex h-12 w-12 items-center justify-center rounded-full text-sm font-medium transition-all duration-300',
                  index < currentStep
                    ? 'bg-primary text-white shadow-primary'
                    : index === currentStep
                      ? 'bg-primary text-white shadow-primary ring-4 ring-primary/20'
                      : 'border border-card-border bg-transparent text-muted-foreground',
                )}
              >
                {index < currentStep ? (
                  <Check className="size-5" />
                ) : (
                  <span>{index + 1}</span>
                )}
              </div>
              <span
                className={cn(
                  'mt-2 text-sm font-medium transition-colors duration-300',
                  index <= currentStep
                    ? 'text-foreground'
                    : 'text-muted-foreground',
                )}
              >
                {step}
              </span>
            </div>
            {index < steps.length - 1 && (
              <div
                className={cn(
                  'mx-4 h-0.5 flex-1 transition-colors duration-300',
                  index < currentStep ? 'bg-primary' : 'bg-card-border',
                )}
              />
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default OnboardingStepper;
