import { ImageAnnotation } from '@annotorious/react';
import clsx from 'clsx';
import {
  ChangeEvent,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';
import { BsPencil, BsSquare } from 'react-icons/bs';
import { PiEraserFill } from 'react-icons/pi';

import { DeleteIcon } from 'assets/icons';
import { Button } from 'common/Button';
import { Label } from 'common/Label';
import {
  CustomImageAnnotator,
  TAnnoter,
} from 'components/CustomImageAnnotator';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import InputCard from 'components/InputCard';
import { IDataValidationHandle, IQuizFields } from 'models/Content';
import { FILE_PATH_PREFIX, ValidImageFormats } from 'utils/Constants';

interface IProps {
  data: IQuizFields;
  updateData: (data: IQuizFields) => void;
}

enum AnnotationTool {
  RECTANGLE = 'rectangle',
  POLYGON = 'polygon',
}

const HotSpotQuiz = forwardRef<IDataValidationHandle, IProps>(
  ({ data, updateData }, ref) => {
    const fileUploaderRef = useRef<FileUploaderHandle>(null);

    const [annotatorInstance, setAnnotatorInstance] = useState<TAnnoter | null>(
      null,
    );
    const [activeDrawingTool, setActiveDrawingTool] = useState<AnnotationTool>(
      AnnotationTool.RECTANGLE,
    );
    const [error, setError] = useState<{
      imageTitle: string;
      tags: string;
      image: string;
      correctFeedback: string;
      incorrectFeedback: string;
    }>({
      imageTitle: '',
      tags: '',
      image: '',
      correctFeedback: '',
      incorrectFeedback: '',
    });

    useEffect(() => {
      if (annotatorInstance) {
        annotatorInstance.clearAnnotations();

        if (data?.annotations) {
          data.annotations.forEach(annotation => {
            annotatorInstance.addAnnotation(annotation);
          });
        }
      }
    }, [annotatorInstance]);

    const handleChangeInput = (
      e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    ) => {
      const { id, value } = e.target;

      updateData({
        ...data,
        [id]: value,
      });

      setError(prev => ({
        ...prev,
        [id]: value.trim() ? '' : 'The field is required',
      }));
    };

    const handleUpdateFiles = (files: FileList) => {
      if (files.length > 0) {
        updateData({
          ...data,
          imageFile: files[0],
          annotations: [],
          correctTags: [],
        });
      }
    };

    const handleChangeDrawingTool = (tool: AnnotationTool) =>
      setActiveDrawingTool(tool);

    const handleInitializeAnnotator = (annotator: TAnnoter) =>
      setAnnotatorInstance(annotator);

    const handleCreateAnnotation = (annotation: ImageAnnotation) => {
      const updatedAnnotations = [...data.annotations!, annotation];

      updateData({
        ...data,
        annotations: updatedAnnotations,
      });
    };

    const handleUpdateAnnotation = (annotation: ImageAnnotation) => {
      const updatedAnnotations = [
        ...data.annotations!.map(a =>
          a.id === annotation.id ? annotation : a,
        ),
      ];

      updateData({
        ...data,
        annotations: updatedAnnotations,
      });
    };

    const handleDeleteAnnotation = (annotation: ImageAnnotation) => {
      const updatedAnnotations = [
        ...data.annotations!.filter(a => a.id !== annotation.id),
      ];

      updateData({
        ...data,
        annotations: updatedAnnotations,
      });
    };

    const handleEraseSelectedAnnotation = () => {
      const selectedAnnotations =
        annotatorInstance?.getSelected() as Array<ImageAnnotation>;
      if (!selectedAnnotations) return;

      const specifiedAnnotation = annotatorInstance?.getAnnotationById(
        selectedAnnotations[0].id,
      ) as ImageAnnotation;
      annotatorInstance?.removeAnnotation(specifiedAnnotation);

      updateData({
        ...data,
        annotations: data.annotations?.filter(
          annotation => annotation.id !== specifiedAnnotation.id,
        ),
        correctTags: data.correctTags?.filter(
          tag => tag !== specifiedAnnotation.id,
        ),
      });
    };

    const handleEraseAllAnnotations = () => {
      annotatorInstance?.clearAnnotations();

      updateData({
        ...data,
        annotations: [],
        correctTags: [],
      });
    };

    const handleToggleCorrectTag = (tag: string) => {
      const isCurrentlyCorrect = data.correctTags?.includes(tag);
      const updatedTags = isCurrentlyCorrect
        ? data.correctTags!.filter(t => t !== tag)
        : [...data.correctTags!, tag];

      updateData({
        ...data,
        correctTags: updatedTags,
      });

      setError(prev => ({
        ...prev,
        tags: updatedTags.length
          ? ''
          : 'At least one correct answer is required',
      }));
    };

    const validateFields = () => {
      let isValid = true;

      if (!data.imageTitle?.trim()) {
        isValid = false;
        setError(prev => ({
          ...prev,
          imageTitle: 'Question title is required',
        }));
      }

      if (!data.imageFile && !data.imageLink) {
        isValid = false;
        setError(prev => ({
          ...prev,
          image: 'Image is required',
        }));
      }

      if (!data.annotations?.length) {
        isValid = false;
        setError(prev => ({
          ...prev,
          tags: 'At least one tag is required',
        }));
      }

      if (!data.correctTags?.length) {
        isValid = false;
        setError(prev => ({
          ...prev,
          tags: 'At least one correct answer is required',
        }));
      }

      if (!data.correctFeedback?.trim()) {
        isValid = false;
        setError(prev => ({
          ...prev,
          correctFeedback: 'Correct answer feedback is required',
        }));
      }

      if (!data.incorrectFeedback?.trim()) {
        isValid = false;
        setError(prev => ({
          ...prev,
          incorrectFeedback: 'Incorrect answer feedback is required',
        }));
      }

      return isValid;
    };

    useImperativeHandle(ref, () => ({
      validateData: () => {
        return { success: validateFields() };
      },
    }));

    return (
      <div className="content-mt-5">
        <InputCard
          title="Question Title"
          inputFields={[
            {
              className:
                'content-my-2 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'imageTitle',
              value: data.imageTitle,
              error: error.imageTitle,
              placeholder:
                'Type your quiz title here (e.g., "Which are the birds?")',
              onChange: handleChangeInput,
            },
          ]}
        />

        <div className="content-mt-5 content-rounded content-border">
          <div className="content-flex content-items-center content-justify-between content-rounded content-p-2">
            <div className="content-flex content-items-center">
              <FileUploader
                ref={fileUploaderRef}
                accept={ValidImageFormats.join(',')}
                placeholder="Upload image"
                onUpload={handleUpdateFiles}
              />

              <div className="content-flex content-items-center content-gap-2 content-pl-2">
                <Button
                  onClick={() =>
                    handleChangeDrawingTool(AnnotationTool.RECTANGLE)
                  }
                  className={clsx(
                    'content-flex content-size-9 content-items-center content-justify-center content-rounded-md content-p-0',
                    activeDrawingTool === AnnotationTool.RECTANGLE
                      ? '!content-bg-blue-100 content-text-blue-600'
                      : '!content-bg-transparent content-text-gray-400 hover:!content-bg-gray-100 hover:!content-text-black',
                  )}
                  title="Rectangle Tool"
                >
                  <BsSquare className="content-text-lg" />
                </Button>

                <Button
                  onClick={() =>
                    handleChangeDrawingTool(AnnotationTool.POLYGON)
                  }
                  className={clsx(
                    'content-flex content-size-9 content-items-center content-justify-center content-rounded-md content-p-0',
                    activeDrawingTool === AnnotationTool.POLYGON
                      ? '!content-bg-blue-100 content-text-blue-600'
                      : '!content-bg-transparent content-text-gray-400 hover:!content-bg-gray-100 hover:!content-text-black',
                  )}
                  title="Polygon Tool"
                >
                  <BsPencil className="content-text-lg" />
                </Button>
              </div>
            </div>
            <div className="content-flex content-items-center content-gap-2">
              <Button
                onClick={handleEraseSelectedAnnotation}
                className="content-flex content-items-center content-gap-1 content-border content-border-card-border !content-bg-transparent content-p-1.5 content-pr-2 content-text-cloudy-white"
              >
                <PiEraserFill className="content-text-lg" /> Erase
              </Button>
              <Button
                onClick={handleEraseAllAnnotations}
                className="content-flex content-items-center content-gap-1 content-border content-border-card-border !content-bg-transparent content-p-1.5 content-text-cloudy-white"
              >
                <DeleteIcon width={16} height={16} fill="#FFFFFFBF" /> Delete
                All
              </Button>
            </div>
          </div>

          <CustomImageAnnotator
            imageFile={data.imageFile}
            imageLink={data.imageLink ? FILE_PATH_PREFIX + data.imageLink : ''}
            drawingTool={activeDrawingTool}
            onInitiateAnnoter={handleInitializeAnnotator}
            onCreateAnnotation={handleCreateAnnotation}
            onUpdateAnnotation={handleUpdateAnnotation}
            onDeleteAnnotation={handleDeleteAnnotation}
          />

          {!!data.annotations?.length && (
            <div className="content-border-t content-border-gray-200 content-p-4">
              <h4 className="content-mb-3 content-font-medium">
                Answer options
              </h4>

              <div className="content-rounded content-border content-border-gray-200 content-p-3">
                <div className="content-space-y-3">
                  {data.annotations.map(annotation => {
                    const isChecked = data.correctTags?.includes(annotation.id);

                    return (
                      <div
                        key={annotation.id}
                        className="content-flex content-items-center content-rounded content-p-2 content-transition-colors"
                      >
                        <div
                          className={clsx(
                            'content-mr-3 content-flex content-size-5 content-cursor-pointer content-items-center content-justify-center content-rounded content-border',
                            isChecked
                              ? 'content-border-blue-500 content-bg-blue-500 content-text-white'
                              : 'content-border-gray-300',
                          )}
                          onClick={() => handleToggleCorrectTag(annotation.id)}
                        >
                          {isChecked && (
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              className="content-size-3.5"
                              viewBox="0 0 20 20"
                              fill="currentColor"
                            >
                              <path
                                fillRule="evenodd"
                                d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                clipRule="evenodd"
                              />
                            </svg>
                          )}
                        </div>
                        <label
                          className="content-grow content-cursor-pointer content-text-sm"
                          onClick={() => handleToggleCorrectTag(annotation.id)}
                        >
                          {annotation.bodies.find(
                            body => body.purpose === 'tagging',
                          )?.value ?? 'No Tag'}
                        </label>
                      </div>
                    );
                  })}
                </div>

                <div className="content-mt-4 content-rounded content-border content-border-blue-100 content-bg-blue-50 content-p-3 content-text-sm content-text-blue-700">
                  <div className="content-mb-1 content-font-medium">
                    Multi-select question
                  </div>
                  <p className="content-text-xs content-text-blue-600">
                    Check the boxes for correct answers. Users will be able to
                    select multiple answers.
                  </p>
                </div>
              </div>
            </div>
          )}
          {error.tags && (
            <p className="content-mt-1 content-text-sm content-text-red-500">
              {error.tags}
            </p>
          )}

          <div className="content-p-6">
            <div className="content-flex content-items-center content-gap-2">
              <button
                onClick={() => {
                  updateData({
                    ...data,
                    showAutoFeedback: !data.showAutoFeedback,
                  });
                }}
                className={clsx(
                  'content-relative content-h-5 content-w-10 content-rounded-full content-transition content-duration-300',
                  data.showAutoFeedback
                    ? 'content-bg-light-blue'
                    : 'content-bg-gray-400',
                )}
              >
                <div
                  className={clsx(
                    'content-absolute content-left-1 content-top-1 content-size-3 content-rounded-full content-bg-white content-shadow-md content-transition-transform content-duration-300',
                    data.showAutoFeedback
                      ? 'content-translate-x-5'
                      : 'content-translate-x-0',
                  )}
                />
              </button>
              <p className="content-text-sm">
                Automated Feedback (Available after submission)
              </p>
            </div>

            <div className="content-mt-3">
              <Label htmlFor="correctFeedback">Correct Answer Feedback</Label>
              <textarea
                name="correctFeedback"
                id="correctFeedback"
                value={data.correctFeedback}
                onChange={handleChangeInput}
                className="content-mt-1 content-w-full content-rounded content-border content-border-gray-400 content-bg-transparent content-px-3 content-py-2 content-text-sm focus-visible:content-outline-soft-blue-gray"
                placeholder="Enter feedback for correct answers"
              />
              {error.correctFeedback && (
                <p className="content-mt-1 content-text-sm content-text-red-500">
                  {error.correctFeedback}
                </p>
              )}
              <Label htmlFor="incorrectFeedback">
                Incorrect Answer Feedback
              </Label>
              <textarea
                name="incorrectFeedback"
                id="incorrectFeedback"
                value={data.incorrectFeedback}
                onChange={handleChangeInput}
                className="content-mt-1 content-w-full content-rounded content-border content-border-gray-400 content-bg-transparent content-px-3 content-py-2 content-text-sm focus-visible:content-outline-soft-blue-gray"
                placeholder="Enter feedback for incorrect answers"
              />
              {error.incorrectFeedback && (
                <p className="content-mt-1 content-text-sm content-text-red-500">
                  {error.incorrectFeedback}
                </p>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  },
);

export default HotSpotQuiz;
