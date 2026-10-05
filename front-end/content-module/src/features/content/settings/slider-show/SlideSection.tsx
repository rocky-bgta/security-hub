import {
  ChangeEvent,
  Dispatch,
  forwardRef,
  SetStateAction,
  useImperativeHandle,
} from 'react';

import ImageCard from 'components/ImageCard';
import InputCard from 'components/InputCard';
import { IDataValidationHandle, ISlide } from 'models/Content';

interface IProps {
  slide: ISlide;
  updateSlides: (updatedSlide: ISlide) => void;
  errors: {
    title: string;
    subtitle: string;
    paragraph: string;
  };
  updateErrors: Dispatch<
    SetStateAction<{
      title: string;
      subtitle: string;
      paragraph: string;
    }>
  >;
}

const SlideSection = forwardRef<IDataValidationHandle, IProps>(
  ({ slide, errors, updateSlides, updateErrors }, ref) => {
    const errorMessages = {
      title: 'Title is required',
      subtitle: 'Subtitle is required',
      paragraph: 'Paragraph is required',
    };

    const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
      const { id, value, checked } = e.target;
      const [key, field] = id.split('_');

      let updatedSlide = { ...slide };

      switch (key) {
        case 'titleFormatting':
          updatedSlide = {
            ...slide,
            titleFormatting: {
              ...slide.titleFormatting,
              [field]: field === 'titleHighContrastMode' ? checked : value,
            },
          };
          break;
        case 'subTitleFormatting':
          updatedSlide = {
            ...slide,
            subTitleFormatting: {
              ...slide.subTitleFormatting,
              [field]: field === 'subtitleHighContrastMode' ? checked : value,
            },
          };
          break;
      }

      updateSlides(updatedSlide);

      if (value.trim().length === 0) {
        updateErrors({
          ...errors,
          [field]: errorMessages[field as keyof typeof errorMessages],
        });
      } else {
        updateErrors({
          ...errors,
          [field]: '',
        });
      }
    };

    const handleResetColor = (id: string) => {
      const [key, field] = id.split('_');

      let updateSlide = { ...slide };

      switch (key) {
        case 'titleFormatting':
          updateSlide = {
            ...slide,
            titleFormatting: {
              ...slide.titleFormatting,
              [field]: '#ffffff',
            },
          };
          break;
        case 'subTitleFormatting':
          updateSlide = {
            ...slide,
            subTitleFormatting: {
              ...slide.subTitleFormatting,
              [field]: '#ffffff',
            },
          };
          break;
      }

      updateSlides(updateSlide);
    };

    const handleChangeMarkdown = (value: string) => {
      if (value === '<p><br></p>') return;

      const updatedSlide = {
        ...slide,
        paragraphFormatting: {
          ...slide.paragraphFormatting,
          paragraph: value,
        },
      };

      updateSlides(updatedSlide);

      if (value.trim().length === 0) {
        updateErrors({
          ...errors,
          paragraph: errorMessages['paragraph'],
        });
      } else {
        updateErrors({
          ...errors,
          paragraph: '',
        });
      }
    };

    const handleUpdateFeatureImage = (file: File | null) => {
      const updatedSlide = {
        ...slide,
        featureImageLink: '',
        featureImageFile: file,
      };

      updateSlides(updatedSlide);
    };

    const validateFields = () => {
      let isValid = true;

      const updatedErrors = { ...errors };
      if (slide.titleFormatting.title.trim().length === 0) {
        isValid = false;
        updatedErrors.title = errorMessages.title;
      }
      if (slide.subTitleFormatting.subtitle.trim().length === 0) {
        isValid = false;
        updatedErrors.subtitle = errorMessages.subtitle;
      }
      if (slide.paragraphFormatting.paragraph.trim().length === 0) {
        isValid = false;
        updatedErrors.paragraph = errorMessages.paragraph;
      }

      updateErrors(updatedErrors);
      return isValid;
    };

    useImperativeHandle(ref, () => ({
      validateData: () => {
        return { success: validateFields() };
      },
    }));

    return (
      <div className="content-mt-5 content-space-y-5">
        <InputCard
          title="Title"
          inputFields={[
            {
              className:
                'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'titleFormatting_title',
              value: slide?.titleFormatting.title,
              error: errors.title,
              placeholder: 'Enter title',
              onChange: handleChangeInput,
            },
            {
              type: 'color',
              id: 'titleFormatting_titleColor',
              value: slide?.titleFormatting.titleColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
            {
              type: 'checkbox',
              id: 'titleFormatting_titleHighContrastMode',
              checked: slide?.titleFormatting.titleHighContrastMode,
              label: 'High Contrast Mode',
              onChange: handleChangeInput,
            },
          ]}
        />

        <InputCard
          title="Subtitle"
          inputFields={[
            {
              className:
                'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'subTitleFormatting_subtitle',
              value: slide?.subTitleFormatting.subtitle,
              error: errors.subtitle,
              placeholder: 'Enter subtitle',
              onChange: handleChangeInput,
            },
            {
              type: 'color',
              id: 'subTitleFormatting_subtitleColor',
              value: slide?.subTitleFormatting.subtitleColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
            {
              type: 'checkbox',
              id: 'subTitleFormatting_subtitleHighContrastMode',
              checked: slide?.subTitleFormatting.subtitleHighContrastMode,
              label: 'High Contrast Mode',
              onChange: handleChangeInput,
            },
          ]}
        />

        <InputCard
          title="Paragraph"
          inputFields={[
            {
              className: 'content-mt-2.5 content-w-full content-rounded',
              type: 'editor',
              id: 'paragraph',
              value: slide?.paragraphFormatting.paragraph,
              error: errors.paragraph,
              placeholder: 'Enter paragraph',
              onChangeEditor: handleChangeMarkdown,
            },
          ]}
        />

        <ImageCard
          title="Feature Image"
          subTitle="Image"
          selectedFile={slide?.featureImageFile}
          selectedImage={slide?.featureImageLink}
          onUploadImage={handleUpdateFeatureImage}
        />
      </div>
    );
  },
);

export default SlideSection;
