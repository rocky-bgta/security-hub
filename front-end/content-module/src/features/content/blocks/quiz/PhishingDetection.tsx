import { Button } from 'common/Button';
import PhishingDetection from 'features/content/interactive-modules/phishing-detection/PhishingDetection';
import {
  DEFAULT_PHISHING_TIME_LIMIT,
  IContent,
  IQuizContent,
} from 'models/Content';

interface IProps {
  content: IContent<IQuizContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const PhishingDetectionBlock = ({
  content,
  isInteractive,
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
      <div className="content-max-h-[700px] content-overflow-y-auto">
        <PhishingDetection
          description={
            content?.specific?.question ||
            'Inspect each email, hover suspicious links, and decide whether it is phishing or safe.'
          }
          emails={content?.specific?.emails}
          timeLimitSeconds={
            content?.specific?.timeLimitSeconds ?? DEFAULT_PHISHING_TIME_LIMIT
          }
        />
      </div>
    </>
  );
};

export default PhishingDetectionBlock;
