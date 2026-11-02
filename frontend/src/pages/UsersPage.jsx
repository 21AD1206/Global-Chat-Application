import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useSocket } from '../context/SocketContext.jsx';
import Avatar from '../components/Avatar.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { timeAgo } from '../utils/format.js';

const FILTERS = [
  { key: 'all', label: 'All' },
  { key: 'online', label: 'Online' },
  { key: 'live', label: 'LIVE' },
  { key: 'offline', label: 'Offline' },
];

/**
 * User discovery: list of all registered users, search by username / display name
 * and filter by status. Search is done on the server (/api/users?search=...);
 * the status filter is applied here using the real-time presence map so it updates live.
 */
export default function UsersPage() {
  const { user } = useAuth();
  const { getStatus } = useSocket();
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('all');
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Debounced server-side search.
  useEffect(() => {
    const t = setTimeout(() => {
      setLoading(true);
      api(`/users?search=${encodeURIComponent(search.trim())}`)
        .then((list) => {
          setUsers(list);
          setError('');
        })
        .catch((e) => setError(e.message))
        .finally(() => setLoading(false));
    }, 250);
    return () => clearTimeout(t);
  }, [search]);

  const shown = useMemo(() => {
    return users
      .map((u) => ({ ...u, status: getStatus(u.username) }))
      .filter((u) => {
        if (filter === 'online') return u.status.online;
        if (filter === 'live') return u.status.live;
        if (filter === 'offline') return !u.status.online;
        return true;
      })
      .sort(
        (a, b) =>
          Number(b.status.live) - Number(a.status.live) ||
          Number(b.status.online) - Number(a.status.online) ||
          a.username.localeCompare(b.username)
      );
  }, [users, filter, getStatus]);

  return (
    <div className="page">
      <header className="page-header">
        <h2>👥 People</h2>
        <p className="muted">Find users, see who is online or LIVE, and open their profile.</p>
      </header>

      <div className="toolbar">
        <input
          className="search"
          type="search"
          placeholder="Search by username or name…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <div className="tabs">
          {FILTERS.map((f) => (
            <button
              key={f.key}
              className={`tab ${filter === f.key ? 'active' : ''}`}
              onClick={() => setFilter(f.key)}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && users.length === 0 && <p className="muted">Loading…</p>}
      {!loading && shown.length === 0 && <p className="empty">No users match.</p>}

      <ul className="user-grid">
        {shown.map((u) => (
          <li key={u.username}>
            <Link to={`/users/${u.username}`} className="card user-card">
              <Avatar name={u.name} username={u.username} status={u.status} size={48} />
              <div className="user-card-body">
                <strong>
                  {u.name} {u.username === user.username && <small className="muted">(you)</small>}
                </strong>
                <small className="muted">@{u.username}</small>
                <small className="muted">
                  {u.status.online ? 'Active now' : `Last seen ${timeAgo(u.lastSeen)}`}
                </small>
              </div>
              <StatusBadge status={u.status} />
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
