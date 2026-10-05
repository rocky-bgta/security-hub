import { CertificateViewIcon, PrevIcon, RetakeIcon } from 'assets/icons';
import clsx from 'clsx';
import Border from 'components/UserBorder';
import ExamCompleteLoader from 'components/skeleton/ExamCompleteLoader';
import dayjs from 'dayjs';
import { AlertCircleIcon, CheckCircleIcon } from 'lucide-react';
import { Fragment, SVGProps, useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { routes } from 'routes/Routes';

interface IProps {
  hostPath: typeof routes;
}

interface IData {
  packageId: string;
  packageName: string;
  examCompletedAt: string;
  totalQuestions: number;
  correctAnswers: number;
  incorrectAnswers: number;
  percentageScore: number;
  status: 'PASSED' | 'FAILED';
  certificateLink: string | null;
  passingScore: number;
}

const FlagSvg = (props: SVGProps<SVGSVGElement>) => {
  const { width = 28, height = 28, fill = 'currentColor' } = props;
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={width}
      height={height}
      viewBox="0 0 24 24"
      fill="none"
    >
      <line
        x1="4"
        y1="2"
        x2="4"
        y2="22"
        stroke={fill}
        strokeWidth="1.5"
        strokeLinecap="round"
      />

      <defs>
        <clipPath id="flagClip">
          <path d="M4 3 Q8 2.5 12 3.5 Q16 4.5 20 3.5 L20 13 Q16 14 12 13 Q8 12 4 13 Z" />
        </clipPath>
      </defs>

      <path
        d="M4 3 Q8 2.5 12 3.5 Q16 4.5 20 3.5 L20 13 Q16 14 12 13 Q8 12 4 13 Z"
        fill="white"
        stroke={fill}
        strokeWidth="0.5"
      />

      <g clipPath="url(#flagClip)" fill="black">
        <rect x="4" y="3" width="2" height="2.5" />
        <rect x="8" y="3" width="2" height="2.5" />
        <rect x="12" y="3" width="2" height="2.5" />
        <rect x="16" y="3" width="2" height="2.5" />

        <rect x="6" y="5.5" width="2" height="2.5" />
        <rect x="10" y="5.5" width="2" height="2.5" />
        <rect x="14" y="5.5" width="2" height="2.5" />
        <rect x="18" y="5.5" width="2" height="2.5" />

        <rect x="4" y="8" width="2" height="2.5" />
        <rect x="8" y="8" width="2" height="2.5" />
        <rect x="12" y="8" width="2" height="2.5" />
        <rect x="16" y="8" width="2" height="2.5" />

        <rect x="6" y="10.5" width="2" height="2.5" />
        <rect x="10" y="10.5" width="2" height="2.5" />
        <rect x="14" y="10.5" width="2" height="2.5" />
        <rect x="18" y="10.5" width="2" height="2.5" />
      </g>

      <path
        d="M4 3 Q8 2.5 12 3.5 Q16 4.5 20 3.5 L20 13 Q16 14 12 13 Q8 12 4 13"
        fill="none"
        stroke="currentColor"
        strokeWidth="1"
        strokeLinejoin="round"
      />
    </svg>
  );
};

