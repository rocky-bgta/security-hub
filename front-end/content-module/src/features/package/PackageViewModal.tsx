import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import InfoViewCard from 'components/InfoViewCard';
import { useAPI } from 'hooks/UseAPI';
import { CourseStatus, IResponse, Status } from 'models/Global';
import { IPackage } from 'models/Package';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { HumanizeDate } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  packageId: string;
  onClose: () => void;
}

const PackageViewModal = ({ isOpen, onClose, packageId }: IProps) => {
  const [product, setProduct] = useState<IPackage>();
  const apiClient = useAPI();

  useEffect(() => {
    if (isOpen) {
      fetchPackageDetails();
    }
  }, [isOpen]);

  const fetchPackageDetails = async () => {
    try {
      const response: IResponse<IPackage> = await apiClient.get(
        API_END_POINTS.PACKAGE_DETAILS + packageId,
      );
      setProduct(response.data);
    } catch (error) {
      console.error('Error fetching package data:', error);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      disableOutsideClick={true}
      className="content-h-auto content-w-3/4"
    >
      <ModalHeader onClose={onClose}>
        <p className="content-py-5 content-text-lg content-font-medium">
          View Product
        </p>
      </ModalHeader>
      <ModalBody className="content-my-6">
        <InfoViewCard
          title={product?.packageName ?? ''}
          description={product?.packageDescription ?? ''}
          createdAt={HumanizeDate(product?.createdAt)}
          updatedAt={HumanizeDate(product?.updatedAt)}
          status={product?.packageStatus == Status.ENABLED}
          features={product?.features?.map((feature: any) => ({
            featureName: feature.name,
          }))}
          courseList={product?.courseIds}
        />
      </ModalBody>
    </Modal>
  );
};

export default PackageViewModal;
