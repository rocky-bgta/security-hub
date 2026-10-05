import { CheckCircle, Eye, Plus } from 'lucide-react';
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
import { API_END_POINTS } from 'routes/APIEndpoints';
import { ISupportTicketList, SupportTicketStatus } from 'models/SupportTicket';
import TableLoader from 'components/skeleton/TableLoader';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import Pagination from 'common/Pagination';
import useDebounce from 'hooks/UseDebounce';
import { InitGetListParams } from 'utils/Constants';
import ClientUserActionModal from 'features/support-ticket/client-user/ActionModal';
import ClientUserViewModal from 'features/support-ticket/client-user/ViewModal';
import { useStore } from 'hooks/UseStore';
import {
  getPriorityColor,
  getStatusColor,
  getStatusIcon,
} from './SupportTickets';
import ClientAdminSolveModal from 'features/support-ticket/client-admin/SolveModal';
import SupportTicketFilters from 'features/support-ticket/SupportTicketFilters';

const UserSupportTickets = () => {
  const apiClient = useAPI();
  const { userInfo } = useStore();
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
  });
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
        API_END_POINTS.GET_SUPPORT_TICKETS +
          queryString +
          `&createdBy=${userInfo?.userId}`,
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

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div className="space-y-4 sm:space-y-6">
      <div className="flex flex-col gap-3 justify-between sm:flex-row sm:items-center sm:gap-4">
        <div>
          <h2 className="text-xl font-semibold text-white sm:text-2xl">
            Support Tickets
          </h2>
          <p className="mt-1 text-sm text-muted-foreground sm:text-base">
            View and manage your support tickets.
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
          status: queryParams.status,
          priority: queryParams.priority,
        }}
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
            status: '',
            priority: '',
          }));
        }}
        showStatus
        showPriority
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
                  <TableHead>Type</TableHead>
                  <TableHead>Priority</TableHead>
                  <TableHead>Status</TableHead>
                  {/* <TableHead>MSP</TableHead> */}
                  <TableHead>Submitted</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {tickets.items.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center">
                      There are no pending tickets at the moment
                    </TableCell>
                  </TableRow>
                )}
                {tickets.items.map(ticket => (
                  <TableRow key={ticket.id}>
                    <TableCell className="font-medium">
                      {ticket.ticketId}
                    </TableCell>
                    <TableCell>{ticket.title}</TableCell>
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
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setIsModalOpen(ModalType.SOLVE);
                            setSelectedTicket(ticket);
                          }}
                          title="Mark as Solved"
                          disabled={
                            ticket.status === SupportTicketStatus.CLOSED
                          }
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
          {tickets.items.length > 10 && (
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
        <ClientUserActionModal
          isOpen={isModalOpen === ModalType.ADD}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={fetchTickets}
        />
      )}
      {isModalOpen === ModalType.VIEW && (
        <ClientUserViewModal
          isOpen={isModalOpen === ModalType.VIEW}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          id={selectedTicketId || ''}
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

export default UserSupportTickets;
