import { zodResolver } from '@hookform/resolvers/zod';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { ICourse } from 'models/Course';
import { IFeatureDetails } from 'models/Feature';
import { IList, IResponse, Status } from 'models/Global';
import { IPackage } from 'models/Package';
import { Fragment, useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  DefaultPackageFormValues,
  PackageFormSchema,
  TPackageFormFields,
} from 'schemas/PackageSchema';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

interface IProps {
  packageId?: string;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
}

const StatusOptions = [
  { id: '1', label: 'Enable', value: Status.ENABLED },
  { id: '2', label: 'Disable', value: Status.DISABLED },
];

const PackageModal = ({ isOpen, onSubmit, packageId, onClose }: IProps) => {
  const {
    control,
    reset,
    clearErrors,
    handleSubmit,
    formState: { errors },
  } = useForm<TPackageFormFields>({
    resolver: zodResolver(PackageFormSchema),
    defaultValues: DefaultPackageFormValues,
    mode: 'onChange',
  });
  const [courseSearch, setCourseSearch] = useState<string>('');
  const [featureSearch, setFeatureSearch] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [courses, setCourses] = useState<IList<ICourse>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [features, setFeatures] = useState<IList<IFeatureDetails>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });

  const apiClient = useAPI();

  const fetchCourses = async (setCourses: (data: IList<ICourse>) => void) => {
    try {
      const response: IResponse<IList<ICourse>> = await apiClient.get(
        API_END_POINTS.COURSE_ENABLED +
          objectToQueryString({ offset: 0, pageSize: 1000 }),
      );
      setCourses(response.data);
    } catch (error) {
      console.error('Error fetching courses:', error);
    }
  };

  const fetchFeatures = async (
    setFeatures: (data: IList<IFeatureDetails>) => void,
  ) => {
    try {
      const response: IResponse<IList<IFeatureDetails>> = await apiClient.get(
        API_END_POINTS.FEATURE_ENABLED +
          objectToQueryString({ offset: 0, pageSize: 1000 }),
      );
      setFeatures(response.data);
    } catch (error) {
      console.error('Error fetching features:', error);
    }
  };

  useEffect(() => {
    if (!isOpen) return;

    clearErrors();

    fetchCourses(setCourses);
    fetchFeatures(setFeatures);
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen || !packageId) return;

    fetchPackageDetails();
  }, [packageId, isOpen]);

  const fetchPackageDetails = async () => {
    try {
      const response: IResponse<IPackage> = await apiClient.get(
        API_END_POINTS.PACKAGE_DETAILS + packageId,
      );
      reset({
        packageName: response.data.packageName,
        packageStatus: response.data.packageStatus,
        price: response.data.price,
        packageDescription: response.data.packageDescription,
        courseIds: response.data.courseIds.map(course => ({
          id: course.id,
          courseName: course.courseName,
        })),
      });
    } catch (error) {
      console.error('Error fetching package details:', error);
    }
  };

  const handleFormSubmit = async (fields: TPackageFormFields) => {
    setSubmitting(true);
    const payload = {
      ...fields,
      courseIds: fields.courseIds?.map(course => course.id),
      featureIds: fields.featureIds?.map(feature => feature.id),
    };

    let response;

    try {
      if (packageId) {
        response = await apiClient.put(
          API_END_POINTS.PACKAGE_UPDATE + packageId,
          { data: payload },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.PACKAGE_CREATE, {
          data: payload,
        });
      }

      if ([200, 201].includes(response.statusCode)) {
        handleClose();
        onSubmit();
        toast.success(response.message);
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error submitting form:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    reset(DefaultPackageFormValues);
    setCourseSearch('');
    setFeatureSearch('');
    onClose();
  };

  const filteredCourses = courses?.items?.filter(course =>
    course.courseName.toLowerCase().includes(courseSearch.toLowerCase()),
  );
  const filteredFeatures = features?.items?.filter(course =>
    course.featureName.toLowerCase().includes(featureSearch.toLowerCase()),
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      disableOutsideClick={true}
      className="content-h-auto content-w-3/4"
    >
      <ModalHeader onClose={handleClose}>
        <p className="content-py-5 content-text-lg content-font-medium">
          {packageId ? 'Edit ' : 'Create '} Package
        </p>
      </ModalHeader>
      <ModalBody>
        <form
          onSubmit={handleSubmit(handleFormSubmit)}
          className="content-my-6 content-grid content-grid-cols-1 content-gap-3 lg:content-grid-cols-2 lg:content-gap-x-10 lg:content-gap-y-6"
        >
          <div className="content-flex content-flex-col">
            <Controller
              name="packageName"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="title">Package Name</Label>
                  <Input
                    key="title"
                    id="title"
                    name="Package Title"
                    value={value}
                    placeholder="Package Title"
                    className={errors['packageName'] && 'content-has-error'}
                    onChange={e => onChange(e.target.value)}
                  />
                </Fragment>
              )}
            />
            {errors['packageName'] && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors['packageName']?.message}
              </p>
            )}
          </div>

          <div className="content-flex content-flex-col">
            <Controller
              name="packageStatus"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="status">Package Status</Label>{' '}
                  <Select
                    value={value}
                    onValueChange={value => onChange(value)}
                  >
                    <SelectTrigger disabled={false}>
                      <SelectValue placeholder="Select a Status" />
                    </SelectTrigger>
                    <SelectContent>
                      {StatusOptions.map(option => (
                        <SelectItem key={option.id} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </Fragment>
              )}
            />
            {errors['packageStatus'] && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors['packageStatus']?.message}
              </p>
            )}
          </div>

          <Controller
            name="featureIds"
            control={control}
            render={({ field: { onChange, value } }) => (
              <div>
                <Label>Add Features (optional)</Label>
                <div className="content-mt-1 content-rounded content-border content-border-soft-blue-gray content-p-2.5">
                  <Input
                    key="search"
                    id="search"
                    placeholder="Search..."
                    value={featureSearch}
                    onChange={e => setFeatureSearch(e.target.value)}
                    className="content-mb-3 content-w-full content-rounded content-border content-p-2"
                  />

                  {(value || []).length > 0 && (
                    <div className="content-mb-2 content-flex content-flex-wrap content-items-center content-gap-1">
                      <span className="content-text-sm">Selected feature:</span>
                      {(value || []).map(feature => (
                        <span
                          key={feature.id}
                          className="content-rounded content-bg-gray-200 content-px-2 content-py-1 content-text-sm"
                        >
                          {feature.featureName}
                        </span>
                      ))}
                    </div>
                  )}

                  <div className="content-h-72 content-overflow-y-scroll content-rounded content-border content-border-soft-blue-gray content-p-2">
                    {filteredFeatures?.map(feature => (
                      <label
                        key={feature.id}
                        htmlFor={feature.id}
                        className="content-mb-2 content-flex content-cursor-pointer content-items-center content-gap-x-3 content-rounded content-border content-p-2"
                      >
                        <Checkbox
                          id={feature.id}
                          checked={!!value?.find(c => c.id === feature.id)}
                          onCheckedChange={checked => {
                            if (checked) {
                              onChange([...(value || []), feature]);
                            } else {
                              onChange(
                                (value || []).filter(c => c.id !== feature.id),
                              );
                            }
                          }}
                        />
                        {feature.featureName}
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            )}
          />

          <Controller
            name="courseIds"
            control={control}
            render={({ field: { onChange, value } }) => (
              <div>
                <Label>Assign Courses (optional)</Label>
                <div className="content-mt-1 content-rounded content-border content-border-soft-blue-gray content-p-2.5">
                  <Input
                    key="search"
                    id="search"
                    placeholder="Search..."
                    value={courseSearch}
                    onChange={e => setCourseSearch(e.target.value)}
                    className="content-mb-3 content-w-full content-rounded content-border content-p-2"
                  />

                  {(value || []).length > 0 && (
                    <div className="content-mb-2 content-flex content-flex-wrap content-items-center content-gap-1">
                      <span className="content-text-sm">Selected Courses:</span>
                      {(value || []).map(course => (
                        <span
                          key={course.id}
                          className="content-rounded content-bg-gray-200 content-px-2 content-py-1 content-text-sm"
                        >
                          {course.courseName}
                        </span>
                      ))}
                    </div>
                  )}

                  <div className="content-h-72 content-overflow-y-scroll content-rounded content-border content-border-soft-blue-gray content-p-2">
                    {filteredCourses?.map(course => (
                      <label
                        key={course.id}
                        htmlFor={course.id}
                        className="content-mb-2 content-flex content-cursor-pointer content-items-center content-gap-x-3 content-rounded content-border content-p-2"
                      >
                        <Checkbox
                          id={course.id}
                          checked={!!value?.find(c => c.id === course.id)}
                          onCheckedChange={checked => {
                            if (checked) {
                              onChange([...(value || []), course]);
                            } else {
                              onChange(
                                (value || []).filter(c => c.id !== course.id),
                              );
                            }
                          }}
                        />
                        {course.courseName}
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            )}
          />

          <div className="content-flex content-flex-col">
            <Controller
              name="price"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="price">Price</Label>
                  <Input
                    key="price"
                    type="number"
                    id="price"
                    name="price"
                    value={value}
                    placeholder="Price"
                    className={errors['price'] && 'content-has-error'}
                    onChange={e => onChange(e.target.value)}
                  />
                </Fragment>
              )}
            />
            {errors['price'] && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors['price']?.message}
              </p>
            )}
          </div>

          <div className="content-flex content-flex-col">
            <Controller
              name="packageDescription"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="description">
                    Package Description (optional)
                  </Label>
                  <textarea
                    key="description"
                    id="description"
                    name="description"
                    value={value}
                    placeholder="Package Description"
                    className="content-h-full content-rounded content-border content-border-soft-blue-gray content-px-2.5 content-py-2 placeholder:content-text-sm"
                    onChange={e => onChange(e.target.value)}
                  />
                </Fragment>
              )}
            />
          </div>

          <div className="content-my-10 content-flex content-justify-center content-gap-x-20 lg:content-col-span-2">
            <Button
              type="button"
              onClick={onClose}
              className="content-flex content-w-40 content-items-center content-justify-center content-gap-x-3 content-rounded content-border content-border-soft-blue-gray !content-bg-white content-py-3 content-text-secondary"
            >
              Discard
            </Button>

            <Button
              type="submit"
              className="content-flex content-w-40 content-items-center content-justify-center content-gap-x-3 content-rounded content-bg-light-blue content-py-3 content-text-white"
            >
              {submitting ? 'Saving...' : 'Save'}
            </Button>
          </div>
        </form>
      </ModalBody>
    </Modal>
  );
};

export default PackageModal;
