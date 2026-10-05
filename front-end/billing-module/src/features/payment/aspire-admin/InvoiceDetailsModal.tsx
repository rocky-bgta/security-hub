import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from 'common/Dialog';
import { Button } from 'common/Button';
import { Fragment, useEffect, useState } from 'react';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import dayjs from 'dayjs';
import { getPaymentStatusBadge } from 'pages/payment/ClientPendingPayment';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IPaymentReport } from 'models/Payment';
import { useFileDownload } from 'hooks/useFileDownload';

interface IProps {
  open: boolean;
  onClose: () => void;
  id: string;
}

const InvoiceDetailsModal = ({ open, onClose, id }: IProps) => {
  const { downloadFile } = useFileDownload();
  const apiClient = useAPI();
  const [loading, setLoading] = useState(false);
  const [reportDetails, setReportDetails] = useState<IPaymentReport | null>(
    null,
  );

  useEffect(() => {
    if (id) {
      fetchPaymentReportDetails();
    }
  }, [id]);

  const fetchPaymentReportDetails = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(API_END_POINTS.INVOICE_DETAILS + id);
      setReportDetails(response.data);
    } catch (error) {
      console.error('Error fetching payment report details:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadReceipt = async (id: string) => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.DOWNLOAD_INVOICE_PDF.replace(':id', id),
        {
          responseType: 'blob',
        },
      );

      const blob = new Blob([response], {
        type: 'application/pdf',
      });

      const url = window.URL.createObjectURL(blob);

      const link = document.createElement('a');
      link.href = url;
      link.download = `invoice-${id}.pdf`;
      document.body.appendChild(link);
      link.click();

      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading receipt:', error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Invoice Details</DialogTitle>
          <DialogDescription>Complete details</DialogDescription>
        </DialogHeader>
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-white">Loading...</div>
          </div>
        ) : (
          <Fragment>
            {reportDetails && (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="font-medium">Invoice:</span>
                    <p>{reportDetails?.id}</p>
                  </div>
                  <div>
                    <span className="font-medium">Date:</span>
                    <p>
                      {reportDetails?.createdAt
                        ? dayjs(reportDetails?.createdAt).format('DD-MM-YYYY')
                        : 'N/A'}
                    </p>
                  </div>
                  <div>
                    <span className="font-medium">Amount:</span>
                    <p>{reportDetails?.totalAmount?.toFixed(2)}</p>
                  </div>
                  <div>
                    <span className="font-medium">Status:</span>
                    <p>{getPaymentStatusBadge(reportDetails?.status)}</p>
                  </div>
                  <div className="col-span-2">
                    <span className="font-medium">Transaction ID:</span>
                    <p className="whitespace-normal break-all">
                      {reportDetails?.transactionId || 'N/A'}
                    </p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() =>
                      handleDownloadReceipt(reportDetails?.id ?? '')
                    }
                  >
                    Download Invoice
                  </Button>
                </div>
              </div>
            )}
          </Fragment>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default InvoiceDetailsModal;
