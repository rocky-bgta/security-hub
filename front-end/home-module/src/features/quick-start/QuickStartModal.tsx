import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Boxes, CheckCircle2, Circle, Palette, UserPlus } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { cn } from 'utils/Helper';
import AddUser from 'features/quick-start/AddUser';
import AssignSubPackage from 'features/quick-start/assign/AssignSubPackage';
import Personalization from 'features/quick-start/Personalization';
import Loader from 'common/loader/Loader';
import CertificateTemplate, {
  ICertificateTemplateHandle,
} from 'features/quick-start/certificate/CertificateTemplate';
import { ClientProductTag } from 'models/Global';

interface IProps {
  isOpen: boolean;
  data: {
    clientType: string;
    hasBranding: boolean;
    hasUser: boolean;
    productAssigned: boolean;
    hasCertificateTemplate: boolean;
    productTags: Array<ClientProductTag>;
  } | null;
  onClose: () => void;
  onFinish: () => void;
}

const allSteps = [
  {
    id: 0,
    title: 'Personalization',
    icon: Palette,
    status: 'complete',
    clientType: ['TRIAL', 'NON_TRIAL'],
    productTags: ['Security', 'Phishing'] as Array<ClientProductTag>,
  },
  {
    id: 1,
    title: 'Add User',
    icon: UserPlus,
    status: 'incomplete',
    clientType: ['TRIAL'],
    productTags: ['Security'] as Array<ClientProductTag>,
  },
  {
    id: 2,
    title: 'Assign Product',
    icon: Boxes,
    status: 'incomplete',
    clientType: ['TRIAL'],
    productTags: ['Security'] as Array<ClientProductTag>,
  },
  {
    id: 1,
    title: 'Choose Certificate Template',
    icon: Boxes,
    status: 'incomplete',
    clientType: ['NON_TRIAL'],
    productTags: ['Security'] as Array<ClientProductTag>,
  },
];

