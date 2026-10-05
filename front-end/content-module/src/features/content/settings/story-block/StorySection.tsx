import { ChangeEvent, forwardRef, Fragment, useImperativeHandle } from 'react';

import { Checkbox } from 'common/Checkbox';
import ImageCard from 'components/ImageCard';
import InputCard from 'components/InputCard';
import {
  CorrectSituation,
  IDataValidationHandle,
  IStoryBlockStoryAddProp,
} from 'models/Content';
import { StoryBlockStoryDefault } from 'utils/ContentDefaultValues';

interface IProps {
  story: IStoryBlockStoryAddProp;
  updateStories: (updatedStory: IStoryBlockStoryAddProp) => void;
  errors: {
    description: string;
    actionButtonTitle: string;
    agreeText: string;
    ignoreText: string;
  };
  updateErrors: (error: {
    description: string;
    actionButtonTitle: string;
    agreeText: string;
    ignoreText: string;
  }) => void;
}

const StorySection = forwardRef<IDataValidationHandle, IProps>(
  ({ story, errors, updateStories, updateErrors }, ref) => {
    const errorMessages = {
      description: 'Description is required',
      actionButtonTitle: 'Action Button Title is required',
      agreeText: 'Agree Alternative Text is required',
      ignoreText: 'Ignore Alternative Text is required',
    };

    const handleUpdateFeatureImage = (file: File | null) => {
      const updatedStory = {
        ...story,
        featureImage: '',
        featureImageFile: file,
      };
      updateStories(updatedStory);
    };

    const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
      const { id, value } = e.target;
      const updatedStory = {
        ...story,
        [id]: value,
      };
      updateStories(updatedStory);

      if (value.trim().length === 0) {
        updateErrors({
          ...errors,
          [id]: errorMessages[id as keyof typeof errorMessages],
        });
      } else {
        updateErrors({ ...errors, [id]: '' });
      }
    };

    const handleResetColor = (id: string) => {
      const [key] = id.split('_');

      const updatedStory = {
        ...story,
        [key]:
          StoryBlockStoryDefault[key as keyof typeof StoryBlockStoryDefault],
      };
      updateStories(updatedStory);
    };

    const handleChangeMarkdown = (value: string) => {
      if (value === '<p><br></p>') return;

      const updatedStory = {
        ...story,
        description: value,
      };
      updateStories(updatedStory);

      if (value.trim().length === 0) {
        updateErrors({
          ...errors,
          description: errorMessages['description'],
        });
      } else {
        updateErrors({
          ...errors,
          description: '',
        });
      }
    };

    const handleChangeCorrectSituation = (value: CorrectSituation) => {
      const updatedStory = {
        ...story,
        correctSituation: value,
      };
      updateStories(updatedStory);
    };

    const validateFields = () => {
      let isValid = true;

      const updatedErrors = Object.keys(errorMessages).reduce(
        (acc, key) => {
          if (
            (story[key as keyof typeof story] as string)?.trim().length === 0
          ) {
            isValid = false;
            return {
              ...acc,
              [key]: errorMessages[key as keyof typeof errorMessages],
            };
          }
          return { ...acc, [key]: '' };
        },
        {} as typeof errors,
      );

      updateErrors(updatedErrors);
      return isValid;
    };

    useImperativeHandle(ref, () => ({
      validateData: () => {
        return { success: validateFields() };
      },
    }));

    return (
      <Fragment>
        <ImageCard
          title="Feature Image"
          subTitle="Image"
          selectedFile={story?.featureImageFile}
          selectedImage={story?.featureImage}
          onUploadImage={handleUpdateFeatureImage}
        />

        <InputCard
          title="Description"
          inputFields={[
            {
              className: 'content-mt-2.5 content-w-full content-rounded',
              type: 'editor',
              id: 'description',
              value: story?.description,
              placeholder: 'Enter description',
              error: errors.description,
              onChangeEditor: handleChangeMarkdown,
            },
          ]}
        />

        <InputCard
          title="Action Button Title"
          inputFields={[
            {
              className:
                'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'actionButtonTitle',
              value: story?.actionButtonTitle,
              error: errors.actionButtonTitle,
              placeholder: 'Enter button title',
              onChange: handleChangeInput,
            },
            {
              type: 'color',
              id: 'actionButtonTitleColor',
              value: story?.actionButtonTitleColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
          ]}
        />

        <InputCard
          title="Action Button"
          inputFields={[
            {
              label: 'Text Color',
              type: 'color',
              id: 'buttonTextColor',
              value: story?.buttonTextColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
            {
              label: 'Button Color',
              type: 'color',
              id: 'buttonColor',
              value: story?.buttonColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
            {
              label: 'Button Hover Color',
              type: 'color',
              id: 'buttonHoverColor',
              value: story?.buttonHoverColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
          ]}
        />

        <h4>Correct Situation</h4>
        <div className="content-flex content-items-center content-gap-2">
          <label className="content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
            <Checkbox
              checked={story?.correctSituation === CorrectSituation.AGREE}
              onCheckedChange={() =>
                handleChangeCorrectSituation(CorrectSituation.AGREE)
              }
            />
            Agree
          </label>
        </div>
        <div className="content-flex content-items-center content-gap-2">
          <label className="content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
            <Checkbox
              checked={story?.correctSituation === CorrectSituation.IGNORE}
              onCheckedChange={() =>
                handleChangeCorrectSituation(CorrectSituation.IGNORE)
              }
            />
            Ignore
          </label>
        </div>

        <InputCard
          title="Agree Alternative Text"
          inputFields={[
            {
              className:
                'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'agreeText',
              value: story?.agreeText,
              error: errors.agreeText,
              placeholder: 'Enter agree text',
              onChange: handleChangeInput,
            },
            {
              type: 'color',
              id: 'agreeTextColor',
              value: story?.agreeTextColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
          ]}
        />

        <InputCard
          title="Ignore Alternative Text"
          inputFields={[
            {
              className:
                'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'ignoreText',
              value: story?.ignoreText,
              error: errors.ignoreText,
              placeholder: 'Enter ignore text',
              onChange: handleChangeInput,
            },
            {
              type: 'color',
              id: 'ignoreTextColor',
              value: story?.ignoreTextColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
          ]}
        />
      </Fragment>
    );
  },
);

StorySection.displayName = 'StorySection';

export default StorySection;
