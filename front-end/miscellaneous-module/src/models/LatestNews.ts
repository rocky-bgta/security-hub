import { ILatestNewsCategory } from './Category';
import { Status } from './Global';

export interface ILatestNews {
  id: string;
  name: string;
  slug: string;
  categoryId: string;
  content: string;
  sequence: number;
  imageUrl: string;
  videoUrl: string;
  publishedDate: string;
  expireDate: string;
  status: Status;
  likeCount: number;
  dislikeCount: number;
  category: ILatestNewsCategory;
}
