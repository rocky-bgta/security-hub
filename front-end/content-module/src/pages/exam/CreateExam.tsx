import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Textarea } from 'common/Textarea';
import { useState } from 'react';

import { PlusCircle, Save } from 'lucide-react';

const CreateExam = () => {
  const [selectedProduct, setSelectedProduct] = useState('');
  const [selectedTopic, setSelectedTopic] = useState('');
  const [questionType, setQuestionType] = useState('');
  const [examData, setExamData] = useState({
    title: '',
    description: '',
  });
  const [questions, setQuestions] = useState<any[]>([]);
  const [currentQuestion, setCurrentQuestion] = useState({
    text: '',
    type: '',
    options: ['', ''],
    correctAnswer: '',
  });

  const questionTypes = [
    {
      value: 'multiple-choice-single',
      label: 'Multiple Choice (Single Answer)',
    },
    {
      value: 'multiple-choice-multiple',
      label: 'Multiple Choice (Multiple Answers)',
    },
    { value: 'true-false', label: 'True/False' },
    { value: 'yes-no', label: 'Yes/No' },
    { value: 'fill-blank', label: 'Fill in the Blank' },
    { value: 'short-answer', label: 'Short Answer' },
  ];

  const addOption = () => {
    setCurrentQuestion({
      ...currentQuestion,
      options: [...currentQuestion.options, ''],
    });
  };

  const updateOption = (index: number, value: string) => {
    const newOptions = [...currentQuestion.options];
    newOptions[index] = value;
    setCurrentQuestion({
      ...currentQuestion,
      options: newOptions,
    });
  };

  const saveQuestion = () => {
    if (!currentQuestion.text || !questionType) {
      //   toast({
      //     title: 'Error',
      //     description: 'Please fill in all required fields.',
      //     variant: 'destructive',
      //   });
      return;
    }

    const newQuestion = {
      id: Date.now().toString(),
      ...currentQuestion,
      type: questionType,
    };

    setQuestions([...questions, newQuestion]);
    setCurrentQuestion({
      text: '',
      type: '',
      options: ['', ''],
      correctAnswer: '',
    });
    setQuestionType('');

    // toast({
    //   title: 'Question Added',
    //   description: 'Question has been successfully added to the exam.',
    // });
  };

  const createExam = () => {
    if (!selectedProduct || !selectedTopic || questions.length === 0) {
      //   toast({
      //     title: 'Error',
      //     description:
      //       'Please complete all required fields and add at least one question.',
      //     variant: 'destructive',
      //   });
      return;
    }

    // toast({
    //   title: 'Exam Created',
    //   description: 'Exam has been created successfully.',
    // });
  };

  return (
    <div className="content-space-y-6">
      <div>
        <h1 className="content-text-3xl content-font-bold content-text-foreground">
          Create Assessment
        </h1>
        <p className="content-text-muted-foreground">
          Create a new assessment by selecting product, topic, and adding questions
        </p>
      </div>

      {/* Exam Details */}
      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <PlusCircle className="content-size-5" />
            Assessment Details
          </CardTitle>
          <CardDescription>Configure basic exam information</CardDescription>
        </CardHeader>
        <CardContent className="content-space-y-4">
          <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-2">
            <div className="content-space-y-2">
              <Label htmlFor="product">Select Product *</Label>
              <Select
                value={selectedProduct}
                onValueChange={setSelectedProduct}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Choose a product..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="mathematics">Mathematics</SelectItem>
                  <SelectItem value="science">Science</SelectItem>
                  <SelectItem value="history">History</SelectItem>
                  <SelectItem value="english">English</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="topic">Select Topic *</Label>
              <Select
                value={selectedTopic}
                onValueChange={setSelectedTopic}
                disabled={!selectedProduct}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Choose a topic..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="algebra">Algebra</SelectItem>
                  <SelectItem value="geometry">Geometry</SelectItem>
                  <SelectItem value="calculus">Calculus</SelectItem>
                  <SelectItem value="physics">Physics</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="exam-title">Exam Title *</Label>
            <Input
              id="exam-title"
              placeholder="Enter exam title..."
              value={examData.title}
              onChange={e =>
                setExamData({ ...examData, title: e.target.value })
              }
            />
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="exam-description">Description</Label>
            <Textarea
              id="exam-description"
              placeholder="Enter exam description..."
              value={examData.description}
              onChange={e =>
                setExamData({ ...examData, description: e.target.value })
              }
            />
          </div>
        </CardContent>
      </Card>

      {/* Question Creation */}
      <Card>
        <CardHeader>
          <CardTitle>Add Questions</CardTitle>
          <CardDescription>Create questions for your assessment</CardDescription>
        </CardHeader>
        <CardContent className="content-space-y-4">
          <div className="content-space-y-2">
            <Label htmlFor="question-type">Question Type *</Label>
            <Select value={questionType} onValueChange={setQuestionType}>
              <SelectTrigger>
                <SelectValue placeholder="Select question type..." />
              </SelectTrigger>
              <SelectContent>
                {questionTypes.map(type => (
                  <SelectItem key={type.value} value={type.value}>
                    {type.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="question-text">Question Text *</Label>
            <Textarea
              id="question-text"
              placeholder="Enter your question..."
              value={currentQuestion.text}
              onChange={e =>
                setCurrentQuestion({
                  ...currentQuestion,
                  text: e.target.value,
                })
              }
            />
          </div>

          {(questionType === 'multiple-choice-single' ||
            questionType === 'multiple-choice-multiple') && (
            <div className="content-space-y-2">
              <Label>Answer Options *</Label>
              {currentQuestion.options.map((option, index) => (
                <div
                  key={index}
                  className="content-flex content-items-center content-gap-2"
                >
                  <Input
                    placeholder={`Option ${index + 1}`}
                    value={option}
                    onChange={e => updateOption(index, e.target.value)}
                  />
                  {index === 0 && (
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={addOption}
                    >
                      Add Option
                    </Button>
                  )}
                </div>
              ))}

              <div className="content-space-y-2">
                <Label>Correct Answer *</Label>
                <Select
                  value={currentQuestion.correctAnswer}
                  onValueChange={value =>
                    setCurrentQuestion({
                      ...currentQuestion,
                      correctAnswer: value,
                    })
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select correct answer..." />
                  </SelectTrigger>
                  <SelectContent>
                    {currentQuestion.options.map(
                      (option, index) =>
                        option && (
                          <SelectItem key={index} value={option}>
                            {option}
                          </SelectItem>
                        ),
                    )}
                  </SelectContent>
                </Select>
              </div>
            </div>
          )}

          {(questionType === 'true-false' || questionType === 'yes-no') && (
            <div className="content-space-y-2">
              <Label>Correct Answer *</Label>
              <Select
                value={currentQuestion.correctAnswer}
                onValueChange={value =>
                  setCurrentQuestion({
                    ...currentQuestion,
                    correctAnswer: value,
                  })
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select correct answer..." />
                </SelectTrigger>
                <SelectContent>
                  {questionType === 'true-false' ? (
                    <>
                      <SelectItem value="true">True</SelectItem>
                      <SelectItem value="false">False</SelectItem>
                    </>
                  ) : (
                    <>
                      <SelectItem value="yes">Yes</SelectItem>
                      <SelectItem value="no">No</SelectItem>
                    </>
                  )}
                </SelectContent>
              </Select>
            </div>
          )}

          <div className="content-flex content-gap-2">
            <Button onClick={saveQuestion}>
              <Save className="content-mr-2 content-size-4" />
              Save Question
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Questions List */}
      {questions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Added Questions ({questions.length})</CardTitle>
            <CardDescription>Review your assessment questions</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="content-space-y-4">
              {questions.map((question, index) => (
                <div
                  key={question.id}
                  className="content-rounded-lg content-border content-p-4"
                >
                  <div className="content-flex content-items-start content-justify-between">
                    <div className="content-flex-1">
                      <h4 className="content-font-medium content-text-white">
                        Question {index + 1}
                      </h4>
                      <p className="content-mt-1 content-text-sm content-text-cloudy-white">
                        {question.text}
                      </p>
                      {question.options && question.options.length > 0 && (
                        <div className="content-mt-2">
                          <p className="content-text-xs content-font-medium content-text-cloudy-white">
                            Options:
                          </p>
                          <ul className="content-list-inside content-list-disc content-text-xs content-text-muted-foreground">
                            {question.options.map(
                              (option: string, idx: number) =>
                                option && <li key={idx}>{option}</li>,
                            )}
                          </ul>
                        </div>
                      )}
                      <p className="content-mt-1 content-text-xs content-text-green-600">
                        Correct: {question.correctAnswer}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Create Exam Button */}
      <div className="content-flex content-justify-end">
        <Button onClick={createExam} size="lg">
          <PlusCircle className="content-mr-2 content-size-4" />
          Create Assessment
        </Button>
      </div>
    </div>
  );
};

export default CreateExam;
