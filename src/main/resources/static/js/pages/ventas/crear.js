let productoCounter = 1;

function actualizarFila(item) {
    const select = item.querySelector('.producto-select');
    const cantidad = parseFloat(item.querySelector('.cantidad-input').value || '0');
    const option = select.options[select.selectedIndex];
    let precio = 0, stock = 0;
    if (option && option.dataset.precio) {
        precio = parseFloat(option.dataset.precio || '0');
        stock = parseInt(option.dataset.stock || '0');
    }
    item.querySelector('.price-value').textContent = precio.toFixed(2);
    item.querySelector('.stock-value').textContent = stock;
    const subtotal = precio * (isNaN(cantidad) ? 0 : cantidad);
    item.querySelector('.subtotal').textContent = `$${subtotal.toFixed(2)}`;
    actualizarTotal();
}

function actualizarTotal() {
    let total = 0;
    document.querySelectorAll('.producto-item').forEach(item => {
        const sub = item.querySelector('.subtotal').textContent.replace(/[^0-9.]/g, '');
        total += parseFloat(sub || '0');
    });
    document.getElementById('totalVenta').textContent = `$${total.toFixed(2)}`;
}

function refreshProductOptions() {
    const selects = Array.from(document.querySelectorAll('.producto-select'));
    const selectedValues = new Set(selects.map(s => s.value).filter(v => v));
    selects.forEach(sel => {
        Array.from(sel.options).forEach(opt => {
            if (!opt.value) return;
            const shouldHide = selectedValues.has(opt.value) && sel.value !== opt.value;
            opt.disabled = shouldHide;
            opt.hidden = shouldHide;
        });
    });
}

window.agregarProducto = function agregarProducto() {
    const container = document.getElementById('productos');
    const template = container.children[0].cloneNode(true);

    template.querySelectorAll('select, input').forEach(el => {
        el.name = el.name.replace('[0]', `[${productoCounter}]`);
        if (el.tagName === 'SELECT') el.selectedIndex = 0;
        if (el.tagName === 'INPUT') el.value = 1;
    });

    template.querySelector('.price-value').textContent = '0.00';
    template.querySelector('.stock-value').textContent = '0';
    template.querySelector('.subtotal').textContent = '$0.00';

    container.appendChild(template);
    productoCounter++;
    bindRowEvents(template);
    refreshProductOptions();
    actualizarTotal();
}

window.eliminarProducto = function eliminarProducto(btn) {
    const items = document.querySelectorAll('.producto-item');
    if (items.length > 1) {
        btn.closest('.producto-item').remove();
        actualizarTotal();
    }
}

function bindRowEvents(row) {
    row.querySelector('.producto-select').addEventListener('change', function() {
        actualizarFila(row);
        refreshProductOptions();
    });
    row.querySelector('.cantidad-input').addEventListener('change', function() {
        actualizarFila(row);
    });
}

// Event listeners iniciales
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.producto-item').forEach(bindRowEvents);
    document.querySelectorAll('.producto-item').forEach(actualizarFila);

    document.getElementById('ventaForm')?.addEventListener('submit', function(e) {
        const productos = document.querySelectorAll('.producto-select');
        const seleccionados = new Set();
        for (const select of productos) {
            if (!select.value) { alert('Seleccione un producto en todas las filas.'); e.preventDefault(); return; }
            if (seleccionados.has(select.value)) { alert('No puede seleccionar el mismo producto más de una vez'); e.preventDefault(); return; }
            seleccionados.add(select.value);
        }
    });
});
