import { Route, Routes } from 'react-router-dom';

import ContentManagement from 'pages/content';
import CourseCards from 'pages/content/CourseCards';
import CourseChapters from 'pages/content/CourseChapters';
import CourseList from 'pages/content/CourseList';
import PackageList from 'pages/content/PackageList';
import ProductList from 'pages/product/ProductList';
import { routes } from 'routes/AppRoutes';

const ContentRouter = () => {
  return (
    <Routes>
      <Route index element={<ContentManagement />} />

      <Route path={routes.addContent.path} element={<CourseCards />} />
      <Route path={routes.courseList.path} element={<CourseList />} />
      <Route path={routes.productList.path} element={<ProductList />} />
      <Route path={routes.packageList.path} element={<PackageList />} />
      <Route path={routes.courseChapters.path} element={<CourseChapters />} />
    </Routes>
  );
};

export default ContentRouter;
