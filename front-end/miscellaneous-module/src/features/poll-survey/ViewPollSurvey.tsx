import { Badge } from 'common/Badge';
import {
  DialogContent,
  DialogDescription,
  DialogTitle,
  DialogHeader,
} from 'common/Dialog';
import { Dialog } from 'common/Dialog';
import { Label } from 'common/Label';
import { useAPI } from 'hooks/UseAPI';
import { Status } from 'models/Global';
import { useEffect, useState } from 'react';
import { IPollSurvey, IPollSurveyDetails } from 'models/PollsSurvey';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { Loader2 } from 'lucide-react';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedItem: IPollSurvey;
}

const ViewPollSurvey = ({ isOpen, onClose, selectedItem }: IProps) => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState(true);
  const [pollSurvey, setPollSurvey] = useState<IPollSurveyDetails | null>(null);

  useEffect(() => {
    if (isOpen && selectedItem) {
      fetchPollSurvey();
    }
  }, [selectedItem]);

  const fetchPollSurvey = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_POLL_SURVEY_DETAILS.replace(
          ':id',
          selectedItem?.id ?? '',
        ),
      );
      if (isSuccessResponse(response.statusCode)) {
        setPollSurvey(response.data);
      }
    } catch (error) {
      console.error('Error fetching poll survey:', error);
    } finally {
      setLoading(false);
    }
  };

  // Calculate total responses from summary
  const getTotalResponses = () => {
    if (!pollSurvey?.summary?.questions) return 0;
    return pollSurvey.summary.questions.reduce((total, question) => {
      const questionTotal = question.answers.reduce(
        (sum, answer) => sum + (answer.voteCount || 0),
        0,
      );
      return total + questionTotal;
    }, 0);
  };

  // Format date helper
  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Poll/Survey Details</DialogTitle>
          <DialogDescription>
            View complete details of the poll/survey
          </DialogDescription>
        </DialogHeader>
        {loading ? (
          <div className="flex h-full items-center justify-center py-8">
            <Loader2 className="size-8 animate-spin" />
          </div>
        ) : (
          pollSurvey && (
            <div className="space-y-4">
              <div>
                <Label className="text-sm font-semibold">Title</Label>
                <p className="mt-1 text-sm">{pollSurvey.title}</p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm font-semibold">Type</Label>
                  <p className="mt-1">
                    <Badge
                      variant={
                        pollSurvey.type === 'POLL' ? 'default' : 'secondary'
                      }
                    >
                      {pollSurvey.type}
                    </Badge>
                  </p>
                </div>
                <div>
                  <Label className="text-sm font-semibold">Status</Label>
                  <p className="mt-1">
                    <Badge
                      variant={
                        pollSurvey.status === Status.ACTIVE
                          ? 'default'
                          : 'secondary'
                      }
                    >
                      {pollSurvey.status}
                    </Badge>
                  </p>
                </div>
              </div>

              <div>
                <Label className="text-sm font-semibold">Total Responses</Label>
                <p className="mt-1">
                  <Badge variant="outline">{getTotalResponses()}</Badge>
                </p>
              </div>

              {pollSurvey.description && (
                <div>
                  <Label className="text-sm font-semibold">Description</Label>
                  <p className="mt-1 text-sm">{pollSurvey.description}</p>
                </div>
              )}

              <div>
                <Label className="text-sm font-semibold">
                  Questions ({pollSurvey.questions.length})
                </Label>
                <div className="mt-2 space-y-4">
                  {pollSurvey.questions.map((question, index) => (
                    <div key={question.id} className="rounded-lg border p-3">
                      <p className="text-sm font-medium">
                        {index + 1}. {question.questionText}
                      </p>
                      {question.answers && question.answers.length > 0 && (
                        <ul className="mt-2 space-y-1 pl-4">
                          {question.answers.map(answer => {
                            const voteCount =
                              pollSurvey.summary?.questions
                                ?.find(q => q.questionId === question.id)
                                ?.answers?.find(a => a.answerId === answer.id)
                                ?.voteCount || 0;

                            return (
                              <li
                                key={answer.id}
                                className="flex justify-between text-sm text-white"
                              >
                                <span>• {answer.answerText}</span>
                                <span className="text-xs text-white">
                                  ({voteCount} votes)
                                </span>
                              </li>
                            );
                          })}
                        </ul>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ViewPollSurvey;
