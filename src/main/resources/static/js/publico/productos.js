// JS mínimo para la página pública de productos
document.addEventListener('DOMContentLoaded', function(){
    const searchBtn = document.getElementById('searchBtn');
    const searchInput = document.getElementById('searchInput');
    const category = document.getElementById('categoriaFilter');
    const order = document.getElementById('ordenFilter');

    searchBtn && searchBtn.addEventListener('click', () => {
        const q = searchInput.value.trim();
        applyFilters(q, category.value, order.value);
    });

    category && category.addEventListener('change', () => applyFilters(searchInput.value.trim(), category.value, order.value));
    order && order.addEventListener('change', () => applyFilters(searchInput.value.trim(), category.value, order.value));

    // Delegación para botones "Agregar"
    document.getElementById('productGrid')?.addEventListener('click', (e) => {
        const btn = e.target.closest('.add-to-cart');
        if (!btn) return;
        const id = btn.getAttribute('data-id');
        // placeholder: llamada fetch para añadir al carrito
        console.log('Agregar al carrito', id);
        btn.innerText = 'Agregado';
        btn.disabled = true;
    });
});

function applyFilters(q, categoria, orden){
    // Por ahora recarga la página con query params (el backend podrá leerlos y filtrar)
    const params = new URLSearchParams();
    if (q) params.set('q', q);
    if (categoria) params.set('categoria', categoria);
    if (orden) params.set('orden', orden);
    const base = window.location.pathname;
    window.location.href = base + '?' + params.toString();
}
