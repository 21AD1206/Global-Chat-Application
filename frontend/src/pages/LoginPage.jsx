import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ usernameOrEmail: '', password: '' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    const v = {};
    if (!form.usernameOrEmail.trim()) v.usernameOrEmail = 'Username or email is required';
    if (!form.password) v.password = 'Password is required';
    setErrors(v);
    setError('');
    if (Object.keys(v).length) return;

    setBusy(true);
    try {
      await login(form.usernameOrEmail.trim(), form.password);
      navigate(location.state?.from || '/', { replace: true });
    } catch (err) {
      setError(err.message);
      setErrors(err.fieldErrors || {});
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={submit} noValidate>
        <div className="auth-logo">💬</div>
        <h1>Welcome back</h1>
        <p className="muted">Log in to ChatSpace</p>

        {error && <div className="alert alert-error">{error}</div>}

        <label>
          Username or email
          <input name="usernameOrEmail" value={form.usernameOrEmail} onChange={update} autoComplete="username" autoFocus />
          {errors.usernameOrEmail && <span className="field-error">{errors.usernameOrEmail}</span>}
        </label>
        <label>
          Password
          <input type="password" name="password" value={form.password} onChange={update} autoComplete="current-password" />
          {errors.password && <span className="field-error">{errors.password}</span>}
        </label>

        <button className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Logging in…' : 'Log in'}
        </button>
        <p className="muted center">
          No account? <Link to="/register">Create one</Link>
        </p>
      </form>
    </div>
  );
}
