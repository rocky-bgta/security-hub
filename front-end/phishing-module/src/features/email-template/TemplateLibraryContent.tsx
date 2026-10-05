import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import Loader from 'common/loader/Loader';
import Pagination from 'common/Pagination';
import ViewToggle, { ViewMode } from 'components/ViewToggle';
import useEmailTemplates from 'hooks/UseEmailTemplates';
import { useTemplateLibraryFilters } from 'hooks/UseTemplateLibraryFilters';
import {
  ChevronDown,
  ChevronUp,
  Filter,
  Mail,
  MessageSquare,
  Plus,
  RotateCcw,
  Search,
  Sparkles,
} from 'lucide-react';
import { IEmailTemplate, TemplateType } from 'models/EmailTemplate';
import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TAIGenerateForm } from 'schemas/EmailTemplateSchema';
import {
  areTemplateListParamsEqual,
  buildTemplateListParams,
  TEMPLATE_LIST_PAGE_SIZE,
} from 'utils/Helper';
import { getTemplateCreateUrl, getTemplateEditUrl } from 'utils/ListNavigation';
import AIGenerateModal from './AIGenerateModal';
import EmailTemplateDeleteConfirm from './EmailTemplateDeleteConfirm';
import EmailTemplateFilterPanel from './EmailTemplateFilterPanel';
import EmailTemplateGrid from './EmailTemplateGrid';
import EmailTemplatePreviewModal from './EmailTemplatePreviewModal';
import EmailTemplateTable from './EmailTemplateTable';

const TAB_CONFIG = [
  {
    value: TemplateType.EMAIL,
    label: 'Email Templates',
    icon: Mail,
    description: 'Manage phishing email templates for campaigns',
    searchPlaceholder: 'Search by name, subject, or tags...',
    createLabel: 'Create Email Template',
    cardDescription:
      'Browse and manage email templates for phishing simulations',
  },
  {
    value: TemplateType.SMS,
    label: 'SMS Templates',
    icon: MessageSquare,
    description: 'Manage SMS templates for smishing simulations',
    searchPlaceholder: 'Search by name, message, or tags...',
    createLabel: 'Create SMS Template',
    cardDescription: 'Browse and manage SMS templates for smishing simulations',
  },
] as const;

function getViewModeKey(templateType: TemplateType): string {
  return `templateLibraryViewMode_${templateType}`;
}

interface TemplateLibraryContentProps {
  templateType: TemplateType;
}

/**
 * Shared template library UI for a single template type (email or SMS).
 */
