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

export default function AdminKyc() {
  const [docs, setDocs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [rejectModal, setRejectModal] = useState(null);
  const [reason, setReason] = useState('');

  const fetchDocs = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/admin/kyc', { params: { page, size: 10 } });
      setDocs(data.data?.content || []);
      setTotalPages(data.data?.totalPages || 0);
    } catch { setDocs([]); }
    setLoading(false);
  }, [page]);

  useEffect(() => { fetchDocs(); }, [fetchDocs]);

  const makeDecision = async (id, status, rejectionReason) => {
    try { await api.put(`/admin/kyc/${id}/decision`, { status, rejectionReason }); fetchDocs(); } catch {}
  };

  const handleReject = () => {
    if (rejectModal) { makeDecision(rejectModal.id, 'REJECTED', reason); setRejectModal(null); setReason(''); }
  };

  const columns = [
    { key: 'userName', label: 'User' },
    { key: 'documentType', label: 'Doc Type' },
    { key: 'documentNumber', label: 'Doc #' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
    { key: 'createdAt', label: 'Submitted', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
  ];

  return (
    <div>
      <div className={styles['page-header']}><h1>KYC / Compliance</h1></div>
      {loading ? <Spinner /> : docs.length === 0 ? <EmptyState message="No KYC submissions" /> : (
        <>
          <Table columns={columns} data={docs} actions={(row) => row.status === 'PENDING' ? (
            <>
              <Button size="small" variant="success" onClick={() => makeDecision(row.id, 'APPROVED', null)}>Approve</Button>
              <Button size="small" variant="danger" onClick={() => setRejectModal(row)}>Reject</Button>
            </>
          ) : null} />
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
      <Modal isOpen={!!rejectModal} onClose={() => setRejectModal(null)} title="Reject KYC Document">
        <FormInput label="Rejection Reason" name="reason" type="textarea" value={reason} onChange={e => setReason(e.target.value)} />
        <Button variant="danger" onClick={handleReject} style={{ marginTop: '0.5rem' }}>Confirm Reject</Button>
      </Modal>
    </div>
  );
}
