import { useState, useEffect, useCallback } from 'react';
import api from '../../api/axiosInstance';
import Button from '../../components/Button/Button';
import Pagination from '../../components/Pagination/Pagination';
import Spinner from '../../components/Spinner/Spinner';
import EmptyState from '../../components/EmptyState/EmptyState';
import styles from '../Page.module.css';

export default function CustomerNotifications() {
  const [notifs, setNotifs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchNotifs = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/customer/notifications', { params: { page, size: 15 } });
      setNotifs(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setNotifs([]); }
    setLoading(false);
  }, [page]);

  useEffect(() => { fetchNotifs(); }, [fetchNotifs]);

  const markRead = async (id) => {
    try { await api.put(`/customer/notifications/${id}/read`); fetchNotifs(); } catch {}
  };

  const markAllRead = async () => {
    try { await api.put('/customer/notifications/read-all'); fetchNotifs(); } catch {}
  };

  return (
    <div>
      <div className={styles['page-header']}>
        <h1>Notifications</h1>
        <Button variant="secondary" onClick={markAllRead}>Mark All as Read</Button>
      </div>
      {loading ? <Spinner /> : notifs.length === 0 ? <EmptyState message="No notifications" /> : (
        <>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            {notifs.map(n => (
              <div key={n.id} style={{
                background: n.isRead ? 'var(--bg-card)' : '#eef2ff',
                border: `1px solid ${n.isRead ? 'var(--border)' : 'var(--primary-light)'}`,
                borderRadius: 'var(--radius-md)', padding: '1rem 1.25rem',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              }}>
                <div>
                  <div style={{ fontSize: '0.875rem', fontWeight: n.isRead ? 400 : 600 }}>{n.message}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>{n.notificationType} · {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}</div>
                </div>
                {!n.isRead && <Button size="small" variant="secondary" onClick={() => markRead(n.id)}>Read</Button>}
              </div>
            ))}
          </div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}
