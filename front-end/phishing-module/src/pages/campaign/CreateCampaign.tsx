import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Step1Setup } from 'features/campaign/CampaignWizard/Step1Setup';
import { Step2EmailTemplate } from 'features/campaign/CampaignWizard/Step2EmailTemplate';
import { Step3LandingPage } from 'features/campaign/CampaignWizard/Step3LandingPage';
import { Step4MailServer } from 'features/campaign/CampaignWizard/Step4MailServer';
import { Step5Tags } from 'features/campaign/CampaignWizard/Step5Tags';
import { Step6Audience } from 'features/campaign/CampaignWizard/Step6Audience';
import { Step7Training } from 'features/campaign/CampaignWizard/Step7Training';
import { Step8Schedule } from 'features/campaign/CampaignWizard/Step8Schedule';
import { Step9Review } from 'features/campaign/CampaignWizard/Step9Review';
import { WizardStepper } from 'features/campaign/CampaignWizard/WizardStepper';
import { useCampaigns } from 'hooks/UseCampaigns';
import {
  CampaignChannel,
  CampaignStatus,
  CampaignType,
  ICampaign,
  ICampaignAudienceForm,
  ICampaignScheduleForm,
  ICampaignTrainingForm,
  WIZARD_STEPS,
} from 'models/Campaign';
import { routes } from 'routes/Routes';
import { getCampaignListUrl, resolveWizardStepFromQuery } from 'utils/ListNavigation';
import {
  mapCampaignScheduleToForm,
  TCampaignScheduleForm,
  TCampaignSetupForm,
} from 'schemas/CampaignSchema';
import { Button } from 'common/Button';
import IconBackButton from 'components/IconBackButton';
import { isSuccessResponse } from 'utils/Helper';

const CreateCampaign = () => {
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
  } = useCampaigns();

  const [campaign, setCampaign] = useState<ICampaign | null>(null);
  const [currentStep, setCurrentStep] = useState(1);
  const [step8Draft, setStep8Draft] = useState<
    Partial<TCampaignScheduleForm> | undefined
  >();
  const [campaignType, setCampaignType] = useState<CampaignType>(
    CampaignType.SIMULATED_PHISHING,
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

  const visibleSteps = useMemo(
    () =>
      WIZARD_STEPS.filter(
        step =>
          !(
            step.step === 7 && campaignType === CampaignType.SIMULATED_PHISHING
          ),
      ),
    [campaignType],
  );

  useEffect(() => {
    if (isEditMode && id) {
      getCampaignById(id).then(data => {
        if (data) {
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
          toast.error('Campaign not found');
          navigate(routes.phishingCampaignCreate.path);
        }
      });
    } else if (!isEditMode) {
      setTimeout(() => {
        setStep8Draft(undefined);
        setCurrentStep(1);
        setCampaign(null);
        setCampaignType(CampaignType.SIMULATED_PHISHING);
      }, 0);
    }
  }, [id, isEditMode, getCampaignById, navigate, searchParams]);

  const getNextStep = (current: number): number => {
    // Skip training step for SIMULATED_PHISHING
    if (current === 6 && campaignType === CampaignType.SIMULATED_PHISHING) {
      return 8;
    }
    return current + 1;
  };

  const getPreviousStep = (current: number): number => {
    // Skip training step for SIMULATED_PHISHING
    if (current === 8 && campaignType === CampaignType.SIMULATED_PHISHING) {
      return 6;
    }
    return current - 1;
  };

  // Step handlers
  const handleStep1Submit = async (data: TCampaignSetupForm) => {
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

  const handleStep4Submit = async (profileId: string, profileName: string) => {
    if (!campaign) return;
    const result = await updateStep4(campaign.campaignId, {
      senderProfileId: profileId,
    });
    if (result) {
      if (isSuccessResponse(result.statusCode) && result.data) {
        setCampaign({
          ...result.data,
          senderProfileName: result.data.senderProfileName ?? profileName,
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
        toast.success('Campaign launched successfully!');
        navigate(
          routes.phishingCampaignDetails.path.replace(
            ':id',
            campaign.campaignId,
          ),
        );
      } else {
        toast.error(result.message);
      }
    }
  };

  const handleSaveAsDraft = () => {
    toast.success('Campaign saved as draft');
    navigate(routes.phishingCampaignCreate.path);
  };

  const handleStepClick = (step: number) => {
    if (campaign && step <= (campaign.currentStep || 1)) {
      setCurrentStep(step);
    }
  };

  const handleBack = () => {
    if (currentStep === 1) {
      if (isEditMode) {
        navigate(getCampaignListUrl(CampaignChannel.EMAIL));
      } else {
        navigate(routes.phishingCampaignCreate.path);
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
                    productPackageId: campaign.productPackageId ?? '',
                    assignedFor: campaign.assignedFor,
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
                ? navigate(getCampaignListUrl(CampaignChannel.EMAIL))
                : navigate(routes.phishingCampaignCreate.path)
            }
            isLoading={saving}
          />
        );
      case 2:
        return (
          <Step2EmailTemplate
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
          <Step4MailServer
            selectedProfileId={campaign?.senderProfileId}
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
            channel={CampaignChannel.EMAIL}
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
          <p className="text-gray-600">Loading campaign...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      {/* Header */}
      <div className="space-y-3">
        <IconBackButton
          onClick={() =>
            isEditMode
              ? navigate(getCampaignListUrl(CampaignChannel.EMAIL))
              : navigate(routes.phishingDashboard.path)
          }
          label={isEditMode ? 'Back to Campaigns' : 'Back to Dashboard'}
        />
        <div className="mx-auto flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-foreground">
              {isEditMode ? 'Edit Email Campaign' : 'Create Email Campaign'}
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

      {/* Stepper */}
      <WizardStepper
        currentStep={currentStep}
        completedStep={campaign?.currentStep || 0}
        steps={visibleSteps}
        onStepClick={handleStepClick}
      />

      {/* Step Content */}
      <div>{renderStep()}</div>
    </div>
  );
};

export default CreateCampaign;
