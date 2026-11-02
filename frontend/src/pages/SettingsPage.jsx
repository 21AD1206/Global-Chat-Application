import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';

/** Edit display name / bio, change password, log out. */
export default function SettingsPage() {
  const { user, setUser, logout } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [profile, setProfile] = useState({ name: user.name, bio: user.bio || '' });
  const [profileErrors, setProfileErrors] = useState({});
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '', confirm: '' });
  const [pwErrors, setPwErrors] = useState({});

  const saveProfile = async (e) => {
    e.preventDefault();
    setProfileErrors({});
    try {
      const updated = await api('/users/me', { method: 'PUT', body: profile });
      setUser(updated);
      toast('Profile updated', 'success');
    } catch (err) {
      setProfileErrors(err.fieldErrors);
      toast(err.message, 'error');
    }
  };

  const changePassword = async (e) => {
    e.preventDefault();
    if (pw.newPassword !== pw.confirm) {
      setPwErrors({ confirm: 'Passwords do not match' });
      return;
    }
    setPwErrors({});
    try {
      await api('/users/me/password', {
        method: 'PUT',
        body: { currentPassword: pw.currentPassword, newPassword: pw.newPassword },
      });
      setPw({ currentPassword: '', newPassword: '', confirm: '' });
      toast('Password changed', 'success');
    } catch (err) {
      setPwErrors(err.fieldErrors);
      toast(err.message === 'Validation failed' ? 'Please fix the highlighted fields' : err.message, 'error');
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="page narrow">
      <header className="page-header">
        <h2>⚙️ Settings</h2>
      </header>

      <form className="card form-card" onSubmit={saveProfile}>
        <h3>Profile</h3>
        <label>
          Username
          <input value={`@${user.username}`} disabled />
        </label>
        <label>
          Email
          <input value={user.email || ''} disabled />
        </label>
        <label>
          Display name
          <input value={profile.name} maxLength={50} onChange={(e) => setProfile({ ...profile, name: e.target.value })} />
          {profileErrors.name && <span className="field-error">{profileErrors.name}</span>}
        </label>
        <label>
          Bio <small className="muted">(shown on your public profile)</small>
          <textarea
            rows={3}
            maxLength={200}
            value={profile.bio}
            onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
          />
          {profileErrors.bio && <span className="field-error">{profileErrors.bio}</span>}
        </label>
        <button className="btn btn-primary">Save profile</button>
      </form>

      <form className="card form-card" onSubmit={changePassword}>
        <h3>Change password</h3>
        <label>
          Current password
          <input
            type="password"
            autoComplete="current-password"
            value={pw.currentPassword}
            onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })}
          />
          {pwErrors.currentPassword && <span className="field-error">{pwErrors.currentPassword}</span>}
        </label>
        <label>
          New password
          <input
            type="password"
            autoComplete="new-password"
            value={pw.newPassword}
            onChange={(e) => setPw({ ...pw, newPassword: e.target.value })}
          />
          {pwErrors.newPassword && <span className="field-error">{pwErrors.newPassword}</span>}
        </label>
        <label>
          Confirm new password
          <input
            type="password"
            autoComplete="new-password"
            value={pw.confirm}
            onChange={(e) => setPw({ ...pw, confirm: e.target.value })}
          />
          {pwErrors.confirm && <span className="field-error">{pwErrors.confirm}</span>}
        </label>
        <button className="btn btn-primary">Change password</button>
      </form>

      <div className="card form-card">
        <h3>Session</h3>
        <p className="muted">Logging out closes your live connection and invalidates your login token.</p>
        <button className="btn btn-danger" onClick={handleLogout}>
          Log out
        </button>
      </div>
    </div>
  );
}
