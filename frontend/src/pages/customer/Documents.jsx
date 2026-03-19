import { useState, useEffect } from 'react';
import api from '../../api/axiosInstance';
import Table from '../../components/Table/Table';
import Badge from '../../components/Badge/Badge';
import Button from '../../components/Button/Button';
import FormInput from '../../components/FormInput/FormInput';
import Spinner from '../../components/Spinner/Spinner';
import EmptyState from '../../components/EmptyState/EmptyState';
import styles from '../Page.module.css';

const DOC_TYPES = [
  { value: 'NATIONAL_ID', label: 'National ID' }, { value: 'PASSPORT', label: 'Passport' },
  { value: 'DRIVING_LICENSE', label: 'Driving License' }, { value: 'UTILITY_BILL', label: 'Utility Bill' },
];

export default function CustomerDocuments() {
  const [docs, setDocs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [file, setFile] = useState(null);
  const [docType, setDocType] = useState('');
  const [docNumber, setDocNumber] = useState('');
  const [kycStatus, setKycStatus] = useState(null);

  const fetchDocs = async () => {
    setLoading(true);
    try { const { data } = await api.get('/customer/documents'); setDocs(data.data || []); } catch { setDocs([]); }
    try { const { data } = await api.get('/customer/documents/kyc/status'); setKycStatus(data.data); } catch {}
    setLoading(false);
  };

  useEffect(() => { fetchDocs(); }, []);

  const upload = async () => {
    if (!file || !docType) return;
    setUploading(true);
    const fd = new FormData();
    fd.append('file', file);
    fd.append('documentType', docType);
    if (docNumber) fd.append('documentNumber', docNumber);
    try {
      await api.post('/customer/documents/kyc/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } });
      setFile(null); setDocType(''); setDocNumber('');
      fetchDocs();
    } catch {}
    setUploading(false);
  };

  const columns = [
    { key: 'documentType', label: 'Type' },
    { key: 'documentNumber', label: 'Doc #', render: (v) => v || '—' },
    { key: 'status', label: 'Status', render: (v) => <Badge status={v} /> },
    { key: 'createdAt', label: 'Uploaded', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
  ];

  return (
    <div>
      <div className={styles['page-header']}>
        <h1>Documents</h1>
        {kycStatus && <Badge status={kycStatus} />}
      </div>
      <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.25rem', marginBottom: '1.5rem', display: 'flex', gap: '0.75rem', alignItems: 'flex-end', flexWrap: 'wrap' }}>
        <FormInput label="Document Type" name="docType" value={docType} onChange={e => setDocType(e.target.value)} options={DOC_TYPES} />
        <FormInput label="Document Number" name="docNumber" value={docNumber} onChange={e => setDocNumber(e.target.value)} />
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '0.375rem' }}>File (PDF/JPG/PNG)</label>
          <input type="file" accept=".pdf,.jpg,.jpeg,.png" onChange={e => setFile(e.target.files[0])} />
        </div>
        <Button variant="primary" loading={uploading} onClick={upload} style={{ marginBottom: '1rem' }}>Upload</Button>
      </div>
      {loading ? <Spinner /> : docs.length === 0 ? <EmptyState message="No documents uploaded" /> : (
        <Table columns={columns} data={docs} />
      )}
    </div>
  );
}
