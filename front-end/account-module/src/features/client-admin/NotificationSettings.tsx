import { Alert, AlertDescription } from 'components/common/Alert';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Label } from 'components/common/Label';
import { Switch } from 'components/common/Switch';
import ConfirmDialog from 'components/ConfirmDialog';
import NotificationSettingsSkeleton from 'components/Skelton/NotificationSettings';
import { useAPI } from 'hooks/UseAPI';
import {
  AlertCircle,
  Bell,
  CheckCircle2,
  Mail,
  MessageSquare,
  Smartphone,
} from 'lucide-react';
import { IList } from 'models/Global';
import {
  IClientAdminNotificationSettings,
  IGlobalChannels,
  NOTIFICATION_CATEGORIES,
  NotificationAction,
} from 'models/Notification';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IUpdatePayload {
  action: NotificationAction;
  notificationType: string;
  emailEnabled: boolean;
  inAppEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
  enabledChannels: string[];
  disabledChannels: string[];
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
    .map(word =>
      word.length > 2
        ? word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
        : word.toUpperCase(),
    )
    .join(' ');
};

const ClientAdminNotificationSettings = () => {
  const apiClient = useAPI();
  const [isLoading, setIsLoading] = useState(true);
  const [saveStatus, setSaveStatus] = useState<'idle' | 'success' | 'error'>(
    'idle',
  );
  const [notificationSettings, setNotificationSettings] = useState<
    IList<IClientAdminNotificationSettings>
  >({
    offset: 0,
    pageSize: 0,
    total: 0,
    items: [],
  });
  const [globalChannels, setGlobalChannels] = useState<IGlobalChannels>({
    email: true,
    inApp: true,
    sms: false,
    push: false,
  });

  // Confirmation dialog state
  const [confirmDialog, setConfirmDialog] = useState<IConfirmDialogState>({
    isOpen: false,
    message: '',
    loading: false,
    pendingAction: null,
  });

  useEffect(() => {
    getNotificationSettings();
  }, []);

  const getNotificationSettings = async () => {
    try {
      setIsLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_CLIENT_ADMIN_NOTIFICATION_SETTINGS,
      );
      setNotificationSettings(response.data);
    } catch (error) {
      console.error('Failed to fetch notification settings:', error);
      toast.error('Failed to load notification settings');
    } finally {
      setIsLoading(false);
    }
  };

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
      setSaveStatus('success');
      setTimeout(() => setSaveStatus('idle'), 3000);
    } catch (error) {
      console.error('Failed to update notification settings:', error);
      setSaveStatus('error');
      setTimeout(() => setSaveStatus('idle'), 3000);
    } finally {
      closeConfirmationDialog();
    }
  };

  const updateNotificationSetting = async (payload: IUpdatePayload) => {
    const response = await apiClient.post(
      API_END_POINTS.UPDATE_CLIENT_ADMIN_NOTIFICATION_SETTINGS,
      {
        data: payload,
      },
    );

    if (isSuccessResponse(response.statusCode)) {
      toast.success('Notification settings updated successfully');
      return response;
    } else {
      toast.error('Failed to update notification settings');
      throw new Error('Failed to update notification settings');
    }
  };

  let isProcessing = false;

  const handleToggleNotification = (
    setting: IClientAdminNotificationSettings,
    field: keyof IClientAdminNotificationSettings,
    value: boolean,
  ) => {
    if (isProcessing) return; // prevent duplicate
    isProcessing = true;

    const fieldLabels: Record<string, string> = {
      enabled: 'notification',
      emailEnabled: 'email notifications',
      inAppEnabled: 'in-app notifications',
      smsEnabled: 'SMS notifications',
      pushEnabled: 'push notifications',
    };

    const action = value ? 'enable' : 'disable';
    const notificationLabel = formatNotificationLabel(setting.notificationType);
    const channelLabel = fieldLabels[field] || field;

    const message = `Are you sure you want to ${action} ${channelLabel} for "${notificationLabel}"?`;

    openConfirmationDialog(message, async () => {
      // Step 1: Create the updated setting object
      const updatedSetting = {
        ...setting,
        [field]: value,
        // If disabling the main notification, disable all channels
        ...(field === 'enabled' && !value
          ? {
            emailEnabled: false,
            inAppEnabled: false,
            smsEnabled: false,
            pushEnabled: false,
          }
          : {}),
      };

      // Step 2: Update state optimistically with the new values
      // setNotificationSettings(prev => {
      //   const updatedItems = prev.items.map(s => {
      //     if (s.id !== setting.id) return s;
      //     return updatedSetting;
      //   });
      //   return { ...prev, items: updatedItems };
      // });

      // Step 3: Define action type
      const actionType =
        field === 'enabled'
          ? value
            ? NotificationAction.ENABLE
            : NotificationAction.DISABLE
          : NotificationAction.UPDATE;

      // Step 4: Always send all channel fields
      const payload: IUpdatePayload = {
        action: actionType,
        notificationType: updatedSetting.notificationType,
        emailEnabled: updatedSetting.emailEnabled,
        inAppEnabled: updatedSetting.inAppEnabled,
        smsEnabled: updatedSetting.smsEnabled,
        pushEnabled: updatedSetting.pushEnabled,
        enabledChannels: [],
        disabledChannels: [],
      };

      // Step 5: Call API
      try {
        const response = await updateNotificationSetting(payload);

        // Step 6: Refresh only if backend sends outdated state
        if (isSuccessResponse(response.statusCode)) {
          await getNotificationSettings();
        } else {
          toast.error('Failed to update notification settings');
        }
      } catch (error) {
        toast.error('Failed to update notification settings');
      } finally {
        isProcessing = false;
      }
    });
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Notification Settings
        </h1>
        <p className="mt-2 text-muted-foreground">
          Manage how and when you receive notifications across different
          channels
        </p>
      </div>
      {isLoading ? (
        <NotificationSettingsSkeleton />
      ) : (
        <>

          {/* Categorized Notifications */}
          {Object.entries(NOTIFICATION_CATEGORIES).map(([category, types]) => {
            const categorySettings = notificationSettings.items.filter(
              (s: IClientAdminNotificationSettings) =>
                types.includes(s.notificationType),
            );

            if (categorySettings.length === 0) return null;

            return (
              <Card key={category}>
                <CardHeader>
                  <CardTitle>{category}</CardTitle>
                  <CardDescription>
                    Configure {category.toLowerCase()} notification preferences
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {categorySettings.map((setting, index) => (
                      <div
                        key={index}
                        className="rounded-lg border border-card-border p-4"
                      >
                        <div className="mb-3 flex items-start justify-between">
                          <div className="flex-1">
                            <div className="mb-1 flex items-center gap-2">
                              <Label className="text-base font-semibold">
                                {formatNotificationLabel(
                                  setting.notificationType,
                                )}
                              </Label>
                              {setting.notificationType ===
                                'SECURITY_ALERTS' && (
                                  <span className="rounded bg-red-100 px-2 py-0.5 text-xs text-red-700">
                                    Required
                                  </span>
                                )}
                            </div>
                          </div>
                          <Switch
                            checked={setting.enabled}
                            onCheckedChange={(checked: boolean) =>
                              handleToggleNotification(
                                setting,
                                'enabled',
                                checked,
                              )
                            }
                            disabled={
                              setting.notificationType === 'SECURITY_ALERTS'
                            }
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
                                  handleToggleNotification(
                                    setting,
                                    'emailEnabled',
                                    checked,
                                  )
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
                                  handleToggleNotification(
                                    setting,
                                    'inAppEnabled',
                                    checked,
                                  )
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
                                  handleToggleNotification(
                                    setting,
                                    'smsEnabled',
                                    checked,
                                  )
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
                                  handleToggleNotification(
                                    setting,
                                    'pushEnabled',
                                    checked,
                                  )
                                }
                                className="ml-auto"
                              />
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </>
      )}

      {/* Confirmation Dialog */}
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

export default ClientAdminNotificationSettings;
