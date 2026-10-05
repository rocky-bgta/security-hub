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
import { useAPI } from 'hooks/UseAPI';
import {
  AlertCircle,
  Bell,
  CheckCircle2,
  Loader2,
  Mail,
  MessageSquare,
  Smartphone,
} from 'lucide-react';
import { IList } from 'models/Global';
import {
  IGlobalChannels,
  INotificationSettings,
  NOTIFICATION_CATEGORIES,
} from 'models/Notification';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IUpdatePayload {
  action: 'UPDATE';
  notificationType: string;
  description: string;
  enabled: boolean;
  emailEnabled: boolean;
  inAppEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
  defaultEmailTemplateId: string | null;
  defaultInAppTemplateId: string | null;
  defaultSmsTemplateId: string | null;
  defaultPushTemplateId: string | null;
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

const AspireAdminNotificationSettings = () => {
  const apiClient = useAPI();
  const [isLoading, setIsLoading] = useState(true);
  const [saveStatus, setSaveStatus] = useState<'idle' | 'success' | 'error'>(
    'idle',
  );
  const [notificationSettings, setNotificationSettings] = useState<
    IList<INotificationSettings>
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
        API_END_POINTS.GET_NOTIFICATION_SETTINGS,
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
      API_END_POINTS.UPDATE_NOTIFICATION_SETTINGS,
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

  const handleToggleNotification = (
    setting: INotificationSettings,
    field: keyof INotificationSettings,
    value: boolean,
  ) => {
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
      // Update local state optimistically
      setNotificationSettings(prev => ({
        ...prev,
        items: prev.items.map(s =>
          s.id === setting.id ? { ...s, [field]: value } : s,
        ),
      }));

      // Prepare payload matching backend schema
      const payload: IUpdatePayload = {
        action: 'UPDATE',
        notificationType: setting.notificationType,
        description: setting.description,
        enabled: field === 'enabled' ? value : setting.enabled,
        emailEnabled: field === 'emailEnabled' ? value : setting.emailEnabled,
        inAppEnabled: field === 'inAppEnabled' ? value : setting.inAppEnabled,
        smsEnabled: field === 'smsEnabled' ? value : setting.smsEnabled,
        pushEnabled: field === 'pushEnabled' ? value : setting.pushEnabled,
        defaultEmailTemplateId: setting.defaultEmailTemplateId,
        defaultInAppTemplateId: setting.defaultInAppTemplateId,
        defaultSmsTemplateId: setting.defaultSmsTemplateId,
        defaultPushTemplateId: setting.defaultPushTemplateId,
      };

      // Call API
      await updateNotificationSetting(payload);
    });
  };

  // const handleGlobalChannelToggle = (
  //   channel: keyof IGlobalChannels,
  //   value: boolean,
  // ) => {
  //   const channelLabels: Record<keyof IGlobalChannels, string> = {
  //     email: 'email',
  //     inApp: 'in-app',
  //     sms: 'SMS',
  //     push: 'push',
  //   };

  //   const action = value ? 'enable' : 'disable';
  //   const message = `Are you sure you want to ${action} ${channelLabels[channel]} notifications for all notification types?`;

  //   openConfirmationDialog(message, async () => {
  //     // Update global channels
  //     setGlobalChannels(prev => ({ ...prev, [channel]: value }));

  //     // Field mapping
  //     const fieldMap: Record<
  //       keyof IGlobalChannels,
  //       keyof INotificationSettings
  //     > = {
  //       email: 'emailEnabled',
  //       inApp: 'inAppEnabled',
  //       sms: 'smsEnabled',
  //       push: 'pushEnabled',
  //     };

  //     // Update all notifications optimistically
  //     setNotificationSettings(prev => ({
  //       ...prev,
  //       items: prev.items.map(setting => ({
  //         ...setting,
  //         [fieldMap[channel]]: value,
  //       })),
  //     }));

  //     // Prepare batch update payloads
  //     const updatePromises = notificationSettings.items.map(setting => {
  //       const payload: IUpdatePayload = {
  //         action: 'UPDATE',
  //         notificationType: setting.notificationType,
  //         description: setting.description,
  //         enabled: setting.enabled,
  //         emailEnabled: channel === 'email' ? value : setting.emailEnabled,
  //         inAppEnabled: channel === 'inApp' ? value : setting.inAppEnabled,
  //         smsEnabled: channel === 'sms' ? value : setting.smsEnabled,
  //         pushEnabled: channel === 'push' ? value : setting.pushEnabled,
  //         defaultEmailTemplateId: setting.defaultEmailTemplateId,
  //         defaultInAppTemplateId: setting.defaultInAppTemplateId,
  //         defaultSmsTemplateId: setting.defaultSmsTemplateId,
  //         defaultPushTemplateId: setting.defaultPushTemplateId,
  //       };
  //       return updateNotificationSetting(payload);
  //     });

  //     await Promise.all(updatePromises);
  //   });
  // };

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="size-8 animate-spin text-primary" />
      </div>
    );
  }

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

      {/* Global Channel Settings */}
      {/* <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bell className="h-5 w-5" />
            Global Channel Preferences
          </CardTitle>
          <CardDescription>
            Control notification channels for all notification types at once
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="flex items-center justify-between p-4 border border-card-border rounded-lg">
              <div className="flex items-center gap-3">
                <Mail className="h-5 w-5 text-blue-600" />
                <div>
                  <Label className="text-base">Email</Label>
                  <p className="text-xs text-muted-foreground">
                    Email notifications
                  </p>
                </div>
              </div>
              <Switch
                checked={globalChannels.email}
                onCheckedChange={(checked: boolean) =>
                  handleGlobalChannelToggle('email', checked)
                }
              />
            </div>

            <div className="flex items-center justify-between p-4 border border-card-border rounded-lg">
              <div className="flex items-center gap-3">
                <Bell className="h-5 w-5 text-purple-600" />
                <div>
                  <Label className="text-base">In-App</Label>
                  <p className="text-xs text-muted-foreground">
                    In-app notifications
                  </p>
                </div>
              </div>
              <Switch
                checked={globalChannels.inApp}
                onCheckedChange={(checked: boolean) =>
                  handleGlobalChannelToggle('inApp', checked)
                }
              />
            </div>

            <div className="flex items-center justify-between p-4 border border-card-border rounded-lg">
              <div className="flex items-center gap-3">
                <MessageSquare className="h-5 w-5 text-green-600" />
                <div>
                  <Label className="text-base">SMS</Label>
                  <p className="text-xs text-muted-foreground">Text messages</p>
                </div>
              </div>
              <Switch
                checked={globalChannels.sms}
                onCheckedChange={(checked: boolean) =>
                  handleGlobalChannelToggle('sms', checked)
                }
              />
            </div>

            <div className="flex items-center justify-between p-4 border border-card-border rounded-lg">
              <div className="flex items-center gap-3">
                <Smartphone className="h-5 w-5 text-orange-600" />
                <div>
                  <Label className="text-base">Push</Label>
                  <p className="text-xs text-muted-foreground">
                    Push notifications
                  </p>
                </div>
              </div>
              <Switch
                checked={globalChannels.push}
                onCheckedChange={(checked: boolean) =>
                  handleGlobalChannelToggle('push', checked)
                }
              />
            </div>
          </div>
        </CardContent>
      </Card> */}

      {/* Categorized Notifications */}
      {Object.entries(NOTIFICATION_CATEGORIES).map(([category, types]) => {
        const categorySettings = notificationSettings.items.filter(
          (s: INotificationSettings) => types.includes(s.notificationType),
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
                {categorySettings.map(setting => (
                  <div
                    key={setting.id}
                    className="rounded-lg border border-card-border p-4"
                  >
                    <div className="mb-3 flex items-start justify-between">
                      <div className="flex-1">
                        <div className="mb-1 flex items-center gap-2">
                          <Label className="text-base font-semibold">
                            {formatNotificationLabel(setting.notificationType)}
                          </Label>
                          {setting.notificationType === 'SECURITY_ALERTS' && (
                            <span className="rounded bg-red-100 px-2 py-0.5 text-xs text-red-700">
                              Required
                            </span>
                          )}
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {setting.description}
                        </p>
                      </div>
                      <Switch
                        checked={setting.enabled}
                        onCheckedChange={(checked: boolean) =>
                          handleToggleNotification(setting, 'enabled', checked)
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

export default AspireAdminNotificationSettings;
