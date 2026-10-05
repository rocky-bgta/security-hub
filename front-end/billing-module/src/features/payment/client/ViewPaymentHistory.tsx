import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { useAPI } from 'hooks/UseAPI';
import { Fragment, useEffect, useState } from 'react';
import { IPaymentReport, IPaymentSource } from 'models/Payment';
import { Download, Package } from 'lucide-react';
import { getPaymentStatusBadge } from 'pages/payment/ClientPendingPayment';
import { formatCurrency, formatDateTime, humanizeText } from 'utils/Helper';
import { Button } from 'common/Button';
import { API_END_POINTS } from 'routes/APIEndpoints';
import dayjs from 'dayjs';

interface IProps {
  id: string;
  isOpen: boolean;
  onClose: () => void;
}

const ViewPaymentHistory = ({ id, isOpen, onClose }: IProps) => {
  const apiClient = useAPI();
  const [reportDetails, setReportDetails] = useState<IPaymentReport | null>(
    null,
  );
  const [reportDetailsLoading, setReportDetailsLoading] = useState(false);

  useEffect(() => {
    if (id) {
      fetchPaymentReportDetails();
    }
  }, [id]);

  const fetchPaymentReportDetails = async () => {
    setReportDetailsLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.PAYMENT_HISTORY_DETAILS + id,
      );
      setReportDetails(response.data);
    } catch (error) {
      console.error('Error fetching payment report details:', error);
    } finally {
      setReportDetailsLoading(false);
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
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] w-2/3 overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Invoice Details</DialogTitle>
          <DialogDescription>
            Complete details for invoice {reportDetails?.invoiceId}
          </DialogDescription>
        </DialogHeader>
        {reportDetailsLoading ? (
          <div className="py-4 text-center text-muted-foreground">
            Loading...
          </div>
        ) : (
          <Fragment>
            <Card>
              <CardContent>
                <div className="mt-6 grid grid-cols-2 gap-4 text-sm md:grid-cols-3">
                  <div>
                    <span className="font-medium text-muted-foreground">
                      Invoice ID:
                    </span>
                    <p className="font-mono text-sm">
                      {reportDetails?.invoiceId}
                    </p>
                  </div>
                  <div>
                    <span className="font-medium text-muted-foreground">
                      Status:
                    </span>
                    <div className="mt-1 w-fit">
                      {getPaymentStatusBadge(reportDetails?.status || '')}
                    </div>
                  </div>
                  {reportDetails?.paymentDate && (
                    <div>
                      <span className="flex items-center gap-1 font-medium text-muted-foreground">
                        Payment Date:
                      </span>
                      <p>{formatDateTime(reportDetails.paymentDate)}</p>
                    </div>
                  )}
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Sub Total Amount:
                    </span>
                    <p>{formatCurrency(reportDetails?.subtotal ?? 0, 'USD')}</p>
                  </div>
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Vat Percentage:
                    </span>
                    <p>
                      {reportDetails?.subtotal && reportDetails?.subtotal > 0
                        ? (
                            (reportDetails.vatAmount /
                              (reportDetails.subtotal -
                                reportDetails.discountAmount)) *
                            100
                          ).toFixed(2)
                        : 0}
                      %
                    </p>
                  </div>
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Vat Amount:
                    </span>
                    <p>
                      {formatCurrency(reportDetails?.vatAmount ?? 0, 'USD')}
                    </p>
                  </div>
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Discount Percentage:
                    </span>
                    <p>
                      {reportDetails?.subtotal && reportDetails?.subtotal > 0
                        ? (
                            (reportDetails.discountAmount /
                              reportDetails.subtotal) *
                            100
                          ).toFixed(2)
                        : 0}
                      %
                    </p>
                  </div>
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Discount Amount:
                    </span>
                    <p>
                      {formatCurrency(
                        reportDetails?.discountAmount ?? 0,
                        'USD',
                      )}
                    </p>
                  </div>

                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Total Amount:
                    </span>
                    <p>
                      {formatCurrency(reportDetails?.totalAmount ?? 0, 'USD')}
                    </p>
                  </div>
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Payment Method:
                    </span>
                    <p>
                      {humanizeText(
                        reportDetails?.paymentSources
                          ?.map((source: IPaymentSource) => source.method)
                          ?.join(', ') ?? '',
                      )}
                    </p>
                  </div>
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Invoice Date:
                    </span>
                    <p>
                      {formatDateTime(reportDetails?.invoiceCreatedAt || '')}
                    </p>
                  </div>

                  {reportDetails?.paidAt &&
                    reportDetails?.productSelections?.length > 0 && (
                      <div>
                        <span className="font-medium text-muted-foreground">
                          Subscription Valid Until:
                        </span>
                        <p>
                          {dayjs(reportDetails.paidAt)
                            .add(
                              reportDetails.productSelections[0].validityPeriod,
                              reportDetails.productSelections[0].validityUnit.toLowerCase() as any,
                            )
                            .format('YYYY-MM-DD')}
                        </p>
                      </div>
                    )}

                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      <Download className="size-3" />
                      Invoice:
                    </span>
                    <Button
                      variant="link"
                      size="sm"
                      className="h-auto p-0 text-blue-600"
                      onClick={() =>
                        handleDownloadReceipt(reportDetails?.invoiceId ?? '')
                      }
                    >
                      Download
                    </Button>
                  </div>
                  <div className="col-span-1">
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Transaction ID:
                    </span>
                    <p className="whitespace-normal break-all">
                      {reportDetails?.transactionId}
                    </p>
                  </div>

                  <div className="col-span-1">
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Coupon Discount Amount:
                    </span>
                    <p className="whitespace-normal break-all">
                      {formatCurrency(reportDetails?.couponDiscountAmount ?? 0, 'USD')}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Product Selections */}
            {reportDetails?.productSelections &&
              reportDetails?.productSelections?.length > 0 && (
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-lg">
                      <Package className="size-4" />
                      Product Details
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {reportDetails?.productSelections.map(
                        (product: any, index: number) => (
                          <div
                            key={index}
                            className="rounded-lg border border-card-border p-3"
                          >
                            <div className="grid grid-cols-2 gap-3 text-sm md:grid-cols-4">
                              <div>
                                <span className="font-medium text-muted-foreground">
                                  Product:
                                </span>
                                <p>{product.productName}</p>
                              </div>
                              <div>
                                <span className="font-medium text-muted-foreground">
                                  Package:
                                </span>
                                <p>{product.packageName}</p>
                              </div>
                              <div>
                                <span className="font-medium text-muted-foreground">
                                  Licenses:
                                </span>
                                <p>{product.licenseCount}</p>
                              </div>
                              <div>
                                <span className="font-medium text-muted-foreground">
                                  Price/License:
                                </span>
                                <p>
                                  {formatCurrency(
                                    product.pricePerLicense,
                                    'USD',
                                  )}
                                </p>
                              </div>
                              <div className="col-span-1">
                                <span className="font-medium text-muted-foreground">
                                  Validity:
                                </span>
                                <p>
                                  {product.validityPeriod}{' '}
                                  {product.validityUnit.toLowerCase()}
                                  (s)
                                </p>
                              </div>
                              <div className="col-span-1">
                                <span className="font-medium text-muted-foreground">
                                  Total Amount:
                                </span>
                                <p>
                                  {formatCurrency(
                                    product.pricePerLicense *
                                      product.licenseCount *
                                      product.validityPeriod,
                                    'USD',
                                  )}
                                </p>
                              </div>
                            </div>
                          </div>
                        ),
                      )}
                    </div>
                  </CardContent>
                </Card>
              )}
          </Fragment>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ViewPaymentHistory;
