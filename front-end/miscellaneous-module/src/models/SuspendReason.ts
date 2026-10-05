export interface ISuspendReasonPayload {
  name: string;
  description: string;
  active: boolean;
}

export interface ISuspendReason extends ISuspendReasonPayload {
  id: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}
