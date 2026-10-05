import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type FocusEvent,
  type PointerEvent,
} from 'react';

import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { cn } from 'utils/Helper';

interface IProps {
  items: Array<{ value: string; label: string }>;
  value: string | undefined;
  onValueChange: (value: string) => void;
  placeholder?: string;
  hasError?: boolean;
  disabled?: boolean;
}

const SearchSelect = ({
  items,
  value,
  onValueChange,
  placeholder = 'Select...',
  hasError = false,
  disabled = false,
}: IProps) => {
  const [open, setOpen] = useState<boolean>(false);
  const [search, setSearch] = useState<string>('');
  const inputRef = useRef<HTMLInputElement>(null);
  const selectedItemRef = useRef<HTMLDivElement>(null);

  const filteredItems = useMemo(
    () =>
      items.filter(item =>
        item.label.toLowerCase().includes(search.toLowerCase()),
      ),
    [items, search],
  );

  useEffect(() => {
    if (!open) return;

    const timeout = setTimeout(() => {
      inputRef.current?.focus();

      if (value && !search) {
        selectedItemRef.current?.scrollIntoView({ block: 'center' });
      }
    }, 10);

    return () => clearTimeout(timeout);
  }, [open, search, value]);

  const handleValueChange = (newValue: string) => {
    onValueChange(newValue);
    setSearch('');
  };

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen);

    if (!isOpen) {
      setSearch('');
    }
  };

  const handleSearchPointerDown = (event: PointerEvent<HTMLInputElement>) => {
    event.preventDefault();
    event.stopPropagation();
    event.currentTarget.focus();
  };

  const handleContentFocusIn = (event: FocusEvent<HTMLDivElement>) => {
    if (event.target === inputRef.current) {
      return;
    }

    inputRef.current?.focus();
  };

  const handleItemPointerDown = (event: PointerEvent<HTMLDivElement>) => {
    event.preventDefault();
  };

  return (
    <Select
      open={open}
      value={value}
      onValueChange={handleValueChange}
      onOpenChange={handleOpenChange}
      disabled={disabled}
    >
      <SelectTrigger className={cn('content-w-full', hasError && 'content-has-error')}>
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent
        onFocusCapture={handleContentFocusIn}
        searchHeader={
          <div className="content-border-b content-bg-secondary content-p-1">
            <Input
              ref={inputRef}
              type="text"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search..."
              className="content-border-none focus-visible:!content-ring-transparent"
              autoFocus
              onKeyDown={e => e.stopPropagation()}
              onPointerDown={handleSearchPointerDown}
            />
          </div>
        }
      >
        {filteredItems.length > 0 ? (
          filteredItems.map(item => (
            <SelectItem
              key={item.value}
              ref={item.value === value ? selectedItemRef : undefined}
              value={item.value}
              onPointerDown={handleItemPointerDown}
            >
              {item.label}
            </SelectItem>
          ))
        ) : (
          <p className="content-p-2 content-text-sm">No results found</p>
        )}
      </SelectContent>
    </Select>
  );
};

export default SearchSelect;
