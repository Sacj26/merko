// Estado global
let clientes = [];
let proveedores = [];
let sucursales = [];
let productosDisponibles = [];
let carritoItems = [];
let clienteSeleccionado = null;
let proveedorSeleccionado = null;
let sucursalSeleccionada = null;

// Paginación
let currentPage = 0;
const pageSize = 30;
let isLoadingMore = false;
let hasMoreProducts = true;
let allProductos = [];

// Inicializar al cargar
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
});

// Configurar event listeners
function setupEventListeners() {
    // Búsqueda de clientes con debounce
    const searchCliente = document.getElementById('searchCliente');
    searchCliente.addEventListener('input', debounce(async (e) => {
        const query = e.target.value.trim();
        if (query.length >= 2) {
            await buscarClientes(query);
        } else if (query.length === 0) {
            document.getElementById('dropdownCliente').classList.remove('active');
        }
    }, 400));

    // Búsqueda de proveedores
    const searchProveedor = document.getElementById('searchProveedor');
    searchProveedor.addEventListener('input', debounce((e) => {
        filtrarProveedores(e.target.value);
    }, 300));
    
    searchProveedor.addEventListener('focus', async () => {
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
        const isSearchInput = e.target.classList.contains('search-input') || e.target.closest('.search-icon');
        const isDropdown = e.target.closest('.dropdown-results') || e.target.closest('.dropdown-item');
        
        if (!isSearchInput && !isDropdown) {
            document.querySelectorAll('.dropdown-results').forEach(d => d.classList.remove('active'));
        }
    });

    // Validar submit
    document.getElementById('ventaForm').addEventListener('submit', (e) => {
        if (carritoItems.length === 0) {
            e.preventDefault();
            alert('Debes agregar al menos un producto al carrito');
            return false;
        }
        if (!clienteSeleccionado) {
            e.preventDefault();
            alert('Debes seleccionar un cliente');
            return false;
        }
        if (!proveedorSeleccionado) {
            e.preventDefault();
            alert('Debes seleccionar un proveedor');
            return false;
        }
        if (!sucursalSeleccionada) {
            e.preventDefault();
            alert('Debes seleccionar una sucursal');
            return false;
        }
    });
}

// Buscar clientes por query (optimizado)
async function buscarClientes(query) {
    try {
        console.log('Buscando clientes con query:', query);
        const resp = await fetch(`/admin/ventas/api/clientes?search=${encodeURIComponent(query)}`);
        if (!resp.ok) {
            throw new Error(`HTTP error! status: ${resp.status}`);
        }
        clientes = await resp.json();
        console.log('Clientes encontrados:', clientes.length);
        filtrarClientes(query);
    } catch (e) {
        console.error('Error buscando clientes:', e);
        clientes = [];
        const dropdown = document.getElementById('dropdownCliente');
        dropdown.innerHTML = '<div class="no-results">Error al cargar clientes</div>';
        dropdown.classList.add('active');
    }
}

// Cargar proveedores
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

// Cargar sucursales
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

