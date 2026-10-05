import { FiPackage } from 'react-icons/fi';

import { Badge } from 'common/Badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { INewProductData } from 'models/Product';

interface IProps {
  productData: INewProductData;
}

const FinalValidationStep = ({ productData }: IProps) => {
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'published':
        return '!content-bg-green-800 !content-text-white';
      case 'enabled':
        return '!content-bg-blue-800 !content-text-white';
      case 'disabled':
        return '!content-bg-red-800 !content-text-white';
      default:
        return '!content-bg-gray-800 !content-text-white';
    }
  };

  return (
    <div className="content-space-y-6">
      <div className="content-text-center">
        <h3 className="content-my-2 content-text-2xl content-font-bold content-text-white">
          Ready to Save!
        </h3>
        <p className="content-text-ash-gray">
          Please review your product and packages below. Click "Save Product &
          Packages" to finalize.
        </p>
      </div>

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
              <p className="content-text-lg content-text-ash-gray">
                {productData.productName}
              </p>
            </div>
            <div>
              <h4 className="content-mb-2 content-font-semibold content-text-white">
                Description
              </h4>
              <p className="content-text-ash-gray">
                {productData.productDescription || 'No description provided'}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <FiPackage className="content-size-5" />
            Packages Summary ({productData.packages.length})
          </CardTitle>
          <CardDescription>
            Here are all the packages that will be created for this product
          </CardDescription>
        </CardHeader>
        <CardContent className="!content-p-6">
          {productData.packages.length > 0 ? (
            <div className="content-space-y-4">
              {productData.packages.map((pkg, index) => (
                <Card key={index}>
                  <CardHeader className="content-pb-3">
                    <div className="content-flex content-items-center content-justify-between">
                      <CardTitle className="content-text-lg">
                        {pkg.packageName}
                      </CardTitle>
                      <div className="content-flex content-items-center content-gap-2">
                        <Badge className={getStatusColor(pkg.packageStatus)}>
                          {pkg.packageStatus}
                        </Badge>
                        <span className="content-text-lg content-font-bold content-text-green-600">
                          {pkg.packagePrice === 0
                            ? 'Free'
                            : `$${pkg.packagePrice}`}
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
                          {pkg.selectedFeatures.map((feature, featureIndex) => (
                            <Badge
                              key={featureIndex}
                              variant="outline"
                              className="content-text-xs content-text-white"
                            >
                              {feature.featureName}
                            </Badge>
                          ))}
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
  );
};

export default FinalValidationStep;
