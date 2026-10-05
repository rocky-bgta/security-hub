import { sanitizeHtml } from 'home-module/security';
import { Button } from 'common/Button';
import UserBlockTypedInputWithFeatureImage from 'components/UserBlockTypedInputWithFeatureImage';
import { AllContentTypes, IContent, ISliderShowContent } from 'models/Content';
import { useState } from 'react';
import { IoArrowForward } from 'react-icons/io5';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<ISliderShowContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
  showCompleteButton?: (type: AllContentTypes) => void;
  courseComplete?: () => void;
}

const SliderShow = ({
  content,
  isInteractive,
  handleNextContent,
  showCompleteButton,
  courseComplete,
}: IProps) => {
  const [currentSlide, setCurrentSlide] = useState<number>(-1);
  const totalSlides = content?.specific?.slides?.length || 0;
  const [showNextButton, setShowNextButton] = useState<boolean>(false);

  const handleNext = () => {
    if (currentSlide < totalSlides - 1) {
      setCurrentSlide(currentSlide + 1);
      if (currentSlide + 1 === totalSlides - 1) {
        showCompleteButton?.(content.common.contentType);
        courseComplete?.();
        setShowNextButton(true);
      }
    }
  };

  const isLastSlide = currentSlide === totalSlides - 1;
  return (
    <div>
      {isInteractive && showNextButton && (
        <div className="content-absolute content-bottom-1 content-right-4 content-z-10">
          <Button
            onClick={handleNextContent}
            size="sm"
            className="content-px-8"
          >
            Next
          </Button>
        </div>
      )}
      {currentSlide === -1 ? (
        <div>
          <UserBlockTypedInputWithFeatureImage content={content} />
          <div className="content-pb-6">
            <Button
              onClick={handleNext}
              className="content-mx-auto content-mt-4 content-flex content-items-center content-gap-2"
              style={{
                backgroundColor:
                  content?.specific?.slideAdditionalProperties?.buttonColor ||
                  '',
                color:
                  content?.specific?.slideAdditionalProperties
                    ?.buttonTextColor || '',
              }}
              onMouseOver={e => {
                e.currentTarget.style.backgroundColor =
                  content?.specific?.slideAdditionalProperties
                    ?.buttonHoverColor || '';
              }}
              onMouseOut={e => {
                e.currentTarget.style.backgroundColor =
                  content?.specific?.slideAdditionalProperties?.buttonColor ||
                  '';
              }}
            >
              {content?.specific?.slideAdditionalProperties?.buttonText}{' '}
              <IoArrowForward />
            </Button>
          </div>
        </div>
      ) : (
        <div className="content-relative">
          <div className="content-absolute content-right-2 content-top-2 content-rounded-full content-bg-white content-bg-opacity-25 content-px-4 content-py-2 content-text-sm content-text-white">
            {currentSlide + 1} of {content?.specific?.slides?.length}
          </div>
          {content.specific.slides[currentSlide].featureImageLink && (
            <img
              src={
                FILE_PATH_PREFIX +
                content.specific.slides[currentSlide].featureImageLink
              }
              alt="preview"
              className="content-mb-6 content-w-full"
            />
          )}
          <div className="content-p-6">
            {content.specific.slides[currentSlide].titleFormatting.title && (
              <h1
                className="content-text-[32px] content-font-semibold content-text-white"
                style={{
                  color:
                    content.specific.slides[currentSlide].titleFormatting
                      .titleColor,
                }}
              >
                {content.specific.slides[currentSlide].titleFormatting.title}
              </h1>
            )}

            {content.specific.slides[currentSlide].subTitleFormatting
              .subtitle && (
              <h3
                className="content-mt-4 content-text-[24px] content-font-medium content-text-white"
                style={{
                  color:
                    content.specific.slides[currentSlide].subTitleFormatting
                      .subtitleColor,
                }}
              >
                {
                  content.specific.slides[currentSlide].subTitleFormatting
                    .subtitle
                }
              </h3>
            )}

            {content.specific.slides[currentSlide].paragraphFormatting
              .paragraph && (
              <div
                className="content-mt-4"
                dangerouslySetInnerHTML={{
                  __html: sanitizeHtml(
                    content.specific.slides[currentSlide].paragraphFormatting
                      .paragraph || '',
                  ),
                }}
              />
            )}
          </div>
          {!isLastSlide && (
            <div className="content-pb-6">
              <Button
                onClick={handleNext}
                className="content-mx-auto content-mt-4 content-flex content-items-center content-gap-2"
                style={{
                  backgroundColor:
                    content?.specific?.slideAdditionalProperties?.buttonColor ||
                    '',
                  color:
                    content?.specific?.slideAdditionalProperties
                      ?.buttonTextColor || '',
                }}
                onMouseOver={e => {
                  e.currentTarget.style.backgroundColor =
                    content?.specific?.slideAdditionalProperties
                      ?.buttonHoverColor || '';
                }}
                onMouseOut={e => {
                  e.currentTarget.style.backgroundColor =
                    content?.specific?.slideAdditionalProperties?.buttonColor ||
                    '';
                }}
              >
                {content?.specific?.slideAdditionalProperties?.buttonText}{' '}
                <IoArrowForward />
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default SliderShow;
