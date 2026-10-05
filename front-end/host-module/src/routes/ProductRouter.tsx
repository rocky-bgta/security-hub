import { Navigate, Route, Routes } from 'react-router-dom';

import ProductAnalytics from 'pages/product/ProductAnalytics';
import ProductList from 'pages/product/ProductList';
import { routes } from 'routes/AppRoutes';

const ProductRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="product-list" />} />

      <Route path={routes.productList.path} element={<ProductList />} />
      <Route
        path={routes.productAnalytics.path}
        element={<ProductAnalytics />}
      />
    </Routes>
  );
};

export default ProductRouter;
