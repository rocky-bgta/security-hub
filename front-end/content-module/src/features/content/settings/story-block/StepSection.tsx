import { ChangeEvent, forwardRef, Fragment, useImperativeHandle } from 'react';

import { Checkbox } from 'common/Checkbox';
import ImageCard from 'components/ImageCard';
import InputCard from 'components/InputCard';
import { IDataValidationHandle, IStoryBlockStepAddProp } from 'models/Content';
import { StoryBlockStepDefault } from 'utils/ContentDefaultValues';

interface IProps {
  step: IStoryBlockStepAddProp;
  errors: {
    title: string;
    description: string;
    advisorInstructionText: string;
    popupText: string;
    popupButtonText: string;
  };
  updateSteps: (updatedStep: IStoryBlockStepAddProp) => void;
  updateErrors: (error: {
    title: string;
    description: string;
    advisorInstructionText: string;
    popupText: string;
    popupButtonText: string;
  }) => void;
}

const StepSection = forwardRef<IDataValidationHandle, IProps>(
  ({ step, errors, updateSteps, updateErrors }, ref) => {
    const errorMessages = {
      title: 'Title is required',
      description: 'Description is required',
      advisorInstructionText: 'Advisor Instruction Text is required',
      popupText: 'Pop-up Text is required',
      popupButtonText: 'Pop-up Button Text is required',
    };

    const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
      const { id, value, checked } = e.target;
      const updatedStep = {
        ...step,
        [id]: id === 'openPopup' ? checked : value,
      };
      updateSteps(updatedStep);

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

      const updatedStep = {
        ...step,
        [key]: StoryBlockStepDefault[key as keyof typeof StoryBlockStepDefault],
      };
      updateSteps(updatedStep);
    };

    const handleChangeMarkdown = (value: string) => {
      if (value === '<p><br></p>') return;

      const updatedStep = {
        ...step,
        description: value,
      };
      updateSteps(updatedStep);

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

    const handleUpdateFeatureImage = (file: File | null) => {
      const updatedStep = {
        ...step,
        featureImage: '',
        featureImageFile: file,
      };
      updateSteps(updatedStep);
    };

    const validateFields = () => {
      let isValid = true;

      const updatedErrors = Object.keys(errorMessages).reduce(
        (acc, key) => {
          if (
            !step.openPopup &&
            ['popupText', 'popupButtonText'].includes(key)
          ) {
            return { ...acc, [key]: '' };
          }

          if ((step[key as keyof typeof step] as string)?.trim().length === 0) {
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
        <InputCard
          title="Title"
          inputFields={[
            {
              className: 'mt-2.5 w-full rounded border px-3 py-2',
              type: 'text',
              id: 'title',
              value: step?.title,
              error: errors.title,
              placeholder: 'Enter title',
              onChange: handleChangeInput,
            },
            {
              type: 'color',
              id: 'titleColor',
              value: step?.titleColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
          ]}
        />

        <ImageCard
          title="Feature Image"
          subTitle="Image"
          selectedFile={step?.featureImageFile}
          selectedImage={step?.featureImage}
          onUploadImage={handleUpdateFeatureImage}
        />

        <label className="content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
          <Checkbox
            id="openPopup"
            onCheckedChange={checked =>
              handleChangeInput({ target: { id: 'openPopup', checked } } as any)
            }
            checked={step?.openPopup}
          />
          Open pop-up
        </label>

        {step?.openPopup && (
          <Fragment>
            <InputCard
              title="Pop-up Text"
              inputFields={[
                {
                  className:
                    'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
                  type: 'text',
                  id: 'popupText',
                  value: step?.popupText,
                  error: errors.popupText,
                  placeholder: 'Enter popup text',
                  onChange: handleChangeInput,
                },
                {
                  type: 'color',
                  id: 'popupTextColor',
                  value: step?.popupTextColor,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
              ]}
            />

            <InputCard
              title="Pop-up Button"
              inputFields={[
                {
                  label: 'Text',
                  className:
                    'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
                  type: 'text',
                  id: 'popupButtonText',
                  value: step?.popupButtonText,
                  error: errors.popupButtonText,
                  placeholder: 'Enter text',
                  onChange: handleChangeInput,
                },
                {
                  label: 'Text Color',
                  type: 'color',
                  id: 'popupButtonTextColor',
                  value: step?.popupButtonTextColor,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
                {
                  label: 'Button Color',
                  type: 'color',
                  id: 'popupButtonColor',
                  value: step?.popupButtonColor,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
                {
                  label: 'Hover Color',
                  type: 'color',
                  id: 'popupButtonHoverColor',
                  value: step?.popupButtonHoverColor,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
              ]}
            />
          </Fragment>
        )}

        <InputCard
          title="Description"
          inputFields={[
            {
              className: 'content-mt-2.5 content-w-full content-rounded',
              type: 'editor',
              id: 'description',
              value: step?.description,
              error: errors.description,
              placeholder: 'Enter description',
              onChangeEditor: handleChangeMarkdown,
            },
          ]}
        />

        <InputCard
          title="Advisor Instruction Text"
          inputFields={[
            {
              className:
                'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'advisorInstructionText',
              value: step?.advisorInstructionText,
              error: errors.advisorInstructionText,
              placeholder: 'Enter advisor instruction text',
              onChange: handleChangeInput,
            },
            {
              type: 'color',
              id: 'advisorInstructionTextColor',
              value: step?.advisorInstructionTextColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
          ]}
        />
      </Fragment>
    );
  },
);

StepSection.displayName = 'StepSection';

export default StepSection;
