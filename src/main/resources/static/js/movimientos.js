const API_URL = 'http://localhost:8080/api/movimientos';
const state = { 
    movimientos: [], 
    bodegas: [], 
    productos: [], 
    usuarioId: null,
    usuarioNombre: null 
};

// ============= UTILIDADES =============
const headers = () => ({ 
    "Content-Type": "application/json", 
    "Authorization": `Bearer ${localStorage.getItem("jwt_token")}` 
});

const formatFecha = f => f ? new Date(f).toISOString().slice(0, 16).replace('T', ' ') : "N/A";
const getEl = id => document.getElementById(id);
const getVal = id => getEl(id)?.value || null;

const fetchJSON = async (url, options = {}) => {
    const res = await fetch(url, { headers: headers(), ...options });
    if (res.status === 401 || res.status === 403) {
        localStorage.removeItem("jwt_token");
        alert("Sesión expirada");
        setTimeout(() => window.location.href = "/templates/login.html", 1500);
        return null;
    }
    if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || `Error ${res.status}`);
    }
    return res.json();
};

// ============= OBTENER USUARIO AUTENTICADO =============
const fetchUsuarioInfo = async () => {
    try {
        const token = localStorage.getItem('jwt_token');
        if (!token) {
            alert("Debes iniciar sesión");
            window.location.href = "/templates/login.html";
            return null;
        }

        // Llamar al endpoint /auth/userinfo para obtener la información completa
        const data = await fetchJSON('http://localhost:8080/auth/userinfo');
        
        if (data && data.id) {
            state.usuarioId = data.id;
            state.usuarioNombre = data.nombre;
            console.log("Usuario autenticado:", state.usuarioNombre, "ID:", state.usuarioId);
            return data;
        } else {
            throw new Error("No se pudo obtener la información del usuario");
        }
    } catch (error) {
        console.error("Error al obtener usuario:", error);
        alert("Error al obtener información del usuario: " + error.message);
        return null;
    }
};

// ============= CARGAR DATOS =============
const cargarDatos = async () => {
    try {
        [state.bodegas, state.productos] = await Promise.all([
            fetchJSON('http://localhost:8080/api/bodega/listar').catch(() => []),
            fetchJSON('http://localhost:8080/api/producto/listar').catch(() => [])
        ]);
        poblarSelects();
        console.log("Datos cargados:", state.bodegas.length, "bodegas,", state.productos.length, "productos");
    } catch (e) { 
        console.error("Error al cargar datos:", e); 
    }
};

const crearOpts = (items, fn) => items.map(i => `<option value="${i.id}">${fn(i)}</option>`).join('');

const poblarSelects = () => {
    const opts = '<option value="">-- Seleccionar bodega --</option>' + crearOpts(state.bodegas, b => b.nombre || `Bodega ${b.id}`);
    ['bodegaOrigenId', 'bodegaDestinoId'].forEach(id => { 
        const s = getEl(id); 
        if (s) s.innerHTML = opts; 
    });
};

// ============= DETALLES =============
const crearFila = i => {
    const row = document.createElement('div');
    row.className = 'detalle-row';
    row.style.cssText = 'display:flex;gap:10px;margin-bottom:10px;align-items:center';
    row.innerHTML = `
        <select class="detalle-producto" style="flex:2;padding:8px;border:1px solid #cbd5e0;border-radius:6px" required>
            <option value="">-- Producto --</option>${crearOpts(state.productos, p => p.nombre || `Producto ${p.id}`)}
        </select>
        <input type="number" min="1" value="1" class="detalle-cantidad" style="flex:1;padding:8px;border:1px solid #cbd5e0;border-radius:6px" required>
        <button type="button" class="detalle-remove boton" style="padding:8px 12px;background:#e53e3e;color:white;border:none;border-radius:6px;cursor:pointer">Eliminar</button>
    `;
    row.querySelector('.detalle-remove').onclick = () => row.remove();
    return row;
};

const agregarFila = () => { 
    const c = getEl('detallesRows'); 
    if (c) c.appendChild(crearFila(c.children.length)); 
};

