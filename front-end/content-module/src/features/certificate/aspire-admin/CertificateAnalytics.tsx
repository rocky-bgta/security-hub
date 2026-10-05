import { Award, BarChart3, Calendar, Download, TrendingUp } from 'lucide-react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Pagination from 'common/Pagination';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import SearchSelect from 'components/SearchSelect';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  ICertificateSummaryStats,
  IExpiringCertificate,
} from 'models/Certificate';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { IProduct } from 'models/Product';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  HumanizeDate,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

interface IClientAdmin {
  id: string;
  organizationName: string;
}

const AspireAdminCertificateAnalytics = () => {
  const [selectedProduct, setSelectedProduct] = useState<string>('all');
  const [productList, setProductList] = useState<IProduct[]>([]);
  const [clientList, setClientList] = useState<IClientAdmin[]>([]);
  const [expiringCertificates, setExpiringCertificates] = useState<
    IList<IExpiringCertificate>
  >({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [summaryStats, setSummaryStats] = useState<ICertificateSummaryStats>({
    totalCertificatesIssued: 0,
    activeCertificatesCount: 0,
    averageCompletionRate: 0,
    expiringThisMonth: 0,
  });
  const [loading, setLoading] = useState<boolean>(false);
  const [loadingStats, setLoadingStats] = useState<boolean>(false);
  const [sendingReminder, setSendingReminder] = useState<string | null>(null);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    clientAdminId: '',
    productId: '',
  });
  const [dateRange, setDateRange] = useState<{
    fromDate: string;
    toDate: string;
  }>({
    fromDate: new Date(new Date().setMonth(new Date().getMonth() - 1))
      .toISOString()
      .split('T')[0],
    toDate: new Date().toISOString().split('T')[0],
  });
  const searchDebounce = useDebounce(queryString, 500);

  const apiClient = useAPI();

  useEffect(() => {
    fetchProductList();
    fetchClientList();
  }, []);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchExpiringCertificates();
    }
  }, [searchDebounce]);

  useEffect(() => {
    fetchCertificateSummaryStats();
  }, [selectedProduct, dateRange]);

  const fetchProductList = async () => {
    try {
      const response: IResponse<IList<IProduct>> = await apiClient.get(
        API_END_POINTS.PRODUCT_LIST + '?status=ENABLED&pageSize=1000',
      );
      if (isSuccessResponse(response.statusCode)) {
        setProductList(response.data?.items || []);
      }
    } catch (error) {
      console.error('Error fetching product list:', error);
    }
  };

  const fetchClientList = async () => {
    try {
      const response: IResponse<any> = await apiClient.get(
        API_END_POINTS.CLIENT_LIST + '?pageSize=1000&status=ACTIVE',
      );
      if (isSuccessResponse(response.statusCode)) {
        setClientList(
          response.data?.clientAdmins?.map((client: any) => ({
            id: client.id,
            organizationName: client.organizationName,
          })) || [],
        );
      }
    } catch (error) {
      console.error('Error fetching client list:', error);
    }
  };

  const fetchExpiringCertificates = async () => {
    setLoading(true);
    try {
      const response: IResponse<IList<IExpiringCertificate>> =
        await apiClient.get(API_END_POINTS.EXPIRING_CERTIFICATES + queryString);

      if (isSuccessResponse(response.statusCode)) {
        setExpiringCertificates(
          response.data || {
            offset: 0,
            pageSize: 10,
            total: 0,
            items: [],
          },
        );
      }
    } catch (error) {
      console.error('Error fetching expiring certificates:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCertificateSummaryStats = async () => {
    setLoadingStats(true);
    try {
      const params: any = {};

      if (selectedProduct && selectedProduct !== 'all') {
        params.productId = selectedProduct;
      }

      if (dateRange.fromDate) {
        const fromDate = new Date(dateRange.fromDate);
        fromDate.setHours(0, 0, 0, 0);
        params.fromDate = fromDate.toISOString();
      }

      if (dateRange.toDate) {
        const toDate = new Date(dateRange.toDate);
        toDate.setHours(23, 59, 59, 999);
        params.toDate = toDate.toISOString();
      }

      const queryString = objectToQueryString(params);
      const response: IResponse<ICertificateSummaryStats> = await apiClient.get(
        API_END_POINTS.CERTIFICATE_SUMMARY_STATS + queryString,
      );

      if (isSuccessResponse(response.statusCode)) {
        setSummaryStats(
          response.data || {
            totalCertificatesIssued: 0,
            activeCertificatesCount: 0,
            averageCompletionRate: 0,
            expiringThisMonth: 0,
          },
        );
      }
    } catch (error) {
      console.error('Error fetching certificate summary stats:', error);
    } finally {
      setLoadingStats(false);
    }
  };

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const sendEmailReminder = async (certificateId: string, adminId: string) => {
    setSendingReminder(certificateId);
    try {
      const response: IResponse<any> = await apiClient.post(
        API_END_POINTS.SEND_EMAIL_EXPIRING_CERTIFICATES,
        {
          data: {
            adminId: adminId,
            certificateIds: [certificateId],
          },
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success('Email reminder sent successfully');
      } else {
        toast.error('Failed to send email reminder');
      }
    } catch (error: any) {
      console.error('Error sending email reminder:', error);
      toast.error(error.message || 'Failed to send email reminder');
    } finally {
      setSendingReminder(null);
    }
  };

  const handleResetSummaryFilter = () => {
    setDateRange({
      fromDate: new Date(new Date().setMonth(new Date().getMonth() - 1))
        .toISOString()
        .split('T')[0],
      toDate: new Date().toISOString().split('T')[0],
    });
    setSelectedProduct('');
  };

  const handleResetFilter = () => {
    setQueryParams(prevState => ({
      ...prevState,
      clientAdminId: '',
    }));
  };

  const handleExportReport = async () => {
    try {
      const response = await apiClient.post(
        API_END_POINTS.EXPIRING_CERTIFICATES_EXPORT_REPORT + queryString,
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
      link.download = `expiring-certificates-${new Date().toISOString()}.csv`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error exporting report:', error);
    }
  };

  return (
    <div className="content-space-y-6 content-text-white">
      <div>
        <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
          Certificate Analytics & Logs
        </h1>
        <p className="content-text-muted-foreground">
          Analyze certificate performance, completion rates, and track
          certificate trends
        </p>
      </div>

      <div className="content-flex content-items-end content-gap-4">
        <div className="content-w-full">
          <Label htmlFor="product" className="content-text-sm">
            Select Product
          </Label>
          <SearchSelect
            items={[
              { value: 'all', label: 'All Products' },
              ...productList.map(product => ({
                value: product.productId,
                label: product.productName,
              })),
            ]}
            value={selectedProduct}
            onValueChange={setSelectedProduct}
            placeholder="Filter by Product"
          />
        </div>

        <div className="content-flex content-items-center content-gap-2">
          <div className="content-flex content-flex-col content-gap-1">
            <Label htmlFor="fromDate" className="content-text-sm">
              From Date
            </Label>
            <Input
              id="fromDate"
              type="date"
              value={dateRange.fromDate}
              onChange={e =>
                setDateRange(prev => ({
                  ...prev,
                  fromDate: e.target.value,
                }))
              }
              onClick={e => {
                const input = e.target as HTMLInputElement;
                input.showPicker();
              }}
              max={dateRange.toDate || new Date().toISOString().split('T')[0]}
              className="content-w-40"
            />
          </div>
          <div className="content-flex content-flex-col content-gap-1">
            <Label htmlFor="toDate" className="content-text-sm">
              To Date
            </Label>
            <Input
              id="toDate"
              type="date"
              value={dateRange.toDate}
              onChange={e =>
                setDateRange(prev => ({
                  ...prev,
                  toDate: e.target.value,
                }))
              }
              onClick={e => {
                const input = e.target as HTMLInputElement;
                input.showPicker();
              }}
              min={dateRange.fromDate}
              max={new Date().toISOString().split('T')[0]}
              className="content-w-40"
            />
          </div>
        </div>
        <Button variant="outline" onClick={handleResetSummaryFilter}>
          Reset Filter
        </Button>
      </div>

      <div className="content-grid content-gap-4 md:content-grid-cols-2 lg:content-grid-cols-4">
        <Card>
          <CardHeader className="!content-flex-row content-items-center content-justify-between content-space-y-0 content-pb-2">
            <CardTitle className="content-text-sm content-font-medium">
              Total Certificates Issued
            </CardTitle>
            <Award className="content-size-4 content-text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {loadingStats ? (
              <div className="content-text-2xl content-font-bold">...</div>
            ) : (
              <>
                <div className="content-text-2xl content-font-bold">
                  {summaryStats.totalCertificatesIssued.toLocaleString()}
                </div>
                <p className="content-text-xs content-text-muted-foreground">
                  Certificates issued in selected period
                </p>
              </>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="!content-flex-row content-items-center content-justify-between content-space-y-0 content-pb-2">
            <CardTitle className="content-text-sm content-font-medium">
              Active Certificates
            </CardTitle>
            <TrendingUp className="content-size-4 content-text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {loadingStats ? (
              <div className="content-text-2xl content-font-bold">...</div>
            ) : (
              <>
                <div className="content-text-2xl content-font-bold">
                  {summaryStats.activeCertificatesCount.toLocaleString()}
                </div>
                <p className="content-text-xs content-text-muted-foreground">
                  {summaryStats.totalCertificatesIssued > 0
                    ? `${Math.round((summaryStats.activeCertificatesCount / summaryStats.totalCertificatesIssued) * 100)}% of total issued`
                    : 'No certificates'}
                </p>
              </>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="!content-flex-row content-items-center content-justify-between content-space-y-0 content-pb-2">
            <CardTitle className="content-text-sm content-font-medium">
              Average Completion Rate
            </CardTitle>
            <BarChart3 className="content-size-4 content-text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {loadingStats ? (
              <div className="content-text-2xl content-font-bold">...</div>
            ) : (
              <>
                <div className="content-text-2xl content-font-bold">
                  {summaryStats.averageCompletionRate.toFixed(1)}%
                </div>
                <p className="content-text-xs content-text-muted-foreground">
                  Average completion rate
                </p>
              </>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="!content-flex-row content-items-center content-justify-between content-space-y-0 content-pb-2">
            <CardTitle className="content-text-sm content-font-medium">
              Expiring This Month
            </CardTitle>
            <Calendar className="content-size-4 content-text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {loadingStats ? (
              <div className="content-text-2xl content-font-bold">...</div>
            ) : (
              <>
                <div className="content-text-2xl content-font-bold">
                  {summaryStats.expiringThisMonth.toLocaleString()}
                </div>
                <p className="content-text-xs content-text-muted-foreground">
                  Requires attention
                </p>
              </>
            )}
          </CardContent>
        </Card>
      </div>

      {/* <div className="content-grid content-gap-6 md:content-grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Completion Rate by Course</CardTitle>
            <CardDescription>
              Percentage of learners who completed courses and received
              certificates
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="content-space-y-4">
              {mockCertificateAnalytics.completionRates.map((course, index) => (
                <div
                  key={index}
                  className="content-flex content-items-center content-justify-between"
                >
                  <span className="content-text-sm content-font-medium">
                    {course.courseName}
                  </span>
                  <div className="content-flex content-items-center content-gap-2">
                    <div className="content-h-2 content-w-24 content-rounded-full content-bg-secondary">
                      <div
                        className="content-h-2 content-rounded-full content-bg-primary"
                        style={{ width: `${course.completionRate}%` }}
                      />
                    </div>
                    <span className="content-text-sm content-text-muted-foreground">
                      {course.completionRate}%
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Pass/Fail Statistics</CardTitle>
            <CardDescription>
              Success rates across different courses
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="content-space-y-4">
              {mockCertificateAnalytics.passFailStats.map((course, index) => (
                <div key={index} className="content-space-y-2">
                  <div className="content-flex content-justify-between content-text-sm">
                    <span className="content-font-medium">
                      {course.courseName}
                    </span>
                    <span className="content-text-muted-foreground">
                      Avg: {course.averageScore}%
                    </span>
                  </div>
                  <div className="content-flex content-gap-1">
                    <div
                      className="content-h-2 content-rounded-l-full content-bg-green-500"
                      style={{ width: `${course.passRate}%` }}
                    />
                    <div
                      className="content-h-2 content-rounded-r-full content-bg-red-500"
                      style={{ width: `${course.failRate}%` }}
                    />
                  </div>
                  <div className="content-flex content-justify-between content-text-xs content-text-muted-foreground">
                    <span>Pass: {course.passRate}%</span>
                    <span>Fail: {course.failRate}%</span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div> */}

      <Card>
        <CardHeader>
          <div className="content-flex content-flex-col content-gap-8">
            <div className="content-flex content-items-center content-justify-between">
              <div>
                <CardTitle>Certificates Expiring Soon</CardTitle>
                <CardDescription>
                  Certificates that will expire within the next 30 days
                </CardDescription>
              </div>
              <Button
                variant="outline"
                onClick={handleExportReport}
                className="content-ml-auto"
              >
                <Download className="content-mr-2 content-size-4" />
                Export Report
              </Button>
            </div>
            <div className="content-flex content-w-full content-items-center content-gap-2">
              <SearchSelect
                items={[
                  { value: 'all', label: 'All Clients' },
                  ...clientList.map(client => ({
                    value: client.id,
                    label: client.organizationName,
                  })),
                ]}
                value={queryParams.clientAdminId}
                onValueChange={value => {
                  setQueryParams(prevState => ({
                    ...prevState,
                    clientAdminId: value === 'all' ? '' : value || '',
                  }));
                }}
                placeholder="Filter by Client"
              />
              <Button variant="outline" size="sm" onClick={handleResetFilter}>
                Reset Filters
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="content-py-8 content-text-center content-text-muted-foreground">
              Loading...
            </div>
          ) : expiringCertificates?.items?.length === 0 ? (
            <div className="content-py-8 content-text-center content-text-muted-foreground">
              No expiring certificates found
            </div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Learner Name</TableHead>
                    <TableHead>Client Admin</TableHead>
                    <TableHead>Course Name</TableHead>
                    <TableHead>Certificate ID</TableHead>
                    <TableHead>Expiry Date</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {expiringCertificates?.items?.map((cert, index) => (
                    <TableRow key={index}>
                      <TableCell className="content-font-medium">
                        {cert.learnerName}
                      </TableCell>
                      <TableCell>{cert.clientAdminName}</TableCell>
                      <TableCell>{cert.courseName}</TableCell>
                      <TableCell className="content-font-mono content-text-sm">
                        {cert.certificateId}
                      </TableCell>
                      <TableCell>{HumanizeDate(cert.expiryDate)}</TableCell>
                      <TableCell>
                        <Badge
                          variant={
                            cert.status === 'Expiring' ||
                            cert.status?.toLowerCase().includes('expiring')
                              ? 'destructive'
                              : 'secondary'
                          }
                        >
                          {cert.status}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            sendEmailReminder(
                              cert.certificateId,
                              cert.clientAdminId,
                            )
                          }
                          disabled={sendingReminder === cert.certificateId}
                        >
                          {sendingReminder === cert.certificateId
                            ? 'Sending...'
                            : 'Send Reminder'}
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              <div className="content-mt-4 content-flex content-justify-end">
                <Pagination
                  total={expiringCertificates.total}
                  perPage={queryParams.pageSize || 10}
                  onPageChange={handlePageChange}
                />
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default AspireAdminCertificateAnalytics;
