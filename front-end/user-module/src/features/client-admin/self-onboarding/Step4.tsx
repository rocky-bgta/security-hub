import { useCallback, useEffect, useState } from 'react';
import { InfoIcon, Loader2Icon } from 'lucide-react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Separator } from 'common/Separator';
import { useAPI } from 'hooks/UseAPI';
import {
  DiscountType,
  IClientOnboarding,
  IInvoice,
  IProductSelection,
} from 'models/Client';
import { ICoupon } from 'models/Coupon';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  data: IInvoice;
  formData: IClientOnboarding;
  couponId?: string;
  loading: boolean;
  pendingPayment?: boolean;
  onUpdate: (data: IInvoice) => void;
  onNext: (invoice: IInvoice) => void;
  onPrevious: () => void;
}

interface IVatData {
  id: string;
  countryName: string;
  defaultVatRate: number;
  regionBased: boolean;
  regions: Array<{
    id: string;
    regionName: string;
    vatRate: number;
  }>;
  createdAt: string;
  updatedAt: string;
}

interface IDiscountConfig {
  discountType: DiscountType;
  discountPercentage: number;
  flatAmount: number;
}

interface IComputedTotals {
  subtotal: number;
  couponAmount: number;
  discountType: DiscountType;
  discountPercentage: number;
  discountAmount: number;
  vatAmount: number;
  totalAmount: number;
}

const formatMoney = (value: number): string => value.toFixed(2);

const resolveDiscountInput = (invoice: Partial<IInvoice>): IDiscountConfig => {
  const discountType = invoice.discountType as DiscountType;

  if (discountType === DiscountType.PERCENTAGE) {
    return {
      discountType,
      discountPercentage: invoice.discountPercentage ?? 0,
      flatAmount: 0,
    };
  }

  return {
    discountType,
    discountPercentage: 0,
    flatAmount: invoice.discountAmount ?? 0,
  };
};

const hasConfiguredDiscount = (config: IDiscountConfig): boolean =>
  (config.discountType === DiscountType.PERCENTAGE &&
    config.discountPercentage > 0) ||
  (config.discountType === DiscountType.FLAT && config.flatAmount > 0);

const getProductLineTotal = (product: IProductSelection): number => {
  const licenseTotal = product.pricePerLicense * product.licenseCount;
  return licenseTotal * product.validityPeriod;
};

const calculateSubtotal = (
  products: IProductSelection[] | undefined,
): number => {
  if (!products?.length) return 0;
  return products.reduce(
    (sum, product) => sum + getProductLineTotal(product),
    0,
  );
};

const isProductCouponEligible = (
  coupon: ICoupon,
  product: IProductSelection,
): boolean =>
  coupon.productRestrictions?.some(
    restriction =>
      restriction.productId === product.productId &&
      restriction.packageId === product.packageId,
  ) ?? false;

const calculateEligibleCouponSubtotal = (
  products: IProductSelection[] | undefined,
  coupon: ICoupon | null,
): number => {
  if (!products?.length || !coupon?.productRestrictions?.length) return 0;

  return products.reduce((sum, product) => {
    if (!isProductCouponEligible(coupon, product)) return sum;
    return sum + getProductLineTotal(product);
  }, 0);
};

const calculateCouponAmount = (
  coupon: ICoupon | null,
  eligibleSubtotal: number,
): number => {
  if (!coupon?.active || eligibleSubtotal <= 0) return 0;

  if (coupon.type === 'PERCENTAGE') {
    return (eligibleSubtotal * coupon.value) / 100;
  }

  if (coupon.type === 'FIXED') {
    return Math.min(coupon.value, eligibleSubtotal);
  }

  return 0;
};

const resolveCouponDiscountAmount = (
  coupon: ICoupon | null,
  products: IProductSelection[] | undefined,
): number => {
  if (!coupon) return 0;

  const eligibleSubtotal = coupon.productRestrictions?.length
    ? calculateEligibleCouponSubtotal(products, coupon)
    : calculateSubtotal(products);

  if (eligibleSubtotal <= 0) return 0;

  if (coupon.type === 'PERCENTAGE') {
    return (eligibleSubtotal * coupon.value) / 100;
  }

  if (coupon.type === 'FIXED') {
    return Math.min(coupon.value, eligibleSubtotal);
  }

  return 0;
};

