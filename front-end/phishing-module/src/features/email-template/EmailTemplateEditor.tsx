import HtmlEditor from 'components/common/HtmlEditor';
import type { IEmailTemplateFixHtmlRequest } from 'models/EmailTemplate';
import type { AIElementGenerateParams, HtmlEditorProps } from 'models/HtmlEditor';

export interface EmailTemplateEditorProps
  extends Omit<
    HtmlEditorProps,
    'onAIGenerate' | 'features' | 'renderCaptureFormModal'
  > {
  onAIGenerate?: (
    params: IEmailTemplateFixHtmlRequest,
  ) => Promise<string | undefined>;
}

const EmailTemplateEditor = ({
  onAIGenerate,
  ...props
}: EmailTemplateEditorProps) => {
  return (
    <HtmlEditor
      iframeTitle="Email Template Editor"
      {...props}
      onAIGenerate={
        onAIGenerate
          ? async (params: AIElementGenerateParams) =>
              onAIGenerate({
                providerType:
                  params.providerType as IEmailTemplateFixHtmlRequest['providerType'],
                model: params.model,
                prompt: params.prompt,
                elementHtml: params.elementHtml,
                templateCode: params.templateCode,
              })
          : undefined
      }
    />
  );
};

export default EmailTemplateEditor;
