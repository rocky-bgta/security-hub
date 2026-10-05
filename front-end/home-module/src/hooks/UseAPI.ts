import { useContext } from 'react';

import { APIServiceContext } from 'models/Context';

const useAPI = () => {
  const context = useContext(APIServiceContext);
  if (!context) throw new Error('useAPI must be used inside APIClientProvider');
  return context;
};

export default useAPI;
