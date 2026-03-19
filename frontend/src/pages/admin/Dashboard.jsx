import { useState, useEffect } from 'react';
import api from '../../api/axiosInstance';
import StatCard from '../../components/StatCard/StatCard';
import Spinner from '../../components/Spinner/Spinner';
import styles from '../Page.module.css';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [usersRes, txRes, loansRes, ticketsRes] = await Promise.all([
          api.get('/admin/users', { params: { page: 0, size: 1 } }),
          api.get('/admin/transactions', { params: { page: 0, size: 1 } }),
          api.get('/admin/loans/pending'),
          api.get('/admin/tickets', { params: { page: 0, size: 1 } }),
        ]);
        setStats({
          totalUsers: usersRes.data.data?.totalElements || 0,
          totalTransactions: txRes.data.data?.totalElements || 0,
          pendingLoans: (loansRes.data.data || []).length,
          openTickets: ticketsRes.data.data?.totalElements || 0,
        });
      } catch { setStats({ totalUsers: 0, totalTransactions: 0, pendingLoans: 0, openTickets: 0 }); }
      setLoading(false);
    };
    fetchStats();
  }, []);

  if (loading) return <Spinner />;

  return (
    <div>
      <div className={styles['page-header']}><h1>Admin Dashboard</h1></div>
      <div className={styles['stats-grid']}>
        <StatCard title="Total Users" value={stats.totalUsers} subtitle="Registered accounts" />
        <StatCard title="Transactions" value={stats.totalTransactions} subtitle="All time" />
        <StatCard title="Pending Loans" value={stats.pendingLoans} subtitle="Awaiting review" />
        <StatCard title="Support Tickets" value={stats.openTickets} subtitle="Total tickets" />
      </div>
    </div>
  );
}
