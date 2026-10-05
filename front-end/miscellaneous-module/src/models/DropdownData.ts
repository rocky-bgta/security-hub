export interface IDropdownData {
  countries?: Array<{ id: string; name: string }>;
  industries?: Array<{ id: string; name: string }>;
  policyTypes?: Array<{ id: string; name: string }>;
}

export interface IDropdownOption {
  id: string;
  name: string;
}
