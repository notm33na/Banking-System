import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../api/axiosInstance';
import FormInput from '../../components/FormInput/FormInput';
import Button from '../../components/Button/Button';
import styles from './Auth.module.css';

const ROLE_OPTIONS = [
  { value: 'CUSTOMER', label: 'Customer' },
  { value: 'BUSINESS', label: 'Business' },
];

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', password: '', phone: '', userType: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.userType) { setError('Please select a role'); return; }
    setLoading(true);
    try {
      await api.post('/auth/register', form);
      navigate('/login?registered=true');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.logo}>VIRTBANK</h1>
        <p className={styles.subtitle}>Create your account</p>
        {error && <div className={styles.error}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 0.75rem' }}>
            <FormInput label="First Name" name="firstName" value={form.firstName} onChange={onChange} />
            <FormInput label="Last Name" name="lastName" value={form.lastName} onChange={onChange} />
          </div>
          <FormInput label="Email" name="email" type="email" value={form.email} onChange={onChange} />
          <FormInput label="Phone" name="phone" type="tel" value={form.phone} onChange={onChange} />
          <FormInput label="Password" name="password" type="password" value={form.password} onChange={onChange} />
          <FormInput label="Account Type" name="userType" value={form.userType} onChange={onChange} options={ROLE_OPTIONS} />
          <Button type="submit" variant="primary" loading={loading} style={{ width: '100%', marginTop: '0.5rem' }}>
            Create Account
          </Button>
        </form>
        <div className={styles.footer}>
          Already have an account? <Link to="/login">Sign In</Link>
        </div>
      </div>
    </div>
  );
}
