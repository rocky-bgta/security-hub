import { CardSkeleton } from 'components/LoadingSkeleton';
import { IEmailTemplate, TemplateType } from 'models/EmailTemplate';
import EmailTemplateCard from './EmailTemplateCard';

interface EmailTemplateGridProps {
  templates: IEmailTemplate[];
  loading: boolean;
  templateType?: TemplateType;
  onPreview: (template: IEmailTemplate) => void;
  onEdit: (template: IEmailTemplate) => void;
  onDuplicate: (template: IEmailTemplate) => void;
  onDelete: (template: IEmailTemplate) => void;
}

/**
 * Grid layout component for displaying email template cards
 */
const EmailTemplateGrid = ({
  templates,
  loading,
  templateType = TemplateType.EMAIL,
  onPreview,
  onEdit,
  onDuplicate,
  onDelete,
}: EmailTemplateGridProps) => {
  const isSms = templateType === TemplateType.SMS;

  if (loading) {
    return <CardSkeleton count={12} />;
  }

  if (templates.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <span className="mb-4 text-6xl">{isSms ? '📱' : '📧'}</span>
        <h3 className="mb-2 text-lg font-medium text-primary">
          No templates found
        </h3>
        <p className="text-center text-muted-foreground">
          No {isSms ? 'SMS' : 'email'} templates match your current filters.
          <br />
          Try adjusting your search or create a new template.
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {templates.map(template => (
        <EmailTemplateCard
          key={template.templateId}
          template={template}
          templateType={templateType}
          onPreview={onPreview}
          onEdit={onEdit}
          onDuplicate={onDuplicate}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
};

export default EmailTemplateGrid;
