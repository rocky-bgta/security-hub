import { ArrowLeft, ArrowRight } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { IProductSelection } from 'models/Client';
import { IList, IResponse, ValidityUnit } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn } from 'utils/Helper';

interface IProps {
  data: Array<IProductSelection>;
  onUpdate: (
    data: Array<IProductSelection>,
    hasPhishingProduct: boolean,
  ) => void;
  onNext: () => void;
  onPrevious: () => void;
  preselectedProductId?: string;
}

interface ProductSelectionErrors {
  selectedProduct?: string;
  selectedPackages?: string;
  [key: string]: string | undefined;
}

interface Feature {
  id: string;
  name: string;
}

export interface RangePricingResponse {
  userRangeId: string;
  rangeName: string;
  pricePerUser: number;
  minUsers: number;
  maxUsers: number;
  yearlyPricePerUser: number;
}

export interface Package {
  id: string;
  packageName: string;
  productId: string;
  features: Feature[];
  price: number;
  packageStatus: string;
  basePackageId: string | null;
  isTrial: boolean;
  rangePricingResponse: RangePricingResponse[];
  yearlyPrice: number;
}

export interface Product {
  productId: string;
  productName: string;
  productDescription: string;
  productStatus: string;
  thumbnailUrl: string | null;
  tags: Array<string>;
  createdAt: string;
  updatedAt: string;
  lastModifiedBy: string;
  packages: Package[];
}

const MAX_SHOWN_PRODUCTS = 10;

