document.addEventListener('DOMContentLoaded', () => {
    const proveedorSelect = document.getElementById('proveedor');
    const productoSelect = document.getElementById('producto');
    const cantidadInput = document.getElementById('cantidad');
    const precioUnitarioInput = document.getElementById('precioUnitario');
    const totalPagarInput = document.getElementById('totalPagar');

    // Guardar todas las opciones para filtrar después (excluyendo el placeholder)
    const allProductoOptions = Array.from(productoSelect.querySelectorAll('option'))
        .filter(opt => opt.value !== "");

    productoSelect.disabled = true;

    function filtrarProductosPorProveedor() {
        const proveedorIdSeleccionado = proveedorSelect.value;
        console.log("Proveedor seleccionado:", proveedorIdSeleccionado);

        // Limpiar opciones (dejar solo la opción placeholder)
        productoSelect.innerHTML = '';
        const placeholderOption = document.createElement('option');
        placeholderOption.value = "";
        placeholderOption.disabled = true;
        placeholderOption.selected = true;
        placeholderOption.textContent = "-- Seleccione un producto --";
        productoSelect.appendChild(placeholderOption);

        if (!proveedorIdSeleccionado) {
            productoSelect.disabled = true;
            actualizarPrecioYTotal();
            return;
        }

        // Filtrar productos que coincidan con el proveedor seleccionado (comparar como strings)
        const productosFiltrados = allProductoOptions.filter(option => {
            const prodProvId = option.getAttribute('data-proveedorid');
            return String(prodProvId) === String(proveedorIdSeleccionado);
        });

        console.log("Productos filtrados:", productosFiltrados.map(o => o.textContent));

        // Agregar opciones filtradas
        productosFiltrados.forEach(option => {
            // Clonar para no mover el original
            const optionCopy = option.cloneNode(true);
            productoSelect.appendChild(optionCopy);
        });

        productoSelect.disabled = productosFiltrados.length === 0;

        // Resetear selección y actualizar precios
        productoSelect.value = "";
        actualizarPrecioYTotal();
    }

    function actualizarPrecioYTotal() {
        const productoSeleccionado = productoSelect.options[productoSelect.selectedIndex];
        if (!productoSeleccionado || !productoSelect.value) {
            precioUnitarioInput.value = "";
            totalPagarInput.value = "";
            return;
        }

        const precioUnitario = parseFloat(productoSeleccionado.getAttribute('data-preciocompra')) || 0;
        const cantidad = parseInt(cantidadInput.value) || 0;

        precioUnitarioInput.value = precioUnitario.toFixed(2);
        totalPagarInput.value = (precioUnitario * cantidad).toFixed(2);
    }

    proveedorSelect.addEventListener('change', filtrarProductosPorProveedor);
    productoSelect.addEventListener('change', actualizarPrecioYTotal);
    cantidadInput.addEventListener('input', actualizarPrecioYTotal);

    // Inicializar
    filtrarProductosPorProveedor();
});
