import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Plus, X } from 'lucide-react';
import { useState } from 'react';
import { cn } from 'utils/Helper';

const PREDEFINED_TAGS = [
  'Security',
  'Awareness',
  'Phishing',
  'Training',
  'Compliance',
  'Cybersecurity',
  'Data Protection',
  'Privacy',
  'Risk Management',
  'Social Engineering',
];

interface TagSelectorProps {
  value: string[];
  onChange: (tags: string[]) => void;
  maxTags?: number;
  error?: string;
  className?: string;
}

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
    <div className={cn('content-space-y-3', className)}>
      {value.length > 0 && (
        <div className="content-flex content-flex-wrap content-gap-2">
          {value.map(tag => (
            <Badge key={tag} variant="default" className="content-gap-1 content-pr-1">
              {tag}
              <button
                type="button"
                onClick={() => handleRemoveTag(tag)}
                className="content-ml-1 content-rounded-full content-p-0.5 hover:content-bg-white/20"
              >
                <X className="content-size-3" />
              </button>
            </Badge>
          ))}
        </div>
      )}

      <div className="content-flex content-gap-2">
        <Input
          value={customTag}
          onChange={e => setCustomTag(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Add custom tag..."
          className="content-flex-1"
          disabled={value.length >= maxTags}
          maxLength={50}
        />
        <Button
          type="button"
          variant="outline"
          onClick={handleAddCustomTag}
          disabled={!customTag.trim() || value.length >= maxTags}
        >
          <Plus className="content-size-4" />
        </Button>
      </div>

      {availablePredefinedTags.length > 0 && value.length < maxTags && (
        <div>
          <p className="content-mb-2 content-text-xs content-text-primary">
            Suggested tags:
          </p>
          <div className="content-flex content-flex-wrap content-gap-2">
            {availablePredefinedTags.map(tag => (
              <button
                key={tag}
                type="button"
                onClick={() => handleAddTag(tag)}
                className="content-rounded-full content-bg-card-background content-px-3 content-py-1 content-text-xs content-text-primary content-transition-colors hover:content-bg-primary/20 hover:content-text-primary"
              >
                + {tag}
              </button>
            ))}
          </div>
        </div>
      )}

      <div className="content-flex content-items-center content-justify-between">
        <span className="content-text-xs content-text-ash-gray">
          {value.length}/{maxTags} tags
        </span>
        {error && (
          <span className="content-text-xs content-text-vibrant-red">
            {error}
          </span>
        )}
      </div>
    </div>
  );
};

export default TagSelector;
