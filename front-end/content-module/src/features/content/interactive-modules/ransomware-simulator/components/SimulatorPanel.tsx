import { HTMLAttributes, ReactNode } from 'react';

import { cn } from 'utils/Helper';

export const SIMULATOR_PANEL_CLASS =
  'content-rounded-lg content-border content-border-white/10 content-bg-card-background/80 content-p-3 content-shadow-sm content-backdrop-blur-sm';

export const SIMULATOR_PANEL_TITLE_CLASS =
  'content-text-sm content-font-semibold content-text-white';

interface IProps extends Omit<HTMLAttributes<HTMLElement>, 'title'> {
  title?: ReactNode;
  headerRight?: ReactNode;
  children: ReactNode;
}

const SimulatorPanel = ({
  title,
  headerRight,
  className,
  children,
  ...props
}: IProps) => {
  return (
    <section className={cn(SIMULATOR_PANEL_CLASS, className)} {...props}>
      {title ? (
        <div
          className={cn(
            'content-mb-2',
            headerRight &&
              'content-flex content-items-center content-justify-between content-gap-2',
          )}
        >
          <h2
            className={cn(
              SIMULATOR_PANEL_TITLE_CLASS,
              headerRight && 'content-flex content-min-w-0 content-items-center content-gap-2',
            )}
          >
            {title}
          </h2>
          {headerRight}
        </div>
      ) : null}
      {children}
    </section>
  );
};

export default SimulatorPanel;
