import { useState } from 'react';

const MAX = 1000;

/** Text box + Send button. Enter sends, Shift+Enter adds a new line. */
export default function MessageComposer({ onSend, disabled, placeholder = 'Type a message…' }) {
  const [text, setText] = useState('');

  const submit = (e) => {
    e?.preventDefault();
    const content = text.trim();
    if (!content || disabled) return;
    if (onSend(content) !== false) setText('');
  };

  const onKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) submit(e);
  };

  return (
    <form className="composer" onSubmit={submit}>
      <textarea
        rows={1}
        value={text}
        maxLength={MAX}
        placeholder={disabled ? 'Connecting…' : placeholder}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={onKeyDown}
        disabled={disabled}
        aria-label="Message"
      />
      {text.length > MAX - 100 && <span className="counter">{MAX - text.length}</span>}
      <button className="btn btn-primary" type="submit" disabled={disabled || !text.trim()}>
        Send
      </button>
    </form>
  );
}
