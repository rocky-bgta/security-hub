import { useEffect, useState } from 'react';
import { Fragment } from 'react/jsx-runtime';

import useStore from 'hooks/UseStore';
import useAPI from 'hooks/UseAPI';
import { getToken, setToken } from 'utils/TokenStorage';
import { LocalStorageKey } from 'utils/Constants';
import { IResponse } from 'models/Context';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import QuickStartModal from 'features/quick-start/QuickStartModal';
import RegularClientAdminDashboard from 'features/client-admin-dashboard/RegularClientAdminDashboard';
import { ClientProductTag } from 'models/Global';

const ClientAdminDashboard = () => {
  const [showQuickStartModal, setShowQuickStartModal] =
    useState<boolean>(false);
  const [requiredInfoData, setRequiredInfoData] = useState<{
    hasBranding: boolean;
    hasUser: boolean;
    productAssigned: boolean;
    hasCertificateTemplate: boolean;
    clientType: string;
    productTags: Array<ClientProductTag>;
  } | null>(null);
  const { userInfo } = useStore();
  const apiClient = useAPI();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response: IResponse<{
          hasBranding: boolean;
          hasUser: boolean;
          productAssigned: boolean;
          hasCertificateTemplate: boolean;
        }> = await apiClient.get(API_END_POINTS.REQUIRED_INFO);

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }

        const productTags = userInfo.clientProductTags ?? [];
        const hasSecurity = productTags.includes(ClientProductTag.SECURITY);
        const hasPhishing = productTags.includes(ClientProductTag.PHISHING);
        const isPhishingOnly = hasPhishing && !hasSecurity;

        setRequiredInfoData({
          ...response.data,
          clientType: userInfo.onboardBy,
          productTags: productTags as Array<ClientProductTag>,
        });

        const shouldShowModal =
          userInfo.onboardBy === 'TRIAL'
            ? !response.data.hasBranding ||
              !response.data.hasUser ||
              !response.data.productAssigned
            : isPhishingOnly
              ? !response.data.hasBranding
              : !response.data.hasBranding ||
                !response.data.hasCertificateTemplate;

        if (shouldShowModal) {
          setShowQuickStartModal(true);
        }
      } catch (error) {
        console.log(error);
      }
    };

    const alreadyShownQuickStart = getToken(LocalStorageKey.QUICK_START);
    if (!alreadyShownQuickStart) {
      fetchData();
    }
  }, [userInfo, apiClient]);

  const closeQuickStartModal = (skip = true) => {
    setShowQuickStartModal(false);
    setToken(LocalStorageKey.QUICK_START, 'true');

    // Reload after a successful finish so dashboard widgets refetch updated setup data
    if (!skip) window.location.reload();
  };

  return (
    <Fragment>
      <RegularClientAdminDashboard />

      {showQuickStartModal && (
        <QuickStartModal
          isOpen={showQuickStartModal}
          data={requiredInfoData}
          onClose={() => closeQuickStartModal(true)}
          onFinish={() => closeQuickStartModal(false)}
        />
      )}
    </Fragment>
  );
};

export default ClientAdminDashboard;
