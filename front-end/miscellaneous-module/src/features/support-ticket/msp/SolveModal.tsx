import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from 'common/Select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import {
  Trash,
  Upload,
  Send,
  MessageSquare,
  User,
  MoreVertical,
  Reply,
  X,
  File,
  Link,
} from 'lucide-react';
import {
  IComment,
  ISupportTicketList,
  SupportTicketStatus,
} from 'models/SupportTicket';
import { useEffect, useState } from 'react';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn, isSuccessResponse } from 'utils/Helper';
import { toast } from 'react-toastify';
import { useUploader } from 'hooks/UseUploader';
import { Textarea } from 'common/Textarea';
import { useStore } from 'hooks/UseStore';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { validateSupportTicketComment } from 'utils/SupportTicketValidation';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
  ticket: ISupportTicketList;
}

const MSPAdminSolveModal = ({
  isOpen,
  onClose,
  onSubmit,
  ticket,
}: IProps) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();
  const { uploadFile } = useUploader();

  const [submitting, setSubmitting] = useState(false);
  const [loadingComments, setLoadingComments] = useState(false);
  const [selectedFiles, setSelectedFiles] = useState<File>();
  const [comments, setComments] = useState<Array<IComment>>([]);
  const [newComment, setNewComment] = useState('');
  const [status, setStatus] = useState<SupportTicketStatus>(
    ticket.status || SupportTicketStatus.OPEN,
  );
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [replyingTo, setReplyingTo] = useState<string | null>(null);
  const [replyText, setReplyText] = useState('');
  const [showMenu, setShowMenu] = useState<string | null>(null);
  const [commentError, setCommentError] = useState('');
  const [replyError, setReplyError] = useState('');

  const fetchComments = async () => {
    setLoadingComments(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_COMMENTS.replace(':id', ticket.id),
      );
      if (isSuccessResponse(response.statusCode)) {
        setComments(response.data.items);
      }
    } catch (error) {
      console.error(error);
      toast.error('Failed to load comments');
    } finally {
      setLoadingComments(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchComments();
    }
  }, [isOpen]);

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

  const handleSubmitComment = async () => {
    if (!newComment.trim() && !selectedFiles) return;

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
      let attachmentUrl: string = '';
      if (selectedFiles) {
        const { url, error } = await uploadFile(selectedFiles, 'CONTENT');
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
        API_END_POINTS.CREATE_COMMENT.replace(':id', ticket.id),
        {
          data: payload,
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success('Comment added successfully');
        setNewComment('');
        setSelectedFiles(undefined);
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
        API_END_POINTS.CREATE_COMMENT.replace(':id', ticket.id),
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
    setSelectedFiles(undefined);
    setReplyingTo(null);
    setReplyText('');
    setShowMenu(null);
    setCommentError('');
    setReplyError('');
    onClose();
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files) {
      const fileArray = Array.from(files);
      const maxFileSize = 2 * 1024 * 1024; // 2 MB in bytes
      const maxTotalSize = 10 * 1024 * 1024; // 10 MB in bytes

      const invalidFiles = fileArray.filter(file => file.size > maxFileSize);
      const validFiles = fileArray.filter(file => file.size <= maxFileSize);

      if (invalidFiles.length > 0) {
        const fileNames = invalidFiles.map(f => f.name).join(', ');
        toast.error(`The following files exceed 2 MB: ${fileNames}`);
      }

      if (validFiles.length > 0) {
        const currentTotalSize = selectedFiles?.size || 0;
        const newFilesSize = validFiles[0].size;
        const totalSize = currentTotalSize + newFilesSize;

        if (totalSize > maxTotalSize) return;
        setSelectedFiles(validFiles[0]);
      }
    }

    e.target.value = '';
  };

  const removeFile = () => {
    setSelectedFiles(undefined);
  };

  const handleUpdateStatus = async () => {
    setUpdatingStatus(true);
    try {
      const response = await apiClient.put(
        API_END_POINTS.SOLVE_SUPPORT_TICKET.replace(':id', ticket.id) +
        '?status=' +
        status,
      );
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Ticket status updated successfully');
        onSubmit();
        handleClose();
      } else {
        toast.error(response.data?.data?.error || 'Failed to update status');
      }
    } catch (error) {
      console.error(error);
      toast.error('Failed to update ticket status');
    } finally {
      setUpdatingStatus(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="flex max-h-[90vh] w-full max-w-4xl flex-col overflow-hidden">
        <DialogHeader>
          <DialogTitle>Solve {ticket.ticketId}</DialogTitle>
          <DialogDescription>
            Manage ticket status and communicate with the user
          </DialogDescription>
        </DialogHeader>

        <div className="flex-1 space-y-6 overflow-y-auto pr-2">
          {/* Status Selection */}
          <div className="rounded-lg border border-card-border p-4">
            <Label htmlFor="status">Ticket Status *</Label>
            <Select
              value={status}
              onValueChange={value => setStatus(value as SupportTicketStatus)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={SupportTicketStatus.OPEN}>Open</SelectItem>
                <SelectItem value={SupportTicketStatus.CLOSED}>
                  Closed
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Comments Thread */}
          <div>
            <div className="mb-4 flex items-center gap-2">
              <MessageSquare className="size-5 text-gray-600" />
              <h3 className="text-lg font-semibold text-white">
                Conversation ({comments.length})
              </h3>
            </div>

            {loadingComments ? (
              <div className="py-8 text-center text-gray-500">
                Loading comments...
              </div>
            ) : comments.length === 0 ? (
              <div className="mb-6 rounded-lg bg-muted/50 py-8 text-center text-muted-foreground">
                No comments yet. Start the conversation!
              </div>
            ) : (
              <div className="space-y-4">
                {comments.map(comment => {
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
                                {isCurrentUser ? 'You' : comment.authorName}
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
                                  href={
                                    FILE_PATH_PREFIX + comment.attachmentUrl
                                  }
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className={`flex items-center gap-2 text-xs underline ${isCurrentUser ? 'text-white' : 'text-blue-600'}`}
                                >
                                  <Link className="size-4" /> View Attachment
                                </a>
                              </div>
                            )}

                            {/* Three-dot Menu */}
                            <div
                              className={`absolute top-1 ${isCurrentUser ? 'left-1' : 'right-1'} opacity-0 transition-opacity group-hover:opacity-100`}
                            >
                              <div className="relative">
                                <button
                                  onClick={() =>
                                    setShowMenu(
                                      showMenu === comment.id
                                        ? null
                                        : comment.id,
                                    )
                                  }
                                  className={`rounded p-1 hover:bg-opacity-20 ${isCurrentUser ? 'hover:bg-white' : 'hover:bg-gray-300'}`}
                                >
                                  <MoreVertical
                                    className={`size-4 text-gray-600`}
                                  />
                                </button>

                                {/* Dropdown Menu */}
                                {showMenu === comment.id && (
                                  <div className="absolute right-0 top-full z-10 mt-1 min-w-[120px] rounded-lg border border-gray-200 bg-white py-1 shadow-lg">
                                    <button
                                      onClick={() => {
                                        setReplyingTo(comment.id);
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
                          </div>
                        </div>
                      </div>

                      {/* Reply Form */}
                      {replyingTo === comment.id && (
                        <div className={`ml-12 space-y-3`}>
                          {/* Original Message Reference */}
                          <div className="flex items-center justify-between rounded border-l-4 border-primary p-3">
                            <div>
                              <div className="mb-1 flex items-center gap-2">
                                <Reply className="size-3 text-primary" />
                                <span className="text-xs font-semibold">
                                  Replying to {comment.authorName}
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
                                onClick={() => handleSubmitReply(comment.id)}
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
                      {comment.replies && comment.replies.length > 0 && (
                        <div className="w-full gap-2 space-y-3">
                          {comment.replies.map(reply => {
                            const isReplyCurrentUser =
                              reply.author === userInfo.userId;
                            return (
                              <div key={reply.id} className="w-full space-y-2">
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
                                  <span>Replied to {comment.commentText}</span>
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
                                          {isReplyCurrentUser
                                            ? 'You'
                                            : reply.authorName}
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
                                            href={
                                              FILE_PATH_PREFIX +
                                              reply.attachmentUrl
                                            }
                                            target="_blank"
                                            rel="noopener noreferrer"
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
          </div>

          {/* Add Comment Form */}
          <div className="rounded-lg border border-card-border p-4">
            <Label className="mb-3 block">Add Comment</Label>
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
                  id="attachments"
                  type="file"
                  multiple
                  onChange={handleFileUpload}
                  className="hidden"
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={() =>
                    document.getElementById('attachments')?.click()
                  }
                >
                  <Upload className="mr-2 size-4" />
                  Add Attachment
                </Button>
              </div>

              {/* Selected Files */}
              {selectedFiles && (
                <div className="space-y-2">
                  <div className="flex items-center justify-between rounded border border-card-border p-2">
                    <div className="flex min-w-0 flex-1 items-center justify-between gap-2">
                      <span className="truncate text-sm text-primary">
                        {selectedFiles.name}
                      </span>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={() => removeFile()}
                        className="ml-2"
                      >
                        <Trash className="size-4 text-destructive" />
                      </Button>
                    </div>
                  </div>
                </div>
              )}

              <div className="flex justify-end">
                <Button
                  type="button"
                  onClick={handleSubmitComment}
                  disabled={
                    submitting || (!newComment.trim() && !selectedFiles)
                  }
                >
                  <Send className="mr-2 size-4" />
                  {submitting ? 'Sending...' : 'Send Comment'}
                </Button>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex justify-end gap-2">
          <Button type="button" variant="outline" onClick={handleClose}>
            Cancel
          </Button>
          <Button
            type="button"
            onClick={handleUpdateStatus}
            disabled={updatingStatus}
          >
            {updatingStatus
              ? 'Updating...'
              : status === SupportTicketStatus.CLOSED
                ? 'Close Ticket'
                : 'Update Status'}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default MSPAdminSolveModal;
