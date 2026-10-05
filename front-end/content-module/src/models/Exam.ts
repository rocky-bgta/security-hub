export enum ExamTypes {
  SINGLE_CHOICE = 'SINGLE_CHOICE',
  MULTIPLE_CHOICE = 'MULTIPLE_CHOICE',
  TRUE_FALSE = 'TRUE_FALSE',
  YES_NO = 'YES_NO',
}

export interface IQuestionOption {
  optionText: string;
  isCorrect: boolean;
}

export interface IQuestion {
  id: string;
  topicId: string;
  questionType: ExamTypes;
  questionText: string;
  status: 'ACTIVE' | 'INACTIVE';
  options: IQuestionOption[];
}

export interface IExamTopic {
  topicId: string;
  topicName: string;
  questionCount: number;
  totalAvailableQuestions: number;
}

export interface IExam {
  examId: string;
  examTitle: string;
  examDescription: string;
  subPackageId: string;
  subPackageName: string;
  totalQuestions: number | null;
  passingScore: number;
  topics: IExamTopic[];
  createdAt: string;
  status: 'SUCCESS' | 'FAILED' | 'PENDING' | string;
}
