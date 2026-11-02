import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useLiveStatus, useSocket, useSocketEvent } from '../context/SocketContext.jsx';
import Avatar from '../components/Avatar.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import MessageList from '../components/MessageList.jsx';
import MessageComposer from '../components/MessageComposer.jsx';
import { formatTime } from '../utils/format.js';

/**
 * Private chats: conversation list on the left, the open conversation on the right.
 * On phones only one of the two is shown at a time.
 */
export default function ChatsPage() {
  const { username } = useParams();
  const { user } = useAuth();
  const { getStatus, unread } = useSocket();
  const [conversations, setConversations] = useState([]);
  const [loaded, setLoaded] = useState(false);

  const load = useCallback(() => {
    api('/private/conversations')
      .then(setConversations)
      .catch(() => {})
      .finally(() => setLoaded(true));
  }, []);

  useEffect(load, [load]);
  useSocketEvent('private', load);
  useSocketEvent('requests', (r) => {
    if (r.status === 'ACCEPTED') load();
  });

  return (
    <div className={`chats-layout ${username ? 'has-active' : ''}`}>
      <aside className="conv-list">
        <h2>💬 Private chats</h2>
        {loaded && conversations.length === 0 && (
          <p className="muted small">
            No private chats yet. Open someone's profile from <Link to="/users">People</Link> and send a chat
            request.
          </p>
        )}
        <ul>
          {conversations.map((c) => {
            const p = c.partner;
            const count = unread[p.username] || 0;
            return (
              <li key={p.username}>
                <Link to={`/chats/${p.username}`} className={`conv ${p.username === username ? 'active' : ''}`}>
                  <Avatar name={p.name} username={p.username} status={getStatus(p.username)} size={40} />
                  <div className="conv-body">
                    <div className="conv-top">
                      <strong>{p.name}</strong>
                      {c.lastMessage && <small className="muted">{formatTime(c.lastMessage.sentAt)}</small>}
                    </div>
                    <small className="muted conv-preview">
                      {c.lastMessage
                        ? `${c.lastMessage.senderUsername === user.username ? 'You: ' : ''}${c.lastMessage.content}`
                        : 'Say hi 👋'}
                    </small>
                  </div>
                  {count > 0 && <span className="nav-badge">{count}</span>}
                </Link>
              </li>
            );
          })}
        </ul>
      </aside>

      <section className="conv-main">
        {username ? (
          <PrivateChat key={username} partnerUsername={username} />
        ) : (
          <div className="empty big">Select a conversation</div>
        )}
      </section>
    </div>
  );
}

/** One private conversation. History over REST, new messages over /user/queue/private. */
function PrivateChat({ partnerUsername }) {
  const { user } = useAuth();
  const { connected, publish, getStatus, setActiveChat } = useSocket();
  const [partner, setPartner] = useState(null);
  const [messages, setMessages] = useState([]);
  const [error, setError] = useState('');
  const [forbidden, setForbidden] = useState(false);

  useLiveStatus();

  useEffect(() => {
    setActiveChat(partnerUsername);
    return () => setActiveChat(null);
  }, [partnerUsername, setActiveChat]);

  useEffect(() => {
    api(`/users/${encodeURIComponent(partnerUsername)}`)
      .then(setPartner)
      .catch((e) => setError(e.message));
  }, [partnerUsername]);

  // (Re)load the history when opened and after a reconnect.
  useEffect(() => {
    if (!connected) return;
    api(`/private/${encodeURIComponent(partnerUsername)}/messages?limit=200`)
      .then((list) => {
        setMessages(list);
        setForbidden(false);
        setError('');
      })
      .catch((e) => {
        if (e.status === 403) setForbidden(true);
        else setError(e.message);
      });
  }, [partnerUsername, connected]);

  useSocketEvent('private', (m) => {
    const belongs =
      (m.senderUsername === partnerUsername && m.recipientUsername === user.username) ||
      (m.senderUsername === user.username && m.recipientUsername === partnerUsername);
    if (belongs) setMessages((list) => (list.some((x) => x.id === m.id) ? list : [...list, m]));
  });

  const send = (content) => publish('/app/private.send', { to: partnerUsername, content });
  const status = getStatus(partnerUsername);

  return (
    <div className="chat">
      <header className="chat-header">
        <Link to="/chats" className="back-btn" aria-label="Back to conversations">
          ←
        </Link>
        {partner && (
          <Link to={`/users/${partner.username}`} className="chat-partner">
            <Avatar name={partner.name} username={partner.username} status={status} size={38} />
            <div>
              <strong>{partner.name}</strong>
              <small className="muted">@{partner.username}</small>
            </div>
          </Link>
        )}
        <StatusBadge status={status} />
      </header>

      {error && <div className="alert alert-error">{error}</div>}

      {forbidden ? (
        <div className="messages">
          <div className="empty">
            🔒 You can chat privately with @{partnerUsername} only after a chat request is accepted.
            <br />
            <Link to={`/users/${partnerUsername}`}>Go to their profile</Link>
          </div>
        </div>
      ) : (
        <>
          <MessageList
            messages={messages}
            me={user.username}
            showSender={false}
            emptyText="No messages yet. Start the conversation!"
          />
          <MessageComposer onSend={send} disabled={!connected} placeholder={`Message @${partnerUsername}…`} />
        </>
      )}
    </div>
  );
}
