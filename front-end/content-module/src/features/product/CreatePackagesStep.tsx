import { useEffect, useState } from 'react';
import { FaPlus } from 'react-icons/fa';
import { FiTrash2 } from 'react-icons/fi';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Checkbox } from 'common/Checkbox';
import CustomCheckbox from 'common/CustomCheckbox';
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
import { IFeatureDetails } from 'models/Feature';
import { IList, IResponse, Status } from 'models/Global';
import { INewPackageData } from 'models/Package';
import { INewProductData } from 'models/Product';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { cn, objectToQueryString } from 'utils/Helper';

interface IProps {
  productData: INewProductData;
  updateProductData: (updates: Partial<INewProductData>) => void;
}

const DEFAULT_PACKAGES = [
  {
    name: 'Silver',
    color: '!content-bg-gray-100 !content-border-gray-300',
    textColor: '!content-text-gray-800',
  },
  {
    name: 'Gold',
    color: '!content-bg-yellow-100 !content-border-yellow-300',
    textColor: '!content-text-yellow-800',
  },
  {
    name: 'Diamond',
    color: '!content-bg-blue-100 !content-border-blue-300',
    textColor: '!content-text-blue-800',
  },
  {
    name: 'Platinum',
    color: '!content-bg-purple-100 !content-border-purple-300',
    textColor: '!content-text-purple-800',
  },
];

