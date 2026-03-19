import { useState } from 'react';
import { NavLink, Outlet, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import useNotifications from '../../hooks/useNotifications';
import styles from './Layout.module.css';

const NAV_CONFIG = {
  ROLE_ADMIN: {
    base: '/admin',
    label: 'Admin',
    sections: [
      {
        title: 'Overview',
        links: [
          { to: '/admin', label: 'Dashboard', icon: '📊', end: true },
        ],
      },
      {
        title: 'Management',
        links: [
          { to: '/admin/users', label: 'Users', icon: '👥' },
          { to: '/admin/accounts', label: 'Accounts', icon: '🏦' },
          { to: '/admin/transactions', label: 'Transactions', icon: '💸' },
          { to: '/admin/kyc', label: 'KYC / Compliance', icon: '📋' },
          { to: '/admin/loans', label: 'Loans', icon: '💳' },
          { to: '/admin/tickets', label: 'Support Tickets', icon: '🎫' },
          { to: '/admin/audit-logs', label: 'Audit Logs', icon: '🔍' },
        ],
      },
    ],
  },
  ROLE_CUSTOMER: {
    base: '/customer',
    label: 'Customer',
    sections: [
      {
        title: 'Overview',
        links: [
          { to: '/customer', label: 'Dashboard', icon: '📊', end: true },
        ],
      },
      {
        title: 'Banking',
        links: [
          { to: '/customer/transactions', label: 'Transactions', icon: '💸' },
          { to: '/customer/loans', label: 'Loans', icon: '💳' },
        ],
      },
      {
        title: 'Account',
        links: [
          { to: '/customer/profile', label: 'Profile', icon: '👤' },
          { to: '/customer/support', label: 'Support', icon: '🎫' },
          { to: '/customer/notifications', label: 'Notifications', icon: '🔔' },
          { to: '/customer/documents', label: 'Documents', icon: '📄' },
        ],
      },
    ],
  },
  ROLE_BUSINESS: {
    base: '/business',
    label: 'Business',
    sections: [
      {
        title: 'Overview',
        links: [
          { to: '/business', label: 'Dashboard', icon: '📊', end: true },
        ],
      },
      {
        title: 'Operations',
        links: [
          { to: '/business/accounts', label: 'Accounts', icon: '🏦' },
          { to: '/business/transactions', label: 'Transactions', icon: '💸' },
          { to: '/business/invoices', label: 'Invoices', icon: '🧾' },
          { to: '/business/payroll', label: 'Payroll', icon: '💰' },
        ],
      },
      {
        title: 'Management',
        links: [
          { to: '/business/team', label: 'Team Access', icon: '👥' },
          { to: '/business/loans', label: 'Loans', icon: '💳' },
          { to: '/business/analytics', label: 'Analytics', icon: '📈' },
        ],
      },
    ],
  },
};

export default function Layout() {
  const { user, logout } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { unreadCount } = useNotifications();
  const nav = NAV_CONFIG[user?.role] || NAV_CONFIG.ROLE_CUSTOMER;

  const closeSidebar = () => setSidebarOpen(false);

  return (
    <div className={styles.layout}>
      {/* Overlay */}
      <div
        className={`${styles['sidebar-overlay']} ${sidebarOpen ? styles.open : ''}`}
        onClick={closeSidebar}
      />

      {/* Sidebar */}
      <aside className={`${styles.sidebar} ${sidebarOpen ? styles.open : ''}`}>
        <div className={styles['sidebar-header']}>
          <span className={styles['sidebar-logo']}>VIRTBANK</span>
        </div>
        <nav className={styles['sidebar-nav']}>
          {nav.sections.map((section) => (
            <div key={section.title} className={styles['sidebar-section']}>
              <div className={styles['sidebar-section-label']}>{section.title}</div>
              {section.links.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={link.end}
                  className={({ isActive }) =>
                    `${styles['sidebar-link']} ${isActive ? styles.active : ''}`
                  }
                  onClick={closeSidebar}
                >
                  <span className={styles['sidebar-link-icon']}>{link.icon}</span>
                  {link.label}
                </NavLink>
              ))}
            </div>
          ))}
        </nav>
      </aside>

      {/* Topbar */}
      <header className={styles.topbar}>
        <div className={styles['topbar-left']}>
          <button className={styles.hamburger} onClick={() => setSidebarOpen(!sidebarOpen)}>
            ☰
          </button>
          <h2 style={{ fontSize: '1rem', fontWeight: 600 }}>{nav.label} Panel</h2>
        </div>
        <div className={styles['topbar-right']}>
          <Link to={nav.base === '/admin' ? '/admin' : `${nav.base}/notifications`}
            style={{ position: 'relative', fontSize: '1.25rem', textDecoration: 'none', cursor: 'pointer' }}>
            🔔
            {unreadCount > 0 && (
              <span style={{
                position: 'absolute', top: -6, right: -8,
                background: 'var(--danger)', color: '#fff',
                fontSize: '0.65rem', fontWeight: 700, minWidth: 18, height: 18,
                borderRadius: 'var(--radius-full)', display: 'flex',
                alignItems: 'center', justifyContent: 'center',
                border: '2px solid var(--bg-topbar)',
              }}>{unreadCount > 99 ? '99+' : unreadCount}</span>
            )}
          </Link>
          <span className={styles['topbar-user']}>{user?.email}</span>
          <span className={styles['topbar-role']}>{user?.role?.replace('ROLE_', '')}</span>
          <button className={styles['topbar-logout']} onClick={logout}>Logout</button>
        </div>
      </header>

      {/* Main */}
      <main className={styles['main-content']}>
        <Outlet />
      </main>
    </div>
  );
}
