import {
  Annotorious,
  AnnotoriousImageAnnotator,
  ImageAnnotation,
  ImageAnnotationPopup,
  ImageAnnotator,
  PopupProps,
  useAnnotator,
} from '@annotorious/react';
import { ChangeEvent, useCallback, useEffect, useState } from 'react';

import '@annotorious/react/annotorious-react.css';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { cn } from 'utils/Helper';

export type TAnnoter = AnnotoriousImageAnnotator<
  ImageAnnotation,
  ImageAnnotation
>;

interface IProps {
  image?: string;
  imageFile?: File | null;
  imageLink?: string;
  onInitiateAnnoter?: (annoter: TAnnoter) => void;
  onCreateAnnotation?: (annotation: ImageAnnotation) => void;
  onUpdateAnnotation?: (
    annotation: ImageAnnotation,
    previous: ImageAnnotation,
  ) => void;
  onDeleteAnnotation?: (annotation: ImageAnnotation) => void;
  drawingTool?: 'rectangle' | 'polygon';
}

const AnnotatableImage = ({
  image,
  drawingTool,
  onInitiateAnnoter,
  onCreateAnnotation,
  onUpdateAnnotation,
  onDeleteAnnotation,
}: IProps) => {
  const annotator = useAnnotator<AnnotoriousImageAnnotator>();

  // Handle annotation creation
  const handleCreateAnnotation = useCallback(
    (annotation: ImageAnnotation) => {
      onCreateAnnotation?.(annotation);
    },
    [onCreateAnnotation],
  );

  // Handle annotation updates
  const handleUpdateAnnotation = useCallback(
    (annotation: ImageAnnotation, previous: ImageAnnotation) => {
      onUpdateAnnotation?.(annotation, previous);
    },
    [onUpdateAnnotation],
  );

  // Handle annotation deletion
  const handleDeleteAnnotation = useCallback(
    (annotation: ImageAnnotation) => {
      onDeleteAnnotation?.(annotation);
    },
    [onDeleteAnnotation],
  );

  // Set up event listeners
  useEffect(() => {
    if (!annotator) return;

    onInitiateAnnoter?.(annotator);

    // Add event listeners
    annotator.on('createAnnotation', handleCreateAnnotation);
    annotator.on('updateAnnotation', handleUpdateAnnotation);
    annotator.on('deleteAnnotation', handleDeleteAnnotation);

    // Cleanup function
    return () => {
      annotator.off('createAnnotation', handleCreateAnnotation);
      annotator.off('updateAnnotation', handleUpdateAnnotation);
      annotator.off('deleteAnnotation', handleDeleteAnnotation);
    };
  }, [
    annotator,
    handleCreateAnnotation,
    handleUpdateAnnotation,
    handleDeleteAnnotation,
  ]);

  return (
    <div className="content-relative">
      {image ? (
        <ImageAnnotator tool={drawingTool} drawingEnabled={true}>
          <img
            src={image}
            alt="Annotatable image"
            className="content-h-auto content-max-w-full"
          />
        </ImageAnnotator>
      ) : (
        <div className="content-flex content-h-64 content-items-center content-justify-center content-text-gray-500">
          Please upload an image to annotate
        </div>
      )}
    </div>
  );
};

