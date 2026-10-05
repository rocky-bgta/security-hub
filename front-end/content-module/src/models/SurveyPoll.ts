import { Status } from './Global';

export interface ISurveySummaryAnswer {
  id: string;
  answerText: string;
  isSelected?: boolean;
}

export interface IQuestion {
  id: string;
  questionText: string;
  questionType: string;
  answers: ISurveySummaryAnswer[];
}

export interface ISurveyPoll {
  id: string;
  title: string;
  description: string;
  type: 'POLL' | 'SURVEY';
  status: Status;
  questions: IQuestion[];
  userSelectedAnswerIds: Array<string>;
}

export interface ISurveySummaryAnswer {
  answerId: string;
  answerText: string;
  voteCount: number;
  percentage: number;
}

export interface ISurveySummaryQuestion {
  questionId: string;
  questionText: string;
  totalVotes: number;
  answers: ISurveySummaryAnswer[];
}

export interface ISurveySummary {
  pollSurveyId: string;
  title: string;
  questions: ISurveySummaryQuestion[];
}
