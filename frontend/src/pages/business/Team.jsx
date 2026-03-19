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

const ROLE_OPTIONS = [
  { value: 'ADMIN', label: 'Admin' }, { value: 'FINANCE_OFFICER', label: 'Finance Officer' },
  { value: 'PAYROLL_MANAGER', label: 'Payroll Manager' }, { value: 'VIEWER', label: 'Viewer' },
];

export default function BusinessTeam() {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [inviteOpen, setInviteOpen] = useState(false);
  const [form, setForm] = useState({ email: '', role: '' });
  const [submitting, setSubmitting] = useState(false);

  const fetchMembers = async () => {
    setLoading(true);
    try { const { data } = await api.get('/business/members'); setMembers(data.data || []); } catch { setMembers([]); }
    setLoading(false);
  };

  useEffect(() => { fetchMembers(); }, []);

  const invite = async (e) => {
    e.preventDefault(); setSubmitting(true);
    try { await api.post('/business/members/invite', form); setInviteOpen(false); setForm({ email: '', role: '' }); fetchMembers(); } catch {}
    setSubmitting(false);
  };

  const revoke = async (id) => {
    if (!confirm('Revoke this member\'s access?')) return;
    try { await api.delete(`/business/members/${id}`); fetchMembers(); } catch {}
  };

  const columns = [
    { key: 'userName', label: 'Name' },
    { key: 'email', label: 'Email' },
    { key: 'role', label: 'Sub-Role' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
    { key: 'createdAt', label: 'Added', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
  ];

  return (
    <div>
      <div className={styles['page-header']}>
        <h1>Team Access</h1>
        <Button variant="primary" onClick={() => setInviteOpen(true)}>Invite Member</Button>
      </div>
      {loading ? <Spinner /> : members.length === 0 ? <EmptyState message="No team members" /> : (
        <Table columns={columns} data={members} actions={(row) => (
          <Button size="small" variant="danger" onClick={() => revoke(row.id)}>Revoke</Button>
        )} />
      )}
      <Modal isOpen={inviteOpen} onClose={() => setInviteOpen(false)} title="Invite Team Member">
        <form onSubmit={invite}>
          <FormInput label="Email" name="email" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
          <FormInput label="Role" name="role" value={form.role} onChange={e => setForm({ ...form, role: e.target.value })} options={ROLE_OPTIONS} />
          <Button type="submit" variant="primary" loading={submitting}>Send Invite</Button>
        </form>
      </Modal>
    </div>
  );
}
