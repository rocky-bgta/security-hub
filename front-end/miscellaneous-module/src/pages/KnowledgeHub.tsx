import { sanitizeHtml } from 'home-module/security';
import {
  FileImage,
  FileText,
  Link2,
  Search,
  Video,
  ViewIcon,
} from 'lucide-react';
import { Fragment, useEffect, useState } from 'react';

import { Badge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'components/common/Dialog';
import { Input } from 'components/common/Input';
import { useAPI } from 'hooks/UseAPI';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { IKnowledgeHub, KnowledgeHubResourceType } from 'models/KnowledgeHub';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  buildFileUrl,
  cn,
  isSuccessResponse,
  objectToQueryString,
  sliceWords,
} from 'utils/Helper';
import { FILE_PATH_PREFIX, InitGetListParams } from 'utils/Constants';
import PdfViewer from 'components/PdfViewer';
import PolicyLoadingSkeleton from 'components/skeleton/PolicyLoadingSkeleton';
import { Link } from 'react-router-dom';
import Pagination from 'common/Pagination';
import useDebounce from 'hooks/UseDebounce';
import SearchSelect from 'components/SearchSelect';

const KnowledgeHub = () => {
  const [resourcesData, setResourcesData] = useState<IList<IKnowledgeHub>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [categories, setCategories] = useState<
    Array<{ id: string; name: string }>
  >([]);
  const [loading, setLoading] = useState<boolean>(true);
  const apiClient = useAPI();
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    pageSize: 9,
    offset: 0,
    categoryId: '',
  });

  const searchDebounce = useDebounce(queryString, 500);
  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);
  useEffect(() => {
    if (searchDebounce) {
      fetchData();
    }
  }, [searchDebounce]);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const response: IResponse<IList<IKnowledgeHub>> = await apiClient.get(
        API_END_POINTS.GET_KNOWLEDGE_HUB_ACTIVE_LIST + queryString,
      );
      setResourcesData(response.data);
    } catch (error) {
      console.error('Error fetching knowledge hub data:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_LATEST_NEWS_CATEGORY_LIST,
      );
      if (isSuccessResponse(response.statusCode)) {
        setCategories(response.data);
      }
    } catch (error) {
      console.error('Error fetching categories:', error);
    }
  };

  const getTypeIcon = (type: KnowledgeHubResourceType | string) => {
    const iconClass = 'size-5 shrink-0 text-current';
    switch (type) {
      case KnowledgeHubResourceType.DOCUMENT:
      case KnowledgeHubResourceType.GUIDE:
        return <FileText className={iconClass} />;
      case KnowledgeHubResourceType.VIDEO:
        return <Video className={iconClass} />;
      case KnowledgeHubResourceType.PDF:
        return <FileImage className={iconClass} />;
      case KnowledgeHubResourceType.EXTERNAL_LINK:
        return <Link2 className={iconClass} />;
      default:
        return <FileText className={iconClass} />;
    }
  };

  const getTypeColor = (type: KnowledgeHubResourceType | string) => {
    switch (type) {
      case KnowledgeHubResourceType.DOCUMENT:
      case KnowledgeHubResourceType.GUIDE:
        return 'bg-blue-100 text-blue-800';
      case KnowledgeHubResourceType.VIDEO:
        return 'bg-green-100 text-green-800';
      case KnowledgeHubResourceType.PDF:
        return 'bg-red-100 text-red-800';
      case KnowledgeHubResourceType.EXTERNAL_LINK:
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getTypeAccent = (type: KnowledgeHubResourceType | string) => {
    switch (type) {
      case KnowledgeHubResourceType.VIDEO:
        return {
          bar: 'bg-emerald-500/80',
          icon: 'bg-emerald-500/20 text-emerald-400',
        };
      case KnowledgeHubResourceType.PDF:
        return {
          bar: 'bg-primary/80',
          icon: 'bg-primary/20 text-primary',
        };
      case KnowledgeHubResourceType.EXTERNAL_LINK:
        return {
          bar: 'bg-violet-500/80',
          icon: 'bg-violet-500/20 text-violet-400',
        };
      case KnowledgeHubResourceType.DOCUMENT:
      case KnowledgeHubResourceType.GUIDE:
      default:
        return {
          bar: 'bg-sky-500/80',
          icon: 'bg-sky-500/20 text-sky-400',
        };
    }
  };

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    setQueryParams({
      ...InitGetListParams,
      categoryId: '',
      search: '',
    });
  };

  return (
    <div className="space-y-4 sm:space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white sm:text-3xl">
          Knowledge Hub
        </h1>
        <p className="mt-1 text-sm text-muted-foreground sm:text-base">
          Access training materials, guides, and resources.
        </p>
      </div>

      {/* Search and Filter */}
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4 lg:grid-cols-4">
        <div className="relative col-span-1 sm:col-span-2">
          <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search knowledge base..."
            value={queryParams.search}
            onChange={e =>
              setQueryParams(prevState => ({
                ...prevState,
                search: e.target.value,
              }))
            }
            className="pl-10"
          />
        </div>
        <SearchSelect
          placeholder="Filter by Category"
          value={queryParams.categoryId}
          onValueChange={(value: string) =>
            setQueryParams((prevState: IGetListParams) => ({
              ...prevState,
              categoryId: value,
            }))
          }
          items={categories.map(category => ({
            value: category.id,
            label: category.name,
          }))}
        />
        <Button variant="outline" onClick={handleReset}>
          Reset Filters
        </Button>
      </div>
      {loading ? (
        <PolicyLoadingSkeleton count={6} />
      ) : (
        <Fragment>
          {resourcesData?.items?.length === 0 ? (
            <div className="rounded-2xl border border-slate-700/50 bg-slate-900/30 px-6 py-16 text-center">
              <p className="text-slate-400">
                No content found matching your criteria.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 sm:gap-5 xl:grid-cols-3">
              {resourcesData?.items?.map((content: IKnowledgeHub) => (
                <article
                  key={content.id}
                  className="group relative flex flex-col overflow-hidden rounded-2xl border border-slate-700/40 bg-slate-900/40 shadow-lg ring-1 ring-slate-800/50 transition-all duration-300 hover:border-slate-600/60 hover:shadow-xl hover:ring-slate-700/50"
                >
                  {/* Accent bar by type */}
                  <div
                    className={`h-1 w-full shrink-0 ${getTypeAccent(content?.resourceType).bar}`}
                  />
                  <div className="flex flex-1 flex-col p-4 sm:p-5">
                    <div className="mb-3 flex items-start justify-between gap-2">
                      <span
                        className={`inline-flex size-10 shrink-0 items-center justify-center rounded-xl sm:size-11 ${getTypeAccent(content?.resourceType).icon}`}
                      >
                        {getTypeIcon(content?.resourceType)}
                      </span>
                      {content?.category?.name && (
                        <span className="rounded-full border border-slate-600/60 bg-slate-800/60 px-2.5 py-0.5 text-xs font-medium text-slate-300">
                          {content?.category?.name}
                        </span>
                      )}
                    </div>
                    <h3 className="mb-2 line-clamp-2 text-sm font-semibold leading-tight text-white sm:text-base">
                      {sliceWords(content?.name, 10)}
                    </h3>
                    {content?.content && (
                      <div
                        className="mb-4 line-clamp-3 flex-1 text-sm leading-relaxed text-slate-400"
                        dangerouslySetInnerHTML={{
                          __html: sanitizeHtml(
                            sliceWords(content?.content, 16),
                          ),
                        }}
                      />
                    )}
                    <Dialog>
                      <DialogTrigger asChild>
                        <button
                          type="button"
                          className="mt-auto inline-flex w-full items-center justify-center gap-2 rounded-xl border border-slate-600/60 bg-slate-800/60 py-2.5 text-sm font-medium text-slate-200 transition-colors hover:border-slate-500 hover:bg-slate-700/60 hover:text-white focus:outline-none focus:ring-2 focus:ring-slate-500 focus:ring-offset-2 focus:ring-offset-slate-900"
                        >
                          View Content
                          <ViewIcon className="size-4 opacity-70" />
                        </button>
                      </DialogTrigger>
                      <DialogContent
                        className={cn(
                          content?.resourceType === KnowledgeHubResourceType.PDF
                            ? 'max-w-5xl'
                            : 'max-w-3xl',
                          'max-h-[90vh] w-full overflow-hidden rounded-2xl border border-slate-700/60 bg-slate-900 p-0 shadow-2xl shadow-black/40 ',
                        )}
                      >
                        <DialogHeader className="border-b border-slate-700/60 bg-slate-800/50 px-4 py-3 sm:px-6 sm:py-4">
                          <DialogTitle className="flex flex-col gap-2">
                            <span className="flex items-center gap-2 text-base font-semibold text-white sm:gap-3 sm:text-lg">
                              <span
                                className={`inline-flex size-9 items-center justify-center rounded-lg sm:size-10 ${getTypeAccent(content?.resourceType).icon}`}
                              >
                                {getTypeIcon(content?.resourceType)}
                              </span>
                              <span className="break-words">
                                {content?.name}
                              </span>
                            </span>
                            <span className="flex flex-wrap items-center gap-2">
                              {content?.category?.name && (
                                <Badge
                                  variant="outline"
                                  className="rounded-full border-slate-600/60 bg-slate-800/40 px-2.5 py-0 text-xs text-slate-300"
                                >
                                  {content?.category?.name}
                                </Badge>
                              )}
                              <Badge
                                className={`rounded-full px-2.5 py-0 text-xs ${getTypeColor(content?.resourceType)}`}
                              >
                                {content?.resourceType}
                              </Badge>
                            </span>
                          </DialogTitle>
                        </DialogHeader>
                        <div className="flex flex-col gap-4 overflow-y-auto px-4 py-4 sm:gap-5 sm:px-6 sm:py-5">
                          <div
                            className="prose prose-invert prose-headings:text-white prose-a:text-sky-400 prose-a:no-underline hover:prose-a:underline max-w-none text-sm leading-relaxed text-slate-300"
                            dangerouslySetInnerHTML={{
                              __html: sanitizeHtml(content?.content ?? ''),
                            }}
                          />
                          {content?.resourceType ===
                          KnowledgeHubResourceType.PDF ? (
                            <PdfViewer
                              src={buildFileUrl(content.imageUrl)}
                              title={content.name}
                            />
                          ) : (
                            <div className="flex flex-wrap items-center gap-3 rounded-xl border border-slate-700/50 bg-slate-800/40 px-4 py-3">
                              <span className="text-sm font-medium text-slate-300">
                                View Content
                              </span>
                              {[
                                KnowledgeHubResourceType.GUIDE,
                                KnowledgeHubResourceType.EXTERNAL_LINK,
                                KnowledgeHubResourceType.DOCUMENT,
                                KnowledgeHubResourceType.VIDEO,
                              ].includes(content?.resourceType) && (
                                <Link
                                  to={FILE_PATH_PREFIX + content?.imageUrl}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                >
                                  <Button
                                    variant="outline"
                                    className="inline-flex items-center gap-2 rounded-lg border-slate-600/60 bg-slate-800/60 text-slate-200 hover:bg-slate-700/60 hover:text-white"
                                  >
                                    <ViewIcon className="size-4" />
                                    <span>
                                      View Content{' '}
                                      {content?.resourceType ===
                                        KnowledgeHubResourceType.GUIDE &&
                                        'Guide'}
                                      {content?.resourceType ===
                                        KnowledgeHubResourceType.EXTERNAL_LINK &&
                                        'External Link'}
                                      {content?.resourceType ===
                                        KnowledgeHubResourceType.DOCUMENT &&
                                        'Document'}
                                      {content?.resourceType ===
                                        KnowledgeHubResourceType.VIDEO &&
                                        'Video'}
                                    </span>
                                  </Button>
                                </Link>
                              )}
                            </div>
                          )}
                        </div>
                      </DialogContent>
                    </Dialog>
                  </div>
                </article>
              ))}
            </div>
          )}
        </Fragment>
      )}
      <div className="mt-4">
        <Pagination
          total={resourcesData?.total}
          perPage={resourcesData?.pageSize}
          onPageChange={handlePageChange}
        />
      </div>
    </div>
  );
};

export default KnowledgeHub;
