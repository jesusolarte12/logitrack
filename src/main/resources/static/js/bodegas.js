// ===== CONSTANTES Y CONFIGURACIÓN =====
const API_BASE_URL = "http://localhost:8080";
const TOKEN_KEY = "jwt_token";

// ===== INICIALIZACIÓN =====
document.addEventListener("DOMContentLoaded", async () => {
    await validarToken();
    await obtenerInfoUsuario();
    await cargarBodegas();
    inicializarEventos();
});

// ===== UTILIDADES =====
const Utils = {
    getToken: () => localStorage.getItem(TOKEN_KEY),
    
    removeToken: () => localStorage.removeItem(TOKEN_KEY),
    
    redirigirLogin: () => {
        Utils.removeToken();
        window.location.href = "login.html";
    },
    
    authHeaders: () => ({
        "Authorization": `Bearer ${Utils.getToken()}`,
        "Content-Type": "application/json"
    }),
    
    mostrarMensaje: (mensaje) => alert(mensaje),
    
    getUserRole: () => sessionStorage.getItem("userRole")
};

// ===== AUTENTICACIÓN =====
async function validarToken() {
    const token = Utils.getToken();
    if (!token) return Utils.redirigirLogin();

    try {
        const res = await fetch(`${API_BASE_URL}/auth/validate`, {
            headers: Utils.authHeaders()
        });
        if (!res.ok) Utils.redirigirLogin();
    } catch (err) {
        Utils.redirigirLogin();
    }
}

async function obtenerInfoUsuario() {
    if (!Utils.getToken()) return Utils.redirigirLogin();

    try {
        const res = await fetch(`${API_BASE_URL}/auth/userinfo`, {
            headers: Utils.authHeaders()
        });

        if (!res.ok) {
            console.error("No se pudo obtener info del usuario");
            return;
        }

        const usuario = await res.json();
        sessionStorage.setItem("userRole", usuario.rol);
        sessionStorage.setItem("userName", usuario.nombre);
        
        actualizarUISegunRol(usuario.rol, usuario.nombre);
    } catch (err) {
        console.error("Error al obtener info del usuario:", err);
    }
}

function actualizarUISegunRol(rol, nombre) {
    const subtitulo = document.getElementById("subtitulo");
    const btnNuevaBodega = document.getElementById("btnNuevaBodega");

    const config = {
        ADMIN: {
            texto: "Administración completa de todas las bodegas del sistema",
            mostrarBoton: true
        },
        EMPLEADO: {
            texto: `Bienvenido ${nombre}, estas son tus bodegas asignadas`,
            mostrarBoton: false
        }
    };

    const userConfig = config[rol];
    if (subtitulo) subtitulo.textContent = userConfig.texto;
    if (btnNuevaBodega) btnNuevaBodega.style.display = userConfig.mostrarBoton ? "inline-flex" : "none";
}

// ===== API BODEGAS =====
async function cargarBodegas() {
    if (!Utils.getToken()) return Utils.redirigirLogin();

    try {
        const response = await fetch(`${API_BASE_URL}/api/bodega/dashboard`, {
            headers: Utils.authHeaders()
        });

        if (response.status === 401 || response.status === 403) {
            return Utils.redirigirLogin();
        }

        if (!response.ok) {
            console.error("Error al cargar bodegas:", await response.text());
            return;
        }

        const data = await response.json();
        mostrarBodegas(data);
    } catch (error) {
        console.error("Error al cargar bodegas:", error);
    }
}

// ===== RENDERIZADO UI =====
const Templates = {
    iconoEditar: `<svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
    </svg>`,
    
    iconoEliminar: `<svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
    </svg>`,
    
    botonesAccion: () => `
        <div class="bodega-actions">
            <button class="btn-icon btn-edit" onclick="abrirModalEditarDesdeCard(this)" title="Editar bodega">
                ${Templates.iconoEditar}
            </button>
            <button class="btn-icon btn-delete" onclick="confirmarEliminacionDesdeCard(this)" title="Eliminar bodega">
                ${Templates.iconoEliminar}
            </button>
        </div>
    `,
    
    bodegaCard: (bodega, isAdmin) => `
        <div class="bodega-header">
            <div>
                <div class="bodega-name">${bodega.nombre}</div>
                <div class="bodega-location">${bodega.ubicacion}</div>
            </div>
            ${isAdmin ? Templates.botonesAccion() : ''}
        </div>
        <div class="bodega-stats">
            ${Templates.estadistica(bodega.totalProducto || 0, 'Total Productos')}
            ${Templates.estadistica((bodega.ocupacion || 0).toFixed(2) + '%', 'Ocupación')}
            ${Templates.estadistica(bodega.espacio || 0, 'Espacio Disponible')}
            ${Templates.estadistica(bodega.encargado || 'Sin asignar', 'Encargado')}
        </div>
    `,
    
    estadistica: (valor, etiqueta) => `
        <div class="bodega-stat">
            <div class="bodega-stat-value">${valor}</div>
            <div class="bodega-stat-label">${etiqueta}</div>
        </div>
    `
};

