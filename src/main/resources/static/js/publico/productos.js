// ============================================
// MERKO - Productos Públicos JavaScript
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    initializeSearch();
    initializeFilters();
    initializeViewToggle();
    initializeCart();
    updateCartCount();
});

// ============================================
// BÚSQUEDA
// ============================================
function initializeSearch() {
    const searchBtn = document.getElementById('searchBtn');
    const searchInput = document.getElementById('searchInput');

    if (searchBtn && searchInput) {
        searchBtn.addEventListener('click', performSearch);
        
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    }
}

function performSearch() {
    const searchInput = document.getElementById('searchInput');
    const category = document.getElementById('categoriaFilter');
    const order = document.getElementById('ordenFilter');
    
    const q = searchInput.value.trim();
    applyFilters(q, category.value, order.value);
}

// ============================================
// FILTROS
// ============================================
function initializeFilters() {
    const categoryFilter = document.getElementById('categoriaFilter');
    const orderFilter = document.getElementById('ordenFilter');
    const searchInput = document.getElementById('searchInput');

    if (categoryFilter) {
        categoryFilter.addEventListener('change', () => {
            applyFilters(searchInput.value.trim(), categoryFilter.value, orderFilter.value);
        });
    }

    if (orderFilter) {
        orderFilter.addEventListener('change', () => {
            applyFilters(searchInput.value.trim(), categoryFilter.value, orderFilter.value);
        });
    }
}

function applyFilters(q, categoria, orden) {
    const params = new URLSearchParams();
    if (q) params.set('q', q);
    if (categoria) params.set('categoria', categoria);
    if (orden) params.set('orden', orden);
    
    const base = window.location.pathname;
    window.location.href = base + '?' + params.toString();
}

function clearFilters() {
    window.location.href = window.location.pathname;
}

// ============================================
// PAGINACIÓN - Función para saltar a página
// ============================================
function jumpToPage() {
    const pageInput = document.getElementById('pageJump');
    if (!pageInput) return;
    
    const pageNumber = parseInt(pageInput.value);
    const maxPage = parseInt(pageInput.max);
    
    if (isNaN(pageNumber) || pageNumber < 1 || pageNumber > maxPage) {
        showToast('Por favor ingresa un número de página válido (1-' + maxPage + ')', 'error');
        return;
    }
    
    // Obtener parámetros actuales
    const urlParams = new URLSearchParams(window.location.search);
    urlParams.set('page', pageNumber - 1); // Las páginas en el backend son 0-indexed
    
    window.location.href = window.location.pathname + '?' + urlParams.toString();
}

// Permitir saltar a página con Enter
document.addEventListener('DOMContentLoaded', function() {
    const pageInput = document.getElementById('pageJump');
    if (pageInput) {
        pageInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                jumpToPage();
            }
        });
    }
    
    // Inicializar filtro de precio
    initPriceFilter();
});

// ============================================
// FILTRO DE PRECIO - Sidebar
// ============================================
function initPriceFilter() {
    const priceRange = document.getElementById('priceRange');
    const priceValue = document.getElementById('priceValue');
    
    if (priceRange && priceValue) {
        priceRange.addEventListener('input', function() {
            const value = parseInt(this.value);
            priceValue.textContent = '$' + value.toLocaleString('es-CO');
        });
        
        // Aplicar filtro al soltar el slider
        priceRange.addEventListener('change', function() {
            // Aquí se puede implementar el filtrado por precio
            // Por ahora solo actualiza el valor visual
            console.log('Filtrar productos hasta: $' + this.value);
        });
    }
}

// ============================================
// VISTA (GRID/LIST)
// ============================================
function initializeViewToggle() {
    const viewButtons = document.querySelectorAll('.btn-view');
    const productGrid = document.getElementById('productGrid');

    viewButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const view = btn.getAttribute('data-view');
            
            viewButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            if (productGrid) {
                if (view === 'list') {
                    productGrid.style.gridTemplateColumns = '1fr';
                } else {
                    productGrid.style.gridTemplateColumns = 'repeat(auto-fill, minmax(280px, 1fr))';
                }
            }
        });
    });
}

// ============================================
// CARRITO
// ============================================
function initializeCart() {
    const productGrid = document.getElementById('productGrid');
    
    if (productGrid) {
        productGrid.addEventListener('click', (e) => {
            const btn = e.target.closest('.add-to-cart');
            if (!btn) return;
            
            const productId = btn.getAttribute('data-id');
            addToCart(productId, btn);
        });
    }
}

