const API_URL = 'http://localhost:8080/api/movimientos';
const state = { 
    movimientos: [], 
    bodegas: [], 
    productos: [], 
    productosDisponibles: [],
    usuarioId: null,
    usuarioNombre: null 
};

const headers = () => ({ "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("jwt_token")}` });
const formatFecha = f => f ? new Date(f).toISOString().slice(0, 16).replace('T', ' ') : "N/A";
const getEl = id => document.getElementById(id);
const getVal = id => getEl(id)?.value || null;

const redirigirLogin = () => {
    localStorage.removeItem("jwt_token");
    (window.parent && window.parent !== window ? window.parent : window).location.href = "/templates/login.html";
};

const fetchJSON = async (url, options = {}) => {
    try {
        const res = await fetch(url, { headers: headers(), ...options });
        if (res.status === 401 || res.status === 403) {
            alert("Sesión expirada. Por favor inicia sesión nuevamente.");
            redirigirLogin();
            return null;
        }
        if (!res.ok) throw new Error((await res.text()) || `Error ${res.status}`);
        const contentType = res.headers.get("content-type");
        return contentType?.includes("application/json") ? res.json() : res.text();
    } catch (error) {
        if (error.message.includes('401') || error.message.includes('403')) {
            alert("Sesión expirada. Por favor inicia sesión nuevamente.");
            redirigirLogin();
            return null;
        }
        throw error;
    }
};

const fetchUsuarioInfo = async () => {
    try {
        if (!localStorage.getItem('jwt_token')) {
            alert("Debes iniciar sesión");
            redirigirLogin();
            return null;
        }
        const data = await fetchJSON('http://localhost:8080/auth/userinfo');
        if (!data) return null;
        if (data.id) {
            state.usuarioId = data.id;
            state.usuarioNombre = data.nombre;
            return data;
        }
        throw new Error("No se pudo obtener la información del usuario");
    } catch (error) {
        alert("Error al obtener información del usuario: " + error.message);
        return null;
    }
};

const cargarProductosBodega = async (bodegaId) => {
    if (!bodegaId) {
        state.productosDisponibles = state.productos;
        actualizarSelectsProductos();
        return;
    }
    try {
        const res = await fetch(`http://localhost:8080/api/inventario/bodega/${bodegaId}`, { headers: headers() });
        
        // Si el endpoint no existe (404) o hay error, mostrar todos los productos
        if (res.status === 404 || !res.ok) {
            console.warn("Endpoint de inventario no disponible, mostrando todos los productos");
            state.productosDisponibles = state.productos;
            actualizarSelectsProductos();
            return;
        }
        
        const inventario = await res.json();
        console.log('Inventario recibido:', inventario); // Debug
        
        if (inventario && inventario.length > 0) {
            // El backend devuelve producto_id (JSON) que se convierte a productoId en JS
            state.productosDisponibles = state.productos.filter(p => 
                inventario.some(inv => {
                    // Probar ambas propiedades por si acaso
                    const invProductoId = inv.producto_id || inv.productoId;
                    const invStock = inv.stock || inv.cantidad || 0;
                    return invProductoId === p.id && invStock > 0;
                })
            );
            console.log('Productos disponibles:', state.productosDisponibles); // Debug
            actualizarSelectsProductos();
        } else {
            console.warn('No hay productos en el inventario de esta bodega');
            state.productosDisponibles = [];
            actualizarSelectsProductos();
        }
    } catch (e) {
        console.warn("Error al cargar inventario, mostrando todos los productos:", e.message);
        state.productosDisponibles = state.productos;
        actualizarSelectsProductos();
    }
};

const cargarDatos = async () => {
    try {
        const [bodegas, productos] = await Promise.all([
            fetchJSON('http://localhost:8080/api/bodega/listar').catch(() => []),
            fetchJSON('http://localhost:8080/api/producto/listar').catch(() => [])
        ]);
        if (bodegas) state.bodegas = bodegas;
        if (productos) {
            state.productos = productos;
            state.productosDisponibles = productos;
        }
        poblarSelects();
    } catch (e) { console.error("Error al cargar datos:", e); }
};

const crearOpts = (items, fn) => items.map(i => `<option value="${i.id}">${fn(i)}</option>`).join('');

const poblarSelects = () => {
    const opts = '<option value="">-- Seleccionar bodega --</option>' + crearOpts(state.bodegas, b => b.nombre || `Bodega ${b.id}`);
    ['bodegaOrigenId', 'bodegaDestinoId'].forEach(id => { 
        const s = getEl(id); 
        if (s) s.innerHTML = opts; 
    });
};

const actualizarSelectsProductos = () => {
    const opts = '<option value="">-- Producto --</option>' + crearOpts(state.productosDisponibles, p => p.nombre || `Producto ${p.id}`);
    document.querySelectorAll('.detalle-producto').forEach(select => {
        const valorActual = select.value;
        select.innerHTML = opts;
        if (valorActual && state.productosDisponibles.some(p => p.id == valorActual)) {
            select.value = valorActual;
        }
    });
};

const crearFila = () => {
    const row = document.createElement('div');
    row.className = 'detalle-row';
    row.style.cssText = 'display:flex;gap:10px;margin-bottom:10px;align-items:center';
    row.innerHTML = `
        <select class="detalle-producto" style="flex:2;padding:8px;border:1px solid #cbd5e0;border-radius:6px" required>
            <option value="">-- Producto --</option>${crearOpts(state.productosDisponibles, p => p.nombre || `Producto ${p.id}`)}
        </select>
        <input type="number" min="1" value="1" class="detalle-cantidad" style="flex:1;padding:8px;border:1px solid #cbd5e0;border-radius:6px" required>
        <button type="button" class="detalle-remove boton" style="padding:8px 12px;background:#e53e3e;color:white;border:none;border-radius:6px;cursor:pointer">Eliminar</button>
    `;
    row.querySelector('.detalle-remove').onclick = () => {
        const container = getEl('detallesRows');
        if (container?.children.length > 1) row.remove();
        else alert("Debe haber al menos un producto en el movimiento");
    };
    return row;
};

const agregarFila = () => { 
    const c = getEl('detallesRows'); 
    if (c) c.appendChild(crearFila()); 
};

const ajustarCampos = () => {
    const tipo = getVal('tipoMovimiento');
    const vis = { 'ENTRADA': ['none', 'block'], 'SALIDA': ['block', 'none'], 'TRANSFERENCIA': ['block', 'block'] };
    const [o, d] = vis[tipo] || ['block', 'block'];
    const rO = getEl('rowBodegaOrigen'), rD = getEl('rowBodegaDestino');
    if (rO) rO.style.display = o;
    if (rD) rD.style.display = d;
    
    if (tipo === 'ENTRADA') {
        const selectOrigen = getEl('bodegaOrigenId');
        if (selectOrigen) {
            selectOrigen.value = '';
            cargarProductosBodega(null);
        }
    } else if (tipo === 'SALIDA') {
        const selectDestino = getEl('bodegaDestinoId');
        if (selectDestino) selectDestino.value = '';
    } else if (tipo === 'TRANSFERENCIA') {
        const bodegaOrigen = getVal('bodegaOrigenId');
        if (bodegaOrigen) cargarProductosBodega(bodegaOrigen);
    }
};

const construirDTO = () => {
    const c = getEl('detallesRows');
    if (!c) { alert("Formulario no inicializado"); return null; }

    const dets = Array.from(c.querySelectorAll('.detalle-row')).map(f => ({
        productoId: Number(f.querySelector('.detalle-producto').value),
        cantidad: Number(f.querySelector('.detalle-cantidad').value)
    }));

    if (!dets.length) { alert("Agrega al menos un producto"); return null; }
    if (dets.some(d => !d.productoId)) { alert("Selecciona un producto en todas las filas"); return null; }
    if (dets.some(d => d.cantidad <= 0)) { alert("Ingresa una cantidad válida mayor a cero"); return null; }

    const tipo = getVal('tipoMovimiento');
    let orig = Number(getVal('bodegaOrigenId')) || null;
    let dest = Number(getVal('bodegaDestinoId')) || null;

    if (tipo === 'ENTRADA') { 
        orig = null; 
        if (!dest) { alert("Selecciona bodega destino para la entrada"); return null; }
    } else if (tipo === 'SALIDA') { 
        dest = null; 
        if (!orig) { alert("Selecciona bodega origen para la salida"); return null; }
    } else if (tipo === 'TRANSFERENCIA') { 
        if (!orig || !dest) { alert("Selecciona ambas bodegas para la transferencia"); return null; }
        if (orig === dest) { alert("Las bodegas de origen y destino no pueden ser iguales"); return null; }
    }

    if (!state.usuarioId) { alert("No se pudo identificar el usuario. Por favor, inicia sesión nuevamente."); redirigirLogin(); return null; }

    return { tipoMovimiento: tipo, usuarioId: state.usuarioId, bodegaOrigenId: orig, bodegaDestinoId: dest, detalles: dets };
};

const submitCrear = async () => {
    const dto = construirDTO();
    if (!dto) return;
    const btn = getEl('btnSubmitCrear');
    const txt = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Creando...';
    try {
        const data = await fetchJSON(`${API_URL}/crear`, { method: "POST", body: JSON.stringify(dto) });
        if (data) { 
            alert(`Movimiento creado exitosamente (ID: ${data.id || "s/n"})\n\nTipo: ${dto.tipoMovimiento}\nProductos: ${dto.detalles.length}\nEl inventario se ha actualizado correctamente.`); 
            cerrarModal(); 
            await cargarMovimientos(); 
        }
    } catch (e) { 
        alert(`Error al crear movimiento:\n\n${e.message}\n\nRevisa que haya stock suficiente y que los datos sean correctos.`); 
    } finally { 
        btn.disabled = false; 
        btn.textContent = txt; 
    }
};

const abrirModal = () => {
    poblarSelects();
    const dr = getEl('detallesRows');
    if (dr) { dr.innerHTML = ''; agregarFila(); }
    const ts = getEl('tipoMovimiento');
    if (ts) ts.value = 'ENTRADA';
    state.productosDisponibles = state.productos;
    ajustarCampos();
    getEl('modalCrearMovimiento').style.display = 'flex';
};

const cerrarModal = () => {
    getEl('modalCrearMovimiento').style.display = 'none';
    const dr = getEl('detallesRows');
    if (dr) dr.innerHTML = '';
    const ts = getEl('tipoMovimiento');
    if (ts) ts.value = 'ENTRADA';
    ['bodegaOrigenId', 'bodegaDestinoId'].forEach(id => {
        const s = getEl(id);
        if (s) s.value = '';
    });
};

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
    tbody.innerHTML = lista.length ? lista.map(crearFilaMov).join('') : 
        '<tr><td colspan="8" style="text-align:center;padding:2rem;color:#718096">No hay movimientos registrados</td></tr>';
};

const cargarMovimientos = async () => {
    try {
        const data = await fetchJSON(`${API_URL}/listar`);
        if (data) { state.movimientos = data; renderMovimientos(); }
    } catch (error) { 
        document.querySelector("#cuerpoTablaMovimientos").innerHTML = 
            '<tr><td colspan="8" style="text-align:center;padding:2rem;color:#e53e3e">Error al cargar movimientos</td></tr>'; 
    }
};

const filtrarRango = () => {
    const ini = getVal("fechaInicio"), fin = getVal("fechaFin");
    if (!ini || !fin) { alert("Selecciona ambas fechas para filtrar"); return; }
    const fIni = new Date(ini + "T00:00:00"), fFin = new Date(fin + "T23:59:59");
    if (fIni > fFin) { alert("La fecha inicial no puede ser mayor a la fecha final"); return; }
    const filtrados = state.movimientos.filter(m => { const f = new Date(m.fecha); return f >= fIni && f <= fFin; });
    renderMovimientos(filtrados);
    if (!filtrados.length) alert("No se encontraron movimientos en el rango de fechas seleccionado");
};

const limpiarFiltros = () => {
    ['fechaInicio', 'fechaFin'].forEach(id => {
        const el = getEl(id);
        if (el) el.value = '';
    });
    renderMovimientos();
};

document.addEventListener("DOMContentLoaded", async () => {
    if (!localStorage.getItem('jwt_token')) {
        alert("No hay sesión activa. Redirigiendo al login...");
        redirigirLogin();
        return;
    }
    
    const usuario = await fetchUsuarioInfo();
    if (!usuario) return;
    
    await cargarDatos();
    await cargarMovimientos();

    const evs = { 
        btnCrearMovimiento: abrirModal, 
        btnCancelarCrear: cerrarModal, 
        btnSubmitCrear: submitCrear, 
        btnAgregarDetalle: agregarFila, 
        tipoMovimiento: ajustarCampos, 
        btnFiltrar: filtrarRango
    };
    
    Object.entries(evs).forEach(([id, fn]) => {
        const el = getEl(id);
        if (el) el.addEventListener(id.includes('tipo') ? 'change' : 'click', fn);
    });
    
    const bodegaOrigenSelect = getEl('bodegaOrigenId');
    if (bodegaOrigenSelect) {
        bodegaOrigenSelect.addEventListener('change', (e) => {
            const bodegaId = e.target.value;
            cargarProductosBodega(bodegaId);
        });
    }
    
    ['fechaInicio', 'fechaFin'].forEach(id => {
        const el = getEl(id);
        if (el) el.addEventListener('keypress', e => { if (e.key === "Enter") filtrarRango(); });
    });
    
    const modal = getEl('modalCrearMovimiento');
    if (modal) modal.addEventListener('click', (e) => { if (e.target === modal) cerrarModal(); });
});