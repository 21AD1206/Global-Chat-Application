// Round avatar with the user's initials (colour derived from the username)
// and an optional status dot: green = online, red pulsing = LIVE.

const COLORS = ['#4f46e5', '#0891b2', '#059669', '#d97706', '#db2777', '#7c3aed', '#2563eb', '#dc2626'];

function colorFor(username = '') {
  let hash = 0;
  for (const ch of username) hash = (hash * 31 + ch.charCodeAt(0)) >>> 0;
  return COLORS[hash % COLORS.length];
}

function initials(name = '?') {
  const parts = name.trim().split(/\s+/);
  return ((parts[0]?.[0] || '') + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase() || '?';
}

export default function Avatar({ name, username, status, size = 40 }) {
  const dot = status ? (status.live ? 'live' : status.online ? 'online' : 'offline') : null;
  return (
    <span className="avatar" style={{ width: size, height: size, background: colorFor(username), fontSize: size * 0.4 }}>
      {initials(name)}
      {dot && <span className={`avatar-dot dot-${dot}`} title={dot} />}
    </span>
  );
}
