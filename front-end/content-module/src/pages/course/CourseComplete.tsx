import { DownloadIcon } from 'assets/icons';
import { Button } from 'common/Button';
import Border from 'components/UserBorder';
import { useAPI } from 'hooks/UseAPI';
import { useDownloader } from 'hooks/UseDownloader';
import { useStore } from 'hooks/UseStore';
import { IUserCertificate } from 'models/Certificate';
import { IList, IResponse } from 'models/Global';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

const CourseComplete = () => {
  const { userInfo } = useStore();
  const { slug } = useParams();
  const navigate = useNavigate();
  const [animate, setAnimate] = useState(false);
  const [loading, setLoading] = useState(true);
  const [certificateData, setCertificateData] = useState<
    IList<IUserCertificate>
  >({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const { downloadFile, loading: downloadLoading, progress } = useDownloader();

  const apiClient = useAPI();

  useEffect(() => {
    const timer = setTimeout(() => {
      setAnimate(true);
    }, 100);

    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    fetchCertificateData();
  }, []);

  const fetchCertificateData = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<IUserCertificate>> = await apiClient.get(
        API_END_POINTS.USER_CERTIFICATE_LIST +
          objectToQueryString({
            ...InitGetListParams,
            userId: userInfo.userId,
            topicId: slug,
          }),
      );
      setCertificateData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching certificate data:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Border>
      <div className="content-flex content-min-h-[calc(100vh-100px)] content-flex-col content-items-center content-justify-center content-bg-transparent content-p-4">
        <div
          className={`content-transform content-transition-all content-duration-1000 content-ease-out ${
            animate
              ? 'content-translate-y-0 content-opacity-100'
              : 'content-translate-y-20 content-opacity-0'
          }`}
        >
          <svg
            className="content-mx-auto content-mb-6 content-size-20"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M12 15.4L8.24 17.67L9.24 13.39L5.92 10.51L10.3 10.13L12 6.1L13.71 10.14L18.09 10.52L14.77 13.4L15.77 17.68L12 15.4Z"
              fill="currentColor"
              className="content-text-primary"
            />
            <path
              d="M12 2L14.39 6.26L19.28 6.97L15.64 10.27L16.47 15.08L12 12.78L7.53 15.08L8.36 10.27L4.72 6.97L9.61 6.26L12 2Z"
              stroke="currentColor"
              className="content-text-primary"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>

          <h1
            className={`content-mb-4 content-text-center content-text-4xl content-font-bold content-text-white content-transition-all content-duration-1000 content-ease-out md:content-text-5xl ${
              animate
                ? 'content-translate-y-0 content-opacity-100'
                : 'content-translate-y-10 content-opacity-0'
            }`}
            style={{ transitionDelay: '200ms' }}
          >
            Congratulations!
          </h1>

          <p
            className={`content-mb-8 content-text-center content-text-xl content-text-white content-transition-all content-duration-1000 content-ease-out ${
              animate
                ? 'content-translate-y-0 content-opacity-100'
                : 'content-translate-y-10 content-opacity-0'
            }`}
            style={{ transitionDelay: '400ms' }}
          >
            You have successfully completed the course!
          </p>
        </div>

        <div
          className={`content-flex content-flex-col content-gap-4 content-transition-all content-duration-1000 content-ease-out sm:content-flex-row ${
            animate
              ? 'content-translate-y-0 content-opacity-100'
              : 'content-translate-y-10 content-opacity-0'
          }`}
          style={{ transitionDelay: '600ms' }}
        >
          <Button
            variant="outline"
            className="content-grow content-p-2"
            disabled={loading}
            onClick={() =>
              downloadFile(
                certificateData?.items?.[0]?.certificateLink,
                'Certificate',
              )
            }
          >
            <DownloadIcon fill="#37BE99" />
            {downloadLoading ? `${progress}%` : 'Download Certificate'}
          </Button>

          <button
            onClick={() => navigate(routes.dashboard.path)}
            className="content-flex content-items-center content-justify-center content-gap-2 content-rounded-lg content-border content-border-white content-bg-transparent content-px-6 content-py-2 content-text-white content-transition-colors content-duration-300 hover:content-bg-primary/10"
          >
            <svg
              className="content-size-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
              />
            </svg>
            Go to Home
          </button>
        </div>
      </div>
    </Border>
  );
};

export default CourseComplete;
