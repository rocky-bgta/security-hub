import { Button } from 'common/Button';
import RansomwareSimulator from 'features/content/interactive-modules/ransomware-simulator/RansomwareSimulator';
import { IContent, IQuizContent } from 'models/Content';

interface IProps {
  content: IContent<IQuizContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const RansomwareSimulatorBlock = ({
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
        <RansomwareSimulator
          description={
            content?.specific?.question ||
            'Learn how ransomware attacks unfold — and why backups beat ransom payments'
          }
        />
      </div>
    </>
  );
};

export default RansomwareSimulatorBlock;
