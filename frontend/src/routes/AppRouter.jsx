import { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import Layout from '../components/Layout/Layout';
import ErrorBoundary from '../components/ErrorBoundary/ErrorBoundary';
import { DashboardSkeleton } from '../components/Skeleton/Skeleton';

// Public pages (loaded eagerly — small)
import Login from '../pages/public/Login';
import Register from '../pages/public/Register';
import Unauthorized from '../pages/public/Unauthorized';

// ── Lazy-loaded panel pages ─────────────────────────────────────────

// Admin
const AdminDashboard    = lazy(() => import('../pages/admin/Dashboard'));
const AdminUsers        = lazy(() => import('../pages/admin/Users'));
const AdminAccounts     = lazy(() => import('../pages/admin/Accounts'));
const AdminTransactions = lazy(() => import('../pages/admin/Transactions'));
const AdminKyc          = lazy(() => import('../pages/admin/Kyc'));
const AdminLoans        = lazy(() => import('../pages/admin/Loans'));
const AdminTickets      = lazy(() => import('../pages/admin/Tickets'));
const AdminAudit        = lazy(() => import('../pages/admin/AuditLogs'));

// Customer
const CustomerDashboard    = lazy(() => import('../pages/customer/Dashboard'));
const CustomerProfile      = lazy(() => import('../pages/customer/Profile'));
const CustomerTransactions = lazy(() => import('../pages/customer/Transactions'));
const CustomerLoans        = lazy(() => import('../pages/customer/Loans'));
const CustomerSupport      = lazy(() => import('../pages/customer/Support'));
const CustomerNotifications= lazy(() => import('../pages/customer/Notifications'));
const CustomerDocuments    = lazy(() => import('../pages/customer/Documents'));

// Business
const BusinessDashboard    = lazy(() => import('../pages/business/Dashboard'));
const BusinessAccounts     = lazy(() => import('../pages/business/Accounts'));
const BusinessTeam         = lazy(() => import('../pages/business/Team'));
const BusinessPayroll      = lazy(() => import('../pages/business/Payroll'));
const BusinessTransactions = lazy(() => import('../pages/business/Transactions'));
const BusinessInvoices     = lazy(() => import('../pages/business/Invoices'));
const BusinessLoans        = lazy(() => import('../pages/business/Loans'));
const BusinessAnalytics    = lazy(() => import('../pages/business/Analytics'));

function LazyFallback() {
  return <DashboardSkeleton />;
}

export default function AppRouter() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      {/* Admin */}
      <Route path="/admin" element={
        <ProtectedRoute requiredRole="ROLE_ADMIN"><ErrorBoundary><Layout /></ErrorBoundary></ProtectedRoute>
      }>
        <Route index element={<Suspense fallback={<LazyFallback />}><AdminDashboard /></Suspense>} />
        <Route path="users" element={<Suspense fallback={<LazyFallback />}><AdminUsers /></Suspense>} />
        <Route path="accounts" element={<Suspense fallback={<LazyFallback />}><AdminAccounts /></Suspense>} />
        <Route path="transactions" element={<Suspense fallback={<LazyFallback />}><AdminTransactions /></Suspense>} />
        <Route path="kyc" element={<Suspense fallback={<LazyFallback />}><AdminKyc /></Suspense>} />
        <Route path="loans" element={<Suspense fallback={<LazyFallback />}><AdminLoans /></Suspense>} />
        <Route path="tickets" element={<Suspense fallback={<LazyFallback />}><AdminTickets /></Suspense>} />
        <Route path="audit-logs" element={<Suspense fallback={<LazyFallback />}><AdminAudit /></Suspense>} />
      </Route>

      {/* Customer */}
      <Route path="/customer" element={
        <ProtectedRoute requiredRole="ROLE_CUSTOMER"><ErrorBoundary><Layout /></ErrorBoundary></ProtectedRoute>
      }>
        <Route index element={<Suspense fallback={<LazyFallback />}><CustomerDashboard /></Suspense>} />
        <Route path="profile" element={<Suspense fallback={<LazyFallback />}><CustomerProfile /></Suspense>} />
        <Route path="transactions" element={<Suspense fallback={<LazyFallback />}><CustomerTransactions /></Suspense>} />
        <Route path="loans" element={<Suspense fallback={<LazyFallback />}><CustomerLoans /></Suspense>} />
        <Route path="support" element={<Suspense fallback={<LazyFallback />}><CustomerSupport /></Suspense>} />
        <Route path="notifications" element={<Suspense fallback={<LazyFallback />}><CustomerNotifications /></Suspense>} />
        <Route path="documents" element={<Suspense fallback={<LazyFallback />}><CustomerDocuments /></Suspense>} />
      </Route>

      {/* Business */}
      <Route path="/business" element={
        <ProtectedRoute requiredRole="ROLE_BUSINESS"><ErrorBoundary><Layout /></ErrorBoundary></ProtectedRoute>
      }>
        <Route index element={<Suspense fallback={<LazyFallback />}><BusinessDashboard /></Suspense>} />
        <Route path="accounts" element={<Suspense fallback={<LazyFallback />}><BusinessAccounts /></Suspense>} />
        <Route path="team" element={<Suspense fallback={<LazyFallback />}><BusinessTeam /></Suspense>} />
        <Route path="payroll" element={<Suspense fallback={<LazyFallback />}><BusinessPayroll /></Suspense>} />
        <Route path="transactions" element={<Suspense fallback={<LazyFallback />}><BusinessTransactions /></Suspense>} />
        <Route path="invoices" element={<Suspense fallback={<LazyFallback />}><BusinessInvoices /></Suspense>} />
        <Route path="loans" element={<Suspense fallback={<LazyFallback />}><BusinessLoans /></Suspense>} />
        <Route path="analytics" element={<Suspense fallback={<LazyFallback />}><BusinessAnalytics /></Suspense>} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
