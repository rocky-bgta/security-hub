import { Link } from 'react-router-dom';

import { Button } from 'common/Button';
import BlockTypedInputWithFeatureImage from 'components/BlockTypedInputWithFeatureImage';
import { IContent, IPdfContent } from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<IPdfContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const PdfBlock = ({ content, isInteractive, handleNextContent }: IProps) => {
  return (
    <>
      {isInteractive && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}

      <div className="content-max-h-[600px] content-overflow-y-auto content-p-5">
        <BlockTypedInputWithFeatureImage content={content} />

        {content?.specific?.metadata?.additionalProp1?.pdfLink && (
          <Link
            to={
              FILE_PATH_PREFIX +
              content?.specific?.metadata?.additionalProp1?.pdfLink
            }
            target="_blank"
            style={{
              color: content.specific.metadata?.additionalProp1.pdfTextColor,
            }}
            className="content-mt-3 content-block content-underline"
          >
            {content.specific.metadata?.additionalProp1.pdfText}
          </Link>
        )}
      </div>
    </>
  );
};

export default PdfBlock;
