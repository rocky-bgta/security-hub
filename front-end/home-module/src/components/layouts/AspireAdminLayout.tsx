import { ReactNode, useState } from 'react';

import AspireAdminNavbar from 'components/AspireAdminNavbar';
import AspireAdminSidebar from 'components/AspireAdminSidebar';
import { AspireAdminRoutes } from 'routes/AspireAdminRoutes';
import { PUBLIC_URL } from 'utils/Constants';
import { cn } from 'utils/Helper';

const ThemeBg = PUBLIC_URL + '/images/theme-bg.webp';

interface IProps {
  children: ReactNode;
  hostPath: typeof AspireAdminRoutes;
}

const AspireAdminLayout = ({ children, hostPath }: IProps) => {
  const [showSidebar, setShowSidebar] = useState<boolean>(true);
  const moduleRoutes = {
    ...AspireAdminRoutes,
    ...hostPath,
  };

  return (
    <div className="home-relative home-min-h-screen home-w-full">
      <div
        className="home-fixed home-inset-0 home-z-0 home-bg-cover home-bg-center"
        style={{ backgroundImage: `url(${ThemeBg})` }}
      />

      <div className="home-relative home-z-10 home-flex home-h-screen home-flex-col">
        <AspireAdminNavbar
          hostPath={moduleRoutes}
          setShowSidebar={setShowSidebar}
        />

        <div className="home-flex home-overflow-hidden">
          <aside
            className={cn(
              'home-h-full home-overflow-y-auto home-transition-all home-duration-300 home-ease-in-out',
              showSidebar ? 'home-w-80' : 'home-w-0',
            )}
          >
            <AspireAdminSidebar />
          </aside>

          <main
            className={cn(
              'home-hide-scrollbar-mobile home-w-full home-space-y-4 home-overflow-y-auto home-p-3 home-transition-all home-duration-300 home-ease-in-out md:home-p-4 lg:home-p-4',
              showSidebar ? 'lg:home-w-[calc(100%-20rem)]' : 'lg:home-w-full',
            )}
          >
            {children}
          </main>
        </div>
      </div>
    </div>
  );
};

export default AspireAdminLayout;
