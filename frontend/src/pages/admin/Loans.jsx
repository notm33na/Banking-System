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

export default function AdminLoans() {
  const [loans, setLoans] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [schedule, setSchedule] = useState([]);

  const fetchLoans = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/loans', { params: { page, size: 10 } });
      setLoans(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setLoans([]); }
    setLoading(false);
  }, [page]);

  useEffect(() => { fetchLoans(); }, [fetchLoans]);

  const openDetail = async (loan) => {
    setSelected(loan);
    if (loan.status === 'ACTIVE') {
      try {
        const { data } = await api.get(`/admin/loans/${loan.id}/schedule`);
        setSchedule(data.data || []);
      } catch { setSchedule([]); }
    } else { setSchedule([]); }
  };

  const makeDecision = async (decision) => {
    if (!selected) return;
    try { await api.put(`/admin/loans/${selected.id}/decision`, { decision }); setSelected(null); fetchLoans(); } catch {}
  };

  const fmt = (v) => v != null ? `$${Number(v).toLocaleString('en', { minimumFractionDigits: 2 })}` : '—';

  const columns = [
    { key: 'borrowerName', label: 'Applicant' },
    { key: 'amount', label: 'Amount', render: (v) => fmt(v) },
    { key: 'loanType', label: 'Type' },
    { key: 'termMonths', label: 'Term', render: (v) => `${v} mo` },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
    { key: 'createdAt', label: 'Applied', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
  ];

  const schedColumns = [
    { key: 'dueDate', label: 'Due Date', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
    { key: 'amount', label: 'Payment', render: (v) => fmt(v) },
    { key: 'principal', label: 'Principal', render: (v) => fmt(v) },
    { key: 'interest', label: 'Interest', render: (v) => fmt(v) },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
  ];

  return (
    <div>
      <div className={styles['page-header']}><h1>Loan Management</h1></div>
      {loading ? <Spinner /> : loans.length === 0 ? <EmptyState message="No loan applications" /> : (
        <>
          <Table columns={columns} data={loans} actions={(row) => (
            <Button size="small" variant="secondary" onClick={() => openDetail(row)}>Details</Button>
          )} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      <Modal isOpen={!!selected} onClose={() => setSelected(null)} title="Loan Details">
        {selected && (
          <>
            <div className={styles['detail-grid']}>
              <div className={styles['detail-item']}><label>Applicant</label><span>{selected.borrowerName}</span></div>
              <div className={styles['detail-item']}><label>Amount</label><span>{fmt(selected.amount)}</span></div>
              <div className={styles['detail-item']}><label>Rate</label><span>{selected.interestRate}%</span></div>
              <div className={styles['detail-item']}><label>Term</label><span>{selected.termMonths} months</span></div>
              <div className={styles['detail-item']}><label>Purpose</label><span>{selected.purpose}</span></div>
              <div className={styles['detail-item']}><label>Status</label><span><Badge status={selected.status} /></span></div>
            </div>
            {selected.status === 'PENDING' && (
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                <Button variant="success" onClick={() => makeDecision('APPROVED')}>Approve</Button>
                <Button variant="danger" onClick={() => makeDecision('REJECTED')}>Reject</Button>
              </div>
            )}
            {schedule.length > 0 && (
              <div style={{ marginTop: '1.5rem' }}>
                <h3 style={{ marginBottom: '0.75rem' }}>Repayment Schedule</h3>
                <Table columns={schedColumns} data={schedule} />
              </div>
            )}
          </>
        )}
      </Modal>
    </div>
  );
}
