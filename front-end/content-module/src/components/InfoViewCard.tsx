import clsx from 'clsx';
import { Fragment } from 'react';
import { MdOutlineEventAvailable } from 'react-icons/md';

import { CalendarIcon } from 'assets/icons';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  title: string;
  description: string;
  image?: string;
  createdAt?: string;
  updatedAt?: string;
  status: boolean;
  availability?: string;
  courseList?: { courseName: string; courseDescription: string }[];
  chapters?: { chapterName: string; chapterDescription: string }[];
  products?: {
    productId: string;
    productName: string;
    productDescription: string;
  }[];
  features?: { featureName: string }[];
}

const InfoViewCard = ({
  title,
  description,
  image,
  createdAt,
  updatedAt,
  status,
  availability,
  courseList = [],
  features = [],
  chapters = [],
  products = [],
}: IProps) => {
  return (
    <div className="content-flex content-gap-x-5 content-text-cloudy-white">
      <div className={clsx('content-w-full', image && 'content-w-4/5')}>
        <p className="content-text-3xl content-font-semibold">{title}</p>
        <p className="content-mt-4">{description}</p>

        <div className="content-my-8 content-flex content-items-center content-gap-x-16">
          <div className="content-flex content-items-center content-gap-x-2">
            <CalendarIcon fill="#2aa684" />{' '}
            <span>Created Date: {createdAt || 'N/A'}</span>
          </div>
          <div className="content-flex content-items-center content-gap-x-2">
            <CalendarIcon fill="#2aa684" />{' '}
            <span>Updated Date: {updatedAt || 'N/A'}</span>
          </div>
          <div className="content-flex content-items-center content-gap-x-2">
            <div className="content-size-5 content-rounded-full content-border-2 content-border-dashed content-border-primary"></div>
            <span>Status: </span>
            <span
              className={clsx(
                'content-rounded content-bg-opacity-10 content-px-2 content-py-1',
                status
                  ? 'content-bg-success content-text-success'
                  : 'content-bg-vibrant-red content-text-vibrant-red',
              )}
            >
              {status ? 'Enabled' : 'Disabled'}
            </span>
          </div>
          {availability && (
            <div className="content-flex content-items-center content-gap-x-2 content-capitalize">
              <MdOutlineEventAvailable />
              <span>Availability: {availability}</span>
            </div>
          )}
        </div>

        {features.length > 0 && (
          <Fragment>
            <p className="content-text-2xl content-font-medium">
              Assigns features: {features?.length}
            </p>
            <div className="content-mb-7 content-mt-3 content-flex content-gap-3">
              {features.map((feature, index) => (
                <div
                  key={index}
                  className="content-rounded content-bg-light-blue content-bg-opacity-10 content-px-2 content-py-1"
                >
                  {feature?.featureName}
                </div>
              ))}
            </div>
          </Fragment>
        )}

        {courseList.length > 0 && (
          <Fragment>
            <p className="content-text-2xl content-font-medium">
              Assigns courses: {courseList?.length}
            </p>
            <div className="content-mt-3 content-grid content-grid-cols-2 content-gap-5">
              {courseList?.map((course, index) => (
                <div
                  key={index}
                  className="content-rounded content-border content-p-3"
                >
                  <p className="content-mb-2 content-text-xl content-font-semibold">
                    {course.courseName}
                  </p>
                  <p>{course.courseDescription}</p>
                </div>
              ))}
            </div>
          </Fragment>
        )}

        {chapters.length > 0 && (
          <Fragment>
            <p className="content-mt-7 content-text-2xl content-font-medium">
              Assigns chapters: {chapters?.length}
            </p>
            <div className="content-mt-3 content-grid content-grid-cols-2 content-gap-5">
              {chapters?.map((chapter, index) => (
                <div
                  key={index}
                  className="content-rounded content-border content-p-3"
                >
                  <p className="content-mb-2 content-text-xl content-font-semibold">
                    {chapter.chapterName}
                  </p>
                  <p>{chapter.chapterDescription}</p>
                </div>
              ))}
            </div>
          </Fragment>
        )}

        {products.length > 0 && (
          <Fragment>
            <p className="content-mt-7 content-text-2xl content-font-medium">
              Assigns products: {products?.length}
            </p>
            <div className="content-mt-3 content-grid content-grid-cols-2 content-gap-5">
              {products?.map((product, index) => (
                <div
                  key={index}
                  className="content-rounded content-border content-p-3"
                >
                  <p className="content-mb-2 content-text-xl content-font-semibold">
                    {product.productName}
                  </p>
                  <p>{product.productDescription}</p>
                </div>
              ))}
            </div>
          </Fragment>
        )}
      </div>

      {image && (
        <div className="content-w-1/5">
          <img src={FILE_PATH_PREFIX + image} alt={title} />
        </div>
      )}
    </div>
  );
};

export default InfoViewCard;
