import { useState, useEffect } from 'react';
import api from '../../api/axiosInstance';
import StatCard from '../../components/StatCard/StatCard';
import Spinner from '../../components/Spinner/Spinner';
import styles from '../Page.module.css';

export default function BusinessDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const [summaryRes, payrollRes, invoiceRes, loanRes] = await Promise.all([
          api.get('/business/accounts/summary'),
          api.get('/business/payroll/history', { params: { page: 0, size: 1 } }).catch(() => ({ data: { data: { totalElements: 0 } } })),
          api.get('/business/invoices', { params: { page: 0, size: 100 } }).catch(() => ({ data: { data: { content: [] } } })),
          api.get('/business/loans').catch(() => ({ data: { data: [] } })),
        ]);
        const summary = summaryRes.data.data || {};
        const invoices = invoiceRes.data.data?.content || [];
        const unpaid = invoices.filter(i => i.status === 'SENT' || i.status === 'DRAFT').length;
        const activeLoans = (loanRes.data.data || []).filter(l => l.status === 'ACTIVE').length;
        setStats({
          balance: summary.aggregateBalance || 0,
          payrollRuns: payrollRes.data.data?.totalElements || 0,
          unpaidInvoices: unpaid,
          activeLoans,
        });
      } catch { setStats({ balance: 0, payrollRuns: 0, unpaidInvoices: 0, activeLoans: 0 }); }
      setLoading(false);
    };
    fetch();
  }, []);

  if (loading) return <Spinner />;
  const fmt = (v) => `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}`;

  return (
    <div>
      <div className={styles['page-header']}><h1>Business Dashboard</h1></div>
      <div className={styles['stats-grid']}>
        <StatCard title="Total Balance" value={fmt(stats.balance)} subtitle="All accounts" />
        <StatCard title="Payroll Runs" value={stats.payrollRuns} subtitle="Total runs" />
        <StatCard title="Unpaid Invoices" value={stats.unpaidInvoices} subtitle="Outstanding" />
        <StatCard title="Active Loans" value={stats.activeLoans} subtitle="Business loans" />
      </div>
    </div>
  );
}
