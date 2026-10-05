import {
  ChangeEvent,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useState,
} from 'react';

import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import TextEditor from 'common/TextEditor';
import InputCard from 'components/InputCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IMarkdownContent,
} from 'models/Content';
import { TabNames } from 'models/Tab';
import { BackgroundFormattingDefault } from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const MarkdownBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<IMarkdownContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [markdown, setMarkdown] = useState<{
    text: string;
    error: string;
  }>({
    text: '',
    error: '',
  });
  const [input, setInput] = useState<{
    value: string;
    error: string;
  }>({
    value: '',
    error: '',
  });

  const [backgroundSettings, setBackgroundSettings] =
    useState<IBackgroundSettings>({
      ...BackgroundFormattingDefault.backgroundFormatting,
      selectedFile: null,
    });

  useEffect(() => {
    if (content?.specific) {
      setInput({
        value: content.common.contentName,
        error: '',
      });
      setMarkdown(prev => ({ ...prev, text: content.specific.markdownText }));

      setBackgroundSettings(prev => ({
        ...prev,
        ...content.specific.backgroundFormatting,
        backgroundOpacity:
          content.specific.backgroundFormatting.backgroundOpacity + '',
      }));
    }
  }, [content]);

  const handleChangeEditor = (value: string) => {
    if (value === '<p><br></p>') return;
    setMarkdown({
      ...markdown,
      text: value,
      error: value.trim().length ? '' : 'Text is required',
    });
  };

  const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    setInput({
      value,
      error: value.trim().length === 0 ? 'Content name is required' : '',
    });
  };

  const validateFields = () => {
    const updatedMarkdown = {
      ...markdown,
      error: markdown.text.trim().length ? '' : 'Text is required',
    };
    setMarkdown(updatedMarkdown);
    return updatedMarkdown.error === '';
  };

  useImperativeHandle(ref, () => ({
    validateAndGetData: () => {
      const isValid = validateFields();
      if (!isValid) {
        return { success: false };
      }

      return {
        success: true,
        data: formatContentDataBasedOnType(
          {
            id: content.id!,
            contentName: input.value,
            updateContent: true,
            backgroundSettings,
            markdownContent: {
              markdown: markdown.text,
            },
          },
          ContentTypes.MARKDOWN,
        ),
      };
    },
  }));

  return (
    <Tabs
      value={activeTab}
      onValueChange={activeTab => {
        if (validateFields()) setActiveTab(activeTab as TabNames);
      }}
    >
      <TabsList>
        {TabOptions.map(item => (
          <TabsTrigger key={item.value} value={item.value}>
            {item.label}
          </TabsTrigger>
        ))}
      </TabsList>

      <TabsContent value={TabNames.SETTING}>
        {!isPartofInteractiveContent && (
          <InputCard
            title="Content Name"
            inputFields={[
              {
                className:
                  'content-mt-2 content-w-full content-rounded content-border content-px-3 content-py-2',
                type: 'text',
                id: 'contentName',
                value: input.value,
                error: input.error,
                placeholder: 'Enter content name',
                onChange: handleChangeInput,
              },
            ]}
          />
        )}

        <div className="content-my-5">
          <InputCard title="Markdown" inputFields={[]}>
            <p className="content-text-sm content-text-[#2E384D]">
              To embed videos hosted on YouTube or Vimeo or azure blob and
              others, simply paste the video's full URL below.
            </p>

            <TextEditor value={markdown.text} onChange={handleChangeEditor} />
            {markdown.error && (
              <p className="content-text-sm content-text-red-500">
                {markdown.error}
              </p>
            )}
          </InputCard>
        </div>
      </TabsContent>
      <TabsContent value={TabNames.BACKGROUND}>
        <BackgroundSettings
          backgroundSettings={backgroundSettings}
          updateBackgroundSettings={setBackgroundSettings}
        />
      </TabsContent>
    </Tabs>
  );
});

MarkdownBased.displayName = 'MarkdownBased';

export default MarkdownBased;