// Cargar productos de sucursal (solo con stock > 0)
async function cargarProductosSucursal(sucursalId) {
    try {
        const resp = await fetch(`/admin/productos/por-sucursal/${sucursalId}`);
        allProductos = await resp.json();
        
        // Filtrar solo productos con stock mayor a 0
        allProductos = allProductos.filter(p => (p.stock || 0) > 0);
        
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

// Filtrar y mostrar clientes
function filtrarClientes(query) {
    console.log('Filtrando clientes, total:', clientes.length);
    const dropdown = document.getElementById('dropdownCliente');
    if (!dropdown) {
        console.error('Dropdown de clientes no encontrado');
        return;
    }
    
    const filtrados = clientes.filter(c => 
        c.nombre.toLowerCase().includes(query.toLowerCase()) ||
        (c.apellido && c.apellido.toLowerCase().includes(query.toLowerCase())) ||
        (c.correo && c.correo.toLowerCase().includes(query.toLowerCase())) ||
        (c.telefono && c.telefono.includes(query))
    );
    
    console.log('Clientes filtrados:', filtrados.length);

    if (filtrados.length === 0) {
        dropdown.innerHTML = '<div class="no-results">No se encontraron clientes</div>';
    } else {
        dropdown.innerHTML = filtrados.map(c => `
            <div class="dropdown-item" onclick="seleccionarCliente(${c.id})">
                <div class="dropdown-item-title">${c.nombre} ${c.apellido || ''} <span style="color: #94a3b8; font-size: 0.875rem;">(ID: ${c.id})</span></div>
                <div class="dropdown-item-meta">
                    <span><i class="fas fa-envelope"></i> ${c.correo || 'Sin email'}</span>
                    ${c.telefono ? ` • <i class="fas fa-phone"></i> ${c.telefono}` : ''}
                </div>
            </div>
        `).join('');
    }
    
    dropdown.classList.add('active');
    console.log('Dropdown classes:', dropdown.className);
    console.log('Dropdown HTML length:', dropdown.innerHTML.length);
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
    if (!proveedorSeleccionado) return;
    
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

// Seleccionar cliente
window.seleccionarCliente = async function(id) {
    clienteSeleccionado = clientes.find(c => c.id === id);
    if (!clienteSeleccionado) return;

    document.getElementById('hiddenClienteId').value = id;
    document.getElementById('searchCliente').value = '';
    document.getElementById('clienteNombre').textContent = `${clienteSeleccionado.nombre} ${clienteSeleccionado.apellido || ''} (ID: ${clienteSeleccionado.id})`;
    document.getElementById('clienteEmail').textContent = clienteSeleccionado.correo || 'Sin email';
    document.getElementById('selectedCliente').style.display = 'flex';
    document.getElementById('dropdownCliente').classList.remove('active');
    
    // Habilitar búsqueda de proveedores
    document.getElementById('searchProveedor').disabled = false;
}

// Limpiar cliente
window.clearCliente = function() {
    if (carritoItems.length > 0) {
        alert('No puedes cambiar el cliente cuando hay productos en el carrito. Vacía el carrito primero.');
        return;
    }
    clienteSeleccionado = null;
    document.getElementById('hiddenClienteId').value = '';
    document.getElementById('selectedCliente').style.display = 'none';
    document.getElementById('searchCliente').value = '';
    document.getElementById('searchProveedor').disabled = true;
    document.getElementById('searchProveedor').value = '';
    document.getElementById('dropdownCliente').classList.remove('active');
    
    clearProveedor();
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
    clearSucursal();
    
    // Recargar todas las sucursales (sin filtro)
    await cargarSucursales();
    
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
    
    // Cargar productos de la sucursal (solo con stock > 0)
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
    
    renderProductos('');
}

// Renderizar productos
function renderProductos(query) {
    const grid = document.getElementById('productosGrid');
    
    if (!clienteSeleccionado || !proveedorSeleccionado || !sucursalSeleccionada) {
        grid.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-box-open" style="font-size: 2rem; color: #ddd; margin-bottom: 0.5rem;"></i>
                <p>Selecciona un proveedor y una sucursal para ver los productos disponibles</p>
            </div>
        `;
        return;
    }

    const filtrados = productosDisponibles.filter(p => 
        p.nombre.toLowerCase().includes(query.toLowerCase()) &&
        (p.stock || 0) > 0  // Solo mostrar productos con stock
    );

    if (filtrados.length === 0) {
        grid.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-search" style="font-size: 2rem; color: #ddd; margin-bottom: 0.5rem;"></i>
                <p>${query ? 'No se encontraron productos' : 'No hay productos con stock disponible'}</p>
            </div>
        `;
        return;
    }

    let productosHtml = filtrados.map(p => {
        const enCarrito = carritoItems.some(item => item.id === p.id);
        const stockDisponible = p.stock || 0;
        
        return `
            <div class="producto-card ${enCarrito || stockDisponible === 0 ? 'disabled' : ''}" 
                 onclick="${enCarrito || stockDisponible === 0 ? '' : `agregarAlCarrito(${p.id})`}">
                <div class="producto-card-header">
                    <div class="producto-nombre">${p.nombre}</div>
                    <span class="producto-tipo-badge">
                        ${p.tipo === 'MATERIA_PRIMA' ? 'MP' : 'PT'}
                    </span>
                </div>
                <div class="producto-info">
                    <span class="${stockDisponible === 0 ? 'text-danger' : ''}">
                        <i class="fas fa-box"></i> Stock: ${stockDisponible}
                    </span>
                    <span><strong>${formatearMoneda(p.precioVenta || 0)}</strong></span>
                </div>
                ${enCarrito ? '<div style="margin-top: 0.5rem; color: #4caf50; font-size: 0.85rem;"><i class="fas fa-check"></i> En carrito</div>' : ''}
                ${stockDisponible === 0 ? '<div style="margin-top: 0.5rem; color: #f44336; font-size: 0.85rem;"><i class="fas fa-times"></i> Sin stock</div>' : ''}
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
            Mostrando ${filtrados.length} de ${allProductos.filter(p => (p.stock || 0) > 0).length} productos con stock
        </div>
    ` : '';
    
    grid.innerHTML = contador + productosHtml + btnVerMas;
}

// Agregar al carrito
window.agregarAlCarrito = function(productoId) {
    const producto = productosDisponibles.find(p => p.id === productoId);
    if (!producto || (producto.stock || 0) === 0) return;

    const item = {
        id: producto.id,
        nombre: producto.nombre,
        cantidad: 1,
        precioVenta: producto.precioVenta || 0,
        stockDisponible: producto.stock || 0
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

// Renderizar carrito
function renderCarrito() {
    const container = document.getElementById('cartItems');
    const btnSubmit = document.getElementById('btnSubmit');

    if (carritoItems.length === 0) {
        container.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-shopping-cart" style="font-size: 2rem; color: #ddd; margin-bottom: 0.5rem;"></i>
                <p>Carrito vacío</p>
            </div>
        `;
        document.getElementById('cartSummary').style.display = 'none';
        btnSubmit.disabled = true;
        document.getElementById('cartCount').textContent = '0';
        return;
    }

    container.innerHTML = carritoItems.map((item, index) => `
        <div class="cart-item">
            <div class="cart-item-header">
                <div class="cart-item-name">${item.nombre}</div>
                <button type="button" onclick="removerDelCarrito(${item.id})" class="btn-remove">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
            <input type="hidden" name="detalles[${index}].productoId" value="${item.id}">
            <input type="hidden" name="detalles[${index}].branchId" value="${sucursalSeleccionada?.id || ''}">
            <div class="cart-item-controls">
                <div class="cart-item-control-group">
                    <label>Cantidad:</label>
                    <input type="number" name="detalles[${index}].cantidad" value="${item.cantidad}" 
                           min="1" max="${item.stockDisponible}" required 
                           onchange="actualizarCarrito(${item.id}, this.value, null)" 
                           title="Stock disponible: ${item.stockDisponible}">
                </div>
                <div class="cart-item-control-group">
                    <label>Precio:</label>
                    <input type="number" name="detalles[${index}].precioVenta" value="${item.precioVenta}" 
                           step="0.01" min="0" required onchange="actualizarCarrito(${item.id}, null, this.value)">
                </div>
            </div>
            <div class="cart-item-detail" style="font-size: 0.85rem; color: #666; margin-top: 0.25rem;">
                <i class="fas fa-box"></i> Stock disponible: ${item.stockDisponible}
            </div>
            <div class="cart-item-total">Total: ${formatearMoneda(item.cantidad * item.precioVenta)}</div>
        </div>
    `).join('');

    // Resumen
    const totalItems = carritoItems.length;
    const totalUnidades = carritoItems.reduce((sum, item) => sum + (parseInt(item.cantidad) || 0), 0);
    const total = carritoItems.reduce((sum, item) => sum + (item.cantidad * item.precioVenta), 0);

    document.getElementById('cartCount').textContent = totalItems;
    document.getElementById('summaryItemCount').textContent = totalItems;
    document.getElementById('summaryTotalUnits').textContent = totalUnidades;
    document.getElementById('summaryTotal').textContent = formatearMoneda(total);
    document.getElementById('cartSummary').style.display = 'block';
    btnSubmit.disabled = false;
}

// Actualizar carrito
window.actualizarCarrito = function(productoId, cantidad, precio) {
    const item = carritoItems.find(i => i.id === productoId);
    if (!item) return;
    
    if (cantidad !== null) {
        const cantidadNum = parseInt(cantidad) || 1;
        // Validar que no exceda el stock disponible
        if (cantidadNum > item.stockDisponible) {
            alert(`Solo hay ${item.stockDisponible} unidades disponibles de este producto`);
            // Revertir a la cantidad anterior
            renderCarrito();
            return;
        }
        item.cantidad = cantidadNum;
    }
    if (precio !== null) item.precioVenta = parseFloat(precio) || 0;
    
    renderCarrito();
}

// Formatear moneda
function formatearMoneda(valor) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    }).format(valor);
}

// Debounce
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
