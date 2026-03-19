import { useState, useEffect } from 'react';
import api from '../../api/axiosInstance';
import Table from '../../components/Table/Table';
import StatCard from '../../components/StatCard/StatCard';
import Spinner from '../../components/Spinner/Spinner';
import EmptyState from '../../components/EmptyState/EmptyState';
import styles from '../Page.module.css';

export default function BusinessAccounts() {
  const [accounts, setAccounts] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const [acctRes, summaryRes] = await Promise.all([
          api.get('/business/accounts'),
          api.get('/business/accounts/summary'),
        ]);
        setAccounts(acctRes.data.data || []);
        setSummary(summaryRes.data.data || {});
      } catch {}
      setLoading(false);
    };
    fetch();
  }, []);

  const fmt = (v) => v != null ? `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}` : '—';

  const columns = [
    { key: 'accountNumber', label: 'Account #' },
    { key: 'accountType', label: 'Type' },
    { key: 'balance', label: 'Balance', render: (v) => fmt(v) },
    { key: 'status', label: 'Status' },
    { key: 'currency', label: 'Currency' },
  ];

  return (
    <div>
      <div className={styles['page-header']}><h1>Business Accounts</h1></div>
      {summary && (
        <div className={styles['stats-grid']} style={{ marginBottom: '1.5rem' }}>
          <StatCard title="Aggregate Balance" value={fmt(summary.aggregateBalance)} subtitle={`${summary.totalAccounts || 0} accounts`} />
        </div>
      )}
      {loading ? <Spinner /> : accounts.length === 0 ? <EmptyState message="No accounts" /> : (
        <Table columns={columns} data={accounts} />
      )}
    </div>
  );
}
