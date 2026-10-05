import { Button } from 'common/Button';
import BlockTypedInputWithFeatureImage from 'components/BlockTypedInputWithFeatureImage';
import { IContent, ITextContent } from 'models/Content';

interface IProps {
  content: IContent<ITextContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const TextBlock = ({ content, isInteractive, handleNextContent }: IProps) => {
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
      </div>
    </>
  );
};

export default TextBlock;
