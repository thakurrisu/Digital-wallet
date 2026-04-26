import type { Transaction } from '../types';

const formatAmount = (amount: number) =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(amount);

const formatDate = (iso: string) => {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const isCredit = (type: string) => type === 'DEPOSIT' || type === 'TRANSFER_IN';

const typeLabel = (type: string) => {
  switch (type) {
    case 'DEPOSIT':
      return 'Deposit';
    case 'WITHDRAW':
      return 'Withdraw';
    case 'TRANSFER_IN':
      return 'Received';
    case 'TRANSFER_OUT':
      return 'Sent';
    default:
      return type;
  }
};

const statusBadge = (status: string) => {
  const base = 'px-2 py-0.5 rounded-full text-xs font-medium';
  switch (status) {
    case 'SUCCESS':
      return `${base} bg-emerald-50 text-emerald-700`;
    case 'PENDING':
      return `${base} bg-amber-50 text-amber-700`;
    case 'FAILED':
      return `${base} bg-red-50 text-red-700`;
    default:
      return `${base} bg-gray-100 text-gray-700`;
  }
};

const TransactionItem = ({ tx }: { tx: Transaction }) => {
  const credit = isCredit(tx.type);
  const sign = credit ? '+' : '-';
  const amountColor = credit ? 'text-emerald-600' : 'text-red-600';

  return (
    <div className="flex items-center justify-between gap-4 py-3 px-4 border-b border-gray-100 last:border-b-0 hover:bg-gray-50 transition">
      <div className="flex items-center gap-3 min-w-0">
        <div
          className={`w-10 h-10 rounded-full grid place-items-center font-semibold ${
            credit ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'
          }`}
        >
          {credit ? '↓' : '↑'}
        </div>
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <p className="font-medium text-gray-900 truncate">{typeLabel(tx.type)}</p>
            <span className={statusBadge(tx.status)}>{tx.status}</span>
          </div>
          <p className="text-xs text-gray-500 truncate">{formatDate(tx.createdAt)}</p>
        </div>
      </div>

      <div className="text-right shrink-0">
        <p className={`font-semibold ${amountColor}`}>
          {sign}
          {formatAmount(tx.amount)}
        </p>
        {tx.balanceAfter !== undefined && tx.balanceAfter !== null && (
          <p className="text-xs text-gray-500">Bal: {formatAmount(tx.balanceAfter)}</p>
        )}
      </div>
    </div>
  );
};

export default TransactionItem;
