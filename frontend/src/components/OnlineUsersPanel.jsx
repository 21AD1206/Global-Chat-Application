import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import { useSocket } from '../context/SocketContext.jsx';
import Avatar from './Avatar.jsx';
import StatusBadge from './StatusBadge.jsx';

/**
 * "Online and LIVE users" list shown beside the global chat.
 * Names come from /api/users; online/LIVE state comes from the real-time presence map,
 * so the list updates instantly when someone connects, disconnects or goes LIVE.
 */
export default function OnlineUsersPanel({ me }) {
  const { presence, getStatus } = useSocket();
  const [users, setUsers] = useState([]);

  // Reload the user list when somebody we don't know yet comes online (e.g. a brand-new account).
  const knownMissing = Object.keys(presence).some((u) => !users.some((x) => x.username === u));
  useEffect(() => {
    api('/users')
      .then(setUsers)
      .catch(() => {});
  }, [knownMissing]);

  const online = useMemo(
    () =>
      users
        .map((u) => ({ ...u, status: getStatus(u.username) }))
        .filter((u) => u.status.online)
        .sort((a, b) => Number(b.status.live) - Number(a.status.live) || a.username.localeCompare(b.username)),
    [users, getStatus]
  );
  const liveCount = online.filter((u) => u.status.live).length;

  return (
    <aside className="side-panel">
      <h3>
        Online <span className="muted">({online.length})</span> · <span className="live-text">LIVE {liveCount}</span>
      </h3>
      {online.length === 0 && <p className="muted small">Nobody is online.</p>}
      <ul className="user-mini-list">
        {online.map((u) => (
          <li key={u.username}>
            <Link to={`/users/${u.username}`} className="user-mini">
              <Avatar name={u.name} username={u.username} status={u.status} size={32} />
              <span className="user-mini-name">
                {u.name}
                {u.username === me && <small className="muted"> (you)</small>}
              </span>
              <StatusBadge status={u.status} />
            </Link>
          </li>
        ))}
      </ul>
      <p className="hint">
        <strong>Online</strong> = app open. <strong className="live-text">LIVE</strong> = actively on a chat screen
        right now.
      </p>
    </aside>
  );
}
