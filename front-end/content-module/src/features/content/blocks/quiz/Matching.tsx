import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { IContent, IMatchingOption, IQuizContent } from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<IQuizContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const createAnswerKey = (pairs: IMatchingOption[]) => {
  // Create a frozen object (immutable) that maps IDs to their correct right values
  return Object.freeze(
    pairs.reduce(
      (map, pair) => {
        map[pair.id] = {
          valueRight: pair.valueRight,
          linkRight: pair.linkRight,
          inputTypeRight: pair.inputTypeRight,
        };
        return map;
      },
      {} as Record<
        string,
        { valueRight: any; linkRight: any; inputTypeRight: string }
      >,
    ),
  );
};

const MatchingBlock = ({
  content,
  isInteractive,
  handleNextContent,
}: IProps) => {
  // State for current options that can be modified
  const [options, setOptions] = useState<Array<IMatchingOption>>([]);

  // State to store the answer key, now initialized when content changes
  const [answerKey, setAnswerKey] = useState<
    Record<string, { valueRight: any; linkRight: any; inputTypeRight: string }>
  >({});

  const shuffleArray = (array: Array<IMatchingOption>) => {
    const shuffled = [...array];

    // Fisher-Yates shuffle algorithm for the right side values
    const rightValues = shuffled.map(item => ({
      valueRight: item.valueRight,
      linkRight: item.linkRight,
      inputTypeRight: item.inputTypeRight,
    }));

    for (let i = rightValues.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [rightValues[i], rightValues[j]] = [rightValues[j], rightValues[i]];
    }

    // Apply shuffled right values to options
    const newOptions = shuffled.map((item, index) => ({
      ...item,
      valueRight: rightValues[index].valueRight,
      linkRight: rightValues[index].linkRight,
      inputTypeRight: rightValues[index].inputTypeRight,
    }));
    return newOptions;
  };

  useEffect(() => {
    // Set a copy of the pairs to options state for dragging/dropping
    if (content?.specific?.pairs && content.specific.pairs.length > 0) {
      setOptions(shuffleArray(content.specific.pairs));
      // Update the answer key whenever content changes
      setAnswerKey(createAnswerKey(content.specific.pairs));
    }
  }, [content]);

  const handleDragStart = (e: React.DragEvent, index: number) => {
    // Set the index of the dragged item
    e.dataTransfer.setData('draggedItemIndex', index.toString());

    // Make sure dragging works properly for all elements including images
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    // Indicate that drop is allowed
    e.dataTransfer.dropEffect = 'move';
  };

  const handleDrop = (e: React.DragEvent, targetIndex: number) => {
    e.preventDefault();
    const draggedIndex = parseInt(
      e.dataTransfer.getData('draggedItemIndex'),
      10,
    );

    if (draggedIndex === targetIndex) return;

    const reorderedOptions = [...options];

    // Store all the relevant right-side properties of the dragged item
    const draggedRightValue = reorderedOptions[draggedIndex].valueRight;
    const draggedRightLink = reorderedOptions[draggedIndex].linkRight;
    const draggedInputTypeRight = reorderedOptions[draggedIndex].inputTypeRight;

    // Swap all right-side properties, including the inputTypeRight
    reorderedOptions[draggedIndex].valueRight =
      reorderedOptions[targetIndex].valueRight;
    reorderedOptions[draggedIndex].linkRight =
      reorderedOptions[targetIndex].linkRight;
    reorderedOptions[draggedIndex].inputTypeRight =
      reorderedOptions[targetIndex].inputTypeRight;

    reorderedOptions[targetIndex].valueRight = draggedRightValue;
    reorderedOptions[targetIndex].linkRight = draggedRightLink;
    reorderedOptions[targetIndex].inputTypeRight = draggedInputTypeRight;

    setOptions(reorderedOptions);
  };

  const handleCheckAnswer = () => {
    // Check if current options have the correct right values for each left ID
    const allCorrect = options.every(option => {
      const correctMatch = answerKey[option.id];

      if (!correctMatch) return false;

      // For text type, compare string values
      if (
        option.inputTypeRight === 'text' &&
        correctMatch.inputTypeRight === 'text'
      ) {
        return String(option.valueRight) === String(correctMatch.valueRight);
      }

      // For image type, compare links (URLs) instead of File objects
      if (
        option.inputTypeRight === 'image' &&
        correctMatch.inputTypeRight === 'image'
      ) {
        // If we have linkRight values, compare those
        if (option.linkRight && correctMatch.linkRight) {
          return option.linkRight === correctMatch.linkRight;
        }
      }

      // Fallback comparison for other cases
      return false;
    });

    if (allCorrect) {
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
        <div className="content-mx-auto content-flex content-max-w-4xl content-flex-col content-gap-4">
          <p
            className="content-text-center content-text-xl content-font-bold content-text-white"
            style={{
              color: content?.specific?.backgroundFormatting?.textColor,
            }}
          >
            {content?.specific?.question}
          </p>

          <div className="content-mt-5 content-flex content-items-center content-justify-around">
            <h4
              style={{
                color: content?.specific?.backgroundFormatting?.textColor,
              }}
            >
              {content?.specific?.labelLeft}
            </h4>
            <h4
              style={{
                color: content?.specific?.backgroundFormatting?.textColor,
              }}
            >
              {content?.specific?.labelRight}
            </h4>
          </div>

          {options.map((option, index) => (
            <div
              key={index}
              className="content-mt-5 content-flex content-items-center content-justify-center content-gap-7"
            >
              {/* LEFT SIDE ITEM */}
              <div className="content-border-soft-gray content-min-w-80 content-rounded-md content-border content-bg-white content-bg-opacity-10 content-p-3">
                {option.inputTypeLeft === 'text' && option.valueLeft && (
                  <p
                    className="content-text-center content-text-xl content-font-bold content-text-white"
                    style={{
                      color: content?.specific?.backgroundFormatting?.textColor,
                    }}
                  >
                    {typeof option.valueLeft === 'string'
                      ? option.valueLeft
                      : ''}
                  </p>
                )}
                {option.inputTypeLeft === 'image' && (
                  <div className="content-flex content-items-center content-justify-center">
                    <img
                      src={FILE_PATH_PREFIX + option.linkLeft || ''}
                      alt="Left option"
                      className="content-size-14 content-object-contain content-p-1"
                    />
                  </div>
                )}
              </div>

              {/* CONNECTING LINE */}
              <div className="content-relative content-h-1 content-w-20 content-bg-white">
                <div className="content-absolute -content-left-1 -content-top-0.5 content-size-2 content-rounded-full content-bg-white" />
                <div className="content-absolute -content-right-1 -content-top-0.5 content-size-2 content-rounded-full content-bg-white" />
              </div>

              {/* RIGHT SIDE ITEM */}
              <div
                className="content-border-soft-gray content-min-w-80 content-cursor-move content-rounded-md content-border content-bg-white content-bg-opacity-10 content-p-3"
                draggable={true}
                onDragStart={e => handleDragStart(e, index)}
                onDragOver={handleDragOver}
                onDrop={e => handleDrop(e, index)}
              >
                {option.inputTypeRight === 'text' && option.valueRight && (
                  <p
                    className="content-text-center content-text-xl content-font-bold content-text-white"
                    style={{
                      color: content?.specific?.backgroundFormatting?.textColor,
                    }}
                  >
                    {typeof option.valueRight === 'string'
                      ? option.valueRight
                      : ''}
                  </p>
                )}
                {option.inputTypeRight === 'image' && (
                  <div className="content-flex content-items-center content-justify-center">
                    <img
                      src={FILE_PATH_PREFIX + option.linkRight || ''}
                      alt="Right option"
                      className="content-size-14 content-object-contain content-p-1"
                      draggable={false}
                    />
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
        {options.length > 0 && (
          <Button
            className="content-float-right content-mt-5"
            size="sm"
            onClick={handleCheckAnswer}
          >
            Check
          </Button>
        )}
      </div>
    </>
  );
};

export default MatchingBlock;
