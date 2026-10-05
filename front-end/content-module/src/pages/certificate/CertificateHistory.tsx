import {
  Calendar,
  Check,
  Download,
  Eye,
  History,
  QrCode,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Pagination from 'common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import Border from 'components/UserBorder';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import CertificateViewModal from 'features/certificate/CertificateViewModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { ICertificateHistory, IUserCertificate } from 'models/Certificate';
import { IGetListParams, IList, IResponse, CourseStatus } from 'models/Global';
import { IAssignedLicense } from 'models/License';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX, InitGetListParams } from 'utils/Constants';
import {
  formatDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

const getUniqueProducts = (items: IAssignedLicense[]) =>
  items.filter(
    (product, index, self) =>
      product.product &&
      index === self.findIndex(p => p.productId === product.productId),
  );

const CertificateHistory = () => {
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const [isValidatorOpen, setIsValidatorOpen] = useState<boolean>(false);
  const [isCertificateViewOpen, setIsCertificateViewOpen] =
    useState<boolean>(false);
  const [certificateData, setCertificateData] = useState<IUserCertificate>();
  const [validationCode, setValidationCode] = useState<string>('');
  const [certificates, setCertificates] = useState<IList<ICertificateHistory>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    certificateName: '',
    productId: '',
    status: CourseStatus.ALL,
    isClientAdmin: true,
  });
  const [products, setProducts] = useState<IList<IAssignedLicense>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [productFilter, setProductFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const searchDebounce = useDebounce(queryString, 500);

  const uniqueProducts = getUniqueProducts(products?.items || []);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchCertificates();
    }
  }, [searchDebounce]);

  const fetchProducts = async () => {
    if (!userInfo?.userId) return;
    try {
      const response: IResponse<IList<IAssignedLicense>> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
          ':clientAdminId',
          userInfo.userId,
        ) + 'offset=0&pageSize=100',
      );
      setProducts(response.data);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [userInfo?.userId]);

  useEffect(() => {
    const unique = getUniqueProducts(products?.items || []);
    if (unique.length === 0) {
      setProductFilter('');
      return;
    }
    if (unique.length === 1) {
      const productId = unique[0].productId;
      setProductFilter(productId);
      setQueryParams(prev => ({ ...prev, productId }));
      return;
    }
    setProductFilter(prev =>
      prev && unique.some(p => p.productId === prev) ? prev : 'all',
    );
  }, [products?.items]);

  const fetchCertificates = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.CERTIFICATE_HISTORY_LIST + queryString,
      );
      if (isSuccessResponse(response.statusCode)) {
        setCertificates({
          ...InitGetListParams,
          total: response.data.total,
          items: response.data.items,
        });
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error fetching certificates', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const normalized = status.toLowerCase().replace(/-/g, '_');
    switch (normalized) {
      case 'valid':
        return (
          <Badge variant="default" className="content-bg-primary">
            Valid
          </Badge>
        );
      case 'expiring_soon':
        return <Badge variant="destructive">Expiring Soon</Badge>;
      case 'expired':
        return <Badge variant="secondary">Expired</Badge>;
      case 'revoked':
        return <Badge variant="destructive">Revoked</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  const handleDownloadCertificate = (certificateUrl: string) => {
    const link = document.createElement('a');
    link.href = certificateUrl;
    link.setAttribute('download', 'Certificate.pdf');
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleValidateCertificate = () => {
    if (!validationCode.trim()) {
      toast.error('Please enter a verification code or scan QR code.');
      return;
    }

    setValidationCode('');
    setIsValidatorOpen(false);
  };

  const handleViewCertificate = (certificate: ICertificateHistory) => {
    setCertificateData({
      certificateLink: certificate.certificateUrl,
      productName: certificate.productName,
      imageCertificateLink: certificate.certificateImageUrl,
    } as IUserCertificate);
    setIsCertificateViewOpen(true);
  };

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    const defaultProductId =
      uniqueProducts.length === 1 ? uniqueProducts[0].productId : '';
    const defaultProductFilter =
      uniqueProducts.length === 1 ? uniqueProducts[0].productId : 'all';
    setProductFilter(
      uniqueProducts.length === 0 ? '' : defaultProductFilter,
    );
    setStatusFilter('ALL');
    setQueryParams({
      ...InitGetListParams,
      certificateName: '',
      productId: defaultProductId,
      status: CourseStatus.ALL,
      isClientAdmin: true,
    });
  };

  return (
    <div className="content-space-y-6 content-text-white">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
            Certificate Management
          </h1>
          <p className="content-text-muted-foreground">
            Manage certificates, templates, and validation
          </p>
        </div>
        {/* <Button
          variant="outline"
          onClick={() => setIsValidatorOpen(true)}
          className="content-border-transparent content-bg-primary"
        >
          <QrCode className="content-mr-2 content-h-4 content-w-4" />
          Validate Certificate
        </Button> */}
      </div>

      <Border>
        <CardHeader>
          <div className="content-flex content-flex-col content-gap-4">
            <div className="content-flex content-flex-col content-gap-2">
              <CardTitle className="content-flex content-items-center content-gap-2">
                <History className="content-size-5" />
                Certificate History ({certificates.total})
              </CardTitle>
              <CardDescription>
                View and manage all issued certificates
              </CardDescription>
            </div>
            <div className="content-grid content-grid-cols-9 content-items-end content-gap-2">
              <div className="content-col-span-4 content-flex content-flex-col content-gap-2">
                <Label htmlFor="search">Search</Label>
                <Input
                  id="search"
                  placeholder="Search certificates..."
                  value={queryParams.certificateName}
                  onChange={e =>
                    setQueryParams(prev => ({
                      ...prev,
                      certificateName: e.target.value,
                      offset: 0,
                    }))
                  }
                />
              </div>
              <div className="content-col-span-2 content-flex content-flex-col content-gap-2">
                <Label htmlFor="product">Product</Label>
                <Select
                  value={productFilter || undefined}
                  onValueChange={value => {
                    setProductFilter(value);
                    setQueryParams(prev => ({
                      ...prev,
                      productId: value === 'all' ? '' : value,
                      offset: 0,
                    }));
                  }}
                  disabled={uniqueProducts.length === 0}
                >
                  <SelectTrigger id="product" className="content-w-full">
                    <SelectValue
                      placeholder={
                        uniqueProducts.length === 0
                          ? 'No products available'
                          : 'Select Product'
                      }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {uniqueProducts.length > 1 && (
                      <SelectItem value="all">All Products</SelectItem>
                    )}
                    {uniqueProducts.map(product => (
                      <SelectItem
                        key={product.productId}
                        value={product.productId}
                      >
                        {product.product?.productName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="content-col-span-2 content-flex content-flex-col content-gap-2">
                <Label htmlFor="status">Status</Label>
                <Select
                  value={statusFilter}
                  onValueChange={value => {
                    setStatusFilter(value);
                    setQueryParams(prev => ({
                      ...prev,
                      status:
                        value === 'ALL'
                          ? CourseStatus.ALL
                          : (value as CourseStatus),
                      offset: 0,
                    }));
                  }}
                >
                  <SelectTrigger id="status" className="content-w-full">
                    <SelectValue placeholder="Filter by Status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ALL">All Status</SelectItem>
                    <SelectItem value="VALID">Valid</SelectItem>
                    <SelectItem value="EXPIRING_SOON">Expiring Soon</SelectItem>
                    <SelectItem value="EXPIRED">Expired</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Button variant="outline" className="content-col-span-1" onClick={handleReset}>
                Reset
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <AssignedPackagesLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Certificate ID</TableHead>
                  <TableHead>User</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Issue Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Expiry Date</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {certificates.items.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} className="content-text-center">
                      No certificates found
                    </TableCell>
                  </TableRow>
                )}

                {certificates.items.map(cert => (
                  <TableRow key={cert.certificateId}>
                    <TableCell className="content-font-medium">
                      {cert.certificateId}
                    </TableCell>
                    <TableCell>
                      <div>
                        <div className="content-font-medium">
                          {cert.fullName}
                        </div>
                        {/* <div className="content-text-sm content-text-muted-foreground">
                        {cert.userId}
                      </div> */}
                      </div>
                    </TableCell>
                    <TableCell>{cert.email}</TableCell>
                    <TableCell>{cert.productName}</TableCell>
                    <TableCell>
                      <div className="content-flex content-items-center content-gap-1">
                        <Calendar className="content-size-3" />
                        {formatDateAndTime(cert.createdAt)}
                      </div>
                    </TableCell>
                    <TableCell>{getStatusBadge(cert.status)}</TableCell>
                    <TableCell>
                      <div className="content-flex content-items-center content-gap-1">
                        <Calendar className="content-size-3" />
                        {formatDateAndTime(cert.expiryDate)}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="content-flex content-gap-1">
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => handleViewCertificate(cert)}
                          className="content-bg-transparent"
                          title="View Certificate"
                        >
                          <Eye className="content-size-3" />
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() =>
                            handleDownloadCertificate(
                              FILE_PATH_PREFIX + cert.certificateUrl,
                            )
                          }
                          className="content-bg-transparent"
                          title="Download Certificate"
                        >
                          <Download className="content-size-3" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <div className="content-mt-4">
            <Pagination
              total={certificates.total}
              perPage={queryParams.pageSize || 10}
              onPageChange={handlePageChange}
            />
          </div>
        </CardContent>
      </Border>

      <Dialog open={isValidatorOpen} onOpenChange={setIsValidatorOpen}>
        <DialogContent className="content-max-w-md content-bg-gradient-to-b content-from-[#324650] content-to-[#12151E] content-text-white">
          <DialogHeader>
            <DialogTitle className="content-text-white">
              Validate Certificate
            </DialogTitle>
            <DialogDescription>
              Enter verification code or scan QR code to validate a certificate
            </DialogDescription>
          </DialogHeader>
          <div className="content-space-y-4">
            <div className="content-flex content-flex-col content-gap-1">
              <Label htmlFor="verification-code" className="content-text-white">
                Verification Code
              </Label>
              <Input
                id="verification-code"
                placeholder="Enter verification code (e.g., ABC123XYZ)"
                value={validationCode}
                onChange={e => setValidationCode(e.target.value)}
                className="content-bg-transparent"
              />
            </div>
            <div className="content-text-center content-text-muted-foreground">
              <QrCode className="content-mx-auto content-mb-2 content-size-12" />
              <p className="content-text-sm">
                Or scan QR code from certificate
              </p>
            </div>
            <div className="content-flex content-gap-2">
              <Button
                variant="outline"
                className="content-flex-1 content-bg-transparent"
                onClick={() => setIsValidatorOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleValidateCertificate}
                className="content-flex-1 content-border-transparent content-bg-primary"
              >
                <Check className="content-mr-2 content-size-4" />
                Validate
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
      {certificateData && (
        <CertificateViewModal
          isOpen={isCertificateViewOpen}
          onClose={() => setIsCertificateViewOpen(false)}
          data={certificateData as unknown as IUserCertificate}
        />
      )}
    </div>
  );
};

export default CertificateHistory;
