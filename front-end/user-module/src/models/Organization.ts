export type TOrganization = {
  id: number;
  index: number;
  organizationName: string;
  email: string;
  designation: string;
  department: string;
  country: string;
  timeZone: string;
};

export interface IOrganizationData {
  organizationName: string;
  email: string;
  department: string;
  country: string;
  role: string;
  status: string;
  supervisorName: string;
  supervisorEmail: string;
  group: string;
  zipCode: string;
}
