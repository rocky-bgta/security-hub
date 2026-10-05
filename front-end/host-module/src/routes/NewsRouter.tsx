import { Navigate, Route, Routes } from 'react-router-dom';

import ArchivedNews from 'pages/news/ArchivedNews';
import CreateNews from 'pages/news/CreateNews';
import ManageNews from 'pages/news/ManageNews';
import NewsAnalytics from 'pages/news/NewsAnalytics';
import { routes } from 'routes/AppRoutes';

const NewsRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="manage-news" />} />

      <Route path={routes.latestNews.path} element={<ManageNews />} />
      <Route path={routes.createNews.path} element={<CreateNews />} />
      <Route path={routes.newsAnalytics.path} element={<NewsAnalytics />} />
      <Route path={routes.archivedNews.path} element={<ArchivedNews />} />
    </Routes>
  );
};

export default NewsRouter;
