export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface User {
  id: string | number;
  name: string;
  email: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface RawUser {
  id: string | number;
  userName?: string;
  name?: string;
  email: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType?: string;
  user: RawUser;
}

export const normalizeUser = (raw: RawUser): User => ({
  id: raw.id,
  name: raw.name ?? raw.userName ?? '',
  email: raw.email,
  status: raw.status,
  createdAt: raw.createdAt,
  updatedAt: raw.updatedAt,
});

export type WalletStatus = 'ACTIVE' | 'FROZEN' | string;

export interface Wallet {
  id: string | number;
  balance: number;
  status: WalletStatus;
  currency?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type TransactionType =
  | 'DEPOSIT'
  | 'WITHDRAW'
  | 'TRANSFER_IN'
  | 'TRANSFER_OUT'
  | string;

export type TransactionStatus = 'SUCCESS' | 'PENDING' | 'FAILED' | string;

export interface Transaction {
  id: string | number;
  type: TransactionType;
  amount: number;
  status: TransactionStatus;
  referenceId: string;
  balanceAfter?: number;
  senderWalletId?: string | number;
  receiverWalletId?: string | number;
  description?: string;
  createdAt: string;
}

export type PaymentStatus = 'CREATED' | 'PENDING' | 'SUCCESS' | 'FAILED' | string;

export interface PaymentInitiateResponse {
  paymentId: string | number;
  gatewayOrderId: string;
  keyId: string;
  amount: number;
  currency: string;
  status?: PaymentStatus;
}

export interface Payment {
  id: string | number;
  amount: number;
  currency: string;
  status: PaymentStatus;
  gatewayOrderId?: string;
  gatewayPaymentId?: string;
  createdAt: string;
}
