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

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/users', { params: { page, size: 10 } });
      let content = data.data?.content || [];
      if (roleFilter) content = content.filter(u => u.userType === roleFilter);
      if (statusFilter) content = content.filter(u => u.status === statusFilter);
      setUsers(content);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setUsers([]); }
    setLoading(false);
  }, [page, roleFilter, statusFilter]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const updateStatus = async (id, status) => {
    try { await api.put(`/admin/users/${id}/status`, { status }); fetchUsers(); } catch {}
  };
  const deleteUser = async (id) => {
    if (!confirm('Delete this user?')) return;
    try { await api.delete(`/admin/users/${id}`); fetchUsers(); } catch {}
  };

  const columns = [
    { key: 'firstName', label: 'Name', render: (_, r) => `${r.firstName} ${r.lastName}` },
    { key: 'email', label: 'Email' },
    { key: 'userType', label: 'Role' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
    { key: 'createdAt', label: 'Joined', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
  ];

  return (
    <div>
      <div className={styles['page-header']}><h1>User Management</h1></div>
      <div className={styles.filters}>
        <div className={styles['filter-group']}>
          <label>Role</label>
          <select value={roleFilter} onChange={e => { setRoleFilter(e.target.value); setPage(0); }}>
            <option value="">All</option>
            <option value="CUSTOMER">Customer</option>
            <option value="BUSINESS">Business</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>
        <div className={styles['filter-group']}>
          <label>Status</label>
          <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0); }}>
            <option value="">All</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="SUSPENDED">Suspended</option>
          </select>
        </div>
      </div>
      {loading ? <Spinner /> : users.length === 0 ? <EmptyState message="No users found" /> : (
        <>
          <Table columns={columns} data={users} actions={(row) => (
            <>
              <Button size="small" variant="secondary" onClick={() => setSelected(row)}>View</Button>
              <select style={{ fontSize: '0.75rem', padding: '0.25rem' }}
                value={row.status} onChange={e => updateStatus(row.id, e.target.value)}>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
                <option value="SUSPENDED">Suspended</option>
              </select>
              <Button size="small" variant="danger" onClick={() => deleteUser(row.id)}>Delete</Button>
            </>
          )} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      <Modal isOpen={!!selected} onClose={() => setSelected(null)} title="User Details">
        {selected && (
          <div className={styles['detail-grid']}>
            <div className={styles['detail-item']}><label>Name</label><span>{selected.firstName} {selected.lastName}</span></div>
            <div className={styles['detail-item']}><label>Email</label><span>{selected.email}</span></div>
            <div className={styles['detail-item']}><label>Phone</label><span>{selected.phone || '—'}</span></div>
            <div className={styles['detail-item']}><label>Role</label><span>{selected.userType}</span></div>
            <div className={styles['detail-item']}><label>Status</label><span><Badge status={selected.status} /></span></div>
            <div className={styles['detail-item']}><label>Verified</label><span>{selected.emailVerified ? 'Yes' : 'No'}</span></div>
          </div>
        )}
      </Modal>
    </div>
  );
}
