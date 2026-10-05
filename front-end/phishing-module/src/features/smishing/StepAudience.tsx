import { Step6Audience } from 'features/campaign/CampaignWizard/Step6Audience';
import { ICampaign } from 'models/Campaign';
import { TCampaignAudienceForm } from 'schemas/CampaignSchema';

interface StepAudienceProps {
  initialData?: Partial<TCampaignAudienceForm>;
  onSubmit: (data: TCampaignAudienceForm) => void;
  onBack: () => void;
  isLoading?: boolean;
  clientAdminId?: string;
  campaignId?: string;
  productPackageId?: string;
  onAudienceAllocated?: () => Promise<ICampaign | null | void>;
}

export default function StepAudience(props: StepAudienceProps) {
  return <Step6Audience {...props} />;
}
