import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Label } from 'components/common/Label';
import Pagination from 'components/common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import { Switch } from 'components/common/Switch';
import ConfirmDialog from 'components/ConfirmDialog';
import SearchSelect from 'components/SearchSelect';
import NotificationSettingsSkeleton from 'components/Skelton/NotificationSettings';
import { useAPI } from 'hooks/UseAPI';
import { useUsedNotificationTypes } from 'hooks/UseUsedNotificationTypes';
import { Bell, Mail, MessageSquare, Smartphone } from 'lucide-react';
import { IList } from 'models/Global';
import {
  IRoleNotificationSettings,
  NOTIFICATION_ROLES,
  NotificationAction,
} from 'models/Notification';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

const ALL_FILTER_VALUE = 'all';
const DEFAULT_PAGE_SIZE = 20;

interface IUpdatePayload {
  action: NotificationAction;
  notificationType: string;
  role: string;
  emailEnabled: boolean;
  inAppEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
}

interface IConfirmDialogState {
  isOpen: boolean;
  message: string;
  loading: boolean;
  pendingAction: (() => Promise<void>) | null;
}

const formatNotificationLabel = (type: string): string => {
  return type
    .split('_')
    .map(word => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
};

const getRoleLabel = (role: string): string => {
  return NOTIFICATION_ROLES.find(r => r.value === role)?.label ?? role;
};

const buildQueryString = (params: {
  role: string;
  notificationType: string;
  offset: number;
  pageSize: number;
}): string => {
  const searchParams = new URLSearchParams();
  searchParams.set('offset', String(params.offset));
  searchParams.set('pageSize', String(params.pageSize));

  if (params.role && params.role !== ALL_FILTER_VALUE) {
    searchParams.set('role', params.role);
  }

  if (params.notificationType && params.notificationType !== ALL_FILTER_VALUE) {
    searchParams.set('notificationType', params.notificationType);
  }

  return `?${searchParams.toString()}`;
};

const RoleWiseNotificationSettings = () => {
  const apiClient = useAPI();
  const { notificationTypes } = useUsedNotificationTypes();
  const [isLoading, setIsLoading] = useState(true);
  const [roleFilter, setRoleFilter] = useState(ALL_FILTER_VALUE);
  const [notificationTypeFilter, setNotificationTypeFilter] = useState('');
  const [offset, setOffset] = useState(0);
  const [notificationSettings, setNotificationSettings] = useState<
    IList<IRoleNotificationSettings>
  >({
    offset: 0,
    pageSize: DEFAULT_PAGE_SIZE,
    total: 0,
    items: [],
  });

  const [confirmDialog, setConfirmDialog] = useState<IConfirmDialogState>({
    isOpen: false,
    message: '',
    loading: false,
    pendingAction: null,
  });

  const getNotificationSettings = useCallback(async () => {
    try {
      setIsLoading(true);
      const query = buildQueryString({
        role: roleFilter,
        notificationType: notificationTypeFilter,
        offset,
        pageSize: DEFAULT_PAGE_SIZE,
      });
      const response = await apiClient.get(
        `${API_END_POINTS.GET_ROLE_NOTIFICATION_SETTINGS}${query}`,
      );
      setNotificationSettings(response.data);
    } catch (error) {
      console.error('Failed to fetch role notification settings:', error);
      toast.error('Failed to load role notification settings');
    } finally {
      setIsLoading(false);
    }
  }, [apiClient, roleFilter, notificationTypeFilter, offset]);

  useEffect(() => {
    getNotificationSettings();
  }, [getNotificationSettings]);

  const openConfirmationDialog = (
    message: string,
    action: () => Promise<void>,
  ) => {
    setConfirmDialog({
      isOpen: true,
      message,
      loading: false,
      pendingAction: action,
    });
  };

  const closeConfirmationDialog = () => {
    setConfirmDialog({
      isOpen: false,
      message: '',
      loading: false,
      pendingAction: null,
    });
  };

  const handleConfirmAction = async () => {
    if (!confirmDialog.pendingAction) return;

    setConfirmDialog(prev => ({ ...prev, loading: true }));

    try {
      await confirmDialog.pendingAction();
    } catch (error) {
      console.error('Failed to update role notification settings:', error);
    } finally {
      closeConfirmationDialog();
    }
  };

  const updateNotificationSetting = async (payload: IUpdatePayload) => {
    const response = await apiClient.post(
      API_END_POINTS.UPDATE_ROLE_NOTIFICATION_SETTINGS,
      { data: payload },
    );

    if (isSuccessResponse(response.statusCode)) {
      toast.success('Role notification settings updated successfully');
      return response;
    }

    toast.error('Failed to update role notification settings');
    throw new Error('Failed to update role notification settings');
  };

  let isProcessing = false;

  const handleToggleNotification = (
    setting: IRoleNotificationSettings,
    field: keyof IRoleNotificationSettings,
    value: boolean,
  ) => {
    if (isProcessing) return;

    const fieldLabels: Record<string, string> = {
      enabled: 'notification',
      emailEnabled: 'email notifications',
      inAppEnabled: 'in-app notifications',
      smsEnabled: 'SMS notifications',
      pushEnabled: 'push notifications',
    };

    const action = value ? 'enable' : 'disable';
    const notificationLabel = formatNotificationLabel(setting.notificationType);
    const roleLabel = getRoleLabel(setting.role);
    const channelLabel = fieldLabels[field] || field;

    const message = `Are you sure you want to ${action} ${channelLabel} for "${notificationLabel}" (${roleLabel})?`;

    openConfirmationDialog(message, async () => {
      isProcessing = true;

      const updatedSetting = {
        ...setting,
        [field]: value,
        ...(field === 'enabled' && !value
          ? {
              emailEnabled: false,
              inAppEnabled: false,
              smsEnabled: false,
              pushEnabled: false,
            }
          : {}),
      };

      const actionType =
        field === 'enabled'
          ? value
            ? NotificationAction.ENABLE
            : NotificationAction.DISABLE
          : NotificationAction.UPDATE;

      const payload: IUpdatePayload = {
        action: actionType,
        notificationType: updatedSetting.notificationType,
        role: updatedSetting.role,
        emailEnabled: updatedSetting.emailEnabled,
        inAppEnabled: updatedSetting.inAppEnabled,
        smsEnabled: updatedSetting.smsEnabled,
        pushEnabled: updatedSetting.pushEnabled,
      };

      try {
        const response = await updateNotificationSetting(payload);

        if (isSuccessResponse(response.statusCode)) {
          await getNotificationSettings();
        }
      } catch {
        toast.error('Failed to update role notification settings');
      } finally {
        isProcessing = false;
      }
    });
  };

  const handleRoleFilterChange = (value: string) => {
    setRoleFilter(value);
    setOffset(0);
  };

  const handleNotificationTypeFilterChange = (value: string) => {
    setNotificationTypeFilter(value);
    setOffset(0);
  };

  const pageSize = notificationSettings.pageSize || DEFAULT_PAGE_SIZE;
  const total = notificationSettings.total;
  const rangeStart = total === 0 ? 0 : offset * pageSize + 1;
  const rangeEnd = Math.min((offset + 1) * pageSize, total);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Role-wise Notification Settings
        </h1>
        <p className="mt-2 text-muted-foreground">
          Configure notification preferences per role across the platform
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>
            Filter settings by role and notification type
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label>Role</Label>
              <Select value={roleFilter} onValueChange={handleRoleFilterChange}>
                <SelectTrigger>
                  <SelectValue placeholder="Select role" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL_FILTER_VALUE}>All Roles</SelectItem>
                  {NOTIFICATION_ROLES.map(role => (
                    <SelectItem key={role.value} value={role.value}>
                      {role.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Notification Type</Label>
              <SearchSelect
                value={notificationTypeFilter || undefined}
                onValueChange={handleNotificationTypeFilterChange}
                items={[
                  { label: 'All Types', value: ALL_FILTER_VALUE },
                  ...notificationTypes.map(type => ({
                    label: formatNotificationLabel(type),
                    value: type,
                  })),
                ]}
                placeholder="Select notification type"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {isLoading ? (
        <NotificationSettingsSkeleton />
      ) : (
        <>
          {notificationSettings.items.length > 0 ? (
            <Card>
              <CardHeader>
                <CardTitle>Notification Settings</CardTitle>
                <CardDescription>
                  Configure notification preferences by role
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {notificationSettings.items.map(setting => (
                    <SettingRow
                      key={setting.id}
                      setting={setting}
                      onToggle={handleToggleNotification}
                    />
                  ))}
                </div>
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardContent className="py-8 text-center text-muted-foreground">
                No notification settings found for the selected filters.
              </CardContent>
            </Card>
          )}

          {total > 0 && (
            <div className="flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                Showing {rangeStart}–{rangeEnd} of {total} settings
              </p>
            </div>
          )}
        </>
      )}
      <Pagination
        key={`${roleFilter}-${notificationTypeFilter}`}
        total={total}
        perPage={pageSize}
        onPageChange={page => setOffset(page - 1)}
      />

      <ConfirmDialog
        isOpen={confirmDialog.isOpen}
        message={confirmDialog.message}
        loading={confirmDialog.loading}
        loadingText="Updating..."
        onClose={closeConfirmationDialog}
        onConfirm={handleConfirmAction}
      />
    </div>
  );
};

interface ISettingRowProps {
  setting: IRoleNotificationSettings;
  onToggle: (
    setting: IRoleNotificationSettings,
    field: keyof IRoleNotificationSettings,
    value: boolean,
  ) => void;
}

const SettingRow = ({ setting, onToggle }: ISettingRowProps) => (
  <div className="rounded-lg border border-card-border p-4">
    <div className="mb-3 flex items-start justify-between">
      <div className="flex-1">
        <div className="mb-1 flex items-center gap-2">
          <Label className="text-base font-semibold">
            {formatNotificationLabel(setting.notificationType)}
          </Label>
          <span className="rounded bg-secondary px-2 py-0.5 text-xs">
            {getRoleLabel(setting.role)}
          </span>
          {setting.notificationType === 'SECURITY_ALERTS' && (
            <span className="rounded bg-red-100 px-2 py-0.5 text-xs text-red-700">
              Required
            </span>
          )}
        </div>
      </div>
      <Switch
        checked={setting.enabled}
        onCheckedChange={(checked: boolean) =>
          onToggle(setting, 'enabled', checked)
        }
        disabled={setting.notificationType === 'SECURITY_ALERTS'}
      />
    </div>

    {setting.enabled && (
      <div className="mt-3 grid grid-cols-2 gap-3 border-t border-card-border pt-3 md:grid-cols-4">
        <div className="flex items-center gap-2">
          <Mail className="size-4 text-blue-600" />
          <Label className="text-sm">Email</Label>
          <Switch
            checked={setting.emailEnabled}
            onCheckedChange={(checked: boolean) =>
              onToggle(setting, 'emailEnabled', checked)
            }
            className="ml-auto"
          />
        </div>

        <div className="flex items-center gap-2">
          <Bell className="size-4 text-purple-600" />
          <Label className="text-sm">In-App</Label>
          <Switch
            checked={setting.inAppEnabled}
            onCheckedChange={(checked: boolean) =>
              onToggle(setting, 'inAppEnabled', checked)
            }
            className="ml-auto"
          />
        </div>

        <div className="flex items-center gap-2">
          <MessageSquare className="size-4 text-green-600" />
          <Label className="text-sm">SMS</Label>
          <Switch
            checked={setting.smsEnabled}
            onCheckedChange={(checked: boolean) =>
              onToggle(setting, 'smsEnabled', checked)
            }
            className="ml-auto"
          />
        </div>

        <div className="flex items-center gap-2">
          <Smartphone className="size-4 text-orange-600" />
          <Label className="text-sm">Push</Label>
          <Switch
            checked={setting.pushEnabled}
            onCheckedChange={(checked: boolean) =>
              onToggle(setting, 'pushEnabled', checked)
            }
            className="ml-auto"
          />
        </div>
      </div>
    )}
  </div>
);

export default RoleWiseNotificationSettings;
