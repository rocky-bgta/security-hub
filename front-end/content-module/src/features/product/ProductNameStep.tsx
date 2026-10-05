import { useEffect, useMemo, useState } from 'react';

import CustomSelect from 'common/CustomSelect';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { useAPI } from 'hooks/UseAPI';
import { ITag } from 'models/Configuration';
import { IResponse, Status } from 'models/Global';
import { ISelectOption, TMultiValue, TSingleValue } from 'models/Input';
import { INewProductData } from 'models/Product';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn, isSuccessResponse } from 'utils/Helper';

interface IProps {
  productData: INewProductData;
  updateProductData: (updates: Partial<INewProductData>) => void;
}

const EXISTING_PRODUCTS = [
  'Security Awareness Training',
  'Cybersecurity Fundamentals',
  'Data Protection Course',
];

const isActiveTag = (status?: string) =>
  String(status || '').toUpperCase() === Status.ACTIVE;

const toSelectOption = (tag: ITag): ISelectOption => ({
  id: tag.id,
  label: tag.name,
  value: tag.name,
});

const ProductNameStep = ({ productData, updateProductData }: IProps) => {
  const [error, setError] = useState<string>('');
  const [isValid, setIsValid] = useState<boolean>(false);
  const [tags, setTags] = useState<Array<ITag>>([]);
  const [isLoadingTags, setIsLoadingTags] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    validateProductName(productData.productName);
  }, [productData.productName]);

  useEffect(() => {
    const fetchTags = async () => {
      setIsLoadingTags(true);
      try {
        const response: IResponse<Array<ITag>> = await apiClient.get(
          API_END_POINTS.TAG_LIST,
        );
        if (isSuccessResponse(response.statusCode)) {
          setTags(Array.isArray(response.data) ? response.data : []);
        }
      } catch (err) {
        console.error('Error fetching product tags:', err);
      } finally {
        setIsLoadingTags(false);
      }
    };

    fetchTags();
  }, [apiClient]);

  const tagOptions = useMemo(
    () => tags.filter(tag => isActiveTag(tag.status)).map(toSelectOption),
    [tags],
  );

  const allTagOptions = useMemo(() => tags.map(toSelectOption), [tags]);

  useEffect(() => {
    if (allTagOptions.length === 0 || productData.tags.length === 0) return;

    const remapped = productData.tags.map(tag => {
      const byName = allTagOptions.find(
        option => option.value.toLowerCase() === tag.toLowerCase(),
      );
      if (byName) return byName.value;
      const byId = allTagOptions.find(option => String(option.id) === tag);
      return byId?.value ?? tag;
    });

    const hasChanged = remapped.some(
      (name, index) => name !== productData.tags[index],
    );
    if (hasChanged) {
      updateProductData({ tags: remapped });
    }
  }, [allTagOptions, productData.tags, updateProductData]);

  const validateProductName = (name: string) => {
    if (!name.trim()) {
      setError('');
      setIsValid(false);
      return;
    }

    if (name.length < 3) {
      setError('Product name must be at least 3 characters long');
      setIsValid(false);
      return;
    }

    if (
      EXISTING_PRODUCTS.some(
        existing => existing.toLowerCase() === name.toLowerCase(),
      )
    ) {
      setError('Product name already exists. Please choose a different name.');
      setIsValid(false);
      return;
    }

    setError('');
    setIsValid(true);
  };

  const handleNameChange = (value: string) => {
    updateProductData({ productName: value });
  };

  const handleDescriptionChange = (value: string) => {
    updateProductData({ productDescription: value });
  };

  const handleDisplayOrderChange = (value: string) => {
    const num = value === '' ? undefined : parseInt(value, 10);
    updateProductData({
      displayOrder: value === '' ? undefined : Number.isNaN(num) ? 0 : num,
    });
  };

  const handleTagsChange = (
    newValue: TMultiValue<ISelectOption> | TSingleValue<ISelectOption>,
  ) => {
    const selectedTags =
      (newValue as TMultiValue<ISelectOption>)?.map(option => option.value) ??
      [];
    updateProductData({ tags: selectedTags });
  };

  const selectedTagOptions = productData.tags.map(tag => {
    const match = allTagOptions.find(
      option =>
        option.value.toLowerCase() === tag.toLowerCase() ||
        String(option.id) === tag,
    );
    return match ?? { id: tag, label: tag, value: tag };
  });

  return (
    <div className="content-space-y-6">
      <div className="content-my-6 content-text-center">
        <h3 className="content-mb-1 content-text-xl content-font-semibold content-text-white">
          Create a Unique Product Name
        </h3>
        <p className="content-text-ash-gray">
          Enter a distinctive name for your training product that reflects the
          type of training or category
        </p>
      </div>

      <div className="content-grid content-grid-cols-1 content-gap-5">
        <div>
          <Label htmlFor="productName">Product Name</Label>
          <Input
            id="productName"
            value={productData.productName}
            onChange={e => handleNameChange(e.target.value)}
            placeholder="Enter product name (e.g., Advanced Security Training)"
            className={cn(
              'content-mt-2 content-h-12 content-text-base',
              error
                ? 'content-border-red-500 focus:content-ring-red-500'
                : isValid
                  ? 'content-border-green-500 focus:content-ring-green-500'
                  : '',
            )}
          />
          {error && (
            <p className="content-text-sm content-text-vibrant-red">{error}</p>
          )}
          <div className="content-mt-2">
            <h4 className="content-mb-2 content-text-sm content-font-medium content-text-cloudy-white">
              Tips for choosing a product name:
            </h4>
            <ul className="content-space-y-1 content-text-xs content-text-ash-gray">
              <li>• Make it descriptive of the training content</li>
              <li>• Keep it unique and memorable</li>
              <li>• Avoid special characters</li>
              <li>• Consider your target audience</li>
            </ul>
          </div>
        </div>

        {/* {isValid && (
          <p className="content-border-green-500 content-bg-green-50 content-text-green-700">
            ✓ Product name is available and valid
          </p>
        )} */}

        <div>
          <Label htmlFor="productDescription">Product Description</Label>
          <div className="content-flex content-flex-col content-space-y-2">
            <textarea
              id="description"
              value={productData.productDescription}
              onChange={e => handleDescriptionChange(e.target.value)}
              placeholder="Enter a brief description of your training product..."
              className="content-input-default content-mt-2 content-min-h-32 content-text-base"
              maxLength={500}
            />
            <p className="content-mt-1 content-text-sm content-text-ash-gray">
              {productData.productDescription.length}/500 characters
            </p>
          </div>

          <div>
            <h4 className="content-mb-2 content-text-sm content-font-medium content-text-cloudy-white">
              Description guidelines:
            </h4>
            <ul className="content-space-y-1 content-text-xs content-text-ash-gray">
              <li>• Explain what learners will gain</li>
              <li>• Mention target audience</li>
              <li>• Highlight key benefits</li>
              <li>• Keep it concise and engaging</li>
            </ul>
          </div>
        </div>

        <div className="content-grid content-grid-cols-2 content-gap-5">
          <div className="content-space-y-2">
            <Label htmlFor="displayOrder">Display Order</Label>
            <Input
              id="displayOrder"
              type="number"
              min={0}
              value={productData.displayOrder ?? ''}
              onChange={e => handleDisplayOrderChange(e.target.value)}
              placeholder="Order for display (e.g., 0, 1, 2...)"
              className="content-h-12 content-text-base"
            />
            <p className="content-mt-1 content-text-sm content-text-ash-gray">
              Lower numbers appear first in product lists
            </p>
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="tags">Tags</Label>
            <CustomSelect
              name="tags"
              value={selectedTagOptions}
              handleChange={handleTagsChange}
              isMulti
              isLoading={isLoadingTags}
              data={tagOptions}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductNameStep;
