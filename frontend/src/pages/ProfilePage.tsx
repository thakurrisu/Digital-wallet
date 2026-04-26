import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Spinner from '../components/Spinner';
import Toast, { ToastMessage } from '../components/Toast';
import { useAuth } from '../context/AuthContext';
import { normalizeUser } from '../types';
import type { ApiResponse, RawUser, User } from '../types';

const formatDate = (iso?: string) => {
  if (!iso) return '—';
  const d = new Date(iso);
  return Number.isNaN(d.getTime())
    ? iso
    : d.toLocaleDateString('en-IN', { day: '2-digit', month: 'long', year: 'numeric' });
};

const ProfilePage = () => {
  const { user, updateUser, logout } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState<User | null>(user);
  const [loading, setLoading] = useState(true);

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [savingProfile, setSavingProfile] = useState(false);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [savingPassword, setSavingPassword] = useState(false);

  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const [toast, setToast] = useState<ToastMessage | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const res = await api.get<ApiResponse<RawUser>>('/v1/users/me');
        const normalized = normalizeUser(res.data.data);
        setProfile(normalized);
        setName(normalized.name);
        setEmail(normalized.email);
        updateUser(normalized);
      } catch (err) {
        setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
      } finally {
        setLoading(false);
      }
    };
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleProfileSave = async (e: FormEvent) => {
    e.preventDefault();
    setSavingProfile(true);
    try {
      const res = await api.put<ApiResponse<RawUser>>('/v1/users/me', { name, email });
      const normalized = normalizeUser(res.data.data);
      setProfile(normalized);
      updateUser(normalized);
      setToast({ id: Date.now(), kind: 'success', text: 'Profile updated' });
    } catch (err) {
      setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
    } finally {
      setSavingProfile(false);
    }
  };

  const handlePasswordSave = async (e: FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setToast({ id: Date.now(), kind: 'error', text: 'New passwords do not match' });
      return;
    }
    setSavingPassword(true);
    try {
      await api.put('/v1/users/me/changePassword', {
        currentPassword,
        newPassword,
        confirmPassword,
      });
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setToast({ id: Date.now(), kind: 'success', text: 'Password changed' });
    } catch (err) {
      setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
    } finally {
      setSavingPassword(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await api.delete('/v1/users/me');
      setToast({ id: Date.now(), kind: 'success', text: 'Account deleted' });
      logout();
      setTimeout(() => navigate('/login', { replace: true }), 500);
    } catch (err) {
      setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
      setDeleting(false);
      setDeleteOpen(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-8">
      <Toast toast={toast} onDismiss={() => setToast(null)} />

      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Profile</h1>
        <p className="text-sm text-gray-500">Manage your account details</p>
      </div>

      {loading ? (
        <div className="grid place-items-center py-20">
          <div className="w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : (
        <div className="space-y-6">
          <div className="bg-white border border-gray-200 rounded-xl p-6">
            <div className="flex items-center gap-4 mb-6">
              <div className="w-14 h-14 rounded-full bg-emerald-600 text-white grid place-items-center font-bold text-xl">
                {profile?.name?.[0]?.toUpperCase() ?? '?'}
              </div>
              <div>
                <p className="font-semibold text-gray-900">{profile?.name}</p>
                <p className="text-sm text-gray-500">{profile?.email}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm mb-4">
              <div>
                <p className="text-gray-500">Status</p>
                <p className="text-gray-900 font-medium">{profile?.status ?? 'ACTIVE'}</p>
              </div>
              <div>
                <p className="text-gray-500">Member since</p>
                <p className="text-gray-900 font-medium">{formatDate(profile?.createdAt)}</p>
              </div>
            </div>

            <form onSubmit={handleProfileSave} className="space-y-3 border-t border-gray-100 pt-4">
              <h2 className="font-medium text-gray-900">Edit profile</h2>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>
              <button
                type="submit"
                disabled={savingProfile}
                className="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium flex items-center gap-2 disabled:opacity-60"
              >
                {savingProfile ? <Spinner /> : 'Save changes'}
              </button>
            </form>
          </div>

          <div className="bg-white border border-gray-200 rounded-xl p-6">
            <h2 className="font-medium text-gray-900 mb-3">Change password</h2>
            <form onSubmit={handlePasswordSave} className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Current password
                </label>
                <input
                  type="password"
                  required
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  New password
                </label>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Confirm new password
                </label>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>
              <button
                type="submit"
                disabled={savingPassword}
                className="px-4 py-2 rounded-lg bg-gray-900 hover:bg-gray-700 text-white text-sm font-medium flex items-center gap-2 disabled:opacity-60"
              >
                {savingPassword ? <Spinner /> : 'Update password'}
              </button>
            </form>
          </div>

          <div className="bg-white border border-red-200 rounded-xl p-6">
            <h2 className="font-medium text-red-700 mb-1">Danger zone</h2>
            <p className="text-sm text-gray-500 mb-3">
              Deleting your account is permanent and cannot be undone.
            </p>
            <button
              onClick={() => setDeleteOpen(true)}
              className="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-medium"
            >
              Delete account
            </button>
          </div>
        </div>
      )}

      {deleteOpen && (
        <div
          className="fixed inset-0 bg-black/40 grid place-items-center px-4 z-50"
          onClick={() => !deleting && setDeleteOpen(false)}
        >
          <div
            className="bg-white rounded-xl p-6 w-full max-w-sm shadow-xl"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Delete account?</h3>
            <p className="text-sm text-gray-600 mb-5">
              This will permanently delete your account and wallet. You will not be able to recover
              this.
            </p>
            <div className="flex items-center justify-end gap-2">
              <button
                onClick={() => setDeleteOpen(false)}
                disabled={deleting}
                className="px-3 py-2 text-sm rounded-lg bg-gray-100 hover:bg-gray-200"
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                disabled={deleting}
                className="px-4 py-2 text-sm rounded-lg bg-red-600 hover:bg-red-700 text-white flex items-center gap-2 disabled:opacity-60"
              >
                {deleting ? <Spinner /> : 'Yes, delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfilePage;
