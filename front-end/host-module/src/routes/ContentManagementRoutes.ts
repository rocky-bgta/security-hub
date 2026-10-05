export const ContentManagementRoutes = {
  dashboard: {
    title: 'Dashboard',
    key: 'dashboard',
    path: '/',
  },
  // mixed routes
  packageList: {
    title: 'Package List',
    key: 'package-list',
    path: '/content-management/package-list',
  },
  courseList: {
    title: 'Course List',
    key: 'course-list',
    path: '/content-management/course-list',
  },

  // super admin exclusive routes
  productList: {
    title: 'Product List',
    key: 'product-list',
    path: '/content-management/product-list',
  },
  contentLibrary: {
    title: 'Content Library',
    key: 'content-library',
    path: '/content-management/content-library',
  },
  courseDetails: {
    title: 'Course Details',
    key: 'course-details',
    path: '/content-management/course/:packageId/:slug',
  },
  addContent: {
    title: 'Add Content',
    key: 'add-content',
    path: '/content-management/add-content',
  },
  featureList: {
    title: 'Feature List',
    key: 'feature-list',
    path: '/content-management/feature-list',
  },
  courseChapters: {
    title: 'Course Chapters',
    key: 'chapter-list',
    path: '/content-management/course/:slug/chapters',
  },

  // End User routes
  exam: {
    title: 'Exam',
    key: 'exam',
    path: '/exam/:slug',
  },
  examResult: {
    title: 'Exam Result',
    key: 'exam-result',
    path: '/exam/:slug/result',
  },
  certificates: {
    title: 'Certifications',
    key: 'certificates',
    path: '/certifications',
  },
};
