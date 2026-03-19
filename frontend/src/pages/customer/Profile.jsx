import { useState, useEffect } from 'react';
import api from '../../api/axiosInstance';
import FormInput from '../../components/FormInput/FormInput';
import Button from '../../components/Button/Button';
import Spinner from '../../components/Spinner/Spinner';
import styles from '../Page.module.css';

export default function CustomerProfile() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({});
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [pwSaving, setPwSaving] = useState(false);
  const [msg, setMsg] = useState('');
  const [pwMsg, setPwMsg] = useState('');

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await api.get('/customer/profile');
        const p = data.data || data;
        setProfile(p);
        setForm({ firstName: p.firstName || '', lastName: p.lastName || '', phone: p.phone || '',
          address: p.address || '', city: p.city || '', state: p.state || '', country: p.country || '' });
      } catch {}
      setLoading(false);
    };
    fetch();
  }, []);

  const updateProfile = async (e) => {
    e.preventDefault(); setSaving(true); setMsg('');
    try { await api.put('/customer/profile', form); setMsg('Profile updated!'); } catch { setMsg('Failed to update'); }
    setSaving(false);
  };

  const changePassword = async (e) => {
    e.preventDefault(); setPwSaving(true); setPwMsg('');
    if (pwForm.newPassword !== pwForm.confirmPassword) { setPwMsg('Passwords do not match'); setPwSaving(false); return; }
    try {
      await api.put('/customer/profile/password', { currentPassword: pwForm.currentPassword, newPassword: pwForm.newPassword });
      setPwMsg('Password changed!');
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) { setPwMsg(err.response?.data?.message || 'Failed'); }
    setPwSaving(false);
  };

  if (loading) return <Spinner />;

  const onFormChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });
  const onPwChange = (e) => setPwForm({ ...pwForm, [e.target.name]: e.target.value });

  return (
    <div>
      <div className={styles['page-header']}><h1>My Profile</h1></div>
      <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h3 style={{ marginBottom: '1rem' }}>Personal Information</h3>
        {msg && <div style={{ color: msg.includes('!') ? 'var(--success)' : 'var(--danger)', fontSize: '0.85rem', marginBottom: '0.75rem' }}>{msg}</div>}
        <form onSubmit={updateProfile}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 1rem' }}>
            <FormInput label="First Name" name="firstName" value={form.firstName} onChange={onFormChange} />
            <FormInput label="Last Name" name="lastName" value={form.lastName} onChange={onFormChange} />
            <FormInput label="Phone" name="phone" value={form.phone} onChange={onFormChange} />
            <FormInput label="Address" name="address" value={form.address} onChange={onFormChange} />
            <FormInput label="City" name="city" value={form.city} onChange={onFormChange} />
            <FormInput label="State" name="state" value={form.state} onChange={onFormChange} />
            <FormInput label="Country" name="country" value={form.country} onChange={onFormChange} />
          </div>
          <Button type="submit" variant="primary" loading={saving} style={{ marginTop: '0.5rem' }}>Save Changes</Button>
        </form>
      </div>
      <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem' }}>
        <h3 style={{ marginBottom: '1rem' }}>Change Password</h3>
        {pwMsg && <div style={{ color: pwMsg.includes('!') ? 'var(--success)' : 'var(--danger)', fontSize: '0.85rem', marginBottom: '0.75rem' }}>{pwMsg}</div>}
        <form onSubmit={changePassword}>
          <FormInput label="Current Password" name="currentPassword" type="password" value={pwForm.currentPassword} onChange={onPwChange} />
          <FormInput label="New Password" name="newPassword" type="password" value={pwForm.newPassword} onChange={onPwChange} />
          <FormInput label="Confirm Password" name="confirmPassword" type="password" value={pwForm.confirmPassword} onChange={onPwChange} />
          <Button type="submit" variant="primary" loading={pwSaving}>Change Password</Button>
        </form>
      </div>
    </div>
  );
}
