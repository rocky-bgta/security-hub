import { CampaignChannel } from 'models/Campaign';
import { routes } from 'routes/Routes';

export type SimulationChannel = 'phishing' | 'smishing' | 'vishing';

export const toCampaignChannel = (
  channel: SimulationChannel,
): CampaignChannel => {
  if (channel === 'smishing') {
    return CampaignChannel.SMS;
  }
  if (channel === 'vishing') {
    return CampaignChannel.VOICE;
  }
  return CampaignChannel.EMAIL;
};

export const fromCampaignChannel = (
  channel: CampaignChannel,
): SimulationChannel => {
  if (channel === CampaignChannel.SMS) {
    return 'smishing';
  }
  if (channel === CampaignChannel.VOICE) {
    return 'vishing';
  }
  return 'phishing';
};

export const getSimulationLabel = (channel: SimulationChannel): string => {
  if (channel === 'smishing') {
    return 'Smishing';
  }
  if (channel === 'vishing') {
    return 'Vishing';
  }
  return 'Phishing';
};

export const getSimulationCopy = (channel: SimulationChannel) => {
  const label = getSimulationLabel(channel);

  if (channel === 'smishing') {
    return {
      label,
      reportsTitle: 'Smishing Reports',
      campaignsDescription:
        'View detailed performance metrics for all smishing campaigns',
      statsTitle: 'SMS Statistics',
      statsDescription: 'Comprehensive SMS delivery and engagement metrics',
      metricsTitle: 'SMS Metrics',
      sentLabel: 'SMS Sent',
      sentUnit: 'SMS sent',
      deliveredLabel: 'Delivered',
      openedLabel: 'Read',
      clickedLabel: 'Clicked',
      bouncedLabel: 'Failed',
      openRateLabel: 'Read Rate',
      clickRateLabel: 'Click Rate',
      clicksColumn: 'Clicks',
      proneLabel: 'SMS-Prone %',
      proneHint: 'Users susceptible to smishing',
      proneUsersLabel: 'SMS-Prone Users',
      proneBadgeLabel: 'SMS-Prone',
      activityDescription:
        'Track user interaction events across smishing campaigns',
      activitySent: 'SMS Sent',
      activityDelivered: 'SMS Delivered',
      activityBounced: 'SMS Failed',
      activityNotOpenedReported: 'Not Read but Reported',
      activityOpened: 'SMS Read',
      activityClicked: 'Link Clicked',
      activityAttachment: 'Attachment Opened',
      activitySubmitted: 'Compromised',
      activityReported: 'SMS Reported',
      topCampaignsTitle: 'Top Campaigns by Click Rate',
      scheduleDescription:
        'Define when and how the smishing campaign is delivered.',
      riskLowDesc: 'No clicks or single click',
      riskMediumDesc: 'Clicked once',
      riskHighDesc: 'Clicked 2+ or submitted data',
      riskCriticalDesc: 'Multiple submissions',
      humanRiskInfo:
        'Average smishing risk for your organization in the selected period. Lower is better.',
      proneUsersInfo:
        'Employees who frequently fail simulations. Critical = submitted data or failed >80% of messages; High = failed ≥50%.',
      credentialSubmitsInfo:
        'How many times employees entered information on fake smishing pages. Zero is the goal.',
      riskTrendInfo:
        'Whether organization-wide smishing risk is improving, worsening, or stable compared to the previous period.',
    };
  }

  if (channel === 'vishing') {
    return {
      label,
      reportsTitle: 'Vishing Reports',
      campaignsDescription:
        'View detailed performance metrics for all vishing campaigns',
      statsTitle: 'Call Statistics',
      statsDescription:
        'Comprehensive voice call delivery and engagement metrics',
      metricsTitle: 'Call Metrics',
      sentLabel: 'Voice Calls Sent',
      sentUnit: 'calls sent',
      deliveredLabel: 'Connected',
      openedLabel: 'Answered',
      clickedLabel: 'Engaged',
      bouncedLabel: 'Failed',
      openRateLabel: 'Pickup Rate',
      clickRateLabel: 'Answer Rate',
      clicksColumn: 'Engagements',
      proneLabel: 'Call-Prone %',
      proneHint: 'Users susceptible to vishing',
      proneUsersLabel: 'Call-Prone Users',
      proneBadgeLabel: 'Call-Prone',
      activityDescription:
        'Track user interaction events across vishing campaigns',
      activitySent: 'Call Sent',
      activityDelivered: 'Call Connected',
      activityBounced: 'Call Failed',
      activityNotOpenedReported: 'Not Answered but Reported',
      activityOpened: 'Call Answered',
      activityClicked: 'Call Engaged',
      activityAttachment: 'Attachment Opened',
      activitySubmitted: 'Compromised',
      activityReported: 'Call Reported',
      topCampaignsTitle: 'Top Campaigns by Answer Rate',
      scheduleDescription:
        'Configure when and how to send the vishing calls.',
      riskLowDesc: 'No engagement or a single engagement',
      riskMediumDesc: 'Engaged once',
      riskHighDesc: 'Engaged 2+ or submitted data',
      riskCriticalDesc: 'Multiple submissions',
      humanRiskInfo:
        'Average vishing risk for your organization in the selected period. Lower is better.',
      proneUsersInfo:
        'Employees who frequently fail simulations. Critical = submitted data or failed >80% of calls; High = failed ≥50%.',
      credentialSubmitsInfo:
        'How many times employees shared information during vishing simulations. Zero is the goal.',
      riskTrendInfo:
        'Whether organization-wide vishing risk is improving, worsening, or stable compared to the previous period.',
    };
  }

  return {
    label,
    reportsTitle: 'Phishing Reports',
    campaignsDescription:
      'View detailed performance metrics for all phishing campaigns',
    statsTitle: 'Email Statistics',
    statsDescription: 'Comprehensive email delivery and engagement metrics',
    metricsTitle: 'Email Metrics',
    sentLabel: 'Emails Sent',
    sentUnit: 'emails sent',
    deliveredLabel: 'Delivered',
    openedLabel: 'Opened',
    clickedLabel: 'Clicked',
    bouncedLabel: 'Bounced',
    openRateLabel: 'Open Rate',
    clickRateLabel: 'Click Rate',
    clicksColumn: 'Clicks',
    proneLabel: 'Phish-Prone %',
    proneHint: 'Users susceptible to phishing',
    proneUsersLabel: 'Phish-Prone Users',
    proneBadgeLabel: 'Phish-Prone',
    activityDescription:
      'Track user interaction events across phishing campaigns',
    activitySent: 'Email Sent',
    activityDelivered: 'Email Delivered',
    activityBounced: 'Email Bounced',
    activityNotOpenedReported: 'Not Opened but Reported',
    activityOpened: 'Opened',
    activityClicked: 'Clicked',
    activityAttachment: 'Attachment Opened',
    activitySubmitted: 'Compromised',
    activityReported: 'Reported',
    topCampaignsTitle: 'Top Campaigns by Click Rate',
    scheduleDescription:
      'Configure when and how to send the phishing emails.',
    riskLowDesc: 'No clicks or single click',
    riskMediumDesc: 'Clicked once',
    riskHighDesc: 'Clicked 2+ or submitted data',
    riskCriticalDesc: 'Multiple submissions',
    humanRiskInfo:
      'Average phishing risk for your organization in the selected period. Lower is better.',
    proneUsersInfo:
      'Employees who frequently fail simulations. Critical = submitted credentials or failed >80% of messages; High = failed ≥50%.',
    credentialSubmitsInfo:
      'How many times employees entered information on fake phishing pages. Zero is the goal.',
    riskTrendInfo:
      'Whether organization-wide phishing risk is improving, worsening, or stable compared to the previous period.',
  };
};

