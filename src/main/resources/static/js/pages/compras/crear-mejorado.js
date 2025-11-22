// Estado global
let proveedores = [];
let sucursales = [];
let productosDisponibles = [];
let carritoItems = [];
let proveedorSeleccionado = null;
let sucursalSeleccionada = null;

// Paginación
let currentPage = 0;
const pageSize = 30;
let isLoadingMore = false;
let hasMoreProducts = true;
let allProductos = []; // Cache completo de productos

// Inicializar al cargar - NO cargamos datos inicialmente para mejorar rendimiento
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    // Los proveedores se cargarán cuando el usuario haga focus en el search
});

// Configurar event listeners
function setupEventListeners() {
    // Búsqueda de proveedores
    const searchProveedor = document.getElementById('searchProveedor');
    searchProveedor.addEventListener('input', debounce((e) => {
        filtrarProveedores(e.target.value);
    }, 300));
    
    searchProveedor.addEventListener('focus', async () => {
        // Cargar proveedores solo cuando el usuario hace focus (lazy loading)
        if (proveedores.length === 0) {
            await cargarProveedores();
        }
        filtrarProveedores(searchProveedor.value);
    });

    // Búsqueda de sucursales
    const searchSucursal = document.getElementById('searchSucursal');
    searchSucursal.addEventListener('input', debounce((e) => {
        if (proveedorSeleccionado) {
            filtrarSucursales(e.target.value);
        }
    }, 300));
    
    searchSucursal.addEventListener('focus', () => {
        if (!proveedorSeleccionado) {
            alert('Primero debes seleccionar un proveedor');
            searchSucursal.blur();
        } else {
            // Mostrar dropdown si hay proveedor
            filtrarSucursales(searchSucursal.value);
        }
    });

    // Búsqueda de productos
    const searchProducto = document.getElementById('searchProducto');
    searchProducto.addEventListener('input', debounce((e) => {
        renderProductos(e.target.value);
    }, 300));

    // Cerrar dropdowns al hacer click fuera
    document.addEventListener('click', (e) => {
        // No cerrar si se hizo click en input de búsqueda o en dropdown
        const isSearchInput = e.target.classList.contains('search-input') || e.target.closest('.search-icon');
        const isDropdown = e.target.closest('.dropdown-results') || e.target.closest('.dropdown-item');
        
        if (!isSearchInput && !isDropdown) {
            document.querySelectorAll('.dropdown-results').forEach(d => d.classList.remove('active'));
        }
    });

    // Validar submit
    document.getElementById('compraForm').addEventListener('submit', (e) => {
        if (carritoItems.length === 0) {
            e.preventDefault();
            alert('Debes agregar al menos un producto al carrito');
            return false;
        }
        if (!proveedorSeleccionado) {
            e.preventDefault();
            alert('Debes seleccionar un proveedor');
            return false;
        }
    });
}

// Cargar datos
async function cargarProveedores() {
    try {
        const resp = await fetch('/admin/proveedores/api/todos');
        proveedores = await resp.json();
        return proveedores;
    } catch (e) {
        console.error('Error cargando proveedores:', e);
        proveedores = [];
        return [];
    }
}

async function cargarSucursales(proveedorId = null) {
    try {
        const url = proveedorId 
            ? `/admin/sucursales/api/todas?proveedorId=${proveedorId}`
            : '/admin/sucursales/api/todas';
        const resp = await fetch(url);
        sucursales = await resp.json();
    } catch (e) {
        console.error('Error cargando sucursales:', e);
        sucursales = [];
    }
}

async function cargarProductosSucursal(sucursalId) {
    try {
        // Cargar todos los productos de la sucursal
        const resp = await fetch(`/admin/productos/por-sucursal/${sucursalId}`);
        allProductos = await resp.json();
        
        // Reset paginación
        currentPage = 0;
        hasMoreProducts = true;
        productosDisponibles = [];
        
        // Cargar primera página
        loadMoreProducts();
        renderProductos('');
    } catch (e) {
        console.error('Error cargando productos de sucursal:', e);
        allProductos = [];
        productosDisponibles = [];
        renderProductos('');
    }
}

