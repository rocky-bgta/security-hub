import { useEffect, useState } from 'react';
import { FaBars } from 'react-icons/fa';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { IContent, IQuizContent, IQuizOption } from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<IQuizContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const OrderingBlock = ({
  content,
  isInteractive,
  handleNextContent,
}: IProps) => {
  const [options, setOptions] = useState<Array<IQuizOption>>([]);
  const [shuffledOptions, setShuffledOptions] = useState<Array<IQuizOption>>(
    [],
  );

  useEffect(() => {
    const shuffledArray = [...options];
    for (let i = shuffledArray.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffledArray[i], shuffledArray[j]] = [
        shuffledArray[j],
        shuffledArray[i],
      ];
    }
    setShuffledOptions(shuffledArray);
  }, [options]);

  useEffect(() => {
    setOptions(content?.specific?.options || []);
  }, [content]);

  const handleDragStart = (e: React.DragEvent, index: number) => {
    e.dataTransfer.setData('draggedItemIndex', index.toString());
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDrop = (e: React.DragEvent, targetIndex: number) => {
    e.preventDefault();
    const draggedIndex = parseInt(
      e.dataTransfer.getData('draggedItemIndex'),
      10,
    );

    if (draggedIndex === targetIndex) return;

    const reorderedOptions = [...shuffledOptions];
    const [draggedItem] = reorderedOptions.splice(draggedIndex, 1);
    reorderedOptions.splice(targetIndex, 0, draggedItem);

    setShuffledOptions(reorderedOptions);
  };

  const handleCheckAnswer = () => {
    if (
      JSON.stringify(shuffledOptions.map(option => option.index)) ===
      JSON.stringify(options.map(option => option.index))
    ) {
      toast.success('Correct answer');
    } else {
      toast.error('Incorrect answer');
    }
  };

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
          style={{ color: content.specific.backgroundFormatting.textColor }}
        >
          {content?.specific?.question}
        </h1>
        <div className="content-mt-5 content-flex content-w-full content-flex-col content-items-center content-gap-3">
          {shuffledOptions.map((option, index) => (
            <div
              key={index}
              className="content-border-soft-gray content-flex content-min-w-80 content-cursor-move content-items-center content-justify-between content-gap-2 content-rounded-md content-border content-p-2"
              draggable
              onDragStart={e => handleDragStart(e, index)}
              onDragOver={handleDragOver}
              onDrop={e => handleDrop(e, index)}
            >
              <FaBars className="content-text-white" />
              <div className="content-flex content-items-center content-gap-3">
                {option.optionImageLink && (
                  <img
                    className="content-h-auto content-w-20"
                    src={FILE_PATH_PREFIX + option.optionImageLink}
                    alt={option.optionText}
                  />
                )}
                {option.optionText && (
                  <p
                    className="content-text-xl content-font-bold"
                    style={{
                      color: content.specific.backgroundFormatting.textColor,
                    }}
                  >
                    {option.optionText}
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
        {content?.specific?.options &&
          content?.specific?.options.length > 0 && (
            <Button
              className="content-float-right content-mt-5"
              size="sm"
              onClick={() => handleCheckAnswer()}
            >
              Check
            </Button>
          )}
      </div>
    </>
  );
};

export default OrderingBlock;
