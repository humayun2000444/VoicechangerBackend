# Magic Call - Next.js Admin Panel

Modern Next.js 14 admin panel for Magic Call Voice Changer system with complete dashboard functionality.

## Features

✅ **Modern Tech Stack**
- Next.js 14 with App Router
- TypeScript for type safety
- Tailwind CSS for styling
- shadcn/ui components
- Lucide React icons
- date-fns for date formatting
- JWT-based authentication

✅ **Authentication & Authorization**
- JWT token-based authentication
- Role-based access control (RBAC)
- Secure localStorage token management
- Auto-redirect on unauthorized access
- Protected routes with auth middleware

✅ **Dashboard Pages**
- **Overview** - Real-time statistics and insights
- **Users** - User management with enable/disable
- **Voice Types** - Voice type CRUD operations
- **Top-Up Requests** - Approve/reject balance top-ups
- **Voice Purchases** - Approve/reject voice purchase requests
- **Expired Voices** - View voice expiry history with cleanup stats
- **Call History** - View call logs and analytics

✅ **UI Features**
- Responsive design (mobile, tablet, desktop)
- Modern gradient color schemes (purple to pink)
- Interactive tables with hover effects
- Status badges with color coding
- Search and filter functionality
- Real-time data updates
- Loading states and error handling
- Confirmation dialogs for destructive actions

## Setup Instructions

### 1. Install Dependencies

```bash
cd src/main/resources/static/magic-call-frontend
npm install
```

### 2. Configure Environment Variables

Create or update `.env.local`:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
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

Or build and run the backend with frontend:
```bash
cd ../../../../../../../  # Back to project root
mvn clean package
java -jar target/VoicechangerBackend-1.0-SNAPSHOT.jar
```

## Default Login Credentials

**Admin User:**
- Username: `admin`
- Password: `admin123`

**Note:** Change these credentials in production!

## Project Structure

```
magic-call-frontend/
├── app/
│   ├── dashboard/
│   │   ├── page.tsx                    # Main dashboard overview
│   │   ├── users/
│   │   │   └── page.tsx                # User management
│   │   ├── voice-types/
│   │   │   └── page.tsx                # Voice types management
│   │   ├── topup/
│   │   │   └── page.tsx                # Top-up request approval
│   │   ├── voice-purchases/
│   │   │   └── page.tsx                # Voice purchase approval
│   │   ├── expired-voices/
│   │   │   └── page.tsx                # Expired voices history
│   │   └── call-history/
│   │       └── page.tsx                # Call history logs
│   ├── login/
│   │   └── page.tsx                    # Login page
│   ├── globals.css                     # Global styles
│   ├── layout.tsx                      # Root layout
│   └── page.tsx                        # Home (redirects to dashboard)
├── components/
│   ├── ui/
│   │   ├── button.tsx                  # Button component
│   │   ├── card.tsx                    # Card components
│   │   ├── input.tsx                   # Input component
│   │   └── table.tsx                   # Table components
│   └── DashboardLayout.tsx             # Dashboard layout with sidebar
├── lib/
│   ├── api.ts                          # Axios API client
│   ├── auth.ts                         # Authentication utilities
│   └── utils.ts                        # Utility functions (cn, etc.)
├── .env.local                          # Environment variables
├── tailwind.config.ts                  # Tailwind configuration
├── tsconfig.json                       # TypeScript configuration
├── next.config.mjs                     # Next.js configuration
└── package.json                        # Dependencies
```

## API Integration

The frontend integrates with the backend API through `lib/api.ts`:

### Authentication
```typescript
// Login
const response = await api.post('/auth/login', { username, password })
authService.login(response.token, username)

// Logout
authService.logout()
```

### API Calls
```typescript
// GET request
const users = await api.get('/users')

// POST request
await api.post('/voice-types', voiceTypeData)

// PUT request
await api.put(`/voice-purchase/${id}/approve`)

// DELETE request
await api.delete(`/users/${id}`)
```

## Dashboard Pages

### 1. Dashboard Overview (`/dashboard`)
- Total users count
- Total voice types count
- Pending top-ups count
- Pending purchases count
- Recent activity

### 2. Users Management (`/dashboard/users`)
- View all users
- Enable/disable users
- Delete users
- View user details (balance, roles, created date)

