import {
  ContentTypes,
  DEFAULT_PHISHING_TIME_LIMIT,
  IContentBlockSettingsData,
  ProcessingStatus,
  QuizTypes,
  Score,
  TContent,
} from 'models/Content';

export const formatContentDataBasedOnType = (
  data: IContentBlockSettingsData,
  type: ContentTypes,
) => {
  let specific: TContent = {
    updateContent: data.updateContent,
    backgroundFormatting: {
      textColor: data.backgroundSettings.textColor,
      backgroundColor: data.backgroundSettings.backgroundColor,
      backgroundImage: data.backgroundSettings.backgroundImage,
      backgroundImageFile: data.backgroundSettings.selectedFile,
      backgroundOpacity: +data.backgroundSettings.backgroundOpacity,
      tone: data.backgroundSettings.tone,
    },
  };

  if (data.typedInputs) {
    specific = {
      ...specific,
      titleFormatting: {
        title:
          data.typedInputs.find(input => input.key === 'Title')?.value ?? '',
        titleColor:
          data.typedInputs.find(input => input.key === 'Title')?.color ?? '',
        titleHighContrastMode:
          data.typedInputs.find(input => input.key === 'Title')?.contrast ??
          false,
      },
      subTitleFormatting: {
        subtitle:
          data.typedInputs.find(input => input.key === 'Subtitle')?.value ?? '',
        subtitleColor:
          data.typedInputs.find(input => input.key === 'Subtitle')?.color ?? '',
        subtitleHighContrastMode:
          data.typedInputs.find(input => input.key === 'Subtitle')?.contrast ??
          false,
      },
      paragraphFormatting: {
        paragraph:
          data.typedInputs.find(input => input.key === 'Paragraph')?.value ??
          '',
        paragraphColor:
          data.typedInputs.find(input => input.key === 'Paragraph')?.color ??
          '',
        paragraphHighContrastMode:
          data.typedInputs.find(input => input.key === 'Paragraph')?.contrast ??
          false,
      },
    };
  }

  if (data.featureImage) {
    specific = {
      ...specific,
      featureImageLink: data.featureImage.link,
      featureImageFile: data.featureImage.selectedFile,
    };
  }

  switch (type) {
    case ContentTypes.LINK:
      specific = {
        ...specific,
        linkFormatting: {
          linkText:
            data.inputs?.find(input => input.key === 'linkText')?.value ?? '',
          url: data.inputs?.find(input => input.key === 'url')?.value ?? '',
          linkColor:
            data.inputs?.find(input => input.key === 'linkColor')?.value ?? '',
        },
      };
      break;
    case ContentTypes.PDF:
      specific = {
        ...specific,
        metadata: {
          additionalProp1: {
            pdfText:
              data.inputs?.find(input => input.key === 'pdfText')?.value ?? '',
            pdfLink:
              data.inputs?.find(input => input.key === 'pdfLink')?.value ||
              data.pdfContent?.pdf.link,
            pdfFile: data.pdfContent?.pdf.selectedFile,
            pdfTextColor: data.inputs?.find(
              input => input.key === 'pdfTextColor',
            )?.value,
            pdfIsRequired:
              data.inputs?.find(input => input.key === 'pdfIsRequired')
                ?.value === 'true',
          },
        },
      } as any;
      break;
    case ContentTypes.MARKDOWN:
      specific = {
        ...specific,
        markdownText: data.markdownContent?.markdown ?? '',
      };
      break;
    case ContentTypes.QUESTION:
      specific = {
        ...specific,
        question:
          data.inputs?.find(input => input.key === 'question')?.value ?? '',
        options:
          data.questionContent?.options.map(item => ({
            option: item.option,
            isCorrect: item.isCorrect,
          })) ?? [],
        scorable: data.questionContent?.scorable ?? false,
        score: data.inputs?.find(input => input.key === 'score')
          ?.value as Score,
      };
      break;
    case ContentTypes.VIDEO:
    case ContentTypes.ANIMATION:
      specific = {
        ...specific,
        captionUrl: data.videoContent?.caption.link ?? '',
        captionFile: data.videoContent?.caption.selectedFile,
        interactiveVideo: {
          id: data.videoContent?.id ?? '',
          videoUrl: data.videoContent?.video.link ?? '',
          videoFile: data.videoContent?.video.selectedFile,
          isProcessing: true,
          processingStatus: ProcessingStatus.QUEUE,
          processVideoUrl: data.videoContent?.processVideoUrl ?? '',
        },
        metadata: {
          additionalProp1: {
            videoText: data.videoContent?.title ?? '',
          },
        },
      };
      break;
    case ContentTypes.TABBED:
      specific = {
        ...specific,
        tabSections:
          data.tabbedContent?.tabs.map(item => ({
            additionalProperties: { ...item },
          })) ?? [],
        additionalProperties: {
          buttonTextColor:
            data.tabbedContent?.buttonValue.find(
              input => input.key === 'buttonTextColor',
            )?.value ?? '#ffffff',
          buttonColor:
            data.tabbedContent?.buttonValue.find(
              input => input.key === 'buttonColor',
            )?.value ?? '#ffffff',
          buttonHoverColor:
            data.tabbedContent?.buttonValue.find(
              input => input.key === 'buttonHoverColor',
            )?.value ?? '#ffffff',
          tabbedPosition: data.tabbedContent?.tabbedPosition ?? '',
          audioUrl: data.tabbedContent?.audio.link ?? '',
          audioFile: data.tabbedContent?.audio.selectedFile,
        },
      };
      break;
    case ContentTypes.STORY_BLOCK:
      specific = {
        ...specific,
        steps:
          data.storyBlockContent?.steps.map(({ stories, ...stepRest }) => ({
            additionalProperties: { ...stepRest },
            stories:
              stories.map(story => ({
                additionalProperties: { ...story },
              })) ?? [],
          })) ?? [],
      };
      break;
    case ContentTypes.SLIDER_LEVEL:
      specific = {
        ...specific,
        steps:
          data.sliderLevelContent?.steps.map(({ buttons, ...stepRest }) => ({
            additionalProperties: { ...stepRest },
            buttons:
              buttons.map(button => ({
                additionalProperties: { ...button },
              })) ?? [],
          })) ?? [],
      };
      break;
    case ContentTypes.SLIDER_SHOW:
      specific = {
        ...specific,
        slides: data.sliderShowContent?.slides ?? [],
        slideAdditionalProperties: {
          buttonText:
            data.sliderShowContent?.buttonValue.find(
              input => input.key === 'buttonText',
            )?.value ?? '',

          buttonTextColor:
            data.sliderShowContent?.buttonValue.find(
              input => input.key === 'buttonTextColor',
            )?.value ?? '#ffffff',
          buttonColor:
            data.sliderShowContent?.buttonValue.find(
              input => input.key === 'buttonColor',
            )?.value ?? '#ffffff',
          buttonHoverColor:
            data.sliderShowContent?.buttonValue.find(
              input => input.key === 'buttonHoverColor',
            )?.value ?? '#ffffff',
        },
      };
      break;
    case ContentTypes.INTERACTIVE_CONTENT:
      specific = {
        ...specific,
        contentList:
          data.interactiveContent?.map(content => ({
            id: content.id,
            contentType: content.contentType,
            time: content.time,
            canSkip: content.canSkip,
            isDefault: content.isDefault,
            contentBody: {
              ...content.contentBody,
            },
          })) ?? [],
      };
      break;
    case ContentTypes.INTERACTIVE_VIDEO:
      specific = {
        ...specific,
        interactiveVideoByLanguage:
          data.interactiveVideoContent?.interactiveVideoByLanguage?.map(
            item => ({
              id: item.id,
              cardTitle: item.cardTitle,
              contentList: item.contentList ?? [],
              isProcessing: item.isProcessing,
              language: item.language,
              processVideoUrl: item.processVideoUrl,
              videoUrl: item.videoUrl,
              processingStatus: item.processingStatus,
              videoLength: item.videoLength,
              videoFile: item.videoFile,
            }),
          ) ?? [],
      };
      break;
    case ContentTypes.QUIZ:
      specific = {
        ...specific,
        quizType: data.quizContent?.quizType,
        ...getQuizPayloadBasedOnType(data),
      };
  }

  return {
    id: data.id,
    contentName: data.contentName,
    specific,
  };
};

