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

export default function CustomerTransactions() {
  const [accounts, setAccounts] = useState([]);
  const [selectedAcct, setSelectedAcct] = useState('');
  const [txs, setTxs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // 'deposit' | 'withdraw' | 'transfer'
  const [form, setForm] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/customer/accounts').then(({ data }) => {
      const accts = data.data || [];
      setAccounts(accts);
      if (accts.length > 0) setSelectedAcct(accts[0].id);
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

  const openModal = (type) => { setModal(type); setForm({}); setError(''); };
  const onFormChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submitAction = async (e) => {
    e.preventDefault(); setSubmitting(true); setError('');
    try {
      if (modal === 'deposit') await api.post('/customer/transactions/deposit', { accountId: Number(selectedAcct), amount: Number(form.amount) });
      else if (modal === 'withdraw') await api.post('/customer/transactions/withdraw', { accountId: Number(selectedAcct), amount: Number(form.amount) });
      else if (modal === 'transfer') await api.post('/customer/transactions/transfer', { sourceAccountId: Number(selectedAcct), destinationAccountId: Number(form.destinationAccountId), amount: Number(form.amount), description: form.description || '' });
      setModal(null); fetchTxs();
    } catch (err) { setError(err.response?.data?.message || 'Transaction failed'); }
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
    { key: 'description', label: 'Description', render: (v) => v || '—' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
  ];

  return (
    <div>
      <div className={styles['page-header']}>
        <h1>Transactions</h1>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <Button variant="success" onClick={() => openModal('deposit')}>Deposit</Button>
          <Button variant="danger" onClick={() => openModal('withdraw')}>Withdraw</Button>
          <Button variant="primary" onClick={() => openModal('transfer')}>Transfer</Button>
        </div>
      </div>
      <div className={styles.filters}>
        <div className={styles['filter-group']}>
          <label>Account</label>
          <select value={selectedAcct} onChange={e => { setSelectedAcct(e.target.value); setPage(0); }}>
            {accounts.map(a => <option key={a.id} value={a.id}>{a.accountNumber} ({a.accountType})</option>)}
          </select>
        </div>
      </div>
      {loading ? <Spinner /> : txs.length === 0 ? <EmptyState message="No transactions yet" /> : (
        <>
          <Table columns={columns} data={txs} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      <Modal isOpen={!!modal} onClose={() => setModal(null)} title={modal === 'deposit' ? 'Deposit Funds' : modal === 'withdraw' ? 'Withdraw Funds' : 'Transfer Funds'}>
        {error && <div style={{ color: 'var(--danger)', fontSize: '0.85rem', marginBottom: '0.75rem' }}>{error}</div>}
        <form onSubmit={submitAction}>
          {modal === 'transfer' && (
            <FormInput label="Destination Account ID" name="destinationAccountId" type="number" value={form.destinationAccountId || ''} onChange={onFormChange} />
          )}
          <FormInput label="Amount ($)" name="amount" type="number" value={form.amount || ''} onChange={onFormChange} />
          {modal === 'transfer' && (
            <FormInput label="Description (optional)" name="description" value={form.description || ''} onChange={onFormChange} />
          )}
          <Button type="submit" variant="primary" loading={submitting} style={{ marginTop: '0.5rem' }}>
            {modal === 'deposit' ? 'Deposit' : modal === 'withdraw' ? 'Withdraw' : 'Transfer'}
          </Button>
        </form>
      </Modal>
    </div>
  );
}
