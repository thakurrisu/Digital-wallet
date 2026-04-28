import { FormEvent, useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { v4 as uuidv4 } from 'uuid';
import api from '../api/axios';
import TransactionItem from '../components/TransactionItem';
import Spinner from '../components/Spinner';
import Toast, { ToastMessage } from '../components/Toast';
import type { ApiResponse, Page, Transaction, Wallet } from '../types';

const formatAmount = (amount: number) =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(amount);

const DashboardPage = () => {
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState<ToastMessage | null>(null);
  const [withdrawOpen, setWithdrawOpen] = useState(false);
  const [withdrawAmount, setWithdrawAmount] = useState('');
  const [withdrawLoading, setWithdrawLoading] = useState(false);
  const [freezingWallet, setFreezingWallet] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [walletRes, txRes] = await Promise.all([
        api.get<ApiResponse<Wallet>>('/v1/wallet'),
        api.get<ApiResponse<Page<Transaction>>>('/v1/transactions', {
          params: { page: 0, size: 5 },
        }),
      ]);
      setWallet(walletRes.data.data);
      setTransactions(txRes.data.data.content);
    } catch (err) {
      setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleToggleFreeze = async () => {
    setFreezingWallet(true);
    try {
      const res = await api.patch<ApiResponse<Wallet>>('/v1/wallet/freeze');
      setWallet(res.data.data);
      setToast({
        id: Date.now(),
        kind: 'success',
        text: `Wallet is now ${res.data.data.status}`,
      });
    } catch (err) {
      setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
    } finally {
      setFreezingWallet(false);
    }
  };

  const handleWithdraw = async (e: FormEvent) => {
    e.preventDefault();
    const amount = parseFloat(withdrawAmount);
    if (!amount || amount <= 0) {
      setToast({ id: Date.now(), kind: 'error', text: 'Enter a valid amount' });
      return;
    }
    setWithdrawLoading(true);
    try {
      await api.post('/v1/transactions/credit', {
        amount,
        referenceId: uuidv4(),
      });
      setToast({ id: Date.now(), kind: 'success', text: 'Withdrawal successful' });
      setWithdrawAmount('');
      setWithdrawOpen(false);
      loadData();
    } catch (err) {
      setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
    } finally {
      setWithdrawLoading(false);
    }
  };

  const isFrozen = wallet?.status === 'FROZEN';

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8">
      <Toast toast={toast} onDismiss={() => setToast(null)} />

      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-sm text-gray-500">Overview of your wallet</p>
      </div>

      {loading ? (
        <div className="grid place-items-center py-20">
          <div className="w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : (
        <>
          <div className="bg-gradient-to-br from-emerald-600 to-emerald-800 text-white rounded-2xl p-6 shadow-sm mb-6">
            <div className="flex items-start justify-between flex-wrap gap-4">
              <div>
                <p className="text-emerald-100 text-sm">Available balance</p>
                <p className="text-4xl font-bold mt-1">
                  {wallet ? formatAmount(wallet.balance) : '—'}
                </p>
                <div className="mt-3 inline-flex items-center gap-2">
                  <span
                    className={`px-2.5 py-1 rounded-full text-xs font-medium ${
                      isFrozen ? 'bg-red-500/30 text-red-50' : 'bg-white/20 text-white'
                    }`}
                  >
                    {wallet?.status ?? '—'}
                  </span>
                  {wallet?.id !== undefined && (
                    <span className="text-xs text-emerald-100">
                      Wallet ID: {String(wallet.id)}
                    </span>
                  )}
                </div>
              </div>
              <button
                onClick={handleToggleFreeze}
                disabled={freezingWallet}
                className="px-3 py-1.5 text-sm rounded-lg bg-white/15 hover:bg-white/25 text-white transition disabled:opacity-60"
              >
                {freezingWallet ? '...' : isFrozen ? 'Unfreeze wallet' : 'Freeze wallet'}
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mb-8">
            <Link
              to="/add-money"
              className="bg-white border border-gray-200 rounded-xl p-4 hover:shadow-md hover:border-emerald-300 transition"
            >
              <div className="w-10 h-10 rounded-lg bg-emerald-50 text-emerald-700 grid place-items-center text-xl mb-2">
                +
              </div>
              <p className="font-medium text-gray-900">Add Money</p>
              <p className="text-xs text-gray-500">Deposit via Razorpay</p>
            </Link>
            <Link
              to="/transfer"
              className="bg-white border border-gray-200 rounded-xl p-4 hover:shadow-md hover:border-emerald-300 transition"
            >
              <div className="w-10 h-10 rounded-lg bg-blue-50 text-blue-700 grid place-items-center text-xl mb-2">
                ↗
              </div>
              <p className="font-medium text-gray-900">Send Money</p>
              <p className="text-xs text-gray-500">Transfer to another wallet</p>
            </Link>
            <button
              onClick={() => setWithdrawOpen(true)}
              className="text-left bg-white border border-gray-200 rounded-xl p-4 hover:shadow-md hover:border-emerald-300 transition"
            >
              <div className="w-10 h-10 rounded-lg bg-amber-50 text-amber-700 grid place-items-center text-xl mb-2">
                ↓
              </div>
              <p className="font-medium text-gray-900">Withdraw</p>
              <p className="text-xs text-gray-500">Withdraw funds</p>
            </button>
          </div>

          <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
            <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
              <h2 className="font-semibold text-gray-900">Recent transactions</h2>
              <Link to="/transactions" className="text-sm text-emerald-700 hover:underline">
                View all →
              </Link>
            </div>
            {transactions.length === 0 ? (
              <div className="py-10 text-center text-sm text-gray-500">
                No transactions yet.
              </div>
            ) : (
              <div>
                {transactions.map((tx) => (
                  <TransactionItem key={String(tx.id)} tx={tx} />
                ))}
              </div>
            )}
          </div>
        </>
      )}

      {withdrawOpen && (
        <div
          className="fixed inset-0 bg-black/40 grid place-items-center px-4 z-50"
          onClick={() => !withdrawLoading && setWithdrawOpen(false)}
        >
          <div
            className="bg-white rounded-xl p-6 w-full max-w-sm shadow-xl"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Withdraw funds</h3>
            <form onSubmit={handleWithdraw} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Amount</label>
                <input
                  type="number"
                  min="1"
                  step="0.01"
                  required
                  value={withdrawAmount}
                  onChange={(e) => setWithdrawAmount(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>
              <div className="flex items-center gap-2 justify-end">
                <button
                  type="button"
                  onClick={() => setWithdrawOpen(false)}
                  disabled={withdrawLoading}
                  className="px-3 py-2 text-sm rounded-lg bg-gray-100 hover:bg-gray-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={withdrawLoading}
                  className="px-4 py-2 text-sm rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white flex items-center gap-2 disabled:opacity-60"
                >
                  {withdrawLoading ? <Spinner /> : 'Confirm withdraw'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardPage;
