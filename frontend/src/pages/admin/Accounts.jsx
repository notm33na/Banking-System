import { useState, useEffect, useCallback } from 'react';
import api from '../../api/axiosInstance';
import Table from '../../components/Table/Table';
import Pagination from '../../components/Pagination/Pagination';
import Badge from '../../components/Badge/Badge';
import Button from '../../components/Button/Button';
import Modal from '../../components/Modal/Modal';
import Spinner from '../../components/Spinner/Spinner';
import EmptyState from '../../components/EmptyState/EmptyState';
import styles from '../Page.module.css';

export default function AdminAccounts() {
  const [accounts, setAccounts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);

  const fetchAccounts = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/accounts', { params: { page, size: 10 } });
      setAccounts(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setAccounts([]); }
    setLoading(false);
  }, [page]);

  useEffect(() => { fetchAccounts(); }, [fetchAccounts]);

  const closeAccount = async (id) => {
    if (!confirm('Close this account?')) return;
    try { await api.put(`/admin/accounts/${id}/close`); fetchAccounts(); } catch {}
  };

  const fmt = (v) => v != null ? `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}` : '—';

  const columns = [
    { key: 'accountNumber', label: 'Account #' },
    { key: 'ownerName', label: 'Owner' },
    { key: 'accountType', label: 'Type' },
    { key: 'balance', label: 'Balance', render: (v) => fmt(v) },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
  ];

  return (
    <div>
      <div className={styles['page-header']}><h1>Account Management</h1></div>
      {loading ? <Spinner /> : accounts.length === 0 ? <EmptyState message="No accounts found" /> : (
        <>
          <Table columns={columns} data={accounts} actions={(row) => (
            <>
              <Button size="small" variant="secondary" onClick={() => setSelected(row)}>Details</Button>
              {row.status !== 'CLOSED' && <Button size="small" variant="danger" onClick={() => closeAccount(row.id)}>Close</Button>}
            </>
          )} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      <Modal isOpen={!!selected} onClose={() => setSelected(null)} title="Account Details">
        {selected && (
          <div className={styles['detail-grid']}>
            <div className={styles['detail-item']}><label>Account #</label><span>{selected.accountNumber}</span></div>
            <div className={styles['detail-item']}><label>Owner</label><span>{selected.ownerName}</span></div>
            <div className={styles['detail-item']}><label>Type</label><span>{selected.accountType}</span></div>
            <div className={styles['detail-item']}><label>Balance</label><span>{fmt(selected.balance)}</span></div>
            <div className={styles['detail-item']}><label>Currency</label><span>{selected.currency}</span></div>
            <div className={styles['detail-item']}><label>Status</label><span><Badge status={selected.status} /></span></div>
            <div className={styles['detail-item']}><label>Name</label><span>{selected.accountName || '—'}</span></div>
            <div className={styles['detail-item']}><label>Created</label><span>{selected.createdAt ? new Date(selected.createdAt).toLocaleDateString() : '—'}</span></div>
          </div>
        )}
      </Modal>
    </div>
  );
}
