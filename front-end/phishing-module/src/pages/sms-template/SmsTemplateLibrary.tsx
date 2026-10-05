import TemplateLibraryContent from 'features/email-template/TemplateLibraryContent';
import { TemplateType } from 'models/EmailTemplate';

const SmsTemplateLibrary = () => (
  <TemplateLibraryContent templateType={TemplateType.SMS} />
);

export default SmsTemplateLibrary;
