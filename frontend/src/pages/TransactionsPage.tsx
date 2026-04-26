import { useCallback, useEffect, useState } from 'react';
import api from '../api/axios';
import TransactionItem from '../components/TransactionItem';
import Toast, { ToastMessage } from '../components/Toast';
import type { ApiResponse, Page, Transaction } from '../types';

const PAGE_SIZE = 10;

const TransactionsPage = () => {
  const [page, setPage] = useState<Page<Transaction> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState<ToastMessage | null>(null);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      const res = await api.get<ApiResponse<Page<Transaction>>>('/v1/transactions', {
        params: { page: p, size: PAGE_SIZE },
      });
      setPage(res.data.data);
    } catch (err) {
      setToast({ id: Date.now(), kind: 'error', text: (err as Error).message });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(currentPage);
  }, [currentPage, load]);

  const totalPages = page?.totalPages ?? 0;
  const pageNumbers: number[] = [];
  if (totalPages > 0) {
    const start = Math.max(0, currentPage - 2);
    const end = Math.min(totalPages - 1, currentPage + 2);
    for (let i = start; i <= end; i++) pageNumbers.push(i);
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8">
      <Toast toast={toast} onDismiss={() => setToast(null)} />

      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Transactions</h1>
        <p className="text-sm text-gray-500">
          {page ? `${page.totalElements} total` : 'Your full transaction history'}
        </p>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        {loading ? (
          <div className="grid place-items-center py-20">
            <div className="w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : page && page.content.length > 0 ? (
          <div>
            {page.content.map((tx) => (
              <TransactionItem key={String(tx.id)} tx={tx} />
            ))}
          </div>
        ) : (
          <div className="py-16 text-center text-sm text-gray-500">
            No transactions found.
          </div>
        )}
      </div>

      {page && totalPages > 1 && (
        <div className="flex items-center justify-between mt-4 flex-wrap gap-3">
          <p className="text-sm text-gray-500">
            Page {currentPage + 1} of {totalPages}
          </p>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
              disabled={!page.hasPrevious}
              className="px-3 py-1.5 text-sm rounded-lg bg-white border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Previous
            </button>
            {pageNumbers.map((n) => (
              <button
                key={n}
                onClick={() => setCurrentPage(n)}
                className={`px-3 py-1.5 text-sm rounded-lg border ${
                  n === currentPage
                    ? 'bg-emerald-600 text-white border-emerald-600'
                    : 'bg-white border-gray-300 hover:bg-gray-50'
                }`}
              >
                {n + 1}
              </button>
            ))}
            <button
              onClick={() => setCurrentPage((p) => p + 1)}
              disabled={!page.hasNext}
              className="px-3 py-1.5 text-sm rounded-lg bg-white border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default TransactionsPage;
