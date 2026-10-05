import { Skeleton } from 'components/LoadingSkeleton';
import { FileWarning } from 'lucide-react';
import { IList } from 'models/Global';
import { ISmsServerConfiguration } from 'models/SmsServerConfiguration';
import SmsServerConfigurationCard from './SmsServerConfigurationCard';

interface SmsServerConfigurationGridProps {
  configurations: IList<ISmsServerConfiguration>;
  loading?: boolean;
  onView: (configuration: ISmsServerConfiguration) => void;
  onEdit: (configuration: ISmsServerConfiguration) => void;
  onDelete: (configuration: ISmsServerConfiguration) => void;
}

export const SmsServerConfigurationGrid = ({
  configurations,
  loading = false,
  onView,
  onEdit,
  onDelete,
}: SmsServerConfigurationGridProps) => {
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
          No SMS Server Configurations
        </h3>
        <p className="text-muted-foreground">
          Create your first SMS server configuration to get started
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {configurations?.items?.map(configuration => (
        <SmsServerConfigurationCard
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

export default SmsServerConfigurationGrid;
