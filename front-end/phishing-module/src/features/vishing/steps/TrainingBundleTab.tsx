import { Step7Training } from 'features/campaign/CampaignWizard/Step7Training';
import { useVishingWizard } from 'features/vishing/context/VishingWizardContext';
import { useVishingCampaigns } from 'hooks/UseVishingCampaigns';
import {
  CampaignChannel,
  type ICampaignTrainingForm,
} from 'models/Campaign';
import type { IVishingTrainingRequest } from 'models/Vishing';
import { toast } from 'react-toastify';
import { isSuccessResponse } from 'utils/Helper';

const mapTrainingFormToRequest = (
  data: ICampaignTrainingForm,
): IVishingTrainingRequest => ({
  name: data.name,
  subPackageName: data.subPackageName,
  description: data.description,
  productId: data.productId,
  packageId: data.packageId,
  productPackageId: data.productPackageId,
  clientId: data.clientId,
  topicId: data.topicId,
  topicIdDetails: data.topicIdDetails,
  trainingModuleId: data.topicId?.[0] ?? data.trainingModuleId,
  completionDays: data.completionDays
    ? {
        durationUnit: data.completionDays.durationUnit,
        durationValue: data.completionDays.durationValue,
      }
    : undefined,
});

export function TrainingBundleTab({
  onBack,
  onContinue,
  isLoading,
  productPackageId,
}: {
  onBack: () => void;
  onContinue: () => void;
  isLoading?: boolean;
  productPackageId?: string;
}) {
  const { campaignId, training, setTraining } = useVishingWizard();
  const { updateTraining, saving } = useVishingCampaigns();

  const handleSubmit = async (data: ICampaignTrainingForm) => {
    if (!campaignId) {
      toast.error('Campaign not found. Please complete setup first.');
      return;
    }

    const payload = mapTrainingFormToRequest(data);
    const response = await updateTraining(campaignId, payload);
    if (!isSuccessResponse(response?.statusCode ?? 0)) {
      toast.error(response?.message ?? 'Failed to save training configuration');
      return;
    }

    setTraining(payload);
    onContinue();
  };

  const initialData =
    training.productId ||
    training.packageId ||
    training.trainingModuleId ||
    training.subPackageName ||
    (training.topicId?.length ?? 0) > 0
      ? {
          name: training.name,
          subPackageName: training.subPackageName,
          description: training.description,
          productId: training.productId,
          packageId: training.packageId,
          productPackageId: training.productPackageId,
          selectedModuleIds:
            training.topicIdDetails?.map(item => ({
              id: item.tid,
              topicName: item.topicName ?? '',
            })) ??
            training.topicId?.map(id => ({ id })) ??
            (training.trainingModuleId
              ? [{ id: training.trainingModuleId }]
              : []),
          trainingModuleId: training.trainingModuleId,
          completionDays: training.completionDays
            ? {
                durationUnit: training.completionDays.durationUnit,
                durationValue: training.completionDays.durationValue,
              }
            : undefined,
        }
      : undefined;

  return (
    <Step7Training
      campaignId={campaignId ?? undefined}
      productPackageId={productPackageId}
      initialData={initialData}
      onSubmit={data => {
        void handleSubmit(data);
      }}
      onBack={onBack}
      isLoading={isLoading || saving}
      channel={CampaignChannel.VOICE}
    />
  );
}
