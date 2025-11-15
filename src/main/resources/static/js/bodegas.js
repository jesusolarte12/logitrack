document.addEventListener("DOMContentLoaded", async () => {
    await validarToken();     // 1. Valida el token
    await cargarBodegas();    // 2. Si es válido, carga las bodegas
});

//Valida el token, si no es valido lo redirecciona al login, si es valido muestra las bodegas
async function validarToken() {
    const token = localStorage.getItem("jwt_token");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    try {
        const res = await fetch("http://localhost:8080/auth/validate", {
            method: "GET",
            headers: { "Authorization": "Bearer " + token }
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

//Funcion para Cargar las bodegas de la base de datos en el endpoint /api/bodega/info
async function cargarBodegas() {

    const token = localStorage.getItem("jwt_token");
    console.log("Token encontrado:", token);

    if (!token) {
        console.warn("No hay token, redirigiendo al login.");
        window.location.href = "login.html";
        return;
    }

    try {
        console.log("Enviando solicitud con token...");

        const response = await fetch("http://localhost:8080/api/bodega/info", {
            headers: {
                "Authorization": "Bearer " + token
            }
        });

        console.log("Status backend:", response.status);

        if (response.status === 401 || response.status === 403) {
            console.warn("Token inválido o expirado. Redirigiendo al login.");
            localStorage.removeItem("jwt_token");
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            console.error("Respuesta NO OK:", await response.text());
            return;
        }

        const data = await response.json();
        console.log("Datos recibidos:", data);

        mostrarBodegas(data);

    } catch (error) {
        console.error("Error al cargar bodegas:", error);
    }
}

//Funcion para mostrar las insertar los datos en HTML
function mostrarBodegas(bodegas) {
    const contenedor = document.getElementById("bodegaGrid");

    if (!contenedor) {
        console.error("Error al intentar encontrar el div");
        return;
    }

    contenedor.innerHTML = ""; // Limpia contenido previo

    bodegas.forEach(bodega => {
        const card = document.createElement("div");
        card.classList.add("bodega-card");

        card.innerHTML = `
            <div class="bodega-header">
                <div>
                    <div class="bodega-name">${bodega.nombre}</div>
                    <div class="bodega-location">${bodega.ubicacion}</div>
                </div>
            </div>

            <div class="bodega-stats">
                <div class="bodega-stat">
                    <div class="bodega-stat-value">${bodega.totalProducto}</div>
                    <div class="bodega-stat-label">Total Productos</div>
                </div>

                <div class="bodega-stat">
                    <div class="bodega-stat-value">${bodega.ocupacion}%</div>
                    <div class="bodega-stat-label">Ocupación</div>
                </div>

                <div class="bodega-stat">
                    <div class="bodega-stat-value">${bodega.espacio}</div>
                    <div class="bodega-stat-label">Espacio Disponible</div>
                </div>

                <div class="bodega-stat">
                    <div class="bodega-stat-value">${bodega.encargado}</div>
                    <div class="bodega-stat-label">Encargado</div>
                </div>
            </div>
        `;

        contenedor.appendChild(card);
    });
}
