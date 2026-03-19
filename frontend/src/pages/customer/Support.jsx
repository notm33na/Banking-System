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

export default function CustomerSupport() {
  const [tickets, setTickets] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [threadOpen, setThreadOpen] = useState(null);
  const [messages, setMessages] = useState([]);
  const [form, setForm] = useState({ subject: '', category: 'GENERAL', priority: 'MEDIUM', message: '' });
  const [submitting, setSubmitting] = useState(false);

  const fetchTickets = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/customer/tickets', { params: { page, size: 10 } });
      setTickets(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setTickets([]); }
    setLoading(false);
  }, [page]);

  useEffect(() => { fetchTickets(); }, [fetchTickets]);

  const createTicket = async (e) => {
    e.preventDefault(); setSubmitting(true);
    try { await api.post('/customer/tickets', form); setCreateOpen(false); setForm({ subject: '', category: 'GENERAL', priority: 'MEDIUM', message: '' }); fetchTickets(); } catch {}
    setSubmitting(false);
  };

  const openThread = async (ticket) => {
    setThreadOpen(ticket);
    try { const { data } = await api.get(`/customer/tickets/${ticket.id}/messages`); setMessages(data.data || []); } catch { setMessages([]); }
  };

  const columns = [
    { key: 'subject', label: 'Subject' },
    { key: 'category', label: 'Category' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
    { key: 'priority', label: 'Priority', render: (v) => <Badge status={v} /> },
    { key: 'createdAt', label: 'Created', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
  ];

  const onFormChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  return (
    <div>
      <div className={styles['page-header']}>
        <h1>Support</h1>
        <Button variant="primary" onClick={() => setCreateOpen(true)}>New Ticket</Button>
      </div>
      {loading ? <Spinner /> : tickets.length === 0 ? <EmptyState message="No support tickets" /> : (
        <>
          <Table columns={columns} data={tickets} actions={(row) => (
            <Button size="small" variant="secondary" onClick={() => openThread(row)}>View</Button>
          )} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Create Support Ticket">
        <form onSubmit={createTicket}>
          <FormInput label="Subject" name="subject" value={form.subject} onChange={onFormChange} />
          <FormInput label="Category" name="category" value={form.category} onChange={onFormChange}
            options={[{ value: 'GENERAL', label: 'General' }, { value: 'TECHNICAL', label: 'Technical' }, { value: 'BILLING', label: 'Billing' }]} />
          <FormInput label="Priority" name="priority" value={form.priority} onChange={onFormChange}
            options={[{ value: 'LOW', label: 'Low' }, { value: 'MEDIUM', label: 'Medium' }, { value: 'HIGH', label: 'High' }]} />
          <FormInput label="Message" name="message" type="textarea" value={form.message} onChange={onFormChange} />
          <Button type="submit" variant="primary" loading={submitting}>Submit Ticket</Button>
        </form>
      </Modal>
      <Modal isOpen={!!threadOpen} onClose={() => setThreadOpen(null)} title={threadOpen?.subject || 'Ticket'}>
        <div className={styles.thread}>
          {messages.map(m => (
            <div key={m.id} className={`${styles['message-bubble']} ${styles.other}`}>
              <strong>{m.senderName}</strong>
              <div>{m.message}</div>
              <div className={styles['message-meta']}>{new Date(m.createdAt).toLocaleString()}</div>
            </div>
          ))}
          {messages.length === 0 && <EmptyState message="No messages yet" />}
        </div>
      </Modal>
    </div>
  );
}
