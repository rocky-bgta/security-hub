import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import {
  CardCalendarIcon,
  DownloadIcon,
  ListIcon,
  LockIcon,
} from 'assets/icons';
import IconBackButton from 'components/IconBackButton';
import CourseDetailsLoader from 'components/skeleton/CourseDetails';
import Border from 'components/UserBorder';
import UserChaptersCardView from 'features/course/UserChaptersCardView';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { IUserCourseDetails } from 'models/Course';
import { IResponse } from 'models/Global';
import { IUserPackageDetails } from 'models/Package';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { FILE_PATH_PREFIX, PUBLIC_URL } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

const courseImg = PUBLIC_URL + '/images/certificate.webp';

interface IProps {
  hostPath?: typeof routes;
}

const CourseViewDetails = ({ hostPath = routes }: IProps) => {
  const { userInfo } = useStore();
  const { packageId, slug } = useParams();
  const navigate = useNavigate();
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(false);
  const [courseData, setCourseData] = useState<IUserCourseDetails>();
  const [openBox, setOpenBox] = useState<string>('');
  const [courseProgress, setCourseProgress] = useState<number>(0);
  const [packageData, setPackageData] = useState<IUserPackageDetails>();

  useEffect(() => {
    fetchCourseDetails();
    fetchPackageDetails();
  }, []);

  const fetchCourseDetails = async () => {
    setLoading(true);
    try {
      const response: IResponse<IUserCourseDetails> = await apiClient.get(
        API_END_POINTS.USER_TOPIC_DETAILS.replace(':topicId', slug || '') +
          '?' +
          objectToQueryString({
            userId: userInfo.userId,
            subPackageId: packageId,
          }),
      );
      setCourseData(response.data);
    } catch (error) {
      console.error('Error fetching selected sub package topics:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchPackageDetails = async () => {
    setLoading(true);

    try {
      const response: IResponse<IUserPackageDetails> = await apiClient.get(
        API_END_POINTS.USER_SUB_PACKAGE_DETAILS +
          objectToQueryString({
            subPackageId: packageId,
            userId: userInfo.userId,
          }),
      );
      setCourseProgress(response.data.progress || 0);
      setPackageData(response.data);
    } catch (error) {
      console.error('Error fetching course complete status:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadCertificate = () => {
    const link = document.createElement('a');
    link.href = FILE_PATH_PREFIX + packageData?.imageCertificateLink;
    link.setAttribute('download', 'Certificate.pdf');
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleGoBackToCourse = () => {
    navigate(hostPath.courseList.path + '?course=' + packageId);
  };

  return (
    <div>
      <IconBackButton
        label="Back to Course List"
        onClick={handleGoBackToCourse}
      />
      {loading ? (
        <CourseDetailsLoader />
      ) : (
        <Border>
          <div className="content-p-4 sm:content-p-6">
            <h2 className="content-text-lg content-font-semibold content-text-white sm:content-text-xl">
              {courseData?.topicName || 'N/A'}
            </h2>
            <div className="content-mt-3 content-flex content-flex-wrap content-items-center content-gap-y-2 sm:content-mt-4">
              <div className="content-tooltip content-flex content-items-center content-gap-1 content-border-r content-border-card-border content-pr-3">
                <CardCalendarIcon fill="#FFFFFF80" />
                <span className="content-text-sm content-text-cloudy-white">
                  {courseData?.publishDate
                    ? dayjs(courseData?.publishDate).format('MMMM YYYY')
                    : 'N/A'}
                </span>
                <div className="content-tooltip-text">
                  <span>Published Date</span>
                </div>
              </div>
              <div className="content-tooltip content-flex content-items-center content-gap-2 content-px-3">
                <ListIcon fill="#FFFFFF80" />
                <span className="content-text-sm content-text-cloudy-white">
                  {courseData?.chapterCount || 0} Chapter •{' '}
                  {courseData?.contentCount || 0} Lesson
                </span>
                <div className="content-tooltip-text">
                  <p>Total course chapter-{courseData?.chapterCount || 0}</p>
                  <p>Total course lesson-{courseData?.contentCount || 0}</p>
                </div>
              </div>
            </div>
            <p className="content-mt-3 content-text-sm content-text-white content-text-opacity-75 sm:content-mt-4 sm:content-text-base">
              {courseData?.topicDescription || 'N/A'}
            </p>

            <div className="content-mt-4 sm:content-mt-6">
              <Border>
                <div className="content-flex content-flex-col content-items-center content-gap-4 content-p-4 sm:content-gap-6 sm:content-p-6 sm:content-flex-row lg:content-gap-24">
                  <div className="content-w-full sm:content-w-3/4">
                    <h3 className="content-mb-2 content-text-lg content-font-semibold content-text-white sm:content-text-xl">
                      Course Progress
                    </h3>
                    <p className="content-text-sm content-text-ash-gray sm:content-text-base">
                      You have completed {Math.round(courseProgress || 0)}% of
                      this course.
                    </p>

                    <div className="content-mt-3.5 content-h-2.5 content-w-full content-rounded-full content-bg-white content-bg-opacity-25">
                      <div
                        className="content-h-2.5 content-rounded-full content-bg-primary"
                        style={{
                          width: `${Math.round(Math.round(courseProgress || 0))}%`,
                        }}
                      ></div>
                    </div>
                  </div>
                  <div className="content-flex content-w-full content-justify-center sm:content-w-1/4 sm:content-justify-end">
                    <div className="content-group content-relative">
                      <img
                        src={
                          packageData?.imageCertificateLink
                            ? FILE_PATH_PREFIX +
                              packageData?.imageCertificateLink
                            : courseImg
                        }
                        alt="course"
                        className="content-w-48 content-rounded"
                      />
                      {(courseProgress || 0) < 100 && (
                        <div className="content-absolute content-left-1/2 content-top-1/2 content-flex content-size-full -content-translate-x-1/2 -content-translate-y-1/2 content-transform content-items-center content-justify-center content-bg-black content-bg-opacity-40">
                          <LockIcon fill="#FFFFFF" width={40} height={40} />
                        </div>
                      )}
                      {(courseProgress || 0) >= 100 && (
                        <div className="content-absolute content-left-1/2 content-top-1/2 content-hidden content-size-full -content-translate-x-1/2 -content-translate-y-1/2 content-transform content-cursor-pointer content-bg-black content-bg-opacity-40 group-hover:content-block">
                          <div className="content-flex content-h-full content-items-center content-justify-center">
                            <button onClick={() => handleDownloadCertificate()}>
                              <DownloadIcon
                                fill="#FFFFFF"
                                width={40}
                                height={40}
                              />
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </Border>
            </div>
            <div className="content-mt-4 sm:content-mt-6">
              <h2 className="content-text-lg content-font-semibold content-text-white sm:content-text-xl">
                Course Chapters ({courseData?.chapterCount || 0})
              </h2>
              <div className="content-mt-4 content-flex content-flex-col content-space-y-4 sm:content-mt-6 sm:content-space-y-6">
                <UserChaptersCardView
                  chapters={courseData?.chapters || []}
                  courseId={courseData?.topicId || ''}
                  openBox={openBox}
                  setOpenBox={setOpenBox}
                  hostPath={hostPath}
                  packageId={packageId || ''}
                />
              </div>
            </div>
          </div>
        </Border>
      )}
    </div>
  );
};

export default CourseViewDetails;
