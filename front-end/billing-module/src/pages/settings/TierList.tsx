import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
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
import DeleteConfirmationModal from 'components/DeleteConfirmationModal';
import TableLoader from 'components/skeleton/TableLoader';
import TierModal from 'features/tier/TierModal';
import ViewTierModal from 'features/tier/ViewTierModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  Award,
  Edit,
  Eye,
  Layers,
  Search,
  TrendingUp,
  Trash,
  Users,
} from 'lucide-react';
import { IGetListParams, IList, ModalType } from 'models/Global';
import { ITier, ITierSummary } from 'models/Tier';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  formatDate,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

const TierList = () => {
  const [data, setData] = useState<IList<ITier>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    pageSize: 10,
    search: '',
  });
  const [statusFilter, setStatusFilter] = useState<string>('true');
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [isShowModal, setIsShowModal] = useState<ModalType>(ModalType.NONE);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [selectedTier, setSelectedTier] = useState<ITier | null>(null);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    const statusParam = statusFilter ? `&status=${statusFilter}` : '';
    setQueryString(resp + statusParam);
  }, [queryParams, statusFilter]);

  useEffect(() => {
    if (searchDebounce) {
      fetchTierList();
    }
  }, [searchDebounce]);

  const fetchTierList = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.TIER_LIST + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching tier list:', error);
      toast.error('Failed to fetch tier list');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (active: boolean) => {
    if (active) {
      return <Badge className="bg-green-500 hover:bg-green-600">Active</Badge>;
    }
    return <Badge variant="secondary">Inactive</Badge>;
  };

  const handleEditTier = (tier: ITier) => {
    setSelectedTier(tier);
    setIsShowModal(ModalType.EDIT);
  };

  const handleViewTier = (tier: ITier) => {
    setSelectedTier(tier);
    setIsViewModalOpen(true);
  };

  const handleDeleteTier = async () => {
    setDeleteLoading(true);

    try {
      await apiClient.del(API_END_POINTS.TIER_DELETE + selectedTier?.id);
      toast.success('Tier deleted successfully');
      setIsDeleteModalOpen(false);
      fetchTierList();
    } catch (error) {
      console.error('Error deleting tier:', error);
      toast.error('Failed to delete tier');
    } finally {
      setDeleteLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleCloseModal = () => {
    setIsShowModal(ModalType.NONE);
    setSelectedTier(null);
  };

  const handleResetFilters = () => {
    setQueryParams({
      ...InitGetListParams,
      search: '',
    });
    setStatusFilter('true');
  };

  return (
    <div className="space-y-6">
      {/* Header */}

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Tier Management</h1>
          <p className="text-muted-foreground">
            Manage commission tiers and sales thresholds
          </p>
        </div>
        <Button onClick={() => setIsShowModal(ModalType.ADD)}>
          <Award className="mr-2 size-4" />
          Add Tier
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
            <div className="relative col-span-2">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder="Search tiers..."
                value={queryParams.search}
                onChange={e =>
                  setQueryParams({
                    ...queryParams,
                    search: e.target.value,
                  })
                }
                className="pl-10"
              />
            </div>
            <div>
              <Select
                value={statusFilter}
                onValueChange={value =>
                  setStatusFilter(value === 'all' ? '' : value)
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All</SelectItem>
                  <SelectItem value="true">Active</SelectItem>
                  <SelectItem value="false">Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Button variant="outline" onClick={() => handleResetFilters()}>
              Reset
            </Button>
          </div>
        </CardContent>
      </Card>
      {/* Table */}
      {loading ? (
        <TableLoader />
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Tier Name</TableHead>
              <TableHead>Description</TableHead>
              <TableHead className="text-center">Commission %</TableHead>
              <TableHead className="text-center">Sales Threshold</TableHead>
              <TableHead>Created At</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-center">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {data?.items?.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} className="text-center">
                  No tiers found
                </TableCell>
              </TableRow>
            )}
            {data?.items?.map(tier => (
              <TableRow key={tier.id}>
                <TableCell>
                  <div className="font-medium">{tier.tierName}</div>
                </TableCell>
                <TableCell>
                  <div className="max-w-xs truncate text-sm text-muted-foreground">
                    {tier.tierDescription || 'N/A'}
                  </div>
                </TableCell>
                <TableCell className="text-center">
                  <Badge variant="outline">{tier.commissionPercentage}%</Badge>
                </TableCell>
                <TableCell className="text-center">
                  ${tier.salesThreshold.toLocaleString()}
                </TableCell>
                <TableCell>{formatDate(tier.createdAt)}</TableCell>
                <TableCell>{getStatusBadge(tier.active)}</TableCell>
                <TableCell className="text-center">
                  <div className="flex items-center justify-center gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleViewTier(tier)}
                    >
                      <Eye className="size-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleEditTier(tier)}
                    >
                      <Edit className="size-4" />
                    </Button>
                    {/* <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => {
                        setIsDeleteModalOpen(true);
                        setSelectedTier(tier);
                      }}
                    >
                      <Trash className="size-4" />
                    </Button> */}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      <div className="flex justify-end py-7">
        <Pagination
          total={data?.total || 0}
          perPage={data?.pageSize}
          onPageChange={onPageChangeHandler}
        />
      </div>

      {/* Modals */}

      {isShowModal === ModalType.ADD && (
        <TierModal
          isOpen={isShowModal === ModalType.ADD}
          onClose={handleCloseModal}
          onSave={() => {
            fetchTierList();
          }}
        />
      )}

      {isShowModal === ModalType.EDIT && (
        <TierModal
          isOpen={isShowModal === ModalType.EDIT}
          onClose={handleCloseModal}
          onSave={() => {
            fetchTierList();
          }}
          tier={selectedTier as ITier | null}
        />
      )}

      <DeleteConfirmationModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteTier}
        title="Delete Tier"
        loading={deleteLoading}
      />

      <ViewTierModal
        isOpen={isViewModalOpen}
        onClose={() => {
          setIsViewModalOpen(false);
          setSelectedTier(null);
        }}
        tier={selectedTier}
      />
    </div>
  );
};

export default TierList;
