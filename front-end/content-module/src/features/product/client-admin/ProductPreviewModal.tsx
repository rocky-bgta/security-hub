import { Badge } from 'common/Badge';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Progress } from 'common/Progress';
import {
  BarChart3,
  Clock,
  FileText,
  Image,
  Layers,
  Play,
  Users,
} from 'lucide-react';

interface ContentItem {
  id: string;
  title: string;
  type: 'video' | 'animation' | 'slideshow' | 'storyboard';
  duration: number; // in minutes
  description: string;
}

interface Product {
  id: string;
  name: string;
  description: string;
  category: string;
  duration: number; // total duration in minutes
  prerequisites: string[];
  enrolledUsers: number;
  completionRate: number; // percentage
  content: ContentItem[];
  createdAt: string;
  updatedAt: string;
}

interface ContentTypeStats {
  type: 'video' | 'animation' | 'slideshow' | 'storyboard';
  count: number;
  totalDuration: number;
}

interface ProductPreviewModalProps {
  product: any;
  isOpen: boolean;
  onClose: () => void;
}

export function ProductPreviewModal({
  product,
  isOpen,
  onClose,
}: ProductPreviewModalProps) {
  if (!product) return null;

  const formatDuration = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  };

  const getContentTypeStats = (): ContentTypeStats[] => {
    const stats: { [key: string]: { count: number; totalDuration: number } } =
      {};

    product.content.forEach((item: any) => {
      if (!stats[item.type]) {
        stats[item.type] = { count: 0, totalDuration: 0 };
      }
      stats[item.type].count++;
      stats[item.type].totalDuration += item.duration;
    });

    return Object.entries(stats).map(([type, data]) => ({
      type: type as 'video' | 'animation' | 'slideshow' | 'storyboard',
      count: data.count,
      totalDuration: data.totalDuration,
    }));
  };

  const getContentTypeIcon = (type: string) => {
    switch (type) {
      case 'video':
        return <Play className="content-size-4 content-text-white" />;
      case 'animation':
        return <Layers className="content-size-4 content-text-white" />;
      case 'slideshow':
        return <Image className="content-size-4 content-text-white" />;
      case 'storyboard':
        return <FileText className="content-size-4 content-text-white" />;
      default:
        return <FileText className="content-size-4 content-text-white" />;
    }
  };

  const contentStats = getContentTypeStats();

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="content-max-h-[90vh] content-max-w-4xl content-overflow-y-auto">
        <DialogHeader className="content-border-b content-pb-4">
          <div className="content-flex content-items-start content-justify-between">
            <div className="content-space-y-2">
              <DialogTitle className="content-text-2xl content-font-bold content-text-foreground">
                {product.name}
              </DialogTitle>
              <div className="content-flex content-items-center content-gap-4">
                <Badge variant="secondary">{product.category}</Badge>
                <div className="content-flex content-items-center content-gap-1 content-text-muted-foreground">
                  <Clock className="content-size-4" />
                  <span className="content-font-medium">
                    {formatDuration(product.duration)}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </DialogHeader>

        <div className="content-space-y-6 content-py-4">
          {/* Product Overview */}
          <div className="content-grid content-gap-4 md:content-grid-cols-3">
            <Card>
              <CardContent className="content-pt-6">
                <div className="content-flex content-items-center content-justify-between">
                  <div>
                    <p className="content-text-2xl content-font-bold content-text-foreground">
                      {product.enrolledUsers}
                    </p>
                    <p className="content-flex content-items-center content-gap-1 content-text-sm content-text-muted-foreground">
                      <Users className="content-size-4" />
                      Enrolled Users
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="content-pt-6">
                <div className="content-flex content-items-center content-justify-between">
                  <div className="content-flex-1">
                    <p className="content-text-2xl content-font-bold content-text-foreground">
                      {product.completionRate}%
                    </p>
                    <p className="content-flex content-items-center content-gap-1 content-text-sm content-text-muted-foreground">
                      <BarChart3 className="content-size-4" />
                      Completion Rate
                    </p>
                  </div>
                </div>
                <Progress
                  value={product.completionRate}
                  className="content-mt-2 content-h-2"
                />
              </CardContent>
            </Card>

            <Card>
              <CardContent className="content-pt-6">
                <div className="content-flex content-items-center content-justify-between">
                  <div>
                    <p className="content-text-2xl content-font-bold content-text-foreground">
                      {product.content.length}
                    </p>
                    <p className="content-text-sm content-text-muted-foreground">
                      Content Items
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Description */}
          <Card>
            <CardHeader>
              <CardTitle className="content-text-lg">
                Product Description
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="content-leading-relaxed content-text-muted-foreground">
                {product.description}
              </p>
            </CardContent>
          </Card>

          {/* Content Breakdown */}
          <Card>
            <CardHeader>
              <CardTitle className="content-text-lg">
                Content Breakdown
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="content-mb-6 content-grid content-gap-4 md:content-grid-cols-2 lg:content-grid-cols-4">
                {contentStats.map(stat => (
                  <div
                    key={stat.type}
                    className="content-rounded-lg content-bg-muted/30 content-p-4"
                  >
                    <div className="content-mb-2 content-flex content-items-center content-gap-2">
                      {getContentTypeIcon(stat.type)}
                      <span className="content-font-medium content-capitalize content-text-foreground">
                        {stat.type}s
                      </span>
                    </div>
                    <div className="content-space-y-1">
                      <p className="content-text-sm content-text-muted-foreground">
                        {stat.count} item{stat.count !== 1 ? 's' : ''}
                      </p>
                      <p className="content-text-sm content-font-medium content-text-secondary">
                        {formatDuration(stat.totalDuration)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>

              {/* Detailed Content List */}
              <div className="content-space-y-3">
                <h4 className="content-mb-3 content-font-semibold content-text-foreground">
                  Content Details
                </h4>
                {product.content.map((item: any) => (
                  <div
                    key={item.id}
                    className="content-flex content-items-center content-gap-3 content-rounded-lg content-bg-muted/20 content-p-3"
                  >
                    <div className="content-shrink-0">
                      {getContentTypeIcon(item.type)}
                    </div>
                    <div className="content-min-w-0 content-flex-1">
                      <h5 className="content-truncate content-font-medium content-text-foreground">
                        {item.title}
                      </h5>
                      <p className="content-line-clamp-2 content-text-sm content-text-muted-foreground">
                        {item.description}
                      </p>
                    </div>
                    <div className="content-shrink-0 content-text-right">
                      <Badge
                        variant="outline"
                        className="content-text-xs content-text-white"
                      >
                        {formatDuration(item.duration)}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Prerequisites */}
          {product.prerequisites && product.prerequisites.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="content-text-lg">Prerequisites</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="content-flex content-flex-wrap content-gap-2">
                  {product.prerequisites.map((prereq: any) => (
                    <Badge key={prereq} variant="outline">
                      {prereq}
                    </Badge>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