const calculateDiscount = (
  config: IDiscountConfig,
  afterCoupon: number,
): Pick<
  IComputedTotals,
  'discountType' | 'discountPercentage' | 'discountAmount'
> => {
  const {
    discountType,
    discountPercentage: sourcePercentage,
    flatAmount,
  } = config;

  if (discountType === DiscountType.PERCENTAGE && sourcePercentage > 0) {
    const discountAmount = (afterCoupon * sourcePercentage) / 100;
    return {
      discountType,
      discountPercentage: sourcePercentage,
      discountAmount,
    };
  }

  if (discountType === DiscountType.FLAT && flatAmount > 0) {
    const discountAmount = Math.min(flatAmount, afterCoupon);
    return {
      discountType,
      discountPercentage:
        afterCoupon > 0 ? (discountAmount / afterCoupon) * 100 : 0,
      discountAmount,
    };
  }

  return {
    discountType,
    discountPercentage: 0,
    discountAmount: 0,
  };
};

const computeInvoiceTotals = ({
  products,
  coupon,
  discountConfig,
  vatRate,
}: {
  products: IProductSelection[] | undefined;
  coupon: ICoupon | null;
  discountConfig: IDiscountConfig;
  vatRate: number;
}): IComputedTotals => {
  const subtotal = calculateSubtotal(products);
  const eligibleSubtotal = calculateEligibleCouponSubtotal(products, coupon);
  const couponAmount = calculateCouponAmount(coupon, eligibleSubtotal);
  const afterCoupon = subtotal - couponAmount;

  const discount = calculateDiscount(discountConfig, afterCoupon);
  const afterDiscounts = afterCoupon - discount.discountAmount;
  const vatAmount = (afterDiscounts * vatRate) / 100;

  return {
    subtotal,
    couponAmount,
    ...discount,
    vatAmount,
    totalAmount: Math.max(0, afterDiscounts + vatAmount),
  };
};

