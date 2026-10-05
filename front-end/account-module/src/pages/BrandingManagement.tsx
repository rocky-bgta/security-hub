import React from 'react';
import NotFound from './NotFound';
import { ROLE } from 'utils/Role';
import { useAuth } from 'hooks/UseAuth';
import ClientAdminBrandingManagement from 'features/client-admin/BrandingManagement';

const BrandingManagement = () => {
  const { role } = useAuth();
  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminBrandingManagement />;
  }
  if (role === ROLE.MSP_ADMIN) {
    return <div> Coming Soon </div>;
  }
  return <NotFound />;
};

export default BrandingManagement;