const QuickStartModal = ({ isOpen, data, onClose, onFinish }: IProps) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [currentStep, setCurrentStep] = useState<number>(0);
  const [steps, setSteps] = useState<typeof allSteps>([]);
  const certificateTemplateRef = useRef<ICertificateTemplateHandle>(null);

  useEffect(() => {
    const initializeSteps = () => {
      if (!data) return;

      let updatedSteps;
      if (data.clientType === 'TRIAL') {
        updatedSteps = allSteps.filter(step =>
          step.clientType.includes('TRIAL'),
        );
        updatedSteps[0].status = data.hasBranding ? 'complete' : 'incomplete';
        updatedSteps[1].status = data.hasUser ? 'complete' : 'incomplete';
        updatedSteps[2].status = data.productAssigned
          ? 'complete'
          : 'incomplete';
      } else {
        const productTags = data.productTags ?? [];
        updatedSteps = allSteps.filter(
          step =>
            step.clientType.includes('NON_TRIAL') &&
            (productTags.length === 0 ||
              step.productTags.some(tag =>
                productTags.includes(tag as ClientProductTag),
              )),
        );
        updatedSteps[0].status = data.hasBranding ? 'complete' : 'incomplete';
        if (updatedSteps.length > 1) {
          updatedSteps[1].status = data.hasCertificateTemplate
            ? 'complete'
            : 'incomplete';
        }
      }

      const incompleteStepIndex = updatedSteps.findIndex(
        step => step.status === 'incomplete',
      );
      setCurrentStep(
        incompleteStepIndex !== -1
          ? incompleteStepIndex
          : updatedSteps.length - 1,
      );
      setSteps(updatedSteps);
      setLoading(false);
    };

    initializeSteps();
  }, [data]);

  const handleClickSkip = () => {
    onClose();
  };

  const handleClickNext = async () => {
    if (currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1);
      return;
    }

    // Last step — for non-trial clients, require an assigned certificate template
    if (
      data?.clientType !== 'TRIAL' &&
      data?.productTags.includes(ClientProductTag.SECURITY)
    ) {
      const isAssigned = await certificateTemplateRef?.current?.setTemplate();
      if (!isAssigned) return;
    }

    onFinish?.();
  };

  const updateStepsStatus = (
    stepId: number,
    status: 'complete' | 'incomplete',
  ) => {
    setSteps(prevSteps =>
      prevSteps.map(step =>
        step.id === stepId ? { ...step, status: status } : step,
      ),
    );

    // Advance to the next step only — never auto-finish/reload from a step save
    if (status === 'complete' && currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1);
    }
  };

  if (loading) {
    return <Loader />;
  }

  return (
    <Dialog
      open={isOpen}
      onOpenChange={open => {
        if (!open) onClose();
      }}
    >
      <DialogContent
        className="!home-flex home-h-[90%] !home-w-11/12 home-flex-col home-overflow-auto"
        shouldPreventClose={true}
      >
        <DialogHeader>
          <DialogTitle>Quick Start</DialogTitle>
          <DialogDescription>
            Update your quick start settings
          </DialogDescription>
        </DialogHeader>
        <div className="home-flex home-h-full home-flex-col home-justify-between home-gap-y-10">
          <div className="home-flex home-gap-x-20">
            <div className="home-flex home-w-4/12 home-flex-col home-gap-y-2">
              {steps.map((step, index) => (
                <div
                  key={step.id + step.title}
                  className={cn(
                    'home-group home-relative home-flex home-cursor-pointer home-items-center home-gap-x-4 home-rounded-xl home-p-4 home-border',
                    currentStep === step.id
                      ? 'home-bg-primary/20 home-border-primary'
                      : 'home-border home-border-card-border hover:home-border-primary/50 hover:home-bg-muted/50',
                  )}
                  onClick={() => setCurrentStep(step.id)}
                >
                  <div
                    className={cn(
                      'home-flex home-size-10 home-shrink-0 home-items-center home-justify-center home-rounded-full home-transition-colors home-duration-200',
                      currentStep === step.id
                        ? 'home-bg-primary home-text-primary-foreground'
                        : step.status === 'complete'
                          ? 'home-bg-primary/20 home-text-primary'
                          : 'home-bg-muted home-text-muted-foreground',
                    )}
                  >
                    <step.icon className="home-size-5" />
                  </div>

                  <div className="home-flex home-flex-1 home-flex-col home-gap-y-0.5">
                    <span className="home-text-xs home-text-muted-foreground">
                      Step {index + 1}
                    </span>
                    <p
                      className={cn(
                        'home-text-sm home-font-medium home-transition-colors',
                        currentStep === step.id
                          ? 'home-text-foreground'
                          : 'home-text-muted-foreground group-hover:home-text-foreground',
                      )}
                    >
                      {step.title}
                    </p>
                  </div>

                  <div className="home-shrink-0">
                    {step.status === 'complete' ? (
                      <CheckCircle2 className="home-size-6 home-text-primary" />
                    ) : (
                      <Circle
                        className={cn(
                          'home-size-6',
                          currentStep === step.id
                            ? 'home-text-primary'
                            : 'home-text-muted-foreground/50',
                        )}
                      />
                    )}
                  </div>
                </div>
              ))}
            </div>
            <div className="home-flex home-w-8/12 home-flex-col">
              {currentStep === 0 ? (
                <Personalization
                  stepId={steps[0].id}
                  updateStepsStatus={updateStepsStatus}
                />
              ) : currentStep === 1 ? (
                data?.clientType === 'TRIAL' ? (
                  <AddUser
                    stepId={steps[1].id}
                    updateStepsStatus={updateStepsStatus}
                  />
                ) : (
                  <CertificateTemplate
                    ref={certificateTemplateRef}
                    stepId={steps[1].id}
                    updateStepsStatus={updateStepsStatus}
                  />
                )
              ) : currentStep === 2 ? (
                <AssignSubPackage
                  stepId={steps[2].id}
                  updateStepsStatus={updateStepsStatus}
                />
              ) : null}
            </div>
          </div>

          <DialogFooter className="home-flex home-w-full home-items-end !home-justify-between home-pb-4">
            <div>
              <p className="home-mb-2 home-text-xs home-text-muted-foreground">
                Complete these setup steps now or later. Organization branding,
                user administration, and product assignments can be managed at
                any time from the Admin Console.
              </p>
              <Button variant="outline" size="lg" onClick={handleClickSkip}>
                Skip
              </Button>
            </div>
            <div className="home-space-x-4">
              {currentStep > 0 && (
                <Button
                  variant="outline"
                  size="lg"
                  onClick={() => setCurrentStep(currentStep - 1)}
                >
                  Back
                </Button>
              )}
              <Button onClick={handleClickNext} size="lg">
                {currentStep < steps.length - 1 ? 'Next' : 'Complete Setup'}
              </Button>
            </div>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default QuickStartModal;
