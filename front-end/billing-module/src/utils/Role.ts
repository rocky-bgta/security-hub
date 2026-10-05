import RemoteRoleConstant from 'home-module/ROLE';

interface IRoleConst {
  SUPER_ADMIN: string;
  ASPIRE_ADMIN: string;
  MSP_ADMIN: string;
  CLIENT_ADMIN: string;
  CLIENT_USER: string;
  FINANCE_ADMIN: string;
}

const ROLE: IRoleConst = {
  SUPER_ADMIN: RemoteRoleConstant.SUPER_ADMIN,
  ASPIRE_ADMIN: RemoteRoleConstant.ASPIRE_ADMIN,
  MSP_ADMIN: RemoteRoleConstant.MSP_ADMIN,
  CLIENT_ADMIN: RemoteRoleConstant.CLIENT_ADMIN,
  CLIENT_USER: RemoteRoleConstant.CLIENT_USER,
  FINANCE_ADMIN: RemoteRoleConstant.FINANCE_ADMIN,
};

export { ROLE };
