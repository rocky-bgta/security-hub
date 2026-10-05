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
import { CampaignChannel } from 'models/Campaign';
import { IDropdownItem } from 'models/DropDown';
import {
  EmailTemplateStatusFilter,
  getEmailTemplateStatusLabel,
  IEmailTemplateListFilters,
  IFilterOptions,
  TemplateType,
} from 'models/EmailTemplate';
import { RefObject, useEffect, useState } from 'react';

export type EmailTemplateDraftFilters = IEmailTemplateListFilters;

interface EmailTemplateFilterPanelProps {
  isExpanded: boolean;
  templateType: TemplateType;
  filterOptions: IFilterOptions | null;
  applied: IEmailTemplateListFilters;
  onApply: (filters: IEmailTemplateListFilters) => void;
  onRemoveApplied: (filters: IEmailTemplateListFilters) => void;
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

function templateTypeToChannel(templateType: TemplateType): CampaignChannel {
  return templateType === TemplateType.SMS
    ? CampaignChannel.SMS
    : CampaignChannel.EMAIL;
}

function optionLabel(items: IDropdownItem[], id?: string): string {
  if (!id) return '';
  return items.find(item => item.id === id)?.name ?? id;
}

const ALL_VALUE = 'all';

/**
 * Filter panel component for email template filtering
 */
const EmailTemplateFilterPanel = ({
  isExpanded,
  templateType,
  filterOptions,
  applied,
  onApply,
  onRemoveApplied,
  onClose,
  containerRef,
}: EmailTemplateFilterPanelProps) => {
  const { fetchDifficulties, fetchPayloadTypes } = useDropDown();
  const [difficulties, setDifficulties] = useState<IDropdownItem[]>([]);
  const [payloadTypes, setPayloadTypes] = useState<IDropdownItem[]>([]);
  const [optionsLoading, setOptionsLoading] = useState(true);
  const [draft, setDraft] = useState<IEmailTemplateListFilters>(() => ({
    ...applied,
    tags: [...applied.tags],
  }));
  const [syncedApplied, setSyncedApplied] = useState(applied);

  if (applied !== syncedApplied) {
    setSyncedApplied(applied);
    setDraft({
      ...applied,
      tags: [...applied.tags],
    });
  }

  useEffect(() => {
    let cancelled = false;

    const loadOptions = async () => {
      setOptionsLoading(true);
      try {
        const [difficultyData, payloadData] = await Promise.all([
          fetchDifficulties(),
          fetchPayloadTypes(templateTypeToChannel(templateType)),
        ]);
        if (cancelled) return;
        setDifficulties(difficultyData.items);
        setPayloadTypes(payloadData.items);
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
  }, [fetchDifficulties, fetchPayloadTypes, templateType]);

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
    return () => document.removeEventListener('pointerdown', onPointerDown, true);
  }, [isExpanded, onClose, containerRef]);

  const toggleTag = (tag: string) => {
    setDraft(prev => {
      const nextTags = prev.tags.includes(tag)
        ? prev.tags.filter(t => t !== tag)
        : [...prev.tags, tag];
      return { ...prev, tags: nextTags };
    });
  };

  const handleApply = () => {
    onApply({
      difficulty: draft.difficulty,
      payloadType: draft.payloadType,
      location: draft.location,
      tags: [...draft.tags],
      language: draft.language,
      status: draft.status,
    });
    onClose();
  };

  const appliedSnapshot = (): IEmailTemplateListFilters => ({
    difficulty: applied.difficulty,
    payloadType: applied.payloadType,
    location: applied.location,
    tags: [...applied.tags],
    language: applied.language,
    status: applied.status,
  });

  const hasActiveFilters =
    Boolean(applied.difficulty) ||
    Boolean(applied.payloadType) ||
    Boolean(applied.location) ||
    Boolean(applied.language) ||
    Boolean(applied.status) ||
    applied.tags.length > 0;

  return (
    <div className="absolute right-0 top-full z-10 mt-2 w-full">
      {isExpanded && (
        <div className="mb-6 rounded-lg border border-card-border bg-card-background p-4 shadow-sm">
          <div className="flex flex-wrap gap-4">
            {/* Difficulty Level */}
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
                        id="email-template-difficulty-all"
                      />
                      <Label htmlFor="email-template-difficulty-all">All</Label>
                    </div>
                    {difficulties.map(level => (
                      <div
                        key={level.id}
                        className="flex items-center space-x-2"
                      >
                        <RadioGroupItem
                          value={level.id}
                          id={`email-template-difficulty-${level.id}`}
                        />
                        <Label
                          htmlFor={`email-template-difficulty-${level.id}`}
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

            {/* Payload Type */}
            <div className="min-w-[180px] flex-1 space-y-2">
              <Label>Payload Type</Label>
              <div className="flex flex-col gap-2">
                {optionsLoading ? (
                  <div className="flex items-center gap-2 py-2 text-sm text-muted-foreground">
                    <Loader2 className="size-4 animate-spin" />
                    Loading...
                  </div>
                ) : (
                  <RadioGroup
                    value={draft.payloadType || ALL_VALUE}
                    onValueChange={value =>
                      setDraft(prev => ({
                        ...prev,
                        payloadType: value === ALL_VALUE ? undefined : value,
                      }))
                    }
                    className="flex flex-col space-y-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        value={ALL_VALUE}
                        id="email-template-payload-all"
                      />
                      <Label htmlFor="email-template-payload-all">All</Label>
                    </div>
                    {payloadTypes.map(type => (
                      <div key={type.id} className="flex items-center space-x-2">
                        <RadioGroupItem
                          value={type.id}
                          id={`email-template-payload-${type.id}`}
                        />
                        <Label
                          htmlFor={`email-template-payload-${type.id}`}
                          className="capitalize"
                        >
                          {type.name}
                        </Label>
                      </div>
                    ))}
                  </RadioGroup>
                )}
              </div>
            </div>

            {/* Location */}
            <div className="min-w-[180px] flex-1 space-y-2">
              <Label>Location</Label>
              <Select
                value={draft.location || ALL_VALUE}
                onValueChange={value =>
                  setDraft(prev => ({
                    ...prev,
                    location: value === ALL_VALUE ? undefined : value,
                  }))
                }
              >
                <SelectTrigger className="w-40">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL_VALUE}>All Locations</SelectItem>
                  {filterOptions?.locations.map(loc => (
                    <SelectItem key={loc} value={loc}>
                      {loc}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Language */}
            <div className="min-w-[180px] flex-1 space-y-2">
              <Label>Language</Label>
              <Select
                value={draft.language || ALL_VALUE}
                onValueChange={value =>
                  setDraft(prev => ({
                    ...prev,
                    language: value === ALL_VALUE ? undefined : value,
                  }))
                }
              >
                <SelectTrigger className="w-40">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL_VALUE}>All Languages</SelectItem>
                  {filterOptions?.languages.map(lang => (
                    <SelectItem key={lang} value={lang}>
                      {lang.toUpperCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Status */}
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
                        : (value as EmailTemplateStatusFilter),
                  }))
                }
              >
                <SelectTrigger className="w-40">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL_VALUE}>All Statuses</SelectItem>
                  {(
                    [
                      'ACTIVE',
                      'INACTIVE',
                      'DRAFT',
                    ] as EmailTemplateStatusFilter[]
                  ).map(s => (
                    <SelectItem key={s} value={s}>
                      {getEmailTemplateStatusLabel(s)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Tags */}
            <div className="min-w-[180px] flex-1 space-y-2 lg:col-span-3">
              <Label>Tags</Label>
              <div className="flex flex-wrap gap-1">
                {filterOptions?.tags.map(tag => (
                  <button
                    key={tag}
                    type="button"
                    onClick={() => toggleTag(tag)}
                    className={`rounded-full px-2 py-1 text-xs transition-colors ${
                      draft.tags.includes(tag)
                        ? 'bg-primary text-white'
                        : 'bg-foreground/10 text-foreground hover:bg-foreground/20'
                    }`}
                  >
                    {tag}
                  </button>
                ))}
              </div>
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
              {applied.difficulty && (
                <span className="inline-flex items-center gap-1 rounded-full bg-green-100 px-2 py-1 text-xs text-green-800">
                  Difficulty: {optionLabel(difficulties, applied.difficulty)}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onRemoveApplied({
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
              {applied.payloadType && (
                <span className="inline-flex items-center gap-1 rounded-full bg-blue-100 px-2 py-1 text-xs text-blue-800">
                  Payload: {optionLabel(payloadTypes, applied.payloadType)}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onRemoveApplied({
                        ...appliedSnapshot(),
                        payloadType: undefined,
                      })
                    }
                    className="h-max !bg-transparent p-1 text-blue-600 hover:text-blue-800"
                  >
                    <X className="size-3" />
                  </Button>
                </span>
              )}
              {applied.location && (
                <span className="inline-flex items-center gap-1 rounded-full bg-purple-100 px-2 py-1 text-xs text-purple-800">
                  Location: {applied.location}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onRemoveApplied({
                        ...appliedSnapshot(),
                        location: undefined,
                      })
                    }
                    className="h-max !bg-transparent p-1 text-purple-600 hover:text-purple-800"
                  >
                    <X className="size-3" />
                  </Button>
                </span>
              )}
              {applied.language && (
                <span className="inline-flex items-center gap-1 rounded-full bg-cyan-100 px-2 py-1 text-xs text-cyan-900">
                  Language: {applied.language.toUpperCase()}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onRemoveApplied({
                        ...appliedSnapshot(),
                        language: undefined,
                      })
                    }
                    className="h-max !bg-transparent p-1 text-cyan-700 hover:text-cyan-900"
                  >
                    <X className="size-3" />
                  </Button>
                </span>
              )}
              {applied.status && (
                <span className="inline-flex items-center gap-1 rounded-full bg-amber-100 px-2 py-1 text-xs text-amber-900">
                  Status: {getEmailTemplateStatusLabel(applied.status)}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onRemoveApplied({
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
              {applied.tags.map(tag => (
                <span
                  key={tag}
                  className="inline-flex items-center gap-1 rounded-full bg-cyan-100 px-2 py-1 text-xs text-cyan-900"
                >
                  Tag: {tag}
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() =>
                      onRemoveApplied({
                        ...appliedSnapshot(),
                        tags: applied.tags.filter(t => t !== tag),
                      })
                    }
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

export default EmailTemplateFilterPanel;
