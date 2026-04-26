import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import Spinner from '../components/Spinner';
import Toast, { ToastMessage } from '../components/Toast';
import { normalizeUser } from '../types';
import type { ApiResponse, AuthResponse } from '../types';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<ToastMessage | null>(null);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const res = await api.post<ApiResponse<AuthResponse>>('/v1/auth/login', {
        email,
        password,
      });

      const { accessToken, user } = res.data.data;

      login(accessToken, normalizeUser(user));

      navigate('/', { replace: true });
    } catch (err: any) {
      setToast({
        id: Date.now(),
        kind: 'error',
        text: err?.response?.data?.message || err.message || 'Login failed',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
      <div className="min-h-screen bg-gray-50 grid place-items-center px-4">
        <Toast toast={toast} onDismiss={() => setToast(null)} />

        <div className="w-full max-w-md bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
          <div className="text-center mb-6">
            <div className="inline-flex w-12 h-12 rounded-xl bg-emerald-600 text-white items-center justify-center font-bold text-xl mb-2">
              W
            </div>
            <h1 className="text-2xl font-bold text-gray-900">Welcome back</h1>
            <p className="text-sm text-gray-500 mt-1">Sign in to your wallet</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500"
                  placeholder="you@example.com"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Password
              </label>
              <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500"
                  placeholder="••••••••"
              />
            </div>

            <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 rounded-lg bg-emerald-600 text-white font-medium hover:bg-emerald-700 transition disabled:opacity-60 flex items-center justify-center gap-2"
            >
              {loading ? <Spinner /> : 'Sign in'}
            </button>
          </form>

          <p className="text-sm text-gray-600 text-center mt-6">
            Don't have an account?{' '}
            <Link
                to="/register"
                className="text-emerald-700 font-medium hover:underline"
            >
              Register
            </Link>
          </p>
        </div>
      </div>
  );
};

export default LoginPage;