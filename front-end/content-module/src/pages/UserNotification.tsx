import { Button } from 'common/Button';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import NotificationCard from 'components/NotificationCard';
import Border from 'components/UserBorder';
import UserHeading from 'components/UserHeading';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { IGetListParams, IList } from 'models/Global';
import { INotification } from 'models/Notification';
import { useEffect, useState } from 'react';
import { IoMdCheckmarkCircle, IoMdNotificationsOutline } from 'react-icons/io';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  formatDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

const UserNotification = () => {
  const [notifications, setNotifications] = useState<IList<INotification>>({
    offset: 0,
    pageSize: 0,
    total: 0,
    items: [],
  });
  const apiClient = useAPI();
  const {
    notificationChangeKey,
    markNotificationAsRead,
    markAllNotificationsAsRead,
  } = useStore();
  const [notificationCount, setNotificationCount] = useState(0);
  const [queryString, setQueryString] = useState('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    pageSize: 10,
    offset: 0,
    search: '',
  });
  const searchDebounce = useDebounce(queryString, 500);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      getNotifications();
    }
  }, [searchDebounce]);

  const getNotifications = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_USER_NOTIFICATIONS + queryString,
      );
      setNotifications(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const getNotificationCount = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_USER_NOTIFICATION_COUNT,
      );
      if (isSuccessResponse(response.statusCode)) {
        setNotificationCount(response.data);
      }
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    getNotifications();
    getNotificationCount();
  }, [notificationChangeKey]);

  const markAsRead = async (id: string) => {
    const success = await markNotificationAsRead(id);
    if (success) {
      toast.success('Notification marked as read');
    }
  };

  const markAllAsRead = async () => {
    const success = await markAllNotificationsAsRead();
    if (success) {
      toast.success('All notifications marked as read');
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div>
      <UserHeading
        variant="title"
        text="Notifications"
        className="content-mb-4 sm:content-mb-6"
      />
      <Border className="content-mt-4 content-p-4 sm:content-p-6">
        <div className="content-mb-4 content-flex content-flex-col content-gap-3 content-justify-between sm:content-mb-6 sm:content-flex-row sm:content-items-center sm:content-gap-4">
          <div>
            <UserHeading variant="subtitle" text="Notification List" />
            <p className="content-text-sm content-text-cloudy-white">
              Stay up to date with your latest alerts and system messages.{' '}
              <span className="content-text-primary">
                ({notificationCount} unread)
              </span>
            </p>
          </div>
          <div className="content-flex content-flex-col content-gap-2 sm:content-flex-row sm:content-items-center sm:content-gap-3">
            <Input
              placeholder="Search notifications"
              value={queryParams.search}
              onChange={e =>
                setQueryParams(prevState => ({
                  ...prevState,
                  search: e.target.value,
                  offset: 0,
                }))
              }
              className="content-w-full content-bg-transparent sm:content-w-80"
            />
            {notificationCount > 0 && (
              <Button
                variant="link"
                onClick={markAllAsRead}
                disabled={notifications.items.length === 0}
                className="content-flex content-items-center content-gap-2 content-text-sm content-text-primary content-transition-colors hover:content-text-primary/90"
              >
                <IoMdCheckmarkCircle className="content-size-4" />
                Mark all as read
              </Button>
            )}
          </div>
        </div>
        <div>
          {notifications.items.length === 0 ? (
            <div className="content-flex content-flex-col content-items-center content-justify-center content-py-12">
              <IoMdNotificationsOutline className="content-mb-3 content-text-5xl content-text-graphite" />
              <p className="content-text-cloudy-white">No notifications</p>
            </div>
          ) : (
            notifications.items.map(notification => (
              <NotificationCard
                key={notification.id}
                id={notification.id}
                title={notification.title}
                message={notification.message}
                notificationType={notification.notificationType}
                date={formatDateAndTime(notification.createdAt ?? '')}
                read={notification.read}
                onMarkAsRead={markAsRead}
                onClose={() => markAsRead(notification.id)}
              />
            ))
          )}

          <div className="content-mt-6">
            <Pagination
              perPage={notifications.pageSize}
              total={notifications.total}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </div>
      </Border>
    </div>
  );
};

export default UserNotification;
