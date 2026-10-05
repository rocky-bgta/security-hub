import { Button } from 'common/Button';
import BlockTypedInputWithFeatureImage from 'components/BlockTypedInputWithFeatureImage';
import { IContent, ILinkContent } from 'models/Content';

interface IProps {
  content: IContent<ILinkContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const LinkBlock = ({ content, isInteractive, handleNextContent }: IProps) => {
  const formattedUrl = content?.specific?.linkFormatting?.url?.startsWith(
    'http',
  )
    ? content?.specific?.linkFormatting?.url
    : `https://${content?.specific?.linkFormatting?.url}`;

  return (
    <>
      {isInteractive && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}

      <div className="content-max-h-[600px] content-overflow-y-auto content-p-5">
        <BlockTypedInputWithFeatureImage content={content} />

        {content?.specific?.linkFormatting?.linkText && (
          <a
            href={formattedUrl}
            target="_blank"
            rel="noopener noreferrer"
            style={{ color: content?.specific?.linkFormatting?.linkColor }}
            className="content-mt-3 content-block content-underline"
          >
            {content?.specific?.linkFormatting?.linkText}
          </a>
        )}
      </div>
    </>
  );
};

export default LinkBlock;
