import { sanitizeHtml } from 'home-module/security';
import { Button } from 'common/Button';
import { IContent, IMarkdownContent } from 'models/Content';

interface IProps {
  content: IContent<IMarkdownContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const Markdown = ({ content, isInteractive, handleNextContent }: IProps) => {
  return (
    <>
      {isInteractive && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}

      <div>
        <div
          className="content-max-h-[600px] content-overflow-y-auto content-p-5 content-text-white"
          dangerouslySetInnerHTML={{
            __html: sanitizeHtml(content.specific.markdownText || ''),
          }}
        />
      </div>
    </>
  );
};

export default Markdown;