const ajustarCampos = () => {
    const tipo = getVal('tipoMovimiento');
    const vis = { 
        'ENTRADA': ['none', 'block'], 
        'SALIDA': ['block', 'none'], 
        'TRANSFERENCIA': ['block', 'block'] 
    };
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

    if (tipo === 'ENTRADA') { 
        orig = null; 
        if (!dest) return alert("Selecciona bodega destino"), null; 
    } else if (tipo === 'SALIDA') { 
        dest = null; 
        if (!orig) return alert("Selecciona bodega origen"), null; 
    } else { 
        if (!orig || !dest) return alert("Selecciona ambas bodegas"), null; 
        if (orig === dest) return alert("Bodegas no pueden ser iguales"), null; 
    }

    if (!state.usuarioId) {
        alert("No se pudo identificar el usuario. Por favor, inicia sesión nuevamente.");
        return null;
    }

    console.log("DTO construido con usuarioId:", state.usuarioId);

    return { 
        tipoMovimiento: tipo, 
        usuarioId: state.usuarioId, 
        bodegaOrigenId: orig, 
        bodegaDestinoId: dest, 
        detalles: dets 
    };
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
        console.log("Enviando movimiento:", JSON.stringify(dto, null, 2));
        
        const data = await fetchJSON(`${API_URL}/crear`, { 
            method: "POST", 
            body: JSON.stringify(dto) 
        });
        
        if (data) { 
            alert(`Movimiento creado exitosamente (ID: ${data.id || "s/n"})`); 
            cerrarModal(); 
            await cargarMovimientos(); 
        }
    } catch (e) { 
        console.error("Error al crear movimiento:", e);
        alert(`Error al crear movimiento: ${e.message}`); 
    } finally { 
        btn.disabled = false; 
        btn.textContent = txt; 
    }
};

// ============= MODAL =============
const abrirModal = () => {
    poblarSelects();
    const dr = getEl('detallesRows');
    if (dr) { 
        dr.innerHTML = ''; 
        agregarFila(); 
    }
    const ts = getEl('tipoMovimiento');
    if (ts) ts.value = 'ENTRADA';
    ajustarCampos();
    getEl('modalCrearMovimiento').style.display = 'flex';
};

const cerrarModal = () => {
    getEl('modalCrearMovimiento').style.display = 'none';
    const dr = getEl('detallesRows');
    if (dr) dr.innerHTML = '';
};

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
    tbody.innerHTML = lista.length 
        ? lista.map(crearFilaMov).join('') 
        : '<tr><td colspan="8" style="text-align:center;padding:2rem;color:#718096">No hay movimientos</td></tr>';
};

const cargarMovimientos = async () => {
    try {
        const data = await fetchJSON(`${API_URL}/listar`);
        if (data) { 
            state.movimientos = data; 
            renderMovimientos(); 
        }
    } catch (error) { 
        console.error("Error al cargar movimientos:", error);
        document.querySelector("#cuerpoTablaMovimientos").innerHTML = 
            '<tr><td colspan="8" style="text-align:center;padding:2rem;color:#e53e3e">Error al cargar movimientos</td></tr>'; 
    }
};

// ============= FILTRAR =============
const filtrarRango = () => {
    const ini = getVal("fechaInicio"), fin = getVal("fechaFin");
    if (!ini || !fin) return alert("Selecciona ambas fechas");
    const fIni = new Date(ini), fFin = new Date(fin);
    fFin.setHours(23, 59, 59);
    if (fIni > fFin) return alert("Fecha inicial no puede ser mayor");
    renderMovimientos(state.movimientos.filter(m => { 
        const f = new Date(m.fecha); 
        return f >= fIni && f <= fFin; 
    }));
};

// ============= INIT =============
document.addEventListener("DOMContentLoaded", async () => {
    console.log("Iniciando aplicación de movimientos...");
    
    // Primero obtener información del usuario
    const usuario = await fetchUsuarioInfo();
    if (!usuario) {
        console.error("No se pudo autenticar el usuario");
        return;
    }
    
    // Luego cargar el resto de datos
    await cargarDatos();
    await cargarMovimientos();

    // Configurar eventos
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
        if (el) {
            el.addEventListener(id.includes('tipo') ? 'change' : 'click', fn);
        }
    });
    
    ['fechaInicio', 'fechaFin'].forEach(id => 
        getEl(id)?.addEventListener('keypress', e => e.key === "Enter" && filtrarRango())
    );
    
    console.log("Aplicación iniciada. Usuario:", state.usuarioNombre);
});