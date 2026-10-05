import clsx from 'clsx';
import { ReactNode } from 'react';

interface IProps {
  children: ReactNode;
  className?: string;
}

const Border = ({ children, className }: IProps) => {
  return (
    <div
      className={clsx(
        className,
        "content-relative content-bg-transparent before:content-pointer-events-none before:content-absolute before:content-inset-x-6 before:content-inset-y-0 before:content-z-0 before:content-border-y before:content-border-card-border before:content-content-[''] after:content-pointer-events-none after:content-absolute after:content-inset-x-0 after:content-inset-y-6 after:content-z-0 after:content-border-x after:content-border-card-border after:content-content-['']",
      )}
    >
      <div className="content-pointer-events-none content-absolute content-left-0 content-top-0 content-size-full">
        <div className="content-absolute content-left-0 content-top-0 content-size-3 content-border-l-2 content-border-t-2 content-border-white content-border-opacity-75" />
        <div className="content-absolute content-right-0 content-top-0 content-size-3 content-border-r-2 content-border-t-2 content-border-white content-border-opacity-75" />
        <div className="content-absolute content-bottom-0 content-left-0 content-size-3 content-border-b-2 content-border-l-2 content-border-white content-border-opacity-75" />
        <div className="content-absolute content-bottom-0 content-right-0 content-size-3 content-border-b-2 content-border-r-2 content-border-white content-border-opacity-75" />
      </div>
      {children}
    </div>
  );
};

export default Border;
