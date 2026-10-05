import { Button } from 'common/Button';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import useDropDown from 'hooks/UseDropDown';
import { Loader2, X } from 'lucide-react';
import { IDropdownItem } from 'models/DropDown';
import {
  LandingPageStatusFilter,
  LandingPageType,
  getLandingPageStatusLabel,
  getLandingPageTypeLabel,
} from 'models/LandingPage';
import { RefObject, useEffect, useState } from 'react';

export interface LandingPageFilterValues {
  pageType?: string;
  category?: string;
  difficulty?: string;
  tags?: string[];
  status?: LandingPageStatusFilter;
}

interface LandingPageFilterPanelProps {
  isExpanded: boolean;
  filters: LandingPageFilterValues;
  tagOptions: string[];
  onFilterChange: (filters: LandingPageFilterValues) => void;
  onClose: () => void;
  containerRef: RefObject<HTMLElement | null>;
}

function isRadixPortaledPointerTarget(target: EventTarget | null): boolean {
  return (
    target instanceof Element &&
    Boolean(
      target.closest('[data-radix-select-content]') ||
        target.closest('[data-radix-popper-content-wrapper]'),
    )
  );
}

function isRadixSelectOpen(): boolean {
  return Boolean(
    document.querySelector('[data-radix-select-content]') ||
      document.querySelector('[data-radix-popper-content-wrapper]'),
  );
}

function optionLabel(items: IDropdownItem[], id?: string): string {
  if (!id) return '';
  return items.find(item => item.id === id)?.name ?? id;
}

const ALL_VALUE = 'all';

const STATUSES: LandingPageStatusFilter[] = ['ACTIVE', 'INACTIVE', 'DRAFT'];

/**
 * Filter panel component for landing page list
 * Based on Task-04 Landing Page Library
 */
