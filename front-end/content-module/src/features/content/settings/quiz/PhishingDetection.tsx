import { ChangeEvent, forwardRef, useImperativeHandle, useState } from 'react';
import { v4 as uuidv4 } from 'uuid';

import { DeleteIcon } from 'assets/icons';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from 'common/Accordion';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import { Textarea } from 'common/Textarea';
import TextEditor from 'common/TextEditor';
import {
  DEFAULT_PHISHING_TIME_LIMIT,
  IDataValidationHandle,
  IPhishingEmail,
  IPhishingEmailLink,
  IQuizFields,
} from 'models/Content';

interface IProps {
  data: IQuizFields;
  updateData: (data: IQuizFields) => void;
}

type EmailFieldErrors = {
  subject?: string;
  senderName?: string;
  senderEmail?: string;
  date?: string;
  time?: string;
  body?: string;
  links?: string;
};

type FormErrors = {
  timeLimit?: string;
  emails?: string;
  byEmail: Record<string, EmailFieldErrors>;
};

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_TIME_LIMIT = 10;

const isEmptyHtml = (html: string) =>
  html
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .trim().length === 0;

const createEmptyLink = (): IPhishingEmailLink => ({
  id: uuidv4(),
  label: '',
  displayText: '',
  actualUrl: '',
});

const createEmptyEmail = (): IPhishingEmail => ({
  id: uuidv4(),
  senderName: '',
  senderEmail: '',
  subject: '',
  date: '',
  time: '',
  avatar: '',
  body: '',
  links: [],
  isPhishing: true,
  explanation: '',
});

const FieldError = ({ message }: { message?: string }) => {
  if (!message) return null;

  return (
    <p className="content-mt-1 content-text-sm content-text-red-500">{message}</p>
  );
};

