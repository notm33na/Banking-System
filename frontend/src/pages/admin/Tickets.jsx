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

export default function AdminTickets() {
  const [tickets, setTickets] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [messages, setMessages] = useState([]);
  const [reply, setReply] = useState('');

  const fetchTickets = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/tickets', { params: { page, size: 10 } });
      setTickets(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setTickets([]); }
    setLoading(false);
  }, [page]);

  useEffect(() => { fetchTickets(); }, [fetchTickets]);

  const openTicket = async (ticket) => {
    setSelected(ticket);
    try {
      const { data } = await api.get(`/admin/tickets/${ticket.id}/messages`);
      setMessages(data.data || []);
    } catch { setMessages([]); }
  };

  const sendReply = async () => {
    if (!reply.trim() || !selected) return;
    try {
      await api.post(`/admin/tickets/${selected.id}/reply`, { message: reply });
      setReply('');
      const { data } = await api.get(`/admin/tickets/${selected.id}/messages`);
      setMessages(data.data || []);
    } catch {}
  };

  const updateStatus = async (id, status) => {
    try { await api.put(`/admin/tickets/${id}/status`, { status }); fetchTickets(); } catch {}
  };

  const columns = [
    { key: 'subject', label: 'Subject' },
    { key: 'userName', label: 'Submitted By' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
    { key: 'priority', label: 'Priority', render: (v) => <Badge status={v} /> },
    { key: 'assignedToName', label: 'Assigned To', render: (v) => v || 'Unassigned' },
    { key: 'createdAt', label: 'Created', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
  ];

  return (
    <div>
      <div className={styles['page-header']}><h1>Support Tickets</h1></div>
      {loading ? <Spinner /> : tickets.length === 0 ? <EmptyState message="No tickets" /> : (
        <>
          <Table columns={columns} data={tickets} actions={(row) => (
            <>
              <Button size="small" variant="secondary" onClick={() => openTicket(row)}>View</Button>
              <select style={{ fontSize: '0.75rem', padding: '0.25rem' }}
                value={row.status} onChange={e => updateStatus(row.id, e.target.value)}>
                <option value="OPEN">Open</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="RESOLVED">Resolved</option>
                <option value="CLOSED">Closed</option>
              </select>
            </>
          )} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      <Modal isOpen={!!selected} onClose={() => { setSelected(null); setMessages([]); }} title={selected?.subject || 'Ticket'}>
        <div className={styles.thread}>
          {messages.map(m => (
            <div key={m.id} className={`${styles['message-bubble']} ${styles.other}`}>
              <div><strong>{m.senderName}</strong></div>
              <div>{m.message}</div>
              <div className={styles['message-meta']}>{new Date(m.createdAt).toLocaleString()}</div>
            </div>
          ))}
        </div>
        <div className={styles['reply-box']}>
          <input value={reply} onChange={e => setReply(e.target.value)} placeholder="Type a reply..."
            onKeyDown={e => e.key === 'Enter' && sendReply()} />
          <Button variant="primary" onClick={sendReply}>Send</Button>
        </div>
      </Modal>
    </div>
  );
}
