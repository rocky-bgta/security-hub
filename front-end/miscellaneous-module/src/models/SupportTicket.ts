import { ISupportTicketType } from './Billing';
import { IList } from './Global';

export interface ISupportTicket {
  title: string;
  parentTicketId: string;
  assignedTo: string;
  priority: SupportTicketPriority;
  supportType: string;
  productId: string;
  courseId: string;
  description: string;
  attachments: Array<string>;
}

export enum SupportTicketPriority {
  HIGH = 'HIGH',
  MEDIUM = 'MEDIUM',
  LOW = 'LOW',
}

export enum SupportTicketStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  CLOSED = 'CLOSED',
}

export enum SupportTicketAssignCategory {
  ASSIGN_TO_CLIENT = 'ASSIGNTOCLIENT',
  ASSIGN_TO_MSP = 'ASSIGNTOMSP',
  ASSIGN_TO_SUPER_ADMIN = 'ASSIGNTOSUPER',
}

export interface ISupportTicketList {
  id: string;
  ticketId: string;
  title: string;
  username: string;
  userType: string;
  clientId: string;
  userId: string;
  parentTicketId: string;
  assignedTo: string;
  status: SupportTicketStatus;
  priority: SupportTicketPriority;
  supportType: string;
  productId: string;
  courseId: string;
  mspId: string;
  description: string;
  attachments: string[];
  createdDate: string;
  updatedDate: string;
  assignToSuperAdmin: boolean;
}

export interface IComment {
  id: string;
  ticketId: string;
  parentCommentId: string | null;
  commentText: string;
  author: string;
  authorName: string;
  attachmentUrl: string[];
  commentedAt: string;
  replies: IComment[];
}

export interface ISupportTicketDetails {
  id: string;
  ticketId: string;
  title: string;
  username: string;
  userType: string;
  clientId: string;
  userId: string;
  parentTicketId: string;
  assignedTo: string;
  status: SupportTicketStatus;
  priority: SupportTicketPriority;
  supportType: ISupportTicketType;
  productId: string;
  courseId: string;
  mspId: string;
  description: string;
  attachments: string[];
  createdDate: string;
  updatedDate: string;
  assignToSuperAdmin: boolean;
}

export interface ISupportTicketReportSummary {
  totalTickets: number;
  openTickets: number;
  closedTickets: number;
  highPriorityTickets: number;
}

export interface ISupportTicketStatusDistribution {
  status: string;
  count: number;
}

export interface ISupportTicketRecentTicket {
  ticketId: string;
  subject: string;
  user: string;
  priority: SupportTicketPriority;
  status: SupportTicketStatus;
  createdDate: string;
  category: string;
}

export type ISupportTicketRecentTickets = IList<ISupportTicketRecentTicket>;

export interface ISupportResolutionByType {
  supportTypeId: string;
  supportTypeName: string;
  averageResolutionTime: number;
  totalTickets: number;
  openTickets: number;
  closedTickets: number;
  inProgressTickets: number;
  slaCompliance: number;
}

export interface ISupportResolutionTimeData {
  averageResolutionTime: number;
  slaCompliance: number;
  totalTickets: number;
  closedTickets: number;
  breaches?: number;
  escalated?: number;
  bySupportType: ISupportResolutionByType[];
}
