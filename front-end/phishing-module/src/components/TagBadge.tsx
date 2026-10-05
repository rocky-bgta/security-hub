import { cn } from 'utils/Helper';

interface TagBadgeProps {
  tag: string;
  className?: string;
}

/**
 * Small pill badge for displaying tags
 */
const TagBadge = ({ tag, className }: TagBadgeProps) => {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full bg-primary/20 px-2 py-0.5 text-xs text-primary',
        className,
      )}
    >
      {tag}
    </span>
  );
};

export default TagBadge;
