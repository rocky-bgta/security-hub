import {
  ContentTypes,
  CorrectAcceptance,
  CorrectSituation,
  DEFAULT_PHISHING_TIME_LIMIT,
  MatchingQuizTypes,
  QuizTypes,
  Tone,
} from 'models/Content';

export const CommonFormatting = {
  titleFormatting: {
    title: '',
    titleColor: '#ffffff',
    titleHighContrastMode: false,
  },
  subTitleFormatting: {
    subtitle: '',
    subtitleColor: '#ffffff',
    subtitleHighContrastMode: false,
  },
  paragraphFormatting: {
    paragraph: '',
    paragraphColor: '#ffffff',
    paragraphHighContrastMode: false,
  },
};

export const BackgroundFormattingDefault = {
  backgroundFormatting: {
    textColor: '#000000',
    backgroundColor: '#ffffff00',
    backgroundImage: '',
    backgroundOpacity: '100',
    tone: Tone.LIGHT,
  },
};

export const TextContentDefault = {
  ...BackgroundFormattingDefault,
  ...CommonFormatting,
  featureImageLink: '',
  updateContent: false,
};

export const LinkContentDefault = {
  ...TextContentDefault,
  linkFormatting: {
    linkText: '',
    url: '',
    linkColor: '#ffffff',
  },
  updateContent: false,
};

export const PdfContentDefault = {
  ...TextContentDefault,
  metadata: {
    additionalProp1: {
      pdfText: '',
      pdfLink: '',
      pdfTextColor: '#ffffff',
      pdfIsRequired: false,
    },
  },
  updateContent: false,
};

export const QuestionContentDefault = {
  ...BackgroundFormattingDefault,
  question: '',
  options: [],
  scorable: false,
  score: '',
  updateContent: false,
};

export const SliderShowDefault = {
  ...TextContentDefault,
  slides: [],
  slideAdditionalProperties: {
    buttonText: '',
    buttonTextColor: '#ffffff',
    buttonColor: '#37BE99',
    buttonHoverColor: '#37BE99',
  },
  updateContent: false,
};

export const SlideDefault = {
  ...CommonFormatting,
  featureImageFile: null,
  featureImageLink: '',
};

export const TabbedContentDefault = {
  ...TextContentDefault,
  tabSections: [],
  additionalProperties: {
    buttonText: '',
    buttonTextColor: '#ffffff',
    buttonColor: '#ffffff00',
    buttonHoverColor: '#ffffff00',
    tabbedPosition: 'COLUMN',
    audioUrl: '',
  },
};

export const TabDefault = {
  paragraph: '',
  navigationButtonText: '',
  displayTime: '00:00',
  audioUrl: '',
  audioFile: null,
};

export const VideoContentDefault = {
  ...BackgroundFormattingDefault,
  captionUrl: '',
  interactiveVideo: {
    id: '',
    videoUrl: '',
    isProcessing: false,
    processingStatus: '',
    processVideoUrl: '',
  },
  metadata: {
    additionalProp1: {
      videoText: '',
    },
  },
};

export const MarkdownContentDefault = {
  ...BackgroundFormattingDefault,
  markdownText: '',
  headingColor: '#ffffff',
  textColor: '#ffffff',
  linkColor: '#ffffff',
  updateContent: false,
};

export const StoryBlockDefault = {
  ...BackgroundFormattingDefault,
  steps: [],
};

export const StepDefault = {
  title: '',
  titleColor: '#ffffff',
  featureImage: '',
  featureImageFile: null,
  openPopup: false,
  popupText: '',
  popupTextColor: '#ffffff',
  popupButtonText: '',
  popupButtonTextColor: '#ffffff',
  popupButtonColor: '#37BE99',
  popupButtonHoverColor: '#37BE99',
  description: '',
  descriptionColor: '#ffffff',
};

export const StoryBlockStepDefault = {
  ...StepDefault,
  advisorInstructionText: '',
  advisorInstructionTextColor: '#ffffff',
};

