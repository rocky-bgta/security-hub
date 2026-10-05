export interface ILanguagePayload {
  displayName: string;
  code: string;
  active: boolean;
}

export interface ILanguage extends ILanguagePayload {
  id: string;
  displayName: string;
  code: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}
