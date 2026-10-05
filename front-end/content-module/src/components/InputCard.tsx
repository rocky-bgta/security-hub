import clsx from 'clsx';
import { ChangeEvent, ReactNode, RefObject } from 'react';

import { Card } from 'common/Card';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import TextEditor from 'common/TextEditor';
import ColorInput from 'components/ColorInput';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';

interface IProps {
  title: string;
  inputFields: Array<{
    className?: string;
    type: string;
    id: string;
    ref?: RefObject<FileUploaderHandle | null>;
    value?: string;
    label?: string;
    placeholder?: string;
    checked?: boolean;
    accept?: string;
    error?: string;
    onChange?: (e: ChangeEvent<HTMLInputElement>) => void;
    onChangeEditor?: (value: string) => void;
    onReset?: (id: string) => void;
    onUpload?: (files: FileList) => void;
  }>;
  children?: ReactNode;
}

const InputCard = ({ title, inputFields, children }: IProps) => {
  return (
    <Card title={title}>
      <div className="content-flex content-flex-col content-gap-y-6 content-p-5 content-pt-2.5">
        {inputFields.map(item => {
          if (item.type === 'color') {
            return (
              <div key={item.id}>
                {item.label && <Label htmlFor={item.id}>{item.label}</Label>}
                <ColorInput
                  id={item.id}
                  value={item.value}
                  onChangeInput={item.onChange}
                  onClickReset={() => item.onReset?.(item.id)}
                />
              </div>
            );
          }

          if (item.type === 'checkbox') {
            return (
              <div key={item.id} className="content-flex content-items-center">
                <Checkbox
                  id={item.id}
                  checked={item.checked}
                  onCheckedChange={checked =>
                    item.onChange?.({ target: { checked } } as any)
                  }
                />
                <label className="content-ml-3 content-text-base content-text-white">
                  {item.label}
                </label>
              </div>
            );
          }

          if (item.type === 'file') {
            return (
              <div
                key={item.id}
                className="content-mb-4 content-flex content-items-center"
              >
                <FileUploader
                  ref={item.ref}
                  id={item.id}
                  containerClassName={item.className}
                  accept={item.accept}
                  placeholder={item.placeholder}
                  onUpload={item.onUpload!}
                />
                <p className="content-ml-3 content-inline-block content-text-gray-700">
                  OR
                </p>
              </div>
            );
          }

          if (item.type === 'editor') {
            return (
              <div key={item.id}>
                {item.label && <Label htmlFor={item.id}>{item.label}</Label>}
                <TextEditor
                  key={item.id}
                  className={item.className}
                  value={item.value ?? ''}
                  onChange={value => item.onChangeEditor?.(value)}
                />
                {item.error && (
                  <p className="content-mt-1 content-text-sm content-text-red-500">
                    {item.error}
                  </p>
                )}
              </div>
            );
          }

          return (
            <div key={item.id}>
              {item.label && <Label htmlFor={item.id}>{item.label}</Label>}
              <Input
                key={item.id}
                className={clsx(item.className, {
                  'content-has-error': !!item.error,
                })}
                type={item.type}
                id={item.id}
                value={item.value}
                placeholder={item.placeholder}
                onChange={item.onChange}
              />
              {item.error && (
                <p className="content-mt-1 content-text-sm content-text-red-500">
                  {item.error}
                </p>
              )}
            </div>
          );
        })}

        {children}
      </div>
    </Card>
  );
};

export default InputCard;
