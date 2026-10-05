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
import { Status } from 'models/Global';
import { ICreateSubPackageFormData } from 'models/SubPackage';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { cn, isSuccessResponse } from 'utils/Helper';
import Step1 from './Step1';
import Step2 from './Step2';
import Step3 from './Step3';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  reLoad: () => void;
}

const steps = [
  {
    id: 1,
    title: 'Product & Package',
    description: 'Select product and package',
  },
  {
    id: 2,
    title: 'Sub package Name',
    description: 'Create unique Sub package name',
  },
  {
    id: 3,
    title: 'Topic Selection',
    description: 'Choose topics for the sub package',
  },
];

const CreateSubPackage = ({ isOpen, onClose, reLoad }: IProps) => {
  const { userInfo } = useStore();
  const navigate = useNavigate();
  const hostPath = routes;
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [formData, setFormData] = useState<ICreateSubPackageFormData>({
    productPackage: { productId: '', packageId: '', productPackageId: '' },
    subPackageName: {
      name: '',
      description: '',
    },
    topics: [],
  });
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();
  const progress = (currentStep / steps.length) * 100;

  useEffect(() => {
    if (!isOpen) {
      // Reset form when modal closes
      setCurrentStep(1);
      setFormData({
        productPackage: { productId: '', packageId: '', productPackageId: '' },
        subPackageName: {
          name: '',
          description: '',
        },
        topics: [],
      });
    }
  }, [isOpen]);

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
            data={formData.productPackage || null}
            onUpdate={data => updateFormData('productPackage', data)}
            onNext={handleNext}
          />
        );
      case 2:
        return (
          <Step2
            data={formData.subPackageName}
            onUpdate={data => updateFormData('subPackageName', data)}
            onNext={handleNext}
            onPrevious={handlePrevious}
          />
        );
      case 3:
        return (
          <Step3
            loading={loading}
            data={formData.topics}
            packageId={formData.productPackage?.packageId || ''}
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
    if (!formData.productPackage) {
      toast.error('Please select a product and package');
      return;
    }

    setLoading(true);
    const payload = {
      name: formData.subPackageName.name,
      description: formData.subPackageName.description,
      topicId: formData.topics.map(topic => topic.id),
      productId: formData.productPackage.productId,
      packageId: formData.productPackage.packageId,
      clientId: userInfo.userId,
      clientAdminId: userInfo.userId,
      productPackageId: formData.productPackage.productPackageId,
      status: Status.ACTIVE,
    };

    try {
      const response = await apiClient.post(API_END_POINTS.SUB_PACKAGE_CREATE, {
        data: payload,
      });
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Sub package created successfully');
        navigate(hostPath.subPackages.path, { replace: true });
        onClose();
        reLoad();
      } else {
        toast.error(response.data?.message || 'Failed to create sub package');
      }
    } catch (error) {
      console.error('Error creating sub package:', error);
      toast.error('Failed to create sub package');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Dialog open={isOpen} onOpenChange={onClose}>
        <DialogContent className="!content-flex !content-max-h-[90vh] !content-w-4/5 content-flex-col !content-overflow-y-auto content-text-white">
          <DialogHeader>
            <DialogTitle className="content-text-white">
              Create Sub Package
            </DialogTitle>
            <DialogDescription>
              Select Product, Package and Topics to create sub package
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
              <div className="content-grid content-grid-cols-3 content-gap-2">
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

export default CreateSubPackage;
