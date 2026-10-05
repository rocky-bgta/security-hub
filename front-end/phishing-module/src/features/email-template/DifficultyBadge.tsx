import { Badge } from 'common/Badge';
import {
  DifficultyLevel,
  IIdName,
} from 'models/EmailTemplate';
import { cn } from 'utils/Helper';

interface DifficultyBadgeProps {
  level: DifficultyLevel | IIdName;
  className?: string;
}

/**
 * Badge component for displaying difficulty level
 */
const DifficultyBadge = ({ level, className }: DifficultyBadgeProps) => {
  const levelKey =
    typeof level === 'object' ? (level.id as DifficultyLevel) : level;
  const label =
    typeof level === 'object' ? level.name : level;

  const getVariant = () => {
    switch (levelKey) {
      case DifficultyLevel.BEGINNER:
        return 'default';
      case DifficultyLevel.INTERMEDIATE:
        return 'warning';
      case DifficultyLevel.ADVANCED:
        return 'destructive';
      case DifficultyLevel.SPEAR_PHISHING:
        return 'secondary';
      default:
        return 'secondary';
    }
  };

  return (
    <Badge variant={getVariant()} className={cn('text-xs', className)}>
      {label}
    </Badge>
  );
};

export default DifficultyBadge;
