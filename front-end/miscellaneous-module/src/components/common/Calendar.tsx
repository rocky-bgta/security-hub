import { ChevronLeft, ChevronRight } from 'lucide-react';
import { ComponentPropsWithoutRef, forwardRef } from 'react';
import type { DateRange } from 'react-day-picker';
import { DayPicker, useDayPicker } from 'react-day-picker';

import { cn } from 'utils/Helper';

export interface IDateRange extends DateRange {}

const CustomNavbar = () => {
  const { previousMonth, nextMonth, goToMonth } = useDayPicker();

  return (
    <div className="absolute inset-x-4 flex items-center justify-between space-x-1">
      {previousMonth && (
        <button
          type="button"
          onClick={() => goToMonth(previousMonth)}
          className="flex size-7 items-center justify-center rounded border border-card-border bg-transparent p-0 text-white opacity-50 transition-all duration-150 ease-in-out hover:bg-secondary hover:text-white hover:opacity-100"
        >
          <ChevronLeft className="size-4" />
        </button>
      )}
      {nextMonth && (
        <button
          type="button"
          onClick={() => goToMonth(nextMonth)}
          className="flex size-7 items-center justify-center rounded border border-card-border bg-transparent p-0 text-white opacity-50 hover:bg-secondary hover:text-white hover:opacity-100"
        >
          <ChevronRight className="size-4" />
        </button>
      )}
    </div>
  );
};

const Calendar = forwardRef<
  HTMLDivElement,
  ComponentPropsWithoutRef<typeof DayPicker>
>(({ className, classNames, showOutsideDays = true, ...props }, ref) => {
  return (
    <div ref={ref}>
      <DayPicker
        showOutsideDays={showOutsideDays}
        className={cn('p-3', className)}
        classNames={{
          months: 'flex flex-col sm:flex-row gap-x-10 space-y-1 px-10 py-2',
          month: 'space-y-4',
          caption: 'flex justify-center pt-1 relative items-center',
          caption_label: 'text-sm font-medium ml-3',
          day: 'hover:bg-secondary hover:text-secondary-foreground h-9 w-9 p-0 font-normal aria-selected:opacity-100',
          today: 'bg-secondary text-secondary-foreground',
          selected:
            'text-white hover:bg-primary hover:text-primary-foreground focus:bg-primary focus:text-primary-foreground',
          range_start: 'bg-primary rounded-l-md text-black',
          range_end: 'bg-primary rounded-r-md text-black',
          range_middle: 'bg-gray-600',
          outside:
            'day-outside text-muted-foreground opacity-50 aria-selected:bg-secondary/50 aria-selected:text-muted-foreground aria-selected:opacity-30',
          disabled: 'text-muted-foreground opacity-50',
          hidden: 'invisible',
          weekday: 'w-9 text-center text-sm text-muted-foreground',
          ...classNames,
        }}
        components={{ Nav: CustomNavbar }}
        {...props}
      />
    </div>
  );
});

export { Calendar };
