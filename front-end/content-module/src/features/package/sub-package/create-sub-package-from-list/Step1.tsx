import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { IList, IResponse } from 'models/Global';
import { IAssignedLicense } from 'models/License';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formatDateAndTime, isSuccessResponse } from 'utils/Helper';

interface IProps {
  data: { productId: string; packageId: string; productPackageId: string };
  onUpdate: (data: {
    productId: string;
    packageId: string;
    productPackageId: string;
  }) => void;
  onNext: () => void;
}

const Step1 = ({ data, onUpdate, onNext }: IProps) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();
  const [assignedLicenses, setAssignedLicenses] = useState<IAssignedLicense[]>(
    [],
  );
  const [productPackagesId, setProductPackagesId] = useState<string>('');
  const [selectedProductId, setSelectedProductId] = useState<string>('');
  const [selectedPackageId, setSelectedPackageId] = useState<string>('');
  const [selectedLicenseId, setSelectedLicenseId] = useState<string>('');
  const [errors, setErrors] = useState<{ product?: string; package?: string }>(
    {},
  );

  useEffect(() => {
    const fetchAssignedProducts = async () => {
      if (!userInfo?.userId) return;

      try {
        const response: IResponse<IList<IAssignedLicense>> =
          await apiClient.get(
            API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
              ':clientAdminId',
              userInfo.userId,
            ) + 'offset=0&pageSize=1000',
          );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }

        const filteredProduct = response?.data?.items.filter(item =>
          item.product?.tags?.includes('Security'),
        );

        setAssignedLicenses(filteredProduct);
      } catch (error) {
        console.error('Error fetching assigned products:', error);
        toast.error((error as Error).message);
      }
    };

    if (userInfo?.userId) {
      fetchAssignedProducts();
    }
  }, [userInfo?.userId]);

  useEffect(() => {
    if (data) {
      setSelectedProductId(data.productId);
      setSelectedPackageId(data.packageId);
      setProductPackagesId(data.productPackageId);
      // Find the licenseId for the selected package
      const license = assignedLicenses.find(
        l => l.packageId === data.packageId && l.productId === data.productId,
      );
      if (license) {
        setSelectedLicenseId(license.id);
      }
    }
  }, [data, assignedLicenses]);

  // Get unique products from assigned licenses
  const uniqueProducts = assignedLicenses.reduce(
    (acc, license) => {
      if (!acc.find(p => p.productId === license.productId)) {
        acc.push({
          productId: license?.productId ?? '',
          productName: license?.product?.productName ?? '',
        });
      }
      return acc;
    },
    [] as Array<{ productId: string; productName: string }>,
  );

  // Get packages for selected product - show all licenses even if same package
  const availablePackages = assignedLicenses
    .filter(license => license.productId === selectedProductId)
    .filter(license => license.packageDetails?.packageStatus === 'ENABLED')
    .map(license => ({
      id: license.packageId,
      licenseId: license.id,
      packageName: license.packageDetails?.packageName,
      packageStatus: license.packageDetails?.packageStatus,
      expiryDate: license.expiryDate,
    }));

  const handleProductChange = (productId: string) => {
    const productPackage = assignedLicenses.find(
      license => license.productId === productId,
    );
    setProductPackagesId(productPackage?.id ?? '');
    setSelectedProductId(productId);
    setSelectedPackageId('');
    setSelectedLicenseId('');
    setErrors(prev => ({ ...prev, product: '', package: '' }));
  };

  const handlePackageChange = (licenseId: string) => {
    const selectedLicense = assignedLicenses.find(
      license => license.id === licenseId,
    );
    if (selectedLicense) {
      setSelectedPackageId(selectedLicense.packageId);
      setProductPackagesId(selectedLicense.id);
      setSelectedLicenseId(licenseId);
    }
    setErrors(prev => ({ ...prev, package: '' }));
  };

  const handleSave = () => {
    const newErrors: { product?: string; package?: string } = {};

    if (!selectedProductId) {
      newErrors.product = 'Please select a product';
    }

    if (!selectedPackageId) {
      newErrors.package = 'Please select a package';
    }

    setErrors(newErrors);

    if (Object.keys(newErrors).length === 0) {
      onUpdate({
        productId: selectedProductId,
        packageId: selectedPackageId,
        productPackageId: productPackagesId,
      });
      onNext();
    }
  };

  return (
    <Card className="content-w-full">
      <CardHeader>
        <CardTitle>Select Product & Package</CardTitle>
      </CardHeader>
      <CardContent className="content-space-y-6">
        <div className="content-mx-auto content-max-w-lg content-space-y-4">
          <div>
            <Label htmlFor="product" className="content-text-cloudy-white">
              Product *
            </Label>
            <Select
              value={selectedProductId}
              onValueChange={handleProductChange}
            >
              <SelectTrigger
                id="product"
                className={errors.product ? 'content-has-error' : ''}
              >
                <SelectValue placeholder="Select a product" />
              </SelectTrigger>
              <SelectContent>
                {uniqueProducts.map(product => (
                  <SelectItem key={product.productId} value={product.productId}>
                    {product.productName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.product && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors.product}
              </p>
            )}
          </div>

          <div>
            <Label htmlFor="package" className="content-text-cloudy-white">
              Package *
            </Label>
            <Select
              value={selectedLicenseId}
              onValueChange={handlePackageChange}
              disabled={!selectedProductId}
            >
              <SelectTrigger
                id="package"
                className={errors.package ? 'content-has-error' : ''}
                disabled={!selectedProductId}
              >
                <SelectValue
                  placeholder={
                    selectedProductId
                      ? 'Select a package'
                      : 'Please select a product first'
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {availablePackages.map(pkg => (
                  <SelectItem key={pkg.licenseId} value={pkg.licenseId}>
                    {pkg.packageName}
                    {pkg.expiryDate &&
                      ` (Expires: ${formatDateAndTime(pkg.expiryDate)})`}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.package && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors.package}
              </p>
            )}
            {selectedProductId && availablePackages.length === 0 && (
              <p className="content-text-sm content-text-cloudy-white">
                No enabled packages available for this product
              </p>
            )}
          </div>
        </div>
        <div className="content-flex content-justify-end content-pt-6">
          <Button onClick={handleSave}>Save & Continue</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step1;
