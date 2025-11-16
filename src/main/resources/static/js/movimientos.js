const API_URL = 'http://localhost:8080/api/movimientos';
let movimientos = [];

// Retorna los headers necesarios con JWT
function headers() {
    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + localStorage.getItem("jwt_token")
    };
}

// Formatea la fecha a YYYY-MM-DD HH:mm
function formatFecha(f) {
    if (!f) return "N/A";
    const d = new Date(f);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const h = String(d.getHours()).padStart(2, "0");
    const min = String(d.getMinutes()).padStart(2, "0");
    return `${y}-${m}-${day} ${h}:${min}`;
}

// Muestra los movimientos en la tabla
function renderMovimientos(data) {
    const tbody = document.querySelector("#cuerpoTablaMovimientos");
    const lista = data || movimientos;

    if (!lista.length) {
        tbody.innerHTML =
            '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#718096;">No hay movimientos registrados</td></tr>';
        return;
    }

    let html = "";

    for (let i = 0; i < lista.length; i++) {
        const m = lista[i];
        const tipo = m.tipoMovimiento ? m.tipoMovimiento.toLowerCase() : "entrada";

        html += `
            <tr>
                <td>#MOV-${m.id}</td>
                <td>${formatFecha(m.fecha)}</td>
                <td><span class="badge ${tipo}">${m.tipoMovimiento || "ENTRADA"}</span></td>
                <td>${m.producto || "N/A"}</td>
                <td>${m.cantidad || 0} unidades</td>
                <td>${m.bodegaOrigen || "N/A"}</td>
                <td>${m.bodegaDestino || "N/A"}</td>
                <td>${m.responsable || "N/A"}</td>
            </tr>
        `;
    }

    tbody.innerHTML = html;
}

// Consulta los movimientos desde la API
function cargarMovimientos() {
    fetch(API_URL + "/listar", { headers: headers() })
        .then(res => {
            if (res.status === 401 || res.status === 403) {
                localStorage.removeItem("jwt_token");
                alert("Sesión expirada");
                setTimeout(() => window.location.href = "/templates/login.html", 1500);
                return null;
            }
            if (!res.ok) throw new Error("Error " + res.status);
            return res.json();
        })
        .then(data => {
            if (data) {
                movimientos = data;
                renderMovimientos(movimientos);
            }
        })
        .catch(() => {
            document.querySelector("#cuerpoTablaMovimientos").innerHTML =
                '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#e53e3e;">Error al cargar movimientos</td></tr>';
        });
}

// Filtra los movimientos según el rango de fechas
function filtrarRango() {
    const inicio = document.getElementById("fechaInicio").value;
    const fin = document.getElementById("fechaFin").value;

    if (!inicio || !fin) {
        alert("Selecciona ambas fechas");
        return;
    }

    const fInicio = new Date(inicio);
    const fFin = new Date(fin);
    fFin.setHours(23, 59, 59);

    if (fInicio > fFin) {
        alert("La fecha inicial no puede ser mayor a la final");
        return;
    }

    const filtrados = movimientos.filter(m => {
        const fecha = new Date(m.fecha);
        return fecha >= fInicio && fecha <= fFin;
    });

    renderMovimientos(filtrados);

    if (!filtrados.length) {
        document.querySelector("#cuerpoTablaMovimientos").innerHTML =
            '<tr><td colspan="8" style="text-align:center; padding:2rem; color:#f59e0b;">No se encontraron movimientos en el rango seleccionado</td></tr>';
    }
}

// Inicializa eventos y carga de datos
document.addEventListener("DOMContentLoaded", () => {
    cargarMovimientos();

    const btnFiltrar = document.getElementById("btnFiltrar");
    if (btnFiltrar) btnFiltrar.addEventListener("click", filtrarRango);

    const fechaInicio = document.getElementById("fechaInicio");
    if (fechaInicio) fechaInicio.addEventListener("keypress", e => {
        if (e.key === "Enter") filtrarRango();
    });

    const fechaFin = document.getElementById("fechaFin");
    if (fechaFin) fechaFin.addEventListener("keypress", e => {
        if (e.key === "Enter") filtrarRango();
    });
});
