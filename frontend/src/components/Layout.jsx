import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useSocket } from '../context/SocketContext.jsx';
import Avatar from './Avatar.jsx';

// App shell: sidebar on desktop, bottom navigation bar on phones.
export default function Layout() {
  const { user, logout } = useAuth();
  const { connected, pendingCount, totalUnread, getStatus } = useSocket();
  const navigate = useNavigate();

  const links = [
    { to: '/', label: 'Global chat', icon: '🌍', end: true },
    { to: '/users', label: 'People', icon: '👥' },
    { to: '/requests', label: 'Requests', icon: '📨', badge: pendingCount },
    { to: '/chats', label: 'Private chats', icon: '💬', badge: totalUnread },
    { to: '/settings', label: 'Settings', icon: '⚙️' },
  ];

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          💬 <span>ChatSpace</span>
        </div>
        <nav className="nav">
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end} className="nav-link">
              <span className="nav-icon">{l.icon}</span>
              <span className="nav-label">{l.label}</span>
              {l.badge > 0 && <span className="nav-badge">{l.badge}</span>}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="me">
            <Avatar name={user.name} username={user.username} status={getStatus(user.username)} size={36} />
            <div className="me-text">
              <strong>{user.name}</strong>
              <small>@{user.username}</small>
            </div>
          </div>
          <div className={`conn ${connected ? 'conn-ok' : 'conn-bad'}`}>
            {connected ? '● Connected' : '○ Reconnecting…'}
          </div>
          <button className="btn btn-ghost btn-block" onClick={handleLogout}>
            Log out
          </button>
        </div>
      </aside>

      <header className="topbar">
        <div className="brand">💬 ChatSpace</div>
        <div className={`conn ${connected ? 'conn-ok' : 'conn-bad'}`}>{connected ? '● Live link' : '○ Offline'}</div>
      </header>

      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
