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
import Pagination from 'common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { Tabs, TabsList, TabsTrigger } from 'common/Tabs';
import { TableSkeleton } from 'components/LoadingSkeleton';
import ConfigurationModal from 'features/configuration/ConfigurationModal';
import useDebounce from 'hooks/UseDebounce';
import { useAPI } from 'hooks/UseAPI';
import {
  CampaignChannel,
  CHANNEL_FILTERS,
  getCampaignChannelLabel,
} from 'models/Campaign';
import { IList, IResponse } from 'models/Global';
import { Pencil, Plus, Search, Trash2 } from 'lucide-react';
import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import ConfirmDialog from 'components/ConfirmDialog';
import { API_END_POINTS } from 'routes/APIEndpoints';

type TConfigurationTabKey =
  | 'payload-types'
  | 'landing-page-categories'
  | 'data-capture-types'
  | 'campaign-objectives'
  | 'personalization-levels'
  | 'brands'
  | 'deception-levels'
  | 'tones'
  | 'attacker-personas'
  | 'call-to-actions'
  | 'social-engineering-strategies'
  | 'emotional-triggers'
  | 'attack-techniques'
  | 'trigger-events'
  | 'expected-user-actions'
  | 'constraints-data'
  | 'difficulties'
  | 'urgency-levels';

interface IConfigurationItem {
  id: string;
  name: string;
  description: string;
  displayOrder: number;
  isDefault: boolean;
  isActive: boolean;
  channel?: CampaignChannel;
  createdAt?: string;
  updatedAt?: string;
}

interface IConfigurationPayload {
  name: string;
  description: string;
  displayOrder: number;
  isDefault: boolean;
  isActive: boolean;
  channel?: CampaignChannel;
}

type TActiveFilter = 'all' | 'true' | 'false';
type TChannelFilter = CampaignChannel | 'all';

const PAGE_SIZE = 10;

const TAB_CONFIG: Array<{ key: TConfigurationTabKey; label: string }> = [
  { key: 'payload-types', label: 'Payload Types' },
  { key: 'landing-page-categories', label: 'Landing Page Categories' },
  { key: 'data-capture-types', label: 'Data Capture Types' },
  { key: 'campaign-objectives', label: 'Campaign Objectives' },
  { key: 'personalization-levels', label: 'Personalization Levels' },
  { key: 'brands', label: 'Brands' },
  { key: 'deception-levels', label: 'Deception Levels' },
  { key: 'tones', label: 'Tones' },
  { key: 'attacker-personas', label: 'Attacker Personas' },
  { key: 'call-to-actions', label: 'Call To Actions' },
  {
    key: 'social-engineering-strategies',
    label: 'Social Engineering Strategies',
  },
  { key: 'emotional-triggers', label: 'Emotional Triggers' },
  { key: 'attack-techniques', label: 'Attack Techniques' },
  { key: 'trigger-events', label: 'Trigger Events' },
  { key: 'expected-user-actions', label: 'Expected User Actions' },
  { key: 'constraints-data', label: 'Constraints Data' },
  { key: 'difficulties', label: 'Difficulties' },
  { key: 'urgency-levels', label: 'Urgency Levels' },
];