// Cargar más productos (paginación)
function loadMoreProducts() {
    if (!hasMoreProducts || isLoadingMore) return;
    
    const start = currentPage * pageSize;
    const end = start + pageSize;
    const nuevosProductos = allProductos.slice(start, end);
    
    if (nuevosProductos.length > 0) {
        productosDisponibles = [...productosDisponibles, ...nuevosProductos];
        currentPage++;
    }
    
    if (end >= allProductos.length) {
        hasMoreProducts = false;
    }
}

// Función para cargar más productos mediante botón
window.cargarMasProductos = function() {
    if (!hasMoreProducts || isLoadingMore) return;
    
    isLoadingMore = true;
    loadMoreProducts();
    const currentQuery = document.getElementById('searchProducto').value;
    renderProductos(currentQuery);
    isLoadingMore = false;
}

// Filtrar y mostrar proveedores
function filtrarProveedores(query) {
    const dropdown = document.getElementById('dropdownProveedor');
    const filtrados = proveedores.filter(p => 
        p.nombre.toLowerCase().includes(query.toLowerCase())
    );

    if (filtrados.length === 0) {
        dropdown.innerHTML = '<div class="no-results">No se encontraron proveedores</div>';
    } else {
        dropdown.innerHTML = filtrados.map(p => `
            <div class="dropdown-item" onclick="seleccionarProveedor(${p.id})">
                <div class="dropdown-item-title">${p.nombre}</div>
                <div class="dropdown-item-meta">
                    <span><i class="fas fa-warehouse"></i> ${p.totalSucursales || 0} sucursales</span>
                    ${p.email ? ' • ' + p.email : ''}
                </div>
            </div>
        `).join('');
    }
    
    dropdown.classList.add('active');
}

// Filtrar y mostrar sucursales
function filtrarSucursales(query) {
    if (!proveedorSeleccionado) {
        return; // No mostrar sucursales sin proveedor
    }
    
    const dropdown = document.getElementById('dropdownSucursal');
    const filtrados = sucursales.filter(s => 
        s.nombre.toLowerCase().includes(query.toLowerCase()) ||
        (s.ciudad && s.ciudad.toLowerCase().includes(query.toLowerCase())) ||
        (s.pais && s.pais.toLowerCase().includes(query.toLowerCase()))
    );

    if (filtrados.length === 0) {
        dropdown.innerHTML = '<div class="no-results">No se encontraron sucursales para este proveedor</div>';
    } else {
        dropdown.innerHTML = filtrados.map(s => `
            <div class="dropdown-item" onclick="seleccionarSucursal(${s.id})">
                <div class="dropdown-item-title">${s.nombre}</div>
                <div class="dropdown-item-meta">
                    <span><i class="fas fa-box"></i> ${s.totalProductos || 0} productos</span>
                    ${s.ciudad ? ' • ' + s.ciudad : ''}
                    ${s.pais ? ', ' + s.pais : ''}
                </div>
            </div>
        `).join('');
    }
    
    dropdown.classList.add('active');
}

// Seleccionar proveedor
window.seleccionarProveedor = async function(id) {
    proveedorSeleccionado = proveedores.find(p => p.id === id);
    if (!proveedorSeleccionado) return;

    document.getElementById('hiddenProveedorId').value = id;
    document.getElementById('searchProveedor').value = '';
    document.getElementById('proveedorNombre').textContent = proveedorSeleccionado.nombre;
    document.getElementById('selectedProveedor').style.display = 'flex';
    document.getElementById('dropdownProveedor').classList.remove('active');
    
    // Solo limpiar sucursal al cambiar proveedor (mantener carrito)
    clearSucursal();
    
    // Habilitar búsqueda de sucursales
    document.getElementById('searchSucursal').disabled = false;
    
    // Recargar sucursales filtradas por proveedor
    await cargarSucursales(id);
    
    // Los productos se cargarán cuando se seleccione una sucursal
    productosDisponibles = [];
    allProductos = [];
    renderProductos('');
}

