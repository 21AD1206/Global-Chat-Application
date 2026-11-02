import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/client.js';
import { useSocket, useSocketEvent } from '../context/SocketContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import Avatar from '../components/Avatar.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { formatDate, timeAgo } from '../utils/format.js';

/**
 * Public profile page. The action button depends on the relation the server reports:
 *   SELF             -> Edit profile
 *   NONE             -> Send chat request
 *   OUTGOING_PENDING -> Cancel request
 *   INCOMING_PENDING -> Accept / Reject
 *   ACCEPTED         -> Open private chat
 */
export default function ProfilePage() {
  const { username } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const { getStatus } = useSocket();
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    api(`/users/${encodeURIComponent(username)}`)
      .then((p) => {
        setProfile(p);
        setError('');
      })
      .catch((e) => setError(e.status === 404 ? 'User not found' : e.message));
  }, [username]);

  useEffect(load, [load]);

  // A request involving this user changed (e.g. they accepted while I'm looking): refresh.
  useSocketEvent('requests', (r) => {
    if (r.sender.username === username || r.recipient.username === username) load();
  });

  const act = async (path, body, successMsg) => {
    setBusy(true);
    try {
      await api(path, { method: 'POST', body });
      if (successMsg) toast(successMsg, 'success');
      load();
    } catch (e) {
      toast(e.message, 'error');
      load();
    } finally {
      setBusy(false);
    }
  };

  if (error) {
    return (
      <div className="page">
        <div className="alert alert-error">{error}</div>
        <Link to="/users">← Back to people</Link>
      </div>
    );
  }
  if (!profile) return <div className="page muted">Loading…</div>;

  const status = getStatus(profile.username);

  return (
    <div className="page">
      <Link to="/users" className="back-link">
        ← People
      </Link>
      <div className="card profile-card">
        <Avatar name={profile.name} username={profile.username} status={status} size={96} />
        <h2>{profile.name}</h2>
        <p className="muted">@{profile.username}</p>
        <StatusBadge status={status} />
        <p className="profile-bio">{profile.bio || <span className="muted">No bio yet.</span>}</p>
        <dl className="profile-meta">
          <div>
            <dt>Joined</dt>
            <dd>{formatDate(profile.createdAt)}</dd>
          </div>
          <div>
            <dt>Last seen</dt>
            <dd>{status.online ? 'Active now' : timeAgo(profile.lastSeen)}</dd>
          </div>
        </dl>

        <div className="profile-actions">
          {profile.relation === 'SELF' && (
            <button className="btn" onClick={() => navigate('/settings')}>
              Edit my profile
            </button>
          )}
          {profile.relation === 'NONE' && (
            <button
              className="btn btn-primary"
              disabled={busy}
              onClick={() => act('/requests', { toUsername: profile.username }, 'Chat request sent')}
            >
              📨 Send chat request
            </button>
          )}
          {profile.relation === 'OUTGOING_PENDING' && (
            <>
              <span className="pill pill-pending">Request pending…</span>
              <button className="btn btn-ghost" disabled={busy} onClick={() => act(`/requests/${profile.requestId}/cancel`)}>
                Cancel request
              </button>
            </>
          )}
          {profile.relation === 'INCOMING_PENDING' && (
            <>
              <span className="pill pill-pending">{profile.name} wants to chat with you</span>
              <button
                className="btn btn-primary"
                disabled={busy}
                onClick={() => act(`/requests/${profile.requestId}/accept`, undefined, 'Request accepted')}
              >
                Accept
              </button>
              <button className="btn btn-danger" disabled={busy} onClick={() => act(`/requests/${profile.requestId}/reject`)}>
                Reject
              </button>
            </>
          )}
          {profile.relation === 'ACCEPTED' && (
            <button className="btn btn-primary" onClick={() => navigate(`/chats/${profile.username}`)}>
              💬 Open private chat
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
