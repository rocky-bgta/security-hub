import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tabs';
import ConfirmDialog from 'components/ConfirmDialog';
import { useCampaigns } from 'hooks/UseCampaigns';
import { useStore } from 'hooks/UseStore';
import { RefreshCw } from 'lucide-react';
import { AudienceType, ICampaign } from 'models/Campaign';
import { useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';
import { CampaignAudienceSchema } from 'schemas/CampaignSchema';
import { isSuccessResponse } from 'utils/Helper';
import { DepartmentAudienceList } from './audience/DepartmentAudienceList';
import {
  buildAllocateLicencePayload,
  calculateAudienceRecipientCount,
} from './audience/helpers';
import { IndividualUserPicker } from './audience/IndividualUserPicker';
import { RiskGroupAudienceList } from './audience/RiskGroupAudienceList';
import { useAudienceDirectory } from './audience/useAudienceDirectory';

interface AllocateUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  campaignId?: string;
  productPackageId?: string;
  clientAdminId?: string;
  onAllocated: () => Promise<ICampaign | null | void>;
}

const ALLOCATE_TABS: { value: AudienceType; label: string }[] = [
  { value: AudienceType.INDIVIDUAL, label: 'Individual Selection' },
  { value: AudienceType.DEPARTMENTS, label: 'By Department' },
  { value: AudienceType.GROUPS, label: 'By Risk Group' },
  // { value: AudienceType.ALL_USERS, label: 'All Users' },
];

