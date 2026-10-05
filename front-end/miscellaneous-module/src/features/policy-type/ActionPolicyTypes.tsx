import { Save, X } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { useAPI } from 'hooks/UseAPI';
// import { IOrganizationSizePayload } from 'models/OrganizationSize';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IPolicyTypes } from 'models/PolicyTypes';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  policyTypes: IPolicyTypes | null;
  onSubmit: (data: IPolicyTypes) => void;
}

type FormPayload = {
  name: string;
  code: string;
  status: string; // string like "DRAFT", "ACTIVE", etc.
};

const DefaultPolicyTypes: FormPayload = {
  name: '',
  code: '',
  status: 'DRAFT',
};

const ActionPolicyTypes = ({
  isOpen,
  onClose,
  policyTypes,
  onSubmit,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [formData, setFormData] = useState<FormPayload>({
    ...DefaultPolicyTypes,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const apiClient = useAPI();

  useEffect(() => {
    setFormData(
      policyTypes
        ? {
            name: policyTypes.name,
            code: policyTypes.code,
            // support both a possible string status from backend or legacy boolean active
            status:
              // if backend provides status string use it
              (policyTypes as any).status
                ? (policyTypes as any).status
                : // otherwise fallback to active boolean
                  (policyTypes as any).active
                  ? 'ACTIVE'
                  : 'INACTIVE',
          }
        : { ...DefaultPolicyTypes },
    );
  }, [policyTypes, isOpen]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Policy type name is required';
    }

    if (!formData.code.trim()) {
      newErrors.code = 'Policy type code is required';
    }

    // validate status
    const allowedStatuses = ['DRAFT', 'ACTIVE', 'INACTIVE'];
    if (!allowedStatuses.includes(formData.status)) {
      newErrors.status = `Invalid status. Allowed: ${allowedStatuses.join(', ')}`;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: string, value: string) => {
    // normalize code to uppercase so it matches backend enum values
    const newValue = field === 'code' ? value.toUpperCase() : value;
    setFormData(prev => ({ ...prev, [field]: newValue }));

    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);

    if (!validateForm()) {
      setLoading(false);
      return;
    }

    // validate code against allowed PolicyTypeCode enum values to avoid backend JSON parse errors
    const allowedCodes = [
      'PRIVACY',
      'IT',
      'SECURITY',
      'COMPLIANCE',
      'HUMAN_RESOURCE',
    ];
    const codeUpper = formData.code.trim().toUpperCase();

    if (!allowedCodes.includes(codeUpper)) {
      setErrors(prev => ({
        ...prev,
        code: 'Invalid policy type code. Allowed values: PRIVACY, IT, SECURITY, COMPLIANCE, HUMAN_RESOURCE',
      }));
      toast.error('Invalid policy type code. Please choose a valid code.');
      setLoading(false);
      return;
    }

    // Backend expects a "status" string (DRAFT/ACTIVE/INACTIVE)
    const requestPayload = {
      name: formData.name,
      code: codeUpper,
      status: formData.status,
    };

    // keep status locally for parent
    const localPayload = {
      name: formData.name,
      code: codeUpper,
      status: formData.status,
    };

    try {
      let response: IResponse<IPolicyTypes>;
      if (policyTypes) {
        response = await apiClient.put(
          API_END_POINTS.UPDATE_POLICY_TYPE.replace(':id', policyTypes.id),
          {
            data: requestPayload,
          },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CREATE_POLICY_TYPE, {
          data: requestPayload,
        });
      }

      if (isSuccessResponse(response.statusCode)) {
        // merge backend response with submitted local payload so parent receives name/code/status
        const result = { ...(response.data as any), ...localPayload };
        onSubmit?.(result as IPolicyTypes);
        toast.success(
          policyTypes
            ? 'Policy type updated successfully'
            : 'Policy type created successfully',
        );
      } else {
        toast.error('Failed to save policy type');
      }
    } catch (error) {
      console.error('Error creating/updating policy type:', error);
      toast.error('Failed to save policy type');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Policy Type Information</DialogTitle>
          <DialogDescription>
            Fill in the details below to create a new policy type
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="name" className="font-medium text-foreground">
              Policy Type Name *
            </Label>
            <Input
              id="name"
              placeholder="e.g. IT, Security, Finance"
              value={formData.name}
              onChange={e => handleInputChange('name', e.target.value)}
              className={errors.name ? 'has-error' : ''}
            />
            {errors.name && (
              <p className="text-sm text-destructive">{errors.name}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="code" className="font-medium text-foreground">
              Policy Type Code *{' '}
              <span className="text-xs text-muted-foreground">
                Allowed values: PRIVACY, IT, SECURITY, COMPLIANCE,
                HUMAN_RESOURCE
              </span>
            </Label>
            <Input
              id="code"
              placeholder="e.g. IT, SECURITY, FINANCE"
              value={formData.code}
              onChange={e => handleInputChange('code', e.target.value)}
              className={errors.code ? 'has-error' : ''}
            />
            {errors.code && (
              <p className="text-sm text-destructive">{errors.code}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="status" className="font-medium text-foreground">
              Policy Type Status *
            </Label>
            <Select
              value={formData.status}
              onValueChange={e => handleInputChange('status', e)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="DRAFT">DRAFT</SelectItem>
                <SelectItem value="ACTIVE">ACTIVE</SelectItem>
                <SelectItem value="INACTIVE">INACTIVE</SelectItem>
              </SelectContent>
            </Select>
            {errors.status && (
              <p className="text-sm text-destructive">{errors.status}</p>
            )}
          </div>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button
              disabled={loading}
              type="button"
              variant="outline"
              onClick={onClose}
            >
              <X className="mr-2 size-4" />
              Cancel
            </Button>
            <Button disabled={loading} type="submit">
              <Save className="mr-2 size-4" />
              {loading ? 'Saving...' : 'Save Policy Type'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionPolicyTypes;
