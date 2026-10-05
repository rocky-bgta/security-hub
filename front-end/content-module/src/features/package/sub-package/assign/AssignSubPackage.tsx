import { Badge } from 'common/Badge';
import { Card, CardContent } from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Progress } from 'common/Progress';
import { useStore } from 'hooks/UseStore';
import { Status } from 'models/Global';
import { useCallback, useState } from 'react';
import { cn, isSuccessResponse } from 'utils/Helper';
import Step1 from './Step1';
import Step2 from './Step2';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from 'hooks/UseAPI';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedSubPackage: any;
  refetch: () => void;
}

interface ICompletionDays {
  durationUnit: string;
  durationValue: number;
  enableFirstUserNotificationEmail: boolean;
  secondaryEmails: string[];
  thirdLevelEmails: string[];
  fourthHREmails: string[];
}
interface IFormData {
  users: string[];
  completionDays: ICompletionDays;
  subPackageId: string;
  productId: string;
  clientAdminId: string;
  status: Status;
}

const steps = [
  { id: 1, title: 'Users Selection', description: 'Send confirmation email' },
  { id: 2, title: 'Confirmation', description: 'Send confirmation email' },
];

const AssignSubPackage = ({
  isOpen,
  onClose,
  selectedSubPackage,
  refetch,
}: IProps) => {
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IFormData>({
    subPackageId: selectedSubPackage.id,
    users: [],
    productId: selectedSubPackage.productId,
    clientAdminId: userInfo.userId,
    status: Status.ACTIVE,
    completionDays: {
      durationUnit: 'WEEKS',
      durationValue: 1,
      enableFirstUserNotificationEmail: true,
      secondaryEmails: [],
      thirdLevelEmails: [],
      fourthHREmails: [],
    },
  });
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

  const updateFormData = (section: keyof IFormData, data: any) => {
    setFormData(prev => ({
      ...prev,
      [section]: data,
    }));
  };

  const handleCompletionDaysUpdate = useCallback((data: any) => {
    updateFormData('completionDays', data);
  }, []);

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <Step1
            data={formData.users}
            onUpdate={data => updateFormData('users', data)}
            onNext={handleNext}
            onPrevious={handlePrevious}
            selectedSubPackage={selectedSubPackage}
          />
        );
      case 2:
        return (
          <Step2
            data={formData.completionDays}
            onUpdate={handleCompletionDaysUpdate}
            onSubmit={handleSubmit}
            onPrevious={handlePrevious}
            isLoading={isLoading}
            selectedSubPackage={selectedSubPackage}
          />
        );

      default:
        return null;
    }
  };

  const handleSubmit = async () => {
    setIsLoading(true);
    try {
      const validFor =
        formData.completionDays.durationUnit === 'DAYS'
          ? 1 * formData.completionDays.durationValue
          : formData.completionDays.durationUnit === 'WEEKS'
            ? 7 * formData.completionDays.durationValue
            : formData.completionDays.durationUnit === 'MONTHS'
              ? 30 * formData.completionDays.durationValue
              : 30;

      const payload = {
        subPackageData: [
          {
            userIdList: formData.users,
            clientAdminId: userInfo.userId,
            productId: selectedSubPackage.productId,
            packageId: selectedSubPackage.packageId,
            subPackageId: selectedSubPackage.id,
            status: Status.ACTIVE,
            enableFirstUserNotificationEmail:
              formData.completionDays.enableFirstUserNotificationEmail,
            secondaryEmails: [...formData.completionDays.secondaryEmails],
            thirdLevelEmails: [...formData.completionDays.thirdLevelEmails],
            fourthHREmails: [...formData.completionDays.fourthHREmails],
            completionDays: {
              durationUnit: formData.completionDays.durationUnit,
              durationValue: formData.completionDays.durationValue,
            },
            validFor: validFor,
            subPackageName: selectedSubPackage.name,
            productName: selectedSubPackage.productName,
            productPackageId: selectedSubPackage.productPackageId,
          },
        ],
      };

      const response = await apiClient.post(API_END_POINTS.ASSIGN_SUB_PACKAGE, {
        data: payload,
      });
      if (isSuccessResponse(response.statusCode)) {
        toast.success(response.message || 'Sub package assigned successfully');
        refetch();
        onClose();
      } else {
        toast.error(
          response.message || 'Failed to assign sub package',
        );
      }
    } catch (error: unknown) {
      toast.error(
        (error as Error)?.message || 'Error assigning sub package',
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <Dialog open={isOpen} onOpenChange={onClose}>
        <DialogContent className="!content-flex !content-h-4/5 !content-w-4/5 content-flex-col !content-overflow-y-auto content-text-white">
          <DialogHeader>
            <DialogTitle className="content-text-white">
              Assign Sub Package
            </DialogTitle>
            <DialogDescription>
              Select users to assign sub package
            </DialogDescription>
          </DialogHeader>

          <Card>
            <CardContent className="!content-p-4">
              <div className="content-flex content-items-center content-justify-between">
                <h2 className="content-mb-2 content-text-xl content-text-white">
                  Progress
                </h2>
                <Badge variant="secondary" className="content-text-sm">
                  Step {Math.min(currentStep, steps.length)} of {steps.length}
                </Badge>
              </div>
              <Progress value={progress} className="content-mb-4" />
              <div className="content-grid content-grid-cols-2 content-gap-2">
                {steps.map(step => (
                  <div
                    key={step.id}
                    className={cn(
                      'content-rounded-lg content-p-2 content-text-center content-transition-all content-duration-200',
                      step.id === currentStep
                        ? 'content-border-2 content-border-primary content-bg-primary/10'
                        : step.id < currentStep
                          ? 'content-border content-border-primary content-bg-primary/20'
                          : 'content-border content-border-primary content-bg-transparent',
                    )}
                  >
                    <div
                      className={cn(
                        'content-text-xs content-font-medium',
                        step.id === currentStep
                          ? 'content-text-primary'
                          : step.id < currentStep
                            ? 'content-text-primary'
                            : 'content-text-primary',
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
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default AssignSubPackage;
