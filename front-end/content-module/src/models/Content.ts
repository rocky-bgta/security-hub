import { ImageAnnotation } from '@annotorious/annotorious';
import { JSX } from 'react';

import {
  NeitherDisagreeOrAgree,
  SomewhatAgree,
  SomewhatDisagree,
  StronglyAgree,
  StronglyDisagree,
} from 'assets/icons';

export enum ContentTypes {
  TEXT = 'TEXT',
  LINK = 'LINK',
  PDF = 'PDF',
  QUESTION = 'QUESTION',
  SLIDER_SHOW = 'SLIDE',
  TABBED = 'TAB',
  VIDEO = 'VIDEO',
  INTERACTIVE_VIDEO = 'INTERACTIVE_VIDEO',
  ANIMATION = 'ANIMATION',
  MARKDOWN = 'MARKDOWN',
  STORY_BLOCK = 'STORY_BLOCK',
  SLIDER_LEVEL = 'SLIDER_LEVEL',
  QUIZ = 'QUIZ',
  INTERACTIVE_CONTENT = 'INTERACTIVE_CONTENT',
  NONE = 'none',
}

export enum QuizTypes {
  MATCHING = 'matching',
  ORDERING = 'ordering',
  LIKER = 'liker',
  PICTURE = 'picture',
  SCENARIO = 'scenario',
  HOT_SPOT = 'hot_spot',
  RANSOMWARE_SIMULATOR = 'ransomware_simulator',
  PASSWORD_COMPLIANCE = 'password_compliance',
  PHISHING_DETECTION = 'phishing_detection',
}

export type AllContentTypes = ContentTypes | QuizTypes;

export interface IContentOption {
  id: number;
  title: string;
  type: ContentTypes;
  icon: JSX.Element;
  disable: boolean;
}

export enum ContentStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
}

export enum Tone {
  DARK = 'DARK',
  LIGHT = 'LIGHT',
}

export enum CorrectSituation {
  AGREE = 'AGREE',
  IGNORE = 'IGNORE',
}

export enum CorrectAcceptance {
  ACCEPTABLE = 'ACCEPTABLE',
  WARNING = 'WARNING',
  UNACCEPTABLE = 'UNACCEPTABLE',
}

export enum Score {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
}

export enum ProcessingStatus {
  QUEUE = 'QUEUE',
  PROCESSING = 'PROCESSING',
  PROCESSED = 'PROCESSED',
}

export enum IntervalType {
  START = 'START',
  CLEAR = 'CLEAR',
}

export interface IInteractiveVideoByLanguage {
  id: string;
  cardTitle: string;
  contentList: Array<IInteractiveContent>;
  videoUrl: string;
  videoFile?: File | null;
  videoLength: string;
  isProcessing: boolean;
  processingStatus: ProcessingStatus;
  processVideoUrl: string;
  language: string;
  default?: boolean;
}

export interface IInputFields {
  key: string;
  label?: string;
  value: string;
  placeholder?: string;
  color?: string;
  contrast?: boolean;
  error?: string;
}

export interface IContentBlockSettings<T> {
  content: IContent<T>;
  isPartofInteractiveContent?: boolean;
  uploadVideoProgress?: number;
}

export interface IFileFields {
  link: string;
  selectedFile: File | null;
  objectURL?: string;
}

export interface IContentBlockSettingsData {
  id: string;
  contentName: string;
  updateContent: boolean;
  backgroundSettings: IBackgroundSettings;
  typedInputs?: Array<IInputFields>;
  inputs?: Array<IInputFields>;
  featureImage?: IFileFields;
  pdfContent?: {
    pdf: IFileFields;
  };
  videoContent?: {
    id: string;
    title: string;
    video: IFileFields;
    caption: IFileFields;
    processVideoUrl: string;
  };
  sliderShowContent?: {
    slides: Array<ISlide>;
    buttonValue: Array<IInputFields>;
  };
  storyBlockContent?: {
    steps: Array<
      IStoryBlockStepAddProp & { stories: Array<IStoryBlockStoryAddProp> }
    >;
  };
  markdownContent?: {
    markdown: string;
  };
  questionContent?: {
    scorable: boolean;
    options: Array<IQuestionOption>;
  };
  tabbedContent?: {
    buttonValue: Array<IInputFields>;
    tabbedPosition: string;
    audio: IFileFields;
    tabs: Array<ITab>;
  };
  sliderLevelContent?: {
    steps: Array<
      ISliderLevelStepAddProp & { buttons: Array<ISliderLevelButtonAddProp> }
    >;
  };
  interactiveContent?: Array<{
    id?: string;
    contentType: string;
    time: string;
    canSkip: boolean;
    isDefault: boolean;
    contentBody: IContent<TContent>;
  }>;
  interactiveVideoContent?: {
    interactiveVideoByLanguage?: Array<IInteractiveVideoByLanguage>;
  };
  quizContent?: {
    quizType: QuizTypes;
    question?: string;
    options?: Array<IQuizOption>;
    pairs?: Array<IMatchingOption>;
    labelLeft?: string;
    labelRight?: string;
    annotations?: Array<IHotSpotAnnotations>;
    imageLink?: string;
    imageFile?: File | null;
    imageTitle?: string;
    correctFeedback?: string;
    incorrectFeedback?: string;
    showAutoFeedback?: boolean;
    correctTags?: Array<string>;
    scenarioText?: string;
    questions?: Array<IScenarioQuizQuestion>;
    emails?: Array<IPhishingEmail>;
    timeLimitSeconds?: number;
  };
}

