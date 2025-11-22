// ===== NUEVO FLUJO FLEXIBLE =====
// Permite seleccionar productos de CUALQUIER proveedor/sucursal en una misma compra
// Cada producto en el carrito tiene su propia sucursal asignada

// Estado global
let todosLosProductos = [];
let todosLosBranches = []; // Todas las sucursales disponibles
let carritoItems = []; // { producto, branch, cantidad, precioUnitario }

// Paginación
let currentPage = 0;
const pageSize = 30;
let productosVisibles = [];

// Inicializar
document.addEventListener('DOMContentLoaded', () => {
    cargarDatosIniciales();
    setupEventListeners();
});

// Cargar todos los productos y sucursales (sin filtros de proveedor)
async function cargarDatosIniciales() {
    try {
        // Cargar productos de todos los proveedores
        const respProductos = await fetch('/admin/productos/api/todos');
        if (!respProductos.ok) {
            throw new Error(`Error ${respProductos.status}: ${respProductos.statusText}`);
        }
        todosLosProductos = await respProductos.json();
        console.log('Productos cargados:', todosLosProductos.length);
        
        // Cargar todas las sucursales
        const respBranches = await fetch('/admin/sucursales/api/todas');
        if (!respBranches.ok) {
            throw new Error(`Error ${respBranches.status}: ${respBranches.statusText}`);
        }
        todosLosBranches = await respBranches.json();
        console.log('Sucursales cargadas:', todosLosBranches.length);
        
        renderProductos();
    } catch (e) {
        console.error('Error cargando datos:', e);
        mostrarError('No se pudieron cargar los productos. Intenta recargar la página. Error: ' + e.message);
    }
}

// Configurar event listeners
function setupEventListeners() {
    // Búsqueda de productos
    const searchProducto = document.getElementById('searchProducto');
    searchProducto.addEventListener('input', debounce((e) => {
        renderProductos(e.target.value);
    }, 300));

    // Validar submit
    document.getElementById('compraForm').addEventListener('submit', (e) => {
        if (carritoItems.length === 0) {
            e.preventDefault();
            alert('Debes agregar al menos un producto al carrito');
            return false;
        }
        
        // Validar que todos los productos tengan sucursal asignada
        const itemSinSucursal = carritoItems.find(item => !item.branch);
        if (itemSinSucursal) {
            e.preventDefault();
            alert(`Debes asignar una sucursal al producto: ${itemSinSucursal.producto.nombre}`);
            return false;
        }
    });

    // Scroll infinito para productos
    const productosGrid = document.getElementById('productosGrid');
    productosGrid.addEventListener('scroll', () => {
        const { scrollTop, scrollHeight, clientHeight } = productosGrid;
        if (scrollTop + clientHeight >= scrollHeight - 100) {
            loadMoreProducts();
        }
    });
}

// Renderizar productos con paginación
function renderProductos(query = '') {
    const grid = document.getElementById('productosGrid');
    
    // Filtrar productos
    let productosFiltrados = todosLosProductos;
    if (query.trim()) {
        const q = query.toLowerCase();
        productosFiltrados = todosLosProductos.filter(p => 
            p.nombre.toLowerCase().includes(q) ||
            (p.descripcion && p.descripcion.toLowerCase().includes(q)) ||
            (p.proveedor && p.proveedor.nombre && p.proveedor.nombre.toLowerCase().includes(q))
        );
    }
    
    // Reset paginación
    currentPage = 0;
    productosVisibles = productosFiltrados.slice(0, pageSize);
    
    if (productosVisibles.length === 0) {
        grid.innerHTML = '<div class="empty-cart"><i class="fas fa-box-open" style="font-size: 2rem; color: #ddd; margin-bottom: 0.5rem;"></i><p>No se encontraron productos</p></div>';
        return;
    }
    
    grid.innerHTML = productosVisibles.map(p => `
        <div class="producto-card" onclick="agregarAlCarrito(${p.id})">
            <div class="producto-card-header">
                <div class="producto-nombre">${p.nombre}</div>
                ${p.tipo ? `<span class="producto-tipo-badge">${p.tipo}</span>` : ''}
            </div>
            <div class="producto-info">
                <span><i class="fas fa-truck"></i> ${p.proveedor ? p.proveedor.nombre : 'Sin proveedor'}</span>
                <span><i class="fas fa-dollar-sign"></i> $${formatNumber(p.precioCompra || 0)}</span>
            </div>
        </div>
    `).join('');
}

// Cargar más productos (scroll infinito)
function loadMoreProducts() {
    currentPage++;
    const start = currentPage * pageSize;
    const end = start + pageSize;
    const siguientesPagina = todosLosProductos.slice(start, end);
    
    if (siguientesPagina.length === 0) return;
    
    const grid = document.getElementById('productosGrid');
    siguientesPagina.forEach(p => {
        const card = document.createElement('div');
        card.className = 'producto-card';
        card.onclick = () => agregarAlCarrito(p.id);
        card.innerHTML = `
            <div class="producto-card-header">
                <div class="producto-nombre">${p.nombre}</div>
                ${p.tipo ? `<span class="producto-tipo-badge">${p.tipo}</span>` : ''}
            </div>
            <div class="producto-info">
                <span><i class="fas fa-truck"></i> ${p.proveedor ? p.proveedor.nombre : 'Sin proveedor'}</span>
                <span><i class="fas fa-dollar-sign"></i> $${formatNumber(p.precioCompra || 0)}</span>
            </div>
        `;
        grid.appendChild(card);
    });
}

