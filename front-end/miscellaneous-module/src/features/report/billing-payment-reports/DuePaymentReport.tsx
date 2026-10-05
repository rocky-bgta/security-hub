import { Badge } from 'common/Badge';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
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
  IDuePaymentItem,
  IDuePaymentReportData,
} from 'models/BillingPaymentReports';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime, isSuccessResponse } from 'utils/Helper';

const formatStatusLabel = (status: string) => {
  const label = status.toLowerCase().replace(/_/g, ' ');
  return label.charAt(0).toUpperCase() + label.slice(1);
};

const StatusBadge = ({ status }: { status: string }) => {
  const variants: Record<string, string> = {
    overdue: 'bg-red-500/20 text-red-400 border-red-500/30',
    upcoming: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    due: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    pending: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    paid: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
  };
  const cls =
    variants[status.toLowerCase()] ||
    'bg-muted text-muted-foreground border-border';
  return (
    <Badge variant="outline" className={`${cls} text-xs`}>
      {formatStatusLabel(status)}
    </Badge>
  );
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

const DuePaymentReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState({
    totalDue: 0,
    totalAmount: 0,
    overdue: 0,
    upcoming: 0,
  });
  const [payments, setPayments] = useState<IDuePaymentItem[]>([]);

  useEffect(() => {
    const fetchDuePaymentReport = async () => {
      try {
        const response: IResponse<IDuePaymentReportData> = await apiClient.get(
          API_END_POINTS.GET_DUE_PAYMENT_REPORT,
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        const data = response.data;
        setSummary({
          totalDue: data.totalDue,
          totalAmount: data.totalAmount,
          overdue: data.overdue,
          upcoming: data.upcoming,
        });
        setPayments(data.payments || []);
      } catch (error) {
        console.error('Error fetching due payment report:', error);
      }
    };

    fetchDuePaymentReport();
  }, [apiClient]);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard label="Total Due" value={summary.totalDue} />
        <StatCard
          label="Total Amount"
          value={`$${summary.totalAmount.toLocaleString()}`}
          color="text-yellow-400"
        />
        <StatCard
          label="Overdue"
          value={summary.overdue}
          color="text-destructive"
        />
        <StatCard
          label="Upcoming"
          value={summary.upcoming}
          color="text-primary"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Due Payments</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Client</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Due Date</TableHead>
                <TableHead>Days Overdue</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Reminders Sent</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {payments.map((payment, index) => (
                <TableRow key={`${payment.client}-${index}`}>
                  <TableCell className="font-medium">
                    {payment.client}
                  </TableCell>
                  <TableCell>${payment.amount.toLocaleString()}</TableCell>
                  <TableCell>{formateDateAndTime(payment.dueDate)}</TableCell>
                  <TableCell
                    className={
                      payment.daysOverdue > 0 ? 'text-destructive' : ''
                    }
                  >
                    {payment.daysOverdue > 0 ? payment.daysOverdue : '-'}
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={payment.status} />
                  </TableCell>
                  <TableCell>{payment.reminders}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default DuePaymentReport;
