import {
  Card,
  CardContent,
} from 'components/common/Card';
import { Button } from 'components/common/Button';
import { Badge } from 'components/common/Badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import { Eye } from 'lucide-react';
import { getPriorityColor, getStatusColor } from './SupportTickets';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import { useEffect, useState } from 'react';
import {
  ISupportTicketList,
  SupportTicketAssignCategory,
  SupportTicketStatus,
} from 'models/SupportTicket';
import useDebounce from 'hooks/UseDebounce';
import { InitGetListParams } from 'utils/Constants';
import { IGetListParams, IList, ModalType } from 'models/Global';
import { useAPI } from 'hooks/UseAPI';
import TableLoader from 'components/skeleton/TableLoader';
import Pagination from 'common/Pagination';
import ClientUserViewModal from 'features/support-ticket/client-user/ViewModal';
import { IDropdownOption } from 'models/DropdownData';
import SupportTicketFilters from 'features/support-ticket/SupportTicketFilters';

const AspireAdminResolvedTicket = () => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(true);
  const [tickets, setTickets] = useState<IList<ISupportTicketList>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
  });
  const [isModalOpen, setIsModalOpen] = useState<ModalType>(ModalType.NONE);
  const [selectedTicketId, setSelectedTicketId] = useState<string | null>(null);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    priority: '',
    status: SupportTicketStatus.CLOSED,
    assignCategory: SupportTicketAssignCategory.ASSIGN_TO_SUPER_ADMIN,
    supportType: '',
  });
  const [supportTypeList, setSupportTypeList] = useState<
    Array<IDropdownOption>
  >([]);
  const searchDebounce = useDebounce(queryString, 1000);
  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchTickets();
    }
  }, [searchDebounce]);

  const fetchTickets = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_SUPPORT_TICKETS + queryString,
      );
      if (isSuccessResponse(response.statusCode)) {
        setTickets(response.data);
      }
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getSupportTypeList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_SUPPORT_ACTIVE_TYPE_LIST,
      );
      const list = response.data.map((supportType: IDropdownOption) => ({
        id: supportType.id,
        name: supportType.name,
      }));
      setSupportTypeList(list);
    } catch (error) {
      console.error('Error fetching support type list:', error);
    }
  };

  useEffect(() => {
    getSupportTypeList();
  }, []);

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };
  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Resolved Tickets</h1>
        <p className="text-muted-foreground">
          Resolved tickets that can be viewed
        </p>
      </div>

      <SupportTicketFilters
        description="Filter resolved tickets by various criteria"
        searchPlaceholder="Search resolved tickets..."
        values={{
          search: queryParams.search || '',
          supportType: queryParams.supportType,
          priority: queryParams.priority,
          assignCategory: queryParams.assignCategory,
        }}
        showAssignTo
        onChange={(field, value) =>
          setQueryParams(prevState => ({
            ...prevState,
            [field]: value,
          }))
        }
        onReset={() => {
          setQueryParams(prevState => ({
            ...prevState,
            offset: 0,
            search: '',
            supportType: '',
            priority: '',
            assignCategory: SupportTicketAssignCategory.ASSIGN_TO_SUPER_ADMIN,
          }));
        }}
        supportTypeList={supportTypeList}
        showSupportType
        showPriority
      />

      {/* Resolved Tickets */}
      <Card className='pt-6'>
        <CardContent>
          {loading ? (
            <TableLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Ticket ID</TableHead>
                  <TableHead>Title</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Priority</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Support Type</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tickets?.items?.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center">
                      There are no Resolved tickets at the moment
                    </TableCell>
                  </TableRow>
                )}
                {tickets?.items?.map(
                  (ticket: ISupportTicketList, index: number) => (
                    <TableRow key={index}>
                      <TableCell className="font-mono text-sm">
                        {ticket?.ticketId}
                      </TableCell>
                      <TableCell className="max-w-xs truncate font-medium">
                        {ticket?.title}
                      </TableCell>
                      <TableCell>{ticket?.username || 'N/A'}</TableCell>
                      <TableCell>
                        <Badge className={getPriorityColor(ticket?.priority)}>
                          {ticket?.priority}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge className={getStatusColor(ticket?.status)}>
                          {ticket?.status.toLowerCase()}
                        </Badge>
                      </TableCell>
                      <TableCell>{ticket?.supportType || '-'}</TableCell>
                      <TableCell>
                        {ticket?.userType
                          ?.toLowerCase()
                          ?.split('_')
                          ?.join(' ') || 'N/A'}
                      </TableCell>

                      <TableCell>
                        <div className="flex gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => {
                              setIsModalOpen(ModalType.VIEW);
                              setSelectedTicketId(ticket.id);
                            }}
                          >
                            <Eye className="size-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ),
                )}
              </TableBody>
            </Table>
          )}
          {tickets?.total > 10 && (
            <div className="my-6 flex justify-end">
              <Pagination
                total={tickets?.total || 0}
                perPage={tickets?.pageSize || 0}
                onPageChange={onPageChangeHandler}
              />
            </div>
          )}
        </CardContent>
      </Card>

      {isModalOpen === ModalType.VIEW && (
        <ClientUserViewModal
          isOpen={isModalOpen === ModalType.VIEW}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          id={selectedTicketId || ''}
        />
      )}
    </div>
  );
};

export default AspireAdminResolvedTicket;
