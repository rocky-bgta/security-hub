export interface IBillingAction {
  id: string;
  name: string;
}

export interface ISupportTicketType {
  id: string;
  name: string;
  description: string;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
  active: boolean;
}
