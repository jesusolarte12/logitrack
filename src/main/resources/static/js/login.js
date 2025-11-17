document.getElementById("formLogin").addEventListener("submit", async (e) => {
    e.preventDefault();

    const user = document.getElementById("username").value.trim();
    const pass = document.getElementById("password").value.trim();

    if (!user || !pass) {
        alert("Completa todos los campos.");
        return;
    }

    try {
        const res = await fetch(window.API_CONFIG.getApiUrl('/auth/login'), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: user, password: pass })
        });

        if (!res.ok) {
            const err = await res.text();
            alert('Error autenticación: ' + (err || res.statusText));
            return;
        }

        // El backend devuelve el token JWT como texto plano
        const token = await res.text();

        // Guardar token (para producción considerar HttpOnly cookie)
        localStorage.setItem('jwt_token', token);

        // Redirigir a dashboard
        window.location.href = "dashboard.html";
        // alert("Vamos bien");

    } catch (error) {
        console.error('Error al conectar con el servidor:', error);
        alert('Error de red. Intenta más tarde.');
    }
});