function mostrarBodegas(bodegas) {
    const contenedor = document.getElementById("bodegaGrid");
    if (!contenedor) return console.error("Contenedor bodegaGrid no encontrado");

    contenedor.innerHTML = "";
    const isAdmin = Utils.getUserRole() === "ADMIN";

    bodegas.forEach(bodega => {
        const card = document.createElement("div");
        card.classList.add("bodega-card");
        Object.assign(card.dataset, {
            bodegaId: bodega.id || 0,
            bodegaNombre: bodega.nombre || '',
            bodegaUbicacion: bodega.ubicacion || '',
            bodegaCapacidad: bodega.capacidad || 0
        });
        card.innerHTML = Templates.bodegaCard(bodega, isAdmin);
        contenedor.appendChild(card);
    });
}

// ===== GESTIÓN DE MODALES =====
const Modal = {
    elementos: {
        modal: null,
        form: null,
        titulo: null,
        btnGuardar: null
    },
    
    init() {
        this.elementos = {
            modal: document.getElementById("modalNuevaBodega"),
            form: document.getElementById("formNuevaBodega"),
            titulo: document.getElementById("tituloModal"),
            btnGuardar: document.getElementById("btnGuardar"),
            modalConfirmar: document.getElementById("modalConfirmarEliminar")
        };
    },
    
    abrir(elemento = this.elementos.modal) {
        elemento.style.display = "flex";
        elemento.classList.add("show");
    },
    
    cerrar(elemento = this.elementos.modal) {
        elemento.style.display = "none";
        elemento.classList.remove("show");
    },
    
    resetearFormulario() {
        this.elementos.form.reset();
        this.elementos.titulo.textContent = "Nueva Bodega";
        this.elementos.btnGuardar.textContent = "Guardar Bodega";
        document.getElementById("bodegaId").value = "";
    }
};

function inicializarEventos() {
    Modal.init();
    
    const eventos = [
        { id: "btnNuevaBodega", evento: "click", handler: abrirModalNuevaBodega },
        { id: "btnCerrarModal", evento: "click", handler: cerrarModal },
        { id: "btnCancelar", evento: "click", handler: cerrarModal },
        { id: "formNuevaBodega", evento: "submit", handler: guardarBodega }
    ];
    
    eventos.forEach(({ id, evento, handler }) => {
        const elemento = document.getElementById(id);
        if (elemento) elemento.addEventListener(evento, handler);
    });
    
    // Cerrar modal al hacer clic fuera
    if (Modal.elementos.modal) {
        Modal.elementos.modal.addEventListener("click", (e) => {
            if (e.target === Modal.elementos.modal) cerrarModal();
        });
    }
}

async function abrirModalNuevaBodega() {
    Modal.abrir();
    await cargarEncargadosDisponibles();
}

function cerrarModal() {
    Modal.cerrar();
    Modal.resetearFormulario();
}

