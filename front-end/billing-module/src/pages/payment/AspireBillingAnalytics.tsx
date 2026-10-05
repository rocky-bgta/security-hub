import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import TableLoader from 'components/skeleton/TableLoader';
import { useAPI } from 'hooks/UseAPI';
import {
  AlertTriangle,
  DollarSign,
  Download,
  RefreshCw,
  TrendingDown,
  TrendingUp,
  Users,
} from 'lucide-react';
import {
  IAnalyticsSummary,
  IFailedPayment,
  IPaymentSuccessRate,
  IRevenueTrend,
  ITopPerformingPackage,
} from 'models/Report';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';

const AspireBillingsAnalytics = () => {
  const apiClient = useAPI();
  const [analyticsSummary, setAnalyticsSummary] = useState<IAnalyticsSummary>();
  const [revenueTrend, setRevenueTrend] = useState<Array<IRevenueTrend>>([]);
  const [topPerformingPackages, setTopPerformingPackages] = useState<
    Array<ITopPerformingPackage>
  >([]);
  const [failedPayments, setFailedPayments] = useState<Array<IFailedPayment>>(
    [],
  );
  const [paymentSuccessRate, setPaymentSuccessRate] =
    useState<IPaymentSuccessRate>();
  const [loading, setLoading] = useState(false);
  const [timePeriod, setTimePeriod] = useState('MONTHLY');

  useEffect(() => {
    fetchAspireBillingAnalytics();
  }, [timePeriod]);

  const fetchAspireBillingAnalytics = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.ASPIRE_BILLING_ANALYTICS + '?period=' + timePeriod,
      );
      setAnalyticsSummary(response.data.summary);
      setRevenueTrend(response.data.revenueTrend);
      setTopPerformingPackages(response.data.topPackages);
      setFailedPayments(response.data.failedPaymentAnalysis);
      setPaymentSuccessRate(response.data.paymentSuccessRate);
    } catch (error) {
      console.error('Error fetching aspire billing analytics:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleExportReportPDF = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.ASPIRE_BILLING_ANALYTICS_EXPORT_PDF +
          '?period=' +
          timePeriod,
        {
          responseType: 'blob',
        },
      );

      const blob = new Blob([response], {
        type: 'application/pdf',
      });
      const url = window.URL.createObjectURL(blob);

      const link = document.createElement('a');
      link.href = url;
      link.download = `aspire-billing-analytics.pdf`;
      document.body.appendChild(link);
      link.click();

      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error exporting report:', error);
    }
  };

  const handleExportReportCSV = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.ASPIRE_BILLING_ANALYTICS_EXPORT_CSV +
          '?period=' +
          timePeriod,
        {
          responseType: 'blob',
        },
      );
      const blob = new Blob([response], {
        type: 'application/csv',
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `aspire-billing-analytics-${timePeriod}.csv`;
      document.body.appendChild(link);
      link.click();

      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error exporting report:', error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Billing Analytics & Reports
          </h1>
          <p className="text-muted-foreground">
            Track revenue, analyze trends, and gain insights into financial
            performance.
          </p>
        </div>
        <div className="flex gap-2">
          <Select value={timePeriod} onValueChange={setTimePeriod}>
            <SelectTrigger className="h-10 w-40 rounded-md px-4 py-2">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="MONTHLY">Monthly</SelectItem>
              <SelectItem value="QUARTERLY">Quarterly</SelectItem>
              <SelectItem value="YEARLY">Yearly</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" onClick={handleExportReportPDF}>
            <Download className="mr-2 size-4" />
            Export Report (PDF)
          </Button>
          <Button variant="outline" onClick={handleExportReportCSV}>
            <Download className="mr-2 size-4" />
            Export Report (CSV)
          </Button>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-3 lg:grid-cols-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="flex items-center gap-2 text-sm font-medium">
              <DollarSign className="size-4" />
              Total Revenue
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${analyticsSummary?.totalRevenue?.toLocaleString() || 0}
            </div>
            <div className="mt-1 flex items-center text-xs text-green-600">
              <TrendingUp className="mr-1 size-3" />+
              {analyticsSummary?.totalRevenueChangePercent}% from last period
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="flex items-center gap-2 text-sm font-medium">
              <RefreshCw className="size-4" />
              Total Refunds
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${analyticsSummary?.totalRefunds?.toLocaleString() || 0}
            </div>
            <div className="mt-1 flex items-center text-xs text-red-600">
              <TrendingDown className="mr-1 size-3" />
              {analyticsSummary?.refundRate}% refund rate
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="flex items-center gap-2 text-sm font-medium">
              <AlertTriangle className="size-4" />
              Failed Payments
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">
              {analyticsSummary?.failedPaymentsCount || 0}
            </div>
            <div className="mt-1 text-xs text-muted-foreground">This month</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="flex items-center gap-2 text-sm font-medium">
              <Users className="size-4" />
              {analyticsSummary?.activeLicensesLabel}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${analyticsSummary?.activeLicenses?.toLocaleString() || 0}
            </div>
            <div className="mt-1 text-xs text-muted-foreground">
              Average lifetime value
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              New Subscriptions
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {analyticsSummary?.newSubscriptions?.toLocaleString() || 0}
            </div>
            <div className="mt-1 text-xs text-green-600">
              +{analyticsSummary?.newSubscriptionsChangePercent}% from last
              month
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Active Plans</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {analyticsSummary?.activeLicenses?.toLocaleString() || 0}
            </div>
            <div className="mt-1 text-xs text-muted-foreground">
              {analyticsSummary?.activeLicensesLabel}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Monthly Revenue Trend */}
      <Card>
        <CardHeader>
          <CardTitle>Monthly Revenue Trend</CardTitle>
          <CardDescription>
            Revenue, new subscriptions, and refunds over time
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <TableLoader count={2} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Month</TableHead>
                  <TableHead>Revenue</TableHead>
                  <TableHead>New Subscriptions</TableHead>
                  <TableHead>Refunds</TableHead>
                  <TableHead>Net Revenue</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {revenueTrend.map((item, index) => (
                  <TableRow key={index}>
                    <TableCell className="font-medium">{item.period}</TableCell>
                    <TableCell className="font-semibold text-green-600">
                      ${item.revenue?.toLocaleString() || 0}
                    </TableCell>
                    <TableCell>{item.newSubscriptions}</TableCell>
                    <TableCell className="text-red-600">
                      ${item.refunds?.toLocaleString() || 0}
                    </TableCell>
                    <TableCell className="font-semibold">
                      ${(item.revenue - item.refunds).toLocaleString()}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Top Performing Plans */}
        <Card>
          <CardHeader>
            <CardTitle>Top Performing Plans</CardTitle>
            <CardDescription>
              Plans ranked by revenue and subscription count
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {topPerformingPackages.length === 0 ? (
                <div className="mt-10 flex h-full justify-center text-sm text-muted-foreground">
                  No top performing packages found
                </div>
              ) : (
                topPerformingPackages.map(pkg => (
                  <div
                    key={pkg.packageName}
                    className="flex items-center justify-between"
                  >
                    <div>
                      <div className="font-medium">{pkg.packageName}</div>
                      <div className="text-sm text-muted-foreground">
                        {pkg.subscriptionCount} subscriptions
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="font-semibold">
                        ${pkg.revenue.toLocaleString()}
                      </div>
                      <Badge variant="secondary">
                        {pkg.revenuePercentage}%
                      </Badge>
                    </div>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>

        {/* Failed Payments Analysis */}
        <Card>
          <CardHeader>
            <CardTitle>Failed Payments Analysis</CardTitle>
            <CardDescription>
              Breakdown of payment failure reasons
            </CardDescription>
          </CardHeader>
          <CardContent className="h-full">
            <div className="h-full space-y-4">
              {failedPayments.length === 0 ? (
                <div className="mt-10 flex h-full justify-center text-sm text-muted-foreground">
                  No failed payments found
                </div>
              ) : (
                failedPayments.map(item => (
                  <div
                    key={item.reason}
                    className="flex items-center justify-between"
                  >
                    <div>
                      <div className="font-medium">{item.reason}</div>
                      <div className="text-sm text-muted-foreground">
                        {item.count} failures
                      </div>
                    </div>
                    <div className="text-right">
                      <Badge
                        variant={
                          item.percentage > 30 ? 'destructive' : 'secondary'
                        }
                      >
                        {item.percentage}%
                      </Badge>
                    </div>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Difficulty-wise Success Rate */}
      <Card>
        <CardHeader>
          <CardTitle>Payment Success & Completion Rates</CardTitle>
          <CardDescription>
            Analysis of payment completion and success patterns
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-6 md:grid-cols-4">
            <div className="text-center">
              <div className="text-3xl font-bold text-green-600">
                {paymentSuccessRate?.paymentSuccessRate}%
              </div>
              <div className="text-sm text-muted-foreground">
                Payment Success Rate
              </div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-blue-600">
                {paymentSuccessRate?.totalPayments}
              </div>
              <div className="text-sm text-muted-foreground">
                Total Payments
              </div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-orange-600">
                {paymentSuccessRate?.successfulPayments}
              </div>
              <div className="text-sm text-muted-foreground">
                Successful Payments
              </div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-orange-600">
                {paymentSuccessRate?.failedPayments}
              </div>
              <div className="text-sm text-muted-foreground">
                Failed Payments
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default AspireBillingsAnalytics;
