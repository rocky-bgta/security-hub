import { ReactNode, useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

import ClientAdminNavbar from 'components/ClientAdminNavbar';
import ClientAdminSidebar from 'components/ClientAdminSidebar';
import { ClientAdminRoutes } from 'routes/ClientAdminRoutes';
import { PUBLIC_URL } from 'utils/Constants';
import { cn } from 'utils/Helper';

const ThemeBg = PUBLIC_URL + '/images/theme-bg.webp';
const DESKTOP_BREAKPOINT = '(min-width: 1024px)';

interface IProps {
  children: ReactNode;
  hostPath: typeof ClientAdminRoutes;
}

const ClientAdminLayout = ({ children, hostPath }: IProps) => {
  const location = useLocation();
  const [showSidebar, setShowSidebar] = useState<boolean>(
    () =>
      typeof window !== 'undefined' &&
      window.matchMedia(DESKTOP_BREAKPOINT).matches,
  );
  const moduleRoutes = {
    ...ClientAdminRoutes,
    ...hostPath,
  };

  useEffect(() => {
    const media = window.matchMedia(DESKTOP_BREAKPOINT);
    const syncSidebar = () => setShowSidebar(media.matches);

    media.addEventListener('change', syncSidebar);
    return () => media.removeEventListener('change', syncSidebar);
  }, []);

  useEffect(() => {
    if (!window.matchMedia(DESKTOP_BREAKPOINT).matches) {
      setShowSidebar(false);
    }
  }, [location.pathname]);

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
        <div className="home-relative home-flex home-min-h-0 home-flex-1 home-overflow-hidden">
          {showSidebar && (
            <button
              type="button"
              aria-label="Close sidebar"
              className="home-absolute home-inset-0 home-z-20 home-bg-black/40 lg:home-hidden"
              onClick={() => setShowSidebar(false)}
            />
          )}
          <aside
            className={cn(
              'home-h-full home-min-w-0 home-overflow-x-hidden home-overflow-y-auto home-transition-all home-duration-300 home-ease-in-out',
              'max-lg:home-absolute max-lg:home-inset-y-0 max-lg:home-left-0 max-lg:home-z-30 max-lg:home-bg-deep-ocean',
              showSidebar ? 'home-w-72' : 'home-w-0',
            )}
          >
            <ClientAdminSidebar />
          </aside>
          <main
            className={cn(
              'home-hide-scrollbar-mobile home-min-w-0 home-w-full home-space-y-4 home-overflow-y-auto home-p-3 home-transition-all home-duration-300 home-ease-in-out md:home-p-4',
              showSidebar ? 'lg:home-w-[calc(100%-18rem)]' : 'lg:home-w-full',
            )}
          >
            {children}
          </main>
        </div>
      </div>
    </div>
  );
};

export default ClientAdminLayout;
