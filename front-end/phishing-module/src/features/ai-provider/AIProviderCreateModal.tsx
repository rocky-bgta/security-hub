import { Button } from 'common/Button';
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
import { Textarea } from 'common/Textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { AIProviderType, IAIProviderConfigRequest } from 'models/AiProvider';
import { FormEvent, useState } from 'react';
import {
  AIProviderConfigFormSchema,
  defaultAIProviderConfigForm,
  TAIProviderConfigForm,
} from 'schemas/AiProviderSchema';
import { toast } from 'react-toastify';

const providerOptions: { value: AIProviderType; label: string }[] = [
  { value: AIProviderType.GEMINI, label: 'Google Gemini' },
  { value: AIProviderType.OPENAI, label: 'OpenAI' },
  { value: AIProviderType.ZAI, label: 'Z.AI' },
  // { value: AIProviderType.ANTHROPIC, label: 'Anthropic' },
];

interface AIProviderCreateModalProps {
  isOpen: boolean;
  isSubmitting: boolean;
  onClose: () => void;
  onCreate: (payload: IAIProviderConfigRequest) => Promise<{
    ok: boolean;
    message: string;
  }>;
  onCreated?: () => void;
}

const AIProviderCreateModal = ({
  isOpen,
  isSubmitting,
  onClose,
  onCreate,
  onCreated,
}: AIProviderCreateModalProps) => {
  const [form, setForm] = useState<TAIProviderConfigForm>(
    defaultAIProviderConfigForm,
  );
  const [apiKeyError, setApiKeyError] = useState<string | undefined>();

  const updateField = <K extends keyof TAIProviderConfigForm>(
    key: K,
    value: TAIProviderConfigForm[K],
  ) => {
    setForm(prev => ({ ...prev, [key]: value }));
    if (key === 'apiKey') {
      setApiKeyError(undefined);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    const parsed = AIProviderConfigFormSchema.safeParse({
      ...form,
      apiKey: form.apiKey.trim(),
      apiSecret: form.apiSecret.trim(),
      description: form.description.trim(),
    });

    if (!parsed.success) {
      const keyErr = parsed.error.flatten().fieldErrors.apiKey?.[0];
      setApiKeyError(keyErr);
      return;
    }

    setApiKeyError(undefined);

    const payload: IAIProviderConfigRequest = {
      providerType: parsed.data.providerType,
      apiKey: parsed.data.apiKey,
      apiSecret: parsed.data.apiSecret,
      description: parsed.data.description,
    };

    const result = await onCreate(payload);

    if (result.ok) {
      toast.success(result.message);
      setForm(defaultAIProviderConfigForm);
      onClose();
      onCreated?.();
      return;
    }

    toast.error(result.message);
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[80vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Create AI provider configuration</DialogTitle>
          <DialogDescription>
            Your API key is required. API secret is optional.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="flex flex-col gap-6">
          <div className="flex flex-col gap-2">
            <Label htmlFor="provider-type">
              Provider type <span className="text-vibrant-red">*</span>
            </Label>
            <Select
              value={form.providerType}
              onValueChange={v =>
                updateField('providerType', v as AIProviderType)
              }
              disabled={isSubmitting}
            >
              <SelectTrigger id="provider-type" className="w-full">
                <SelectValue placeholder="Select provider" />
              </SelectTrigger>
              <SelectContent>
                {providerOptions.map(opt => (
                  <SelectItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="api-key">
              API key <span className="text-vibrant-red">*</span>
            </Label>
            <Input
              id="api-key"
              name="apiKey"
              type="password"
              autoComplete="off"
              value={form.apiKey}
              onChange={e => updateField('apiKey', e.target.value)}
              placeholder="Enter your API key"
              disabled={isSubmitting}
              error={apiKeyError}
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="api-secret">API secret (optional)</Label>
            <Input
              id="api-secret"
              name="apiSecret"
              type="password"
              autoComplete="off"
              value={form.apiSecret}
              onChange={e => updateField('apiSecret', e.target.value)}
              placeholder="Optional secret"
              disabled={isSubmitting}
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              name="description"
              value={form.description}
              onChange={e => updateField('description', e.target.value)}
              placeholder="Optional notes for this configuration"
              disabled={isSubmitting}
              rows={3}
            />
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
              {isSubmitting ? 'Saving…' : 'Create configuration'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default AIProviderCreateModal;
