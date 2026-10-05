import { useState } from 'react';
import { IoSearchSharp } from 'react-icons/io5';

import {
  CardToListIcon,
  ContentModuleIcon,
  GlobeIcon,
  ThreeDotIcon,
} from 'assets/icons';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { ISelectOption } from 'models/Input';
import { PUBLIC_URL } from 'utils/Constants';

const StaticImage = PUBLIC_URL + '/images/What-Is-Digital-Forensics.png';

const ContentLibrary = () => {
  const [value, setValue] = useState<string>('');

  const dropdowns: {
    [x: string]: Array<ISelectOption>;
  } = {
    status: [
      { id: 'Published', label: 'Published', value: 'Published' },
      { id: 'Archived', label: 'Archived', value: 'Archived' },
      { id: 'Draft', label: 'Draft', value: 'Draft' },
    ],
  };

  const onChange = (value: string) => {
    setValue(value);
  };

  return (
    <section>
      <h4 className="content-mb-10 content-font-medium">Content Library</h4>
      <div className="content-mb-7 content-grid content-grid-cols-1 content-items-end content-gap-12 md:content-grid-cols-3">
        <div className="content-flex content-flex-col content-gap-y-2.5">
          <label htmlFor="content-type">Content type</label>
          <Select value={value} onValueChange={value => onChange(value)}>
            <SelectTrigger disabled={false}>
              <SelectValue placeholder="Select a Content type" />
            </SelectTrigger>
            <SelectContent>
              {dropdowns['status'].map(option => (
                <SelectItem key={option.id} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="content-flex content-flex-col content-gap-y-2.5">
          <label htmlFor="topics">Topics</label>
          <Select value={value} onValueChange={value => onChange(value)}>
            <SelectTrigger disabled={false}>
              <SelectValue placeholder="Select a Topic" />
            </SelectTrigger>
            <SelectContent>
              {dropdowns['status'].map(option => (
                <SelectItem key={option.id} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="content-flex content-flex-col content-gap-y-2.5">
          <label htmlFor="Search">Search</label>
          <div className="content-flex content-items-center">
            <Input
              id="search"
              placeholder="Search"
              className="content-rounded-r-none content-border-gray-200"
            />
            <IoSearchSharp className="content-h-auto content-w-10 content-rounded-r content-bg-primary content-p-2 content-text-3xl content-text-white" />
          </div>
        </div>
      </div>
      <div className="content-mb-7 content-flex content-items-center content-justify-between">
        <p>2097 Results</p>
        <div className="content-flex content-w-3/5 content-items-center content-justify-end content-gap-x-8">
          <CardToListIcon />
          <Button className="content-py-2">Create Content</Button>
          <Button className="content-bg-gray-50 content-py-2 content-text-[#2E384D] hover:!content-bg-gray-200">
            Download Results
          </Button>
          <Button className="content-bg-gray-50 content-py-2 content-text-[#2E384D] hover:!content-bg-gray-200">
            Clear Filter
          </Button>
          <Select value={value} onValueChange={value => onChange(value)}>
            <SelectTrigger disabled={false}>
              <SelectValue placeholder="Select a Topic" />
            </SelectTrigger>
            <SelectContent>
              {dropdowns['status'].map(option => (
                <SelectItem key={option.id} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
      <section className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-3 lg:content-grid-cols-6">
        {Array.from({ length: 30 }).map((_, idx) => (
          <div
            key={idx}
            className="content-w-72 content-rounded-lg content-bg-white content-shadow-xl"
          >
            <div className="content-relative">
              <img
                src={StaticImage}
                alt="content-image"
                className="content-size-72"
              />
              <p className="content-absolute content-bottom-0 content-w-full content-bg-[#FFAD33] content-py-2 content-text-center content-text-white">
                Upgrade Subscription
              </p>
            </div>
            <h4 className="content-px-3 content-py-4">
              2023 Your Role: internet Security and you
            </h4>
            <div className="content-flex content-items-center content-justify-between content-px-3 content-py-4 content-text-stormy-gray">
              <div className="content-flex content-items-center">
                <ContentModuleIcon />
                <span className="content-ml-2">Training Module</span>
              </div>
              <div className="content-flex content-items-center">
                <GlobeIcon />
                <span className="content-ml-2">43</span>
              </div>
              <ThreeDotIcon />
            </div>
          </div>
        ))}
      </section>
    </section>
  );
};

export default ContentLibrary;
