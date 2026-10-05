import { SearchIcon } from 'assets/icons';
import clsx from 'clsx';
import { Input } from 'common/Input';

interface IProps {
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  className?: string;
}

const SearchBox = ({
  value,
  onChange,
  placeholder = 'Search',
  className,
}: IProps) => {
  return (
    <div className="content-relative">
      <Input
        id="search"
        placeholder={placeholder}
        className={clsx(
          'content-h-10 content-w-full content-pl-10 sm:content-w-[280px] lg:content-w-[380px]',
          className,
        )}
        value={value}
        onChange={e => onChange(e.target.value)}
      />
      <SearchIcon
        width={20}
        height={20}
        stroke="#FFFFFF80"
        className="content-absolute content-left-3 content-top-[50%] -content-translate-y-2/4"
      />
    </div>
  );
};

export default SearchBox;