const CreatePackagesStep = ({ productData, updateProductData }: IProps) => {
  const [selectedPackageId, setSelectedPackageId] = useState<string>('');
  const [features, setFeatures] = useState<IList<IFeatureDetails>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [userRanges, setUserRanges] = useState<
    Array<{ id: string; rangeName: string }>
  >([]);
  const [showCustomPackageForm, setShowCustomPackageForm] = useState(false);
  const [customPackageName, setCustomPackageName] = useState('');

  const apiClient = useAPI();

  useEffect(() => {
    fetchUserRanges();
    fetchFeatures();
  }, []);

  const fetchUserRanges = async () => {
    try {
      const response: IResponse<Array<{ id: string; rangeName: string }>> =
        await apiClient.get(API_END_POINTS.GET_USER_RANGES);

      setUserRanges(response.data);
    } catch (error) {
      console.error('Error fetching user ranges:', error);
    }
  };

  const fetchFeatures = async () => {
    try {
      const response: IResponse<IList<IFeatureDetails>> = await apiClient.get(
        API_END_POINTS.FEATURE_ENABLED +
          objectToQueryString({ offset: 0, pageSize: 1000 }),
      );

      setFeatures(response.data);
    } catch (error) {
      console.error('Error fetching features:', error);
    }
  };

  const createNewPackage = (packageType: string) => {
    const newPackage: INewPackageData = {
      id: Date.now().toString(),
      packageName: packageType,
      packagePrice: 0,
      packageYearlyPrice: 0,
      selectedFeatures: [],
      packageStatus: Status.ENABLED,
      isTrial: false,
      showInSite: false,
      rangePricing: [],
    };

    const updatedPackages = [...productData.packages, newPackage];
    updateProductData({ packages: updatedPackages });
    setSelectedPackageId(newPackage.id);
  };

  const updatePackage = (
    packageId: string,
    updates: Partial<INewPackageData>,
  ) => {
    const updatedPackages = productData.packages.map(pkg =>
      pkg.id === packageId ? { ...pkg, ...updates } : pkg,
    );
    updateProductData({ packages: updatedPackages });
  };

  const deletePackage = (packageId: string) => {
    const updatedPackages = productData.packages.filter(
      pkg => pkg.id !== packageId,
    );
    updateProductData({ packages: updatedPackages });
    if (selectedPackageId === packageId) {
      setSelectedPackageId('');
    }
  };

  const togglePackageFeature = (
    packageId: string,
    featureId: string,
    featureName: string,
    checked: boolean,
  ) => {
    const pkg = productData.packages.find(p => p.id === packageId);
    if (!pkg) return;

    const updatedFeatures = checked
      ? [...pkg.selectedFeatures, { id: featureId, featureName }]
      : pkg.selectedFeatures.filter(f => f.id !== featureId);

    updatePackage(packageId, { selectedFeatures: updatedFeatures });
  };

  const selectedPackage = productData.packages.find(
    pkg => pkg.id === selectedPackageId,
  );

  const availablePackageTypes = DEFAULT_PACKAGES.filter(
    defaultPkg =>
      !productData.packages.some(pkg => pkg.packageName === defaultPkg.name),
  );

  const availableUserRanges = userRanges.filter(
    ur => !selectedPackage?.rangePricing.some(rp => rp.userRangeId === ur.id),
  );

  const createCustomPackage = () => {
    if (!customPackageName.trim()) return;

    const newPackage: INewPackageData = {
      id: Date.now().toString(),
      packageName: customPackageName.trim(),
      packagePrice: 0,
      packageYearlyPrice: 0,
      selectedFeatures: [],
      packageStatus: Status.ENABLED,
      isTrial: false,
      showInSite: false,
      rangePricing: [],
    };

    const updatedPackages = [...productData.packages, newPackage];
    updateProductData({ packages: updatedPackages });
    setSelectedPackageId(newPackage.id);
    setCustomPackageName('');
    setShowCustomPackageForm(false);
  };

  return (
    <div className="content-space-y-8">
      <div className="content-mt-4 content-text-center">
        <h3 className="content-mb-2 content-text-xl content-font-semibold content-text-white">
          Create Multiple Packages
        </h3>
        <p className="content-text-ash-gray">
          Create different package tiers (Silver, Gold, Diamond, Platinum) with
          varying features and pricing
        </p>
      </div>

      <div className="content-grid content-grid-cols-1 content-gap-8 lg:content-grid-cols-3">
        <div className="content-space-y-4">
          <div className="content-flex content-items-center content-justify-between">
            <h4 className="content-font-semibold content-text-cloudy-white">
              Available Packages
            </h4>
          </div>

          {/* Custom Package Creation */}
          <div className="content-space-y-2">
            <p className="content-text-sm content-text-ash-gray">
              Create custom package:
            </p>
            {!showCustomPackageForm ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowCustomPackageForm(true)}
                className="content-flex content-w-full content-items-center content-justify-center content-border-2 content-border-dashed content-border-gray-300 content-text-gray-400 hover:content-border-gray-200 hover:content-text-gray-200"
              >
                <FaPlus className="content-mr-2 content-size-4" />
                Add Custom Package
              </Button>
            ) : (
              <div className="content-space-y-2 content-rounded-lg content-border content-border-card-border content-p-3">
                <Input
                  id="customPackageName"
                  placeholder="Enter package name..."
                  value={customPackageName}
                  onChange={e => setCustomPackageName(e.target.value)}
                  className="content-text-sm"
                />
                <div className="content-flex content-gap-2">
                  <Button
                    size="sm"
                    onClick={createCustomPackage}
                    disabled={!customPackageName.trim()}
                    className="content-flex-1"
                  >
                    Create
                  </Button>
                  <Button
                    size="sm"
                    variant="destructive"
                    onClick={() => {
                      setShowCustomPackageForm(false);
                      setCustomPackageName('');
                    }}
                    className="content-flex-1"
                  >
                    Cancel
                  </Button>
                </div>
              </div>
            )}
          </div>

          {availablePackageTypes.length > 0 && (
            <div className="content-space-y-2">
              <p className="content-text-sm content-text-ash-gray">
                Create new packages:
              </p>
              <div className="content-grid content-grid-cols-2 content-gap-2">
                {availablePackageTypes.map(packageType => (
                  <Button
                    key={packageType.name}
                    variant="outline"
                    size="sm"
                    onClick={() => createNewPackage(packageType.name)}
                    className={cn(
                      packageType.color,
                      packageType.textColor,
                      'content-flex content-items-center content-justify-center content-border-2',
                    )}
                  >
                    <FaPlus className="content-mr-1 content-size-4" />
                    {packageType.name}
                  </Button>
                ))}
              </div>
            </div>
          )}

          <div className="content-space-y-2">
            {productData.packages.map(pkg => {
              const packageStyle = DEFAULT_PACKAGES.find(
                p => p.name === pkg.packageName,
              );
              return (
                <Card
                  key={pkg.id}
                  className={cn('content-cursor-pointer')}
                  onClick={() => setSelectedPackageId(pkg.id)}
                >
                  <CardContent className="!content-p-4">
                    <div className="content-flex content-items-center content-justify-between">
                      <div>
                        <h5
                          className={cn(
                            'content-font-medium content-text-white',
                          )}
                        >
                          {pkg.packageName}
                        </h5>
                        <p className="content-text-sm content-text-ash-gray">
                          ${pkg.packagePrice}
                        </p>
                      </div>
                      <Button
                        size="sm"
                        onClick={e => {
                          e.stopPropagation();
                          deletePackage(pkg.id);
                        }}
                        className="!content-bg-transparent content-p-0 content-text-red-500 hover:content-text-red-700"
                      >
                        <FiTrash2 className="content-size-4" />
                      </Button>
                    </div>
                    <div className="content-mt-2">
                      <Badge variant="secondary" className="content-text-xs">
                        {pkg.packageStatus}
                      </Badge>
                      <Badge
                        variant="outline"
                        className="content-ml-2 content-text-xs content-text-white"
                      >
                        {pkg.selectedFeatures.length} features included
                      </Badge>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>

          {productData.packages.length === 0 && (
            <div className="content-py-8 content-text-center content-text-gray-500">
              <p>No packages created yet.</p>
              <p className="content-text-sm">
                Click a package type above to get started.
              </p>
            </div>
          )}
        </div>

        <div className="lg:content-col-span-2">
          {selectedPackage ? (
            <Card>
              <CardHeader>
                <CardTitle>
                  Configure {selectedPackage.packageName} Package
                </CardTitle>
              </CardHeader>
              <CardContent className="content-space-y-6">
                <div className="content-grid content-grid-cols-2 content-items-center content-gap-4">
                  <div className="content-space-y-1">
                    <Label htmlFor="packageName">Package Name</Label>
                    <Input
                      id="packageName"
                      value={selectedPackage.packageName}
                      onChange={e =>
                        updatePackage(selectedPackage.id, {
                          packageName: e.target.value,
                        })
                      }
                    />
                  </div>

                  <div className="content-space-y-1">
                    <Label htmlFor="packageStatus">Status</Label>
                    <Select
                      value={selectedPackage.packageStatus}
                      onValueChange={value =>
                        updatePackage(selectedPackage.id, {
                          packageStatus: value as Status,
                        })
                      }
                    >
                      <SelectTrigger disabled={false}>
                        <SelectValue placeholder="Select a user range" />
                      </SelectTrigger>
                      <SelectContent>
                        {[
                          {
                            id: Status.ENABLED,
                            value: Status.ENABLED,
                            label: 'Enabled',
                          },
                          {
                            id: Status.DISABLED,
                            value: Status.DISABLED,
                            label: 'Disabled',
                          },
                        ].map(option => (
                          <SelectItem key={option.id} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="content-space-y-1">
                    <Label htmlFor="packagePrice">Price Per Month ($)</Label>
                    <Input
                      id="packagePrice"
                      type="number"
                      value={selectedPackage.packagePrice}
                      onChange={e =>
                        updatePackage(selectedPackage.id, {
                          packagePrice: parseFloat(e.target.value) || 0,
                        })
                      }
                    />
                  </div>
                  <div className="content-space-y-1">
                    <Label htmlFor="packageYearlyPrice">
                      Price Per Year ($)
                    </Label>
                    <Input
                      id="packageYearlyPrice"
                      type="number"
                      value={selectedPackage.packageYearlyPrice}
                      onChange={e =>
                        updatePackage(selectedPackage.id, {
                          packageYearlyPrice: parseFloat(e.target.value) || 0,
                        })
                      }
                    />
                  </div>

                  <div className="content-flex content-items-center content-space-x-2">
                    <Checkbox
                      id="isTrial"
                      checked={selectedPackage.isTrial}
                      onCheckedChange={checked =>
                        updatePackage(selectedPackage.id, {
                          isTrial: checked === true,
                        })
                      }
                    />
                    <Label htmlFor="isTrial">Is Trial Package</Label>
                  </div>
                  <div className="content-flex content-items-center content-space-x-2">
                    <Checkbox
                      id="showInSite"
                      checked={selectedPackage.showInSite}
                      onCheckedChange={checked =>
                        updatePackage(selectedPackage.id, {
                          showInSite: checked === true,
                        })
                      }
                    />
                    <Label htmlFor="showInSite">Show in Site</Label>
                  </div>

                  <div className="content-col-span-2 content-rounded-lg content-border content-border-card-border content-p-4">
                    <div className="content-mb-4 content-flex content-justify-between">
                      <Label className="content-text-base content-font-medium">
                        User Range Pricing
                      </Label>
                      {availableUserRanges.length > 0 && (
                        <Button
                          size="sm"
                          onClick={() => {
                            updatePackage(selectedPackage.id, {
                              rangePricing: [
                                ...selectedPackage.rangePricing,
                                {
                                  userRangeId: availableUserRanges[0].id,
                                  pricePerUser: 0,
                                  yearlyPricePerUser: 0,
                                },
                              ],
                            });
                          }}
                        >
                          Add Range
                        </Button>
                      )}
                    </div>
                    {selectedPackage.rangePricing.length ? (
                      selectedPackage.rangePricing.map((rp, idx) => {
                        return (
                          <div
                            key={rp.userRangeId}
                            className="content-mb-2 content-flex content-items-center content-justify-between content-gap-2"
                          >
                            <Select
                              value={rp.userRangeId}
                              onValueChange={value => {
                                const userRangeId = value;
                                if (
                                  !availableUserRanges.find(
                                    ur => ur.id === userRangeId,
                                  )
                                ) {
                                  return;
                                }

                                const updatedRange =
                                  selectedPackage.rangePricing.map(
                                    (rangePrice, _idx) =>
                                      _idx === idx
                                        ? { ...rp, userRangeId }
                                        : rangePrice,
                                  );
                                updatePackage(selectedPackage.id, {
                                  rangePricing: updatedRange,
                                });
                              }}
                            >
                              <SelectTrigger className='content-flex-1' disabled={false}>
                                <SelectValue placeholder="Select a user range" />
                              </SelectTrigger>
                              <SelectContent>
                                {userRanges.map(option => (
                                  <SelectItem key={option.id} value={option.id}>
                                    {option.rangeName}
                                  </SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                            <div className="content-relative content-w-3/12">
                              <Input
                                id={`pricePerUser_${idx}`}
                                type="number"
                                placeholder="Price per user"
                                value={rp.pricePerUser}
                                onChange={e => {
                                  const pricePerUser =
                                    parseFloat(e.target.value) || 0;
                                  const updatedRangePricing =
                                    selectedPackage.rangePricing.map(pricing =>
                                      pricing.userRangeId === rp.userRangeId
                                        ? { ...pricing, pricePerUser }
                                        : pricing,
                                    );
                                  updatePackage(selectedPackage.id, {
                                    rangePricing: updatedRangePricing,
                                  });
                                }}
                                className="!content-overflow-hidden content-pr-[78px]"
                              />
                              <span className="content-absolute content-right-2 content-top-1/2 -content-translate-y-1/2 content-text-xs content-text-ash-gray">
                                $ per Month
                              </span>
                            </div>
                            <div className="content-relative content-w-3/12">
                              <Input
                                id={`yearlyPricePerUser_${idx}`}
                                type="number"
                                placeholder="Yearly price per user"
                                value={rp.yearlyPricePerUser}
                                onChange={e => {
                                  const yearlyPricePerUser =
                                    parseFloat(e.target.value) || 0;
                                  const updatedRangePricing =
                                    selectedPackage.rangePricing.map(pricing =>
                                      pricing.userRangeId === rp.userRangeId
                                        ? { ...pricing, yearlyPricePerUser }
                                        : pricing,
                                    );
                                  updatePackage(selectedPackage.id, {
                                    rangePricing: updatedRangePricing,
                                  });
                                }}
                                className="!content-overflow-hidden content-pr-[74px]"
                              />
                              <span className="content-absolute content-right-2 content-top-1/2 -content-translate-y-1/2 content-text-xs content-text-ash-gray">
                                $ per Year
                              </span>
                            </div>
                            <Button
                              size="sm"
                              onClick={e => {
                                e.stopPropagation();
                                const updatedRangePricing =
                                  selectedPackage.rangePricing.filter(
                                    pricing =>
                                      pricing.userRangeId !== rp.userRangeId,
                                  );
                                updatePackage(selectedPackage.id, {
                                  rangePricing: updatedRangePricing,
                                });
                              }}
                              className="!content-bg-transparent content-p-0 content-text-red-500 hover:content-text-red-700"
                            >
                              <FiTrash2 className="content-size-4" />
                            </Button>
                          </div>
                        );
                      })
                    ) : (
                      <p className="content-mt-2 content-text-xs content-text-gray-300">
                        No range pricing added yet.
                      </p>
                    )}
                  </div>
                </div>

                <div>
                  <Label className="content-text-base content-font-medium">
                    Package Features
                  </Label>
                  <p className="content-mb-2 content-text-sm content-text-ash-gray">
                    Select features to include in this package
                  </p>
                  <div className="content-mt-2 content-max-h-40 content-space-y-2 content-overflow-y-auto">
                    {features.items.map(feature => (
                      <div
                        key={feature.id}
                        className="content-relative content-flex content-items-center content-space-x-2"
                      >
                        <CustomCheckbox
                          id={`${selectedPackage.id}-${feature.id}`}
                          checked={selectedPackage.selectedFeatures.some(
                            f => f.id === feature.id,
                          )}
                          onChange={e =>
                            togglePackageFeature(
                              selectedPackage.id,
                              feature.id,
                              feature.featureName,
                              e.target.checked,
                            )
                          }
                        />
                        <Label
                          htmlFor={`${selectedPackage.id}-${feature.id}`}
                          className="content-text-sm"
                        >
                          {feature.featureName}
                        </Label>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="content-rounded-lg content-border content-border-card-border content-p-4">
                  <h5 className="content-mb-2 content-font-medium content-text-white">
                    Package Summary
                  </h5>
                  <div className="content-space-y-1 content-text-sm content-text-ash-gray">
                    <p>• Price: ${selectedPackage.packagePrice}</p>
                    <p>
                      • Features: {selectedPackage.selectedFeatures.length}{' '}
                      selected
                    </p>
                    <p>• Status: {selectedPackage.packageStatus}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardContent className="content-flex content-h-64 content-items-center content-justify-center">
                <div className="content-text-center content-text-gray-500">
                  <p>Select a package from the list to configure its details</p>
                  <p className="content-mt-1 content-text-sm">
                    Or create a new package to get started
                  </p>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};

export default CreatePackagesStep;
