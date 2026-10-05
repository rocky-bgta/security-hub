export interface IContentTypePayload {
  typeName: string;
  description: string;
  sortOrder: number;
}

export interface IContentType extends IContentTypePayload {
  id: string;
  createdAt?: string;
  updatedAt?: string;
}
