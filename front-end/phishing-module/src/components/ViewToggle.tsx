import { LayoutGrid, List } from 'lucide-react';

import { Button } from 'common/Button';
import { cn } from 'utils/Helper';

export type ViewMode = 'grid' | 'table';

interface ViewToggleProps {
  value: ViewMode;
  onChange: (mode: ViewMode) => void;
  className?: string;
}

/**
 * Toggle component for switching between Grid and Table views
 */
const ViewToggle = ({ value, onChange, className }: ViewToggleProps) => {
  return (
    <div className={cn('flex rounded-md border border-card-border', className)}>
      <Button
        variant={value === 'grid' ? 'default' : 'ghost'}
        size="sm"
        onClick={() => onChange('grid')}
        className="rounded-r-none"
        title="Grid View"
      >
        <LayoutGrid className="size-4" />
      </Button>
      <Button
        variant={value === 'table' ? 'default' : 'ghost'}
        size="sm"
        onClick={() => onChange('table')}
        className="rounded-l-none"
        title="Table View"
      >
        <List className="size-4" />
      </Button>
    </div>
  );
};

export default ViewToggle;
