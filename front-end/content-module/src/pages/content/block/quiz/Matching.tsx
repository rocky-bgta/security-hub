import { DragEvent, useEffect, useState } from 'react';
import { ArrowLeftRight, FlaskConical, GripVertical } from 'lucide-react';

import { Button } from 'common/Button';
import ContentCompleteModal from 'common/modal/miniModal/ContentComplete';
import {
  AllContentTypes,
  IContent,
  IMatchingOption,
  IQuizContent,
  QuizTypes,
} from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<IQuizContent>;
  showCompleteButton?: (type: AllContentTypes) => void;
  isInteractiveVideo?: boolean;
  isInteractiveContent?: boolean;
  handleNextContent?: () => void;
  courseComplete?: () => void;
  isCompleted?: boolean;
}

type DragSide = 'left' | 'right';

const sameValue = (
  typeA: string,
  valueA: any,
  linkA: any,
  typeB: string,
  valueB: any,
  linkB: any,
): boolean => {
  if (typeA !== typeB) return false;
  if (typeA === 'text') return String(valueA) === String(valueB);
  if (typeA === 'image') return linkA && linkB && linkA === linkB;
  return false;
};

const MatchingBlock = ({
  content,
  showCompleteButton,
  isInteractiveVideo = false,
  isInteractiveContent = false,
  handleNextContent,
  courseComplete,
  isCompleted,
}: IProps) => {
  const [options, setOptions] = useState<Array<IMatchingOption>>([]);
  const [originalPairs, setOriginalPairs] = useState<Array<IMatchingOption>>(
    [],
  );
  const [showNextButton, setShowNextButton] = useState(false);
  const [showModal, setShowModal] = useState<boolean>(false);
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null);
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);
  const [dragSide, setDragSide] = useState<DragSide | null>(null);
  const [hasInteracted, setHasInteracted] = useState(false);

  const allCorrect = options.every(option =>
    originalPairs.some(
      pair =>
        sameValue(
          option.inputTypeLeft,
          option.valueLeft,
          option.linkLeft,
          pair.inputTypeLeft,
          pair.valueLeft,
          pair.linkLeft,
        ) &&
        sameValue(
          option.inputTypeRight,
          option.valueRight,
          option.linkRight,
          pair.inputTypeRight,
          pair.valueRight,
          pair.linkRight,
        ),
    ),
  );

  const shuffleArray = (array: Array<IMatchingOption>) => {
    const shuffled = [...array];

    const rightValues = shuffled.map(item => ({
      valueRight: item.valueRight,
      linkRight: item.linkRight,
      inputTypeRight: item.inputTypeRight,
    }));

    for (let i = rightValues.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [rightValues[i], rightValues[j]] = [rightValues[j], rightValues[i]];
    }

    return shuffled.map((item, index) => ({
      ...item,
      valueRight: rightValues[index].valueRight,
      linkRight: rightValues[index].linkRight,
      inputTypeRight: rightValues[index].inputTypeRight,
    }));
  };

  useEffect(() => {
    if (content?.specific?.pairs && content.specific.pairs.length > 0) {
      setTimeout(() => {
        setOptions(shuffleArray(content.specific.pairs || []));
      }, 0);
      setTimeout(() => {
        setOriginalPairs(content.specific.pairs || []);
      }, 0);
    }
  }, [content]);

  const handleDragStart = (e: DragEvent, index: number, side: DragSide) => {
    e.dataTransfer.setData('draggedItemIndex', index.toString());
    e.dataTransfer.effectAllowed = 'move';
    setDraggedIndex(index);
    setDragSide(side);
    setHasInteracted(true);
  };

  const handleDragOver = (e: DragEvent, index: number, side: DragSide) => {
    if (dragSide !== side) return;
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    if (dragOverIndex !== index) setDragOverIndex(index);
  };

  const handleDragLeave = () => {
    setDragOverIndex(null);
  };

  const handleDragEnd = () => {
    setDraggedIndex(null);
    setDragOverIndex(null);
    setDragSide(null);
  };

  const handleDrop = (e: DragEvent, targetIndex: number, side: DragSide) => {
    e.preventDefault();

    if (dragSide !== side) {
      handleDragEnd();
      return;
    }

    const draggedIdx = parseInt(e.dataTransfer.getData('draggedItemIndex'), 10);

    if (draggedIdx === targetIndex) {
      handleDragEnd();
      return;
    }

    const reorderedOptions = [...options];

    if (side === 'right') {
      const draggedRightValue = reorderedOptions[draggedIdx].valueRight;
      const draggedRightLink = reorderedOptions[draggedIdx].linkRight;
      const draggedInputTypeRight = reorderedOptions[draggedIdx].inputTypeRight;

      reorderedOptions[draggedIdx].valueRight =
        reorderedOptions[targetIndex].valueRight;
      reorderedOptions[draggedIdx].linkRight =
        reorderedOptions[targetIndex].linkRight;
      reorderedOptions[draggedIdx].inputTypeRight =
        reorderedOptions[targetIndex].inputTypeRight;

      reorderedOptions[targetIndex].valueRight = draggedRightValue;
      reorderedOptions[targetIndex].linkRight = draggedRightLink;
      reorderedOptions[targetIndex].inputTypeRight = draggedInputTypeRight;
    } else {
      const draggedLeftValue = reorderedOptions[draggedIdx].valueLeft;
      const draggedLeftLink = reorderedOptions[draggedIdx].linkLeft;
      const draggedInputTypeLeft = reorderedOptions[draggedIdx].inputTypeLeft;

      reorderedOptions[draggedIdx].valueLeft =
        reorderedOptions[targetIndex].valueLeft;
      reorderedOptions[draggedIdx].linkLeft =
        reorderedOptions[targetIndex].linkLeft;
      reorderedOptions[draggedIdx].inputTypeLeft =
        reorderedOptions[targetIndex].inputTypeLeft;

      reorderedOptions[targetIndex].valueLeft = draggedLeftValue;
      reorderedOptions[targetIndex].linkLeft = draggedLeftLink;
      reorderedOptions[targetIndex].inputTypeLeft = draggedInputTypeLeft;
    }

    setOptions(reorderedOptions);
    handleDragEnd();
  };

  const handleCheckAnswer = () => {
    setShowModal(true);
  };

  const textColor = content?.specific?.backgroundFormatting?.textColor;

  return (
    <>
      {isInteractiveContent && showNextButton && (
        <div className="content-absolute content-bottom-1 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}
      <div className="content-max-h-[600px] content-p-16">
        <div className="content-mx-auto content-flex content-flex-col content-gap-4">
          <p
            className="content-text-center content-text-3xl content-font-bold"
            style={{ color: textColor }}
          >
            {content?.specific?.question}
          </p>

          {/* Instructional hint */}
          {options.length > 0 && (
            <div className="content-mx-auto content-flex content-items-center content-gap-2 content-rounded-full content-border content-border-white content-border-opacity-15 content-bg-white content-bg-opacity-5 content-px-5 content-py-2">
              <ArrowLeftRight className="content-size-4 content-shrink-0 content-text-primary" />
              <span className="content-text-sm content-text-white content-text-opacity-60">
                Drag items on either side to create the correct matches
              </span>
            </div>
          )}

          {/* Column headers */}
          <div className="content-mt-3 content-flex content-items-center content-justify-between content-px-2">
            <h4
              className="content-w-[42%] content-text-center content-text-sm content-uppercase content-tracking-wider content-text-white content-text-opacity-50"
              style={{ color: textColor }}
            >
              {content?.specific?.labelLeft}
            </h4>
            <div className="content-w-[16%]" />
            <h4
              className="content-w-[42%] content-text-center content-text-sm content-uppercase content-tracking-wider content-text-white content-text-opacity-50"
              style={{ color: textColor }}
            >
              {content?.specific?.labelRight}
            </h4>
          </div>

          {options.map((option, index) => {
            const isLeftDragged = dragSide === 'left' && draggedIndex === index;
            const isLeftDragOver =
              dragSide === 'left' &&
              dragOverIndex === index &&
              draggedIndex !== null &&
              draggedIndex !== index;

            const isRightDragged =
              dragSide === 'right' && draggedIndex === index;
            const isRightDragOver =
              dragSide === 'right' &&
              dragOverIndex === index &&
              draggedIndex !== null &&
              draggedIndex !== index;

            return (
              <div
                key={index}
                className="content-flex content-items-center content-justify-between content-gap-3"
              >
                {/* LEFT SIDE - draggable */}
                <div
                  className={[
                    'content-group content-flex content-h-20 content-w-[42%] content-items-center content-gap-3 content-rounded-lg content-border content-px-4',
                    'content-transition-all content-duration-200 content-ease-in-out',
                    isLeftDragged
                      ? 'content-scale-95 content-border-primary content-opacity-50'
                      : isLeftDragOver
                        ? 'content-border-primary content-bg-primary content-bg-opacity-10 content-shadow-lg'
                        : 'content-border-graphite content-bg-white content-bg-opacity-10 hover:content-border-white hover:content-border-opacity-40 hover:content-bg-opacity-15',
                    dragSide === 'left' && draggedIndex !== null
                      ? 'content-cursor-grabbing'
                      : 'content-cursor-grab',
                    !hasInteracted ? 'quiz-drag-hint' : '',
                  ].join(' ')}
                  style={
                    !hasInteracted
                      ? { animationDelay: `${1.2 + index * 0.12}s` }
                      : undefined
                  }
                  draggable
                  onDragStart={e => handleDragStart(e, index, 'left')}
                  onDragOver={e => handleDragOver(e, index, 'left')}
                  onDragLeave={handleDragLeave}
                  onDragEnd={handleDragEnd}
                  onDrop={e => handleDrop(e, index, 'left')}
                >
                  <GripVertical className="content-size-5 content-shrink-0 content-text-white content-opacity-30 content-transition-opacity group-hover:content-opacity-60" />
                  <div className="content-flex content-flex-1 content-items-center content-justify-center">
                    {option.inputTypeLeft === 'text' && option.valueLeft && (
                      <p
                        className="content-text-center content-font-medium"
                        style={{ color: textColor }}
                      >
                        {typeof option.valueLeft === 'string'
                          ? option.valueLeft
                          : ''}
                      </p>
                    )}
                    {option.inputTypeLeft === 'image' && (
                      <img
                        src={FILE_PATH_PREFIX + option.linkLeft || ''}
                        alt="Left option"
                        className="content-size-16 content-object-contain content-p-1"
                        draggable={false}
                      />
                    )}
                  </div>
                </div>

                {/* CONNECTOR */}
                <div className="content-flex content-w-[16%] content-items-center content-justify-center">
                  <div className="content-flex content-items-center content-gap-1">
                    <div className="content-h-px content-w-4 content-bg-white content-bg-opacity-25" />
                    <ArrowLeftRight className="content-size-4 content-text-white content-text-opacity-30" />
                    <div className="content-h-px content-w-4 content-bg-white content-bg-opacity-25" />
                  </div>
                </div>

                {/* RIGHT SIDE - draggable */}
                <div
                  className={[
                    'content-group content-flex content-h-20 content-w-[42%] content-items-center content-gap-3 content-rounded-lg content-border content-px-4',
                    'content-transition-all content-duration-200 content-ease-in-out',
                    isRightDragged
                      ? 'content-scale-95 content-border-primary content-opacity-50'
                      : isRightDragOver
                        ? 'content-border-primary content-bg-primary content-bg-opacity-10 content-shadow-lg'
                        : 'content-border-graphite content-bg-white content-bg-opacity-10 hover:content-border-white hover:content-border-opacity-40 hover:content-bg-opacity-15',
                    dragSide === 'right' && draggedIndex !== null
                      ? 'content-cursor-grabbing'
                      : 'content-cursor-grab',
                    !hasInteracted ? 'quiz-drag-hint' : '',
                  ].join(' ')}
                  style={
                    !hasInteracted
                      ? { animationDelay: `${1.2 + index * 0.12}s` }
                      : undefined
                  }
                  draggable
                  onDragStart={e => handleDragStart(e, index, 'right')}
                  onDragOver={e => handleDragOver(e, index, 'right')}
                  onDragLeave={handleDragLeave}
                  onDragEnd={handleDragEnd}
                  onDrop={e => handleDrop(e, index, 'right')}
                >
                  <GripVertical className="content-size-5 content-shrink-0 content-text-white content-opacity-30 content-transition-opacity group-hover:content-opacity-60" />
                  <div className="content-flex content-flex-1 content-items-center content-justify-center">
                    {option.inputTypeRight === 'text' && option.valueRight && (
                      <p
                        className="content-text-center content-font-medium"
                        style={{ color: textColor }}
                      >
                        {typeof option.valueRight === 'string'
                          ? option.valueRight
                          : ''}
                      </p>
                    )}
                    {option.inputTypeRight === 'image' && (
                      <img
                        src={FILE_PATH_PREFIX + option.linkRight || ''}
                        alt="Right option"
                        className="content-size-16 content-rounded-sm content-object-contain"
                        draggable={false}
                      />
                    )}
                  </div>
                </div>
              </div>
            );
          })}

          {options.length > 0 && (
            <div className="content-flex content-justify-end">
              <Button
                className="content-float-right content-mt-5 !content-text-base !content-py-7"
                size="lg"
                onClick={handleCheckAnswer}
                disabled={isCompleted}
              >
                <FlaskConical /> Check Answer
              </Button>
            </div>
          )}
        </div>
      </div>

      <ContentCompleteModal
        isCorrect={allCorrect}
        isOpen={showModal}
        isInteractive={isInteractiveVideo || isInteractiveContent}
        onClose={() => setShowModal(false)}
        onClick={() => {
          setShowModal(false);

          if (isInteractiveVideo) {
            handleNextContent?.();
            return;
          }

          if (allCorrect) {
            courseComplete?.();
            showCompleteButton?.(content?.specific?.quizType as QuizTypes);
            setShowNextButton(true);
          }
        }}
      />
    </>
  );
};

export default MatchingBlock;
