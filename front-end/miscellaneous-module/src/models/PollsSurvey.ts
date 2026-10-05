import { Status } from './Global';

export interface IQuestion {
  id: string;
  questionText: string;
  questionType: string;
  answers: IAnswer[];
}

export interface IAnswer {
  id: string;
  answerText: string;
}

export interface IPollSurvey {
  id: string;
  title: string;
  description: string;
  type: 'POLL' | 'SURVEY';
  status: Status;
  questions: IQuestion[];
}

export interface ISummaryAnswer {
  answerId: string;
  answerText: string;
  voteCount: number;
}

export interface ISummaryQuestion {
  questionId: string;
  questionText: string;
  answers: ISummaryAnswer[];
}

export interface ISummary {
  pollSurveyId: string;
  title: string;
  questions: ISummaryQuestion[];
}

export interface IPollSurveyDetails {
  id: string;
  title: string;
  description: string;
  type: string;
  status: string;
  questions: IQuestion[];
  summary: ISummary;
}
