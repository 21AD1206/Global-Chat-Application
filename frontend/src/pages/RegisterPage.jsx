import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

// The same rules are enforced again on the server; checking here just gives faster feedback.
function validate(f) {
  const e = {};
  if (f.name.trim().length < 2 || f.name.trim().length > 50) e.name = 'Name must be 2-50 characters';
  if (!/^[a-zA-Z0-9_]{3,20}$/.test(f.username.trim()))
    e.username = 'Username: 3-20 letters, digits or underscore';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email.trim())) e.email = 'Enter a valid email address';
  if (f.password.length < 6 || f.password.length > 64) e.password = 'Password must be 6-64 characters';
  else if (!/[A-Za-z]/.test(f.password) || !/[0-9]/.test(f.password))
    e.password = 'Password must contain at least one letter and one digit';
  if (f.confirm !== f.password) e.confirm = 'Passwords do not match';
  return e;
}

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', username: '', email: '', password: '', confirm: '' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    const v = validate(form);
    setErrors(v);
    setError('');
    if (Object.keys(v).length) return;

    setBusy(true);
    try {
      await register({
        name: form.name.trim(),
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
      });
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message === 'Validation failed' ? 'Please fix the highlighted fields' : err.message);
      setErrors(err.fieldErrors || {});
    } finally {
      setBusy(false);
    }
  };

  const field = (name, label, type = 'text', autoComplete) => (
    <label>
      {label}
      <input type={type} name={name} value={form[name]} onChange={update} autoComplete={autoComplete} />
      {errors[name] && <span className="field-error">{errors[name]}</span>}
    </label>
  );

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={submit} noValidate>
        <div className="auth-logo">💬</div>
        <h1>Create account</h1>
        <p className="muted">Join the conversation</p>

        {error && <div className="alert alert-error">{error}</div>}

        {field('name', 'Full name', 'text', 'name')}
        {field('username', 'Username', 'text', 'username')}
        {field('email', 'Email', 'email', 'email')}
        {field('password', 'Password', 'password', 'new-password')}
        {field('confirm', 'Confirm password', 'password', 'new-password')}

        <button className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Creating account…' : 'Register'}
        </button>
        <p className="muted center">
          Already registered? <Link to="/login">Log in</Link>
        </p>
      </form>
    </div>
  );
}
