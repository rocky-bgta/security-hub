import CertificateStatistics from 'features/dashboard/Certificates';
import DashboardSummary from 'features/dashboard/DashboardSummary';
import EmailPhishing from 'features/dashboard/EmailPhishing';
import LatestNews from 'features/dashboard/LatestNews';
import LeaderBoard from 'features/dashboard/LeaderBoard';
import PoolsAndSurvey from 'features/dashboard/PoolsAndSurvey';
import SmsPhishing from 'features/dashboard/SmsPhishing';
import TopicsProgress from 'features/dashboard/TopicsProgress';
import TopicsStatistics from 'features/dashboard/TopicsStatistics';
import Transcripts from 'features/dashboard/Transcripts';
import UserActivities from 'features/dashboard/UserActivities';
import VoicePhishing from 'features/dashboard/VoicePhishing';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { IPhishingCampaignEnrollment } from 'models/Phishing';
import ClientUserCourseList from 'pages/course/ClientUserList';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { cn } from 'utils/Helper';

interface IProps {
  hostPath: typeof routes;
}

const INITIAL_ENROLLMENT: IPhishingCampaignEnrollment = {
  hasReceivedCampaign: false,
  hasReceivedEmailCampaign: false,
  hasReceivedSmsCampaign: false,
  hasReceivedVoiceCampaign: false,
};

const ClientUserDashboard = ({ hostPath }: IProps) => {
  const { userInfo } = useStore();
  const [enrollment, setEnrollment] =
    useState<IPhishingCampaignEnrollment>(INITIAL_ENROLLMENT);
  const apiClient = useAPI();

  const checkIsPhishing = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.HAS_RECEIVED_CAMPAIGN,
      );
      setEnrollment({
        hasReceivedCampaign: Boolean(response.data.hasReceivedCampaign),
        hasReceivedEmailCampaign: Boolean(
          response.data.hasReceivedEmailCampaign,
        ),
        hasReceivedSmsCampaign: Boolean(response.data.hasReceivedSmsCampaign),
        hasReceivedVoiceCampaign: Boolean(
          response.data.hasReceivedVoiceCampaign,
        ),
      });
    } catch (error) {
      console.error('Error fetching campaign enrollment status:', error);
    }
  };

  useEffect(() => {
    setTimeout(() => {
      checkIsPhishing();
    }, 0);
  }, []);

  const phishingCardCount = [
    enrollment.hasReceivedEmailCampaign,
    enrollment.hasReceivedSmsCampaign,
    enrollment.hasReceivedVoiceCampaign,
  ].filter(Boolean).length;

  return (
    <div className="content-space-y-4 md:content-space-y-6">
      <div className="content-mb-4 md:content-mb-6">
        <h1 className="content-text-xl content-font-semibold content-text-white sm:content-text-2xl">
          Welcome Back, {userInfo?.fullName}
        </h1>
        <p className="content-text-sm content-text-white content-text-opacity-75">
          Continue your security learning journey and strengthen your
          organization&apos;s security readiness
        </p>
      </div>
      <DashboardSummary />
      <div className="content-mt-4 content-grid content-grid-cols-1 content-gap-4 md:content-mt-6 lg:content-grid-cols-2 lg:content-gap-6 [&>*]:content-min-w-0">
        <UserActivities />
        <Transcripts />
        <CertificateStatistics />
        <TopicsStatistics />
        {enrollment.hasReceivedEmailCampaign && <EmailPhishing />}
        {enrollment.hasReceivedSmsCampaign && <SmsPhishing />}
        {enrollment.hasReceivedVoiceCampaign && <VoicePhishing />}
        <div
          className={cn(
            phishingCardCount % 2 === 0
              ? 'lg:content-col-span-2'
              : 'lg:content-col-span-1',
          )}
        >
          <TopicsProgress />
        </div>
      </div>
      <div className="content-my-4 md:content-my-6">
        <ClientUserCourseList hostPath={hostPath} isDashboard={true} />
      </div>
      <div className="content-my-4 content-grid content-grid-cols-1 content-gap-4 md:content-my-6 md:content-grid-cols-2 md:content-gap-6 xl:content-grid-cols-3">
        <LeaderBoard />
        <PoolsAndSurvey />
        <LatestNews />
      </div>
    </div>
  );
};

export default ClientUserDashboard;
