import { Button } from 'common/Button';
import { Card } from 'common/Card';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import BreachDetailsModal from 'features/breach/BreachDetailsModal';
import BreachSettingsForm from 'features/breach/BreachSettingsForm';
import DataBreachTable from 'features/breach/DataBreachTable';
import ExportModal from 'features/breach/ExportModal';
import RecipientBreachTable from 'features/breach/RecipientBreachTable';
import {
  IBreachListParams,
  IRecipientBreachListParams,
  useBreaches,
} from 'hooks/UseBreaches';
import { DownloadIcon } from 'lucide-react';
import {
  BreachSeverity,
  BreachStatus,
  IBreachConfig,
  IBreachConfigRequest,
  IBreachRecord,
  IBreachSyncResult,
  IRecipientBreach,
  RecipientBreachStatus,
} from 'models/Breach';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

type TabType = 'breaches' | 'recipients' | 'settings';

/**
 * Main Breach Management Page with tabs
 */
const BreachManagement = () => {
  // Tab State
  const [activeTab, setActiveTab] = useState<TabType>('breaches');

  // Breach Records State
  const [breaches, setBreaches] = useState<IBreachRecord[]>([]);
  const [totalBreaches, setTotalBreaches] = useState(0);
  const [breachFilters, setBreachFilters] = useState<IBreachListParams>({
    offset: 0,
    pageSize: 10,
  });
  const [selectedBreach, setSelectedBreach] = useState<IBreachRecord | null>(
    null,
  );
  const [showBreachDetails, setShowBreachDetails] = useState(false);
  const [showExportModal, setShowExportModal] = useState(false);

  // Recipient Breaches State
  const [recipients, setRecipients] = useState<IRecipientBreach[]>([]);
  const [totalRecipients, setTotalRecipients] = useState(0);
  const [recipientFilters, setRecipientFilters] =
    useState<IRecipientBreachListParams>({
      offset: 0,
      pageSize: 20,
    });

  // Settings State
  const [config, setConfig] = useState<IBreachConfig | null>(null);
  const [lastSyncResult, setLastSyncResult] =
    useState<IBreachSyncResult | null>(null);

  // Search State
  const [searchKeyword, setSearchKeyword] = useState('');
  const [filterDomain, setFilterDomain] = useState('');
  const [filterStatus, setFilterStatus] = useState<BreachStatus | ''>('');
  const [filterSeverity, setFilterSeverity] = useState<BreachSeverity | ''>('');
  const [filterRecipientStatus, setFilterRecipientStatus] = useState<
    RecipientBreachStatus | ''
  >('');

  // Hook
  const {
    loading,
    actionLoading,
    syncing,
    fetchBreaches,
    fetchBreachById,
    updateBreachStatus,
    exportBreaches,
    fetchRecipientBreaches,
    notifyRecipient,
    resetRecipientPassword,
    resolveRecipientBreach,
    fetchConfig,
    updateConfig,
    triggerSync,
    downloadExport,
  } = useBreaches();

  // ==================== Data Loading ====================

  const loadBreaches = useCallback(async () => {
    const params: IBreachListParams = {
      ...breachFilters,
      keyword: searchKeyword || undefined,
      domain: filterDomain || undefined,
      status: filterStatus || undefined,
      severity: filterSeverity || undefined,
    };
    const result = await fetchBreaches(params);
    if (result) {
      setBreaches(result.items || []);
      setTotalBreaches(result.total || 0);
    }
  }, [
    breachFilters,
    searchKeyword,
    filterDomain,
    filterStatus,
    filterSeverity,
    fetchBreaches,
  ]);

  const loadRecipients = useCallback(async () => {
    const params: IRecipientBreachListParams = {
      ...recipientFilters,
      keyword: searchKeyword || undefined,
      status: filterRecipientStatus || undefined,
    };
    const result = await fetchRecipientBreaches(params);
    if (result) {
      setRecipients(result.items || []);
      setTotalRecipients(result.total || 0);
    }
  }, [
    recipientFilters,
    searchKeyword,
    filterRecipientStatus,
    fetchRecipientBreaches,
  ]);

  const loadConfig = useCallback(async () => {
    const result = await fetchConfig();
    if (result) {
      setConfig(result);
    }
  }, [fetchConfig]);

  useEffect(() => {
    if (activeTab === 'breaches') {
      setTimeout(() => {
        loadBreaches();
      }, 0);
    } else if (activeTab === 'recipients') {
      setTimeout(() => {
        loadRecipients();
      }, 0);
    } else if (activeTab === 'settings') {
      setTimeout(() => {
        loadConfig();
      }, 0);
    }
  }, [activeTab, loadBreaches, loadRecipients, loadConfig]);

  // ==================== Breach Actions ====================

  const handleBreachRowClick = async (breach: IBreachRecord) => {
    const details = await fetchBreachById(breach.id);
    if (details) {
      setSelectedBreach(details);
      setShowBreachDetails(true);
    }
  };

  const handleBreachStatusChange = async (id: string, status: BreachStatus) => {
    const result = await updateBreachStatus(id, { status });
    if (result) {
      toast.success('Breach status updated');
      setTimeout(() => {
        loadBreaches();
      }, 0);
      if (selectedBreach?.id === id) {
        setSelectedBreach(result);
      }
    } else {
      toast.error('Failed to update breach status');
    }
  };

  const handleViewRecipients = (breachId: string) => {
    setShowBreachDetails(false);
    setRecipientFilters(prev => ({
      ...prev,
      breachRecordId: breachId,
      offset: 0,
    }));
    setActiveTab('recipients');
  };

  const handleExport = async (
    format: string,
    domain?: string,
    status?: BreachStatus,
  ) => {
    const blob = await exportBreaches(format, domain, status);
    if (blob) {
      const filename = `breaches-export-${new Date().toISOString().split('T')[0]}.${format}`;
      downloadExport(blob, filename);
      toast.success('Export downloaded successfully');
      setShowExportModal(false);
    } else {
      toast.error('Failed to export breaches');
    }
  };

  // ==================== Recipient Actions ====================

  const handleNotifyRecipient = async (id: string, notes?: string) => {
    const result = await notifyRecipient(id, { notes, sendEmail: true });
    if (result) {
      toast.success('User notified successfully');
      setTimeout(() => {
        loadRecipients();
      }, 0);
    } else {
      toast.error('Failed to notify user');
    }
  };

  const handleResetPassword = async (id: string, notes?: string) => {
    const result = await resetRecipientPassword(id, { notes });
    if (result) {
      toast.success('Password reset triggered');
      setTimeout(() => {
        loadRecipients();
      }, 0);
    } else {
      toast.error('Failed to trigger password reset');
    }
  };

  const handleResolveRecipient = async (id: string, notes?: string) => {
    const result = await resolveRecipientBreach(id, { notes });
    if (result) {
      toast.success('Breach resolved for user');
      setTimeout(() => {
        loadRecipients();
      }, 0);
    } else {
      toast.error('Failed to resolve breach');
    }
  };

  // ==================== Settings Actions ====================

  const handleSaveConfig = async (configRequest: IBreachConfigRequest) => {
    const result = await updateConfig(configRequest);
    if (result) {
      setConfig(result);
      toast.success('Settings saved successfully');
    } else {
      toast.error('Failed to save settings');
    }
  };

  const handleSync = async () => {
    const result = await triggerSync();
    if (result) {
      setLastSyncResult(result);
      toast.success(result.message || 'Sync completed');
      setTimeout(() => {
        loadBreaches();
      }, 0);
    } else {
      toast.error('Sync failed');
    }
  };

  // ==================== Pagination ====================

  const handleBreachPageChange = (offset: number) => {
    setBreachFilters(prev => ({ ...prev, offset }));
  };

  const handleRecipientPageChange = (offset: number) => {
    setRecipientFilters(prev => ({ ...prev, offset }));
  };

  // ==================== Render ====================

  const tabs = [
    { id: 'breaches' as TabType, label: 'Data Breaches', count: totalBreaches },
    {
      id: 'recipients' as TabType,
      label: 'Affected Recipients',
      count: totalRecipients,
    },
    { id: 'settings' as TabType, label: 'Settings', count: null },
  ];

  return (
    <div className="min-h-screen p-6">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-foreground">
          Breach Intelligence
        </h1>
        <p className="text-muted-foreground">
          Monitor and manage data breaches affecting your organization
        </p>
      </div>

      {/* Tabs */}
      <Card>
        <div className=" ">
          <nav className="flex">
            {tabs.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`border-b-2 px-6 py-4 text-sm font-medium transition-colors ${
                  activeTab === tab.id
                    ? 'border-primary text-primary'
                    : 'border-transparent text-muted-foreground hover:border-card-border hover:text-foreground'
                }`}
              >
                {tab.label}
                {tab.count !== null && (
                  <span
                    className={`ml-2 rounded-full px-2 py-0.5 text-xs ${
                      activeTab === tab.id
                        ? 'bg-primary/10 text-primary'
                        : 'bg-muted-foreground/10 text-muted-foreground'
                    }`}
                  >
                    {tab.count}
                  </span>
                )}
              </button>
            ))}
          </nav>
        </div>

        {/* Tab Content */}
        <div className="p-6">
          {/* Data Breaches Tab */}
          {activeTab === 'breaches' && (
            <div>
              {/* Filters */}
              <div className="mb-6 flex gap-4">
                <Input
                  type="text"
                  value={searchKeyword}
                  onChange={e => setSearchKeyword(e.target.value)}
                  placeholder="Search breaches..."
                />
                <Input
                  type="text"
                  value={filterDomain}
                  onChange={e => setFilterDomain(e.target.value)}
                  placeholder="Filter by domain"
                />
                <Select
                  value={filterStatus}
                  onValueChange={value =>
                    setFilterStatus(value as BreachStatus | '')
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select a status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={BreachStatus.ACTION_REQUIRED}>
                      Action Required
                    </SelectItem>
                    <SelectItem value={BreachStatus.IN_PROGRESS}>
                      In Progress
                    </SelectItem>
                    <SelectItem value={BreachStatus.RESOLVED}>
                      Resolved
                    </SelectItem>
                  </SelectContent>
                </Select>
                <Select
                  value={filterSeverity}
                  onValueChange={value =>
                    setFilterSeverity(value as BreachSeverity | '')
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select a severity" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={BreachSeverity.HIGH}>High</SelectItem>
                    <SelectItem value={BreachSeverity.MEDIUM}>
                      Medium
                    </SelectItem>
                    <SelectItem value={BreachSeverity.LOW}>Low</SelectItem>
                  </SelectContent>
                </Select>
                <Button
                  variant="outline"
                  onClick={() => {
                    setTimeout(() => {
                      loadBreaches();
                    }, 0);
                  }}
                >
                  Apply Filters
                </Button>
                <Button
                  variant="outline"
                  onClick={() => setShowExportModal(true)}
                >
                  <span className="flex items-center gap-2">
                    <DownloadIcon size={16} />
                    Export
                  </span>
                </Button>
              </div>

              {/* Table */}
              <DataBreachTable
                breaches={breaches}
                loading={loading}
                onRowClick={handleBreachRowClick}
                onStatusChange={handleBreachStatusChange}
              />

              {/* Pagination */}
              {totalBreaches > breachFilters.pageSize! && (
                <div className="mt-6 flex items-center justify-between">
                  <p className="text-sm text-muted-foreground">
                    Showing {breachFilters.offset! + 1} to{' '}
                    {Math.min(
                      breachFilters.offset! + breachFilters.pageSize!,
                      totalBreaches,
                    )}{' '}
                    of {totalBreaches} breaches
                  </p>
                  <div className="flex gap-2">
                    <button
                      onClick={() =>
                        handleBreachPageChange(
                          breachFilters.offset! - breachFilters.pageSize!,
                        )
                      }
                      disabled={breachFilters.offset === 0}
                      className="hover: rounded-lg border border-card-border bg-card-background px-4 py-2 text-sm font-medium text-card-foreground disabled:opacity-50"
                    >
                      Previous
                    </button>
                    <button
                      onClick={() =>
                        handleBreachPageChange(
                          breachFilters.offset! + breachFilters.pageSize!,
                        )
                      }
                      disabled={
                        breachFilters.offset! + breachFilters.pageSize! >=
                        totalBreaches
                      }
                      className="hover: rounded-lg border border-card-border bg-card-background px-4 py-2 text-sm font-medium text-card-foreground disabled:opacity-50"
                    >
                      Next
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Affected Recipients Tab */}
          {activeTab === 'recipients' && (
            <div>
              {/* Filters */}
              <div className="mb-6 flex gap-4">
                <Input
                  type="text"
                  value={searchKeyword}
                  onChange={e => setSearchKeyword(e.target.value)}
                  placeholder="Search by name or email..."
                />
                <Select
                  value={filterRecipientStatus}
                  onValueChange={value =>
                    setFilterRecipientStatus(
                      value as RecipientBreachStatus | '',
                    )
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select a status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={RecipientBreachStatus.PENDING}>
                      Pending
                    </SelectItem>
                    <SelectItem value={RecipientBreachStatus.NOTIFIED}>
                      Notified
                    </SelectItem>
                    <SelectItem value={RecipientBreachStatus.RESOLVED}>
                      Resolved
                    </SelectItem>
                  </SelectContent>
                </Select>
                <button
                  onClick={() => {
                    setTimeout(() => {
                      loadRecipients();
                    }, 0);
                  }}
                  className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/80"
                >
                  Apply Filters
                </button>
                {recipientFilters.breachRecordId && (
                  <button
                    onClick={() => {
                      setRecipientFilters(prev => ({
                        ...prev,
                        breachRecordId: undefined,
                        offset: 0,
                      }));
                      setTimeout(() => {
                        loadRecipients();
                      }, 0);
                    }}
                    className="rounded-lg bg-muted-foreground/10 px-4 py-2 text-sm font-medium text-muted-foreground hover:bg-muted-foreground/20"
                  >
                    Clear Breach Filter
                  </button>
                )}
              </div>

              {/* Table */}
              <RecipientBreachTable
                recipients={recipients}
                loading={loading}
                onNotify={handleNotifyRecipient}
                onResetPassword={handleResetPassword}
                onResolve={handleResolveRecipient}
                actionLoading={actionLoading}
              />

              {/* Pagination */}
              {totalRecipients > recipientFilters.pageSize! && (
                <div className="mt-6 flex items-center justify-between">
                  <p className="text-sm text-muted-foreground">
                    Showing {recipientFilters.offset! + 1} to{' '}
                    {Math.min(
                      recipientFilters.offset! + recipientFilters.pageSize!,
                      totalRecipients,
                    )}{' '}
                    of {totalRecipients} recipients
                  </p>
                  <div className="flex gap-2">
                    <button
                      onClick={() =>
                        handleRecipientPageChange(
                          recipientFilters.offset! - recipientFilters.pageSize!,
                        )
                      }
                      disabled={recipientFilters.offset === 0}
                      className="hover: rounded-lg border border-card-border bg-card-background px-4 py-2 text-sm font-medium text-card-foreground disabled:opacity-50"
                    >
                      Previous
                    </button>
                    <button
                      onClick={() =>
                        handleRecipientPageChange(
                          recipientFilters.offset! + recipientFilters.pageSize!,
                        )
                      }
                      disabled={
                        recipientFilters.offset! + recipientFilters.pageSize! >=
                        totalRecipients
                      }
                      className="hover: rounded-lg border border-card-border bg-card-background px-4 py-2 text-sm font-medium text-card-foreground disabled:opacity-50"
                    >
                      Next
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Settings Tab */}
          {activeTab === 'settings' && (
            <BreachSettingsForm
              config={config}
              loading={loading}
              saving={actionLoading}
              syncing={syncing}
              lastSyncResult={lastSyncResult}
              onSave={handleSaveConfig}
              onSync={handleSync}
            />
          )}
        </div>
      </Card>

      {/* Modals */}
      <BreachDetailsModal
        breach={selectedBreach}
        isOpen={showBreachDetails}
        onClose={() => setShowBreachDetails(false)}
        onStatusChange={handleBreachStatusChange}
        onViewRecipients={handleViewRecipients}
        actionLoading={actionLoading}
      />

      <ExportModal
        isOpen={showExportModal}
        onClose={() => setShowExportModal(false)}
        onExport={handleExport}
        exporting={actionLoading}
      />
    </div>
  );
};

export default BreachManagement;
