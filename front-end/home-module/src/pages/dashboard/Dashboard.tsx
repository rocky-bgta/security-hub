import useAuth from 'hooks/UseAuth';
import AspireAdminDashboard from 'pages/dashboard/AspireAdminDashboard';
import ClientAdminDashboard from 'pages/dashboard/ClientAdminDashboard';
import ClientUserDashboard from 'pages/dashboard/ClientUserDashboard';
import MspAdminDashboard from 'pages/dashboard/MspAdminDashboard';
import { routes } from 'routes/Route';
import ROLE from 'utils/Role';

interface IProps {
  hostPath?: typeof routes;
}

const Dashboard = ({ hostPath }: IProps) => {
  const { role } = useAuth();
  const authenticatedRoutes = {
    ...routes,
    ...hostPath,
  };

  if (role === ROLE.SUPER_ADMIN) {
    return <AspireAdminDashboard />;
  }

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminDashboard />;
  }

  if (role === ROLE.MSP_ADMIN) {
    return <MspAdminDashboard />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminDashboard />;
  }

  if (role === ROLE.CLIENT_USER) {
    return <ClientUserDashboard hostPath={authenticatedRoutes} />;
  }
};

export default Dashboard;
