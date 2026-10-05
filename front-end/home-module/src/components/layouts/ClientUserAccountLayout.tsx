import { ReactNode } from 'react';
import { Fragment } from 'react/jsx-runtime';

import { Card } from 'common/Card';
import ClientUserAccountSidebar from 'components/ClientUserAccountSidebar';
import { ClientUserRoutes } from 'routes/ClientUserRoutes';
import { routes } from 'routes/Route';

interface IProps {
  children: ReactNode;
  hostPath: typeof ClientUserRoutes;
}

const ClientUserAccountLayout = ({ children, hostPath }: IProps) => {
  const moduleRoutes = {
    ...routes,
    ...hostPath,
  };

  return (
    <Fragment>
      <h2 className="home-mb-6 home-text-2xl home-text-white">Accounts</h2>
      <Card>
        <div className="home-grid home-grid-cols-12">
          <div className="home-col-span-12 home-h-full home-border-r home-border-secondary home-p-4 md:home-col-span-2">
            <ClientUserAccountSidebar hostPath={moduleRoutes} />
          </div>
          <div className="home-col-span-12 home-p-6 md:home-col-span-10">
            {children}
          </div>
        </div>
      </Card>
    </Fragment>
  );
};

export default ClientUserAccountLayout;
