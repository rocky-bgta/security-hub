import { ArrowLeft, ArrowRight } from 'lucide-react';
import { Fragment, MouseEvent, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Package,
  Product,
  RangePricingResponse,
} from 'features/client-admin/onboarding/Step4';
import { useAPI } from 'hooks/UseAPI';
import { IList, IResponse, ValidityUnit } from 'models/Global';
import { IMSPSelectedProduct } from 'models/MSP';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn } from 'utils/Helper';

interface IProps {
  data: Array<IMSPSelectedProduct>;
  onUpdate: (data: Array<IMSPSelectedProduct>) => void;
  onNext: () => void;
  onPrevious: () => void;
}

const MAX_SHOWN_PRODUCTS = 10;

interface IErrors {
  id: string;
  message: string;
}

const getSelectedProductIds = (selections: Array<IMSPSelectedProduct>) =>
  new Set(selections.map(item => item.productId));

const createPackageSelection = (
  product: Product,
  pkg: Package,
): IMSPSelectedProduct => {
  const firstRange = pkg.rangePricingResponse[0];
  return {
    productId: product.productId,
    productName: product.productName,
    packageId: pkg.id,
    packageName: pkg.packageName,
    licenseCount: firstRange?.minUsers ?? 5,
    pricePerLicense:
      firstRange?.yearlyPricePerUser ?? pkg.yearlyPrice ?? pkg.price,
    validityPeriod: 1,
    validityUnit: ValidityUnit.YEAR,
    userRangeId: firstRange?.userRangeId ?? '',
  };
};

