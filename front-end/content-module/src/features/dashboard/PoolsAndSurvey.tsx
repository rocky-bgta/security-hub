import clsx from 'clsx';
import { Button } from 'common/Button';
import CustomCheckbox from 'common/CustomCheckbox';
import { Label } from 'common/Label';
import NoDataText from 'common/NoDataText';
import Border from 'components/UserBorder';
import PoolsAndSurveySkeleton from 'components/skeleton/dashboard/PoolsAndSurvey';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import {
  ISurveyPoll,
  ISurveySummary,
  ISurveySummaryAnswer,
} from 'models/SurveyPoll';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse, sliceWords } from 'utils/Helper';

const PoolsAndSurvey = () => {
  const [surveyData, setSurveyData] = useState<ISurveyPoll>();
  const [surveySummary, setSurveySummary] = useState<ISurveySummary>();
  const [loading, setLoading] = useState<boolean>(false);
  const [selectedAnswerIds, setSelectedAnswerIds] = useState<string[]>([]);
  const [hasVoted, setHasVoted] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchSurveyData();
  }, []);

  const fetchSurveySummary = async (id: string) => {
    try {
      const response: IResponse<ISurveySummary> = await apiClient.get(
        API_END_POINTS.USER_POLL_SURVEY_SUMMARY.replace(':id', id ?? ''),
      );
      if (isSuccessResponse(response.statusCode)) {
        setSurveySummary(response.data);
      }
    } catch (error) {
      console.error('Error fetching survey summary:', error);
    }
  };

  const fetchSurveyData = async () => {
    setLoading(true);

    try {
      const response: IResponse<ISurveyPoll> = await apiClient.get(
        API_END_POINTS.USER_SURVEY,
      );
      if (isSuccessResponse(response.statusCode)) {
        setSurveyData(response.data);

        // Check if user has already voted
        if (
          response.data.userSelectedAnswerIds &&
          response.data.userSelectedAnswerIds.length > 0
        ) {
          setHasVoted(true);
          if (response.data.id) {
            fetchSurveySummary(response.data.id);
          }
          setSelectedAnswerIds(response.data.userSelectedAnswerIds);
        }
      }
    } catch (error) {
      console.error('Error fetching survey details:', error);
    } finally {
      setLoading(false);
    }
  };

  const isMCQ = surveyData?.questions[0]?.questionType === 'MCQ' ? true : false;

  const handleVote = async () => {
    if (selectedAnswerIds.length === 0 || surveyData?.questions?.length === 0) {
      return;
    }

    const payload = {
      votes: [
        {
          questionId: surveyData?.questions[0]?.id,
          answerIds: selectedAnswerIds,
          textAnswer: 'votes',
        },
      ],
    };

    try {
      await apiClient.post(
        API_END_POINTS.USER_POLL_SURVEY_REACT.replace(
          ':id',
          surveyData?.id ?? '',
        ),
        {
          data: payload,
        },
      );
      setHasVoted(true);
      if (surveyData?.id) {
        await fetchSurveySummary(surveyData.id);
      }
      toast.success(
        ' Thank you for sharing your feedback. Your input is essential in helping us evaluate the effectiveness of our program and make informed improvements.',
      );
    } catch (error) {
      console.error('Error submitting survey:', error);
    }
  };

  const handleOptionChange = (answerId: string) => {
    if (hasVoted) return;

    if (isMCQ) {
      setSelectedAnswerIds(prev =>
        prev.includes(answerId)
          ? prev.filter(id => id !== answerId)
          : [...prev, answerId],
      );
    } else {
      setSelectedAnswerIds([answerId]);
    }
  };

  const isAnswerSelected = (answerId: string) => {
    return selectedAnswerIds.includes(answerId);
  };

  return (
    <Border>
      <div className="content-p-4 sm:content-p-6">
        <h2 className="content-border-b content-border-card-border content-pb-2 content-text-lg content-font-semibold content-text-white sm:content-text-xl">
          Polls & Surveys
        </h2>
        {loading ? (
          <PoolsAndSurveySkeleton count={1} />
        ) : surveyData ? (
          <div className="content-mt-3 content-pr-1 sm:content-mt-4 lg:content-max-h-[300px] lg:content-overflow-y-auto">
            <div className="content-mb-2">
              {surveyData.title && (
                <p className="content-text-sm content-font-medium content-text-white sm:content-text-base">
                  {sliceWords(surveyData.title, 20)}
                </p>
              )}
              {surveyData.description && (
                <p className="content-mt-1 content-text-xs content-text-white content-text-opacity-60">
                  {sliceWords(surveyData.description, 50)}
                </p>
              )}
            </div>
            {surveyData.questions[0]?.questionText && (
              <p className="content-text-sm content-text-white content-text-opacity-75">
                {surveyData.questions[0]?.questionText}
              </p>
            )}
            {hasVoted && surveySummary ? (
              <div className="content-mt-4 content-space-y-3">
                {surveySummary.questions[0]?.answers.map(answer => {
                  const isUserSelected = selectedAnswerIds.includes(
                    answer.answerId,
                  );
                  return (
                    <div
                      key={answer.answerId}
                      className={clsx(
                        'content-rounded-lg content-p-3 content-transition-all',
                        isUserSelected
                          ? 'content-border content-border-primary content-bg-primary content-bg-opacity-20'
                          : 'content-bg-white content-bg-opacity-5',
                      )}
                    >
                      <div className="content-mb-2 content-flex content-flex-wrap content-items-center content-justify-between content-gap-y-1">
                        <div className="content-flex content-items-center content-gap-2">
                          <span className="content-text-sm content-font-medium content-text-white">
                            {answer.answerText}
                          </span>
                          {isUserSelected && (
                            <span className="content-rounded content-bg-primary content-bg-opacity-20 content-px-2 content-py-0.5 content-text-xs content-font-semibold content-text-primary">
                              Your Vote
                            </span>
                          )}
                        </div>
                        <div className="content-flex content-items-center content-gap-2">
                          <span className="content-text-sm content-font-semibold content-text-white">
                            {answer.percentage.toFixed(1)}%
                          </span>
                          <span className="content-text-xs content-text-white content-text-opacity-60">
                            ({answer.voteCount}{' '}
                            {answer.voteCount === 1 ? 'vote' : 'votes'})
                          </span>
                        </div>
                      </div>
                      <div className="content-h-2 content-w-full content-overflow-hidden content-rounded-full content-bg-white content-bg-opacity-10">
                        <div
                          className={clsx(
                            'content-h-full content-transition-all content-duration-500 content-ease-out',
                            isUserSelected
                              ? 'content-bg-primary'
                              : 'content-bg-[#EFF4FB40]',
                          )}
                          style={{ width: `${answer.percentage}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
                <div className="content-mt-4 content-border-t content-border-card-border content-pt-3">
                  <p className="content-text-center content-text-xs content-text-white content-text-opacity-60">
                    Total Votes:{' '}
                    <span className="content-font-semibold">
                      {surveySummary.questions[0]?.totalVotes || 0}
                    </span>
                  </p>
                </div>
              </div>
            ) : (
              <>
                <div className="content-mt-2 content-flex content-flex-col content-gap-2">
                  {surveyData.questions[0]?.answers.map(
                    (answer: ISurveySummaryAnswer) => (
                      <div key={answer.id} className="content-w-fit">
                        <Label
                          htmlFor={`option-${answer.id}`}
                          className="content-relative content-ml-2 content-flex content-items-center content-gap-2 content-text-sm !content-font-normal content-text-white content-text-opacity-75"
                        >
                          <CustomCheckbox
                            id={`option-${answer.id}`}
                            checked={isAnswerSelected(answer.id)}
                            onChange={() => handleOptionChange(answer.id)}
                            variant={isMCQ ? 'default' : 'exam'}
                          />

                          {answer.answerText}
                        </Label>
                      </div>
                    ),
                  )}
                </div>
                <div className="content-mt-2 content-flex content-w-full content-gap-2 sm:content-w-auto">
                  <Button
                    className={clsx(
                      `content-w-full content-rounded-full sm:content-w-24 ${hasVoted && 'content-bg-[#EFF4FB40]'}`,
                    )}
                    size="sm"
                    onClick={handleVote}
                    disabled={hasVoted || selectedAnswerIds.length === 0}
                  >
                    {hasVoted ? 'Voted' : 'Submit Vote'}
                  </Button>
                </div>
              </>
            )}
          </div>
        ) : (
          <NoDataText className="content-mt-4" text="No Survey Found" />
        )}
      </div>
    </Border>
  );
};

export default PoolsAndSurvey;
