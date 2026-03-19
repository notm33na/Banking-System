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

export default function BusinessInvoices() {
  const [invoices, setInvoices] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [selected, setSelected] = useState(null);
  const [statusFilter, setStatusFilter] = useState('');
  const [form, setForm] = useState({
    customerName: '', customerEmail: '', dueDate: '', notes: '',
    items: [{ description: '', quantity: 1, unitPrice: '' }],
  });
  const [submitting, setSubmitting] = useState(false);

  const fetchInvoices = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/business/invoices', { params: { page, size: 10 } });
      let content = data.data?.content || [];
      if (statusFilter) content = content.filter(i => i.status === statusFilter);
      setInvoices(content);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setInvoices([]); }
    setLoading(false);
  }, [page, statusFilter]);

  useEffect(() => { fetchInvoices(); }, [fetchInvoices]);

  const addItem = () => setForm({ ...form, items: [...form.items, { description: '', quantity: 1, unitPrice: '' }] });
  const updateItem = (i, field, value) => {
    const items = [...form.items]; items[i] = { ...items[i], [field]: value }; setForm({ ...form, items });
  };

  const createInvoice = async (e) => {
    e.preventDefault(); setSubmitting(true);
    try {
      await api.post('/business/invoices', {
        ...form,
        items: form.items.map(i => ({ ...i, quantity: Number(i.quantity), unitPrice: Number(i.unitPrice) })),
      });
      setCreateOpen(false);
      setForm({ customerName: '', customerEmail: '', dueDate: '', notes: '', items: [{ description: '', quantity: 1, unitPrice: '' }] });
      fetchInvoices();
    } catch {}
    setSubmitting(false);
  };

  const updateStatus = async (id, status) => {
    try { await api.put(`/business/invoices/${id}/status`, { status }); fetchInvoices(); } catch {}
  };

  const fmt = (v) => v != null ? `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}` : '—';

  const columns = [
    { key: 'invoiceNumber', label: 'Invoice #' },
    { key: 'customerName', label: 'Client' },
    { key: 'total', label: 'Amount', render: (v) => fmt(v) },
    { key: 'dueDate', label: 'Due Date' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
  ];

  return (
    <div>
      <div className={styles['page-header']}>
        <h1>Invoices</h1>
        <Button variant="primary" onClick={() => setCreateOpen(true)}>Create Invoice</Button>
      </div>
      <div className={styles.filters}>
        <div className={styles['filter-group']}>
          <label>Status</label>
          <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0); }}>
            <option value="">All</option>
            <option value="DRAFT">Draft</option>
            <option value="SENT">Sent</option>
            <option value="PAID">Paid</option>
            <option value="OVERDUE">Overdue</option>
          </select>
        </div>
      </div>
      {loading ? <Spinner /> : invoices.length === 0 ? <EmptyState message="No invoices" /> : (
        <>
          <Table columns={columns} data={invoices} actions={(row) => (
            <>
              <Button size="small" variant="secondary" onClick={() => setSelected(row)}>View</Button>
              {row.status === 'DRAFT' && <Button size="small" variant="primary" onClick={() => updateStatus(row.id, 'SENT')}>Send</Button>}
              {row.status === 'SENT' && <Button size="small" variant="success" onClick={() => updateStatus(row.id, 'PAID')}>Mark Paid</Button>}
            </>
          )} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      {/* Detail Modal */}
      <Modal isOpen={!!selected} onClose={() => setSelected(null)} title={`Invoice ${selected?.invoiceNumber || ''}`}>
        {selected && (
          <div className={styles['detail-grid']}>
            <div className={styles['detail-item']}><label>Client</label><span>{selected.customerName}</span></div>
            <div className={styles['detail-item']}><label>Email</label><span>{selected.customerEmail || '—'}</span></div>
            <div className={styles['detail-item']}><label>Subtotal</label><span>{fmt(selected.subtotal)}</span></div>
            <div className={styles['detail-item']}><label>Tax</label><span>{fmt(selected.tax)}</span></div>
            <div className={styles['detail-item']}><label>Total</label><span style={{ fontWeight: 700 }}>{fmt(selected.total)}</span></div>
            <div className={styles['detail-item']}><label>Due</label><span>{selected.dueDate}</span></div>
            <div className={styles['detail-item']}><label>Status</label><span><Badge status={selected.status} /></span></div>
          </div>
        )}
      </Modal>
      {/* Create Modal */}
      <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Create Invoice">
        <form onSubmit={createInvoice}>
          <FormInput label="Client Name" name="customerName" value={form.customerName} onChange={e => setForm({ ...form, customerName: e.target.value })} />
          <FormInput label="Client Email" name="customerEmail" type="email" value={form.customerEmail} onChange={e => setForm({ ...form, customerEmail: e.target.value })} />
          <FormInput label="Due Date" name="dueDate" type="date" value={form.dueDate} onChange={e => setForm({ ...form, dueDate: e.target.value })} />
          <FormInput label="Notes" name="notes" type="textarea" value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
          <h4 style={{ margin: '1rem 0 0.5rem' }}>Line Items</h4>
          {form.items.map((item, i) => (
            <div key={i} style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '0.5rem' }}>
              <FormInput label="Description" value={item.description} onChange={e => updateItem(i, 'description', e.target.value)} />
              <FormInput label="Qty" type="number" value={item.quantity} onChange={e => updateItem(i, 'quantity', e.target.value)} style={{ width: 80 }} />
              <FormInput label="Price" type="number" value={item.unitPrice} onChange={e => updateItem(i, 'unitPrice', e.target.value)} />
            </div>
          ))}
          <Button variant="secondary" onClick={addItem} style={{ marginBottom: '1rem' }}>+ Add Item</Button>
          <div><Button type="submit" variant="primary" loading={submitting}>Create Invoice</Button></div>
        </form>
      </Modal>
    </div>
  );
}
