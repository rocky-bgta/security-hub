import { sanitizeHtml } from 'home-module/security';
import { Button } from 'common/Button';
import { IContent, IMarkdownContent } from 'models/Content';

interface IProps {
  content: IContent<IMarkdownContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const Markdown = ({
  content,
  isInteractive = false,
  handleNextContent,
}: IProps) => {
  return (
    <>
      {isInteractive && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}
      <div
        className="content-p-6"
        dangerouslySetInnerHTML={{
          __html: sanitizeHtml(content.specific.markdownText || ''),
        }}
      />
    </>
  );
};

export default Markdown;
