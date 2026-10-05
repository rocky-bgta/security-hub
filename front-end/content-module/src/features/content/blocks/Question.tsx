import clsx from 'clsx';

import { Button } from 'common/Button';
import { IContent, IQuestionContent } from 'models/Content';

interface IProps {
  content: IContent<IQuestionContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
  setIsCompleted?: (value: boolean) => void;
}

const QuestionBlock = ({
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

      <div className="content-max-h-[600px] content-overflow-y-auto content-p-5">
        {content?.specific?.question && (
          <p className="content-text-center content-text-2xl content-text-white">
            {content?.specific?.question}
          </p>
        )}

        {content?.specific?.options && (
          <div className="content-mt-5 content-flex content-flex-col content-items-center content-justify-center content-gap-y-4 content-text-lg">
            {content?.specific?.options.map((option, idx) => (
              <p
                key={idx}
                className={clsx(
                  'content-min-w-48 content-rounded-full content-border content-p-1 content-text-center',
                  option.isCorrect
                    ? 'content-border-green-500 content-text-green-500'
                    : 'content-border-white content-text-white',
                )}
              >
                {option.option}
              </p>
            ))}
          </div>
        )}
      </div>
    </>
  );
};

export default QuestionBlock;
