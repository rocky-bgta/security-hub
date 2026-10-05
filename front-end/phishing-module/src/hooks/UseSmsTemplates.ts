import { TemplateType } from 'models/EmailTemplate';
import useEmailTemplates from './UseEmailTemplates';

export const useSmsTemplates = () =>
  useEmailTemplates({ templateType: TemplateType.SMS });

export default useSmsTemplates;
