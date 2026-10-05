import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Step5Tags } from 'features/campaign/CampaignWizard/Step5Tags';
import { Step6Audience } from 'features/campaign/CampaignWizard/Step6Audience';
import { Step7Training } from 'features/campaign/CampaignWizard/Step7Training';
import { Step8Schedule } from 'features/campaign/CampaignWizard/Step8Schedule';
import { SmsWizardStepper } from 'features/sms-campaign/SmsCampaignWizard/SmsWizardStepper';
import { Step1Setup } from 'features/sms-campaign/SmsCampaignWizard/Step1Setup';
import { Step2SmsTemplate } from 'features/sms-campaign/SmsCampaignWizard/Step2SmsTemplate';
import { Step3LandingPage } from 'features/sms-campaign/SmsCampaignWizard/Step3LandingPage';
import { Step4SmsServer } from 'features/sms-campaign/SmsCampaignWizard/Step4SmsServer';
import { Step9Review } from 'features/sms-campaign/SmsCampaignWizard/Step9Review';
import { useSmsCampaigns } from 'hooks/UseSmsCampaigns';
import {
  ICampaignAudienceForm,
  ICampaignScheduleForm,
  ICampaignTrainingForm,
} from 'models/Campaign';
import {
  CampaignChannel,
  CampaignLearningMode,
  CampaignStatus,
  CampaignType,
  ICampaign,
} from 'models/Campaign';
import { routes } from 'routes/Routes';
import {
  getCampaignListUrl,
  resolveWizardStepFromQuery,
} from 'utils/ListNavigation';
import {
  mapCampaignScheduleToForm,
  TCampaignScheduleForm,
} from 'schemas/CampaignSchema';
import { TSmsCampaignSetupForm } from 'schemas/SmsCampaignSchema';
import { Button } from 'common/Button';
import IconBackButton from 'components/IconBackButton';
import { isSuccessResponse } from 'utils/Helper';

