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
import ViewToggle from 'components/ViewToggle';
import useDebounce from 'hooks/UseDebounce';
import useSmsServerConfigurations from 'hooks/UseSmsServerConfigurations';
import { MessageSquare, PlusIcon } from 'lucide-react';
import {
  ISmsServerConfiguration,
  ISmsServerConfigurationForm,
  ISmsServerConfigurationListParams,
} from 'models/SmsServerConfiguration';
import { useCallback, useEffect, useState } from 'react';
import SmsServerConfigurationDeleteConfirm from './SmsServerConfigurationDeleteConfirm';
import SmsServerConfigurationFormModal from './SmsServerConfigurationFormModal';
import SmsServerConfigurationGrid from './SmsServerConfigurationGrid';
import SmsServerConfigurationTable from './SmsServerConfigurationTable';
import SmsServerConfigurationViewModal from './SmsServerConfigurationViewModal';

const PAGE_SIZE = 12;
const VIEW_MODE_KEY = 'senderProfileView_SMS';

/**
 * SMS server configuration list page content.
 */
const SmsServerConfigurationListContent = () => {
  const {
    configurations,
    loading: smsLoading,
    saving: smsSaving,
    fetchConfigurations,
    getConfigurationById,
    createConfiguration,
    updateConfiguration,
    deleteConfiguration,
  } = useSmsServerConfigurations();

  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [viewMode, setViewMode] = useState<'grid' | 'table'>(() => {
    const saved = localStorage.getItem(VIEW_MODE_KEY);
    return saved === 'table' ? 'table' : 'grid';
  });
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

  const [showSmsFormModal, setShowSmsFormModal] = useState(false);
  const [editingSmsConfiguration, setEditingSmsConfiguration] =
    useState<ISmsServerConfiguration | null>(null);
  const [viewingSmsConfigurationId, setViewingSmsConfigurationId] = useState<
    string | null
  >(null);
  const [smsConfigurationToDelete, setSmsConfigurationToDelete] =
    useState<ISmsServerConfiguration | null>(null);

  const debouncedSearch = useDebounce(searchTerm, 300);

  useEffect(() => {
    localStorage.setItem(VIEW_MODE_KEY, viewMode);
  }, [viewMode]);

  const loadSmsConfigurations = useCallback(() => {
    const params: ISmsServerConfigurationListParams = {
      offset: currentPage - 1,
      pageSize: PAGE_SIZE,
      searchParam: debouncedSearch || undefined,
      sortBy,
      sortOrder,
    };
    fetchConfigurations(params);
  }, [
    currentPage,
    debouncedSearch,
    sortBy,
    sortOrder,
    fetchConfigurations,
  ]);

  useEffect(() => {
    loadSmsConfigurations();
  }, [loadSmsConfigurations]);

  useEffect(() => {
    setTimeout(() => {
      setCurrentPage(1);
    }, 0);
  }, [debouncedSearch]);

  const handleCreateNew = () => {
    setEditingSmsConfiguration(null);
    setShowSmsFormModal(true);
  };

  const handleSmsView = (configuration: ISmsServerConfiguration) => {
    setViewingSmsConfigurationId(configuration.id);
  };

  const handleSmsEdit = (configuration: ISmsServerConfiguration) => {
    setEditingSmsConfiguration(configuration);
    setShowSmsFormModal(true);
  };

  const handleSmsDeleteClick = (configuration: ISmsServerConfiguration) => {
    setSmsConfigurationToDelete(configuration);
  };

  const handleSmsDeleteConfirm = async (id: string) => {
    const success = await deleteConfiguration(id);
    if (success) {
      loadSmsConfigurations();
    }
    return success;
  };

  const handleSmsSave = async (
    data: ISmsServerConfigurationForm,
    id?: string,
  ) => {
    const result = id
      ? await updateConfiguration(id, data)
      : await createConfiguration(data);
    if (result) {
      loadSmsConfigurations();
    }
    return result;
  };

  const handleSort = (field: string) => {
    if (sortBy === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortOrder('asc');
    }
  };

  const listTotal = configurations.total;
  const visibleCount = configurations.items.length;

  return (
    <div className="min-h-screen space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">
            SMS Server Configurations
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Manage SMS gateway providers for smishing simulations
          </p>
        </div>
        <Button variant="default" onClick={handleCreateNew}>
          <PlusIcon className="mr-2 size-4" />
          Create SMS Configuration
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2 text-lg">
                <MessageSquare className="size-5" />
                SMS Server Configurations ({listTotal})
              </CardTitle>
              <CardDescription>
                Browse and manage SMS server configurations for smishing
                simulations
              </CardDescription>
            </div>
            <ViewToggle value={viewMode} onChange={mode => setViewMode(mode)} />
          </div>
        </CardHeader>
        <CardContent>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-4">
            <div className="flex min-w-[280px] flex-1 flex-wrap items-center gap-4">
              <div className="min-w-[280px] flex-1">
                <Input
                  type="search"
                  placeholder="Search configurations..."
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                />
              </div>
            </div>

            <span className="text-nowrap text-sm text-gray-500">
              {visibleCount} configuration{visibleCount !== 1 ? 's' : ''}
            </span>
          </div>

          {viewMode === 'grid' ? (
            <SmsServerConfigurationGrid
              configurations={configurations}
              loading={smsLoading}
              onView={handleSmsView}
              onEdit={handleSmsEdit}
              onDelete={handleSmsDeleteClick}
            />
          ) : (
            <SmsServerConfigurationTable
              configurations={configurations}
              loading={smsLoading}
              sortBy={sortBy}
              sortOrder={sortOrder}
              onSort={handleSort}
              onView={handleSmsView}
              onEdit={handleSmsEdit}
              onDelete={handleSmsDeleteClick}
            />
          )}

          <div className="mt-6 flex justify-end">
            <Pagination
              currentPage={currentPage}
              perPage={PAGE_SIZE}
              total={listTotal}
              onPageChange={page => setCurrentPage(page)}
            />
          </div>
        </CardContent>
      </Card>

      <SmsServerConfigurationFormModal
        key={editingSmsConfiguration?.id ?? 'create'}
        isOpen={showSmsFormModal}
        onClose={() => {
          setShowSmsFormModal(false);
          setEditingSmsConfiguration(null);
        }}
        configuration={editingSmsConfiguration}
        getConfigurationById={getConfigurationById}
        onSave={handleSmsSave}
        saving={smsSaving}
      />

      <SmsServerConfigurationViewModal
        isOpen={!!viewingSmsConfigurationId}
        onClose={() => setViewingSmsConfigurationId(null)}
        configurationId={viewingSmsConfigurationId}
        getConfigurationById={getConfigurationById}
      />

      <SmsServerConfigurationDeleteConfirm
        isOpen={!!smsConfigurationToDelete}
        onClose={() => setSmsConfigurationToDelete(null)}
        configuration={smsConfigurationToDelete}
        onDelete={handleSmsDeleteConfirm}
      />
    </div>
  );
};

export default SmsServerConfigurationListContent;
