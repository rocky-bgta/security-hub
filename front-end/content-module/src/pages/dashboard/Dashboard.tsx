import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

import { useAuth } from 'hooks/UseAuth';
import ClientUserDashboard from 'pages/dashboard/ClientUserDashboard';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

interface IProps {
  hostPath?: typeof routes;
}

const Dashboard = ({ hostPath }: IProps) => {
  const { role } = useAuth();
  const navigate = useNavigate();

  const moduleRoutes = {
    ...routes,
    ...hostPath,
  };

  useEffect(() => {
    if (role === ROLE.SUPER_ADMIN) {
      navigate(routes.courseList.path, { replace: true });
    }

    if (role === ROLE.ASPIRE_ADMIN) {
      navigate(routes.addContent.path, { replace: true });
    }

    if (role === ROLE.CLIENT_ADMIN) {
      navigate(routes.productList.path, { replace: true });
    }
  }, [role, navigate]);

  if (role === ROLE.CLIENT_USER) {
    return <ClientUserDashboard hostPath={moduleRoutes} />;
  }
};

export default Dashboard;
