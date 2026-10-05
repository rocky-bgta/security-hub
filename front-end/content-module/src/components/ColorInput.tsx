import { ChangeEvent, useEffect, useRef, useState } from 'react';
import ColorPicker from 'react-pick-color';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { rgbaToHex } from 'utils/Helper';

interface IProps {
  id: string;
  value?: string;
  onChangeInput?: (e: ChangeEvent<HTMLInputElement>) => void;
  onClickReset?: () => void;
}

const ColorInput = ({
  id,
  value = '#ffffffff',
  onChangeInput,
  onClickReset,
}: IProps) => {
  const toggleRef = useRef<HTMLButtonElement>(null);
  const inputRef = useRef<HTMLDivElement>(null);
  const pickerRef = useRef<HTMLDivElement>(null);

  const [showPicker, setShowPicker] = useState<boolean>(false);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        pickerRef.current &&
        !pickerRef.current.contains(event.target as Node) &&
        toggleRef.current &&
        !toggleRef.current.contains(event.target as Node) &&
        inputRef.current &&
        !inputRef.current.contains(event.target as Node)
      ) {
        setShowPicker(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="content-relative content-flex content-items-center content-gap-x-2">
      <button
        ref={toggleRef}
        className="content-h-10 content-w-14 content-rounded-md content-border content-border-soft-blue-gray"
        style={{ backgroundColor: value }}
        onClick={_ => setShowPicker(!showPicker)}
      />

      <div ref={inputRef} className="content-w-full content-text-white">
        <Input id={id} type="text" value={value} onChange={onChangeInput} />
      </div>
      <Button onClick={onClickReset} size="sm">
        Reset
      </Button>

      {showPicker && (
        <div
          ref={pickerRef}
          className="content-absolute content-top-12 content-z-10"
        >
          <ColorPicker
            color={value}
            hideInputs={true}
            onChange={color => {
              onChangeInput?.({
                target: {
                  id,
                  value: rgbaToHex(color.rgb),
                },
              } as ChangeEvent<HTMLInputElement>);
            }}
          />
        </div>
      )}
    </div>
  );
};

export default ColorInput;
