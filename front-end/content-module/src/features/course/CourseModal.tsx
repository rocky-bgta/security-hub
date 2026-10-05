import { zodResolver } from '@hookform/resolvers/zod';
import { Fragment, useEffect, useRef, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Card, CardDescription, CardTitle } from 'common/Card';
import CustomSelect from 'common/CustomSelect';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import SearchSelect from 'components/SearchSelect';
import TagSelector from 'components/TagSelector';
import { useAPI } from 'hooks/UseAPI';
import useDropDown from 'hooks/UseDropDown';
import { useUploader } from 'hooks/UseUploader';
import { ChevronDown, Plus, Trash2 } from 'lucide-react';
import {
  ICategory,
  ICompliance,
  IContentType,
  ICountry,
} from 'models/Configuration';
import { ICourse } from 'models/Course';
import { IDropdownOption } from 'models/DropDown';
import { IList, IResponse } from 'models/Global';
import { ISelectOption, TMultiValue, TSingleValue } from 'models/Input';
import { ITopic } from 'models/Topic';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  CourseFormSchema,
  DefaultCourseFormValues,
  TCourseFormFields,
} from 'schemas/CourseSchema';
import { FILE_PATH_PREFIX, ValidImageFormats } from 'utils/Constants';

interface IProps {
  topicId?: string;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (id: string) => void;
}

interface ProductPackagePair {
  id: string;
  productId: string;
  packageIds: string[];
  productName: string;
}

// Interface for API response
interface IPackage {
  id: string;
  packageName: string;
  productId: string;
  featureIds: string[];
  price: number;
  packageStatus: string;
  basePackageId: string;
}

interface IProductWithPackages {
  productId: string;
  productName: string;
  productDescription: string;
  productStatus: string;
  thumbnailUrl: string;
  createdAt: string;
  updatedAt: string;
  lastModifiedBy: string;
  packages: IPackage[];
}

interface AdvancedDropdownOption {
  id: string;
  name: string;
}

const ADVANCED_FIELD_CONFIG = [
  { key: 'payloadType', label: 'Payload Type', idField: 'payloadTypeId', nameField: 'payloadTypeName', fetchKey: 'fetchPayloadTypes' },
  { key: 'difficulty', label: 'Difficulty', idField: 'difficultyId', nameField: 'difficultyName', fetchKey: 'fetchDifficulty' },
  { key: 'tone', label: 'Tone', idField: 'toneId', nameField: 'toneName', fetchKey: 'fetchTones' },
  { key: 'attackerPersona', label: 'Attacker Persona', idField: 'attackerPersonaId', nameField: 'attackerPersonaName', fetchKey: 'fetchAttackerPersonas' },
  { key: 'socialEngineeringStrategy', label: 'Social Engineering Strategy', idField: 'socialEngineeringStrategyId', nameField: 'socialEngineeringStrategyName', fetchKey: 'fetchSocialEngineeringStrategies' },
  { key: 'campaignObjective', label: 'Campaign Objective', idField: 'campaignObjectiveId', nameField: 'campaignObjectiveName', fetchKey: 'fetchCampaignObjectives' },
  { key: 'triggerEvent', label: 'Trigger Event', idField: 'triggerEventId', nameField: 'triggerEventName', fetchKey: 'fetchTriggerEvents' },
  { key: 'attackTechnique', label: 'Attack Technique', idField: 'attackTechniqueId', nameField: 'attackTechniqueName', fetchKey: 'fetchAttackTechniques' },
  { key: 'emotionalTrigger', label: 'Emotional Trigger', idField: 'emotionalTriggerId', nameField: 'emotionalTriggerName', fetchKey: 'fetchEmotionalTriggers' },
  { key: 'urgencyLevel', label: 'Urgency Level', idField: 'urgencyLevelId', nameField: 'urgencyLevelName', fetchKey: 'fetchUrgencyLevels' },
  { key: 'brand', label: 'Brand', idField: 'brandId', nameField: 'brandName', fetchKey: 'fetchBrands' },
  { key: 'callToAction', label: 'Call to Action', idField: 'callToActionId', nameField: 'callToActionName', fetchKey: 'fetchCallToActions' },
] as const;

type AdvancedFieldKey = (typeof ADVANCED_FIELD_CONFIG)[number]['key'] | 'industry' | 'subIndustry';

