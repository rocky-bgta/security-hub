import { useAuth } from 'hooks/UseAuth';

import { ROLE } from 'utils/Role';
import UserSupportTickets from './UserSupportTickets';
import ClientAdminSupportTickets from './ClientAdminSupportTickets';
import {
  SupportTicketPriority,
  SupportTicketStatus,
} from 'models/SupportTicket';
import { AlertCircle, CheckCircle, Clock } from 'lucide-react';
import MSPAdminSupportTicketList from './MSPAdminSupportTickets';

export const getStatusIcon = (status: SupportTicketStatus) => {
  switch (status) {
    case SupportTicketStatus.OPEN:
      return <Clock className="size-4" />;
    case SupportTicketStatus.IN_PROGRESS:
      return <AlertCircle className="size-4" />;
    case SupportTicketStatus.CLOSED:
      return <CheckCircle className="size-4" />;
    default:
      return <Clock className="size-4" />;
  }
};

export const getStatusColor = (status: SupportTicketStatus) => {
  switch (status) {
    case SupportTicketStatus.OPEN:
      return 'bg-blue-100 text-blue-800';
    case SupportTicketStatus.IN_PROGRESS:
      return 'bg-yellow-100 text-yellow-800';
    case SupportTicketStatus.CLOSED:
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

export const getPriorityColor = (priority: string) => {
  switch (priority) {
    case SupportTicketPriority.HIGH:
      return 'bg-red-100 text-red-800';
    case SupportTicketPriority.MEDIUM:
      return 'bg-yellow-100 text-yellow-800';
    case SupportTicketPriority.LOW:
      return 'bg-green-100 text-green-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

const SupportTickets = () => {
  const { role } = useAuth();

  if (role === ROLE.CLIENT_USER) {
    return <UserSupportTickets />;
  }

  if (role === ROLE.CLIENT_ADMIN || role === ROLE.ASPIRE_ADMIN) {
    return <ClientAdminSupportTickets />;
  }
  if (role === ROLE.MSP_ADMIN) {
    return <MSPAdminSupportTicketList />;
  }
  return null;
};

export default SupportTickets;
