import { Link } from 'react-router-dom';

import { IOrganizationInfo } from 'models/Profile';

const organizationData: IOrganizationInfo = {
  organization_name: 'Aspire Tech',
  country: 'Bangladesh',
  organization_domain: 'aspiretss.com',
  time_zone: 'Time zone in Dhaka (GMT+6)',
  organization_email: 'aspire@gmail.com',
  default_language: 'Bangla',
  type_role: 'MSP Client',
  technical_contact: 'Aspire Boy',
  organization_address: 'Dhaka, Bangladesh',
  billing_contact: 'Aspire Boy',
  phone: '+8801711223344',
  industry: 'IT Company',
  organization_size: 'Large',
};

const Profile = () => {
  return (
    <section className="home-ml-3 home-w-full">
      <div>
        <h2 className="home-font-medium home-uppercase">Profile</h2>
      </div>
      <div className="home-mt-10 home-flex">
        <div className="home-w-8/12 home-overflow-x-auto">
          <div className="home-flex home-flex-col">
            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Organization Name
              </div>
              <div className="home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Country
              </div>
            </div>
            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.organization_name}
              </div>
              <div className="home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.country}
              </div>
            </div>

            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Organization Domain
              </div>
              <div className="home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Time Zone
              </div>
            </div>
            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.organization_domain}
              </div>
              <div className="home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.time_zone}
              </div>
            </div>

            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Organization Email
              </div>
              <div className="home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Default Language
              </div>
            </div>
            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.organization_email}
              </div>
              <div className="home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.default_language}
              </div>
            </div>

            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Type / Role
              </div>
              <div className="home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Technical Contact
              </div>
            </div>
            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.type_role}
              </div>
              <div className="home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.technical_contact}
              </div>
            </div>

            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Organization Address
              </div>
              <div className="home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Billing Contact
              </div>
            </div>
            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.organization_address}
              </div>
              <div className="home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.billing_contact}
              </div>
            </div>

            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Phone
              </div>
              <div className="home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Industry
              </div>
            </div>
            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.phone}
              </div>
              <div className="home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.industry}
              </div>
            </div>

            <div className="home-flex home-py-2">
              <div className="home-w-1/2 home-px-4 home-text-lg home-font-semibold home-text-secondary">
                Organization Size
              </div>
            </div>
            <div className="home-flex home-py-2">
              <div className="home-px-4 home-text-lg home-text-stormy-gray">
                {organizationData.organization_size}
              </div>
            </div>
          </div>
        </div>

        <div className="home-w-1/4">
          <h3 className="home-text-xl home-font-semibold home-text-secondary">
            Activation
          </h3>
          <div className="home-mt-5 home-flex home-justify-between">
            <h3 className="home-text-lg home-font-semibold home-text-secondary">
              Creation date
            </h3>
            <p className="home-text-lg home-text-stormy-gray">
              Sunday 05 May 2024
            </p>
          </div>
          <div className="home-flex home-justify-between home-py-5">
            <h3 className="home-text-lg home-font-semibold home-text-secondary">
              Activation date
            </h3>
            <p className="home-text-lg home-text-stormy-gray">
              Sunday 05 May 2024
            </p>
          </div>
          <div className="home-flex home-justify-between">
            <h3 className="home-text-lg home-font-semibold home-text-secondary">
              Last update
            </h3>
            <p className="home-text-lg home-text-stormy-gray">
              Sunday 05 May 2024
            </p>
          </div>
        </div>
      </div>
      <div className="home-my-10 home-flex home-w-11/12 home-justify-between">
        <div className="home-w-1/2 home-overflow-x-auto">
          <div className="home-w-full">
            <div className="home-mb-4 home-px-4 home-py-2 home-text-left home-text-lg home-font-semibold home-text-secondary">
              Vital Statistics
            </div>

            <div className="home-flex home-justify-between home-px-4 home-py-2">
              <div className="home-text-lg home-text-secondary">
                Company Status
              </div>
              <div className="home-text-lg">Active</div>
            </div>

            <div className="home-flex home-justify-between home-px-4 home-py-2">
              <div className="home-text-lg home-text-secondary">
                Number of Active Domains
              </div>
              <div className="home-text-lg">0</div>
            </div>

            <div className="home-flex home-justify-between home-px-4 home-py-2">
              <div className="home-text-lg home-text-secondary">
                Number of User Groups
              </div>
              <div className="home-text-lg">0</div>
            </div>

            <div className="home-flex home-justify-between home-px-4 home-py-2">
              <div className="home-text-lg home-text-secondary">
                Number of Active Users
              </div>
              <div className="home-text-lg">2</div>
            </div>

            <div className="home-flex home-justify-between home-px-4 home-py-2">
              <div className="home-text-lg home-text-secondary">
                Number of Active Functional Accounts
              </div>
              <div className="home-text-lg">0</div>
            </div>

            <div className="home-flex home-justify-between home-px-4 home-py-2">
              <div className="home-text-lg home-text-secondary">
                Number of Registered Email Addresses
              </div>
              <div className="home-text-lg">2</div>
            </div>

            <div className="home-flex home-justify-between home-px-4 home-py-2">
              <div className="home-text-lg home-text-secondary">
                Number of SMTP Discovery Email Addresses
              </div>
              <div className="home-text-lg">0</div>
            </div>

            <div className="home-flex home-justify-between home-px-4 home-py-2">
              <div className="home-text-lg home-text-secondary">
                Total Number of Active Email Addresses
              </div>
              <div className="home-text-lg">2</div>
            </div>
          </div>
        </div>

        <div className="home-w-2/5">
          <h3 className="home-text-xl home-font-semibold home-text-secondary">
            Products Overview
          </h3>
          <div className="home-mt-4 home-text-lg home-text-secondary">
            <p>Number of Licensed Users: 100</p>
          </div>
          <h3 className="home-mt-10 home-text-xl home-font-semibold home-text-secondary">
            Security Awareness
          </h3>
          <div className="home-mt-4 home-flex home-justify-between home-text-lg">
            <p className="home-font-semibold home-text-secondary">Start Date</p>
            <p className="home-text-stormy-gray">Sunday 05 May 2024</p>
          </div>
          <div className="home-mt-4 home-flex home-justify-between home-text-lg">
            <p className="home-font-semibold home-text-secondary">Status</p>
            <p className="home-text-stormy-gray">
              Awaiting Domain Verification
            </p>
          </div>
          <div className="home-mt-4 home-flex home-justify-between home-text-lg">
            <p className="home-font-semibold home-text-secondary">
              Accepted EULA
            </p>
            <p className="home-text-stormy-gray">Yes</p>
          </div>
          <div className="home-float-right home-mt-4">
            <Link to={'/'} className="home-text-blue-500 home-underline">
              View EULA
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
};

export default Profile;
