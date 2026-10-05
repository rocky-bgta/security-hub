import { Fragment, useEffect, useState } from 'react';

import { DeleteIcon } from 'assets/icons';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import CustomCheckbox from 'common/CustomCheckbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Pagination from 'common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { Textarea } from 'common/Textarea';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import Border from 'components/UserBorder';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { BookOpen, Edit, Eye, Save, Search } from 'lucide-react';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { IoIosArrowDown } from 'react-icons/io';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { cn, objectToQueryString } from 'utils/Helper';

interface IExam {
  examId: string;
  packageId: string;
  packageName: string;
  title: string;
  passingScore: number;
  examDetails: string;
  questions: Question[];
}

interface Question {
  questionId: string;
  text: string;
  options: string[];
  correctAnswers: string[];
  questionType:
    | 'SINGLE_CHOICE'
    | 'MULTIPLE_CHOICE'
    | 'YES_NO'
    | 'TRUE_FALSE'
    | string; // extend if needed
}

const questionTypes = [
  { value: 'SINGLE_CHOICE', label: 'Single Choice' },
  { value: 'MULTIPLE_CHOICE', label: 'Multiple Choice' },
  { value: 'TRUE_FALSE', label: 'True/False' },
  { value: 'YES_NO', label: 'Yes/No' },
  // { value: 'FILL_BLANK', label: 'Fill in the Blank' },
  // { value: 'SHORT_ANSWER', label: 'Short Answer' },
];

