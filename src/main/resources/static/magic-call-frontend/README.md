# Magic Call - Next.js Admin Panel

Modern Next.js 14 admin panel for Magic Call Voice Changer system.

## Features

✅ **Modern Tech Stack**
- Next.js 14 with App Router
- TypeScript for type safety
- Tailwind CSS for styling
- Custom UI components (shadcn-style)
- Axios for API calls
- Cookie-based JWT authentication

✅ **Authentication & Authorization**
- JWT-based authentication
- Role-based access control (RBAC)
- Secure cookie storage
- Auto-redirect on unauthorized access

✅ **Admin Dashboard**
- Real-time statistics (Users, Packages, Purchases, Voice Types)
- Recent purchases table
- Responsive sidebar navigation
- Clean, modern UI design

✅ **Management Pages**
- Dashboard overview
- User management (to be implemented)
- Package management (to be implemented)
- Voice types management (to be implemented)
- Purchase history (to be implemented)

## Setup Instructions

### 1. Install Dependencies

```bash
cd /home/prototype/Downloads/magic-call-frontend
npm install
```

### 2. Configure Environment Variables

The `.env.local` file is already configured with:

```bash
NEXT_PUBLIC_API_BASE_URL=http://192.168.0.103:8080/api
```

Update if your backend API runs on a different URL.

### 3. Run Development Server

```bash
npm run dev
```

The app will be available at `http://localhost:3000`

### 4. Build for Production

```bash
npm run build
npm start
```

## Default Login Credentials

**Admin User:**
- Username: `admin`
- Password: `admin123`

## Project Structure

```
magic-call-frontend/
├── app/
│   ├── dashboard/
│   │   └── page.tsx          # Main dashboard
│   ├── login/
│   │   └── page.tsx          # Login page
│   ├── globals.css           # Global styles
│   ├── layout.tsx            # Root layout
│   └── page.tsx              # Home (redirects to login)
├── components/
│   ├── ui/
│   │   ├── button.tsx        # Button component
│   │   ├── card.tsx          # Card components
│   │   ├── input.tsx         # Input component
│   │   └── table.tsx         # Table components
│   └── DashboardLayout.tsx   # Dashboard layout with sidebar
├── lib/
│   ├── api.ts                # Axios API client
│   ├── auth.ts               # Authentication utilities
│   └── utils.ts              # Utility functions
├── types/
│   └── index.ts              # TypeScript type definitions
├── .env.local                # Environment variables
└── package.json
```

## Comparison: Old vs New

### Old HTML/JS Stack:
- ❌ Plain HTML files
- ❌ Vanilla JavaScript
- ❌ jQuery-style DOM manipulation
- ❌ No type safety
- ❌ Manual routing
- ❌ Bootstrap CDN

### New Next.js Stack:
- ✅ React components
- ✅ TypeScript
- ✅ Modern React hooks
- ✅ Full type safety
- ✅ File-based routing
- ✅ Tailwind CSS
- ✅ Server-side rendering (SSR)
- ✅ API route optimization
- ✅ Built-in image optimization
- ✅ Automatic code splitting

## Getting Started

1. Start the Spring Boot backend (port 8080)
2. Run the Next.js dev server:
   ```bash
   cd /home/prototype/Downloads/magic-call-frontend
   npm run dev
   ```
3. Open `http://localhost:3000`
4. Login with admin credentials

## Next Steps

To complete the admin panel, create these pages:

1. **Users Management** - `app/dashboard/users/page.tsx`
2. **Packages Management** - `app/dashboard/packages/page.tsx`
3. **Voice Types Management** - `app/dashboard/voice-types/page.tsx`
4. **Purchases Management** - `app/dashboard/purchases/page.tsx`

Each page should follow the same pattern as the dashboard page with proper layouts and API integration.
