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
  IInvoiceSummaryItem,
  IInvoiceSummaryReportData,
} from 'models/BillingPaymentReports';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime, isSuccessResponse } from 'utils/Helper';

const formatStatusLabel = (status: string) => {
  const label = status.toLowerCase().replace(/_/g, ' ');
  return label.charAt(0).toUpperCase() + label.slice(1);
};

const StatusBadge = ({ status }: { status: string }) => {
  const variants: Record<string, string> = {
    paid: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    unpaid: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    pending: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    disputed: 'bg-red-500/20 text-red-400 border-red-500/30',
    overdue: 'bg-red-500/20 text-red-400 border-red-500/30',
    partial: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
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

const InvoiceSummaryReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState({
    totalInvoices: 0,
    paidInvoices: 0,
    unpaidInvoices: 0,
    disputedInvoices: 0,
  });
  const [invoices, setInvoices] = useState<IInvoiceSummaryItem[]>([]);

  useEffect(() => {
    const fetchInvoiceSummaryReport = async () => {
      try {
        const response: IResponse<IInvoiceSummaryReportData> =
          await apiClient.get(API_END_POINTS.GET_INVOICE_SUMMARY_REPORT);

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        const data = response.data;
        setSummary({
          totalInvoices: data.totalInvoices,
          paidInvoices: data.paidInvoices,
          unpaidInvoices: data.unpaidInvoices,
          disputedInvoices: data.disputedInvoices,
        });
        setInvoices(data.invoices || []);
      } catch (error) {
        console.error('Error fetching invoice summary report:', error);
      }
    };

    fetchInvoiceSummaryReport();
  }, [apiClient]);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard label="Total Invoices" value={summary.totalInvoices} />
        <StatCard
          label="Paid"
          value={summary.paidInvoices}
          color="text-emerald-400"
        />
        <StatCard
          label="Unpaid"
          value={summary.unpaidInvoices}
          color="text-yellow-400"
        />
        <StatCard
          label="Disputed"
          value={summary.disputedInvoices}
          color="text-destructive"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Invoice Details</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Invoice ID</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Client</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Paid Date</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {invoices.map((invoice, index) => (
                <TableRow key={`${invoice.id}-${index}`}>
                  <TableCell className="font-medium">{invoice.id}</TableCell>
                  <TableCell>{formateDateAndTime(invoice.date)}</TableCell>
                  <TableCell>{invoice.client}</TableCell>
                  <TableCell>${invoice.amount.toLocaleString()}</TableCell>
                  <TableCell>
                    <StatusBadge status={invoice.status} />
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {invoice.paidDate
                      ? formateDateAndTime(invoice.paidDate)
                      : '-'}
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

export default InvoiceSummaryReport;
