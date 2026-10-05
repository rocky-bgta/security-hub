import { Status, VideoType } from 'models/Global';

export interface ILeaderBoard {
  id: string;
  title: string;
  name: string;
  designation: string;
  videoType: VideoType;
  videoUrl: string;
  thumbnailUrl: string;
  status: Status;
  createdDate: string;
  createdBy: string;
  clientId: string;
  clientName: string | null;
  isDefault: boolean;
}
