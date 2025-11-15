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

        document.getElementById("username").textContent = capitalizar(usuario.nombre);
        document.getElementById("username-rol").textContent = capitalizar(usuario.rol);
        document.getElementById("iniciales").textContent = obtenerIniciales(usuario.nombre);

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
        });
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
});


