import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { useAPI } from 'hooks/UseAPI';
import { Fragment, useEffect, useState } from 'react';
import { IPaymentReport } from 'models/Payment';
import {
  Calendar,
  CheckCircle,
  Download,
  Loader2,
  Package,
  Percent,
  Tag,
} from 'lucide-react';
import { getPaymentStatusBadge } from 'pages/payment/ClientPendingPayment';
import {
  formatCurrency,
  formatDateTime,
  isSuccessResponse,
} from 'utils/Helper';
import { Button } from 'common/Button';
import { API_END_POINTS } from 'routes/APIEndpoints';
import dayjs from 'dayjs';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Textarea } from 'common/Textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { toast } from 'react-toastify';

type DiscountType = 'PERCENTAGE' | 'FLAT';

const DISCOUNT_REASON_MIN_LENGTH = 10;
const DISCOUNT_REASON_MAX_LENGTH = 500;
const MIN_PAYABLE_AFTER_DISCOUNT = 1;

const isDiscountReasonValid = (reason: string) => {
  const trimmed = reason.trim();
  return (
    trimmed.length >= DISCOUNT_REASON_MIN_LENGTH &&
    trimmed.length <= DISCOUNT_REASON_MAX_LENGTH
  );
};

const getMaxDiscountAmount = (totalAmount: number) =>
  Math.max(
    0,
    Number((totalAmount - MIN_PAYABLE_AFTER_DISCOUNT).toFixed(2)),
  );

const getDiscountAmountInDollars = (
  type: DiscountType,
  value: number,
  totalAmount: number,
) => (type === 'PERCENTAGE' ? (value / 100) * totalAmount : value);

interface IProps {
  id: string;
  isOpen: boolean;
  onClose: () => void;
}

