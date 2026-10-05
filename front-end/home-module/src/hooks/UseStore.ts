import { useContext } from 'react';

import { StoreContext } from 'models/Context';

const useStore = () => {
  const context = useContext(StoreContext);
  if (!context) throw new Error('useStore must be used inside StoreProvider');
  return context;
};

export default useStore;
