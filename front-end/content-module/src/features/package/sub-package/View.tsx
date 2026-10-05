import { Badge } from 'common/Badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Separator } from 'common/Separator';
import { useAPI } from 'hooks/UseAPI';
import {
  Calendar,
  Clock,
  FileText,
  Loader2,
  Package,
  Package2,
  Settings,
} from 'lucide-react';
import { ISubPackageDetails } from 'models/SubPackage';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formatDateAndTime } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedSubPackage: any;
}

const ViewSubPackage = ({ isOpen, onClose, selectedSubPackage }: IProps) => {
  const apiClient = useAPI();
  const [subPackage, setSubPackage] = useState<ISubPackageDetails>();
  const [loading, setLoading] = useState(true);

  const fetchSubPackage = async (id: string) => {
    try {
      const response = await apiClient.get(
        `${API_END_POINTS.SUB_PACKAGE_DETAILS.replace(':id', id)}`,
      );
      setSubPackage(response.data);
      setLoading(false);
    } catch (error) {
      console.error(error);
      return;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedSubPackage) {
      setTimeout(() => {
        fetchSubPackage(selectedSubPackage.id);
      }, 0);
    }
  }, [selectedSubPackage]);

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="!content-h-4/5 !content-w-4/5 !content-overflow-y-auto content-text-white">
        {loading && (
          <div className="content-flex content-h-full content-items-center content-justify-center">
            <Loader2 className="content-size-5 content-animate-spin" />
          </div>
        )}
        {!loading && (
          <Fragment>
            <DialogHeader>
              <DialogTitle className="content-flex content-items-center content-gap-2 content-text-white">
                <Package /> Sub-package details
              </DialogTitle>
            </DialogHeader>
            <div className="content-space-y-6">
              <div className="content-grid content-grid-cols-1 content-gap-6 lg:content-grid-cols-3">
                {/* Left Column - Main Info */}
                <div className="content-space-y-6 lg:content-col-span-2">
                  {/* Basic Information */}
                  <Card>
                    <CardHeader>
                      <div className="content-flex content-items-center content-gap-2">
                        <Package2 className="content-size-5" />
                        Basic Information
                      </div>
                    </CardHeader>
                    <CardContent className="content-space-y-4 ">
                      <div className="content-grid content-grid-cols-2 content-gap-4">
                        <div>
                          <h4 className="content-mb-2 content-font-medium">
                            Sub-Package Name
                          </h4>
                          <p className="content-text-muted-foreground dark:content-text-muted-foreground">
                            {subPackage?.name}
                          </p>
                        </div>

                        <div>
                          <h4 className="content-mb-1 content-font-medium">
                            Status
                          </h4>
                          <Badge
                            className={
                              subPackage?.status === 'ACTIVE'
                                ? 'content-bg-green-100 content-text-green-800 dark:content-bg-green-900 dark:content-text-green-200'
                                : 'content-bg-gray-100 content-text-gray-800 dark:content-bg-gray-800 dark:content-text-gray-200'
                            }
                          >
                            {subPackage?.status}
                          </Badge>
                        </div>
                      </div>
                      {subPackage?.description && (
                        <div>
                          <h4 className="content-mb-2 content-font-medium">
                            Description
                          </h4>
                          <p className="content-text-muted-foreground dark:content-text-muted-foreground">
                            {subPackage?.description}
                          </p>
                        </div>
                      )}
                      <Separator className="content-bg-blue-200 dark:content-bg-blue-800" />

                      <div className="content-grid content-grid-cols-2 content-gap-4">
                        <div>
                          <h4 className="content-mb-1 content-font-medium">
                            Product
                          </h4>
                          <p className="content-text-gray-700 dark:content-text-gray-300">
                            {subPackage?.productDetails.productName}
                          </p>
                        </div>
                        <div>
                          <h4 className="content-mb-1 content-font-medium">
                            Package
                          </h4>
                          <p className="content-text-gray-700 dark:content-text-gray-300">
                            {subPackage?.packageDetails.name}
                          </p>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Topics List */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="content-flex content-items-center content-gap-2">
                        <FileText className="content-size-5" />
                        Topics ({subPackage?.topicDetails.length})
                      </CardTitle>
                      <CardDescription>
                        Training topics included in this sub-package
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="content-pt-6">
                      <div className="content-space-y-4">
                        {subPackage?.topicDetails.map((topic, index) => (
                          <div
                            key={topic.id}
                            className="content-rounded-lg content-border content-border-card-border content-p-4"
                          >
                            <div className="content-flex content-items-start content-justify-between">
                              <div className="content-flex-1">
                                <h4 className="content-mb-1 content-font-medium">
                                  {index + 1}. {topic.topicName}
                                </h4>
                                <p className="content-mb-3 content-text-sm content-text-gray-600 dark:content-text-gray-400">
                                  {topic.description}
                                </p>
                                <div className="content-flex content-flex-wrap content-gap-2">
                                  <Badge variant="outline">
                                    Contents {topic?.totalContentCount}
                                  </Badge>
                                  <Badge variant="outline">
                                    Durations {topic?.durationMinutes}
                                  </Badge>
                                </div>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                </div>

                {/* Right Column - Metadata & Stats */}
                <div className="content-space-y-6">
                  {/* Metadata */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="content-flex content-items-center content-gap-2">
                        <Settings className="content-size-5" />
                        Metadata
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="content-space-y-4">
                      {/* <div className="content-flex content-items-center content-gap-3">
                    <User className="content-h-4 content-w-4 content-text-blue-600 dark:content-text-blue-400" />
                    <div>
                      <p className="content-font-medium  ">Created By</p>
                      <p className="content-text-sm content-text-gray-600 dark:content-text-gray-400">
                        {subPackage?.createdBy}
                      </p>
                    </div>
                  </div> */}

                      <div className="content-flex content-items-center content-gap-3">
                        <Calendar className="content-size-4 content-text-blue-600 dark:content-text-blue-400" />
                        <div>
                          <p className="content-font-medium">Created On</p>
                          <p className="content-text-sm content-text-gray-600 dark:content-text-gray-400">
                            {subPackage?.createdAt
                              ? formatDateAndTime(subPackage?.createdAt)
                              : 'N/A'}
                          </p>
                        </div>
                      </div>

                      <div className="content-flex content-items-center content-gap-3">
                        <Clock className="content-size-4 content-text-blue-600 dark:content-text-blue-400" />
                        <div>
                          <p className="content-font-medium">Last Updated</p>
                          <p className="content-text-sm content-text-gray-600 dark:content-text-gray-400">
                            {subPackage?.updatedAt
                              ? formatDateAndTime(subPackage?.updatedAt)
                              : 'N/A'}
                          </p>
                        </div>
                      </div>

                      {/* <div className="content-flex content-items-center content-gap-3">
                    <User className="content-h-4 content-w-4 content-text-blue-600 dark:content-text-blue-400" />
                    <div>
                      <p className="content-font-medium  ">Updated By</p>
                      <p className="content-text-sm content-text-gray-600 dark:content-text-gray-400">
                        {subPackage?.createdBy }
                      </p>
                    </div>
                  </div> */}
                    </CardContent>
                  </Card>

                  {/* Usage Statistics */}
                  <Card>
                    <CardHeader>
                      <CardTitle>Usage Statistics</CardTitle>
                    </CardHeader>
                    <CardContent className="content-space-y-4 content-pt-6">
                      <div className="content-text-center">
                        <div className="content-text-3xl content-font-bold content-text-blue-600 dark:content-text-blue-400">
                          {subPackage?.assignedUserCount || 0}
                        </div>
                        <p className="content-text-sm content-text-gray-600 dark:content-text-gray-400">
                          Assigned Users
                        </p>
                      </div>

                      <Separator className="content-bg-blue-200 dark:content-bg-blue-800" />

                      {/* <div className="content-text-center">
                        <div className="content-text-3xl content-font-bold content-text-green-600 dark:content-text-green-400">
                          {subPackage?.packageDetails.progress || 0}
                          Demo Data
                        </div>
                        <p className="content-text-sm content-text-gray-600 dark:content-text-gray-400">
                          Completion Rate
                        </p>
                      </div> */}

                      {/* <Separator className="content-bg-blue-200 dark:content-bg-blue-800" /> */}

                      <div className="content-text-center">
                        <div className="content-text-3xl content-font-bold content-text-purple-600 dark:content-text-purple-400">
                          {subPackage?.topicDetails.length}
                        </div>
                        <p className="content-text-sm content-text-gray-600 dark:content-text-gray-400">
                          Total Topics
                        </p>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </div>
            </div>
          </Fragment>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ViewSubPackage;
