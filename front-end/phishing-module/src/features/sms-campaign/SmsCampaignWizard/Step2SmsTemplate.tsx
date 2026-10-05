import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import EmailTemplateFilterPanel from 'features/email-template/EmailTemplateFilterPanel';
import EmailTemplatePreviewModal from 'features/email-template/EmailTemplatePreviewModal';
import { useSmsTemplates } from 'hooks/UseSmsTemplates';
import { useTemplateLibraryFilters } from 'hooks/UseTemplateLibraryFilters';
import {
  ArrowLeftIcon,
  ArrowRightIcon,
  CheckIcon,
  ChevronDown,
  ChevronUp,
  EyeIcon,
  Filter,
  MessageSquare,
  RotateCcw,
  SearchIcon,
} from 'lucide-react';
import { ICampaign } from 'models/Campaign';
import { IEmailTemplate, TemplateType } from 'models/EmailTemplate';
import { useCallback, useEffect, useRef, useState } from 'react';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import {
  areTemplateListParamsEqual,
  buildTemplateListParams,
  cn,
  TEMPLATE_LIST_PAGE_SIZE,
} from 'utils/Helper';

interface Step2SmsTemplateProps {
  selectedTemplate?: ICampaign;
  onSubmit: (templateId: string, templateName: string) => void;
  onBack: () => void;
  isLoading?: boolean;
}

const PAGE_SIZE = TEMPLATE_LIST_PAGE_SIZE;

