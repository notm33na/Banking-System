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

export default function BusinessPayroll() {
  const [history, setHistory] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [form, setForm] = useState({ payPeriodStart: '', payPeriodEnd: '', items: [{ employeeName: '', employeeAccount: '', amount: '' }] });
  const [submitting, setSubmitting] = useState(false);

  const fetchHistory = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/business/payroll/history', { params: { page, size: 10 } });
      setHistory(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setHistory([]); }
    setLoading(false);
  }, [page]);

  useEffect(() => { fetchHistory(); }, [fetchHistory]);

  const addItem = () => setForm({ ...form, items: [...form.items, { employeeName: '', employeeAccount: '', amount: '' }] });
  const removeItem = (i) => setForm({ ...form, items: form.items.filter((_, idx) => idx !== i) });
  const updateItem = (i, field, value) => {
    const items = [...form.items]; items[i] = { ...items[i], [field]: value }; setForm({ ...form, items });
  };

  const createPayroll = async (e) => {
    e.preventDefault(); setSubmitting(true);
    try {
      await api.post('/business/payroll', {
        payPeriodStart: form.payPeriodStart, payPeriodEnd: form.payPeriodEnd,
        items: form.items.map(i => ({ ...i, amount: Number(i.amount) })),
      });
      setCreateOpen(false); setForm({ payPeriodStart: '', payPeriodEnd: '', items: [{ employeeName: '', employeeAccount: '', amount: '' }] });
      fetchHistory();
    } catch {}
    setSubmitting(false);
  };

  const runPayroll = async (id) => {
    try { await api.put(`/business/payroll/${id}/run`); fetchHistory(); } catch {}
  };

  const fmt = (v) => v != null ? `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}` : '—';

  const columns = [
    { key: 'payPeriodStart', label: 'Period Start' },
    { key: 'payPeriodEnd', label: 'Period End' },
    { key: 'totalAmount', label: 'Total', render: (v) => fmt(v) },
    { key: 'itemCount', label: 'Employees' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
  ];

  return (
    <div>
      <div className={styles['page-header']}>
        <h1>Payroll</h1>
        <Button variant="primary" onClick={() => setCreateOpen(true)}>Create Payroll</Button>
      </div>
      {loading ? <Spinner /> : history.length === 0 ? <EmptyState message="No payroll history" /> : (
        <>
          <Table columns={columns} data={history} actions={(row) => row.status === 'DRAFT' ? (
            <Button size="small" variant="success" onClick={() => runPayroll(row.id)}>Run</Button>
          ) : null} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Create Payroll">
        <form onSubmit={createPayroll}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 1rem' }}>
            <FormInput label="Period Start" name="payPeriodStart" type="date" value={form.payPeriodStart} onChange={e => setForm({ ...form, payPeriodStart: e.target.value })} />
            <FormInput label="Period End" name="payPeriodEnd" type="date" value={form.payPeriodEnd} onChange={e => setForm({ ...form, payPeriodEnd: e.target.value })} />
          </div>
          <h4 style={{ margin: '1rem 0 0.5rem' }}>Employees</h4>
          {form.items.map((item, i) => (
            <div key={i} style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '0.5rem' }}>
              <FormInput label="Name" name="employeeName" value={item.employeeName} onChange={e => updateItem(i, 'employeeName', e.target.value)} />
              <FormInput label="Account" name="employeeAccount" value={item.employeeAccount} onChange={e => updateItem(i, 'employeeAccount', e.target.value)} />
              <FormInput label="Amount" name="amount" type="number" value={item.amount} onChange={e => updateItem(i, 'amount', e.target.value)} />
              {form.items.length > 1 && <Button size="small" variant="danger" onClick={() => removeItem(i)} style={{ marginBottom: '1rem' }}>✕</Button>}
            </div>
          ))}
          <Button variant="secondary" onClick={addItem} style={{ marginBottom: '1rem' }}>+ Add Employee</Button>
          <div><Button type="submit" variant="primary" loading={submitting}>Create Payroll</Button></div>
        </form>
      </Modal>
    </div>
  );
}