const TAB_ENDPOINTS: Record<TConfigurationTabKey, string> = {
  'payload-types': API_END_POINTS.CONFIGURATION_PAYLOAD_TYPES,
  'landing-page-categories':
    API_END_POINTS.CONFIGURATION_LANDING_PAGE_CATEGORIES,
  'data-capture-types': API_END_POINTS.CONFIGURATION_DATA_CAPTURE_TYPES,
  'campaign-objectives': API_END_POINTS.CONFIGURATION_CAMPAIGN_OBJECTIVES,
  'personalization-levels':
    API_END_POINTS.CONFIGURATION_PERSONALIZATION_LEVELS,
  brands: API_END_POINTS.CONFIGURATION_BRANDS,
  'deception-levels': API_END_POINTS.CONFIGURATION_DECEPTION_LEVELS,
  tones: API_END_POINTS.CONFIGURATION_TONES,
  'attacker-personas': API_END_POINTS.CONFIGURATION_ATTACKER_PERSONAS,
  'call-to-actions': API_END_POINTS.CONFIGURATION_CALL_TO_ACTIONS,
  'social-engineering-strategies':
    API_END_POINTS.CONFIGURATION_SOCIAL_ENGINEERING_STRATEGIES,
  'emotional-triggers': API_END_POINTS.CONFIGURATION_EMOTIONAL_TRIGGERS,
  'attack-techniques': API_END_POINTS.CONFIGURATION_ATTACK_TECHNIQUES,
  'trigger-events': API_END_POINTS.CONFIGURATION_TRIGGER_EVENTS,
  'expected-user-actions': API_END_POINTS.CONFIGURATION_EXPECTED_USER_ACTIONS,
  'constraints-data': API_END_POINTS.CONFIGURATION_CONSTRAINTS_DATA,
  difficulties: API_END_POINTS.CONFIGURATION_DIFFICULTIES,
  'urgency-levels': API_END_POINTS.CONFIGURATION_URGENCY_LEVELS,
};

const toInitialForm = (tabKey?: TConfigurationTabKey): IConfigurationPayload => ({
  name: '',
  description: '',
  displayOrder: 0,
  isDefault: false,
  isActive: true,
  ...(tabKey === 'payload-types'
    ? { channel: CampaignChannel.EMAIL }
    : {}),
});

