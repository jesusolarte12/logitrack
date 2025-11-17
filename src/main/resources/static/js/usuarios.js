// ===== CONSTANTES Y CONFIGURACIÓN =====

const TOKEN_KEY = "jwt_token";

// ===== VARIABLES GLOBALES =====
let usuarioEnEdicion = null;
let documentoAEliminar = null;

// ===== INICIALIZACIÓN =====
document.addEventListener("DOMContentLoaded", async () => {
    await validarToken();
    await cargarUsuarios();
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
    
    mostrarMensaje: (mensaje, tipo = "info") => {
        alert(mensaje);
    }
};

// ===== AUTENTICACIÓN =====
async function validarToken() {
    const token = Utils.getToken();
    if (!token) return Utils.redirigirLogin();

    try {
        const res = await fetch(`${window.API_CONFIG.API_BASE}/auth/validate`, {
            headers: Utils.authHeaders()
        });
        if (!res.ok) Utils.redirigirLogin();
    } catch (err) {
        Utils.redirigirLogin();
    }
}

// ===== EVENTOS =====
function inicializarEventos() {
    // Botón nuevo usuario
    document.getElementById("btnNuevoUsuario").addEventListener("click", abrirModalNuevo);
    
    // Botones del modal
    document.getElementById("btnCerrarModal").addEventListener("click", cerrarModal);
    document.getElementById("btnCancelar").addEventListener("click", cerrarModal);
    
    // Formulario
    document.getElementById("formUsuario").addEventListener("submit", guardarUsuario);
    
    // Búsqueda
    document.getElementById("btnBuscar").addEventListener("click", buscarPorDocumento);
    document.getElementById("btnLimpiarBusqueda").addEventListener("click", limpiarBusqueda);
    document.getElementById("buscarDocumento").addEventListener("keypress", (e) => {
        if (e.key === "Enter") buscarPorDocumento();
    });
    
    // Modal de confirmación
    document.getElementById("btnCerrarConfirmar").addEventListener("click", cerrarModalConfirmar);
    document.getElementById("btnCancelarEliminar").addEventListener("click", cerrarModalConfirmar);
    document.getElementById("btnConfirmarEliminar").addEventListener("click", confirmarEliminar);
    
    // Cerrar modales al hacer clic fuera
    window.addEventListener("click", (e) => {
        const modalUsuario = document.getElementById("modalUsuario");
        const modalConfirmar = document.getElementById("modalConfirmarEliminar");
        if (e.target === modalUsuario) cerrarModal();
        if (e.target === modalConfirmar) cerrarModalConfirmar();
    });
}

// ===== API - LISTAR USUARIOS =====
async function cargarUsuarios() {
    try {
        const response = await fetch(`${window.API_CONFIG.API_BASE}/api/usuario/listar`, {
            headers: Utils.authHeaders()
        });

        if (!response.ok) {
            throw new Error("Error al cargar usuarios");
        }

        const usuarios = await response.json();
        renderizarTabla(usuarios);
    } catch (error) {
        console.error("Error:", error);
        Utils.mostrarMensaje("Error al cargar la lista de usuarios");
    }
}

// ===== RENDERIZAR TABLA =====
function renderizarTabla(usuarios) {
    const tbody = document.getElementById("tablaUsuarios");
    
    if (usuarios.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 40px; color: #6b7280;">
                    <svg width="48" height="48" fill="none" stroke="currentColor" viewBox="0 0 24 24" style="margin: 0 auto 16px;">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
                    </svg>
                    <p style="font-size: 16px; font-weight: 600; margin-bottom: 8px;">No hay usuarios registrados</p>
                    <p style="font-size: 14px;">Comienza agregando un nuevo usuario</p>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = usuarios.map(usuario => `
        <tr>
            <td>${usuario.username}</td>
            <td>${usuario.nombre}</td>
            <td>${usuario.cargo}</td>
            <td>${usuario.documento}</td>
            <td>${usuario.email}</td>
            <td>
                <span class="badge ${usuario.rol === 'ADMIN' ? 'badge-admin' : 'badge-user'}">
                    ${usuario.rol}
                </span>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn-action btn-edit" onclick="editarUsuario('${usuario.documento}')">
                        <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                        </svg>
                        Editar
                    </button>
                    <button class="btn-action btn-delete" onclick="eliminarUsuario('${usuario.documento}')">
                        <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                        </svg>
                        Eliminar
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// ===== BUSCAR POR DOCUMENTO =====
async function buscarPorDocumento() {
    const documento = document.getElementById("buscarDocumento").value.trim();
    
    if (!documento) {
        Utils.mostrarMensaje("Por favor ingrese un documento");
        return;
    }

    try {
        const response = await fetch(`${window.API_CONFIG.API_BASE}/api/usuario/buscar/${documento}`, {
            headers: Utils.authHeaders()
        });

        if (!response.ok) {
            if (response.status === 404) {
                Utils.mostrarMensaje("Usuario no encontrado");
                return;
            }
            throw new Error("Error al buscar usuario");
        }

        const usuario = await response.json();
        renderizarTabla([usuario]);
    } catch (error) {
        console.error("Error:", error);
        Utils.mostrarMensaje("Error al buscar usuario");
    }
}

// ===== LIMPIAR BÚSQUEDA =====
function limpiarBusqueda() {
    document.getElementById("buscarDocumento").value = "";
    cargarUsuarios();
}

// ===== MODAL - NUEVO USUARIO =====
function abrirModalNuevo() {
    usuarioEnEdicion = null;
    document.getElementById("tituloModal").textContent = "Agregar Usuario";
    document.getElementById("formUsuario").reset();
    document.getElementById("documentoOriginal").value = "";
    document.getElementById("password").required = true;
    document.getElementById("modalUsuario").classList.add("show");
}

// ===== MODAL - EDITAR USUARIO =====
async function editarUsuario(documento) {
    try {
        const response = await fetch(`${window.API_CONFIG.API_BASE}/api/usuario/buscar/${documento}`, {
            headers: Utils.authHeaders()
        });

        if (!response.ok) {
            throw new Error("Error al obtener usuario");
        }

        const usuario = await response.json();
        usuarioEnEdicion = usuario;
        
        document.getElementById("tituloModal").textContent = "Editar Usuario";
        document.getElementById("documentoOriginal").value = usuario.documento;
        document.getElementById("username").value = usuario.username;
        document.getElementById("nombre").value = usuario.nombre;
        document.getElementById("cargo").value = usuario.cargo;
        document.getElementById("documento").value = usuario.documento;
        document.getElementById("email").value = usuario.email;
        document.getElementById("rol").value = usuario.rol;
        document.getElementById("password").value = "";
        document.getElementById("password").required = false;
        document.getElementById("password").placeholder = "Dejar vacío para mantener la actual";
        
        document.getElementById("modalUsuario").classList.add("show");
    } catch (error) {
        console.error("Error:", error);
        Utils.mostrarMensaje("Error al cargar datos del usuario");
    }
}

// ===== GUARDAR USUARIO =====
async function guardarUsuario(e) {
    e.preventDefault();
    
    const formData = {
        username: document.getElementById("username").value.trim(),
        nombre: document.getElementById("nombre").value.trim(),
        cargo: document.getElementById("cargo").value.trim(),
        documento: document.getElementById("documento").value.trim(),
        email: document.getElementById("email").value.trim(),
        rol: document.getElementById("rol").value
    };

    const password = document.getElementById("password").value;
    if (password) {
        formData.password = password;
    }

    try {
        let response;
        
        if (usuarioEnEdicion) {
            // Actualizar usuario existente
            const documentoOriginal = document.getElementById("documentoOriginal").value;
            response = await fetch(`${window.API_CONFIG.API_BASE}/api/usuario/actualizar/${documentoOriginal}`, {
                method: "PATCH",
                headers: Utils.authHeaders(),
                body: JSON.stringify(formData)
            });
        } else {
            // Crear nuevo usuario
            response = await fetch(`${window.API_CONFIG.API_BASE}/api/usuario/crear`, {
                method: "POST",
                headers: Utils.authHeaders(),
                body: JSON.stringify(formData)
            });
        }

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || "Error al guardar usuario");
        }

        Utils.mostrarMensaje(
            usuarioEnEdicion ? "Usuario actualizado correctamente" : "Usuario creado correctamente"
        );
        
        cerrarModal();
        await cargarUsuarios();
        
    } catch (error) {
        console.error("Error:", error);
        Utils.mostrarMensaje("Error al guardar: " + error.message);
    }
}

// ===== ELIMINAR USUARIO =====
function eliminarUsuario(documento) {
    documentoAEliminar = documento;
    document.getElementById("modalConfirmarEliminar").classList.add("show");
}

async function confirmarEliminar() {
    if (!documentoAEliminar) return;
    
    try {
        const response = await fetch(`${window.API_CONFIG.API_BASE}/api/usuario/eliminar/${documentoAEliminar}`, {
            method: "DELETE",
            headers: Utils.authHeaders()
        });

        if (!response.ok) {
            throw new Error("Error al eliminar usuario");
        }

        Utils.mostrarMensaje("Usuario eliminado correctamente");
        cerrarModalConfirmar();
        await cargarUsuarios();
        
    } catch (error) {
        console.error("Error:", error);
        Utils.mostrarMensaje("Error al eliminar usuario");
    }
}

// ===== CERRAR MODALES =====
function cerrarModal() {
    document.getElementById("modalUsuario").classList.remove("show");
    document.getElementById("formUsuario").reset();
    usuarioEnEdicion = null;
}

function cerrarModalConfirmar() {
    document.getElementById("modalConfirmarEliminar").classList.remove("show");
    documentoAEliminar = null;
}
