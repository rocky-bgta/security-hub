import { ArrowRight, CheckCircle, Eye, Plus } from 'lucide-react';
import { useEffect, useState } from 'react';

import { Badge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
import { Card, CardContent } from 'components/common/Card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import { IGetListParams, IList, ModalType } from 'models/Global';
import { useAPI } from 'hooks/UseAPI';
import { useAuth } from 'hooks/UseAuth';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  ISupportTicketList,
  SupportTicketAssignCategory,
  SupportTicketStatus,
} from 'models/SupportTicket';
import TableLoader from 'components/skeleton/TableLoader';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import Pagination from 'common/Pagination';
import useDebounce from 'hooks/UseDebounce';
import { InitGetListParams } from 'utils/Constants';
import ClientAdminActionModal from 'features/support-ticket/client-admin/ActionModal';
import ClientAdminViewModal from 'features/support-ticket/client-admin/ViewModal';
import ClientAdminForwardModal from 'features/support-ticket/client-admin/ForwardModal';
import { useStore } from 'hooks/UseStore';
import {
  getPriorityColor,
  getStatusColor,
  getStatusIcon,
} from './SupportTickets';
import ClientAdminSolveModal from 'features/support-ticket/client-admin/SolveModal';
import SupportTicketFilters, {
  SupportTicketView,
} from 'features/support-ticket/SupportTicketFilters';
import { IDropdownOption } from 'models/DropdownData';
import { ROLE } from 'utils/Role';

type TicketView = SupportTicketView;

const getViewParams = (
  view: TicketView,
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
        status: '',
      };
    case 'resolved':
      return {
        clientId: userId,
        createdBy: '',
        assignCategory: '',
        status: SupportTicketStatus.CLOSED,
      };
    case 'all':
    default:
      return {
        clientId: userId,
        createdBy: '',
        assignCategory: '',
        status: '',
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

const ClientAdminSupportTickets = () => {
  const apiClient = useAPI();
  const { role } = useAuth();
  const { userInfo } = useStore();
  const isClientAdmin = role === ROLE.CLIENT_ADMIN;
  const [loading, setLoading] = useState<boolean>(true);
  const [ticketView, setTicketView] = useState<TicketView>('all');
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
    supportType: '',
    ...(role === ROLE.ASPIRE_ADMIN
      ? { createdBy: userInfo?.userId || '' }
      : { clientId: userInfo?.userId || '' }),
  });
  const [supportTypeList, setSupportTypeList] = useState<
    Array<IDropdownOption>
  >([]);
  const searchDebounce = useDebounce(queryString, 1000);
  const isStatusLocked =
    isClientAdmin &&
    (ticketView === 'myPending' || ticketView === 'resolved');
  const isScopedQueryString =
    searchDebounce.includes('clientId=') ||
    searchDebounce.includes('createdBy=');

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (!userInfo?.userId) return;

    if (!isClientAdmin) {
      setQueryParams(prevState =>
        prevState.createdBy === userInfo.userId
          ? prevState
          : {
              ...prevState,
              createdBy: userInfo.userId,
              clientId: '',
              assignCategory: '',
            },
      );
      return;
    }

    setQueryParams(prevState => ({
      ...prevState,
      offset: 0,
      search: prevState.search,
      supportType: prevState.supportType,
      priority: prevState.priority,
      ...getViewParams(ticketView, userInfo.userId),
    }));
  }, [isClientAdmin, ticketView, userInfo?.userId]);

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

  const canForwardTicket = (ticket: ISupportTicketList) => {
    if (!isClientAdmin) return false;
    if (ticketView !== 'all' && ticketView !== 'user') return false;
    if (ticket.status === SupportTicketStatus.CLOSED) return false;
    return isClientUserTicket(ticket);
  };

  const canSolveTicket = () => {
    if (isClientAdmin && ticketView === 'resolved') return false;
    return true;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-white">Support Tickets</h2>
          <p className="text-muted-foreground">
            View and manage all support tickets
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(ModalType.ADD)}>
          <Plus className="mr-2 size-4" />
          Create Ticket
        </Button>
      </div>

      <SupportTicketFilters
        description="Filter support tickets using the criteria below."
        searchPlaceholder="Search tickets..."
        values={{
          search: queryParams.search || '',
          supportType: queryParams.supportType,
          priority: queryParams.priority,
          status: queryParams.status,
          ticketView,
        }}
        onChange={(field, value) => {
          if (field === 'ticketView') {
            setTicketView(value as TicketView);
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
            status: '',
          }));
        }}
        supportTypeList={supportTypeList}
        showSupportType
        showPriority
        showStatus
        disableStatus={isStatusLocked}
        showTicketView={isClientAdmin}
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
                  <TableHead>Subject</TableHead>
                  <TableHead>Submitted By</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Priority</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Submitted</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tickets?.items?.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center">
                      There are no support tickets at the moment
                    </TableCell>
                  </TableRow>
                )}
                {tickets?.items?.map(ticket => (
                  <TableRow key={ticket.id}>
                    <TableCell className="font-medium">
                      {ticket.ticketId}
                    </TableCell>
                    <TableCell>{ticket.title}</TableCell>
                    <TableCell>
                      <div>{ticket.username || '-'}</div>
                      {ticket.userType && (
                        <div className="text-xs capitalize text-muted-foreground">
                          {formatUserType(ticket.userType)}
                        </div>
                      )}
                    </TableCell>
                    <TableCell className="capitalize">
                      {ticket?.supportType || '-'}
                    </TableCell>
                    <TableCell className="capitalize">
                      <Badge className={getPriorityColor(ticket.priority)}>
                        {ticket.priority.toLowerCase()}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge className={getStatusColor(ticket.status)}>
                        <div className="flex items-center gap-1 capitalize">
                          {getStatusIcon(ticket.status)}
                          {ticket.status.toLowerCase()}
                        </div>
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
                        {canForwardTicket(ticket) && (
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
                        {canSolveTicket() && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => {
                              setIsModalOpen(ModalType.SOLVE);
                              setSelectedTicket(ticket);
                            }}
                            title="Solve Ticket"
                            disabled={
                              ticket.status === SupportTicketStatus.CLOSED
                            }
                          >
                            <CheckCircle className="size-4" />
                          </Button>
                        )}
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

      {isModalOpen === ModalType.ADD && (
        <ClientAdminActionModal
          isOpen={isModalOpen === ModalType.ADD}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={fetchTickets}
        />
      )}
      {isModalOpen === ModalType.VIEW && (
        <ClientAdminViewModal
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

export default ClientAdminSupportTickets;
