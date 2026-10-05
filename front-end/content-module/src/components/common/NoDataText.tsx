import clsx from 'clsx';

interface IProps {
  className?: string;
  text: string;
}

const NoDataText = ({ text, className }: IProps) => {
  return (
    <p
      className={clsx(
        className,
        'content-mb-0 content-text-center content-text-base content-text-white',
      )}
    >
      {text}
    </p>
  );
};
export default NoDataText;
