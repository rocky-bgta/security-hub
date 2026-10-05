import HtmlEditor from 'components/common/HtmlEditor';
import type { ILandingPageFixHtmlRequest } from 'models/LandingPage';
import type { AIElementGenerateParams, HtmlEditorProps } from 'models/HtmlEditor';
import CaptureFieldsFormGenerator from './CaptureFieldsFormGenerator';

export interface LandingPageHtmlEditorProps
  extends Omit<
    HtmlEditorProps,
    'onAIGenerate' | 'features' | 'renderCaptureFormModal'
  > {
  onAIGenerate?: (
    params: ILandingPageFixHtmlRequest,
  ) => Promise<string | undefined>;
}

const LandingPageHtmlEditor = ({
  onAIGenerate,
  ...props
}: LandingPageHtmlEditorProps) => {
  return (
    <HtmlEditor
      iframeTitle="Landing Page Editor"
      features={{ captureForm: true }}
      renderCaptureFormModal={modalProps => (
        <CaptureFieldsFormGenerator {...modalProps} />
      )}
      {...props}
      onAIGenerate={
        onAIGenerate
          ? async (params: AIElementGenerateParams) =>
              onAIGenerate({
                providerType: params.providerType as ILandingPageFixHtmlRequest['providerType'],
                model: params.model,
                input: {
                  prompt: params.prompt,
                  elementHtml: params.elementHtml,
                  templateCode: params.templateCode,
                },
              })
          : undefined
      }
    />
  );
};

export default LandingPageHtmlEditor;
