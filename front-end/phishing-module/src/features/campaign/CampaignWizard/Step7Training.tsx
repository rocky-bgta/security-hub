import {
  SyntheticEvent,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { toast } from 'react-toastify';
import {
  ChevronDown,
  Filter,
  Layers,
  Loader2,
  RotateCcw,
  Sparkles,
  Video,
  X,
} from 'lucide-react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import CustomSelect from 'common/CustomSelect';
import Pagination from 'common/Pagination';
import { useAPI } from 'hooks/UseAPI';
import useDropDown from 'hooks/UseDropDown';
import { useStore } from 'hooks/UseStore';
import {
  CampaignValidityUnit,
  CampaignChannel,
  ICampaignTrainingForm,
} from 'models/Campaign';
import { IDropdownItem } from 'models/DropDown';
import {
  ICustomSelectOption,
  ISelectOption,
  TMultiValue,
  TSingleValue,
} from 'models/Input';
import { IList, IResponse } from 'models/Global';
import { IAssignedLicense } from 'models/License';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn } from 'utils/Helper';

export interface Step7TrainingInitialData {
  name?: string;
  subPackageName?: string;
  description?: string;
  productId?: string;
  packageId?: string;
  productPackageId?: string;
  selectedModuleIds?: Array<{ id: string; topicName?: string }>;
  completionDays?: {
    durationUnit?: string;
    durationValue?: number;
  };
}

interface Step7TrainingProps {
  campaignId?: string;
  initialData?: Step7TrainingInitialData;
  /** Licensed product-package selected in campaign setup (step 1). */
  productPackageId?: string;
  onSubmit: (data: ICampaignTrainingForm) => void;
  onBack: () => void;
  isLoading?: boolean;
  channel?: CampaignChannel;
  /** Override recommended-topics API path (defaults to email campaign endpoint). */
  getRecommendedTopicsPath?: (campaignId: string) => string;
}

interface IRecommendedTopic {
  id: string;
  topicName: string;
  description: string;
  durationMinutes: number;
  thumbnailUrl: string;
  thumbnailPreviewUrl: string;
  contentTypeId: string;
  totalContentCount: number;
  status: string;
  tags: string[];
  categoryIds: string[];
  countryIds: string[];
  complianceIds: string[];
  createdAt: string;
}

interface ITopic {
  id: string;
  topicName: string;
  description: string;
  durationMinutes: number;
  categoryDetails?: Array<{ categoryName: string }>;
  contentTypeDetails?: { typeName: string };
}

