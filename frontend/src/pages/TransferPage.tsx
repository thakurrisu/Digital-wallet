import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { v4 as uuidv4 } from 'uuid';
import api from '../api/axios';
import Spinner from '../components/Spinner';
import Toast, { ToastMessage } from '../components/Toast';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const TransferPage = () => {
  const [receiverEmail, setReceiverEmail] = useState('');
  const [amount, setAmount] = useState('');
  const [confirming, setConfirming] = useState(false);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<ToastMessage | null>(null);
  const navigate = useNavigate();

  const handleReview = (e: FormEvent) => {
    e.preventDefault();
    const email = receiverEmail.trim().toLowerCase();
    if (!email || !EMAIL_REGEX.test(email)) {
      setToast({ id: Date.now(), kind: 'error', text: 'Enter a valid receiver email' });
      return;
    }
    const amt = parseFloat(amount);
    if (!amt || amt <= 0) {
      setToast({ id: Date.now(), kind: 'error', text: 'Enter a valid amount' });
      return;
    }
    setConfirming(true);
  };

  const handleConfirm = async () => {
    setLoading(true);
    try {
      await api.post('/v1/transactions/transfer', {
        amount: parseFloat(amount),
        referenceId: uuidv4(),
        receiverEmail: receiverEmail.trim().toLowerCase(),
      });
      setToast({ id: Date.now(), kind: 'success', text: 'Transfer successful!' });
      setTimeout(() => navigate('/'), 800);
    } catch (err) {
      setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
      setConfirming(false);
    } finally {
      setLoading(false);
    }
  };

  const formattedAmount = () =>
    new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(parseFloat(amount) || 0);

  return (
    <div className="max-w-lg mx-auto px-4 sm:px-6 py-8">
      <Toast toast={toast} onDismiss={() => setToast(null)} />

      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Send money</h1>
        <p className="text-sm text-gray-500">Transfer to another user by email</p>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl p-6">
        {!confirming ? (
          <form onSubmit={handleReview} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Receiver email
              </label>
              <input
                type="email"
                required
                autoComplete="email"
                value={receiverEmail}
                onChange={(e) => setReceiverEmail(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                placeholder="user@example.com"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Amount (INR)</label>
              <input
                type="number"
                required
                min="1"
                step="0.01"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                placeholder="0.00"
              />
            </div>
            <button
              type="submit"
              className="w-full py-2.5 rounded-lg bg-emerald-600 text-white font-medium hover:bg-emerald-700 transition"
            >
              Review transfer
            </button>
          </form>
        ) : (
          <div className="space-y-5">
            <div className="rounded-lg border border-gray-200 p-4 bg-gray-50">
              <p className="text-sm text-gray-500">You are sending</p>
              <p className="text-3xl font-bold text-gray-900 mt-1">{formattedAmount()}</p>
              <div className="mt-3 text-sm text-gray-600">
                to{' '}
                <span className="font-mono font-semibold text-gray-900">{receiverEmail}</span>
              </div>
            </div>

            <p className="text-xs text-gray-500">
              This action cannot be undone. Please confirm the details before proceeding.
            </p>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setConfirming(false)}
                disabled={loading}
                className="flex-1 py-2.5 rounded-lg bg-gray-100 hover:bg-gray-200 text-gray-900 font-medium transition disabled:opacity-60"
              >
                Back
              </button>
              <button
                onClick={handleConfirm}
                disabled={loading}
                className="flex-1 py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-medium transition disabled:opacity-60 flex items-center justify-center gap-2"
              >
                {loading ? <Spinner /> : 'Confirm & send'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default TransferPage;
