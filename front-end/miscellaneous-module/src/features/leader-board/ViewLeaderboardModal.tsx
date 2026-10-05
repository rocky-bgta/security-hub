import { Play } from 'lucide-react';

import { Badge } from 'components/common/Badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import { Label } from 'components/common/Label';
import { Status, VideoType } from 'models/Global';
import { ILeaderBoard } from 'models/LeaderBoard';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  entry: ILeaderBoard | null;
}

const ViewLeaderboardModal = ({ isOpen, onClose, entry }: IProps) => {
  return (
    <Dialog
      open={isOpen}
      onOpenChange={open => {
        if (!open) onClose();
      }}
    >
      <DialogContent className="max-h-[90vh] max-w-[60%] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>View Leader Message Entry</DialogTitle>
          <DialogDescription>
            Leader message details and video content
          </DialogDescription>
        </DialogHeader>
        {entry && (
          <div className="space-y-6">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-sm font-medium text-muted-foreground">
                  Title
                </Label>
                <p className="text-white">{entry.title}</p>
              </div>
              <div>
                <Label className="text-sm font-medium text-muted-foreground">
                  Leader Name
                </Label>
                <p className="text-lg text-white">{entry.name}</p>
              </div>
              <div>
                <Label className="text-sm font-medium text-muted-foreground">
                  Designation
                </Label>
                <p className="text-white">{entry.designation}</p>
              </div>
              <div>
                <Label className="mr-2 text-sm font-medium text-muted-foreground">
                  Status
                </Label>
                <Badge
                  variant={
                    entry.status === Status.ACTIVE ? 'default' : 'secondary'
                  }
                >
                  {entry.status === Status.ACTIVE ? 'Active' : 'Inactive'}
                </Badge>
              </div>
            </div>
            <div>
              <Label className="text-sm font-medium text-muted-foreground">
                Video Content
              </Label>
              <div className="mt-2 rounded-lg p-8 text-center">
                <Play className="mx-auto mb-4 size-8 text-primary" />
                <p className="mb-4 text-sm text-muted-foreground">
                  Video:{' '}
                  {entry.videoType === VideoType.UPLOAD_FILE
                    ? 'Uploaded File'
                    : entry.videoType === VideoType.YOUTUBE_URL
                      ? 'YouTube URL'
                      : 'Vimeo URL'}
                </p>
                <div className="flex items-center justify-center gap-4">
                  <div className="h-64 w-full overflow-hidden rounded-lg">
                    <video
                      src={FILE_PATH_PREFIX + entry.videoUrl}
                      controls
                      className="h-64 w-full bg-black object-contain"
                    />
                  </div>
                  <div className="h-64 w-full overflow-hidden rounded-lg">
                    <img
                      src={FILE_PATH_PREFIX + entry.thumbnailUrl}
                      alt="Thumbnail"
                      className="h-64 w-full bg-black object-contain"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ViewLeaderboardModal;
