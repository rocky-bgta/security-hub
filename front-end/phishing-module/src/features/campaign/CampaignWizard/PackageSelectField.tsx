import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAssignedPackages } from 'hooks/UseAssignedPackages';
import { CampaignChannel } from 'models/Campaign';
import { useEffect } from 'react';
import { formatDate } from 'utils/Helper';

interface PackageSelectFieldProps {
  channel: CampaignChannel;
  productPackageId?: string;
  onChange: (value: { productPackageId: string }) => void;
  productError?: string;
  packageError?: string;
}

export const PackageSelectField = ({
  channel,
  productPackageId,
  onChange,
  productError,
  packageError,
}: PackageSelectFieldProps) => {
  const {
    loading,
    error,
    productId: resolvedProductId,
    productName,
    packages,
  } = useAssignedPackages(channel);

  const selectedPackageId =
    !loading && packages.some(pkg => pkg.id === productPackageId)
      ? productPackageId
      : undefined;

  useEffect(() => {
    const firstPackageId = packages[0]?.id;
    if (loading || !firstPackageId || selectedPackageId) return;
    onChange({ productPackageId: firstPackageId });
  }, [loading, onChange, packages, selectedPackageId]);

  return (
    <div className="mb-8">
      <Label className="mb-3 block text-sm font-medium text-foreground">
        Licensed package <span className="text-vibrant-red">*</span>
      </Label>
      <p className="mb-3 text-xs text-muted-foreground">
        Choose the package this campaign should run against. The product is
        selected from your license for this channel.
      </p>
      <div className="grid grid-cols-1 items-start gap-4 sm:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor="campaign-product">Product</Label>
          <Input
            id="campaign-product"
            readOnly
            tabIndex={-1}
            value={loading ? 'Loading product…' : productName}
            placeholder="No product assigned"
            className="cursor-not-allowed bg-muted/50 text-foreground"
            aria-readonly="true"
          />
          {(productError || error) && (
            <p className="text-sm text-vibrant-red">{productError || error}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="campaign-package">
            Package <span className="text-vibrant-red">*</span>
          </Label>
          <Select
            value={selectedPackageId}
            onValueChange={value => onChange({ productPackageId: value })}
            disabled={loading || !resolvedProductId || packages.length === 0}
          >
            <SelectTrigger
              id="campaign-package"
              className={`w-full ${packageError ? 'border-vibrant-red' : ''}`}
            >
              <SelectValue
                placeholder={
                  loading
                    ? 'Loading packages…'
                    : resolvedProductId
                      ? 'Select a package'
                      : 'No package available'
                }
              />
            </SelectTrigger>
            <SelectContent>
              {packages.map(pkg => (
                <SelectItem key={pkg.id} value={pkg.id}>
                  {pkg.packageName}
                  {pkg.expiryDate
                    ? ` (expires ${formatDate(pkg.expiryDate)})`
                    : ''}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {packageError && (
            <p className="text-sm text-vibrant-red">{packageError}</p>
          )}
          {!loading && resolvedProductId && packages.length === 0 && (
            <p className="text-sm text-muted-foreground">
              No enabled packages are available for this product.
            </p>
          )}
        </div>
      </div>
    </div>
  );
};
