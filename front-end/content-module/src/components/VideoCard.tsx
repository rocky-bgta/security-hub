import { ReactNode, useMemo, useRef } from 'react';

import { Button } from 'common/Button';
import { Card } from 'common/Card';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import { FILE_PATH_PREFIX, ValidVideoFormats } from 'utils/Constants';

interface IProps {
  title: string;
  subTitle?: string;
  selectedFile?: File | null;
  selectedVideo?: string;
  onUploadVideo: (file: File | null) => void;
  progress?: number;
  children?: ReactNode;
}

const VideoCard = ({
  title,
  subTitle,
  selectedFile,
  selectedVideo,
  onUploadVideo,
  progress = 0,
  children,
}: IProps) => {
  const videoUploaderRef = useRef<FileUploaderHandle>(null);

  const videoUrl = useMemo(() => {
    if (selectedFile) return URL.createObjectURL(selectedFile);
    if (selectedVideo) return FILE_PATH_PREFIX + selectedVideo;
  }, [selectedFile, selectedVideo]);

  const handleUpdateFiles = (files: FileList) => {
    if (files.length > 0) {
      onUploadVideo?.(files[0]);
    }
  };

  const handleClearVideo = () => {
    onUploadVideo?.(null);
    if (videoUploaderRef.current) {
      videoUploaderRef.current.clearFiles();
    }
  };

  return (
    <Card title={title}>
      <div className="content-p-5 content-pt-2.5">
        {subTitle && (
          <p className="content-text-base content-text-stormy-gray">
            {subTitle}
          </p>
        )}
        {videoUrl ? (
          <div className="content-mt-5 content-flex content-flex-col content-gap-y-2">
            <div className="content-relative">
              <video src={videoUrl} controls />
              {progress > 0 && progress < 100 && (
                <div className="content-absolute content-inset-0 content-flex content-items-center content-justify-center content-bg-black/50 content-text-sm content-text-white">
                  <span className="content-flex content-size-10 content-items-center content-justify-center content-rounded-full content-border content-border-white content-text-white">
                    {progress}%
                  </span>
                </div>
              )}
            </div>
            <Button
              onClick={handleClearVideo}
              className="content-w-fit !content-bg-transparent content-px-0 content-text-stormy-gray content-underline"
            >
              Clear Video
            </Button>
          </div>
        ) : null}

        <FileUploader
          ref={videoUploaderRef}
          containerClassName="content-my-5 content-text-secondary"
          accept={ValidVideoFormats.join(',')}
          placeholder="Upload video"
          onUpload={handleUpdateFiles}
          maxSize={30000000}
        />
        {children}
      </div>
    </Card>
  );
};

export default VideoCard;
