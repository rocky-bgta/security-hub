import { ChangeEvent, useState } from 'react';

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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Textarea } from 'common/Textarea';

interface IProps {
  isAddPolicyOpen: boolean;
  setIsAddPolicyOpen: (open: boolean) => void;
  handleAddPolicyRequest: (policy: any) => void;
}

const RequestNewPolicy = ({
  isAddPolicyOpen,
  setIsAddPolicyOpen,
  handleAddPolicyRequest,
}: IProps) => {
  const [policy, setPolicy] = useState<{
    name: string;
    type: string;
    description: string;
    file: File | undefined;
    effectiveDate: string;
    expiryDate: string;
  }>({
    name: '',
    type: '',
    description: '',
    file: undefined,
    effectiveDate: '',
    expiryDate: '',
  });

  const handleChangeValue = (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => {
    const { id, value } = e.target;
    setPolicy(prev => ({ ...prev, [id]: value }));
  };

  return (
    <Dialog open={isAddPolicyOpen} onOpenChange={setIsAddPolicyOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Request New Policy</DialogTitle>
          <DialogDescription>
            Submit a request for a new policy (requires Super Admin approval)
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Policy Name</Label>
            <Input
              id="name"
              placeholder="Enter policy name"
              value={policy.name}
              onChange={handleChangeValue}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="type">Policy Type</Label>
            <Select
              value={policy.type}
              onValueChange={value =>
                setPolicy(prev => ({ ...prev, type: value }))
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Select policy type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="security">Security</SelectItem>
                <SelectItem value="compliance">Compliance</SelectItem>
                <SelectItem value="operational">Operational</SelectItem>
                <SelectItem value="hr">HR</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="description">Policy Description</Label>
            <Textarea
              id="description"
              placeholder="Describe the policy requirements"
              value={policy.description}
              onChange={handleChangeValue}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="file">Attach Policy File (Optional)</Label>
            <Input
              id="file"
              type="file"
              accept=".pdf,.doc,.docx"
              onChange={e =>
                setPolicy(prev => ({ ...prev, file: e.target.files?.[0] }))
              }
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="effectiveDate">Effective Date</Label>
              <Input
                id="effectiveDate"
                type="date"
                value={policy.effectiveDate}
                onChange={handleChangeValue}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="expiryDate">Expiry Date</Label>
              <Input
                id="expiryDate"
                type="date"
                value={policy.expiryDate}
                onChange={handleChangeValue}
              />
            </div>
          </div>
          <Button
            onClick={() => handleAddPolicyRequest(policy)}
            className="w-full"
          >
            Submit Request
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default RequestNewPolicy;
