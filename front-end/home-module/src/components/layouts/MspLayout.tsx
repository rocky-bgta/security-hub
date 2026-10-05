import { ReactNode, useState } from 'react';

import { PUBLIC_URL } from 'utils/Constants';
import { cn } from 'utils/Helper';
import { MspRoutes } from 'routes/MspRoutes';
import MspNavbar from 'components/MspNavbar';
import MspSidebar from 'components/MspSidebar';

const ThemeBg = PUBLIC_URL + '/images/theme-bg.webp';

interface IProps {
  children: ReactNode;
  hostPath: typeof MspRoutes;
}

const MspLayout = ({ children, hostPath }: IProps) => {
  const [showSidebar, setShowSidebar] = useState<boolean>(true);
  const moduleRoutes = {
    ...MspRoutes,
    ...hostPath,
  };

  return (
    <div className="home-relative home-min-h-screen home-w-full">
      <div
        className="home-fixed home-inset-0 home-z-0 home-bg-cover home-bg-center"
        style={{ backgroundImage: `url(${ThemeBg})` }}
      />
      <div className="home-relative home-z-10 home-flex home-h-screen home-flex-col">
        <MspNavbar
          hostPath={moduleRoutes}
          setShowSidebar={setShowSidebar}
        />
        <main className="home-flex home-overflow-hidden">
          <aside
            className={cn(
              'home-h-full home-overflow-y-auto home-transition-all home-duration-300 home-ease-in-out',
              showSidebar ? 'home-w-72' : 'home-w-0',
            )}
          >
            <MspSidebar />
          </aside>
          <main
            className={cn(
              'home-space-y-4 home-overflow-y-auto home-p-4 home-transition-all home-duration-300 home-ease-in-out',
              showSidebar ? 'home-w-[calc(100%-18rem)]' : 'home-w-full',
            )}
          >
            {children}
          </main>
        </main>
      </div>
    </div>
  );
};

export default MspLayout;
