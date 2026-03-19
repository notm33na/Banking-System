import { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosInstance';

export default function useNotifications() {
  const { token, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const eventSourceRef = useRef(null);

  // Fetch initial unread count
  const fetchUnreadCount = useCallback(async () => {
    try {
      const { data } = await api.get('/customer/notifications/unread-count');
      setUnreadCount(data.data ?? 0);
    } catch { /* silent */ }
  }, []);

  useEffect(() => {
    if (!isAuthenticated || !token) return;

    fetchUnreadCount();

    // Open SSE connection
    const url = `http://localhost:8080/api/notifications/stream?token=${token}`;
    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.addEventListener('notification', (event) => {
      try {
        const notif = JSON.parse(event.data);
        setNotifications((prev) => [notif, ...prev]);
        setUnreadCount((prev) => prev + 1);
      } catch { /* silent */ }
    });

    es.onerror = () => {
      // Auto-reconnect is handled by EventSource
    };

    return () => {
      es.close();
      eventSourceRef.current = null;
    };
  }, [isAuthenticated, token, fetchUnreadCount]);

  const markAllRead = useCallback(async () => {
    try {
      await api.put('/customer/notifications/read-all');
      setUnreadCount(0);
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch { /* silent */ }
  }, []);

  return { notifications, unreadCount, markAllRead, fetchUnreadCount };
}
