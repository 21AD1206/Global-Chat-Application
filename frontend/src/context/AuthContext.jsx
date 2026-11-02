import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api, getToken, setToken, setUnauthorizedHandler } from '../api/client.js';

// Holds the logged-in user and exposes login / register / logout to every page.
// The JWT is kept in localStorage so a page refresh keeps you logged in.
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  const clearSession = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  // On first load: if a token is stored, ask the server who we are (also checks the token is still valid).
  useEffect(() => {
    setUnauthorizedHandler(clearSession);
    if (!getToken()) {
      setReady(true);
      return;
    }
    api('/users/me')
      .then(setUser)
      .catch(() => clearSession())
      .finally(() => setReady(true));
  }, [clearSession]);

  const login = useCallback(async (usernameOrEmail, password) => {
    const res = await api('/auth/login', { method: 'POST', body: { usernameOrEmail, password } });
    setToken(res.token);
    setUser(res.user);
  }, []);

  const register = useCallback(async (form) => {
    const res = await api('/auth/register', { method: 'POST', body: form });
    setToken(res.token);
    setUser(res.user);
  }, []);

  const logout = useCallback(async () => {
    try {
      await api('/auth/logout', { method: 'POST' });
    } catch {
      /* even if the server is unreachable, log out locally */
    }
    clearSession();
  }, [clearSession]);

  if (!ready) {
    return <div className="splash">Loading…</div>;
  }

  return (
    <AuthContext.Provider value={{ user, setUser, login, register, logout }}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
