async function validarToken() {
    const token = localStorage.getItem("jwt_token");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    try {
        const res = await fetch("http://localhost:8080/auth/validate", {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + token
            }
        });

        if (!res.ok) {
            localStorage.removeItem("jwt_token");
            window.location.href = "login.html";
        }
    } catch (err) {
        localStorage.removeItem("jwt_token");
        window.location.href = "login.html";
    }
}

async function cargarInfoUsuario() {
     try {
        const token = localStorage.getItem("jwt_token");
        if (!token) throw new Error("No se encontró el token JWT.");

        const response = await fetch(`http://localhost:8080/auth/userinfo`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(`Error al obtener el usuario: ${response.status}`);
        }

        const usuario = await response.json();

        // Desktop
        document.getElementById("username").textContent = capitalizar(usuario.nombre);
        document.getElementById("username-rol").textContent = capitalizar(usuario.rol);
        document.getElementById("iniciales").textContent = obtenerIniciales(usuario.nombre);

        // Mobile
        document.getElementById("username-mobile").textContent = capitalizar(usuario.nombre);
        document.getElementById("username-rol-mobile").textContent = capitalizar(usuario.rol);
        document.getElementById("iniciales-mobile").textContent = obtenerIniciales(usuario.nombre);

        // Mostrar opción de Usuarios solo para ADMIN
        if (usuario.rol === "ADMIN") {
            const navUsuarios = document.getElementById("nav-usuarios");
            if (navUsuarios) {
                navUsuarios.style.display = "block";
            }
        }

        return usuario;
    } catch (error) {
        console.error(error);
    }
}

function capitalizar(texto) {
    if (!texto) return '';
    return texto
        .split(' ')
        .map(palabra => palabra.charAt(0).toUpperCase() + palabra.slice(1).toLowerCase())
        .join(' ');
}

function obtenerIniciales(nombreCompleto) {
    if (!nombreCompleto) return "";
    const palabras = nombreCompleto.trim().split(/\s+/);
    const iniciales = palabras.slice(0, 2) 
                             .map(p => p[0].toUpperCase())
                             .join("");
    return iniciales;
}

// Ejecutar validación al cargar la página
validarToken();

// Cambiar sección activa y mostrar iframe correspondiente
document.addEventListener("DOMContentLoaded", () => {
    const navItems = document.querySelectorAll(".nav-item");
    const iframes = document.querySelectorAll(".section");
    const hamburger = document.getElementById("hamburger");
    const navMenu = document.getElementById("navMenu");

    // Mostrar sección de bodegas por defecto
    // Toggle menú hamburguesa
    if (hamburger) {
        hamburger.addEventListener("click", () => {
            hamburger.classList.toggle("active");
            navMenu.classList.toggle("active");
        });
    }

    // Cerrar menú al hacer click en una opción (solo en móvil)
    navItems.forEach(item => {
        item.addEventListener("click", () => {
            // Cambiar activo en el navbar
            navItems.forEach(i => i.classList.remove("activo"));
            item.classList.add("activo");

            // Mostrar el iframe correcto
            const target = item.getAttribute("data-target");
            iframes.forEach(frame => {
                frame.style.display = frame.id === target ? "block" : "none";
            });

            // Cerrar menú en móvil
            if (window.innerWidth <= 768) {
                hamburger.classList.remove("active");
                navMenu.classList.remove("active");
            }
        });
    });

    // Cerrar menú al hacer click fuera de él
    document.addEventListener("click", (e) => {
        if (window.innerWidth <= 768) {
            const isClickInsideMenu = navMenu.contains(e.target);
            const isClickOnHamburger = hamburger.contains(e.target);
            
            if (!isClickInsideMenu && !isClickOnHamburger && navMenu.classList.contains("active")) {
                hamburger.classList.remove("active");
                navMenu.classList.remove("active");
            }
        }
    });

    // Cargar información del usuario
    cargarInfoUsuario();
    // Cerrar sesión
    const btnCerrar = document.querySelector(".cerrar-sesion");
    if (btnCerrar) {
        btnCerrar.addEventListener("click", () => {
            localStorage.removeItem("jwt_token");
            window.location.href = "login.html";
        });
    }
    
    // Escuchar mensajes desde los iframes para actualizar datos
    window.addEventListener('message', function(event) {
        // Verificar que el mensaje es para actualizar bodegas
        if (event.data && event.data.type === 'actualizarBodegas') {
            console.log('Recibido mensaje para actualizar bodegas desde iframe');
            // Recargar el iframe de bodegas si está visible o cargado
            const iframeBodegas = document.getElementById('bodegas');
            if (iframeBodegas && iframeBodegas.contentWindow) {
                // Enviar mensaje al iframe de bodegas para que se recargue
                iframeBodegas.contentWindow.postMessage({ type: 'recargarDatos' }, '*');
            }
            
            // También recargar inventario si está visible
            const iframeInventario = document.getElementById('inventario');
            if (iframeInventario && iframeInventario.contentWindow) {
                iframeInventario.contentWindow.postMessage({ type: 'recargarDatos' }, '*');
            }
        }
    });
});


