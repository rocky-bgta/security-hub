import {
  ChangeEvent,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import InputCard from 'components/InputCard';
import BackgroundSettings from 'features/content/settings/BackgroundSettings';
import HotSpotQuiz from 'features/content/settings/quiz/HotSpot';
import LikerSelect from 'features/content/settings/quiz/LikerSelect';
import MatchingQuiz from 'features/content/settings/quiz/MatchingQuiz';
import OrderingQuiz from 'features/content/settings/quiz/OrderingQuiz';
import PasswordComplianceSettings from 'features/content/settings/quiz/PasswordCompliance';
import PhishingDetectionSettings from 'features/content/settings/quiz/PhishingDetection';
import PicturesQuiz from 'features/content/settings/quiz/Pictures';
import RansomwareSimulatorSettings from 'features/content/settings/quiz/RansomwareSimulator';
import ScenarioQuiz from 'features/content/settings/quiz/Scenario';
import {
  ContentTypes,
  DEFAULT_PHISHING_TIME_LIMIT,
  IBackgroundSettings,
  IContentBlockHandle,
  IContentBlockSettings,
  IDataValidationHandle,
  IQuizContent,
  IQuizFields,
  QuizTypes,
} from 'models/Content';
import { TabNames } from 'models/Tab';
import {
  BackgroundFormattingDefault,
  QuizFieldDefault,
} from 'utils/ContentDefaultValues';
import { formatContentDataBasedOnType } from 'utils/Formatter';

const QuizOptions = [
  { id: 1, label: 'Matching', value: QuizTypes.MATCHING },
  { id: 2, label: 'Ordering/Sequencing Answer', value: QuizTypes.ORDERING },
  { id: 3, label: 'Liker Scale Question', value: QuizTypes.LIKER },
  { id: 4, label: 'Picture Choice', value: QuizTypes.PICTURE },
  { id: 5, label: 'Scenario-based/Case Study', value: QuizTypes.SCENARIO },
  { id: 6, label: 'Hot spot Quiz', value: QuizTypes.HOT_SPOT },
  { id: 7, label: 'Ransomware Simulator Quiz', value: QuizTypes.RANSOMWARE_SIMULATOR },
  { id: 8, label: 'Password Compliance Quiz', value: QuizTypes.PASSWORD_COMPLIANCE },
  { id: 9, label: 'Phishing Detection Quiz', value: QuizTypes.PHISHING_DETECTION },
];

const TabOptions = [
  { label: 'Setting', value: TabNames.SETTING },
  { label: 'Background', value: TabNames.BACKGROUND },
];

const QuizBased = forwardRef<
  IContentBlockHandle,
  IContentBlockSettings<IQuizContent>
>(({ content, isPartofInteractiveContent = false }, ref) => {
  const selectedContentRef = useRef<IDataValidationHandle>(null);

  const [activeTab, setActiveTab] = useState<TabNames>(TabNames.SETTING);

  const [contentName, setContentName] = useState<{
    text: string;
    error: string;
  }>({
    text: '',
    error: '',
  });
  const [quizData, setQuizData] = useState<IQuizFields>({
    ...QuizFieldDefault,
  });

  const [backgroundSettings, setBackgroundSettings] =
    useState<IBackgroundSettings>({
      ...BackgroundFormattingDefault.backgroundFormatting,
      selectedFile: null,
    });

  useEffect(() => {
    if (content?.specific) {
      setContentName({
        text: content.common.contentName,
        error: '',
      });

      setQuizData(prev => ({
        ...prev,
        quizType: content.specific.quizType,
        question: content.specific.question ?? '',
        labelLeft: content.specific.labelLeft ?? '',
        labelRight: content.specific.labelRight ?? '',
        questions: content.specific.questions ?? [],
        scenarioText: content.specific.scenarioText ?? '',
        options: content.specific.options ?? [],
        pairs: content.specific.pairs ?? [],
        annotations: content.specific.annotations ?? [],
        imageLink: content.specific.imageLink ?? '',
        imageTitle: content.specific.imageTitle ?? '',
        correctFeedback: content.specific.correctFeedback ?? '',
        incorrectFeedback: content.specific.incorrectFeedback ?? '',
        showAutoFeedback: content.specific.showAutoFeedback ?? false,
        correctTags: content.specific.correctTags ?? [],
        emails: content.specific.emails ?? [],
        timeLimitSeconds:
          content.specific.timeLimitSeconds ?? DEFAULT_PHISHING_TIME_LIMIT,
      }));

      setBackgroundSettings(prev => ({
        ...prev,
        ...content.specific.backgroundFormatting,
        backgroundOpacity:
          content.specific.backgroundFormatting.backgroundOpacity + '',
      }));
    }
  }, [content]);

  const handleChangeInput = (
    e: ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { id, value } = e.target;
    if (id === 'contentName') {
      setContentName({
        text: value,
        error: value.trim().length === 0 ? 'Content name is required' : '',
      });
      return;
    }

    setQuizData({
      ...QuizFieldDefault,
      quizType: value as QuizTypes,
    });
  };

  const updateData = (data: IQuizFields) => setQuizData(data);

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

      if (selectedContentRef.current) {
        const blockData = selectedContentRef.current?.validateData()!;
        if (!blockData.success) {
          return { success: false };
        }
      }

      return {
        success: true,
        data: formatContentDataBasedOnType(
          {
            id: content.id!,
            contentName: contentName.text,
            updateContent: true,
            backgroundSettings,
            quizContent: {
              ...quizData,
              quizType: quizData.quizType!,
            },
          },
          ContentTypes.QUIZ,
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
                value: contentName.text,
                error: contentName.error,
                placeholder: 'Enter content name',
                onChange: handleChangeInput,
              },
            ]}
          />
        )}

        <div className="content-mt-5">
          <Select
            value={quizData.quizType}
            onValueChange={value =>
              handleChangeInput({
                target: { id: 'quizType', value },
              } as ChangeEvent<HTMLInputElement>)
            }
          >
            <SelectTrigger disabled={false}>
              <SelectValue placeholder="Select a Quiz Type" />
            </SelectTrigger>
            <SelectContent>
              {QuizOptions.map(option => (
                <SelectItem key={option.id} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          {quizData.quizType === QuizTypes.MATCHING ? (
            <MatchingQuiz
              ref={selectedContentRef}
              data={quizData}
              updateData={updateData}
            />
          ) : quizData.quizType === QuizTypes.ORDERING ? (
            <OrderingQuiz
              ref={selectedContentRef}
              data={quizData}
              updateData={updateData}
            />
          ) : quizData.quizType === QuizTypes.LIKER ? (
            <LikerSelect
              ref={selectedContentRef}
              data={quizData}
              updateData={updateData}
            />
          ) : quizData.quizType === QuizTypes.PICTURE ? (
            <PicturesQuiz
              ref={selectedContentRef}
              data={quizData}
              updateData={updateData}
            />
          ) : quizData.quizType === QuizTypes.SCENARIO ? (
            <ScenarioQuiz
              ref={selectedContentRef}
              data={quizData}
              updateData={updateData}
            />
          ) : quizData.quizType === QuizTypes.HOT_SPOT ? (
            <HotSpotQuiz
              ref={selectedContentRef}
              data={quizData}
              updateData={updateData}
            />
          ) : quizData.quizType === QuizTypes.PASSWORD_COMPLIANCE ? (
            <PasswordComplianceSettings
              ref={selectedContentRef}
              data={quizData}
              updateData={updateData}
            />
          ) : quizData.quizType === QuizTypes.RANSOMWARE_SIMULATOR ? (
            <RansomwareSimulatorSettings
              ref={selectedContentRef}
              data={quizData}
              updateData={updateData}
            />
          ) : quizData.quizType === QuizTypes.PHISHING_DETECTION ? (
            <PhishingDetectionSettings
              ref={selectedContentRef}
              data={quizData}
              updateData={updateData}
            />
          ) : null}
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
QuizBased.displayName = 'QuizBased';

export default QuizBased;
