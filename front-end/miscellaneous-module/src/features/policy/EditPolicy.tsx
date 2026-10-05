import { ChangeEvent } from 'react';

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
import { Textarea } from 'common/Textarea';

interface IProps {
  isEditPolicyOpen: boolean;
  setIsEditPolicyOpen: (open: boolean) => void;
  selectedPolicy: any;
  handleEditPolicyRequest: () => void;
  handleChangeValue: (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => void;
}

const EditPolicy = ({
  isEditPolicyOpen,
  setIsEditPolicyOpen,
  selectedPolicy,
  handleEditPolicyRequest,
  handleChangeValue,
}: IProps) => {
  return (
    <Dialog open={isEditPolicyOpen} onOpenChange={setIsEditPolicyOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Request Policy Update</DialogTitle>
          <DialogDescription>
            Submit a request to update policy (requires Super Admin approval)
          </DialogDescription>
        </DialogHeader>
        {selectedPolicy && (
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Policy Name</Label>
              <Input
                id="name"
                value={selectedPolicy.name}
                onChange={handleChangeValue}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Update Description</Label>
              <Textarea
                id="description"
                placeholder="Describe the changes needed"
                value={selectedPolicy.description}
                onChange={handleChangeValue}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="effectiveDate">Effective Date</Label>
                <Input
                  id="effectiveDate"
                  type="date"
                  value={selectedPolicy.effectiveDate}
                  onChange={handleChangeValue}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="expiryDate">Expiry Date</Label>
                <Input
                  id="expiryDate"
                  type="date"
                  value={selectedPolicy.expiryDate}
                  onChange={handleChangeValue}
                />
              </div>
            </div>
            <Button onClick={handleEditPolicyRequest} className="w-full">
              Submit Update Request
            </Button>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default EditPolicy;
