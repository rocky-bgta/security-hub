import { safeExecCommand, sanitizeHtml } from 'home-module/security';
import { Button } from 'common/Button';
import {
  AlignCenter,
  AlignLeft,
  AlignRight,
  Bold,
  Code,
  Eye,
  Italic,
  Link,
  List,
  ListOrdered,
  Underline,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { cn } from 'utils/Helper';

interface RichTextEditorProps {
  value: string;
  onChange: (html: string) => void;
  textValue?: string;
  onTextChange?: (text: string) => void;
  placeholder?: string;
  className?: string;
  error?: string;
}

type EditorMode = 'visual' | 'html';

/**
 * Rich text editor with visual and HTML modes
 * Based on Task-03 requirements for email body composition
 */
const RichTextEditor = ({
  value,
  onChange,
  textValue: _textValue,
  onTextChange,
  placeholder = 'Compose your email body here...',
  className,
  error,
}: RichTextEditorProps) => {
  const [mode, setMode] = useState<EditorMode>('visual');
  const [htmlContent, setHtmlContent] = useState(value);

  useEffect(() => {
    setHtmlContent(value);
  }, [value]);

  const handleModeToggle = (newMode: EditorMode) => {
    setMode(newMode);
  };

  const handleHtmlChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newHtml = e.target.value;
    setHtmlContent(newHtml);
    onChange(newHtml);
  };

  const handleVisualChange = (e: React.FormEvent<HTMLDivElement>) => {
    const newHtml = e.currentTarget.innerHTML;
    setHtmlContent(newHtml);
    onChange(newHtml);
    // Extract plain text for text version
    if (onTextChange) {
      onTextChange(e.currentTarget.innerText);
    }
  };

  const execCommand = (command: string, value?: string) => {
    safeExecCommand(document, command, value);
  };

  const insertLink = () => {
    const url = prompt('Enter URL:');
    if (url) {
      execCommand('createLink', url);
    }
  };

  return (
    <div
      className={cn(
        'rounded-lg border border-card-border bg-card-background',
        className,
      )}
    >
      {/* Toolbar */}
      <div className="flex flex-wrap items-center gap-1 border-b border-card-border p-2">
        {/* Mode toggle */}
        <div className="mr-2 flex rounded-md border border-card-border">
          <Button
            type="button"
            variant={mode === 'visual' ? 'default' : 'ghost'}
            size="sm"
            onClick={() => handleModeToggle('visual')}
            className="rounded-r-none"
          >
            <Eye className="mr-1 size-4" />
            Visual
          </Button>
          <Button
            type="button"
            variant={mode === 'html' ? 'default' : 'ghost'}
            size="sm"
            onClick={() => handleModeToggle('html')}
            className="rounded-l-none"
          >
            <Code className="mr-1 size-4" />
            HTML
          </Button>
        </div>

        {mode === 'visual' && (
          <>
            <div className="mx-2 h-6 w-px bg-card-border" />

            {/* Formatting buttons */}
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => execCommand('bold')}
              title="Bold"
            >
              <Bold className="size-4" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => execCommand('italic')}
              title="Italic"
            >
              <Italic className="size-4" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => execCommand('underline')}
              title="Underline"
            >
              <Underline className="size-4" />
            </Button>

            <div className="mx-2 h-6 w-px bg-card-border" />

            {/* Alignment */}
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => execCommand('justifyLeft')}
              title="Align Left"
            >
              <AlignLeft className="size-4" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => execCommand('justifyCenter')}
              title="Align Center"
            >
              <AlignCenter className="size-4" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => execCommand('justifyRight')}
              title="Align Right"
            >
              <AlignRight className="size-4" />
            </Button>

            <div className="mx-2 h-6 w-px bg-card-border" />

            {/* Lists */}
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => execCommand('insertUnorderedList')}
              title="Bullet List"
            >
              <List className="size-4" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => execCommand('insertOrderedList')}
              title="Numbered List"
            >
              <ListOrdered className="size-4" />
            </Button>

            <div className="mx-2 h-6 w-px bg-card-border" />

            {/* Link */}
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={insertLink}
              title="Insert Link"
            >
              <Link className="size-4" />
            </Button>
          </>
        )}
      </div>

      {/* Editor content */}
      <div className="min-h-[300px]">
        {mode === 'visual' ? (
          <div
            contentEditable
            className="min-h-[300px] p-4 text-primary outline-none"
            dangerouslySetInnerHTML={{ __html: sanitizeHtml(htmlContent) }}
            onInput={handleVisualChange}
            data-placeholder={placeholder}
          />
        ) : (
          <textarea
            value={htmlContent}
            onChange={handleHtmlChange}
            placeholder={placeholder}
            className="size-full min-h-[300px] resize-none bg-transparent p-4 font-mono text-sm text-primary outline-none"
          />
        )}
      </div>

      {/* Error message */}
      {error && <p className="px-4 pb-2 text-sm text-vibrant-red">{error}</p>}

      {/* Variable hints */}
      <div className="border-t border-primary px-4 py-2">
        <p className="text-xs text-muted-foreground">
          <span className="font-medium">Available variables: </span>
          {
            '{{FIRST_NAME}}, {{LAST_NAME}}, {{EMAIL_ADDRESS}}, {{COMPANY}}, {{DEPARTMENT}}, {{PHISHING_LINK}}'
          }
        </p>
      </div>
    </div>
  );
};

export default RichTextEditor;
