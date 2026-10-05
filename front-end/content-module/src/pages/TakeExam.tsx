import { Fragment, useCallback, useEffect, useState } from 'react';
import { GoArrowLeft } from 'react-icons/go';
import { useNavigate, useParams } from 'react-router-dom';

import { CheckIcon, ExamIcon } from 'assets/icons';
import { Button } from 'common/Button';
import Border from 'components/UserBorder';
import ExamLoader from 'components/skeleton/ExamLoader';
import ExamPage from 'features/exam/ExamPage';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { IExam } from 'models/Exam';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { isSuccessResponse } from 'utils/Helper';
import { toast } from 'react-toastify';

const TakeExam = () => {
  const { slug } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState<boolean>(true);
  const [examData, setExamData] = useState<IExam>();
  const [redirect, setRedirect] = useState<boolean>(true);

  const { userInfo } = useStore();
  const apiClient = useAPI();

  const fetchExamDetails = useCallback(async () => {
    try {
      const payload = {
        subPackageId: slug,
        userId: userInfo.userId,
        clientId: userInfo?.clientAdminId,
      };

      const response: IResponse<IExam> = await apiClient.post(
        API_END_POINTS.USER_CREATE_EXAM,
        { data: payload },
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to fetch exam details');
      }
      setExamData(response.data);
    } catch (error) {
      console.error('Error fetching exam details:', error);
      toast.error((error as Error).message);
      navigate(routes.dashboard.path);
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [apiClient, slug, userInfo]);

  useEffect(() => {
    if (userInfo) {
      fetchExamDetails();
    }
  }, [userInfo, fetchExamDetails]);

  return (
    <div className="content-relative content-flex content-h-[96vh] content-items-center">
      {redirect ? (
        <Fragment>
          <Button
            className="content-absolute content-left-4 content-top-4 content-rounded-full content-px-4 sm:content-left-6 sm:content-top-6 sm:content-px-8"
            onClick={() => navigate(routes.dashboard.path)}
          >
            <GoArrowLeft /> Return to Dashboard
          </Button>

          <div className="content-mx-auto content-w-[92%] sm:content-w-3/5">
            {loading ? (
              <ExamLoader />
            ) : (
              <Fragment>
                <p className="content-text-center content-text-2xl content-font-semibold content-text-white sm:content-text-[32px]">
                  {examData?.examTitle}
                </p>
                <p className="content-mt-2 content-text-center content-text-sm content-text-cloudy-white/75 sm:content-text-base">
                  {examData?.examDescription}
                </p>
                <Border className="content-mt-4 content-p-4 sm:content-mt-6 sm:content-p-6">
                  <div className="content-mb-4 content-flex content-flex-col content-gap-3 content-border-b content-border-card-border content-pb-4 sm:content-gap-4 sm:content-flex-row sm:content-justify-between">
                    <div>
                      <p className="content-flex content-items-center content-gap-2 content-text-xl content-font-semibold content-text-white sm:content-text-2xl">
                        <ExamIcon width={24} hanging={24} />{' '}
                        <span>Assessment</span>
                      </p>
                      <p className="content-text-sm content-text-white/75 sm:content-text-base">
                        Validate your understanding of the course material.
                      </p>
                    </div>
                    <div className="content-text-sm content-text-white/75 sm:content-text-base">
                      <div className="content-flex content-items-center content-gap-4 sm:content-gap-5">
                        <div className="content-font-medium">
                          <p>Questions:</p>
                          <p>Passing Score:</p>
                        </div>
                        <div>
                          <p>{examData?.totalQuestions || 0} Questions</p>
                          <p>{examData?.passingScore}%</p>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="content-mb-4 content-text-white">
                    <p className="content-mb-3 content-text-lg content-font-semibold sm:content-mb-4 sm:content-text-xl">
                      Completed Topics
                    </p>
                    <ul className="content-grid content-grid-cols-1 content-gap-x-6 content-gap-y-3 sm:content-gap-x-10 sm:content-gap-y-4 sm:content-grid-cols-2">
                      {examData?.topics?.map((topic, index) => (
                        <li
                          key={index}
                          className="content-flex content-items-center content-gap-2 content-rounded content-bg-white content-bg-opacity-25 content-p-2 content-text-sm sm:content-text-base"
                        >
                          <CheckIcon /> {topic.topicName}
                        </li>
                      ))}
                    </ul>
                  </div>
                </Border>
                <Button
                  className="content-mx-auto content-mt-6 content-rounded-full content-px-6 sm:content-mt-8 sm:content-px-8"
                  onClick={() => setRedirect(false)}
                >
                  Start Assessment
                </Button>
              </Fragment>
            )}
          </div>
        </Fragment>
      ) : (
        <ExamPage examId={examData?.examId || ''} />
      )}
    </div>
  );
};

export default TakeExam;
