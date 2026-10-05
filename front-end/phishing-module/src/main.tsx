import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import { BrowserRouter } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import { AuthProvider } from 'providers/AuthProvider';
import { APIClientProvider } from 'providers/APIClientProvider';
import { StoreProvider } from 'providers/StoreProvider';

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
