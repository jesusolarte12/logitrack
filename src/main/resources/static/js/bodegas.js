document.addEventListener("DOMContentLoaded", () => {
    cargarBodegas();
});

function cargarBodegas() {
    console.log("Llamando al backend...");

    fetch("http://localhost:8080/api/bodega/info")
        .then(response => {
            console.log("Respuesta cruda:", response);
            return response.json();
        })
        .then(data => {
            console.log("Datos recibidos:", data);
            mostrarBodegas(data);
        })
        .catch(error => console.error("Error al cargar bodegas:", error));
}

function mostrarBodegas(bodegas) {
    const contenedor = document.getElementById("bodegaGrid");

    console.log("¿Existe el contenedor?", contenedor);

    if (!contenedor) {
        console.error("ERROR: No existe el DIV con id='bodegaGrid'");
        return;
    }

    console.log("Datos recibidos en mostrarBodegas:", bodegas);

    contenedor.innerHTML = "";

    bodegas.forEach(b => {
        const card = `
            <div class="bodega-card">
                <div class="bodega-header">
                    <div>
                        <div class="bodega-name">${b.nombre}</div>
                        <div class="bodega-location">${b.ubicacion}</div>
                    </div>
                </div>
                <div class="bodega-stats">
                    <div class="bodega-stat">
                        <div class="bodega-stat-value">${b.totalProducto}</div>
                        <div class="bodega-stat-label">Total Productos</div>
                    </div>
                    <div class="bodega-stat">
                        <div class="bodega-stat-value">${b.ocupacion.toFixed(1)}%</div>
                        <div class="bodega-stat-label">Ocupación</div>
                    </div>
                    <div class="bodega-stat">
                        <div class="bodega-stat-value">${b.espacio}</div>
                        <div class="bodega-stat-label">Espacio disponible</div>
                    </div>
                    <div class="bodega-stat">
                        <div class="bodega-stat-value">${b.encargado}</div>
                        <div class="bodega-stat-label">Encargado</div>
                    </div>
                </div>
            </div>
        `;
        contenedor.innerHTML += card;
    });
}
