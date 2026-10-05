import { useMemo } from 'react';

import { DOT } from 'utils/Constants';
import { range } from 'utils/Helper';

interface PaginationConfig {
  total: number;
  perPage: number;
  currentPage: number;
  siblingCount?: number;
}

export const usePagination = ({
  total,
  perPage,
  currentPage,
  siblingCount = 1,
}: PaginationConfig) => {
  const paginationRange = useMemo<number[]>((): number[] => {
    const totalPageCount = Math.ceil(total / perPage);
    const totalPageNumbers = siblingCount + 5;

    if (totalPageNumbers >= totalPageCount) {
      return range(1, totalPageCount);
    }

    const leftSiblingIndex = Math.max(currentPage - siblingCount, 1);
    const rightSiblingIndex = Math.min(
      currentPage + siblingCount,
      totalPageCount,
    );

    const showLeftDots = leftSiblingIndex > 2;
    const showRightDots = rightSiblingIndex < totalPageCount - 2;

    const firstPageIndex = 1;
    const lastPageIndex = totalPageCount;

    if (!showLeftDots && showRightDots) {
      const leftItemCount = 3 + 2 * siblingCount;
      const leftRange = range(1, leftItemCount);
      return [...leftRange, DOT, totalPageCount];
    }

    if (showLeftDots && !showRightDots) {
      const rightItemCount = 3 + 2 * siblingCount;
      const rightRange = range(
        totalPageCount - rightItemCount + 1,
        totalPageCount,
      );
      return [firstPageIndex, DOT, ...rightRange];
    }

    if (showLeftDots && showRightDots) {
      const middleRange = range(leftSiblingIndex, rightSiblingIndex);
      return [firstPageIndex, DOT, ...middleRange, DOT, lastPageIndex];
    }

    return [];
  }, [total, perPage, currentPage, siblingCount]);

  return paginationRange;
};
