export const DURATION_OPTIONS = [
  { value: 'ONE_TO_THREE', label: '1 min - 3 min' },
  { value: 'THREE_TO_FIVE', label: '3 min - 5 min' },
  { value: 'FIVE_TO_TEN', label: '5 min - 10 min' },
  { value: 'TEN_TO_FIFTEEN', label: '10 min - 15 min' },
  { value: 'FIFTEEN_TO_TWENTY', label: '15 min - 20 min' },
  { value: 'TWENTY_PLUS', label: '20 min +' },
];

export interface IDropdownOption {
  id: string;
  name: string;
  description?: string;
  displayOrder?: number;
  isDefault?: boolean;
  isActive?: boolean;
}