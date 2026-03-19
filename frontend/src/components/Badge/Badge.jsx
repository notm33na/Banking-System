import styles from './Badge.module.css';

const COLOR_MAP = {
  ACTIVE: 'green', APPROVED: 'green', PAID: 'green', COMPLETED: 'green', RESOLVED: 'green',
  SUSPENDED: 'red', REJECTED: 'red', OVERDUE: 'red', FAILED: 'red', FLAGGED: 'red',
  PENDING: 'yellow', IN_PROGRESS: 'yellow', PROCESSING: 'yellow', DRAFT: 'yellow',
  INACTIVE: 'gray', CLOSED: 'gray', CANCELLED: 'gray',
  OPEN: 'blue', SENT: 'blue',
};

export default function Badge({ status }) {
  const variant = COLOR_MAP[status?.toUpperCase()] || 'gray';
  return <span className={`${styles.badge} ${styles[variant]}`}>{status}</span>;
}