const Configurations = () => {
  const { get, post, put, del } = useAPI();

  const [activeTab, setActiveTab] =
    useState<TConfigurationTabKey>('payload-types');
  const [list, setList] = useState<IList<IConfigurationItem>>({
    offset: 0,
    pageSize: PAGE_SIZE,
    total: 0,
    items: [],
  });
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [searchParam, setSearchParam] = useState('');
  const [activeFilter, setActiveFilter] = useState<TActiveFilter>('all');
  const [channelFilter, setChannelFilter] = useState<TChannelFilter>('all');
  const [currentPage, setCurrentPage] = useState(1);
  const debouncedSearch = useDebounce(searchParam, 350);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<IConfigurationItem | null>(
    null,
  );
  const [form, setForm] = useState<IConfigurationPayload>(toInitialForm());
  const [formErrors, setFormErrors] = useState<
    Partial<Record<keyof IConfigurationPayload, string>>
  >({});

  const [deleteCandidate, setDeleteCandidate] =
    useState<IConfigurationItem | null>(null);

  const activeTabLabel = useMemo(
    () =>
      TAB_CONFIG.find(tab => tab.key === activeTab)?.label ?? 'Configuration',
    [activeTab],
  );

  const getModulePath = useCallback(
    (tabKey: TConfigurationTabKey) => TAB_ENDPOINTS[tabKey],
    [],
  );

  const isPayloadTypesTab = activeTab === 'payload-types';

  const loadItems = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const queryString = objectToQueryString({
        offset: currentPage - 1,
        pageSize: PAGE_SIZE,
        searchParam: debouncedSearch,
        isActive: activeFilter === 'all' ? undefined : activeFilter === 'true',
        sortOrder: 'asc',
        sortBy: 'displayOrder',
        ...(isPayloadTypesTab && channelFilter !== 'all'
          ? { channel: channelFilter }
          : {}),
      });
      const response = (await get(
        `${getModulePath(activeTab)}?${queryString}`,
      )) as IResponse<IList<IConfigurationItem>> | undefined;

      setList({
        offset: response?.data?.offset ?? 0,
        pageSize: response?.data?.pageSize ?? PAGE_SIZE,
        total: response?.data?.total ?? 0,
        items: response?.data?.items ?? [],
      });
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : `Failed to load ${activeTabLabel}`;
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [
    activeFilter,
    activeTab,
    activeTabLabel,
    channelFilter,
    currentPage,
    debouncedSearch,
    get,
    getModulePath,
    isPayloadTypesTab,
  ]);

  useEffect(() => {
    loadItems();
  }, [loadItems]);

  useEffect(() => {
    setCurrentPage(1);
  }, [activeTab, debouncedSearch, activeFilter, channelFilter]);

  const openCreateModal = () => {
    setEditingItem(null);
    setForm(toInitialForm(activeTab));
    setFormErrors({});
    setIsModalOpen(true);
  };

  const openEditModal = (item: IConfigurationItem) => {
    setEditingItem(item);
    setForm({
      name: item.name,
      description: item.description || '',
      displayOrder: item.displayOrder,
      isDefault: item.isDefault,
      isActive: item.isActive,
      ...(isPayloadTypesTab ? { channel: item.channel ?? CampaignChannel.EMAIL } : {}),
    });
    setFormErrors({});
    setIsModalOpen(true);
  };

  const validateForm = () => {
    const errors: Partial<Record<keyof IConfigurationPayload, string>> = {};
    if (!form.name.trim()) {
      errors.name = 'Name is required.';
    } else if (form.name.trim().length > 120) {
      errors.name = 'Name must be within 120 characters.';
    }

    if (form.description.trim().length > 500) {
      errors.description = 'Description must be within 500 characters.';
    }

    if (!Number.isInteger(form.displayOrder) || form.displayOrder < 0) {
      errors.displayOrder = 'Display order must be a non-negative integer.';
    }

    if (isPayloadTypesTab && !form.channel) {
      errors.channel = 'Channel is required.';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    if (!validateForm()) {
      return;
    }

    const payload: IConfigurationPayload = {
      name: form.name.trim(),
      description: form.description.trim(),
      displayOrder: form.displayOrder,
      isDefault: form.isDefault,
      isActive: form.isActive,
      ...(isPayloadTypesTab && form.channel ? { channel: form.channel } : {}),
    };

    setSubmitting(true);
    try {
      if (editingItem) {
        const response = await put(
          `${getModulePath(activeTab)}/${editingItem.id}`,
          { data: payload },
        );
        if (isSuccessResponse(response.statusCode)) {
          toast.success(`${activeTabLabel} updated successfully`);
        } else {
          toast.error(response.message);
        }
      } else {
        const response = await post(getModulePath(activeTab), {
          data: payload,
        });
        if (isSuccessResponse(response.statusCode)) {
          toast.success(`${activeTabLabel} created successfully`);
        } else {
          toast.error(response.message);
        }
      }

      setIsModalOpen(false);
      setEditingItem(null);
      await loadItems();
    } catch (err: unknown) {
      toast.error(
        err instanceof Error ? err.message : `Failed to save ${activeTabLabel}`,
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteCandidate) {
      return;
    }

    setSubmitting(true);
    try {
      await del(`${getModulePath(activeTab)}/${deleteCandidate.id}`);
      toast.success(`${activeTabLabel} deleted successfully`);
      setDeleteCandidate(null);
      await loadItems();
    } catch (err: unknown) {
      toast.error(
        err instanceof Error
          ? err.message
          : `Failed to delete ${activeTabLabel}`,
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">
            Configuration Management
          </h1>
          <p className="mt-1 text-muted-foreground">
            Manage reusable phishing configuration modules from one place.
          </p>
        </div>
        <Button onClick={openCreateModal}>
          <Plus className="size-4" />
          Create {activeTabLabel}
        </Button>
      </div>

      <Tabs
        value={activeTab}
        onValueChange={value => setActiveTab(value as TConfigurationTabKey)}
      >
        <TabsList className="flex h-auto w-full flex-wrap justify-start gap-1 p-2">
          {TAB_CONFIG.map(tab => (
            <TabsTrigger key={tab.key} value={tab.key} className="text-xs">
              {tab.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      <Card>
        <CardHeader>
          <CardTitle>{activeTabLabel}</CardTitle>
          <CardDescription>
            Search, filter, and maintain {activeTabLabel.toLowerCase()}.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-col gap-3 md:flex-row">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                value={searchParam}
                onChange={e => setSearchParam(e.target.value)}
                placeholder={`Search ${activeTabLabel.toLowerCase()}...`}
                className="pl-9"
              />
            </div>
            <div className="w-full md:w-56">
              <Select
                value={activeFilter}
                onValueChange={value => setActiveFilter(value as TActiveFilter)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Filter active status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="true">Active</SelectItem>
                  <SelectItem value="false">Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {isPayloadTypesTab && (
              <div className="w-full md:w-56">
                <Select
                  value={channelFilter}
                  onValueChange={value =>
                    setChannelFilter(value as TChannelFilter)
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Filter channel" />
                  </SelectTrigger>
                  <SelectContent>
                    {CHANNEL_FILTERS.map(filter => (
                      <SelectItem key={filter.value} value={filter.value}>
                        {filter.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}
          </div>

          {loading ? (
            <TableSkeleton count={6} />
          ) : error ? (
            <div className="rounded border border-vibrant-red/40 bg-vibrant-red/10 p-4 text-sm text-vibrant-red">
              <p>{error}</p>
              <Button
                variant="outline"
                className="mt-3"
                onClick={() => loadItems()}
                disabled={loading}
              >
                Retry
              </Button>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  {isPayloadTypesTab && <TableHead>Channel</TableHead>}
                  <TableHead>Description</TableHead>
                  <TableHead>Display Order</TableHead>
                  <TableHead>Default</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-center">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.items.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={isPayloadTypesTab ? 7 : 6}
                      className="text-center"
                    >
                      No {activeTabLabel.toLowerCase()} found.
                    </TableCell>
                  </TableRow>
                ) : (
                  list.items.map(item => (
                    <TableRow key={item.id}>
                      <TableCell>{item.name}</TableCell>
                      {isPayloadTypesTab && (
                        <TableCell>
                          {item.channel
                            ? getCampaignChannelLabel(item.channel)
                            : '-'}
                        </TableCell>
                      )}
                      <TableCell className="max-w-[360px] truncate">
                        {item.description || '-'}
                      </TableCell>
                      <TableCell>{item.displayOrder}</TableCell>
                      <TableCell>
                        <Badge
                          variant={item.isDefault ? 'default' : 'secondary'}
                        >
                          {item.isDefault ? 'Yes' : 'No'}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge variant={item.isActive ? 'default' : 'warning'}>
                          {item.isActive ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => openEditModal(item)}
                          >
                            <Pencil className="size-3" />
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setDeleteCandidate(item)}
                          >
                            <Trash2 className="size-3" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}

          <div className="flex justify-end">
            <Pagination
              total={list.total}
              perPage={list.pageSize || PAGE_SIZE}
              currentPage={currentPage}
              onPageChange={setCurrentPage}
            />
          </div>
        </CardContent>
      </Card>

      <ConfigurationModal
        isOpen={isModalOpen}
        activeTabLabel={activeTabLabel}
        editingItem={editingItem}
        form={form}
        formErrors={formErrors}
        submitting={submitting}
        showChannelField={isPayloadTypesTab}
        onOpenChange={setIsModalOpen}
        onFieldChange={(field, value) =>
          setForm(prev => ({ ...prev, [field]: value }))
        }
        onSubmit={handleSave}
      />

      <ConfirmDialog
        isOpen={!!deleteCandidate}
        onClose={() => setDeleteCandidate(null)}
        onConfirm={handleDelete}
        message={`Are you sure you want to delete "${deleteCandidate?.name || ''}"? This action cannot be undone.`}
        loading={submitting}
        loadingText="Deleting..."
        buttonText="Delete"
      />
    </div>
  );
};

export default Configurations;
