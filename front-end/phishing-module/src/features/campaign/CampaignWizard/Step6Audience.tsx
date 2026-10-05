import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import {
  ArrowLeftIcon,
  ArrowRightIcon,
  Building2,
  RefreshCw,
  UserPlus,
  UserRound,
  Users,
} from 'lucide-react';
import { AudienceType, ICampaign } from 'models/Campaign';
import { useCallback, useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  CampaignAudienceSchema,
  TCampaignAudienceForm,
  campaignAudienceDefaultValues,
} from 'schemas/CampaignSchema';
import { cn } from 'utils/Helper';
import {
  getAllUsersListUrl,
  getAudienceReturnToPath,
} from 'utils/ListNavigation';
import { useCampaigns } from 'hooks/UseCampaigns';
import { useStore } from 'hooks/UseStore';
import { AllocateUserModal } from './AllocateUserModal';
import { DepartmentAudienceList } from './audience/DepartmentAudienceList';
import { calculateAudienceRecipientCount } from './audience/helpers';
import { IndividualUserPicker } from './audience/IndividualUserPicker';
import { RiskGroupAudienceList } from './audience/RiskGroupAudienceList';
import { useAudienceDirectory } from './audience/useAudienceDirectory';

interface Step6AudienceProps {
  initialData?: Partial<TCampaignAudienceForm>;
  onSubmit: (data: TCampaignAudienceForm) => void;
  onBack: () => void;
  isLoading?: boolean;
  clientAdminId?: string;
  campaignId?: string;
  productPackageId?: string;
  onAudienceAllocated?: () => Promise<ICampaign | null | void>;
}

const AUDIENCE_OPTIONS = [
  {
    value: AudienceType.INDIVIDUAL,
    label: 'Individual Selection',
    description: 'Choose specific users',
    icon: <UserRound className="size-4" />,
  },
  {
    value: AudienceType.DEPARTMENTS,
    label: 'By Department',
    description: 'Select specific departments',
    icon: <Building2 className="size-4" />,
  },
  {
    value: AudienceType.GROUPS,
    label: 'By Risk Group',
    description: 'Select specific risk groups',
    icon: <Users className="size-4" />,
  },
  // {
  //   value: AudienceType.ALL_USERS,
  //   label: 'All Assigned Users',
  //   description: 'Send to all users in your organization',
  //   icon: <Users className="size-4" />,
  // },
];

