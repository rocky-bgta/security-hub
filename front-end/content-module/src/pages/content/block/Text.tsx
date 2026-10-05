import { Button } from 'common/Button';
import UserBlockTypedInputWithFeatureImage from 'components/UserBlockTypedInputWithFeatureImage';
import { IContent, ITextContent } from 'models/Content';

interface IProps {
  content: IContent<ITextContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const TextBased = ({
  content,
  isInteractive = false,
  handleNextContent,
}: IProps) => {
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
    </>
  );
};

export default TextBased;
