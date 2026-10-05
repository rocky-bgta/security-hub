import { IContent, TContent } from 'models/Content';
import { Status } from 'models/Global';

export interface IChapter {
  id: string;
  chapterName: string;
  chapterStatus: Status;
  chapterDescription: string;
  courseId: string;
  position: number;
  contentIds: Array<IContent<TContent>>;
  createdAt: string;
  updatedAt: string;
}
