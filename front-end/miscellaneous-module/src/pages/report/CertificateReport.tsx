import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tabs';
import ExpiredCertificatesReport from 'features/report/certificate-reports/ExpiredCertificatesReport';
import IssuedCertificatesReport from 'features/report/certificate-reports/IssuedCertificatesReport';

import { useState } from 'react';

type ReportTabValue = 'issued-certificates' | 'expired-certificates';

const CertificateReport = () => {
  const [activeTab, setActiveTab] = useState<ReportTabValue>(
    'issued-certificates',
  );

  const tabSubHeaders: Record<ReportTabValue, string> = {
    'issued-certificates':
      'All certificates issued to users with timestamps',
    'expired-certificates':
      'Expired or about-to-expire certificates for timely renewals',
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">
          Certificate Reports
        </h1>
        <p className="text-muted-foreground">{tabSubHeaders[activeTab]}</p>
      </div>

      <Tabs
        value={activeTab}
        onValueChange={value => setActiveTab(value as ReportTabValue)}
        className="space-y-6"
      >
        <TabsList>
          <TabsTrigger value="issued-certificates">
            Issued Certificates Report
          </TabsTrigger>
          <TabsTrigger value="expired-certificates">
            Expired/Expiring Certificates
          </TabsTrigger>
        </TabsList>

        <TabsContent value="issued-certificates">
          <IssuedCertificatesReport />
        </TabsContent>

        <TabsContent value="expired-certificates">
          <ExpiredCertificatesReport />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default CertificateReport;
