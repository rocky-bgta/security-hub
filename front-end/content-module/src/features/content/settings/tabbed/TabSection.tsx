import {
  ChangeEvent,
  Dispatch,
  forwardRef,
  Fragment,
  SetStateAction,
  useImperativeHandle,
  useRef,
} from 'react';

import { Button } from 'common/Button';
import { Card } from 'common/Card';
import { Input } from 'common/Input';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import InputCard from 'components/InputCard';
import { IDataValidationHandle, ITab } from 'models/Content';
import { ValidAudioFormats } from 'utils/Constants';
import { timeToSeconds } from 'utils/Helper';

interface IProps {
  tab: ITab;
  updateTabs: (updatedTab: ITab) => void;
  errors: {
    navigationButtonText: string;
    displayTime: string;
    paragraph: string;
  };
  updateErrors: Dispatch<
    SetStateAction<{
      navigationButtonText: string;
      displayTime: string;
      paragraph: string;
    }>
  >;
  audioSeconds: number | null;
}

const TabSection = forwardRef<IDataValidationHandle, IProps>(
  ({ tab, errors, updateTabs, updateErrors, audioSeconds }, ref) => {
    const audioUploaderRef = useRef<FileUploaderHandle>(null);

    const errorMessages = {
      navigationButtonText: 'Navigation Button Text is required',
      paragraph: 'Description is required',
      displayTime: 'Display Time is required',
    };

    // const timeStringToSeconds = (time: string): number => {
    //   const [minutesStr, secondsStr] = time.split(':');

    //   const minutes = parseInt(minutesStr, 10) || 0;
    //   const seconds = parseInt(secondsStr, 10) || 0;

    //   return minutes * 60 + seconds;
    // };

    const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
      const { id, value } = e.target;
      const updatedTab = {
        ...tab,
        [id]: value,
      };

      if (id === 'displayTime') {
        const displaySeconds = timeToSeconds(value);
        // console.log('Updated Tab:', displaySeconds, audioSeconds);
        if (!isNaN(displaySeconds) && audioSeconds !== null) {
          if (displaySeconds > audioSeconds) {
            updateErrors(prev => ({
              ...prev,
              [id]: 'Display time cannot be longer than audio duration.',
            }));
            return;
          } else {
            updateErrors(prev => ({
              ...prev,
              [id]: '',
            }));
          }
        }
      }

      if (id === 'audioUrl') {
        updatedTab.audioFile = null;
        audioUploaderRef.current?.clearFiles();
      }

      updateTabs(updatedTab);

      if (value.trim().length === 0) {
        updateErrors(prev => ({
          ...prev,
          [id]: errorMessages[id as keyof typeof errorMessages],
        }));
      } else {
        updateErrors(prev => ({
          ...prev,
          [id]: '',
        }));
      }
    };

    const handleChangeMarkdown = (value: string) => {
      if (value === '<p><br></p>') return;

      const updatedTab = {
        ...tab,
        paragraph: value,
      };
      updateTabs(updatedTab);

      if (value.trim().length === 0) {
        updateErrors(prev => ({
          ...prev,
          paragraph: errorMessages['paragraph'],
        }));
      } else {
        updateErrors(prev => ({
          ...prev,
          paragraph: '',
        }));
      }
    };

    const handleOnUploadAudio = (files: FileList) => {
      const updatedTab = {
        ...tab,
        audioFile: files.length ? files[0] : null,
        audioUrl: '',
      };
      updateTabs(updatedTab);
    };

    const handleClearAudio = () => {
      audioUploaderRef.current?.clearFiles();
      updateTabs({
        ...tab,
        audioFile: null,
        audioUrl: '',
      });
    };

    const validateFields = () => {
      let isValid = true;

      const updatedErrors = Object.keys(errorMessages).reduce(
        (acc, key) => {
          if ((tab[key as keyof typeof tab] as string)?.trim().length === 0) {
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
      <div className="content-mt-5 content-w-full content-space-y-5">
        <InputCard
          title="Navigation Button"
          inputFields={[
            {
              className:
                'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'navigationButtonText',
              value: tab?.navigationButtonText,
              error: errors.navigationButtonText,
              placeholder: 'Navigation Button Text',
              onChange: handleChangeInput,
            },
          ]}
        />

        <InputCard
          title="Display Time (in seconds 00:10)"
          inputFields={[
            {
              className:
                'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'displayTime',
              value: tab?.displayTime,
              error: errors.displayTime,
              placeholder: 'Enter display seconds',
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
              value: tab?.paragraph,
              placeholder: 'Enter paragraph',
              error: errors.paragraph,
              onChangeEditor: handleChangeMarkdown,
            },
          ]}
        />

        <Card className="content-mt-5" title="Audio">
          <div className="content-px-5 content-pb-5">
            <FileUploader
              ref={audioUploaderRef}
              containerClassName="content-mb-5 content-text-secondary"
              accept={ValidAudioFormats.join(',')}
              placeholder="Upload audio"
              onUpload={handleOnUploadAudio}
            />
            <Input
              id="audioUrl"
              name="audioUrl"
              type="text"
              placeholder="Paste audio url"
              value={tab.audioUrl}
              onChange={handleChangeInput}
            />
            {tab.audioUrl || tab.audioFile ? (
              <Fragment>
                <Button
                  className="content-w-fit !content-bg-transparent content-px-0 content-text-stormy-gray content-underline"
                  onClick={handleClearAudio}
                >
                  Clear Audio
                </Button>

                <div className="content-mt-4">
                  <audio key={tab.id} controls>
                    <source
                      src={
                        tab.audioFile
                          ? URL.createObjectURL(tab.audioFile)
                          : tab.audioUrl
                      }
                      type="audio/mpeg"
                    />
                    Your browser does not support the audio element.
                  </audio>
                </div>
              </Fragment>
            ) : null}
          </div>
        </Card>
      </div>
    );
  },
);

export default TabSection;
