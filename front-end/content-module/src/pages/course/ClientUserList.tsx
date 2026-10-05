import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Fragment } from 'react/jsx-runtime';

import { Button } from 'common/Button';
import Border from 'components/UserBorder';
import UserHeading from 'components/UserHeading';
import UserTopicList from 'features/course/UserTopicList';
import UserSubPackageList from 'features/package/sub-package/UserSubPackageList';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { IResponse } from 'models/Global';
import {
  IUserSubPackage,
  IUserSubPackageTopic,
  UserSubPackageStatus,
} from 'models/Package';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { cn, objectToQueryString } from 'utils/Helper';

interface IProps {
  hostPath: typeof routes;
  isDashboard?: boolean;
}

const ClientUserCourseList = ({ hostPath, isDashboard }: IProps) => {
  const { userInfo } = useStore();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [selectedSubpackageStatus, setSelectedSubpackageStatus] =
    useState<UserSubPackageStatus>(UserSubPackageStatus.NOT_COMPLETED);
  const [subPackageLoading, setSubPackageLoading] = useState<boolean>(true);
  const [subPackageList, setSubPackageList] = useState<Array<IUserSubPackage>>(
    [],
  );
  const [selectedSubPackage, setSelectedSubPackage] =
    useState<IUserSubPackage | null>(null);
  const [topicList, setTopicList] = useState<Array<IUserSubPackageTopic>>([]);
  const [topicListLoading, setTopicListLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const selectedSubpackageId = searchParams.get('course');

  useEffect(() => {
    fetchSubPackageList();
  }, []);

  useEffect(() => {
    if (selectedSubPackage || selectedSubpackageId) {
      fetchSubPackageTopicList();
    }
  }, [selectedSubPackage, selectedSubpackageId]);

  const completeCount = subPackageList.filter(
    sub =>
      sub.status === UserSubPackageStatus.COMPLETED ||
      sub.status === UserSubPackageStatus.PHISHING_TRAINING_COMPLETED,
  ).length;

  const pendingCount = subPackageList.filter(
    sub =>
      sub.status !== UserSubPackageStatus.COMPLETED &&
      sub.status !== UserSubPackageStatus.PHISHING_TRAINING_COMPLETED,
  ).length;

  const fetchSubPackageList = async () => {
    try {
      const response: IResponse<Array<IUserSubPackage>> = await apiClient.get(
        API_END_POINTS.GET_USER_SUB_PACKAGES + userInfo.userId,
      );

      setSubPackageList(response.data);

      if (selectedSubpackageId) {
        const selectedSubPackage = response.data.find(
          sub => sub.subPackageId === selectedSubpackageId,
        );

        if (selectedSubPackage) {
          setSelectedSubPackage(selectedSubPackage);
          setSelectedSubpackageStatus(
            selectedSubPackage.status === UserSubPackageStatus.COMPLETED
              ? UserSubPackageStatus.COMPLETED
              : UserSubPackageStatus.NOT_COMPLETED,
          );
          setSubPackageLoading(false);
          return;
        }
      }
      if (!isDashboard) {
        navigate(
          '?course=' +
            response.data.find(
              sub => sub.status !== UserSubPackageStatus.COMPLETED,
            )?.subPackageId || '',
        );
      }

      if (selectedSubpackageStatus === UserSubPackageStatus.COMPLETED) {
        setSelectedSubPackage(
          response.data.find(
            sub => sub.status === UserSubPackageStatus.COMPLETED,
          ) ??
            response.data[0] ??
            null,
        );
      } else {
        setSelectedSubPackage(
          response.data.find(
            sub => sub.status !== UserSubPackageStatus.COMPLETED,
          ) ??
            response.data[0] ??
            null,
        );
      }
    } catch (error) {
      console.error('Error fetching sub packages:', error);
    } finally {
      setSubPackageLoading(false);
    }
  };

  const fetchSubPackageTopicList = async () => {
    if (!selectedSubPackage) return;

    setTopicListLoading(true);
    try {
      const response: IResponse<Array<IUserSubPackageTopic>> =
        await apiClient.get(
          API_END_POINTS.GET_USER_SUB_PACKAGE_TOPICS.replace(
            ':subPackageId',
            selectedSubPackage?.subPackageId || '',
          ) +
            '?' +
            objectToQueryString({ userId: userInfo.userId }),
        );

      setTopicList(response.data);
    } catch (error) {
      console.error('Error fetching selected sub package topics:', error);
    } finally {
      setTopicListLoading(false);
    }
  };

  const handleChangeSubpackageStatus = (status: UserSubPackageStatus) => {
    setSelectedSubpackageStatus(status);
    setTopicList([]);
    navigate(
      '?course=' +
        (status === UserSubPackageStatus.COMPLETED
          ? subPackageList.find(
              sub =>
                sub.status === UserSubPackageStatus.COMPLETED ||
                sub.status === UserSubPackageStatus.PHISHING_TRAINING_COMPLETED,
            )?.subPackageId
          : subPackageList.find(
              sub =>
                sub.status !== UserSubPackageStatus.COMPLETED &&
                sub.status !== UserSubPackageStatus.PHISHING_TRAINING_COMPLETED,
            )?.subPackageId),
    );
    setSelectedSubPackage(
      status === UserSubPackageStatus.COMPLETED
        ? subPackageList.find(
            sub =>
              sub.status === UserSubPackageStatus.COMPLETED ||
              sub.status === UserSubPackageStatus.PHISHING_TRAINING_COMPLETED,
          ) || null
        : subPackageList.find(
            sub =>
              sub.status !== UserSubPackageStatus.COMPLETED &&
              sub.status !== UserSubPackageStatus.PHISHING_TRAINING_COMPLETED,
          ) || null,
    );
  };

  const handleSeeMore = () => {
    navigate(
      hostPath.courseList.path + '?course=' + selectedSubPackage?.subPackageId,
    );
  };

  const filteredSubPackages =
    selectedSubpackageStatus === UserSubPackageStatus.COMPLETED
      ? subPackageList.filter(
          sub =>
            sub.status === UserSubPackageStatus.COMPLETED ||
            sub.status === UserSubPackageStatus.PHISHING_TRAINING_COMPLETED,
        )
      : subPackageList.filter(
          sub =>
            sub.status !== UserSubPackageStatus.COMPLETED &&
            sub.status !== UserSubPackageStatus.PHISHING_TRAINING_COMPLETED,
        );

  return (
    <Fragment>
      {!isDashboard && (
        <UserHeading
          variant="title"
          text="All Courses"
          className="content-mb-4 sm:content-mb-6"
        />
      )}
      <Border>
        <div className="content-p-4 sm:content-p-6">
          <div className="content-mb-4 sm:content-mb-6">
            {isDashboard && (
              <UserHeading
                variant="subtitle"
                text="All Courses"
                className="content-mb-4 sm:content-mb-6"
              />
            )}

            <div className="content-flex content-flex-wrap content-items-center content-gap-3 sm:content-gap-10">
              <button
                onClick={() =>
                  handleChangeSubpackageStatus(
                    UserSubPackageStatus.NOT_COMPLETED,
                  )
                }
                className={cn(
                  selectedSubpackageStatus ===
                    UserSubPackageStatus.NOT_COMPLETED &&
                    'content-text-primary',
                  'content-text-base content-text-cloudy-white md:content-text-lg',
                )}
              >
                Pending Courses ({pendingCount})
              </button>
              <button
                onClick={() =>
                  handleChangeSubpackageStatus(UserSubPackageStatus.COMPLETED)
                }
                className={cn(
                  selectedSubpackageStatus === UserSubPackageStatus.COMPLETED &&
                    'content-text-primary',
                  'content-text-base content-text-cloudy-white md:content-text-lg',
                )}
              >
                Completed Courses ({completeCount})
              </button>
            </div>
          </div>
          <UserSubPackageList
            loading={subPackageLoading}
            data={filteredSubPackages}
            selectedSubPackage={selectedSubPackage}
            setSelectedSubPackage={setSelectedSubPackage}
          />

          {!subPackageLoading && filteredSubPackages.length > 0 && (
            <UserTopicList
              loading={topicListLoading}
              data={isDashboard ? topicList.slice(0, 8) : topicList}
              subPackageId={selectedSubPackage?.subPackageId}
            />
          )}
        </div>

        {isDashboard && topicList.length > 8 && (
          <div className="content-pb-6">
            <Button
              className="content-mx-auto content-rounded-full"
              onClick={handleSeeMore}
            >
              See More
            </Button>
          </div>
        )}
      </Border>
    </Fragment>
  );
};

export default ClientUserCourseList;
