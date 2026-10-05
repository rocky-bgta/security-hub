import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import InfoViewCard from 'components/InfoViewCard';
import Border from 'components/UserBorder';
import { useAPI } from 'hooks/UseAPI';
import { ITopicDetails } from 'models/Course';
import { IResponse } from 'models/Global';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { HumanizeDate } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  topicId: string;
  onClose: () => void;
}

const CourseViewModal = ({ isOpen, onClose, topicId }: IProps) => {
  const [topic, setTopic] = useState<ITopicDetails>();
  const [loading, setLoading] = useState(false);
  const apiClient = useAPI();

  useEffect(() => {
    if (isOpen && topicId) {
      fetchTopicDetails();
    }
  }, [isOpen, topicId]);

  const fetchTopicDetails = async () => {
    setLoading(true);
    try {
      const response: IResponse<ITopicDetails> = await apiClient.get(
        API_END_POINTS.TOPIC_DETAILS + topicId,
      );
      setTopic(response.data);
    } catch (error) {
      console.error('Error fetching topic data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!topic && !loading) return null;

  // Extract categories
  const categories = topic?.categoryDetails || [];

  // Extract countries
  const countries = topic?.countryDetails || [];

  // Extract compliance details
  const compliances = topic?.complianceDetails || [];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      disableOutsideClick={false}
      variant="user"
      className="content-h-auto content-w-3/4"
    >
      <ModalHeader onClose={onClose}>
        <p className="content-py-5 content-text-lg content-font-medium content-text-white">
          View Course
        </p>
      </ModalHeader>
      <ModalBody className="content-my-6">
        {loading ? (
          <div className="content-flex content-items-center content-justify-center content-py-10">
            <div className="content-text-white">Loading...</div>
          </div>
        ) : topic ? (
          <Fragment>
            <InfoViewCard
              title={topic.topicName}
              description={topic.description || 'No description available'}
              createdAt={HumanizeDate(topic.createdAt)}
              updatedAt={HumanizeDate(topic.updatedAt)}
              status={topic.status === 'ENABLED'}
              // availability={topic.contentTypeDetails?.typeName}
              features={[]} // Add features if available in your data structure
              courseList={[]} // Add courses if available in your data structure
              chapters={[]} // Topic chapters would go here if available
              // products={products}
              image={topic.thumbnailUrl}
            />

            {topic.contentTypeDetails && (
              <div>
                <p className="content-mb-4 content-text-2xl content-font-medium content-text-cloudy-white">
                  Content Type: {topic.contentTypeDetails.typeName}
                </p>
                <div className="content-flex content-flex-wrap content-gap-3">
                  <div className="content-rounded content-bg-dark-blue content-px-3 content-py-2 content-text-cloudy-white">
                    {' '}
                    {topic.contentTypeDetails.typeName}
                  </div>
                </div>
              </div>
            )}

            {categories.length > 0 && (
              <div className="content-mt-8">
                <p className="content-mb-4 content-text-2xl content-font-medium content-text-cloudy-white">
                  Categories: {categories.length}
                </p>
                <div className="content-flex content-flex-wrap content-gap-3">
                  {categories.map((category, index) => (
                    <div
                      key={index}
                      className="content-rounded content-bg-dark-blue content-px-3 content-py-2 content-text-cloudy-white"
                    >
                      {category.categoryName}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {countries.length > 0 && (
              <div className="content-mt-6">
                <p className="content-mb-4 content-text-2xl content-font-medium content-text-cloudy-white">
                  Available Countries: {countries.length}
                </p>
                <div className="content-flex content-flex-wrap content-gap-3">
                  {countries.map((country, index) => (
                    <div
                      key={index}
                      className="content-flex content-items-center content-gap-2 content-rounded content-bg-success content-bg-opacity-10 content-px-3 content-py-2 content-text-success"
                    >
                      <span>{country.countryName}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {compliances.length > 0 && (
              <div className="content-mt-6">
                <p className="content-mb-4 content-text-2xl content-font-medium content-text-cloudy-white">
                  Compliance Standards: {compliances.length}
                </p>
                <div className="content-grid content-grid-cols-2 content-gap-4">
                  {compliances.map((compliance, index) => (
                    <div
                      key={index}
                      className="content-rounded content-border content-border-primary content-p-4"
                    >
                      <div className="content-flex content-items-center content-gap-2">
                        <div className="content-size-3 content-rounded-full content-bg-primary"></div>
                        <span className="content-text-xl content-font-semibold content-text-cloudy-white">
                          {compliance.complianceName}
                        </span>
                      </div>
                      {compliance.description && (
                        <p className="content-mt-2 content-text-gray-400">
                          {compliance.description}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Product & Package Details */}
            {topic.productPackages?.length > 0 && (
              <div className="content-mt-8">
                <p className="content-mb-4 content-text-2xl content-font-medium content-text-cloudy-white">
                  Packages: {topic.productPackages?.length}
                </p>
                {topic.productPackages.map(
                  (productPkg: any, productIndex: number) => (
                    <div
                      key={productIndex}
                      className="content-last:mb-0 content-mb-6"
                    >
                      <Border className="content-p-4">
                        <p className="content-mb-4 content-font-medium content-text-cloudy-white">
                          {productPkg?.productDetails?.productName}
                        </p>
                        {/* Packages under this product */}
                        {productPkg.packageDetails?.map(
                          (pkg: any, pkgIndex: number) => (
                            <div
                              key={pkgIndex}
                              className="content-mb-2 content-rounded content-bg-dark-blue content-px-3 content-py-2 content-text-cloudy-white"
                            >
                              <span className="content-font-medium">
                                {pkg.name}
                              </span>
                            </div>
                          ),
                        )}
                      </Border>
                    </div>
                  ),
                )}
              </div>
            )}

            {/* Additional Metadata */}
            <div className="content-mt-8 content-grid content-grid-cols-2 content-gap-6">
              <div className="content-rounded content-bg-gray-800 content-p-4">
                <p className="content-mb-2 content-text-lg content-font-medium content-text-cloudy-white">
                  Content Details
                </p>
                <div className="content-space-y-2">
                  <div className="content-flex content-justify-between">
                    <span className="content-text-gray-400">Type:</span>
                    <span className="content-text-cloudy-white">
                      {topic.contentTypeDetails?.typeName || 'N/A'}
                    </span>
                  </div>
                  <div className="content-flex content-justify-between">
                    <span className="content-text-gray-400">Duration:</span>
                    <span className="content-text-cloudy-white">
                      {topic.durationMinutes
                        ? `${topic.durationMinutes} minutes`
                        : 'N/A'}
                    </span>
                  </div>
                  <div className="content-flex content-justify-between">
                    <span className="content-text-gray-400">
                      Total Content:
                    </span>
                    <span className="content-text-cloudy-white">
                      {topic.totalContentCount || 'N/A'}
                    </span>
                  </div>
                </div>
              </div>

              <div className="content-rounded content-bg-gray-800 content-p-4">
                <p className="content-mb-2 content-text-lg content-font-medium content-text-cloudy-white">
                  System Information
                </p>
                <div className="content-space-y-2">
                  <div className="content-flex content-justify-between">
                    <span className="content-text-gray-400">Topic ID:</span>
                    <span className="content-break-all content-text-xs content-text-cloudy-white">
                      {topic.id}
                    </span>
                  </div>
                  <div className="content-flex content-justify-between">
                    <span className="content-text-gray-400">Created By:</span>
                    <span className="content-text-cloudy-white">
                      {topic.createdBy || 'System'}
                    </span>
                  </div>
                  <div className="content-flex content-justify-between">
                    <span className="content-text-gray-400">Chapters:</span>
                    <span className="content-text-cloudy-white">
                      {topic.chapterIds?.length || 0}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </Fragment>
        ) : (
          <div className="content-flex content-items-center content-justify-center content-py-10">
            <div className="content-text-white">No data available</div>
          </div>
        )}
      </ModalBody>
    </Modal>
  );
};

export default CourseViewModal;
