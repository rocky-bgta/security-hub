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
import { useAPI } from 'hooks/UseAPI';
import { Status } from 'models/Global';
import { ISubPackageDetails } from 'models/SubPackage';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn } from 'utils/Helper';
import AspireAdminAssignSubPackageStep1 from './Step1';
import AspireAdminAssignSubPackageStep2 from './Step2';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedSubPackage: ISubPackageDetails;
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

const AspireAdminAssignSubPackage = ({
  isOpen,
  onClose,
  selectedSubPackage,
  refetch,
}: IProps) => {
  const apiClient = useAPI();
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IFormData>({
    subPackageId: selectedSubPackage.id,
    users: [],
    productId: selectedSubPackage.productId,
    clientAdminId: selectedSubPackage.clientAdminId,
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

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <AspireAdminAssignSubPackageStep1
            data={formData.users}
            onUpdate={data => updateFormData('users', data)}
            onNext={handleNext}
            onPrevious={handlePrevious}
            selectedSubPackage={selectedSubPackage}
          />
        );
      case 2:
        return (
          <AspireAdminAssignSubPackageStep2
            data={formData.completionDays}
            onUpdate={data => updateFormData('completionDays', data)}
            onSubmit={handleSubmit}
            onPrevious={handlePrevious}
            isLoading={isLoading}
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
            clientAdminId: selectedSubPackage.clientAdminId,
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
          },
        ],
      };

      const response = await apiClient.post(API_END_POINTS.ASSIGN_SUB_PACKAGE, {
        data: payload,
      });
      if ([200, 201].includes(response.statusCode)) {
        toast.success('Sub package assigned successfully');
        onClose();
        setIsLoading(false);
        refetch();
      } else {
        toast.error(response.data.message);
        setIsLoading(false);
      }
    } catch (error) {
      toast.error('Error assigning sub package');
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

export default AspireAdminAssignSubPackage;
