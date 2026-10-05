export const generateRSAKey = (): string => {
  const chars =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
  let key = '';
  for (let i = 0; i < 344; i += 1) {
    key += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return key;
};

export const generateEncryptedData = (): string => {
  const chars =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
  let result = '';
  for (let i = 0; i < 1024; i += 1) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
    if (i > 0 && i % 64 === 0) {
      result += '\n';
    }
  }
  return result;
};
