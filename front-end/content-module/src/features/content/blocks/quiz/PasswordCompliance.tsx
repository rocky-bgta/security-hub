import { Button } from 'common/Button';
import PasswordCompliance from 'features/content/interactive-modules/password-compliance/PasswordCompliance';
import { IContent, IQuizContent } from 'models/Content';

interface IProps {
  content: IContent<IQuizContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const PasswordComplianceBlock = ({
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
        <PasswordCompliance
          description={
            content?.specific?.question ||
            'Validate your password against multiple security and privacy frameworks'
          }
        />
      </div>
    </>
  );
};

export default PasswordComplianceBlock;
