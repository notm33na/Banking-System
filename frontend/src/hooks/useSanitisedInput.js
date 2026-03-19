import { useState, useCallback } from 'react';

/**
 * Hook that returns a sanitised input state + onChange handler.
 * Strips leading/trailing whitespace and encodes <, >, & characters.
 */
export default function useSanitisedInput(initialValue = '') {
  const [value, setValue] = useState(initialValue);

  const sanitise = (raw) => {
    if (typeof raw !== 'string') return raw;
    return raw
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  };

  const onChange = useCallback((e) => {
    const raw = e.target ? e.target.value : e;
    setValue(sanitise(raw));
  }, []);

  const trimmed = typeof value === 'string' ? value.trim() : value;

  return { value, trimmedValue: trimmed, onChange, setValue };
}
