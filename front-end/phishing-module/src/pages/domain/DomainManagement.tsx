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
import DomainAddModal from 'features/domain/DomainAddModal';
import DomainDeleteModal from 'features/domain/DomainDeleteModal';
import DomainLockModal from 'features/domain/DomainLockModal';
import DomainTable from 'features/domain/DomainTable';
import DomainVerifyModal from 'features/domain/DomainVerifyModal';
import { useAuth } from 'hooks/UseAuth';
import useDebounce from 'hooks/UseDebounce';
import useDomains from 'hooks/UseDomains';
import { Globe, PlusIcon, RotateCcw, Search, ShieldCheck } from 'lucide-react';
import { IDomain } from 'models/Domain';
import { Fragment, useCallback, useEffect, useState } from 'react';
import { DEBOUNCE_DELAY } from 'utils/Constants';
import { ROLE } from 'utils/Role';

enum ModalType {
  NONE = 'NONE',
  ADD = 'ADD',
  VERIFY = 'VERIFY',
  LOCK = 'LOCK',
  DELETE = 'DELETE',
}

/**
 * Domain Management Page
 * Displays list of domains with verification and locking functionality
 */
const DomainManagement = () => {
  const {
    domains,
    loading,
    queryParams,
    setQueryParams,
    fetchDomains,
    generateVerificationEmail,
    addDomain,
    verifyDomain,
    lockDomain,
    unlockDomain,
    deleteDomain,
    resendVerificationEmail,
  } = useDomains();

  const [searchValue, setSearchValue] = useState('');
  const [modalType, setModalType] = useState<ModalType>(ModalType.NONE);
  const [selectedDomain, setSelectedDomain] = useState<IDomain | null>(null);

  const { role } = useAuth();

  const debouncedSearch = useDebounce(searchValue, DEBOUNCE_DELAY);

  // Update query params when search changes
  useEffect(() => {
    setQueryParams(prev => ({
      ...prev,
      search: debouncedSearch,
      offset: 0,
    }));
  }, [debouncedSearch, setQueryParams]);

  // Fetch domains when query params change
  useEffect(() => {
    fetchDomains();
  }, [queryParams, fetchDomains]);

  // Modal handlers
  const handleOpenAddModal = useCallback((domain?: IDomain) => {
    setSelectedDomain(domain || null);
    setModalType(ModalType.ADD);
  }, []);

  const handleOpenVerifyModal = useCallback((domain?: IDomain) => {
    setSelectedDomain(domain || null);
    setModalType(ModalType.VERIFY);
  }, []);

  const handleOpenLockModal = useCallback((domain: IDomain) => {
    setSelectedDomain(domain);
    setModalType(ModalType.LOCK);
  }, []);

  const handleOpenDeleteModal = useCallback((domain: IDomain) => {
    setSelectedDomain(domain);
    setModalType(ModalType.DELETE);
  }, []);

  const handleCloseModal = useCallback(() => {
    setModalType(ModalType.NONE);
    setSelectedDomain(null);
  }, []);

  // Action handlers
  const handleGenerateCode = async (data: { emailAddress: string }) => {
    const success = await generateVerificationEmail(data);
    return success;
  };

  const handleAdd = async (data: { domain: string }) => {
    const result = await addDomain(data);
    if (result) {
      fetchDomains();
      return true;
    }
    return false;
  };

  const handleVerify = async (data: {
    emailAddress: string;
    verificationCode: string;
  }) => {
    const result = await verifyDomain(data);
    if (result) {
      fetchDomains();
      return true;
    }
    return false;
  };

  const handleResendCode = async (data: { emailAddress: string }) => {
    return await resendVerificationEmail(data);
  };

  const handleLock = async (domainId: string) => {
    const success = await lockDomain(domainId);
    if (success) {
      fetchDomains();
    }
    return success;
  };

  const handleUnlock = async (domainId: string) => {
    const success = await unlockDomain(domainId);
    if (success) {
      fetchDomains();
    }
    return success;
  };

  const handleDelete = async (domainId: string) => {
    const success = await deleteDomain(domainId);
    if (success) {
      fetchDomains();
    }
    return success;
  };

  // Pagination handler
  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  // Reset filters
  const handleReset = () => {
    setSearchValue('');
    setQueryParams(prev => ({
      ...prev,
      search: '',
      offset: 0,
    }));
  };

  return (
    <Fragment>
      <div className="space-y-6">
        {/* Page Header */}

        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-3xl font-bold text-foreground">
              Domain Management
            </h2>
            <p className="text-muted-foreground">
              Verify and lock your organization&apos;s email domains
            </p>
          </div>
          <div className="flex items-center gap-2">
            {role === ROLE.ASPIRE_ADMIN && (
              <Button onClick={() => handleOpenAddModal()}>
                <PlusIcon className="size-4" />
                Add New Domain
              </Button>
            )}
            <Button onClick={() => handleOpenVerifyModal()}>
              <ShieldCheck className="size-4" />
              Verify New Domain
            </Button>
          </div>
        </div>

        {/* Domain List Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-lg">
              <Globe className="size-5" />
              Domains ({domains.total})
            </CardTitle>
            <CardDescription>
              Manage verified and locked domains for your phishing campaigns
            </CardDescription>
          </CardHeader>
          <CardContent>
            {/* Search and Filters */}
            <div className="mb-4 flex items-center gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
                <Input
                  placeholder="Search domains..."
                  value={searchValue}
                  onChange={e => setSearchValue(e.target.value)}
                  className="pl-9"
                />
              </div>
              <Button variant="destructive" onClick={handleReset}>
                <RotateCcw className="mr-2 size-4" />
                Reset
              </Button>
            </div>

            {/* Domain Table */}
            <DomainTable
              domains={domains.items}
              loading={loading}
              onVerify={handleOpenVerifyModal}
              onLock={handleOpenLockModal}
              onUnlock={handleOpenLockModal}
              onDelete={handleOpenDeleteModal}
            />

            {/* Pagination */}
            {domains.total > 0 && (
              <div className="mt-6 flex justify-end">
                <Pagination
                  total={domains.total}
                  perPage={queryParams.pageSize || 10}
                  currentPage={(queryParams.offset || 0) + 1}
                  onPageChange={handlePageChange}
                />
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Modals */}
      <DomainAddModal
        isOpen={modalType === ModalType.ADD}
        onClose={handleCloseModal}
        onAdd={handleAdd}
      />

      <DomainVerifyModal
        isOpen={modalType === ModalType.VERIFY}
        onClose={handleCloseModal}
        onGenerateCode={handleGenerateCode}
        onVerify={handleVerify}
        onResendCode={handleResendCode}
      />

      <DomainLockModal
        isOpen={modalType === ModalType.LOCK}
        onClose={handleCloseModal}
        domain={selectedDomain}
        onLock={handleLock}
        onUnlock={handleUnlock}
      />

      <DomainDeleteModal
        isOpen={modalType === ModalType.DELETE}
        onClose={handleCloseModal}
        domain={selectedDomain}
        onDelete={handleDelete}
      />
    </Fragment>
  );
};

export default DomainManagement;