const Step3 = ({
  data,
  onUpdate,
  onNext,
  onPrevious,
  preselectedProductId,
}: IProps) => {
  const [formData, setFormData] = useState<Array<IProductSelection>>([...data]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(
    data.length > 0
      ? products.find(p => p.productId === data[0].productId) || null
      : null,
  );

  const [search, setSearch] = useState<string>('');
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [errors, setErrors] = useState<ProductSelectionErrors>({});
  const [hasPhishingProduct, setHasPhishingProduct] = useState<boolean>(false);
  const preselectionAppliedRef = useRef(false);

  const apiClient = useAPI();

  const applyProductSelection = (product: Product) => {
    setSelectedProduct(product);
    setHasPhishingProduct(product.tags.includes('Phishing'));

    const firstPackage = product.packages.find(
      pkg => pkg.packageStatus === 'ENABLED' && !pkg.isTrial,
    );

    if (!firstPackage) return;

    const selection: IProductSelection = {
      productId: product.productId,
      productName: product.productName,
      packageId: firstPackage.id,
      packageName: firstPackage.packageName,
      licenseCount: firstPackage.rangePricingResponse[0]?.minUsers ?? 0,
      pricePerLicense:
        firstPackage.rangePricingResponse[0]?.yearlyPricePerUser ??
        firstPackage.yearlyPrice,
      validityPeriod: 1,
      validityUnit: ValidityUnit.YEAR,
      userRangeId: firstPackage.rangePricingResponse[0]?.userRangeId ?? '',
    };

    setFormData([selection]);
  };

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const response: IResponse<IList<Product>> = await apiClient.get(
          API_END_POINTS.PRODUCT_LIST + 'status=ENABLED&offset=0&pageSize=100',
        );

        if (response.data?.items) {
          setProducts(response.data.items);
        }
      } catch (error) {
        console.error('Error fetching products:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
    // Fetch once on mount — do not re-run when `data` gets a new array
    // reference from the parent (e.g. after BuyProduct loads client details).
  }, [apiClient]);

  useEffect(() => {
    if (products.length === 0) return;

    if (data.length > 0) {
      const newProductSelection = data.map(item => {
        const packageInfo = products
          .find((p: Product) => p.productId === item.productId)
          ?.packages.find((pkg: Package) => pkg.id === item.packageId);

        return {
          ...item,
          pricePerLicense:
            packageInfo?.rangePricingResponse.find(
              (rp: RangePricingResponse) => rp.userRangeId === item.userRangeId,
            )?.yearlyPricePerUser ??
            packageInfo?.yearlyPrice ??
            item.pricePerLicense,
          validityPeriod: item.validityPeriod || 1,
          licenseCount: item.licenseCount || 0,
        };
      });

      setFormData(newProductSelection);
      preselectionAppliedRef.current = true;

      const foundProduct = products.find(
        (p: Product) => p.productId === data[0].productId,
      );
      if (foundProduct) {
        setSelectedProduct(foundProduct);
        setHasPhishingProduct(foundProduct.tags.includes('Phishing'));
      }
      return;
    }

    if (preselectedProductId && !preselectionAppliedRef.current) {
      const foundProduct = products.find(
        (p: Product) => p.productId === preselectedProductId,
      );
      if (foundProduct) {
        preselectionAppliedRef.current = true;
        applyProductSelection(foundProduct);
        const productIndex = products.findIndex(
          (p: Product) => p.productId === preselectedProductId,
        );
        if (productIndex >= 0) {
          setCurrentPage(Math.floor(productIndex / MAX_SHOWN_PRODUCTS));
        }
      }
    }
  }, [data, products, preselectedProductId]);

  const handleProductChange = (product: Product) => {
    const included = formData.find(
        item => item.productId === product.productId,
      ),
      isProductPhishing = product.tags.includes('Phishing');

    if (included) {
      setFormData(prev => [
        ...prev.filter(item => item.productId !== product.productId),
      ]);
      if (isProductPhishing) setHasPhishingProduct(false);
    } else {
      const firstPackage = product.packages.find(
        pkg => pkg.packageStatus === 'ENABLED' && !pkg.isTrial,
      );
      if (firstPackage) {
        setFormData(prev => [
          ...prev,
          {
            productId: product.productId,
            productName: product.productName,
            packageId: firstPackage.id,
            packageName: firstPackage.packageName,
            licenseCount: firstPackage.rangePricingResponse[0]?.minUsers ?? 0,
            pricePerLicense:
              firstPackage.rangePricingResponse[0]?.yearlyPricePerUser ??
              firstPackage.yearlyPrice,
            validityPeriod: 1,
            validityUnit: ValidityUnit.YEAR,
            userRangeId:
              firstPackage.rangePricingResponse[0]?.userRangeId ?? '',
          },
        ]);
      }
      if (isProductPhishing) setHasPhishingProduct(true);
    }
    if (errors.selectedProducts) {
      setErrors(prev => ({ ...prev, selectedProducts: '' }));
    }
  };

  const handleSelectProduct = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>,
    product: Product,
  ) => {
    e.preventDefault();
    e.stopPropagation();

    setSelectedProduct(product);
  };

  const handlePackageChange = (packageId: string) => {
    const selectedPackage = selectedProduct!.packages.find(
      pkg => pkg.id === packageId,
    );

    if (!selectedPackage) return;

    const newProduct = {
      productId: selectedProduct!.productId,
      productName: selectedProduct!.productName,
      packageId: packageId,
      packageName: selectedPackage.packageName,
      licenseCount: selectedPackage.rangePricingResponse[0]?.minUsers ?? 0,
      pricePerLicense:
        selectedPackage.rangePricingResponse[0]?.yearlyPricePerUser ??
        selectedPackage.yearlyPrice,
      validityPeriod: 1,
      validityUnit: ValidityUnit.YEAR,
      userRangeId: selectedPackage.rangePricingResponse[0]?.userRangeId ?? '',
    };

    if (!formData.find(p => p.productId === selectedProduct!.productId)) {
      setFormData(prev => [...prev, newProduct]);
    } else {
      setFormData(prev => [
        ...prev.map(p =>
          p.productId === selectedProduct!.productId ? newProduct : p,
        ),
      ]);
    }
    setHasPhishingProduct(selectedProduct!.tags.includes('Phishing'));
  };

  const updatePackageDetails = (
    packageId: string,
    field: 'licenseCount' | 'validityPeriod' | 'userRangeId',
    value: number | string,
  ) => {
    if (!selectedProduct) return;
    const newProduct = formData.find(p => p.packageId === packageId);

    if (newProduct) {
      if (field === 'licenseCount' || field === 'validityPeriod') {
        (newProduct[field] as number) = value as number;

        if (field === 'licenseCount') {
          const { minUsers, maxUsers } = selectedProduct?.packages
            .find(pkg => pkg.id === packageId)
            ?.rangePricingResponse.find(
              rp => rp.userRangeId === newProduct.userRangeId,
            ) || { minUsers: 5, maxUsers: Infinity };

          if (minUsers <= (value as number) && maxUsers >= (value as number))
            setErrors(prev => ({ ...prev, [`licenses_${packageId}`]: '' }));
          else
            setErrors(prev => ({
              ...prev,
              [`licenses_${packageId}`]: `Number of licenses must be between ${minUsers} and ${maxUsers}`,
            }));
        }
      } else {
        (newProduct[field] as string) = value as string;
      }
      if (field === 'userRangeId') {
        newProduct.pricePerLicense =
          selectedProduct?.packages
            .find(pkg => pkg.id === packageId)
            ?.rangePricingResponse.find(rp => rp.userRangeId === value)
            ?.yearlyPricePerUser ?? newProduct.pricePerLicense;
      }
      setFormData(prev => [
        ...prev.map(p => (p.packageId === packageId ? newProduct : p)),
      ]);
    }
  };

  const validateForm = () => {
    const newErrors: ProductSelectionErrors = {};

    if (formData.length === 0) {
      newErrors.selectedProduct = 'Please select a product';
    }

    formData.forEach(product => {
      const { minUsers, maxUsers } = products
        .find(p => p.productId === product.productId)
        ?.packages.find(pkg => pkg.id === product.packageId)
        ?.rangePricingResponse.find(
          rp => rp.userRangeId === product.userRangeId,
        ) || {
        minUsers: 5,
        maxUsers: Infinity,
      };

      if (product.licenseCount < minUsers || product.licenseCount > maxUsers) {
        newErrors[`licenses_${product.packageId}`] =
          `Number of licenses must be between ${minUsers} and ${maxUsers}`;
        toast.error(
          `Number of licenses must be between ${minUsers} and ${maxUsers}`,
        );
      }

      if (product.validityPeriod <= 0) {
        newErrors[`validity_${product.packageId}`] =
          'Validity period must be greater than 0';
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (validateForm()) {
      onUpdate(formData, hasPhishingProduct);
      onNext();
    }
  };

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

  const selectedPackageId =
    formData.find(product => product.productId === selectedProduct?.productId)
      ?.packageId || '';

  const getProductById = (productId: string) => {
    return products.find(p => p.productId === productId);
  };

  const getPackageById = (productId: string, packageId: string) => {
    const product = getProductById(productId);
    return product?.packages.find(pkg => pkg.id === packageId);
  };

  if (loading) {
    return (
      <Card className="w-full">
        <CardHeader>
          <CardTitle className="text-2xl font-bold">
            Product Selection
          </CardTitle>
          <p className="text-gray-400">
            Choose a product and package for your organization
          </p>
        </CardHeader>
        <CardContent className="flex justify-center py-8">
          <div className="text-center text-gray-400">Loading products...</div>
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
              <Input
                type="text"
                placeholder="Search products..."
                value={search}
                onChange={e => setSearch(e.target.value)}
                className="w-64"
              />
            </div>

            <div className="space-y-3">
              {filteredProducts.map(product => (
                <div
                  key={product.productId}
                  className={cn(
                    'cursor-pointer rounded-lg border p-4 transition-all duration-200',
                    selectedProduct?.productId === product.productId
                      ? 'border-green-500 bg-transparent text-gray-400'
                      : 'border-muted-foreground text-gray-400 hover:border-gray-300',
                  )}
                  onClick={e => handleSelectProduct(e, product)}
                >
                  <div className="flex items-start space-x-3">
                    <Checkbox
                      id={`product-${product.productId}`}
                      checked={
                        !!formData.find(
                          item => item.productId === product.productId,
                        )
                      }
                      onCheckedChange={_ => handleProductChange(product)}
                      className="mt-1"
                    />
                    <div className="flex-1">
                      <Label
                        htmlFor={`product-${product.productId}`}
                        className="cursor-pointer"
                      >
                        <p className="text-xl font-semibold">
                          {product.productName}
                        </p>
                        <p className="mt-1 text-sm text-muted-foreground">
                          {product.productDescription ||
                            'No description available'}
                        </p>
                      </Label>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            {errors.selectedProduct && (
              <p className="text-sm text-red-500">{errors.selectedProduct}</p>
            )}

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
                <RadioGroup
                  value={selectedPackageId}
                  onValueChange={handlePackageChange}
                >
                  {selectedProduct.packages
                    .filter(
                      pkg => pkg.packageStatus === 'ENABLED' && !pkg.isTrial,
                    )
                    .map(pkg => (
                      <div
                        key={pkg.id}
                        className={cn(
                          'rounded-lg border p-4',
                          selectedPackageId === pkg.id
                            ? 'border-green-500 bg-transparent'
                            : 'border-gray-200 hover:border-gray-300',
                        )}
                      >
                        <div className="mb-3 flex items-start space-x-3">
                          <RadioGroupItem
                            value={pkg.id}
                            id={`package-${pkg.id}`}
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
                          <div className="flex w-full">
                            <Select
                              value={
                                formData.find(
                                  item =>
                                    item.productId ===
                                      selectedProduct.productId &&
                                    item.packageId === pkg.id,
                                )?.userRangeId ?? ''
                              }
                              onValueChange={value => {
                                updatePackageDetails(
                                  pkg.id,
                                  'userRangeId',
                                  value,
                                );
                              }}
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
                                    {tier.yearlyPricePerUser}/user per year
                                  </SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                          </div>
                        )}

                        {selectedPackageId === pkg.id && (
                          <div className="mt-3 grid grid-cols-2 gap-3 border-t pt-3">
                            <div className="space-y-1">
                              <Label className="text-sm">
                                Number of Licenses
                              </Label>
                              <Input
                                type="text"
                                value={
                                  formData.find(
                                    item =>
                                      item.productId ===
                                      selectedProduct.productId,
                                  )?.licenseCount
                                }
                                onChange={e =>
                                  updatePackageDetails(
                                    pkg.id,
                                    'licenseCount',
                                    parseInt(e.target.value) || 0,
                                  )
                                }
                                placeholder="0"
                                min="5"
                                className={
                                  errors[`licenses_${pkg.id}`]
                                    ? 'has-error'
                                    : ''
                                }
                              />
                              {errors[`licenses_${pkg.id}`] && (
                                <p className="text-xs text-red-500">
                                  {errors[`licenses_${pkg.id}`]}
                                </p>
                              )}
                            </div>
                            <div className="space-y-1">
                              <Label className="text-sm">
                                Validity Period (in years)
                              </Label>
                              <div className="flex space-x-1">
                                <Input
                                  type="number"
                                  value={
                                    formData.find(
                                      item =>
                                        item.productId ===
                                        selectedProduct.productId,
                                    )?.validityPeriod
                                  }
                                  onChange={e =>
                                    updatePackageDetails(
                                      pkg.id,
                                      'validityPeriod',
                                      parseInt(e.target.value) || 0,
                                    )
                                  }
                                  placeholder="1"
                                  min="1"
                                  className={`flex-1 ${errors[`validity_${pkg.id}`] ? 'has-error' : ''}`}
                                />

                                {/* <Select
                                  value={
                                    formData.find(
                                      item =>
                                        item.productId ===
                                        selectedProduct.productId,
                                    )?.validityUnit ?? ValidityUnit.MONTH
                                  }
                                  onValueChange={value =>
                                    updatePackageDetails(
                                      pkg.id,
                                      'validityUnit',
                                      value,
                                    )
                                  }
                                >
                                  <SelectTrigger className="w-24">
                                    <SelectValue />
                                  </SelectTrigger>
                                  <SelectContent>
                                    <SelectItem value={ValidityUnit.MONTH}>
                                      Months
                                    </SelectItem>
                                    <SelectItem value={ValidityUnit.YEAR}>
                                      Years
                                    </SelectItem>
                                  </SelectContent>
                                </Select> */}
                              </div>
                              {errors[`validity_${pkg.id}`] && (
                                <p className="text-xs text-red-500">
                                  {errors[`validity_${pkg.id}`]}
                                </p>
                              )}
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                </RadioGroup>
              </div>
            ) : (
              <div className="py-8 text-center text-gray-500">
                Select a product to view available packages
              </div>
            )}
            {errors.selectedPackages && (
              <p className="text-sm text-red-500">{errors.selectedPackages}</p>
            )}
          </div>
        </div>

        {formData.length > 0 && (
          <Card className="mt-4 pt-4">
            <CardContent>
              <h3>Selected Products</h3>
              <div className="mt-4 space-y-2">
                {formData.map(product => {
                  const productInfo = getProductById(product.productId);
                  const packageInfo = getPackageById(
                    product.productId,
                    product.packageId,
                  );

                  return (
                    <Card key={product.productId} className="p-4">
                      <h4 className="flex items-center justify-between font-semibold">
                        <span>{productInfo?.productName}</span>
                        <span>
                          $
                          {product.pricePerLicense *
                            product.licenseCount *
                            product.validityPeriod}
                        </span>
                      </h4>
                      <div className="mt-2 text-sm text-gray-400">
                        <p>
                          <strong>Package Name:</strong>{' '}
                          {packageInfo?.packageName}
                        </p>
                        <p>
                          <strong>Licenses:</strong> {product.licenseCount}
                        </p>
                        <p>
                          <strong>Validity:</strong> {product.validityPeriod}{' '}
                          {product.validityUnit.toLowerCase() +
                            (product.validityPeriod > 1 ? 's' : '')}
                        </p>
                        <p>
                          <strong>Price per License:</strong> $
                          {product.pricePerLicense}/
                          {product.validityUnit.toLowerCase()}
                        </p>
                      </div>
                    </Card>
                  );
                })}
              </div>

              <CardFooter className="flex justify-end px-4 pb-0 pt-6 font-semibold">
                Total: $
                {formData.reduce(
                  (total, product) =>
                    total +
                    product.pricePerLicense *
                      product.licenseCount *
                      product.validityPeriod,
                  0,
                )}
              </CardFooter>
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

export default Step3;
