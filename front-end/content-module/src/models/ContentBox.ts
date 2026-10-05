export type IContentBoxProps = {
  title: string;
  status?: string;
  description: string;
  image: string;
  onClick: () => void;
  isOpenView: () => void;
  isOpenEdit: () => void;
  viewOnly?: boolean;
};
