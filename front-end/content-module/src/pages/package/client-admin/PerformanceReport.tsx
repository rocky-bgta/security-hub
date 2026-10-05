import { Award, BarChart, Download } from 'lucide-react';

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
import PerformanceReportLoader from 'components/skeleton/PerformanceReport';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { IGetListParams, IResponse } from 'models/Global';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { HumanizeDate, objectToQueryString } from 'utils/Helper';

interface PackageData {
  packageName: string;
  productName: string;
  assignedUsers: number;
  licensesUsed: number;
  topicsCompleted: number;
  completionRate: string;
  avgTimeSpent: string;
  certifications: number;
  certificationRate: number | string;
}

interface ReportResponse {
  total: number;
  data: PackageData[];
}

const ClientAdminPerformanceReport = () => {
  const { userInfo } = useStore();
  const [loading, setLoading] = useState<boolean>(true);
  const [reportData, setReportData] = useState<ReportResponse>({
    total: 0,
    data: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    clientAdminId: userInfo.userId,
  });
  const searchDebounce = useDebounce(queryString, 500);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchReportData();
    }
  }, [searchDebounce]);

  const fetchReportData = async () => {
    setLoading(true);

    try {
      const response: IResponse<ReportResponse> = await apiClient.get(
        API_END_POINTS.PACKAGE_PERFORMANCE_REPORT + queryString,
      );
      setReportData(response.data);
    } catch (error) {
      console.error('Error fetching report data:', error);
    } finally {
      setLoading(false);
    }
  };
  const handleExportReport = (reportType: string) => {};

  return (
    <div className="content-space-y-6 content-text-white">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
            Package Management
          </h1>
          <p className="content-text-muted-foreground">
            Manage training packages and track performance
          </p>
        </div>
      </div>

      <Border>
        <CardHeader>
          <div className="content-flex content-items-center content-justify-between">
            <div>
              <CardTitle className="content-mb-2 content-flex content-items-center content-gap-2">
                <BarChart className="content-size-5" />
                Package Performance & Reports ({reportData.total})
              </CardTitle>
              <CardDescription>
                Monitor completion rates, time spent, and certification metrics
              </CardDescription>
            </div>
            <Button
              onClick={() => handleExportReport('Performance Report')}
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
            <PerformanceReportLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Package Name</TableHead>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Assigned Users</TableHead>
                  <TableHead>License Used</TableHead>
                  <TableHead>Topics Completed</TableHead>
                  <TableHead>Completion Rate</TableHead>
                  <TableHead>Avg. Time Spent</TableHead>
                  <TableHead>Certifications</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {reportData?.data?.map((performance, index) => (
                  <TableRow key={index}>
                    <TableCell className="content-font-medium">
                      {performance.packageName}
                    </TableCell>
                    <TableCell>{performance.productName}</TableCell>
                    <TableCell>{performance.assignedUsers}</TableCell>
                    <TableCell>{performance.licensesUsed}</TableCell>
                    <TableCell>{performance.topicsCompleted}</TableCell>
                    <TableCell>
                      <div className="content-flex content-items-center content-gap-2">
                        <Progress
                          value={Number(
                            performance.completionRate.replace('%', ''),
                          )}
                          className="content-w-16"
                        />
                        <span className="content-text-sm">
                          {performance.completionRate}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>
                      {HumanizeDate(performance.avgTimeSpent)}
                    </TableCell>
                    <TableCell>
                      <div className="content-flex content-items-center content-gap-1">
                        <Award className="content-size-3" />
                        {performance.certifications}
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

export default ClientAdminPerformanceReport;