const Step4 = ({
  data,
  formData,
  couponId = '',
  loading,
  pendingPayment = false,
  onUpdate,
  onNext,
  onPrevious,
}: IProps) => {
  const apiClient = useAPI();

  const [invoiceData, setInvoiceData] = useState<IInvoice>({ ...data });
  const [coupon, setCoupon] = useState<ICoupon | null>(null);
  const [couponDiscount, setCouponDiscount] = useState(0);
  const [couponLoading, setCouponLoading] = useState(false);
  const [discountConfig, setDiscountConfig] = useState<IDiscountConfig>(() =>
    resolveDiscountInput(data),
  );

  const products = formData.productSelections;
  const afterCouponAmount = invoiceData.subtotal - couponDiscount;
  const afterDiscountAmount = afterCouponAmount - invoiceData.discountAmount;

  const recalculateTotals = useCallback(() => {
    const totals = computeInvoiceTotals({
      products,
      coupon,
      discountConfig,
      vatRate: invoiceData.vatRate,
    });

    setCouponDiscount(totals.couponAmount);
    setInvoiceData(prev => ({
      ...prev,
      discountType: totals.discountType,
      subtotal: totals.subtotal,
      discountPercentage: totals.discountPercentage,
      discountAmount: totals.discountAmount,
      vatAmount: totals.vatAmount,
      totalAmount: totals.totalAmount,
    }));
  }, [products, coupon, discountConfig, invoiceData.vatRate]);

  useEffect(() => {
    const resolvedDiscount = resolveDiscountInput(data);

    if (hasConfiguredDiscount(resolvedDiscount)) {
      setDiscountConfig(resolvedDiscount);
    }

    setInvoiceData({ ...data });
  }, [data]);

  useEffect(() => {
    const loadCoupon = async () => {
      try {
        const response = couponId.trim()
          ? await apiClient.get(
              API_END_POINTS.GET_COUPON_BY_ID.replace(':id', couponId),
            )
          : data.couponCode?.trim()
            ? await apiClient.get(
                API_END_POINTS.GET_COUPON_BY_CODE.replace(
                  ':code',
                  data.couponCode,
                ),
              )
            : null;

        if (!response) return;

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }

        setCoupon(response.data);
        setInvoiceData(prev => ({
          ...prev,
          couponCode: response.data.code ?? prev.couponCode,
        }));
      } catch (error) {
        console.error('Error fetching coupon details:', error);
      }
    };

    if (!couponId.trim() && !data.couponCode?.trim()) return;
    loadCoupon();
  }, [apiClient, couponId, data.couponCode]);

  useEffect(() => {
    if (!pendingPayment) return;
    setCouponDiscount(resolveCouponDiscountAmount(coupon, products));
  }, [pendingPayment, coupon, products]);

  useEffect(() => {
    if (pendingPayment) return;

    const loadVatRate = async () => {
      try {
        const response: IResponse<IVatData> = await apiClient.get(
          API_END_POINTS.BILLING_VAT_RATE_BY_COUNTRY.replace(
            ':countryId',
            formData.billing?.country,
          ),
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error('Failed to fetch VAT data');
        }

        let vatRate = response.data?.defaultVatRate ?? 0;

        if (response.data?.regionBased && response.data.regions.length > 0) {
          const matchedRegion = response.data.regions.find(
            region => region.id === formData.billing?.stateProvince,
          );

          if (matchedRegion) {
            vatRate = matchedRegion.vatRate;
          }
        }

        setInvoiceData(prev => ({ ...prev, vatRate }));
      } catch (error) {
        console.error('Error fetching VAT data:', error);
      }
    };

    if (!formData.billing?.country) return;
    loadVatRate();
  }, [apiClient, formData.billing, pendingPayment]);

  useEffect(() => {
    if (pendingPayment) return;
    recalculateTotals();
  }, [pendingPayment, recalculateTotals]);

  const handleCouponCodeChange = (couponCode: string) => {
    setInvoiceData(prev => ({ ...prev, couponCode }));
  };

  const applyCoupon = async () => {
    try {
      setCouponLoading(true);

      const response = await apiClient.get(
        API_END_POINTS.GET_COUPON_BY_CODE.replace(
          ':code',
          invoiceData.couponCode,
        ),
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      const fetchedCoupon = response.data as ICoupon;

      if (!fetchedCoupon.active) {
        toast.error(response.message);
        setCoupon(null);
        setCouponDiscount(0);
        return;
      }

      const hasMatchingProduct = products?.some(product =>
        fetchedCoupon.productRestrictions?.some(
          restriction =>
            restriction.productId === product.productId &&
            restriction.packageId === product.packageId,
        ),
      );

      if (
        !hasMatchingProduct &&
        fetchedCoupon.productRestrictions?.length > 0
      ) {
        toast.warning(
          'This coupon is not applicable to your selected products/packages',
        );
      } else {
        toast.success(response.message);
      }

      setCoupon(fetchedCoupon);
    } catch (error) {
      console.error('Error fetching coupon by code:', error);
      toast.error((error as Error).message);
      setCoupon(null);
      setCouponDiscount(0);
      setInvoiceData(prev => ({ ...prev, couponCode: '' }));
    } finally {
      setCouponLoading(false);
    }
  };

  const handlePay = () => {
    onUpdate(invoiceData);
    onNext(invoiceData);
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl font-bold">Invoice Generation</CardTitle>
        <p className="text-gray-400">Generate and review your invoice</p>
      </CardHeader>

      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 gap-6 space-y-4">
          <Card className="pt-4">
            <CardContent>
              <h3 className="mb-4 text-lg font-semibold">Invoice Preview</h3>

              <div className="mb-4 flex items-start justify-between">
                <div>
                  <h4 className="font-medium">Bill To:</h4>
                  <p className="text-sm text-gray-400">
                    {formData.organization?.organizationName ||
                      'Organization Name'}
                  </p>
                  <p className="text-sm text-gray-400">
                    {formData.billing?.billingEmail || 'billing@company.com'}
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="coupon" className="flex items-center gap-2">
                    Coupon Code
                    <span className="flex items-center gap-2 text-xs text-gray-400">
                      <InfoIcon className="size-4 text-primary" />
                      Coupon can apply only for one product
                    </span>
                  </Label>

                  <div className="flex space-x-2">
                    <Input
                      id="coupon"
                      value={invoiceData.couponCode}
                      onChange={e => handleCouponCodeChange(e.target.value)}
                      placeholder="Enter coupon code"
                      disabled={pendingPayment}
                    />
                    <Button
                      variant="default"
                      onClick={applyCoupon}
                      disabled={
                        pendingPayment || couponLoading || couponDiscount > 0
                      }
                    >
                      {couponLoading ? (
                        <Loader2Icon className="size-4 animate-spin" />
                      ) : (
                        'Apply'
                      )}
                    </Button>
                  </div>

                  {couponDiscount > 0 && (
                    <p className="text-sm text-green-600">
                      Coupon applied: ${formatMoney(couponDiscount)} off
                    </p>
                  )}

                  {coupon &&
                    couponDiscount === 0 &&
                    coupon.productRestrictions?.length > 0 && (
                      <p className="text-sm text-yellow-600">
                        Coupon loaded but no matching products/packages selected
                      </p>
                    )}
                </div>
              </div>

              <Separator className="my-4" />

              <div className="space-y-4">
                <h4 className="font-medium">Items:</h4>
                {products?.map(product => {
                  const lineTotal = getProductLineTotal(product);
                  const isCouponEligible =
                    coupon && isProductCouponEligible(coupon, product);

                  return (
                    <div
                      key={`${product.productId}-${product.packageId}`}
                      className="flex justify-between text-sm text-gray-400"
                    >
                      <div className="space-y-1">
                        <h4>{product.productName}</h4>
                        <p>
                          {product.packageName} ({product.licenseCount} licenses
                          × ${product.pricePerLicense} per license ×{' '}
                          {product.validityPeriod}{' '}
                          {product.validityUnit.toLowerCase()})
                        </p>
                        {isCouponEligible && couponDiscount > 0 && (
                          <p className="text-xs text-green-500">
                            ✓ Coupon applicable
                          </p>
                        )}
                      </div>
                      <div className="text-right">
                        <p>${formatMoney(lineTotal)}</p>
                      </div>
                    </div>
                  );
                })}
              </div>

              <Separator className="my-4" />

              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span>Subtotal:</span>
                  <span>${formatMoney(invoiceData.subtotal)}</span>
                </div>

                {couponDiscount > 0 && (
                  <>
                    <div className="flex justify-between text-green-600">
                      <span>
                        Coupon
                        {invoiceData.couponCode
                          ? ` (${invoiceData.couponCode}${
                              coupon
                                ? ` - ${
                                    coupon.type === 'PERCENTAGE'
                                      ? `${coupon.value}%`
                                      : `$${coupon.value}`
                                  }`
                                : ''
                            })`
                          : ''}
                        :
                      </span>
                      <span>-${formatMoney(couponDiscount)}</span>
                    </div>
                    <div className="flex justify-between font-medium">
                      <span>After Coupon:</span>
                      <span>${formatMoney(afterCouponAmount)}</span>
                    </div>
                  </>
                )}

                {hasConfiguredDiscount(discountConfig) && (
                  <>
                    {discountConfig.discountType === DiscountType.PERCENTAGE ? (
                      <div className="flex justify-between text-green-600">
                        <span>
                          Discount ({discountConfig.discountPercentage}%):
                        </span>
                        <span>-${formatMoney(invoiceData.discountAmount)}</span>
                      </div>
                    ) : (
                      <div className="flex justify-between text-green-600">
                        <span>Discount (Flat):</span>
                        <span>-${formatMoney(invoiceData.discountAmount)}</span>
                      </div>
                    )}
                    <div className="flex justify-between font-medium">
                      <span>After Discount:</span>
                      <span>${formatMoney(afterDiscountAmount)}</span>
                    </div>
                  </>
                )}

                {(invoiceData.vatRate > 0 || invoiceData.vatAmount > 0) && (
                  <div className="flex justify-between text-yellow-600">
                    <span>
                      VAT
                      {invoiceData.vatRate > 0
                        ? ` (${invoiceData.vatRate}%)`
                        : ''}
                      :
                    </span>
                    <span>${formatMoney(invoiceData.vatAmount)}</span>
                  </div>
                )}

                <Separator />

                <div className="flex justify-between text-lg font-semibold">
                  <span>Total:</span>
                  <span>${formatMoney(invoiceData.totalAmount)}</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="flex justify-between pt-6">
          <Button
            variant="outline"
            onClick={onPrevious}
            className="px-8 py-2"
            disabled={pendingPayment || loading}
          >
            Previous
          </Button>
          <Button onClick={handlePay} disabled={loading} className="px-8 py-2">
            {loading ? 'Processing...' : 'Pay Now'}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step4;
