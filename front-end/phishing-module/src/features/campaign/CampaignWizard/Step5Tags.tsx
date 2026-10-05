import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { ArrowLeftIcon, ArrowRightIcon, XIcon } from 'lucide-react';
import { useState } from 'react';

interface Step5TagsProps {
  initialTags?: string[];
  onSubmit: (tags: string[]) => void;
  onBack: () => void;
  isLoading?: boolean;
}

const SUGGESTED_TAGS = [
  'Q1 Campaign',
  'Q2 Campaign',
  'Training',
  'Compliance',
  'Executive',
  'IT Department',
  'Finance',
  'HR',
  'Sales',
  'Urgent',
  'Spear Phishing',
  'Credential Harvest',
];

export const Step5Tags = ({
  initialTags = [],
  onSubmit,
  onBack,
  isLoading,
}: Step5TagsProps) => {
  const [tags, setTags] = useState<string[]>(initialTags);
  const [inputValue, setInputValue] = useState('');

  const addTag = (tag: string) => {
    const trimmedTag = tag.trim();
    if (trimmedTag && !tags.includes(trimmedTag) && tags.length < 10) {
      setTags([...tags, trimmedTag]);
      setInputValue('');
    }
  };

  const removeTag = (tagToRemove: string) => {
    setTags(tags.filter(tag => tag !== tagToRemove));
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      addTag(inputValue);
    }
  };

  const handleSubmit = () => {
    onSubmit(tags);
  };

  return (
    <div className="mx-auto max-w-2xl">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground">Campaign Tags</h2>
        <p className="mt-2 text-muted-foreground">
          Add tags to categorize and organize your campaign (optional).
        </p>
      </div>

      {/* Tag Input */}
      <div className="mb-6">
        <label className="mb-2 block text-sm font-medium text-foreground">
          Tags (max 10)
        </label>
        <div>
          <div className="mb-2 flex flex-wrap gap-2">
            {tags.map(tag => (
              <span
                key={tag}
                className="inline-flex items-center rounded-full bg-primary/10 px-4 py-1 text-sm text-primary"
              >
                {tag}
                <button
                  type="button"
                  onClick={() => removeTag(tag)}
                  className="ml-2 hover:text-primary/80"
                >
                  <XIcon className="size-3" />
                </button>
              </span>
            ))}
          </div>
          <Input
            type="text"
            value={inputValue}
            onChange={e => setInputValue(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={
              tags.length >= 10
                ? 'Maximum tags reached'
                : 'Type and press Enter to add...'
            }
            disabled={tags.length >= 10}
          />
        </div>
        <p className="mt-1 text-xs text-muted-foreground">
          Press Enter or comma to add a tag. {tags.length}/10 tags used.
        </p>
      </div>

      {/* Suggested Tags */}
      <div className="mb-8">
        <label className="mb-2 block text-sm font-medium text-foreground">
          Suggested Tags
        </label>
        <div className="flex flex-wrap gap-2">
          {SUGGESTED_TAGS.filter(tag => !tags.includes(tag)).map(tag => (
            <Button
              variant="outline"
              key={tag}
              onClick={() => addTag(tag)}
              disabled={tags.length >= 10}
              className="rounded-full bg-card-background px-3 py-1 text-sm text-foreground hover:bg-card-border disabled:cursor-not-allowed disabled:opacity-50"
            >
              + {tag}
            </Button>
          ))}
        </div>
      </div>

      {/* Actions */}
      <div className="flex justify-between border-t border-card-border pt-6">
        <Button type="button" variant="outline" onClick={onBack}>
          <ArrowLeftIcon />
          Back
        </Button>
        <Button variant="default" onClick={handleSubmit} disabled={isLoading}>
          {isLoading ? 'Saving...' : 'Save & Continue'}
          <ArrowRightIcon />
        </Button>
      </div>
    </div>
  );
};
