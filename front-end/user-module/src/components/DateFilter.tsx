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
        '!size-9 !min-w-9 p-0 font-normal',
        isSameDayRange &&
        '!rounded-md !bg-primary !text-black hover:!bg-primary hover:!text-black',
        isRangeStart &&
        !isSameDayRange &&
        '!rounded-l-md !rounded-r-none !bg-primary !text-black hover:!bg-primary hover:!text-black',
        isRangeEnd &&
        !isSameDayRange &&
        '!rounded-r-md !rounded-l-none !bg-primary !text-black hover:!bg-primary hover:!text-black',
        isRangeMiddle &&
        '!rounded-none !bg-gray-600 !text-white hover:!bg-gray-600 hover:!text-white',
        isSelected &&
        '!rounded-md !bg-primary !text-black hover:!bg-primary hover:!text-black',
        modifiers.today &&
        !isRangeDay &&
        !modifiers.selected &&
        'rounded-md bg-secondary text-secondary-foreground',
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
          'group/calendar bg-secondary p-3 [--cell-size:2.25rem]',
          className,
        )}
        classNames={{
          root: cn('w-fit', defaultClassNames.root),
          months: cn(
            'relative flex flex-col gap-4 sm:flex-row sm:gap-10',
            defaultClassNames.months,
          ),
          month: cn('flex flex-col gap-4', defaultClassNames.month),
          nav: cn(
            'absolute inset-x-0 top-0 flex w-full items-center justify-between',
            defaultClassNames.nav,
          ),
          button_previous: cn(
            buttonVariants({ variant: 'outline', size: 'icon' }),
            'size-7 min-w-7 opacity-70 hover:opacity-100',
            defaultClassNames.button_previous,
          ),
          button_next: cn(
            buttonVariants({ variant: 'outline', size: 'icon' }),
            'size-7 min-w-7 opacity-70 hover:opacity-100',
            defaultClassNames.button_next,
          ),
          month_caption: cn(
            'flex h-[--cell-size] items-center justify-center px-[--cell-size]',
            defaultClassNames.month_caption,
          ),
          caption_label: cn(
            'text-sm font-medium text-foreground',
            defaultClassNames.caption_label,
          ),
          month_grid: cn('w-full border-collapse', defaultClassNames.month_grid),
          weekdays: cn('flex', defaultClassNames.weekdays),
          weekday: cn(
            'flex-1 text-center text-sm text-muted-foreground',
            defaultClassNames.weekday,
          ),
          week: cn('mt-2 flex w-full', defaultClassNames.week),
          day: cn(
            'relative aspect-square p-0 text-center',
            defaultClassNames.day,
          ),
          outside: cn(
            'text-muted-foreground opacity-50',
            defaultClassNames.outside,
          ),
          disabled: cn(
            'text-muted-foreground opacity-50',
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
              <Icon className={cn('size-4', chevronClassName)} {...chevronProps} />
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
