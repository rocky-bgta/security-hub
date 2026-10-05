import { Button } from 'common/Button';
import UserBlockTypedInputWithFeatureImage from 'components/UserBlockTypedInputWithFeatureImage';
import { IContent, IPdfContent } from 'models/Content';
import { useEffect, useState } from 'react';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<IPdfContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const PdfBased = ({
  content,
  isInteractive = false,
  handleNextContent,
}: IProps) => {
  const [windowHeight, setWindowHeight] = useState(window.innerHeight);
  useEffect(() => {
    const handleResize = () => setWindowHeight(window.innerHeight);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return (
    <>
      {isInteractive && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}
      <UserBlockTypedInputWithFeatureImage content={content} />

      <div className="content-mt-5 content-w-full content-overflow-hidden">
        <iframe
          src={
            FILE_PATH_PREFIX +
            content?.specific?.metadata?.additionalProp1?.pdfLink
          }
          width={'100%'}
          height={Math.min(windowHeight * 0.7, 800)}
          title="PDF Viewer"
          className="content-max-w-full"
        />
      </div>
    </>
  );
};
export default PdfBased;
