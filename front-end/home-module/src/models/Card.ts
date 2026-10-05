export interface IAnalystCount {
  id: number;
  name: string;
  value: number;
}

export interface ISalesProps {
  name: string;
  APish: number;
  ASAT: number;
}

export interface IOnBoardedData {
  id: number;
  name: string;
  totalLicense: number;
  onBoarded: number;
  expired: number;
  toBeExpired: number;
}

export enum OnBoardedTypes {
  CLIENT = 'client',
  MSP = 'msp',
}

export interface IOnBoardedCardProps {
  isLoading: boolean;
  cardClass?: string;
  cardBodyClass?: string;
  title: string;
  type: OnBoardedTypes;
  data: Array<IOnBoardedData> | null;
  error: any;
  showModal: boolean;
  modalData: IOnBoardedData | null;
  onOpenModal: (id: number, type: OnBoardedTypes) => void;
  onCloseModal: () => void;
}
