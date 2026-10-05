import { ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from 'utils/Helper';
import { Button } from './Button';

interface PaginationProps {
  total: number;
  perPage: number;
  currentPage?: number;
  onPageChange: (page: number) => void;
}

/**
 * Pagination component
 */
const Pagination = ({
  total,
  perPage,
  currentPage = 1,
  onPageChange,
}: PaginationProps) => {
  const totalPages = Math.ceil(total / perPage);

  if (totalPages <= 1) return null;

  const getPageNumbers = (): (number | string)[] => {
    const pages: (number | string)[] = [];
    const maxVisiblePages = 5;

    if (totalPages <= maxVisiblePages) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage <= 3) {
        for (let i = 1; i <= 4; i++) {
          pages.push(i);
        }
        pages.push('...');
        pages.push(totalPages);
      } else if (currentPage >= totalPages - 2) {
        pages.push(1);
        pages.push('...');
        for (let i = totalPages - 3; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        pages.push(1);
        pages.push('...');
        for (let i = currentPage - 1; i <= currentPage + 1; i++) {
          pages.push(i);
        }
        pages.push('...');
        pages.push(totalPages);
      }
    }

    return pages;
  };

  return (
    <nav className="flex items-center gap-1">
      <Button
        type="button"
        variant="outline"
        size="icon"
        onClick={e => {
          e.preventDefault();
          onPageChange(currentPage - 1);
        }}
        disabled={currentPage === 1}
      >
        <ChevronLeft className="size-4" />
      </Button>

      {getPageNumbers().map((page, index) => (
        <Button
          key={index}
          type="button"
          variant={page === currentPage ? 'default' : 'outline'}
          size="sm"
          onClick={e => {
            e.preventDefault();
            if (typeof page === 'number') onPageChange(page);
          }}
          disabled={typeof page !== 'number'}
          className={cn(
            'min-w-[36px]',
            typeof page !== 'number' && 'cursor-default hover:bg-transparent',
          )}
        >
          {page}
        </Button>
      ))}

      <Button
        type="button"
        variant="outline"
        size="icon"
        onClick={e => {
          e.preventDefault();
          onPageChange(currentPage + 1);
        }}
        disabled={currentPage === totalPages}
      >
        <ChevronRight className="size-4" />
      </Button>
    </nav>
  );
};

export default Pagination;
