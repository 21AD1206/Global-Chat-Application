import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useLiveStatus, useSocket, useSocketEvent } from '../context/SocketContext.jsx';
import MessageList from '../components/MessageList.jsx';
import MessageComposer from '../components/MessageComposer.jsx';
import OnlineUsersPanel from '../components/OnlineUsersPanel.jsx';

/**
 * Global chat dashboard.
 * 1. On open (and after every reconnect) load the last 100 messages over REST.
 * 2. New messages arrive over the WebSocket (/topic/global) and are appended.
 * 3. Sending publishes to /app/global.send; the server saves it and broadcasts it to everyone.
 * 4. While this page is open and visible, the user is LIVE.
 */
export default function GlobalChatPage() {
  const { user } = useAuth();
  const { connected, publish } = useSocket();
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useLiveStatus();

  useEffect(() => {
    if (!connected) return;
    api('/global/messages?limit=100')
      .then((list) => {
        setMessages(list);
        setError('');
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [connected]);

  useSocketEvent('global', (m) => {
    setMessages((list) => (list.some((x) => x.id === m.id) ? list : [...list, m]));
  });

  const send = (content) => publish('/app/global.send', { content });

  return (
    <div className="chat-layout">
      <section className="chat">
        <header className="chat-header">
          <div>
            <h2>🌍 Global chat</h2>
            <small className="muted">Everyone can read and post here</small>
          </div>
        </header>
        {error && <div className="alert alert-error">{error}</div>}
        {loading && !connected ? (
          <div className="messages">
            <div className="empty">Connecting…</div>
          </div>
        ) : (
          <MessageList messages={messages} me={user.username} emptyText="No messages yet. Say hello! 👋" />
        )}
        <MessageComposer onSend={send} disabled={!connected} placeholder="Message everyone…" />
      </section>
      <OnlineUsersPanel me={user.username} />
    </div>
  );
}
