import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import { LandingPageFilterValues } from 'features/landing-page/LandingPageFilterPanel';
import LandingPageFilterPanel from 'features/landing-page/LandingPageFilterPanel';
import LandingPagePreviewModal from 'features/landing-page/LandingPagePreviewModal';
import useDebounce from 'hooks/UseDebounce';
import { useAPI } from 'hooks/UseAPI';
import { useLandingPages } from 'hooks/UseLandingPages';
import {
  ArrowLeftIcon,
  ArrowRightIcon,
  CheckIcon,
  ChevronDown,
  ChevronUp,
  EyeIcon,
  Filter,
  ImageIcon,
  RotateCcw,
  SearchIcon,
} from 'lucide-react';
import { ICampaign } from 'models/Campaign';
import { DifficultyLevel, getDifficultyColor } from 'models/EmailTemplate';
import { IResponse, PageTypeEnum } from 'models/Global';
import {
  ILandingPage,
  ILandingPageListParams,
  ILandingPagePreview,
  ITemplateLandingPage,
} from 'models/LandingPage';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { DEBOUNCE_DELAY, FILE_PATH_PREFIX } from 'utils/Constants';
import { cn } from 'utils/Helper';

type RecommendedState = {
  pages: ITemplateLandingPage[];
  landingPages: ILandingPage[];
};

interface Step3LandingPageProps {
  isEditMode?: boolean;
  emailTemplateId?: string;
  selectedPage?: ICampaign;
  onSubmit: (
    pageId: string | undefined,
    pageType: string,
    landingPageName: string,
  ) => void;
  onBack: () => void;
  isLoading?: boolean;
}

const PAGE_SIZE = 12;

const DEFAULT_QUERY_PARAMS: ILandingPageListParams = {
  offset: 0,
  pageSize: PAGE_SIZE,
  searchParam: '',
  sortBy: 'createdAt',
  sortOrder: 'desc',
};

const PAGE_TYPE_OPTIONS = [
  {
    value: PageTypeEnum.LANDING_PAGE,
    label: 'Landing Page',
    description: 'Show a custom landing page',
  },
  {
    value: PageTypeEnum.PAGE_NOT_FOUND_404,
    label: '404 Page',
    description: 'Show a "Page Not Found" error',
  },
  {
    value: PageTypeEnum.CUSTOM,
    label: 'Custom Page',
    description: 'Use your own custom HTML',
  },
];

