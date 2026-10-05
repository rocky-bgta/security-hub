import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { ISubPackageName } from 'models/Form';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  data: ISubPackageName;
  onUpdate: (data: ISubPackageName) => void;
  onNext: () => void;
  onPrevious: () => void;
}

const Step2 = ({ data, onUpdate, onNext, onPrevious }: IProps) => {
  const apiClient = useAPI();
  const [formData, setFormData] = useState<ISubPackageName>({
    ...data,
  });
  const [errors, setErrors] = useState<any>({});
  const [isCheckingName, setIsCheckingName] = useState<boolean>(false);
  const [lastCheckedName, setLastCheckedName] = useState<string>('');
  const [lastCheckExists, setLastCheckExists] = useState<boolean | null>(null);

  const checkSubPackageNameExists = async (subPackageName: string) => {
    const queryName = subPackageName.trim();

    if (!queryName) {
      return { exists: false, message: '' };
    }

    if (lastCheckedName === queryName && lastCheckExists !== null) {
      return { exists: lastCheckExists };
    }

    setIsCheckingName(true);
    try {
      const response = await apiClient.get(
        `${API_END_POINTS.SUB_PACKAGE_EXISTS}${encodeURIComponent(queryName)}`,
      );

      const exists = Boolean(response?.data);
      const message =
        response?.message ||
        'Unable to validate sub package name at the moment. Please try again.';

      setLastCheckedName(queryName);
      setLastCheckExists(exists);

      return { exists, message };
    } catch (error) {
      return {
        exists: true,
        message:
          (error as Error)?.message ||
          'Failed to validate sub package name. Please try again.',
      };
    } finally {
      setIsCheckingName(false);
    }
  };

  const handleSave = async () => {
    const normalizedName = formData.name.trim();

    if (validateForm()) {
      const { exists, message } = await checkSubPackageNameExists(normalizedName);
      if (exists) {
        toast.error(message);
        return;
      }

      onUpdate({
        ...formData,
        name: normalizedName,
      });
      onNext();
    }
  };

  const handleChange = (field: string, value: string) => {
    const newErrors = { ...errors };

    if (field === 'name') {
      if (value !== lastCheckedName) {
        setLastCheckedName('');
        setLastCheckExists(null);
      }
      if (value.length > 50) {
        newErrors.name = 'Name must be less than 50 characters';
      } else {
        newErrors.name = '';
      }
    }

    setFormData({ ...formData, [field]: value });
    setErrors(newErrors);
  };
  const validateForm = () => {
    const newErrors: any = {};

    if (
      !formData.name ||
      formData.name.length < 3 ||
      errors.name ||
      formData.name.length > 50
    ) {
      newErrors.name =
        'Name is required and must be at least 3 characters long and must be less than 50 characters.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  return (
    <Card className="content-w-full">
      <CardHeader>
        <CardTitle>Create Sub Package Name and Description</CardTitle>
      </CardHeader>
      <CardContent className="content-space-y-6">
        <div className="content-mx-auto content-max-w-md content-space-y-4">
          <div>
            <Label
              htmlFor="subPackageName"
              className="content-text-cloudy-white"
            >
              Name
            </Label>
            <Input
              id="subPackageName"
              value={formData.name}
              onChange={e => handleChange('name', e.target.value)}
              placeholder="Enter sub package name (e.g., Advanced Security Training)"
              className={errors.name ? 'content-has-error' : ''}
            />
            {errors.name && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors.name}
              </p>
            )}
            {formData.name.length > 0 && (
              <p className="content-float-right content-text-sm content-text-cloudy-white">
                {formData.name.length}/50 characters
              </p>
            )}
          </div>
          <div>
            <Label htmlFor="description" className="content-text-cloudy-white">
              Description
            </Label>
            <Textarea
              id="description"
              value={formData.description}
              onChange={e => handleChange('description', e.target.value)}
              placeholder="Enter sub package description (e.g., Advanced Security Training)"
            />
          </div>

          <div>
            <h4 className="content-mb-2 content-font-medium content-text-primary">
              Tips for choosing a Sub Package name:
            </h4>
            <ul className="content-space-y-1 content-text-sm content-text-primary/90">
              <li>• Make it descriptive of the training content</li>
              <li>• Keep it unique and memorable</li>
              <li>• Avoid special characters</li>
              <li>• Consider your target audience</li>
            </ul>
          </div>
        </div>
        <div className="content-flex content-justify-between content-pt-6">
          <Button
            variant="outline"
            onClick={onPrevious}
            className="content-px-8 content-py-2"
          >
            Previous
          </Button>
          <Button onClick={handleSave} disabled={isCheckingName}>
            {isCheckingName ? 'Validating...' : 'Save & Continue'}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step2;
