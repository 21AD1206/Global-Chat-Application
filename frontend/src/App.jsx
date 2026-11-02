import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext.jsx';
import { SocketProvider } from './context/SocketContext.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Layout from './components/Layout.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import GlobalChatPage from './pages/GlobalChatPage.jsx';
import UsersPage from './pages/UsersPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import RequestsPage from './pages/RequestsPage.jsx';
import ChatsPage from './pages/ChatsPage.jsx';
import SettingsPage from './pages/SettingsPage.jsx';

/**
 * Routes
 *   /login, /register        public (logged-in users are sent to /)
 *   everything else          protected: requires login, wrapped in the WebSocket provider + app layout
 */
export default function App() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/register" element={user ? <Navigate to="/" replace /> : <RegisterPage />} />

      <Route
        element={
          <ProtectedRoute>
            <SocketProvider>
              <Layout />
            </SocketProvider>
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<GlobalChatPage />} />
        <Route path="/users" element={<UsersPage />} />
        <Route path="/users/:username" element={<ProfilePage />} />
        <Route path="/requests" element={<RequestsPage />} />
        <Route path="/chats" element={<ChatsPage />} />
        <Route path="/chats/:username" element={<ChatsPage />} />
        <Route path="/settings" element={<SettingsPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
