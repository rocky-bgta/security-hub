import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import ViewToggle, { ViewMode } from 'components/ViewToggle';
import AILandingPageModal from 'features/landing-page/AILandingPageModal';
import LandingPageFilterPanel from 'features/landing-page/LandingPageFilterPanel';
import LandingPageGrid from 'features/landing-page/LandingPageGrid';
import LandingPagePreviewModal from 'features/landing-page/LandingPagePreviewModal';
import LandingPageTable from 'features/landing-page/LandingPageTable';
import useDebounce from 'hooks/UseDebounce';
import useLandingPages from 'hooks/UseLandingPages';
import {
  ChevronDown,
  ChevronUp,
  Filter,
  LayoutTemplate,
  Plus,
  RotateCcw,
  Search,
  Sparkles,
  Trash2,
} from 'lucide-react';
import {
  IAILandingPageParams,
  ILandingPage,
  ILandingPageListParams,
  ILandingPagePreview,
  LandingPageStatusFilter,
} from 'models/LandingPage';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { TAILandingPageForm } from 'schemas/LandingPageSchema';
import { DEBOUNCE_DELAY } from 'utils/Constants';
import { getSimulationLabel } from 'utils/SimulationChannel';
import {
  getLandingPageChannelFromPath,
  getLandingPageCreateUrl,
  getLandingPageEditUrl,
} from 'utils/ListNavigation';

const VIEW_MODE_KEY = 'landingPageViewMode';
const DEFAULT_PAGE_SIZE = 12;

/**
 * Landing Page Library Page
 * Displays landing pages in Grid or Table view with search, filter, and CRUD operations
 * Based on Task-04 Landing Page Library requirements
 */