export interface IContentBlockHandle {
  validateAndGetData: () => {
    success: boolean;
    data?: {
      id: string;
      contentName: string;
      specific: TContent;
    };
    interactiveContentData?: Array<IInteractiveContent>;
  };
}

export interface IDataValidationHandle {
  validateData: () => {
    success: boolean;
  };
}

export type TContent =
  | ITextContent
  | ILinkContent
  | IPdfContent
  | IQuestionContent
  | ISliderShowContent
  | IMarkdownContent
  | IStoryBlockContent
  | IVideoContent
  | ITabbedContent
  | ISliderLevelContent
  | IInteractiveVideoContent
  | IInteractiveGeneralContent
  | IQuizContent;

export interface IContent<T> {
  id?: string;
  common: {
    contentName: string;
    description?: string;
    contentType: ContentTypes;
    status: ContentStatus;
    author?: string;
    chapterIds?: Array<string>;
    tags?: Array<string>;
  };
  specific: T;
  createdAt?: string;
  updatedAt?: string;
}

export interface IBackgroundSettings {
  textColor: string;
  backgroundColor: string;
  backgroundImage: string;
  selectedFile?: File | null;
  backgroundOpacity: string;
  tone: Tone;
}

interface ICommonFormatting {
  titleFormatting: {
    title: string;
    titleColor: string;
    titleHighContrastMode: boolean;
  };
  subTitleFormatting: {
    subtitle: string;
    subtitleColor: string;
    subtitleHighContrastMode: boolean;
  };
  paragraphFormatting: {
    paragraph: string;
    paragraphColor: string;
    paragraphHighContrastMode: boolean;
  };
}

interface IBackgroundFormatting {
  backgroundFormatting: {
    textColor: string;
    backgroundColor: string;
    backgroundImage: string;
    backgroundImageFile?: File | null;
    backgroundOpacity: number;
    tone: Tone;
  };
  updateContent?: boolean;
}

export interface ITextContent extends ICommonFormatting, IBackgroundFormatting {
  featureImageLink: string;
  featureImageFile?: File | null;
}

export interface ILinkContent extends ITextContent {
  linkFormatting: {
    linkText: string;
    url: string;
    linkColor: string;
  };
}

export interface IPdfContent extends ITextContent {
  pdfFileName: string;
  pdfFileURL: string;
  metadata: {
    additionalProp1: {
      pdfText: string;
      pdfLink: string;
      pdfFile?: File | null;
      pdfTextColor: string;
      pdfIsRequired: boolean;
    };
  };
}

export interface IQuestionOption {
  id?: string;
  option: string;
  isCorrect: boolean;
}

export interface IQuestionContent extends IBackgroundFormatting {
  question: string;
  options: Array<IQuestionOption>;
  scorable: boolean;
  score: Score;
}

export interface ISlide extends ICommonFormatting {
  id: string;
  featureImageLink: string;
  featureImageFile?: File | null;
}

export interface ISliderShowContent extends ITextContent {
  slides: Array<ISlide>;
  slideAdditionalProperties: {
    buttonText: string;
    buttonTextColor: string;
    buttonColor: string;
    buttonHoverColor: string;
  };
}

export interface ITab {
  id: string;
  paragraph: string;
  navigationButtonText: string;
  displayTime: string;
  audioUrl: string;
  audioFile?: File | null;
}

export interface ITabbedContent extends ITextContent {
  tabSections: Array<{
    additionalProperties: ITab;
  }>;
  additionalProperties: {
    buttonTextColor: string;
    buttonColor: string;
    buttonHoverColor: string;
    tabbedPosition: string;
    audioUrl: string;
    audioFile?: File | null;
  };
}

export interface IVideoContent extends IBackgroundFormatting {
  videoLength?: string;
  captionUrl: string;
  captionFile?: File | null;
  interactiveVideo: {
    id: string;
    videoUrl: string;
    videoFile?: File | null;
    isProcessing: boolean;
    processingStatus: ProcessingStatus;
    processVideoUrl: string;
  };
  metadata: {
    additionalProp1: {
      videoText: string;
    };
  };
}

export interface IMarkdownContent extends IBackgroundFormatting {
  markdownText: string;
  headingColor: string;
  textColor: string;
  linkColor: string;
}

interface IStep {
  id: string;
  title: string;
  titleColor: string;
  featureImage: string;
  featureImageFile?: File | null;
  openPopup: boolean;
  popupText?: string;
  popupTextColor?: string;
  popupButtonText?: string;
  popupButtonTextColor?: string;
  popupButtonColor?: string;
  popupButtonHoverColor?: string;
  description: string;
  descriptionColor: string;
}

export interface IStoryBlockStepAddProp extends IStep {
  advisorInstructionText: string;
  advisorInstructionTextColor: string;
}

