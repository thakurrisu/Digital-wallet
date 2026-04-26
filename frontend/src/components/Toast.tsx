import { useEffect } from 'react';

export type ToastKind = 'success' | 'error' | 'info';

export interface ToastMessage {
  id: number;
  kind: ToastKind;
  text: string;
}

interface Props {
  toast: ToastMessage | null;
  onDismiss: () => void;
}

const colors: Record<ToastKind, string> = {
  success: 'bg-emerald-600',
  error: 'bg-red-600',
  info: 'bg-gray-800',
};

const Toast = ({ toast, onDismiss }: Props) => {
  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(onDismiss, 3500);
    return () => clearTimeout(t);
  }, [toast, onDismiss]);

  if (!toast) return null;

  return (
    <div className="fixed top-4 right-4 z-50">
      <div
        className={`${colors[toast.kind]} text-white px-4 py-3 rounded-lg shadow-lg max-w-sm`}
        role="alert"
      >
        <div className="flex items-start gap-3">
          <span className="text-sm">{toast.text}</span>
          <button onClick={onDismiss} className="text-white/80 hover:text-white">
            ×
          </button>
        </div>
      </div>
    </div>
  );
};

export default Toast;
