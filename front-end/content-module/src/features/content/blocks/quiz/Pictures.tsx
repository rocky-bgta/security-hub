import clsx from 'clsx';
import { Button } from 'common/Button';

import { IContent, IQuizContent } from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<IQuizContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const PictureBlock = ({
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
        <h1 className="content-text-center content-text-2xl content-font-bold content-text-white">
          {content?.specific?.question}
        </h1>
        <div className="content-mx-auto content-mt-5 content-grid content-w-1/2 content-grid-cols-2 content-gap-5">
          {content?.specific?.options?.map(option => (
            <div
              key={option.id}
              className={clsx(
                'content-h-40 content-w-full content-rounded content-border content-p-3',
                option.isCorrect && 'content-border-green-500',
              )}
            >
              <img
                src={FILE_PATH_PREFIX + option.optionImageLink}
                alt={option.optionText}
                className="content-size-full content-object-cover"
              />
            </div>
          ))}
        </div>
      </div>
    </>
  );
};

export default PictureBlock;