// Agregar producto al carrito
window.agregarAlCarrito = function(productoId) {
    const producto = todosLosProductos.find(p => p.id === productoId);
    if (!producto) return;
    
    // Verificar si ya está en el carrito
    const existe = carritoItems.find(item => item.producto.id === productoId);
    if (existe) {
        alert('Este producto ya está en el carrito');
        return;
    }
    
    // Agregar al carrito SIN sucursal asignada (se asigna después)
    carritoItems.push({
        producto: producto,
        branch: null, // Se asignará después
        cantidad: 1,
        precioUnitario: producto.precioCompra || 0
    });
    
    renderCarrito();
    actualizarTotales();
};

// Renderizar carrito
function renderCarrito() {
    const container = document.getElementById('cartItems');
    const cartCount = document.getElementById('cartCount');
    const btnSubmit = document.getElementById('btnSubmit');
    
    if (carritoItems.length === 0) {
        container.innerHTML = '<div class="empty-cart"><i class="fas fa-cart-plus" style="font-size: 2rem; color: #ddd; margin-bottom: 0.5rem;"></i><p>Agrega productos al carrito</p></div>';
        btnSubmit.disabled = true;
        cartCount.textContent = '0';
        document.getElementById('cartSummary').style.display = 'none';
        return;
    }
    
    container.innerHTML = carritoItems.map((item, index) => {
        // Filtrar sucursales del proveedor del producto
        const sucursalesDelProveedor = todosLosBranches.filter(b => 
            b.proveedor && item.producto.proveedor && b.proveedor.id === item.producto.proveedor.id
        );
        
        console.log(`Producto: ${item.producto.nombre}, Proveedor ID: ${item.producto.proveedor ? item.producto.proveedor.id : 'N/A'}, Sucursales disponibles: ${sucursalesDelProveedor.length}`);
        
        return `
            <div class="cart-item">
                <div class="cart-item-header">
                    <div class="cart-item-name">${item.producto.nombre}</div>
                    <button type="button" class="btn-remove-item" onclick="removerDelCarrito(${index})" title="Eliminar">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                
                <!-- Selector de Sucursal -->
                <div class="cart-item-control-group">
                    <label>Sucursal:</label>
                    <select class="form-control" onchange="actualizarSucursal(${index}, this.value)" required>
                        <option value="">Selecciona sucursal...</option>
                        ${sucursalesDelProveedor.map(b => `
                            <option value="${b.id}" ${item.branch && item.branch.id === b.id ? 'selected' : ''}>
                                ${b.nombre} - ${b.ciudad || ''}
                            </option>
                        `).join('')}
                    </select>
                    <input type="hidden" name="detalles[${index}].branchId" value="${item.branch ? item.branch.id : ''}" />
                    <input type="hidden" name="detalles[${index}].productoId" value="${item.producto.id}" />
                </div>
                
                <div class="cart-item-controls">
                    <div class="cart-item-control-group">
                        <label>Cantidad:</label>
                        <input type="number" 
                               name="detalles[${index}].cantidad" 
                               value="${item.cantidad}" 
                               min="1" 
                               onchange="actualizarCantidad(${index}, this.value)" 
                               required />
                    </div>
                    <div class="cart-item-control-group">
                        <label>Precio:</label>
                        <input type="number" 
                               name="detalles[${index}].precioUnitario" 
                               value="${item.precioUnitario}" 
                               min="0" 
                               step="0.01" 
                               onchange="actualizarPrecio(${index}, this.value)" 
                               required />
                    </div>
                </div>
                
                <div class="cart-item-total">
                    Subtotal: $${formatNumber(item.cantidad * item.precioUnitario)}
                </div>
            </div>
        `;
    }).join('');
    
    btnSubmit.disabled = false;
    cartCount.textContent = carritoItems.length;
    document.getElementById('cartSummary').style.display = 'block';
}

// Actualizar sucursal de un item
window.actualizarSucursal = function(index, branchId) {
    const branch = todosLosBranches.find(b => b.id === parseInt(branchId));
    carritoItems[index].branch = branch || null;
    renderCarrito();
    actualizarTotales();
};

// Actualizar cantidad
window.actualizarCantidad = function(index, cantidad) {
    carritoItems[index].cantidad = parseInt(cantidad) || 1;
    renderCarrito();
    actualizarTotales();
};

// Actualizar precio
window.actualizarPrecio = function(index, precio) {
    carritoItems[index].precioUnitario = parseFloat(precio) || 0;
    renderCarrito();
    actualizarTotales();
};

// Remover del carrito
window.removerDelCarrito = function(index) {
    carritoItems.splice(index, 1);
    renderCarrito();
    actualizarTotales();
};

// Actualizar totales
function actualizarTotales() {
    const totalUnidades = carritoItems.reduce((sum, item) => sum + item.cantidad, 0);
    const totalCompra = carritoItems.reduce((sum, item) => sum + (item.cantidad * item.precioUnitario), 0);
    
    document.getElementById('summaryItemCount').textContent = carritoItems.length;
    document.getElementById('summaryTotalUnits').textContent = totalUnidades;
    document.getElementById('summaryTotal').textContent = '$' + formatNumber(totalCompra);
}

// Utilidades
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), wait);
    };
}

function formatNumber(num) {
    return parseFloat(num).toLocaleString('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function mostrarError(mensaje) {
    alert(mensaje); // Placeholder - implementar toast/notificación mejor
}
