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
import { Eye, CheckCircle } from 'lucide-react';
import { useAPI } from 'hooks/UseAPI';
import { useEffect, useState } from 'react';
import { IGetListParams, IList, ModalType } from 'models/Global';
import {
  ISupportTicketList,
  SupportTicketAssignCategory,
  SupportTicketStatus,
} from 'models/SupportTicket';
import { InitGetListParams } from 'utils/Constants';
import useDebounce from 'hooks/UseDebounce';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { getPriorityColor, getStatusColor } from './SupportTickets';
import TableLoader from 'components/skeleton/TableLoader';
import ClientUserViewModal from 'features/support-ticket/client-user/ViewModal';
import ClientAdminSolveModal from 'features/support-ticket/client-admin/SolveModal';
import Pagination from 'common/Pagination';
import { IDropdownOption } from 'models/DropdownData';
import SupportTicketFilters from 'features/support-ticket/SupportTicketFilters';

const AspireAdminPendingTickets = () => {
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
  const [selectedTicket, setSelectedTicket] =
    useState<ISupportTicketList | null>(null);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    priority: '',
    status: SupportTicketStatus.OPEN,
    assignCategory: SupportTicketAssignCategory.ASSIGN_TO_SUPER_ADMIN,
    clientId: '',
    supportType: '',
  });
  const [clients, setClients] = useState<Array<any>>([]);
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
      const supportTypeList = response.data.map(
        (supportType: IDropdownOption) => ({
          id: supportType.id,
          name: supportType.name,
        }),
      );
      setSupportTypeList(supportTypeList);
    } catch (error) {
      console.error('Error fetching support type list:', error);
    }
  };

  useEffect(() => {
    fetchClients();
    getSupportTypeList();
  }, []);

  const fetchClients = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_CLIENTS + '?pageSize=1000',
      );
      if (isSuccessResponse(response.statusCode)) {
        setClients(response.data.clientAdmins);
      }
    } catch (error) {
      console.error(error);
    }
  };

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
        <h1 className="text-3xl font-bold tracking-tight">Pending Tickets</h1>
        <p className="text-muted-foreground">
          Manage tickets that are pending resolution, mark as solved or forward
          to teams
        </p>
      </div>

      <SupportTicketFilters
        description="Filter pending tickets by various criteria"
        searchPlaceholder="Search pending tickets..."
        values={{
          search: queryParams.search || '',
          clientId: queryParams.clientId,
          supportType: queryParams.supportType,
          priority: queryParams.priority,
          assignCategory: queryParams.assignCategory,
        }}
        onChange={(field, value) =>
          setQueryParams(prevState => {
            const { assignCategory: _, ...rest } = prevState as IGetListParams & {
              assignCategory?: string;
            };

            return {
              ...rest,
              offset: 0,
              ...(field === 'assignCategory'
                ? { assignCategory: value }
                : { [field]: value }),
            };
          })
        }
        onReset={() => {
          setQueryParams(prevState => {
            const { assignTo: _, ...rest } = prevState as IGetListParams & {
              assignTo?: string;
            };

            return {
              ...rest,
              offset: 0,
              search: '',
              clientId: '',
              supportType: '',
              priority: '',
              assignCategory: SupportTicketAssignCategory.ASSIGN_TO_SUPER_ADMIN,
            };
          });
        }}
        clients={clients}
        supportTypeList={supportTypeList}
        showClient
        showSupportType
        showPriority
        showAssignTo
      />

      {/* Pending Ticket List */}
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
                  <TableHead>Support Type</TableHead>
                  <TableHead>Priority</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead>Created Date</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tickets?.items?.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center">
                      There are no pending tickets at the moment
                    </TableCell>
                  </TableRow>
                )}
                {tickets?.items?.map((ticket, index) => (
                  <TableRow key={index}>
                    <TableCell className="font-mono text-sm">
                      {ticket?.ticketId}
                    </TableCell>
                    <TableCell className="max-w-xs truncate font-medium">
                      {ticket?.title}
                    </TableCell>
                    <TableCell>{ticket?.username || 'N/A'}</TableCell>
                    <TableCell>{ticket?.supportType || '-'}</TableCell>
                    <TableCell>
                      <Badge className={getPriorityColor(ticket?.priority)}>
                        {ticket?.priority}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge className={getStatusColor(ticket?.status)}>
                        {ticket?.status}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {ticket?.userType?.toLowerCase()?.split('_')?.join(' ') ||
                        'N/A'}
                    </TableCell>
                    <TableCell>
                      {formateDateAndTime(ticket?.createdDate)}
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
                          title="Ticket Details"
                        >
                          <Eye className="size-4" />
                        </Button>
                        {/* <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setIsModalOpen(ModalType.FORWARD);
                            setSelectedTicket(ticket);
                          }}
                          title="Forward to MSP"
                        >
                          <ArrowRight className="h-4 w-4" />
                        </Button> */}
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setIsModalOpen(ModalType.SOLVE);
                            // setSelectedTicketId(ticket.id);
                            setSelectedTicket(ticket);
                          }}
                          title="Mark as Solved"
                        >
                          <CheckCircle className="size-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
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
      {/* {isModalOpen === ModalType.FORWARD && (
        <ClientAdminForwardModal
          isOpen={isModalOpen === ModalType.FORWARD}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={fetchTickets}
          ticket={selectedTicket as ISupportTicketList}
        />
      )} */}
      {isModalOpen === ModalType.SOLVE && (
        <ClientAdminSolveModal
          isOpen={isModalOpen === ModalType.SOLVE}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={fetchTickets}
          ticket={selectedTicket as ISupportTicketList}
        />
      )}
    </div>
  );
};

export default AspireAdminPendingTickets;