const MSPOnboardingStep3 = ({ data, onUpdate, onNext, onPrevious }: IProps) => {
  const [formData, setFormData] = useState<Array<IMSPSelectedProduct>>([
    ...data,
  ]);
  const [selectedProductIds, setSelectedProductIds] = useState<Set<string>>(
    () => getSelectedProductIds(data),
  );
  const [products, setProducts] = useState<Array<Product>>([]);
  const [selectAllProducts, setSelectAllProducts] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [selectedPackage, setSelectedPackage] = useState<Package | null>(null);
  const [productError, setProductError] = useState<Array<IErrors>>([]);
  const [packageError, setPackageError] = useState<Array<IErrors>>([]);
  const [search, setSearch] = useState<string>('');
  const [currentPage, setCurrentPage] = useState<number>(0);

  const apiClient = useAPI();

  useEffect(() => {
    fetchProducts();
  }, [data]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response: IResponse<IList<Product>> = await apiClient.get(
        API_END_POINTS.PRODUCT_LIST +
          'status=ENABLED&offset=0&pageSize=1000',
      );

      if (response.data?.items) {
        setProducts(response.data.items);

        if (data.length > 0) {
          const hydrated = data.map(item => {
            const packageInfo = response.data.items
              .find((p: Product) => p.productId === item.productId)
              ?.packages.find((pkg: Package) => pkg.id === item.packageId);

            const rangePricing = packageInfo?.rangePricingResponse.find(
              (rp: RangePricingResponse) => rp.userRangeId === item.userRangeId,
            );

            return {
              ...item,
              pricePerLicense:
                rangePricing?.yearlyPricePerUser ??
                packageInfo?.yearlyPrice ??
                item.pricePerLicense,
              validityPeriod: item.validityPeriod || 1,
              licenseCount: item.licenseCount || rangePricing?.minUsers || 5,
              validityUnit: item.validityUnit || ValidityUnit.YEAR,
            };
          });

          setFormData(hydrated);
          setSelectedProductIds(getSelectedProductIds(hydrated));
        }
      }
    } catch (error) {
      console.error('Error fetching products:', error);
    } finally {
      setLoading(false);
    }
  };

  const getRangeForSelection = (selection: IMSPSelectedProduct) => {
    const packageInfo = products
      .find(p => p.productId === selection.productId)
      ?.packages.find(pkg => pkg.id === selection.packageId);

    return (
      packageInfo?.rangePricingResponse.find(
        rp => rp.userRangeId === selection.userRangeId,
      ) || { minUsers: 5, maxUsers: Infinity }
    );
  };

  const handleClickProduct = (
    e: MouseEvent<HTMLDivElement>,
    product: Product,
  ) => {
    e.preventDefault();
    e.stopPropagation();
    setSelectedProduct(product);
  };

  const handleCheckProduct = (product: Product, checked: boolean) => {
    if (checked) {
      setSelectedProductIds(prev => {
        const next = new Set([...prev, product.productId]);
        if (next.size === products.length) setSelectAllProducts(true);
        return next;
      });
    } else {
      setSelectedProductIds(prev => {
        const next = new Set(prev);
        next.delete(product.productId);
        return next;
      });
      setFormData(prev =>
        prev.filter(item => item.productId !== product.productId),
      );
      setSelectAllProducts(false);
      setProductError(prev =>
        prev.filter(error => error.id !== product.productId),
      );
    }
  };

  const handleClickPackage = (e: MouseEvent<HTMLDivElement>, pkg: Package) => {
    e.preventDefault();
    e.stopPropagation();
    setSelectedPackage(pkg);
  };

  const handleCheckPackage = (pkg: Package, checked: boolean) => {
    if (!selectedProduct) return;

    setSelectedProductIds(prev => new Set([...prev, selectedProduct.productId]));

    if (checked) {
      const newSelection = createPackageSelection(selectedProduct, pkg);
      setFormData(prev => {
        const withoutDuplicate = prev.filter(
          item =>
            !(
              item.productId === selectedProduct.productId &&
              item.packageId === pkg.id
            ),
        );
        return [...withoutDuplicate, newSelection];
      });
    } else {
      setFormData(prev =>
        prev.filter(
          item =>
            !(
              item.productId === selectedProduct.productId &&
              item.packageId === pkg.id
            ),
        ),
      );
      setPackageError(prev => prev.filter(error => error.id !== pkg.id));
    }
  };

  const updatePackageDetails = (
    packageId: string,
    field: 'licenseCount' | 'validityPeriod' | 'userRangeId',
    value: number | string,
  ) => {
    if (!selectedProduct) return;

    setFormData(prev =>
      prev.map(selection => {
        if (
          selection.productId !== selectedProduct.productId ||
          selection.packageId !== packageId
        ) {
          return selection;
        }

        const updated = { ...selection };

        if (field === 'licenseCount' || field === 'validityPeriod') {
          updated[field] = value as number;
        } else {
          updated.userRangeId = value as string;
          const packageInfo = selectedProduct.packages.find(
            pkg => pkg.id === packageId,
          );
          updated.pricePerLicense =
            packageInfo?.rangePricingResponse.find(
              rp => rp.userRangeId === value,
            )?.yearlyPricePerUser ?? updated.pricePerLicense;
        }

        return updated;
      }),
    );
  };

  const validateForm = () => {
    const newProductError: Array<IErrors> = [];

    selectedProductIds.forEach(productId => {
      const hasPackage = formData.some(item => item.productId === productId);
      if (!hasPackage) {
        newProductError.push({
          id: productId,
          message: 'At least one package must be selected for this product.',
        });
      }
    });
    setProductError(newProductError);

    const newPackageError: Array<IErrors> = [];

    formData.forEach(selection => {
      const { minUsers, maxUsers } = getRangeForSelection(selection);

      if (
        selection.licenseCount < minUsers ||
        selection.licenseCount > maxUsers
      ) {
        newPackageError.push({
          id: selection.packageId,
          message: `Number of licenses must be between ${minUsers} and ${maxUsers}`,
        });
        setSelectedProduct(
          products.find(p => p.productId === selection.productId)!,
        );
        toast.error(
          `Number of licenses must be between ${minUsers} and ${maxUsers}`,
        );
      }

      if (selection.validityPeriod < 1) {
        newPackageError.push({
          id: selection.packageId,
          message: 'Validity period must be at least 1.',
        });
        setSelectedProduct(
          products.find(p => p.productId === selection.productId)!,
        );
      }
    });
    setPackageError(newPackageError);

    if (selectedProductIds.size === 0) {
      toast.error('Please select at least one product');
      return false;
    }

    return newProductError.length === 0 && newPackageError.length === 0;
  };

  const handleSave = () => {
    if (validateForm()) {
      onUpdate(formData);
      onNext();
    }
  };

  const handleClickSelectAll = () => {
    if (!selectAllProducts) {
      setSelectedProductIds(new Set(products.map(p => p.productId)));
      setSelectAllProducts(true);
    } else {
      setSelectedProductIds(new Set());
      setFormData([]);
      setSelectAllProducts(false);
    }
    setSearch('');
  };

  const getSelectionForPackage = (packageId: string) =>
    formData.find(
      item =>
        item.productId === selectedProduct?.productId &&
        item.packageId === packageId,
    );

  const filteredProducts = products
    .filter(
      product =>
        product.productStatus === 'ENABLED' &&
        (product.productName.toLowerCase().includes(search.toLowerCase()) ||
          product.productDescription
            .toLowerCase()
            .includes(search.toLowerCase())),
    )
    .slice(
      currentPage * MAX_SHOWN_PRODUCTS,
      currentPage * MAX_SHOWN_PRODUCTS + MAX_SHOWN_PRODUCTS,
    );

  if (loading) {
    return (
      <Card className="w-full">
        <CardHeader>
          <CardTitle className="text-2xl font-bold">
            Product Selection
          </CardTitle>
          <p className="text-gray-400">Loading products...</p>
        </CardHeader>
        <CardContent className="flex justify-center py-8">
          <div className="text-center">Loading...</div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl font-bold">Product Selection</CardTitle>
        <p className="text-gray-400">
          Choose a product and package for your organization
        </p>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <Label className="text-lg font-semibold">Products *</Label>
              <div className="flex items-center space-x-2">
                <Button size="sm" onClick={handleClickSelectAll}>
                  {selectAllProducts ? 'Deselect' : 'Select'} All
                </Button>
                <Input
                  type="text"
                  placeholder="Search products..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="w-48"
                />
              </div>
            </div>

            <div className="space-y-3">
              {filteredProducts.map(product => (
                <Fragment key={product.productId}>
                  <div
                    className={cn(
                      'cursor-pointer rounded-lg border p-4 transition-all duration-200',
                      selectedProduct?.productId === product.productId
                        ? 'border-primary bg-transparent text-gray-400'
                        : 'border-muted-foreground text-gray-400 hover:border-gray-300',
                    )}
                    onClick={e => handleClickProduct(e, product)}
                  >
                    <div className="flex items-start space-x-3">
                      <Checkbox
                        id={`product-${product.productId}`}
                        checked={selectedProductIds.has(product.productId)}
                        onCheckedChange={checked =>
                          handleCheckProduct(product, checked as boolean)
                        }
                        className="mt-1"
                      />
                      <div className="flex-1">
                        <Label
                          htmlFor={`product-${product.productId}`}
                          className="cursor-pointer"
                        >
                          <h3 className="font-semibold">
                            {product.productName}
                          </h3>
                          <p className="mt-1 text-sm">
                            {product.productDescription}
                          </p>
                        </Label>
                      </div>
                    </div>
                  </div>
                  {productError.find(
                    error => error.id === product.productId,
                  ) && (
                    <p className="text-sm text-red-500">
                      {
                        productError.find(
                          error => error.id === product.productId,
                        )?.message
                      }
                    </p>
                  )}
                </Fragment>
              ))}
            </div>

            <div className="flex justify-end space-x-2">
              <Button
                variant="outline"
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 0))}
                disabled={currentPage === 0}
              >
                <ArrowLeft />
              </Button>
              <Button
                variant="outline"
                onClick={() =>
                  setCurrentPage(prev =>
                    prev + 1 < Math.ceil(products.length / MAX_SHOWN_PRODUCTS)
                      ? prev + 1
                      : prev,
                  )
                }
                disabled={
                  currentPage + 1 >=
                  Math.ceil(products.length / MAX_SHOWN_PRODUCTS)
                }
              >
                <ArrowRight />
              </Button>
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <Label className="text-lg font-semibold">Packages *</Label>
            </div>

            {selectedProduct ? (
              <div className="space-y-3">
                {selectedProduct.packages
                  .filter(pkg => pkg.packageStatus === 'ENABLED')
                  .map(pkg => {
                    const selection = getSelectionForPackage(pkg.id);
                    const isPackageSelected = !!selection;

                    return (
                      <Fragment key={pkg.id}>
                        <div
                          className={cn(
                            'rounded-lg border p-4',
                            selectedPackage?.id === pkg.id
                              ? 'border-green-500 bg-transparent'
                              : 'border-gray-200 hover:border-gray-300',
                          )}
                          onClick={e => handleClickPackage(e, pkg)}
                        >
                          <div className="mb-3 flex items-start space-x-3">
                            <Checkbox
                              id={`package-${pkg.id}`}
                              checked={isPackageSelected}
                              onCheckedChange={checked =>
                                handleCheckPackage(pkg, checked as boolean)
                              }
                              className="mt-1"
                            />
                            <div className="flex-1">
                              <Label
                                htmlFor={`package-${pkg.id}`}
                                className="cursor-pointer"
                              >
                                <div className="flex items-center justify-between">
                                  <h4 className="font-semibold">
                                    {pkg.packageName}
                                  </h4>
                                  {pkg.rangePricingResponse.length === 0 && (
                                    <Badge variant="secondary">
                                      ${pkg.yearlyPrice}/year
                                    </Badge>
                                  )}
                                </div>
                                <ul className="mt-1 text-sm text-gray-400">
                                  {pkg.features.map(feature => (
                                    <li key={feature.id}>• {feature.name}</li>
                                  ))}
                                </ul>
                              </Label>
                            </div>
                          </div>

                          {pkg.rangePricingResponse.length > 0 && (
                            <div className="mb-3 w-full space-y-1">
                              <Label className="text-sm">User Range</Label>
                              <Select
                                value={selection?.userRangeId ?? ''}
                                onValueChange={value =>
                                  updatePackageDetails(
                                    pkg.id,
                                    'userRangeId',
                                    value,
                                  )
                                }
                                disabled={!isPackageSelected}
                              >
                                <SelectTrigger className="w-full">
                                  <SelectValue placeholder="Choose Pricing Tier" />
                                </SelectTrigger>
                                <SelectContent>
                                  {pkg.rangePricingResponse.map(tier => (
                                    <SelectItem
                                      key={tier.userRangeId}
                                      value={tier.userRangeId}
                                    >
                                      {tier.rangeName} - $
                                      {tier.yearlyPricePerUser}
                                      /user per year
                                    </SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            </div>
                          )}

                          {isPackageSelected && (
                            <div className="grid grid-cols-2 gap-3 border-t pt-3">
                              <div className="space-y-1">
                                <Label className="text-sm">
                                  Number of Licenses
                                </Label>
                                <Input
                                  type="number"
                                  value={selection?.licenseCount}
                                  onChange={e =>
                                    updatePackageDetails(
                                      pkg.id,
                                      'licenseCount',
                                      parseInt(e.target.value) || 0,
                                    )
                                  }
                                  placeholder="0"
                                  min="1"
                                />
                              </div>
                              <div className="space-y-1">
                                <Label className="text-sm">
                                  Validity Period (in years)
                                </Label>
                                <Input
                                  type="number"
                                  value={selection?.validityPeriod}
                                  onChange={e =>
                                    updatePackageDetails(
                                      pkg.id,
                                      'validityPeriod',
                                      parseInt(e.target.value) || 0,
                                    )
                                  }
                                  placeholder="1"
                                  min="1"
                                />
                              </div>
                            </div>
                          )}
                        </div>

                        {packageError
                          .filter(error => error.id === pkg.id)
                          .map((err, index) => (
                            <p key={index} className="text-sm text-red-500">
                              {err.message}
                            </p>
                          ))}
                      </Fragment>
                    );
                  })}
              </div>
            ) : (
              <div className="py-8 text-center text-gray-500">
                Select a product to view available packages
              </div>
            )}
          </div>
        </div>

        {formData.length > 0 && (
          <Card className="mt-4 pt-4">
            <CardContent>
              <h3>Selected Products</h3>
              <div className="mt-4 space-y-2">
                {formData.map(selection => (
                  <Card
                    key={`${selection.productId}-${selection.packageId}`}
                    className="p-4"
                  >
                    <h4 className="font-semibold">{selection.productName}</h4>
                    <div className="mt-2 text-sm text-gray-400">
                      <p>
                        <strong>Package Name:</strong> {selection.packageName}
                      </p>
                      <p>
                        <strong>Licenses:</strong> {selection.licenseCount}
                      </p>
                      <p>
                        <strong>Validity:</strong> {selection.validityPeriod}{' '}
                        {selection.validityUnit.toLowerCase()}
                        {selection.validityPeriod > 1 ? 's' : ''}
                      </p>
                      <p>
                        <strong>Price per License:</strong> $
                        {selection.pricePerLicense}/
                        {selection.validityUnit.toLowerCase()}
                      </p>
                    </div>
                  </Card>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        <div className="flex justify-between pt-6">
          <Button variant="outline" onClick={onPrevious} className="px-8 py-2">
            Previous
          </Button>
          <Button onClick={handleSave}>Save & Continue</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default MSPOnboardingStep3;
