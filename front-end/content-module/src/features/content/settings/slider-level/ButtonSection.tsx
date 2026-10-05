import { ChangeEvent, forwardRef, Fragment, useImperativeHandle } from 'react';

import { Checkbox } from 'common/Checkbox';
import InputCard from 'components/InputCard';
import {
  CorrectAcceptance,
  IDataValidationHandle,
  ISliderLevelButtonAddProp,
} from 'models/Content';
import { SliderLevelButtonDefault } from 'utils/ContentDefaultValues';

interface IProps {
  button: ISliderLevelButtonAddProp;
  updateButtons: (updatedButton: ISliderLevelButtonAddProp) => void;
  errors: {
    navigationButtonText: string;
    acceptanceText: string;
    popupText: string;
    popupButtonText: string;
  };
  updateErrors: (error: {
    navigationButtonText: string;
    acceptanceText: string;
    popupText: string;
    popupButtonText: string;
  }) => void;
}

const ButtonSection = forwardRef<IDataValidationHandle, IProps>(
  ({ button, errors, updateButtons, updateErrors }, ref) => {
    const errorMessages = {
      acceptanceText: 'Acceptance Text is required',
      navigationButtonText: 'Navigation Button Text is required',
      popupText: 'Pop-up Text is required',
      popupButtonText: 'Pop-up Button Text is required',
    };

    const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
      const { id, value, checked } = e.target;
      const updatedButton = {
        ...button,
        [id]: id === 'openPopup' ? checked : value,
      };
      updateButtons(updatedButton);

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

      const updatedButton = {
        ...button,
        [key]:
          SliderLevelButtonDefault[
            key as keyof typeof SliderLevelButtonDefault
          ],
      };
      updateButtons(updatedButton);
    };

    const handleChangeCorrectAcceptance = (value: CorrectAcceptance) => {
      const updatedButton = {
        ...button,
        correctAcceptanceText: value,
      };
      updateButtons(updatedButton);
    };

    const validateFields = () => {
      let isValid = true;

      const updatedErrors = Object.keys(errorMessages).reduce(
        (acc, key) => {
          if (
            !button.openPopup &&
            ['popupText', 'popupButtonText'].includes(key)
          ) {
            return { ...acc, [key]: '' };
          }

          if (
            (button[key as keyof typeof button] as string)?.trim().length === 0
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
        <InputCard
          title="Navigation Button"
          inputFields={[
            {
              label: 'Text',
              type: 'text',
              id: 'navigationButtonText',
              value: button?.navigationButtonText,
              error: errors.navigationButtonText,
              placeholder: 'Enter text',
              onChange: handleChangeInput,
            },
            {
              label: 'Text Color',
              type: 'color',
              id: 'navigationButtonTextColor',
              value: button?.navigationButtonTextColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
            {
              label: 'Button Color',
              type: 'color',
              id: 'navigationButtonColor',
              value: button?.navigationButtonColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
            {
              label: 'Button Hover Color',
              type: 'color',
              id: 'navigationButtonHoverColor',
              value: button?.navigationButtonHoverColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
          ]}
        />

        <h4 className="content-text-lg content-font-semibold">
          Correct Acceptance
        </h4>
        <div className="content-flex content-items-center content-gap-2">
          <label className="content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
            <Checkbox
              checked={
                button?.correctAcceptanceText === CorrectAcceptance.ACCEPTABLE
              }
              onCheckedChange={() =>
                handleChangeCorrectAcceptance(CorrectAcceptance.ACCEPTABLE)
              }
            />
            Acceptable
          </label>
        </div>
        <div className="content-flex content-items-center content-gap-2">
          <label className="content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
            <Checkbox
              checked={
                button?.correctAcceptanceText === CorrectAcceptance.WARNING
              }
              onCheckedChange={() =>
                handleChangeCorrectAcceptance(CorrectAcceptance.WARNING)
              }
            />
            Warning
          </label>
        </div>
        <div className="content-flex content-items-center content-gap-2">
          <label className="content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
            <Checkbox
              checked={
                button?.correctAcceptanceText === CorrectAcceptance.UNACCEPTABLE
              }
              onCheckedChange={() =>
                handleChangeCorrectAcceptance(CorrectAcceptance.UNACCEPTABLE)
              }
            />
            Unacceptable
          </label>
        </div>

        <InputCard
          title="Acceptance Text"
          inputFields={[
            {
              label: 'Text',
              type: 'text',
              id: 'acceptanceText',
              value: button?.acceptanceText,
              error: errors.acceptanceText,
              placeholder: 'Enter text',
              onChange: handleChangeInput,
            },
            {
              label: 'Text Color',
              type: 'color',
              id: 'acceptanceTextColor',
              value: button?.acceptanceTextColor,
              onChange: handleChangeInput,
              onReset: handleResetColor,
            },
          ]}
        />

        <label className="content-flex content-cursor-pointer content-items-center content-gap-2 content-text-base content-font-normal">
          <Checkbox
            id="openPopup"
            onCheckedChange={checked =>
              handleChangeInput({ target: { id: 'openPopup', checked } } as any)
            }
            checked={button?.openPopup}
          />
          Open pop-up
        </label>

        {button?.openPopup && (
          <Fragment>
            <InputCard
              title="Pop-up Text"
              inputFields={[
                {
                  className:
                    'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
                  type: 'text',
                  id: 'popupText',
                  value: button?.popupText,
                  error: errors.popupText,
                  placeholder: 'Enter popup text',
                  onChange: handleChangeInput,
                },
                {
                  type: 'color',
                  id: 'popupTextColor',
                  value: button?.popupTextColor,
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
                  value: button?.popupButtonText,
                  error: errors.popupButtonText,
                  placeholder: 'Enter text',
                  onChange: handleChangeInput,
                },
                {
                  label: 'Text Color',
                  type: 'color',
                  id: 'popupButtonTextColor',
                  value: button?.popupButtonTextColor,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
                {
                  label: 'Button Color',
                  type: 'color',
                  id: 'popupButtonColor',
                  value: button?.popupButtonColor,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
                {
                  label: 'Hover Color',
                  type: 'color',
                  id: 'popupButtonHoverColor',
                  value: button?.popupButtonHoverColor,
                  onChange: handleChangeInput,
                  onReset: handleResetColor,
                },
              ]}
            />
          </Fragment>
        )}
      </Fragment>
    );
  },
);

ButtonSection.displayName = 'ButtonSection';

export default ButtonSection;
