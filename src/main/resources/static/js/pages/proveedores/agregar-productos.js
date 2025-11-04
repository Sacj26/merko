let productoIndex = 0; // será actualizado al cargar según los items ya presentes

// Inicializar productoIndex cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    const container = document.getElementById('productos-container');
    if (container) productoIndex = container.querySelectorAll('.producto-item').length;
    // Asegurar que el primer producto no muestre el botón eliminar
    reindexAllProducts();
    actualizarVisibilidadEliminar();
});

function limpiarControlesDentro(element) {
    const controls = element.querySelectorAll('input, select, textarea');
    controls.forEach(c => {
        const tag = c.tagName.toLowerCase();
        if (tag === 'select') {
            c.selectedIndex = 0;
        } else if (c.type === 'checkbox' || c.type === 'radio') {
            c.checked = false;
        } else if (c.type === 'file') {
            // Reemplazar input file por uno nuevo vacío para garantizar que no tenga valor
            const newFile = c.cloneNode();
            newFile.value = '';
            c.parentNode.replaceChild(newFile, c);
        } else {
            c.value = '';
        }
    });
}

function reindexAllProducts() {
    const productos = document.querySelectorAll('.producto-item');
    productos.forEach((producto, idx) => {
        producto.setAttribute('data-producto-index', idx);
        const header = producto.querySelector('.producto-header h4');
        if (header) header.textContent = `Producto #${idx + 1}`;
        const btn = producto.querySelector('.btn-remove-producto');
        if (btn) btn.setAttribute('onclick', `eliminarProducto(${idx})`);

        // actualizar atributos id, name y for que contengan un índice
        const elems = producto.querySelectorAll('[id],[name],[for]');
        elems.forEach(el => {
            if (el.id) el.id = el.id.replace(/\[\s*\d+\s*\]/, `[${idx}]`);
            if (el.name) el.name = el.name.replace(/\[\s*\d+\s*\]/, `[${idx}]`);
            if (el.htmlFor) el.htmlFor = el.htmlFor.replace(/\[\s*\d+\s*\]/, `[${idx}]`);
        });
    });
    productoIndex = productos.length;
}

// Evitar eliminar el primer producto: ocultar/mostrar botones según índice y cantidad
function actualizarVisibilidadEliminar() {
    const productos = document.querySelectorAll('.producto-item');
    productos.forEach((producto, idx) => {
        const btn = producto.querySelector('.btn-remove-producto');
        if (!btn) return;
        // ocultar el botón del primer bloque (idx 0)
        if (idx === 0) btn.style.display = 'none';
        else btn.style.display = '';
    });
}

window.agregarProducto = function agregarProducto() {
    const container = document.getElementById('productos-container');
    if (!container) return;

    const source = container.querySelector('.producto-item');
    let nuevoProducto;
    if (source) {
        nuevoProducto = source.cloneNode(true);
        // limpiar valores en los campos clonados
        limpiarControlesDentro(nuevoProducto);
        // marcar temporalmente el índice (reindexAllProducts lo corregirá)
        nuevoProducto.setAttribute('data-producto-index', productoIndex);
        container.appendChild(nuevoProducto);
        reindexAllProducts();
    } else {
        // Fallback: si no existe un bloque fuente (página limpia), crear un bloque mínimo
        nuevoProducto = document.createElement('div');
        nuevoProducto.className = 'producto-item';
        nuevoProducto.setAttribute('data-producto-index', productoIndex);
        nuevoProducto.innerHTML = `<div class="producto-header"><h4>Producto #${productoIndex + 1}</h4><button type="button" class="btn-remove-producto" onclick="eliminarProducto(${productoIndex})" title="Eliminar producto"><i class="fas fa-trash-alt" aria-hidden="true"></i></button></div><div class="form-grid"><div class="form-group"><label for="productos[${productoIndex}].nombre">Nombre del Producto: <span class="required">*</span></label><input type="text" id="productos[${productoIndex}].nombre" name="productos[${productoIndex}].nombre" class="form-control" required /></div></div>`;
        container.appendChild(nuevoProducto);
    }

    productoIndex++;
    actualizarNumerosProductos();
}

window.eliminarProducto = function eliminarProducto(index) {
    const producto = document.querySelector(`[data-producto-index="${index}"]`);
    if (producto) {
        producto.remove();
        actualizarNumerosProductos();
    }
}

function actualizarNumerosProductos() {
    const productos = document.querySelectorAll('.producto-item');
    productos.forEach((producto, idx) => {
        const header = producto.querySelector('.producto-header h4');
        if (header) {
            header.textContent = `Producto #${idx + 1}`;
        }
    });
    // ajustar visibilidad de botones eliminar y reindexar
    reindexAllProducts();
    actualizarVisibilidadEliminar();
}

// Inicialización del formulario principal
(function initFormularioProveedorProducto(){
    document.addEventListener('DOMContentLoaded', function() {
            const selectProveedor = document.getElementById('proveedorId');
            const form = document.getElementById('form-productos');
            const btnSubmit = form?.querySelector('button[type="submit"]');

            // Inicialmente, habilitar submit si el formulario contiene proveedorId o branchId ocultos (caso sucursal)
            const hiddenProveedor = document.querySelector('input[name="proveedorId"]')?.value;
            const hiddenBranch = document.querySelector('input[name="branchId"]')?.value;

            if (hiddenProveedor || hiddenBranch) {
                if (btnSubmit) btnSubmit.disabled = false;
            } else {
                if (btnSubmit) btnSubmit.disabled = true;
            }

            const base = form?.getAttribute('data-action-base') || '/admin/proveedores/agregar-productos/';

            function actualizarAction() {
                const proveedorId = selectProveedor?.value;
                if (proveedorId) {
                    if (form) form.action = base + proveedorId;
                    if (btnSubmit) btnSubmit.disabled = false;
                } else {
                    if (form) form.action = base + '0';
                    if (btnSubmit) btnSubmit.disabled = true;
                }
            }

            if (selectProveedor) {
                selectProveedor.addEventListener('change', actualizarAction);
            }

            if (form) {
                form.addEventListener('submit', function(e) {
                    const proveedorSelected = selectProveedor?.value;
                    const proveedorHiddenNow = document.querySelector('input[name="proveedorId"]')?.value;
                    const branchHiddenNow = document.querySelector('input[name="branchId"]')?.value;
                    if (!proveedorSelected && !proveedorHiddenNow && !branchHiddenNow) {
                        e.preventDefault();
                        alert('Por favor, seleccione un proveedor antes de guardar.');
                        selectProveedor?.focus();
                    }
                });
            }
    });
})();