async function cargarEncargadosDisponibles() {
    const select = document.getElementById("encargadoBodega");
    if (!select) return;

    try {
        const response = await fetch(`${API_BASE_URL}/api/bodega/encargados-disponibles`, {
            headers: Utils.authHeaders()
        });

        if (!response.ok) throw new Error("Error al cargar encargados");

        const usuarios = await response.json();
        select.innerHTML = '<option value="">Seleccione un encargado...</option>';
        
        usuarios.forEach(usuario => {
            const option = document.createElement("option");
            option.value = usuario.id;
            option.textContent = `${usuario.nombre} (${usuario.rol})`;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error al cargar encargados:", error);
        Utils.mostrarMensaje("Error al cargar la lista de encargados");
    }
}

// ===== CRUD BODEGAS =====
async function guardarBodega(event) {
    event.preventDefault();

    const bodegaId = document.getElementById("bodegaId").value;
    const formData = {
        nombre: document.getElementById("nombreBodega").value.trim(),
        ubicacion: document.getElementById("ubicacionBodega").value.trim(),
        capacidad: parseInt(document.getElementById("capacidadBodega").value),
        encargadoId: parseInt(document.getElementById("encargadoBodega").value)
    };

    if (!validarFormulario(formData)) {
        return Utils.mostrarMensaje("Por favor complete todos los campos");
    }

    try {
        const isUpdate = bodegaId && bodegaId !== "";
        const url = `${API_BASE_URL}/api/bodega/${isUpdate ? `update/${bodegaId}` : 'create'}`;
        
        const response = await fetch(url, {
            method: isUpdate ? "PUT" : "POST",
            headers: Utils.authHeaders(),
            body: JSON.stringify(formData)
        });

        if (!response.ok) throw new Error(await response.text());

        cerrarModal();
        await cargarBodegas();
        Utils.mostrarMensaje(`Bodega ${isUpdate ? 'actualizada' : 'creada'} exitosamente`);
    } catch (error) {
        console.error("Error al guardar bodega:", error);
        Utils.mostrarMensaje("Error al guardar la bodega: " + error.message);
    }
}

function validarFormulario(formData) {
    return formData.nombre && formData.ubicacion && formData.capacidad && formData.encargadoId;
}

// ===== EDICIÓN =====
function abrirModalEditarDesdeCard(button) {
    const card = button.closest('.bodega-card');
    abrirModalEditar(
        card.dataset.bodegaId,
        card.dataset.bodegaNombre,
        card.dataset.bodegaUbicacion,
        card.dataset.bodegaCapacidad
    );
}

async function abrirModalEditar(id, nombre, ubicacion, capacidad) {
    Modal.elementos.titulo.textContent = "Editar Bodega";
    Modal.elementos.btnGuardar.textContent = "Actualizar Bodega";
    
    document.getElementById("bodegaId").value = id;
    document.getElementById("nombreBodega").value = nombre;
    document.getElementById("ubicacionBodega").value = ubicacion;
    document.getElementById("capacidadBodega").value = capacidad;
    
    Modal.abrir();
    await cargarEncargadosDisponibles();
    await cargarEncargadoActual(id);
}

async function cargarEncargadoActual(bodegaId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/bodega/buscar/${bodegaId}`, {
            headers: Utils.authHeaders()
        });
        
        if (!response.ok) throw new Error("Error al cargar bodega");
        
        const bodega = await response.json();
        const selectEncargado = document.getElementById("encargadoBodega");
        
        if (bodega.encargado?.id && selectEncargado) {
            selectEncargado.value = bodega.encargado.id;
        }
    } catch (error) {
        console.error("Error al cargar encargado actual:", error);
    }
}

// ===== ELIMINACIÓN =====
let bodegaAEliminar = null;

function confirmarEliminacionDesdeCard(button) {
    const card = button.closest('.bodega-card');
    confirmarEliminacion(card.dataset.bodegaId, card.dataset.bodegaNombre);
}

function confirmarEliminacion(id, nombre) {
    bodegaAEliminar = { id, nombre };
    
    const mensaje = document.getElementById("mensajeConfirmacion");
    mensaje.innerHTML = `¿Está seguro que desea eliminar la bodega <strong>"${nombre}"</strong>?`;
    
    Modal.abrir(Modal.elementos.modalConfirmar);
    
    document.getElementById("btnConfirmarEliminar").onclick = eliminarBodega;
    document.getElementById("btnCancelarEliminar").onclick = cerrarModalConfirmacion;
    document.getElementById("btnCerrarConfirmar").onclick = cerrarModalConfirmacion;
}

function cerrarModalConfirmacion() {
    Modal.cerrar(Modal.elementos.modalConfirmar);
    bodegaAEliminar = null;
}

async function eliminarBodega() {
    if (!bodegaAEliminar) return;
    
    const nombreBodega = bodegaAEliminar.nombre; // Guardar nombre antes de limpiar
    
    try {
        const response = await fetch(`${API_BASE_URL}/api/bodega/delete/${bodegaAEliminar.id}`, {
            method: "DELETE",
            headers: Utils.authHeaders()
        });
        
        if (!response.ok) throw new Error("Error al eliminar la bodega");
        
        cerrarModalConfirmacion();
        await cargarBodegas();
        Utils.mostrarMensaje(`Bodega "${nombreBodega}" eliminada exitosamente`);
    } catch (error) {
        console.error("Error al eliminar bodega:", error);
        Utils.mostrarMensaje("Error al eliminar la bodega: " + error.message);
    }
}
