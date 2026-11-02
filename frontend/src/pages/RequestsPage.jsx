import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useSocket, useSocketEvent } from '../context/SocketContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import Avatar from '../components/Avatar.jsx';
import { formatTime } from '../utils/format.js';

const STATUS_CLASS = {
  PENDING: 'pill-pending',
  ACCEPTED: 'pill-accepted',
  REJECTED: 'pill-rejected',
  CANCELLED: 'pill-cancelled',
};

/** Incoming and outgoing chat requests. Updates in real time via /user/queue/requests. */
export default function RequestsPage() {
  const { user } = useAuth();
  const { getStatus, refreshPending } = useSocket();
  const toast = useToast();
  const navigate = useNavigate();
  const [tab, setTab] = useState('incoming');
  const [incoming, setIncoming] = useState([]);
  const [outgoing, setOutgoing] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([api('/requests/incoming'), api('/requests/outgoing')])
      .then(([inc, out]) => {
        setIncoming(inc);
        setOutgoing(out);
      })
      .catch((e) => setError(e.message));
  }, []);

  // Insert or replace a request in the right list.
  const upsert = (r) => {
    const apply = (list) => [r, ...list.filter((x) => x.id !== r.id)].sort((a, b) => b.createdAt.localeCompare(a.createdAt));
    if (r.recipient.username === user.username) setIncoming(apply);
    if (r.sender.username === user.username) setOutgoing(apply);
  };

  useSocketEvent('requests', upsert);

  const act = async (id, action) => {
    try {
      const r = await api(`/requests/${id}/${action}`, { method: 'POST' });
      upsert(r);
      refreshPending();
      if (action === 'accept') toast('Request accepted. You can now chat privately.', 'success');
    } catch (e) {
      toast(e.message, 'error');
    }
  };

  const list = tab === 'incoming' ? incoming : outgoing;
  const pendingIn = incoming.filter((r) => r.status === 'PENDING').length;
  const pendingOut = outgoing.filter((r) => r.status === 'PENDING').length;

  return (
    <div className="page">
      <header className="page-header">
        <h2>📨 Chat requests</h2>
        <p className="muted">
          Private chat starts only after a request is accepted. Send requests from a user's profile on the{' '}
          <Link to="/users">People</Link> page.
        </p>
      </header>

      <div className="tabs">
        <button className={`tab ${tab === 'incoming' ? 'active' : ''}`} onClick={() => setTab('incoming')}>
          Incoming {pendingIn > 0 && <span className="nav-badge">{pendingIn}</span>}
        </button>
        <button className={`tab ${tab === 'outgoing' ? 'active' : ''}`} onClick={() => setTab('outgoing')}>
          Outgoing {pendingOut > 0 && <span className="tab-count">{pendingOut}</span>}
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {list.length === 0 && <p className="empty">No {tab} requests.</p>}

      <ul className="request-list">
        {list.map((r) => {
          const other = tab === 'incoming' ? r.sender : r.recipient;
          return (
            <li key={r.id} className="card request-item">
              <Link to={`/users/${other.username}`} className="request-user">
                <Avatar name={other.name} username={other.username} status={getStatus(other.username)} size={44} />
                <div>
                  <strong>{other.name}</strong>
                  <small className="muted">
                    @{other.username} · {tab === 'incoming' ? 'received' : 'sent'} {formatTime(r.createdAt)}
                  </small>
                </div>
              </Link>
              <div className="request-actions">
                <span className={`pill pill-status ${STATUS_CLASS[r.status]}`}>{r.status.toLowerCase()}</span>
                {r.status === 'PENDING' && tab === 'incoming' && (
                  <>
                    <button className="btn btn-primary btn-sm" onClick={() => act(r.id, 'accept')}>
                      Accept
                    </button>
                    <button className="btn btn-danger btn-sm" onClick={() => act(r.id, 'reject')}>
                      Reject
                    </button>
                  </>
                )}
                {r.status === 'PENDING' && tab === 'outgoing' && (
                  <button className="btn btn-ghost btn-sm" onClick={() => act(r.id, 'cancel')}>
                    Cancel
                  </button>
                )}
                {r.status === 'ACCEPTED' && (
                  <button className="btn btn-sm" onClick={() => navigate(`/chats/${other.username}`)}>
                    Chat
                  </button>
                )}
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
