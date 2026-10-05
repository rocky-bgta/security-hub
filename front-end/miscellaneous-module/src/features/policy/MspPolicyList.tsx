import { sanitizeHtml } from 'home-module/security';
import { Fragment, useEffect, useState } from 'react';
import { Badge } from 'components/common/Badge';
import {
  Edit,
  FileImage,
  FileText,
  Monitor,
  Plus,
  Search,
  Trash,
  Video,
  ViewIcon,
} from 'lucide-react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from 'hooks/UseAPI';
import { IPolicy, IPolicyList } from 'models/Policy';
import { IGetListParams, IList, IResponse, ModalType } from 'models/Global';
import {
  isSuccessResponse,
  objectToQueryString,
  sliceWords,
} from 'utils/Helper';
import ViewPolicyContent from './ViewPolicyContent';
import PolicyLoadingSkeleton from 'components/skeleton/PolicyLoadingSkeleton';
import { InitGetListParams } from 'utils/Constants';
import useDebounce from 'hooks/UseDebounce';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'common/Select';
import { IDropdownData } from 'models/DropdownData';
import { Button } from 'common/Button';
import ActionPolicy from './ActionPolicy';
import ConfirmDialog from 'components/ConfirmDialog';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { useStore } from 'hooks/UseStore';
import SearchSelect from 'components/SearchSelect';