const CreateSmsCampaign = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const isEditMode = !!id;

  const {
    getCampaignById,
    createCampaign,
    updateStep1,
    updateStep2,
    updateStep3,
    updateStep4,
    updateStep5,
    updateStep6,
    updateStep7,
    updateStep8,
    launchCampaign,
    loading,
    saving,
    launching,
  } = useSmsCampaigns();

  const [campaign, setCampaign] = useState<ICampaign | null>(null);
  const [currentStep, setCurrentStep] = useState(1);
  const [step8Draft, setStep8Draft] = useState<
    Partial<TCampaignScheduleForm> | undefined
  >();
  const [campaignType, setCampaignType] = useState<CampaignType>(
    CampaignType.SMISHING_SIMULATION,
  );

  const step7InitialData = useMemo(() => {
    if (!campaign) return undefined;
    return {
      name: campaign.trainingData?.name,
      subPackageName: campaign.trainingData?.subPackageName,
      description: campaign.trainingData?.description,
      productId: campaign.trainingData?.productId,
      packageId: campaign.trainingData?.packageId,
      productPackageId: campaign.trainingData?.productPackageId,
      selectedModuleIds:
        campaign.trainingData?.topicIdDetails?.map(item => ({
          id: item.tid,
          topicName: item.topicName ?? '',
        })) ?? [],
      trainingModuleId: campaign.trainingData?.trainingModuleId,
      completionDays: {
        durationUnit: campaign.trainingData?.completionDays?.durationUnit,
        durationValue: campaign.trainingData?.completionDays?.durationValue,
      },
    };
  }, [campaign]);

  const step8InitialData = useMemo(() => {
    if (step8Draft) return step8Draft;
    if (!campaign?.schedule) return undefined;
    return mapCampaignScheduleToForm(campaign.schedule);
  }, [step8Draft, campaign]);

  useEffect(() => {
    if (isEditMode && id) {
      getCampaignById(id).then(data => {
        if (data) {
          if (
            data.status === CampaignStatus.RUNNING ||
            data.status === CampaignStatus.COMPLETED ||
            data.status === CampaignStatus.PAUSED
          ) {
            navigate(
              routes.smishingCampaignDetails.path.replace(
                ':id',
                data.campaignId,
              ),
              { replace: true },
            );
            return;
          }
          setStep8Draft(undefined);
          setCampaign(data);
          setCurrentStep(
            resolveWizardStepFromQuery(
              data.currentStep || 1,
              Number(searchParams.get('step')),
            ),
          );
          setCampaignType(data.campaignType);
        } else {
          toast.error('SMS campaign not found');
          navigate(routes.smishingSimulationCreate.path);
        }
      });
    } else if (!isEditMode) {
      setTimeout(() => {
        setStep8Draft(undefined);
        setCurrentStep(1);
        setCampaign(null);
        setCampaignType(CampaignType.SMISHING_SIMULATION);
      }, 0);
    }
  }, [id, isEditMode, getCampaignById, navigate, searchParams]);

  const getNextStep = (current: number): number => {
    if (current === 6 && campaignType === CampaignType.SMISHING_SIMULATION) {
      return 8;
    }
    return current + 1;
  };

  const getPreviousStep = (current: number): number => {
    if (current === 8 && campaignType === CampaignType.SMISHING_SIMULATION) {
      return 6;
    }
    return current - 1;
  };

  const handleStep1Submit = async (data: TSmsCampaignSetupForm) => {
    setCampaignType(data.campaignType);

    if (campaign) {
      const result = await updateStep1(campaign.campaignId, data);
      if (result) {
        if (isSuccessResponse(result.statusCode) && result.data) {
          setCampaign(result.data);
          setCurrentStep(getNextStep(1));
        } else {
          toast.error(result.message);
        }
      }
    } else {
      const result = await createCampaign(data);
      if (result) {
        if (isSuccessResponse(result.statusCode) && result.data) {
          setCampaign(result.data);
          setCurrentStep(getNextStep(1));
        } else {
          toast.error(result.message);
        }
      }
    }
  };

  const handleStep2Submit = async (
    templateId: string,
    templateName: string,
  ) => {
    if (!campaign) return;
    const result = await updateStep2(campaign.campaignId, {
      emailTemplateId: templateId,
    });
    if (result) {
      if (isSuccessResponse(result.statusCode) && result.data) {
        setCampaign({
          ...result.data,
          emailTemplateName: result.data.emailTemplateName ?? templateName,
        });
        setCurrentStep(getNextStep(2));
      } else {
        toast.error(result.message);
      }
    }
  };

  const handleStep3Submit = async (
    pageId: string | undefined,
    pageType: string,
    landingPageName: string,
  ) => {
    if (!campaign) return;
    const result = await updateStep3(campaign.campaignId, {
      landingPageId: pageId,
      landingPageType: pageType,
    });
    if (result) {
      if (isSuccessResponse(result.statusCode) && result.data) {
        setCampaign({
          ...result.data,
          landingPageName: result.data.landingPageName ?? landingPageName,
        });
        setCurrentStep(getNextStep(3));
      } else {
        toast.error(result.message);
      }
    }
  };

  const handleStep4Submit = async (
    configurationId: string,
    configurationName: string,
  ) => {
    if (!campaign) return;
    const result = await updateStep4(campaign.campaignId, {
      smsServerConfigurationId: configurationId,
    });
    if (result) {
      if (isSuccessResponse(result.statusCode) && result.data) {
        setCampaign({
          ...result.data,
          smsServerConfigurationName:
            result.data.smsServerConfigurationName ?? configurationName,
        });
        setCurrentStep(getNextStep(4));
      } else {
        toast.error(result.message);
      }
    }
  };

  const handleStep5Submit = async (tags: string[]) => {
    if (!campaign) return;
    const result = await updateStep5(campaign.campaignId, { tags });
    if (result) {
      if (isSuccessResponse(result.statusCode) && result.data) {
        setCampaign(result.data);
        setCurrentStep(getNextStep(5));
      } else {
        toast.error(result.message);
      }
    }
  };

  const handleStep6Submit = async (data: ICampaignAudienceForm) => {
    if (!campaign) return;
    const result = await updateStep6(campaign.campaignId, data);
    if (result) {
      if (isSuccessResponse(result.statusCode) && result.data) {
        setCampaign(result.data);
        setCurrentStep(getNextStep(6));
      } else {
        toast.error(result.message);
      }
    }
  };

  const handleAudienceAllocated = async () => {
    if (!campaign) return null;
    const data = await getCampaignById(campaign.campaignId, { silent: true });
    if (data) setCampaign(data);
    return data;
  };

  const handleStep7Submit = async (data: ICampaignTrainingForm) => {
    if (!campaign) return;
    const result = await updateStep7(campaign.campaignId, data);
    if (result) {
      if (isSuccessResponse(result.statusCode) && result.data) {
        setCampaign(result.data);
        setCurrentStep(getNextStep(7));
      } else {
        toast.error(result.message);
      }
    }
  };

  const handleStep8Submit = async (data: ICampaignScheduleForm) => {
    if (!campaign) return;
    const result = await updateStep8(campaign.campaignId, data);
    if (result) {
      if (isSuccessResponse(result.statusCode) && result.data) {
        setStep8Draft(undefined);
        setCampaign(result.data);
        setCurrentStep(getNextStep(8));
      } else {
        toast.error(result.message);
      }
    }
  };

  const handleLaunch = async () => {
    if (!campaign) return;
    const result = await launchCampaign(campaign.campaignId);
    if (result) {
      if (isSuccessResponse(result.statusCode)) {
        toast.success('SMS campaign launched successfully!');
        const campaignId = result.data?.campaignId ?? campaign.campaignId;
        navigate(
          routes.smishingCampaignDetails.path.replace(':id', campaignId),
        );
      } else {
        toast.error(result.message);
      }
    }
  };

  const handleSaveAsDraft = () => {
    toast.success('SMS campaign saved as draft');
    navigate(routes.smishingSimulationCreate.path);
  };

  const handleStepClick = (step: number) => {
    if (campaign && step <= (campaign.currentStep || 1)) {
      setCurrentStep(step);
    }
  };

  const handleBack = () => {
    if (currentStep === 1) {
      if (isEditMode) {
        navigate(getCampaignListUrl(CampaignChannel.SMS));
      } else {
        navigate(routes.smishingSimulationCreate.path);
      }
    } else {
      setCurrentStep(getPreviousStep(currentStep));
    }
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <Step1Setup
            initialData={
              campaign
                ? {
                    campaignName: campaign.campaignName,
                    campaignType: campaign.campaignType,
                    channel: CampaignChannel.SMS,
                    productPackageId: campaign.productPackageId ?? '',
                    assignedFor: campaign.assignedFor,
                    learningMode:
                      campaign.learningMode ??
                      CampaignLearningMode.MICRO_CONTENT,
                    ...(campaign.expireDate && {
                      expireDate: {
                        validityUnit: campaign.expireDate.validityUnit,
                        validityPeriod: campaign.expireDate.validityPeriod,
                      },
                    }),
                  }
                : undefined
            }
            onSubmit={handleStep1Submit}
            onBack={() =>
              isEditMode
                ? navigate(getCampaignListUrl(CampaignChannel.SMS))
                : navigate(routes.smishingSimulationCreate.path)
            }
            isLoading={saving}
          />
        );
      case 2:
        return (
          <Step2SmsTemplate
            selectedTemplate={campaign ?? undefined}
            onSubmit={handleStep2Submit}
            onBack={handleBack}
            isLoading={saving}
          />
        );
      case 3:
        return (
          <Step3LandingPage
            isEditMode={isEditMode}
            emailTemplateId={campaign?.emailTemplateId}
            selectedPage={campaign ?? undefined}
            onSubmit={handleStep3Submit}
            onBack={handleBack}
            isLoading={saving}
          />
        );
      case 4:
        return (
          <Step4SmsServer
            selectedConfigurationId={campaign?.smsServerConfigurationId}
            onSubmit={handleStep4Submit}
            onBack={handleBack}
            isLoading={saving}
          />
        );
      case 5:
        return (
          <Step5Tags
            initialTags={campaign?.campaignTags}
            onSubmit={handleStep5Submit}
            onBack={handleBack}
            isLoading={saving}
          />
        );
      case 6:
        return (
          <Step6Audience
            campaignId={campaign?.campaignId}
            productPackageId={campaign?.productPackageId}
            initialData={
              campaign?.audience
                ? {
                    audienceType: campaign.audience.type,
                    departmentIds: campaign.audience.departmentIds,
                    groupIds: campaign.audience.groupIds,
                    userIds: campaign.audience.userIds,
                  }
                : undefined
            }
            onSubmit={handleStep6Submit}
            onBack={handleBack}
            onAudienceAllocated={handleAudienceAllocated}
            isLoading={saving}
          />
        );
      case 7:
        return (
          <Step7Training
            campaignId={campaign?.campaignId}
            productPackageId={campaign?.productPackageId}
            initialData={step7InitialData}
            onSubmit={handleStep7Submit}
            onBack={handleBack}
            isLoading={saving}
            channel={CampaignChannel.SMS}
          />
        );
      case 8:
        return (
          <Step8Schedule
            initialData={step8InitialData}
            onSubmit={handleStep8Submit}
            onBack={handleBack}
            onPersistDraft={setStep8Draft}
            isLoading={saving}
          />
        );
      case 9:
        return campaign ? (
          <Step9Review
            campaign={campaign}
            onLaunch={handleLaunch}
            onSaveAsDraft={handleSaveAsDraft}
            onEditStep={handleStepClick}
            onBack={handleBack}
            isLaunching={launching}
            isSaving={saving}
          />
        ) : null;
      default:
        return null;
    }
  };

  if (isEditMode && loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <svg
            className="mx-auto mb-4 size-8 animate-spin text-blue-600"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          <p className="text-gray-600">Loading SMS campaign...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <div className="space-y-3">
        <IconBackButton
          onClick={() =>
            isEditMode
              ? navigate(getCampaignListUrl(CampaignChannel.SMS))
              : navigate(routes.smishingDashboard.path)
          }
          label={isEditMode ? 'Back to Campaigns' : 'Back to Dashboard'}
        />
        <div className="mx-auto flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-foreground">
              {isEditMode ? 'Edit SMS Campaign' : 'Create SMS Campaign'}
            </h1>
            {campaign && (
              <p className="text-sm text-muted-foreground">
                {campaign.campaignName}
              </p>
            )}
          </div>
          {campaign?.status === CampaignStatus.DRAFT && (
            <Button
              onClick={handleSaveAsDraft}
              className="rounded-lg py-2 text-sm text-white"
            >
              Save & Exit
            </Button>
          )}
        </div>
      </div>

      <SmsWizardStepper
        currentStep={currentStep}
        completedStep={campaign?.currentStep || 0}
        campaignType={campaignType}
        onStepClick={handleStepClick}
      />

      <div>{renderStep()}</div>
    </div>
  );
};

export default CreateSmsCampaign;
