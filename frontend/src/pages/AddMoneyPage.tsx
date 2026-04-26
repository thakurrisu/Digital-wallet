import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Spinner from '../components/Spinner';
import Toast, { ToastMessage } from '../components/Toast';
import { useAuth } from '../context/AuthContext';
import type { ApiResponse, Payment, PaymentInitiateResponse } from '../types';

declare global {
  interface Window {
    Razorpay: new (options: RazorpayOptions) => { open: () => void };
  }
}

interface RazorpayOptions {
  key: string;
  amount: number;
  currency: string;
  name: string;
  description?: string;
  order_id: string;
  prefill?: { name?: string; email?: string };
  theme?: { color?: string };
  handler: (response: RazorpayHandlerResponse) => void;
  modal?: { ondismiss?: () => void };
}

interface RazorpayHandlerResponse {
  razorpay_payment_id: string;
  razorpay_order_id: string;
  razorpay_signature: string;
}

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

const AddMoneyPage = () => {
  const [amount, setAmount] = useState('');
  const [loading, setLoading] = useState(false);
  const [polling, setPolling] = useState(false);
  const [toast, setToast] = useState<ToastMessage | null>(null);
  const { user } = useAuth();
  const navigate = useNavigate();

  const pollStatus = async (paymentId: string | number) => {
    setPolling(true);
    const maxAttempts = 20;
    for (let i = 0; i < maxAttempts; i++) {
      try {
        const res = await api.get<ApiResponse<Payment>>(`/v1/payments/${paymentId}/status`);
        const status = res.data.data.status;
        if (status === 'SUCCESS') {
          setToast({
            id: Date.now(),
            kind: 'success',
            text: 'Payment successful! Wallet credited.',
          });
          setPolling(false);
          setTimeout(() => navigate('/'), 1000);
          return;
        }
        if (status === 'FAILED') {
          setToast({ id: Date.now(), kind: 'error', text: 'Payment failed.' });
          setPolling(false);
          return;
        }
      } catch {
        // transient — retry
      }
      await sleep(1500);
    }
    setPolling(false);
    setToast({
      id: Date.now(),
      kind: 'info',
      text: 'Still processing. Check transactions shortly.',
    });
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const amt = parseFloat(amount);
    if (!amt || amt <= 0) {
      setToast({ id: Date.now(), kind: 'error', text: 'Enter a valid amount' });
      return;
    }
    if (!window.Razorpay) {
      setToast({
        id: Date.now(),
        kind: 'error',
        text: 'Razorpay checkout failed to load. Refresh and try again.',
      });
      return;
    }

    setLoading(true);
    try {
      const res = await api.post<ApiResponse<PaymentInitiateResponse>>(
        '/v1/payments/initiate',
        { amount: amt }
      );
      const init = res.data.data;

      const options: RazorpayOptions = {
        key: init.keyId,
        amount: Math.round(init.amount * 100),
        currency: init.currency || 'INR',
        name: 'Digital Wallet',
        description: 'Add money to wallet',
        order_id: init.gatewayOrderId,
        prefill: { name: user?.name, email: user?.email },
        theme: { color: '#059669' },
        handler: () => {
          pollStatus(init.paymentId);
        },
        modal: {
          ondismiss: () => {
            setToast({ id: Date.now(), kind: 'info', text: 'Payment cancelled' });
          },
        },
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (err) {
      setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-lg mx-auto px-4 sm:px-6 py-8">
      <Toast toast={toast} onDismiss={() => setToast(null)} />

      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Add money</h1>
        <p className="text-sm text-gray-500">Top up your wallet via Razorpay</p>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
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
              disabled={loading || polling}
            />
          </div>

          <div className="flex gap-2 flex-wrap">
            {[100, 500, 1000, 2000].map((v) => (
              <button
                type="button"
                key={v}
                onClick={() => setAmount(String(v))}
                className="px-3 py-1.5 text-sm rounded-full border border-gray-300 hover:bg-gray-50 transition"
                disabled={loading || polling}
              >
                ₹{v}
              </button>
            ))}
          </div>

          <button
            type="submit"
            disabled={loading || polling}
            className="w-full py-2.5 rounded-lg bg-emerald-600 text-white font-medium hover:bg-emerald-700 transition disabled:opacity-60 flex items-center justify-center gap-2"
          >
            {loading ? <Spinner /> : polling ? 'Verifying payment...' : 'Pay with Razorpay'}
          </button>
        </form>

        {polling && (
          <p className="text-xs text-gray-500 mt-3 text-center">
            Waiting for payment confirmation...
          </p>
        )}
      </div>
    </div>
  );
};

export default AddMoneyPage;
