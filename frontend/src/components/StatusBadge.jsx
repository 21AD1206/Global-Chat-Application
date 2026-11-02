// Text label for a user's status: a red "LIVE" pill, "Online" or "Offline".
export default function StatusBadge({ status }) {
  if (status?.live) return <span className="badge badge-live">● LIVE</span>;
  if (status?.online) return <span className="badge badge-online">Online</span>;
  return <span className="badge badge-offline">Offline</span>;
}
