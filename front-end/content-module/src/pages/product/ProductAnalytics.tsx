import { BarChart, Download } from 'lucide-react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Progress } from 'common/Progress';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import Border from 'components/UserBorder';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useDownloader } from 'hooks/UseDownloader';
import { useStore } from 'hooks/UseStore';
import { IGetListParams, IResponse } from 'models/Global';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';

interface ICourseAnalytics {
  productId: string;
  courseName: string;
  totalEnrolled: number;
  completed: number;
  inProgress: number;
  notStarted: number;
  completionRate: number;
}

const ProductAnalytics = () => {
  const { userInfo } = useStore();
  const { downloadFile } = useDownloader();
  const [loading, setLoading] = useState<boolean>(true);
  const [productData, setProductData] = useState<ICourseAnalytics[]>([]);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    clientAdminId: userInfo.userId,
  });
  const searchDebounce = useDebounce(queryString, 1000);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchProductData();
    }
  }, [searchDebounce]);

  const fetchProductData = async () => {
    setLoading(true);
    try {
      const response: IResponse<ICourseAnalytics[]> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_PRODUCT_ANALYTICS_LIST + queryString,
      );
      setProductData(response.data);
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleExportAnalytics = async () => {
    try {
      const response: IResponse<any> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_PRODUCT_ANALYTICS_EXPORT + queryString,
      );
      downloadFile(response.data);
    } catch (error) {
      console.error('Error fetching data:', error);
    }
  };

  return (
    <div className="content-space-y-6 content-text-white">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
            Product Management
          </h1>
          <p className="content-text-muted-foreground">
            Manage training content and materials
          </p>
        </div>
      </div>

      <Border>
        <CardHeader>
          <div className="content-flex content-items-center content-justify-between">
            <div>
              <CardTitle className="content-mb-2 content-flex content-items-center content-gap-2">
                <BarChart className="content-size-5" />
                Product Analytics
              </CardTitle>
              <CardDescription>
                Track product completion and user participation
              </CardDescription>
            </div>
            <Button
              onClick={handleExportAnalytics}
              variant="outline"
              className="content-border-transparent content-bg-primary"
            >
              <Download className="content-mr-2 content-size-4" />
              Export Report
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <AssignedPackagesLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Course Name</TableHead>
                  <TableHead>Total Enrolled</TableHead>
                  <TableHead>Completed</TableHead>
                  <TableHead>In Progress</TableHead>
                  <TableHead>Not Started</TableHead>
                  <TableHead>Completion Rate</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {productData?.map((analytics: ICourseAnalytics) => (
                  <TableRow key={analytics.productId}>
                    <TableCell className="content-font-medium">
                      {analytics.courseName}
                    </TableCell>
                    <TableCell>{analytics.totalEnrolled}</TableCell>
                    <TableCell>
                      <Badge variant="default" className="content-bg-primary">
                        {analytics.completed}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge variant="secondary" className="content-bg-primary">
                        {analytics.inProgress}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant="outline"
                        className="content-border-transparent content-text-white"
                      >
                        {analytics.notStarted}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="content-flex content-items-center content-gap-2">
                        <Progress
                          value={analytics.completionRate}
                          className="content-w-16"
                        />
                        <span className="content-text-sm">
                          {analytics.completionRate}%
                        </span>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Border>
    </div>
  );
};

export default ProductAnalytics;
