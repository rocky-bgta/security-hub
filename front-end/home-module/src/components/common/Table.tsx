import {
  forwardRef,
  HTMLAttributes,
  TdHTMLAttributes,
  ThHTMLAttributes,
} from 'react';

import { cn } from 'utils/Helper';

const Table = forwardRef<HTMLTableElement, HTMLAttributes<HTMLTableElement>>(
  ({ className, ...props }, ref) => (
    <div className="home-relative home-w-full home-overflow-x-auto home-overflow-y-hidden">
      <table
        ref={ref}
        className={cn(
          'home-min-w-[640px] home-w-full home-caption-bottom home-border home-border-card-border home-text-xs md:home-text-sm',
          className,
        )}
        {...props}
      />
    </div>
  ),
);

Table.displayName = 'Table';

const TableHeader = forwardRef<
  HTMLTableSectionElement,
  HTMLAttributes<HTMLTableSectionElement>
>(({ className, ...props }, ref) => (
  <thead
    ref={ref}
    className={cn(
      'home-bg-white !home-bg-opacity-25 [&_tr]:home-border-b',
      className,
    )}
    {...props}
  />
));

TableHeader.displayName = 'TableHeader';

const TableBody = forwardRef<
  HTMLTableSectionElement,
  HTMLAttributes<HTMLTableSectionElement>
>(({ className, ...props }, ref) => (
  <tbody
    ref={ref}
    className={cn('[&_tr:last-child]:home-border-0', className)}
    {...props}
  />
));

TableBody.displayName = 'TableBody';

const TableFooter = forwardRef<
  HTMLTableSectionElement,
  HTMLAttributes<HTMLTableSectionElement>
>(({ className, ...props }, ref) => (
  <tfoot
    ref={ref}
    className={cn(
      'home-border-t home-bg-muted/50 home-font-medium [&>tr]:last:home-border-b-0',
      className,
    )}
    {...props}
  />
));

TableFooter.displayName = 'TableFooter';

const TableRow = forwardRef<
  HTMLTableRowElement,
  HTMLAttributes<HTMLTableRowElement>
>(({ className, ...props }, ref) => (
  <tr
    ref={ref}
    className={cn(
      'home-border-b home-border-card-border home-transition-colors data-[state=selected]:home-bg-secondary',
      className,
    )}
    {...props}
  />
));

TableRow.displayName = 'TableRow';

const TableHead = forwardRef<
  HTMLTableCellElement,
  ThHTMLAttributes<HTMLTableCellElement>
>(({ className, ...props }, ref) => (
  <th
    ref={ref}
    className={cn(
      'home-h-10 home-whitespace-nowrap home-px-2 home-text-left home-align-middle home-font-medium home-text-cloudy-white md:home-h-12 md:home-px-4 [&:has([role=checkbox])]:home-pr-0',
      className,
    )}
    {...props}
  />
));

TableHead.displayName = 'TableHead';

const TableCell = forwardRef<
  HTMLTableCellElement,
  TdHTMLAttributes<HTMLTableCellElement>
>(({ className, ...props }, ref) => (
  <td
    ref={ref}
    className={cn(
      'home-whitespace-nowrap home-p-2 home-align-middle home-text-cloudy-white md:home-p-4 [&:has([role=checkbox])]:home-pr-0',
      className,
    )}
    {...props}
  />
));

TableCell.displayName = 'TableCell';

export {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
};
