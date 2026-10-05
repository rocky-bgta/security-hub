import {
  Download,
  Download as DownloadIcon,
  Eye,
  FileText,
  TrendingUp,
} from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';

// Mock data for analytics
const resourceUsageData = [
  { name: 'Documents', views: 1200, downloads: 450 },
  { name: 'Videos', views: 890, downloads: 0 },
  { name: 'Guides', views: 670, downloads: 230 },
  { name: 'Links', views: 340, downloads: 0 },
  { name: 'FAQs', views: 520, downloads: 180 },
];

const categoryData = [
  { name: 'Getting Started', value: 35, color: '#8884d8' },
  { name: 'Advanced Learning', value: 30, color: '#82ca9d' },
  { name: 'Instructor Help', value: 20, color: '#ffc658' },
  { name: 'Troubleshooting', value: 15, color: '#ff7c7c' },
];

const topResources = [
  {
    id: 'RES-001',
    title: 'Getting Started with Cybersecurity',
    type: 'Document',
    views: 1245,
    downloads: 456,
    category: 'Getting Started',
  },
  {
    id: 'RES-002',
    title: 'Advanced Security Tutorial Video',
    type: 'Video',
    views: 890,
    downloads: 0,
    category: 'Advanced Learning',
  },
  {
    id: 'RES-003',
    title: 'Security Best Practices Guide',
    type: 'Guide',
    views: 756,
    downloads: 289,
    category: 'Best Practices',
  },
];

const courseConsumption = [
  {
    course: 'Cybersecurity Fundamentals',
    resources: 15,
    totalViews: 3450,
    totalDownloads: 1230,
  },
  {
    course: 'Advanced Security',
    resources: 12,
    totalViews: 2890,
    totalDownloads: 890,
  },
  {
    course: 'Compliance Training',
    resources: 8,
    totalViews: 1560,
    totalDownloads: 340,
  },
];

const ResourceAnalytics = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Resource Analytics
          </h1>
          <p className="text-muted-foreground">
            Track and analyze resource usage and engagement
          </p>
        </div>
        <Button>
          <Download className="mr-2 size-4" />
          Export Analytics
        </Button>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Views</CardTitle>
            <Eye className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">15,420</div>
            <p className="text-xs text-muted-foreground">
              <span className="flex items-center text-green-600">
                <TrendingUp className="mr-1 size-3" />
                +12.5%
              </span>
              from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Total Downloads
            </CardTitle>
            <DownloadIcon className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">4,256</div>
            <p className="text-xs text-muted-foreground">
              <span className="flex items-center text-green-600">
                <TrendingUp className="mr-1 size-3" />
                +8.2%
              </span>
              from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Total Resources
            </CardTitle>
            <FileText className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">125</div>
            <p className="text-xs text-muted-foreground">
              <span className="flex items-center text-green-600">
                <TrendingUp className="mr-1 size-3" />
                +5 new
              </span>
              this month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Avg. Downloads/Resource
            </CardTitle>
            <TrendingUp className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">34.1</div>
            <p className="text-xs text-muted-foreground">
              <span className="flex items-center text-green-600">
                <TrendingUp className="mr-1 size-3" />
                +3.2%
              </span>
              from last month
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Resource Usage by Type</CardTitle>
            <CardDescription>
              Views and downloads by resource type
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={resourceUsageData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="views" fill="hsl(var(--primary))" />
                <Bar dataKey="downloads" fill="hsl(var(--secondary))" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Resources by Category</CardTitle>
            <CardDescription>
              Distribution of resources across categories
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={categoryData}
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                  label={({ name, value }) => `${name}: ${value}%`}
                >
                  {categoryData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Top Resources */}
      <Card>
        <CardHeader>
          <CardTitle>Most Viewed Resources</CardTitle>
          <CardDescription>Resources with highest engagement</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Title</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Category</TableHead>
                <TableHead>Views</TableHead>
                <TableHead>Downloads</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {topResources.map(resource => (
                <TableRow key={resource.id}>
                  <TableCell>
                    <div className="font-medium">{resource.title}</div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">{resource.type}</Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="secondary">{resource.category}</Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {resource.views.toLocaleString()}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {resource.downloads.toLocaleString()}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Course-wise Consumption */}
      <Card>
        <CardHeader>
          <CardTitle>Course-wise Resource Consumption</CardTitle>
          <CardDescription>Resource usage by course</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Course</TableHead>
                <TableHead>Resources</TableHead>
                <TableHead>Total Views</TableHead>
                <TableHead>Total Downloads</TableHead>
                <TableHead>Avg. Views/Resource</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {courseConsumption.map((course, index) => (
                <TableRow key={index}>
                  <TableCell>
                    <div className="font-medium">{course.course}</div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">{course.resources}</Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {course.totalViews.toLocaleString()}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {course.totalDownloads.toLocaleString()}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="secondary">
                      {Math.round(course.totalViews / course.resources)}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default ResourceAnalytics;
