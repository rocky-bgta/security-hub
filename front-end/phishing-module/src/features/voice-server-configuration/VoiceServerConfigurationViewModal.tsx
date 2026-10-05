import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Loader2 } from 'lucide-react';
import {
  IVoiceServerConfiguration,
  resolveVoiceProviderLabel,
} from 'models/VoiceServerConfiguration';
import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { formatDate } from 'utils/Helper';
import VoiceServerConfigurationStatusBadge from './VoiceServerConfigurationStatusBadge';

interface VoiceServerConfigurationViewModalProps {
  isOpen: boolean;
  onClose: () => void;
  configurationId: string | null;
  initialConfiguration?: IVoiceServerConfiguration | null;
  getConfigurationById: (
    id: string,
  ) => Promise<IVoiceServerConfiguration | null>;
}

const DetailRow = ({
  label,
  value,
}: {
  label: string;
  value: ReactNode;
}) => (
  <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between sm:gap-4">
    <span className="shrink-0 text-sm text-muted-foreground">{label}</span>
    <span className="break-all text-sm font-medium text-foreground sm:max-w-[60%] sm:text-right">
      {value || '—'}
    </span>
  </div>
);

export const VoiceServerConfigurationViewModal = ({
  isOpen,
  onClose,
  configurationId,
  initialConfiguration = null,
  getConfigurationById,
}: VoiceServerConfigurationViewModalProps) => {
  const [configuration, setConfiguration] =
    useState<IVoiceServerConfiguration | null>(initialConfiguration);
  const [loading, setLoading] = useState(false);

  const loadConfiguration = useCallback(async () => {
    if (!configurationId) return;
    setLoading(!initialConfiguration);
    const data = await getConfigurationById(configurationId);
    if (data) {
      setConfiguration(data);
    } else if (!initialConfiguration) {
      setConfiguration(null);
    }
    setLoading(false);
  }, [configurationId, getConfigurationById, initialConfiguration]);

  useEffect(() => {
    if (isOpen && configurationId) {
      setConfiguration(initialConfiguration);
      const timer = window.setTimeout(() => {
        void loadConfiguration();
      }, 0);
      return () => window.clearTimeout(timer);
    }

    if (!isOpen) {
      setConfiguration(null);
      setLoading(false);
    }
  }, [isOpen, configurationId, initialConfiguration, loadConfiguration]);

  return (
    <Dialog open={isOpen} onOpenChange={open => !open && onClose()}>
      <DialogContent className="max-h-[85vh] w-full max-w-lg overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Voice Server Configuration Details</DialogTitle>
        </DialogHeader>

        {loading && !configuration ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="size-8 animate-spin text-muted-foreground" />
          </div>
        ) : configuration ? (
          <div className="space-y-4 py-2">
            <div className="flex flex-wrap items-center gap-2">
              <h3 className="text-lg font-semibold">{configuration.name}</h3>
              {configuration.default ? (
                <Badge variant="outline">Default</Badge>
              ) : null}
              {configuration.global ? (
                <Badge variant="secondary">Global</Badge>
              ) : null}
              <VoiceServerConfigurationStatusBadge
                status={configuration.status}
              />
            </div>

            <div className="space-y-3 rounded-md border border-border p-4">
              <DetailRow
                label="Provider"
                value={resolveVoiceProviderLabel(configuration.provider)}
              />
              <DetailRow
                label="Caller ID"
                value={configuration.callerId || '—'}
              />
              <DetailRow
                label="Base URL"
                value={configuration.baseUrl || '—'}
              />
              <DetailRow label="Region" value={configuration.region || '—'} />
              <DetailRow
                label="Country"
                value={configuration.countryCode || '—'}
              />
              <DetailRow
                label="API Key"
                value={
                  <span className="font-mono text-xs">
                    {configuration.apiKeyMasked || '—'}
                  </span>
                }
              />
              <DetailRow
                label="API Secret"
                value={
                  <span className="font-mono text-xs">
                    {configuration.apiSecretMasked || '—'}
                  </span>
                }
              />
              <DetailRow
                label="Created"
                value={
                  configuration.createdAt
                    ? formatDate(configuration.createdAt)
                    : '—'
                }
              />
              <DetailRow
                label="Updated"
                value={
                  configuration.updatedAt
                    ? formatDate(configuration.updatedAt)
                    : '—'
                }
              />
            </div>
          </div>
        ) : (
          <p className="py-8 text-center text-sm text-muted-foreground">
            Configuration not found.
          </p>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default VoiceServerConfigurationViewModal;
