import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from 'common/Accordion';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Label } from 'common/Label';
import Pagination from 'common/Pagination';
import ConfirmDialog from 'components/ConfirmDialog';
import FilterControls from 'components/FilterControls';
import SearchSelect from 'components/SearchSelect';
import QuestionEditModal from 'features/exam/QuestionEditModal';
import QuestionModal from 'features/exam/QuestionModal';
import QuestionView from 'features/exam/QuestionView';
import { useAPI } from 'hooks/UseAPI';
import { BookOpen, BookOpenCheck, Edit, Eye, Plus, Trash2 } from 'lucide-react';
import {
  ICategory,
  ICompliance,
  IContentType,
  ICountry,
} from 'models/Configuration';
import { ITopic } from 'models/Course';
import { IQuestion } from 'models/Exam';
import { IFilterData } from 'models/Filter';
import { ISelectOption } from 'models/Input';
import { IProduct } from 'models/Product';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';

enum ModalType {
  None = 'none',
  General = 'general',
  View = 'view',
  Edit = 'edit',
}

interface ITopicQueryParams {
  page: number;
  size: number;
}

const ExamLibrary = () => {
  const apiClient = useAPI();
  const [openModal, setOpenModal] = useState<ModalType>(ModalType.None);
  const [selectedProduct, setSelectedProduct] = useState<string>('');
  const [selectedTopicId, setSelectedTopicId] = useState<string>('');
  const [selectedQuestionId, setSelectedQuestionId] = useState<string>('');
  const [filterData, setFilterData] = useState<IFilterData>({
    countryIds: [],
    complianceIds: [],
    categoryIds: [],
    contentTypeIds: [],
    durationMinutes: [],
  });
  const [search, setSearch] = useState<string>('');
  const [products, setProducts] = useState<Array<ISelectOption>>([]);
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
  const [topics, setTopics] = useState<any>({ topics: [] });
  const [topicQuestions, setTopicQuestions] = useState<
    Record<string, IQuestion[]>
  >({});
  const [loadingQuestions, setLoadingQuestions] = useState<
    Record<string, boolean>
  >({});
  const [openConfirmDialog, setOpenConfirmDialog] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [topicQueryParams, setTopicQueryParams] = useState<ITopicQueryParams>({
    page: 1,
    size: 10,
  });

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.PRODUCT_LIST + 'pageSize=1000',
      );
      setProducts(
        response.data.items.map((item: IProduct) => ({
          id: item.productId,
          label: item.productName,
          value: item.productName,
        })),
      );
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

  const fetchOptions = async () => {
    try {
      const [categoryRes, contentTypeRes, complianceRes, countryRes] =
        await Promise.all([
          apiClient.get(API_END_POINTS.CATEGORY_LIST),
          apiClient.get(API_END_POINTS.CONTENT_TYPE_LIST),
          apiClient.get(API_END_POINTS.COMPLIANCE_LIST),
          apiClient.get(API_END_POINTS.COUNTRY_LIST),
        ]);

      setCategoryOptions(
        categoryRes.data?.map((item: ICategory) => ({
          id: item.id,
          label: item.categoryName,
          value: item.id,
        })) || [],
      );

      setContentTypeOptions(
        contentTypeRes.data?.map((item: IContentType) => ({
          id: item.id,
          label: item.typeName,
          value: item.id,
        })) || [],
      );

      setComplianceOptions(
        complianceRes.data?.map((item: ICompliance) => ({
          id: item.id,
          label: item.complianceName,
          value: item.id,
        })) || [],
      );

      setCountryOptions(
        countryRes.data?.map((item: ICountry) => ({
          id: item.id,
          label: item.name,
          value: item.id,
        })) || [],
      );
    } catch (error) {
      console.error('Error fetching filter options:', error);
    }
  };

  const fetchTopics = async (selectedProduct: string) => {
    try {
      const response = await apiClient.post(
        API_END_POINTS.TOPIC_FILTER_BY_PRODUCT + selectedProduct,
        {
          data: {
            countryIds: filterData.countryIds.map(item => item.id),
            complianceIds: filterData.complianceIds.map(item => item.id),
            categoryIds: filterData.categoryIds.map(item => item.id),
            contentTypeId: filterData.contentTypeIds.map(item => item.id),
            durationRanges: filterData.durationMinutes,
            searchText: search,
            ...topicQueryParams,
          },
        },
      );

      setTopics(response.data);

      setTopicQuestions({});
      setLoadingQuestions({});
    } catch (error) {
      console.error('Error fetching topics:', error);
    }
  };

  useEffect(() => {
    if (!selectedProduct) return;
    fetchTopics(selectedProduct);
  }, [selectedProduct, filterData, search, topicQueryParams]);

  useEffect(() => {
    if (!selectedProduct) return;
    fetchOptions();
  }, [selectedProduct]);

  const handleFilterChange = useCallback((updates: Partial<IFilterData>) => {
    setFilterData(prev => ({ ...prev, ...updates }));
  }, []);

  const handleOpenModal = (
    content: ModalType,
    topicId?: string,
    questionId?: string,
  ) => {
    setOpenModal(content);
    if (topicId) setSelectedTopicId(topicId);
    if (questionId) setSelectedQuestionId(questionId);
  };

  const handleCloseModal = () => {
    setOpenModal(ModalType.None);
    setSelectedTopicId('');
    setSelectedQuestionId('');
  };

  const handleDeleteQuestion = async (questionId: string) => {
    try {
      await apiClient.del(API_END_POINTS.QUESTION_DELETE + questionId);
      renderExam();
      toast.success('Question deleted successfully');
      setOpenConfirmDialog(false);
      setSelectedQuestionId('');
    } catch (error) {
      console.error('Error deleting question:', error);
      toast.error('Failed to delete question');
      setOpenConfirmDialog(false);
      setSelectedQuestionId('');
    }
  };

  const handleDeleteConfirm = async () => {
    setSubmitting(true);
    await handleDeleteQuestion(selectedQuestionId);
    setSubmitting(false);
  };

  const handleCloseConfirmDialog = () => {
    setOpenConfirmDialog(false);
  };

  const handleResetFilters = () => {
    setFilterData({
      countryIds: [],
      complianceIds: [],
      categoryIds: [],
      contentTypeIds: [],
      durationMinutes: [],
    });
  };

  const fetchQuestions = async (topicId: string) => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.QUESTION_LIST + 'topicId=' + topicId + '&pageSize=100',
      );

      const questions: IQuestion[] =
        response.data?.items?.map((item: any) => ({
          id: item.id,
          questionText: item.questionText,
          questionType: item.questionType,
          status: item.status,
        })) || [];

      setTopicQuestions(prev => ({
        ...prev,
        [topicId]: questions,
      }));
    } catch (error) {
      console.error('Error fetching questions:', error);
      // Set empty array on error to prevent infinite loading
      setTopicQuestions(prev => ({
        ...prev,
        [topicId]: [],
      }));
    } finally {
      setLoadingQuestions(prev => ({ ...prev, [topicId]: false }));
    }
  };

  const handleQuestionsAccordion = async (topicId: string) => {
    if (topicQuestions[topicId] || loadingQuestions[topicId]) {
      return;
    }
    setSelectedTopicId(topicId);
    setLoadingQuestions(prev => ({ ...prev, [topicId]: true }));
    fetchQuestions(topicId);
  };

  const renderExam = () => {
    fetchQuestions(selectedTopicId);
  };

  const onPageChangeHandler = (page: number) => {
    setTopicQueryParams(prevState => ({
      ...prevState,
      page: page,
    }));
  };

  const renderQuestion = (question: IQuestion, topicId: string) => {
    return (
      <div
        key={question.id}
        className="content-space-y-3 content-rounded content-border content-border-primary content-bg-primary/20 content-p-4"
      >
        <div className="content-flex content-items-start content-justify-between">
          <div className="content-flex-1 content-space-y-2">
            <h4 className="content-font-medium content-leading-relaxed content-text-foreground">
              {question.questionText}
            </h4>
            <div className="content-flex content-items-center content-gap-2">
              <Badge variant="outline" className="content-text-xs">
                {question.questionType}
              </Badge>
              <Badge
                variant={
                  question.status === 'ACTIVE' ? 'default' : 'destructive'
                }
                className={
                  question.status === 'ACTIVE'
                    ? 'content-bg-primary content-text-white'
                    : ''
                }
              >
                {question.status === 'ACTIVE' ? 'Active' : 'Inactive'}
              </Badge>
            </div>
          </div>

          <div className="content-ml-4 content-flex content-items-center content-gap-1">
            <Button
              variant="ghost"
              size="sm"
              onClick={() =>
                handleOpenModal(ModalType.View, topicId, question.id)
              }
              title="View Question"
            >
              <Eye className="content-size-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() =>
                handleOpenModal(ModalType.Edit, topicId, question.id)
              }
              title="Edit Question"
            >
              <Edit className="content-size-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="hover:content-bg-destructive/20 hover:content-text-destructive"
              onClick={() => {
                setOpenConfirmDialog(true);
                setSelectedQuestionId(question.id);
                setSelectedTopicId(topicId);
              }}
              title="Delete Question"
            >
              <Trash2 className="content-size-4" />
            </Button>
          </div>
        </div>
      </div>
    );
  };

  const renderTopicHeader = (topic: ITopic) => {
    const questions = topicQuestions[topic.id] || [];
    const totalQuestions = questions.length;
    const activeQuestions = questions.filter(q => q.status).length;
    const isLoading = loadingQuestions[topic.id];
    const hasLoadedQuestions = topicQuestions[topic.id] !== undefined;

    return (
      <div className="content-flex content-w-full content-items-center content-justify-between">
        <div className="content-flex content-items-center content-gap-4">
          <div className="content-flex content-items-center content-gap-3">
            <BookOpen className="content-size-5 content-text-primary" />
            <div className="content-flex content-flex-col content-items-start">
              <h3 className="content-font-medium content-text-foreground">
                {topic.topicName}
              </h3>
              <p className="content-text-sm content-text-muted-foreground">
                Duration: {topic.durationMinutes || 0} minutes
              </p>
            </div>
          </div>
        </div>

        <div className="content-flex content-items-center content-gap-3">
          <div className="content-flex content-items-center content-gap-2">
            {isLoading ? (
              <Badge variant="outline" className="content-text-xs">
                Loading...
              </Badge>
            ) : hasLoadedQuestions ? (
              <>
                <Badge variant="outline" className="content-text-xs">
                  {totalQuestions} Questions
                </Badge>
                <Badge variant="outline" className="content-text-xs">
                  {activeQuestions} Active
                </Badge>
              </>
            ) : (
              <Badge variant="outline" className="content-text-xs">
                Click to load questions
              </Badge>
            )}
          </div>

          <Button
            onClick={e => {
              e.stopPropagation();
              handleOpenModal(ModalType.General, topic.id);
            }}
            variant="outline"
            size="sm"
            className="content-mr-2"
            title="Add Question"
          >
            <Plus className="content-mr-1 content-size-4" />
            Add Question
          </Button>
        </div>
      </div>
    );
  };

  const renderAccordionContent = (topic: ITopic) => {
    const questions = topicQuestions[topic.id];
    const isLoading = loadingQuestions[topic.id];

    if (isLoading) {
      return (
        <div className="content-py-6 content-text-center">
          <p className="content-text-muted-foreground">Loading questions...</p>
        </div>
      );
    }

    if (!questions || questions.length === 0) {
      return (
        <div className="content-py-6 content-text-center content-text-muted-foreground">
          <p>No questions available for this topic.</p>
          <Button
            onClick={() => handleOpenModal(ModalType.General, topic.id)}
            variant="outline"
            size="sm"
            className="content-mt-2"
          >
            <Plus className="content-mr-1 content-size-4" />
            Add First Question
          </Button>
        </div>
      );
    }

    return (
      <div className="content-max-h-[600px] content-space-y-3 content-overflow-y-auto">
        {questions.map(question => renderQuestion(question, topic.id))}
      </div>
    );
  };

  return (
    <div className="content-space-y-6">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-text-foreground">
            Exam Library
          </h1>
          <p className="content-text-muted-foreground">
            Manage and organize all exams in the system
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <BookOpen className="content-size-5" />
            Select Product
          </CardTitle>
          <CardDescription>
            View, edit, and manage all exams and their questions
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div>
            <div className="content-space-y-2">
              <Label htmlFor="search">Search & Select Product</Label>
              <div className="content-relative">
                <SearchSelect
                  value={selectedProduct}
                  onValueChange={value => setSelectedProduct(value.toString())}
                  placeholder="Search and Select Product"
                  items={products.map(item => ({
                    value: item.id.toString(),
                    label: item.value,
                  }))}
                />
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {selectedProduct && (
        <Card>
          <CardHeader>
            <div className="content-flex content-items-center content-justify-between">
              <div className="content-flex content-flex-col content-gap-y-1.5">
                <CardTitle className="content-flex content-items-center content-gap-2">
                  <BookOpenCheck className="content-size-5" />
                  Available Topics & Questions
                </CardTitle>
                <CardDescription>
                  View, edit, and manage all topics and their questions
                </CardDescription>
              </div>
              <Button variant="outline" size="sm" onClick={handleResetFilters}>
                Reset Filters
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <FilterControls
              filterData={filterData}
              onFilterChange={handleFilterChange}
              options={{
                categoryOptions,
                contentTypeOptions,
                complianceOptions,
                countryOptions,
              }}
              search={search}
              setSearch={setSearch}
            />

            <div className="content-mt-6">
              {topics.topics.length === 0 ? (
                <div className="content-py-8 content-text-center">
                  <p className="content-text-muted-foreground">
                    No topics found
                  </p>
                </div>
              ) : (
                <Accordion
                  type="single"
                  collapsible
                  className="content-w-full content-space-y-4"
                >
                  {topics.topics.map((topic: ITopic) => (
                    <AccordionItem
                      key={topic.id}
                      value={topic.id}
                      className="content-border-b-0"
                    >
                      <Card>
                        <CardContent className="!content-p-0">
                          <AccordionTrigger
                            onClick={() => handleQuestionsAccordion(topic.id)}
                            className="content-px-6 content-py-4 hover:content-no-underline"
                          >
                            {renderTopicHeader(topic)}
                          </AccordionTrigger>

                          <AccordionContent className="content-px-6 content-pb-4">
                            {renderAccordionContent(topic)}
                          </AccordionContent>
                        </CardContent>
                      </Card>
                    </AccordionItem>
                  ))}
                </Accordion>
              )}
            </div>
            <div className="content-mt-6">
              <Pagination
                total={topics?.totalElements}
                perPage={topicQueryParams.size}
                onPageChange={onPageChangeHandler}
              />
            </div>
          </CardContent>
        </Card>
      )}
      {openModal === ModalType.General && (
        <QuestionModal
          isOpen={openModal === ModalType.General}
          onClose={handleCloseModal}
          topicId={selectedTopicId}
          onSave={renderExam}
        />
      )}
      {openModal === ModalType.Edit && (
        <QuestionEditModal
          isOpen={openModal === ModalType.Edit}
          onClose={handleCloseModal}
          questionId={selectedQuestionId}
          topicId={selectedTopicId}
          onSave={renderExam}
        />
      )}
      {openModal === ModalType.View && (
        <QuestionView
          isOpen={openModal === ModalType.View}
          onClose={handleCloseModal}
          questionId={selectedQuestionId}
        />
      )}
      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        isOpen={openConfirmDialog}
        message="Are you sure you want to delete this Question?"
        loading={submitting}
        loadingText="Deleting..."
        onClose={handleCloseConfirmDialog}
        onConfirm={handleDeleteConfirm}
      />
    </div>
  );
};

export default ExamLibrary;