### 3. Voice Types (`/dashboard/voice-types`)
- Create new voice types
- Edit existing voice types
- Delete voice types
- View voice type codes

### 4. Top-Up Requests (`/dashboard/topup`)
- View pending top-up requests
- Approve/reject requests
- View transaction details
- Search by username or transaction ID

### 5. Voice Purchases (`/dashboard/voice-purchases`)
- View pending purchase requests
- Approve/reject purchases
- View subscription details
- Filter by status

### 6. Expired Voices (`/dashboard/expired-voices`)
- View all expired voice mappings
- Filter by expiry reason (TRIAL_EXPIRED, SUBSCRIPTION_EXPIRED, BOTH_EXPIRED)
- Search by username or voice name
- View cleanup statistics
- Manual cleanup trigger

### 7. Call History (`/dashboard/call-history`)
- View all call logs
- Filter by user, date range
- View call duration and status
- Search functionality

## Component Architecture

### Reusable Components

**DashboardLayout**
- Responsive sidebar navigation
- Top navigation bar with user info
- Logout functionality
- Mobile menu toggle

**UI Components (shadcn/ui)**
- `Button` - Various button styles and sizes
- `Card` - Card container components
- `Input` - Form input fields
- `Table` - Data table components

### Design System

**Colors:**
- Primary: Purple to Pink gradient (`from-purple-600 to-pink-600`)
- Success: Green (`green-500`)
- Warning: Yellow (`yellow-500`)
- Error: Red (`red-500`)
- Info: Blue (`blue-500`)

**Typography:**
- Headings: Bold with gradient text
- Body: Regular gray text
- Labels: Semibold

## Authentication Flow

1. User navigates to `/login`
2. Enters credentials
3. Backend validates and returns JWT token
4. Token stored in localStorage
5. Redirected to `/dashboard`
6. Protected routes check for valid token
7. Invalid/expired token redirects to `/login`

## State Management

- React hooks (useState, useEffect) for local state
- localStorage for auth persistence
- API calls with loading/error states
- Real-time data refresh on actions

## Styling Approach

- **Tailwind CSS** utility classes
- Responsive breakpoints (sm, md, lg, xl)
- Hover and focus states
- Gradient backgrounds
- Shadow effects
- Smooth transitions

## Error Handling

- API error display with user-friendly messages
- Try-catch blocks for async operations
- Loading states during API calls
- Empty states when no data
- Form validation

## Getting Started

1. **Start the Backend:**
   ```bash
   cd /home/prototype/Downloads/VoicechnagerBackend
   mvn spring-boot:run
   ```

2. **Start the Frontend:**
   ```bash
   cd src/main/resources/static/magic-call-frontend
   npm run dev
   ```

3. **Open Browser:**
   - Navigate to `http://localhost:3000`
   - Login with admin credentials
   - Explore the dashboard

## Development Tips

### Adding a New Page

1. Create page file: `app/dashboard/new-page/page.tsx`
2. Add route to sidebar: `components/DashboardLayout.tsx`
3. Create API integration in `lib/api.ts`
4. Add TypeScript types if needed

### Styling Guidelines

- Use Tailwind utility classes
- Follow existing color scheme (purple/pink gradients)
- Maintain responsive design (mobile-first)
- Use Lucide icons for consistency

### API Integration

- Always use the `api` utility from `lib/api.ts`
- Handle loading and error states
- Use TypeScript interfaces for API responses
- Add try-catch blocks for error handling

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Performance

- Next.js automatic code splitting
- Image optimization
- CSS purging in production
- Lazy loading for routes

## Security

- JWT token validation
- Protected routes
- XSS prevention
- CORS configuration
- Secure API calls

## Troubleshooting

### Backend Connection Issues
- Ensure backend is running on port 8080
- Check `NEXT_PUBLIC_API_BASE_URL` in `.env.local`
- Verify CORS settings in Spring Boot

### Authentication Issues
- Clear localStorage: `localStorage.clear()`
- Check token expiration (24 hours)
- Verify admin user exists in database

### Build Issues
- Clear `.next` folder: `rm -rf .next`
- Reinstall dependencies: `rm -rf node_modules && npm install`
- Check Node.js version: `node --version` (requires v18+)

## Version

**v1.3.0** - Complete admin dashboard with all management features

## License

Proprietary - All rights reserved
