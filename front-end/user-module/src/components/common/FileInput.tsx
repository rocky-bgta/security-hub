import { IFileInputProps } from 'models/Input';

const FileInput = ({
  type = 'text',
  className = '',
  id,
  name,
  value,
  placeholder,
  accept,
  onChangeInput,
}: IFileInputProps) => {
  return (
    <input
      id={id}
      className={className}
      type={type}
      placeholder={placeholder}
      onChange={onChangeInput}
      name={name}
      value={value}
      accept={accept}
    />
  );
};

export default FileInput;
