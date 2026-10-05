import clsx from 'clsx';
import { FC, useState } from 'react';
import { BsThreeDots } from 'react-icons/bs';
import { FaAngleLeft, FaAngleRight } from 'react-icons/fa';

import { usePagination } from 'hooks/UsePagination';
import { DOT } from 'utils/Constants';

import 'styles/pagination.css';

export interface PaginationProps {
  total: number;
  perPage: number;
  siblingCount?: number;
  onPageChange: (currentPage: number) => void;
  variant?: string;
}

const Pagination: FC<PaginationProps> = ({
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
    <div className="content-pagination-container">
      <div className={clsx('content-pagination-numbers', variant)}>
        <div
          className={clsx(
            'content-flex content-items-center content-gap-x-1 lg:content-gap-x-2',
            variant,
          )}
        >
          <button
            className={clsx(
              variant !== 'user' && 'content-mr-3',
              'content-pagination-number !content-rounded-full content-border content-p-1 content-text-[#545D7A] md:!content-p-3',
              currentPage === 1 && 'content-border-opacity-60',
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
                <span key={index} className="content-number-range">
                  <BsThreeDots />
                </span>
              );
            }

            return (
              <button
                key={index}
                className={clsx(
                  'content-pagination-number',
                  pageNumber === currentPage && 'content-is-active',
                  variant,
                )}
                onClick={() => goToPage(pageNumber)}
              >
                {pageNumber}
              </button>
            );
          })}

          <button
            className={clsx(
              variant !== 'user' && 'content-ml-3',
              'content-pagination-number !content-rounded-full content-border content-p-1 content-text-[#545D7A] md:!content-p-3',
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
