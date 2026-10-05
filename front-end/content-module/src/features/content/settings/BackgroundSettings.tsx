import { ChangeEvent, Dispatch, SetStateAction } from 'react';

import { Card } from 'common/Card';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import ImageCard from 'components/ImageCard';
import InputCard from 'components/InputCard';
import { IBackgroundSettings, Tone } from 'models/Content';
import { ISelectOption } from 'models/Input';
import { BackgroundFormattingDefault } from 'utils/ContentDefaultValues';

interface IProps {
  backgroundSettings: IBackgroundSettings;
  updateBackgroundSettings: Dispatch<SetStateAction<IBackgroundSettings>>;
}

const Dropdowns: {
  [x: string]: Array<ISelectOption>;
} = {
  opacity: [
    { id: '10', label: '10%', value: '10' },
    { id: '20', label: '20%', value: '20' },
    { id: '30', label: '30%', value: '30' },
    { id: '40', label: '40%', value: '40' },
    { id: '50', label: '50%', value: '50' },
    { id: '60', label: '60%', value: '60' },
    { id: '70', label: '70%', value: '70' },
    { id: '80', label: '80%', value: '80' },
    { id: '90', label: '90%', value: '90' },
    { id: '100', label: '100%', value: '100' },
  ],
  tone: [
    { id: '1', label: 'Dark', value: Tone.DARK },
    { id: '2', label: 'Light', value: Tone.LIGHT },
  ],
};

const BackgroundSettings = ({
  backgroundSettings,
  updateBackgroundSettings,
}: IProps) => {
  const handleChangeInput = (
    e: ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { id, value } = e.target;
    updateBackgroundSettings(prev => ({
      ...prev,
      [id]: value,
    }));
  };

  const handleResetColor = (id: string) => {
    updateBackgroundSettings(prev => ({
      ...prev,
      [id]:
        id === 'textColor'
          ? BackgroundFormattingDefault.backgroundFormatting.textColor
          : BackgroundFormattingDefault.backgroundFormatting.backgroundColor,
    }));
  };

  const handleUpdateImage = (file: File | null) => {
    updateBackgroundSettings(prev => ({
      ...prev,
      backgroundImage: '',
      selectedFile: file,
    }));
  };

  return (
    <div className="content-mt-5 content-flex content-flex-col content-gap-y-5">
      <InputCard
        title="Text Color"
        inputFields={[
          {
            type: 'color',
            id: 'textColor',
            value: backgroundSettings.textColor,
            onChange: handleChangeInput,
            onReset: handleResetColor,
          },
        ]}
      />

      <InputCard
        title="Background Color"
        inputFields={[
          {
            type: 'color',
            id: 'backgroundColor',
            value: backgroundSettings.backgroundColor,
            onChange: handleChangeInput,
            onReset: handleResetColor,
          },
        ]}
      />

      <ImageCard
        title="Image"
        subTitle="Change the background image and opacity."
        selectedImage={backgroundSettings.backgroundImage}
        selectedFile={backgroundSettings.selectedFile}
        onUploadImage={handleUpdateImage}
      >
        <div className="content-mt-7 content-flex content-flex-col content-gap-y-3">
          <p className="content-text-white">Background image opacity</p>
          <Select
            value={backgroundSettings.backgroundOpacity}
            onValueChange={value =>
              updateBackgroundSettings(prev => ({
                ...prev,
                backgroundOpacity: value,
              }))
            }
          >
            <SelectTrigger disabled={false}>
              <SelectValue placeholder="Select Background Opacity" />
            </SelectTrigger>
            <SelectContent>
              {Dropdowns.opacity.map(option => (
                <SelectItem key={option.id} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </ImageCard>

      <Card title="Tone">
        <div className="content-p-5 content-pt-2.5">
          <Select
            value={backgroundSettings.tone}
            onValueChange={value =>
              updateBackgroundSettings(prev => ({
                ...prev,
                tone: value as Tone,
              }))
            }
          >
            <SelectTrigger disabled={false}>
              <SelectValue placeholder="Select Tone" />
            </SelectTrigger>
            <SelectContent>
              {Dropdowns.tone.map(option => (
                <SelectItem key={option.id} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <p className="content-pt-3 content-text-stormy-gray">
            Choose Dark when the block&apos;s background color is darker then
            it&apos;s foreground and Light when the background color is lighter
            then the foreground.
          </p>
        </div>
      </Card>
    </div>
  );
};

export default BackgroundSettings;
