import { ChevronLeft, ChevronRight } from 'lucide-react';
import { ComponentPropsWithoutRef, forwardRef, useEffect, useRef } from 'react';
import type { DateRange } from 'react-day-picker';
import {
  DayPicker,
  getDefaultClassNames,
  type ChevronProps,
  type DayButtonProps,
} from 'react-day-picker';

import { Button, buttonVariants } from 'common/Button';
import { cn } from 'utils/Helper';

export type IDateRange = DateRange;

const DateFilterDayButton = ({
  className,
  day,
  modifiers,
  ...props
}: DayButtonProps) => {
  const ref = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (modifiers.focused) ref.current?.focus();
  }, [modifiers.focused]);

  const isRangeStart = modifiers.range_start;
  const isRangeEnd = modifiers.range_end;
  const isRangeMiddle = modifiers.range_middle;
  const isRangeDay = isRangeStart || isRangeEnd || isRangeMiddle;
  const isSameDayRange = isRangeStart && isRangeEnd;
  const isSelected = modifiers.selected && !isRangeDay;

  return (
    <Button
      ref={ref}
      type="button"
      variant="ghost"
      size="icon"
      data-day={day.date.toLocaleDateString()}
      className={cn(
        '!content-size-9 !content-min-w-9 content-p-0 content-font-normal',
        isSameDayRange &&
          '!content-rounded-md !content-bg-primary !content-text-black hover:!content-bg-primary hover:!content-text-black',
        isRangeStart &&
          !isSameDayRange &&
          '!content-rounded-l-md !content-rounded-r-none !content-bg-primary !content-border-none !content-text-black hover:!content-bg-primary hover:!content-text-black',
        isRangeEnd &&
          !isSameDayRange &&
          '!content-rounded-r-md !content-rounded-l-none !content-bg-primary !content-border-none !content-text-black hover:!content-bg-primary hover:!content-text-black',
        isRangeMiddle &&
          '!content-rounded-none !content-bg-gray-600 !content-text-white hover:!content-bg-gray-600 hover:!content-text-white',
        isSelected &&
          '!content-rounded-md !content-bg-primary !content-text-black hover:!content-bg-primary hover:!content-text-black',
        modifiers.today &&
          !isRangeDay &&
          !modifiers.selected &&
          'content-rounded-md !content-bg-secondary !content-text-secondary-foreground',
        className,
      )}
      {...props}
    />
  );
};

const DateFilter = forwardRef<
  HTMLDivElement,
  ComponentPropsWithoutRef<typeof DayPicker>
>(({ className, classNames, showOutsideDays = true, ...props }, ref) => {
  const defaultClassNames = getDefaultClassNames();

  return (
    <div ref={ref}>
      <DayPicker
        showOutsideDays={showOutsideDays}
        className={cn(
          'group/calendar content-bg-secondary content-p-3 [--cell-size:2.25rem]',
          className,
        )}
        classNames={{
          root: cn('content-w-fit', defaultClassNames.root),
          months: cn(
            'content-relative content-flex content-flex-col content-gap-4 sm:content-flex-row sm:content-gap-10',
            defaultClassNames.months,
          ),
          month: cn('content-flex content-flex-col content-gap-4', defaultClassNames.month),
          nav: cn(
            'content-absolute content-inset-x-0 content-top-0 content-flex content-w-full content-items-center content-justify-between',
            defaultClassNames.nav,
          ),
          button_previous: cn(
            buttonVariants({ variant: 'outline', size: 'icon' }),
            'content-size-7 content-min-w-7 content-opacity-70 hover:content-opacity-100',
            defaultClassNames.button_previous,
          ),
          button_next: cn(
            buttonVariants({ variant: 'outline', size: 'icon' }),
            'content-size-7 content-min-w-7 content-opacity-70 hover:content-opacity-100',
            defaultClassNames.button_next,
          ),
          month_caption: cn(
            'content-flex content-h-[--cell-size] content-items-center content-justify-center content-px-[--cell-size]',
            defaultClassNames.month_caption,
          ),
          caption_label: cn(
            'content-text-sm content-font-medium content-text-foreground',
            defaultClassNames.caption_label,
          ),
          month_grid: cn('content-w-full content-border-collapse', defaultClassNames.month_grid),
          weekdays: cn('content-flex', defaultClassNames.weekdays),
          weekday: cn(
            'content-flex-1 content-text-center content-text-sm content-text-muted-foreground',
            defaultClassNames.weekday,
          ),
          week: cn('content-mt-2 content-flex content-w-full', defaultClassNames.week),
          day: cn(
            'content-relative content-flex content-size-9 content-items-center content-justify-center content-p-0 content-text-center',
            defaultClassNames.day,
          ),
          range_start: cn('!content-bg-transparent', defaultClassNames.range_start),
          range_middle: cn('!content-bg-transparent', defaultClassNames.range_middle),
          range_end: cn('!content-bg-transparent', defaultClassNames.range_end),
          selected: cn('!content-bg-transparent', defaultClassNames.selected),
          outside: cn(
            'content-text-muted-foreground content-opacity-50',
            defaultClassNames.outside,
          ),
          disabled: cn(
            'content-text-muted-foreground content-opacity-50',
            defaultClassNames.disabled,
          ),
          hidden: cn('invisible', defaultClassNames.hidden),
          ...classNames,
        }}
        components={{
          Chevron: ({
            orientation,
            className: chevronClassName,
            ...chevronProps
          }: ChevronProps) => {
            const Icon = orientation === 'left' ? ChevronLeft : ChevronRight;
            return (
              <Icon className={cn('content-size-4', chevronClassName)} {...chevronProps} />
            );
          },
          DayButton: DateFilterDayButton,
        }}
        {...props}
      />
    </div>
  );
});

DateFilter.displayName = 'DateFilter';

export { DateFilter };
