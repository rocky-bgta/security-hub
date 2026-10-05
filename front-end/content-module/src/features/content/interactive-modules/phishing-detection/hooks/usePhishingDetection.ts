import { useCallback, useEffect, useRef, useState } from 'react';

import { DEFAULT_PHISHING_TIME_LIMIT, IPhishingEmail } from 'models/Content';

import {
  cloneInbox,
  IClassifyFeedback,
  IPhishingGameEmail,
} from '../types';

interface IUsePhishingDetectionArgs {
  emails: Array<IPhishingEmail>;
  timeLimitSeconds?: number;
  onGameOver?: () => void;
}

const FEEDBACK_DURATION_MS = 2000;

const usePhishingDetection = ({
  emails,
  timeLimitSeconds = DEFAULT_PHISHING_TIME_LIMIT,
  onGameOver,
}: IUsePhishingDetectionArgs) => {
  const gameOverRef = useRef(false);
  const endGameTimeoutRef = useRef<number | null>(null);
  const onGameOverRef = useRef(onGameOver);

  const [inbox, setInbox] = useState<Array<IPhishingGameEmail>>(() =>
    cloneInbox(emails),
  );
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [score, setScore] = useState(0);
  const [correctCount, setCorrectCount] = useState(0);
  const [totalEmails] = useState(emails.length);
  const [timeLeft, setTimeLeft] = useState(timeLimitSeconds);
  const [isGameOver, setIsGameOver] = useState(false);
  const [feedback, setFeedback] = useState<IClassifyFeedback | null>(null);
  const [hoveredUrl, setHoveredUrl] = useState<string | null>(null);
  const [showMobileViewer, setShowMobileViewer] = useState(false);

  const selectedEmail = inbox.find(email => email.id === selectedId) ?? null;
  const unreadCount = inbox.filter(email => !email.read).length;

  useEffect(() => {
    onGameOverRef.current = onGameOver;
  }, [onGameOver]);

  const clearEndGameTimeout = useCallback(() => {
    if (endGameTimeoutRef.current != null) {
      window.clearTimeout(endGameTimeoutRef.current);
      endGameTimeoutRef.current = null;
    }
  }, []);

  const endGame = useCallback(() => {
    if (gameOverRef.current) return;

    gameOverRef.current = true;
    clearEndGameTimeout();
    setIsGameOver(true);
    setSelectedId(null);
    setShowMobileViewer(false);
    setHoveredUrl(null);
    onGameOverRef.current?.();
  }, [clearEndGameTimeout]);

  const resetGame = useCallback(() => {
    gameOverRef.current = false;
    clearEndGameTimeout();
    setInbox(cloneInbox(emails));
    setSelectedId(null);
    setScore(0);
    setCorrectCount(0);
    setTimeLeft(timeLimitSeconds);
    setIsGameOver(false);
    setFeedback(null);
    setHoveredUrl(null);
    setShowMobileViewer(false);
  }, [clearEndGameTimeout, emails, timeLimitSeconds]);

  useEffect(() => {
    if (isGameOver || emails.length === 0) return undefined;

    let remaining = timeLeft;
    const intervalId = window.setInterval(() => {
      remaining -= 1;
      setTimeLeft(Math.max(0, remaining));
      if (remaining <= 0) {
        window.clearInterval(intervalId);
        endGame();
      }
    }, 1000);

    return () => window.clearInterval(intervalId);
    // Restart only when a new round begins, not on every tick.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [endGame, emails.length, isGameOver]);

  useEffect(() => {
    if (!feedback) return undefined;

    const timeoutId = window.setTimeout(() => {
      setFeedback(null);
    }, FEEDBACK_DURATION_MS);

    return () => window.clearTimeout(timeoutId);
  }, [feedback]);

  useEffect(() => () => clearEndGameTimeout(), [clearEndGameTimeout]);

  const openEmail = useCallback((id: string) => {
    if (gameOverRef.current) return;

    setInbox(prev =>
      prev.map(email => (email.id === id ? { ...email, read: true } : email)),
    );
    setSelectedId(id);
    setShowMobileViewer(true);
  }, []);

  const closeMobileViewer = useCallback(() => {
    setShowMobileViewer(false);
  }, []);

  const classifyEmail = useCallback(
    (chosePhishing: boolean) => {
      if (gameOverRef.current) return;

      if (!selectedEmail) {
        setFeedback({
          isCorrect: false,
          message: 'Please select an email first!',
        });
        return;
      }

      const isCorrect = selectedEmail.isPhishing === chosePhishing;
      const explanation = selectedEmail.explanation?.trim();

      if (isCorrect) {
        setScore(prev => prev + 1);
        setCorrectCount(prev => prev + 1);
        setFeedback({
          isCorrect: true,
          message: explanation || 'Correct! Well done!',
        });
      } else {
        setFeedback({
          isCorrect: false,
          message:
            explanation ||
            `Incorrect! This was ${
              selectedEmail.isPhishing
                ? 'a phishing email'
                : 'a legitimate email'
            }`,
        });
      }

      const remaining = inbox.filter(email => email.id !== selectedEmail.id);
      setInbox(remaining);
      setSelectedId(null);
      setShowMobileViewer(false);
      setHoveredUrl(null);

      if (remaining.length === 0) {
        endGameTimeoutRef.current = window.setTimeout(() => {
          endGame();
        }, FEEDBACK_DURATION_MS);
      }
    },
    [endGame, inbox, selectedEmail],
  );

  return {
    inbox,
    selectedEmail,
    score,
    correctCount,
    totalEmails,
    timeLeft,
    unreadCount,
    isGameOver,
    feedback,
    hoveredUrl,
    showMobileViewer,
    openEmail,
    closeMobileViewer,
    classifyEmail,
    resetGame,
    setHoveredUrl,
  };
};

export { usePhishingDetection };
