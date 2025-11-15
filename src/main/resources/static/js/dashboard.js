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

document.addEventListener("DOMContentLoaded", () => {
    const btnCerrar = document.querySelector(".cerrar-sesion");

    if (btnCerrar) {
        btnCerrar.addEventListener("click", () => {
            localStorage.removeItem("jwt_token");
            window.location.href = "login.html";
        });
    }
});
