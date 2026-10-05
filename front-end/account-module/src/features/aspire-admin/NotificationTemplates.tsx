import { Badge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
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
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import SearchSelect from 'components/SearchSelect';
import NotificationSettingsSkeleton from 'components/Skelton/NotificationSettings';
import { INotificationTemplateListFilters } from 'features/aspire-admin/EditNotificationTemplate';
import { useAPI } from 'hooks/UseAPI';
import { useUsedNotificationTypes } from 'hooks/UseUsedNotificationTypes';
import { Edit, RotateCcw } from 'lucide-react';
import { IList } from 'models/Global';
import {
  INotificationTemplate,
  NOTIFICATION_CHANNELS,
  NOTIFICATION_ROLES,
  NotificationChannel,
} from 'models/Notification';
import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Route';

interface IListLocationState {
  listFilters?: INotificationTemplateListFilters;
}

const DEFAULT_PAGE_SIZE = 20;

const EMPTY_LIST_FILTERS: INotificationTemplateListFilters = {
  recipientRole: '',
  notificationType: '',
  channel: '',
  offset: 0,
};

const EMPTY_TEMPLATES: IList<INotificationTemplate> = {
  offset: 0,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
  items: [],
};

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

const formatDateTime = (dateString: string): string => {
  return new Date(dateString).toLocaleString();
};

const getSubjectOrTitle = (template: INotificationTemplate): string => {
  if (template.channel === NotificationChannel.EMAIL) {
    return template.subjectTemplate || '—';
  }

  if (template.channel === NotificationChannel.IN_APP) {
    return template.titleTemplate || '—';
  }

  return template.textTemplate || '—';
};

const buildQueryString = (params: {
  recipientRole: string;
  notificationType: string;
  channel: string;
  offset: number;
  pageSize: number;
}): string => {
  const searchParams = new URLSearchParams();
  searchParams.set('recipientRole', params.recipientRole);
  searchParams.set('notificationType', params.notificationType);
  searchParams.set('channel', params.channel);
  searchParams.set('offset', String(params.offset));
  searchParams.set('pageSize', String(params.pageSize));

  return `?${searchParams.toString()}`;
};

const areFiltersComplete = (
  recipientRole: string,
  notificationType: string,
  channel: string,
): boolean => {
  return Boolean(recipientRole && notificationType && channel);
};

const NotificationTemplates = () => {
  const apiClient = useAPI();
  const { notificationTypes } = useUsedNotificationTypes();
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoading, setIsLoading] = useState(false);
  const [recipientRoleFilter, setRecipientRoleFilter] = useState('');
  const [notificationTypeFilter, setNotificationTypeFilter] = useState('');
  const [channelFilter, setChannelFilter] = useState('');
  const [offset, setOffset] = useState(EMPTY_LIST_FILTERS.offset);
  const [filterResetKey, setFilterResetKey] = useState(0);
  const [templates, setTemplates] =
    useState<IList<INotificationTemplate>>(EMPTY_TEMPLATES);

  const filtersComplete = areFiltersComplete(
    recipientRoleFilter,
    notificationTypeFilter,
    channelFilter,
  );

  const clearNavigationState = useCallback(() => {
    navigate(location.pathname, { replace: true, state: null });
  }, [navigate, location.pathname]);

  useEffect(() => {
    const listFilters = (location.state as IListLocationState | undefined)
      ?.listFilters;

    if (!listFilters) {
      return;
    }

    setRecipientRoleFilter(listFilters.recipientRole);
    setNotificationTypeFilter(listFilters.notificationType);
    setChannelFilter(listFilters.channel);
    setOffset(listFilters.offset);
    clearNavigationState();
  }, [location.state, clearNavigationState]);

  const getNotificationTemplates = useCallback(async () => {
    if (!filtersComplete) {
      setTemplates(EMPTY_TEMPLATES);
      return;
    }

    try {
      setIsLoading(true);
      const query = buildQueryString({
        recipientRole: recipientRoleFilter,
        notificationType: notificationTypeFilter,
        channel: channelFilter,
        offset,
        pageSize: DEFAULT_PAGE_SIZE,
      });
      const response = await apiClient.get(
        `${API_END_POINTS.GET_NOTIFICATION_TEMPLATES}${query}`,
      );
      setTemplates(response.data);
    } catch (error) {
      console.error('Failed to fetch notification templates:', error);
      toast.error('Failed to load notification templates');
    } finally {
      setIsLoading(false);
    }
  }, [
    apiClient,
    recipientRoleFilter,
    notificationTypeFilter,
    channelFilter,
    offset,
    filtersComplete,
  ]);

  useEffect(() => {
    getNotificationTemplates();
  }, [getNotificationTemplates]);

  const handleRecipientRoleChange = (value: string) => {
    setRecipientRoleFilter(value);
    setNotificationTypeFilter('');
    setChannelFilter('');
    setOffset(0);
  };

  const handleNotificationTypeChange = (value: string) => {
    setNotificationTypeFilter(value);
    setChannelFilter('');
    setOffset(0);
  };

  const handleChannelChange = (value: string) => {
    setChannelFilter(value);
    setOffset(0);
  };

  const hasActiveFilters = Boolean(
    recipientRoleFilter || notificationTypeFilter || channelFilter,
  );

  const handleResetFilters = () => {
    setRecipientRoleFilter(EMPTY_LIST_FILTERS.recipientRole);
    setNotificationTypeFilter(EMPTY_LIST_FILTERS.notificationType);
    setChannelFilter(EMPTY_LIST_FILTERS.channel);
    setOffset(EMPTY_LIST_FILTERS.offset);
    setIsLoading(false);
    setTemplates(EMPTY_TEMPLATES);
    setFilterResetKey(previousKey => previousKey + 1);
    clearNavigationState();
  };

  const handleEditTemplate = (template: INotificationTemplate) => {
    navigate(
      routes.editNotificationTemplate.path.replace(':templateId', template.id),
      {
        state: {
          template,
          listFilters: {
            recipientRole: recipientRoleFilter,
            notificationType: notificationTypeFilter,
            channel: channelFilter,
            offset,
          },
        },
      },
    );
  };

  const pageSize = templates.pageSize || DEFAULT_PAGE_SIZE;
  const total = templates.total;
  const rangeStart = total === 0 ? 0 : offset * pageSize + 1;
  const rangeEnd = Math.min((offset + 1) * pageSize, total);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Notification Templates
        </h1>
        <p className="mt-2 text-muted-foreground">
          View notification templates by recipient role, notification type, and
          channel
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>
            Select recipient role, notification type, and channel to load
            templates
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div
            key={filterResetKey}
            className="grid grid-cols-1 gap-4 md:grid-cols-3"
          >
            <div className="space-y-2">
              <Label>Recipient Role</Label>
              <Select
                value={recipientRoleFilter || undefined}
                onValueChange={handleRecipientRoleChange}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select recipient role" />
                </SelectTrigger>
                <SelectContent>
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
                items={notificationTypes.map(type => ({
                  label: formatNotificationLabel(type),
                  value: type,
                }))}
                onValueChange={handleNotificationTypeChange}
                placeholder="Select notification type"
                disabled={!recipientRoleFilter}
              />
            </div>

            <div className="space-y-2">
              <Label>Channel</Label>
              <Select
                value={channelFilter || undefined}
                onValueChange={handleChannelChange}
                disabled={!notificationTypeFilter}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select channel" />
                </SelectTrigger>
                <SelectContent>
                  {NOTIFICATION_CHANNELS.map(channel => (
                    <SelectItem key={channel.value} value={channel.value}>
                      {channel.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="mt-4 flex justify-end">
            <Button
              type="button"
              variant="outline"
              onClick={handleResetFilters}
              disabled={!hasActiveFilters}
            >
              <RotateCcw className="size-4" />
              Reset Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      {!filtersComplete ? (
        <Card>
          <CardContent className="py-8 text-center text-muted-foreground">
            Select recipient role, notification type, and channel to view
            notification templates.
          </CardContent>
        </Card>
      ) : isLoading ? (
        <NotificationSettingsSkeleton />
      ) : (
        <>
          {templates.items.length > 0 ? (
            <Card>
              <CardHeader>
                <CardTitle>Templates</CardTitle>
                <CardDescription>
                  Notification templates matching the selected filters
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Template Name</TableHead>
                      <TableHead>Notification Type</TableHead>
                      <TableHead>Channel</TableHead>
                      <TableHead>Recipient Role</TableHead>
                      <TableHead>Subject / Title</TableHead>
                      <TableHead>Active</TableHead>
                      <TableHead>Default</TableHead>
                      <TableHead>Updated</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {templates.items.map(template => (
                      <TableRow key={template.id}>
                        <TableCell className="font-medium">
                          {template.templateName}
                        </TableCell>
                        <TableCell>
                          {formatNotificationLabel(template.notificationType)}
                        </TableCell>
                        <TableCell>
                          {getChannelLabel(template.channel)}
                        </TableCell>
                        <TableCell>
                          {getRoleLabel(template.recipientRole)}
                        </TableCell>
                        <TableCell className="max-w-xs truncate">
                          {getSubjectOrTitle(template)}
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={template.active ? 'default' : 'secondary'}
                          >
                            {template.active ? 'Active' : 'Inactive'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={template.default ? 'default' : 'outline'}
                          >
                            {template.default ? 'Yes' : 'No'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          {formatDateTime(template.updatedAt)}
                        </TableCell>
                        <TableCell>
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => handleEditTemplate(template)}
                            aria-label={`Edit ${template.templateName}`}
                          >
                            <Edit className="size-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardContent className="py-8 text-center text-muted-foreground">
                No templates found for the selected filters.
              </CardContent>
            </Card>
          )}

          {total > 0 && (
            <div className="flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                Showing {rangeStart}–{rangeEnd} of {total} templates
              </p>
            </div>
          )}
        </>
      )}

      {filtersComplete && (
        <Pagination
          key={`${filterResetKey}-${recipientRoleFilter}-${notificationTypeFilter}-${channelFilter}-${offset}`}
          total={total}
          perPage={pageSize}
          onPageChange={page => setOffset(page - 1)}
        />
      )}
    </div>
  );
};

export default NotificationTemplates;
