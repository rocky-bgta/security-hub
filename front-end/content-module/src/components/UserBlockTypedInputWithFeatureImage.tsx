import clsx from 'clsx';
import { sanitizeHtml } from 'home-module/security';
import {
  IContent,
  ILinkContent,
  IPdfContent,
  ISliderShowContent,
  ITabbedContent,
  ITextContent,
} from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<
    | ITextContent
    | ILinkContent
    | IPdfContent
    | ISliderShowContent
    | ITabbedContent
  >;
}

const UserBlockTypedInputWithFeatureImage = ({ content }: IProps) => {
  return (
    <>
      {content?.specific?.featureImageLink && (
        <img
          src={FILE_PATH_PREFIX + content?.specific?.featureImageLink}
          alt="preview"
          className="content-h-auto content-w-full content-max-w-full"
        />
      )}

      <div className="content-px-4 content-pt-4 sm:content-px-6 sm:content-pt-6">
        {content?.specific?.titleFormatting?.title && (
          <h1
            className={clsx(
              'content-break-words content-text-2xl content-font-semibold sm:content-text-[32px]',
              content?.specific?.titleFormatting?.titleHighContrastMode
                ? 'content-rounded content-bg-black content-px-2 content-py-0.5 content-shadow-md'
                : '',
            )}
            style={{
              color: content?.specific?.titleFormatting?.titleColor,
            }}
          >
            {content?.specific?.titleFormatting?.title}
          </h1>
        )}

        {content?.specific?.subTitleFormatting?.subtitle && (
          <h3
            className={clsx(
              'content-mt-3 content-break-words content-text-xl content-font-medium sm:content-mt-4 sm:content-text-[24px]',
              content?.specific?.subTitleFormatting?.subtitleHighContrastMode &&
                'content-rounded content-bg-black content-px-2 content-py-0.5 content-shadow-md',
            )}
            style={{
              color: content?.specific?.subTitleFormatting?.subtitleColor,
            }}
          >
            {content?.specific?.subTitleFormatting?.subtitle}
          </h3>
        )}

        {content?.specific?.paragraphFormatting?.paragraph && (
          <div
            className={clsx(
              'content-my-4',
              content?.specific?.paragraphFormatting
                ?.paragraphHighContrastMode &&
                'content-rounded content-bg-black content-px-2 content-py-0.5 content-shadow-md',
            )}
            style={{
              color: content?.specific?.paragraphFormatting?.paragraphColor,
            }}
            dangerouslySetInnerHTML={{
              __html: sanitizeHtml(
                content?.specific?.paragraphFormatting?.paragraph || '',
              ),
            }}
          />
        )}
      </div>
    </>
  );
};

export default UserBlockTypedInputWithFeatureImage;
