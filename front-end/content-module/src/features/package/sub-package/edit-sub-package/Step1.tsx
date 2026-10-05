import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Textarea } from 'common/Textarea';
import { ISubPackageName } from 'models/Form';
import { Status } from 'models/Global';
import { useEffect, useState } from 'react';

interface IProps {
  data: ISubPackageName;
  onUpdate: (data: ISubPackageName) => void;
  onNext: () => void;
}

const Step1 = ({ data, onUpdate, onNext }: IProps) => {
  const [formData, setFormData] = useState<ISubPackageName>({
    name: '',
    status: '',
    description: '',
  });
  const [errors, setErrors] = useState<any>({});

  useEffect(() => {
    setFormData(data ?? { name: '', description: '' });
  }, [data]);

  const handleSave = () => {
    if (validateForm()) {
      onUpdate(formData);
      onNext();
    }
  };

  const validateForm = () => {
    const newErrors: any = {};
    if (!formData.name) {
      newErrors.name = 'Name is required';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  return (
    <Card className="content-w-full">
      <CardHeader>
        <CardTitle>Edit Sub Package Name</CardTitle>
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
              onChange={e => (
                setFormData({ ...formData, name: e.target.value }),
                setErrors({ ...errors, name: '' })
              )}
              placeholder="Enter sub package name (e.g., Advanced Security Training)"
              className={errors.name ? 'content-has-error' : ''}
            />
            {errors.name && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors.name}
              </p>
            )}
          </div>
          <div>
            <Label htmlFor="status" className="content-text-cloudy-white">
              Status
            </Label>
            <Select
              value={formData.status}
              onValueChange={value =>
                setFormData({ ...formData, status: value })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Select status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={Status.ACTIVE}>Active</SelectItem>
                <SelectItem value={Status.INACTIVE}>Inactive</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label htmlFor="description" className="content-text-cloudy-white">
              Description
            </Label>
            <Textarea
              id="description"
              value={formData.description}
              onChange={e =>
                setFormData({ ...formData, description: e.target.value })
              }
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
        <div className="content-flex content-justify-end content-pt-6">
          <Button onClick={handleSave}>Save & Continue</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step1;