const TemplateLibraryContent = ({
  templateType,
}: TemplateLibraryContentProps) => {
  const navigate = useNavigate();
  const {
    templates,
    loading,
    filterOptions,
    queryParams,
    setQueryParams,
    fetchTemplates,
    getTemplatePreview,
    deleteTemplate,
    duplicateTemplate,
    fetchFilterOptions,
    generateAITemplate,
    creating,
  } = useEmailTemplates({ templateType });

  const pageConfig =
    TAB_CONFIG.find(tab => tab.value === templateType) ?? TAB_CONFIG[0];
  const PageIcon = pageConfig.icon;

  const [viewMode, setViewMode] = useState<ViewMode>(() => {
    const saved = localStorage.getItem(getViewModeKey(templateType));
    return (saved as ViewMode) || 'grid';
  });

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

  const [previewTemplate, setPreviewTemplate] = useState<IEmailTemplate | null>(
    null,
  );
  const [deleteTemplateItem, setDeleteTemplateItem] =
    useState<IEmailTemplate | null>(null);
  const [showAIModal, setShowAIModal] = useState(false);
  const pollingInFlightRef = useRef(false);
  const hasProcessingTemplates = templates.items.some(
    template =>
      (template as IEmailTemplate & { status?: string }).status ===
      'PROCESSING',
  );

  useEffect(() => {
    localStorage.setItem(getViewModeKey(templateType), viewMode);
  }, [viewMode, templateType]);

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
        templateType,
        searchParam: prev.searchParam,
        applied,
        offset: 0,
        pageSize: TEMPLATE_LIST_PAGE_SIZE,
        sortBy: prev.sortBy,
        sortOrder: prev.sortOrder,
      });
      return areTemplateListParamsEqual(prev, next) ? prev : next;
    });
  }, [applied, templateType, setQueryParams]);

  useEffect(() => {
    if (!queryParams.templateType) return;
    fetchTemplates(queryParams);
  }, [queryParams, fetchTemplates]);

  useEffect(() => {
    if (!hasProcessingTemplates) return;

    const intervalId = window.setInterval(async () => {
      if (pollingInFlightRef.current) return;
      pollingInFlightRef.current = true;
      try {
        await Promise.resolve(fetchTemplates(queryParams));
      } finally {
        pollingInFlightRef.current = false;
      }
    }, 5000);

    return () => {
      window.clearInterval(intervalId);
      pollingInFlightRef.current = false;
    };
  }, [hasProcessingTemplates, fetchTemplates, queryParams]);

  useEffect(() => {
    fetchFilterOptions();
  }, [fetchFilterOptions]);

  const handleViewModeChange = (mode: ViewMode) => {
    setViewMode(mode);
  };

  const handleSort = useCallback(
    (field: string) => {
      setQueryParams(prev => ({
        ...prev,
        sortBy: field,
        sortOrder:
          prev.sortBy === field && prev.sortOrder === 'asc' ? 'desc' : 'asc',
      }));
    },
    [setQueryParams],
  );

  const handlePreview = useCallback((template: IEmailTemplate) => {
    setPreviewTemplate(template);
  }, []);

  const handleEdit = useCallback(
    (template: IEmailTemplate) => {
      navigate(getTemplateEditUrl(template.templateId, templateType));
    },
    [navigate, templateType],
  );

  const handleAIGenerate = async (data: TAIGenerateForm) => {
    const result = await generateAITemplate(data);
    if (result) {
      setShowAIModal(false);
      fetchTemplates();
    }
  };

  const handleDuplicate = useCallback(
    async (template: IEmailTemplate) => {
      const result = await duplicateTemplate(template.templateId);
      if (result) {
        fetchTemplates();
      }
    },
    [duplicateTemplate, fetchTemplates],
  );

  const handleDelete = useCallback((template: IEmailTemplate) => {
    setDeleteTemplateItem(template);
  }, []);

  const handleConfirmDelete = useCallback(
    async (id: string) => {
      const success = await deleteTemplate(id);
      if (success) {
        fetchTemplates();
      }
      return success;
    },
    [deleteTemplate, fetchTemplates],
  );

  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    resetFilters();
    setIsFilterExpanded(false);
    setFilterPanelKey(k => k + 1);
    setQueryParams({
      offset: 0,
      pageSize: TEMPLATE_LIST_PAGE_SIZE,
      searchParam: '',
      templateType,
      sortBy: 'createdAt',
      sortOrder: 'desc',
    });
  };

  const handleCreate = () => {
    navigate(getTemplateCreateUrl(templateType));
  };

  const shouldShowSkeleton =
    loading &&
    templates.items.length === 0 &&
    !hasProcessingTemplates;

  const shouldShowListOverlay =
    loading && templates.items.length > 0 && !hasProcessingTemplates;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold text-foreground">
            {pageConfig.label}
          </h2>
          <p className="text-muted-foreground">{pageConfig.description}</p>
        </div>
        <div className="flex items-center gap-3">
          {templateType === TemplateType.EMAIL && (
            <Button variant="outline" onClick={() => setShowAIModal(true)}>
              <Sparkles className="size-4" />
              Create with AI
            </Button>
          )}
          <Button onClick={handleCreate}>
            <Plus className="size-4" />
            {pageConfig.createLabel}
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2 text-lg">
                <PageIcon className="size-5" />
                {pageConfig.label} ({templates.total})
              </CardTitle>
              <CardDescription>{pageConfig.cardDescription}</CardDescription>
            </div>
            <ViewToggle value={viewMode} onChange={handleViewModeChange} />
          </div>
        </CardHeader>
        <CardContent>
          <div className="relative mb-4 flex w-full items-center gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder={pageConfig.searchPlaceholder}
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

              <EmailTemplateFilterPanel
                key={filterPanelKey}
                isExpanded={isFilterExpanded}
                templateType={templateType}
                filterOptions={filterOptions}
                applied={applied}
                onApply={applyFilters}
                onRemoveApplied={applyFilters}
                onClose={() => setIsFilterExpanded(false)}
                containerRef={filterDropdownRef}
              />
            </div>
            <Button variant="destructive" onClick={handleReset}>
              <RotateCcw className="mr-2 size-4" />
              Reset
            </Button>
          </div>

          <div className="relative min-h-[200px]">
            {shouldShowListOverlay && <Loader mode="container" blur />}
            {viewMode === 'grid' ? (
              <EmailTemplateGrid
                templates={templates.items}
                loading={shouldShowSkeleton}
                templateType={templateType}
                onPreview={handlePreview}
                onEdit={handleEdit}
                onDuplicate={handleDuplicate}
                onDelete={handleDelete}
              />
            ) : (
              <EmailTemplateTable
                templates={templates.items}
                loading={shouldShowSkeleton}
                templateType={templateType}
                sortBy={queryParams.sortBy || 'createdAt'}
                sortOrder={queryParams.sortOrder || 'desc'}
                onSort={handleSort}
                onPreview={handlePreview}
                onEdit={handleEdit}
                onDuplicate={handleDuplicate}
                onDelete={handleDelete}
              />
            )}
          </div>

          {templates.total > 0 && (
            <div className="mt-6 flex justify-end">
              <Pagination
                total={templates.total}
                perPage={queryParams.pageSize || TEMPLATE_LIST_PAGE_SIZE}
                currentPage={(queryParams.offset || 0) + 1}
                onPageChange={handlePageChange}
              />
            </div>
          )}
        </CardContent>
      </Card>

      <EmailTemplatePreviewModal
        isOpen={!!previewTemplate}
        onClose={() => setPreviewTemplate(null)}
        templateId={previewTemplate?.templateId || null}
        templateName={previewTemplate?.templateName || ''}
        templateType={previewTemplate?.templateType}
        getPreview={getTemplatePreview}
      />

      <EmailTemplateDeleteConfirm
        isOpen={!!deleteTemplateItem}
        onClose={() => setDeleteTemplateItem(null)}
        template={deleteTemplateItem}
        onDelete={handleConfirmDelete}
      />

      {templateType === TemplateType.EMAIL && (
        <AIGenerateModal
          isOpen={showAIModal}
          onClose={() => setShowAIModal(false)}
          onGenerate={handleAIGenerate}
          isGenerating={creating}
        />
      )}
    </div>
  );
};

export default TemplateLibraryContent;
