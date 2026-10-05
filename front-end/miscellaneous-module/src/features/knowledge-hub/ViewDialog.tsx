import { sanitizeHtml } from 'home-module/security';
import { Badge } from 'common/Badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Label } from 'common/Label';
import { useAPI } from 'hooks/UseAPI';
import { IKnowledgeHub } from 'models/KnowledgeHub';
import { JSX, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { formateDate, isSuccessResponse } from 'utils/Helper';

interface IViewDialog {
  isOpen: boolean;
  onClose: () => void;
  selectedId: string;
  getTypeIcon: (type: string) => JSX.Element;
  getStatusBadge: (status: string) => JSX.Element;
}

const ViewDialog = ({
  isOpen,
  onClose,
  selectedId,
  getTypeIcon,
  getStatusBadge,
}: IViewDialog) => {
  const apiClient = useAPI();
  const [selectedResource, setSelectedResource] =
    useState<IKnowledgeHub | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (selectedId) {
      fetchKnowledgeHub();
    }
  }, [selectedId]);
  const fetchKnowledgeHub = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_KNOWLEDGE_HUB_DETAILS.replace(':id', selectedId),
      );
      if (isSuccessResponse(response.statusCode)) {
        setSelectedResource(response.data);
      }
    } catch (error) {
      console.error('Error fetching knowledge hub:', error);
    } finally {
      setLoading(false);
    }
  };
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] w-2/3 overflow-auto">
        <DialogHeader>
          <DialogTitle>Knowledge Hub Details</DialogTitle>
          <DialogDescription>
            View detailed information about this knowledge hub
          </DialogDescription>
        </DialogHeader>
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-white">Loading...</div>
          </div>
        ) : selectedResource ? (
          <div className="space-y-4">
            <div>
              <Label className="text-sm font-semibold">Title</Label>
              <p className="text-sm text-muted-foreground">
                {selectedResource?.name}
              </p>
            </div>
            <div className="grid grid-cols-3 gap-4">
              <div>
                <Label className="text-sm font-semibold">Type</Label>
                <div className="mt-1 flex items-center gap-2">
                  {getTypeIcon(selectedResource?.resourceType)}
                  <Badge variant="outline">
                    {selectedResource?.resourceType}
                  </Badge>
                </div>
              </div>
              <div>
                <Label className="text-sm font-semibold">Status</Label>
                <div className="mt-1">
                  {getStatusBadge(selectedResource?.status?.toString())}
                </div>
              </div>
              <div>
                <Label className="text-sm font-semibold">Category</Label>
                <p className="text-sm text-muted-foreground">
                  {selectedResource?.category?.name}
                </p>
              </div>
              <div>
                <Label className="text-sm font-semibold">Tags</Label>
                <p className="text-sm text-muted-foreground">
                  {selectedResource?.tags?.map(tag => tag?.name).join(', ')}
                </p>
              </div>
            </div>
            <div>
              <Label className="text-sm font-semibold">Description</Label>
              <div
                className="text-sm text-muted-foreground"
                dangerouslySetInnerHTML={{
                  __html: sanitizeHtml(selectedResource?.content ?? ''),
                }}
              />
            </div>
            <div className="grid grid-cols-3 gap-4">
              <div>
                <Label className="text-sm font-semibold">Publish Date</Label>
                <p className="text-sm text-muted-foreground">
                  {formateDate(selectedResource?.publishedDate)}
                </p>
              </div>
              {/* <div>
                <Label className="text-sm font-semibold">Views</Label>
                <Badge variant="outline">{selectedResource.likeCount}</Badge>
              </div> */}
              <div>
                <Label className="text-sm font-semibold">Downloads</Label>
                <Badge variant="outline">
                  {selectedResource?.dislikeCount}
                </Badge>
              </div>
            </div>
            <div>
              <Label className="text-sm font-semibold">Image</Label>
              <img
                src={FILE_PATH_PREFIX + selectedResource?.imageUrl}
                alt={selectedResource?.name}
                className="size-full object-cover"
              />
            </div>
          </div>
        ) : (
          <div className="flex items-center justify-center py-12">
            <div className="text-white">No data found</div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ViewDialog;
