import styles from './Spinner.module.css';

export default function Spinner() {
  return (
    <div className={styles['spinner-wrapper']}>
      <div className={styles.spinner} />
    </div>
  );
}
