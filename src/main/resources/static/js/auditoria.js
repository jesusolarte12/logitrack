// Elementos
const $ = id => document.getElementById(id);
const modal = $('modal');
const tabla = $('tabla-auditoria');
const valorAntes = $('valor-antes');
const valorDespues = $('valor-despues');
const btnCerrar = $('btn-cerrar');

// Estado local
let auditorias = [];

// Helpers
const esc = str => String(str || '').replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
})[m]);

const tipoBadge = tipo => {
    // Mapea tipoOperacion a una clase de badge
    switch ((tipo || '').toUpperCase()) {
        case 'INSERT': return 'creacion';
        case 'UPDATE': return 'modificacion';
        case 'DELETE': return 'eliminacion';
        default: return 'info';
    }
};

const parseValor = raw => {
    if (!raw) return null;
    // valorAntes/valorDespues vienen como String (serializados). Intentamos parsear JSON.
    try {
        return JSON.parse(raw);
    } catch (e) {
        // Si no es JSON válido, devolvemos el texto crudo
        return raw;
    }
};

const formatearValor = valor => {
    if (!valor) return '<span style="color:#6b7280;font-style:italic">Sin datos</span>';

    // Si es string (no JSON), mostrar como preformateado
    if (typeof valor === 'string') return '<pre style="white-space:pre-wrap;color:#e5e7eb">' + esc(valor) + '</pre>';

    // Si es objeto, iterar entradas
    return Object.entries(valor).map(function (entry) {
        var k = entry[0];
        var v = entry[1];
        return '<div style="display:flex;gap:8px;margin-bottom:8px">'
            + '<span style="color:#60a5fa;font-weight:600;min-width:120px">' + esc(k) + ':</span>'
            + '<span style="color:#e5e7eb">' + esc(String(v)) + '</span>'
            + '</div>';
    }).join('');
};

// Renderizar tabla
const renderTabla = function () {
    if (!tabla) return;
    tabla.innerHTML = auditorias.map(function (item, i) {
        return '<tr>'
            + '<td>' + esc(item.fechaHora) + '</td>'
            + '<td>' + esc(item.usuarioNombre || ('id:' + (item.usuarioId || 'n/a'))) + '</td>'
            + '<td><span class="badge badge-' + tipoBadge(item.tipoOperacion) + '">' + esc(item.tipoOperacion) + '</span></td>'
            + '<td>' + esc((item.entidad || '').toUpperCase()) + '</td>'
            + '<td>' + esc('id:' + (item.registroId || 'n/a')) + '</td>'
            + '<td style="text-align:right"><button class="btn-secondary" data-idx="' + i + '">Ver más</button></td>'
            + '</tr>';
    }).join('');
};

// Modal
const abrirModal = function (item) {
    var antes = parseValor(item.valorAntes);
    var despues = parseValor(item.valorDespues);
    valorAntes.innerHTML = formatearValor(antes);
    valorDespues.innerHTML = formatearValor(despues);
    modal.style.display = 'flex';
};

const cerrarModal = function () {
    modal.style.display = 'none';
};

// Eventos
tabla.addEventListener('click', function (e) {
    var btn = e.target.closest('[data-idx]');
    if (btn) abrirModal(auditorias[btn.dataset.idx]);
});

btnCerrar.addEventListener('click', cerrarModal);
modal.addEventListener('click', function (e) { if (e.target === modal) cerrarModal(); });
document.addEventListener('keydown', function (e) { if (e.key === 'Escape') cerrarModal(); });

// Cargar auditorías desde el backend
async function loadAuditorias() {
    try {
        var token = localStorage.getItem('jwt_token');
        var headers = { 'Content-Type': 'application/json' };
        if (token) headers['Authorization'] = 'Bearer ' + token;

        var res = await fetch('/api/auditoria/listar', { headers: headers });
        if (!res.ok) {
            console.error('Error cargando auditorías', res.status, await res.text());
            tabla.innerHTML = '<tr><td colspan="6">Error cargando auditorías (status ' + res.status + ')</td></tr>';
            return;
        }

        var data = await res.json();

        // Normalizar el formato esperado por el UI
        auditorias = data.map(function (d) {
            return {
                id: d.id,
                fechaHora: d.fechaHora || d.fecha || d.fechaHoraString,
                tipoOperacion: d.tipoOperacion,
                usuarioId: d.usuarioId,
                usuarioNombre: d.usuarioNombre,
                entidad: d.entidad,
                registroId: d.registroId,
                valorAntes: d.valorAntes,
                valorDespues: d.valorDespues
            };
        });

        renderTabla();
        console.log('Auditoría cargada', auditorias.length);
    } catch (err) {
        console.error('Error de red al cargar auditorías', err);
        tabla.innerHTML = '<tr><td colspan="6">Error de red al cargar auditorías</td></tr>';
    }
}

// Init
loadAuditorias();