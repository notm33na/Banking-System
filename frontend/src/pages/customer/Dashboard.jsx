import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosInstance';
import StatCard from '../../components/StatCard/StatCard';
import Button from '../../components/Button/Button';
import Spinner from '../../components/Spinner/Spinner';
import styles from '../Page.module.css';

export default function CustomerDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const [balRes, txRes, loanRes, notifRes] = await Promise.all([
          api.get('/customer/accounts/total-balance'),
          api.get('/customer/transactions/history/0', { params: { page: 0, size: 1 } }).catch(() => ({ data: { data: { totalElements: 0 } } })),
          api.get('/customer/loans').catch(() => ({ data: { data: [] } })),
          api.get('/customer/notifications/unread-count').catch(() => ({ data: { data: 0 } })),
        ]);
        setStats({
          balance: balRes.data.data ?? 0,
          txCount: txRes.data.data?.totalElements || 0,
          activeLoans: (loanRes.data.data || []).filter(l => l.status === 'ACTIVE').length,
          unread: notifRes.data.data ?? 0,
        });
      } catch { setStats({ balance: 0, txCount: 0, activeLoans: 0, unread: 0 }); }
      setLoading(false);
    };
    fetch();
  }, []);

  if (loading) return <Spinner />;

  const fmt = (v) => `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}`;

  return (
    <div>
      <div className={styles['page-header']}><h1>My Dashboard</h1></div>
      <div className={styles['stats-grid']}>
        <StatCard title="Total Balance" value={fmt(stats.balance)} subtitle="Across all accounts" />
        <StatCard title="Transactions" value={stats.txCount} subtitle="All time" />
        <StatCard title="Active Loans" value={stats.activeLoans} subtitle="Currently active" />
        <StatCard title="Unread Alerts" value={stats.unread} subtitle="Notifications" />
      </div>
      <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
        <Link to="/customer/transactions"><Button variant="primary">💸 Transfer</Button></Link>
        <Link to="/customer/transactions"><Button variant="secondary">💰 Pay</Button></Link>
        <Link to="/customer/loans"><Button variant="secondary">💳 Apply for Loan</Button></Link>
      </div>
    </div>
  );
}
