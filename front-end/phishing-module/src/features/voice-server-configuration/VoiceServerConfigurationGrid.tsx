import { Skeleton } from 'components/LoadingSkeleton';
import { FileWarning } from 'lucide-react';
import { IList } from 'models/Global';
import { IVoiceServerConfiguration } from 'models/VoiceServerConfiguration';
import VoiceServerConfigurationCard from './VoiceServerConfigurationCard';

interface VoiceServerConfigurationGridProps {
  configurations: IList<IVoiceServerConfiguration>;
  loading?: boolean;
  onView: (configuration: IVoiceServerConfiguration) => void;
  onEdit: (configuration: IVoiceServerConfiguration) => void;
  onDelete: (configuration: IVoiceServerConfiguration) => void;
}

export const VoiceServerConfigurationGrid = ({
  configurations,
  loading = false,
  onView,
  onEdit,
  onDelete,
}: VoiceServerConfigurationGridProps) => {
  if (loading) {
    return (
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {[...Array(8)].map((_, index) => (
          <Skeleton key={index} className="h-56 rounded-lg" />
        ))}
      </div>
    );
  }

  if (configurations?.items?.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <FileWarning className="mb-4 size-16 text-muted-foreground" />
        <h3 className="mb-2 text-lg font-medium text-primary">
          No Voice Server Configurations
        </h3>
        <p className="text-muted-foreground">
          Create your first voice server configuration to get started
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {configurations?.items?.map(configuration => (
        <VoiceServerConfigurationCard
          key={configuration.id}
          configuration={configuration}
          onView={onView}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
};

export default VoiceServerConfigurationGrid;
