import { Skeleton } from 'components/LoadingSkeleton';
import { IList } from 'models/Global';
import { ISenderProfile } from 'models/SenderProfile';
import SenderProfileCard from './SenderProfileCard';
import { FileWarning } from 'lucide-react';

interface SenderProfileGridProps {
  profiles: IList<ISenderProfile>;
  loading?: boolean;
  onEdit: (profile: ISenderProfile) => void;
  onDuplicate: (profile: ISenderProfile) => void;
  onDelete: (profile: ISenderProfile) => void;
  onTest: (profile: ISenderProfile) => void;
}

/**
 * Grid component for displaying sender profiles
 * Based on Task-06 Sender Profile Management (AC-01)
 */
export const SenderProfileGrid = ({
  profiles,
  loading = false,
  onEdit,
  onDuplicate,
  onDelete,
  onTest,
}: SenderProfileGridProps) => {
  if (loading) {
    return (
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {[...Array(8)].map((_, index) => (
          <Skeleton key={index} className="h-56 rounded-lg" />
        ))}
      </div>
    );
  }

  if (profiles?.items?.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <FileWarning className="mb-4 size-16 text-muted-foreground" />
        <h3 className="mb-2 text-lg font-medium text-primary">
          No Sender Profiles
        </h3>
        <p className="text-muted-foreground">
          Create your first sender profile to get started
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {profiles?.items?.map(profile => (
        <SenderProfileCard
          key={profile.profileId}
          profile={profile}
          onEdit={onEdit}
          onDuplicate={onDuplicate}
          onDelete={onDelete}
          onTest={onTest}
        />
      ))}
    </div>
  );
};

export default SenderProfileGrid;