// Limpiar proveedor
window.clearProveedor = async function() {
    if (carritoItems.length > 0) {
        alert('No puedes cambiar el proveedor cuando hay productos en el carrito. Vacía el carrito primero.');
        return;
    }
    proveedorSeleccionado = null;
    productosDisponibles = [];
    allProductos = [];
    currentPage = 0;
    hasMoreProducts = true;
    document.getElementById('hiddenProveedorId').value = '';
    document.getElementById('selectedProveedor').style.display = 'none';
    document.getElementById('searchProveedor').value = '';
    document.getElementById('searchSucursal').disabled = true;
    document.getElementById('searchSucursal').value = '';
    document.getElementById('searchProducto').disabled = true;
    document.getElementById('searchProducto').value = '';
    document.getElementById('dropdownProveedor').classList.remove('active');
    
    // Limpiar sucursales completamente
    sucursales = [];
    clearSucursal();
    
    renderProductos('');
    renderCarrito();
}

// Seleccionar sucursal
window.seleccionarSucursal = async function(id) {
    sucursalSeleccionada = sucursales.find(s => s.id === id);
    if (!sucursalSeleccionada) return;

    document.getElementById('hiddenSucursalId').value = id;
    document.getElementById('searchSucursal').value = '';
    document.getElementById('sucursalNombre').textContent = 
        `${sucursalSeleccionada.nombre} - ${sucursalSeleccionada.ciudad || ''}`;
    document.getElementById('selectedSucursal').style.display = 'flex';
    document.getElementById('dropdownSucursal').classList.remove('active');
    
    // Cargar productos de la sucursal
    await cargarProductosSucursal(id);
    document.getElementById('searchProducto').disabled = false;
}

// Limpiar sucursal
window.clearSucursal = function() {
    sucursalSeleccionada = null;
    productosDisponibles = [];
    allProductos = [];
    currentPage = 0;
    hasMoreProducts = true;
    document.getElementById('hiddenSucursalId').value = '';
    document.getElementById('selectedSucursal').style.display = 'none';
    document.getElementById('searchSucursal').value = '';
    document.getElementById('dropdownSucursal').classList.remove('active');
    document.getElementById('searchProducto').disabled = true;
    document.getElementById('searchProducto').value = '';
    
    // Si hay proveedor, las sucursales del proveedor ya están cargadas
    // Si no hay proveedor, no debería haber sucursales disponibles
    
    // Limpiar productos
    renderProductos('');
}

// Renderizar productos
function renderProductos(query) {
    const grid = document.getElementById('productosGrid');
    
    if (!sucursalSeleccionada) {
        grid.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-warehouse" style="font-size: 2rem; color: #ddd; margin-bottom: 0.5rem;"></i>
                <p>Selecciona una sucursal para ver sus productos</p>
            </div>
        `;
        return;
    }

    const filtrados = productosDisponibles.filter(p => 
        p.nombre.toLowerCase().includes(query.toLowerCase())
    );

    if (filtrados.length === 0) {
        grid.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-search" style="font-size: 2rem; color: #ddd; margin-bottom: 0.5rem;"></i>
                <p>No se encontraron productos</p>
            </div>
        `;
        return;
    }

    let productosHtml = filtrados.map(p => {
        const enCarrito = carritoItems.some(item => item.id === p.id);
        return `
            <div class="producto-card ${enCarrito ? 'disabled' : ''}" 
                 onclick="${enCarrito ? '' : `agregarAlCarrito(${p.id})`}">
                <div class="producto-card-header">
                    <div class="producto-nombre">${p.nombre}</div>
                    <span class="producto-tipo-badge">
                        ${p.tipo === 'MATERIA_PRIMA' ? 'MP' : 'PT'}
                    </span>
                </div>
                <div class="producto-info">
                    <span><i class="fas fa-box"></i> Stock: ${p.stock || 0}</span>
                    <span><strong>${formatearMoneda(p.precioCompra || 0)}</strong></span>
                </div>
                ${enCarrito ? '<div style="margin-top: 0.5rem; color: #4caf50; font-size: 0.85rem;"><i class="fas fa-check"></i> En carrito</div>' : ''}
            </div>
        `;
    }).join('');
    
    // Botón para cargar más productos si hay disponibles
    const btnVerMas = hasMoreProducts && filtrados.length > 0 ? `
        <div style="grid-column: 1/-1; text-align: center; padding: 1rem;">
            <button type="button" onclick="cargarMasProductos()" class="btn btn-outline-primary" style="min-width: 200px;">
                <i class="fas fa-plus-circle"></i> Ver más productos
            </button>
        </div>
    ` : '';
    
    // Mostrar contador de productos
    const contador = filtrados.length > 0 ? `
        <div style="grid-column: 1/-1; text-align: center; padding: 0.5rem; color: #888; font-size: 0.9rem;">
            Mostrando ${filtrados.length} de ${allProductos.length} productos
        </div>
    ` : '';
    
    grid.innerHTML = contador + productosHtml + btnVerMas;
}

