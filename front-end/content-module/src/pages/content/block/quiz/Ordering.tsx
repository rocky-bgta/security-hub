import { DragEvent, useEffect, useState } from 'react';
import { FlaskConical, GripVertical, ListOrdered } from 'lucide-react';

import { Button } from 'common/Button';
import ContentCompleteModal from 'common/modal/miniModal/ContentComplete';
import {
  AllContentTypes,
  IContent,
  IQuizContent,
  IQuizOption,
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

const OrderingBlock = ({
  content,
  showCompleteButton,
  isInteractiveVideo = false,
  isInteractiveContent = false,
  handleNextContent,
  courseComplete,
  isCompleted,
}: IProps) => {
  const [options, setOptions] = useState<Array<IQuizOption>>([]);
  const [shuffledOptions, setShuffledOptions] = useState<Array<IQuizOption>>(
    [],
  );
  const [showNextButton, setShowNextButton] = useState(false);
  const [showModal, setShowModal] = useState<boolean>(false);
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null);
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);
  const [hasInteracted, setHasInteracted] = useState(false);

  const isCorrect =
    JSON.stringify(shuffledOptions.map(option => option.index)) ===
    JSON.stringify(options.map(option => option.index));

  useEffect(() => {
    const shuffledArray = [...options];
    for (let i = shuffledArray.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffledArray[i], shuffledArray[j]] = [
        shuffledArray[j],
        shuffledArray[i],
      ];
    }
    setTimeout(() => {
      setShuffledOptions(shuffledArray);
    }, 0);
  }, [options]);

  useEffect(() => {
    setTimeout(() => {
      setOptions(content?.specific?.options || []);
    }, 0);
  }, [content]);

  const handleDragStart = (e: DragEvent, index: number) => {
    e.dataTransfer.setData('draggedItemIndex', index.toString());
    e.dataTransfer.effectAllowed = 'move';
    setDraggedIndex(index);
    setHasInteracted(true);
  };

  const handleDragOver = (e: DragEvent, index: number) => {
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
  };

  const handleDrop = (e: DragEvent, targetIndex: number) => {
    e.preventDefault();
    const draggedIdx = parseInt(e.dataTransfer.getData('draggedItemIndex'), 10);

    if (draggedIdx === targetIndex) {
      setDraggedIndex(null);
      setDragOverIndex(null);
      return;
    }

    const reorderedOptions = [...shuffledOptions];
    const [draggedItem] = reorderedOptions.splice(draggedIdx, 1);
    reorderedOptions.splice(targetIndex, 0, draggedItem);

    setShuffledOptions(reorderedOptions);
    setDraggedIndex(null);
    setDragOverIndex(null);
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
      <div className="content-min-h-[600px] content-px-40 content-py-16">
        <h1
          className="content-text-center content-text-[32px] content-font-bold"
          style={{ color: textColor }}
        >
          {content?.specific?.question}
        </h1>

        {/* Instructional hint */}
        {shuffledOptions.length > 0 && (
          <div className="content-mx-auto content-mt-4 content-flex content-w-fit content-items-center content-gap-2 content-rounded-full content-border content-border-white content-border-opacity-15 content-bg-white content-bg-opacity-5 content-px-5 content-py-2">
            <ListOrdered className="content-size-4 content-shrink-0 content-text-primary" />
            <span className="content-text-sm content-text-white content-text-opacity-60">
              Drag items to arrange them in the correct order
            </span>
          </div>
        )}

        <div className="content-mt-5 content-grid content-w-full content-grid-cols-1 content-flex-col content-items-center content-gap-3">
          {shuffledOptions.map((option, index) => {
            const isDragged = draggedIndex === index;
            const isDragOver =
              dragOverIndex === index && draggedIndex !== null && !isDragged;

            return (
              <div
                key={index}
                className={[
                  'content-group content-flex content-h-20 content-items-center content-rounded-lg content-border content-p-3',
                  'content-transition-all content-duration-200 content-ease-in-out',
                  isDragged
                    ? 'content-scale-[0.98] content-border-primary content-opacity-50'
                    : isDragOver
                      ? 'content-border-primary content-bg-primary content-bg-opacity-10 content-shadow-lg'
                      : 'content-border-graphite content-bg-white content-bg-opacity-10 hover:content-border-white hover:content-border-opacity-40 hover:content-bg-opacity-15',
                  draggedIndex !== null
                    ? 'content-cursor-grabbing'
                    : 'content-cursor-grab',
                  !hasInteracted ? 'quiz-drag-hint' : '',
                ].join(' ')}
                style={
                  !hasInteracted
                    ? { animationDelay: `${0.8 + index * 0.1}s` }
                    : undefined
                }
                draggable
                onDragStart={e => handleDragStart(e, index)}
                onDragOver={e => handleDragOver(e, index)}
                onDragLeave={handleDragLeave}
                onDragEnd={handleDragEnd}
                onDrop={e => handleDrop(e, index)}
              >
                {/* Step number indicator */}
                <div className="content-mr-3 content-flex content-size-8 content-shrink-0 content-items-center content-justify-center content-rounded-full content-bg-white content-bg-opacity-10 content-text-sm content-font-semibold content-text-white content-text-opacity-50">
                  {index + 1}
                </div>

                {/* Drag handle */}
                <GripVertical className="content-mr-3 content-size-5 content-shrink-0 content-text-white content-opacity-30 content-transition-opacity group-hover:content-opacity-60" />

                <div className="content-flex content-items-center content-gap-3">
                  {option.optionImageLink && (
                    <img
                      className="content-h-auto content-w-20 content-rounded-sm content-p-1"
                      src={FILE_PATH_PREFIX + option.optionImageLink}
                      alt={option.optionText}
                      draggable={false}
                    />
                  )}
                  {option.optionText && (
                    <p
                      className="content-font-medium"
                      style={{ color: textColor }}
                    >
                      {option.optionText}
                    </p>
                  )}
                </div>
              </div>
            );
          })}
          {!!content?.specific?.options?.length && (
            <div className="content-flex content-justify-end">
              <Button
                className="content-float-right content-mt-5 !content-text-base !content-py-7"
                size="sm"
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
        isCorrect={isCorrect}
        isOpen={showModal}
        isInteractive={isInteractiveVideo || isInteractiveContent}
        onClose={() => setShowModal(false)}
        onClick={() => {
          setShowModal(false);

          if (isInteractiveVideo) {
            handleNextContent?.();
            return;
          }

          if (
            JSON.stringify(shuffledOptions.map(option => option.index)) ===
            JSON.stringify(options.map(option => option.index))
          ) {
            showCompleteButton?.(content?.specific?.quizType as QuizTypes);
            courseComplete?.();
            setShowNextButton(true);
          }
        }}
      />
    </>
  );
};

export default OrderingBlock;
