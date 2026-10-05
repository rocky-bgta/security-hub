import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Progress } from 'common/Progress';

import { Download } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import {
  IProductUsageReportData,
  IProductUtilizationItem,
} from 'models/ProductUsageReport';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { downloadFromUrl, isSuccessResponse } from 'utils/Helper';

const StatCard = ({
  label,
  value,
  color,
}: {
  label: string;
  value: string | number;
  color?: string;
}) => (
  <Card>
    <CardContent className="p-4">
      <div className={`text-2xl font-bold ${color || ''}`}>{value}</div>
      <div className="mt-1 text-xs text-muted-foreground">{label}</div>
    </CardContent>
  </Card>
);

const ProductUsageReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState({
    totalProduct: 0,
    totalActiveModule: 0,
    totalInteraction: 0,
    averageEngagement: 0,
  });
  const [products, setProducts] = useState<IProductUtilizationItem[]>([]);
  const [isExporting, setIsExporting] = useState(false);

  const handleExportReport = async () => {
    try {
      setIsExporting(true);
      const response: IResponse<string> = await apiClient.get(
        API_END_POINTS.GET_CLIENT_ADMIN_PRODUCT_USAGE_REPORT_EXPORT,
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Export failed');
      }

      if (!response.data) {
        toast.warning('No file to download');
        return;
      }

      await downloadFromUrl(response.data, 'product-usage-report.csv');
    } catch (error) {
      console.error('Error exporting product usage report:', error);
      toast.error('Failed to export report');
    } finally {
      setIsExporting(false);
    }
  };

  useEffect(() => {
    const fetchProductUsageReport = async () => {
      try {
        const endpoint = API_END_POINTS.GET_CLIENT_ADMIN_PRODUCT_USAGE_REPORT;

        const response: IResponse<IProductUsageReportData> =
          await apiClient.get(endpoint);

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        const data = response.data;
        setSummary({
          totalProduct: data.totalProduct ?? 0,
          totalActiveModule: data.totalActiveModule ?? 0,
          totalInteraction: data.totalInteraction ?? 0,
          averageEngagement: data.averageEngagement ?? 0,
        });
        setProducts(data.products || []);
      } catch (error) {
        console.error('Error fetching product usage report:', error);
      }
    };

    fetchProductUsageReport();
  }, [apiClient]);

  return (
    <div className="space-y-6">
      <div className="flex justify-end">
        <Button
          variant="outline"
          onClick={handleExportReport}
          disabled={isExporting}
        >
          <Download className="size-4" />
          {isExporting ? 'Exporting...' : 'Export Report'}
        </Button>
      </div>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard label="Total Products" value={summary.totalProduct} />
        <StatCard label="Active Modules" value={summary.totalActiveModule} />
        <StatCard
          label="Total Interactions"
          value={summary.totalInteraction.toLocaleString()}
        />
        <StatCard
          label="Avg Engagement"
          value={`${summary.averageEngagement}%`}
          color="text-primary"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Product Utilization</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {!products.length ? (
              <p className="py-6 text-center text-sm text-muted-foreground">
                No data available
              </p>
            ) : (
              products.map((product, index) => (
                <div
                  key={product.id || `${product.productId}-${index}`}
                  className="flex items-center gap-4"
                >
                  <div className="w-56 text-sm font-medium">
                    {product.productName}
                  </div>
                  <div className="flex-1">
                    <Progress
                      value={product.utilizationPercentage}
                      className="h-3"
                    />
                  </div>
                  <div className="w-12 text-right text-sm">
                    {product.utilizationPercentage}%
                  </div>
                  <div className="w-28 text-right text-xs text-muted-foreground">
                    {product.totalUsers} / {product.totalLicenseCount} users
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ProductUsageReport;
