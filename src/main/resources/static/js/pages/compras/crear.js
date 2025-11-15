let popupProductos = null;
// Rutas base (mantener literales funcionales)
const baseAgregarProductos = '/admin/proveedores/agregar-productos/';
const apiProductosPorProveedor = '/admin/productos/por-proveedor/';
const apiProductosPorSucursal = '/admin/productos/por-sucursal/';
let productosProveedor = [];
let itemIndex = 0;

window.irAAgregarProductos = function irAAgregarProductos() {
    const prov = document.getElementById('proveedor').value;
    if (prov) {
        const url = baseAgregarProductos + prov;
        popupProductos = window.open(url, '_blank');
        if (popupProductos) {
            const checkClosed = setInterval(() => {
                if (popupProductos.closed) {
                    clearInterval(checkClosed);
                    const provId = document.getElementById('proveedor').value;
                    if (provId) cargarProductosProveedor(provId);
                }
            }, 1200);

            const onFocus = () => {
                if (popupProductos && popupProductos.closed) {
                    window.removeEventListener('focus', onFocus);
                    const provId = document.getElementById('proveedor').value;
                    if (provId) cargarProductosProveedor(provId);
                }
            };
            window.addEventListener('focus', onFocus);
        } else {
            window.location.href = url;
        }
    }
}

async function cargarProductosProveedor(provId) {
    productosProveedor = [];
    // Si hay sucursal seleccionada, cargar por sucursal (toma prioridad)
    const sucursalSel = document.getElementById('sucursal');
    const sucursalId = sucursalSel ? sucursalSel.value : '';
    if (sucursalId) {
        try {
            const resp = await fetch(apiProductosPorSucursal + sucursalId, { headers: { 'Accept': 'application/json' } });
            if (!resp.ok) throw new Error('Error al cargar productos por sucursal');
            productosProveedor = await resp.json();
        } catch (e) { console.error(e); }
        actualizarSelectsProductos();
        actualizarTotalGeneral();
        // habilitar botón de agregar ítem cuando hay sucursal
        const addBtn = document.getElementById('btnAgregarItem');
        if (addBtn) addBtn.disabled = false;
        return;
    }

    // Si no hay sucursal, pero sí proveedor, cargar por proveedor
    if (!provId) {
        actualizarSelectsProductos();
        actualizarTotalGeneral();
        return;
    }
    try {
        const resp = await fetch(apiProductosPorProveedor + provId, { headers: { 'Accept': 'application/json' } });
        if (!resp.ok) throw new Error('Error al cargar productos');
        productosProveedor = await resp.json();
    } catch (e) { console.error(e); }
    actualizarSelectsProductos();
    actualizarTotalGeneral();
}

function crearFilaItem(idx) {
    const tr = document.createElement('tr');
    tr.setAttribute('data-index', idx);
    tr.innerHTML = `
        <td>
            <div class="cell-inline">
                <select name="detalles[${idx}].productoId" class="form-control sel-producto" required></select>
                <div class="item-meta">
                    <span class="meta">
                        <i class="fas fa-box" aria-hidden="true"></i>
                        <span class="stock-actual"></span>
                    </span>
                </div>
            </div>
        </td>
        <td>
            <input type="number" name="detalles[${idx}].cantidad" class="form-control inp-cantidad" min="1" required />
        </td>
        <td>
            <input type="number" name="detalles[${idx}].precioUnitario" class="form-control inp-precio" min="0" step="0.01" required readonly title="Precio de compra del producto (no editable)" />
        </td>
        <td>
            <strong class="total-linea">$0.00</strong>
        </td>
        <td class="actions-col">
            <div class="actions-cell">
                <button type="button" class="btn-icon btn-icon-danger btn-remover" title="Eliminar">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </td>`;
    return tr;
}

function formatearMoneda(n) {
    try { return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(Number(n||0)); }
    catch(e) { return '$' + (n||0); }
}

function actualizarSelectsProductos() {
    const filas = document.querySelectorAll('#itemsBody tr');
    const seleccionados = new Set();
    filas.forEach(tr => {
        const v = tr.querySelector('.sel-producto')?.value;
        if (v) seleccionados.add(String(v));
    });

    filas.forEach(tr => {
        const sel = tr.querySelector('.sel-producto');
        const selected = sel.value ? String(sel.value) : '';
        sel.innerHTML = '';
        const opt0 = document.createElement('option');
        opt0.value = '';
        opt0.textContent = 'Seleccione un producto';
        sel.appendChild(opt0);

        const mpGroup = document.createElement('optgroup');
        mpGroup.label = 'Materias primas';
        const ptGroup = document.createElement('optgroup');
        ptGroup.label = 'Productos terminados';

        productosProveedor.forEach(p => {
            const idStr = String(p.id);
            const tipo = (p.tipo || '').toUpperCase();
            if (seleccionados.has(idStr) && idStr !== selected) return;
            const opt = document.createElement('option');
            opt.value = idStr;
            opt.textContent = p.nombre;
            opt.dataset.precio = (p.precioCompra ?? 0);
            opt.dataset.stock = p.stock ?? 0;
            opt.dataset.tipo = tipo;
            if (tipo === 'MATERIA_PRIMA') mpGroup.appendChild(opt);
            else ptGroup.appendChild(opt);
        });

        if (mpGroup.children.length > 0) sel.appendChild(mpGroup);
        if (ptGroup.children.length > 0) sel.appendChild(ptGroup);

        if (selected) sel.value = selected;
        actualizarInfoFila(tr);
    });
}

