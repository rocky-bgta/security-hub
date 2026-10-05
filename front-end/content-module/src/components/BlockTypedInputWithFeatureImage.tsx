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

const BlockTypedInputWithFeatureImage = ({ content }: IProps) => {
  return (
    <>
      {content?.specific?.featureImageLink && (
        <img
          src={FILE_PATH_PREFIX + content?.specific?.featureImageLink}
          alt="preview"
          className="content-mx-auto content-mb-5 content-w-1/5"
        />
      )}

      {content?.specific?.titleFormatting?.title && (
        <h1
          className={
            content?.specific?.titleFormatting?.titleHighContrastMode
              ? 'content-rounded content-bg-black content-px-2 content-py-0.5 content-shadow-md'
              : ''
          }
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
            'content-mt-5 content-font-medium',
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
            'content-mt-4',
            content?.specific?.paragraphFormatting?.paragraphHighContrastMode &&
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
    </>
  );
};

export default BlockTypedInputWithFeatureImage;
