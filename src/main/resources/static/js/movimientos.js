const API_URL = 'http://localhost:8080/api/movimientos';
const state = { movimientos: [], bodegas: [], productos: [], usuarioId: null };

// ============= UTILIDADES =============
const headers = () => ({ "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("jwt_token")}` });
const formatFecha = f => f ? new Date(f).toISOString().slice(0, 16).replace('T', ' ') : "N/A";
const getEl = id => document.getElementById(id);
const getVal = id => getEl(id)?.value || null;

const decodeJWT = token => {
    try {
        const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(decodeURIComponent(atob(payload).split('').map(c => `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`).join('')));
    } catch { return null; }
};

const fetchJSON = async (url, options = {}) => {
    const res = await fetch(url, { headers: headers(), ...options });
    if (res.status === 401 || res.status === 403) {
        localStorage.removeItem("jwt_token");
        alert("Sesión expirada");
        setTimeout(() => window.location.href = "/templates/login.html", 1500);
        return null;
    }
    if (!res.ok) throw new Error(`Error ${res.status}`);
    return res.json();
};

// ============= CARGAR DATOS =============
const fetchUsuarioId = async () => {
    try {
        const token = localStorage.getItem('jwt_token');
        if (!token) return alert("Debes iniciar sesión"), null;
        const payload = decodeJWT(token);
        const username = payload?.sub || payload?.username || payload?.user;
        if (!username) return null;
        const data = await fetchJSON(`/api/usuario/username/${encodeURIComponent(username)}`);
        return data?.id || null;
    } catch { return null; }
};

const cargarDatos = async () => {
    try {
        [state.bodegas, state.productos] = await Promise.all([
            fetchJSON('/api/bodega/listar').catch(() => []),
            fetchJSON('/api/producto/listar').catch(() => [])
        ]);
        poblarSelects();
    } catch (e) { console.error("Error:", e); }
};

const crearOpts = (items, fn) => items.map(i => `<option value="${i.id}">${fn(i)}</option>`).join('');

const poblarSelects = () => {
    const opts = '<option value="">-- Seleccionar bodega --</option>' + crearOpts(state.bodegas, b => b.nombre || `Bodega ${b.id}`);
    ['bodegaOrigenId', 'bodegaDestinoId'].forEach(id => { const s = getEl(id); if (s) s.innerHTML = opts; });
};

// ============= DETALLES =============
const crearFila = i => {
    const row = document.createElement('div');
    row.className = 'detalle-row';
    row.style.cssText = 'display:flex;gap:10px;margin-bottom:10px;align-items:center';
    row.innerHTML = `
        <select class="detalle-producto" style="flex:2;padding:8px;border:1px solid #cbd5e0;border-radius:6px">
            <option value="">-- Producto --</option>${crearOpts(state.productos, p => p.nombre || `Producto ${p.id}`)}
        </select>
        <input type="number" min="1" value="1" class="detalle-cantidad" style="flex:1;padding:8px;border:1px solid #cbd5e0;border-radius:6px">
        <button type="button" class="detalle-remove boton" style="padding:8px 12px;background:#e53e3e;color:white;border:none;border-radius:6px;cursor:pointer">Eliminar</button>
    `;
    row.querySelector('.detalle-remove').onclick = () => row.remove();
    return row;
};

const agregarFila = () => { const c = getEl('detallesRows'); if (c) c.appendChild(crearFila(c.children.length)); };

const ajustarCampos = () => {
    const tipo = getVal('tipoMovimiento');
    const vis = { 'ENTRADA': ['none', 'block'], 'SALIDA': ['block', 'none'], 'TRANSFERENCIA': ['block', 'block'] };
    const [o, d] = vis[tipo] || ['block', 'block'];
    const rO = getEl('rowBodegaOrigen'), rD = getEl('rowBodegaDestino');
    if (rO) rO.style.display = o;
    if (rD) rD.style.display = d;
};

// ============= DTO Y VALIDACIÓN =============
const construirDTO = () => {
    const c = getEl('detallesRows');
    if (!c) return alert("Formulario no inicializado"), null;

    const dets = Array.from(c.querySelectorAll('.detalle-row')).map(f => ({
        productoId: Number(f.querySelector('.detalle-producto').value),
        cantidad: Number(f.querySelector('.detalle-cantidad').value)
    }));

    if (!dets.length) return alert("Agrega al menos un producto"), null;
    if (dets.some(d => !d.productoId)) return alert("Selecciona un producto en todas las filas"), null;
    if (dets.some(d => d.cantidad <= 0)) return alert("Ingresa cantidad válida"), null;

    const tipo = getVal('tipoMovimiento');
    let orig = Number(getVal('bodegaOrigenId')) || null;
    let dest = Number(getVal('bodegaDestinoId')) || null;

    if (tipo === 'ENTRADA') { orig = null; if (!dest) return alert("Selecciona bodega destino"), null; }
    else if (tipo === 'SALIDA') { dest = null; if (!orig) return alert("Selecciona bodega origen"), null; }
    else { if (!orig || !dest) return alert("Selecciona ambas bodegas"), null; if (orig === dest) return alert("Bodegas no pueden ser iguales"), null; }

    if (!state.usuarioId) return alert("Usuario no determinado"), null;

    return { tipoMovimiento: tipo, usuarioId: state.usuarioId, bodegaOrigenId: orig, bodegaDestinoId: dest, detalles: dets };
};

// ============= CREAR =============
const submitCrear = async () => {
    const dto = construirDTO();
    if (!dto) return;
    const btn = getEl('btnSubmitCrear');
    const txt = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Creando...';
    try {
        const data = await fetchJSON(`${API_URL}/crear`, { method: "POST", body: JSON.stringify(dto) });
        if (data) { alert(`Movimiento creado (ID: ${data.id || "s/n"})`); cerrarModal(); cargarMovimientos(); }
    } catch (e) { alert(`Error: ${e.message}`); }
    finally { btn.disabled = false; btn.textContent = txt; }
};

// ============= MODAL =============
const abrirModal = () => {
    poblarSelects();
    const dr = getEl('detallesRows');
    if (dr) { dr.innerHTML = ''; agregarFila(); }
    const ts = getEl('tipoMovimiento');
    if (ts) ts.value = 'ENTRADA';
    ajustarCampos();
    getEl('modalCrearMovimiento').style.display = 'flex';
};
const cerrarModal = () => getEl('modalCrearMovimiento').style.display = 'none';

// ============= RENDERIZAR =============
const crearFilaMov = m => `<tr>
    <td>#MOV-${m.id}</td>
    <td>${formatFecha(m.fecha)}</td>
    <td><span class="badge ${(m.tipoMovimiento || "entrada").toLowerCase()}">${m.tipoMovimiento || "ENTRADA"}</span></td>
    <td>${m.producto || "N/A"}</td>
    <td>${m.cantidad || 0} unidades</td>
    <td>${m.bodegaOrigen || "N/A"}</td>
    <td>${m.bodegaDestino || "N/A"}</td>
    <td>${m.responsable || "N/A"}</td>
</tr>`;

const renderMovimientos = (lista = state.movimientos) => {
    const tbody = document.querySelector("#cuerpoTablaMovimientos");
    if (!tbody) return;
    tbody.innerHTML = lista.length ? lista.map(crearFilaMov).join('') : '<tr><td colspan="8" style="text-align:center;padding:2rem;color:#718096">No hay movimientos</td></tr>';
};

const cargarMovimientos = async () => {
    try {
        const data = await fetchJSON(`${API_URL}/listar`);
        if (data) { state.movimientos = data; renderMovimientos(); }
    } catch { document.querySelector("#cuerpoTablaMovimientos").innerHTML = '<tr><td colspan="8" style="text-align:center;padding:2rem;color:#e53e3e">Error al cargar</td></tr>'; }
};

// ============= FILTRAR =============
const filtrarRango = () => {
    const ini = getVal("fechaInicio"), fin = getVal("fechaFin");
    if (!ini || !fin) return alert("Selecciona ambas fechas");
    const fIni = new Date(ini), fFin = new Date(fin);
    fFin.setHours(23, 59, 59);
    if (fIni > fFin) return alert("Fecha inicial no puede ser mayor");
    renderMovimientos(state.movimientos.filter(m => { const f = new Date(m.fecha); return f >= fIni && f <= fFin; }));
};

// ============= INIT =============
document.addEventListener("DOMContentLoaded", async () => {
    await cargarDatos();
    state.usuarioId = await fetchUsuarioId();
    cargarMovimientos();

    const evs = { btnCrearMovimiento: abrirModal, btnCancelarCrear: cerrarModal, btnSubmitCrear: submitCrear, btnAgregarDetalle: agregarFila, tipoMovimiento: ajustarCampos, btnFiltrar: filtrarRango };
    Object.entries(evs).forEach(([id, fn]) => getEl(id)?.addEventListener(id.includes('tipo') ? 'change' : 'click', fn));
    ['fechaInicio', 'fechaFin'].forEach(id => getEl(id)?.addEventListener('keypress', e => e.key === "Enter" && filtrarRango()));
});