export const AllocateUserModal = ({
  isOpen,
  onClose,
  campaignId,
  productPackageId,
  clientAdminId = '',
  onAllocated,
}: AllocateUserModalProps) => {
  const { allocateLicence } = useCampaigns();
  const { userInfo } = useStore();
  const resolvedClientId = userInfo?.clientAdminId || clientAdminId;

  const [audienceType, setAudienceType] = useState<AudienceType>(
    AudienceType.INDIVIDUAL,
  );
  const [departmentIds, setDepartmentIds] = useState<string[]>([]);
  const [groupIds, setGroupIds] = useState<string[]>([]);
  const [userIds, setUserIds] = useState<string[]>([]);
  const [allocating, setAllocating] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [confirmationMessage, setConfirmationMessage] = useState('');
  const [selectionError, setSelectionError] = useState('');
  const pendingConfirm = useRef(false);

  const directory = useAudienceDirectory({
    clientAdminId: resolvedClientId,
    source: 'registration',
    productPackageId,
    enabled: isOpen,
    fetchUsersEnabled:
      audienceType === AudienceType.ALL_USERS ||
      audienceType === AudienceType.INDIVIDUAL,
  });

  useEffect(() => {
    if (isOpen) return;
    setAudienceType(AudienceType.INDIVIDUAL);
    setDepartmentIds([]);
    setGroupIds([]);
    setUserIds([]);
    setConfirmOpen(false);
    setConfirmationMessage('');
    setSelectionError('');
    pendingConfirm.current = false;
  }, [isOpen]);

  const recipientCount = calculateAudienceRecipientCount({
    audienceType,
    usersTotal: directory.users.total,
    departmentUserCounts: directory.departmentUserCounts,
    departmentIds,
    riskGroupUserCounts: directory.riskGroupUserCounts,
    groupIds,
    userIds,
  });

  const toggleDepartment = (id: string) => {
    setDepartmentIds(current =>
      current.includes(id)
        ? current.filter(item => item !== id)
        : [...current, id],
    );
    setSelectionError('');
  };

  const toggleAllDepartments = () => {
    const ids = directory.departmentUserCounts.map(d => d.departmentName);
    const allSelected =
      ids.length > 0 && ids.every(id => departmentIds.includes(id));
    setDepartmentIds(allSelected ? [] : ids);
    setSelectionError('');
  };

  const toggleGroup = (id: string) => {
    setGroupIds(current =>
      current.includes(id)
        ? current.filter(item => item !== id)
        : [...current, id],
    );
    setSelectionError('');
  };

  const toggleAllGroups = () => {
    const ids = directory.riskGroupUserCounts.map(g => g.riskGroup);
    const allSelected =
      ids.length > 0 && ids.every(id => groupIds.includes(id));
    setGroupIds(allSelected ? [] : ids);
    setSelectionError('');
  };

  const toggleUser = (userId: string) => {
    setUserIds(current =>
      current.includes(userId)
        ? current.filter(id => id !== userId)
        : [...current, userId],
    );
    setSelectionError('');
  };

  const toggleSelectAll = () => {
    const currentPageIds = directory.users.items.map(user => user.id);
    const allSelected = currentPageIds.every(id => userIds.includes(id));
    setUserIds(current =>
      allSelected
        ? current.filter(id => !currentPageIds.includes(id))
        : [...new Set([...current, ...currentPageIds])],
    );
    setSelectionError('');
  };

  const submitAllocation = async (confirm: boolean) => {
    if (pendingConfirm.current) return;
    if (!campaignId) {
      toast.error('Campaign not found');
      return;
    }

    if (!confirm) {
      const parsed = CampaignAudienceSchema.safeParse({
        audienceType,
        departmentIds,
        groupIds,
        userIds,
      });
      if (!parsed.success) {
        const message =
          parsed.error.issues[0]?.message ||
          'Please select at least one recipient';
        setSelectionError(message);
        return;
      }
      if (recipientCount === 0) {
        setSelectionError('Please select at least one recipient');
        return;
      }
    }

    pendingConfirm.current = true;
    setAllocating(true);
    try {
      const payload = buildAllocateLicencePayload(
        audienceType,
        { departmentIds, groupIds, userIds },
        confirm,
      );
      const response = await allocateLicence(campaignId, payload);

      if (!response || !isSuccessResponse(response.statusCode)) {
        toast.error(response?.message || 'Failed to allocate licences');
        return;
      }

      if (!confirm && response.data?.requiresConfirmation) {
        setConfirmationMessage(response.data.confirmationMessage || '');
        setConfirmOpen(true);
        return;
      }

      toast.success(
        response.message || 'License allocation completed successfully',
      );
      setConfirmOpen(false);
      await onAllocated();
      onClose();
    } catch (error) {
      console.error('Error allocating licences:', error);
      toast.error('Failed to allocate licences');
    } finally {
      pendingConfirm.current = false;
      setAllocating(false);
    }
  };

  const handleClose = () => {
    if (allocating || confirmOpen) return;
    onClose();
  };

  return (
    <>
      <Dialog
        open={isOpen}
        onOpenChange={open => {
          if (!open) handleClose();
        }}
      >
        <DialogContent className="max-h-[90vh] overflow-y-auto !w-4/5 max-w-5xl">
          <DialogHeader>
            <DialogTitle>Allocate User</DialogTitle>
          </DialogHeader>

          <Tabs
            value={audienceType}
            onValueChange={value => setAudienceType(value as AudienceType)}
          >
            <TabsList className="grid grid-cols-3">
              {ALLOCATE_TABS.map(tab => (
                <TabsTrigger key={tab.value} value={tab.value}>
                  {tab.label}
                </TabsTrigger>
              ))}
            </TabsList>

            {/* <TabsContent value={AudienceType.ALL_USERS}>
              <div className="mb-6 rounded-lg border border-card-border p-4">
                <div className="flex items-center gap-2">
                  <Users className="size-5 text-muted-foreground" />
                  <span className="text-sm font-medium text-foreground">
                    All users in your organization will be allocated.
                  </span>
                </div>
                <p className="mt-2 text-sm text-muted-foreground">
                  {directory.usersLoading
                    ? 'Loading user count…'
                    : `${directory.users.total} users`}
                </p>
              </div>
            </TabsContent> */}

            <TabsContent value={AudienceType.DEPARTMENTS}>
              <DepartmentAudienceList
                departmentUserCounts={directory.departmentUserCounts}
                selectedIds={departmentIds}
                onToggle={toggleDepartment}
                onToggleAll={toggleAllDepartments}
              />
            </TabsContent>

            <TabsContent value={AudienceType.GROUPS}>
              <RiskGroupAudienceList
                riskGroupUserCounts={directory.riskGroupUserCounts}
                selectedIds={groupIds}
                onToggle={toggleGroup}
                onToggleAll={toggleAllGroups}
              />
            </TabsContent>

            <TabsContent value={AudienceType.INDIVIDUAL}>
              <IndividualUserPicker
                directory={directory}
                userIds={userIds}
                onToggleUser={toggleUser}
                onToggleSelectAll={toggleSelectAll}
                onClearAll={() => {
                  setUserIds([]);
                  setSelectionError('');
                }}
              />
            </TabsContent>
          </Tabs>

          <div className="rounded-lg p-4">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">
                Total Recipients:
              </span>
              <span className="text-2xl font-bold text-primary">
                {recipientCount}
              </span>
            </div>
          </div>

          {selectionError && (
            <p className="text-sm text-destructive">{selectionError}</p>
          )}

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={allocating}
            >
              Cancel
            </Button>
            <Button
              type="button"
              onClick={() => void submitAllocation(false)}
              disabled={allocating || confirmOpen || recipientCount === 0}
            >
              {allocating ? (
                <>
                  <RefreshCw className="mr-2 size-4 animate-spin" />
                  Allocating...
                </>
              ) : (
                'Allocate'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        isOpen={confirmOpen}
        message={confirmationMessage}
        loading={allocating}
        loadingText="Allocating..."
        buttonText="Continue"
        onClose={() => {
          if (!allocating) setConfirmOpen(false);
        }}
        onConfirm={() => void submitAllocation(true)}
      />
    </>
  );
};
