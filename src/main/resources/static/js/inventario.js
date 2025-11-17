// ===== CONSTANTES Y CONFIGURACIÓN =====

const TOKEN_KEY = "jwt_token";

// ===== INICIALIZACIÓN =====
document.addEventListener("DOMContentLoaded", async () => {
    await validarToken();
    await obtenerInfoUsuario();
    await cargarBodegas();
    await cargarInventario();
    inicializarEventos();
    
    // Escuchar mensajes desde el dashboard principal
    window.addEventListener('message', async function(event) {
        if (event.data && event.data.type === 'recargarDatos') {
            console.log('Recargando datos de inventario desde mensaje del dashboard');
            await cargarBodegas();
            await cargarInventario();
        }
    });
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
        const res = await fetch(`${window.API_CONFIG.API_BASE}/auth/validate`, {
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
        const res = await fetch(`${window.API_CONFIG.API_BASE}/auth/userinfo`, {
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
        subtitulo.textContent = `Bienvenido ${nombre}, gestiona el inventario de tus bodegas`;
        if (btnAgregar) btnAgregar.style.display = "inline-flex"; // Ahora EMPLEADO también puede agregar
    }
}

// ===== CARGAR DATOS =====
async function cargarBodegas() {
    try {
        const response = await fetch(`${window.API_CONFIG.API_BASE}/api/bodega/listar`, {
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
            ? `${window.API_CONFIG.API_BASE}/api/inventario/detalle/bodega/${encodeURIComponent(nombreBodega)}`
            : `${window.API_CONFIG.API_BASE}/api/inventario/detalle`;

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

    productoItem: (item, isAdmin) => {
        const stockClass = item.stock < 10 ? 'stock-low' : item.stock < 50 ? 'stock-medium' : 'stock-high';
        
        const botonesAccion = isAdmin ? `
            <div class="product-actions">
                <button class="btn-icon btn-edit" onclick="abrirModalEditarDesdeCard(this)" title="Editar producto">
                    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                    </svg>
                </button>
                <button class="btn-icon btn-delete" onclick="confirmarEliminacionDesdeCard(this)" title="Eliminar producto">
                    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                    </svg>
                </button>
            </div>
        ` : '';
        
        return `
            <div class="product-item" 
                 data-inventario-id="${item.inventarioId || ''}"
                 data-producto-id="${item.productoId || ''}"
                 data-nombre="${item.nombreProducto || ''}"
                 data-bodega="${item.nombreBodega || ''}"
                 data-categoria="${item.categoriaProducto || ''}"
                 data-precio-compra="${item.precioCompra || 0}"
                 data-precio-venta="${item.precioVenta || 0}"
                 data-stock="${item.stock || 0}">
                <div class="product-info">
                    <div class="product-icon">
                        ${Templates.iconoProducto}
                    </div>
                    <div class="product-details">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <h4>${item.nombreProducto}</h4>
                                <p>Bodega: ${item.nombreBodega} · Categoría: ${item.categoriaProducto}</p>
                            </div>
                            ${botonesAccion}
                        </div>
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

    const isAdmin = Utils.getUserRole() === "ADMIN";
    contenedor.innerHTML = inventario.map(item => Templates.productoItem(item, isAdmin)).join('');
}

// ===== EVENTOS =====
function inicializarEventos() {
    const selectBodega = document.getElementById("selectBodega");
    const btnBuscar = document.getElementById("btnBuscar");
    const inputBuscar = document.getElementById("buscarProducto");
    const btnAgregarProducto = document.getElementById("btnAgregarProducto");

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

    // Modal nuevo producto
    if (btnAgregarProducto) {
        btnAgregarProducto.addEventListener("click", abrirModalNuevo);
    }

    const btnCerrarModal = document.getElementById("btnCerrarModal");
    const btnCancelar = document.getElementById("btnCancelar");
    const modalNuevo = document.getElementById("modalNuevoProducto");
    const formNuevo = document.getElementById("formNuevoProducto");

    if (btnCerrarModal) {
        btnCerrarModal.addEventListener("click", cerrarModalNuevo);
    }

    if (btnCancelar) {
        btnCancelar.addEventListener("click", cerrarModalNuevo);
    }

    if (modalNuevo) {
        modalNuevo.addEventListener("click", (e) => {
            if (e.target === modalNuevo) {
                cerrarModalNuevo();
            }
        });
    }

    if (formNuevo) {
        formNuevo.addEventListener("submit", crearNuevoProducto);
    }

    // Modal editar
    const btnCerrarModalEditar = document.getElementById("btnCerrarModalEditar");
    const btnCancelarEditar = document.getElementById("btnCancelarEditar");
    const modalEditar = document.getElementById("modalEditarInventario");
    const formEditar = document.getElementById("formEditarInventario");

    if (btnCerrarModalEditar) {
        btnCerrarModalEditar.addEventListener("click", cerrarModalEditar);
    }

    if (btnCancelarEditar) {
        btnCancelarEditar.addEventListener("click", cerrarModalEditar);
    }

    if (modalEditar) {
        modalEditar.addEventListener("click", (e) => {
            if (e.target === modalEditar) {
                cerrarModalEditar();
            }
        });
    }

    if (formEditar) {
        formEditar.addEventListener("submit", actualizarInventario);
    }

    // Modal confirmación eliminar
    const btnCerrarConfirmar = document.getElementById("btnCerrarConfirmar");
    const btnCancelarEliminar = document.getElementById("btnCancelarEliminar");
    const btnConfirmarEliminar = document.getElementById("btnConfirmarEliminar");
    const modalConfirmar = document.getElementById("modalConfirmarEliminar");

    if (btnCerrarConfirmar) {
        btnCerrarConfirmar.addEventListener("click", cerrarModalConfirmacion);
    }

    if (btnCancelarEliminar) {
        btnCancelarEliminar.addEventListener("click", cerrarModalConfirmacion);
    }

    if (btnConfirmarEliminar) {
        btnConfirmarEliminar.addEventListener("click", eliminarInventario);
    }

    if (modalConfirmar) {
        modalConfirmar.addEventListener("click", (e) => {
            if (e.target === modalConfirmar) {
                cerrarModalConfirmacion();
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

// ===== MODAL NUEVO PRODUCTO =====
async function abrirModalNuevo() {
    const modal = document.getElementById("modalNuevoProducto");
    modal.style.display = "flex";
    modal.classList.add("show");

    await cargarBodegasModalNuevo();
    await cargarCategoriasModalNuevo();
}

function cerrarModalNuevo() {
    const modal = document.getElementById("modalNuevoProducto");
    const form = document.getElementById("formNuevoProducto");
    
    modal.style.display = "none";
    modal.classList.remove("show");
    form.reset();
}

async function cargarBodegasModalNuevo() {
    try {
        const userRole = Utils.getUserRole();
        const url = userRole === "ADMIN" 
            ? `${window.API_CONFIG.API_BASE}/api/bodega/listar`
            : `${window.API_CONFIG.API_BASE}/api/bodega/dashboard`;

        const response = await fetch(url, {
            headers: Utils.authHeaders()
        });

        if (!response.ok) throw new Error("Error al cargar bodegas");

        const bodegas = await response.json();
        const select = document.getElementById("bodegaProducto");
        
        select.innerHTML = '<option value="">Seleccione una bodega...</option>';
        
        bodegas.forEach(bodega => {
            const option = document.createElement("option");
            option.value = bodega.id;
            option.textContent = bodega.nombre;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error al cargar bodegas:", error);
        Utils.mostrarMensaje("Error al cargar la lista de bodegas");
    }
}

async function cargarCategoriasModalNuevo() {
    try {
        const response = await fetch(`${window.API_CONFIG.API_BASE}/api/categoria/listar`, {
            headers: Utils.authHeaders()
        });

        if (!response.ok) throw new Error("Error al cargar categorías");

        const categorias = await response.json();
        const select = document.getElementById("categoriaProducto");
        
        select.innerHTML = '<option value="">Seleccione una categoría...</option>';
        
        categorias.forEach(categoria => {
            const option = document.createElement("option");
            option.value = categoria.id;
            option.textContent = categoria.nombre;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error al cargar categorías:", error);
        Utils.mostrarMensaje("Error al cargar la lista de categorías");
    }
}

async function crearNuevoProducto(event) {
    event.preventDefault();

    const nombre = document.getElementById("nombreProducto").value.trim();
    const categoriaId = parseInt(document.getElementById("categoriaProducto").value);
    const precioCompra = parseFloat(document.getElementById("precioCompraProducto").value);
    const precioVenta = parseFloat(document.getElementById("precioVentaProducto").value);
    const bodegaId = parseInt(document.getElementById("bodegaProducto").value);
    const stock = parseInt(document.getElementById("stockProducto").value);

    if (!nombre || !categoriaId || !precioCompra || !precioVenta || !bodegaId || !stock) {
        return Utils.mostrarMensaje("Por favor complete todos los campos");
    }

    try {
        // Primero crear el producto
        const nuevoProducto = {
            nombre,
            categoria_id: categoriaId,
            precio_compra: precioCompra,
            precio_venta: precioVenta
        };

        const responseProducto = await fetch(`${window.API_CONFIG.API_BASE}/api/producto/create`, {
            method: "POST",
            headers: Utils.authHeaders(),
            body: JSON.stringify(nuevoProducto)
        });

        if (!responseProducto.ok) {
            const errorText = await responseProducto.text();
            throw new Error(errorText || "Error al crear el producto");
        }

        const productoCreado = await responseProducto.json();

        // Luego agregarlo al inventario
        const nuevoInventario = {
            bodega_id: bodegaId,
            producto_id: productoCreado.id,
            stock
        };

        const responseInventario = await fetch(`${window.API_CONFIG.API_BASE}/api/inventario/create`, {
            method: "POST",
            headers: Utils.authHeaders(),
            body: JSON.stringify(nuevoInventario)
        });

        if (!responseInventario.ok) {
            const errorText = await responseInventario.text();
            throw new Error(errorText || "Error al agregar al inventario");
        }

        cerrarModalNuevo();
        await cargarBodegas(); // Actualizar datos de bodegas locales
        await cargarInventario();
        
        // Notificar al dashboard principal para actualizar sus datos
        if (window.parent && window.parent !== window) {
            window.parent.postMessage({ type: 'actualizarBodegas' }, '*');
        }
        
        Utils.mostrarMensaje("Producto creado y agregado al inventario exitosamente");
    } catch (error) {
        console.error("Error al crear producto:", error);
        Utils.mostrarMensaje("Error: " + error.message);
    }
}

// ===== MODAL EDITAR INVENTARIO =====
async function abrirModalEditarDesdeCard(button) {
    try {
        const card = button.closest('.product-item');
        
        // Obtener datos del card
        const inventarioId = card.dataset.inventarioId;
        const productoId = card.dataset.productoId;
        const nombre = card.dataset.nombre;
        const bodega = card.dataset.bodega;
        const categoria = card.dataset.categoria;
        const precioCompra = card.dataset.precioCompra;
        const precioVenta = card.dataset.precioVenta;
        const stock = card.dataset.stock;

        console.log("Datos del card:", { inventarioId, productoId, nombre, bodega, categoria, precioCompra, precioVenta, stock });

        // Mostrar modal primero
        const modal = document.getElementById("modalEditarInventario");
        modal.style.display = "flex";
        modal.classList.add("show");

        // Cargar bodegas y categorías
        await cargarBodegasModalEditar();
        await cargarCategoriasModalEditar();

        // Llenar el formulario
        document.getElementById("inventarioId").value = inventarioId;
        document.getElementById("productoId").value = productoId;
        document.getElementById("nombreProductoEditar").value = nombre;
        document.getElementById("precioCompraEditar").value = precioCompra;
        document.getElementById("precioVentaEditar").value = precioVenta;
        document.getElementById("stockEditar").value = stock;

        // Esperar un momento para que se carguen los selects
        await new Promise(resolve => setTimeout(resolve, 200));

        // Buscar y seleccionar la categoría por nombre
        const selectCategoria = document.getElementById("categoriaProductoEditar");
        Array.from(selectCategoria.options).forEach(option => {
            if (option.textContent === categoria) {
                option.selected = true;
            }
        });

        // Buscar y seleccionar la bodega por nombre
        const selectBodega = document.getElementById("bodegaEditar");
        Array.from(selectBodega.options).forEach(option => {
            if (option.textContent === bodega) {
                option.selected = true;
            }
        });
    } catch (error) {
        console.error("Error al abrir modal de edición:", error);
        Utils.mostrarMensaje("Error al abrir el formulario de edición");
    }
}

function cerrarModalEditar() {
    const modal = document.getElementById("modalEditarInventario");
    const form = document.getElementById("formEditarInventario");
    
    modal.style.display = "none";
    modal.classList.remove("show");
    form.reset();
}

async function cargarBodegasModalEditar() {
    try {
        const userRole = Utils.getUserRole();
        const url = userRole === "ADMIN" 
            ? `${window.API_CONFIG.API_BASE}/api/bodega/listar`
            : `${window.API_CONFIG.API_BASE}/api/bodega/dashboard`;

        const response = await fetch(url, {
            headers: Utils.authHeaders()
        });

        if (!response.ok) throw new Error("Error al cargar bodegas");

        const bodegas = await response.json();
        const select = document.getElementById("bodegaEditar");
        
        select.innerHTML = '<option value="">Seleccione una bodega...</option>';
        
        bodegas.forEach(bodega => {
            const option = document.createElement("option");
            option.value = bodega.id;
            option.textContent = bodega.nombre;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error al cargar bodegas:", error);
        Utils.mostrarMensaje("Error al cargar la lista de bodegas");
    }
}

async function cargarCategoriasModalEditar() {
    try {
        const response = await fetch(`${window.API_CONFIG.API_BASE}/api/categoria/listar`, {
            headers: Utils.authHeaders()
        });

        if (!response.ok) throw new Error("Error al cargar categorías");

        const categorias = await response.json();
        const select = document.getElementById("categoriaProductoEditar");
        
        select.innerHTML = '<option value="">Seleccione una categoría...</option>';
        
        categorias.forEach(categoria => {
            const option = document.createElement("option");
            option.value = categoria.id;
            option.textContent = categoria.nombre;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error al cargar categorías:", error);
        Utils.mostrarMensaje("Error al cargar la lista de categorías");
    }
}

async function actualizarInventario(event) {
    event.preventDefault();

    const inventarioId = document.getElementById("inventarioId").value;
    const productoId = document.getElementById("productoId").value;
    const nombre = document.getElementById("nombreProductoEditar").value.trim();
    const categoriaId = parseInt(document.getElementById("categoriaProductoEditar").value);
    const precioCompra = parseFloat(document.getElementById("precioCompraEditar").value);
    const precioVenta = parseFloat(document.getElementById("precioVentaEditar").value);
    const bodegaId = parseInt(document.getElementById("bodegaEditar").value);
    const stock = parseInt(document.getElementById("stockEditar").value);

    if (!nombre || !categoriaId || !precioCompra || !precioVenta || !bodegaId || !stock) {
        return Utils.mostrarMensaje("Por favor complete todos los campos");
    }

    try {
        // Actualizar producto
        const productoActualizado = {
            nombre,
            categoria_id: categoriaId,
            precio_compra: precioCompra,
            precio_venta: precioVenta
        };

        const responseProducto = await fetch(`${window.API_CONFIG.API_BASE}/api/producto/update/${productoId}`, {
            method: "PUT",
            headers: Utils.authHeaders(),
            body: JSON.stringify(productoActualizado)
        });

        if (!responseProducto.ok) {
            const errorText = await responseProducto.text();
            throw new Error(errorText || "Error al actualizar el producto");
        }

        // Actualizar inventario
        const inventarioActualizado = {
            bodega_id: bodegaId,
            producto_id: parseInt(productoId),
            stock
        };

        const responseInventario = await fetch(`${window.API_CONFIG.API_BASE}/api/inventario/update/${inventarioId}`, {
            method: "PUT",
            headers: Utils.authHeaders(),
            body: JSON.stringify(inventarioActualizado)
        });

        if (!responseInventario.ok) {
            const errorText = await responseInventario.text();
            throw new Error(errorText || "La cantidad de stock tiene que ser un número válido, verifica la cantidad.");
        }

        cerrarModalEditar();
        await cargarBodegas(); // Actualizar datos de bodegas locales
        await cargarInventario();
        
        // Notificar al dashboard principal para actualizar sus datos
        if (window.parent && window.parent !== window) {
            window.parent.postMessage({ type: 'actualizarBodegas' }, '*');
        }
        
        Utils.mostrarMensaje("Producto e inventario actualizados exitosamente");
    } catch (error) {
        console.error("Error al actualizar:", error);
        Utils.mostrarMensaje("Error: " + error.message);
    }
}

// ===== MODAL CONFIRMAR ELIMINACIÓN =====
let inventarioIdEliminar = null;

function confirmarEliminacionDesdeCard(button) {
    try {
        const card = button.closest('.product-item');
        
        inventarioIdEliminar = card.dataset.inventarioId;
        const nombre = card.dataset.nombre;
        const bodega = card.dataset.bodega;

        console.log("ID a eliminar:", inventarioIdEliminar);

        if (!inventarioIdEliminar) {
            return Utils.mostrarMensaje("Error: No se pudo obtener el ID del inventario");
        }

        // Establecer mensaje
        const mensaje = document.getElementById("mensajeConfirmacion");
        mensaje.textContent = `¿Está seguro que desea eliminar el producto "${nombre}" de la bodega "${bodega}"?`;

        // Mostrar modal
        const modal = document.getElementById("modalConfirmarEliminar");
        modal.style.display = "flex";
        modal.classList.add("show");
    } catch (error) {
        console.error("Error al confirmar eliminación:", error);
        Utils.mostrarMensaje("Error al abrir el diálogo de confirmación");
    }
}

function cerrarModalConfirmacion() {
    const modal = document.getElementById("modalConfirmarEliminar");
    
    modal.style.display = "none";
    modal.classList.remove("show");
    inventarioIdEliminar = null;
}

async function eliminarInventario() {
    if (!inventarioIdEliminar) {
        return Utils.mostrarMensaje("Error: No se ha seleccionado ningún producto");
    }

    try {
        console.log("Eliminando inventario ID:", inventarioIdEliminar);

        const response = await fetch(`${window.API_CONFIG.API_BASE}/api/inventario/delete/${inventarioIdEliminar}`, {
            method: "DELETE",
            headers: Utils.authHeaders()
        });

        console.log("Response status:", response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error("Error del servidor:", errorText);
            throw new Error(errorText || "Error al eliminar del inventario");
        }

        cerrarModalConfirmacion();
        await cargarBodegas(); // Actualizar datos de bodegas locales
        await cargarInventario();
        
        // Notificar al dashboard principal para actualizar sus datos
        if (window.parent && window.parent !== window) {
            window.parent.postMessage({ type: 'actualizarBodegas' }, '*');
        }
        
        Utils.mostrarMensaje("Producto eliminado del inventario exitosamente");
    } catch (error) {
        console.error("Error al eliminar:", error);
        Utils.mostrarMensaje("Error: " + error.message);
    }
}
