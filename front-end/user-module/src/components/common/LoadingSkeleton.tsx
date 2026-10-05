import { ReactNode } from 'react';

const LoadingSkeleton = ({
  rows,
  columns,
}: {
  rows: number;
  columns: Array<{ key: string }>;
}) => {
  return Array.from({ length: rows }).map(_ =>
    columns.reduce(
      (acc, col) => {
        acc[col.key] = (
          <div className="h-4 animate-pulse rounded bg-gray-200" />
        );
        return acc;
      },
      {} as { [key: string]: ReactNode },
    ),
  );
};

export default LoadingSkeleton;