const MSPAdminPolicyList = () => {
  const apiClient = useAPI();
  const { userInfo } = useStore();

  const [selectedPolicy, setSelectedPolicy] = useState<IPolicyList | null>(
    null,
  );
  const [isOpenPolicyContent, setIsOpenPolicyContent] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState<ModalType>(ModalType.NONE);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [policies, setPolicies] = useState<IList<IPolicyList>>({
    items: [],
    offset: 0,
    pageSize: 0,
    total: 0,
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    policyName: '',
    offset: 0,
    pageSize: 9,
    industryId: '',
    countryId: '',
    isOwnPolicy: '',
  });
  const [dropdownData, setDropdownData] = useState<IDropdownData>({
    countries: [],
    industries: [],
  });

  const searchDebounce = useDebounce(queryString, 1000);



  const fetchDropdownData = async () => {
    try {
      const [countryRes, industryRes] = await Promise.all<
        [
          IResponse<Array<{ id: string; name: string }>>,

          IResponse<Array<{ id: string; name: string }>>,

        ]
      >([
        apiClient.get(API_END_POINTS.GET_ACTIVE_COUNTRY_LIST),
        apiClient.get(API_END_POINTS.GET_INDUSTRIES_LIST),
      ]);

      setDropdownData({
        countries: countryRes.data.map(item => ({
          id: item.id,
          name: item.name,
        })),
        industries: industryRes.data.map(item => ({
          id: item.id,
          name: item.name,
        })),
      });
    } catch (error) {
      console.error('Error fetching dropdown data:', error);
    }
  };

  useEffect(() => {
    setTimeout(() => {
      fetchDropdownData();
    }, 0);
  }, []);

  const fetchPolicies = async () => {
    setIsLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_CLIENT_AND_USER_POLICY_LIST + queryString,
      );
      if (isSuccessResponse(response.statusCode)) {
        setPolicies(response.data);
      }
    } catch (error) {
      console.error('Error fetching policies:', error);
    }
    setIsLoading(false);
  };

  useEffect(() => {
    setTimeout(() => {
      const resp = objectToQueryString(queryParams);
      setQueryString(resp);
    }, 0);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      setTimeout(() => {
        fetchPolicies();
      }, 0);
    }
  }, [searchDebounce]);

  const getTypeIcon = (type: string) => {
    const iconClass = 'size-5 shrink-0 text-current';
    switch (type) {
      case 'Text':
        return <FileText className={iconClass} />;
      case 'Video':
        return <Video className={iconClass} />;
      case 'PDF':
        return <FileImage className={iconClass} />;
      case 'PPT':
        return <Monitor className={iconClass} />;
      default:
        return <FileText className={iconClass} />;
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'Text':
        return 'bg-blue-100 text-blue-800';
      case 'Video':
        return 'bg-green-100 text-green-800';
      case 'PDF':
        return 'bg-red-100 text-red-800';
      case 'PPT':
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getTypeAccent = (type: string) => {
    switch (type) {
      case 'Video':
        return {
          bar: 'bg-emerald-500/80',
          icon: 'bg-emerald-500/20 text-emerald-400',
        };
      case 'PDF':
        return { bar: 'bg-rose-500/80', icon: 'bg-rose-500/20 text-rose-400' };
      case 'PPT':
        return {
          bar: 'bg-amber-500/80',
          icon: 'bg-amber-500/20 text-amber-400',
        };
      default:
        return { bar: 'bg-sky-500/80', icon: 'bg-sky-500/20 text-sky-400' };
    }
  };

  const handleViewPolicy = (policy: IPolicyList) => {
    setSelectedPolicy(policy);
    setIsOpenPolicyContent(true);
  };

  const handleRequestEditPolicy = (policy: IPolicyList) => {
    setIsModalOpen(ModalType.EDIT);
    setSelectedPolicy(policy);
  };

  const handleDeleteConfirm = (policy: IPolicyList) => {
    setSelectedPolicy(policy);
    setDeleteDialogOpen(true);
  };

  const handleDeletePolicy = async () => {
    setDeleteLoading(true);
    try {
      const response = await apiClient.del(
        API_END_POINTS.DELETE_POLICY.replace(':id', selectedPolicy?.id ?? ''),
      );
      if (isSuccessResponse(response.statusCode)) {
        fetchPolicies();
      }
    } catch (error) {
      console.error('Error deleting policy:', error);
    } finally {
      setDeleteLoading(false);
      setDeleteDialogOpen(false);
      setSelectedPolicy(null);
    }
  };

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-white">
              Policy List
            </h1>
            <p className="text-muted-foreground">Access policies and resources</p>
          </div>
          <Button onClick={() => setIsModalOpen(ModalType.ADD)}>
            <Plus className="mr-2 size-4" />
            Create New Policy
          </Button>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Filters</CardTitle>
            <CardDescription>
              Search and filter policies by criteria
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-4">
              <div className="flex items-center gap-2"><div className="relative w-3/4">
                <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search content..."
                  value={queryParams.policyName ?? ''}
                  onChange={e =>
                    setQueryParams(prevState => ({
                      ...prevState,
                      policyName: e.target.value,
                    }))
                  }
                  className="w-full pl-10"
                />
              </div>
                <Select
                  value={queryParams.isOwnPolicy ?? ''}
                  onValueChange={value =>
                    setQueryParams((prevState: IGetListParams) => ({
                      ...prevState,
                      isOwnPolicy: value === 'all' ? '' : value,
                    }))
                  }
                >
                  <SelectTrigger className="w-1/4">
                    <SelectValue placeholder="Filter by own policy" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All</SelectItem>
                    <SelectItem value="true">My policies only</SelectItem>
                    <SelectItem value="false">Assigned only</SelectItem>
                  </SelectContent>
                </Select></div>
              <div className="grid grid-cols-3 gap-4">
                <SearchSelect placeholder="Filter by industry" value={queryParams.industryId} onValueChange={(value: string) => setQueryParams((prevState: IGetListParams) => ({ ...prevState, industryId: value }))} items={dropdownData.industries?.map(industry => ({
                  value: industry.id,
                  label: industry.name,
                })) ?? []} />
                <SearchSelect placeholder="Filter by country" value={queryParams.countryId} onValueChange={(value: string) => setQueryParams((prevState: IGetListParams) => ({ ...prevState, countryId: value }))} items={dropdownData.countries?.map(country => ({
                  value: country.id,
                  label: country.name,
                })) ?? []} />
                <Button
                  variant="outline"
                  onClick={() =>
                    setQueryParams((prevState: IGetListParams) => ({
                      ...prevState,
                      policyName: '',
                      industryId: '',
                      countryId: '',
                      isOwnPolicy: '',
                    }))
                  }
                >
                  Reset
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
      {/* Content Grid */}
      {isLoading ? (
        <PolicyLoadingSkeleton />
      ) : policies.items.length === 0 ? (
        <div className="rounded-2xl border border-slate-700/50 bg-slate-900/30 px-6 py-16 text-center">
          <p className="text-slate-400">
            {queryParams.policyName
              ? 'No content found matching your criteria.'
              : 'No topics assigned yet.'}
          </p>
        </div>
      ) : (
        <Fragment>
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-3">
            {policies.items.map(policy => (
              <article
                key={policy.id}
                className="group relative flex flex-col overflow-hidden rounded-2xl border border-slate-700/40 bg-slate-900/40 shadow-lg ring-1 ring-slate-800/50 transition-all duration-300 hover:border-slate-600/60 hover:shadow-xl hover:ring-slate-700/50"
              >
                {/* Accent bar by type */}
                <div
                  className={`h-1 w-full shrink-0 ${getTypeAccent(policy.policyTypeName).bar}`}
                />
                <div className="flex flex-1 flex-col p-5">
                  <div className="mb-3 flex items-start justify-between gap-2">
                    <span
                      className={`inline-flex size-11 shrink-0 items-center justify-center rounded-xl ${getTypeAccent(policy.policyTypeName).icon}`}
                    >
                      {getTypeIcon(policy.policyTypeName)}
                    </span>
                    <Badge
                      className={`rounded-full px-2.5 py-0.5 text-xs ${getTypeColor(policy.policyTypeName)}`}
                    >
                      {policy.policyTypeName}
                    </Badge>
                  </div>
                  <h3 className="mb-2 line-clamp-2 text-base font-semibold leading-tight text-white">
                    {policy.policyName}
                  </h3>
                  {policy.description && (
                    <div
                      className="mb-4 line-clamp-3 flex-1 text-sm leading-relaxed text-slate-400"
                      dangerouslySetInnerHTML={{
                        __html: sanitizeHtml(sliceWords(policy.description, 20)),
                      }}
                    />
                  )}
                  <div className="mt-auto flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleViewPolicy(policy)}
                      className="inline-flex flex-1 items-center justify-center gap-2 rounded-xl border border-slate-600/60 bg-slate-800/60 py-2.5 text-sm font-medium text-slate-200 transition-colors hover:border-slate-500 hover:bg-slate-700/60 hover:text-white focus:outline-none focus:ring-2 focus:ring-slate-500 focus:ring-offset-2 focus:ring-offset-slate-900"
                    >
                      View
                      <ViewIcon className="size-4 opacity-70" />
                    </button>
                    {policy.clientAdminId === userInfo?.userId && (
                      <>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="shrink-0 text-slate-400 hover:text-slate-200"
                          onClick={() => handleRequestEditPolicy(policy)}
                          title="Edit"
                        >
                          <Edit className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="shrink-0 text-slate-400 hover:text-destructive"
                          onClick={() => handleDeleteConfirm(policy)}
                          title="Delete"
                        >
                          <Trash className="size-4" />
                        </Button>
                      </>
                    )}
                  </div>
                </div>
              </article>
            ))}
          </div>
        </Fragment>
      )}
      <div className="mt-4">
        <Pagination
          total={policies.total}
          perPage={policies.pageSize}
          onPageChange={handlePageChange}
        />
      </div>
      {isOpenPolicyContent && selectedPolicy && (
        <ViewPolicyContent
          isOpen={isOpenPolicyContent}
          onClose={() => setIsOpenPolicyContent(false)}
          policy={selectedPolicy}
        />
      )}
      {isModalOpen === ModalType.ADD && (
        <ActionPolicy
          isOpen={isModalOpen === ModalType.ADD}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={() => {
            fetchPolicies();
          }}
        />
      )}
      {isModalOpen === ModalType.EDIT && selectedPolicy && (
        <ActionPolicy
          isOpen={isModalOpen === ModalType.EDIT}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={() => {
            fetchPolicies();
          }}
          selectedPolicy={selectedPolicy as IPolicy}
        />
      )}

      <ConfirmDialog
        isOpen={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleDeletePolicy}
        message={`Are you sure you want to delete "${selectedPolicy?.policyName}"?`}
        buttonText="Delete"
        loading={deleteLoading}
        loadingText="Deleting..."
      />
    </div>
  );
};

export default MSPAdminPolicyList;
