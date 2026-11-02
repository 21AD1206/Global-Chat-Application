import { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { formatTime } from '../utils/format.js';

/**
 * Scrollable list of chat bubbles. My messages are on the right.
 * The sender's name is shown when it differs from the previous message (global chat only).
 * Automatically scrolls to the newest message.
 */
export default function MessageList({ messages, me, showSender = true, emptyText = 'No messages yet.' }) {
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ block: 'end' });
  }, [messages.length]);

  if (messages.length === 0) {
    return (
      <div className="messages">
        <div className="empty">{emptyText}</div>
      </div>
    );
  }

  return (
    <div className="messages">
      {messages.map((m, i) => {
        const mine = m.senderUsername === me;
        const prev = messages[i - 1];
        const showName = showSender && !mine && (!prev || prev.senderUsername !== m.senderUsername);
        return (
          <div key={m.id} className={`msg-row ${mine ? 'mine' : ''}`}>
            <div className="bubble">
              {showName && (
                <Link to={`/users/${m.senderUsername}`} className="msg-sender">
                  {m.senderName} <span>@{m.senderUsername}</span>
                </Link>
              )}
              <div className="msg-text">{m.content}</div>
              <div className="msg-time">{formatTime(m.sentAt)}</div>
            </div>
          </div>
        );
      })}
      <div ref={bottomRef} />
    </div>
  );
}
