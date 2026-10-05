class ROLE {
  static SUPER_ADMIN = 'SUPER_ADMIN';
  static ASPIRE_ADMIN = 'ASPIRE_ADMIN';
  static MSP_ADMIN = 'MSP';
  static CLIENT_ADMIN = 'CLIENT_ADMIN';
  static CLIENT_USER = 'USER';
  static FINANCE_ADMIN = 'FINANCE_ADMIN';
  static NONE = 'none';
}

export const roleList = [
  ROLE.SUPER_ADMIN,
  ROLE.ASPIRE_ADMIN,
  ROLE.MSP_ADMIN,
  ROLE.CLIENT_ADMIN,
  ROLE.CLIENT_USER,
  ROLE.FINANCE_ADMIN,
];

export default ROLE;
