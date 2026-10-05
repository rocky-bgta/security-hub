import { useEffect, useState } from 'react';
import { FaCheckCircle } from 'react-icons/fa';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Card } from 'common/Card';
import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import { Progress } from 'common/Progress';
import CreatePackagesStep from 'features/product/CreatePackagesStep';
import FinalValidationStep from 'features/product/FinalValidationStep';
import ProductNameStep from 'features/product/ProductNameStep';
import { useAPI } from 'hooks/UseAPI';
import { IResponse, Status } from 'models/Global';
import { INewProductData, IProduct } from 'models/Product';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn, isSuccessResponse } from 'utils/Helper';

interface IProps {
  productId?: string;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
  showPackage?: boolean;
}

const normalizeProductTags = (
  tags?: Array<string | { id?: string; name?: string }>,
): Array<string> => {
  if (!Array.isArray(tags)) return [];

  return tags
    .map(tag => {
      if (typeof tag === 'string') return tag;
      return tag?.name || tag?.id || '';
    })
    .filter(Boolean);
};

const NewModal = ({
  productId = '',
  isOpen,
  onClose,
  onSubmit,
  showPackage,
}: IProps) => {
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [productData, setProductData] = useState<INewProductData>({
    productName: '',
    productStatus: '' as Status,
    productDescription: '',
    displayOrder: 0,
    packages: [],
    tags: [],
  });

  const apiClient = useAPI();

  const steps = [
    {
      number: 1,
      title: 'Product Name & Description',
      description: 'Create unique product name and description',
    },
    {
      number: 2,
      title: 'Create Packages',
      description: 'Setup Silver, Gold, Diamond, Platinum',
    },
    { number: 3, title: 'Final Validation', description: 'Review and confirm' },
  ];

  useEffect(() => {
    if (!isOpen || !productId) return;

    fetchProductData();
  }, [productId, isOpen]);

  const fetchProductData = async () => {
    try {
      const response: IResponse<IProduct> = await apiClient.get(
        API_END_POINTS.PRODUCT_DETAILS + productId,
      );
      const newData = {
        productName: response.data.productName,
        productDescription: response.data.productDescription,
        displayOrder:
          (response.data as { displayOrder?: number }).displayOrder ?? 0,
        tags: normalizeProductTags(
          response.data.tags as Array<string | { id?: string; name?: string }>,
        ),
        productStatus: response.data.productStatus as Status,
        packages: response.data.packages.map(item => ({
          id: item.id,
          packageName: item.packageName,
          packagePrice: item.price,
          packageYearlyPrice: item.yearlyPrice ?? 0,
          packageStatus: item.packageStatus,
          selectedFeatures: item.features.map(
            (fid: { id: string; name: string }) => ({
              id: fid.id,
              featureName: fid.name,
            }),
          ),
          basePackageId: item.basePackageId,
          productId: item.productId,
          isTrial: !!item.isTrial,
          showInSite: !!item.showInSite,
          rangePricing:
            item.rangePricingResponse?.map(rp => ({
              userRangeId: rp.userRangeId,
              pricePerUser: rp.pricePerUser,
              yearlyPricePerUser: rp.yearlyPricePerUser ?? 0,
            })) || [],
        })),
      };

      setProductData(newData);
      if (showPackage) {
        setCurrentStep(2);
      }
    } catch (error) {
      console.error('Error fetching product data:', error);
    }
  };

  const handleClose = () => {
    onClose();
    setCurrentStep(1);
    setProductData({
      productName: '',
      productDescription: '',
      displayOrder: 0,
      productStatus: '' as Status,
      packages: [],
      tags: [],
    });
  };

  const packageExists = async (name: string) => {
    try {
      const response: IResponse<boolean> = await apiClient.get(
        API_END_POINTS.PACKAGE_EXISTS + name,
      );
      if (response.data) {
        setCurrentStep(2);
      } else {
        setCurrentStep(1);
        toast.error(
          'Product name already exists. Please choose a different name.',
        );
      }
    } catch (error) {
      console.error('Error checking package existence:', error);
    }
  };

  const handleNext = async () => {
    if (
      currentStep === 1 &&
      productData.productName.trim() &&
      productData.productName.length >= 3 &&
      !productId
    ) {
      packageExists(productData.productName);
    }

    if (currentStep === 3) {
      setIsLoading(true);
      const createPayload = {
        productName: productData.productName,
        productDescription: productData.productDescription.trim(),
        displayOrder: productData.displayOrder ?? 0,
        productStatus: Status.ENABLED,
        packages: productData.packages.map((pkg: any) => ({
          packageName: pkg.packageName,
          features: pkg.selectedFeatures.map((f: any) => ({
            id: f.id,
            name: f.featureName,
          })),
          price: pkg.packagePrice,
          yearlyPrice: pkg.packageYearlyPrice,
          packageStatus: pkg.packageStatus,
          isTrial: pkg.isTrial,
          showInSite: pkg.showInSite,
          rangePricing: pkg.rangePricing.map((rp: any) => ({
            userRangeId: rp.userRangeId,
            pricePerUser: rp.pricePerUser,
            yearlyPricePerUser: rp.yearlyPricePerUser,
          })),
        })),
        tags: productData.tags,
      };

      const updatePayload = {
        productName: productData.productName,
        productDescription: productData.productDescription.trim(),
        displayOrder: productData.displayOrder ?? 0,
        productStatus: productData.productStatus,
        packages: productData.packages.map((pkg: any) => ({
          id: pkg.id,
          packageName: pkg.packageName,
          features: pkg.selectedFeatures.map((f: any) => ({
            id: f.id,
            name: f.featureName,
          })),
          price: pkg.packagePrice,
          yearlyPrice: pkg.packageYearlyPrice,
          packageStatus: pkg.packageStatus,
          basePackageId: pkg.basePackageId,
          productId: pkg.productId,
          isTrial: pkg.isTrial,
          showInSite: pkg.showInSite,
          rangePricing: pkg.rangePricing.map((rp: any) => ({
            userRangeId: rp.userRangeId,
            pricePerUser: rp.pricePerUser,
            yearlyPricePerUser: rp.yearlyPricePerUser,
          })),
        })),
        tags: productData.tags,
      };

      let response;
      try {
        if (productId) {
          response = await apiClient.put(
            API_END_POINTS.PRODUCT_UPDATE + productId,
            { data: updatePayload },
          );
        } else {
          response = await apiClient.post(API_END_POINTS.PRODUCT_CREATE, {
            data: createPayload,
          });
        }
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'Failed to save product');
        }

        onSubmit();
        handleClose();
        toast.success(response.message);
      } catch (error) {
        console.error('Error creating/updating product:', error);
        toast.error((error as Error).message);
      } finally {
        setIsLoading(false);
      }

      return;
    }

    if (validateCurrentStep()) {
      if (currentStep > 1) {
        setCurrentStep(prev => Math.min(prev + 1, 6));
      } else if (productId) {
        setCurrentStep(2);
      }
    }
  };

  const handlePrevious = () => {
    setCurrentStep(prev => Math.max(prev - 1, 1));
  };

  const validateCurrentStep = () => {
    switch (currentStep) {
      case 1:
        if (!productData.productName.trim()) {
          toast.error('Product name is required');
          return false;
        }
        if (productData.productName.length < 3) {
          toast.error('Product name must be at least 3 characters long');
          return false;
        }
        return true;
      case 2:
        if (productData.packages.length === 0) {
          toast.error('Please create at least one package');
          return false;
        }
        return true;
      default:
        return true;
    }
  };

  const updateProductData = (updates: Partial<INewProductData>) => {
    setProductData(prev => ({ ...prev, ...updates }));
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      disableOutsideClick={true}
      className="content-h-auto content-max-h-[95%] content-w-3/4 content-overflow-hidden"
      variant="user"
    >
      <ModalHeader onClose={handleClose}>
        <p className="content-py-5 content-text-lg content-font-medium content-text-white">
          {productId ? 'Edit ' : 'Create '} Product
        </p>
      </ModalHeader>
      <ModalBody className="content-h-full content-overflow-auto content-pb-16">
        <Card className="content-my-6">
          <div className="content-p-4">
            <div className="content-mb-4 content-grid content-grid-cols-3">
              {steps.map((step, index) => (
                <div
                  key={step.number}
                  className="content-flex content-items-center"
                >
                  <div
                    className={cn(
                      'content-flex content-items-center content-justify-center',
                    )}
                  >
                    {currentStep > step.number && (
                      <FaCheckCircle className="content-size-5 content-text-primary" />
                    )}
                  </div>
                  <div className="content-ml-3 content-text-left">
                    <p
                      className={cn(
                        'content-font-medium',
                        currentStep >= step.number
                          ? 'content-text-white'
                          : 'content-text-gray-400',
                      )}
                    >
                      {step.title}
                    </p>
                    <p
                      className={cn(
                        'content-text-sm',
                        currentStep >= step.number
                          ? 'content-text-cloudy-white'
                          : 'content-text-gray-400',
                      )}
                    >
                      {step.description}
                    </p>
                  </div>
                  {index < steps.length - 1 && (
                    <div
                      className={cn(
                        'content-mx-auto content-h-0.5 content-w-10',
                        currentStep > step.number
                          ? 'content-bg-primary'
                          : 'content-bg-gray-300',
                      )}
                    />
                  )}
                </div>
              ))}
            </div>
            <Progress value={(currentStep / 6) * 100} className="content-h-2" />
          </div>
        </Card>

        <Card>
          <div className="content-flex content-flex-col content-space-y-1.5 content-border-b content-border-card-border content-p-4">
            <div className="content-text-2xl content-font-semibold content-leading-none content-tracking-tight content-text-white">
              {`Step ${currentStep}: ${steps[currentStep - 1].title}`}
            </div>
            <div className="content-text-sm content-text-ash-gray">
              {steps[currentStep - 1].description}
            </div>
          </div>
          <div className="content-p-8 content-pt-0">
            {currentStep === 1 ? (
              <ProductNameStep
                productData={productData}
                updateProductData={updateProductData}
              />
            ) : currentStep === 2 ? (
              <CreatePackagesStep
                productData={productData}
                updateProductData={updateProductData}
              />
            ) : (
              <FinalValidationStep productData={productData} />
            )}
          </div>
        </Card>

        <div className="content-my-8 content-flex content-justify-between">
          <Button onClick={handlePrevious} disabled={currentStep === 1}>
            Previous
          </Button>

          <Button onClick={handleNext} disabled={isLoading}>
            {currentStep < 3 ? (
              'Next'
            ) : isLoading ? (
              <div className="content-flex content-items-center content-gap-2">
                <FaCheckCircle className="content-size-5" />
                Saving...
              </div>
            ) : (
              <>
                <FaCheckCircle className="content-mr-2 content-size-5" />
                Save Product & Packages
              </>
            )}
          </Button>
        </div>
      </ModalBody>
    </Modal>
  );
};

export default NewModal;
