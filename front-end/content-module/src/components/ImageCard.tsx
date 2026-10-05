import { ReactNode, useRef } from 'react';

import { Button } from 'common/Button';
import { Card } from 'common/Card';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import { FILE_PATH_PREFIX, ValidImageFormats } from 'utils/Constants';

interface IProps {
  title: string;
  subTitle?: string;
  selectedFile?: File | null;
  selectedImage?: string;
  onUploadImage: (file: File | null) => void;
  children?: ReactNode;
}

const ImageCard = ({
  title,
  subTitle,
  selectedFile,
  selectedImage,
  onUploadImage,
  children,
}: IProps) => {
  const imageUploaderRef = useRef<FileUploaderHandle>(null);

  const handleUpdateFiles = (files: FileList) => {
    if (files.length > 0) {
      onUploadImage?.(files[0]);
    }
  };

  const handleClearImage = () => {
    onUploadImage?.(null);
    imageUploaderRef.current?.clearFiles();
  };

  return (
    <Card title={title}>
      <div className="content-p-5 content-pt-2.5">
        {subTitle && (
          <p className="content-text-base content-text-white">{subTitle}</p>
        )}
        {selectedFile || selectedImage ? (
          <div className="content-mt-5 content-flex content-flex-col content-gap-y-2">
            <img
              src={
                selectedFile
                  ? URL.createObjectURL(selectedFile)
                  : FILE_PATH_PREFIX + selectedImage
              }
              alt="preview"
              className="content-aspect-video"
            />
            <Button
              onClick={handleClearImage}
              className="content-w-fit !content-bg-transparent content-px-0 content-text-stormy-gray content-underline"
            >
              Clear Image
            </Button>
          </div>
        ) : null}

        <FileUploader
          ref={imageUploaderRef}
          containerClassName="content-my-5 content-text-secondary"
          accept={ValidImageFormats.join(',')}
          placeholder="Upload image"
          onUpload={handleUpdateFiles}
        />

        {children}
      </div>
    </Card>
  );
};

export default ImageCard;
