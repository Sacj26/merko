// JS para crear ventas (adaptado desde compras)
// Comportamiento: cargar productos por sucursal, añadir/quitar filas, validar stock y calcular totales.

const apiProductosPorProveedor = '/admin/productos/por-proveedor/';
const apiProductosPorSucursal = '/admin/productos/por-sucursal/';
const apiSucursalesPorProveedorBase = '/admin/proveedores/'; // will call {id}/sucursales/json
let productosProveedor = [];
let itemIndex = 0;

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
            <input type="number" name="detalles[${idx}].precioUnitario" class="form-control inp-precio" min="0" step="0.01" required readonly title="Precio de venta (no editable)" />
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
        const placeholder = document.createElement('option');
        placeholder.value = '';
        placeholder.textContent = 'Seleccione un producto';
        sel.appendChild(placeholder);

        productosProveedor.forEach(p => {
            const idStr = String(p.id);
            if (seleccionados.has(idStr) && idStr !== selected) return;
            const opt = document.createElement('option');
            opt.value = idStr;
            opt.textContent = p.nombre;
            // Preferir precio de venta; caer a precio de compra si no hay
            opt.dataset.precio = (p.precioVenta ?? p.precioCompra ?? 0);
            opt.dataset.stock = p.stock ?? 0;
            sel.appendChild(opt);
        });

        if (selected) sel.value = selected;
        actualizarInfoFila(tr);
    });
}

function actualizarInfoFila(tr) {
    const sel = tr.querySelector('.sel-producto');
    const opt = sel.options[sel.selectedIndex];
    const inpPrecio = tr.querySelector('.inp-precio');
    const qtyInput = tr.querySelector('.inp-cantidad');
    if (opt && opt.value) {
        const precio = parseFloat(opt.dataset.precio || '0');
        inpPrecio.value = isNaN(precio) ? '' : precio;
        qtyInput.max = opt.dataset.stock ?? '';
    } else {
        inpPrecio.value = '';
        qtyInput.removeAttribute('max');
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
    const el = document.getElementById('totalVenta');
    if (el) el.textContent = formatearMoneda(total);
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

async function cargarSucursales(provId) {
    const sucSel = document.getElementById('sucursal');
    if (!sucSel) return;
    sucSel.innerHTML = '';
    const opt0 = document.createElement('option');
    opt0.value = '';
    opt0.textContent = '(Sin sucursal)';
    sucSel.appendChild(opt0);
    if (!provId) return;
    try {
        const resp = await fetch(apiSucursalesPorProveedorBase + provId + '/sucursales/json', { headers: { 'Accept': 'application/json' } });
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
    } catch (e) { console.error(e); }
}

function filtrarProductosPorProveedor() {
    const provId = document.getElementById('proveedor').value;
    const btn = document.getElementById('btnAgregarItem');
    if (btn) btn.disabled = !provId;
    cargarSucursales(provId).then(() => {
        productosProveedor = [];
        actualizarSelectsProductos();
        actualizarTotalGeneral();
        const addBtn = document.getElementById('btnAgregarItem');
        if (addBtn) addBtn.disabled = true;
    });
}

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('btnAgregarItem').addEventListener('click', agregarFila);
    // start with one empty row
    agregarFila();

    // sucursal change handled below (after proveedor binding)

    const prov = document.getElementById('proveedor');
    if (prov) prov.addEventListener('change', filtrarProductosPorProveedor);

    const suc2 = document.getElementById('sucursal');
    if (suc2) {
        suc2.addEventListener('change', () => {
            const provId = document.getElementById('proveedor').value;
            cargarProductosProveedor(provId);
        });
    }

    document.getElementById('ventaForm')?.addEventListener('submit', function(e) {
        const filas = document.querySelectorAll('#itemsBody tr');
        if (filas.length === 0) { alert('Agregue al menos un ítem a la venta.'); e.preventDefault(); return; }
        for (const tr of filas) {
            const sel = tr.querySelector('.sel-producto');
            const qty = parseFloat(tr.querySelector('.inp-cantidad').value || '0');
            const stock = parseInt(sel.options[sel.selectedIndex]?.dataset.stock || '0', 10);
            if (!sel.value) { alert('Seleccione un producto en todas las filas.'); e.preventDefault(); return; }
            if (qty <= 0) { alert('La cantidad debe ser mayor a cero.'); e.preventDefault(); return; }
            if (stock < qty) { alert('Stock insuficiente para ' + sel.options[sel.selectedIndex].text); e.preventDefault(); return; }
        }
    });
    // Initialize provider filters like in compras: set sucursales/products state according to any preselected proveedor
    filtrarProductosPorProveedor();
});
