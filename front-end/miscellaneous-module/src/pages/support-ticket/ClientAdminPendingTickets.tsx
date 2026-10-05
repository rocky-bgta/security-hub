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
import { Eye, ArrowRight, CheckCircle } from 'lucide-react';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
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
import ClientAdminForwardModal from 'features/support-ticket/client-admin/ForwardModal';
import ClientAdminSolveModal from 'features/support-ticket/client-admin/SolveModal';
import Pagination from 'common/Pagination';
import { IDropdownOption } from 'models/DropdownData';
import SupportTicketFilters, {
  SupportTicketView,
} from 'features/support-ticket/SupportTicketFilters';
import { ROLE } from 'utils/Role';

type PendingTicketView = Extract<
  SupportTicketView,
  'all' | 'myPending' | 'user'
>;

const getPendingViewParams = (
  view: PendingTicketView,
  userId: string,
): Pick<
  IGetListParams,
  'clientId' | 'createdBy' | 'assignCategory' | 'status'
> => {
  switch (view) {
    case 'myPending':
      return {
        createdBy: userId,
        clientId: '',
        assignCategory: '',
        status: SupportTicketStatus.OPEN,
      };
    case 'user':
      return {
        clientId: userId,
        createdBy: '',
        assignCategory: SupportTicketAssignCategory.ASSIGN_TO_CLIENT,
        status: SupportTicketStatus.OPEN,
      };
    case 'all':
    default:
      return {
        clientId: userId,
        createdBy: '',
        assignCategory: '',
        status: SupportTicketStatus.OPEN,
      };
  }
};

const isClientUserTicket = (ticket: ISupportTicketList) => {
  const userType = ticket.userType?.toUpperCase();
  return userType === ROLE.CLIENT_USER || userType === 'CLIENT_USER';
};

const formatUserType = (userType?: string) => {
  if (!userType) return '';
  return userType.toLowerCase().split('_').join(' ');
};

const ClientAdminPendingTickets = () => {
  const apiClient = useAPI();
  const { userInfo } = useStore();
  const [loading, setLoading] = useState<boolean>(true);
  const [ticketView, setTicketView] = useState<PendingTicketView>('all');
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
    supportType: '',
    clientId: userInfo?.userId || '',
  });
  const [supportTypeList, setSupportTypeList] = useState<
    Array<IDropdownOption>
  >([]);
  const searchDebounce = useDebounce(queryString, 1000);
  const isScopedQueryString =
    searchDebounce.includes('clientId=') ||
    searchDebounce.includes('createdBy=');

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (!userInfo?.userId) return;

    setQueryParams(prevState => ({
      ...prevState,
      offset: 0,
      search: prevState.search,
      supportType: prevState.supportType,
      priority: prevState.priority,
      ...getPendingViewParams(ticketView, userInfo.userId),
    }));
  }, [ticketView, userInfo?.userId]);

  useEffect(() => {
    if (searchDebounce && isScopedQueryString) {
      fetchTickets();
    }
  }, [searchDebounce, isScopedQueryString]);

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
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Pending Tickets</h1>
        <p className="text-muted-foreground">
          Manage pending tickets from client and users, mark as solved or
          forward to teams
        </p>
      </div>

      <SupportTicketFilters
        description="Filter pending tickets by client, user, and other criteria"
        searchPlaceholder="Search pending tickets..."
        values={{
          search: queryParams.search || '',
          supportType: queryParams.supportType,
          priority: queryParams.priority,
          ticketView,
        }}
        onChange={(field, value) => {
          if (field === 'ticketView') {
            setTicketView(value as PendingTicketView);
            return;
          }
          setQueryParams(prevState => ({
            ...prevState,
            offset: 0,
            [field]: value,
          }));
        }}
        onReset={() => {
          setTicketView('all');
          setQueryParams(prevState => ({
            ...prevState,
            offset: 0,
            search: '',
            supportType: '',
            priority: '',
          }));
        }}
        supportTypeList={supportTypeList}
        showSupportType
        showPriority
        showTicketView
        ticketViewOptions={[
          { value: 'all', label: 'All' },
          { value: 'myPending', label: 'Admin Tickets' },
          { value: 'user', label: 'User Tickets' },
        ]}
      />

      <Card className="pt-6">
        <CardContent>
          {loading ? (
            <TableLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Ticket ID</TableHead>
                  <TableHead>Title</TableHead>
                  <TableHead>Submitted By</TableHead>
                  <TableHead>Support Type</TableHead>
                  <TableHead>Priority</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Created Date</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tickets?.items?.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center">
                      There are no pending tickets at the moment
                    </TableCell>
                  </TableRow>
                )}
                {tickets?.items?.map(ticket => (
                  <TableRow key={ticket.id}>
                    <TableCell className="font-mono text-sm">
                      {ticket.ticketId}
                    </TableCell>
                    <TableCell className="max-w-xs truncate font-medium">
                      {ticket.title}
                    </TableCell>
                    <TableCell>
                      <div>{ticket.username || '-'}</div>
                      {ticket.userType && (
                        <div className="text-xs capitalize text-muted-foreground">
                          {formatUserType(ticket.userType)}
                        </div>
                      )}
                    </TableCell>
                    <TableCell>{ticket?.supportType || '-'}</TableCell>
                    <TableCell>
                      <Badge className={getPriorityColor(ticket.priority)}>
                        {ticket.priority}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge className={getStatusColor(ticket.status)}>
                        {ticket.status}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {formateDateAndTime(ticket.createdDate)}
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
                        {isClientUserTicket(ticket) && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => {
                              setIsModalOpen(ModalType.FORWARD);
                              setSelectedTicket(ticket);
                            }}
                            title="Forward to MSP"
                          >
                            <ArrowRight className="size-4" />
                          </Button>
                        )}
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setIsModalOpen(ModalType.SOLVE);
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
      {isModalOpen === ModalType.FORWARD && (
        <ClientAdminForwardModal
          isOpen={isModalOpen === ModalType.FORWARD}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={fetchTickets}
          ticket={selectedTicket as ISupportTicketList}
        />
      )}
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

export default ClientAdminPendingTickets;
