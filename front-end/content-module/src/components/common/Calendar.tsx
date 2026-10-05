import { ComponentProps } from 'react';
import { DayPicker } from 'react-day-picker';

import { cn } from 'utils/Helper';

export type CalendarProps = ComponentProps<typeof DayPicker>;

const Calendar = ({
  className,
  classNames,
  showOutsideDays = true,
  ...props
}: CalendarProps) => {
  return (
    <DayPicker
      showOutsideDays={showOutsideDays}
      className={cn('content-p-3', className)}
      classNames={{
        ...classNames,
      }}
      {...props}
    />
  );
};
Calendar.displayName = 'Calendar';

export { Calendar };
