import { safeWindowOpen, sanitizeHtml } from 'home-module/security';
import {
  Edit,
  Eye,
  Filter,
  Paperclip,
  Plus,
  Search,
  Trash,
} from 'lucide-react';
import { useEffect, useState } from 'react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { CustomCountrySelect } from 'components/CustomCountryStateSelect';
import SearchSelect from 'components/SearchSelect';
import ViewPolicy from 'features/policy/ViewPolicy';
import useDebounce from 'hooks/UseDebounce';
import { IPolicy } from 'models/Policy';
import { IGetListParams, IList, ModalType, Status } from 'models/Global';
import ActionPolicy from './ActionPolicy';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from 'hooks/UseAPI';
import TableLoader from 'components/skeleton/TableLoader';
import {
  formateDate,
  isSuccessResponse,
  objectToQueryString,
  sliceWords,
} from 'utils/Helper';
import ConfirmDialog from 'components/ConfirmDialog';
import { FILE_PATH_PREFIX, InitGetListParams } from 'utils/Constants';
import Pagination from 'common/Pagination';

const mockMSPs = ['All MSPs', 'TechCorp MSP', 'ComplianceCorp MSP'];
const mockClients = ['All Clients', 'Client A', 'Client B', 'Client C'];
const mockProducts = ['All Products', 'Product 1', 'Product 2', 'Product 3'];

