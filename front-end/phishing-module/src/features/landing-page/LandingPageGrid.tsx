import { CardSkeleton } from 'components/LoadingSkeleton';
import { ILandingPage } from 'models/LandingPage';
import LandingPageCard from './LandingPageCard';

interface LandingPageGridProps {
  pages: ILandingPage[];
  loading?: boolean;
  onPreview: (page: ILandingPage) => void;
  onEdit: (page: ILandingPage) => void;
  onDuplicate: (page: ILandingPage) => void;
  onDelete: (page: ILandingPage) => void;
}

/**
 * Grid component for displaying landing pages
 * Based on Task-04 Landing Page Library
 */
export const LandingPageGrid = ({
  pages,
  loading = false,
  onPreview,
  onEdit,
  onDuplicate,
  onDelete,
}: LandingPageGridProps) => {
  if (loading) {
    return <CardSkeleton count={12} />;
  }

  if (pages?.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-muted-foreground">
        <svg
          className="mb-4 size-16"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1}
            d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
          />
        </svg>
        <p className="mb-2 text-lg font-medium">No landing pages found</p>
        <p className="text-sm">
          Create a new landing page or adjust your filters
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {pages?.map(page => (
        <LandingPageCard
          key={page.pageId}
          page={page}
          onPreview={onPreview}
          onEdit={onEdit}
          onDuplicate={onDuplicate}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
};

export default LandingPageGrid;
