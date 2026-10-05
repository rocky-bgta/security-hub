import {
  ChangeEvent,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';

import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import InputCard from 'components/InputCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import InteractiveContentSection from 'features/content/settings/interactive/ContentSection';
import {
  ContentTypes,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IInteractiveContent,
  IInteractiveGeneralContent,
} from 'models/Content';
import { TabNames } from 'models/Tab';
import { BackgroundFormattingDefault } from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const InteractiveContentBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<IInteractiveGeneralContent>
>(({ content }, ref) => {
  const contentSectionRef = useRef<IContentBlockHandle | null>(null);

  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [contentName, setContentName] = useState<{
    text: string;
    error: string;
  }>({
    text: '',
    error: '',
  });
  const [contents, setContents] = useState<Array<IInteractiveContent>>([]);
  const [selectedContentId, setSelectedContentId] = useState<string>('-1');

  const [backgroundSettings, setBackgroundSettings] =
    useState<IBackgroundSettings>({
      ...BackgroundFormattingDefault.backgroundFormatting,
      selectedFile: null,
    });

  useEffect(() => {
    if (content?.specific) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setContentName({
        ...contentName,
        text: content.common.contentName,
      });

      setBackgroundSettings(prev => ({
        ...prev,
        ...content.specific.backgroundFormatting,
        backgroundOpacity:
          content.specific.backgroundFormatting.backgroundOpacity + '',
      }));

      if (content.specific.contentList?.length) {
        const tempContents = content.specific.contentList.map(content => ({
          id: content.contentBody.id,
          contentType: content.contentType,
          contentBody: content.contentBody,
          time: content.time,
          canSkip: content.canSkip,
          isDefault: content.isDefault,
        }));
        setContents(tempContents);
        setSelectedContentId(tempContents[0].id ?? '-1');
      }
    }
  }, [content, contentName]);

  const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;

    setContentName({
      text: value,
      error: value.trim().length === 0 ? 'Content name is required' : '',
    });
  };

  const handleTabChange = (activeTab: TabNames) => {
    if (!validateFields()) return;

    if (contentSectionRef?.current) {
      const payload = contentSectionRef?.current?.validateAndGetData();

      if (!payload.success) {
        return;
      }

      const updatedContents = contents.map(content =>
        content.id === payload.data!.id
          ? {
              ...content,
              contentBody: { ...content.contentBody, ...payload.data! },
            }
          : content,
      );
      setContents(updatedContents);
    }

    setActiveTab(activeTab);
  };

  const validateFields = () => {
    let isValid = true;

    if (contentName.text.trim().length === 0) {
      isValid = false;
      setContentName({
        ...contentName,
        error: 'Content name is required',
      });
    }

    return isValid;
  };

  useImperativeHandle(ref, () => ({
    validateAndGetData: () => {
      if (!validateFields()) {
        return { success: false };
      }

      if (!contentSectionRef?.current) {
        return {
          success: true,
          data: formatContentDataBasedOnType(
            {
              id: content.id!,
              contentName: contentName.text,
              updateContent: true,
              backgroundSettings,
              interactiveContent: contents,
            },
            ContentTypes.INTERACTIVE_CONTENT,
          ),
        };
      }

      const payload = contentSectionRef?.current?.validateAndGetData();

      if (!payload.success) {
        return { success: false };
      }

      if (!payload.data) {
        return {
          success: true,
          data: formatContentDataBasedOnType(
            {
              id: content.id!,
              contentName: contentName.text,
              updateContent: true,
              backgroundSettings,
              interactiveContent: contents,
            },
            ContentTypes.INTERACTIVE_CONTENT,
          ),
        };
      }

      const { contentName: payloadContentName, ...rest } = payload.data;
      const updatedContents = contents.map(content =>
        content.id === payload.data!.id
          ? {
              ...content,
              contentBody: {
                ...content.contentBody,
                ...rest,
                common: {
                  ...content.contentBody.common,
                  contentName: payloadContentName,
                },
              },
            }
          : content,
      );

      setContents(updatedContents);

      return {
        success: true,
        data: formatContentDataBasedOnType(
          {
            id: content.id!,
            contentName: contentName.text,
            updateContent: true,
            backgroundSettings,
            interactiveContent: updatedContents,
          },
          ContentTypes.INTERACTIVE_CONTENT,
        ),
      };
    },
  }));

  return (
    <Tabs
      value={activeTab}
      onValueChange={activeTab => handleTabChange(activeTab as TabNames)}
    >
      <TabsList>
        {TabOptions.map(item => (
          <TabsTrigger key={item.value} value={item.value}>
            {item.label}
          </TabsTrigger>
        ))}
      </TabsList>

      <TabsContent value={TabNames.SETTING}>
        <InputCard
          title="Content Name"
          inputFields={[
            {
              className:
                'content-mt-2 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'contentName',
              value: contentName.text,
              error: contentName.error,
              placeholder: 'Enter content name',
              onChange: handleChangeInput,
            },
          ]}
        />

        <InteractiveContentSection
          ref={contentSectionRef}
          type={ContentTypes.INTERACTIVE_CONTENT}
          contents={contents}
          setContents={setContents}
          selectedContentId={selectedContentId}
          setSelectedContentId={setSelectedContentId}
        />
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

InteractiveContentBased.displayName = 'InteractiveContentBased';

export default InteractiveContentBased;
