import { Fragment } from 'react/jsx-runtime';

import Border from 'components/UserBorder';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { IResponse, RiskGroup, Status } from 'models/Global';
import { IUserInfo } from 'models/Users';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { cn } from 'utils/Helper';

const MyProfile = () => {
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const [userDetails, setUserDetails] = useState<IUserInfo>();

  useEffect(() => {
    fetchUserInfo();
  }, []);

  const fetchUserInfo = async () => {
    try {
      const response: IResponse<IUserInfo> = await apiClient.get(
        API_END_POINTS.GET_USER_INFO.replace(':userId', userInfo?.userId),
      );
      setUserDetails(response.data);
    } catch (error) {
      console.error('Failed to fetch user info:', error);
    }
  };

  return (
    <Fragment>
      <h2 className="content-mb-6 content-text-xl content-text-white">
        Profile Information
      </h2>
      <div className="content-mb-6">
        <Border>
          <div className="content-flex content-items-center content-gap-6 content-px-8 content-py-6">
            <img
              src={
                userDetails?.profilePicture
                  ? FILE_PATH_PREFIX + userDetails?.profilePicture
                  : 'https://placehold.co/80x80'
              }
              alt="Profile-img"
              className="content-size-20 content-rounded-full content-object-cover"
            />
            <div className="content-flex content-flex-col content-gap-1">
              <h3 className="content-text-lg content-font-semibold content-text-cloudy-white">
                {userDetails?.firstName} {userDetails?.lastName}
              </h3>
              <a className="content-text-base content-font-normal content-text-cloudy-white">
                {userDetails?.email}
              </a>
              <p
                className={cn(
                  'content-w-fit content-rounded content-px-2 content-py-1 content-text-base content-font-normal content-text-cloudy-white',
                  userDetails?.status == Status.ACTIVE
                    ? 'content-bg-green-500/10 content-text-green-500'
                    : userDetails?.status == Status.INACTIVE
                      ? 'content-bg-red-500/10 content-text-red-500'
                      : 'content-bg-gray-500/10 content-text-gray-500',
                )}
              >
                {userDetails?.status == Status.ACTIVE
                  ? 'Active'
                  : userDetails?.status == Status.INACTIVE
                    ? 'Inactive'
                    : 'Disabled'}
              </p>
            </div>
          </div>
        </Border>
      </div>
      <div className="content-mb-6">
        <Border>
          <div className="content-p-6">
            <h2 className="content-mb-6 content-text-xl content-text-white">
              Profile Information
            </h2>
            <div className="content-grid content-grid-cols-1 content-gap-6 md:content-grid-cols-2">
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  First Name
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.firstName}
                </span>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Last Name
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.lastName}
                </span>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Email Address
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.email}
                </span>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Phone Number
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.phoneNumber}
                </span>
              </div>
              {/* <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Organization Name
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.organizationName}
                </span>
              </div> */}
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Department
                </span>
                <span className="content-capitalize content-text-cloudy-white">
                  {userDetails?.department.toLocaleLowerCase()}
                </span>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Risk Group
                </span>
                <p className="content-m-0 content-text-base content-font-normal content-text-cloudy-white">
                  {userDetails?.riskGroup == RiskGroup.LOW_RISK
                    ? 'Safe Users'
                    : userDetails?.riskGroup == RiskGroup.MEDIUM_RISK
                      ? 'Medium Risk'
                      : userDetails?.riskGroup == RiskGroup.HIGH_RISK
                        ? 'High Risk'
                        : userDetails?.riskGroup == RiskGroup.CRITICAL_RISK
                          ? 'Critical Risk'
                          : 'Safe Users'}
                </p>
              </div>
              {/* <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Designation/Job Title
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.designation}
                </span>
              </div> */}
            </div>
          </div>
        </Border>
      </div>
      {/* <div className="content-mb-6">
        <Border>
          <div className="content-p-6">
            <h2 className="content-mb-6 content-text-xl content-text-white">
              Address
            </h2>
            <div className="content-grid content-grid-cols-1 md:content-grid-cols-2">
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Country
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.countryName}
                </span>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Address
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.address}
                </span>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Time Zone
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.timeZone}
                </span>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Language
                </span>
                <span className="content-text-cloudy-white">
                  {userDetails?.language}
                </span>
              </div>
            </div>
          </div>
        </Border>
      </div> */}
      {/* <div className="content-mb-6">
        <Border>
          <div className="content-p-6">
            <h2 className="content-mb-6 content-text-xl content-text-white">
              Other Info
            </h2>
            <div className="content-grid content-grid-cols-1 content-gap-6 md:content-grid-cols-2">
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Supervisor Name
                </span>
                <span className="content-text-cloudy-white">MD Abdur Razzak</span>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Supervisor Email
                </span>
                <a
                  href="mailto:developerrazzak@gmail.com"
                  className="content-text-cloudy-white"
                >
                  developerrazzak@gmail.com.com
                </a>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Language
                </span>
                <span className="content-text-cloudy-white">Bengali</span>
              </div>
              <div>
                <span className="content-mb-2 content-block content-text-ash-gray">
                  Group
                </span>
                <span className="content-text-cloudy-white">Safe Users</span>
              </div>
            </div>
          </div>
        </Border>
      </div> */}
    </Fragment>
  );
};

export default MyProfile;
