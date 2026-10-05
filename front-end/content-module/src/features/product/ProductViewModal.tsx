import { useEffect, useState } from 'react';
import { FiPackage } from 'react-icons/fi';

import { Badge } from 'common/Badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IPackage } from 'models/Package';
import { IProduct } from 'models/Product';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  productId: string;
  isOpen: boolean;
  onClose: () => void;
}

const ProductViewModal = ({ productId, isOpen, onClose }: IProps) => {
  const [product, setProduct] = useState<IProduct>();

  const apiClient = useAPI();

  useEffect(() => {
    if (isOpen) {
      fetchProductDetails();
    }
  }, [isOpen]);

  const fetchProductDetails = async () => {
    try {
      const response: IResponse<IProduct> = await apiClient.get(
        API_END_POINTS.PRODUCT_DETAILS + productId,
      );
      setProduct(response.data);
    } catch (error) {
      console.error('Error fetching product data:', error);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ENABLED':
        return '!content-bg-success !content-text-white';
      case 'DISABLED':
        return '!content-bg-vibrant-red !content-text-white';
      default:
        return '!content-bg-gray-800 !content-text-white';
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      disableOutsideClick={true}
      className="content-h-auto content-w-3/4"
    >
      <ModalHeader onClose={onClose}>
        <p className="content-py-5 content-text-lg content-font-medium">
          View Product
        </p>
      </ModalHeader>
      <ModalBody className="content-my-6">
        {product && (
          <div className="content-space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="content-flex content-items-center content-gap-2">
                  <FiPackage className="content-size-5" />
                  Product Summary
                </CardTitle>
              </CardHeader>
              <CardContent className="!content-p-6">
                <div className="content-grid content-grid-cols-1 content-gap-6 md:content-grid-cols-2">
                  <div>
                    <h4 className="content-mb-2 content-font-semibold content-text-white">
                      Product Name
                    </h4>
                    <p className="content-text-lg content-text-cloudy-white">
                      {product.productName}
                    </p>
                  </div>
                  <div>
                    <h4 className="content-mb-2 content-font-semibold content-text-white">
                      Description
                    </h4>
                    <p className="content-text-cloudy-white">
                      {product.productDescription || 'No description provided'}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="content-flex content-items-center content-gap-2">
                  <FiPackage className="content-size-5" />
                  Packages Summary ({product.packages?.length})
                </CardTitle>
                <CardDescription></CardDescription>Here are all the packages
                that will be created for this product
              </CardHeader>
              <CardContent className="!content-p-6">
                {product.packages?.length > 0 ? (
                  <div className="content-space-y-4">
                    {product.packages?.map((pkg: IPackage, index: number) => (
                      <Card key={index}>
                        <CardHeader className="content-pb-3">
                          <div className="content-flex content-items-center content-justify-between">
                            <CardTitle className="content-text-lg">
                              {pkg.packageName}
                            </CardTitle>
                            <div className="content-flex content-items-center content-gap-2">
                              <Badge
                                className={getStatusColor(pkg.packageStatus)}
                              >
                                {pkg.packageStatus}
                              </Badge>
                              <span className="content-text-lg content-font-bold content-text-green-600">
                                {pkg.price === 0 ? 'Free' : `$${pkg.price}`}
                              </span>
                            </div>
                          </div>
                        </CardHeader>
                        <CardContent>
                          <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-2">
                            <div>
                              <h5 className="content-mb-2 content-font-medium content-text-white">
                                Features
                              </h5>
                              <div className="content-flex content-flex-wrap content-gap-1">
                                {pkg.features?.map(
                                  (feature: any, featureIndex: number) => (
                                    <Badge
                                      key={featureIndex}
                                      variant="outline"
                                      className="content-text-xs content-text-white"
                                    >
                                      {feature.name}
                                    </Badge>
                                  ),
                                )}
                              </div>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                ) : (
                  <p className="content-py-8 content-text-center content-text-gray-500">
                    No packages created yet
                  </p>
                )}
              </CardContent>
            </Card>
          </div>
        )}
      </ModalBody>
    </Modal>
  );
};

export default ProductViewModal;