const ExamList = () => {
  const [isViewExamOpen, setIsViewExamOpen] = useState(false);
  const [isEditExamOpen, setIsEditExamOpen] = useState(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [updating, setUpdating] = useState<boolean>(false);
  const [examData, setExamData] = useState<IList<IExam>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [examDetails, setExamDetails] = useState<IExam>({
    examId: '',
    packageId: '',
    packageName: '',
    title: '',
    examDetails: '',
    passingScore: 0,
    questions: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
  });
  const searchDebounce = useDebounce(queryString, 500);

  const [currentQuestion, setCurrentQuestion] = useState({
    text: '',
    type: '',
    options: [''],
    correctAnswers: [] as string[],
  });
  const [selectedQuestion, setSelectedQuestion] = useState('');

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchExams();
    }
  }, [searchDebounce]);

  const fetchExams = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<IExam>> = await apiClient.get(
        API_END_POINTS.EXAM_LIST + queryString,
      );
      setExamData(response.data);
    } catch (error) {
      console.error('Error fetching exams:', error);
    } finally {
      setLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleViewExam = (exam: IExam) => {
    setExamDetails(exam);
    setIsViewExamOpen(true);
  };

  const handleEditExam = (exam: IExam) => {
    setExamDetails(exam);
    setIsEditExamOpen(true);
  };

  const addOption = () => {
    setCurrentQuestion(prev => ({
      ...prev,
      options: [...prev.options, ''],
    }));
  };

  const updateOption = (index: number, value: string) => {
    setCurrentQuestion(prev => {
      const opts = [...prev.options];
      opts[index] = value;
      return { ...prev, options: opts };
    });
  };

  const removeOption = (index: number) => {
    setCurrentQuestion(prev => {
      const removed = prev.options[index];
      const opts = prev.options.filter((_, i) => i !== index);
      const answers = prev.correctAnswers.filter(ans => ans !== removed);
      return { ...prev, options: opts, correctAnswers: answers };
    });
  };

  const saveQuestion = () => {
    const { text, type, options, correctAnswers } = currentQuestion;
    if (!text.trim() || !type) {
      console.warn('Question text and type are required.');
      return;
    }

    let finalOptions: string[];
    let finalAnswers: string[];

    if (type === 'TRUE_FALSE') {
      finalOptions = ['True', 'False'];
      finalAnswers = correctAnswers;
    } else if (type === 'YES_NO') {
      finalOptions = ['Yes', 'No'];
      finalAnswers = correctAnswers;
    } else {
      const filtered = options.map(o => o.trim()).filter(o => o);
      finalOptions = filtered;
      finalAnswers = correctAnswers.filter(ans => filtered.includes(ans));
    }

    const newQ = {
      questionId: Date.now().toString(),
      text: text.trim(),
      questionType: type,
      options: finalOptions,
      correctAnswers: finalAnswers,
    };

    setExamDetails(prev => ({
      ...prev,
      questions: [...prev.questions, newQ],
    }));

    setCurrentQuestion({
      text: '',
      type: '',
      options: [''],
      correctAnswers: [],
    });
  };

  const handleDeleteQuestion = (id: string) => {
    // setQuestions(prev => prev.filter(q => q.id !== id));
    const question = examDetails.questions.find(q => q.questionId === id);
    if (question) {
      const newQuestions = examDetails.questions.filter(
        q => q.questionId !== id,
      );
      setExamDetails(prev => ({
        ...prev,
        questions: newQuestions,
      }));
    }
  };

  const handleEditQuestion = (id: string) => {
    const question = examDetails.questions.find(q => q.questionId === id);
    if (question) {
      setCurrentQuestion({
        text: question.text,
        type: question.questionType,
        options: question.options,
        correctAnswers: question.correctAnswers,
      });
      // setQuestions(prev => prev.filter(q => q.id !== id));
      const newQuestions = examDetails.questions.filter(
        q => q.questionId !== id,
      );
      setExamDetails(prev => ({
        ...prev,
        questions: newQuestions,
      }));
    }
  };

  const handleSelectQuestion = (id: string) => {
    if (selectedQuestion === id) {
      setSelectedQuestion('');
    } else {
      setSelectedQuestion(id);
    }
  };

  const handleUpdateQuestion = async () => {
    setUpdating(true);
    const payload = {
      title: examDetails.title,
      passingScore: examDetails.passingScore,
      examDetails: examDetails.examDetails,
      questions: examDetails.questions.map((q: any) => ({
        questionId: q.questionId,
        text: q.text,
        questionType: q.questionType,
        options: q.options,
        correctAnswers: q.correctAnswers,
      })),
    };

    try {
      await apiClient.put(API_END_POINTS.EDIT_EXAM(examDetails.examId), {
        data: payload,
      });
      setIsEditExamOpen(false);
      fetchExams();
      setUpdating(false);
    } catch (error) {
      console.error('Error updating questions:', error);
    } finally {
      setLoading(false);
      setUpdating(false);
    }
  };

  const handleDownloadTemplate = () => {
    const link = document.createElement('a');
    link.href =
      'https://aspcstorageprd.blob.core.windows.net/question-import/sample_exam_questions.csv';
    link.download = 'sample_exam_questions.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleImportCSV = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await apiClient.post(
        API_END_POINTS.PRODUCT_EXAM_CSV_UPLOAD,
        {
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
        },
      );
      setExamDetails({
        ...examDetails,
        questions: [
          ...examDetails.questions,
          ...response.data.map((q: any, index: number) => ({
            questionId:
              q.id ||
              `${Date.now()}-${Math.random().toString(36).substr(2, 6)}`,
            text: q.text,
            questionType: q.questionType,
            options: q.options || [],
            correctAnswers: q.correctAnswers || [],
          })),
        ],
      });
      e.target.value = '';
    } catch (error) {
      console.error('Error fetching courses:', error);
    }
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
        {/* <div className="content-flex content-gap-2">
          <Button variant="default">
            <Plus className="content-h-4 content-w-4 content-mr-2" />
            Create Exam
          </Button>
          <Button variant="outline">
            <Download className="content-h-4 content-w-4 content-mr-2" />
            Export
          </Button>
        </div> */}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <BookOpen className="content-size-5" />
            All Exams
          </CardTitle>
          <CardDescription>
            View, edit, and manage all exams and their questions
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Filters */}
          <div className="content-mb-6">
            <div className="content-space-y-2">
              <Label htmlFor="search">Search Exams</Label>
              <div className="content-relative">
                <Search className="content-absolute content-left-3 content-top-3 content-size-4 content-text-muted-foreground" />
                <Input
                  id="search"
                  placeholder="Search by exam or product name..."
                  value={queryParams.search}
                  onChange={e => {
                    setQueryParams(prevState => ({
                      ...prevState,
                      search: e.target.value,
                    }));
                  }}
                  className="content-pl-10"
                />
              </div>
            </div>

            {/* <div className="content-space-y-2">
              <Label htmlFor="product-filter">Product</Label>
              <Select value={productFilter} onValueChange={setProductFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All products" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Products</SelectItem>
                  <SelectItem value="mathematics">Mathematics</SelectItem>
                  <SelectItem value="science">Science</SelectItem>
                  <SelectItem value="history">History</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="status-filter">Status</Label>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All statuses" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Statuses</SelectItem>
                  <SelectItem value="active">Active</SelectItem>
                  <SelectItem value="inactive">Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="content-space-y-2 content-flex content-items-end">
              <Button
                variant="outline"
                className="content-w-full"
                onClick={() => {
                  setSearchTerm('');
                  setStatusFilter('all');
                  setProductFilter('all');
                }}
              >
                Clear Filters
              </Button>
            </div> */}
          </div>

          {/* Exams Table */}
          {loading ? (
            <AssignedPackagesLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Exam Name</TableHead>
                  <TableHead>Total Questions</TableHead>
                  <TableHead>Passing Score</TableHead>
                  {/* <TableHead>Status</TableHead> */}
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {examData.items?.map((exam, index) => (
                  <TableRow key={index}>
                    <TableCell>{exam.packageName}</TableCell>
                    <TableCell className="content-font-medium">
                      {exam.title}
                    </TableCell>
                    <TableCell>{exam.questions.length}</TableCell>
                    <TableCell>{exam.passingScore}</TableCell>
                    {/* <TableCell>
                    <StatusBadge status={exam.status} />
                  </TableCell> */}
                    <TableCell>
                      <div className="content-flex content-space-x-1">
                        <Button
                          onClick={() => handleViewExam(exam)}
                          variant="ghost"
                          size="sm"
                        >
                          <Eye className="content-size-4" />
                        </Button>
                        <Button
                          onClick={() => handleEditExam(exam)}
                          variant="ghost"
                          size="sm"
                        >
                          <Edit className="content-size-4" />
                        </Button>
                        {/* <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleToggleStatus(exam.id)}
                      >
                        <Power className="content-h-4 content-w-4" />
                      </Button> */}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}

          <div className="content-flex content-justify-end content-py-7">
            <Pagination
              total={examData?.total}
              perPage={examData?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>

          {examData?.items?.length === 0 && (
            <div className="content-py-8 content-text-center content-text-muted-foreground">
              No exams found matching your criteria.
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={isEditExamOpen} onOpenChange={setIsEditExamOpen}>
        <DialogContent className="content-h-[90%] !content-w-3/5 content-overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Edit User</DialogTitle>
            <DialogDescription>Update user information</DialogDescription>
          </DialogHeader>

          <Border className="content-p-6">
            <CardContent className="content-mb-4 content-space-y-4 !content-p-0">
              <div>
                <Label htmlFor="exam-title">Title *</Label>
                <Input
                  id="exam-title"
                  value={examDetails.title}
                  onChange={e =>
                    setExamDetails({ ...examDetails, title: e.target.value })
                  }
                  placeholder="Enter exam title"
                />
              </div>
              <div>
                <Label htmlFor="exam-score">Score *</Label>
                <Input
                  id="exam-score"
                  type="text"
                  value={examDetails.passingScore}
                  onChange={e =>
                    setExamDetails({
                      ...examDetails,
                      passingScore: Number(e.target.value),
                    })
                  }
                  placeholder="Enter exam score"
                />
              </div>
              <div>
                <Label htmlFor="exam-description">Description</Label>
                <Textarea
                  id="exam-description"
                  value={examDetails.examDetails}
                  onChange={e =>
                    setExamDetails({
                      ...examDetails,
                      examDetails: e.target.value,
                    })
                  }
                  placeholder="Enter exam description"
                />
              </div>
            </CardContent>
            <div className="content-mb-4 content-flex content-items-center content-justify-between">
              <CardHeader className="!content-p-0">
                <CardTitle>Add Questions</CardTitle>
                <CardDescription>Define your questions</CardDescription>
              </CardHeader>
              <div className="content-space-x-3">
                <Button
                  onClick={handleDownloadTemplate}
                  size="sm"
                  variant="secondary"
                >
                  Download Template
                </Button>
                <Label>
                  <span className="content-cursor-pointer content-rounded-md content-bg-primary content-px-3 content-py-2.5 content-text-sm">
                    Import CSV
                  </span>
                  <input
                    type="file"
                    accept=".csv"
                    className="content-hidden"
                    onChange={handleImportCSV}
                  />
                </Label>
              </div>
            </div>
            <CardContent className="content-mb-6 content-space-y-4 !content-p-0">
              <div>
                <Label htmlFor="question-type">Type *</Label>
                <Select
                  value={currentQuestion.type}
                  onValueChange={value =>
                    setCurrentQuestion(prev => ({ ...prev, type: value }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select type" />
                  </SelectTrigger>
                  <SelectContent>
                    {questionTypes.map(t => (
                      <SelectItem key={t.value} value={t.value}>
                        {t.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div>
                <Label htmlFor="question-text">Text *</Label>
                <Textarea
                  id="question-text"
                  value={currentQuestion.text}
                  onChange={e =>
                    setCurrentQuestion(prev => ({
                      ...prev,
                      text: e.target.value,
                    }))
                  }
                  placeholder="Enter your question"
                />
              </div>

              {['MULTIPLE_CHOICE'].includes(currentQuestion.type) && (
                <Fragment>
                  <div className="content-mb-2 content-flex content-items-center content-justify-between">
                    <Label>Options *</Label>
                    <Button variant="default" size="sm" onClick={addOption}>
                      Add Option
                    </Button>
                  </div>
                  {currentQuestion.options.map((opt, idx) => (
                    <div
                      key={idx}
                      className="content-flex content-items-center content-gap-2"
                    >
                      <Input
                        value={opt}
                        onChange={e => updateOption(idx, e.target.value)}
                        placeholder={`Option ${idx + 1}`}
                      />
                      {currentQuestion.options.length > 1 && (
                        <Button
                          variant="secondary"
                          size="sm"
                          onClick={() => removeOption(idx)}
                        >
                          Remove
                        </Button>
                      )}
                    </div>
                  ))}

                  <div className="content-text-cloudy-white">
                    Correct Answer *
                  </div>
                  {currentQuestion.options
                    .filter(o => o.trim())
                    .map(opt => (
                      <div
                        key={opt}
                        className="content-relative content-flex content-items-center content-gap-2"
                      >
                        <CustomCheckbox
                          checked={currentQuestion.correctAnswers.includes(opt)}
                          onChange={e => {
                            const checked = e.target.checked;
                            setCurrentQuestion(prev => ({
                              ...prev,
                              correctAnswers: checked
                                ? [...prev.correctAnswers, opt]
                                : prev.correctAnswers.filter(a => a !== opt),
                            }));
                          }}
                        />
                        <span className="content-ml-2 content-text-white">
                          {opt}
                        </span>
                      </div>
                    ))}
                </Fragment>
              )}

              {currentQuestion.type === 'SINGLE_CHOICE' && (
                <Fragment>
                  <div className="content-mb-2 content-flex content-items-center content-justify-between">
                    <Label>Options *</Label>
                    <Button variant="outline" size="sm" onClick={addOption}>
                      Add Option
                    </Button>
                  </div>
                  {currentQuestion.options.map((opt, idx) => (
                    <div
                      key={idx}
                      className="content-flex content-items-center content-gap-2"
                    >
                      <Input
                        value={opt}
                        onChange={e => updateOption(idx, e.target.value)}
                        placeholder={`Option ${idx + 1}`}
                      />
                      {currentQuestion.options.length > 1 && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => removeOption(idx)}
                        >
                          Remove
                        </Button>
                      )}
                    </div>
                  ))}

                  <div className="content-text-cloudy-white">
                    Correct Answer *
                  </div>
                  {currentQuestion.options
                    .filter(o => o.trim())
                    .map((opt, index) => (
                      <div
                        key={opt}
                        className="content-relative content-flex content-items-center content-gap-2"
                      >
                        <Label
                          htmlFor={`SINGLE_CHOICE_${index}`}
                          className="content-flex content-cursor-pointer content-items-center"
                        >
                          <input
                            type="radio"
                            id={`SINGLE_CHOICE_${index}`}
                            name="SINGLE_CHOICE"
                            className="content-mr-2 content-size-5"
                            value={opt}
                            checked={currentQuestion.correctAnswers.includes(
                              opt,
                            )}
                            onChange={e => {
                              const checked = e.target.checked;
                              setCurrentQuestion(prev => ({
                                ...prev,
                                correctAnswers: checked
                                  ? [opt]
                                  : prev.correctAnswers.filter(a => a !== opt),
                              }));
                            }}
                          />
                          <span className="content-text-white">{opt}</span>
                        </Label>
                      </div>
                    ))}
                </Fragment>
              )}

              {['TRUE_FALSE', 'YES_NO'].includes(currentQuestion.type) && (
                <div>
                  <div className="content-text-cloudy-white">
                    Correct Answer *
                  </div>
                  <Select
                    value={currentQuestion.correctAnswers[0] || ''}
                    onValueChange={val =>
                      setCurrentQuestion(prev => ({
                        ...prev,
                        correctAnswers: [val],
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select answer" />
                    </SelectTrigger>
                    <SelectContent>
                      {currentQuestion.type === 'TRUE_FALSE' ? (
                        <>
                          <SelectItem value="True">True</SelectItem>
                          <SelectItem value="False">False</SelectItem>
                        </>
                      ) : (
                        <>
                          <SelectItem value="Yes">Yes</SelectItem>
                          <SelectItem value="No">No</SelectItem>
                        </>
                      )}
                    </SelectContent>
                  </Select>
                </div>
              )}

              <Button onClick={saveQuestion}>
                <Save className="content-mr-2 content-size-4" /> Save Question
              </Button>
            </CardContent>

            {examDetails?.questions.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>
                    Questions ({examDetails?.questions.length})
                  </CardTitle>
                  <CardDescription>Review added questions</CardDescription>
                </CardHeader>
                <CardContent className="content-space-y-4">
                  {examDetails?.questions.map((q, i) => (
                    <div
                      key={i}
                      className="content-rounded content-border content-border-card-border content-p-3"
                    >
                      <div className="content-flex content-items-center content-justify-between">
                        <h4 className="content-font-medium content-text-white">
                          {q.text}
                        </h4>
                        <div className="content-flex content-items-center content-gap-4">
                          <Edit
                            onClick={() => handleEditQuestion(q.questionId)}
                            className="content-size-5 content-cursor-pointer content-text-ash-gray"
                          />
                          <DeleteIcon
                            onClick={() => handleDeleteQuestion(q.questionId)}
                            fill="#9a9a9a"
                            className="content-size-4 content-cursor-pointer content-text-ash-gray"
                          />
                          <IoIosArrowDown
                            onClick={() => handleSelectQuestion(q.questionId)}
                            className={cn(
                              'content-cursor-pointer content-text-xl content-text-ash-gray',
                              selectedQuestion === q.questionId
                                ? 'content-rotate-180'
                                : '',
                            )}
                          />
                        </div>
                      </div>
                      {selectedQuestion &&
                        selectedQuestion === q.questionId && (
                          <Fragment>
                            {q.options.length > 0 && (
                              <>
                                <p className="content-mt-2 content-font-medium content-text-ash-gray">
                                  Options:
                                </p>
                                <ul className="content-list-inside content-list-disc content-text-muted-foreground">
                                  {q.options.map((opt: string) => (
                                    <li key={opt}>{opt}</li>
                                  ))}
                                </ul>
                              </>
                            )}
                            <p className="content-mt-1 content-text-green-600">
                              Correct: {q.correctAnswers.join(', ')}
                            </p>
                          </Fragment>
                        )}
                    </div>
                  ))}
                </CardContent>
              </Card>
            )}

            <Button
              className="content-float-right content-mt-4"
              onClick={handleUpdateQuestion}
            >
              <Save className="content-mr-2 content-size-4" />{' '}
              {updating ? 'Updating...' : ' Update Question'}
            </Button>
          </Border>
        </DialogContent>
      </Dialog>

      {/* View User Dialog */}
      <Dialog open={isViewExamOpen} onOpenChange={setIsViewExamOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>View Exam</DialogTitle>
            <DialogDescription>View Exam Details</DialogDescription>
          </DialogHeader>
          <Border className="content-px-6 content-py-4">
            <div className="content-space-y-4">
              <div className="content-text-muted-foreground">
                <strong className="content-text-foreground">Title:</strong>{' '}
                {examDetails?.title}
              </div>
              <div className="content-text-muted-foreground">
                <strong className="content-text-foreground">Package:</strong>{' '}
                {examDetails?.packageName}
              </div>
              <div className="content-text-muted-foreground">
                <strong className="content-text-foreground">
                  Passing Score:
                </strong>{' '}
                {examDetails?.passingScore}
              </div>
              <div className="content-text-muted-foreground">
                <strong className="content-text-foreground">
                  Description:
                </strong>{' '}
                {examDetails?.examDetails}
              </div>

              <div className="content-mt-4">
                <strong className="content-text-foreground">Questions:</strong>
                <div className="content-mt-2 content-max-h-[480px] content-overflow-y-auto">
                  <div className="content-space-y-6">
                    {examDetails?.questions.map((question, index) => (
                      <div key={index}>
                        <Border className="content-p-4">
                          <div className="content-mb-2 content-font-semibold content-text-foreground">
                            {index + 1}. {question.text}
                          </div>
                          <div className="content-space-y-1 content-pl-4">
                            {question.options.map((option, i) => (
                              <div
                                key={i}
                                className={`${
                                  question.correctAnswers.includes(option)
                                    ? 'content-font-medium content-text-green-600'
                                    : 'content-text-muted-foreground'
                                }`}
                              >
                                - {option}
                              </div>
                            ))}
                          </div>
                          <div className="content-mt-2 content-text-sm content-text-muted-foreground">
                            Type: {question.questionType}
                          </div>
                        </Border>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </Border>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default ExamList;
