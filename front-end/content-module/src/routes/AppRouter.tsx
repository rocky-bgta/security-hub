import { Navigate, Route, Routes } from 'react-router-dom';

import BaseLayout from 'components/layouts/BaseLayout';
import ClientUserAccountLayout from 'components/layouts/ClientUserAccountLayout';
import CourseLayout from 'components/layouts/CourseLayout';
import RoleBasedProtectedRoute from 'components/layouts/RoleBasedProtectedRoute';
import CourseChapters from 'features/chapter/CourseWiseChapters';
import ChangePassword from 'pages/account/ChangePassword';
import MyProfile from 'pages/account/MyProfile';
import ProfileSettings from 'pages/account/ProfileSettings';
import BookMarks from 'pages/BookMarks';
import Campaigns from 'pages/Campaigns';
import CertificateAnalytics from 'pages/certificate/CertificateAnalytics';
import CertificateHistory from 'pages/certificate/CertificateHistory';
import CertificateList from 'pages/certificate/CertificateList';
import CertificateTemplates from 'pages/certificate/CertificateTemplates';
import IndividualClientSubPackages from 'pages/client/SubPackage';
import ClientViewDetails from 'pages/client/ViewDetails';
import ContentView from 'pages/content/ContentView';
import CourseCardList from 'pages/course/Cards';
import CourseComplete from 'pages/course/CourseComplete';
import CourseList from 'pages/course/List';
import CourseViewDetails from 'pages/course/ViewDetails';
import Dashboard from 'pages/dashboard/Dashboard';
import Analytics from 'pages/exam/Analytics';
import BulkImport from 'pages/exam/BulkImport';
import CreateExam from 'pages/exam/CreateExam';
import ExamineeReports from 'pages/exam/ExamineeReports';
import ExamLibrary from 'pages/exam/ExamLibrary';
import ExamSettings from 'pages/exam/ExamSettings';
import ExamsList from 'pages/exam/ExamsList';
import ExamResults from 'pages/ExamResults';
import FeatureList from 'pages/FeatureList';
import Login from 'pages/Login';
import NotFound from 'pages/NotFound';
import AssignedPackages from 'pages/package/AssignedPackages';
import AvailablePackages from 'pages/package/AvailablePackages';
import PackageList from 'pages/package/List';
import PackageLicense from 'pages/package/PackageLicense';
import PerformanceReport from 'pages/package/PerformanceReport';
import SubPackages from 'pages/package/SubPackages';
import PhishingList from 'pages/PhishingList';
import ProductAnalytics from 'pages/product/ProductAnalytics';
import ProductList from 'pages/product/ProductList';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';
import UserActivityLogs from 'pages/UserActivityLogs';
import UserNotification from 'pages/UserNotification';
import MFASetup from 'pages/MFASetup';
import MFAVerify from 'pages/MFAVerify';
import ClientLicenseHistory from 'pages/package/ClientLicenseHistory';
import MSPLicenseHistory from 'pages/package/aspire-admin/MSPLicenseHistory';
import CertificateIssued from 'pages/certificate/CertificateIssued';
import TakeExam from 'pages/TakeExam';

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<BaseLayout />}>
        <Route index element={<Dashboard />} />
        <Route
          path={routes.courseList.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[
                ROLE.SUPER_ADMIN,
                ROLE.CLIENT_USER,
                ROLE.CLIENT_ADMIN,
                ROLE.ASPIRE_ADMIN,
              ]}
            >
              <CourseList />
            </RoleBasedProtectedRoute>
          }
        />
        {/* <Route
          path={routes.aspireAdmin.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[
                ROLE.SUPER_ADMIN,
                ROLE.CLIENT_USER,
                ROLE.ASPIRE_ADMIN,
                ROLE.NONE,
              ]}
            >
              <AspireAdminDashboard />
            </RoleBasedProtectedRoute>
          }
        /> */}

        <Route
          path={routes.packageList.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.SUPER_ADMIN, ROLE.CLIENT_USER, ROLE.ASPIRE_ADMIN]}
            >
              <PackageList />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.subPackages.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.ASPIRE_ADMIN, ROLE.MSP_ADMIN]}
            >
              <SubPackages />
            </RoleBasedProtectedRoute>
          }
        />

        <Route
          path={routes.addContent.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.SUPER_ADMIN, ROLE.ASPIRE_ADMIN, ROLE.MSP_ADMIN]}
            >
              <CourseCardList hostPath={routes} />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.productList.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[
                ROLE.SUPER_ADMIN,
                ROLE.CLIENT_ADMIN,
                ROLE.ASPIRE_ADMIN,
                ROLE.MSP_ADMIN,
              ]}
            >
              <ProductList />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.featureList.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.SUPER_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <FeatureList />
            </RoleBasedProtectedRoute>
          }
        />

        {/* <Route path={routes.contentLibrary.path} element={<RoleBasedProtectedRoute
              allowed={[ROLE.SUPER_ADMIN, ROLE.CLIENT_USER]}
            ><ContentLibrary />
            </RoleBasedProtectedRoute>} /> */}

        <Route
          path={routes.campaigns.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <Campaigns />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.bookmarks.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <BookMarks />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.certificates.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <CertificateList />
            </RoleBasedProtectedRoute>
          }
        />

        <Route
          path={routes.courseDetails.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <CourseViewDetails />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.courseComplete.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <CourseComplete />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.contentDetails.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <ContentView />
            </RoleBasedProtectedRoute>
          }
        />

        <Route
          path={routes.phishingList.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <PhishingList />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.notification.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <UserNotification />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.userActivityLogs.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <UserActivityLogs />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.account.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <ClientUserAccountLayout />
            </RoleBasedProtectedRoute>
          }
        >
          <Route
            path={routes.account.path}
            element={
              <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
                <Navigate to={routes.accountProfile.path} replace />
              </RoleBasedProtectedRoute>
            }
          />
          <Route
            path={routes.accountProfile.path}
            element={
              <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
                <MyProfile />
              </RoleBasedProtectedRoute>
            }
          />
          <Route
            path={routes.accountProfileSettings.path}
            element={
              <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
                <ProfileSettings />
              </RoleBasedProtectedRoute>
            }
          />
          <Route
            path={routes.accountSecurity.path}
            element={
              <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
                <ChangePassword />
              </RoleBasedProtectedRoute>
            }
          />
        </Route>

        <Route
          path={routes.productAnalytics.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_ADMIN]}>
              <ProductAnalytics />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.assignedPackages.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <AssignedPackages />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.availablePackages.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <AvailablePackages />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.packageLicenseHistory.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <PackageLicense />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.licenseOverview.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_ADMIN]}>
              <AssignedPackages />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.licenseAssignment.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_ADMIN]}>
              <PackageLicense />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.packageReports.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <PerformanceReport />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.certificateHistory.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.MSP_ADMIN]}
            >
              <CertificateHistory />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.certificateTemplate.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.ASPIRE_ADMIN, ROLE.MSP_ADMIN]}
            >
              <CertificateTemplates />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.certificateIssued.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.ASPIRE_ADMIN, ROLE.MSP_ADMIN]}
            >
              <CertificateIssued />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.certificateAnalytics.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN, ROLE.MSP_ADMIN]}>
              <CertificateAnalytics />
            </RoleBasedProtectedRoute>
          }
        />

        <Route // Remove it after completed examLibrary feature
          path={routes.examList.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <ExamsList />
            </RoleBasedProtectedRoute>
          }
        />

        <Route
          path={routes.examLibrary.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.ASPIRE_ADMIN, ROLE.SUPER_ADMIN]}
            >
              <ExamLibrary />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.createExam.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <CreateExam />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.examSettings.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <ExamSettings />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.examineeReports.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <ExamineeReports />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.examAnalytics.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <Analytics />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.bulkExamImport.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <BulkImport />
            </RoleBasedProtectedRoute>
          }
        />

        <Route
          path={routes.courseChapters.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.SUPER_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <CourseLayout>
                <CourseChapters />
              </CourseLayout>
            </RoleBasedProtectedRoute>
          }
        />

        <Route
          path={routes.clientViewDetails.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.SUPER_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <ClientViewDetails />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.clientIndividualSubPackage.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.SUPER_ADMIN, ROLE.ASPIRE_ADMIN]}
            >
              <IndividualClientSubPackages />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.clientLicenseHistory.path}
          element={
            <RoleBasedProtectedRoute
              allowed={[ROLE.CLIENT_ADMIN, ROLE.ASPIRE_ADMIN, ROLE.MSP_ADMIN]}
            >
              <ClientLicenseHistory />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.mspLicenseHistory.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.ASPIRE_ADMIN]}>
              <MSPLicenseHistory />
            </RoleBasedProtectedRoute>
          }
        />
      </Route>

      <Route>
        <Route
          path={routes.exam.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <TakeExam />
            </RoleBasedProtectedRoute>
          }
        />
        <Route
          path={routes.examResult.path}
          element={
            <RoleBasedProtectedRoute allowed={[ROLE.CLIENT_USER]}>
              <ExamResults hostPath={routes} />
            </RoleBasedProtectedRoute>
          }
        />
      </Route>

      <Route path={routes.login.path} element={<Login />} />
      <Route path={routes.twoFactorAuthSetup.path} element={<MFASetup />} />
      <Route path={routes.twoFactorAuthVerify.path} element={<MFAVerify />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default AppRouter;
