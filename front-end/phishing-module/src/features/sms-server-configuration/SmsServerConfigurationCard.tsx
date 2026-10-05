import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card } from 'common/Card';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/Dropdown';
import { MoreHorizontal } from 'lucide-react';
import {
  ISmsServerConfiguration,
  getSmsProviderLabel,
} from 'models/SmsServerConfiguration';
import SmsServerConfigurationStatusBadge from './SmsServerConfigurationStatusBadge';

interface SmsServerConfigurationCardProps {
  configuration: ISmsServerConfiguration;
  onView: (configuration: ISmsServerConfiguration) => void;
  onEdit: (configuration: ISmsServerConfiguration) => void;
  onDelete: (configuration: ISmsServerConfiguration) => void;
}

export const SmsServerConfigurationCard = ({
  configuration,
  onView,
  onEdit,
  onDelete,
}: SmsServerConfigurationCardProps) => {
  const actions = [
    { label: 'View', onClick: () => onView(configuration) },
    { label: 'Edit', onClick: () => onEdit(configuration) },
    {
      label: 'Delete',
      onClick: () => onDelete(configuration),
      danger: true,
    },
  ];

  return (
    <Card>
      <div className="border-b border-card-border p-4">
        <div className="flex items-start justify-between">
          <div className="min-w-0 flex-1">
            <div className="mb-1 flex flex-wrap items-center gap-2">
              <h3 className="truncate text-base font-medium text-card-foreground">
                {configuration.name}
              </h3>
              {configuration.default && (
                <Badge variant="outline" className="text-xs">
                  Default
                </Badge>
              )}
            </div>
            <p className="truncate text-sm text-card-foreground/50">
              {getSmsProviderLabel(configuration.provider)}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <SmsServerConfigurationStatusBadge status={configuration.status} />
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="size-8">
                  <MoreHorizontal className="size-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                {actions.map(action => (
                  <DropdownMenuItem
                    key={action.label}
                    onSelect={() => action.onClick()}
                  >
                    {action.label}
                  </DropdownMenuItem>
                ))}
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      </div>

      <div className="space-y-2 p-4 text-sm">
        <div className="flex justify-between gap-2">
          <span className="text-muted-foreground">Sender ID</span>
          <span className="truncate font-medium">{configuration.senderId}</span>
        </div>
        <div className="flex justify-between gap-2">
          <span className="text-muted-foreground">Base URL</span>
          <span className="truncate font-medium" title={configuration.baseUrl}>
            {configuration.baseUrl}
          </span>
        </div>
        <div className="flex justify-between gap-2">
          <span className="text-muted-foreground">API Key</span>
          <span className="truncate font-mono text-xs">
            {configuration.apiKeyMasked}
          </span>
        </div>
      </div>
    </Card>
  );
};

export default SmsServerConfigurationCard;
