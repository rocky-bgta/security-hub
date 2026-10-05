import ClientAdminProductList from 'features/product/client-admin/ProductList';
import SuperAdminProductList from 'features/product/SuperAdminProductList';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import NotFound from '../NotFound';
import MSPAdminProductList from 'features/product/msp-admin/ProductList';

const ProductList = () => {
  const { role } = useAuth();

  if (role === ROLE.SUPER_ADMIN) {
    return <SuperAdminProductList />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminProductList />;
  }

  if (role === ROLE.ASPIRE_ADMIN) {
    return <SuperAdminProductList />;
  }

  if (role === ROLE.MSP_ADMIN) {
    return <MSPAdminProductList />;
  }

  return <NotFound />;
};

export default ProductList;
