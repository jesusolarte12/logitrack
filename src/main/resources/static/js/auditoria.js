// Elementos
const $ = id => document.getElementById(id);
const modal = $('modal');
const tabla = $('tabla-auditoria');
const valorAntes = $('valor-antes');
const valorDespues = $('valor-despues');
const btnCerrar = $('btn-cerrar');

// Estado local
let auditorias = [];
let auditoriasOriginales = []; // *** NUEVO ***

// Helpers
const esc = str => String(str || '').replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
})[m]);

const tipoBadge = tipo => {
    switch ((tipo || '').toUpperCase()) {
        case 'INSERT': return 'creacion';
        case 'UPDATE': return 'modificacion';
        case 'DELETE': return 'eliminacion';
        default: return 'info';
    }
};

const parseValor = raw => {
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch (e) {
        return raw;
    }
};

const formatearValor = valor => {
    if (!valor) return '<span style="color:#6b7280;font-style:italic">Sin datos</span>';

    if (typeof valor === 'string')
        return '<pre style="white-space:pre-wrap;color:#e5e7eb">' + esc(valor) + '</pre>';

    return Object.entries(valor).map(([k, v]) =>
        `<div style="display:flex;gap:8px;margin-bottom:8px">
            <span style="color:#60a5fa;font-weight:600;min-width:120px">${esc(k)}:</span>
            <span style="color:#e5e7eb">${esc(String(v))}</span>
        </div>`
    ).join('');
};

// Renderizar tabla
const renderTabla = () => {
    if (!tabla) return;
    tabla.innerHTML = auditorias.map((item, i) => `
        <tr>
            <td>${esc(item.fechaHora)}</td>
            <td>${esc(item.usuarioNombre || ('id:' + (item.usuarioId || 'n/a')))}</td>
            <td><span class="badge badge-${tipoBadge(item.tipoOperacion)}">${esc(item.tipoOperacion)}</span></td>
            <td>${esc((item.entidad || '').toUpperCase())}</td>
            <td>${esc('id:' + (item.registroId || 'n/a'))}</td>
            <td style="text-align:right"><button class="btn-secondary" data-idx="${i}">Ver más</button></td>
        </tr>
    `).join('');
};

// Modal
const abrirModal = item => {
    valorAntes.innerHTML = formatearValor(parseValor(item.valorAntes));
    valorDespues.innerHTML = formatearValor(parseValor(item.valorDespues));
    modal.style.display = 'flex';
};

const cerrarModal = () => modal.style.display = 'none';

// Eventos modal
tabla.addEventListener('click', e => {
    const btn = e.target.closest('[data-idx]');
    if (btn) abrirModal(auditorias[btn.dataset.idx]);
});

btnCerrar.addEventListener('click', cerrarModal);
modal.addEventListener('click', e => { if (e.target === modal) cerrarModal(); });
document.addEventListener('keydown', e => { if (e.key === 'Escape') cerrarModal(); });

// *** FILTRO POR FECHA ***
function filtrarPorFecha(auditorias, fechaInicio, fechaFin) {
    const inicio = new Date(fechaInicio + " 00:00:00");
    const fin = new Date(fechaFin + " 23:59:59");

    return auditorias.filter(item => {
        const fechaItem = new Date(item.fechaHora);
        return fechaItem >= inicio && fechaItem <= fin;
    });
}

document.getElementById("btnFiltrarAuditoria").addEventListener("click", () => {
    const inicio = document.getElementById("filtroFechaInicio").value;
    const fin = document.getElementById("filtroFechaFin").value;

    if (!inicio || !fin) {
        auditorias = [...auditoriasOriginales];
        renderTabla();
        return;
    }

    auditorias = filtrarPorFecha(auditoriasOriginales, inicio, fin);
    renderTabla();
});

// Cargar auditorías
async function loadAuditorias() {
    try {
        const token = localStorage.getItem('jwt_token');
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers['Authorization'] = 'Bearer ' + token;

        const res = await fetch('/api/auditoria/listar', { headers });

        if (!res.ok) {
            tabla.innerHTML = `<tr><td colspan="6">Error cargando auditorías (status ${res.status})</td></tr>`;
            return;
        }

        const data = await res.json();

        auditorias = data.map(d => ({
            id: d.id,
            fechaHora: d.fechaHora || d.fecha || d.fechaHoraString,
            tipoOperacion: d.tipoOperacion,
            usuarioId: d.usuarioId,
            usuarioNombre: d.usuarioNombre,
            entidad: d.entidad,
            registroId: d.registroId,
            valorAntes: d.valorAntes,
            valorDespues: d.valorDespues
        }));

        auditoriasOriginales = [...auditorias]; // *** Copy original dataset ***
        renderTabla();

    } catch (err) {
        tabla.innerHTML = '<tr><td colspan="6">Error de red al cargar auditorías</td></tr>';
    }
}

// INIT
loadAuditorias();
