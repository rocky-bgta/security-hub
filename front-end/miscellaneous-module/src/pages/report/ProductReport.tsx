import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tabs';
import PackageAssignmentReport from 'features/report/product-reports/PackageAssignmentReport';
import ProductUsageReport from 'features/report/product-reports/ProductUsageReport';
import SubPackageReport from 'features/report/product-reports/SubPackageReport';
import TopicAssignmentReport from 'features/report/product-reports/TopicAssignmentReport';

import { useState } from 'react';

type ReportTabValue =
  | 'product-usage'
  | 'package-assignment'
  | 'topic-assignment'
  | 'sub-package';

const ProductReport = () => {
  const [activeTab, setActiveTab] = useState<ReportTabValue>('product-usage');

  const tabSubHeaders: Record<ReportTabValue, string> = {
    'product-usage':
      'User interactions with products and modules, feature utilization',
    'package-assignment':
      'Distribution of product packages across users',
    'topic-assignment':
      'Topics/modules assigned to users and progress tracking',
    'sub-package':
      'Usage and assignment of sub-packages within larger packages',
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">
          Product & Package Report
        </h1>
        <p className="text-muted-foreground">{tabSubHeaders[activeTab]}</p>
      </div>

      <Tabs
        value={activeTab}
        onValueChange={value => setActiveTab(value as ReportTabValue)}
        className="space-y-6"
      >
        <TabsList>
          <TabsTrigger value="product-usage">Product Usage</TabsTrigger>
          <TabsTrigger value="package-assignment">
            Package Assignment
          </TabsTrigger>
          <TabsTrigger value="topic-assignment">Topic Assignment</TabsTrigger>
          <TabsTrigger value="sub-package">Sub-Package</TabsTrigger>
        </TabsList>

        <TabsContent value="product-usage">
          <ProductUsageReport />
        </TabsContent>

        <TabsContent value="package-assignment">
          <PackageAssignmentReport />
        </TabsContent>

        <TabsContent value="topic-assignment">
          <TopicAssignmentReport />
        </TabsContent>

        <TabsContent value="sub-package">
          <SubPackageReport />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default ProductReport;
