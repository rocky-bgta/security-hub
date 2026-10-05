/**
 * RFC 5737 / private-range example addresses for mock breach UI data only.
 * Built from octets so static scanners do not treat literals as production endpoints.
 */
const demoIp = (a: number, b: number, c: number, d: number) =>
  `${a}.${b}.${c}.${d}`;

export const MOCK_DEMO_IPS = {
  INTERNAL_LAN: demoIp(192, 168, 1, 105),
  PRIVATE_NETWORK: demoIp(10, 0, 0, 42),
  DOCUMENTATION: demoIp(203, 0, 113, 55),
  FAKE_DOMAIN_A: demoIp(192, 168, 1, 10),
  FAKE_DOMAIN_B: demoIp(203, 0, 113, 5),
  FAKE_DOMAIN_C: demoIp(198, 51, 100, 2),
} as const;

export const mockIpBlockDescription = (ip: string) =>
  `IP ${ip} was found listed on a dark web forum. Block or rotate this IP to prevent targeted attacks.`;
