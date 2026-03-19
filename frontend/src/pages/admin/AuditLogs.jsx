import { useState, useEffect, useCallback } from 'react';
import api from '../../api/axiosInstance';
import Table from '../../components/Table/Table';
import Pagination from '../../components/Pagination/Pagination';
import Spinner from '../../components/Spinner/Spinner';
import EmptyState from '../../components/EmptyState/EmptyState';
import styles from '../Page.module.css';

export default function AdminAuditLogs() {
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchLogs = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/audit', { params: { page, size: 15 } });
      setLogs(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setLogs([]); }
    setLoading(false);
  }, [page]);

  useEffect(() => { fetchLogs(); }, [fetchLogs]);

  const columns = [
    { key: 'userName', label: 'Actor', render: (v) => v || '—' },
    { key: 'action', label: 'Action' },
    { key: 'entityType', label: 'Entity Type' },
    { key: 'entityId', label: 'Entity ID', render: (v) => v || '—' },
    { key: 'ipAddress', label: 'IP Address', render: (v) => v || '—' },
    { key: 'createdAt', label: 'Timestamp', render: (v) => v ? new Date(v).toLocaleString() : '—' },
  ];

  return (
    <div>
      <div className={styles['page-header']}><h1>Audit Logs</h1></div>
      {loading ? <Spinner /> : logs.length === 0 ? <EmptyState message="No audit logs" /> : (
        <>
          <Table columns={columns} data={logs} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}
