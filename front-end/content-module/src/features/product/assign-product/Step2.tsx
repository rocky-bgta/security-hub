import { ArrowLeft, ArrowRight, Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';

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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { IProductSelection } from 'models/Form';
import { IList, IResponse } from 'models/Global';
import { IProduct } from 'models/Product';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn } from 'utils/Helper';

interface IProps {
  data: IProductSelection;
  onUpdate: (data: IProductSelection) => void;
  onNext: () => void;
  onPrevious: () => void;
}

interface ProductSelectionErrors {
  selectedProduct?: string;
  selectedPackages?: string;
  [key: string]: string | undefined;
}

const MAX_SHOWN_PRODUCTS = 5;

const Step2 = ({ data, onUpdate, onNext, onPrevious }: IProps) => {
  const apiClient = useAPI();
  const [formData, setFormData] = useState<IProductSelection>({
    selectAll: data.selectAll || false,
    products: data.products || [],
  });
  const [selectedProduct, setSelectedProduct] = useState<IProduct | null>(null);
  const [search, setSearch] = useState<string>('');
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [errors, setErrors] = useState<ProductSelectionErrors>({});
  const [loading, setLoading] = useState<boolean>(true);
  const [productsList, setProductsList] = useState<IProduct[]>([]);

  const getProductById = (productId: string) => {
    return productsList.find(p => p.productId === productId);
  };

  const getPackageById = (productId: string, packageId: string) => {
    const product = productsList.find(p => p.productId === productId);
    return product?.packages.find(pkg => pkg.id.toString() === packageId);
  };

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response: IResponse<IList<IProduct>> = await apiClient.get(
        API_END_POINTS.PRODUCT_LIST + 'status=ENABLED&offset=0&pageSize=1000',
      );

      if (response.data?.items) {
        setProductsList(response.data.items);

        if (data.products?.length > 0) {
          const enrichedProducts = data.products.map(item => {
            const packageInfo = response
              .data!.items.find(p => p.productId === item.productId)
              ?.packages.find(pkg => pkg.id.toString() === item.packageId);

            return {
              ...item,
              pricePerLicense:
                packageInfo?.rangePricingResponse.find(
                  rp => rp.userRangeId === item.userRangeId,
                )?.yearlyPricePerUser ??
                packageInfo?.rangePricingResponse[0]?.yearlyPricePerUser ??
                packageInfo?.yearlyPrice ??
                item.pricePerLicense,
              licenseCount:
                item.licenseCount ||
                packageInfo?.rangePricingResponse[0]?.minUsers ||
                5,
              userRangeId:
                item.userRangeId ||
                packageInfo?.rangePricingResponse[0]?.userRangeId,
            };
          });

          setFormData(prev => ({ ...prev, products: enrichedProducts }));
        }
      }
    } catch (error) {
      console.error('Error fetching products:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  // Initialize selected product from existing data
  useEffect(() => {
    if (
      formData.products.length > 0 &&
      productsList.length > 0 &&
      !selectedProduct
    ) {
      const foundProduct = productsList.find(
        p => p.productId === formData.products[0].productId,
      );
      if (foundProduct) {
        setSelectedProduct(foundProduct);
      }
    }
  }, [productsList, formData.products]);

  const handleProductChange = (product: IProduct) => {
    const included = formData.products.find(
      item => item.productId === product.productId,
    );

    if (included) {
      // Remove all packages for this product
      setFormData(prev => ({
        ...prev,
        products: [
          ...prev.products.filter(item => item.productId !== product.productId),
        ],
      }));
    } else {
      // Find first enabled package
      const firstPackage = product.packages.find(
        pkg => pkg.packageStatus === 'ENABLED',
      );
      if (firstPackage) {
        const firstRange = firstPackage.rangePricingResponse[0];
        setFormData(prev => ({
          ...prev,
          products: [
            ...prev.products,
            {
              productId: product.productId,
              packageId: firstPackage.id.toString(),
              licenseCount: firstRange?.minUsers ?? 5,
              pricePerLicense:
                firstRange?.yearlyPricePerUser ?? firstPackage.yearlyPrice,
              validityPeriod: 1,
              validityUnit: 'YEAR',
              productName: product.productName,
              packageName: firstPackage.packageName,
              userRangeId: firstRange?.userRangeId,
            },
          ],
        }));
      }
    }
    if (errors.selectedProducts) {
      setErrors(prev => ({ ...prev, selectedProducts: '' }));
    }
  };

  const handleSelectProduct = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>,
    product: IProduct,
  ) => {
    e.preventDefault();
    e.stopPropagation();

    setSelectedProduct(product);
  };

  const handlePackageChange = (
    packageId: string,
    pkg: IProduct['packages'][number],
  ) => {
    const productId = selectedProduct!.productId;
    const existingPackage = formData.products.find(
      p => p.productId === productId && p.packageId === packageId,
    );

    if (existingPackage) {
      // Remove the package if it's already selected
      setFormData(prev => ({
        ...prev,
        products: prev.products.filter(
          p => !(p.productId === productId && p.packageId === packageId),
        ),
      }));
    } else {
      const firstRange = pkg.rangePricingResponse[0];
      const newProduct = {
        productId,
        packageId,
        licenseCount: firstRange?.minUsers ?? 5,
        pricePerLicense: firstRange?.yearlyPricePerUser ?? pkg.yearlyPrice,
        validityPeriod: 1,
        validityUnit: 'YEAR',
        productName: selectedProduct!.productName,
        packageName: pkg.packageName,
        userRangeId: firstRange?.userRangeId,
      };

      setFormData(prev => ({
        ...prev,
        products: [...prev.products, newProduct],
      }));
    }
  };

  const updatePackageDetails = (
    productId: string,
    packageId: string,
    field: 'licenseCount' | 'validityPeriod' | 'validityUnit' | 'userRangeId',
    value: number | string,
  ) => {
    setFormData(prev => ({
      ...prev,
      products: prev.products.map(p => {
        if (p.productId === productId && p.packageId === packageId) {
          const updatedProduct = { ...p, [field]: value };

          // If userRangeId is being updated, update pricePerLicense based on selected range
          if (field === 'userRangeId' && typeof value === 'string') {
            const packageInfo = getPackageById(productId, packageId);
            const selectedRange = packageInfo?.rangePricingResponse.find(
              range => range.userRangeId === value,
            );
            if (selectedRange) {
              updatedProduct.pricePerLicense = selectedRange.yearlyPricePerUser;
              updatedProduct.userRangeId = selectedRange.userRangeId;
            }
          }

          if (field === 'validityPeriod') {
            updatedProduct.validityPeriod = Math.max(1, value as number);
          }

          return updatedProduct;
        }
        return p;
      }),
    }));
  };

  const validateForm = () => {
    const newErrors: ProductSelectionErrors = {};

    if (!formData.selectAll) {
      if (formData.products.length === 0) {
        newErrors.selectedProduct = 'Please select at least one package';
      }

      formData.products.forEach(product => {
        if (product.licenseCount <= 4) {
          newErrors[`licenses_${product.productId}_${product.packageId}`] =
            'Number of licenses must be greater than 4';
        }

        // Validate license count against selected user range
        if (product.userRangeId) {
          const selectedRange = getPackageById(
            product.productId,
            product.packageId,
          )?.rangePricingResponse.find(
            range => range.userRangeId === product.userRangeId,
          );
          if (selectedRange) {
            if (
              product.licenseCount < selectedRange.minUsers ||
              product.licenseCount > selectedRange.maxUsers
            ) {
              newErrors[`licenses_${product.productId}_${product.packageId}`] =
                `Number of licenses must be between ${selectedRange.minUsers} and ${selectedRange.maxUsers} for the selected range`;
              toast.error(
                `Number of licenses must be between ${selectedRange.minUsers} and ${selectedRange.maxUsers} for the selected range`,
              );
            }
          }
        }

        if (product.validityPeriod <= 0) {
          newErrors[`validity_${product.productId}_${product.packageId}`] =
            'Validity period must be greater than 0';
        }
      });
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (validateForm()) {
      onUpdate(formData);
      onNext();
    }
  };

  const handleClickSelectAll = () => {
    setFormData(prev => ({
      selectAll: !prev.selectAll,
      products: [],
    }));
    setSearch('');
  };

  const filteredProducts = productsList
    .filter(
      p =>
        p.productName.toLowerCase().includes(search.toLowerCase()) ||
        p.productDescription.toLowerCase().includes(search.toLowerCase()),
    )
    .slice(
      currentPage * MAX_SHOWN_PRODUCTS,
      currentPage * MAX_SHOWN_PRODUCTS + MAX_SHOWN_PRODUCTS,
    );

  const getSelectedPackagesForProduct = (productId: string) => {
    return formData.products.filter(p => p.productId === productId);
  };

  const isPackageSelected = (productId: string, packageId: string) => {
    return formData.products.some(
      p => p.productId === productId && p.packageId === packageId,
    );
  };

  return (
    <Card className="content-w-full">
      <CardHeader>
        <CardTitle className="content-text-2xl content-font-bold">
          Product Selection
        </CardTitle>
        <p className="content-text-gray-400">
          Choose products and packages for your organization
        </p>
      </CardHeader>
      <CardContent className="content-space-y-6">
        <div className="content-grid content-grid-cols-1 content-gap-6 lg:content-grid-cols-2">
          <div className="content-space-y-4">
            <div className="content-flex content-items-center content-justify-between">
              <Label className="content-text-lg content-font-semibold">
                Products *
              </Label>
              <div className="content-flex content-items-center content-space-x-2">
                <Button size="sm" onClick={handleClickSelectAll}>
                  {formData.selectAll ? 'Deselect' : 'Select'} All
                </Button>
                <Input
                  type="text"
                  placeholder="Search products..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="content-w-48"
                />
              </div>
            </div>

            <div className="content-space-y-3">
              {loading ? (
                <div className="content-flex content-items-center content-justify-center">
                  <Loader2 className="content-animate-spin" />
                </div>
              ) : (
                filteredProducts.map(p => {
                  const selectedPackages = getSelectedPackagesForProduct(
                    p.productId,
                  );
                  return (
                    <div
                      key={p.productId}
                      className={cn(
                        'content-cursor-pointer content-rounded-lg content-border content-p-4 content-transition-all content-duration-200',
                        selectedProduct?.productId === p.productId
                          ? 'content-border content-border-green-500 content-bg-transparent content-text-gray-400'
                          : 'content-border-card-border content-text-gray-400 hover:content-border-gray-300',
                      )}
                      onClick={e => handleSelectProduct(e, p)}
                    >
                      <div className="content-flex content-items-center content-space-x-3">
                        <Checkbox
                          id={`product-${p.productId}`}
                          checked={
                            formData.selectAll || selectedPackages.length > 0
                          }
                          onCheckedChange={_ => handleProductChange(p)}
                          className="content-mt-1"
                        />
                        <div className="content-flex-1">
                          <Label
                            htmlFor={`product-${p.productId}`}
                            className="content-cursor-pointer"
                          >
                            <div className="content-flex content-items-center content-justify-between">
                              <h3 className="content-font-semibold content-text-white">
                                {p.productName}
                              </h3>
                              {selectedPackages.length > 0 && (
                                <Badge variant="secondary">
                                  {selectedPackages.length} package
                                  {selectedPackages.length > 1 ? 's' : ''}{' '}
                                  selected
                                </Badge>
                              )}
                            </div>
                            <p className="content-mt-1 content-text-sm content-text-cloudy-white">
                              {p.productDescription}
                            </p>
                          </Label>
                        </div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
            {errors.selectedProduct && (
              <p className="content-text-sm content-text-red-500">
                {errors.selectedProduct}
              </p>
            )}

            <div className="content-flex content-justify-end content-space-x-2">
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
                    prev + 1 <
                    Math.ceil(productsList.length / MAX_SHOWN_PRODUCTS)
                      ? prev + 1
                      : prev,
                  )
                }
                disabled={
                  currentPage + 1 >=
                  Math.ceil(productsList.length / MAX_SHOWN_PRODUCTS)
                }
              >
                <ArrowRight />
              </Button>
            </div>
          </div>

          <div className="content-space-y-4">
            <div className="content-flex content-items-center content-justify-between">
              <Label className="content-text-lg content-font-semibold">
                Packages *
              </Label>
            </div>

            {selectedProduct ? (
              <div className="content-space-y-3">
                <p className="content-text-sm content-text-gray-400">
                  Select multiple packages for {selectedProduct.productName}
                </p>
                {selectedProduct.packages.map(pkg => {
                  const isSelected = isPackageSelected(
                    selectedProduct.productId,
                    pkg.id.toString(),
                  );
                  const selectedPackage = formData.products.find(
                    p =>
                      p.productId === selectedProduct.productId &&
                      p.packageId === pkg.id.toString(),
                  );

                  // Use pricePerLicense from formData if available (set when user selects a range), otherwise use base price
                  const displayPrice =
                    selectedPackage?.pricePerLicense ?? pkg.price;

                  return (
                    <div
                      key={pkg.id.toString()}
                      className={cn(
                        'content-rounded-lg content-border content-p-4',
                        isSelected
                          ? 'content-border-green-500 content-bg-transparent'
                          : 'content-border-gray-200 hover:content-border-gray-300',
                      )}
                    >
                      <div className="content-mb-3 content-flex content-items-start content-space-x-3">
                        <Checkbox
                          id={`package-${pkg.id}`}
                          checked={isSelected}
                          onCheckedChange={() =>
                            handlePackageChange(pkg.id.toString(), pkg)
                          }
                          className="content-mt-1"
                        />
                        <div className="content-flex-1">
                          <label
                            htmlFor={`package-${pkg.id}`}
                            className="content-cursor-pointer"
                          >
                            <div className="content-flex content-items-center content-justify-between">
                              <h4 className="content-font-semibold content-text-cloudy-white">
                                {pkg.packageName}
                              </h4>
                              <Badge variant="secondary">
                                ${displayPrice.toFixed(2)}/year
                              </Badge>
                            </div>
                            <ul className="content-mt-1 content-text-sm content-text-gray-400">
                              {pkg.features.map(feature => (
                                <li key={feature.id}>• {feature.name}</li>
                              ))}
                            </ul>
                          </label>
                        </div>
                      </div>

                      {isSelected && selectedPackage && (
                        <div className="content-grid content-grid-cols-3 content-gap-3 content-border-t content-pt-3">
                          {pkg.rangePricingResponse.length > 0 && (
                            <div className="content-space-y-1">
                              <Label className="content-text-sm">
                                Select User Range
                              </Label>
                              <Select
                                value={selectedPackage.userRangeId || ''}
                                onValueChange={value =>
                                  updatePackageDetails(
                                    selectedProduct.productId,
                                    pkg.id.toString(),
                                    'userRangeId',
                                    value,
                                  )
                                }
                              >
                                <SelectTrigger className="content-w-full content-text-left">
                                  <SelectValue placeholder="Select range" />
                                </SelectTrigger>
                                <SelectContent>
                                  {pkg.rangePricingResponse.map(range => (
                                    <SelectItem
                                      key={range.userRangeId}
                                      value={range.userRangeId}
                                    >
                                      {range.rangeName} ($
                                      {range.yearlyPricePerUser}/user)
                                    </SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            </div>
                          )}
                          <div className="content-space-y-1">
                            <Label className="content-text-sm">
                              Number of Licenses
                            </Label>
                            <Input
                              type="text"
                              value={selectedPackage.licenseCount}
                              onChange={e =>
                                updatePackageDetails(
                                  selectedProduct.productId,
                                  pkg.id.toString(),
                                  'licenseCount',
                                  parseInt(e.target.value) || 0,
                                )
                              }
                              placeholder="0"
                              min="5"
                              className={
                                errors[
                                  `licenses_${selectedProduct.productId}_${pkg.id}`
                                ]
                                  ? 'content-has-error'
                                  : ''
                              }
                            />
                            {errors[
                              `licenses_${selectedProduct.productId}_${pkg.id}`
                            ] && (
                              <p className="content-text-xs content-text-red-500">
                                {
                                  errors[
                                    `licenses_${selectedProduct.productId}_${pkg.id}`
                                  ]
                                }
                              </p>
                            )}
                          </div>
                          <div className="content-space-y-1">
                            <Label className="content-text-sm">
                              Validity Period (Years)
                            </Label>
                            <div className="content-flex content-space-x-1">
                              <Input
                                type="text"
                                value={selectedPackage.validityPeriod}
                                onChange={e =>
                                  updatePackageDetails(
                                    selectedProduct.productId,
                                    pkg.id.toString(),
                                    'validityPeriod',
                                    Math.max(
                                      1,
                                      parseInt(e.target.value, 10) || 0,
                                    ),
                                  )
                                }
                                placeholder="1"
                                min="1"
                                className={`${
                                  errors[
                                    `validity_${selectedProduct.productId}_${pkg.id}`
                                  ]
                                    ? 'content-has-error'
                                    : ''
                                }`}
                              />
                              {/* <Select
                                value={selectedPackage.validityUnit}
                                onValueChange={value =>
                                  updatePackageDetails(
                                    selectedProduct.productId,
                                    pkg.id.toString(),
                                    'validityUnit',
                                    value,
                                  )
                                }
                              >
                                <SelectTrigger className="content-w-24">
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem value="MONTH">Months</SelectItem>
                                  <SelectItem value="YEAR">Years</SelectItem>
                                </SelectContent>
                              </Select> */}
                            </div>
                            {errors[
                              `validity_${selectedProduct.productId}_${pkg.id}`
                            ] && (
                              <p className="content-text-xs content-text-red-500">
                                {
                                  errors[
                                    `validity_${selectedProduct.productId}_${pkg.id}`
                                  ]
                                }
                              </p>
                            )}
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="content-py-8 content-text-center content-text-gray-500">
                {formData.selectAll
                  ? 'All the packages are selected'
                  : 'Select a product to view available packages'}
              </div>
            )}
            {errors.selectedPackages && (
              <p className="content-text-sm content-text-red-500">
                {errors.selectedPackages}
              </p>
            )}
          </div>
        </div>

        {formData.products.length > 0 && (
          <Card className="content-mt-4 content-pt-4">
            <CardContent>
              <h3 className="content-text-white">
                Selected Products and Packages
              </h3>
              <div className="content-mt-4 content-space-y-2">
                {formData.products.map(product => {
                  const productInfo = getProductById(product.productId);
                  const packageInfo = getPackageById(
                    product.productId,
                    product.packageId,
                  );
                  const lineTotal =
                    product.pricePerLicense *
                    product.licenseCount *
                    product.validityPeriod;
                  const validityLabel =
                    product.validityUnit.toLowerCase() +
                    (product.validityPeriod > 1 ? 's' : '');

                  return (
                    <Card
                      key={`${product.productId}-${product.packageId}`}
                      className="content-p-4"
                    >
                      <h4 className="content-flex content-items-start content-justify-between content-font-semibold content-text-cloudy-white">
                        <span>{productInfo?.productName}</span>
                        <span className="content-text-right content-text-sm content-font-normal content-text-gray-400">
                          ${product.pricePerLicense} × {product.licenseCount} ×{' '}
                          {product.validityPeriod} ={' '}
                          <span className="content-font-semibold content-text-cloudy-white">
                            ${lineTotal}
                          </span>
                        </span>
                      </h4>
                      <div className="content-mt-2 content-text-sm content-text-gray-400">
                        <p>
                          <strong>Package Name:</strong>{' '}
                          {packageInfo?.packageName}
                        </p>
                        <p>
                          <strong>Licenses:</strong> {product.licenseCount}
                        </p>
                        <p>
                          <strong>Validity:</strong> {product.validityPeriod}{' '}
                          {validityLabel}
                        </p>
                        <p>
                          <strong>Price per License:</strong> $
                          {product.pricePerLicense}/{validityLabel}
                        </p>
                      </div>
                    </Card>
                  );
                })}
              </div>

              <CardFooter className="content-flex content-justify-end content-px-4 content-pb-0 content-pt-6 content-font-semibold">
                Total: $
                {formData.products.reduce(
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

        {formData.selectAll && (
          <Card className="content-mt-4 content-pt-4">
            <CardContent>
              <h3 className="content-font-semibold content-text-white">
                All the products are selected
              </h3>
            </CardContent>
          </Card>
        )}

        <div className="content-flex content-justify-between content-pt-6">
          <Button
            variant="outline"
            onClick={onPrevious}
            className="content-px-8 content-py-2"
          >
            Previous
          </Button>
          <Button onClick={handleSave}>Save & Continue</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step2;