const getQuizPayloadBasedOnType = (data: IContentBlockSettingsData) => {
  switch (data.quizContent?.quizType) {
    case QuizTypes.MATCHING:
      return {
        question: data.quizContent?.question ?? '',
        labelLeft: data.quizContent?.labelLeft ?? '',
        labelRight: data.quizContent?.labelRight ?? '',
        pairs: data.quizContent?.pairs ?? [],
      };
    case QuizTypes.ORDERING:
    case QuizTypes.LIKER:
      return {
        question: data.quizContent?.question ?? '',
        options:
          data.quizContent?.options?.map((item, index) => {
            return {
              id: item.id,
              index: index,
              optionText: item.optionText,
              optionImage: item.optionImage,
              optionImageLink: item.optionImageLink,
            };
          }) ?? [],
      };
    case QuizTypes.PICTURE:
      return {
        question: data.quizContent?.question ?? '',
        options:
          data.quizContent?.options?.map((item, index) => {
            return {
              id: item.id,
              index: index,
              optionText: item.optionText,
              optionImage: item.optionImage,
              optionImageLink: item.optionImageLink,
              isCorrect: item.isCorrect,
            };
          }) ?? [],
      };
    case QuizTypes.SCENARIO:
      return {
        scenarioText: data.quizContent?.scenarioText ?? '',
        questions: data.quizContent?.questions ?? [],
      };
    case QuizTypes.HOT_SPOT:
      return {
        imageLink: data.quizContent?.imageLink ?? '',
        annotations: data.quizContent?.annotations ?? [],
        imageTitle: data.quizContent?.imageTitle ?? '',
        imageFile: data.quizContent?.imageFile ?? null,
        correctFeedback: data.quizContent?.correctFeedback ?? '',
        incorrectFeedback: data.quizContent?.incorrectFeedback ?? '',
        showAutoFeedback: data.quizContent?.showAutoFeedback ?? false,
        correctTags: data.quizContent?.correctTags ?? [],
      };
    case QuizTypes.RANSOMWARE_SIMULATOR:
    case QuizTypes.PASSWORD_COMPLIANCE:
      return {
        question: data.quizContent?.question ?? '',
      };
    case QuizTypes.PHISHING_DETECTION:
      return {
        question: data.quizContent?.question ?? '',
        timeLimitSeconds:
          data.quizContent?.timeLimitSeconds ?? DEFAULT_PHISHING_TIME_LIMIT,
        emails: data.quizContent?.emails ?? [],
      };
  }
};
