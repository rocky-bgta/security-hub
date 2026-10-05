import { Route, Routes } from 'react-router-dom';

import SupportManagement from 'pages/support';
import PendingTickets from 'pages/support/PendingTickets';
import ResolvedTickets from 'pages/support/ResolvedTickets';
import SupportTickets from 'pages/support/SupportTickets';
import TicketLibrary from 'pages/support/TicketLibrary';
import { routes } from 'routes/AppRoutes';

const SupportRouter = () => {
  return (
    <Routes>
      <Route index element={<SupportManagement />} />

      <Route path={routes.ticketLibrary.path} element={<TicketLibrary />} />
      <Route path={routes.pendingTickets.path} element={<PendingTickets />} />
      <Route path={routes.resolvedTickets.path} element={<ResolvedTickets />} />

      <Route path={routes.supportTickets.path} element={<SupportTickets />} />
    </Routes>
  );
};

export default SupportRouter;
