import { useEffect, useState } from 'react';

import { Button } from 'common/Button';
import {
  IContent,
  IQuizContent,
  IQuizOption,
  LikerSelectOptionIconMapper,
} from 'models/Content';

interface IProps {
  content: IContent<IQuizContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const LikerSelectBlock = ({
  content,
  isInteractive,
  handleNextContent,
}: IProps) => {
  const [options, setOptions] = useState<Array<IQuizOption>>([]);

  useEffect(() => {
    setOptions(content?.specific?.options || []);
  }, [content]);

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
        <h1
          className="content-text-center content-text-3xl content-font-bold"
          style={{
            color: content?.specific?.backgroundFormatting?.textColor,
          }}
        >
          {content?.specific?.question}
        </h1>
        <div className="content-mt-10 content-flex content-w-full content-justify-center content-gap-8">
          {options.map((option, index) => {
            const IconComponent = option.optionImageLink
              ? LikerSelectOptionIconMapper[
                  option.optionImageLink as keyof typeof LikerSelectOptionIconMapper
                ]
              : null;

            return (
              <div
                key={index}
                className="content-flex content-flex-col content-items-center content-gap-2"
              >
                {IconComponent && <IconComponent />}
                <p
                  className="content-max-w-[80px] content-text-center content-text-sm content-leading-tight"
                  style={{
                    color: content?.specific?.backgroundFormatting?.textColor,
                  }}
                >
                  {option.optionText}
                </p>
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
};

export default LikerSelectBlock;
