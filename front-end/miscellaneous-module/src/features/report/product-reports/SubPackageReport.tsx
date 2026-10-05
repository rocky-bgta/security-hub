import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Progress } from 'common/Progress';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';

import { useEffect, useState } from 'react';

import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import {
  ISubPackageItem,
  ISubPackageReportData,
} from 'models/ProductPackageReports';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime, isSuccessResponse } from 'utils/Helper';

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

const SubPackageReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState({
    totalSubPackages: 0,
    activeUsage: 0,
    underutilized: 0,
  });
  const [subPackages, setSubPackages] = useState<ISubPackageItem[]>([]);

  useEffect(() => {
    const fetchSubPackageReport = async () => {
      try {
        const response: IResponse<ISubPackageReportData> = await apiClient.get(
          API_END_POINTS.GET_SUB_PACKAGE_REPORT,
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        const data = response.data;
        setSummary({
          totalSubPackages: data.totalSubPackages,
          activeUsage: data.activeUsage,
          underutilized: data.underutilized,
        });
        setSubPackages(data.subPackages || []);
      } catch (error) {
        console.error('Error fetching sub-package report:', error);
      }
    };

    fetchSubPackageReport();
  }, [apiClient]);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
        <StatCard label="Total Sub-Packages" value={summary.totalSubPackages} />
        <StatCard
          label="Active Usage"
          value={summary.activeUsage}
          color="text-primary"
        />
        <StatCard
          label="Underutilized"
          value={summary.underutilized}
          color="text-yellow-400"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Sub-Package Details</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Sub-Package</TableHead>
                <TableHead>Parent Package</TableHead>
                <TableHead>Users</TableHead>
                <TableHead>Usage %</TableHead>
                <TableHead>Last Accessed</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {subPackages.map((subPackage, index) => (
                <TableRow key={`${subPackage.name}-${index}`}>
                  <TableCell className="font-medium">
                    {subPackage.name}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {subPackage.parentPackage}
                  </TableCell>
                  <TableCell>{subPackage.users}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Progress
                        value={subPackage.usage}
                        className="h-2 w-16"
                      />
                      <span className="text-xs">{subPackage.usage}%</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    {formateDateAndTime(subPackage.lastAccessed)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default SubPackageReport;
