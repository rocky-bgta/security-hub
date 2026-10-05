import { zodResolver } from '@hookform/resolvers/zod';
import clsx from 'clsx';
import { useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';

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
import { IList, IResponse, Status } from 'models/Global';
import { IProductDetails } from 'models/Product';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  DefaultProductFormValues,
  ProductFormFields,
  ProductFormSchema,
  TProductFormFieldKeys,
  TProductFormFields,
} from 'schemas/ProductSchema';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

interface IProps {
  productId?: string;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
}

const ProductModal = ({
  productId = '',
  isOpen,
  onClose,
  onSubmit,
}: IProps) => {
  const {
    control,
    reset,
    clearErrors,
    handleSubmit,
    formState: { errors },
  } = useForm<TProductFormFields>({
    resolver: zodResolver(ProductFormSchema),
    defaultValues: DefaultProductFormValues,
    mode: 'onChange',
  });

  const [search, setSearch] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [courses, setCourses] = useState<IList<ICourse>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });

  const apiClient = useAPI();

  useEffect(() => {
    if (!isOpen) return;

    clearErrors();
    fetchEnabledCourses();
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen || !productId) return;

    fetchProductDetails();
  }, [productId, isOpen]);

  const fetchEnabledCourses = async () => {
    try {
      const response: IResponse<IList<ICourse>> = await apiClient.get(
        API_END_POINTS.COURSE_ENABLED +
          objectToQueryString({
            offset: 0,
            pageSize: 1000,
          }),
      );
      setCourses({
        ...courses,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching enabled courses:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchProductDetails = async () => {
    try {
      const response: IResponse<IProductDetails> = await apiClient.get(
        API_END_POINTS.PRODUCT_DETAILS + productId,
      );
      reset(response.data);
    } catch (error) {
      console.error('Error fetching product data:', error);
    } finally {
      setLoading(false);
    }
  };

  const StatusOptions = [
    { id: '1', label: 'Enable', value: Status.ENABLED },
    { id: '2', label: 'Disable', value: Status.DISABLED },
  ];

  const handleFormSubmit = async (fields: TProductFormFields) => {
    setSubmitting(true);

    const payload = {
      ...fields,
      courseIds: fields.courseIds?.map(course => course.id),
    };

    let response;
    try {
      if (productId) {
        response = await apiClient.put(
          API_END_POINTS.PRODUCT_UPDATE + productId,
          { data: payload },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.PRODUCT_CREATE, {
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
      console.error('Error creating/updating product:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const filteredCourses = courses?.items?.filter(course =>
    course.courseName.toLowerCase().includes(search.toLowerCase()),
  );

  if (loading) {
    return null;
  }

  const handleClose = () => {
    reset(DefaultProductFormValues);
    setSearch('');
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      disableOutsideClick={true}
      className="content-h-auto content-w-3/4"
    >
      <ModalHeader onClose={handleClose}>
        <p className="content-py-5 content-text-lg content-font-medium">
          {productId ? 'Edit ' : 'Create '} Product
        </p>
      </ModalHeader>
      <ModalBody>
        <form
          onSubmit={handleSubmit(handleFormSubmit)}
          className="content-my-6 content-grid content-grid-cols-1 content-gap-3 lg:content-grid-cols-2 lg:content-gap-x-10 lg:content-gap-y-6"
        >
          {ProductFormFields.map((field, idx) => (
            <div key={idx} className="content-flex content-flex-col">
              <Controller
                name={field.key as TProductFormFieldKeys}
                control={control}
                render={({ field: { onChange, value } }) => {
                  if (field.type === 'select') {
                    return (
                      <>
                        <Label htmlFor={field.key}>{field.name}</Label>
                        <Select
                          value={value as string}
                          onValueChange={value => onChange(value)}
                        >
                          <SelectTrigger disabled={false}>
                            <SelectValue placeholder={field.placeholder} />
                          </SelectTrigger>
                          <SelectContent>
                            {StatusOptions.map(option => (
                              <SelectItem key={option.id} value={option.value}>
                                {option.label}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </>
                    );
                  }

                  if (field.type === 'textarea') {
                    return (
                      <>
                        <Label htmlFor={field.key}>{field.name}</Label>
                        <textarea
                          key={field.key}
                          id={field.key}
                          name={field.name}
                          value={value as string}
                          placeholder={field.placeholder}
                          className={clsx(
                            'content-h-full content-rounded content-border content-border-soft-blue-gray content-px-2.5 content-py-2 placeholder:content-text-sm',
                            errors[field.key as TProductFormFieldKeys] &&
                              'content-has-error',
                          )}
                          onChange={e => onChange(e.target.value)}
                        />
                      </>
                    );
                  }

                  return (
                    <>
                      <Label htmlFor={field.key}>{field.name}</Label>
                      <Input
                        key={field.key}
                        id={field.key}
                        name={field.name}
                        value={value as string}
                        placeholder={field.placeholder}
                        className={
                          errors[field.key as TProductFormFieldKeys] &&
                          'content-has-error'
                        }
                        onChange={e => onChange(e.target.value)}
                      />
                    </>
                  );
                }}
              />
              {errors[field.key as TProductFormFieldKeys] && (
                <p className="content-text-sm content-text-vibrant-red">
                  {errors[field.key as TProductFormFieldKeys]?.message}
                </p>
              )}
            </div>
          ))}

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
                    value={search}
                    onChange={e => setSearch(e.target.value)}
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

          <div className="content-my-10 content-flex content-justify-center content-gap-x-20 lg:content-col-span-2">
            <Button
              onClick={handleClose}
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

export default ProductModal;
