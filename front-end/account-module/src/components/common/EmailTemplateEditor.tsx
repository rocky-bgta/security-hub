import HtmlEditor from 'components/common/HtmlEditor';
import type { HtmlEditorProps } from 'models/HtmlEditor';

export type EmailTemplateEditorProps = HtmlEditorProps;

const EmailTemplateEditor = (props: EmailTemplateEditorProps) => {
  return <HtmlEditor iframeTitle="Email Template Editor" {...props} />;
};

export default EmailTemplateEditor;
