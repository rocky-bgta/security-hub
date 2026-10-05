export interface ITagPayload {
  name: string;
  description: string;
}

export interface ITagStatusPayload {
  status: string;
}

export interface ITag extends ITagPayload {
  id: string;
  createdAt: string;
  createdBy: string;
  status: string;
}
