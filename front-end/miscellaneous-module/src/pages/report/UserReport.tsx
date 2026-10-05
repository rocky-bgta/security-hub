import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tabs';
import OnboardingReport from 'features/report/user-reports/OnboardingReport';
import UserSummaryReport from 'features/report/user-reports/UserSummaryReport';

import { useState } from 'react';

type ReportTabValue =
  | 'user-summary-reports'
  | 'user-activity-reports'
  | 'onboarding-reports';

const UserReport = () => {
  const [activeTab, setActiveTab] = useState<ReportTabValue>(
    'user-summary-reports',
  );

  const tabSubHeaders: Record<ReportTabValue, string> = {
    'user-summary-reports':
      'High-level overview of all users, status, engagement, and role-based activity',
    'user-activity-reports':
      'Detailed user actions including logins, feature usage, and audit logs',
    'onboarding-reports':
      'Overview of license utilization and employee security readiness',
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">User Report</h1>
        <p className="text-muted-foreground">{tabSubHeaders[activeTab]}</p>
      </div>

      <Tabs
        value={activeTab}
        onValueChange={value => setActiveTab(value as ReportTabValue)}
        className="space-y-6"
      >
        <TabsList>
          <TabsTrigger value="user-summary-reports">
            User Summary Reports
          </TabsTrigger>
          {/* <TabsTrigger value="user-activity-reports">
            User Activity Reports
          </TabsTrigger> */}
          <TabsTrigger value="onboarding-reports">
            Onboarding Reports
          </TabsTrigger>
        </TabsList>

        <TabsContent value="user-summary-reports">
          <UserSummaryReport />
        </TabsContent>

        <TabsContent value="user-activity-reports">
          <p className="text-muted-foreground">Coming Soon</p>
        </TabsContent>

        <TabsContent value="onboarding-reports">
          <OnboardingReport />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default UserReport;
