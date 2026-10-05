import { useAuth } from 'hooks/UseAuth';
import ClientUserPackageList from 'pages/package/ClientUserList';
import SuperAdminPackageList from 'pages/package/SuperAdminList';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

interface IProps {
  hostPath?: typeof routes;
}

const PackageList = ({ hostPath }: IProps) => {
  const { role } = useAuth();
  const moduleRoutes = {
    ...routes,
    ...hostPath,
  };

  if (role === ROLE.SUPER_ADMIN) {
    return <SuperAdminPackageList />;
  }

  if (role === ROLE.CLIENT_USER) {
    return <ClientUserPackageList hostPath={moduleRoutes} />;
  }
};

export default PackageList;
