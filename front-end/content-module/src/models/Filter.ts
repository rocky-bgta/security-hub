import { ISelectOption } from './Input';

export interface IFilterData {
  countryIds: ISelectOption[];
  complianceIds: ISelectOption[];
  categoryIds: ISelectOption[];
  contentTypeIds: ISelectOption[];
  durationMinutes: string[];
}