function addToCart(productId, button) {
    console.log('Agregando producto al carrito:', productId);
    
    const originalText = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> <span>Agregando...</span>';
    button.disabled = true;
    
    // Get CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    
    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
    };
    
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }
    
    // Send POST request to add to cart
    fetch('/carrito/agregar', {
        method: 'POST',
        headers: headers,
        body: new URLSearchParams({
            'productoId': productId,
            'cantidad': 1
        })
    })
    .then(response => {
        // Check if redirect (success or error message will be in flash attributes)
        if (response.redirected) {
            // Fetch worked, reload to see flash message
            window.location.href = response.url;
            return;
        }
        
        if (response.ok) {
            // Success
            button.innerHTML = '<i class="fas fa-check"></i> <span>Agregado</span>';
            button.style.background = '#10b981';
            
            showToast('Producto agregado al carrito exitosamente');
            updateCartCount();
            
            setTimeout(() => {
                button.innerHTML = originalText;
                button.disabled = false;
                button.style.background = '';
            }, 2000);
        } else {
            // Error
            button.innerHTML = originalText;
            button.disabled = false;
            showToast('Producto sin stock o no disponible', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        button.innerHTML = originalText;
        button.disabled = false;
        showToast('Error al agregar el producto', 'error');
    });
}

function updateCartCount() {
    // Fetch actual cart count from server
    fetch('/carrito/api/count')
        .then(response => response.json())
        .then(data => {
            const cartBadge = document.getElementById('cartCount');
            if (cartBadge) {
                cartBadge.textContent = data.count || 0;
                
                cartBadge.style.transform = 'scale(1.3)';
                setTimeout(() => {
                    cartBadge.style.transform = 'scale(1)';
                }, 200);
            }
        })
        .catch(error => {
            console.error('Error fetching cart count:', error);
        });
}

// ============================================
// TOAST NOTIFICATIONS
// ============================================
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    if (toast && toastMessage) {
        toastMessage.textContent = message;
        
        if (type === 'error') {
            toast.style.background = '#ef4444';
        } else {
            toast.style.background = '#16a34a';
        }
        
        toast.classList.add('show');
        
        setTimeout(() => {
            toast.classList.remove('show');
        }, 3000);
    }
}

// ============================================
// QUICK VIEW MODAL
// ============================================
function quickView(button) {
    const productId = button.getAttribute('data-id');
    const productCard = button.closest('.product-card');
    
    if (!productCard) return;
    
    const image = productCard.querySelector('.product-image img')?.src || '';
    const title = productCard.querySelector('.product-title')?.textContent || '';
    const description = productCard.querySelector('.product-desc')?.textContent || '';
    const price = productCard.querySelector('.price-current')?.textContent || '0';
    const stock = productCard.querySelector('.product-stock span')?.textContent || '';
    
    const modal = document.getElementById('quickViewModal');
    if (!modal) return;
    
    document.getElementById('modalImage').src = image;
    document.getElementById('modalTitle').textContent = title;
    document.getElementById('modalDescription').textContent = description;
    document.getElementById('modalPrice').textContent = `$${price} COP`;
    document.getElementById('modalStock').textContent = stock;
    
    const modalAddCart = document.getElementById('modalAddCart');
    modalAddCart.onclick = () => {
        addToCart(productId, modalAddCart);
        setTimeout(() => closeModal(), 1000);
    };
    
    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeModal() {
    const modal = document.getElementById('quickViewModal');
    if (modal) {
        modal.classList.remove('show');
        document.body.style.overflow = '';
    }
}

window.addEventListener('click', (e) => {
    const modal = document.getElementById('quickViewModal');
    if (e.target === modal) {
        closeModal();
    }
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeModal();
    }
});

// ============================================
// SCROLL TO TOP
// ============================================
let scrollTopBtn;

window.addEventListener('scroll', () => {
    if (!scrollTopBtn) {
        scrollTopBtn = document.createElement('button');
        scrollTopBtn.innerHTML = '<i class="fas fa-arrow-up"></i>';
        scrollTopBtn.className = 'scroll-to-top';
        scrollTopBtn.onclick = () => {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        };
        document.body.appendChild(scrollTopBtn);
    }
    
    if (window.scrollY > 500) {
        scrollTopBtn.style.display = 'flex';
    } else {
        scrollTopBtn.style.display = 'none';
    }
});

const style = document.createElement('style');
style.textContent = `
    .scroll-to-top {
        position: fixed;
        bottom: 24px;
        right: 24px;
        width: 48px;
        height: 48px;
        background: #16a34a;
        color: white;
        border: none;
        border-radius: 50%;
        cursor: pointer;
        display: none;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 999;
        transition: all 0.3s ease;
    }
    
    .scroll-to-top:hover {
        background: #15803d;
        transform: translateY(-4px);
        box-shadow: 0 6px 20px rgba(0,0,0,0.2);
    }
`;
document.head.appendChild(style);

console.log('✅ Merko productos JS inicializado correctamente');
