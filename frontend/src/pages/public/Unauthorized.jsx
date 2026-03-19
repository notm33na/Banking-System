import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Button from '../../components/Button/Button';
import styles from './Auth.module.css';

const ROLE_HOME = {
  ROLE_ADMIN: '/admin',
  ROLE_CUSTOMER: '/customer',
  ROLE_BUSINESS: '/business',
};

export default function Unauthorized() {
  const { user } = useAuth();
  const home = ROLE_HOME[user?.role] || '/login';

  return (
    <div className={styles.page}>
      <div className={styles.card} style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🚫</div>
        <h2 style={{ marginBottom: '0.5rem' }}>Access Denied</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.9rem' }}>
          You do not have permission to view this page.
        </p>
        <Link to={home}>
          <Button variant="primary">Go Back</Button>
        </Link>
      </div>
    </div>
  );
}
