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
  { value: 'BUSINESS', label: 'Business' }, { value: 'WORKING_CAPITAL', label: 'Working Capital' },
  { value: 'EQUIPMENT', label: 'Equipment' }, { value: 'REAL_ESTATE', label: 'Real Estate' },
];

export default function BusinessLoans() {
  const [tab, setTab] = useState('list');
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [accounts, setAccounts] = useState([]);
  const [scheduleModal, setScheduleModal] = useState(null);
  const [schedule, setSchedule] = useState([]);
  const [form, setForm] = useState({ accountId: '', loanType: '', amount: '', interestRate: '', termMonths: '', purpose: '' });
  const [submitting, setSubmitting] = useState(false);
  const [msg, setMsg] = useState('');

  const fetchLoans = async () => {
    setLoading(true);
    try { const { data } = await api.get('/business/loans'); setLoans(data.data || []); } catch { setLoans([]); }
    setLoading(false);
  };

  useEffect(() => {
    fetchLoans();
    api.get('/business/accounts').then(({ data }) => setAccounts(data.data || [])).catch(() => {});
  }, []);

  const openSchedule = async (loan) => {
    setScheduleModal(loan);
    try { const { data } = await api.get(`/business/loans/${loan.id}/schedule`); setSchedule(data.data || []); } catch { setSchedule([]); }
  };

  const apply = async (e) => {
    e.preventDefault(); setSubmitting(true); setMsg('');
    try {
      await api.post('/business/loans/apply', {
        accountId: Number(form.accountId), loanType: form.loanType,
        amount: Number(form.amount), interestRate: Number(form.interestRate),
        termMonths: Number(form.termMonths), purpose: form.purpose,
      });
      setMsg('Loan application submitted!');
      setForm({ accountId: '', loanType: '', amount: '', interestRate: '', termMonths: '', purpose: '' });
      fetchLoans();
    } catch (err) { setMsg(err.response?.data?.message || 'Failed'); }
    setSubmitting(false);
  };

  const fmt = (v) => v != null ? `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}` : '—';
  const onFormChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const loanCols = [
    { key: 'loanType', label: 'Type' },
    { key: 'amount', label: 'Amount', render: (v) => fmt(v) },
    { key: 'termMonths', label: 'Term', render: (v) => `${v} mo` },
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
      <div className={styles['page-header']}><h1>Business Loans</h1></div>
      <div className={styles['tab-bar']}>
        <button className={`${styles.tab} ${tab === 'list' ? styles.active : ''}`} onClick={() => setTab('list')}>My Loans</button>
        <button className={`${styles.tab} ${tab === 'apply' ? styles.active : ''}`} onClick={() => setTab('apply')}>Apply</button>
      </div>
      {tab === 'list' && (
        loading ? <Spinner /> : loans.length === 0 ? <EmptyState message="No business loans" /> : (
          <Table columns={loanCols} data={loans} actions={(row) => (
            <Button size="small" variant="secondary" onClick={() => openSchedule(row)}>Schedule</Button>
          )} />
        )
      )}
      {tab === 'apply' && (
        <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem', maxWidth: 500 }}>
          {msg && <div style={{ color: msg.includes('!') ? 'var(--success)' : 'var(--danger)', fontSize: '0.85rem', marginBottom: '0.75rem' }}>{msg}</div>}
          <form onSubmit={apply}>
            <FormInput label="Account" name="accountId" value={form.accountId} onChange={onFormChange}
              options={accounts.map(a => ({ value: a.id, label: a.accountNumber }))} />
            <FormInput label="Loan Type" name="loanType" value={form.loanType} onChange={onFormChange} options={LOAN_TYPES} />
            <FormInput label="Amount ($)" name="amount" type="number" value={form.amount} onChange={onFormChange} />
            <FormInput label="Interest Rate (%)" name="interestRate" type="number" value={form.interestRate} onChange={onFormChange} />
            <FormInput label="Term (months)" name="termMonths" type="number" value={form.termMonths} onChange={onFormChange} />
            <FormInput label="Purpose / Business Details" name="purpose" type="textarea" value={form.purpose} onChange={onFormChange} />
            <Button type="submit" variant="primary" loading={submitting}>Submit Application</Button>
          </form>
        </div>
      )}
      <Modal isOpen={!!scheduleModal} onClose={() => setScheduleModal(null)} title="Repayment Schedule">
        {schedule.length === 0 ? <EmptyState message="No schedule generated yet" /> : <Table columns={schedCols} data={schedule} />}
      </Modal>
    </div>
  );
}
