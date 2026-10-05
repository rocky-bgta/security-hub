import { Fragment } from 'react/jsx-runtime';

import { Button } from 'common/Button';
import UserHeading from 'components/UserHeading';
import { useState } from 'react';

const Campaigns = () => {
  const [showModal, setShowModal] = useState(false);
  const handleHideModal = () => {
    setShowModal(false);
  };
  return (
    <Fragment>
      <UserHeading variant="title" text="Campaigns" className="content-mb-6" />
      <p className="content-text-white">No campaigns yet</p>
      <Button onClick={() => setShowModal(!showModal)}>Click</Button>

      {/* <CourseCompleted isOpen={showModal} onClose={handleHideModal} /> */}
      {/* <RetakeCourse isOpen={showModal} onClose={handleHideModal} /> */}
    </Fragment>
  );
};

export default Campaigns;