const AspireViewPendingPayment = ({ id, isOpen, onClose }: IProps) => {
  const apiClient = useAPI();
  const [reportDetails, setReportDetails] = useState<IPaymentReport | null>(
    null,
  );
  const [reportDetailsLoading, setReportDetailsLoading] = useState(false);
  const [applyCouponCode, setApplyCouponCode] = useState('');
  const [isApplyingCoupon, setIsApplyingCoupon] = useState(false);
  const [discountType, setDiscountType] = useState<DiscountType>('PERCENTAGE');
  const [discountValue, setDiscountValue] = useState<number>(0);
  const [discountReason, setDiscountReason] = useState('');
  const [isApplyingDiscount, setIsApplyingDiscount] = useState(false);

  const hasCouponApplied = !!reportDetails?.couponCode?.trim();
  const hasDiscountApplied = (reportDetails?.discountAmount ?? 0) > 0;
  const canApplyCoupon = !hasCouponApplied;
  const canApplyDiscount = !hasDiscountApplied;
  const invoiceTotalAmount = reportDetails?.totalAmount ?? 0;
  const maxDiscountAmount = getMaxDiscountAmount(invoiceTotalAmount);
  const maxDiscountPercentage =
    invoiceTotalAmount > 0
      ? Number(((maxDiscountAmount / invoiceTotalAmount) * 100).toFixed(2))
      : 0;
  const appliedDiscountInDollars = getDiscountAmountInDollars(
    discountType,
    discountValue,
    invoiceTotalAmount,
  );
  const isDiscountExceedingMax =
    discountValue > 0 &&
    Number(appliedDiscountInDollars.toFixed(2)) > maxDiscountAmount;

  useEffect(() => {
    if (id) {
      fetchPaymentReportDetails();
    }
  }, [id]);

  const fetchPaymentReportDetails = async () => {
    setReportDetailsLoading(true);

    try {
      const response = await apiClient.get(API_END_POINTS.INVOICE_DETAILS + id);
      setReportDetails(response.data);
      setApplyCouponCode('');
      setDiscountValue(0);
      setDiscountReason('');
    } catch (error) {
      console.error('Error fetching payment report details:', error);
    } finally {
      setReportDetailsLoading(false);
    }
  };

  const handleApplyCoupon = async () => {
    if (!applyCouponCode.trim()) {
      toast.error('Please enter a coupon code');
      return;
    }

    setIsApplyingCoupon(true);
    try {
      const response = await apiClient.post(
        API_END_POINTS.APPLY_COUPON.replace(':invoiceId', id),
        {
          data: {
            couponCode: applyCouponCode.trim(),
            sendEmail: true,
          },
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success(response.message || 'Coupon applied successfully');
        await fetchPaymentReportDetails();
      } else {
        toast.error(response.message || 'Failed to apply coupon');
      }
    } catch (error) {
      console.error('Error applying coupon:', error);
      toast.error('Failed to apply coupon. Please try again.');
    } finally {
      setIsApplyingCoupon(false);
    }
  };

  const handleApplyDiscount = async () => {
    const trimmedReason = discountReason.trim();

    if (trimmedReason.length < DISCOUNT_REASON_MIN_LENGTH) {
      toast.error(
        `Reason must be at least ${DISCOUNT_REASON_MIN_LENGTH} characters`,
      );
      return;
    }

    if (trimmedReason.length > DISCOUNT_REASON_MAX_LENGTH) {
      toast.error(
        `Reason must not exceed ${DISCOUNT_REASON_MAX_LENGTH} characters`,
      );
      return;
    }

    if (!discountValue || discountValue <= 0) {
      toast.error('Please enter a valid discount value');
      return;
    }

    if (discountType === 'PERCENTAGE' && discountValue > 100) {
      toast.error('Discount percentage cannot exceed 100%');
      return;
    }

    if (maxDiscountAmount <= 0) {
      toast.error(
        'Discount cannot be applied. At least $1 must remain payable.',
      );
      return;
    }

    if (isDiscountExceedingMax) {
      toast.error(
        `Discount cannot exceed $${maxDiscountAmount.toFixed(2)}. At least $1 must remain payable.`,
      );
      return;
    }

    const payload =
      discountType === 'PERCENTAGE'
        ? {
          discountType: 'PERCENTAGE',
          discountPercentage: discountValue,
          sendEmail: true,
          reason: trimmedReason,
        }
        : {
          discountType: 'FLAT',
          discountAmount: discountValue,
          sendEmail: true,
          reason: trimmedReason,
        };

    setIsApplyingDiscount(true);
    try {
      const response = await apiClient.post(
        API_END_POINTS.APPLY_DISCOUNT.replace(':invoiceId', id),
        { data: payload },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success(response.message || 'Discount applied successfully');
        await fetchPaymentReportDetails();
      } else {
        toast.error(response.message || 'Failed to apply discount');
      }
    } catch (error) {
      console.error('Error applying discount:', error);
      toast.error('Failed to apply discount. Please try again.');
    } finally {
      setIsApplyingDiscount(false);
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
            Complete details for invoice {reportDetails?.id}
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
                    <p className="font-mono text-sm">{reportDetails?.id}</p>
                  </div>
                  <div>
                    <span className="font-medium text-muted-foreground">
                      MSP Name:
                    </span>
                    <p className="font-mono text-sm">
                      {reportDetails?.mspName}
                    </p>
                  </div>
                  <div>
                    <span className="font-medium text-muted-foreground">
                      Client Name:
                    </span>
                    <p className="font-mono text-sm">
                      {reportDetails?.clientName}
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
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Sub Total Amount:
                    </span>
                    <p>{formatCurrency(reportDetails?.subtotal ?? 0, 'USD')}</p>
                  </div>
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Coupon Discount Amount:
                    </span>
                    <p>{formatCurrency(reportDetails?.couponDiscountAmount ?? 0, 'USD')}</p>
                  </div>
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Vat Percentage:
                    </span>
                    <p>{reportDetails?.vatPercentage}%</p>
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
                      {reportDetails?.discountPercentage}
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
                      <Calendar className="size-3" />
                      Invoice Created:
                    </span>
                    <p>{formatDateTime(reportDetails?.createdAt || '')}</p>
                  </div>
                  {reportDetails?.paidAt && (
                    <div>
                      <span className="flex items-center gap-1 font-medium text-muted-foreground">
                        <CheckCircle className="size-3" />
                        Paid date:
                      </span>
                      <p>{formatDateTime(reportDetails?.paidAt || '')}</p>
                    </div>
                  )}
                  {reportDetails?.paymentDate && (
                    <div>
                      <span className="flex items-center gap-1 font-medium text-muted-foreground">
                        Payment Date:
                      </span>
                      <p>{formatDateTime(reportDetails.paymentDate)}</p>
                    </div>
                  )}
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
                        handleDownloadReceipt(reportDetails?.id ?? '')
                      }
                    >
                      Download
                    </Button>
                  </div>
                  <div>
                    <span className="flex items-center gap-1 font-medium text-muted-foreground">
                      Transaction ID:
                    </span>
                    <p className="whitespace-normal break-all">
                      {reportDetails?.transactionId || 'N/A'}
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

            {canApplyCoupon && (
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2 text-lg">
                    <Tag className="size-4" />
                    Apply Coupon
                  </CardTitle>
                  <CardDescription>
                    Enter a coupon code to apply a discount to this invoice.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex gap-2">
                      <div className="flex-1">
                        <Input
                          placeholder="Enter coupon code"
                          value={applyCouponCode}
                          onChange={e => setApplyCouponCode(e.target.value)}
                          disabled={isApplyingCoupon}
                        />
                      </div>
                      <Button
                        onClick={handleApplyCoupon}
                        disabled={isApplyingCoupon || !applyCouponCode.trim()}
                        variant="outline"
                      >
                        {isApplyingCoupon ? (
                          <Loader2 className="size-4 animate-spin" />
                        ) : (
                          'Apply'
                        )}
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}

            {canApplyDiscount && (
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2 text-lg">
                    <Percent className="size-4" />
                    Apply Discount
                  </CardTitle>
                  <CardDescription>
                    Apply a percentage or flat discount to this invoice.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex gap-2">
                      <div className="flex-1">
                        <Label>Discount Type</Label>
                        <Select
                          value={discountType}
                          onValueChange={value =>
                            setDiscountType(value as DiscountType)
                          }
                        >
                          <SelectTrigger>
                            <SelectValue placeholder="Select discount type" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="PERCENTAGE">Percentage</SelectItem>
                            <SelectItem value="FLAT">Flat Amount</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="flex-1">
                        <Label htmlFor="aspireDiscountValue">
                          Discount{' '}
                          {discountType === 'PERCENTAGE' ? '(%)' : '($)'}
                        </Label>
                        <Input
                          id="aspireDiscountValue"
                          type="number"
                          value={discountValue || ''}
                          onChange={e =>
                            setDiscountValue(parseFloat(e.target.value) || 0)
                          }
                          placeholder="0"
                          min="0"
                          max={
                            discountType === 'PERCENTAGE'
                              ? maxDiscountPercentage
                              : maxDiscountAmount || undefined
                          }
                          step="0.01"
                          disabled={isApplyingDiscount}
                        />
                        <p className="mt-1 text-xs text-muted-foreground">
                          Maximum{' '}
                          {discountType === 'PERCENTAGE'
                            ? `${maxDiscountPercentage}%`
                            : `$${maxDiscountAmount.toFixed(2)}`}{' '}
                          (at least $1 must remain payable)
                        </p>

                      </div>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="aspireDiscountReason">
                        Reason <span className="text-destructive">*</span>
                      </Label>
                      <Textarea
                        id="aspireDiscountReason"
                        value={discountReason}
                        onChange={e =>
                          setDiscountReason(
                            e.target.value.slice(0, DISCOUNT_REASON_MAX_LENGTH),
                          )
                        }
                        placeholder="e.g. Finance approval"
                        rows={2}
                        maxLength={DISCOUNT_REASON_MAX_LENGTH}
                        disabled={isApplyingDiscount}
                      />
                      <p className="text-xs text-muted-foreground">
                        {discountReason.trim().length}/
                        {DISCOUNT_REASON_MAX_LENGTH} characters (minimum{' '}
                        {DISCOUNT_REASON_MIN_LENGTH})
                      </p>
                    </div>
                    <div className="flex justify-end">
                      <Button
                        onClick={handleApplyDiscount}
                        disabled={
                          isApplyingDiscount ||
                          !isDiscountReasonValid(discountReason) ||
                          !discountValue ||
                          maxDiscountAmount <= 0 ||
                          isDiscountExceedingMax
                        }
                      >
                        {isApplyingDiscount ? (
                          <>
                            <Loader2 className="mr-2 size-4 animate-spin" />
                            Applying...
                          </>
                        ) : (
                          'Apply Discount'
                        )}
                      </Button>
                    </div>
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

export default AspireViewPendingPayment;
