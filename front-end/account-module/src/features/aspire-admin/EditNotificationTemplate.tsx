import { Badge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
import IconBackButton from 'components/IconBackButton';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import { Switch } from 'components/common/Switch';
import { Textarea } from 'components/common/Textarea';
import EmailTemplateEditor from 'components/common/EmailTemplateEditor';
import NotificationTemplateEmailPreview from 'features/aspire-admin/NotificationTemplateEmailPreview';
import { useAPI } from 'hooks/UseAPI';
import { Loader2 } from 'lucide-react';
import {
  INotificationTemplate,
  NOTIFICATION_CHANNELS,
  NOTIFICATION_ROLES,
  NotificationChannel,
  toUpdateNotificationTemplatePayload,
} from 'models/Notification';
import { useEffect, useState } from 'react';
import { Navigate, useLocation, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Route';
import { isSuccessResponse } from 'utils/Helper';

export interface INotificationTemplateListFilters {
  recipientRole: string;
  notificationType: string;
  channel: string;
  offset: number;
}

export interface IEditNotificationTemplateLocationState {
  template: INotificationTemplate;
  listFilters: INotificationTemplateListFilters;
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

const getChannelLabel = (channel: string): string => {
  return NOTIFICATION_CHANNELS.find(c => c.value === channel)?.label ?? channel;
};

const isHtmlEmpty = (html: string): boolean => {
  const stripped = html.replace(/<[^>]*>/g, '').trim();
  return stripped.length === 0;
};

const EditNotificationTemplate = () => {
  const apiClient = useAPI();
  const navigate = useNavigate();
  const location = useLocation();
  const { templateId } = useParams<{ templateId: string }>();
  const locationState = location.state as
    | IEditNotificationTemplateLocationState
    | undefined;
  const template = locationState?.template;
  const listFilters = locationState?.listFilters;

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [templateName, setTemplateName] = useState('');
  const [subjectTemplate, setSubjectTemplate] = useState('');
  const [htmlTemplate, setHtmlTemplate] = useState('');
  const [textTemplate, setTextTemplate] = useState('');
  const [messageTemplate, setMessageTemplate] = useState('');
  const [isActive, setIsActive] = useState(true);
  const [isDefault, setIsDefault] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!template) {
      return;
    }

    setTemplateName(template.templateName);
    setSubjectTemplate(template.subjectTemplate);
    setHtmlTemplate(template.htmlTemplate);
    setTextTemplate(template.textTemplate);
    setMessageTemplate(template.messageTemplate);
    setIsActive(template.active);
    setIsDefault(template.default);
    setErrors({});
  }, [template]);

  if (!template || !templateId || template.id !== templateId) {
    return <Navigate to={routes.notificationTemplates.path} replace />;
  }

  const navigateToList = () => {
    navigate(routes.notificationTemplates.path, {
      state: listFilters ? { listFilters } : undefined,
    });
  };

  const validateForm = (): boolean => {
    const nextErrors: Record<string, string> = {};

    if (!templateName.trim()) {
      nextErrors.templateName = 'Template name is required';
    }

    if (template.channel === NotificationChannel.EMAIL) {
      if (isHtmlEmpty(htmlTemplate)) {
        nextErrors.htmlTemplate = 'HTML template is required';
      }
    } else if (template.channel === NotificationChannel.SMS) {
      if (!textTemplate.trim()) {
        nextErrors.textTemplate = 'SMS template is required';
      }
    } else if (template.channel === NotificationChannel.IN_APP) {
      if (!messageTemplate.trim()) {
        nextErrors.messageTemplate = 'In-app message template is required';
      }
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      return;
    }

    const payload = toUpdateNotificationTemplatePayload(template, {
      templateName: templateName.trim(),
      subjectTemplate,
      htmlTemplate,
      textTemplate,
      messageTemplate,
      isActive,
      isDefault,
    });

    try {
      setIsSubmitting(true);
      const response = await apiClient.post(
        API_END_POINTS.UPDATE_NOTIFICATION_TEMPLATES,
        { data: payload },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success('Notification template updated successfully');
        navigateToList();
        return;
      }

      toast.error('Failed to update notification template');
    } catch (error) {
      console.error('Failed to update notification template:', error);
      toast.error('Failed to update notification template');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="space-y-3">
          <IconBackButton
            onClick={navigateToList}
            label="Back to Templates"
          />
          <div>
            <h1 className="text-3xl font-bold tracking-tight">
              Edit Notification Template
            </h1>
            <p className="mt-2 text-muted-foreground">
              Update the template content and settings for this notification.
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Badge variant="secondary">
              {formatNotificationLabel(template.notificationType)}
            </Badge>
            <Badge variant="outline">
              {getChannelLabel(template.channel)}
            </Badge>
            <Badge variant="outline">
              {getRoleLabel(template.recipientRole)}
            </Badge>
          </div>
        </div>

        <div className="flex gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={navigateToList}
            disabled={isSubmitting}
          >
            Cancel
          </Button>
          <Button type="button" onClick={handleSubmit} disabled={isSubmitting}>
            {isSubmitting && <Loader2 className="size-4 animate-spin" />}
            Save Changes
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Template Details</CardTitle>
          <CardDescription>
            Configure the template name and status settings
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="templateName">Template Name</Label>
            <Input
              id="templateName"
              value={templateName}
              onChange={event => {
                setTemplateName(event.target.value);
                if (errors.templateName) {
                  setErrors(prev => ({ ...prev, templateName: '' }));
                }
              }}
              placeholder="Enter template name"
            />
            {errors.templateName && (
              <p className="text-sm text-destructive">{errors.templateName}</p>
            )}
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div className="flex items-center justify-between rounded-lg border border-card-border p-4">
              <div className="space-y-1">
                <Label>Active</Label>
                <p className="text-sm text-muted-foreground">
                  Enable or disable this template
                </p>
              </div>
              <Switch checked={isActive} onCheckedChange={setIsActive} />
            </div>

            <div className="flex items-center justify-between rounded-lg border border-card-border p-4">
              <div className="space-y-1">
                <Label>Default</Label>
                <p className="text-sm text-muted-foreground">
                  Mark as the default template
                </p>
              </div>
              <Switch checked={isDefault} onCheckedChange={setIsDefault} />
            </div>
          </div>
        </CardContent>
      </Card>

      {template.channel === NotificationChannel.EMAIL && (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Email Content</CardTitle>
              <CardDescription>
                Edit the subject and HTML body for this email template
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="subjectTemplate">Subject</Label>
                <Input
                  id="subjectTemplate"
                  value={subjectTemplate}
                  onChange={event => setSubjectTemplate(event.target.value)}
                  placeholder="Enter email subject"
                />
              </div>

              <div className="space-y-2">
                <Label>HTML Template</Label>
                <EmailTemplateEditor
                  value={htmlTemplate}
                  onChange={setHtmlTemplate}
                  error={errors.htmlTemplate}
                />
              </div>
            </CardContent>
          </Card>

          <NotificationTemplateEmailPreview
            subject={subjectTemplate}
            html={htmlTemplate}
          />
        </div>
      )}

      {template.channel === NotificationChannel.SMS && (
        <Card>
          <CardHeader>
            <CardTitle>SMS Content</CardTitle>
            <CardDescription>
              Edit the SMS message body for this template
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <Label htmlFor="textTemplate">SMS Template</Label>
              <Textarea
                id="textTemplate"
                value={textTemplate}
                onChange={event => {
                  setTextTemplate(event.target.value);
                  if (errors.textTemplate) {
                    setErrors(prev => ({ ...prev, textTemplate: '' }));
                  }
                }}
                placeholder="Enter SMS template content"
                rows={12}
              />
              {errors.textTemplate && (
                <p className="text-sm text-destructive">{errors.textTemplate}</p>
              )}
            </div>
          </CardContent>
        </Card>
      )}

      {template.channel === NotificationChannel.IN_APP && (
        <Card>
          <CardHeader>
            <CardTitle>In-App Content</CardTitle>
            <CardDescription>
              Edit the in-app message body for this template
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <Label htmlFor="messageTemplate">In-App Message Template</Label>
              <Textarea
                id="messageTemplate"
                value={messageTemplate}
                onChange={event => {
                  setMessageTemplate(event.target.value);
                  if (errors.messageTemplate) {
                    setErrors(prev => ({ ...prev, messageTemplate: '' }));
                  }
                }}
                placeholder="Enter in-app message template content"
                rows={12}
              />
              {errors.messageTemplate && (
                <p className="text-sm text-destructive">
                  {errors.messageTemplate}
                </p>
              )}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default EditNotificationTemplate;
