export interface IColumnProps {
  key: string;
  header: string | React.ReactNode;
  tdClassName?: string;
  render?: (row: { [key: string]: any }) => React.ReactNode;
}

export interface ITableProps {
  className?: string;
  thClassName?: string;
  tdClassName?: string;
  columns: Array<IColumnProps>;
  data: Array<{ [key: string]: any }>;
}
