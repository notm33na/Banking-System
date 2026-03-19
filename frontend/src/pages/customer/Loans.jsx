import { useState, useEffect } from 'react';
import api from '../../api/axiosInstance';
import Table from '../../components/Table/Table';
import Badge from '../../components/Badge/Badge';
import Button from '../../components/Button/Button';
import Modal from '../../components/Modal/Modal';
import FormInput from '../../components/FormInput/FormInput';
import Spinner from '../../components/Spinner/Spinner';
import EmptyState from '../../components/EmptyState/EmptyState';
import styles from '../Page.module.css';

const LOAN_TYPES = [
  { value: 'PERSONAL', label: 'Personal' }, { value: 'HOME', label: 'Home' },
  { value: 'AUTO', label: 'Auto' }, { value: 'EDUCATION', label: 'Education' },
  { value: 'BUSINESS', label: 'Business' },
];

export default function CustomerLoans() {
  const [tab, setTab] = useState('list');
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [schedule, setSchedule] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [form, setForm] = useState({ accountId: '', loanType: '', amount: '', interestRate: '', termMonths: '', purpose: '' });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchLoans = async () => {
    setLoading(true);
    try { const { data } = await api.get('/customer/loans'); setLoans(data.data || []); } catch { setLoans([]); }
    setLoading(false);
  };

  useEffect(() => {
    fetchLoans();
    api.get('/customer/accounts').then(({ data }) => setAccounts(data.data || [])).catch(() => {});
  }, []);

  const openSchedule = async (loan) => {
    setSelected(loan);
    try { const { data } = await api.get(`/customer/loans/${loan.id}/schedule`); setSchedule(data.data || []); } catch { setSchedule([]); }
  };

  const applyLoan = async (e) => {
    e.preventDefault(); setSubmitting(true); setError(''); setSuccess('');
    try {
      await api.post('/customer/loans/apply', {
        accountId: Number(form.accountId), loanType: form.loanType,
        amount: Number(form.amount), interestRate: Number(form.interestRate),
        termMonths: Number(form.termMonths), purpose: form.purpose,
      });
      setSuccess('Loan application submitted!');
      setForm({ accountId: '', loanType: '', amount: '', interestRate: '', termMonths: '', purpose: '' });
      fetchLoans();
    } catch (err) { setError(err.response?.data?.message || 'Failed to apply'); }
    setSubmitting(false);
  };

  const fmt = (v) => v != null ? `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}` : '—';
  const onFormChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const loanCols = [
    { key: 'loanType', label: 'Type' },
    { key: 'amount', label: 'Amount', render: (v) => fmt(v) },
    { key: 'termMonths', label: 'Term', render: (v) => `${v} mo` },
    { key: 'interestRate', label: 'Rate', render: (v) => `${v}%` },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
  ];

  const schedCols = [
    { key: 'dueDate', label: 'Due Date', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
    { key: 'amount', label: 'Payment', render: (v) => fmt(v) },
    { key: 'principal', label: 'Principal', render: (v) => fmt(v) },
    { key: 'interest', label: 'Interest', render: (v) => fmt(v) },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
  ];

  return (
    <div>
      <div className={styles['page-header']}><h1>Loans</h1></div>
      <div className={styles['tab-bar']}>
        <button className={`${styles.tab} ${tab === 'list' ? styles.active : ''}`} onClick={() => setTab('list')}>My Loans</button>
        <button className={`${styles.tab} ${tab === 'apply' ? styles.active : ''}`} onClick={() => setTab('apply')}>Apply</button>
      </div>
      {tab === 'list' && (
        loading ? <Spinner /> : loans.length === 0 ? <EmptyState message="No loans yet" /> : (
          <Table columns={loanCols} data={loans} actions={(row) => (
            <Button size="small" variant="secondary" onClick={() => openSchedule(row)}>Schedule</Button>
          )} />
        )
      )}
      {tab === 'apply' && (
        <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem', maxWidth: 500 }}>
          {error && <div style={{ color: 'var(--danger)', fontSize: '0.85rem', marginBottom: '0.75rem' }}>{error}</div>}
          {success && <div style={{ color: 'var(--success)', fontSize: '0.85rem', marginBottom: '0.75rem' }}>{success}</div>}
          <form onSubmit={applyLoan}>
            <FormInput label="Account" name="accountId" value={form.accountId} onChange={onFormChange}
              options={accounts.map(a => ({ value: a.id, label: `${a.accountNumber} (${a.accountType})` }))} />
            <FormInput label="Loan Type" name="loanType" value={form.loanType} onChange={onFormChange} options={LOAN_TYPES} />
            <FormInput label="Amount ($)" name="amount" type="number" value={form.amount} onChange={onFormChange} />
            <FormInput label="Interest Rate (%)" name="interestRate" type="number" value={form.interestRate} onChange={onFormChange} />
            <FormInput label="Term (months)" name="termMonths" type="number" value={form.termMonths} onChange={onFormChange} />
            <FormInput label="Purpose" name="purpose" type="textarea" value={form.purpose} onChange={onFormChange} />
            <Button type="submit" variant="primary" loading={submitting}>Submit Application</Button>
          </form>
        </div>
      )}
      <Modal isOpen={!!selected} onClose={() => setSelected(null)} title="Repayment Schedule">
        {schedule.length === 0 ? <EmptyState message="No schedule generated yet" /> : <Table columns={schedCols} data={schedule} />}
      </Modal>
    </div>
  );
}
