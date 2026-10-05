import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Dialog, DialogContent } from 'common/Dialog';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import Pagination from 'common/Pagination';
import ViewToggle from 'components/ViewToggle';
import { useAuth } from 'hooks/UseAuth';
import useDebounce from 'hooks/UseDebounce';
import useSenderProfiles from 'hooks/UseSenderProfiles';
import { Mail, PlusIcon, Upload } from 'lucide-react';
import {
  ISenderProfile,
  ISenderProfileForm,
  ISenderProfileImportData,
  ISenderProfileListParams,
  ProfileType,
} from 'models/SenderProfile';
import { useCallback, useEffect, useState } from 'react';
import { ROLE } from 'utils/Role';
import SenderProfileFormModal from './SenderProfileFormModal';
import SenderProfileGrid from './SenderProfileGrid';
import SenderProfileImportModal from './SenderProfileImportModal';
import SenderProfileImportResultModal from './SenderProfileImportResultModal';
import SenderProfileTable from './SenderProfileTable';
import TestConnectionModal from './TestConnectionModal';

const PAGE_SIZE = 12;
const VIEW_MODE_KEY = 'senderProfileView_EMAIL';

/**
 * Email sender profile list page content.
 */
const SenderProfileListContent = () => {
  const { role } = useAuth();

  const {
    profiles,
    loading: emailLoading,
    saving: emailSaving,
    testing,
    fetchProfiles,
    createProfile,
    updateProfile,
    deleteProfile,
    duplicateProfile,
    testProfileConnection,
    testNewConnection,
    checkDomainVerification,
    importing,
    importSenderProfiles,
  } = useSenderProfiles();

  const [searchTerm, setSearchTerm] = useState('');
  const [profileTypeFilter, setProfileTypeFilter] = useState<ProfileType | ''>(
    '',
  );
  const [verificationFilter, setVerificationFilter] = useState<
    'all' | 'verified' | 'unverified'
  >('all');
  const [currentPage, setCurrentPage] = useState(1);
  const [viewMode, setViewMode] = useState<'grid' | 'table'>(() => {
    const saved = localStorage.getItem(VIEW_MODE_KEY);
    return saved === 'table' ? 'table' : 'grid';
  });
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

  const [showEmailFormModal, setShowEmailFormModal] = useState(false);
  const [editingProfile, setEditingProfile] = useState<ISenderProfile | null>(
    null,
  );
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [profileToDelete, setProfileToDelete] = useState<ISenderProfile | null>(
    null,
  );
  const [showTestModal, setShowTestModal] = useState(false);
  const [testingProfile, setTestingProfile] = useState<ISenderProfile | null>(
    null,
  );
  const [showImportModal, setShowImportModal] = useState(false);
  const [showImportResult, setShowImportResult] = useState(false);
  const [importResult, setImportResult] =
    useState<ISenderProfileImportData | null>(null);

  const debouncedSearch = useDebounce(searchTerm, 300);

  useEffect(() => {
    localStorage.setItem(VIEW_MODE_KEY, viewMode);
  }, [viewMode]);

  const loadEmailProfiles = useCallback(() => {
    const params: ISenderProfileListParams = {
      offset: currentPage - 1,
      pageSize: PAGE_SIZE,
      searchParam: debouncedSearch || undefined,
      profileType: profileTypeFilter || undefined,
      sortBy,
      sortOrder,
    };
    fetchProfiles(params);
  }, [
    currentPage,
    debouncedSearch,
    profileTypeFilter,
    sortBy,
    sortOrder,
    fetchProfiles,
  ]);

  useEffect(() => {
    loadEmailProfiles();
  }, [loadEmailProfiles]);

  useEffect(() => {
    setTimeout(() => {
      setCurrentPage(1);
    }, 0);
  }, [debouncedSearch, profileTypeFilter]);

  const handleCreateNew = () => {
    setEditingProfile(null);
    setShowEmailFormModal(true);
  };

  const handleEdit = (profile: ISenderProfile) => {
    setEditingProfile(profile);
    setShowEmailFormModal(true);
  };

  const handleDuplicate = async (profile: ISenderProfile) => {
    const result = await duplicateProfile(profile.profileId);
    if (result) {
      loadEmailProfiles();
    }
  };

  const handleDeleteClick = (profile: ISenderProfile) => {
    setProfileToDelete(profile);
    setShowDeleteConfirm(true);
  };

  const handleDeleteConfirm = async () => {
    if (!profileToDelete) return;

    const success = await deleteProfile(profileToDelete.profileId);
    if (success) {
      setShowDeleteConfirm(false);
      setProfileToDelete(null);
    }
  };

  const handleTest = (profile: ISenderProfile) => {
    setTestingProfile(profile);
    setShowTestModal(true);
  };

  const handleEmailSave = async (
    data: ISenderProfileForm,
  ): Promise<ISenderProfile | null> => {
    let result: ISenderProfile | null;
    if (editingProfile) {
      result = await updateProfile(editingProfile.profileId, data);
    } else {
      result = await createProfile(data);
    }
    if (result) {
      loadEmailProfiles();
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

  const handleImportClick = () => {
    setShowImportModal(true);
  };

  const handleImportCsvFromModal = useCallback(
    async (file: File) => {
      const result = await importSenderProfiles(file);
      if (result) {
        setImportResult(result);
        setShowImportResult(true);
        loadEmailProfiles();
        setShowImportModal(false);
      }
    },
    [importSenderProfiles, loadEmailProfiles],
  );

  const filteredEmailItems =
    verificationFilter === 'all'
      ? profiles.items
      : profiles.items.filter(profile =>
          verificationFilter === 'verified'
            ? profile.verified
            : !profile.verified,
        );

  const visibleProfiles = {
    ...profiles,
    items: filteredEmailItems,
    total: filteredEmailItems.length,
  };

  const visibleCount = filteredEmailItems.length;
  const listTotal = profiles.total;
  const isEmailDropdownFilterApplied =
    profileTypeFilter !== '' || verificationFilter !== 'all';

  return (
    <div className="min-h-screen space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">
            Email Sender Profiles
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Manage SMTP configurations for sending campaign emails
          </p>
        </div>
        <div className="flex items-center gap-2">
          {(role === ROLE.SUPER_ADMIN || role === ROLE.ASPIRE_ADMIN) && (
            <Button
              type="button"
              variant="outline"
              onClick={handleImportClick}
              disabled={importing}
            >
              <Upload className="size-4" />
              Import Sender Profiles
            </Button>
          )}
          <Button variant="default" onClick={handleCreateNew}>
            <PlusIcon className="mr-2 size-4" />
            Create Sender Profile
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2 text-lg">
                <Mail className="size-5" />
                Email Sender Profiles ({listTotal})
              </CardTitle>
              <CardDescription>
                Browse and manage email sender profiles for campaigns
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
                  placeholder="Search profiles..."
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                />
              </div>

              <div className="w-[200px] shrink-0">
                <Select
                  value={profileTypeFilter || 'all'}
                  onValueChange={value => {
                    setProfileTypeFilter(
                      value === 'all' ? '' : (value as ProfileType | ''),
                    );
                    setVerificationFilter('all');
                  }}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Types</SelectItem>
                    <SelectItem value={ProfileType.CUSTOM}>Custom</SelectItem>
                    <SelectItem value={ProfileType.MANAGED}>Managed</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {profileTypeFilter && (
                <div className="w-[200px] shrink-0">
                  <Select
                    value={verificationFilter}
                    onValueChange={value =>
                      setVerificationFilter(
                        value as 'all' | 'verified' | 'unverified',
                      )
                    }
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Verification</SelectItem>
                      <SelectItem value="verified">Verified</SelectItem>
                      <SelectItem value="unverified">Unverified</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}

              {isEmailDropdownFilterApplied && (
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setProfileTypeFilter('');
                    setVerificationFilter('all');
                  }}
                >
                  Reset Filters
                </Button>
              )}
            </div>

            <span className="text-nowrap text-sm text-gray-500">
              {visibleCount} profile{visibleCount !== 1 ? 's' : ''}
            </span>
          </div>

          {viewMode === 'grid' ? (
            <SenderProfileGrid
              profiles={visibleProfiles}
              loading={emailLoading}
              onEdit={handleEdit}
              onDuplicate={handleDuplicate}
              onDelete={handleDeleteClick}
              onTest={handleTest}
            />
          ) : (
            <SenderProfileTable
              profiles={visibleProfiles}
              loading={emailLoading}
              sortBy={sortBy}
              sortOrder={sortOrder}
              onSort={handleSort}
              onEdit={handleEdit}
              onDuplicate={handleDuplicate}
              onDelete={handleDeleteClick}
              onTest={handleTest}
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

      <SenderProfileFormModal
        isOpen={showEmailFormModal}
        onClose={() => {
          setShowEmailFormModal(false);
          setEditingProfile(null);
        }}
        profile={editingProfile}
        onSave={handleEmailSave}
        onTest={testNewConnection}
        onCheckDomain={checkDomainVerification}
        saving={emailSaving}
        testing={testing}
      />

      {testingProfile && (
        <TestConnectionModal
          isOpen={showTestModal}
          onClose={() => {
            setShowTestModal(false);
            setTestingProfile(null);
            loadEmailProfiles();
          }}
          profileName={testingProfile.profileName}
          onTest={() => testProfileConnection(testingProfile.profileId)}
        />
      )}

      <SenderProfileImportModal
        open={showImportModal}
        onOpenChange={setShowImportModal}
        importing={importing}
        onImportCsv={handleImportCsvFromModal}
      />

      <SenderProfileImportResultModal
        open={showImportResult}
        onOpenChange={open => {
          setShowImportResult(open);
          if (!open) {
            setImportResult(null);
          }
        }}
        result={importResult}
      />

      <Dialog
        open={showDeleteConfirm}
        onOpenChange={open => {
          setShowDeleteConfirm(open);
          if (!open) {
            setProfileToDelete(null);
          }
        }}
      >
        <DialogContent>
          <div className="space-y-4">
            <p className="text-muted-foreground">
              Warning: You are about to delete{' '}
              <strong>{profileToDelete?.profileName}</strong>. This action
              cannot be undone.
            </p>
            <div className="flex justify-end gap-3">
              <Button
                variant="secondary"
                onClick={() => {
                  setShowDeleteConfirm(false);
                  setProfileToDelete(null);
                }}
              >
                Cancel
              </Button>
              <Button
                variant="destructive"
                onClick={handleDeleteConfirm}
                disabled={emailSaving}
              >
                {emailSaving ? 'Deleting...' : 'Delete'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default SenderProfileListContent;
