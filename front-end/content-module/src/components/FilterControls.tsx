import CustomSelect from 'common/CustomSelect';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { DURATION_OPTIONS } from 'models/DropDown';
import { IFilterData } from 'models/Filter';
import { ISelectOption, TMultiValue, TSingleValue } from 'models/Input';

const FilterControls = ({
  filterData,
  onFilterChange,
  options,
  search,
  setSearch,
}: {
  filterData: IFilterData;
  onFilterChange: (updates: Partial<IFilterData>) => void;
  options: {
    categoryOptions: ISelectOption[];
    contentTypeOptions: ISelectOption[];
    complianceOptions: ISelectOption[];
    countryOptions: ISelectOption[];
  };
  search: string;
  setSearch: (value: string) => void;
}) => {
  const handleMultiSelectChange = (
    key: keyof IFilterData,
    value: TMultiValue<ISelectOption> | TSingleValue<ISelectOption>,
  ) => {
    onFilterChange({ [key]: Array.isArray(value) ? value : [value] });
  };
  return (
    <div className="content-grid content-grid-cols-3 content-gap-3">
      <div className="content-col-span-1">
        <Label htmlFor="searchTopic">Search Topic</Label>
        <Input
          type="text"
          placeholder="Search by topic name or keyword"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="content-w-full"
        />
      </div>

      <div className="content-col-span-1">
        <Label htmlFor="country">Country</Label>
        <CustomSelect
          name="country"
          value={filterData.countryIds}
          handleChange={value => handleMultiSelectChange('countryIds', value)}
          isMulti
          isSearchable
          data={options.countryOptions}
        />
      </div>

      <div className="content-col-span-1">
        <Label htmlFor="compliance">Compliance</Label>
        <CustomSelect
          name="compliance"
          value={filterData.complianceIds}
          handleChange={value =>
            handleMultiSelectChange('complianceIds', value)
          }
          isMulti
          isSearchable
          data={options.complianceOptions}
        />
      </div>

      <div className="content-col-span-1">
        <Label htmlFor="categories">Categories</Label>
        <CustomSelect
          name="categories"
          value={filterData.categoryIds}
          handleChange={value => handleMultiSelectChange('categoryIds', value)}
          isMulti
          data={options.categoryOptions}
          isSearchable
        />
      </div>

      <div className="content-col-span-1">
        <Label htmlFor="contentTypes">Content Types</Label>
        <CustomSelect
          name="contentTypes"
          value={filterData.contentTypeIds}
          handleChange={value =>
            handleMultiSelectChange('contentTypeIds', value)
          }
          isMulti
          data={options.contentTypeOptions}
          isSearchable
        />
      </div>

      <div className="content-col-span-1">
        <Label htmlFor="durationRange">Duration Range</Label>
        <Select
          value={filterData.durationMinutes[0] ?? ''}
          onValueChange={value => onFilterChange({ durationMinutes: [value] })}
        >
          <SelectTrigger>
            <SelectValue placeholder="Duration Range" />
          </SelectTrigger>
          <SelectContent>
            {DURATION_OPTIONS.map(option => (
              <SelectItem key={option.value} value={option.value}>
                {option.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
    </div>
  );
};

export default FilterControls;
