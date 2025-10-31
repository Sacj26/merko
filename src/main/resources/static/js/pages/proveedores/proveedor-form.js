let productoIndex = 0; // será actualizado cuando la sección tenga items

// Deshabilitar todos los campos de la sección productos mientras esté oculta
afterDOMContentLoaded(function initDisableSeccionProductos(){
    const seccion = document.getElementById('seccion-productos');
    if (seccion && seccion.style.display === 'none') {
        const controls = seccion.querySelectorAll('input, select, textarea');
        controls.forEach(c => { c.disabled = true; });
    }
    // inicializar productoIndex según items ya presentes (ignorar prototipo)
    const container = document.getElementById('productos-container');
    if (container) productoIndex = container.querySelectorAll('.producto-item:not(.prototype)').length;
    // asegurar visibilidad correcta de botones eliminar
    reindexAllProducts();
    if (typeof actualizarVisibilidadEliminar === 'function') actualizarVisibilidadEliminar();
});

window.mostrarSeccionProductos = function mostrarSeccionProductos() {
    const seccion = document.getElementById('seccion-productos');
    const btn = document.getElementById('btn-mostrar-productos');
    if (seccion && seccion.style.display === 'none') {
        seccion.style.display = '';
        if (btn) btn.style.display = 'none';
        // Si no hay productos reales todavía (ignorar prototipo), agregar el primero dinámicamente
        const container = document.getElementById('productos-container');
        if (container) {
            const realCount = container.querySelectorAll('.producto-item:not(.prototype)').length;
            if (realCount === 0) {
                agregarProducto();
            }
        }
        // Habilitar controles y marcar requeridos mínimos (en los nuevos inputs ya creados)
        // Enable only real product controls (not the prototype inputs)
        const controls = seccion.querySelectorAll('.producto-item:not(.prototype) input, .producto-item:not(.prototype) select, .producto-item:not(.prototype) textarea');
        controls.forEach(c => { c.disabled = false; });
        const reqIds = [
            'productos[0].nombre',
            'productos[0].precioCompra',
            'productos[0].precioVenta',
            'productos[0].stock'
        ];
        reqIds.forEach(id => {
            const el = document.getElementById(id);
            if (el) el.required = true;
        });
        // Focus al primer campo del primer producto real (no prototipo)
        const firstInput = seccion.querySelector('.producto-item:not(.prototype) input[name$=".nombre"]');
        if (firstInput) firstInput.focus();
    }
}

// Helpers para clonar y reindexar productos (similar a agregar-productos.js)
function limpiarControlesDentro(element) {
    const controls = element.querySelectorAll('input, select, textarea');
    controls.forEach(c => {
        const tag = c.tagName.toLowerCase();
        if (tag === 'select') {
            c.selectedIndex = 0;
        } else if (c.type === 'checkbox' || c.type === 'radio') {
            c.checked = false;
        } else if (c.type === 'file') {
            const newFile = c.cloneNode();
            newFile.value = '';
            c.parentNode.replaceChild(newFile, c);
        } else {
            c.value = '';
        }
    });
}

function reindexAllProducts() {
    // Reindex only real producto items (ignore .prototype)
    const productos = document.querySelectorAll('.producto-item:not(.prototype)');
    productos.forEach((producto, idx) => {
        producto.setAttribute('data-producto-index', idx);
        const header = producto.querySelector('.producto-header h4');
        if (header) header.textContent = `Producto #${idx + 1}`;
        const btn = producto.querySelector('.btn-remove-producto');
        if (btn) btn.setAttribute('onclick', `eliminarProducto(${idx})`);

        const elems = producto.querySelectorAll('[id],[name],[for]');
        elems.forEach(el => {
            if (el.id) el.id = el.id.replace(/\[\s*\d+\s*\]/, `[${idx}]`);
            if (el.name) el.name = el.name.replace(/\[\s*\d+\s*\]/, `[${idx}]`);
            if (el.htmlFor) el.htmlFor = el.htmlFor.replace(/\[\s*\d+\s*\]/, `[${idx}]`);
        });
    });
    productoIndex = productos.length;
}

function actualizarVisibilidadEliminar() {
    const productos = document.querySelectorAll('.producto-item:not(.prototype)');
    productos.forEach((producto, idx) => {
        const btn = producto.querySelector('.btn-remove-producto');
        if (!btn) return;
        if (idx === 0) btn.style.display = 'none';
        else btn.style.display = '';
    });
}

window.agregarProducto = function agregarProducto() {
    const container = document.getElementById('productos-container');
    if (!container) return;

    // Prefer the server-rendered prototype element for cloning
    const source = container.querySelector('.producto-item.prototype') || container.querySelector('.producto-item');
    let nuevoProducto;
    if (source) {
        nuevoProducto = source.cloneNode(true);
        // If cloned from prototype, remove prototype marker and make visible
        if (nuevoProducto.classList.contains('prototype')) {
            nuevoProducto.classList.remove('prototype');
            nuevoProducto.style.display = '';
        }
        limpiarControlesDentro(nuevoProducto);
        // Ensure inputs inside the new product are enabled
        const controls = nuevoProducto.querySelectorAll('input, select, textarea');
        controls.forEach(c => { c.disabled = false; });
        nuevoProducto.setAttribute('data-producto-index', productoIndex);
        container.appendChild(nuevoProducto);
        reindexAllProducts();
    } else {
        // Fallback mínimo
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
    const producto = document.querySelector(`.producto-item:not(.prototype)[data-producto-index="${index}"]`);
    if (producto) {
        producto.remove();
        actualizarNumerosProductos();
    }
}

function actualizarNumerosProductos() {
    const productos = document.querySelectorAll('.producto-item:not(.prototype)');
    productos.forEach((producto, idx) => {
        const header = producto.querySelector('.producto-header h4');
        if (header) {
            header.textContent = `Producto #${idx + 1}`;
        }
    });
    reindexAllProducts();
    actualizarVisibilidadEliminar();
}

function afterDOMContentLoaded(fn){
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', fn);
    else fn();
}
