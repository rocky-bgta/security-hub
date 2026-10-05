import { Editor } from '@tinymce/tinymce-react';
import { useCallback, useState } from 'react';

import { TINYMCE_URL } from 'utils/Constants';
import { cn } from 'utils/Helper';

interface IProps {
  value: string;
  onChange: (value: string) => void;
  className?: string;
  toolbar?: string;
  isDark?: boolean;
}

const TextEditor = ({
  value,
  onChange,
  toolbar = 'undo redo fontsize bold italic forecolor backcolor emoticons blockquote alignleft aligncenter alignright alignjustify bullist numlist outdent indent link image media code preview fullscreen',
  className,
  isDark = false,
}: IProps) => {
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const handleEditorChange = useCallback(
    (content: string) => onChange(content),
    [onChange],
  );

  return (
    <div className={cn('custom-text-editor', className)}>
      {!isLoading && (
        <div className="home-flex home-h-[500px] home-items-center home-justify-center home-rounded-[10px] home-bg-muted home-text-sm home-text-muted-foreground">
          Loading editor…
        </div>
      )}
      <Editor
        tinymceScriptSrc={TINYMCE_URL + '/tinymce/tinymce.min.js'}
        value={value}
        onEditorChange={handleEditorChange}
        licenseKey="gpl"
        onInit={() => setIsLoading(true)}
        init={{
          skin: isDark ? 'oxide-dark' : 'oxide',
          content_css: isDark ? 'dark' : 'default',

          height: 500,
          menubar: false,
          branding: false,
          plugins: [
            'anchor',
            'autolink',
            'code',
            'emoticons',
            'fullscreen',
            'help',
            'image',
            'insertdatetime',
            'link',
            'lists',
            'media',
            'preview',
            'table',
            'wordcount',
          ],
          fullscreen_native: true,
          convert_urls: false,
          link_quicklink: true,
          link_context_toolbar: true,
          toolbar: toolbar,
          toolbar_mode: 'wrap',
          elementpath: false,
          statusbar: false,
          promotion: false,
        }}
      />
    </div>
  );
};

export default TextEditor;