const CourseModal = ({ isOpen, onSubmit, onClose, topicId = '' }: IProps) => {
  const apiClient = useAPI();
  const {
    control,
    reset,
    clearErrors,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm<TCourseFormFields>({
    resolver: zodResolver(CourseFormSchema),
    defaultValues: DefaultCourseFormValues,
    mode: 'onChange',
  });

  const imageUploaderRef = useRef<FileUploaderHandle>(null);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [chapterIds, setChapterIds] = useState<Array<string>>([]);
  const [topicDetails, setTopicDetails] = useState<ITopic | null>(null);
  const [isDataLoaded, setIsDataLoaded] = useState<boolean>(false);
  const [categoryOptions, setCategoryOptions] = useState<Array<ISelectOption>>(
    [],
  );
  const [contentTypeOptions, setContentTypeOptions] = useState<
    Array<ISelectOption>
  >([]);
  const [complianceOptions, setComplianceOptions] = useState<
    Array<ISelectOption>
  >([]);
  const [countryOptions, setCountryOptions] = useState<Array<ISelectOption>>(
    [],
  );

  // Updated state for dynamic product/package data
  const [productOptions, setProductOptions] = useState<Array<ISelectOption>>(
    [],
  );
  const [packageOptionsByProduct, setPackageOptionsByProduct] = useState<
    Record<string, Array<ISelectOption>>
  >({});
  const [productData, setProductData] = useState<IProductWithPackages[]>([]);

  // Product-Package pairs state
  const [productPackagePairs, setProductPackagePairs] = useState<
    ProductPackagePair[]
  >([{ id: '1', productId: '', packageIds: [], productName: '' }]);

  const [thumbnail, setThumbnail] = useState<{
    link: string;
    selectedFile: File | null;
  }>({ link: '', selectedFile: null });

  const { uploadFile } = useUploader();

  // Advanced options state
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [tags, setTags] = useState<string[]>([]);

  const {
    fetchPayloadTypes,
    fetchDifficulty,
    fetchTones,
    fetchAttackerPersonas,
    fetchSocialEngineeringStrategies,
    fetchCampaignObjectives,
    fetchTriggerEvents,
    fetchAttackTechniques,
    fetchEmotionalTriggers,
    fetchUrgencyLevels,
    fetchBrands,
    fetchCallToActions,
    fetchIndustries,
    fetchSubIndustries,
  } = useDropDown();

  const dropdownFetchers: Record<string, () => Promise<IList<IDropdownOption>>> = {
    fetchPayloadTypes,
    fetchDifficulty,
    fetchTones,
    fetchAttackerPersonas,
    fetchSocialEngineeringStrategies,
    fetchCampaignObjectives,
    fetchTriggerEvents,
    fetchAttackTechniques,
    fetchEmotionalTriggers,
    fetchUrgencyLevels,
    fetchBrands,
    fetchCallToActions,
  };

  const [advancedDropdowns, setAdvancedDropdowns] = useState<
    Record<AdvancedFieldKey, IDropdownOption[]>
  >({
    payloadType: [],
    difficulty: [],
    tone: [],
    attackerPersona: [],
    socialEngineeringStrategy: [],
    campaignObjective: [],
    triggerEvent: [],
    attackTechnique: [],
    emotionalTrigger: [],
    urgencyLevel: [],
    brand: [],
    callToAction: [],
    industry: [],
    subIndustry: [],
  });

  const [advancedSelections, setAdvancedSelections] = useState<
    Record<AdvancedFieldKey, AdvancedDropdownOption[]>
  >({
    payloadType: [],
    difficulty: [],
    tone: [],
    attackerPersona: [],
    socialEngineeringStrategy: [],
    campaignObjective: [],
    triggerEvent: [],
    attackTechnique: [],
    emotionalTrigger: [],
    urgencyLevel: [],
    brand: [],
    callToAction: [],
    industry: [],
    subIndustry: [],
  });

  const getOptionsFromIds = (
    ids: string[],
    optionsList: ISelectOption[],
  ): ISelectOption[] => {
    return optionsList.filter(option => ids.includes(option.id.toString()));
  };

  // Helper function to get single option from ID
  const getOptionFromId = (
    id: string,
    optionsList: ISelectOption[],
  ): ISelectOption | null => {
    return optionsList.find(option => option.value === id) || null;
  };

  // Fetch advanced dropdown options
  useEffect(() => {
    if (!isOpen) return;

    ADVANCED_FIELD_CONFIG.forEach(async field => {
      const fetcher = dropdownFetchers[field.fetchKey];
      if (fetcher) {
        const data = await fetcher();
        setAdvancedDropdowns(prev => ({ ...prev, [field.key]: data.items }));
      }
    });

    const loadIndustries = async () => {
      try {
        const response = await fetchIndustries();
        if (response.statusCode === 200 && Array.isArray(response.data)) {
          setAdvancedDropdowns(prev => ({
            ...prev,
            industry: response.data.map(item => ({ id: item.id, name: item.name })),
          }));
        }
      } catch (e) {
        console.error('Error loading industries:', e);
      }
    };

    const loadSubIndustries = async () => {
      try {
        const response = await fetchSubIndustries();       
        if (response.statusCode === 200 && Array.isArray(response.data)) {
          setAdvancedDropdowns(prev => ({
            ...prev,
            subIndustry: response.data.map(item => ({ id: item.id, name: item.name })),
          }));
        }
      } catch (e) {
        console.error('Error loading sub-industries:', e);
      }
    };

    void loadIndustries();
    void loadSubIndustries();
  }, [isOpen]);

  // Fetch product and package data
  useEffect(() => {
    if (!isOpen) return;

    fetchProductList();
  }, [isOpen]);

  // Initialize options on modal open
  useEffect(() => {
    if (!isOpen) return;
    setIsDataLoaded(false);

    clearErrors();

    // Fetch other options
    Promise.all([
      apiClient.get(API_END_POINTS.CATEGORY_LIST),
      apiClient.get(API_END_POINTS.CONTENT_TYPE_LIST),
      apiClient.get(API_END_POINTS.COMPLIANCE_LIST),
      apiClient.get(API_END_POINTS.COUNTRY_LIST),
    ])
      .then(
        ([categoryRes, contentTypeRes, complianceRes, countryRes]: [
          IResponse<Array<ICategory>>,
          IResponse<Array<IContentType>>,
          IResponse<Array<ICompliance>>,
          IResponse<Array<ICountry>>,
        ]) => {
          const categoryOptions = categoryRes.data?.map((item: ICategory) => ({
            id: item.id,
            label: item.categoryName,
            value: item.id,
          }));
          const contentTypeOptions = contentTypeRes.data.map(
            (item: IContentType) => ({
              id: item.id,
              label: item.typeName,
              value: item.id,
            }),
          );
          const complianceOptions = complianceRes.data.map(
            (item: ICompliance) => ({
              id: item.id,
              label: item.complianceName,
              value: item.id,
            }),
          );

          const countryOptions = countryRes.data.map((item: ICountry) => ({
            id: item.id,
            label: item.name,
            value: item.id,
          }));
          setCategoryOptions(categoryOptions);
          setContentTypeOptions(contentTypeOptions);
          setComplianceOptions(complianceOptions);
          setCountryOptions(countryOptions);
          setIsDataLoaded(true);
        },
      )
      .catch(error => {
        console.error('Error fetching data:', error);
        setIsDataLoaded(true);
      })
      .finally(() => setIsDataLoaded(true));
  }, [isOpen]);

  // Get Individual Topic Details
  useEffect(() => {
    if (!isOpen || !topicId || !isDataLoaded) return;

    fetchTopicDetails();
  }, [
    topicId,
    isOpen,
    isDataLoaded,
    countryOptions,
    categoryOptions,
    complianceOptions,
    contentTypeOptions,
    productData,
  ]);

  const fetchProductList = async () => {
    try {
      const response: IResponse<IList<IProductWithPackages>> =
        await apiClient.get(API_END_POINTS.PRODUCT_LIST + 'pageSize=1000');

      if (response.statusCode === 200 && response.data?.items) {
        const products = response.data.items;
        setProductData(products);

        // Create product options
        const productOptions = products.map(
          (product: IProductWithPackages) => ({
            id: product.productId,
            label: product.productName,
            value: product.productId,
          }),
        );
        setProductOptions(productOptions);

        // Create package options grouped by product
        const packagesByProduct: Record<string, Array<ISelectOption>> = {};
        products.forEach((product: IProductWithPackages) => {
          packagesByProduct[product.productId] = product.packages
            .filter(pkg => pkg.packageStatus === 'ENABLED') // Only enabled packages
            .map((pkg: IPackage) => ({
              id: pkg.id,
              label: pkg.packageName,
              value: pkg.id,
              productName: product.productName,
            }));
        });

        setPackageOptionsByProduct(packagesByProduct);
      } else {
        console.error('Invalid product data response:', response);
        toast.error('Error loading product data');
      }
    } catch (error) {
      console.error('Error fetching Product data:', error);
      toast.error('Error loading Product details');
    }
  };

  const fetchTopicDetails = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.TOPIC_DETAILS + topicId,
      );

      const topicData = response.data;
      // Convert IDs to option objects
      const selectedCountries = getOptionsFromIds(
        topicData.countryDetails.map((item: any) => item.id) || [],
        countryOptions,
      );
      const selectedCategories = getOptionsFromIds(
        topicData.categoryDetails.map((item: any) => item.id) || [],
        categoryOptions,
      );
      const selectedCompliances = getOptionsFromIds(
        topicData.complianceDetails.map((item: any) => item.id) || [],
        complianceOptions,
      );
      const selectedContentType = getOptionFromId(
        topicData.contentTypeDetails.id,
        contentTypeOptions,
      );

      // Set form values with proper option objects
      setValue('topicName', topicData.topicName || '');
      setValue('topicDescription', topicData.topicDescription || '');
      setValue('duration', topicData.duration?.toString() || '');
      setValue('country', selectedCountries as any);
      setValue('category', selectedCategories as any);
      setValue('compliance', selectedCompliances as any);
      setValue('contentType', selectedContentType?.value.toString() || '');
      setValue('duration', topicData.durationMinutes?.toString() || '');
      setValue('topicDescription', topicData.description || '');

      // Set product-package pairs with product name lookup
      if (topicData.productPackages && topicData.productPackages.length > 0) {
        const mappedPairs = topicData.productPackages.map(
          (productPackage: any, index: number) => {
            const productId = productPackage.productDetails?.id;
            const productName =
              productPackage.productDetails?.productName || '';

            // Extract package IDs from the nested packageDetails array
            const packageIds =
              productPackage.packageDetails?.map((pkg: any) => pkg.id) || [];

            return {
              id: (Date.now() + index).toString(),
              productId: productId || '',
              productName: productName,
              packageIds: packageIds,
            };
          },
        );

        setProductPackagePairs(mappedPairs);
      } else {
        // Reset to default if no product packages
        setProductPackagePairs([
          { id: '1', productId: '', packageIds: [], productName: '' },
        ]);
      }

      // Set advanced selections from topic data
      ADVANCED_FIELD_CONFIG.forEach(field => {
        const fieldData = topicData[field.key];
        if (Array.isArray(fieldData)) {
          const mapped = fieldData.map((item: any) => ({
            id: item[field.idField] || item.id || '',
            name: item[field.nameField] || item.name || '',
          }));
          setAdvancedSelections(prev => ({ ...prev, [field.key]: mapped }));
        }
      });

      if (Array.isArray(topicData.industry)) {
        setAdvancedSelections(prev => ({
          ...prev,
          industry: topicData.industry.map((item: any) => ({
            id: item.industryId || item.id || '',
            name: item.industryName || item.name || '',
          })),
        }));
      }
      if (Array.isArray(topicData.subIndustry)) {
        setAdvancedSelections(prev => ({
          ...prev,
          subIndustry: topicData.subIndustry.map((item: any) => ({
            id: item.subIndustryId || item.id || '',
            name: item.subIndustryName || item.name || '',
          })),
        }));
      }

      // Set tags
      if (Array.isArray(topicData.tags)) {
        setTags(topicData.tags);
      }

      // Set thumbnail
      setThumbnail({
        link: topicData.thumbnailUrl || '',
        selectedFile: null,
      });

      // Set chapter IDs
      const chapterIds =
        topicData.chapterIds?.map((chapter: any) =>
          typeof chapter === 'object' ? chapter.id : chapter,
        ) || [];
      setChapterIds(chapterIds);
      setTopicDetails(topicData);
    } catch (error) {
      console.error('Error fetching course data:', error);
      toast.error('Error loading topic details');
    }
  };

  const handleAddProduct = () => {
    const newPair: ProductPackagePair = {
      id: Date.now().toString(),
      productId: '',
      productName: '',
      packageIds: [],
    };
    setProductPackagePairs(prev => [...prev, newPair]);
  };

  const handleRemoveProduct = (pairId: string) => {
    if (productPackagePairs.length > 1) {
      setProductPackagePairs(prev => prev.filter(pair => pair.id !== pairId));
    }
  };

  const handleProductChange = (pairId: string, productId: string) => {
    // Find the product name from productData
    const selectedProduct = productData.find(p => p.productId === productId);

    setProductPackagePairs(prev =>
      prev.map(pair =>
        pair.id === pairId
          ? {
            ...pair,
            productId,
            productName: selectedProduct?.productName || '',
            packageIds: [], // Reset packages when product changes
          }
          : pair,
      ),
    );
  };

  const handlePackageChange = (pairId: string, packageIds: string[]) => {
    setProductPackagePairs(prev =>
      prev.map(pair => (pair.id === pairId ? { ...pair, packageIds } : pair)),
    );
  };

  const handleFormSubmit = async (fields: TCourseFormFields) => {
    setSubmitting(true);

    try {
      let thumbnailLink = thumbnail.link;

      if (thumbnail.selectedFile) {
        console.log('Uploading thumbnail...');
        const { url, error } = await uploadFile(
          thumbnail.selectedFile,
          'THUMBNAIL',
        );
        if (error) {
          toast.error(error);
          return;
        }
        thumbnailLink = url;
      }

      // Filter out incomplete product-package pairs
      const validProductPackagePairs = productPackagePairs.filter(
        pair => pair.productId && pair.packageIds.length > 0,
      );

      if (validProductPackagePairs.length === 0) {
        toast.error('Please select at least one product with packages');
        return;
      }

      const advancedPayload = ADVANCED_FIELD_CONFIG.reduce(
        (acc, field) => {
          acc[field.key] = advancedSelections[field.key].map(item => ({
            [field.idField]: item.id,
            [field.nameField]: item.name,
          }));
          return acc;
        },
        {} as Record<string, Array<Record<string, string>>>,
      );

      advancedPayload.industry = advancedSelections.industry.map(
        item => ({ industryId: item.id, industryName: item.name }),
      );
      advancedPayload.subIndustry = advancedSelections.subIndustry.map(
        item => ({ subIndustryId: item.id, subIndustryName: item.name }),
      );

      const createPayload = {
        topicName: fields.topicName,
        description: fields.topicDescription,
        countryIds: fields.country.map(country => country.id),
        complianceIds: fields.compliance.map(compliance => compliance.id),
        categoryIds: fields.category.map(category => category.id),
        contentTypeId: fields.contentType,
        durationMinutes: Number(fields.duration),
        productPackages: validProductPackagePairs.map(pair => ({
          productId: pair.productId,
          productName: pair.productName,
          packageIds: pair.packageIds,
        })),
        chapterIds: [],
        thumbnailUrl: thumbnailLink,
        ...advancedPayload,
        tags,
      };

      const editPayload = {
        topicName: fields.topicName,
        description: fields.topicDescription,
        countryIds: fields.country.map(country => country.id),
        complianceIds: fields.compliance.map(compliance => compliance.id),
        categoryIds: fields.category.map(category => category.id),
        contentTypeId: fields.contentType,
        durationMinutes: Number(fields.duration),
        productPackages: validProductPackagePairs.map(pair => ({
          productId: pair.productId,
          productName: pair.productName,
          packageIds: pair.packageIds,
        })),
        totalContentCount: topicDetails?.totalContentCount,
        chapterIds: chapterIds,
        thumbnailUrl: thumbnailLink,
        ...advancedPayload,
        tags,
      };

      let response: IResponse<ICourse>;

      if (topicId) {
        response = await apiClient.put(API_END_POINTS.TOPIC_UPDATE + topicId, {
          data: editPayload,
        });
      } else {
        response = await apiClient.post(API_END_POINTS.TOPIC_CREATE, {
          data: createPayload,
        });
      }

      if ([200, 201].includes(response.statusCode)) {
        handleClose();
        onSubmit(response.data.id);
        toast.success(response.message);
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error saving topic:', error);
      toast.error('An error occurred while saving the topic');
    } finally {
      setSubmitting(false);
      if (imageUploaderRef.current) {
        imageUploaderRef.current.clearFiles();
      }
      setThumbnail({ link: '', selectedFile: null });
    }
  };

  const handleAdvancedSelectionChange = (
    key: AdvancedFieldKey,
    newValue: TMultiValue<ISelectOption> | TSingleValue<ISelectOption>,
  ) => {
    const selected = Array.isArray(newValue)
      ? newValue.map(item => ({ id: item.value, name: item.label }))
      : [];
    setAdvancedSelections(prev => ({ ...prev, [key]: selected }));
  };

  const handleClose = () => {
    reset(DefaultCourseFormValues);
    setProductPackagePairs([
      { id: '1', productId: '', packageIds: [], productName: '' },
    ]);
    setThumbnail({ link: '', selectedFile: null });
    setShowAdvanced(false);
    setTags([]);
    setAdvancedSelections({
      payloadType: [],
      difficulty: [],
      tone: [],
      attackerPersona: [],
      socialEngineeringStrategy: [],
      campaignObjective: [],
      triggerEvent: [],
      attackTechnique: [],
      emotionalTrigger: [],
      urgencyLevel: [],
      brand: [],
      callToAction: [],
      industry: [],
      subIndustry: [],
    });
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      disableOutsideClick={true}
      variant="user"
      className="content-h-auto content-w-2/3"
    >
      <ModalHeader onClose={handleClose}>
        <p className="content-py-5 content-text-lg content-font-medium content-text-white">
          {topicId ? 'Edit ' : 'Create '} Topic
        </p>
      </ModalHeader>
      <ModalBody>
        <form
          onSubmit={handleSubmit(handleFormSubmit)}
          className="content-grid content-grid-cols-1 content-gap-6 content-py-6 lg:content-grid-cols-2"
        >
          {/* Topic Name */}
          <div className="content-flex content-flex-col">
            <Controller
              name="topicName"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="title">Topic Name <span className="content-text-vibrant-red">*</span></Label>
                  <Input
                    key="title"
                    id="title"
                    name="Topic Name"
                    value={value || ''}
                    placeholder="Topic Name"
                    onChange={e => onChange(e.target.value)}
                  />
                </Fragment>
              )}
            />
            {errors['topicName'] && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors['topicName']?.message}
              </p>
            )}
          </div>

          {/* Country */}
          <div className="content-flex content-flex-col">
            <Controller
              name="country"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="country">Country <span className="content-text-vibrant-red">*</span></Label>
                  <CustomSelect
                    data={countryOptions}
                    key="country"
                    isMulti
                    name="country"
                    value={value || []}
                    handleChange={(
                      newValue:
                        | TMultiValue<ISelectOption>
                        | TSingleValue<ISelectOption>,
                    ) => onChange(newValue || [])}
                  />
                </Fragment>
              )}
            />
            {errors['country'] && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors['country']?.message}
              </p>
            )}
          </div>

          {/* Compliance */}
          <div className="content-flex content-flex-col">
            <Controller
              name="compliance"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="compliance">Compliance <span className="content-text-vibrant-red">*</span></Label>
                  <CustomSelect
                    data={complianceOptions}
                    key="compliance"
                    isMulti
                    name="compliance"
                    value={value || []}
                    handleChange={(
                      newValue:
                        | TMultiValue<ISelectOption>
                        | TSingleValue<ISelectOption>,
                    ) => onChange(newValue || [])}
                  />
                </Fragment>
              )}
            />
            {errors['compliance'] && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors['compliance']?.message}
              </p>
            )}
          </div>

          {/* Category */}
          <div className="content-flex content-flex-col">
            <Controller
              name="category"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="category">Category <span className="content-text-vibrant-red">*</span></Label>
                  <CustomSelect
                    data={categoryOptions}
                    key="category"
                    isMulti
                    name="category"
                    value={value || []}
                    handleChange={(
                      newValue:
                        | TMultiValue<ISelectOption>
                        | TSingleValue<ISelectOption>,
                    ) => onChange(newValue || [])}
                  />
                </Fragment>
              )}
            />
            {errors['category'] && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors['category']?.message}
              </p>
            )}
          </div>

          {/* Content Type */}
          <div className="content-flex content-flex-col">
            <Controller
              name="contentType"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="contentType">Content Type <span className="content-text-vibrant-red">*</span></Label>
                  <CustomSelect
                    data={contentTypeOptions}
                    key="contentType"
                    name="contentType"
                    value={
                      contentTypeOptions.find(opt => opt.value === value) ??
                      null
                    }
                    handleChange={newValue =>
                      onChange((newValue as ISelectOption | null)?.value ?? '')
                    }
                  />
                </Fragment>
              )}
            />
            {errors['contentType'] && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors['contentType']?.message}
              </p>
            )}
          </div>

          {/* Duration */}
          <div className="content-flex content-flex-col">
            <Controller
              name="duration"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="duration">Duration (minutes) <span className="content-text-vibrant-red">*</span></Label>
                  <Input
                    key="duration"
                    id="duration"
                    name="duration"
                    type="number"
                    value={value || ''}
                    placeholder="0"
                    onChange={e => onChange(e.target.value)}
                  />
                </Fragment>
              )}
            />
            {errors['duration'] && (
              <p className="content-text-sm content-text-vibrant-red">
                {errors['duration']?.message}
              </p>
            )}
          </div>

          {/* Product-Package Selection */}
          <div className="content-col-span-full">
            <Card className="content-p-4">
              <div className="content-mb-4 content-flex content-items-center content-justify-between">
                <div>
                  <CardTitle>Product & Package Selection</CardTitle>
                  <CardDescription>
                    Choose products and their packages for your organization
                  </CardDescription>
                </div>
                <button
                  type="button"
                  onClick={handleAddProduct}
                  className="content-flex content-items-center content-gap-2 content-rounded content-bg-primary content-px-4 content-py-2 content-text-sm content-font-medium content-text-white hover:content-bg-primary/90"
                >
                  <Plus className="content-size-4" /> Add Product
                </button>
              </div>

              <div className="content-space-y-4">
                {productPackagePairs.map((pair, index) => (
                  <div
                    key={pair.id}
                    className="content-rounded content-border content-border-card-border content-p-4"
                  >
                    <div className="content-mb-3 content-flex content-items-center content-justify-between">
                      <p className="content-text-xl content-font-medium">
                        Product Set {index + 1}
                        {pair.productName && (
                          <span className="content-ml-2 content-text-sm content-text-ash-gray">
                            ({pair.productName})
                          </span>
                        )}
                      </p>
                      {productPackagePairs.length > 1 && (
                        <button
                          type="button"
                          onClick={() => handleRemoveProduct(pair.id)}
                          className="content-p-1 content-text-red-500 hover:content-text-red-700"
                        >
                          <Trash2 className="content-size-4" />
                        </button>
                      )}
                    </div>

                    <div className="content-grid content-grid-cols-1 content-gap-4 lg:content-grid-cols-2">
                      {/* Product Selection */}
                      <div className="content-flex content-flex-col">
                        <Label htmlFor={`product-${pair.id}`}>Product</Label>
                        <SearchSelect
                          items={productOptions.map(option => ({
                            value: option.value,
                            label: option.label,
                          }))}
                          value={pair.productId}
                          onValueChange={value =>
                            handleProductChange(pair.id, value)
                          }
                        />
                      </div>

                      {/* Package Selection */}
                      <div className="content-flex content-flex-col">
                        <Label htmlFor={`package-${pair.id}`}>Package</Label>
                        <CustomSelect
                          data={
                            pair.productId
                              ? packageOptionsByProduct[pair.productId] || []
                              : []
                          }
                          key={`package-${pair.id}-${pair.productId}`} // Include productId in key for re-render
                          isMulti
                          name={`package-${pair.id}`}
                          value={
                            pair.packageIds
                              .map(id =>
                                packageOptionsByProduct[pair.productId]?.find(
                                  pkg => pkg.value === id,
                                ),
                              )
                              .filter(Boolean) as TMultiValue<ISelectOption>
                          }
                          handleChange={(
                            newValue:
                              | TMultiValue<ISelectOption>
                              | TSingleValue<ISelectOption>,
                          ) => {
                            const ids = Array.isArray(newValue)
                              ? newValue.map(item => item.value)
                              : newValue
                                ? [(newValue as ISelectOption).value]
                                : [];
                            handlePackageChange(pair.id, ids);
                          }}
                          isDisabled={!pair.productId}
                          placeholder={
                            pair.productId
                              ? 'Select packages'
                              : 'Select a product first'
                          }
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          </div>

          {/* Description */}
          <div className="content-col-span-full content-flex content-flex-col">
            <Controller
              name="topicDescription"
              control={control}
              render={({ field: { onChange, value } }) => (
                <Fragment>
                  <Label htmlFor="topicDescription">Description</Label>
                  <textarea
                    key="topicDescription"
                    id="topicDescription"
                    name="topicDescription"
                    value={value || ''}
                    placeholder="Topic Description"
                    className="content-h-20 content-rounded content-border content-border-card-border content-bg-transparent content-px-2.5 content-py-2 content-text-cloudy-white placeholder:content-text-sm"
                    onChange={e => onChange(e.target.value)}
                  />
                </Fragment>
              )}
            />
          </div>

          {/* Thumbnail Upload */}
          <div className="content-col-span-full content-flex content-gap-5">
            <div>
              <FileUploader
                ref={imageUploaderRef}
                containerClassName="content-mb-3 content-text-secondary"
                accept={ValidImageFormats.join(',')}
                placeholder="Upload thumbnail"
                onUpload={e =>
                  setThumbnail({
                    link: '',
                    selectedFile: e[0],
                  })
                }
              />
              {(thumbnail.selectedFile || thumbnail.link) && (
                <Button
                  type="button"
                  onClick={() => setThumbnail({ link: '', selectedFile: null })}
                  className="content-ml-2 content-rounded-none content-border-none content-bg-transparent content-p-0 content-text-vibrant-red content-underline hover:!content-bg-transparent"
                >
                  Remove
                </Button>
              )}
            </div>
            {(thumbnail.selectedFile || thumbnail.link) && (
              <img
                src={
                  thumbnail.selectedFile
                    ? URL.createObjectURL(thumbnail.selectedFile)
                    : FILE_PATH_PREFIX + thumbnail.link
                }
                alt="Thumbnail"
                className="content-h-28 content-w-auto content-rounded"
              />
            )}
          </div>

          {/* Advanced Options Toggle */}
          <div className="content-col-span-full">
            <button
              type="button"
              onClick={() => setShowAdvanced(prev => !prev)}
              className="content-flex content-items-center content-gap-2 content-text-sm content-font-medium content-text-primary hover:content-text-primary/80"
            >
              <ChevronDown
                className={`content-size-4 content-transition-transform content-duration-300 ${showAdvanced ? 'content-rotate-180' : ''}`}
              />
              {showAdvanced ? 'Hide' : 'Show'} Advanced Options...
            </button>

            <div
              className={`content-grid content-transition-all content-duration-300 content-ease-in-out ${showAdvanced
                ? 'content-mt-4 content-grid-rows-[1fr] content-opacity-100'
                : 'content-grid-rows-[0fr] content-opacity-0'
                }`}
            >
              <div className="content-overflow-hidden">
                <div className="content-grid content-grid-cols-1 content-gap-6 lg:content-grid-cols-2">
                  {ADVANCED_FIELD_CONFIG.map(field => {
                    const options: ISelectOption[] =
                      advancedDropdowns[field.key]?.map(item => ({
                        id: item.id,
                        label: item.name,
                        value: item.id,
                      })) || [];

                    const selectedValues: ISelectOption[] =
                      advancedSelections[field.key]?.map(item => ({
                        id: item.id,
                        label: item.name,
                        value: item.id,
                      })) || [];

                    return (
                      <div
                        key={field.key}
                        className="content-flex content-flex-col"
                      >
                        <Label htmlFor={field.key}>{field.label}</Label>
                        <CustomSelect
                          data={options}
                          key={field.key}
                          isMulti
                          name={field.key}
                          value={selectedValues}
                          handleChange={newValue =>
                            handleAdvancedSelectionChange(
                              field.key,
                              newValue as
                              | TMultiValue<ISelectOption>
                              | TSingleValue<ISelectOption>,
                            )
                          }
                          placeholder={`Select ${field.label}`}
                        />
                      </div>
                    );
                  })}

                  {/* Industry */}
                  <div className="content-flex content-flex-col">
                    <Label htmlFor="industry">Industry</Label>
                    <CustomSelect
                      data={advancedDropdowns.industry.map(item => ({
                        id: item.id,
                        label: item.name,
                        value: item.id,
                      }))}
                      key="industry"
                      isMulti
                      name="industry"
                      value={advancedSelections.industry.map(item => ({
                        id: item.id,
                        label: item.name,
                        value: item.id,
                      }))}
                      handleChange={newValue =>
                        handleAdvancedSelectionChange(
                          'industry',
                          newValue as
                          | TMultiValue<ISelectOption>
                          | TSingleValue<ISelectOption>,
                        )
                      }
                      placeholder="Select Industry"
                    />
                  </div>

                  {/* Sub Industry */}
                  <div className="content-flex content-flex-col">
                    <Label htmlFor="subIndustry">Sub Industry</Label>
                    <CustomSelect
                      data={advancedDropdowns.subIndustry.map(item => ({
                        id: item.id,
                        label: item.name,
                        value: item.id,
                      }))}
                      key="subIndustry"
                      isMulti
                      name="subIndustry"
                      value={advancedSelections.subIndustry.map(item => ({
                        id: item.id,
                        label: item.name,
                        value: item.id,
                      }))}
                      handleChange={newValue =>
                        handleAdvancedSelectionChange(
                          'subIndustry',
                          newValue as
                          | TMultiValue<ISelectOption>
                          | TSingleValue<ISelectOption>,
                        )
                      }
                      placeholder="Select Sub Industry"
                    />
                  </div>

                  {/* Tags */}
                  <div className="content-col-span-full content-flex content-flex-col">
                    <Label htmlFor="tags">Tags</Label>
                    <TagSelector
                      value={tags}
                      onChange={setTags}
                      className="content-mt-2"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="content-my-10 content-flex content-justify-center content-gap-x-20 lg:content-col-span-2">
            <Button
              type="button"
              onClick={handleClose}
              variant="destructive"
              className="content-w-56"
            >
              Discard
            </Button>

            <Button
              type="submit"
              className="content-w-56"
              disabled={submitting}
            >
              {submitting ? 'Saving...' : 'Save'}
            </Button>
          </div>
        </form>
      </ModalBody>
    </Modal>
  );
};

export default CourseModal;
