import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
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
import { CampaignChannel } from 'models/Campaign';
import { FormEvent } from 'react';

interface IConfigurationPayload {
  name: string;
  description: string;
  displayOrder: number;
  isDefault: boolean;
  isActive: boolean;
  channel?: CampaignChannel;
}

interface IConfigurationModalProps {
  isOpen: boolean;
  activeTabLabel: string;
  editingItem: { id: string } | null;
  form: IConfigurationPayload;
  formErrors: Partial<Record<keyof IConfigurationPayload, string>>;
  submitting: boolean;
  showChannelField?: boolean;
  onOpenChange: (open: boolean) => void;
  onFieldChange: <K extends keyof IConfigurationPayload>(
    field: K,
    value: IConfigurationPayload[K],
  ) => void;
  onSubmit: (e: FormEvent<HTMLFormElement>) => void;
}

const ConfigurationModal = ({
  isOpen,
  activeTabLabel,
  editingItem,
  form,
  formErrors,
  submitting,
  showChannelField = false,
  onOpenChange,
  onFieldChange,
  onSubmit,
}: IConfigurationModalProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[80vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {editingItem
              ? `Edit ${activeTabLabel}`
              : `Create ${activeTabLabel}`}
          </DialogTitle>
          <DialogDescription>
            Maintain shared configuration values for phishing campaigns.
          </DialogDescription>
        </DialogHeader>

        <form className="space-y-4" onSubmit={onSubmit}>
          <div className="space-y-2">
            <Label htmlFor="config-name">
              Name <span className="text-vibrant-red">*</span>
            </Label>
            <Input
              id="config-name"
              value={form.name}
              onChange={e => onFieldChange('name', e.target.value)}
              placeholder="Enter name"
              error={formErrors.name}
              disabled={submitting}
            />
          </div>
          {showChannelField && (
            <div className="space-y-2">
              <Label htmlFor="config-channel">
                Channel <span className="text-vibrant-red">*</span>
              </Label>
              <Select
                value={form.channel ?? ''}
                onValueChange={value =>
                  onFieldChange('channel', value as CampaignChannel)
                }
                disabled={submitting}
              >
                <SelectTrigger id="config-channel">
                  <SelectValue placeholder="Select channel" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={CampaignChannel.EMAIL}>Email</SelectItem>
                  <SelectItem value={CampaignChannel.SMS}>SMS</SelectItem>
                </SelectContent>
              </Select>
              {formErrors.channel && (
                <p className="text-sm text-vibrant-red">{formErrors.channel}</p>
              )}
            </div>
          )}
          <div className="space-y-2">
            <Label htmlFor="config-description">Description</Label>
            <Textarea
              id="config-description"
              value={form.description}
              onChange={e => onFieldChange('description', e.target.value)}
              rows={4}
              placeholder="Enter description"
              disabled={submitting}
            />
            {formErrors.description && (
              <p className="text-sm text-vibrant-red">
                {formErrors.description}
              </p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="config-order">
              Display Order <span className="text-vibrant-red">*</span>
            </Label>
            <Input
              id="config-order"
              type="number"
              min={0}
              step={1}
              value={form.displayOrder}
              onChange={e =>
                onFieldChange('displayOrder', Number(e.target.value))
              }
              error={formErrors.displayOrder}
              disabled={submitting}
            />
          </div>
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <Checkbox
                id="config-default"
                checked={form.isDefault}
                onCheckedChange={checked =>
                  onFieldChange('isDefault', checked === true)
                }
                disabled={submitting}
              />
              <Label htmlFor="config-default">Is Default</Label>
            </div>
            <div className="flex items-center gap-2">
              <Checkbox
                id="config-active"
                checked={form.isActive}
                onCheckedChange={checked =>
                  onFieldChange('isActive', checked === true)
                }
                disabled={submitting}
              />
              <Label htmlFor="config-active">Is Active</Label>
            </div>
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={submitting}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting
                ? 'Saving...'
                : editingItem
                  ? 'Update Configuration'
                  : 'Create Configuration'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ConfigurationModal;