const CommentPopup = ({
  annotation,
  onCreateBody,
  onUpdateBody,
}: PopupProps) => {
  const [comment, setComment] = useState<string>('');
  const [tag, setTag] = useState<string>('');
  const [error, setError] = useState<string>('');
  const [buttonText, setButtonText] = useState<string>('Save');

  // Add CSS to parent div via a side effect
  useEffect(() => {
    // Find the parent popup element and add z-index
    const popupElement = document.querySelector(
      '.a9s-image-popup',
    ) as HTMLElement;
    if (popupElement) {
      popupElement.style.zIndex = '10';
    }
  }, []);

  useEffect(() => {
    // Get existing comment body
    const commentBody = annotation.bodies.find(
      body => body.purpose === 'commenting',
    );
    setComment(commentBody?.value || '');

    // Get existing tag body
    const tagBody = annotation.bodies.find(body => body.purpose === 'tagging');
    setTag(tagBody?.value || '');
  }, [annotation.bodies]);

  const handleChangeInput = (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => {
    const { id, value } = e.target;
    if (id === 'tag') {
      setTag(value);
      setError(value.trim() ? '' : 'Please enter a tag for this area');
    } else {
      setComment(value);
    }
  };

  const handleClickSave = () => {
    // Validate - require a tag
    if (!tag.trim()) {
      setError('Please enter a tag for this area');
      return;
    }

    setError('');

    // Update or create comment body
    const commentData = {
      purpose: 'commenting',
      value: comment,
    };

    const commentBody = annotation.bodies.find(
      body => body.purpose === 'commenting',
    );

    if (commentBody) {
      onUpdateBody(commentBody, commentData);
    } else {
      onCreateBody(commentData);
    }

    // Update or create tag body - without is_correct property
    if (tag) {
      const tagData = {
        purpose: 'tagging',
        value: tag,
      };

      const tagBody = annotation.bodies.find(
        body => body.purpose === 'tagging',
      );

      if (tagBody) {
        onUpdateBody(tagBody, tagData);
      } else {
        onCreateBody(tagData);
      }
    }

    setButtonText('Saved!');

    setTimeout(() => {
      setButtonText('Save');
    }, 1500);
  };

  return (
    <div className="content-min-w-64 content-rounded content-bg-gradient-to-t content-from-[#12151E] content-to-[#324650] content-p-3 content-shadow-md">
      <div className="content-mb-3">
        <Label className="content-mb-1 content-block content-text-sm content-font-medium">
          Tag this area: <span className="content-text-red-500">*</span>
        </Label>
        <Input
          type="text"
          id="tag"
          name="tag"
          value={tag}
          onChange={handleChangeInput}
          className={cn(
            'content-w-full content-rounded content-border content-p-2 content-text-white',
            { 'content-has-error': error },
          )}
          placeholder="Enter a tag for this area (e.g., Bird, Animal)"
        />
        <p
          className={cn(
            'content-mt-1 content-text-xs',
            error ? 'content-text-red-500' : 'content-text-gray-500',
          )}
        >
          {!error ? 'Tag will be used as an answer option' : error}
        </p>
      </div>

      <div className="content-mb-3">
        <Label className="content-mb-1 content-block content-text-sm content-font-medium">
          Comment (optional):
        </Label>
        <textarea
          value={comment}
          onChange={handleChangeInput}
          className="content-w-full content-rounded content-border content-bg-transparent content-p-2 content-text-sm focus-visible:content-outline-soft-blue-gray"
          rows={3}
          placeholder="Add any additional information about this area"
        />
      </div>

      <div className="content-flex content-justify-end content-gap-2">
        <Button
          onClick={handleClickSave}
          className="content-rounded content-bg-blue-500 content-px-3 content-py-1 content-text-sm content-text-white"
        >
          {buttonText}
        </Button>
      </div>
    </div>
  );
};

export const CustomImageAnnotator = ({
  imageFile,
  imageLink = '',
  drawingTool = 'rectangle',
  onInitiateAnnoter,
  onCreateAnnotation,
  onUpdateAnnotation,
  onDeleteAnnotation,
}: IProps) => {
  return (
    <Annotorious>
      <AnnotatableImage
        image={imageFile ? URL.createObjectURL(imageFile) : imageLink}
        drawingTool={drawingTool}
        onInitiateAnnoter={onInitiateAnnoter}
        onCreateAnnotation={onCreateAnnotation}
        onUpdateAnnotation={onUpdateAnnotation}
        onDeleteAnnotation={onDeleteAnnotation}
      />

      <ImageAnnotationPopup
        popup={(props: PopupProps) => <CommentPopup {...props} />}
      />
    </Annotorious>
  );
};
