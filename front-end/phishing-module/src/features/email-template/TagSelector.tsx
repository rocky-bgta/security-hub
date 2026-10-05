import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Plus, X } from 'lucide-react';
import { PREDEFINED_TAGS } from 'models/EmailTemplate';
import { useState } from 'react';
import { cn } from 'utils/Helper';

interface TagSelectorProps {
  value: string[];
  onChange: (tags: string[]) => void;
  maxTags?: number;
  error?: string;
  className?: string;
}

/**
 * Tag selector with predefined options and custom tag input
 */
const TagSelector = ({
  value,
  onChange,
  maxTags = 10,
  error,
  className,
}: TagSelectorProps) => {
  const [customTag, setCustomTag] = useState('');

  const handleAddTag = (tag: string) => {
    if (tag && !value.includes(tag) && value.length < maxTags) {
      onChange([...value, tag]);
    }
  };

  const handleRemoveTag = (tag: string) => {
    onChange(value.filter(t => t !== tag));
  };

  const handleAddCustomTag = () => {
    if (customTag.trim()) {
      handleAddTag(customTag.trim());
      setCustomTag('');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddCustomTag();
    }
  };

  const availablePredefinedTags = PREDEFINED_TAGS.filter(
    tag => !value.includes(tag),
  );

  return (
    <div className={cn('space-y-3', className)}>
      {/* Selected tags */}
      {value.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {value.map(tag => (
            <Badge key={tag} variant="default" className="gap-1 pr-1">
              {tag}
              <button
                type="button"
                onClick={() => handleRemoveTag(tag)}
                className="ml-1 rounded-full p-0.5 hover:bg-white/20"
              >
                <X className="size-3" />
              </button>
            </Badge>
          ))}
        </div>
      )}

      {/* Custom tag input */}
      <div className="flex gap-2">
        <Input
          value={customTag}
          onChange={e => setCustomTag(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Add custom tag..."
          className="flex-1"
          disabled={value.length >= maxTags}
          maxLength={50}
        />
        <Button
          type="button"
          variant="outline"
          onClick={handleAddCustomTag}
          disabled={!customTag.trim() || value.length >= maxTags}
        >
          <Plus className="size-4" />
        </Button>
      </div>

      {/* Predefined tags */}
      {availablePredefinedTags.length > 0 && value.length < maxTags && (
        <div>
          <p className="mb-2 text-xs text-primary">Suggested tags:</p>
          <div className="flex flex-wrap gap-2">
            {availablePredefinedTags.map(tag => (
              <button
                key={tag}
                type="button"
                onClick={() => handleAddTag(tag)}
                className="rounded-full bg-card-background px-3 py-1 text-xs text-primary transition-colors hover:bg-primary/20 hover:text-primary"
              >
                + {tag}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Counter and error */}
      <div className="flex items-center justify-between">
        <span className="text-xs text-muted-foreground">
          {value.length}/{maxTags} tags
        </span>
        {error && <span className="text-xs text-vibrant-red">{error}</span>}
      </div>
    </div>
  );
};

export default TagSelector;
