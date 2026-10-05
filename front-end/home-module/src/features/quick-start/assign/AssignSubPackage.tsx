import { Badge } from 'common/Badge';
import { Card, CardContent } from 'common/Card';
import { Separator } from 'common/Separator';
import useAPI from 'hooks/UseAPI';
import useStore from 'hooks/UseStore';
import { Status } from 'models/Global';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn, isSuccessResponse } from 'utils/Helper';
import Step1 from './Step1';
import Step2 from './Step2';
import Step3 from './Step3';

interface IProps {
  stepId: number;
  updateStepsStatus: (
    stepId: number,
    status: 'complete' | 'incomplete',
  ) => void;
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
  selectedSubPackage: any;
}

const steps = [
  { id: 1, title: 'Sub Package Selection' },
  { id: 2, title: 'Users Selection' },
  { id: 3, title: 'Confirmation' },
];

const AssignSubPackage = ({ stepId, updateStepsStatus }: IProps) => {
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<IFormData>({
    selectedSubPackage: null,
    subPackageId: '',
    users: [],
    productId: '',
    clientAdminId: userInfo?.userId as string,
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
          <Step1
            data={formData.selectedSubPackage}
            onUpdate={(pkg: any) => updateFormData('selectedSubPackage', pkg)}
            onNext={handleNext}
          />
        );
      case 2:
        return (
          <Step2
            data={formData.users}
            onUpdate={data => updateFormData('users', data)}
            onNext={handleNext}
            onPrevious={handlePrevious}
            selectedSubPackage={formData.selectedSubPackage}
          />
        );

      case 3:
        return (
          <Step3
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
    if (!userInfo) return;

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
            productId: formData.selectedSubPackage.productId,
            packageId: formData.selectedSubPackage.packageId,
            subPackageId: formData.selectedSubPackage.id,
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
            subPackageName: formData.selectedSubPackage.name,
            productName: formData.selectedSubPackage.productName,
            productPackageId: formData.selectedSubPackage.productPackageId,
          },
        ],
      };

      const response = await apiClient.post(API_END_POINTS.ASSIGN_SUB_PACKAGE, {
        data: payload,
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Something went wrong');
      }
      toast.success(response.message || 'Sub package assigned successfully');
      updateStepsStatus(stepId, 'complete');
    } catch (error) {
      toast.error((error as Error).message || 'Error assigning sub package');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card>
      <CardContent className="!home-p-4">
        <div className="home-mb-4 home-flex home-items-center home-justify-end">
          <Badge variant="secondary" className="home-text-sm">
            Step {Math.min(currentStep, steps.length)} of {steps.length}
          </Badge>
        </div>
        <div className="home-grid home-grid-cols-3 home-gap-2">
          {steps.map(step => (
            <div
              key={step.id}
              className={cn(
                'home-rounded-lg home-border home-p-2 home-text-center home-transition-all home-duration-200',
                step.id === currentStep
                  ? 'home-border-2 home-border-primary home-bg-transparent'
                  : step.id < currentStep
                    ? 'home-border-primary home-bg-[#00d4aa]/10'
                    : 'home-border-gray-400 home-bg-transparent',
              )}
            >
              <div
                className={cn(
                  'home-text-sm home-font-medium',
                  step.id === currentStep
                    ? 'home-text-primary'
                    : step.id < currentStep
                      ? 'home-text-primary'
                      : 'home-text-gray-400',
                )}
              >
                {step.title}
              </div>
            </div>
          ))}
        </div>

        <Separator className="home-my-4" />

        {renderStep()}
      </CardContent>
    </Card>
  );
};

export default AssignSubPackage;
