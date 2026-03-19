import styles from './FormInput.module.css';

export default function FormInput({
  label, name, type = 'text', value, onChange, error, options, ...rest
}) {
  const Tag = type === 'textarea' ? 'textarea' : options ? 'select' : 'input';

  return (
    <div className={styles.group}>
      {label && <label className={styles.label} htmlFor={name}>{label}</label>}
      <Tag
        id={name}
        name={name}
        type={Tag === 'input' ? type : undefined}
        value={value}
        onChange={onChange}
        className={`${styles.input} ${error ? styles.error : ''}`}
        {...rest}
      >
        {options && (
          <>
            <option value="">Select...</option>
            {options.map((opt) => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </>
        )}
      </Tag>
      {error && <span className={styles.errorMsg}>{error}</span>}
    </div>
  );
}
