const productoSelect = document.getElementById('productoSelect');
const cantidadInput = document.getElementById('cantidadInput');
const precioUnitarioInput = document.getElementById('precioUnitario');
const totalLineaInput = document.getElementById('totalLinea');
const btnAgregarDetalle = document.getElementById('btnAgregarDetalle');
const tablaDetallesBody = document.querySelector('#tablaDetalles tbody');
const inputsDetalles = document.getElementById('inputsDetalles');

let detalles = [];

// Campos precio y total no editables
precioUnitarioInput.setAttribute('readonly', true);
totalLineaInput.setAttribute('readonly', true);

// Actualizar precios y total línea
function actualizarPrecios() {
    const selectedOption = productoSelect.options[productoSelect.selectedIndex];
    if (!selectedOption || !selectedOption.value) {
        precioUnitarioInput.value = '';
        totalLineaInput.value = '';
        return;
    }
    const precioUnitario = parseFloat(selectedOption.getAttribute('data-precio'));
    precioUnitarioInput.value = precioUnitario.toFixed(2);

    const cantidad = parseInt(cantidadInput.value);
    if (!cantidad || cantidad < 1) {
        totalLineaInput.value = '';
        return;
    }

    totalLineaInput.value = (precioUnitario * cantidad).toFixed(2);
}

productoSelect.addEventListener('change', actualizarPrecios);
cantidadInput.addEventListener('input', actualizarPrecios);

// Agregar detalle a la lista y renderizar
btnAgregarDetalle.addEventListener('click', () => {
    const productoId = productoSelect.value;
    if (!productoId) {
        alert('Seleccione un producto');
        return;
    }
    const productoNombre = productoSelect.options[productoSelect.selectedIndex].text;
    const cantidad = parseInt(cantidadInput.value);
    if (!cantidad || cantidad < 1) {
        alert('Ingrese una cantidad válida');
        return;
    }
    const precioUnitario = parseFloat(precioUnitarioInput.value);
    const total = parseFloat(totalLineaInput.value);

    detalles.push({
        productoId,
        productoNombre,
        cantidad,
        precioUnitario,
        total
    });

    renderizarDetalles();

    // Limpiar inputs
    productoSelect.selectedIndex = 0;
    cantidadInput.value = 1;
    precioUnitarioInput.value = '';
    totalLineaInput.value = '';
});

// Función para eliminar detalle
function eliminarDetalle(index) {
    detalles.splice(index, 1);
    renderizarDetalles();
}

// Renderizar tabla y inputs hidden
function renderizarDetalles() {
    tablaDetallesBody.innerHTML = '';
    inputsDetalles.innerHTML = '';

    detalles.forEach((detalle, index) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${detalle.productoNombre}</td>
            <td>${detalle.cantidad}</td>
            <td>${detalle.precioUnitario.toFixed(2)}</td>
            <td>${detalle.total.toFixed(2)}</td>
            <td><button type="button" onclick="eliminarDetalle(${index})" class="btn-logout btn-eliminar">Eliminar</button></td>
        `;
        tablaDetallesBody.appendChild(tr);

        // Crear inputs hidden con createElement
        const inputProductoId = document.createElement('input');
        inputProductoId.type = 'hidden';
        inputProductoId.name = `detalles[${index}].productoId`;
        inputProductoId.value = detalle.productoId;

        const inputCantidad = document.createElement('input');
        inputCantidad.type = 'hidden';
        inputCantidad.name = `detalles[${index}].cantidad`;
        inputCantidad.value = detalle.cantidad;

        const inputPrecioUnitario = document.createElement('input');
        inputPrecioUnitario.type = 'hidden';
        inputPrecioUnitario.name = `detalles[${index}].precioUnitario`;
        inputPrecioUnitario.value = detalle.precioUnitario;

        inputsDetalles.appendChild(inputProductoId);
        inputsDetalles.appendChild(inputCantidad);
        inputsDetalles.appendChild(inputPrecioUnitario);
    });
}

// Para que eliminarDetalle funcione en botón inline
window.eliminarDetalle = eliminarDetalle;
