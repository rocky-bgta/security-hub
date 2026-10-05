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
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Separator } from 'common/Separator';
import useAPI from 'hooks/UseAPI';
import {
  Calendar,
  Clock,
  FileText,
  Loader2,
  Package2,
  Settings,
} from 'lucide-react';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formatDateAndTime } from 'utils/Helper';
import { ISubPackageDetails } from './SubPackage';

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
        API_END_POINTS.SUB_PACKAGE_DETAILS.replace(':id', id),
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
      <DialogContent className="!home-h-[85%] !home-w-4/5 !home-overflow-y-auto home-text-white">
        {loading && (
          <div className="home-flex home-h-full home-items-center home-justify-center">
            <Loader2 className="home-size-5 home-animate-spin" />
          </div>
        )}
        {!loading && (
          <Fragment>
            <DialogHeader>
              <DialogTitle className="home-text-white">
                {subPackage?.name}
              </DialogTitle>
              <DialogDescription>
                Sub-package details and configuration
              </DialogDescription>
            </DialogHeader>
            <div className="home-space-y-6">
              <div className="home-grid home-grid-cols-1 home-gap-6 lg:home-grid-cols-3">
                {/* Left Column - Main Info */}
                <div className="home-space-y-6 lg:home-col-span-2">
                  {/* Basic Information */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="home-flex home-items-center home-gap-2">
                        <Package2 className="home-size-5" />
                        Basic Information
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="home-space-y-4 home-pt-6">
                      <div>
                        <h3 className="home-mb-2 home-font-semibold">
                          Description
                        </h3>
                        <p className="home-text-gray-700 dark:home-text-gray-300">
                          {subPackage?.description}
                        </p>
                      </div>

                      <Separator className="home-bg-blue-200 dark:home-bg-blue-800" />

                      <div className="home-grid home-grid-cols-2 home-gap-4">
                        <div>
                          <h4 className="home-mb-1 home-font-medium">
                            Product
                          </h4>
                          <p className="home-text-gray-700 dark:home-text-gray-300">
                            {subPackage?.productDetails.productName}
                          </p>
                        </div>
                        <div>
                          <h4 className="home-mb-1 home-font-medium">Status</h4>
                          <Badge
                            className={
                              subPackage?.status === 'ACTIVE'
                                ? 'home-bg-green-100 home-text-green-800 dark:home-bg-green-900 dark:home-text-green-200'
                                : 'home-bg-gray-100 home-text-gray-800 dark:home-bg-gray-800 dark:home-text-gray-200'
                            }
                          >
                            {subPackage?.status}
                          </Badge>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Topics List */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="home-flex home-items-center home-gap-2">
                        <FileText className="home-size-5" />
                        Topics ({subPackage?.topicDetails.length})
                      </CardTitle>
                      <CardDescription>
                        Training topics included in this sub-package
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="home-pt-6">
                      <div className="home-space-y-4">
                        {subPackage?.topicDetails.map((topic, index) => (
                          <div
                            key={topic.id}
                            className="home-rounded-lg home-border home-border-card-border home-p-4"
                          >
                            <div className="home-flex home-items-start home-justify-between">
                              <div className="home-flex-1">
                                <h4 className="home-mb-1 home-font-medium">
                                  {index + 1}. {topic.topicName}
                                </h4>
                                <p className="home-mb-3 home-text-sm home-text-gray-600 dark:home-text-gray-400">
                                  {topic.description}
                                </p>
                                <div className="home-flex home-flex-wrap home-gap-2">
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
                <div className="home-space-y-6">
                  {/* Metadata */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="home-flex home-items-center home-gap-2">
                        <Settings className="home-size-5" />
                        Metadata
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="home-space-y-4">
                      {/* <div className="home-flex home-items-center home-gap-3">
                    <User className="home-h-4 home-w-4 home-text-blue-600 dark:home-text-blue-400" />
                    <div>
                      <p className="home-font-medium  ">Created By</p>
                      <p className="home-text-sm home-text-gray-600 dark:home-text-gray-400">
                        {subPackage?.createdBy}
                      </p>
                    </div>
                  </div> */}

                      <div className="home-flex home-items-center home-gap-3">
                        <Calendar className="home-size-4 home-text-blue-600 dark:home-text-blue-400" />
                        <div>
                          <p className="home-font-medium">Created On</p>
                          <p className="home-text-sm home-text-gray-600 dark:home-text-gray-400">
                            {subPackage?.createdAt
                              ? formatDateAndTime(subPackage?.createdAt)
                              : 'N/A'}
                          </p>
                        </div>
                      </div>

                      <div className="home-flex home-items-center home-gap-3">
                        <Clock className="home-size-4 home-text-blue-600 dark:home-text-blue-400" />
                        <div>
                          <p className="home-font-medium">Last Updated</p>
                          <p className="home-text-sm home-text-gray-600 dark:home-text-gray-400">
                            {subPackage?.updatedAt
                              ? formatDateAndTime(subPackage?.updatedAt)
                              : 'N/A'}
                          </p>
                        </div>
                      </div>

                      {/* <div className="home-flex home-items-center home-gap-3">
                    <User className="home-h-4 home-w-4 home-text-blue-600 dark:home-text-blue-400" />
                    <div>
                      <p className="home-font-medium  ">Updated By</p>
                      <p className="home-text-sm home-text-gray-600 dark:home-text-gray-400">
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
                    <CardContent className="home-space-y-4 home-pt-6">
                      <div className="home-text-center">
                        <div className="home-text-3xl home-font-bold home-text-blue-600 dark:home-text-blue-400">
                          {subPackage?.assignedUserCount || 0}
                        </div>
                        <p className="home-text-sm home-text-gray-600 dark:home-text-gray-400">
                          Assigned Users
                        </p>
                      </div>

                      <Separator className="home-bg-blue-200 dark:home-bg-blue-800" />

                      {/* <div className="home-text-center">
                        <div className="home-text-3xl home-font-bold home-text-green-600 dark:home-text-green-400">
                          {subPackage?.packageDetails.progress || 0}
                          Demo Data
                        </div>
                        <p className="home-text-sm home-text-gray-600 dark:home-text-gray-400">
                          Completion Rate
                        </p>
                      </div> */}

                      {/* <Separator className="home-bg-blue-200 dark:home-bg-blue-800" /> */}

                      <div className="home-text-center">
                        <div className="home-text-3xl home-font-bold home-text-purple-600 dark:home-text-purple-400">
                          {subPackage?.topicDetails.length}
                        </div>
                        <p className="home-text-sm home-text-gray-600 dark:home-text-gray-400">
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