export const Step6Audience = ({
  initialData,
  onSubmit,
  onBack,
  isLoading,
  clientAdminId = '1b3761ad-5103-431a-895d-332eb4855c3b',
  campaignId,
  productPackageId,
  onAudienceAllocated,
}: Step6AudienceProps) => {
  const { pathname } = useLocation();
  const { userInfo } = useStore();
  const { checkIsFirstCampaign } = useCampaigns();
  const loginClientId = userInfo?.clientAdminId || '';
  const resolvedClientId = loginClientId || clientAdminId;

  const {
    setValue,
    watch,
    handleSubmit,
    trigger,
    reset,
    formState: { errors },
  } = useForm<TCampaignAudienceForm>({
    resolver: zodResolver(CampaignAudienceSchema),
    defaultValues: { ...campaignAudienceDefaultValues, ...initialData },
  });

  const audienceType = watch('audienceType');
  const departmentIds = watch('departmentIds');
  const groupIds = watch('groupIds');
  const userIds = watch('userIds');

  const directory = useAudienceDirectory({
    clientAdminId: resolvedClientId,
    source: 'licensed',
    licensedCampaignId: campaignId,
    fetchUsersEnabled:
      audienceType === AudienceType.INDIVIDUAL ||
      audienceType === AudienceType.ALL_USERS,
  });

  const [isFirstCampaign, setIsFirstCampaign] = useState<boolean | null>(null);
  const [allocationCompleted, setAllocationCompleted] = useState(false);
  const [allocateOpen, setAllocateOpen] = useState(false);
  const fetchedIsFirstKey = useRef<string | undefined>(undefined);

  useEffect(() => {
    if (!productPackageId || !campaignId) {
      if (!productPackageId) setIsFirstCampaign(false);
      return;
    }
    const fetchKey = `${productPackageId}:${campaignId}`;
    if (fetchedIsFirstKey.current === fetchKey) return;

    let cancelled = false;
    setIsFirstCampaign(null);

    const loadIsFirst = async () => {
      const result = await checkIsFirstCampaign(productPackageId, campaignId);
      if (cancelled) return;
      fetchedIsFirstKey.current = fetchKey;
      if (result === null) {
        toast.error('Unable to determine campaign license status');
        setIsFirstCampaign(false);
        return;
      }
      setIsFirstCampaign(result);
    };

    void loadIsFirst();
    return () => {
      cancelled = true;
    };
  }, [productPackageId, campaignId, checkIsFirstCampaign]);

  const toggleUserSelection = (userId: string) => {
    const current = userIds || [];
    setValue(
      'userIds',
      current.includes(userId)
        ? current.filter(id => id !== userId)
        : [...current, userId],
      { shouldValidate: true },
    );
    void trigger();
  };

  const toggleSelectAll = () => {
    const currentPageIds = directory.users.items.map(u => u.id);
    const allSelected = currentPageIds.every(id => userIds.includes(id));
    setValue(
      'userIds',
      allSelected
        ? userIds.filter(id => !currentPageIds.includes(id))
        : [...new Set([...userIds, ...currentPageIds])],
      { shouldValidate: true },
    );
    void trigger();
  };

  const toggleDepartment = (id: string) => {
    const current = departmentIds || [];
    setValue(
      'departmentIds',
      current.includes(id) ? current.filter(d => d !== id) : [...current, id],
      { shouldValidate: true },
    );
    void trigger();
  };

  const toggleAllDepartments = () => {
    const ids = directory.departmentUserCounts.map(d => d.departmentName);
    const allSelected =
      ids.length > 0 && ids.every(id => departmentIds.includes(id));
    setValue('departmentIds', allSelected ? [] : ids, { shouldValidate: true });
    void trigger();
  };

  const toggleGroup = (id: string) => {
    const current = groupIds || [];
    setValue(
      'groupIds',
      current.includes(id) ? current.filter(g => g !== id) : [...current, id],
      { shouldValidate: true },
    );
    void trigger();
  };

  const toggleAllGroups = () => {
    const ids = directory.riskGroupUserCounts.map(g => g.riskGroup);
    const allSelected =
      ids.length > 0 && ids.every(id => groupIds.includes(id));
    setValue('groupIds', allSelected ? [] : ids, { shouldValidate: true });
    void trigger();
  };

  const recipientCount = calculateAudienceRecipientCount({
    audienceType,
    usersTotal: directory.users.total,
    departmentUserCounts: directory.departmentUserCounts,
    departmentIds,
    riskGroupUserCounts: directory.riskGroupUserCounts,
    groupIds,
    userIds,
  });

  const recipientSelectionError =
    errors.departmentIds?.message ??
    errors.groupIds?.message ??
    errors.userIds?.message;

  const showRecipientError = recipientCount === 0 && !!recipientSelectionError;

  const hasStoredAudience = Boolean(initialData);
  const showAllocationGate =
    isFirstCampaign === true && !allocationCompleted && !hasStoredAudience;
  const isCheckingFirst = isFirstCampaign === null;

  const openAllocateModal = () => {
    if (!campaignId) {
      toast.error('Campaign not found');
      return;
    }
    setAllocateOpen(true);
  };

  const openAllUserList = () => {
    if (!campaignId) {
      toast.error('Campaign not found');
      return;
    }
    const returnTo = getAudienceReturnToPath(pathname, campaignId);
    window.location.assign(getAllUsersListUrl(campaignId, returnTo));
  };

  const loadLicensedAudienceRef = useRef(directory.loadLicensedAudience);
  loadLicensedAudienceRef.current = directory.loadLicensedAudience;

  const handleAllocated = useCallback(async () => {
    setAllocationCompleted(true);
    setAllocateOpen(false);

    const licensedLoad = campaignId
      ? loadLicensedAudienceRef.current(campaignId)
      : Promise.resolve();

    try {
      const updated = await onAudienceAllocated?.();
      if (updated?.audience) {
        reset({
          audienceType: updated.audience.type,
          departmentIds: updated.audience.departmentIds ?? [],
          groupIds: updated.audience.groupIds ?? [],
          userIds: updated.audience.userIds ?? [],
        });
      }
    } finally {
      await licensedLoad;
    }
  }, [campaignId, onAudienceAllocated, reset]);

  const allocateModal = (
    <AllocateUserModal
      isOpen={allocateOpen}
      onClose={() => setAllocateOpen(false)}
      campaignId={campaignId}
      productPackageId={productPackageId}
      clientAdminId={resolvedClientId}
      onAllocated={handleAllocated}
    />
  );

  if (isCheckingFirst) {
    return (
      <div className="mx-auto max-w-6xl">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-foreground">
            Select Audience
          </h2>
          <p className="mt-2 text-muted-foreground">
            Choose who will receive the phishing simulation.
          </p>
        </div>
        <div className="flex items-center justify-center p-8">
          <RefreshCw className="size-6 animate-spin text-muted-foreground" />
          <span className="ml-2 text-muted-foreground">Loading…</span>
        </div>
        <div className="flex justify-between border-t border-card-border pt-6">
          <Button type="button" variant="outline" onClick={onBack}>
            <ArrowLeftIcon className="size-4" />
            Back
          </Button>
        </div>
        {allocateModal}
      </div>
    );
  }

  if (showAllocationGate) {
    return (
      <div className="mx-auto max-w-6xl">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-foreground">
            Select Audience
          </h2>
          <p className="mt-2 text-muted-foreground">
            Allocate users before selecting the campaign audience.
          </p>
        </div>
        <div className="mb-6 flex justify-center py-12">
          <Button type="button" onClick={openAllocateModal}>
            <UserPlus className="size-4" />
            Assign Users
          </Button>
        </div>
        <div className="flex justify-between border-t border-card-border pt-6">
          <Button type="button" variant="outline" onClick={onBack}>
            <ArrowLeftIcon className="size-4" />
            Back
          </Button>
        </div>
        {allocateModal}
      </div>
    );
  }

  return (
    <>
      <form onSubmit={handleSubmit(onSubmit)} className="mx-auto max-w-6xl">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-foreground">
            Select Audience
          </h2>
          <p className="mt-2 text-muted-foreground">
            Choose who will receive the phishing simulation.
          </p>
        </div>
        <div className="mb-6 flex items-center justify-between gap-4">
          <div className="flex w-fit overflow-hidden rounded-md border border-card-border">
            <Button
              type="button"
              className="bg-primary px-3 py-2 text-sm font-medium text-primary-foreground"
            >
              Assigned users
            </Button>
            <Button
              type="button"
              className="bg-transparent px-3 py-2 text-sm font-medium text-foreground hover:bg-secondary"
              onClick={openAllUserList}
            >
              All user list
            </Button>
          </div>
          <Button type="button" onClick={openAllocateModal}>
            <UserPlus className="size-4" />
            Assign Users
          </Button>
        </div>

        <div className="mb-6 grid grid-cols-3 gap-3">
          {AUDIENCE_OPTIONS.map(option => (
            <button
              key={option.value}
              type="button"
              className={cn(
                audienceType === option.value
                  ? 'rounded-md border border-primary bg-primary p-2 text-primary-foreground hover:bg-primary/90'
                  : 'rounded-md border border-card-border bg-transparent p-2 text-white hover:bg-secondary hover:text-white',
              )}
              onClick={() => {
                setValue('audienceType', option.value, {
                  shouldValidate: true,
                });
                void trigger();
              }}
            >
              <div className="flex items-center gap-4 text-left">
                {option.icon}
                <div>
                  <span className="block text-sm font-medium text-foreground">
                    {option.label}
                  </span>
                  <span className="block text-xs text-foreground">
                    {option.description}
                  </span>
                </div>
              </div>
            </button>
          ))}
        </div>

        {audienceType === AudienceType.DEPARTMENTS && (
          <DepartmentAudienceList
            departmentUserCounts={directory.departmentUserCounts}
            selectedIds={departmentIds}
            onToggle={toggleDepartment}
            onToggleAll={toggleAllDepartments}
          />
        )}

        {audienceType === AudienceType.GROUPS && (
          <RiskGroupAudienceList
            riskGroupUserCounts={directory.riskGroupUserCounts}
            selectedIds={groupIds}
            onToggle={toggleGroup}
            onToggleAll={toggleAllGroups}
          />
        )}

        {audienceType === AudienceType.INDIVIDUAL && (
          <IndividualUserPicker
            directory={directory}
            userIds={userIds}
            onToggleUser={toggleUserSelection}
            onToggleSelectAll={toggleSelectAll}
            onClearAll={() => {
              setValue('userIds', [], { shouldValidate: true });
              void trigger();
            }}
          />
        )}

        <div className="mb-6 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">
              Total Recipients:
            </span>
            <span className="text-2xl font-bold text-primary">
              {recipientCount}
            </span>
          </div>
        </div>

        {showRecipientError && (
          <p className="mb-4 text-sm text-destructive">
            {recipientSelectionError}
          </p>
        )}

        <div className="flex justify-between border-t border-card-border pt-6">
          <Button type="button" variant="outline" onClick={onBack}>
            <ArrowLeftIcon className="size-4" />
            Back
          </Button>
          <Button
            type="submit"
            variant="default"
            onClick={handleSubmit(onSubmit)}
            disabled={isLoading || recipientCount === 0}
          >
            {isLoading ? 'Saving...' : 'Save & Continue'}
            <ArrowRightIcon className="size-4" />
          </Button>
        </div>
      </form>
      {allocateModal}
    </>
  );
};
