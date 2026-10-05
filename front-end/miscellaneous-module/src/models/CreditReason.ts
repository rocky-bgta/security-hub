export interface ICreditReasonPayload {
  reasonName: string;
  description: string;
  isActive: boolean;
}

export interface ICreditReason extends ICreditReasonPayload {
  id: string;
  reasonName: string;
  description: string;
  isActive: boolean;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
}
