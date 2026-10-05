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
import VoiceServerConfigurationDeleteConfirm from 'features/voice-server-configuration/VoiceServerConfigurationDeleteConfirm';
import VoiceServerConfigurationFormModal from 'features/voice-server-configuration/VoiceServerConfigurationFormModal';
import VoiceServerConfigurationGrid from 'features/voice-server-configuration/VoiceServerConfigurationGrid';
import VoiceServerConfigurationTable from 'features/voice-server-configuration/VoiceServerConfigurationTable';
import VoiceServerConfigurationViewModal from 'features/voice-server-configuration/VoiceServerConfigurationViewModal';
import useDebounce from 'hooks/UseDebounce';
import useVoiceServerConfigurations from 'hooks/UseVoiceServerConfigurations';
import { Phone, PlusIcon } from 'lucide-react';
import {
  IVoiceServerConfiguration,
  IVoiceServerConfigurationForm,
  IVoiceServerConfigurationListParams,
} from 'models/VoiceServerConfiguration';
import { useCallback, useEffect, useState } from 'react';

const PAGE_SIZE = 12;

/**
 * Standalone Voice Server Configuration CRUD page
 */
const VoiceServerConfiguration = () => {
  const {
    configurations,
    loading,
    saving,
    fetchConfigurations,
    getConfigurationById,
    createConfiguration,
    updateConfiguration,
    deleteConfiguration,
  } = useVoiceServerConfigurations();

  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [viewMode, setViewMode] = useState<'grid' | 'table'>(() => {
    return (
      (localStorage.getItem('voiceServerConfigView') as 'grid' | 'table') ||
      'grid'
    );
  });
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

  const [showFormModal, setShowFormModal] = useState(false);
  const [editingConfiguration, setEditingConfiguration] =
    useState<IVoiceServerConfiguration | null>(null);
  const [viewingConfiguration, setViewingConfiguration] =
    useState<IVoiceServerConfiguration | null>(null);
  const [configurationToDelete, setConfigurationToDelete] =
    useState<IVoiceServerConfiguration | null>(null);

  const debouncedSearch = useDebounce(searchTerm, 300);

  useEffect(() => {
    localStorage.setItem('voiceServerConfigView', viewMode);
  }, [viewMode]);

  const loadConfigurations = useCallback(() => {
    const params: IVoiceServerConfigurationListParams = {
      offset: currentPage - 1,
      pageSize: PAGE_SIZE,
      searchParam: debouncedSearch || undefined,
      sortBy,
      sortOrder,
    };
    void fetchConfigurations(params);
  }, [
    currentPage,
    debouncedSearch,
    sortBy,
    sortOrder,
    fetchConfigurations,
  ]);

  useEffect(() => {
    loadConfigurations();
  }, [loadConfigurations]);

  useEffect(() => {
    setTimeout(() => {
      setCurrentPage(1);
    }, 0);
  }, [debouncedSearch]);

  const handleCreateNew = () => {
    setEditingConfiguration(null);
    setShowFormModal(true);
  };

  const handleView = (configuration: IVoiceServerConfiguration) => {
    setViewingConfiguration(configuration);
  };

  const handleEdit = (configuration: IVoiceServerConfiguration) => {
    setEditingConfiguration(configuration);
    setShowFormModal(true);
  };

  const handleDeleteClick = (configuration: IVoiceServerConfiguration) => {
    setConfigurationToDelete(configuration);
  };

  const handleDeleteConfirm = async (id: string) => {
    const success = await deleteConfiguration(id);
    if (success) {
      loadConfigurations();
    }
    return success;
  };

  const handleSave = async (
    data: IVoiceServerConfigurationForm,
    id?: string,
  ) => {
    const result = id
      ? await updateConfiguration(id, data)
      : await createConfiguration(data);
    if (result) {
      loadConfigurations();
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

  return (
    <div className="min-h-screen space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">
            Voice Server Configurations
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Manage voice/SIP providers for vishing simulations
          </p>
        </div>
        <Button variant="default" onClick={handleCreateNew}>
          <PlusIcon className="mr-2 size-4" />
          Create Voice Configuration
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2 text-lg">
                <Phone className="size-5" />
                Voice Server Configurations ({configurations.total})
              </CardTitle>
              <CardDescription>
                Browse and manage voice server configurations for vishing
                simulations
              </CardDescription>
            </div>
            <ViewToggle value={viewMode} onChange={mode => setViewMode(mode)} />
          </div>
        </CardHeader>
        <CardContent>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-4">
            <div className="min-w-[280px] flex-1">
              <Input
                type="search"
                placeholder="Search configurations..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
              />
            </div>
            <span className="text-nowrap text-sm text-gray-500">
              {configurations.items.length} configuration
              {configurations.items.length !== 1 ? 's' : ''}
            </span>
          </div>

          {viewMode === 'grid' ? (
            <VoiceServerConfigurationGrid
              configurations={configurations}
              loading={loading}
              onView={handleView}
              onEdit={handleEdit}
              onDelete={handleDeleteClick}
            />
          ) : (
            <VoiceServerConfigurationTable
              configurations={configurations}
              loading={loading}
              sortBy={sortBy}
              sortOrder={sortOrder}
              onSort={handleSort}
              onView={handleView}
              onEdit={handleEdit}
              onDelete={handleDeleteClick}
            />
          )}

          <div className="mt-6 flex justify-end">
            <Pagination
              currentPage={currentPage}
              perPage={PAGE_SIZE}
              total={configurations.total}
              onPageChange={page => setCurrentPage(page)}
            />
          </div>
        </CardContent>
      </Card>

      <VoiceServerConfigurationFormModal
        key={editingConfiguration?.id ?? 'create-voice'}
        isOpen={showFormModal}
        onClose={() => {
          setShowFormModal(false);
          setEditingConfiguration(null);
        }}
        configuration={editingConfiguration}
        getConfigurationById={getConfigurationById}
        onSave={handleSave}
        saving={saving}
      />

      <VoiceServerConfigurationViewModal
        isOpen={!!viewingConfiguration}
        onClose={() => setViewingConfiguration(null)}
        configurationId={viewingConfiguration?.id ?? null}
        initialConfiguration={viewingConfiguration}
        getConfigurationById={getConfigurationById}
      />

      <VoiceServerConfigurationDeleteConfirm
        isOpen={!!configurationToDelete}
        onClose={() => setConfigurationToDelete(null)}
        configuration={configurationToDelete}
        onDelete={handleDeleteConfirm}
      />
    </div>
  );
};

export default VoiceServerConfiguration;