const PhishingDetectionSettings = forwardRef<IDataValidationHandle, IProps>(
  ({ data, updateData }, ref) => {
    const emails = data.emails ?? [];
    const timeLimitSeconds =
      data.timeLimitSeconds ?? DEFAULT_PHISHING_TIME_LIMIT;

    const [openEmailId, setOpenEmailId] = useState<string>(
      emails[0]?.id ?? '',
    );
    const [errors, setErrors] = useState<FormErrors>({ byEmail: {} });

    const updateEmails = (nextEmails: Array<IPhishingEmail>) => {
      updateData({
        ...data,
        emails: nextEmails,
      });
    };

    const updateEmail = (id: string, patch: Partial<IPhishingEmail>) => {
      updateEmails(
        emails.map(email => (email.id === id ? { ...email, ...patch } : email)),
      );
    };

    const clearEmailError = (id: string, field: keyof EmailFieldErrors) => {
      setErrors(prev => {
        const current = prev.byEmail[id];
        if (!current?.[field]) return prev;

        return {
          ...prev,
          emails: '',
          byEmail: {
            ...prev.byEmail,
            [id]: {
              ...current,
              [field]: undefined,
            },
          },
        };
      });
    };

    const handleInstructionsChange = (e: ChangeEvent<HTMLInputElement>) => {
      updateData({
        ...data,
        question: e.target.value,
      });
    };

    const handleTimeLimitChange = (e: ChangeEvent<HTMLInputElement>) => {
      const parsed = Number(e.target.value);
      updateData({
        ...data,
        timeLimitSeconds: Number.isFinite(parsed) ? parsed : 0,
      });
      setErrors(prev => ({
        ...prev,
        timeLimit:
          !Number.isFinite(parsed) || parsed < MIN_TIME_LIMIT
            ? `Time limit must be at least ${MIN_TIME_LIMIT} seconds`
            : '',
      }));
    };

    const handleAddEmail = () => {
      const nextEmail = createEmptyEmail();
      updateEmails([...emails, nextEmail]);
      setOpenEmailId(nextEmail.id);
      setErrors(prev => ({
        ...prev,
        emails: '',
      }));
    };

    const handleRemoveEmail = (id: string) => {
      const remaining = emails.filter(email => email.id !== id);
      updateEmails(remaining);
      if (openEmailId === id) {
        setOpenEmailId(remaining[0]?.id ?? '');
      }
      setErrors(prev => {
        const { [id]: _removed, ...rest } = prev.byEmail;
        return {
          ...prev,
          emails:
            remaining.length === 0 ? 'At least one email is required' : '',
          byEmail: rest,
        };
      });
    };

    const handleAddLink = (emailId: string) => {
      const email = emails.find(item => item.id === emailId);
      if (!email) return;

      updateEmail(emailId, {
        links: [...(email.links ?? []), createEmptyLink()],
      });
      clearEmailError(emailId, 'links');
    };

    const handleUpdateLink = (
      emailId: string,
      linkId: string,
      patch: Partial<IPhishingEmailLink>,
    ) => {
      const email = emails.find(item => item.id === emailId);
      if (!email) return;

      updateEmail(emailId, {
        links: (email.links ?? []).map(link =>
          link.id === linkId ? { ...link, ...patch } : link,
        ),
      });
      clearEmailError(emailId, 'links');
    };

    const handleRemoveLink = (emailId: string, linkId: string) => {
      const email = emails.find(item => item.id === emailId);
      if (!email) return;

      updateEmail(emailId, {
        links: (email.links ?? []).filter(link => link.id !== linkId),
      });
    };

    const validateFields = () => {
      const nextErrors: FormErrors = { byEmail: {} };
      let isValid = true;

      if (
        !Number.isFinite(timeLimitSeconds) ||
        timeLimitSeconds < MIN_TIME_LIMIT
      ) {
        isValid = false;
        nextErrors.timeLimit = `Time limit must be at least ${MIN_TIME_LIMIT} seconds`;
      }

      if (emails.length === 0) {
        isValid = false;
        nextErrors.emails = 'At least one email is required';
      }

      emails.forEach(email => {
        const emailErrors: EmailFieldErrors = {};

        if (!email.subject.trim()) {
          isValid = false;
          emailErrors.subject = 'Subject is required';
        }
        if (!email.senderName.trim()) {
          isValid = false;
          emailErrors.senderName = 'From name is required';
        }
        if (!email.senderEmail.trim()) {
          isValid = false;
          emailErrors.senderEmail = 'From email is required';
        } else if (!EMAIL_PATTERN.test(email.senderEmail.trim())) {
          isValid = false;
          emailErrors.senderEmail = 'Enter a valid email address';
        }
        if (!email.date.trim()) {
          isValid = false;
          emailErrors.date = 'Date is required';
        }
        if (!email.time.trim()) {
          isValid = false;
          emailErrors.time = 'Time is required';
        }
        if (isEmptyHtml(email.body ?? '')) {
          isValid = false;
          emailErrors.body = 'Email body is required';
        }

        const incompleteLink = (email.links ?? []).some(
          link => !link.displayText.trim() || !link.actualUrl.trim(),
        );
        if (incompleteLink) {
          isValid = false;
          emailErrors.links =
            'Each hover link needs display text and an actual URL';
        }

        if (Object.keys(emailErrors).length > 0) {
          nextErrors.byEmail[email.id] = emailErrors;
        }
      });

      setErrors(nextErrors);
      return isValid;
    };

    useImperativeHandle(ref, () => ({
      validateData: () => ({ success: validateFields() }),
    }));

    return (
      <div className="content-mt-5 content-space-y-5">
        <div className="content-space-y-2">
          <Label
            htmlFor="phishing-detection-instructions"
            className="content-text-sm content-font-medium content-text-white"
          >
            Instructions (optional)
          </Label>
          <Input
            id="phishing-detection-instructions"
            type="text"
            placeholder="Shown as the subtitle under the module title"
            value={data.question ?? ''}
            onChange={handleInstructionsChange}
            className="content-w-full"
          />
        </div>

        <div className="content-space-y-2">
          <Label
            htmlFor="phishing-detection-time-limit"
            className="content-text-sm content-font-medium content-text-white"
          >
            Time limit (seconds)
          </Label>
          <Input
            id="phishing-detection-time-limit"
            type="number"
            min={MIN_TIME_LIMIT}
            placeholder="180"
            value={String(timeLimitSeconds)}
            onChange={handleTimeLimitChange}
            className="content-w-full"
          />
          <p className="content-text-sm content-text-muted-foreground">
            Learners classify every inbox email before this countdown ends.
            Default is {DEFAULT_PHISHING_TIME_LIMIT} seconds.
          </p>
          <FieldError message={errors.timeLimit} />
        </div>

        <div className="content-flex content-items-center content-justify-between content-gap-3">
          <div>
            <h4 className="content-text-base content-font-semibold content-text-white">
              Inbox emails
            </h4>
            <p className="content-text-sm content-text-muted-foreground">
              Add the emails learners will inspect. Hover links are the main
              phishing clue.
            </p>
          </div>
          <Button type="button" size="sm" onClick={handleAddEmail}>
            Add email
          </Button>
        </div>
        <FieldError message={errors.emails} />

        {emails.length > 0 && (
          <Accordion
            type="single"
            collapsible
            value={openEmailId}
            onValueChange={setOpenEmailId}
            className="content-rounded-lg content-border content-border-steel-gray content-px-3"
          >
            {emails.map((email, index) => {
              const emailErrors = errors.byEmail[email.id] ?? {};
              const title = email.subject.trim() || `Email ${index + 1}`;

              return (
                <AccordionItem key={email.id} value={email.id}>
                  <div className="content-flex content-items-center content-gap-2">
                    <AccordionTrigger className="content-flex-1 content-text-left content-text-white">
                      <span className="content-flex content-items-center content-gap-2">
                        <span>{title}</span>
                        <span className="content-text-xs content-font-normal content-text-muted-foreground">
                          {email.isPhishing ? 'Phishing' : 'Safe'}
                        </span>
                      </span>
                    </AccordionTrigger>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="content-shrink-0 content-text-red-400"
                      onClick={() => handleRemoveEmail(email.id)}
                      aria-label={`Remove ${title}`}
                    >
                      <DeleteIcon fill="currentColor" />
                    </Button>
                  </div>
                  <AccordionContent>
                    <div className="content-grid content-gap-4 md:content-grid-cols-2">
                      <div className="content-space-y-2 md:content-col-span-2">
                        <Label
                          htmlFor={`phishing-subject-${email.id}`}
                          className="content-text-white"
                        >
                          Subject
                        </Label>
                        <Input
                          id={`phishing-subject-${email.id}`}
                          value={email.subject}
                          placeholder="Your account has been compromised"
                          onChange={e => {
                            updateEmail(email.id, { subject: e.target.value });
                            clearEmailError(email.id, 'subject');
                          }}
                        />
                        <FieldError message={emailErrors.subject} />
                      </div>

                      <div className="content-space-y-2">
                        <Label
                          htmlFor={`phishing-sender-name-${email.id}`}
                          className="content-text-white"
                        >
                          From name
                        </Label>
                        <Input
                          id={`phishing-sender-name-${email.id}`}
                          value={email.senderName}
                          placeholder="Amazon Support"
                          onChange={e => {
                            updateEmail(email.id, {
                              senderName: e.target.value,
                            });
                            clearEmailError(email.id, 'senderName');
                          }}
                        />
                        <FieldError message={emailErrors.senderName} />
                      </div>

                      <div className="content-space-y-2">
                        <Label
                          htmlFor={`phishing-sender-email-${email.id}`}
                          className="content-text-white"
                        >
                          From email
                        </Label>
                        <Input
                          id={`phishing-sender-email-${email.id}`}
                          type="email"
                          value={email.senderEmail}
                          placeholder="security@amazon.com"
                          onChange={e => {
                            updateEmail(email.id, {
                              senderEmail: e.target.value,
                            });
                            clearEmailError(email.id, 'senderEmail');
                          }}
                        />
                        <FieldError message={emailErrors.senderEmail} />
                      </div>

                      <div className="content-space-y-2">
                        <Label
                          htmlFor={`phishing-date-${email.id}`}
                          className="content-text-white"
                        >
                          Date
                        </Label>
                        <Input
                          id={`phishing-date-${email.id}`}
                          value={email.date}
                          placeholder="June 15, 2023"
                          onChange={e => {
                            updateEmail(email.id, { date: e.target.value });
                            clearEmailError(email.id, 'date');
                          }}
                        />
                        <FieldError message={emailErrors.date} />
                      </div>

                      <div className="content-space-y-2">
                        <Label
                          htmlFor={`phishing-time-${email.id}`}
                          className="content-text-white"
                        >
                          Time
                        </Label>
                        <Input
                          id={`phishing-time-${email.id}`}
                          value={email.time}
                          placeholder="10:42 AM"
                          onChange={e => {
                            updateEmail(email.id, { time: e.target.value });
                            clearEmailError(email.id, 'time');
                          }}
                        />
                        <FieldError message={emailErrors.time} />
                      </div>

                      <div className="content-space-y-2 md:content-col-span-2">
                        <Label
                          htmlFor={`phishing-avatar-${email.id}`}
                          className="content-text-white"
                        >
                          Avatar initials (optional)
                        </Label>
                        <Input
                          id={`phishing-avatar-${email.id}`}
                          value={email.avatar ?? ''}
                          placeholder="Auto-generated from the sender name"
                          maxLength={3}
                          onChange={e =>
                            updateEmail(email.id, { avatar: e.target.value })
                          }
                        />
                      </div>

                      <div className="content-space-y-2 md:content-col-span-2">
                        <Label className="content-text-white">
                          Email body
                        </Label>
                        <TextEditor
                          value={email.body}
                          onChange={value => {
                            updateEmail(email.id, { body: value });
                            clearEmailError(email.id, 'body');
                          }}
                        />
                        <FieldError message={emailErrors.body} />
                      </div>

                      <div className="content-space-y-3 md:content-col-span-2">
                        <div className="content-flex content-items-center content-justify-between content-gap-3">
                          <div>
                            <Label className="content-text-white">
                              Hover links
                            </Label>
                            <p className="content-text-sm content-text-muted-foreground">
                              Learners see the display text. Hovering reveals
                              the actual URL without navigating.
                            </p>
                          </div>
                          <Button
                            type="button"
                            size="sm"
                            variant="outline"
                            onClick={() => handleAddLink(email.id)}
                          >
                            Add link
                          </Button>
                        </div>

                        {(email.links ?? []).map((link, linkIndex) => (
                          <div
                            key={link.id}
                            className="content-space-y-3 content-rounded-md content-border content-border-steel-gray content-p-3"
                          >
                            <div className="content-flex content-items-center content-justify-between">
                              <p className="content-text-sm content-font-medium content-text-white">
                                Link {linkIndex + 1}
                              </p>
                              <Button
                                type="button"
                                variant="ghost"
                                size="icon"
                                className="content-text-red-400"
                                onClick={() =>
                                  handleRemoveLink(email.id, link.id)
                                }
                                aria-label={`Remove link ${linkIndex + 1}`}
                              >
                                <DeleteIcon fill="currentColor" />
                              </Button>
                            </div>
                            <div className="content-grid content-gap-3 md:content-grid-cols-3">
                              <div className="content-space-y-2">
                                <Label
                                  htmlFor={`phishing-link-label-${link.id}`}
                                  className="content-text-white"
                                >
                                  Label (optional)
                                </Label>
                                <Input
                                  id={`phishing-link-label-${link.id}`}
                                  value={link.label ?? ''}
                                  placeholder="Verify your account now:"
                                  onChange={e =>
                                    handleUpdateLink(email.id, link.id, {
                                      label: e.target.value,
                                    })
                                  }
                                />
                              </div>
                              <div className="content-space-y-2">
                                <Label
                                  htmlFor={`phishing-link-display-${link.id}`}
                                  className="content-text-white"
                                >
                                  Display text
                                </Label>
                                <Input
                                  id={`phishing-link-display-${link.id}`}
                                  value={link.displayText}
                                  placeholder="https://www.amazon.com/verify"
                                  onChange={e =>
                                    handleUpdateLink(email.id, link.id, {
                                      displayText: e.target.value,
                                    })
                                  }
                                />
                              </div>
                              <div className="content-space-y-2">
                                <Label
                                  htmlFor={`phishing-link-url-${link.id}`}
                                  className="content-text-white"
                                >
                                  Actual URL
                                </Label>
                                <Input
                                  id={`phishing-link-url-${link.id}`}
                                  value={link.actualUrl}
                                  placeholder="http://amazon-security-update.com/verify"
                                  onChange={e =>
                                    handleUpdateLink(email.id, link.id, {
                                      actualUrl: e.target.value,
                                    })
                                  }
                                />
                              </div>
                            </div>
                          </div>
                        ))}
                        <FieldError message={emailErrors.links} />
                      </div>

                      <div className="content-space-y-2 md:content-col-span-2">
                        <Label className="content-text-white">
                          This email is
                        </Label>
                        <RadioGroup
                          value={email.isPhishing ? 'phishing' : 'safe'}
                          onValueChange={value =>
                            updateEmail(email.id, {
                              isPhishing: value === 'phishing',
                            })
                          }
                          className="!content-flex content-gap-6"
                        >
                          <div className="content-flex content-items-center content-space-x-2">
                            <RadioGroupItem
                              value="phishing"
                              id={`phishing-type-phishing-${email.id}`}
                            />
                            <Label
                              htmlFor={`phishing-type-phishing-${email.id}`}
                              className="content-cursor-pointer content-text-white"
                            >
                              Phishing
                            </Label>
                          </div>
                          <div className="content-flex content-items-center content-space-x-2">
                            <RadioGroupItem
                              value="safe"
                              id={`phishing-type-safe-${email.id}`}
                            />
                            <Label
                              htmlFor={`phishing-type-safe-${email.id}`}
                              className="content-cursor-pointer content-text-white"
                            >
                              Safe
                            </Label>
                          </div>
                        </RadioGroup>
                      </div>

                      <div className="content-space-y-2 md:content-col-span-2">
                        <Label
                          htmlFor={`phishing-explanation-${email.id}`}
                          className="content-text-white"
                        >
                          Explanation (optional)
                        </Label>
                        <Textarea
                          id={`phishing-explanation-${email.id}`}
                          value={email.explanation ?? ''}
                          placeholder="Shown after the learner classifies this email"
                          onChange={e =>
                            updateEmail(email.id, {
                              explanation: e.target.value,
                            })
                          }
                        />
                      </div>
                    </div>
                  </AccordionContent>
                </AccordionItem>
              );
            })}
          </Accordion>
        )}
      </div>
    );
  },
);

PhishingDetectionSettings.displayName = 'PhishingDetectionSettings';

export default PhishingDetectionSettings;
