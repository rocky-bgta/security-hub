import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';

import { Badge } from 'common/Badge';
import { Card, CardContent } from 'common/Card';
import { Progress } from 'common/Progress';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { ICreateSubPackageFormData } from 'models/Form';
import { Status } from 'models/Global';
import { IAssignedLicense } from 'models/License';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { cn, isSuccessResponse } from 'utils/Helper';
import Step1 from './Step1';
import Step2 from './Step2';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  product: IAssignedLicense;
}

const steps = [
  {
    id: 1,
    title: 'Sub package Name',
    description: 'Create unique Sub package name',
  },
  {
    id: 2,
    title: 'Topic Selection',
    description: 'Choose topics for the sub package',
  },
];

const CreateAndAssignProduct = ({ isOpen, onClose, product }: IProps) => {
  const { userInfo } = useStore();
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [formData, setFormData] = useState<ICreateSubPackageFormData>({
    subPackageName: {
      name: '',
      description: '',
    },
    topics: [],
  });
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();
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
    section: keyof ICreateSubPackageFormData,
    data: any,
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
          <Step1
            data={formData.subPackageName}
            onUpdate={data => updateFormData('subPackageName', data)}
            onNext={handleNext}
          />
        );
      case 2:
        return (
          <Step2
            loading={loading}
            data={formData.topics}
            packageId={product?.packageDetails.id}
            onUpdate={data => updateFormData('topics', data)}
            onPrevious={handlePrevious}
            onSubmit={handleSubmit}
          />
        );
      default:
        return null;
    }
  };

  const handleSubmit = async () => {
    setLoading(true);
    const payload = {
      name: formData.subPackageName.name,
      description: formData.subPackageName.description,
      topicId: formData.topics.map(topic => topic.id),
      productId: product.productId,
      packageId: product.packageDetails.id,
      clientId: userInfo.userId,
      clientAdminId: userInfo.userId,
      productPackageId: product.id,
      status: Status.ACTIVE,
    };

    const response = await apiClient.post(API_END_POINTS.SUB_PACKAGE_CREATE, {
      data: payload,
    });
    if (isSuccessResponse(response.statusCode)) {
      toast.success('Sub package created successfully');
      navigate(routes.subPackages.path, { replace: true });
      onClose();
    } else {
      toast.error(response.data.message);
      setLoading(false);
    }
  };

  return (
    <div>
      <Dialog open={isOpen} onOpenChange={onClose}>
        <DialogContent className="!content-flex !content-h-[90%] !content-w-4/5 content-flex-col !content-overflow-y-auto content-text-white">
          <DialogHeader>
            <DialogTitle className="content-text-white">
              Create Sub Package
            </DialogTitle>
            <DialogDescription>
              Select Topics to create sub package
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

export default CreateAndAssignProduct;
