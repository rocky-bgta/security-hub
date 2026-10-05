import { sanitizeHtml } from 'home-module/security';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'components/common/Dialog';
import { useAPI } from 'hooks/UseAPI';
import { useUploader } from 'hooks/UseUploader';
import {
  MessageSquare,
  User,
  Upload,
  Trash,
  Send,
  MoreVertical,
  Reply,
  X,
  File,
  Link,
} from 'lucide-react';
import {
  IComment,
  ISupportTicketDetails,
  SupportTicketStatus,
} from 'models/SupportTicket';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  cn,
  formateDateAndTime,
  getTicketAuthorDisplayName,
  isSuccessResponse,
} from 'utils/Helper';
import { toast } from 'react-toastify';
import { useStore } from 'hooks/UseStore';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import { Textarea } from 'common/Textarea';
import {
  getPriorityColor,
  getStatusColor,
  getStatusIcon,
} from 'pages/support-ticket/SupportTickets';
import { validateSupportTicketComment } from 'utils/SupportTicketValidation';
import {
  SUPPORT_TICKET_FILE_ACCEPT,
  getSupportTicketAttachmentLinkProps,
  validateSupportTicketFile,
} from 'utils/SupportTicketFileValidation';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  id: string;
}

const ClientUserViewModal = ({ isOpen, onClose, id }: IProps) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();
  const { role } = useAuth();
  const { uploadFile } = useUploader();
  const hideInternalContact = role === ROLE.CLIENT_USER;

  const [loading, setLoading] = useState(false);
  const [selectedTicket, setSelectedTicket] =
    useState<ISupportTicketDetails | null>(null);
  const [comments, setComments] = useState<Array<IComment>>([]);
  const [loadingComments, setLoadingComments] = useState(false);
  const [newComment, setNewComment] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [replyingTo, setReplyingTo] = useState<string | null>(null);
  const [replyText, setReplyText] = useState('');
  const [showMenu, setShowMenu] = useState<string | null>(null);
  const [commentError, setCommentError] = useState('');
  const [replyError, setReplyError] = useState('');

  useEffect(() => {
    if (id && isOpen) {
      fetchTicket();
      fetchComments();
    }
  }, [id, isOpen]);

  const fetchTicket = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_SUPPORT_TICKET_DETAILS.replace(':id', id),
      );
      if (isSuccessResponse(response.statusCode)) {
        setSelectedTicket(response.data);
      }
    } catch (error) {
      console.error('Error fetching ticket:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchComments = async () => {
    setLoadingComments(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_COMMENTS.replace(':id', id),
      );
      if (isSuccessResponse(response.statusCode)) {
        setComments(response.data.items);
      }
    } catch (error) {
      console.error('Error fetching comments:', error);
      toast.error('Failed to load comments');
    } finally {
      setLoadingComments(false);
    }
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;

    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined,
    });
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;

    const error = await validateSupportTicketFile(file);
    if (error) {
      toast.error(error);
      return;
    }

    setSelectedFile(file);
  };

  const removeFile = () => {
    setSelectedFile(null);
  };

  const handleSubmitComment = async () => {
    if (!newComment.trim() && !selectedFile) return;

    if (newComment.trim()) {
      const error = validateSupportTicketComment(newComment);
      if (error) {
        setCommentError(error);
        return;
      }
    }
    setCommentError('');

    setSubmitting(true);

    try {
      let attachmentUrl = '';
      if (selectedFile) {
        const fileError = await validateSupportTicketFile(selectedFile);
        if (fileError) {
          toast.error(fileError);
          setSubmitting(false);
          return;
        }

        const { url, error } = await uploadFile(selectedFile, 'CONTENT');
        if (error) {
          toast.error(error);
          setSubmitting(false);
          return;
        }
        attachmentUrl = url;
      }

      const payload = {
        commentText: newComment,
        parentCommentId: '',
        attachmentUrl: attachmentUrl,
      };

      const response = await apiClient.post(
        API_END_POINTS.CREATE_COMMENT.replace(':id', id),
        {
          data: payload,
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success('Comment added successfully');
        setNewComment('');
        setSelectedFile(null);
        fetchComments();
      } else {
        toast.error(response.data?.data?.error || 'Failed to add comment');
      }
    } catch (error) {
      console.error('Error submitting comment:', error);
      toast.error('Failed to add comment');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmitReply = async (parentCommentId: string) => {
    if (!replyText.trim()) return;

    const error = validateSupportTicketComment(replyText);
    if (error) {
      setReplyError(error);
      return;
    }
    setReplyError('');

    setSubmitting(true);

    try {
      const payload = {
        commentText: replyText,
        parentCommentId: parentCommentId,
        attachmentUrl: '',
      };

      const response = await apiClient.post(
        API_END_POINTS.CREATE_COMMENT.replace(':id', id),
        {
          data: payload,
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success('Reply added successfully');
        setReplyText('');
        setReplyingTo(null);
        fetchComments();
      } else {
        toast.error(response.data?.data?.error || 'Failed to add reply');
      }
    } catch (error) {
      console.error('Error submitting reply:', error);
      toast.error('Failed to add reply');
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    setNewComment('');
    setSelectedFile(null);
    setReplyingTo(null);
    setReplyText('');
    setShowMenu(null);
    setCommentError('');
    setReplyError('');
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogTrigger asChild></DialogTrigger>
      <DialogContent className="flex max-h-[90vh] w-full max-w-4xl flex-col overflow-hidden">
        <DialogHeader>
          <DialogTitle>Ticket Details</DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <p>Loading...</p>
          </div>
        ) : (
          selectedTicket && (
            <div className="flex-1 space-y-4 overflow-y-auto pr-2 sm:space-y-6">
              {/* Ticket Information */}
              <div className="space-y-3 border-b border-card-border pb-4 sm:space-y-4 sm:pb-6">
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
                  <div>
                    <Label>Ticket ID</Label>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {selectedTicket?.ticketId}
                    </p>
                  </div>
                  <div>
                    <Label>Status</Label>
                    <div className="mt-1">
                      <Badge className={getStatusColor(selectedTicket?.status)}>
                        <div className="flex items-center gap-1">
                          {getStatusIcon(selectedTicket?.status)}
                          {selectedTicket?.status}
                        </div>
                      </Badge>
                    </div>
                  </div>
                  <div>
                    <Label>Priority</Label>
                    <div className="mt-1">
                      <Badge
                        className={getPriorityColor(selectedTicket?.priority)}
                      >
                        {selectedTicket?.priority}
                      </Badge>
                    </div>
                  </div>
                  <div>
                    <Label>Submitted</Label>
                    <p className="mt-1 text-sm">
                      {formateDateAndTime(selectedTicket?.createdDate)}
                    </p>
                  </div>
                </div>
                <div>
                  <Label>Support Type</Label>
                  <p className="mt-1 text-muted-foreground">
                    {selectedTicket?.supportType?.name}
                  </p>
                </div>
                <div>
                  <Label>Subject</Label>
                  <p className="mt-1 text-muted-foreground">
                    {selectedTicket?.title}
                  </p>
                </div>
                <div>
                  <Label>Description</Label>
                  <div
                    className="mt-1 text-sm text-muted-foreground"
                    dangerouslySetInnerHTML={{
                      __html: sanitizeHtml(selectedTicket?.description ?? ''),
                    }}
                  />
                </div>
                {selectedTicket?.attachments?.length > 0 && (
                  <div>
                    <Label>Attachments</Label>
                    <div className="mt-1 flex items-center justify-start gap-2 text-sm text-muted-foreground">
                      {selectedTicket?.attachments?.map(attachment => (
                        <a
                          key={attachment}
                          className="flex items-center gap-2 rounded-md px-2 py-1 text-sm text-white underline hover:text-primary"
                          {...getSupportTicketAttachmentLinkProps(attachment)}
                        >
                          <File className="size-4" /> Open File
                        </a>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              {/* Comments Section */}
              <div>
                <div className="mb-4 flex items-center gap-2">
                  <MessageSquare className="size-5 text-gray-600" />
                  <h3 className="text-lg font-semibold text-white">
                    Conversation ({comments?.length})
                  </h3>
                </div>

                {loadingComments ? (
                  <div className="py-8 text-center text-gray-500">
                    Loading comments...
                  </div>
                ) : comments?.length === 0 &&
                  selectedTicket?.status !== SupportTicketStatus.CLOSED ? (
                  <div className="mb-6 rounded-lg bg-muted/50 py-8 text-center text-muted-foreground">
                    No comments yet. Be the first to comment!
                  </div>
                ) : (
                  <div className="mb-6 space-y-4">
                    {comments?.map(comment => {
                      const isCurrentUser = comment.author === userInfo.userId;
                      return (
                        <div key={comment.id} className="space-y-3">
                          <div
                            className={`flex gap-3 ${isCurrentUser ? 'flex-row-reverse' : 'flex-row'}`}
                          >
                            {/* Avatar */}
                            <div
                              className={`flex size-10 shrink-0 items-center justify-center rounded-full ${isCurrentUser ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600'}`}
                            >
                              <User className="size-5" />
                            </div>

                            {/* Comment Content */}
                            <div
                              className={`flex flex-1 flex-col ${isCurrentUser ? 'items-end' : 'items-start'}`}
                            >
                              <div
                                className={`${isCurrentUser ? 'bg-primary text-white' : 'bg-gray-100 text-gray-900'} group relative max-w-[80%] rounded-lg px-6 py-3`}
                              >
                                <div className="mb-1 flex items-center gap-2">
                                  <span
                                    className={`text-sm font-semibold ${isCurrentUser ? 'text-white' : 'text-gray-700'}`}
                                  >
                                    {getTicketAuthorDisplayName(
                                      comment.authorName,
                                      isCurrentUser,
                                      hideInternalContact,
                                    )}
                                  </span>
                                  <span
                                    className={`text-xs ${isCurrentUser ? 'text-white' : 'text-gray-500'}`}
                                  >
                                    {formatDate(comment.commentedAt)}
                                  </span>
                                </div>
                                <p
                                  className={`whitespace-pre-wrap text-sm ${isCurrentUser ? 'text-white' : 'text-gray-900'}`}
                                >
                                  {comment.commentText}
                                </p>

                                {/* Attachment */}
                                {comment.attachmentUrl && (
                                  <div className="mt-3">
                                    <a
                                      {...getSupportTicketAttachmentLinkProps(
                                        String(comment.attachmentUrl),
                                      )}
                                      className={`flex items-center gap-2 text-xs underline ${isCurrentUser ? 'text-white' : 'text-blue-600'}`}
                                    >
                                      <Link className="size-4" /> View
                                      Attachment
                                    </a>
                                  </div>
                                )}

                                {/* Three-dot Menu */}
                                {selectedTicket?.status !==
                                  SupportTicketStatus.CLOSED && (
                                    <div
                                      className={`absolute top-1 ${isCurrentUser ? 'left-1' : 'right-1'} opacity-0 transition-opacity group-hover:opacity-100`}
                                    >
                                      <div className="relative">
                                        <button
                                          onClick={() =>
                                            setShowMenu(
                                              showMenu === comment?.id
                                                ? null
                                                : comment?.id,
                                            )
                                          }
                                          className={`rounded p-1 hover:bg-opacity-20 ${isCurrentUser ? 'hover:bg-white' : 'hover:bg-gray-300'}`}
                                        >
                                          <MoreVertical
                                            className={`size-4 text-gray-600`}
                                          />
                                        </button>

                                        {/* Dropdown Menu */}
                                        {showMenu === comment?.id && (
                                          <div className="absolute right-0 top-full z-10 mt-1 min-w-[120px] rounded-lg border border-gray-200 bg-white py-1 shadow-lg">
                                            <button
                                              onClick={() => {
                                                setReplyingTo(comment?.id);
                                                setShowMenu(null);
                                              }}
                                              className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                                            >
                                              <Reply className="size-4" />
                                              Reply
                                            </button>
                                          </div>
                                        )}
                                      </div>
                                    </div>
                                  )}
                              </div>
                            </div>
                          </div>

                          {/* Reply Form */}
                          {replyingTo === comment?.id && (
                            <div className={`ml-12 space-y-3`}>
                              {/* Original Message Reference */}
                              <div className="flex items-center justify-between rounded border-l-4 border-primary p-3">
                                <div>
                                  <div className="mb-1 flex items-center gap-2">
                                    <Reply className="size-3 text-primary" />
                                    <span className="text-xs font-semibold">
                                      Replying to{' '}
                                      {getTicketAuthorDisplayName(
                                        comment.authorName,
                                        isCurrentUser,
                                        hideInternalContact,
                                      )}
                                    </span>
                                  </div>
                                  <p className="line-clamp-2 text-sm">
                                    {comment.commentText}
                                  </p>
                                </div>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  onClick={() => {
                                    setReplyingTo(null);
                                    setReplyText('');
                                  }}
                                >
                                  <X className="size-3 text-destructive" />
                                </Button>
                              </div>

                              {/* Reply Input */}
                              <div className="rounded-lg border border-card-border p-3">
                                <Textarea
                                  value={replyText}
                                  onChange={e => {
                                    setReplyText(e.target.value);
                                    setReplyError('');
                                  }}
                                  placeholder="Type your reply..."
                                  rows={3}
                                  className="mb-3"
                                />
                                {replyError && (
                                  <p className="mb-3 text-sm text-red-500">
                                    {replyError}
                                  </p>
                                )}
                                <div className="flex justify-end gap-2">
                                  <Button
                                    type="button"
                                    variant="outline"
                                    size="sm"
                                    onClick={() => {
                                      setReplyingTo(null);
                                      setReplyText('');
                                    }}
                                  >
                                    Cancel
                                  </Button>
                                  <Button
                                    type="button"
                                    size="sm"
                                    onClick={() =>
                                      handleSubmitReply(comment?.id)
                                    }
                                    disabled={submitting || !replyText.trim()}
                                  >
                                    <Send className="mr-2 size-3" />
                                    {submitting ? 'Sending...' : 'Send Reply'}
                                  </Button>
                                </div>
                              </div>
                            </div>
                          )}

                          {/* Render Replies */}
                          {comment?.replies && comment?.replies?.length > 0 && (
                            <div className="w-full gap-2 space-y-3">
                              {comment.replies.map(reply => {
                                const isReplyCurrentUser =
                                  reply.author === userInfo.userId;
                                return (
                                  <div
                                    key={reply.id}
                                    className="w-full space-y-2"
                                  >
                                    {/* Reply Reference Indicator */}
                                    <div
                                      className={cn(
                                        'flex gap-2 text-xs text-gray-500',
                                        isReplyCurrentUser
                                          ? 'flex-row-reverse'
                                          : 'flex-row',
                                      )}
                                    >
                                      <Reply className="size-3" />
                                      <span>
                                        Replied to {comment.commentText}
                                      </span>
                                    </div>

                                    {/* Reply Message */}
                                    <div
                                      className={`flex gap-3 ${isReplyCurrentUser ? 'flex-row-reverse' : 'flex-row'}`}
                                    >
                                      {/* Avatar */}
                                      <div
                                        className={`flex size-8 shrink-0 items-center justify-center rounded-full ${isReplyCurrentUser ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600'}`}
                                      >
                                        <User className="size-4" />
                                      </div>

                                      {/* Reply Content */}
                                      <div
                                        className={`flex flex-1 flex-col ${isReplyCurrentUser ? 'items-end' : 'items-start'}`}
                                      >
                                        <div
                                          className={`${isReplyCurrentUser ? 'bg-primary text-white' : 'bg-gray-100 text-gray-900'} max-w-[75%] rounded-lg px-3 py-2`}
                                        >
                                          <div className="mb-1 flex items-center gap-2">
                                            <span
                                              className={`text-xs font-semibold ${isReplyCurrentUser ? 'text-white' : 'text-gray-700'}`}
                                            >
                                              {getTicketAuthorDisplayName(
                                                reply.authorName,
                                                isReplyCurrentUser,
                                                hideInternalContact,
                                              )}
                                            </span>
                                            <span
                                              className={`text-xs ${isReplyCurrentUser ? 'text-white/80' : 'text-gray-500'}`}
                                            >
                                              {formatDate(reply.commentedAt)}
                                            </span>
                                          </div>
                                          <p
                                            className={`whitespace-pre-wrap text-sm ${isReplyCurrentUser ? 'text-white' : 'text-gray-900'}`}
                                          >
                                            {reply.commentText}
                                          </p>

                                          {/* Attachment */}
                                          {reply.attachmentUrl && (
                                            <div className="mt-2">
                                              <a
                                                {...getSupportTicketAttachmentLinkProps(
                                                  String(reply.attachmentUrl),
                                                )}
                                                className={`flex items-center gap-2 text-xs underline ${isReplyCurrentUser ? 'text-white' : 'text-blue-600'}`}
                                              >
                                                <Link className="size-4" />
                                                View Attachment
                                              </a>
                                            </div>
                                          )}
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                );
                              })}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}

                {/* Add Comment Form */}
                {selectedTicket?.status !== SupportTicketStatus.CLOSED && (
                  <div className="mb-2 rounded-lg border border-card-border p-4">
                    <Label className="mb-3 block text-primary">
                      Add Comment
                    </Label>
                    <div className="space-y-3">
                      <Textarea
                        value={newComment}
                        onChange={e => {
                          setNewComment(e.target.value);
                          setCommentError('');
                        }}
                        placeholder="Enter your response..."
                        rows={3}
                      />
                      {commentError && (
                        <p className="text-sm text-red-500">{commentError}</p>
                      )}

                      {/* File Upload */}
                      <div>
                        <Input
                          id="attachment"
                          type="file"
                          accept={SUPPORT_TICKET_FILE_ACCEPT}
                          onChange={handleFileUpload}
                          className="hidden"
                        />
                        <Button
                          type="button"
                          variant="outline"
                          onClick={() =>
                            document.getElementById('attachment')?.click()
                          }
                        >
                          <Upload className="mr-2 size-4" />
                          Add Attachment
                        </Button>
                        <span className="ml-2 text-xs text-gray-500">
                          PDF, JPG, PNG, DOC, DOCX (Max 2 MB)
                        </span>
                      </div>

                      {/* Selected File */}
                      {selectedFile && (
                        <div className="flex items-center justify-between rounded border border-gray-200 bg-gray-50 p-2">
                          <div className="flex min-w-0 flex-1 items-center gap-2">
                            <span className="truncate text-sm text-gray-600">
                              {selectedFile?.name}
                            </span>
                            <span className="whitespace-nowrap text-xs text-gray-400">
                              (
                              {selectedFile?.size >= 1024 * 1024
                                ? `${(selectedFile?.size / (1024 * 1024)).toFixed(2)} MB`
                                : `${(selectedFile?.size / 1024).toFixed(2)} KB`}
                              )
                            </span>
                          </div>
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            onClick={removeFile}
                            className="ml-2"
                          >
                            <Trash className="size-4 text-red-500" />
                          </Button>
                        </div>
                      )}

                      <div className="flex justify-end">
                        <Button
                          type="button"
                          onClick={handleSubmitComment}
                          disabled={
                            submitting || (!newComment.trim() && !selectedFile)
                          }
                        >
                          <Send className="mr-2 size-4" />
                          {submitting ? 'Sending...' : 'Send Comment'}
                        </Button>
                      </div>
                    </div>
                  </div>
                )}

                {selectedTicket?.status === SupportTicketStatus?.CLOSED && (
                  <div className="border-t border-gray-200 pt-4">
                    <div className="rounded-lg bg-gray-50 p-4 text-center text-gray-600">
                      This ticket is closed. Comments are disabled.
                    </div>
                  </div>
                )}
              </div>
            </div>
          )
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ClientUserViewModal;