// Agregar al carrito
window.agregarAlCarrito = function(productoId) {
    const producto = productosDisponibles.find(p => p.id === productoId);
    if (!producto) return;

    const item = {
        id: producto.id,
        nombre: producto.nombre,
        precioCompra: producto.precioCompra || 0,
        cantidad: 1,
        tipo: producto.tipo
    };

    carritoItems.push(item);
    renderCarrito();
    renderProductos(document.getElementById('searchProducto').value);
}

// Remover del carrito
window.removerDelCarrito = function(productoId) {
    carritoItems = carritoItems.filter(item => item.id !== productoId);
    renderCarrito();
    renderProductos(document.getElementById('searchProducto').value);
}

// Actualizar cantidad o precio
window.actualizarItem = function(productoId, campo, valor) {
    const item = carritoItems.find(i => i.id === productoId);
    if (!item) return;

    if (campo === 'cantidad') {
        item.cantidad = Math.max(1, parseInt(valor) || 1);
    } else if (campo === 'precio') {
        item.precioCompra = Math.max(0, parseFloat(valor) || 0);
    }

    renderCarrito();
}

// Renderizar carrito
function renderCarrito() {
    const container = document.getElementById('cartItems');
    const summary = document.getElementById('cartSummary');
    const count = document.getElementById('cartCount');
    const btnSubmit = document.getElementById('btnSubmit');

    count.textContent = carritoItems.length;

    if (carritoItems.length === 0) {
        container.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-cart-plus" style="font-size: 2rem; color: #ddd; margin-bottom: 0.5rem;"></i>
                <p>Agrega productos al carrito</p>
            </div>
        `;
        summary.style.display = 'none';
        btnSubmit.disabled = true;
        return;
    }

    container.innerHTML = carritoItems.map((item, index) => `
        <div class="cart-item">
            <div class="cart-item-header">
                <div class="cart-item-name">${item.nombre}</div>
                <button type="button" class="btn-remove-item" onclick="removerDelCarrito(${item.id})" title="Eliminar">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
            <input type="hidden" name="detalles[${index}].productoId" value="${item.id}">
            <div class="cart-item-controls">
                <div class="cart-item-control-group">
                    <label>Cantidad:</label>
                    <input type="number" 
                           name="detalles[${index}].cantidad"
                           value="${item.cantidad}" 
                           min="1" 
                           required
                           onchange="actualizarItem(${item.id}, 'cantidad', this.value)">
                </div>
                <div class="cart-item-control-group">
                    <label>Precio:</label>
                    <input type="number" 
                           name="detalles[${index}].precioUnitario"
                           value="${item.precioCompra}" 
                           min="0" 
                           step="0.01" 
                           required
                           onchange="actualizarItem(${item.id}, 'precio', this.value)">
                </div>
            </div>
            <div class="cart-item-total">
                Total: ${formatearMoneda(item.cantidad * item.precioCompra)}
            </div>
        </div>
    `).join('');

    // Calcular totales
    const totalUnits = carritoItems.reduce((sum, item) => sum + item.cantidad, 0);
    const total = carritoItems.reduce((sum, item) => sum + (item.cantidad * item.precioCompra), 0);

    document.getElementById('summaryItemCount').textContent = carritoItems.length;
    document.getElementById('summaryTotalUnits').textContent = totalUnits;
    document.getElementById('summaryTotal').textContent = formatearMoneda(total);

    summary.style.display = 'block';
    btnSubmit.disabled = false;
}

// Utilidades
function formatearMoneda(n) {
    try {
        return new Intl.NumberFormat('es-CO', { 
            style: 'currency', 
            currency: 'COP', 
            minimumFractionDigits: 0 
        }).format(Number(n || 0));
    } catch (e) {
        return '$' + (n || 0);
    }
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}