export interface IStoryBlockStoryAddProp {
  id: string;
  featureImage: string;
  featureImageFile?: File | null;
  description: string;
  descriptionColor: string;
  actionButtonTitle: string;
  actionButtonTitleColor: string;
  buttonTextColor: string;
  buttonColor: string;
  buttonHoverColor: string;
  correctSituation: CorrectSituation;
  agreeText: string;
  agreeTextColor: string;
  ignoreText: string;
  ignoreTextColor: string;
}

export interface IStoryBlockContent extends IBackgroundFormatting {
  steps: Array<{
    additionalProperties: IStoryBlockStepAddProp;
    stories: Array<{
      additionalProperties: IStoryBlockStoryAddProp;
    }>;
  }>;
}

export interface ISliderLevelStepAddProp extends IStep {
  navigationButtonText: string;
  navigationButtonTextColor: string;
  navigationButtonColor: string;
  navigationButtonHoverColor: string;
}

export interface ISliderLevelButtonAddProp {
  id: string;
  navigationButtonText: string;
  navigationButtonTextColor: string;
  navigationButtonColor: string;
  navigationButtonHoverColor: string;
  correctAcceptanceText: CorrectAcceptance;
  acceptanceText: string;
  acceptanceTextColor: string;
  openPopup: boolean;
  popupText: string;
  popupTextColor: string;
  popupButtonText: string;
  popupButtonTextColor: string;
  popupButtonColor: string;
  popupButtonHoverColor: string;
}

export interface ISliderLevelContent extends IBackgroundFormatting {
  steps: Array<{
    additionalProperties: ISliderLevelStepAddProp;
    buttons: Array<{
      additionalProperties: ISliderLevelButtonAddProp;
    }>;
  }>;
}

export interface IInteractiveContent {
  id?: string;
  contentType: string;
  time: string;
  canSkip: boolean;
  isDefault: boolean;
  contentBody: IContent<TContent>;
}

export interface IInteractiveGeneralContent extends IBackgroundFormatting {
  contentList: Array<IInteractiveContent>;
  interactiveVideo: {
    id: string;
    videoUrl: string;
    videoFile?: File | null;
    videoLength: string;
    isProcessing: boolean;
    processingStatus: ProcessingStatus;
    processVideoUrl: string;
  };
}

export interface IInteractiveVideoContent extends IBackgroundFormatting {
  interactiveVideoByLanguage: Array<IInteractiveVideoByLanguage>;
}

export interface IQuizFields {
  quizType?: QuizTypes;

  question?: string;
  options?: Array<IQuizOption>;

  labelLeft?: string;
  labelRight?: string;
  pairs?: Array<IMatchingOption>;

  annotations?: Array<IHotSpotAnnotations>;
  imageLink?: string;
  imageFile?: File | null;
  imageTitle?: string;
  correctFeedback?: string;
  incorrectFeedback?: string;
  showAutoFeedback?: boolean;
  correctTags?: Array<string>;

  scenarioText?: string;
  questions?: Array<IScenarioQuizQuestion>;

  emails?: Array<IPhishingEmail>;
  timeLimitSeconds?: number;
}

export interface IQuizContent extends IQuizFields, IBackgroundFormatting {
  updateContent?: boolean;
}

export interface IQuizOption {
  index?: number;
  id: string;
  optionText: string;
  optionImage?: File | null;
  optionImageLink: string;
  isCorrect?: boolean;
}

export interface IHotSpotAnnotations extends ImageAnnotation {
  id: string;
  target: {
    annotation: string;
    selector: any;
  };
  bodies: Array<{
    id: string;
    annotation: string;
    type?: string;
    purpose?: string;
    value?: string;
  }>;
}

export enum MatchingQuizTypes {
  TEXT = 'text',
  IMAGE = 'image',
}

export interface IMatchingOption {
  id: string;
  inputTypeLeft: MatchingQuizTypes;
  valueLeft: string;
  linkLeft: string;
  fileLeft?: File | null;
  objectURLLeft?: string;
  inputTypeRight: MatchingQuizTypes;
  valueRight: string;
  linkRight: string;
  fileRight?: File | null;
  objectURLRight?: string;
}

export interface IScenarioQuizQuestion {
  id: string;
  question: string;
  options: Array<IQuestionOption>;
}

export interface IPhishingEmailLink {
  id: string;
  label?: string;
  displayText: string;
  actualUrl: string;
}

export interface IPhishingEmail {
  id: string;
  senderName: string;
  senderEmail: string;
  subject: string;
  date: string;
  time: string;
  avatar?: string;
  body: string;
  links: Array<IPhishingEmailLink>;
  isPhishing: boolean;
  explanation?: string;
}

export const DEFAULT_PHISHING_TIME_LIMIT = 180;

export const LikerSelectOptionIconMapper = {
  'strongly-disagree': StronglyDisagree,
  'somewhat-disagree': SomewhatDisagree,
  'neither-disagree-agree': NeitherDisagreeOrAgree,
  'somewhat-agree': SomewhatAgree,
  'strongly-agree': StronglyAgree,
} as const;