interface ITopicResponse {
  topics: ITopic[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

interface IPrivateTopicProductPackage {
  productId: string;
  productName: string;
  packageIds: string[];
}

interface IPrivateTopic {
  id: string;
  topicName: string;
  durationMinutes: number;
  description: string;
  productPackages: IPrivateTopicProductPackage[];
  chapterIds: string[];
  totalContentCount: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

interface IFilterData {
  countryIds: ISelectOption[];
  complianceIds: ISelectOption[];
  categoryIds: ISelectOption[];
  contentTypeIds: ISelectOption[];
  durationMinutes: ISelectOption[];
  payloadTypeId: ISelectOption[];
  difficultyId: ISelectOption[];
  toneId: ISelectOption[];
  attackerPersonaId: ISelectOption[];
  socialEngineeringStrategyId: ISelectOption[];
  campaignObjectiveId: ISelectOption[];
  triggerEventId: ISelectOption[];
  attackTechniqueId: ISelectOption[];
  emotionalTriggerId: ISelectOption[];
  urgencyLevelId: ISelectOption[];
  brandId: ISelectOption[];
  callToActionId: ISelectOption[];
}

interface ICategory {
  id: string;
  categoryName: string;
}

interface IContentType {
  id: string;
  typeName: string;
}

interface ICompliance {
  id: string;
  complianceName: string;
}

interface ICountry {
  id: string;
  name: string;
}

const ADVANCED_FILTER_FIELDS = [
  {
    key: 'payloadTypeId' as const,
    label: 'Payload Type',
    fetchKey: 'fetchPayloadTypes',
  },
  {
    key: 'difficultyId' as const,
    label: 'Difficulty',
    fetchKey: 'fetchDifficulty',
  },
  { key: 'toneId' as const, label: 'Tone', fetchKey: 'fetchTones' },
  {
    key: 'attackerPersonaId' as const,
    label: 'Attacker Persona',
    fetchKey: 'fetchAttackerPersonas',
  },
  {
    key: 'socialEngineeringStrategyId' as const,
    label: 'Social Engineering Strategy',
    fetchKey: 'fetchSocialEngineeringStrategies',
  },
  {
    key: 'campaignObjectiveId' as const,
    label: 'Campaign Objective',
    fetchKey: 'fetchCampaignObjectives',
  },
  {
    key: 'triggerEventId' as const,
    label: 'Trigger Event',
    fetchKey: 'fetchTriggerEvents',
  },
  {
    key: 'attackTechniqueId' as const,
    label: 'Attack Technique',
    fetchKey: 'fetchAttackTechniques',
  },
  {
    key: 'emotionalTriggerId' as const,
    label: 'Emotional Trigger',
    fetchKey: 'fetchEmotionalTriggers',
  },
  {
    key: 'urgencyLevelId' as const,
    label: 'Urgency Level',
    fetchKey: 'fetchUrgencyLevels',
  },
  { key: 'brandId' as const, label: 'Brand', fetchKey: 'fetchBrands' },
  {
    key: 'callToActionId' as const,
    label: 'Call to Action',
    fetchKey: 'fetchCallToActions',
  },
];

const DURATION_OPTIONS: ISelectOption[] = [
  { id: 'ONE_TO_THREE', value: 'ONE_TO_THREE', label: '1 min - 3 min' },
  { id: 'THREE_TO_FIVE', value: 'THREE_TO_FIVE', label: '3 min - 5 min' },
  { id: 'FIVE_TO_TEN', value: 'FIVE_TO_TEN', label: '5 min - 10 min' },
  { id: 'TEN_TO_FIFTEEN', value: 'TEN_TO_FIFTEEN', label: '10 min - 15 min' },
  {
    id: 'FIFTEEN_TO_TWENTY',
    value: 'FIFTEEN_TO_TWENTY',
    label: '15 min - 20 min',
  },
  { id: 'TWENTY_PLUS', value: 'TWENTY_PLUS', label: '20 min +' },
];

const DEFAULT_FILTER_DATA: IFilterData = {
  countryIds: [],
  complianceIds: [],
  categoryIds: [],
  contentTypeIds: [],
  durationMinutes: [],
  payloadTypeId: [],
  difficultyId: [],
  toneId: [],
  attackerPersonaId: [],
  socialEngineeringStrategyId: [],
  campaignObjectiveId: [],
  triggerEventId: [],
  attackTechniqueId: [],
  emotionalTriggerId: [],
  urgencyLevelId: [],
  brandId: [],
  callToActionId: [],
};

export const Step7Training = ({
  campaignId,
  initialData,
  productPackageId,
  onSubmit,
  onBack,
  isLoading,
  channel = CampaignChannel.EMAIL,
  getRecommendedTopicsPath,
}: Step7TrainingProps) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();

  const [assignedLicenses, setAssignedLicenses] = useState<IAssignedLicense[]>(
    [],
  );
  const [licensesLoading, setLicensesLoading] = useState(true);
  const [productPackagesId, setProductPackagesId] = useState('');
  const [selectedProductId, setSelectedProductId] = useState('');
  const [selectedPackageId, setSelectedPackageId] = useState('');
  const [packageName, setPackageName] = useState('');

  const resolvedProductPackageId =
    productPackageId?.trim() || initialData?.productPackageId?.trim() || '';

  const [subPackageName, setSubPackageName] = useState('');
  const [subPackageNameError, setSubPackageNameError] = useState('');
  const [durationUnit, setDurationUnit] = useState<string>(
    CampaignValidityUnit.DAYS,
  );
  const [durationValue, setDurationValue] = useState<number | ''>('');
  const [completionDaysError, setCompletionDaysError] = useState('');

  const MAX_COMPLETION_DAYS = 365;
  const MAX_COMPLETION_WEEKS = 4;
  const MAX_COMPLETION_MONTHS = 12;
  const completionMaxValue =
    durationUnit === CampaignValidityUnit.MONTHS
      ? MAX_COMPLETION_MONTHS
      : durationUnit === CampaignValidityUnit.WEEKS
        ? MAX_COMPLETION_WEEKS
        : MAX_COMPLETION_DAYS;

  const [selectedTopics, setSelectedTopics] = useState<
    Array<{ id: string; topicName: string }>
  >([]);

  const [topics, setTopics] = useState<ITopicResponse>({
    topics: [],
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    hasNext: false,
    hasPrevious: false,
  });
  const [topicsLoading, setTopicsLoading] = useState(false);
  const [search, setSearch] = useState('');

  const [filterData, setFilterData] =
    useState<IFilterData>(DEFAULT_FILTER_DATA);
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);

  const [categoryOptions, setCategoryOptions] = useState<ISelectOption[]>([]);
  const [contentTypeOptions, setContentTypeOptions] = useState<ISelectOption[]>(
    [],
  );
  const [complianceOptions, setComplianceOptions] = useState<ISelectOption[]>(
    [],
  );
  const [countryOptions, setCountryOptions] = useState<ISelectOption[]>([]);

  const {
    fetchPayloadTypes,
    fetchDifficulty,
    fetchTones,
    fetchAttackerPersonas,
    fetchSocialEngineeringStrategies,
    fetchCampaignObjectives,
    fetchTriggerEvents,
    fetchAttackTechniques,
    fetchEmotionalTriggers,
    fetchUrgencyLevels,
    fetchBrands,
    fetchCallToActions,
  } = useDropDown();

  const advancedFetchers: Record<string, () => Promise<IList<IDropdownItem>>> =
    useMemo(
      () => ({
        fetchPayloadTypes: () => fetchPayloadTypes(channel),
        fetchDifficulty,
        fetchTones,
        fetchAttackerPersonas,
        fetchSocialEngineeringStrategies,
        fetchCampaignObjectives,
        fetchTriggerEvents,
        fetchAttackTechniques,
        fetchEmotionalTriggers,
        fetchUrgencyLevels,
        fetchBrands,
        fetchCallToActions,
      }),
      [
        channel,
        fetchPayloadTypes,
        fetchDifficulty,
        fetchTones,
        fetchAttackerPersonas,
        fetchSocialEngineeringStrategies,
        fetchCampaignObjectives,
        fetchTriggerEvents,
        fetchAttackTechniques,
        fetchEmotionalTriggers,
        fetchUrgencyLevels,
        fetchBrands,
        fetchCallToActions,
      ],
    );

  const [advancedFilterOptions, setAdvancedFilterOptions] = useState<
    Record<string, ISelectOption[]>
  >(Object.fromEntries(ADVANCED_FILTER_FIELDS.map(f => [f.key, []])));

  const [queryParams, setQueryParams] = useState({
    page: 1,
    size: 10,
  });

  const [recommendedTopics, setRecommendedTopics] = useState<
    IRecommendedTopic[]
  >([]);
  const [recommendedTopicsLoading, setRecommendedTopicsLoading] =
    useState(false);
  const hasAutoSelectedRecommendedRef = useRef(false);

  const PRIVATE_TOPICS_PAGE_SIZE = 10;
  const [showPrivateDeepfakeLibrary, setShowPrivateDeepfakeLibrary] =
    useState(false);
  const [privateTopics, setPrivateTopics] = useState<IPrivateTopic[]>([]);
  const [privateTopicsLoading, setPrivateTopicsLoading] = useState(false);
  const [privateTopicsTotal, setPrivateTopicsTotal] = useState(0);
  const [privateTopicsPage, setPrivateTopicsPage] = useState(1);

  const fetchAssignedProducts = useCallback(async () => {
    if (!userInfo?.userId) {
      setLicensesLoading(false);
      return;
    }

    setLicensesLoading(true);
    try {
      const response: IResponse<IList<IAssignedLicense>> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
          ':clientAdminId',
          userInfo.userId,
        ) + 'offset=0&pageSize=1000',
      );
      setAssignedLicenses(response.data?.items ?? []);
    } catch (error) {
      console.error('Error fetching assigned products:', error);
      toast.error('Failed to load assigned products');
      setAssignedLicenses([]);
    } finally {
      setLicensesLoading(false);
    }
  }, [apiClient, userInfo?.userId]);

  const fetchRecommendedTopics = useCallback(async () => {
    if (!campaignId) return;

    setRecommendedTopicsLoading(true);
    try {
      const topicsPath = getRecommendedTopicsPath
        ? getRecommendedTopicsPath(campaignId)
        : API_END_POINTS.CAMPAIGN_RECOMMENDED_TOPICS(campaignId);
      const response = await apiClient.get(
        topicsPath +
          'productId=' +
          selectedProductId +
          '&packageId=' +
          selectedPackageId +
          '&page=1&size=6',
      );
      const topics: IRecommendedTopic[] = response.data?.topics ?? [];
      setRecommendedTopics(topics);
    } catch (error) {
      console.error('Error fetching recommended topics:', error);
    } finally {
      setRecommendedTopicsLoading(false);
    }
  }, [
    apiClient,
    campaignId,
    selectedProductId,
    selectedPackageId,
    getRecommendedTopicsPath,
  ]);

  const fetchPrivateTopics = useCallback(async () => {
    setPrivateTopicsLoading(true);
    try {
      const offset = (privateTopicsPage - 1) * PRIVATE_TOPICS_PAGE_SIZE;
      const response: IResponse<IList<IPrivateTopic>> = await apiClient.get(
        API_END_POINTS.CMS_PRIVATE_TOPICS +
          `offset=${offset}&pageSize=${PRIVATE_TOPICS_PAGE_SIZE}&sortBy=createdAt&order=desc`,
      );
      setPrivateTopics(response.data?.items ?? []);
      setPrivateTopicsTotal(response.data?.total ?? 0);
    } catch (error) {
      console.error('Error fetching private topics:', error);
      toast.error('Failed to load private content library');
    } finally {
      setPrivateTopicsLoading(false);
    }
  }, [apiClient, privateTopicsPage]);

  useEffect(() => {
    if (userInfo?.userId) {
      fetchAssignedProducts();
    }
  }, [userInfo?.userId, fetchAssignedProducts]);

  useEffect(() => {
    if (campaignId && selectedPackageId) {
      fetchRecommendedTopics();
    }
  }, [campaignId, fetchRecommendedTopics, selectedPackageId]);

  useEffect(() => {
    if (showPrivateDeepfakeLibrary) {
      fetchPrivateTopics();
    }
  }, [showPrivateDeepfakeLibrary, privateTopicsPage, fetchPrivateTopics]);

  useEffect(() => {
    hasAutoSelectedRecommendedRef.current = false;
  }, [selectedPackageId]);

  useEffect(() => {
    if (hasAutoSelectedRecommendedRef.current) return;
    if (recommendedTopics.length === 0) return;

    if (initialData?.selectedModuleIds?.length) {
      hasAutoSelectedRecommendedRef.current = true;
      return;
    }

    hasAutoSelectedRecommendedRef.current = true;
    setSelectedTopics(prev => {
      const existingIds = new Set(prev.map(topic => topic.id));
      const toAdd = recommendedTopics
        .filter(topic => !existingIds.has(topic.id))
        .map(topic => ({ id: topic.id, topicName: topic.topicName }));

      if (toAdd.length === 0) return prev;
      return [...prev, ...toAdd];
    });
  }, [recommendedTopics, initialData?.selectedModuleIds]);

  useEffect(() => {
    if (!initialData) return;

    if (initialData.subPackageName !== undefined) {
      setSubPackageName(initialData.subPackageName);
    }

    if (initialData.completionDays) {
      setDurationUnit(
        initialData.completionDays.durationUnit || CampaignValidityUnit.DAYS,
      );
      setDurationValue(initialData.completionDays.durationValue ?? '');
    }

    const ids = initialData.selectedModuleIds ?? [];
    if (ids.length > 0) {
      setSelectedTopics(
        ids.map(item => ({ id: item.id, topicName: item.topicName ?? '' })),
      );
    }
  }, [initialData]);

  useEffect(() => {
    const license = assignedLicenses.find(
      item => item.id === resolvedProductPackageId,
    );
    if (!license) {
      setSelectedProductId('');
      setSelectedPackageId('');
      setProductPackagesId('');
      setPackageName('');
      return;
    }

    setSelectedProductId(license.productId);
    setSelectedPackageId(license.packageId);
    setProductPackagesId(license.id);
    setPackageName(license.packageDetails?.packageName ?? '');
  }, [assignedLicenses, resolvedProductPackageId]);

  const fetchFilterOptions = useCallback(async () => {
    try {
      const [categoryRes, contentTypeRes, complianceRes, countryRes] =
        await Promise.all([
          apiClient.get(API_END_POINTS.CMS_CATEGORY_LIST),
          apiClient.get(API_END_POINTS.CMS_CONTENT_TYPE_LIST),
          apiClient.get(API_END_POINTS.CMS_COMPLIANCE_LIST),
          apiClient.get(API_END_POINTS.REGISTRATION_COUNTRY_LIST),
        ]);

      setCategoryOptions(
        categoryRes.data?.map((item: ICategory) => ({
          id: item.id,
          label: item.categoryName,
          value: item.id,
        })) || [],
      );

      setContentTypeOptions(
        contentTypeRes.data?.map((item: IContentType) => ({
          id: item.id,
          label: item.typeName,
          value: item.id,
        })) || [],
      );

      setComplianceOptions(
        complianceRes.data?.map((item: ICompliance) => ({
          id: item.id,
          label: item.complianceName,
          value: item.id,
        })) || [],
      );

      setCountryOptions(
        countryRes.data?.map((item: ICountry) => ({
          id: item.id,
          label: item.name,
          value: item.id,
        })) || [],
      );
      ADVANCED_FILTER_FIELDS.forEach(async field => {
        const fetcher = advancedFetchers[field.fetchKey];
        if (fetcher) {
          try {
            const data = await fetcher();
            setAdvancedFilterOptions(prev => ({
              ...prev,
              [field.key]: (data.items || []).map((item: IDropdownItem) => ({
                id: item.id,
                label: item.name,
                value: item.id,
              })),
            }));
          } catch (e) {
            console.error(`Error fetching ${field.label}:`, e);
          }
        }
      });
    } catch (error) {
      console.error('Error fetching filter options:', error);
    }
  }, [apiClient, advancedFetchers]);

  const fetchTopics = useCallback(async () => {
    if (!selectedPackageId) return;

    setTopicsLoading(true);
    try {
      const response = await apiClient.post(
        API_END_POINTS.CMS_TOPIC_FILTER_BY_PACKAGE + selectedPackageId,
        {
          data: {
            countryIds: filterData.countryIds.map(item => item.id),
            complianceIds: filterData.complianceIds.map(item => item.id),
            categoryIds: filterData.categoryIds.map(item => item.id),
            contentTypeId: filterData.contentTypeIds.map(item => item.id),
            durationRanges: filterData.durationMinutes.map(item => item.value),
            payloadTypeIds: filterData.payloadTypeId.map(item => item.id),
            difficultyIds: filterData.difficultyId.map(item => item.id),
            toneIds: filterData.toneId.map(item => item.id),
            attackerPersonaIds: filterData.attackerPersonaId.map(
              item => item.id,
            ),
            socialEngineeringStrategyIds:
              filterData.socialEngineeringStrategyId.map(item => item.id),
            campaignObjectiveIds: filterData.campaignObjectiveId.map(
              item => item.id,
            ),
            triggerEventIds: filterData.triggerEventId.map(item => item.id),
            attackTechniqueIds: filterData.attackTechniqueId.map(
              item => item.id,
            ),
            emotionalTriggerIds: filterData.emotionalTriggerId.map(
              item => item.id,
            ),
            urgencyLevelIds: filterData.urgencyLevelId.map(item => item.id),
            brandIds: filterData.brandId.map(item => item.id),
            callToActionIds: filterData.callToActionId.map(item => item.id),
            searchText: search,
            page: queryParams.page,
            size: queryParams.size,
            status: 'ENABLED',
          },
        },
      );

      const responseTopics: ITopic[] = Array.isArray(response.data?.topics)
        ? response.data.topics
        : [];
      setTopics({
        ...response.data,
        topics: responseTopics,
      });
      setSelectedTopics(prev => {
        if (prev.length === 0) return prev;

        const topicNameById = new Map<string, string>(
          responseTopics.map((topic: ITopic) => [topic.id, topic.topicName]),
        );
        let changed = false;

        const next = prev.map(topic => {
          if (!topic.topicName && topicNameById.has(topic.id)) {
            changed = true;
            return {
              ...topic,
              topicName: topicNameById.get(topic.id) ?? topic.topicName,
            };
          }
          return topic;
        });

        return changed ? next : prev;
      });
    } catch (error) {
      console.error('Error fetching topics:', error);
      toast.error('Failed to fetch training modules');
    } finally {
      setTopicsLoading(false);
    }
  }, [apiClient, selectedPackageId, filterData, search, queryParams]);

  useEffect(() => {
    if (selectedPackageId) {
      fetchFilterOptions();
    }
  }, [selectedPackageId, fetchFilterOptions]);

  useEffect(() => {
    if (selectedProductId && selectedPackageId) {
      fetchTopics();
    }
  }, [selectedProductId, selectedPackageId, filterData, fetchTopics]);

  const handleFilterChange = useCallback((updates: Partial<IFilterData>) => {
    setFilterData(prev => ({ ...prev, ...updates }));
    setQueryParams(prev => ({ ...prev, page: 1 }));
  }, []);

  const handleMultiSelectChange = (
    key: keyof IFilterData,
    value: TMultiValue<ICustomSelectOption> | TSingleValue<ICustomSelectOption>,
  ) => {
    handleFilterChange({ [key]: Array.isArray(value) ? value : [value] });
  };

  const handleTopicToggle = useCallback(
    (topicId: string, topicName: string) => {
      setSelectedTopics(prev => {
        const isSelected = prev.some(topic => topic.id === topicId);
        if (isSelected) {
          return prev.filter(topic => topic.id !== topicId);
        }
        return [...prev, { id: topicId, topicName }];
      });
    },
    [],
  );

  const handleRemoveTopic = useCallback((topicId: string) => {
    setSelectedTopics(prev => prev.filter(topic => topic.id !== topicId));
  }, []);

  const handleReset = useCallback(() => {
    setFilterData(DEFAULT_FILTER_DATA);
    setSearch('');
    setQueryParams(prev => ({ ...prev, page: 1 }));
  }, []);

  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({ ...prev, page }));
  };

  const handleSubmit = (e?: SyntheticEvent) => {
    e?.preventDefault();
    if (!selectedProductId || !selectedPackageId || !productPackagesId) {
      toast.error(
        'Could not resolve the package selected in campaign setup. Go back to setup and choose a package.',
      );
      return;
    }

    const trimmedSubPackageName = subPackageName.trim();
    let nameErr = '';
    if (!trimmedSubPackageName) {
      nameErr = 'Sub package name is required';
    }
    setSubPackageNameError(nameErr);

    let completionErr = '';
    if (durationValue === '' || durationValue < 1) {
      completionErr = 'Completion period is required';
    } else if (durationValue > completionMaxValue) {
      const unitLabel =
        durationUnit === CampaignValidityUnit.MONTHS
          ? 'months'
          : durationUnit === CampaignValidityUnit.WEEKS
            ? 'weeks'
            : 'days';
      completionErr = `Maximum ${completionMaxValue} ${unitLabel}`;
    }
    setCompletionDaysError(completionErr);

    if (selectedTopics.length < 2 || completionErr || nameErr) {
      if (selectedTopics.length < 2) {
        toast.error('Please select at least two training modules');
      }
      if (nameErr) {
        toast.error('Sub package name is required');
      }
      if (durationValue === '' || durationValue < 1) {
        toast.error('Your Training Period length is missing');
      }
      return;
    }

    const topicIds = selectedTopics.map(topic => topic.id);
    onSubmit({
      name: packageName,
      subPackageName: trimmedSubPackageName,
      description: '',
      productId: selectedProductId,
      packageId: selectedPackageId,
      productPackageId: productPackagesId,
      clientId: userInfo?.userId ?? '',
      topicId: topicIds,
      topicIdDetails: selectedTopics.map(item => ({
        tid: item.id,
        topicName: item.topicName,
      })),
      completionDays: {
        durationUnit,
        durationValue: Number(durationValue),
      },
    });
  };

  const truncateText = (text: string, wordLimit: number) => {
    if (!text) return '';
    const words = text.split(' ');
    if (words.length <= wordLimit) return text;
    return words.slice(0, wordLimit).join(' ') + '...';
  };

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground">
          Training &amp; content bundle
        </h2>
        <p className="mt-2 text-muted-foreground">
          Set the completion period, then pick the modules assigned when users
          fail the simulation.
        </p>
      </div>

      <Card>
        <CardHeader className="pb-2">
          <div className="flex items-start gap-3">
            <div className="mt-0.5 rounded-md bg-primary/15 p-2 text-primary">
              <Layers className="size-5" />
            </div>
            <div>
              <CardTitle className="text-lg">Training package details</CardTitle>
              <p className="mt-1 text-sm text-muted-foreground">
                Name this training bundle and set how long learners have to
                finish it.
              </p>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div>
            <div className="space-y-5">
              <div className="space-y-2">
                <Label htmlFor="cw-sub-package-name">
                  Training name
                  <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="cw-sub-package-name"
                  value={subPackageName}
                  onChange={e => {
                    setSubPackageName(e.target.value);
                    setSubPackageNameError('');
                  }}
                  className={subPackageNameError ? 'border-destructive' : ''}
                  placeholder="e.g. Phishing recovery training"
                />
                {subPackageNameError && (
                  <p className="text-sm text-destructive">
                    {subPackageNameError}
                  </p>
                )}
              </div>
              <div className="grid grid-cols-2 items-start gap-4">
                <div className="space-y-2">
                  <Label>
                    Training Completion Period
                    <span className="text-destructive">*</span>
                  </Label>
                  <Select
                    value={durationUnit}
                    onValueChange={value => {
                      setDurationUnit(value);
                      setCompletionDaysError('');
                    }}
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Select unit" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={CampaignValidityUnit.DAYS}>
                        Days
                      </SelectItem>
                      <SelectItem value={CampaignValidityUnit.WEEKS}>
                        Weeks
                      </SelectItem>
                      <SelectItem value={CampaignValidityUnit.MONTHS}>
                        Months
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="cw-completion-value">
                    Training Period length{' '}
                    <span className="text-destructive">*</span>
                  </Label>
                  <Input
                    id="cw-completion-value"
                    type="number"
                    min={1}
                    max={completionMaxValue}
                    step={1}
                    value={durationValue}
                    onChange={e => {
                      const val = e.target.value;
                      setDurationValue(val === '' ? '' : Number(val));
                      setCompletionDaysError('');
                    }}
                    className={completionDaysError ? 'border-destructive' : ''}
                    placeholder="ex: 1, 2, 3, etc."
                  />
                  <p className="text-xs text-muted-foreground">
                    Between 1 and {completionMaxValue}{' '}
                    {durationUnit === CampaignValidityUnit.MONTHS
                      ? 'months'
                      : durationUnit === CampaignValidityUnit.WEEKS
                        ? 'weeks'
                        : 'days'}
                    .
                  </p>
                  {completionDaysError && (
                    <p className="text-sm text-destructive">
                      {completionDaysError}
                    </p>
                  )}
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {selectedProductId && (
        <Card>
          <div className="px-5 pt-5">
            <div className="mb-3 flex items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                <Filter className="size-5" />
                Filter training modules
              </CardTitle>
              <Button type="button" variant="outline" onClick={handleReset}>
                <RotateCcw className="size-4" />
                Reset
              </Button>
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div className="col-span-1">
                <Label htmlFor="searchTopic">Search module</Label>
                <Input
                  type="text"
                  placeholder="Search by name or keyword"
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="w-full"
                />
              </div>

              <div className="col-span-1">
                <Label htmlFor="country">Country</Label>
                <CustomSelect
                  name="country"
                  value={filterData.countryIds as ICustomSelectOption[]}
                  handleChange={value =>
                    handleMultiSelectChange('countryIds', value)
                  }
                  isMulti
                  data={countryOptions as ICustomSelectOption[]}
                  placeholder="Select countries"
                />
              </div>

              <div className="col-span-1">
                <Label htmlFor="compliance">Compliance</Label>
                <CustomSelect
                  name="compliance"
                  value={filterData.complianceIds as ICustomSelectOption[]}
                  handleChange={value =>
                    handleMultiSelectChange('complianceIds', value)
                  }
                  isMulti
                  data={complianceOptions as ICustomSelectOption[]}
                  placeholder="Select compliance"
                />
              </div>

              <div className="col-span-1">
                <Label htmlFor="categories">Categories</Label>
                <CustomSelect
                  name="categories"
                  value={filterData.categoryIds as ICustomSelectOption[]}
                  handleChange={value =>
                    handleMultiSelectChange('categoryIds', value)
                  }
                  isMulti
                  data={categoryOptions as ICustomSelectOption[]}
                  placeholder="Select categories"
                />
              </div>

              <div className="col-span-1">
                <Label htmlFor="contentTypes">Content types</Label>
                <CustomSelect
                  name="contentTypes"
                  value={filterData.contentTypeIds as ICustomSelectOption[]}
                  handleChange={value =>
                    handleMultiSelectChange('contentTypeIds', value)
                  }
                  isMulti
                  data={contentTypeOptions as ICustomSelectOption[]}
                  placeholder="Select content types"
                />
              </div>

              <div className="col-span-1">
                <Label htmlFor="durationRange">Duration range</Label>
                <CustomSelect
                  name="durationRange"
                  value={filterData.durationMinutes as ICustomSelectOption[]}
                  handleChange={value =>
                    handleMultiSelectChange('durationMinutes', value)
                  }
                  isMulti
                  data={DURATION_OPTIONS as ICustomSelectOption[]}
                  placeholder="Select duration ranges"
                />
              </div>

              <div className="col-span-full mt-3">
                <button
                  type="button"
                  onClick={() => setShowAdvancedFilters(prev => !prev)}
                  className="flex w-full items-center justify-center gap-2 rounded-md border border-card-border p-2 text-sm font-medium text-primary hover:text-primary/80"
                >
                  <ChevronDown
                    className={cn(
                      'size-4 transition-transform duration-300',
                      showAdvancedFilters ? 'rotate-180' : '',
                    )}
                  />
                  {showAdvancedFilters ? 'Hide' : 'Show'} Advanced Filters...
                </button>

                <div
                  className={cn(
                    'grid transition-all duration-300 ease-in-out',
                    showAdvancedFilters && 'mt-3 grid-rows-[1fr] opacity-100',
                    !showAdvancedFilters && 'grid-rows-[0fr] opacity-0',
                  )}
                >
                  <div className="overflow-hidden">
                    <div className="grid grid-cols-3 gap-3">
                      {ADVANCED_FILTER_FIELDS.map(field => (
                        <div key={field.key} className="col-span-1">
                          <Label htmlFor={field.key}>{field.label}</Label>
                          <CustomSelect
                            name={field.key}
                            value={
                              filterData[field.key] as ICustomSelectOption[]
                            }
                            handleChange={value =>
                              handleMultiSelectChange(field.key, value)
                            }
                            isMulti
                            data={
                              (advancedFilterOptions[field.key] ||
                                []) as ICustomSelectOption[]
                            }
                            placeholder={`Select ${field.label.toLowerCase()}`}
                          />
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {campaignId && (
            <div className="px-5 pb-2 pt-5">
              <div className="mb-3 flex items-center gap-2">
                <div className="rounded-md bg-amber-500/15 p-1.5 text-amber-600">
                  <Sparkles className="size-4" />
                </div>
                <h3 className="text-lg font-semibold">
                  AI Recommended Training Modules
                </h3>
              </div>
              <p className="mb-4 text-sm text-muted-foreground">
                Based on your campaign configuration, AI has recommended the
                following training topics. Select any to include them.
              </p>

              {recommendedTopicsLoading ? (
                <div className="flex h-[80px] items-center justify-center">
                  <Loader2 className="size-5 animate-spin text-amber-600" />
                  <span className="ml-2 text-sm text-muted-foreground">
                    Generating recommendations…
                  </span>
                </div>
              ) : recommendedTopics.length === 0 ? (
                <div className="rounded-lg border border-dashed border-muted-foreground/30 py-6 text-center text-sm text-muted-foreground">
                  No AI recommendations available for this campaign yet.
                </div>
              ) : (
                <div className="grid max-h-[300px] grid-cols-2 gap-3 overflow-y-auto">
                  {recommendedTopics.map(topic => {
                    const isSelected = selectedTopics.some(
                      t => t.id === topic.id,
                    );
                    return (
                      <div
                        key={topic.id}
                        className="cursor-pointer rounded-lg bg-primary/10 p-4 transition-colors"
                        onClick={() =>
                          handleTopicToggle(topic.id, topic.topicName)
                        }
                      >
                        <div className="flex items-start space-x-3">
                          <Checkbox
                            checked={isSelected}
                            onCheckedChange={() =>
                              handleTopicToggle(topic.id, topic.topicName)
                            }
                            onClick={e => e?.stopPropagation?.()}
                            className="mt-1"
                          />
                          <div className="min-w-0 flex-1">
                            <h4 className="font-medium text-foreground">
                              {topic.topicName}
                            </h4>
                            <p className="mt-1 text-sm text-muted-foreground">
                              {truncateText(topic.description, 12)}
                            </p>
                            <div className="mt-2 flex flex-wrap gap-1">
                              {topic.tags?.slice(0, 3).map((tag, idx) => (
                                <Badge
                                  key={idx}
                                  variant="secondary"
                                  className="text-xs"
                                >
                                  {tag}
                                </Badge>
                              ))}
                              <Badge variant="secondary" className="text-xs">
                                {topic.durationMinutes} min
                              </Badge>
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          <div className="px-5 pb-2 pt-5">
            <div className="mb-3 flex items-center gap-2">
              <div className="rounded-md bg-violet-500/15 p-1.5 text-violet-600">
                <Video className="size-4" />
              </div>
              <h3 className="text-lg font-semibold">
                My Content Library – Deepfake
              </h3>
            </div>
            <p className="mb-4 text-sm text-muted-foreground">
              Your private deepfake training content. Select any to include them
              in this campaign.
            </p>

            <Button
              type="button"
              variant="outline"
              onClick={() => setShowPrivateDeepfakeLibrary(prev => !prev)}
              className="w-full text-primary"
            >
              <ChevronDown
                className={cn(
                  'size-4 transition-transform duration-300',
                  showPrivateDeepfakeLibrary ? 'rotate-180' : '',
                )}
              />
              {showPrivateDeepfakeLibrary ? 'Hide' : 'Show'} My Content Library
              – Deepfake
            </Button>

            <div
              className={cn(
                'grid transition-all duration-300 ease-in-out',
                showPrivateDeepfakeLibrary &&
                  'mt-3 grid-rows-[1fr] opacity-100',
                !showPrivateDeepfakeLibrary && 'grid-rows-[0fr] opacity-0',
              )}
            >
              <div className="overflow-hidden">
                {privateTopicsLoading ? (
                  <div className="flex h-[80px] items-center justify-center">
                    <Loader2 className="size-5 animate-spin text-violet-600" />
                    <span className="ml-2 text-sm text-muted-foreground">
                      Loading private content…
                    </span>
                  </div>
                ) : privateTopics.length === 0 ? (
                  <div className="rounded-lg border border-dashed border-muted-foreground/30 py-6 text-center text-sm text-muted-foreground">
                    No private deepfake content available yet.
                  </div>
                ) : (
                  <>
                    <div className="grid max-h-[300px] grid-cols-2 gap-3 overflow-y-auto">
                      {privateTopics.map(topic => {
                        const isSelected = selectedTopics.some(
                          t => t.id === topic.id,
                        );
                        return (
                          <div
                            key={topic.id}
                            className="cursor-pointer rounded-lg bg-primary/10 p-4 transition-colors"
                            onClick={() =>
                              handleTopicToggle(topic.id, topic.topicName)
                            }
                          >
                            <div className="flex items-start space-x-3">
                              <Checkbox
                                checked={isSelected}
                                onCheckedChange={() =>
                                  handleTopicToggle(topic.id, topic.topicName)
                                }
                                onClick={e => e?.stopPropagation?.()}
                                className="mt-1"
                              />
                              <div className="min-w-0 flex-1">
                                <h4 className="font-medium text-foreground">
                                  {topic.topicName}
                                </h4>
                                <p className="mt-1 text-sm text-muted-foreground">
                                  {truncateText(topic.description, 10)}
                                </p>
                                <div className="mt-2 flex flex-wrap gap-1">
                                  {topic.productPackages?.[0]?.productName && (
                                    <Badge
                                      variant="secondary"
                                      className="text-xs"
                                    >
                                      {topic.productPackages[0].productName}
                                    </Badge>
                                  )}
                                  <Badge
                                    variant="secondary"
                                    className="text-xs"
                                  >
                                    {topic.totalContentCount} contents
                                  </Badge>
                                  <Badge
                                    variant="secondary"
                                    className="text-xs"
                                  >
                                    {topic.durationMinutes} min
                                  </Badge>
                                </div>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>

                    {privateTopicsTotal > PRIVATE_TOPICS_PAGE_SIZE && (
                      <div className="mt-4 flex justify-end">
                        <Pagination
                          total={privateTopicsTotal}
                          perPage={PRIVATE_TOPICS_PAGE_SIZE}
                          currentPage={privateTopicsPage}
                          onPageChange={setPrivateTopicsPage}
                        />
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
          </div>

          <CardContent>
            <p className="mt-5 text-xl font-semibold">
              Select training modules{' '}
              <span className="text-base text-primary">
                ({topics.totalElements || 0} modules available)
              </span>
            </p>
            <p className="mb-4 text-muted-foreground">
              Choose modules that users who fall for the simulation will be
              assigned.{' '}
              <span className="text-primary">
                At least two modules are required
              </span>
            </p>

            {selectedTopics.length > 0 && (
              <div className="mb-4 rounded-lg border border-card-border p-3">
                <p className="mb-2 text-sm font-medium text-primary">
                  Selected training modules ({selectedTopics.length})
                </p>
                <div className="flex flex-wrap gap-2">
                  {selectedTopics.map(topic => (
                    <span
                      key={topic.id}
                      className="flex items-center gap-1 rounded-full bg-primary/20 px-3 py-1 text-xs text-primary"
                    >
                      {topic.topicName || topic.id}
                      <button
                        type="button"
                        onClick={e => {
                          e.stopPropagation();
                          handleRemoveTopic(topic.id);
                        }}
                        className="ml-1 rounded-full p-0.5 hover:bg-primary/30"
                      >
                        <X className="size-3" />
                      </button>
                    </span>
                  ))}
                </div>
              </div>
            )}

            <div className="grid max-h-[400px] grid-cols-2 gap-4 overflow-y-auto">
              {topicsLoading ? (
                <div className="col-span-2 flex h-[100px] items-center justify-center">
                  <Loader2 className="size-6 animate-spin text-primary" />
                </div>
              ) : topics.topics.length === 0 ? (
                <div className="col-span-2 py-8 text-center text-muted-foreground">
                  No training modules found
                </div>
              ) : (
                topics.topics
                  .filter(
                    topic => !recommendedTopics.some(t => t.id === topic.id),
                  )
                  .map(topic => {
                    const isSelected = selectedTopics.some(
                      t => t.id === topic.id,
                    );
                    return (
                      <div
                        key={topic.id}
                        className="cursor-pointer rounded-lg bg-primary/10 p-4 transition-colors"
                        onClick={() =>
                          handleTopicToggle(topic.id, topic.topicName)
                        }
                      >
                        <div className="flex items-start space-x-3">
                          <Checkbox
                            checked={isSelected}
                            onCheckedChange={() =>
                              handleTopicToggle(topic.id, topic.topicName)
                            }
                            onClick={e => e?.stopPropagation?.()}
                            className="mt-1"
                          />
                          <div className="min-w-0 flex-1">
                            <h4 className="text-primary">{topic.topicName}</h4>
                            <p className="mt-1 text-sm text-muted-foreground">
                              {truncateText(topic.description, 10)}
                            </p>
                            <div className="mt-2 flex flex-wrap gap-1">
                              {topic.categoryDetails?.map((category, idx) => (
                                <Badge
                                  key={idx}
                                  variant="secondary"
                                  className="text-xs"
                                >
                                  {category.categoryName}
                                </Badge>
                              ))}
                              {topic.contentTypeDetails && (
                                <Badge variant="secondary" className="text-xs">
                                  {topic.contentTypeDetails.typeName}
                                </Badge>
                              )}
                              <Badge variant="secondary" className="text-xs">
                                {topic.durationMinutes} minutes
                              </Badge>
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })
              )}
            </div>

            {topics.totalElements > queryParams.size && (
              <div className="mt-4 flex justify-end">
                <Pagination
                  total={topics.totalElements}
                  perPage={queryParams.size}
                  currentPage={queryParams.page}
                  onPageChange={handlePageChange}
                />
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {!selectedProductId && (
        <Card>
          <CardContent className="py-10 text-center text-muted-foreground">
            {licensesLoading
              ? 'Loading training modules…'
              : 'Could not load training for the package selected in campaign setup.'}
          </CardContent>
        </Card>
      )}

      <div className="flex justify-between border-t border-card-border pt-6">
        <Button type="button" variant="outline" onClick={onBack}>
          <svg
            className="mr-2 size-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 19l-7-7 7-7"
            />
          </svg>
          Back
        </Button>
        <Button
          type="button"
          onClick={handleSubmit}
          disabled={
            !selectedProductId ||
            !selectedPackageId ||
            !productPackagesId ||
            selectedTopics.length < 2 ||
            isLoading
          }
        >
          {isLoading ? 'Saving...' : 'Save & Continue'}
          <svg
            className="ml-2 size-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 5l7 7-7 7"
            />
          </svg>
        </Button>
      </div>
    </div>
  );
};
