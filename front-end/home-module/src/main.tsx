import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';

import APIClientProvider from 'providers/APIClientProvider';
import AuthProvider from 'providers/AuthProvider';
import StoreProvider from 'providers/StoreProvider';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <APIClientProvider>
          <StoreProvider>
            <App />
          </StoreProvider>
        </APIClientProvider>
      </AuthProvider>
      <ToastContainer aria-label="Toast Container" />
    </BrowserRouter>
  </React.StrictMode>,
);