export const getSimulationPaths = (channel: SimulationChannel) => {
  if (channel === 'smishing') {
    return {
      dashboard: routes.smishingDashboard.path,
      campaigns: routes.smishingCampaigns.path,
      campaignDetails: routes.smishingCampaignDetails.path,
      reports: routes.smishingReports.path,
      campaignReports: routes.smishingCampaignReports.path,
      campaignDetailReport: routes.smishingCampaignDetailReport.path,
      userRiskReport: routes.smishingUserRiskReport.path,
      recentActivity: routes.smishingRecentActivity.path,
      campaignRiskImpacts: routes.smishingCampaignRiskImpacts.path,
      courseStatistics: routes.smishingCourseStatisticsDetails.path,
      templates: routes.smsTemplateLibrary.path,
      landingPages: routes.smishingLandingPages.path,
    };
  }

  if (channel === 'vishing') {
    return {
      dashboard: routes.vishingDashboard.path,
      campaigns: routes.vishingCampaigns.path,
      campaignDetails: routes.vishingCampaignDetails.path,
      reports: routes.vishingReports.path,
      campaignReports: routes.vishingCampaignReports.path,
      campaignDetailReport: routes.vishingCampaignDetailReport.path,
      userRiskReport: routes.vishingUserRiskReport.path,
      recentActivity: routes.vishingRecentActivity.path,
      campaignRiskImpacts: routes.vishingCampaignRiskImpacts.path,
      courseStatistics: routes.vishingCourseStatisticsDetails.path,
      templates: routes.vishingTemplateLibrary.path,
      landingPages: routes.phishingLandingPages.path,
    };
  }

  return {
    dashboard: routes.phishingDashboard.path,
    campaigns: routes.phishingCampaings.path,
    campaignDetails: routes.phishingCampaignDetails.path,
    reports: routes.phishingReports.path,
    campaignReports: routes.phishingCampaignReports.path,
    campaignDetailReport: routes.phishingCampaignDetailReport.path,
    userRiskReport: routes.phishingUserRiskReport.path,
    recentActivity: routes.phishingRecentActivity.path,
    campaignRiskImpacts: routes.phishingCampaignRiskImpacts.path,
    courseStatistics: routes.phishingCourseStatisticsDetails.path,
    templates: routes.phishingTemplateLibrary.path,
    landingPages: routes.phishingLandingPages.path,
  };
};
