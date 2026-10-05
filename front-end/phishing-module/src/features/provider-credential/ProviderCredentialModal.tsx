import { FormEvent, useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import { Loader2 } from 'lucide-react';

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
import useProviderCredentials from 'hooks/UseProviderCredentials';
import {
  PROVIDER_CREDENTIAL_CATEGORIES,
  type IProviderCredential,
  type IProviderCredentialCreateRequest,
  type IProviderCredentialUpdateRequest,
  type IVideoRenderProvider,
  type ProviderCredentialCategory,
} from 'models/ProviderCredential';
import {
  ProviderCredentialFormSchema,
  defaultProviderCredentialForm,
  type TProviderCredentialForm,
} from 'schemas/ProviderCredentialSchema';

interface ProviderCredentialModalProps {
  isOpen: boolean;
  isSubmitting: boolean;
  credential: IProviderCredential | null;
  onClose: () => void;
  onCreate: (
    payload: IProviderCredentialCreateRequest,
  ) => Promise<{ ok: boolean; message: string }>;
  onUpdate: (
    id: string,
    payload: IProviderCredentialUpdateRequest,
  ) => Promise<{ ok: boolean; message: string }>;
  onSaved?: () => void;
}

const ProviderCredentialModal = ({
  isOpen,
  isSubmitting,
  credential,
  onClose,
  onCreate,
  onUpdate,
  onSaved,
}: ProviderCredentialModalProps) => {
  const isEdit = !!credential;
  const { getVideoRenderProviders } = useProviderCredentials();
  const [form, setForm] = useState<TProviderCredentialForm>(
    defaultProviderCredentialForm,
  );
  const [errors, setErrors] = useState<
    Partial<Record<keyof TProviderCredentialForm, string>>
  >({});
  const [videoProviders, setVideoProviders] = useState<IVideoRenderProvider[]>(
    [],
  );

  const isVideoCategory = form.category === 'VIDEO_RENDERING';

  useEffect(() => {
    if (!isOpen) return;

    if (credential) {
      setForm({
        providerName: credential.providerName,
        category: credential.category,
        modelName: credential.modelName || '',
        apiKey: '',
        apiSecret: '',
        baseUrl: credential.baseUrl || '',
        isActive: credential.isActive,
        isDefault: credential.isDefault,
      });
    } else {
      setForm(defaultProviderCredentialForm);
    }
    setErrors({});
  }, [credential, isOpen]);

  useEffect(() => {
    if (!isOpen || !isVideoCategory) return;

    let active = true;

    void getVideoRenderProviders().then(result => {
      if (!active) return;

      if (!result.ok) {
        toast.error(result.message);
        setVideoProviders([]);
        return;
      }

      setVideoProviders(result.data);
    });

    return () => {
      active = false;
    };
  }, [getVideoRenderProviders, isOpen, isVideoCategory]);

  const videoProviderOptions = useMemo(() => {
    const options = [...videoProviders];
    const currentName = form.providerName.trim();
    if (
      currentName &&
      !options.some(option => option.provider === currentName)
    ) {
      options.push({ provider: currentName, displayName: currentName });
    }
    return options;
  }, [form.providerName, videoProviders]);

  const updateField = <K extends keyof TProviderCredentialForm>(
    key: K,
    value: TProviderCredentialForm[K],
  ) => {
    setForm(prev => ({ ...prev, [key]: value }));
    setErrors(prev => ({ ...prev, [key]: undefined }));
  };

  const handleCategoryChange = (value: ProviderCredentialCategory) => {
    updateField('category', value);
    if (value !== 'VIDEO_RENDERING') return;

    const isKnownVideoProvider = videoProviders.some(
      option => option.provider === form.providerName,
    );
    if (!isKnownVideoProvider) {
      updateField('providerName', '');
    }
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    const parsed = ProviderCredentialFormSchema.safeParse({
      ...form,
      providerName: form.providerName.trim(),
      modelName: form.modelName.trim(),
      apiKey: form.apiKey.trim(),
      apiSecret: form.apiSecret.trim(),
      baseUrl: form.baseUrl.trim(),
    });

    if (!parsed.success) {
      const fieldErrors = parsed.error.flatten().fieldErrors;
      setErrors({
        providerName: fieldErrors.providerName?.[0],
        category: fieldErrors.category?.[0],
        modelName: fieldErrors.modelName?.[0],
        apiKey: fieldErrors.apiKey?.[0],
        apiSecret: fieldErrors.apiSecret?.[0],
        baseUrl: fieldErrors.baseUrl?.[0],
      });
      return;
    }

    if (!isEdit && !parsed.data.apiKey) {
      setErrors(prev => ({ ...prev, apiKey: 'API key is required' }));
      return;
    }

    setErrors({});

    const basePayload = {
      providerName: parsed.data.providerName,
      category: parsed.data.category,
      modelName: parsed.data.modelName || undefined,
      baseUrl: parsed.data.baseUrl || undefined,
      isActive: parsed.data.isActive,
      isDefault: parsed.data.isDefault,
    };

    const result = isEdit
      ? await onUpdate(credential.id, {
          ...basePayload,
          ...(parsed.data.apiKey ? { apiKey: parsed.data.apiKey } : {}),
          ...(parsed.data.apiSecret
            ? { apiSecret: parsed.data.apiSecret }
            : {}),
        })
      : await onCreate({
          ...basePayload,
          apiKey: parsed.data.apiKey,
          apiSecret: parsed.data.apiSecret || undefined,
        });

    if (result.ok) {
      toast.success(result.message);
      onClose();
      onSaved?.();
      return;
    }

    toast.error(result.message);
  };

  return (
    <Dialog open={isOpen} onOpenChange={open => !open && onClose()}>
      <DialogContent className="max-h-[85vh] max-w-xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {isEdit ? 'Edit provider credential' : 'Add provider credential'}
          </DialogTitle>
          <DialogDescription>
            Configure API credentials for voice cloning or video rendering
            providers.
            {isEdit
              ? ' Leave API key/secret blank to keep existing values.'
              : ''}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label>
                Category <span className="text-vibrant-red">*</span>
              </Label>
              <Select
                value={form.category}
                onValueChange={value =>
                  handleCategoryChange(value as ProviderCredentialCategory)
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {PROVIDER_CREDENTIAL_CATEGORIES.map(option => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.category ? (
                <p className="text-xs text-vibrant-red">{errors.category}</p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="providerName">
                Provider name <span className="text-vibrant-red">*</span>
              </Label>
              {isVideoCategory ? (
                <Select
                  value={form.providerName || undefined}
                  onValueChange={value => updateField('providerName', value)}
                >
                  <SelectTrigger id="providerName">
                    <SelectValue placeholder="Select a video provider" />
                  </SelectTrigger>
                  <SelectContent>
                    {videoProviderOptions.map(option => (
                      <SelectItem key={option.provider} value={option.provider}>
                        {option.displayName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              ) : (
                <Input
                  id="providerName"
                  type="search"
                  value={form.providerName}
                  onChange={event =>
                    updateField('providerName', event.target.value)
                  }
                  placeholder="e.g. ELEVENLABS, FISH_AUDIO"
                />
              )}
              {errors.providerName ? (
                <p className="text-xs text-vibrant-red">{errors.providerName}</p>
              ) : null}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="modelName">Model name</Label>
            <Input
              id="modelName"
              value={form.modelName}
              onChange={event => updateField('modelName', event.target.value)}
              placeholder="Optional model identifier"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="apiKey">
              API key {!isEdit ? <span className="text-vibrant-red">*</span> : null}
            </Label>
            <Input
              id="apiKey"
              type="password"
              autoComplete="off"
              value={form.apiKey}
              onChange={event => updateField('apiKey', event.target.value)}
              placeholder={
                isEdit
                  ? credential?.apiKeyLast4
                    ? `••••${credential.apiKeyLast4}`
                    : 'Leave blank to keep current key'
                  : 'Enter API key'
              }
            />
            {errors.apiKey ? (
              <p className="text-xs text-vibrant-red">{errors.apiKey}</p>
            ) : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="apiSecret">API secret</Label>
            <Input
              id="apiSecret"
              type="password"
              autoComplete="off"
              value={form.apiSecret}
              onChange={event => updateField('apiSecret', event.target.value)}
              placeholder={
                isEdit
                  ? credential?.hasApiSecret
                    ? 'Leave blank to keep current secret'
                    : 'Optional'
                  : 'Optional'
              }
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="baseUrl">Base URL</Label>
            <Input
              id="baseUrl"
              value={form.baseUrl}
              onChange={event => updateField('baseUrl', event.target.value)}
              placeholder="https://api.example.com"
            />
            {errors.baseUrl ? (
              <p className="text-xs text-vibrant-red">{errors.baseUrl}</p>
            ) : null}
          </div>

          <div className="flex flex-wrap gap-6">
            <label className="flex items-center gap-2 text-sm">
              <Checkbox
                checked={form.isActive}
                onCheckedChange={checked =>
                  updateField('isActive', checked === true)
                }
              />
              Active
            </label>
            <label className="flex items-center gap-2 text-sm">
              <Checkbox
                checked={form.isDefault}
                onCheckedChange={checked =>
                  updateField('isDefault', checked === true)
                }
              />
              Default for category
            </label>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="secondary"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? (
                <Loader2 className="mr-2 size-4 animate-spin" />
              ) : null}
              {isEdit ? 'Save changes' : 'Create'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ProviderCredentialModal;
