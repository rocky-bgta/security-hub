import { Fragment, useState } from 'react';
import { GrPowerReset } from 'react-icons/gr';

import { sanitizeHtml } from 'home-module/security';
import { Button } from 'common/Button';
import BlockTypedInputWithFeatureImage from 'components/BlockTypedInputWithFeatureImage';
import { IContent, ISliderShowContent } from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<ISliderShowContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const SliderShow = ({ content, isInteractive, handleNextContent }: IProps) => {
  const [currentSlide, setCurrentSlide] = useState<number>(-1);

  const handleNext = () => {
    if (currentSlide < content?.specific?.slides?.length - 1) {
      setCurrentSlide(currentSlide + 1);
    }
  };

  const handleRestart = () => setCurrentSlide(-1);

  return (
    <>
      {isInteractive &&
        currentSlide >= content?.specific?.slides?.length - 1 && (
          <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
            <Button onClick={handleNextContent} size="sm">
              Next
            </Button>
          </div>
        )}

      <div className="content-max-h-[600px] content-overflow-y-auto content-p-5">
        {currentSlide >= content?.specific?.slides?.length - 1 &&
          content?.specific?.updateContent && (
            <Button
              size="sm"
              onClick={handleRestart}
              className="content-absolute content-right-5 content-top-5"
            >
              <GrPowerReset />
            </Button>
          )}

        {currentSlide === -1 ? (
          <Fragment>
            <BlockTypedInputWithFeatureImage content={content} />

            <div className="content-mt-5">
              {content?.specific?.slideAdditionalProperties?.buttonText && (
                <Button
                  onClick={handleNext}
                  style={{
                    backgroundColor:
                      content?.specific?.slideAdditionalProperties
                        ?.buttonColor || '',
                    color:
                      content?.specific?.slideAdditionalProperties
                        ?.buttonTextColor || '',
                  }}
                  className="hover:content-transition-colors"
                  onMouseOver={e => {
                    e.currentTarget.style.backgroundColor =
                      content?.specific?.slideAdditionalProperties
                        ?.buttonHoverColor || '';
                  }}
                  onMouseOut={e => {
                    e.currentTarget.style.backgroundColor =
                      content?.specific?.slideAdditionalProperties
                        ?.buttonColor || '';
                  }}
                >
                  {content?.specific?.slideAdditionalProperties?.buttonText}
                </Button>
              )}
            </div>
          </Fragment>
        ) : (
          <div className="content-mt-5">
            {content?.specific?.slides[currentSlide]?.featureImageLink && (
              <img
                src={
                  FILE_PATH_PREFIX +
                  content?.specific?.slides[currentSlide]?.featureImageLink
                }
                alt="preview"
                className="content-mx-auto content-mb-5 content-w-1/5"
              />
            )}

            <h1
              style={{
                color:
                  content?.specific?.slides[currentSlide]?.titleFormatting
                    .titleColor,
              }}
            >
              {content?.specific?.slides[currentSlide]?.titleFormatting.title}
            </h1>

            <h3
              className="content-mt-5 content-font-medium"
              style={{
                color:
                  content?.specific?.slides[currentSlide]?.subTitleFormatting
                    .subtitleColor,
              }}
            >
              {
                content?.specific?.slides[currentSlide]?.subTitleFormatting
                  .subtitle
              }
            </h3>

            <div
              className="content-mt-4 content-text-base"
              style={{
                color:
                  content?.specific?.slides[currentSlide]?.paragraphFormatting
                    .paragraphColor,
              }}
              dangerouslySetInnerHTML={{
                __html: sanitizeHtml(
                  content?.specific?.slides[currentSlide]?.paragraphFormatting
                    .paragraph || '',
                ),
              }}
            />

            <div className="content-mt-5">
              {currentSlide < content?.specific?.slides?.length - 1 && (
                <Button
                  onClick={handleNext}
                  style={{
                    backgroundColor:
                      content?.specific?.slideAdditionalProperties?.buttonColor,
                    color:
                      content?.specific?.slideAdditionalProperties
                        ?.buttonTextColor,
                  }}
                  className="hover:content-transition-colors"
                  onMouseOver={e => {
                    e.currentTarget.style.backgroundColor =
                      content?.specific?.slideAdditionalProperties
                        ?.buttonHoverColor || '';
                  }}
                  onMouseOut={e => {
                    e.currentTarget.style.backgroundColor =
                      content?.specific?.slideAdditionalProperties
                        ?.buttonColor || '';
                  }}
                >
                  Next
                </Button>
              )}
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default SliderShow;
