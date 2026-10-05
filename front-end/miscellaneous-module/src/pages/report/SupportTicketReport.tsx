import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tabs';
import OpenCloseTicketReport from 'features/report/support-ticket-reports/OpenCloseTicketReport';
import TicketResolutionReport from 'features/report/support-ticket-reports/TicketResolutionReport';

import { useState } from 'react';

type ReportTabValue = 'open-vs-close-ticket' | 'ticket-resolution';

const SupportTicketReport = () => {
  const [activeTab, setActiveTab] = useState<ReportTabValue>(
    'open-vs-close-ticket',
  );

  const tabSubHeaders: Record<ReportTabValue, string> = {
    'open-vs-close-ticket':
      'Open vs closed support tickets with resolution status',
    'ticket-resolution': 'Resolution time tracking and SLA compliance',
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">
          Support Ticket Report
        </h1>
        <p className="text-muted-foreground">{tabSubHeaders[activeTab]}</p>
      </div>

      <Tabs
        value={activeTab}
        onValueChange={value => setActiveTab(value as ReportTabValue)}
        className="space-y-6"
      >
        <TabsList>
          <TabsTrigger value="open-vs-close-ticket">
            Open vs Close Ticket
          </TabsTrigger>
          <TabsTrigger value="ticket-resolution">
            Ticket Resolution Time
          </TabsTrigger>
        </TabsList>

        <TabsContent value="open-vs-close-ticket">
          <OpenCloseTicketReport />
        </TabsContent>

        <TabsContent value="ticket-resolution">
          <TicketResolutionReport />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default SupportTicketReport;
