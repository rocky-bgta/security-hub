import { Edit, Plus, Search, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
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
import { Input } from 'common/Input';
import ActionSupportTicketType from 'features/support-ticket-type/ActionSupportTicketType';
import DeleteSupportTicketType from 'features/support-ticket-type/DeleteSupportTicketType';
import { useAPI } from 'hooks/UseAPI';
import {
  IResponse,
  IList,
  IGetListParams,
  Status,
  ModalType,
} from 'models/Global';
import { ISupportTicketType } from 'models/Billing';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import useDebounce from 'hooks/UseDebounce';
import { InitGetListParams } from 'utils/Constants';

const SupportTicketType = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<IList<ISupportTicketType>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [selectedSupportTicketType, setSelectedSupportTicketType] =
    useState<ISupportTicketType | null>(null);
  const [actionType, setActionType] = useState<ModalType>(ModalType.NONE);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    active: '',
  });

  const apiClient = useAPI();
  const searchDebounce = useDebounce(queryString, 1000);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchData();
    }
  }, [searchDebounce]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_SUPPORT_TICKET_TYPE_LIST + queryString,
      );
      if (isSuccessResponse(response?.statusCode)) {
        setData(response.data);
      }
    } catch (error) {
      console.error('Error fetching support ticket type data:', error);
      toast.error('Failed to fetch support ticket types');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitSupportTicketType = (
    supportTicketType: ISupportTicketType,
  ) => {
    setData(prevData => {
      if (actionType === ModalType.ADD) {
        return {
          ...prevData,
          items: [...prevData.items, supportTicketType],
          total: prevData.total + 1,
        };
      } else {
        return {
          ...prevData,
          items: prevData.items.map(r =>
            r.id === supportTicketType.id ? supportTicketType : r,
          ),
        };
      }
    });

    setSelectedSupportTicketType(null);
    setActionType(ModalType.NONE);
  };

  const handleDeleteSupportTicketType = () => {
    toast.success(
      `Support ticket type ${selectedSupportTicketType?.name} has been successfully deleted.`,
    );
    setData(prevData => ({
      ...prevData,
      items: prevData.items.filter(r => r.id !== selectedSupportTicketType?.id),
      total: prevData.total - 1,
    }));
    setSelectedSupportTicketType(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          Support Ticket Type Management
        </h1>
        <Button
          onClick={() => setActionType(ModalType.ADD)}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Support Ticket Type
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Support Ticket Types List
          </CardTitle>
          <CardDescription>
            View and manage all support ticket types
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex flex-col gap-4 sm:flex-row">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder="Search support ticket types..."
                value={queryParams.search}
                onChange={e =>
                  setQueryParams({ ...queryParams, search: e.target.value })
                }
                className="pl-10"
              />
            </div>
            <Select
              value={queryParams.active}
              onValueChange={value =>
                setQueryParams({
                  ...queryParams,
                  active: value === 'all' ? '' : value,
                })
              }
            >
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="true">Active</SelectItem>
                <SelectItem value="false">Inactive</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Serial Number
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Description
                </TableHead>
                <TableHead className="text-center font-semibold text-foreground">
                  Status
                </TableHead>
                <TableHead className="text-center font-semibold text-foreground">
                  Actions
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell
                    colSpan={5}
                    className="text-center text-muted-foreground"
                  >
                    Loading...
                  </TableCell>
                </TableRow>
              ) : data?.total === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="text-center">
                    No support ticket types found
                  </TableCell>
                </TableRow>
              ) : (
                data?.items?.map((supportTicketType, index) => (
                  <TableRow key={supportTicketType?.id}>
                    <TableCell className="font-medium text-foreground">
                      {index + 1}
                    </TableCell>
                    <TableCell className="font-medium text-foreground">
                      {supportTicketType?.name}
                    </TableCell>
                    <TableCell className="text-foreground">
                      {supportTicketType?.description || 'N/A'}
                    </TableCell>
                    <TableCell className="text-center">
                      <span
                        className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
                          supportTicketType?.active
                            ? 'bg-green-100 text-green-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {supportTicketType?.active ? 'Active' : 'Inactive'}
                      </span>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedSupportTicketType(supportTicketType);
                            setActionType(ModalType.EDIT);
                          }}
                          className="hover:bg-muted"
                        >
                          <Edit className="size-4" />
                        </Button>
                        {/* <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedSupportTicketType(supportTicketType);
                            setShowDeleteDialog(true);
                          }}
                          className="hover:bg-destructive/20 hover:text-destructive"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button> */}
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
      {actionType !== ModalType.NONE && (
        <ActionSupportTicketType
          isOpen={actionType === ModalType.ADD || actionType === ModalType.EDIT}
          onClose={() => {
            setActionType(ModalType.NONE);
            setSelectedSupportTicketType(null);
          }}
          supportTicketType={selectedSupportTicketType}
          onSubmit={handleSubmitSupportTicketType}
        />
      )}
      {selectedSupportTicketType && (
        <DeleteSupportTicketType
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          supportTicketType={selectedSupportTicketType}
          onDeleteSupportTicketType={handleDeleteSupportTicketType}
        />
      )}
    </div>
  );
};

export default SupportTicketType;