const AspireAdminPolicyList = () => {
  const apiClient = useAPI();
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [countryFilter, setCountryFilter] = useState<string>('all');
  const [mspFilter, setMSPFilter] = useState<string>('All MSPs');
  const [clientFilter, setClientFilter] = useState<string>('All Clients');
  const [productFilter, setProductFilter] = useState<string>('All Products');
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);

  const [policies, setPolicies] = useState<IList<IPolicy>>({
    items: [],
    offset: 0,
    pageSize: 0,
    total: 0,
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedPolicy, setSelectedPolicy] = useState<IPolicy | null>(null);
  const [showPreview, setShowPreview] = useState<boolean>(false);
  const [isModalOpen, setIsModalOpen] = useState<ModalType>(ModalType.NONE);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    policyName: '',
    offset: 0,
    pageSize: 10,
  });

  const searchDebounce = useDebounce(queryString, 1000);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchPolicies();
    }
  }, [searchDebounce]);

  const fetchPolicies = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_POLICY_LIST + queryString,
      );
      setPolicies(response.data);
    } catch (error) {
      console.error('Error fetching policies:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewPolicy = (policy: IPolicy) => {
    setIsModalOpen(ModalType.VIEW);
    setSelectedPolicy(policy);
  };

  const handleRequestEditPolicy = (policy: IPolicy) => {
    setIsModalOpen(ModalType.EDIT);
    setSelectedPolicy(policy);
  };

  const handleDownloadFile = async (fileUrl: string) => {
    safeWindowOpen(FILE_PATH_PREFIX + fileUrl, '_blank');
  };

  const handleDeleteConfirm = async (policy: IPolicy) => {
    setSelectedPolicy(policy);
    setDeleteDialogOpen(true);
  };

  const handleDeletePolicy = async () => {
    setDeleteLoading(true);
    try {
      const response = await apiClient.del(
        API_END_POINTS.DELETE_POLICY.replace(':id', selectedPolicy?.id ?? ''),
      );
      if (isSuccessResponse(response.statusCode)) {
        fetchPolicies();
      }
    } catch (error) {
      console.error('Error deleting policy:', error);
    } finally {
      setDeleteLoading(false);
      setDeleteDialogOpen(false);
      setSelectedPolicy(null);
    }
  };

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      page: page,
    }));
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Policy Management
          </h1>
          <p className="text-muted-foreground">
            Create, manage, and assign policies across your organization
          </p>
        </div>
        <div className="flex gap-2">
          <Button onClick={() => setIsModalOpen(ModalType.ADD)}>
            <Plus className="mr-2 size-4" />
            Create New Policy
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Policy Library</CardTitle>
          <CardDescription>
            Manage all organizational policies and their assignments
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-6 space-y-4">
            <div className="flex gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
                <Input
                  placeholder="Search policies..."
                  value={queryParams.policyName ?? ''}
                  onChange={e =>
                    setQueryParams(prevState => ({
                      ...prevState,
                      policyName: e.target.value,
                    }))
                  }
                  className="pl-10"
                />
              </div>
              <Button variant="outline" className="flex items-center gap-2">
                <Filter className="size-4" />
                Advanced Filters
              </Button>
            </div>

            <div className="grid grid-cols-2 gap-4 md:grid-cols-5">
              <SearchSelect
                value={statusFilter}
                onValueChange={setStatusFilter}
                items={[
                  { value: 'all', label: 'All Statuses' },
                  { value: 'ACTIVE', label: 'Active' },
                  { value: 'INACTIVE', label: 'Inactive' },
                  { value: 'DRAFT', label: 'Draft' },
                ]}
                placeholder="Filter by Status"
              />

              <CustomCountrySelect
                value={countryFilter}
                handleChange={setCountryFilter}
                showAllCountryOption
              />

              <SearchSelect
                value={mspFilter}
                onValueChange={setMSPFilter}
                items={mockMSPs.map(msp => ({ value: msp, label: msp }))}
              />

              <SearchSelect
                value={clientFilter}
                onValueChange={setClientFilter}
                items={mockClients.map(client => ({
                  value: client,
                  label: client,
                }))}
              />

              <SearchSelect
                value={productFilter}
                onValueChange={setProductFilter}
                items={mockProducts.map(product => ({
                  value: product,
                  label: product,
                }))}
              />
            </div>
          </div>

          {loading ? (
            <TableLoader count={10} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Policy Name</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Effective Date</TableHead>
                  <TableHead>End Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Attachment</TableHead>
                  <TableHead className="text-center">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {policies.items.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={10} className="text-center">
                      No policies found.
                    </TableCell>
                  </TableRow>
                )}
                {policies.items.map((policy, index) => (
                  <TableRow key={index}>
                    <TableCell className="w-1/5">
                      {sliceWords(policy?.policyName ?? '', 10)}
                    </TableCell>
                    <TableCell className="w-1/4">
                      <div
                        dangerouslySetInnerHTML={{
                          __html: sanitizeHtml(
                            policy?.description
                              ? sliceWords(policy?.description ?? '', 10)
                              : '-',
                          ),
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">{policy?.policyTypeName}</Badge>
                    </TableCell>
                    <TableCell>{formateDate(policy.effectiveDate)}</TableCell>
                    <TableCell>{formateDate(policy?.policyEndDate)}</TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          policy.status === Status.ACTIVE
                            ? 'default'
                            : 'destructive'
                        }
                      >
                        {policy.status}
                      </Badge>
                    </TableCell>

                    <TableCell>
                      {policy?.files?.length > 0 ? (
                        <Button
                          variant="ghost"
                          size="sm"
                          className="text-blue-600"
                          onClick={() =>
                            handleDownloadFile(
                              policy?.files?.[0]?.fileUrl ?? '',
                            )
                          }
                        >
                          <Paperclip className="size-4" />
                          {policy?.files?.[0]?.fileType ?? ''}
                        </Button>
                      ) : (
                        'No attachment'
                      )}
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleViewPolicy(policy)}
                          title="View Details"
                        >
                          <Eye className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleRequestEditPolicy(policy)}
                          title="Request Edit"
                        >
                          <Edit className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDeleteConfirm(policy)}
                          title="Delete"
                        >
                          <Trash className="size-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <div className="mt-4">
            <Pagination
              total={policies.total}
              perPage={policies.pageSize}
              onPageChange={handlePageChange}
            />
          </div>
        </CardContent>
      </Card>

      {selectedPolicy && (
        <ViewPolicy
          open={showPreview}
          onClose={() => setShowPreview(false)}
          selectedPolicy={selectedPolicy}
        />
      )}
      {isModalOpen === ModalType.ADD && (
        <ActionPolicy
          isOpen={isModalOpen === ModalType.ADD}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={() => {
            fetchPolicies();
          }}
        />
      )}
      {isModalOpen === ModalType.EDIT && (
        <ActionPolicy
          isOpen={isModalOpen === ModalType.EDIT}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={() => {
            fetchPolicies();
          }}
          selectedPolicy={selectedPolicy as IPolicy}
        />
      )}
      {isModalOpen === ModalType.VIEW && (
        <ViewPolicy
          open={isModalOpen === ModalType.VIEW}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          selectedPolicy={selectedPolicy as IPolicy}
        />
      )}

      <ConfirmDialog
        isOpen={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleDeletePolicy}
        message={`Are you sure you want to delete "${selectedPolicy?.policyName}"?`}
        buttonText="Delete"
        loading={deleteLoading}
        loadingText="Deleting..."
      />
    </div>
  );
};

export default AspireAdminPolicyList;
