import { ChangeEvent, Dispatch, Fragment, SetStateAction } from 'react';

import CustomSelect from 'common/CustomSelect';
import InputCard from 'components/InputCard';
import { IInputFields } from 'models/Content';
import { ISelectOption, TMultiValue, TSingleValue } from 'models/Input';
import { TypeOptions } from 'utils/Constants';

interface IProps {
  selectedTypes?: Array<ISelectOption>;
  setSelectedTypes?: Dispatch<SetStateAction<Array<ISelectOption>>>;
  typedInputs?: Array<IInputFields>;
  setTypedInputs?: Dispatch<SetStateAction<Array<IInputFields>>>;
}

const TypeSection = ({
  selectedTypes = [],
  setSelectedTypes,
  typedInputs = [],
  setTypedInputs,
}: IProps) => {
  const handleChangeType = (
    newValue: TMultiValue<ISelectOption> | TSingleValue<ISelectOption>,
  ) => {
    setSelectedTypes?.(
      (newValue as Array<ISelectOption>).sort((a, b) => +a.id - +b.id),
    );

    const newTypedInputs = (newValue as Array<ISelectOption>).map(item => {
      return (
        typedInputs.find(input => input.key === item.label) ?? {
          key: item.label,
          label: item.label,
          value: '',
          placeholder: 'Enter ' + item.value,
          color: '#ffffff',
          contrast: false,
          error: '',
        }
      );
    });

    setTypedInputs?.(newTypedInputs);
  };

  const handleChangeTypedInput = (e: ChangeEvent<HTMLInputElement>) => {
    const [key, type] = e.target.id.split('_'),
      { value, checked } = e.target;

    const updatedInputs = typedInputs.map(input => {
      if (input.key === key) {
        if (type === 'color') return { ...input, color: value };
        if (type === 'checkbox') return { ...input, contrast: checked };
        return {
          ...input,
          value,
          error: value.trim().length ? '' : key + ' is required',
        };
      }
      return input;
    });

    setTypedInputs?.(updatedInputs);
  };

  const handleResetColor = (id: string) => {
    const [key] = id.split('_');

    const updatedInputs = typedInputs.map(input => {
      if (input.key === key) {
        return { ...input, color: '#ffffff' };
      }
      return input;
    });

    setTypedInputs?.(updatedInputs);
  };

  return (
    <Fragment>
      <p className="content-mb-3 content-mt-5 content-text-white">Type</p>
      <CustomSelect
        name="type"
        value={selectedTypes}
        handleChange={handleChangeType}
        isMulti
        data={TypeOptions}
      />

      <div className="content-my-5 content-flex content-flex-col content-gap-y-5">
        {selectedTypes.map(item => (
          <div key={item.id}>
            <InputCard
              title={item.label}
              inputFields={[
                {
                  className:
                    item.label === 'Paragraph'
                      ? ''
                      : 'content-mt-2.5 content-w-full content-rounded content-border content-px-3 content-py-2',
                  type: item.label === 'Paragraph' ? 'editor' : 'text',
                  id:
                    item.label +
                    (item.label === 'Paragraph' ? '_editor' : '_input'),
                  value: typedInputs.find(input => input.key === item.label)
                    ?.value,
                  placeholder: typedInputs.find(
                    input => input.key === item.label,
                  )?.placeholder,
                  error: typedInputs.find(input => input.key === item.label)
                    ?.error,
                  onChange: handleChangeTypedInput,
                  onChangeEditor: (value: string) => {
                    const updatedInputs = typedInputs.map(input => {
                      if (input.key === item.label) {
                        return { ...input, value };
                      }
                      return input;
                    });
                    setTypedInputs?.(updatedInputs);
                  },
                },
                {
                  type: 'color',
                  id: item.label + '_color',
                  value: typedInputs.find(input => input.key === item.label)
                    ?.color,
                  onChange: handleChangeTypedInput,
                  onReset: handleResetColor,
                },
                ...(item.label !== 'Paragraph'
                  ? []
                  : [
                      {
                        type: 'checkbox',
                        id: item.label + '_checkbox',
                        checked: typedInputs.find(
                          input => input.key === item.label,
                        )?.contrast,
                        label: 'High Contrast Mode',
                        onChange: handleChangeTypedInput,
                      },
                    ]),
              ]}
            />
          </div>
        ))}
      </div>
    </Fragment>
  );
};

export default TypeSection;