export const Step3LandingPage = ({
  isEditMode,
  emailTemplateId,
  selectedPage,
  onSubmit,
  onBack,
  isLoading,
}: Step3LandingPageProps) => {
  const {
    landingPages,
    loading,
    fetchLandingPages,
    fetchLandingPagesByTemplate,
    getLandingPagePreview,
  } = useLandingPages();
  const { get } = useAPI();

  // ── Selection state ──────────────────────────────────────────────────────
  const [selectedId, setSelectedId] = useState<string>(
    selectedPage?.landingPageId || '',
  );
  const hasAutoSelectedRef = useRef(false);
  const templateFetchRef = useRef<{
    templateId: string;
    promise: Promise<ITemplateLandingPage[]>;
  } | null>(null);

  // ── Template-linked recommendations (null = not ready for current template)
  const [recommendedState, setRecommendedState] =
    useState<RecommendedState | null>(null);

  // ── Search ───────────────────────────────────────────────────────────────
  const [searchValue, setSearchValue] = useState('');
  const debouncedSearch = useDebounce(searchValue, DEBOUNCE_DELAY);

  // ── Filter panel ─────────────────────────────────────────────────────────
  const [isFilterExpanded, setIsFilterExpanded] = useState(false);
  const [filterPanelKey, setFilterPanelKey] = useState(0);
  const filterDropdownRef = useRef<HTMLDivElement>(null);

  /**
   * `filters.pageType` is the single source of truth for both the page-type
   * selector buttons and the LandingPageFilterPanel.
   */
  const [filters, setFilters] = useState<LandingPageFilterValues>({
  });

  // Derived page type (used for submit + button highlight)
  const pageType = filters.pageType ?? PageTypeEnum.LANDING_PAGE;

  // ── Query params ─────────────────────────────────────────────────────────
  const [queryParams, setQueryParams] =
    useState<ILandingPageListParams>(DEFAULT_QUERY_PARAMS);

  // ── Preview modal state ──────────────────────────────────────────────────
  const [previewPage, setPreviewPage] = useState<ILandingPage | null>(null);
  const [previewData, setPreviewData] = useState<ILandingPagePreview | null>(
    null,
  );
  const [previewLoading, setPreviewLoading] = useState(false);

  // ── Tag options (derived from current page items) ─────────────────────────
  const tagOptions = useMemo(() => {
    const unique = new Set<string>();
    for (const page of landingPages.items) {
      for (const tag of page.tags ?? []) unique.add(tag);
    }
    return Array.from(unique).sort((a, b) => a.localeCompare(b));
  }, [landingPages.items]);

  // ── Active filter count ───────────────────────────────────────────────────
  const activeFilterCount = [
    filters.category,
    filters.difficulty,
    filters.status,
    ...(filters.tags && filters.tags.length > 0 ? [true] : []),
  ].filter(Boolean).length;

  // ── Derived ──────────────────────────────────────────────────────────────
  const libraryPages = useMemo(
    () => landingPages.items || [],
    [landingPages.items],
  );
  const currentPage = (queryParams.offset || 0) + 1;
  const isFirstPage = (queryParams.offset || 0) === 0;
  const needsRecommendedMerge = isFirstPage && !!emailTemplateId;
  const isGridLoading =
    loading || (needsRecommendedMerge && recommendedState === null);

  const recommendedPages = recommendedState?.pages ?? [];
  const recommendedPageIds = useMemo(
    () => recommendedState?.pages.map(p => p.pageId) ?? [],
    [recommendedState],
  );
  const recommendedPageIdSet = useMemo(
    () => new Set(recommendedPageIds),
    [recommendedPageIds],
  );

  // Recommended pages first, then remaining library pages (deduped)
  const displayPages = useMemo(() => {
    const recommendedLanding = recommendedState?.landingPages ?? [];
    const others = libraryPages.filter(
      p => !recommendedPageIdSet.has(p.pageId),
    );

    if (!isFirstPage || recommendedLanding.length === 0) {
      return others;
    }

    return [...recommendedLanding, ...others];
  }, [
    libraryPages,
    recommendedState,
    recommendedPageIdSet,
    isFirstPage,
  ]);

  const selectedPageName =
    displayPages.find(p => p.pageId === selectedId)?.name ??
    recommendedPages.find(p => p.pageId === selectedId)?.name ??
    '';

  const resolveRecommendedLandingPages = useCallback(
    async (
      templatePages: ITemplateLandingPage[],
      items: ILandingPage[],
    ): Promise<ILandingPage[]> => {
      const results = await Promise.all(
        templatePages.map(async rec => {
          const fromList = items.find(p => p.pageId === rec.pageId);
          if (fromList) return fromList;

          try {
            const response = (await get(
              API_END_POINTS.LANDING_PAGE_DETAILS(rec.pageId),
            )) as IResponse<ILandingPage> | undefined;
            return response?.data ?? null;
          } catch {
            return null;
          }
        }),
      );
      return results.filter((p): p is ILandingPage => p != null);
    },
    [get],
  );

  const applyAutoSelectIfNeeded = useCallback(
    (ids: string[]) => {
      if (hasAutoSelectedRef.current) return;

      const existingId = selectedPage?.landingPageId;
      if (existingId || isEditMode) {
        hasAutoSelectedRef.current = true;
        return;
      }

      if (ids.length > 0) {
        hasAutoSelectedRef.current = true;
        setSelectedId(ids[0]);
      }
    },
    [isEditMode, selectedPage?.landingPageId],
  );

  // ── Effects ──────────────────────────────────────────────────────────────

  // Push debounced search into queryParams, reset to page 1
  useEffect(() => {
    setTimeout(() => {
      setQueryParams(prev => ({
        ...prev,
        searchParam: debouncedSearch,
        offset: 0,
      }));
    }, 0);
  }, [debouncedSearch]);

  // Push filter changes (including pageType) into queryParams, reset to page 1
  useEffect(() => {
    setTimeout(() => {
      setQueryParams(prev => ({
        ...prev,
        pageType: filters.pageType,
        category: filters.category,
        difficulty: filters.difficulty,
        tags:
          filters.tags && filters.tags.length > 0 ? filters.tags : undefined,
        status: filters.status,
        offset: 0,
      }));
    }, 0);
  }, [filters]);

  // Fetch whenever queryParams change
  useEffect(() => {
    fetchLandingPages(queryParams);
  }, [queryParams, fetchLandingPages]);

  // Start template-linked fetch when emailTemplateId changes
  useEffect(() => {
    hasAutoSelectedRef.current = false;

    if (!emailTemplateId) {
      setTimeout(() => {
        setRecommendedState({ pages: [], landingPages: [] });
      }, 0);
      templateFetchRef.current = null;
      return;
    }

    setTimeout(() => setRecommendedState(null), 0);
    templateFetchRef.current = {
      templateId: emailTemplateId,
      promise: fetchLandingPagesByTemplate(emailTemplateId),
    };
  }, [emailTemplateId, fetchLandingPagesByTemplate]);

  // Merge library + recommended data once both are ready (page 1 only)
  useEffect(() => {
    if (!emailTemplateId || !isFirstPage || loading) return;
    if (recommendedState !== null) return;

    const fetchRef = templateFetchRef.current;
    if (!fetchRef || fetchRef.templateId !== emailTemplateId) return;

    let cancelled = false;

    const merge = async () => {
      const templatePages = await fetchRef.promise;
      if (cancelled) return;

      const landingPagesResolved = await resolveRecommendedLandingPages(
        templatePages,
        libraryPages,
      );
      if (cancelled) return;

      setRecommendedState({
        pages: templatePages,
        landingPages: landingPagesResolved,
      });
      applyAutoSelectIfNeeded(templatePages.map(p => p.pageId));
    };

    void merge();

    return () => {
      cancelled = true;
    };
  }, [
    emailTemplateId,
    isFirstPage,
    loading,
    libraryPages,
    recommendedState,
    resolveRecommendedLandingPages,
    applyAutoSelectIfNeeded,
  ]);

  // ── Handlers ─────────────────────────────────────────────────────────────

  const handleSelectPage = useCallback((pageId: string) => {
    hasAutoSelectedRef.current = true;
    setSelectedId(pageId);
  }, []);

  const handlePageChange = (page: number) =>
    setQueryParams(prev => ({ ...prev, offset: page - 1 }));

  const handleFilterChange = useCallback(
    (newFilters: LandingPageFilterValues) => {
      setFilters(newFilters);
    },
    [],
  );

  const handleReset = useCallback(() => {
    setSearchValue('');
    setFilters({});
    setIsFilterExpanded(false);
    setFilterPanelKey(k => k + 1);
    setQueryParams({
      ...DEFAULT_QUERY_PARAMS
    });
  }, []);

  const handlePreview = useCallback(
    async (page: ILandingPage) => {
      setPreviewPage(page);
      setPreviewLoading(true);
      const data = await getLandingPagePreview(page.pageId);
      setPreviewData(data);
      setPreviewLoading(false);
    },
    [getLandingPagePreview],
  );

  const handleClosePreview = useCallback(() => {
    setPreviewPage(null);
    setPreviewData(null);
  }, []);

  const handleSubmit = () => {
    const picked = displayPages.find(p => p.pageId === selectedId);
    const recommended = recommendedPages.find(p => p.pageId === selectedId);
    onSubmit(
      selectedId || undefined,
      pageType,
      picked?.name ?? recommended?.name ?? '',
    );
  };

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <div className="mx-auto max-w-[80%]">
      {/* Header */}
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground">
          Select Landing Page
        </h2>
        <p className="mt-2 text-muted-foreground">
          Choose what users see when they click the phishing link.
        </p>
      </div>

      {/* Search + Filter bar */}
      <div className="relative mb-4 flex w-full items-center gap-3">
        {/* Search input */}
        <div className="relative flex-1">
          <SearchIcon className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search by name or tags..."
            value={searchValue}
            onChange={e => setSearchValue(e.currentTarget.value)}
            className="w-full pl-9"
          />
        </div>

        {/* Filter button + dropdown panel */}
        <div ref={filterDropdownRef}>
          <Button
            variant="secondary"
            className="flex w-40 items-center justify-between gap-2 text-sm font-medium"
            onClick={() => {
              if (!isFilterExpanded) setFilterPanelKey(k => k + 1);
              setIsFilterExpanded(open => !open);
            }}
          >
            <div className="flex items-center gap-2">
              <Filter className="size-4" />
              Filters
            </div>
            {activeFilterCount > 0 && (
              <Badge variant="default" className="ml-1">
                {activeFilterCount}
              </Badge>
            )}
            {isFilterExpanded ? (
              <ChevronUp className="size-4" />
            ) : (
              <ChevronDown className="size-4" />
            )}
          </Button>

          <LandingPageFilterPanel
            key={filterPanelKey}
            isExpanded={isFilterExpanded}
            filters={filters}
            tagOptions={tagOptions}
            onFilterChange={handleFilterChange}
            onClose={() => setIsFilterExpanded(false)}
            containerRef={filterDropdownRef}
          />
        </div>

        {/* Reset button */}
        <Button variant="destructive" onClick={handleReset}>
          <RotateCcw className="mr-2 size-4" />
          Reset
        </Button>
      </div>

      {/* Active selection banner */}
      {(selectedId || pageType !== PageTypeEnum.LANDING_PAGE) && (
        <div className="mb-4 flex items-center gap-2 rounded-lg border border-primary/30 bg-primary/10 px-4 py-2.5">
          <div className="flex size-5 shrink-0 items-center justify-center rounded-full bg-primary">
            <CheckIcon className="size-3 text-white" />
          </div>
          {pageType !== PageTypeEnum.LANDING_PAGE ? (
            <span className="text-sm font-medium text-primary">
              {PAGE_TYPE_OPTIONS.find(o => o.value === pageType)?.label ??
                pageType}
            </span>
          ) : (
            <>
              Landing Page:{' '}
              <span className="text-sm font-medium text-primary">
                {selectedPageName ||
                  selectedPage?.landingPageName ||
                  'Page selected'}
              </span>
            </>
          )}
        </div>
      )}

      {/* Landing Page Grid */}
      <div className="mb-4 grid grid-cols-2 gap-4 md:grid-cols-3">
        {isGridLoading ? (
          Array.from({ length: PAGE_SIZE }).map((_, i) => (
            <div
              key={i}
              className="animate-pulse rounded-lg border border-card-border p-4"
            >
              <div className="h-28 rounded-t-lg bg-card-border" />
              <div className="mt-2">
                <div className="mb-2 h-4 w-3/4 rounded bg-card-border" />
                <div className="h-3 w-1/2 rounded bg-card-border" />
              </div>
            </div>
          ))
        ) : displayPages.length === 0 ? (
          <div className="col-span-3 py-12 text-center text-muted-foreground">
            No landing pages found
          </div>
        ) : (
          displayPages.map((page: ILandingPage) => {
            const isSelected = selectedId === page.pageId;
            const recommendedIndex = recommendedPageIds.indexOf(page.pageId);

            return (
              <div
                key={page.pageId}
                onClick={() => handleSelectPage(page.pageId)}
                className={cn(
                  'group relative cursor-pointer overflow-hidden rounded-lg border p-4 transition-all duration-200',
                  isSelected
                    ? 'border-primary bg-primary/10 shadow-md ring-2 ring-primary'
                    : 'border-card-border hover:border-primary/40 hover:shadow-sm',
                )}
              >
                {/* Selected badge */}
                {isSelected && (
                  <div className="absolute right-2 top-2 z-10 flex size-6 items-center justify-center rounded-full bg-primary shadow">
                    <CheckIcon className="size-3.5 text-white" />
                  </div>
                )}

                {/* Recommended badge (2nd+ template-linked pages) */}
                {recommendedIndex !== -1 && (
                  <span className="absolute left-2 top-2 z-10 rounded bg-amber-500 px-3 py-1 text-sm font-medium text-white drop-shadow-2xl">
                    Recommended
                  </span>
                )}

                {/* Thumbnail */}
                {page.thumbnailUrl ? (
                  <div className="h-28 overflow-hidden rounded">
                    <img
                      src={FILE_PATH_PREFIX + page.thumbnailUrl}
                      alt={page.name}
                      className="size-full object-cover transition-transform duration-200 group-hover:scale-105"
                    />
                  </div>
                ) : (
                  <div className="flex h-28 items-center justify-center rounded bg-card-border">
                    <ImageIcon className="size-8 text-muted-foreground" />
                  </div>
                )}

                {/* Card body */}
                <div className="mt-2">
                  <h4
                    className={cn(
                      'truncate text-sm font-medium',
                      isSelected ? 'text-primary' : 'text-foreground',
                    )}
                  >
                    {isSelected && (
                      <span className="mr-1.5 inline-block size-2 rounded-full bg-primary align-middle" />
                    )}
                    {page.name}
                  </h4>
                  <div className="mt-2 flex items-center justify-between space-x-2">
                    {page.difficultyLevel && (
                      <span
                        className={cn(
                          'rounded px-2 py-0.5 text-xs text-white',
                          getDifficultyColor(
                            page.difficultyLevel.id as DifficultyLevel,
                          ),
                        )}
                      >
                        {page.difficultyLevel.name}
                      </span>
                    )}
                    <button
                      onClick={e => {
                        e.stopPropagation();
                        void handlePreview(page);
                      }}
                      className="flex items-center gap-2 rounded-md border border-primary px-2 py-1 text-xs text-primary hover:bg-primary/10 hover:text-primary"
                    >
                      <EyeIcon className="size-4" />
                      Preview
                    </button>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Pagination */}
      {landingPages.total > 0 && (
        <div className="mb-6 flex items-center justify-between">
          <span className="text-sm text-muted-foreground">
            Showing {displayPages.length} of {landingPages.total} pages
            {(recommendedState?.landingPages.length ?? 0) > 0 &&
              isFirstPage &&
              ` (${recommendedState?.landingPages.length} recommended)`}
          </span>
          <Pagination
            total={landingPages.total}
            perPage={PAGE_SIZE}
            currentPage={currentPage}
            onPageChange={handlePageChange}
          />
        </div>
      )}

      {/* Preview Modal — uses full detail API + device toggle */}
      <LandingPagePreviewModal
        isOpen={!!previewPage}
        onClose={handleClosePreview}
        preview={previewData}
        loading={previewLoading}
        onUseInCampaign={
          previewPage
            ? () => {
              handleSelectPage(previewPage.pageId);
              setFilters(prev => ({
                ...prev,
                pageType: previewPage.pageType,
              }));
              handleClosePreview();
            }
            : undefined
        }
      />

      {/* Actions */}
      <div className="flex justify-between border-t border-card-border pt-6">
        <Button variant="outline" onClick={onBack} disabled={isLoading}>
          <ArrowLeftIcon className="size-4" />
          Back
        </Button>
        <Button
          variant="default"
          onClick={handleSubmit}
          disabled={
            (pageType === PageTypeEnum.LANDING_PAGE && !selectedId) ||
            !!isLoading
          }
        >
          {isLoading ? 'Saving...' : 'Save & Continue'}
          <ArrowRightIcon className="size-4" />
        </Button>
      </div>
    </div>
  );
};
