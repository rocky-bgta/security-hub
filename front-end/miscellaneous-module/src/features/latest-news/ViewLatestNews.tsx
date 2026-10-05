import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Label } from 'common/Label';
import { sanitizeHtml } from 'home-module/security';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { DialogFooter } from 'common/Dialog';
import { useEffect, useState } from 'react';
import { ILatestNews } from 'models/LatestNews';
import { formateDateAndTime, isSuccessResponse } from 'utils/Helper';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedNews: ILatestNews | null;
}
const ViewLatestNews = ({ isOpen, onClose, selectedNews }: IProps) => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState(true);
  const [news, setNews] = useState<ILatestNews | null>(null);

  useEffect(() => {
    if (isOpen && selectedNews) {
      fetchNews();
    }
  }, [selectedNews]);

  const fetchNews = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_LATEST_NEWS_DETAILS.replace(
          ':id',
          selectedNews?.id ?? '',
        ),
      );
      if (isSuccessResponse(response.statusCode)) {
        setNews(response.data);
      }
    } catch (error) {
      console.error('Error fetching news:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] w-2/3 overflow-y-auto">
        <DialogHeader>
          <DialogTitle>View News Post</DialogTitle>
          <DialogDescription>
            View the details of the news post
          </DialogDescription>
        </DialogHeader>
        {loading ? (
          <div className="flex h-full items-center justify-center">
            <p className="text-sm text-muted-foreground">Loading...</p>
          </div>
        ) : (
          news && (
            <div className="space-y-4">
              <div className="grid grid-cols-5 gap-4">
                <div className="col-span-4">
                  <Label className="text-sm font-semibold">Title</Label>
                  <p className="mt-1 text-base">{news.name}</p>
                </div>
                <div className="col-span-1">
                  <img
                    src={FILE_PATH_PREFIX + news.imageUrl}
                    alt={news.name}
                    className="size-full object-cover"
                  />
                </div>
              </div>
              <div>
                <Label className="text-sm font-semibold">Content</Label>
                <div
                  dangerouslySetInnerHTML={{
                    __html: sanitizeHtml(news.content ?? ''),
                  }}
                  className="mt-1 text-base"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm font-semibold">Category</Label>
                  <p className="mt-1 text-base">{news.category.name}</p>
                </div>
                <div>
                  <Label className="text-sm font-semibold">Status</Label>
                  <div className="mt-1">
                    <Badge variant="outline">{news.status}</Badge>
                  </div>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm font-semibold">
                    Published Date
                  </Label>
                  <p className="mt-1 text-base">
                    {formateDateAndTime(news.publishedDate ?? '')}
                  </p>
                </div>
                <div>
                  <Label className="text-sm font-semibold">Expire Date</Label>
                  <p className="mt-1 text-base">
                    {formateDateAndTime(news.expireDate ?? '')}
                  </p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm font-semibold">Like Count</Label>
                  <p className="mt-1 text-base">{news.likeCount}</p>
                </div>
                <div>
                  <Label className="text-sm font-semibold">Dislike Count</Label>
                  <p className="mt-1 text-base">{news.dislikeCount}</p>
                </div>
              </div>
            </div>
          )
        )}
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default ViewLatestNews;
