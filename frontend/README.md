# Digital Wallet Frontend

React + TypeScript + Vite + Tailwind frontend for the Digital Wallet Spring Boot backend.

## Setup

```bash
cd frontend
npm install
npm run dev
```

Runs on http://localhost:5173. Backend is expected at http://localhost:8080/api.

## Scripts

- `npm run dev` — start dev server
- `npm run build` — typecheck and build production bundle
- `npm run preview` — preview the built bundle

## Notes

- JWT token is held in React state (AuthContext), not localStorage. Refresh = logout.
- Every deposit / withdraw / transfer generates a fresh UUID `referenceId` for idempotency.
- Razorpay checkout is loaded via `<script>` tag in `index.html`.
- On any `401` response, the axios interceptor clears the token and the router redirects to `/login`.
