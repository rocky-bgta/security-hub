import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Popover, PopoverContent, PopoverTrigger } from 'common/Popover';
import { Progress } from 'common/Progress';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';

import { Filter } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

import { useAPI } from 'hooks/UseAPI';
import { IGetListParams, IResponse } from 'models/Global';
import {
  ISupportResolutionByType,
  ISupportResolutionTimeData,
} from 'models/SupportTicket';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';

interface ITicketResolutionQueryParams extends IGetListParams {
  fromDate?: string;
  toDate?: string;
}

const slaTableColorClass = (value: number) => {
  if (value >= 90) return 'text-emerald-400';
  if (value >= 80) return 'text-yellow-400';
  return 'text-destructive';
};

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

const TicketResolutionReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState({
    averageResolutionTime: 0,
    slaCompliance: 0,
    breaches: 0,
    escalated: 0,
  });
  const [bySupportType, setBySupportType] = useState<
    ISupportResolutionByType[]
  >([]);
  const [isFilterPopoverOpen, setIsFilterPopoverOpen] = useState(false);
  const [queryParams, setQueryParams] = useState<ITicketResolutionQueryParams>({
    fromDate: '',
    toDate: '',
  });
  const [draftFilters, setDraftFilters] = useState({
    fromDate: '',
    toDate: '',
  });

  const queryString = useMemo(
    () => objectToQueryString(queryParams),
    [queryParams],
  );

  useEffect(() => {
    const fetchTicketResolutionReport = async () => {
      try {
        const response: IResponse<ISupportResolutionTimeData> =
          await apiClient.get(
            API_END_POINTS.GET_SUPPORT_RESOLUTION_TIME + queryString,
          );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        const data = response.data;
        setSummary({
          averageResolutionTime: data.averageResolutionTime ?? 0,
          slaCompliance: data.slaCompliance ?? 0,
          breaches: data.breaches ?? 0,
          escalated: data.escalated ?? 0,
        });
        setBySupportType(data.bySupportType || []);
      } catch (error) {
        console.error('Error fetching ticket resolution report:', error);
      }
    };

    fetchTicketResolutionReport();
  }, [apiClient, queryString]);

  const handleResetQueries = () => {
    setQueryParams({
      fromDate: '',
      toDate: '',
    });
    setDraftFilters({
      fromDate: '',
      toDate: '',
    });
  };

  const handleFilterPopoverOpenChange = (isOpen: boolean) => {
    if (isOpen) {
      setDraftFilters({
        fromDate: queryParams.fromDate || '',
        toDate: queryParams.toDate || '',
      });
    }
    setIsFilterPopoverOpen(isOpen);
  };

  const handleApplyFilters = () => {
    setQueryParams({
      fromDate: draftFilters.fromDate,
      toDate: draftFilters.toDate,
    });
    setIsFilterPopoverOpen(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-end">
        <div className="flex items-center gap-2">
          <Popover
            open={isFilterPopoverOpen}
            onOpenChange={handleFilterPopoverOpenChange}
          >
            <PopoverTrigger asChild>
              <Button variant="outline">
                <Filter className="mr-2 size-4" />
                Filter
              </Button>
            </PopoverTrigger>
            <PopoverContent align="end" className="w-full space-y-4">
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <p className="text-sm font-medium">From Date</p>
                  <Input
                    type="date"
                    value={draftFilters.fromDate}
                    onChange={event =>
                      setDraftFilters(prevState => ({
                        ...prevState,
                        fromDate: event.target.value,
                      }))
                    }
                  />
                </div>

                <div className="space-y-2">
                  <p className="text-sm font-medium">To Date</p>
                  <Input
                    type="date"
                    value={draftFilters.toDate}
                    onChange={event =>
                      setDraftFilters(prevState => ({
                        ...prevState,
                        toDate: event.target.value,
                      }))
                    }
                  />
                </div>
              </div>

              <div className="flex justify-end">
                <Button type="button" onClick={handleApplyFilters}>
                  Apply
                </Button>
              </div>
            </PopoverContent>
          </Popover>

          <Button variant="destructive" onClick={handleResetQueries}>
            Reset
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard
          label="Avg Resolution Time"
          value={summary.averageResolutionTime}
        />
        <StatCard
          label="SLA Compliance"
          value={`${summary.slaCompliance}%`}
          color="text-primary"
        />
        <StatCard
          label="SLA Breaches"
          value={summary.breaches}
          color="text-destructive"
        />
        <StatCard
          label="Escalated"
          value={summary.escalated}
          color="text-yellow-400"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Resolution by Category</CardTitle>
        </CardHeader>
        <CardContent>
          {!bySupportType.length ? (
            <p className="py-6 text-center text-sm text-muted-foreground">
              No data available
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Type</TableHead>
                  <TableHead>Avg Time</TableHead>
                  <TableHead>Tickets</TableHead>
                  <TableHead>SLA Compliance</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {bySupportType.map(item => (
                  <TableRow key={item.supportTypeId}>
                    <TableCell className="font-medium">
                      {item.supportTypeName}
                    </TableCell>
                    <TableCell>{item.averageResolutionTime}</TableCell>
                    <TableCell>{item.totalTickets}</TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <Progress
                          value={item.slaCompliance}
                          className="h-2 w-20"
                        />
                        <span
                          className={`text-xs ${slaTableColorClass(item.slaCompliance)}`}
                        >
                          {item.slaCompliance}%
                        </span>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default TicketResolutionReport;
