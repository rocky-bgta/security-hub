import { Download, History } from 'lucide-react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
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
import { useStore } from 'hooks/UseStore';
import { IGetListParams, IResponse } from 'models/Global';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { HumanizeDate, objectToQueryString } from 'utils/Helper';

interface HistoryEntry {
  packageName: string;
  action: string;
  date: string;
  licensesAllocated: number;
  expiryDate: string;
  status: string;
}

interface HistoryResponse {
  total: number;
  historyList: HistoryEntry[];
}
const AspireAdminPackageLicense = () => {
  const { userInfo } = useStore();
  const [loading, setLoading] = useState<boolean>(true);
  const [licenseData, setLicenseData] = useState<HistoryResponse>({
    total: 0,
    historyList: [],
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
      fetchPackageData();
    }
  }, [searchDebounce]);

  const fetchPackageData = async () => {
    setLoading(true);

    try {
      const response: IResponse<HistoryResponse> = await apiClient.get(
        API_END_POINTS.LICENSE_HISTORY_LIST + queryString,
      );
      setLicenseData(response.data);
    } catch (error) {
      console.error('Error fetching license history data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleExportReport = (reportType: string) => {};

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge variant="default">Active</Badge>;
      case 'EXPIRING_SOON':
        return <Badge variant="destructive">Expiring Soon</Badge>;
      case 'EXPIRED':
        return <Badge variant="secondary">Expired</Badge>;
      default:
        return (
          <Badge
            variant="outline"
            className="content-border-transparent content-text-white"
          >
            {status}
          </Badge>
        );
    }
  };

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
                <History className="content-size-5" />
                License History ({licenseData?.total})
              </CardTitle>
              <CardDescription>
                Track license allocations, renewals, and expirations
              </CardDescription>
            </div>
            <Button
              onClick={() => handleExportReport('License History')}
              variant="outline"
              className="content-border-transparent content-bg-primary"
            >
              <Download className="content-mr-2 content-size-4" />
              Export History
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
                  <TableHead>Package Name</TableHead>
                  <TableHead>Action</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead>Licenses Allocated</TableHead>
                  <TableHead>Expiry Date</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {licenseData?.historyList.map(
                  (history: HistoryEntry, index: number) => (
                    <TableRow key={index}>
                      <TableCell className="content-font-medium">
                        {history.packageName}
                      </TableCell>
                      <TableCell>{history.action}</TableCell>
                      <TableCell>{HumanizeDate(history.date)}</TableCell>
                      <TableCell>{history.licensesAllocated}</TableCell>
                      <TableCell>{HumanizeDate(history.expiryDate)}</TableCell>
                      <TableCell>{getStatusBadge(history.status)}</TableCell>
                    </TableRow>
                  ),
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Border>
    </div>
  );
};

export default AspireAdminPackageLicense;