const ExamResults = ({ hostPath = routes }: IProps) => {
  const { slug } = useParams();
  const location = useLocation();
  const { data }: { data: IData } = location.state || {};

  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    setTimeout(() => {
      setLoading(false);
    }, 1000);
  }, []);

  return (
    <div className="content-relative content-flex content-min-h-[calc(100vh-100px)] content-items-start content-justify-center content-overflow-y-auto content-px-4 content-py-6 sm:content-px-6 sm:content-py-8 md:content-items-center lg:content-px-8">
      <div className="content-relative content-mx-auto content-w-full content-text-white lg:content-w-3/5">
        {loading ? (
          <ExamCompleteLoader />
        ) : (
          <Fragment>
            <div className="content-flex content-flex-col content-items-center content-justify-center content-text-center">
              <h1
                className={clsx(
                  data?.status === 'PASSED'
                    ? 'content-text-[#28C76F]'
                    : 'content-text-[#F65E5B]',
                  'content-mb-2 content-inline-flex content-max-w-full content-flex-wrap content-items-center content-justify-center content-gap-2 content-text-2xl content-font-semibold sm:content-text-3xl',
                )}
              >
                {data?.status === 'PASSED' ? (
                  <CheckCircleIcon
                    className="content-size-7 content-shrink-0 sm:content-size-8"
                    width={28}
                    height={28}
                  />
                ) : (
                  <AlertCircleIcon
                    className="content-size-7 content-shrink-0 sm:content-size-8"
                    width={28}
                    height={28}
                  />
                )}
                {data?.status === 'PASSED'
                  ? 'Assessment Complete'
                  : 'Assessment Failed'}
              </h1>
              {/* Finished indicator */}
              <div className="content-mb-2 content-inline-flex content-max-w-full content-flex-wrap content-items-center content-justify-center content-gap-2 content-rounded-full content-border content-border-white content-border-opacity-30 content-bg-white content-bg-opacity-20 content-px-3 content-py-1.5 sm:content-px-4 sm:content-py-2">
                {data?.status === 'PASSED' ? (
                  <span className="content-inline-flex content-size-2 content-animate-pulse content-rounded-full content-bg-[#28C76F]" />
                ) : (
                  <span className="content-inline-flex content-size-2 content-animate-pulse content-rounded-full content-bg-[#F65E5B]" />
                )}
                <span className="content-text-sm content-font-semibold content-text-white">
                  Completed
                </span>
                <span className="content-text-xs content-text-white content-text-opacity-75">
                  {data?.examCompletedAt
                    ? dayjs(data?.examCompletedAt).format('M/D/YYYY, h:mm A')
                    : 'N/A'}
                </span>
              </div>
              <p className="content-mb-4 content-max-w-md content-px-2 content-text-sm content-text-white content-text-opacity-60 sm:content-mb-6">
                Your assessment has been submitted and scored successfully.
              </p>
            </div>
            <Border>
              <div className="content-p-4 sm:content-p-6">
                {/* Marks obtained – primary visual */}
                <div className="content-mb-4 content-text-center sm:content-mb-6">
                  <p className="content-mb-1 content-text-xs content-font-medium content-uppercase content-tracking-wide content-text-white content-text-opacity-80 sm:content-text-sm">
                    Assessment Score
                  </p>
                  <div className="content-flex content-items-baseline content-justify-center content-gap-2">
                    <span className="content-text-4xl content-font-bold content-text-[#55B89D] sm:content-text-5xl">
                      {data?.correctAnswers ?? 0}
                    </span>
                    <span className="content-text-xl content-font-medium content-text-white content-text-opacity-60 sm:content-text-2xl">
                      /
                    </span>
                    <span className="content-text-2xl content-font-semibold content-text-white content-text-opacity-90 sm:content-text-3xl">
                      {data?.totalQuestions ?? 0}
                    </span>
                  </div>
                  <p className="content-mt-2 content-text-xs content-text-white content-text-opacity-75 sm:content-text-sm">
                    {data?.correctAnswers ?? 0} correct ·{' '}
                    {data?.totalQuestions ?? 0} total questions
                    <span className="content-ml-1.5 content-text-white content-text-opacity-60">
                      ({data?.percentageScore ?? 0}%)
                    </span>
                  </p>
                </div>

                {/* Visual score bar: correct vs total, with Finished flag at score */}
                <div className="content-relative content-mb-2 content-mt-12 sm:content-mt-10">
                  <div className="content-mb-1.5 content-flex content-justify-between">
                    <span className="content-text-sm content-font-medium content-text-white content-text-opacity-80">
                      Your Assessment Score
                    </span>
                  </div>
                  <div className="content-relative content-overflow-visible">
                    <div className="content-flex content-h-3 content-w-full content-overflow-hidden content-rounded-full content-bg-white content-bg-opacity-25">
                      <div
                        className="content-h-full content-rounded-l-full content-bg-[#55B89D] content-transition-all content-duration-500"
                        style={{
                          width: `${((data?.correctAnswers ?? 0) / Math.max(data?.totalQuestions ?? 1, 1)) * 100}%`,
                        }}
                      />
                      <div
                        className="content-h-full content-rounded-r-full content-bg-white content-bg-opacity-20"
                        style={{
                          width: `${(((data?.totalQuestions ?? 0) - (data?.correctAnswers ?? 0)) / Math.max(data?.totalQuestions ?? 1, 1)) * 100}%`,
                        }}
                      />
                    </div>
                    {/* Finished flag indicator at achieved score */}
                    <div
                      className="content-absolute content-top-0 content-flex content-flex-col content-items-center content-transition-all content-duration-300"
                      style={{
                        left: `clamp(1.25rem, ${data?.passingScore ?? 70}%, calc(100% - 1.25rem))`,
                        transform: 'translate(-50%, -100%)',
                        marginTop: '-0.5rem',
                      }}
                    >
                      <span className="content-whitespace-nowrap content-text-xs content-font-semibold content-text-primary content-drop-shadow-md sm:content-text-sm">
                        {data?.passingScore ?? 70}%
                      </span>
                      {/* <Flag width={24} height={24} className='content-text-primary' /> */}
                      <FlagSvg />
                    </div>
                  </div>
                  <div className="content-mt-1 content-flex content-justify-between content-text-xs content-text-white content-text-opacity-60">
                    <span>0</span>
                    <span>{data?.totalQuestions ?? 0}</span>
                  </div>
                </div>

                {/* Status row */}
                <div className="content-mt-4 content-flex content-flex-wrap content-items-center content-justify-between content-gap-3 content-border-t content-border-white content-border-opacity-20 content-pt-4 sm:content-mt-6">
                  <h3 className="content-text-base content-font-semibold content-text-white sm:content-text-lg">
                    Result
                  </h3>
                  <div
                    className={clsx(
                      data?.status === 'PASSED'
                        ? 'content-bg-[#28C76F]'
                        : 'content-bg-[#F65E5B]',
                      'content-flex content-items-center content-gap-2 content-rounded-full content-px-4 content-py-1.5',
                    )}
                  >
                    {data?.status === 'PASSED' ? (
                      <CheckCircleIcon width={16} height={16} />
                    ) : (
                      <AlertCircleIcon width={16} height={16} />
                    )}
                    <span className="content-text-sm content-font-semibold content-text-white">
                      {data?.status === 'PASSED' ? 'Passed' : 'Failed'}
                    </span>
                  </div>
                </div>
              </div>
            </Border>

            <div className="content-mt-4 sm:content-mt-6">
              <Border>
                <div className="content-p-4 sm:content-p-6">
                  <h2 className="content-mb-2 content-text-center content-text-lg content-font-semibold content-text-white sm:content-text-xl">
                    Performance Summary
                  </h2>
                  <p className="content-text-center content-text-sm content-text-white content-text-opacity-75">
                    Detailed analysis of your assessment performance
                  </p>
                  <div className="content-mt-4 content-grid content-grid-cols-1 content-gap-3 sm:content-mt-6 sm:content-grid-cols-3 sm:content-gap-4">
                    <div className="content-flex content-flex-col content-items-center content-rounded-lg content-border content-border-white content-border-opacity-35 content-p-4 sm:content-p-6">
                      <span className="content-mb-2 content-text-xl content-font-semibold content-text-[#55B89D]">
                        {data?.correctAnswers || 0}
                      </span>
                      <span className="content-text-center content-text-sm content-text-white content-text-opacity-75 sm:content-text-base">
                        Correct Answers
                      </span>
                    </div>

                    <div className="content-flex content-flex-col content-items-center content-rounded-lg content-border content-border-white content-border-opacity-35 content-p-4 sm:content-p-6">
                      <span className="content-mb-2 content-text-xl content-font-semibold content-text-red-400">
                        {data?.incorrectAnswers || 0}
                      </span>
                      <span className="content-text-center content-text-sm content-text-white content-text-opacity-75 sm:content-text-base">
                        Incorrect Answers
                      </span>
                    </div>

                    <div className="content-flex content-flex-col content-items-center content-rounded-lg content-border content-border-white content-border-opacity-35 content-p-4 sm:content-p-6">
                      <span className="content-mb-2 content-text-xl content-font-semibold content-text-white">
                        {data?.percentageScore || 0}%
                      </span>
                      <span className="content-text-center content-text-sm content-text-white content-text-opacity-75 sm:content-text-base">
                        Overall Assessment Score
                      </span>
                    </div>
                  </div>
                </div>
              </Border>
            </div>

            <div className="content-mt-5 content-flex content-flex-col content-items-stretch content-justify-center content-gap-3 sm:content-mt-7 sm:content-flex-row sm:content-flex-wrap sm:content-items-center sm:content-gap-4">
              <Link
                to={hostPath.dashboard.path}
                className="content-flex content-items-center content-justify-center content-gap-2 content-rounded-full content-bg-white content-bg-opacity-25 content-px-6 content-py-3 content-text-center content-text-sm content-text-white content-transition-all content-duration-300 hover:content-bg-opacity-50 sm:content-px-8 sm:content-text-base"
              >
                <PrevIcon /> Return to Dashboard
              </Link>
              {data?.status === 'PASSED' ? (
                <Link
                  to={`${hostPath.certificates.path}?courseId=${data?.packageId}`}
                  className="content-flex content-items-center content-justify-center content-gap-2 content-rounded-full content-bg-primary content-px-6 content-py-3 content-text-center content-text-sm content-text-white content-transition-all content-duration-300 hover:content-bg-opacity-75 sm:content-px-8 sm:content-text-base"
                >
                  <CertificateViewIcon /> View Certificate
                </Link>
              ) : (
                <Link
                  to={routes.exam.path.replace(':slug', slug ?? '')}
                  className="content-flex content-items-center content-justify-center content-gap-2 content-rounded-full content-bg-primary content-px-6 content-py-3 content-text-center content-text-sm content-text-white content-transition-all content-duration-300 hover:content-bg-opacity-75 sm:content-px-8 sm:content-text-base"
                >
                  <RetakeIcon /> Retake Assessment
                </Link>
              )}
            </div>
          </Fragment>
        )}
      </div>
    </div>
  );
};

export default ExamResults;
