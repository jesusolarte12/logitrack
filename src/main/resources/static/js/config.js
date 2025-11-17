// Configuración global de la aplicación
// IMPORTANTE: Este archivo debe cargarse ANTES que cualquier otro JS

// Detecta automáticamente el contexto (Tomcat o desarrollo local)
(function() {
    const API_BASE = window.location.pathname.includes('/logitrack/') 
        ? '/logitrack' 
        : '';

    // Función helper para construir URLs con el contexto correcto
    function getApiUrl(endpoint) {
        return `${API_BASE}${endpoint}`;
    }

    // Función helper para hacer fetch con autenticación JWT
    function authFetch(url, options = {}) {
        const token = localStorage.getItem('jwt_token');
        options.headers = options.headers || {};
        if (token) {
            options.headers['Authorization'] = 'Bearer ' + token;
        }
        // Agregar contexto si la URL no es absoluta
        const fullUrl = url.startsWith('http') ? url : getApiUrl(url);
        return fetch(fullUrl, options);
    }

    // Función para verificar si el usuario está autenticado
    function isAuthenticated() {
        return !!localStorage.getItem('jwt_token');
    }

    // Función para cerrar sesión
    function logout() {
        localStorage.removeItem('jwt_token');
        window.location.href = getApiUrl('/templates/login.html');
    }

    // Exportar para uso global
    window.API_CONFIG = {
        API_BASE: API_BASE,
        getApiUrl: getApiUrl,
        authFetch: authFetch,
        isAuthenticated: isAuthenticated,
        logout: logout
    };

    // Log para debug (remover en producción)
    console.log('API_CONFIG inicializado:', window.API_CONFIG.API_BASE);
})();
