import { Package } from 'lucide-react';

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import Border from 'components/UserBorder';
import AvailablePackagesLoader from 'components/skeleton/AvailablePackages';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { IGetListParams, IResponse } from 'models/Global';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString, truncateText } from 'utils/Helper';

interface PackageInfo {
  packageId: string;
  packageName: string;
  packageDescription: string;
  productName: string;
  featureList: Array<string>;
}

interface PackageResponse {
  total: number;
  packages: PackageInfo[];
}

const AspireAdminAvailablePackages = () => {
  const { userInfo } = useStore();
  const [loading, setLoading] = useState<boolean>(true);
  const [availablePackages, setAvailablePackages] = useState<PackageResponse>({
    total: 0,
    packages: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    clientAdminId: userInfo.userId,
  });
  const searchDebounce = useDebounce(queryString, 500);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchPackageData();
    }
  }, [searchDebounce]);

  const fetchPackageData = async () => {
    setLoading(true);

    try {
      const response: IResponse<PackageResponse> = await apiClient.get(
        API_END_POINTS.AVAILABLE_PACKAGE_LIST + queryString,
      );
      setAvailablePackages(response.data);
    } catch (error) {
      console.error('Error fetching available packages:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="content-space-y-6 content-text-white">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
            Package Management
          </h1>
          <p className="content-text-muted-foreground">
            Manage training packages and track performance
          </p>
        </div>
      </div>

      <Border>
        <CardHeader>
          <CardTitle className="content-mb-2 content-flex content-items-center content-gap-2">
            <Package className="content-size-5" />
            Available Packages ({availablePackages?.total})
          </CardTitle>
          <CardDescription>
            Packages available for assignment to users
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="content-grid content-gap-4 md:content-grid-cols-2 lg:content-grid-cols-3">
            {loading ? (
              <AvailablePackagesLoader />
            ) : (
              availablePackages?.packages.map(
                (pkg: PackageInfo, index: number) => (
                  <Card key={index}>
                    <CardHeader>
                      <CardTitle className="content-text-lg">
                        {pkg.packageName}
                      </CardTitle>
                      <CardDescription>
                        {truncateText(pkg.packageDescription || '', 20)}
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="content-mb-4 content-space-y-2 content-text-sm content-text-muted-foreground">
                        {/* <div>Topics: {pkg.packageId}</div>
                      <div>Duration: {pkg.duration}</div>
                      <div>Category: {pkg.category}</div> */}
                        <div>Product Name: {pkg.productName}</div>
                        <div>
                          Features:
                          <ul className="content-list-disc content-pl-5">
                            {pkg.featureList.map((feature, index) => (
                              <li key={index} className="content-my-1">
                                {feature}
                              </li>
                            ))}
                          </ul>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ),
              )
            )}
          </div>
        </CardContent>
      </Border>
    </div>
  );
};

export default AspireAdminAvailablePackages;
