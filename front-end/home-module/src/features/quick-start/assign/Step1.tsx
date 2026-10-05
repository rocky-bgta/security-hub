import Pagination from 'common/Pagination';
import { Badge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
import { Input } from 'components/common/Input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import useAPI from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import useStore from 'hooks/UseStore';
import { Eye, Search } from 'lucide-react';
import { IResponse } from 'models/Context';
import { CourseStatus, IGetListParams, IList } from 'models/Global';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';
import AssignedPackagesLoader from './AssignedPackagesLoader';
import ViewSubPackage from './View';
import { RadioGroup, RadioGroupItem } from 'common/Radio';

enum modalTypes {
  AssignSubPackage = 'assign_sub_package',
  ViewSubPackage = 'view_sub_package',
  EditSubPackage = 'edit_sub_package',
  None = 'none',
}

export interface IAssignedLicense {
  id: string;
  clientAdminId: string | null;
  productId: string;
  packageId: string;
  product: IProduct;
  packageDetails: {
    id: string;
    packageName: string;
    packageStatus: string;
  };
  licenseCount: number;
  usedLicenseCount: number;
  assignedAt: string;
  expiryDate: string | null;
  topicCount: number;
}

interface IProduct {
  productId: string;
  productName: string;
  productDescription: string;
  productStatus: string | null;
  thumbnailUrl: string | null;
  createdAt: string;
  updatedAt: string;
  lastModifiedBy: string;
  packages: IPackage[];
  tags?: string[];
}

interface IPackage {
  id: string;
  packageName: string;
  packageDescription: string;
  price: number;
  features: { id: string; name: string }[];
  packages: any[];
  thumbnailUrl: string;
  createdAt: string;
  updatedAt: string;
  packageStatus: CourseStatus;
  courseIds: any[];
  bundlesIds: any[];
}

interface IProps {
  data?: any;
  onUpdate: (data: string[]) => void;
  onNext: () => void;
}

const Step1 = ({ data, onUpdate, onNext }: IProps) => {
  const { userInfo } = useStore();
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpenModal, setIsOpenModal] = useState<modalTypes>(modalTypes.None);
  const [selectedSubPackage, setSelectedSubPackage] = useState<any>();
  const [subPackages, setSubPackages] = useState<IList<any>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    status: CourseStatus.ALL,
    productId: '',
    clientAdminId: userInfo?.userId,
  });
  const [products, setProducts] = useState<IList<IAssignedLicense>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
  });
  const [loading, setLoading] = useState(true);
  const searchDebounce = useDebounce(queryString, 1000);
  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchSubPackages();
    }
  }, [searchDebounce]);

  const fetchSubPackages = async () => {
    try {
      setLoading(true);
      const response: IResponse<IList<any>> = await apiClient.get(
        API_END_POINTS.SUB_PACKAGE_LIST + queryString,
      );
      setSubPackages(response.data);
      if (data) {
        const selected = response.data.items.find(
          (pkg: any) => pkg.id === data.id,
        );
        setSelectedSubPackage(selected);
      } else if (response.data.items?.length === 1) {
        setSelectedSubPackage(response.data.items[0]);
      }
    } catch (error) {
      console.log(error);
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    if (!userInfo?.userId) return;
    const response: IResponse<IList<IAssignedLicense>> = await apiClient.get(
      API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
        ':clientAdminId',
        userInfo?.userId,
      ) + 'offset=0&pageSize=100',
    );
    setProducts(response.data);
  };

  useEffect(() => {
    fetchProducts();
  }, [userInfo]);

  const handleView = (pkg: any) => {
    setIsOpenModal(modalTypes.ViewSubPackage);
    setSelectedSubPackage(pkg);
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleSave = () => {
    onUpdate(selectedSubPackage);
    onNext();
  };

  return (
    <div className="home-space-y-6">
      {loading ? (
        <AssignedPackagesLoader count={5} />
      ) : (
        <div className="home-overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead></TableHead>
                <TableHead>Sub-Package Name</TableHead>
                <TableHead>Product</TableHead>
                <TableHead className="home-text-center">Topics</TableHead>
                <TableHead className="home-text-center">
                  Assigned Users
                </TableHead>
                <TableHead className="home-text-center">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {subPackages?.items?.length === 0 && (
                <TableRow>
                  <TableCell colSpan={10} className="home-text-center">
                    No sub-packages found
                  </TableCell>
                </TableRow>
              )}
              {subPackages?.items?.length > 0 &&
                subPackages?.items?.map(pkg => (
                  <TableRow
                    key={pkg.id}
                  // onClick={_ => setSelectedSubPackage(pkg)}
                  // className={cn(
                  //   'home-cursor-pointer',
                  //   selectedSubPackage?.id === pkg.id
                  //     ? 'home-bg-blue-600'
                  //     : '',
                  // )}
                  >
                    <TableCell>
                      <RadioGroup
                        value={selectedSubPackage?.id}
                        onValueChange={_ => setSelectedSubPackage(pkg)}
                      >
                        <RadioGroupItem
                          value={pkg.id}
                          id={pkg.id}
                        ></RadioGroupItem>
                      </RadioGroup>
                    </TableCell>
                    <TableCell>{pkg.name}</TableCell>
                    <TableCell className="home-text-gray-700 dark:home-text-gray-300">
                      {pkg.productName}
                    </TableCell>
                    <TableCell className="home-text-center">
                      <Badge variant="secondary">
                        {pkg.topicId.length} topics
                      </Badge>
                    </TableCell>
                    <TableCell className="home-text-center">
                      <Badge variant="secondary">{pkg.assignedUserCount}</Badge>
                    </TableCell>
                    <TableCell className="home-text-right">
                      <div className="home-flex home-items-center home-justify-end home-gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleView(pkg)}
                          title="View Details"
                        >
                          <Eye className="home-size-3" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>
        </div>
      )}
      {subPackages.total > 0 && (
        <div className="home-flex home-justify-end home-pt-4">
          <Pagination
            total={subPackages.total}
            perPage={subPackages.pageSize}
            onPageChange={onPageChangeHandler}
          />
        </div>
      )}

      <div className="home-flex home-justify-end home-pt-6">
        <Button onClick={handleSave} disabled={!selectedSubPackage}>
          Next Step
        </Button>
      </div>

      {isOpenModal === modalTypes.ViewSubPackage && (
        <ViewSubPackage
          selectedSubPackage={selectedSubPackage}
          isOpen={isOpenModal === modalTypes.ViewSubPackage}
          onClose={() => setIsOpenModal(modalTypes.None)}
        />
      )}
    </div>
  );
};

export default Step1;
