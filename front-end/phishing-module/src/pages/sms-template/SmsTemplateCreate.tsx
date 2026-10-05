import EmailTemplateCreate from 'pages/email-template/EmailTemplateCreate';
import { TemplateType } from 'models/EmailTemplate';

const SmsTemplateCreate = () => (
  <EmailTemplateCreate forcedTemplateType={TemplateType.SMS} />
);

export default SmsTemplateCreate;
