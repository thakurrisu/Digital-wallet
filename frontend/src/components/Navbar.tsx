import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await api.post('/v1/auth/logout');
    } catch {
      // ignore network error; still clear local auth state
    } finally {
      logout();
      navigate('/login', { replace: true });
    }
  };

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `px-3 py-2 rounded-md text-sm font-medium transition ${
      isActive
        ? 'bg-emerald-50 text-emerald-700'
        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
    }`;

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-40">
      <div className="max-w-6xl mx-auto px-4 sm:px-6">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center gap-2 font-bold text-lg text-emerald-700">
            <span className="inline-block w-8 h-8 rounded-lg bg-emerald-600 text-white grid place-items-center text-sm">
              W
            </span>
            Wallet
          </Link>

          <div className="hidden md:flex items-center gap-1">
            <NavLink to="/" end className={linkClass}>
              Dashboard
            </NavLink>
            <NavLink to="/transactions" className={linkClass}>
              Transactions
            </NavLink>
            <NavLink to="/transfer" className={linkClass}>
              Transfer
            </NavLink>
            <NavLink to="/add-money" className={linkClass}>
              Add Money
            </NavLink>
            <NavLink to="/profile" className={linkClass}>
              Profile
            </NavLink>
          </div>

          <div className="hidden md:flex items-center gap-3">
            <span className="text-sm text-gray-600 truncate max-w-[150px]">
              {user?.name}
            </span>
            <button
              onClick={handleLogout}
              className="px-3 py-1.5 text-sm rounded-md bg-gray-900 text-white hover:bg-gray-700 transition"
            >
              Logout
            </button>
          </div>

          <button
            className="md:hidden p-2 text-gray-700"
            onClick={() => setOpen((o) => !o)}
            aria-label="Toggle menu"
          >
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 6h18M3 12h18M3 18h18" />
            </svg>
          </button>
        </div>

        {open && (
          <div className="md:hidden pb-3 flex flex-col gap-1">
            <NavLink to="/" end className={linkClass} onClick={() => setOpen(false)}>
              Dashboard
            </NavLink>
            <NavLink to="/transactions" className={linkClass} onClick={() => setOpen(false)}>
              Transactions
            </NavLink>
            <NavLink to="/transfer" className={linkClass} onClick={() => setOpen(false)}>
              Transfer
            </NavLink>
            <NavLink to="/add-money" className={linkClass} onClick={() => setOpen(false)}>
              Add Money
            </NavLink>
            <NavLink to="/profile" className={linkClass} onClick={() => setOpen(false)}>
              Profile
            </NavLink>
            <button
              onClick={handleLogout}
              className="mt-1 px-3 py-2 text-sm rounded-md bg-gray-900 text-white hover:bg-gray-700 transition text-left"
            >
              Logout ({user?.name})
            </button>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
