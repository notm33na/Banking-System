import { useState, useEffect, useCallback } from 'react';
import api from '../../api/axiosInstance';
import Table from '../../components/Table/Table';
import Pagination from '../../components/Pagination/Pagination';
import Badge from '../../components/Badge/Badge';
import Button from '../../components/Button/Button';
import Modal from '../../components/Modal/Modal';
import FormInput from '../../components/FormInput/FormInput';
import Spinner from '../../components/Spinner/Spinner';
import EmptyState from '../../components/EmptyState/EmptyState';
import styles from '../Page.module.css';

export default function BusinessTransactions() {
  const [accounts, setAccounts] = useState([]);
  const [selectedAcct, setSelectedAcct] = useState('');
  const [txs, setTxs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [bulkOpen, setBulkOpen] = useState(false);
  const [bulkForm, setBulkForm] = useState({
    sourceAccountId: '', description: '',
    items: [{ recipientAccount: '', recipientName: '', amount: '', reference: '' }],
  });
  const [submitting, setSubmitting] = useState(false);
  const [bulkResult, setBulkResult] = useState(null);

  useEffect(() => {
    api.get('/business/accounts').then(({ data }) => {
      const accts = data.data || [];
      setAccounts(accts);
      if (accts.length > 0) { setSelectedAcct(accts[0].id); setBulkForm(f => ({ ...f, sourceAccountId: accts[0].id })); }
    }).catch(() => {});
  }, []);

  const fetchTxs = useCallback(async () => {
    if (!selectedAcct) { setLoading(false); return; }
    setLoading(true);
    try {
      const { data } = await api.get(`/customer/transactions/history/${selectedAcct}`, { params: { page, size: 10 } });
      setTxs(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setTxs([]); }
    setLoading(false);
  }, [selectedAcct, page]);

  useEffect(() => { fetchTxs(); }, [fetchTxs]);

  const addItem = () => setBulkForm({ ...bulkForm, items: [...bulkForm.items, { recipientAccount: '', recipientName: '', amount: '', reference: '' }] });
  const removeItem = (i) => setBulkForm({ ...bulkForm, items: bulkForm.items.filter((_, idx) => idx !== i) });
  const updateItem = (i, field, value) => {
    const items = [...bulkForm.items]; items[i] = { ...items[i], [field]: value }; setBulkForm({ ...bulkForm, items });
  };

  const submitBulk = async (e) => {
    e.preventDefault(); setSubmitting(true); setBulkResult(null);
    try {
      const { data } = await api.post('/business/bulk-transactions', {
        sourceAccountId: Number(bulkForm.sourceAccountId), description: bulkForm.description,
        items: bulkForm.items.map(i => ({ ...i, amount: Number(i.amount) })),
      });
      setBulkResult(data.data);
      fetchTxs();
    } catch {}
    setSubmitting(false);
  };

  const fmt = (v) => v != null ? `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}` : '—';

  const columns = [
    { key: 'createdAt', label: 'Date', render: (v) => v ? new Date(v).toLocaleString() : '—' },
    { key: 'transactionType', label: 'Type' },
    { key: 'amount', label: 'Amount', render: (v, row) => (
      <span className={row.transactionType === 'DEPOSIT' ? 'amount-credit' : 'amount-debit'}>
        {row.transactionType === 'DEPOSIT' ? '+' : '-'}{fmt(v)}
      </span>
    )},
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
  ];

  return (
    <div>
      <div className={styles['page-header']}>
        <h1>Transactions</h1>
        <Button variant="primary" onClick={() => { setBulkOpen(true); setBulkResult(null); }}>Bulk Payment</Button>
      </div>
      <div className={styles.filters}>
        <div className={styles['filter-group']}>
          <label>Account</label>
          <select value={selectedAcct} onChange={e => { setSelectedAcct(e.target.value); setPage(0); }}>
            {accounts.map(a => <option key={a.id} value={a.id}>{a.accountNumber}</option>)}
          </select>
        </div>
      </div>
      {loading ? <Spinner /> : txs.length === 0 ? <EmptyState message="No transactions" /> : (
        <>
          <Table columns={columns} data={txs} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      <Modal isOpen={bulkOpen} onClose={() => setBulkOpen(false)} title="Bulk Payment">
        {bulkResult ? (
          <div style={{ textAlign: 'center' }}>
            <h3 style={{ color: 'var(--success)', marginBottom: '1rem' }}>Batch Processed</h3>
            <div className={styles['detail-grid']}>
              <div className={styles['detail-item']}><label>Total</label><span>{fmt(bulkResult.totalAmount)}</span></div>
              <div className={styles['detail-item']}><label>Count</label><span>{bulkResult.totalCount}</span></div>
              <div className={styles['detail-item']}><label>Success</label><span style={{ color: 'var(--success)' }}>{bulkResult.successCount}</span></div>
              <div className={styles['detail-item']}><label>Failed</label><span style={{ color: 'var(--danger)' }}>{bulkResult.failedCount}</span></div>
            </div>
            <Button variant="primary" onClick={() => { setBulkOpen(false); setBulkResult(null); }} style={{ marginTop: '1rem' }}>Done</Button>
          </div>
        ) : (
          <form onSubmit={submitBulk}>
            <FormInput label="Source Account" name="sourceAccountId" value={bulkForm.sourceAccountId} onChange={e => setBulkForm({ ...bulkForm, sourceAccountId: e.target.value })}
              options={accounts.map(a => ({ value: a.id, label: a.accountNumber }))} />
            <FormInput label="Description" name="description" value={bulkForm.description} onChange={e => setBulkForm({ ...bulkForm, description: e.target.value })} />
            <h4 style={{ margin: '1rem 0 0.5rem' }}>Recipients</h4>
            {bulkForm.items.map((item, i) => (
              <div key={i} style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '0.5rem' }}>
                <FormInput label="Name" value={item.recipientName} onChange={e => updateItem(i, 'recipientName', e.target.value)} />
                <FormInput label="Account" value={item.recipientAccount} onChange={e => updateItem(i, 'recipientAccount', e.target.value)} />
                <FormInput label="Amount" type="number" value={item.amount} onChange={e => updateItem(i, 'amount', e.target.value)} />
                {bulkForm.items.length > 1 && <Button size="small" variant="danger" onClick={() => removeItem(i)} style={{ marginBottom: '1rem' }}>✕</Button>}
              </div>
            ))}
            <Button variant="secondary" onClick={addItem} style={{ marginBottom: '1rem' }}>+ Add Recipient</Button>
            <div><Button type="submit" variant="primary" loading={submitting}>Submit Batch</Button></div>
          </form>
        )}
      </Modal>
    </div>
  );
}
