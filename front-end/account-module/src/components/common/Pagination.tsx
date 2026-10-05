import { useState } from 'react';
import { BsThreeDots } from 'react-icons/bs';
import { FaAngleLeft, FaAngleRight } from 'react-icons/fa';

import { usePagination } from 'hooks/UsePagination';
import { DOT } from 'utils/Constants';
import { cn } from 'utils/Helper';

import 'styles/pagination.css';

export interface PaginationProps {
  total: number;
  perPage: number;
  siblingCount?: number;
  onPageChange: (currentPage: number) => void;
  variant?: string;
}

const Pagination = ({
  total,
  perPage,
  siblingCount = 1,
  onPageChange,
  variant = '',
}: PaginationProps) => {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const paginationRange = usePagination({
    total,
    perPage,
    currentPage,
    siblingCount,
  });

  if (currentPage === 0 || paginationRange.length < 2) {
    return null;
  }

  const goToPage = (page: number = 1) => {
    setCurrentPage(page);
    onPageChange(page);
  };

  const lastPage = paginationRange[paginationRange.length - 1];

  return (
    <div className="pagination-container">
      <div className={cn('pagination-numbers', variant)}>
        <div className={cn('flex items-center gap-x-1 lg:gap-x-2', variant)}>
          <button
            className={cn(
              variant !== 'user' && 'mr-3',
              'pagination-number !rounded-full border p-1 text-[#545D7A] md:!p-3',
              currentPage === 1 && 'border-opacity-60',
              variant,
            )}
            onClick={() => goToPage(currentPage - 1)}
            disabled={currentPage === 1}
          >
            <FaAngleLeft />
          </button>

          {paginationRange.map((pageNumber, index) => {
            if (pageNumber === DOT) {
              return (
                <span key={index} className="number-range">
                  <BsThreeDots />
                </span>
              );
            }

            return (
              <button
                key={index}
                className={cn(
                  'pagination-number',
                  pageNumber === currentPage && 'is-active',
                  variant,
                )}
                onClick={() => goToPage(pageNumber)}
              >
                {pageNumber}
              </button>
            );
          })}

          <button
            className={cn(
              variant !== 'user' && 'ml-3',
              'pagination-number !rounded-full border p-1 text-[#545D7A] md:!p-3',
              variant,
            )}
            onClick={() => goToPage(currentPage + 1)}
            disabled={currentPage === lastPage}
          >
            <FaAngleRight />
          </button>
        </div>
      </div>
    </div>
  );
};

export default Pagination;
