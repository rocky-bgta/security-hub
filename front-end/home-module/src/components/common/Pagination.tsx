import { useEffect, useState } from 'react';
import { BsThreeDots } from 'react-icons/bs';
import { FaAngleLeft, FaAngleRight } from 'react-icons/fa';

import { DOT } from 'utils/Constants';
import { usePagination } from 'hooks/UsePagination';
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
  const [viewport, setViewport] = useState<'mobile' | 'tablet' | 'desktop'>(
    'desktop',
  );

  useEffect(() => {
    const mobileQuery = window.matchMedia('(max-width: 767px)');
    const tabletQuery = window.matchMedia('(max-width: 1023px)');

    const updateViewport = () => {
      if (mobileQuery.matches) {
        setViewport('mobile');
        return;
      }

      if (tabletQuery.matches) {
        setViewport('tablet');
        return;
      }

      setViewport('desktop');
    };

    updateViewport();

    mobileQuery.addEventListener('change', updateViewport);
    tabletQuery.addEventListener('change', updateViewport);

    return () => {
      mobileQuery.removeEventListener('change', updateViewport);
      tabletQuery.removeEventListener('change', updateViewport);
    };
  }, []);

  const responsiveSiblingCount =
    viewport === 'mobile'
      ? 0
      : viewport === 'tablet'
        ? Math.min(1, siblingCount)
        : siblingCount;

  const paginationRange = usePagination({
    total,
    perPage,
    currentPage,
    siblingCount: responsiveSiblingCount,
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
    <div className="home-pagination-container">
      <div className={cn('home-pagination-numbers', variant)}>
        <div
          className={cn(
            'home-flex home-flex-wrap home-items-center home-justify-center home-gap-1 md:home-flex-nowrap md:home-gap-x-2',
            variant,
          )}
        >
          <button
            className={cn(
              variant !== 'user' && 'home-mr-3',
              'home-pagination-number !home-rounded-full home-border home-p-1 home-text-[#545D7A] md:!home-p-3',
              currentPage === 1 && 'home-border-opacity-60',
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
                <span key={index} className="home-number-range">
                  <BsThreeDots />
                </span>
              );
            }

            return (
              <button
                key={index}
                className={cn(
                  'home-pagination-number',
                  pageNumber === currentPage && 'home-is-active',
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
              variant !== 'user' && 'home-ml-3',
              'home-pagination-number !home-rounded-full home-border home-p-1 home-text-[#545D7A] md:!home-p-3',
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
