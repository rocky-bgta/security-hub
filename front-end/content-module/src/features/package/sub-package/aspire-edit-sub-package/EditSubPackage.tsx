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
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn, isSuccessResponse } from 'utils/Helper';
import Step1 from './Step1';
import Step2 from './Step2';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedSubPackage: any;
  refetch: () => void;
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

const AspireAdminEditSubPackage = ({
  isOpen,
  onClose,
  selectedSubPackage,
  refetch,
}: IProps) => {
  const { userInfo } = useStore();
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [formData, setFormData] = useState<ICreateSubPackageFormData>({
    subPackageName: {
      name: '',
      description: '',
      status: '',
    },
    topics: [],
  });
  const [loading, setLoading] = useState(false);
  const apiClient = useAPI();
  const progress = (currentStep / steps.length) * 100;

  useEffect(() => {
    if (selectedSubPackage) {
      fetchSubPackage(selectedSubPackage.id);
    }
  }, [selectedSubPackage]);

  const fetchSubPackage = async (id: string) => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        `${API_END_POINTS.SUB_PACKAGE_DETAILS.replace(':id', id)}`,
      );
      setFormData({
        subPackageName: {
          name: response.data.name,
          description: response.data.description,
          status: response.data.status,
        },
        topics: response.data.topicDetails,
      });
    } catch (error) {
      console.error('Error fetching sub package:', error);
      toast.error('Failed to fetch sub package');
    } finally {
      setLoading(false);
    }
  };

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
            packageId={selectedSubPackage.packageId}
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
      clientAdminId: userInfo.userId,
      status: formData.subPackageName.status,
    };
    try {
      const response = await apiClient.put(
        API_END_POINTS.SUB_PACKAGE_UPDATE + selectedSubPackage.id,
        {
          data: payload,
        },
      );
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Sub package updated successfully');
        refetch();
        onClose();
      } else {
        toast.error(response.data.message);
      }
    } catch (error) {
      console.error('Error updating sub package:', error);
      toast.error('Failed to update sub package');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Dialog open={isOpen} onOpenChange={onClose}>
        <DialogContent className="!content-flex !content-h-4/5 !content-w-4/5 content-flex-col !content-overflow-y-auto content-text-white">
          <DialogHeader>
            <DialogTitle className="content-text-white">
              Edit Sub Package
            </DialogTitle>
            <DialogDescription>
              Edit sub package name and description
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
                        ? 'content-border-2 content-border-primary content-bg-transparent'
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

export default AspireAdminEditSubPackage;