export const StoryBlockStoryDefault = {
  featureImage: '',
  featureImageFile: null,
  description: '',
  descriptionColor: '#ffffff',
  actionButtonTitle: '',
  actionButtonTitleColor: '#ffffff',
  buttonTextColor: '#ffffff',
  buttonColor: '#37BE99',
  buttonHoverColor: '#37BE99',
  correctSituation: CorrectSituation.AGREE,
  agreeText: '',
  agreeTextColor: '#ffffff',
  ignoreText: '',
  ignoreTextColor: '#ffffff',
};

export const SliderLevelStepDefault = {
  ...StepDefault,
  navigationButtonText: '',
  navigationButtonTextColor: '#ffffff',
  navigationButtonColor: '#37BE99',
  navigationButtonHoverColor: '#37BE99',
};

export const SliderLevelButtonDefault = {
  navigationButtonText: '',
  navigationButtonTextColor: '#ffffff',
  navigationButtonColor: '#37BE99',
  navigationButtonHoverColor: '#37BE99',
  correctAcceptanceText: CorrectAcceptance.ACCEPTABLE,
  acceptanceText: '',
  acceptanceTextColor: '#ffffff',
  openPopup: false,
  popupText: '',
  popupTextColor: '#ffffff',
  popupButtonText: '',
  popupButtonTextColor: '#ffffff',
  popupButtonColor: '#37BE99',
  popupButtonHoverColor: '#37BE99',
};

export const SliderLevelContentDefault = {
  ...BackgroundFormattingDefault,
  steps: [],
};

export const InteractiveGeneralContentDefault = {
  ...BackgroundFormattingDefault,
  contentList: [],
  updateContent: false,
};

export const InteractiveVideoContentDefault = {
  ...InteractiveGeneralContentDefault,
  // interactiveVideo: {
  //   id: '',
  //   videoUrl: '',
  //   videoLength: '',
  //   isProcessing: false,
  //   processingStatus: '',
  //   processVideoUrl: '',
  // },
  interactiveVideoByLanguage: [],
};

export const QuizDefault = {
  ...BackgroundFormattingDefault,
  quizType: QuizTypes.MATCHING,
  updateContent: false,
};

export const QuizFieldDefault = {
  question: '',
  quizType: QuizTypes.MATCHING,
  options: [],
  labelLeft: '',
  labelRight: '',
  pairs: [],
  annotations: [],
  imageLink: '',
  imageFile: null,
  imageTitle: '',
  correctFeedback: '',
  incorrectFeedback: '',
  showAutoFeedback: false,
  scenarioText: '',
  questions: [],
  emails: [],
  timeLimitSeconds: DEFAULT_PHISHING_TIME_LIMIT,
};

export const MatchingQuizDefault = {
  inputTypeLeft: MatchingQuizTypes.TEXT,
  valueLeft: '',
  linkLeft: '',
  fileLeft: null,
  objectURLLeft: '',
  inputTypeRight: MatchingQuizTypes.TEXT,
  valueRight: '',
  linkRight: '',
  fileRight: null,
  objectURLRight: '',
};

export const GetContentDefaultValue = (type: ContentTypes) => {
  switch (type) {
    case ContentTypes.TEXT:
      return { ...TextContentDefault };
    case ContentTypes.LINK:
      return { ...LinkContentDefault };
    case ContentTypes.PDF:
      return { ...PdfContentDefault };
    case ContentTypes.QUESTION:
      return { ...QuestionContentDefault };
    case ContentTypes.SLIDER_SHOW:
      return { ...SliderShowDefault };
    case ContentTypes.TABBED:
      return { ...TabbedContentDefault };
    case ContentTypes.VIDEO:
    case ContentTypes.ANIMATION:
      return { ...VideoContentDefault };
    case ContentTypes.MARKDOWN:
      return { ...MarkdownContentDefault };
    case ContentTypes.STORY_BLOCK:
      return { ...StoryBlockDefault };
    case ContentTypes.SLIDER_LEVEL:
      return { ...SliderLevelContentDefault };
    case ContentTypes.INTERACTIVE_CONTENT:
      return { ...InteractiveGeneralContentDefault };
    case ContentTypes.INTERACTIVE_VIDEO:
      return { ...InteractiveVideoContentDefault };
    case ContentTypes.QUIZ:
      return { ...QuizDefault };
    default:
      return {};
  }
};
