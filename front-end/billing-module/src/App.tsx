import { Suspense } from 'react';

import Loader from 'common/loader/Loader';
import { ToastContainer } from 'react-toastify';
import AppRouter from 'routes/AppRouter';

function App() {
  return (
    <Suspense fallback={<Loader />}>
      <ToastContainer />
      <AppRouter />
    </Suspense>
  );
}

export default App;
