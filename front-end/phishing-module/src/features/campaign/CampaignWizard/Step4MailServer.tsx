import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { useSenderProfiles } from 'hooks/UseSenderProfiles';
import {
  ArrowLeftIcon,
  ArrowRightIcon,
  CheckIcon,
  ChevronDown,
  ChevronUp,
  Filter,
  RotateCcw,
  Search,
} from 'lucide-react';
import { IResponse } from 'models/Global';
import {
  DeceptionLevel,
  DomainType,
  InterfaceType,
  ISenderProfile,
  ISenderProfileListParams,
  PersonalizationLevel,
  ProviderType,
} from 'models/SenderProfile';
import { useEffect, useMemo, useRef, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface Step4MailServerProps {
  selectedProfileId?: string;
  /** Second arg is used when the campaign API omits `senderProfileName` on the saved campaign. */
  onSubmit: (profileId: string, profileName: string) => void;
  onBack: () => void;
  isLoading?: boolean;
}

type MailServerFilters = Pick<
  ISenderProfileListParams,
  'offset' | 'pageSize' | 'sortBy' | 'sortOrder' | 'searchParam'
> &
  Partial<
    Pick<
      ISenderProfile,
      | 'interfaceType'
      | 'category'
      | 'targetIndustryId'
      | 'regionId'
      | 'deceptionLevel'
      | 'psychologicalTriggers'
      | 'domainType'
      | 'personalizationLevel'
      | 'providerType'
      | 'tags'
    >
  >;

const DEFAULT_FILTERS: MailServerFilters = {
  offset: 0,
  pageSize: 50,
  sortBy: 'profileName',
  sortOrder: 'asc',
  searchParam: '',
  interfaceType: undefined,
  category: undefined,
  targetIndustryId: undefined,
  regionId: undefined,
  deceptionLevel: undefined,
  psychologicalTriggers: undefined,
  domainType: undefined,
  personalizationLevel: undefined,
  providerType: undefined,
  tags: undefined,
};

interface IDropdownOption {
  id: string;
  name: string;
}

interface ICountry extends IDropdownOption {
  code: string;
}

type IIndustry = IDropdownOption;

export const Step4MailServer = ({
  selectedProfileId,
  onSubmit,
  onBack,
  isLoading,
}: Step4MailServerProps) => {
  const { profiles, fetchProfiles, loading } = useSenderProfiles();
  const [selectedId, setSelectedId] = useState<string>(selectedProfileId || '');
  const [isFilterExpanded, setIsFilterExpanded] = useState(false);
  const [industryOptions, setIndustryOptions] = useState<
    { id: string; name: string }[]
  >([]);
  const [regionOptions, setRegionOptions] = useState<
    { id: string; name: string; code: string }[]
  >([]);
  const [filters, setFilters] = useState<MailServerFilters>(DEFAULT_FILTERS);
  const [draftFilters, setDraftFilters] =
    useState<MailServerFilters>(DEFAULT_FILTERS);
  const filterDropdownRef = useRef<HTMLDivElement | null>(null);

  const apiClient = useAPI();

  useEffect(() => {
    const fetchDropdownOptions = async () => {
      try {
        const [countryRes, industryRes] = await Promise.all<
          [IResponse<Array<ICountry>>, IResponse<Array<IIndustry>>]
        >([
          apiClient.get(API_END_POINTS.GET_ACTIVE_COUNTRY_LIST),
          apiClient.get(API_END_POINTS.GET_ACTIVE_INDUSTRIES_LIST),
        ]);

        if (countryRes)
          setRegionOptions(
            countryRes.data.map(item => ({
              id: item.id,
              name: item.name,
              code: item.code,
            })),
          );
        if (industryRes)
          setIndustryOptions(
            industryRes.data.map(item => ({
              id: item.id,
              name: item.name,
            })),
          );
      } catch (error) {
        console.error('Error fetching dropdown data:', error);
      }
    };
    fetchDropdownOptions();
  }, [apiClient]);

  useEffect(() => {
    fetchProfiles({
      ...filters,
    });
  }, [fetchProfiles, filters]);

  useEffect(() => {
    if (!isFilterExpanded) return;
    setDraftFilters(filters);
  }, [isFilterExpanded, filters]);

  const tagOptions = useMemo(() => {
    const tags = (profiles?.items || []).flatMap(profile => profile.tags || []);
    return [...new Set(tags)].sort((a, b) => a.localeCompare(b));
  }, [profiles?.items]);

  const interfaceTypeOptions = Object.values(InterfaceType);

  const deceptionLevelOptions = Object.values(DeceptionLevel);

  const psychologicalTriggerOptions = [
    'Authority',
    'Urgency',
    'Curiosity',
    'Fear',
  ];

  const domainTypeOptions = Object.values(DomainType);

  const personalizationLevelOptions = Object.values(PersonalizationLevel);

  const providerTypeOptions = Object.values(ProviderType);

  const handleReset = () => {
    setFilters(DEFAULT_FILTERS);
    setDraftFilters(DEFAULT_FILTERS);
    setIsFilterExpanded(false);
  };

  const handleApplyFilters = () => {
    setFilters({ ...draftFilters, offset: 0 });
    setIsFilterExpanded(false);
  };

  const handleSubmit = () => {
    if (!selectedId) return;
    const picked = profiles?.items?.find(p => p.profileId === selectedId);
    onSubmit(selectedId, picked?.profileName ?? '');
  };

  return (
    <div className="mx-auto max-w-3xl">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground">
          Select Mail Server
        </h2>
        <p className="mt-2 text-muted-foreground">
          Choose the sender profile to use for sending emails.
        </p>
      </div>

      <div className="relative mb-4 flex w-full items-center gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
          <Input
            placeholder="Search by name or tags..."
            value={filters.searchParam}
            onChange={e =>
              setFilters(prev => ({ ...prev, searchParam: e.target.value }))
            }
            className="pl-9"
          />
        </div>

        <div ref={filterDropdownRef} data-mailserver-filter-container>
          <Button
            variant="secondary"
            className="flex w-40 items-center justify-between gap-2 text-sm font-medium"
            onClick={() => setIsFilterExpanded(open => !open)}
          >
            <div className="flex items-center gap-2">
              <Filter className="size-4" />
              Filters
            </div>
            {isFilterExpanded ? (
              <ChevronUp className="size-4" />
            ) : (
              <ChevronDown className="size-4" />
            )}
          </Button>

          {isFilterExpanded && (
            <div
              className="absolute right-0 top-full z-10 mt-2 w-full"
              data-mailserver-filter-container
              onPointerDownCapture={event => event.stopPropagation()}
            >
              <div
                className="mb-6 rounded-lg border border-card-border bg-card-background p-4 shadow-sm"
                data-mailserver-filter-container
                onPointerDownCapture={event => event.stopPropagation()}
              >
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <Label className="mb-1 block text-xs font-medium text-muted-foreground">
                      Interface Type
                    </Label>
                    <Select
                      value={draftFilters.interfaceType || 'all'}
                      onValueChange={value =>
                        setDraftFilters(prev => ({
                          ...prev,
                          interfaceType:
                            value === 'all'
                              ? undefined
                              : (value as MailServerFilters['interfaceType']),
                        }))
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="All" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All</SelectItem>
                        {interfaceTypeOptions.map(value => (
                          <SelectItem key={value} value={value}>
                            {value}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label className="mb-1 block text-xs font-medium text-muted-foreground">
                      Category
                    </Label>
                    <Select
                      value={draftFilters.category || 'all'}
                      onValueChange={value =>
                        setDraftFilters(prev => ({
                          ...prev,
                          category: value === 'all' ? undefined : value,
                        }))
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="All Categories" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Categories</SelectItem>
                        <SelectItem value="Internal">Internal</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label className="mb-1 block text-xs font-medium text-muted-foreground">
                      Target Industry
                    </Label>
                    <Select
                      value={draftFilters.targetIndustryId}
                      onValueChange={value =>
                        setDraftFilters(prev => ({
                          ...prev,
                          targetIndustryId: value === 'all' ? undefined : value,
                        }))
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="All Industries" />
                      </SelectTrigger>
                      <SelectContent>
                        {industryOptions.map(option => (
                          <SelectItem key={option.id} value={option.id}>
                            {option.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label className="mb-1 block text-xs font-medium text-muted-foreground">
                      Region
                    </Label>
                    <Select
                      value={draftFilters.regionId || 'all'}
                      onValueChange={value =>
                        setDraftFilters(prev => ({
                          ...prev,
                          regionId: value === 'all' ? undefined : value,
                        }))
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="All Regions" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Regions</SelectItem>
                        {regionOptions.map(option => (
                          <SelectItem key={option.id} value={option.id}>
                            {option.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label className="mb-1 block text-xs font-medium text-muted-foreground">
                      Deception Level
                    </Label>
                    <Select
                      value={draftFilters.deceptionLevel?.id || 'all'}
                      onValueChange={value =>
                        setDraftFilters(prev => ({
                          ...prev,
                          deceptionLevel:
                            value === 'all'
                              ? undefined
                              : (value as unknown as MailServerFilters['deceptionLevel']),
                        }))
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="All Levels" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Levels</SelectItem>
                        {deceptionLevelOptions.map(value => (
                          <SelectItem
                            key={value}
                            value={value}
                            className="capitalize"
                          >
                            {value.toLowerCase()}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label className="mb-1 block text-xs font-medium text-muted-foreground">
                      Domain Type
                    </Label>
                    <Select
                      value={draftFilters.domainType || 'all'}
                      onValueChange={value =>
                        setDraftFilters(prev => ({
                          ...prev,
                          domainType:
                            value === 'all'
                              ? undefined
                              : (value as MailServerFilters['domainType']),
                        }))
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="All Domain Types" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Domain Types</SelectItem>
                        {domainTypeOptions.map(value => (
                          <SelectItem
                            key={value}
                            value={value}
                            className="capitalize"
                          >
                            {value.toLowerCase().split('_').join(' ')}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label className="mb-1 block text-xs font-medium text-muted-foreground">
                      Personalization Level
                    </Label>
                    <Select
                      value={draftFilters.personalizationLevel?.id || 'all'}
                      onValueChange={value =>
                        setDraftFilters(prev => ({
                          ...prev,
                          personalizationLevel:
                            value === 'all'
                              ? undefined
                              : (value as unknown as MailServerFilters['personalizationLevel']),
                        }))
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="All Levels" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Levels</SelectItem>
                        {personalizationLevelOptions.map(value => (
                          <SelectItem
                            key={value}
                            value={value}
                            className="capitalize"
                          >
                            {value.toLowerCase().split('_').join(' ')}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label className="mb-1 block text-xs font-medium text-muted-foreground">
                      Provider Type
                    </Label>
                    <Select
                      value={draftFilters.providerType || 'all'}
                      onValueChange={value =>
                        setDraftFilters(prev => ({
                          ...prev,
                          providerType:
                            value === 'all'
                              ? undefined
                              : (value as MailServerFilters['providerType']),
                        }))
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="All Providers" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Providers</SelectItem>
                        {providerTypeOptions.map(value => (
                          <SelectItem
                            key={value}
                            value={value}
                            className="capitalize"
                          >
                            {value.toLowerCase().split('_').join(' ')}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="col-span-3">
                    <Label className="mb-2 block text-xs font-medium text-muted-foreground">
                      Psychological Triggers
                    </Label>
                    <div className="flex flex-wrap gap-2">
                      {psychologicalTriggerOptions.map(trigger => {
                        const isSelected =
                          draftFilters.psychologicalTriggers?.includes(
                            trigger,
                          ) || false;
                        return (
                          <button
                            key={trigger}
                            onClick={() =>
                              setDraftFilters(prev => {
                                const current =
                                  prev.psychologicalTriggers || [];
                                const updated = isSelected
                                  ? current.filter(t => t !== trigger)
                                  : [...current, trigger];
                                return {
                                  ...prev,
                                  psychologicalTriggers:
                                    updated.length > 0 ? updated : undefined,
                                };
                              })
                            }
                            className={`rounded-full px-3 py-1.5 text-sm font-medium transition-all ${isSelected
                              ? 'bg-primary text-primary-foreground'
                              : 'border border-card-border bg-background text-foreground hover:border-primary/50'
                              }`}
                          >
                            {trigger}
                          </button>
                        );
                      })}
                    </div>
                  </div>

                  <div className="col-span-3">
                    <Label className="mb-1 block text-xs font-medium text-muted-foreground">
                      Tags (comma separated)
                    </Label>
                    <Input
                      value={draftFilters.tags?.join(', ') || ''}
                      onChange={e => {
                        const parsedTags = e.target.value
                          .split(',')
                          .map(tag => tag.trim())
                          .filter(Boolean);

                        setDraftFilters(prev => ({
                          ...prev,
                          tags: parsedTags.length > 0 ? parsedTags : undefined,
                        }));
                      }}
                      placeholder="security, awareness, phishing"
                    />
                    {tagOptions.length > 0 && (
                      <p className="mt-1 text-xs text-muted-foreground">
                        Suggestions: {tagOptions.join(', ')}
                      </p>
                    )}
                  </div>
                </div>

                <div className="mt-4 flex justify-end gap-2">
                  <Button
                    variant="outline"
                    onClick={() => setDraftFilters(filters)}
                  >
                    Clear Draft
                  </Button>
                  <Button variant="default" onClick={handleApplyFilters}>
                    Apply
                  </Button>
                </div>
              </div>
            </div>
          )}
        </div>

        <Button variant="destructive" onClick={handleReset}>
          <RotateCcw className="mr-2 size-4" />
          Reset
        </Button>
      </div>

      <div className="mb-6 max-h-96 space-y-3 overflow-y-auto">
        {loading ? (
          Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="animate-pulse rounded-lg border p-4">
              <div className="mb-2 h-5 w-1/3 rounded bg-card-border" />
              <div className="h-4 w-1/2 rounded bg-card-border" />
            </div>
          ))
        ) : (profiles?.items?.length || 0) === 0 ? (
          <div className="py-12 text-center text-muted-foreground">
            No sender profiles found. Please create a sender profile first.
          </div>
        ) : (profiles?.items?.length || 0) === 0 ? (
          <div className="py-12 text-center text-muted-foreground">
            No sender profiles match the current search and filters.
          </div>
        ) : (
          profiles?.items?.map(profile => (
            <div
              key={profile.profileId}
              onClick={() => setSelectedId(profile.profileId)}
              className={`cursor-pointer rounded-lg border p-4 transition-all ${selectedId === profile.profileId
                ? 'border-primary bg-primary/10 ring-2 ring-primary/20'
                : 'hover:border-card-border hover:border-card-border/80'
                }`}
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <h4 className="font-medium text-foreground">
                    {profile.profileName}
                  </h4>
                  <p className="mt-1 text-sm text-muted-foreground">
                    {profile.fromAddress}
                  </p>
                  <p className="mt-1 text-xs text-muted-foreground">
                    {profile.host}:{profile.port}
                  </p>
                </div>
                <div className="flex items-center space-x-2">
                  {profile.verified ? (
                    <span className="flex items-center rounded-full bg-primary/10 px-2 py-0.5 text-xs text-foreground">
                      <CheckIcon className="size-3" />
                      Verified
                    </span>
                  ) : (
                    <span className="rounded-full bg-yellow-100 px-2 py-0.5 text-xs text-muted-foreground">
                      Not Verified
                    </span>
                  )}
                  {selectedId === profile.profileId && (
                    <CheckIcon className="size-5 text-primary" />
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      <div className="flex justify-between border-t border-card-border pt-6">
        <Button variant="outline" onClick={onBack} disabled={isLoading}>
          <ArrowLeftIcon className="size-4" />
          Back
        </Button>
        <Button
          variant="default"
          onClick={handleSubmit}
          disabled={!selectedId || isLoading}
        >
          {isLoading ? 'Saving...' : 'Save & Continue'}
          <ArrowRightIcon className="size-4" />
        </Button>
      </div>
    </div>
  );
};
