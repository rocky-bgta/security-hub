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
import { AIProviderType, IAIModelRequest } from 'models/AiProvider';
import { FormEvent, useState } from 'react';
import { toast } from 'react-toastify';

const providerOptions: { value: AIProviderType; label: string }[] = [
  { value: AIProviderType.GEMINI, label: 'Google Gemini' },
  { value: AIProviderType.OPENAI, label: 'OpenAI' },
  { value: AIProviderType.CLAUDE, label: 'Claude' },
  { value: AIProviderType.ZAI, label: 'Z.AI' },
];

interface AIModelCreateModalProps {
  isOpen: boolean;
  isSubmitting: boolean;
  onClose: () => void;
  onCreate: (payload: IAIModelRequest) => Promise<{
    ok: boolean;
    message: string;
  }>;
  onCreated?: () => void;
}

const defaultForm: IAIModelRequest = {
  name: '',
  providerType: AIProviderType.OPENAI,
  default: false,
  active: true,
};

const AIModelCreateModal = ({
  isOpen,
  isSubmitting,
  onClose,
  onCreate,
  onCreated,
}: AIModelCreateModalProps) => {
  const [form, setForm] = useState<IAIModelRequest>(defaultForm);
  const [nameError, setNameError] = useState<string>('');

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const name = form.name.trim();
    if (!name) {
      setNameError('Model name is required');
      return;
    }

    setNameError('');
    const result = await onCreate({
      ...form,
      name,
    });
    if (result.ok) {
      toast.success(result.message);
      setForm(defaultForm);
      onClose();
      onCreated?.();
      return;
    }
    toast.error(result.message);
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Create AI model</DialogTitle>
          <DialogDescription>
            Add an AI model and map it to a provider.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="flex flex-col gap-6">
          <div className="flex flex-col gap-2">
            <Label htmlFor="model-name">
              Model name <span className="text-vibrant-red">*</span>
            </Label>
            <Input
              id="model-name"
              name="name"
              value={form.name}
              onChange={e => {
                setForm(prev => ({ ...prev, name: e.target.value }));
                setNameError('');
              }}
              placeholder="e.g. gpt-4o-mini"
              disabled={isSubmitting}
              error={nameError || undefined}
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="model-provider-type">
              Provider type <span className="text-vibrant-red">*</span>
            </Label>
            <Select
              value={form.providerType}
              onValueChange={value =>
                setForm(prev => ({
                  ...prev,
                  providerType: value as AIProviderType,
                }))
              }
              disabled={isSubmitting}
            >
              <SelectTrigger id="model-provider-type" className="w-full">
                <SelectValue placeholder="Select provider" />
              </SelectTrigger>
              <SelectContent>
                {providerOptions.map(option => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="flex items-center gap-2">
            <Checkbox
              id="model-default"
              checked={form.default}
              onCheckedChange={checked =>
                setForm(prev => ({ ...prev, default: Boolean(checked) }))
              }
              disabled={isSubmitting}
            />
            <Label htmlFor="model-default">Set as default model</Label>
          </div>

          <div className="flex items-center gap-2">
            <Checkbox
              id="model-active"
              checked={form.active}
              onCheckedChange={checked =>
                setForm(prev => ({ ...prev, active: Boolean(checked) }))
              }
              disabled={isSubmitting}
            />
            <Label htmlFor="model-active">Active</Label>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Saving…' : 'Create model'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default AIModelCreateModal;