export const LandingPageFilterPanel = ({
  isExpanded,
  filters,
  tagOptions,
  onFilterChange,
  onClose,
  containerRef,
}: LandingPageFilterPanelProps) => {
  const { fetchLandingPageCategories, fetchDifficulties } = useDropDown();
  const pageTypes = Object.values(LandingPageType);

  const [categories, setCategories] = useState<IDropdownItem[]>([]);
  const [difficulties, setDifficulties] = useState<IDropdownItem[]>([]);
  const [optionsLoading, setOptionsLoading] = useState(true);

  const [draft, setDraft] = useState<LandingPageFilterValues>(() => ({
    ...filters,
    tags: filters.tags ? [...filters.tags] : undefined,
  }));
  const [syncedFilters, setSyncedFilters] = useState(filters);

  if (filters !== syncedFilters) {
    setSyncedFilters(filters);
    setDraft({
      ...filters,
      tags: filters.tags ? [...filters.tags] : undefined,
    });
  }

  useEffect(() => {
    let cancelled = false;

    const loadOptions = async () => {
      setOptionsLoading(true);
      try {
        const [categoryData, difficultyData] = await Promise.all([
          fetchLandingPageCategories(),
          fetchDifficulties(),
        ]);
        if (cancelled) return;
        setCategories(categoryData.items);
        setDifficulties(difficultyData.items);
      } finally {
        if (!cancelled) {
          setOptionsLoading(false);
        }
      }
    };

    void loadOptions();

    return () => {
      cancelled = true;
    };
  }, [fetchLandingPageCategories, fetchDifficulties]);

  useEffect(() => {
    if (!isExpanded || !containerRef?.current) return;

    const onPointerDown = (event: PointerEvent) => {
      const t = event.target;
      if (!(t instanceof Node)) return;
      if (containerRef.current?.contains(t)) return;
      if (isRadixPortaledPointerTarget(t)) return;
      if (isRadixSelectOpen()) return;
      onClose();
    };

    document.addEventListener('pointerdown', onPointerDown, true);
    return () =>
      document.removeEventListener('pointerdown', onPointerDown, true);
  }, [isExpanded, onClose, containerRef]);

  const appliedSnapshot = (): LandingPageFilterValues => ({
    ...filters,
    tags: filters.tags ? [...filters.tags] : undefined,
  });

  const handleApply = () => {
    onFilterChange({
      ...draft,
      tags: draft.tags && draft.tags.length > 0 ? [...draft.tags] : undefined,
    });
    onClose();
  };

  const toggleDraftTag = (tag: string) => {
    setDraft(prev => {
      const current = prev.tags ?? [];
      const nextTags = current.includes(tag)
        ? current.filter(t => t !== tag)
        : [...current, tag];
      return {
        ...prev,
        tags: nextTags.length > 0 ? nextTags : undefined,
      };
    });
  };

  const hasActiveFilters =
    Boolean(filters.pageType) ||
    Boolean(filters.category) ||
    Boolean(filters.difficulty) ||
    Boolean(filters.status) ||
    (filters.tags && filters.tags.length > 0);

  return (
    <div className="absolute right-0 top-full z-50 mt-2 w-full">
      {isExpanded && (
        <div className="mb-6 rounded-lg border border-card-border bg-card-background p-4 shadow-sm">
          <div className="flex flex-wrap gap-4">
            {/* Page Type Filter */}
            <div className="min-w-[180px] flex-1 space-y-2">
              <Label>Page Type</Label>
              <Select
                value={draft.pageType || ALL_VALUE}
                onValueChange={value =>
                  setDraft(prev => ({
                    ...prev,
                    pageType: value === ALL_VALUE ? undefined : value,
                  }))
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL_VALUE}>All Types</SelectItem>
                  {pageTypes.map(type => (
                    <SelectItem key={type} value={type}>
                      {getLandingPageTypeLabel(type)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Category Filter */}
            <div className="min-w-[180px] flex-1 space-y-2">
              <Label>Category</Label>
              <Select
                value={draft.category || ALL_VALUE}
                onValueChange={value =>
                  setDraft(prev => ({
                    ...prev,
                    category: value === ALL_VALUE ? undefined : value,
                  }))
                }
                disabled={optionsLoading}
              >
                <SelectTrigger>
                  <SelectValue
                    placeholder={
                      optionsLoading
                        ? 'Loading categories...'
                        : 'Select a category'
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL_VALUE}>All Categories</SelectItem>
                  {categories.map(cat => (
                    <SelectItem key={cat.id} value={cat.id}>
                      {cat.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Difficulty Level Filter */}
            <div className="min-w-[180px] flex-1 space-y-2">
              <Label>Difficulty Level</Label>
              <div className="flex flex-col gap-2">
                {optionsLoading ? (
                  <div className="flex items-center gap-2 py-2 text-sm text-muted-foreground">
                    <Loader2 className="size-4 animate-spin" />
                    Loading...
                  </div>
                ) : (
                  <RadioGroup
                    value={draft.difficulty || ALL_VALUE}
                    onValueChange={value =>
                      setDraft(prev => ({
                        ...prev,
                        difficulty: value === ALL_VALUE ? undefined : value,
                      }))
                    }
                    className="flex flex-col space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        value={ALL_VALUE}
                        id="landing-page-difficulty-all"
                      />
                      <Label htmlFor="landing-page-difficulty-all">All</Label>
                    </div>
                    {difficulties.map(level => (
                      <div
                        key={level.id}
                        className="flex items-center space-x-2"
                      >
                        <RadioGroupItem
                          value={level.id}
                          id={`landing-page-difficulty-${level.id}`}
                        />
                        <Label
                          htmlFor={`landing-page-difficulty-${level.id}`}
                          className="capitalize"
                        >
                          {level.name}
                        </Label>
                      </div>
                    ))}
                  </RadioGroup>
                )}
              </div>
            </div>

            {/* Status Filter */}
            <div className="min-w-[180px] flex-1 space-y-2">
              <Label>Status</Label>
              <Select
                value={draft.status || ALL_VALUE}
                onValueChange={value =>
                  setDraft(prev => ({
                    ...prev,
                    status:
                      value === ALL_VALUE
                        ? undefined
                        : (value as LandingPageStatusFilter),
                  }))
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL_VALUE}>All Statuses</SelectItem>
                  {STATUSES.map(s => (
                    <SelectItem key={s} value={s}>
                      {getLandingPageStatusLabel(s)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Tags */}
            <div className="min-w-[220px] flex-[2] space-y-2">
              <Label>Tags</Label>
              {tagOptions.length > 0 ? (
                <div className="flex max-h-28 flex-wrap gap-1 overflow-y-auto">
                  {tagOptions.map(tag => (
                    <button
                      key={tag}
                      type="button"
                      onClick={() => toggleDraftTag(tag)}
                      className={`rounded-full px-2 py-1 text-xs transition-colors ${
                        (draft.tags ?? []).includes(tag)
                          ? 'bg-primary text-white'
                          : 'bg-foreground/10 text-foreground hover:bg-foreground/20'
                      }`}
                    >
                      {tag}
                    </button>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-muted-foreground">
                  No tags in the current result set. Create pages with tags to
                  filter by them.
                </p>
              )}
            </div>
          </div>

          <div className="mt-4 flex justify-end border-t border-card-border pt-3">
            <Button type="button" onClick={handleApply}>
              Apply
            </Button>
          </div>

          {hasActiveFilters && (
            <div className="mt-3 flex flex-wrap items-center gap-2 border-t border-card-border pt-3">
              <span className="text-xs text-muted-foreground">
                Active filters:
              </span>
              {filters.pageType && (
                <span className="inline-flex items-center gap-1 rounded-full bg-blue-100 px-2 py-1 text-xs text-blue-800">
                  Type:{' '}
                  {getLandingPageTypeLabel(filters.pageType as LandingPageType)}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onFilterChange({
                        ...appliedSnapshot(),
                        pageType: undefined,
                      })
                    }
                    className="h-max !bg-transparent p-1 text-blue-600 hover:text-blue-800"
                  >
                    <X className="size-3" />
                  </Button>
                </span>
              )}
              {filters.category && (
                <span className="inline-flex items-center gap-1 rounded-full bg-purple-100 px-2 py-1 text-xs text-purple-800">
                  Category: {optionLabel(categories, filters.category)}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onFilterChange({
                        ...appliedSnapshot(),
                        category: undefined,
                      })
                    }
                    className="h-max !bg-transparent p-1 text-purple-600 hover:text-purple-800"
                  >
                    <X className="size-3" />
                  </Button>
                </span>
              )}
              {filters.difficulty && (
                <span className="inline-flex items-center gap-1 rounded-full bg-green-100 px-2 py-1 text-xs text-green-800">
                  Difficulty: {optionLabel(difficulties, filters.difficulty)}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onFilterChange({
                        ...appliedSnapshot(),
                        difficulty: undefined,
                      })
                    }
                    className="h-max !bg-transparent p-1 text-green-600 hover:text-green-800"
                  >
                    <X className="size-3" />
                  </Button>
                </span>
              )}
              {filters.status && (
                <span className="inline-flex items-center gap-1 rounded-full bg-amber-100 px-2 py-1 text-xs text-amber-900">
                  Status: {getLandingPageStatusLabel(filters.status)}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onFilterChange({
                        ...appliedSnapshot(),
                        status: undefined,
                      })
                    }
                    className="h-max !bg-transparent p-1 text-amber-700 hover:text-amber-900"
                  >
                    <X className="size-3" />
                  </Button>
                </span>
              )}
              {(filters.tags ?? []).map(tag => (
                <span
                  key={tag}
                  className="inline-flex items-center gap-1 rounded-full bg-cyan-100 px-2 py-1 text-xs text-cyan-900"
                >
                  Tag: {tag}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => {
                      const nextTags = (filters.tags ?? []).filter(
                        t => t !== tag,
                      );
                      onFilterChange({
                        ...appliedSnapshot(),
                        tags: nextTags.length > 0 ? nextTags : undefined,
                      });
                    }}
                    className="h-max !bg-transparent p-1 text-cyan-700 hover:text-cyan-900"
                  >
                    <X className="size-3" />
                  </Button>
                </span>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default LandingPageFilterPanel;