const LandingPageLibrary = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const channel = getLandingPageChannelFromPath(pathname);
  const simulationLabel = getSimulationLabel(channel);
  const {
    landingPages,
    loading,
    totalCount,
    fetchLandingPages,
    getLandingPagePreview,
    deleteLandingPage,
    duplicateLandingPage,
    generateAILandingPage,
    generating,
  } = useLandingPages();

  // Query params state
  const [queryParams, setQueryParams] = useState<ILandingPageListParams>({
    offset: 0,
    pageSize: DEFAULT_PAGE_SIZE,
    searchParam: '',
    sortBy: 'createdAt',
    sortOrder: 'desc',
  });

  // View mode (persisted in localStorage) - BR-01: Default is Grid
  const [viewMode, setViewMode] = useState<ViewMode>(() => {
    const saved = localStorage.getItem(VIEW_MODE_KEY);
    return (saved as ViewMode) || 'grid';
  });

  // Search state
  const [searchValue, setSearchValue] = useState('');
  const [isFilterExpanded, setIsFilterExpanded] = useState(false);
  const [filterPanelKey, setFilterPanelKey] = useState(0);
  const debouncedSearch = useDebounce(searchValue, DEBOUNCE_DELAY);
  const filterDropdownRef = useRef<HTMLDivElement>(null);

  // Filter states
  const [filters, setFilters] = useState<{
    pageType?: string;
    category?: string;
    difficulty?: string;
    tags?: string[];
    status?: LandingPageStatusFilter;
  }>({});

  const tagOptions = useMemo(() => {
    const unique = new Set<string>();
    for (const page of landingPages.items) {
      for (const tag of page.tags ?? []) {
        unique.add(tag);
      }
    }
    return Array.from(unique).sort((a, b) => a.localeCompare(b));
  }, [landingPages.items]);

  const activeFilterCount = useMemo(
    () =>
      [
        filters.pageType,
        filters.category,
        filters.difficulty,
        filters.status,
        ...(filters.tags && filters.tags.length > 0 ? [true] : []),
      ].filter(Boolean).length,
    [filters],
  );

  // Modal states
  const [previewPage, setPreviewPage] = useState<ILandingPage | null>(null);
  const [previewData, setPreviewData] = useState<ILandingPagePreview | null>(
    null,
  );
  const [previewLoading, setPreviewLoading] = useState(false);
  const [deletePage, setDeletePage] = useState<ILandingPage | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [showAIModal, setShowAIModal] = useState(false);
  const pollingInFlightRef = useRef(false);
  const hasProcessingPages = landingPages.items.some(page => {
    const status = (page as { status?: string }).status;
    return status === 'PROCESSING';
  });

  // Save view mode preference - AC-03
  useEffect(() => {
    localStorage.setItem(VIEW_MODE_KEY, viewMode);
  }, [viewMode]);

  // Update query params when search changes
  useEffect(() => {
    setTimeout(() => {
      setQueryParams(prev => ({
        ...prev,
        searchParam: debouncedSearch,
        offset: 0, // Reset to first page on search
      }));
    }, 0);
  }, [debouncedSearch]);

  // Update query params when filters change
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
        offset: 0, // Reset to first page on filter change
      }));
    }, 0);
  }, [filters]);

  // Fetch landing pages when query params change
  useEffect(() => {
    fetchLandingPages(queryParams);
  }, [queryParams, fetchLandingPages]);

  // Auto refresh while any landing page is processing.
  useEffect(() => {
    if (!hasProcessingPages) return;

    const intervalId = window.setInterval(async () => {
      if (pollingInFlightRef.current) return;
      pollingInFlightRef.current = true;
      try {
        await Promise.resolve(fetchLandingPages(queryParams));
      } finally {
        pollingInFlightRef.current = false;
      }
    }, 5000);

    return () => {
      window.clearInterval(intervalId);
      pollingInFlightRef.current = false;
    };
  }, [hasProcessingPages, fetchLandingPages, queryParams]);

  // Handlers
  const handleViewModeChange = (mode: ViewMode) => {
    setViewMode(mode);
  };

  const handleSort = useCallback((field: string) => {
    setQueryParams(prev => ({
      ...prev,
      sortBy: field,
      sortOrder:
        prev.sortBy === field && prev.sortOrder === 'asc' ? 'desc' : 'asc',
    }));
  }, []);

  const handlePreview = useCallback(
    async (page: ILandingPage) => {
      setPreviewPage(page);
      setPreviewLoading(true);
      const preview = await getLandingPagePreview(page.pageId);
      setPreviewData(preview);
      setPreviewLoading(false);
    },
    [getLandingPagePreview],
  );

  const handleClosePreview = useCallback(() => {
    setPreviewPage(null);
    setPreviewData(null);
  }, []);

  const handleEdit = useCallback(
    (page: ILandingPage) => {
      navigate(getLandingPageEditUrl(page.pageId, channel));
    },
    [channel, navigate],
  );

  const handleDuplicate = useCallback(
    async (page: ILandingPage) => {
      const result = await duplicateLandingPage(page.pageId);
      if (result) {
        fetchLandingPages(queryParams);
      }
    },
    [duplicateLandingPage, fetchLandingPages, queryParams],
  );

  const handleDelete = useCallback((page: ILandingPage) => {
    setDeletePage(page);
  }, []);

  const handleConfirmDelete = useCallback(async () => {
    if (!deletePage) return;
    setIsDeleting(true);
    const success = await deleteLandingPage(deletePage.pageId);
    setIsDeleting(false);
    if (success) {
      setDeletePage(null);
      fetchLandingPages(queryParams);
    }
  }, [deletePage, deleteLandingPage, fetchLandingPages, queryParams]);

  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  const handleFilterChange = (newFilters: typeof filters) => {
    setFilters(newFilters);
  };

  const handleClearFilters = () => {
    setFilters({});
  };

  const handleReset = () => {
    setSearchValue('');
    handleClearFilters();
    setIsFilterExpanded(false);
    setFilterPanelKey(k => k + 1);
    setQueryParams({
      offset: 0,
      pageSize: DEFAULT_PAGE_SIZE,
      searchParam: '',
      sortBy: 'createdAt',
      sortOrder: 'desc',
    });
  };

  const handleCreateNew = () => {
    navigate(getLandingPageCreateUrl(channel));
  };

  const handleAIGenerate = useCallback(
    async (params: TAILandingPageForm): Promise<boolean> => {
      const result = await generateAILandingPage(
        params as IAILandingPageParams,
      );
      if (result) {
        setShowAIModal(false);
        fetchLandingPages(queryParams);
        return true;
      }
      return false;
    },
    [generateAILandingPage, fetchLandingPages, queryParams],
  );

  // Calculate current page (1-indexed)
  const currentPage = (queryParams.offset || 0) + 1;

  const shouldShowSkeleton =
    loading && !landingPages.items.some(page => page.status === 'PROCESSING');

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-2xl font-semibold text-foreground">
            Landing Page Library
          </h3>
          <p className="mt-1 text-muted-foreground">
            Manage your {simulationLabel.toLowerCase()} landing pages for
            campaigns
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            onClick={() => setShowAIModal(true)}
            disabled={generating}
          >
            <Sparkles className="mr-2 size-4" />
            Create with AI
          </Button>
          <Button onClick={handleCreateNew}>
            <Plus className="size-4" />
            Create Landing Page
          </Button>
        </div>
      </div>

      {/* Main Content Card */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2 text-lg">
                <LayoutTemplate className="size-5" />
                Landing Pages ({totalCount})
              </CardTitle>
              <CardDescription>
                Browse and manage landing pages for{' '}
                {simulationLabel.toLowerCase()} simulations
              </CardDescription>
            </div>
            <ViewToggle value={viewMode} onChange={handleViewModeChange} />
          </div>
        </CardHeader>
        <CardContent>
          {/* Search and Reset Bar */}
          <div className="relative mb-4 flex w-full items-center gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder="Search by name or tags..."
                value={searchValue}
                onChange={e => setSearchValue(e.target.value)}
                className="pl-9"
              />
            </div>
            <div ref={filterDropdownRef}>
              <Button
                variant="secondary"
                className="flex w-40 items-center justify-between gap-2 text-sm font-medium"
                onClick={() => {
                  if (!isFilterExpanded) {
                    setFilterPanelKey(k => k + 1);
                  }
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

              {/* Filter Panel */}
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
            <Button variant="destructive" onClick={handleReset}>
              <RotateCcw className="mr-2 size-4" />
              Reset
            </Button>
          </div>

          {/* Landing Page Display */}
          {viewMode === 'grid' ? (
            <LandingPageGrid
              pages={landingPages.items}
              loading={shouldShowSkeleton}
              onPreview={handlePreview}
              onEdit={handleEdit}
              onDuplicate={handleDuplicate}
              onDelete={handleDelete}
            />
          ) : (
            <LandingPageTable
              pages={landingPages.items}
              loading={shouldShowSkeleton}
              sortBy={queryParams.sortBy || 'createdAt'}
              sortOrder={queryParams.sortOrder || 'desc'}
              onSort={handleSort}
              onPreview={handlePreview}
              onEdit={handleEdit}
              onDuplicate={handleDuplicate}
              onDelete={handleDelete}
            />
          )}

          {/* Pagination - AC-11 */}
          {totalCount > 0 && (
            <div className="mt-6 flex justify-end">
              <Pagination
                total={totalCount}
                perPage={queryParams.pageSize || DEFAULT_PAGE_SIZE}
                currentPage={currentPage}
                onPageChange={handlePageChange}
              />
            </div>
          )}
        </CardContent>
      </Card>

      {/* Preview Modal - AC-06, AC-07, BR-05 */}
      <LandingPagePreviewModal
        isOpen={!!previewPage}
        onClose={handleClosePreview}
        preview={previewData}
        loading={previewLoading}
      />

      {/* Delete Confirmation Dialog - BR-04, AC-10 */}
      <Dialog open={!!deletePage} onOpenChange={() => setDeletePage(null)}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-vibrant-red">
              <Trash2 className="size-5" />
              Delete Landing Page
            </DialogTitle>
            <DialogDescription>
              Are you sure you want to delete &quot;{deletePage?.name}&quot;?
              This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setDeletePage(null)}
              disabled={isDeleting}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={handleConfirmDelete}
              disabled={isDeleting}
            >
              {isDeleting ? 'Deleting...' : 'Delete'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AILandingPageModal
        isOpen={showAIModal}
        onClose={() => setShowAIModal(false)}
        onGenerate={handleAIGenerate}
        generating={generating}
      />
    </div>
  );
};

export default LandingPageLibrary;
