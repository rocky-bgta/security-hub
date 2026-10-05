import { Check, Loader2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'components/common/Button';
import { Label } from 'components/common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import useProviderCredentials from 'hooks/UseProviderCredentials';
import type { IProviderCredential } from 'models/ProviderCredential';
import { cn } from 'utils/Helper';

interface IPreviewGenerateProps {
  provider: string;
  setProvider: (value: string) => void;
  providerId: string;
  setProviderId: (value: string) => void;
  model: string;
  setModel: (value: string) => void;
  generating: boolean;
  progress: number;
  approved: boolean;
  background: string;
  face: string | null;
}

interface IVideoProviderOption {
  providerName: string;
  models: { name: string; providerId: string }[];
  defaultProviderId: string;
  isDefault: boolean;
}

const normalizeProviderName = (value?: string): string =>
  (value || '').trim().replace(/[ ,+_-]+/g, '_');

const resolveProviderLabel = (providerName: string): string =>
  providerName.replace(/_/g, ' ');

const resolveVideoProviders = (
  items: IProviderCredential[],
): IVideoProviderOption[] => {
  const videoCreds = items
    .filter(item => item.category === 'VIDEO_RENDERING' && item.isActive)
    .sort((a, b) => Number(b.isDefault) - Number(a.isDefault));

  const byProvider = new Map<string, IVideoProviderOption>();

  for (const item of videoCreds) {
    const providerName = normalizeProviderName(item.providerName);
    if (!providerName || !item.id) continue;

    const existing = byProvider.get(providerName);
    const modelName = item.modelName?.trim() || '';

    if (!existing) {
      byProvider.set(providerName, {
        providerName,
        models: modelName ? [{ name: modelName, providerId: item.id }] : [],
        defaultProviderId: item.id,
        isDefault: item.isDefault,
      });
      continue;
    }

    if (modelName && !existing.models.some(model => model.name === modelName)) {
      existing.models.push({ name: modelName, providerId: item.id });
    }
    if (item.isDefault) {
      existing.isDefault = true;
      existing.defaultProviderId = item.id;
    }
  }

  return Array.from(byProvider.values());
};

const resolveCredentialId = (
  option: IVideoProviderOption | null | undefined,
  modelName: string,
): string => {
  if (!option) return '';
  const matchedModel = option.models.find(model => model.name === modelName);
  return matchedModel?.providerId || option.defaultProviderId || '';
};

const PreviewGenerate = ({
  provider,
  setProvider,
  providerId,
  setProviderId,
  model,
  setModel,
  generating,
  approved,
  background,
  face,
}: IPreviewGenerateProps) => {
  const { getCredentialList, isLoadingList: providersLoading } =
    useProviderCredentials();
  const [videoProviders, setVideoProviders] = useState<IVideoProviderOption[]>(
    [],
  );

  useEffect(() => {
    let active = true;
    void getCredentialList({
      isActive: true,
      offset: 0,
      pageSize: 100,
      sortBy: 'createdAt',
      sortOrder: 'asc',
    }).then(result => {
      if (!active) return;

      if (!result.ok) {
        toast.error(result.message);
        setVideoProviders([]);
        return;
      }

      setVideoProviders(resolveVideoProviders(result.data.items));
    });

    return () => {
      active = false;
    };
  }, [getCredentialList]);

  const selectedProviderKey = normalizeProviderName(provider);
  const selectedProvider = useMemo(
    () =>
      videoProviders.find(item => item.providerName === selectedProviderKey) ||
      null,
    [videoProviders, selectedProviderKey],
  );
  const availableModels = selectedProvider?.models ?? [];

  const applySelection = (
    option: IVideoProviderOption,
    nextModel: string,
  ) => {
    setProvider(option.providerName);
    setModel(nextModel);
    setProviderId(resolveCredentialId(option, nextModel));
  };

  useEffect(() => {
    if (providersLoading || videoProviders.length === 0) return;

    const matched = videoProviders.find(
      item => item.providerName === selectedProviderKey,
    );

    if (matched) {
      const nextModel =
        model && matched.models.some(item => item.name === model)
          ? model
          : matched.models[0]?.name || '';
      const nextProviderId = resolveCredentialId(matched, nextModel);

      if (model !== nextModel) setModel(nextModel);
      if (providerId !== nextProviderId) setProviderId(nextProviderId);
      return;
    }

    const fallback =
      videoProviders.find(item => item.isDefault) || videoProviders[0];
    if (!fallback) return;

    applySelection(fallback, fallback.models[0]?.name || '');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    providersLoading,
    videoProviders,
    selectedProviderKey,
    model,
    providerId,
    setProvider,
    setProviderId,
    setModel,
  ]);

  return (
    <div className="space-y-6">
      {providersLoading ? (
        <div className="flex items-center justify-center gap-2 rounded-xl border border-card-border p-10 text-sm text-muted-foreground">
          <Loader2 className="size-4 animate-spin" />
          Loading video providers…
        </div>
      ) : videoProviders.length === 0 ? (
        <div className="rounded-xl border border-dashed border-card-border p-10 text-center text-sm text-muted-foreground">
          No active video rendering providers configured. Add one under Provider
          Configuration.
        </div>
      ) : (
        <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
          {videoProviders.map(option => {
            const active = selectedProviderKey === option.providerName;
            return (
              <Button
                key={option.providerName}
                variant="outline"
                onClick={() =>
                  applySelection(option, option.models[0]?.name || '')
                }
                className={cn(
                  'h-auto items-start justify-between rounded-xl p-4 transition-all hover:border-primary',
                  active
                    ? 'border-primary bg-primary/10 hover:bg-primary/10'
                    : 'border-card-border',
                )}
              >
                <div className="flex flex-col justify-between text-wrap text-left">
                  <span className="text-lg font-medium">
                    {resolveProviderLabel(option.providerName)}
                  </span>
                  <p className="mt-1 text-xs text-muted-foreground">
                    {option.models.length > 0
                      ? `${option.models.length} model${option.models.length === 1 ? '' : 's'} available`
                      : 'No models configured'}
                  </p>
                </div>
                {active && <Check className="size-4 text-primary" />}
              </Button>
            );
          })}
        </div>
      )}

      {selectedProvider && availableModels.length > 0 ? (
        <div className="space-y-2">
          <Label>Model</Label>
          <Select
            value={model || undefined}
            onValueChange={value => {
              setModel(value);
              setProviderId(resolveCredentialId(selectedProvider, value));
            }}
          >
            <SelectTrigger>
              <SelectValue placeholder="Select a model" />
            </SelectTrigger>
            <SelectContent>
              {availableModels.map(item => (
                <SelectItem key={item.providerId} value={item.name}>
                  {item.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      ) : null}

      <div
        className="relative grid aspect-video place-items-center overflow-hidden rounded-xl border border-card-border"
        style={{ background }}
      >
        {face && (
          <img
            src={face}
            alt="Avatar"
            className={cn(
              'absolute bottom-0 left-1/2 h-[80%] -translate-x-1/2 object-contain transition-all',
              generating && 'blur-sm',
            )}
          />
        )}
      </div>

      <div className="flex flex-col items-center gap-2">
        {!approved && provider && model ? (
          <p className="text-xs text-muted-foreground">
            Review &amp; approve your setup to enable video generation.
          </p>
        ) : null}
      </div>
    </div>
  );
};

export default PreviewGenerate;
