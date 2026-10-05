import { useCallback, useEffect, useMemo, useState } from 'react';

import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { CampaignChannel } from 'models/Campaign';
import { IList, IResponse } from 'models/Global';
import { IAssignedLicense } from 'models/License';
import { API_END_POINTS } from 'routes/APIEndpoints';

export const CHANNEL_PRODUCT_TAG: Record<CampaignChannel, string> = {
  [CampaignChannel.EMAIL]: 'Phishing',
  [CampaignChannel.SMS]: 'Smishing',
  [CampaignChannel.VOICE]: 'Vishing',
};

export interface IAssignedPackageOption {
  id: string;
  packageId: string;
  packageName: string;
  expiryDate: string | null;
}

const productHasTag = (
  tags: string[] | undefined,
  tagName: string,
  tagId?: string,
) =>
  (tags ?? []).some(
    tag =>
      tag.toLowerCase() === tagName.toLowerCase() ||
      (Boolean(tagId) && tag === tagId),
  );

export const useAssignedPackages = (channel: CampaignChannel) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();
  const [loading, setLoading] = useState(true);
  const [licenses, setLicenses] = useState<IAssignedLicense[]>([]);
  const [productId, setProductId] = useState('');
  const [error, setError] = useState('');

  const preferredTagName =
    CHANNEL_PRODUCT_TAG[channel] ?? CHANNEL_PRODUCT_TAG[CampaignChannel.EMAIL];

  const fetchAssignedPackages = useCallback(async () => {
    if (!userInfo?.userId) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');

    try {
      let preferredTagId: string | undefined;
      try {
        const tagsResponse: IResponse<Array<{ id: string; name: string }>> =
          await apiClient.get(API_END_POINTS.CMS_TAG_LIST);
        const tagCatalog = Array.isArray(tagsResponse.data)
          ? tagsResponse.data
          : [];
        preferredTagId = tagCatalog.find(
          tag => tag.name.toLowerCase() === preferredTagName.toLowerCase(),
        )?.id;
      } catch (tagError) {
        console.error('Error fetching product tags:', tagError);
      }

      const response: IResponse<IList<IAssignedLicense>> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
          ':clientAdminId',
          userInfo.userId,
        ) + 'offset=0&pageSize=1000',
      );

      const items = response.data?.items ?? [];
      setLicenses(items);

      const licensesWithProduct = items.filter(item => item.product);
      const taggedProduct = licensesWithProduct.find(item =>
        productHasTag(item.product?.tags, preferredTagName, preferredTagId),
      );
      const fallback = [...licensesWithProduct].sort(
        (a, b) =>
          (productHasTag(a.product.tags, preferredTagName, preferredTagId)
            ? 0
            : 1) -
          (productHasTag(b.product.tags, preferredTagName, preferredTagId)
            ? 0
            : 1),
      )[0];
      const chosen = taggedProduct ?? fallback;

      if (chosen) {
        setProductId(chosen.productId);
      } else {
        setProductId('');
        setError('No licensed product is assigned for this campaign channel.');
      }
    } catch (fetchError) {
      console.error('Error fetching assigned packages:', fetchError);
      setError('Failed to load packages. Please try again.');
      setLicenses([]);
      setProductId('');
    } finally {
      setLoading(false);
    }
  }, [apiClient, preferredTagName, userInfo?.userId]);

  useEffect(() => {
    fetchAssignedPackages();
  }, [fetchAssignedPackages]);

  const productName = useMemo(() => {
    const license = licenses.find(item => item.productId === productId);
    return license?.product?.productName ?? '';
  }, [licenses, productId]);

  const packages = useMemo<IAssignedPackageOption[]>(
    () =>
      licenses
        .filter(license => license.productId === productId)
        .filter(license => license.packageDetails?.packageStatus === 'ENABLED')
        .map(license => ({
          id: license.id,
          packageId: license.packageId,
          packageName: license.packageDetails?.packageName ?? 'Untitled package',
          expiryDate: license.expiryDate,
        })),
    [licenses, productId],
  );

  return {
    loading,
    error,
    productId,
    productName,
    packages,
  };
};
