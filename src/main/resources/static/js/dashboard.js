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

    // Cerrar sesión
    const btnCerrar = document.querySelector(".cerrar-sesion");
    if (btnCerrar) {
        btnCerrar.addEventListener("click", () => {
            localStorage.removeItem("jwt_token");
            window.location.href = "login.html";
        });
    }
});

