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
  ISmsServerConfiguration,
  getSmsProviderLabel,
} from 'models/SmsServerConfiguration';
import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { formatDate } from 'utils/Helper';
import SmsServerConfigurationStatusBadge from './SmsServerConfigurationStatusBadge';

interface SmsServerConfigurationViewModalProps {
  isOpen: boolean;
  onClose: () => void;
  configurationId: string | null;
  getConfigurationById: (
    id: string,
  ) => Promise<ISmsServerConfiguration | null>;
}

const DetailRow = ({
  label,
  value,
}: {
  label: string;
  value: ReactNode;
}) => (
  <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
    <span className="text-sm text-muted-foreground">{label}</span>
    <span className="text-sm font-medium text-foreground sm:max-w-[60%] sm:text-right">
      {value}
    </span>
  </div>
);

export const SmsServerConfigurationViewModal = ({
  isOpen,
  onClose,
  configurationId,
  getConfigurationById,
}: SmsServerConfigurationViewModalProps) => {
  const [configuration, setConfiguration] =
    useState<ISmsServerConfiguration | null>(null);
  const [loading, setLoading] = useState(false);

  const loadConfiguration = useCallback(async () => {
    if (!configurationId) return;
    setLoading(true);
    const data = await getConfigurationById(configurationId);
    setConfiguration(data);
    setLoading(false);
  }, [configurationId, getConfigurationById]);

  useEffect(() => {
    if (isOpen && configurationId) {
      const timer = window.setTimeout(() => {
        void loadConfiguration();
      }, 0);
      return () => window.clearTimeout(timer);
    }

    if (!isOpen) {
      setConfiguration(null);
    }
  }, [isOpen, configurationId, loadConfiguration]);


  return (
    <Dialog open={isOpen} onOpenChange={open => !open && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>SMS Server Configuration Details</DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="size-8 animate-spin text-muted-foreground" />
          </div>
        ) : configuration ? (
          <div className="space-y-4 py-2">
            <div className="flex flex-wrap items-center gap-2">
              <h3 className="text-lg font-semibold">{configuration.name}</h3>
              {configuration.default && (
                <Badge variant="outline">Default</Badge>
              )}
              <SmsServerConfigurationStatusBadge status={configuration.status} />
            </div>

            <div className="space-y-3 rounded-md border border-border p-4">
              <DetailRow
                label="Provider"
                value={getSmsProviderLabel(configuration.provider)}
              />
              <DetailRow label="Sender ID" value={configuration.senderId} />
              <DetailRow label="Base URL" value={configuration.baseUrl} />
              <DetailRow
                label="API Key"
                value={
                  <span className="font-mono text-xs">
                    {configuration.apiKeyMasked}
                  </span>
                }
              />
              <DetailRow
                label="API Secret"
                value={
                  <span className="font-mono text-xs">
                    {configuration.apiSecretMasked}
                  </span>
                }
              />
              <DetailRow
                label="Created"
                value={formatDate(configuration.createdAt)}
              />
              <DetailRow
                label="Updated"
                value={formatDate(configuration.updatedAt)}
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

export default SmsServerConfigurationViewModal;
