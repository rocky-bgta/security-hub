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
  IPaymentHistoryItem,
  IPaymentReportData,
} from 'models/BillingPaymentReports';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime, isSuccessResponse } from 'utils/Helper';

const formatStatusLabel = (status: string) => {
  const label = status.toLowerCase().replace(/_/g, ' ');
  return label.charAt(0).toUpperCase() + label.slice(1);
};

const StatusBadge = ({ status }: { status: string }) => {
  const variants: Record<string, string> = {
    successful: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    success: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    paid: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    completed: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    failed: 'bg-red-500/20 text-red-400 border-red-500/30',
    pending: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    refunded: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
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

const PaymentReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState({
    totalPayments: 0,
    totalRevenue: 0,
    successfulPayments: 0,
    failedPayments: 0,
  });
  const [payments, setPayments] = useState<IPaymentHistoryItem[]>([]);

  useEffect(() => {
    const fetchPaymentReport = async () => {
      try {
        const response: IResponse<IPaymentReportData> = await apiClient.get(
          API_END_POINTS.GET_PAYMENT_REPORT,
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        const data = response.data;
        setSummary({
          totalPayments: data.totalPayments,
          totalRevenue: data.totalRevenue,
          successfulPayments: data.successfulPayments,
          failedPayments: data.failedPayments,
        });
        setPayments(data.payments || []);
      } catch (error) {
        console.error('Error fetching payment report:', error);
      }
    };

    fetchPaymentReport();
  }, [apiClient]);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard label="Total Payments" value={summary.totalPayments} />
        <StatCard
          label="Total Revenue"
          value={`$${summary.totalRevenue.toLocaleString()}`}
          color="text-primary"
        />
        <StatCard
          label="Successful"
          value={summary.successfulPayments}
          color="text-emerald-400"
        />
        <StatCard
          label="Failed"
          value={summary.failedPayments}
          color="text-destructive"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Payment History</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Payer</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Method</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Invoice</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {payments.map((payment, index) => (
                <TableRow key={`${payment.id}-${index}`}>
                  <TableCell className="font-medium">{payment.id}</TableCell>
                  <TableCell>{formateDateAndTime(payment.date)}</TableCell>
                  <TableCell>{payment.payer}</TableCell>
                  <TableCell>${payment.amount.toLocaleString()}</TableCell>
                  <TableCell>{payment.method}</TableCell>
                  <TableCell>
                    <StatusBadge status={payment.status} />
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {payment.invoice}
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

export default PaymentReport;
