import { ReactNode, useState } from 'react';

import ClientAdminNavbar from 'components/ClientAdminNavbar';
import { ClientAdminRoutes } from 'routes/ClientAdminRoutes';
import { PUBLIC_URL } from 'utils/Constants';
import { cn } from 'utils/Helper';

const ThemeBg = PUBLIC_URL + '/images/theme-bg.webp';

interface IProps {
  children: ReactNode;
}

const SelfClientAdminLayout = ({ children }: IProps) => {
  const [_showSidebar, setShowSidebar] = useState<boolean>(false);
  const moduleRoutes = {
    ...ClientAdminRoutes,
  };

  return (
    <div className="home-relative home-min-h-screen home-w-full">
      <div
        className="home-fixed home-inset-0 home-z-0 home-bg-cover home-bg-center"
        style={{ backgroundImage: `url(${ThemeBg})` }}
      />
      <div className="home-relative home-z-10 home-flex home-h-screen home-flex-col">
        <ClientAdminNavbar
          hostPath={moduleRoutes}
          setShowSidebar={setShowSidebar}
        />
        <main className="home-flex home-overflow-hidden">
          <main
            className={cn(
              'home-w-full home-space-y-4 home-overflow-y-auto home-p-4 home-transition-all home-duration-300 home-ease-in-out',
            )}
          >
            {children}
          </main>
        </main>
      </div>
    </div>
  );
};

export default SelfClientAdminLayout;
