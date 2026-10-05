import { ReactNode } from 'react';

import CourseHeader from 'features/layout/CourseHeader';

interface IProps {
  children: ReactNode;
}

const CourseLayout = ({ children }: IProps) => {
  return (
    <section className="content-w-full">
      <CourseHeader />
      {children}
    </section>
  );
};

export default CourseLayout;
