import clsx from 'clsx';

import 'styles/userHeading.css';

interface IProps {
  text: string;
  variant: 'title' | 'subtitle';
  className?: string;
}

const UserHeading = ({ text, variant, className }: IProps) => {
  return (
    <h2 className={clsx('content-text-white', variant, className)}>{text}</h2>
  );
};

export default UserHeading;
