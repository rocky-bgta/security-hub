export interface IPaymentHistoryItem {
  id: string;
  date: string;
  payer: string;
  amount: number;
  method: string;
  status: string;
  invoice: string;
}

export interface IPaymentReportData {
  totalPayments: number;
  totalRevenue: number;
  successfulPayments: number;
  failedPayments: number;
  payments: IPaymentHistoryItem[];
}

export interface IDuePaymentItem {
  client: string;
  amount: number;
  dueDate: string;
  daysOverdue: number;
  status: string;
  reminders: number;
}

export interface IDuePaymentReportData {
  totalDue: number;
  totalAmount: number;
  overdue: number;
  upcoming: number;
  payments: IDuePaymentItem[];
}

export interface IInvoiceSummaryItem {
  id: string;
  date: string;
  client: string;
  amount: number;
  status: string;
  paidDate: string;
}

export interface IInvoiceSummaryReportData {
  totalInvoices: number;
  paidInvoices: number;
  unpaidInvoices: number;
  disputedInvoices: number;
  invoices: IInvoiceSummaryItem[];
}
