import React, { useEffect, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './style.css';

async function api(path, method = 'GET', signal) {
  const response = await fetch(`/web/finder${path}`, { method, signal, cache: 'no-store' });
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}));
    throw new Error(problem.detail || `Không thể thực hiện yêu cầu (HTTP ${response.status})`);
  }
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

function App() {
  const [rows, setRows] = useState([]);
  const [storeId, setStoreId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const [configuration, setConfiguration] = useState(null);
  const loadController = useRef(null);
  const actionRunning = useRef(false);

  async function refresh() {
    loadController.current?.abort();
    const controller = new AbortController();
    loadController.current = controller;
    try {
      const [data, config] = await Promise.all([
        api('/devices', 'GET', controller.signal),
        api('/configuration', 'GET', controller.signal),
      ]);
      if (!controller.signal.aborted) { setRows(data); setConfiguration(config); setError(''); }
    } catch (e) {
      if (!controller.signal.aborted) setError(e.message);
    } finally {
      if (!controller.signal.aborted) setLoading(false);
    }
  }

  useEffect(() => {
    let stopped = false;
    let timer;
    async function tick() {
      if (!actionRunning.current) await refresh();
      if (!stopped) timer = setTimeout(tick, 5000);
    }
    tick();
    return () => { stopped = true; clearTimeout(timer); loadController.current?.abort(); };
  }, []);

  async function command(row, stop) {
    if (actionRunning.current) return;
    actionRunning.current = true;
    setBusy(true); setError(''); setMessage('');
    loadController.current?.abort();
    try {
      await api(stop ? `/requests/${row.requestId}/stop` : `/devices/${row.deviceId}/find`, 'POST');
      setMessage(`${stop ? 'Đã gửi lệnh dừng' : 'Đã gửi yêu cầu tìm'}: ${row.deviceName}`);
      await refresh();
    } catch (e) {
      // Refresh after a conflict/lost response to show any request already created by the server.
      await refresh();
      setError(e.message);
    } finally {
      actionRunning.current = false; setBusy(false);
    }
  }

  async function removeDevice(row) {
    if (actionRunning.current || !window.confirm(
      `Xóa ${row.deviceName} (${row.deviceCode}) và lịch sử tìm của thiết bị? PDA cần đăng ký lại sau khi mở Home.`
    )) return;
    actionRunning.current = true;
    setBusy(true); setError(''); setMessage('');
    loadController.current?.abort();
    try {
      await api(`/devices/${row.deviceId}`, 'DELETE');
      setMessage(`Đã xóa ${row.deviceName}. Mở Home trên PDA để đăng ký lại.`);
      await refresh();
    } catch (e) {
      await refresh(); setError(e.message);
    } finally {
      actionRunning.current = false; setBusy(false);
    }
  }

  const stores = [...new Map(rows.map(row => [row.storeId, {
    id: row.storeId, code: row.storeCode, name: row.storeName,
  }])).values()];
  const devices = rows.filter(row => row.deviceId != null);

  return <main>
    <header><div><h1>Tìm thiết bị PDA</h1><p>{stores.length} cửa hàng · {devices.length} thiết bị</p></div>
      <button disabled={busy} onClick={refresh}>Làm mới</button></header>
    <label>Cửa hàng <select value={storeId} onChange={e => setStoreId(e.target.value)}>
      <option value="">Tất cả cửa hàng</option>
      {stores.map(store => <option key={store.id} value={store.id}>{store.code} — {store.name}</option>)}
    </select></label>
    <p className="hint">Trạng thái cập nhật mỗi 5 giây. SENT là đã gửi tới FCM; RINGING là PDA đã báo đang phát chuông.</p>
    {configuration && !configuration.schedulerEnabled && <p role="alert" className="error">
      Backend đang tắt scheduler: lệnh FCM nằm trong hàng đợi, chưa được gửi. Bật APP_SCHEDULER_ENABLED=true rồi khởi động lại backend.
    </p>}
    {configuration && !configuration.fcmEnabled && <p role="alert" className="error">
      Backend đang tắt FCM. PDA dùng FCM sẽ không nhận lệnh: cần Firebase Admin credential và FCM_ENABLED=true rồi khởi động lại backend.
      Chỉ PDA đã cấu hình polling mới có thể nhận qua HTTP.
    </p>}
    {error && <p role="alert" className="error">{error}</p>}
    {message && <p role="status" className="success">{message}</p>}
    {loading && <p>Đang tải thiết bị…</p>}
    {!loading && !stores.length && !error && <p>Chưa có cửa hàng trong hệ thống.</p>}
    {stores.filter(store => !storeId || String(store.id) === storeId).map(store => <section key={store.id}>
      <h2>{store.code} — {store.name}</h2>
      {!devices.some(d => d.storeId === store.id) ? <p>Chưa có thiết bị đăng ký.</p> :
        <div className="table-scroll"><table>
          <thead><tr><th>Thiết bị</th><th>Token FCM</th><th>Hoạt động gần nhất</th><th>Yêu cầu gần nhất</th><th>Thao tác</th></tr></thead>
          <tbody>{devices.filter(d => d.storeId === store.id).map(row => {
            const active = ['QUEUED', 'SENT', 'RINGING'].includes(row.status)
              && Date.parse(row.expiresAt) > Date.now();
            const status = !active && ['QUEUED', 'SENT', 'RINGING'].includes(row.status)
              ? 'Hết thời hạn' : row.status || 'Chưa tìm';
            return <tr key={row.deviceId}>
              <td><strong>{row.deviceName}</strong><small>{row.deviceCode}</small></td>
              <td>{row.hasPushToken ? 'Đã có token' : 'Chưa có token'}</td>
              <td>{row.lastActiveAt ? new Date(row.lastActiveAt).toLocaleString('vi-VN') : '—'}</td>
              <td><span className={row.status === 'RINGING' && active ? 'ringing' : ''}>{status}</span>
                {row.lastEvent && <small>Log: {row.lastEvent}{row.lastEventMessage ? ` — ${row.lastEventMessage}` : ''}</small>}</td>
              <td className="actions"><button disabled={busy || active} onClick={() => command(row, false)}>Tìm</button>
                <button disabled={busy || !active} onClick={() => command(row, true)}>Dừng</button>
                <button disabled={busy || active} onClick={() => removeDevice(row)}>Xóa</button></td>
            </tr>;
          })}</tbody>
        </table></div>}
    </section>)}
    <p className="hint">Đăng ký PDA sau khi đăng nhập app Android. Có token không có nghĩa thiết bị đang online.</p>
  </main>;
}

createRoot(document.getElementById('root')).render(<App />);
