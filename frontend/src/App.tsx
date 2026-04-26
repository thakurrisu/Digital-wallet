import { ReactNode } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import TransactionsPage from './pages/TransactionsPage';
import TransferPage from './pages/TransferPage';
import AddMoneyPage from './pages/AddMoneyPage';
import ProfilePage from './pages/ProfilePage';

const AuthedLayout = ({ children }: { children: ReactNode }) => (
  <div className="min-h-screen bg-gray-50">
    <Navbar />
    {children}
  </div>
);

const PublicOnly = ({ children }: { children: ReactNode }) => {
  const { isAuthenticated } = useAuth();
  if (isAuthenticated) return <Navigate to="/" replace />;
  return <>{children}</>;
};

const AppRoutes = () => (
  <Routes>
    <Route
      path="/login"
      element={
        <PublicOnly>
          <LoginPage />
        </PublicOnly>
      }
    />
    <Route
      path="/register"
      element={
        <PublicOnly>
          <RegisterPage />
        </PublicOnly>
      }
    />

    <Route
      path="/"
      element={
        <ProtectedRoute>
          <AuthedLayout>
            <DashboardPage />
          </AuthedLayout>
        </ProtectedRoute>
      }
    />
    <Route
      path="/transactions"
      element={
        <ProtectedRoute>
          <AuthedLayout>
            <TransactionsPage />
          </AuthedLayout>
        </ProtectedRoute>
      }
    />
    <Route
      path="/transfer"
      element={
        <ProtectedRoute>
          <AuthedLayout>
            <TransferPage />
          </AuthedLayout>
        </ProtectedRoute>
      }
    />
    <Route
      path="/add-money"
      element={
        <ProtectedRoute>
          <AuthedLayout>
            <AddMoneyPage />
          </AuthedLayout>
        </ProtectedRoute>
      }
    />
    <Route
      path="/profile"
      element={
        <ProtectedRoute>
          <AuthedLayout>
            <ProfilePage />
          </AuthedLayout>
        </ProtectedRoute>
      }
    />

    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
);

const App = () => (
  <BrowserRouter>
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  </BrowserRouter>
);

export default App;