export const Step2SmsTemplate = ({
  selectedTemplate,
  onSubmit,
  onBack,
  isLoading,
}: Step2SmsTemplateProps) => {
  const {
    templates: templateData,
    fetchTemplates,
    loading,
    setQueryParams,
    queryParams,
    getTemplatePreview,
    filterOptions,
    fetchFilterOptions,
  } = useSmsTemplates();

  const [selectedId, setSelectedId] = useState<string>(
    selectedTemplate?.emailTemplateId || '',
  );
  const [previewTemplate, setPreviewTemplate] = useState<IEmailTemplate | null>(
    null,
  );

  const {
    applied,
    searchValue,
    setSearchValue,
    debouncedSearch,
    applyFilters,
    resetFilters,
    activeFilterCount,
  } = useTemplateLibraryFilters();

  const [isFilterExpanded, setIsFilterExpanded] = useState(false);
  const [filterPanelKey, setFilterPanelKey] = useState(0);
  const filterDropdownRef = useRef<HTMLDivElement>(null);

  const templates = templateData.items || [];
  const currentPage = (queryParams.offset || 0) + 1;

  const selectedTemplateName =
    templates.find(t => t.templateId === selectedId)?.templateName ?? '';

  // ── Effects ──────────────────────────────────────────────────────────────

  useEffect(() => {
    fetchFilterOptions();
  }, [fetchFilterOptions]);

  useEffect(() => {
    setQueryParams(prev => {
      const next = {
        ...prev,
        searchParam: debouncedSearch,
        offset: 0,
      };
      return areTemplateListParamsEqual(prev, next) ? prev : next;
    });
  }, [debouncedSearch, setQueryParams]);

  useEffect(() => {
    setQueryParams(prev => {
      const next = buildTemplateListParams({
        templateType: TemplateType.SMS,
        searchParam: prev.searchParam,
        applied,
        offset: 0,
        pageSize: PAGE_SIZE,
        sortBy: prev.sortBy,
        sortOrder: prev.sortOrder,
      });
      return areTemplateListParamsEqual(prev, next) ? prev : next;
    });
  }, [applied, setQueryParams]);

  useEffect(() => {
    if (queryParams.templateType !== TemplateType.SMS) return;
    fetchTemplates(queryParams);
  }, [queryParams, fetchTemplates]);

  // ── Handlers ─────────────────────────────────────────────────────────────

  const handlePageChange = (page: number) =>
    setQueryParams(prev => ({ ...prev, offset: page - 1 }));

  const handleReset = useCallback(() => {
    resetFilters();
    setIsFilterExpanded(false);
    setFilterPanelKey(k => k + 1);
    setQueryParams({
      offset: 0,
      pageSize: PAGE_SIZE,
      searchParam: '',
      templateType: TemplateType.SMS,
      sortBy: 'createdAt',
      sortOrder: 'desc',
    });
  }, [resetFilters, setQueryParams]);

  const handleSubmit = () => {
    if (!selectedId) return;
    onSubmit(selectedId, selectedTemplateName);
  };

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <div className="mx-auto max-w-[80%]">
      {/* Header */}
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground">
          Select SMS Template
        </h2>
        <p className="mt-2 text-muted-foreground">
          Choose the smishing SMS template to use in this campaign.
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

          <EmailTemplateFilterPanel
            key={filterPanelKey}
            isExpanded={isFilterExpanded}
            templateType={TemplateType.SMS}
            filterOptions={filterOptions}
            applied={applied}
            onApply={applyFilters}
            onRemoveApplied={applyFilters}
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
      {selectedId && (
        <div className="mb-4 flex items-center gap-2 rounded-lg border border-primary/30 bg-primary/10 px-4 py-2.5">
          <div className="flex size-5 shrink-0 items-center justify-center rounded-full bg-primary">
            <CheckIcon className="size-3 text-white" />
          </div>
          Template Name:{' '}
          <span className="text-sm font-medium text-primary">
            {selectedTemplateName ||
              selectedTemplate?.emailTemplateName ||
              'Template selected'}
          </span>
        </div>
      )}

      {/* Template Grid */}
      <div className="mb-4 grid grid-cols-2 gap-4 md:grid-cols-3">
        {loading ? (
          Array.from({ length: PAGE_SIZE }).map((_, i) => (
            <div
              key={i}
              className="animate-pulse rounded-lg border border-card-border p-4"
            >
              <div className="mb-3 h-28 rounded bg-card-border" />
              <div className="mb-2 h-4 w-3/4 rounded bg-card-border" />
              <div className="h-3 w-1/2 rounded bg-card-border" />
            </div>
          ))
        ) : templates.length === 0 ? (
          <div className="col-span-3 py-12 text-center text-muted-foreground">
            No templates found
          </div>
        ) : (
          templates.map(template => {
            const isSelected = selectedId === template.templateId;
            return (
              <div
                key={template.templateId}
                onClick={() => setSelectedId(template.templateId)}
                className={cn(
                  'group relative cursor-pointer rounded-lg border p-4 transition-all duration-200',
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

                {/* Thumbnail */}
                {template.thumbnailUrl ? (
                  <div className="mb-3 h-28 overflow-hidden rounded">
                    <img
                      src={FILE_PATH_PREFIX + template.thumbnailUrl}
                      alt={template.templateName}
                      className="size-full object-cover transition-transform duration-200 group-hover:scale-105"
                    />
                  </div>
                ) : (
                  <div className="mb-3 flex h-28 items-center justify-center rounded bg-card-border">
                    <MessageSquare className="size-8 text-muted-foreground" />
                  </div>
                )}

                <h4 className="truncate text-sm font-medium text-foreground">
                  {template.templateName}
                </h4>

                <div className="mt-2 flex items-center justify-between space-x-2">
                  {template.difficultyLevel && (
                    <span
                      className='rounded px-2 py-0.5 text-xs text-white'
                    >
                      {template.difficultyLevel.name}
                    </span>
                  )}
                  <button
                    onClick={e => {
                      e.stopPropagation();
                      setPreviewTemplate(template);
                    }}
                    className="flex items-center gap-2 rounded-md border border-primary px-2 py-1 text-xs text-primary hover:bg-primary/10 hover:text-primary"
                  >
                    <EyeIcon className="size-4" />
                    Preview
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Pagination */}
      {templateData.total > 0 && (
        <div className="mb-6 flex items-center justify-between">
          <span className="text-sm text-muted-foreground">
            Showing {templates.length} of {templateData.total} templates
          </span>
          <Pagination
            total={templateData.total}
            perPage={PAGE_SIZE}
            currentPage={currentPage}
            onPageChange={handlePageChange}
          />
        </div>
      )}

      {/* Preview Modal */}
      <EmailTemplatePreviewModal
        isOpen={!!previewTemplate}
        onClose={() => setPreviewTemplate(null)}
        templateId={previewTemplate?.templateId ?? null}
        templateName={previewTemplate?.templateName ?? ''}
        templateType={TemplateType.SMS}
        getPreview={getTemplatePreview}
      />

      {/* Actions */}
      <div className="flex justify-between border-t border-card-border pt-6">
        <button
          type="button"
          onClick={onBack}
          className="rounded-lg bg-card-border px-6 py-2.5 font-medium text-foreground hover:bg-card-border/80"
        >
          <ArrowLeftIcon className="mr-2 inline-block size-4" />
          Back
        </button>
        <button
          onClick={handleSubmit}
          disabled={!selectedId || isLoading}
          className="flex items-center rounded-lg bg-primary px-6 py-2.5 font-medium text-white hover:bg-primary/80 disabled:cursor-not-allowed disabled:bg-primary/30"
        >
          {isLoading ? 'Saving...' : 'Save & Continue'}
          <ArrowRightIcon className="ml-2 size-4" />
        </button>
      </div>
    </div>
  );
};
