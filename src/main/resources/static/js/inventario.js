// ===== CONSTANTES Y CONFIGURACIÓN =====
const API_BASE_URL = "http://localhost:8080";
const TOKEN_KEY = "jwt_token";

// ===== INICIALIZACIÓN =====
document.addEventListener("DOMContentLoaded", async () => {
    await validarToken();
    await obtenerInfoUsuario();
    await cargarBodegas();
    await cargarInventario();
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
    
    getUserRole: () => sessionStorage.getItem("userRole"),
    
    formatearPrecio: (precio) => {
        return new Intl.NumberFormat('es-CO', {
            style: 'currency',
            currency: 'COP',
            minimumFractionDigits: 0
        }).format(precio);
    }
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
    const btnAgregar = document.getElementById("btnAgregarProducto");

    if (rol === "ADMIN") {
        subtitulo.textContent = "Gestión completa del inventario en todas las bodegas";
        if (btnAgregar) btnAgregar.style.display = "inline-flex";
    } else if (rol === "EMPLEADO") {
        subtitulo.textContent = `Bienvenido ${nombre}, visualiza el inventario de tus bodegas`;
        if (btnAgregar) btnAgregar.style.display = "none";
    }
}

// ===== CARGAR DATOS =====
async function cargarBodegas() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/bodega/listar`, {
            headers: Utils.authHeaders()
        });

        if (!response.ok) throw new Error("Error al cargar bodegas");

        const bodegas = await response.json();
        const select = document.getElementById("selectBodega");
        
        select.innerHTML = '<option value="">Todas las Bodegas</option>';
        
        bodegas.forEach(bodega => {
            const option = document.createElement("option");
            option.value = bodega.nombre;
            option.textContent = bodega.nombre;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error al cargar bodegas:", error);
    }
}

async function cargarInventario(nombreBodega = null) {
    try {
        const url = nombreBodega 
            ? `${API_BASE_URL}/api/inventario/detalle/bodega/${encodeURIComponent(nombreBodega)}`
            : `${API_BASE_URL}/api/inventario/detalle`;

        const response = await fetch(url, {
            headers: Utils.authHeaders()
        });

        if (response.status === 401 || response.status === 403) {
            return Utils.redirigirLogin();
        }

        if (!response.ok) {
            console.error("Error al cargar inventario:", await response.text());
            return;
        }

        const data = await response.json();
        mostrarInventario(data);
    } catch (error) {
        console.error("Error al cargar inventario:", error);
        Utils.mostrarMensaje("Error al cargar el inventario");
    }
}

// ===== RENDERIZADO UI =====
const Templates = {
    iconoProducto: `<svg width="24" height="24" fill="none" stroke="#4f46e5" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
    </svg>`,

    estadoVacio: () => `
        <div class="empty-state">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/>
            </svg>
            <h3>No hay productos en inventario</h3>
            <p>No se encontraron productos para mostrar</p>
        </div>
    `,

    productoItem: (item) => {
        const stockClass = item.stock < 10 ? 'stock-low' : item.stock < 50 ? 'stock-medium' : 'stock-high';
        
        return `
            <div class="product-item">
                <div class="product-info">
                    <div class="product-icon">
                        ${Templates.iconoProducto}
                    </div>
                    <div class="product-details">
                        <h4>${item.nombreProducto}</h4>
                        <p>Bodega: ${item.nombreBodega} · Categoría: ${item.categoriaProducto}</p>
                    </div>
                </div>
                <div class="product-meta">
                    <div class="product-price">
                        <span class="meta-label">Precio Venta</span>
                        <span class="meta-value">${Utils.formatearPrecio(item.precioVenta || 0)}</span>
                    </div>
                    <div class="product-price">
                        <span class="meta-label">Precio Compra</span>
                        <span class="meta-value">${Utils.formatearPrecio(item.precioCompra || 0)}</span>
                    </div>
                    <div class="product-stock">
                        <span class="meta-value ${stockClass}">${item.stock}</span>
                        <span class="stock-unit">Unidades</span>
                    </div>
                </div>
            </div>
        `;
    }
};

function mostrarInventario(inventario) {
    const contenedor = document.getElementById("productList");
    if (!contenedor) return console.error("Contenedor productList no encontrado");

    if (!inventario || inventario.length === 0) {
        contenedor.innerHTML = Templates.estadoVacio();
        return;
    }

    contenedor.innerHTML = inventario.map(item => Templates.productoItem(item)).join('');
}

// ===== EVENTOS =====
function inicializarEventos() {
    const selectBodega = document.getElementById("selectBodega");
    const btnBuscar = document.getElementById("btnBuscar");
    const inputBuscar = document.getElementById("buscarProducto");

    if (selectBodega) {
        selectBodega.addEventListener("change", (e) => {
            const nombreBodega = e.target.value;
            cargarInventario(nombreBodega || null);
        });
    }

    if (btnBuscar) {
        btnBuscar.addEventListener("click", buscarProducto);
    }

    if (inputBuscar) {
        inputBuscar.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                buscarProducto();
            }
        });
    }
}

function buscarProducto() {
    const input = document.getElementById("buscarProducto");
    const termino = input.value.toLowerCase().trim();
    
    if (!termino) {
        const selectBodega = document.getElementById("selectBodega");
        cargarInventario(selectBodega.value || null);
        return;
    }

    const items = document.querySelectorAll(".product-item");
    let encontrados = 0;

    items.forEach(item => {
        const texto = item.textContent.toLowerCase();
        if (texto.includes(termino)) {
            item.style.display = "flex";
            encontrados++;
        } else {
            item.style.display = "none";
        }
    });

    if (encontrados === 0) {
        const contenedor = document.getElementById("productList");
        contenedor.innerHTML = Templates.estadoVacio();
    }
}