function actualizarInfoFila(tr) {
    const sel = tr.querySelector('.sel-producto');
    const opt = sel.options[sel.selectedIndex];
    const txtStock = tr.querySelector('.stock-actual');
    const inpPrecio = tr.querySelector('.inp-precio');
    if (opt && opt.value) {
        const stock = parseInt(opt.dataset.stock || '0', 10);
        txtStock.textContent = `Stock: ${stock}`;
        const precio = parseFloat(opt.dataset.precio || '0');
        inpPrecio.value = isNaN(precio) ? '' : precio;
        inpPrecio.readOnly = true;
        inpPrecio.title = 'Precio de compra del producto (no editable)';
    } else {
        txtStock.textContent = '';
        inpPrecio.value = '';
    }
    actualizarTotalFila(tr);
}

function actualizarTotalFila(tr) {
    const cantidad = parseFloat(tr.querySelector('.inp-cantidad').value || '0');
    const precio = parseFloat(tr.querySelector('.inp-precio').value || '0');
    const total = cantidad * precio;
    tr.querySelector('.total-linea').textContent = formatearMoneda(total);
    actualizarTotalGeneral();
}

function actualizarTotalGeneral() {
    let total = 0;
    document.querySelectorAll('#itemsBody tr').forEach(tr => {
        const cantidad = parseFloat(tr.querySelector('.inp-cantidad').value || '0');
        const precio = parseFloat(tr.querySelector('.inp-precio').value || '0');
        total += cantidad * precio;
    });
    document.getElementById('totalCompra').textContent = formatearMoneda(total);
}

function agregarFila() {
    const tbody = document.getElementById('itemsBody');
    const tr = crearFilaItem(itemIndex);
    tbody.appendChild(tr);
    tr.querySelector('.sel-producto').addEventListener('change', () => { actualizarInfoFila(tr); actualizarSelectsProductos(); });
    tr.querySelector('.inp-cantidad').addEventListener('input', () => actualizarTotalFila(tr));
    tr.querySelector('.btn-remover').addEventListener('click', () => { tr.remove(); actualizarTotalGeneral(); actualizarSelectsProductos(); });
    itemIndex++;
    actualizarSelectsProductos();
}

function filtrarProductosPorProveedor() {
    const provId = document.getElementById('proveedor').value;
    const btn = document.getElementById('btnAgregarProductoProveedor');

    btn.disabled = !provId;
    // Recargar la lista de sucursales para el proveedor.
    // NO cargar productos por proveedor aquí: queremos que los productos solo se muestren
    // cuando el usuario seleccione explícitamente una sucursal.
    cargarSucursales(provId).then(() => {
        // limpiar la lista de productos visible y actualizar selects para reflejar que
        // todavía no hay sucursal seleccionada
        productosProveedor = [];
        actualizarSelectsProductos();
        actualizarTotalGeneral();
        // deshabilitar añadir ítem hasta que se seleccione sucursal
        const addBtn = document.getElementById('btnAgregarItem');
        if (addBtn) addBtn.disabled = true;
    });
}

async function cargarSucursales(provId) {
    const sucSel = document.getElementById('sucursal');
    if (!sucSel) return;
    // limpiar opciones
    sucSel.innerHTML = '';
    const opt0 = document.createElement('option');
    opt0.value = '';
    opt0.textContent = '(Sin sucursal)';
    sucSel.appendChild(opt0);
    if (!provId) {
        // si no hay proveedor seleccionado, dejar sólo la opción vacía
        return;
    }
    try {
        const resp = await fetch('/admin/proveedores/' + provId + '/sucursales/json', { headers: { 'Accept': 'application/json' } });
        if (!resp.ok) throw new Error('Error al cargar sucursales');
        const list = await resp.json();
        list.forEach(s => {
            const o = document.createElement('option');
            o.value = s.id;
            let txt = s.nombre || '';
            if (s.ciudad) txt += ' — ' + s.ciudad;
            if (s.pais) txt += ', ' + s.pais;
            o.textContent = txt;
            sucSel.appendChild(o);
        });
    } catch (e) {
        console.error(e);
    }
}

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('proveedor').addEventListener('change', filtrarProductosPorProveedor);
    const suc = document.getElementById('sucursal');
    if (suc) {
        suc.addEventListener('change', () => {
            // Re-run product loading; provider may stay selected but branch takes priority
            const provId = document.getElementById('proveedor').value;
            cargarProductosProveedor(provId);
        });
    }
    document.getElementById('btnAgregarItem').addEventListener('click', agregarFila);
    agregarFila();
    filtrarProductosPorProveedor();
});
