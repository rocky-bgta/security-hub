import { BlankTabIcon, LinkIcon } from 'assets/icons';
import { Button } from 'common/Button';
import UserBlockTypedInputWithFeatureImage from 'components/UserBlockTypedInputWithFeatureImage';
import { AllContentTypes, IContent, ILinkContent } from 'models/Content';
interface IProps {
  content: IContent<ILinkContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
  showCompleteButton?: (type: AllContentTypes) => void;
  courseComplete?: () => void;
}

const LinkBased = ({
  content,
  isInteractive = false,
  handleNextContent,
  showCompleteButton,
  courseComplete,
}: IProps) => {
  const formattedUrl = content?.specific?.linkFormatting?.url?.startsWith(
    'http',
  )
    ? content?.specific?.linkFormatting?.url
    : `https://${content?.specific?.linkFormatting?.url}`;

  const handleLinkClick = () => {
    showCompleteButton?.(content.common.contentType);
    courseComplete?.();
  };
  return (
    <>
      {isInteractive && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}

      <UserBlockTypedInputWithFeatureImage content={content} />

      <div className="content-mx-6 content-pb-4">
        <a
          onClick={handleLinkClick}
          href={formattedUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="content-mt-4 content-flex content-w-fit content-items-center content-justify-center content-gap-2 content-rounded content-bg-[#EFF4FB40] content-p-3 content-underline"
          style={{
            color: content?.specific?.backgroundFormatting?.textColor,
          }}
        >
          <LinkIcon />
          {content?.specific?.linkFormatting?.linkText}
          <BlankTabIcon />
        </a>
      </div>
    </>
  );
};

export default LinkBased;
