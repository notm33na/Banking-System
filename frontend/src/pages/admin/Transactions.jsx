import { useState, useEffect, useCallback } from 'react';
import api from '../../api/axiosInstance';
import Table from '../../components/Table/Table';
import Pagination from '../../components/Pagination/Pagination';
import Badge from '../../components/Badge/Badge';
import Button from '../../components/Button/Button';
import Spinner from '../../components/Spinner/Spinner';
import EmptyState from '../../components/EmptyState/EmptyState';
import styles from '../Page.module.css';

export default function AdminTransactions() {
  const [txs, setTxs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');

  const fetchTxs = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/transactions', { params: { page, size: 10 } });
      let content = data.data?.content || [];
      if (statusFilter) content = content.filter(t => t.status === statusFilter);
      setTxs(content);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setTxs([]); }
    setLoading(false);
  }, [page, statusFilter]);

  useEffect(() => { fetchTxs(); }, [fetchTxs]);

  const flagTx = async (id) => { try { await api.put(`/admin/transactions/${id}/flag`); fetchTxs(); } catch {} };
  const approveTx = async (id) => { try { await api.put(`/admin/transactions/${id}/approve`); fetchTxs(); } catch {} };

  const fmt = (v) => v != null ? `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}` : '—';

  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'sourceAccountId', label: 'From', render: (v) => v || '—' },
    { key: 'destinationAccountId', label: 'To', render: (v) => v || '—' },
    { key: 'amount', label: 'Amount', render: (v) => fmt(v) },
    { key: 'transactionType', label: 'Type' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
    { key: 'createdAt', label: 'Date', render: (v) => v ? new Date(v).toLocaleString() : '—' },
  ];

  return (
    <div>
      <div className={styles['page-header']}><h1>Transaction Monitoring</h1></div>
      <div className={styles.filters}>
        <div className={styles['filter-group']}>
          <label>Status</label>
          <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0); }}>
            <option value="">All</option>
            <option value="COMPLETED">Completed</option>
            <option value="PENDING">Pending</option>
            <option value="FLAGGED">Flagged</option>
          </select>
        </div>
      </div>
      {loading ? <Spinner /> : txs.length === 0 ? <EmptyState message="No transactions found" /> : (
        <>
          <Table columns={columns} data={txs} actions={(row) => (
            <>
              {row.status !== 'FLAGGED' && <Button size="small" variant="danger" onClick={() => flagTx(row.id)}>Flag</Button>}
              {row.status !== 'COMPLETED' && <Button size="small" variant="success" onClick={() => approveTx(row.id)}>Approve</Button>}
            </>
          )} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}
