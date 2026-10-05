import TemplateLibraryContent from 'features/email-template/TemplateLibraryContent';
import { TemplateType } from 'models/EmailTemplate';
const EmailTemplateLibrary = () => (
  <TemplateLibraryContent templateType={TemplateType.EMAIL} />
);

export default EmailTemplateLibrary;
