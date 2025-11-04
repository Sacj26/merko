let productoIndex = 0; // será actualizado cuando la sección tenga items
let branchIndex = 0; // índice de sucursales

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
    // inicializar branchIndex según items ya presentes (ignorar prototipo)
    const branchesContainer = document.getElementById('sucursales-container');
    if (branchesContainer) branchIndex = branchesContainer.querySelectorAll('.branch-item:not(.prototype)').length;
    // Si no hay sucursales reales al cargar, ocultar la sección de sucursales y deshabilitar sus controles
    const seccionSuc = document.getElementById('seccion-sucursales');
    if (seccionSuc) {
        const realBranches = branchesContainer ? branchesContainer.querySelectorAll('.branch-item:not(.prototype)').length : 0;
        if (realBranches === 0) {
            // Mostrar la sección y crear la primera sucursal con su contacto por defecto.
            seccionSuc.style.display = '';
            // Habilitar controles del bloque (el propio agregarSucursal() habilitará los del nuevo branch)
            const controls = seccionSuc.querySelectorAll('input, select, textarea');
            controls.forEach(c => { c.disabled = false; });
            // Añadir la primera sucursal automáticamente
            try {
                if (typeof agregarSucursal === 'function') agregarSucursal();
            } catch (e) {
                console.warn('No se pudo añadir la sucursal inicial automáticamente:', e);
            }
        } else {
            seccionSuc.style.display = '';
        }
    }
    // Defensive sanitation: ensure first branch/contact never has remove buttons in DOM
    try { sanitizeFirstBranch(); } catch(e) {}
    // Defensive sanitation: remove any accidental literal placeholders inserted into inputs (e.g. "branch" or "contact")
    try { sanitizeLiteralPlaceholders(); } catch(e) {}
    // asegurar visibilidad correcta de botones eliminar
    reindexAllProducts();
    if (typeof actualizarVisibilidadEliminar === 'function') actualizarVisibilidadEliminar();
    // reindex sucursales si existen
    if (typeof reindexAllBranches === 'function') reindexAllBranches();
    if (typeof actualizarVisibilidadEliminarSucursales === 'function') actualizarVisibilidadEliminarSucursales();
});

// Limpia valores literales inesperados que a veces aparecen en inputs al editar (p.ej. "branch", "contact")
function sanitizeLiteralPlaceholders() {
    try {
        // buscar inputs dentro de sucursales y contactos
        const container = document.getElementById('sucursales-container');
        if (!container) return;
        const inputs = container.querySelectorAll('input[type="text"], input[type="email"], textarea');
        inputs.forEach(inp => {
            if (!inp) return;
            const v = (inp.value || '').toString().trim();
            if (!v) return;
            // limpiar valores obvios generados por prototipos/undo equivocado
            if (v.toLowerCase() === 'branch' || v.toLowerCase() === 'contact' || v.toLowerCase() === 'branches' || v.toLowerCase() === 'contacts') {
                inp.value = '';
            }
        });
    } catch (e) {
        console.warn('sanitizeLiteralPlaceholders error', e);
    }
}

// Helpers de plantillas: crear data-name-template/data-id-template a partir del prototipo
function ensureTemplatesForPrototype(prototypeEl) {
    if (!prototypeEl) return;
    // For each form control, if no data-name-template exists, create one
    const elems = prototypeEl.querySelectorAll('input,select,textarea,label,[for]');
    elems.forEach(el => {
        // name/id/for
        try {
            if (el.name && !el.dataset.nameTemplate) {
                // replace numeric indices for branches/contacts/products with placeholders
                let tmpl = el.name.replace(/branches\[\s*\d+\s*\]/g, 'branches[{branch}]').replace(/contacts\[\s*\d+\s*\]/g, 'contacts[{contact}]').replace(/productos?\[\s*\d+\s*\]/g, 'productos[{product}]');
                el.dataset.nameTemplate = tmpl;
            }
            if (el.id && !el.dataset.idTemplate) {
                let tmplId = el.id.replace(/branches\[\s*\d+\s*\]/g, 'branches[{branch}]').replace(/contacts\[\s*\d+\s*\]/g, 'contacts[{contact}]').replace(/productos?\[\s*\d+\s*\]/g, 'productos[{product}]');
                el.dataset.idTemplate = tmplId;
            }
            if (el.htmlFor && !el.dataset.forTemplate) {
                let tmplFor = el.htmlFor.replace(/branches\[\s*\d+\s*\]/g, 'branches[{branch}]').replace(/contacts\[\s*\d+\s*\]/g, 'contacts[{contact}]').replace(/productos?\[\s*\d+\s*\]/g, 'productos[{product}]');
                el.dataset.forTemplate = tmplFor;
            }
        } catch (e) { /* ignore */ }
    });
}

function applyTemplatesToElement(el, indices) {
    if (!el || !indices) return;
    const elems = el.querySelectorAll('[data-name-template], [data-id-template], [data-for-template]');
    elems.forEach(child => {
        try {
            if (child.dataset.nameTemplate) {
                let name = child.dataset.nameTemplate.replace('{branch}', indices.branch ?? indices.product ?? 0).replace('{contact}', indices.contact ?? 0).replace('{product}', indices.product ?? 0);
                child.name = name;
            }
            if (child.dataset.idTemplate) {
                let id = child.dataset.idTemplate.replace('{branch}', indices.branch ?? indices.product ?? 0).replace('{contact}', indices.contact ?? 0).replace('{product}', indices.product ?? 0);
                child.id = id;
            }
            if (child.dataset.forTemplate) {
                let f = child.dataset.forTemplate.replace('{branch}', indices.branch ?? indices.product ?? 0).replace('{contact}', indices.contact ?? 0).replace('{product}', indices.product ?? 0);
                child.htmlFor = f;
            }
        } catch (e) { /* ignore */ }
    });
}

window.mostrarSeccionProductos = function mostrarSeccionProductos() {
    const seccion = document.getElementById('seccion-productos');
    const btn = document.getElementById('btn-mostrar-productos');
    if (seccion && seccion.style.display === 'none') {
        seccion.style.display = '';
        if (btn) btn.style.display = 'none';
        // Si no hay productos reales todavía (ignorar prototipo), agregar el primero dinámicamente
        const container = document.getElementById('productos-container');
        if (container) {
            const reales = container.querySelectorAll('.producto-item:not(.prototype)').length;
            if (reales === 0) {
                // Añadir automáticamente el primer producto para facilitar la creación
                try {
                    if (typeof agregarProducto === 'function') agregarProducto();
                } catch (e) {
                    console.warn('No se pudo añadir el primer producto automáticamente:', e);
                }
            }
        }
        // Habilitar controles y marcar requeridos mínimos (en los inputs reales)
        // Esperar un tick para permitir que agregarProducto() inserte el DOM si fue llamado
        setTimeout(() => {
            const controls = seccion.querySelectorAll('.producto-item:not(.prototype) input, .producto-item:not(.prototype) select, .producto-item:not(.prototype) textarea');
            controls.forEach(c => { c.disabled = false; });
            // Intentar marcar como required los campos del primer producto (índice 0)
            const firstPrefix = 'productos[0]';
            const reqFields = ['.nombre', '.precioCompra', '.precioVenta', '.stock'];
            reqFields.forEach(suffix => {
                const id = firstPrefix + suffix;
                const el = document.getElementById(id);
                if (el) el.required = true;
            });
            // Focus al primer campo del primer producto real (no prototipo)
            const firstInput = seccion.querySelector('.producto-item:not(.prototype) input[name$=".nombre"], .producto-item:not(.prototype) input[id$=".nombre"]');
            if (firstInput) firstInput.focus();
        }, 30);
    }
}

// --- Sucursales: funciones para POC frontend (clonar/reindex anidado) ---

function reindexAllBranches() {
    const branches = document.querySelectorAll('.branch-item:not(.prototype)');
    branches.forEach((branch, bIdx) => {
        branch.setAttribute('data-branch-index', bIdx);
        const header = branch.querySelector('.branch-header h4');
        if (header) header.textContent = `Sucursal #${bIdx + 1}`;
        const btnRemove = branch.querySelector('.btn-remove-branch');
        if (btnRemove) {
            if (bIdx === 0) {
                // ensure first branch has no onclick
                try { btnRemove.removeAttribute('onclick'); } catch(e){}
            } else {
                btnRemove.setAttribute('onclick', `eliminarSucursal(${bIdx})`);
            }
        }
        const btnAddContact = branch.querySelector('.btn-add-contact');
        if (btnAddContact) btnAddContact.setAttribute('onclick', `agregarContacto(${bIdx})`);

        // Reindexar campos dentro de la sucursal: reemplazar branches[\d+] por branches[bIdx]
        // Prefer templates stored on the prototype elements when available
        const proto = branch.closest('#sucursales-container') ? branch.closest('#sucursales-container').querySelector('.branch-item.prototype') : null;
        if (proto) ensureTemplatesForPrototype(proto);
        // Apply templates if present on children; otherwise fallback to regex replace
        const elems = branch.querySelectorAll('[id],[name],[for],[data-name-template],[data-id-template],[data-for-template]');
        elems.forEach(el => {
            if (el.dataset && (el.dataset.nameTemplate || el.dataset.idTemplate || el.dataset.forTemplate)) {
                // apply template
                applyTemplatesToElement(el, { branch: bIdx });
            } else {
                try {
                    if (el.id) el.id = el.id.replace(/branches\[\s*\d+\s*\]/, `branches[${bIdx}]`);
                    if (el.name) el.name = el.name.replace(/branches\[\s*\d+\s*\]/, `branches[${bIdx}]`);
                    if (el.htmlFor) el.htmlFor = el.htmlFor.replace(/branches\[\s*\d+\s*\]/, `branches[${bIdx}]`);
                } catch (e) {}
            }
        });

        // Reindexar contacts dentro de esta sucursal
        reindexContacts(branch);
    });
    branchIndex = branches.length;
}

function reindexContacts(branchElement) {
    const bIdx = parseInt(branchElement.getAttribute('data-branch-index')) || 0;
    const contacts = branchElement.querySelectorAll('.contact-item:not(.prototype)');
    contacts.forEach((contact, cIdx) => {
        contact.setAttribute('data-contact-index', cIdx);
        const header = contact.querySelector('.contact-header h6');
        if (header) header.textContent = `Contacto #${cIdx + 1}`;
        const btn = contact.querySelector('.btn-remove-contact');
        if (btn) btn.setAttribute('onclick', `eliminarContacto(${bIdx}, ${cIdx})`);

        // Prefer templates if available from the parent prototype
        const proto = branchElement.closest('#sucursales-container') ? branchElement.closest('#sucursales-container').querySelector('.branch-item.prototype') : null;
        if (proto) ensureTemplatesForPrototype(proto);
        const elems = contact.querySelectorAll('[id],[name],[for],[data-name-template],[data-id-template],[data-for-template]');
        elems.forEach(el => {
            if (el.dataset && (el.dataset.nameTemplate || el.dataset.idTemplate || el.dataset.forTemplate)) {
                applyTemplatesToElement(el, { branch: bIdx, contact: cIdx });
            } else {
                try {
                    if (el.id) el.id = el.id.replace(/branches\[\s*\d+\s*\]/, `branches[${bIdx}]`).replace(/contacts\[\s*\d+\s*\]/, `contacts[${cIdx}]`);
                    if (el.name) el.name = el.name.replace(/branches\[\s*\d+\s*\]/, `branches[${bIdx}]`).replace(/contacts\[\s*\d+\s*\]/, `contacts[${cIdx}]`);
                    if (el.htmlFor) el.htmlFor = el.htmlFor.replace(/branches\[\s*\d+\s*\]/, `branches[${bIdx}]`).replace(/contacts\[\s*\d+\s*\]/, `contacts[${cIdx}]`);
                } catch (e) {}
            }
        });
    });
    // Bind handlers to ensure only one 'isPrimary' per branch is checked
    try {
        const primaryChecks = branchElement.querySelectorAll('.contact-is-primary');
        primaryChecks.forEach(chk => {
            // remove previous listeners to avoid duplicates (defensive)
            chk.addEventListener('change', function () {
                if (this.checked) {
                    primaryChecks.forEach(other => { if (other !== this) other.checked = false; });
                }
            });
        });
    } catch (e) {
        // ignore
    }
}

function actualizarVisibilidadEliminarSucursales() {
    const branches = document.querySelectorAll('.branch-item:not(.prototype)');
    branches.forEach((b, idx) => {
        const btn = b.querySelector('.btn-remove-branch');
        if (!btn) return;
        if (idx === 0) {
            // Ocultar y desactivar el botón eliminar para la primera sucursal
            try { btn.style.display = 'none'; } catch(e){}
            try { btn.disabled = true; } catch(e){}
            try { btn.removeAttribute('onclick'); } catch(e){}
            try { btn.setAttribute('aria-hidden', 'true'); } catch(e){}
        } else {
            try { btn.style.display = ''; } catch(e){}
            try { btn.disabled = false; } catch(e){}
            try { btn.removeAttribute('aria-hidden'); } catch(e){}
        }

        // contacts visibility first contact
        const contacts = b.querySelectorAll('.contact-item:not(.prototype)');
        const contactCount = contacts.length;
        contacts.forEach((c, ci) => {
            const btnc = c.querySelector('.btn-remove-contact');
            if (!btnc) return;
            // El primer contacto de cada sucursal no debe poder eliminarse.
            // Los contactos adicionales sí pueden eliminarse.
            if (ci === 0) {
                btnc.style.display = 'none';
            } else {
                btnc.style.display = '';
            }
        });
    });
}

// Elimina cualquier botón de eliminar accidental en la primera sucursal/contacto
function sanitizeFirstBranch() {
    try {
        const first = document.querySelector('.branch-item:not(.prototype)');
        if (!first) return;
        // remove branch remove button
        const brBtn = first.querySelector('.btn-remove-branch');
        if (brBtn) {
            try { brBtn.remove(); } catch(e) {}
        }
        // remove delete on first contact
        const firstContactBtn = first.querySelector('.contact-item:not(.prototype):first-child .btn-remove-contact');
        if (firstContactBtn) {
            try { firstContactBtn.remove(); } catch(e) {}
        }
        // ensure no onclick attributes
        try { first.querySelectorAll('[onclick]').forEach(el => el.removeAttribute('onclick')); } catch(e) {}
    } catch (e) {
        // ignore
    }
}

window.mostrarSeccionSucursales = function mostrarSeccionSucursales() {
    const seccion = document.getElementById('seccion-sucursales');
    if (!seccion) return;

    // Simple show (no nested overflow animation). Ensure visible and scroll into view.
    if (seccion.style.display === 'none' || getComputedStyle(seccion).display === 'none') {
        seccion.style.display = '';
    }
    // scroll the section into view for the user
    setTimeout(() => {
        try { seccion.scrollIntoView({ behavior: 'smooth', block: 'start' }); } catch(e) {}
    }, 60);
}

window.ocultarSeccionSucursales = function ocultarSeccionSucursales() {
    const seccion = document.getElementById('seccion-sucursales');
    if (!seccion) return;

    const container = document.getElementById('sucursales-container');
    if (container) {
        // remove only real branch items (not the prototype)
        const reales = container.querySelectorAll('.branch-item:not(.prototype)');
        reales.forEach(n => n.remove());
    }

    // reset index and UI
    branchIndex = 0;
    reindexAllBranches();
    actualizarVisibilidadEliminarSucursales();

    // hide the section
    seccion.style.display = 'none';
    // disable inputs inside the (now hidden) section
    const controls = seccion.querySelectorAll('input, select, textarea');
    controls.forEach(c => { c.disabled = true; });
}

// Mostrar la sección de sucursales y añadir una sucursal editable si es necesario.
// - Si la sección está oculta: se muestra y la propia mostrarSeccionSucursales() añadirá la
//   primera sucursal si no existe ninguna.
// - Si ya está visible: agrega inmediatamente una nueva sucursal.
window.mostrarYAgregarSucursal = function mostrarYAgregarSucursal() {
    const seccion = document.getElementById('seccion-sucursales');
    const container = document.getElementById('sucursales-container');
    if (!seccion || !container) {
        console.warn('Sección de sucursales o contenedor no encontrados.');
        return;
    }
    // Ensure the section is visible
    if (seccion.style.display === 'none' || getComputedStyle(seccion).display === 'none') {
        mostrarSeccionSucursales();
    }

    // If there are no real branches, add the first one; otherwise add a new one
    // Añadir siempre una nueva sucursal (la primera ya existirá al cargar en la página nueva)
    agregarSucursal();

    // Scroll to the newly added branch and focus its nombre input
    setTimeout(() => {
        const lastBranch = container.querySelector('.branch-item:not(.prototype):last-child');
        if (lastBranch) {
            try { lastBranch.scrollIntoView({ behavior: 'smooth', block: 'center' }); } catch(e) {}
            const input = lastBranch.querySelector('input[name$=".nombre"], input[id$=".nombre"]');
            if (input) input.focus();
        }
    }, 80);
};

window.agregarSucursal = function agregarSucursal() {
    const container = document.getElementById('sucursales-container');
    if (!container) return;
    const source = container.querySelector('.branch-item.prototype');
    let nuevo;
    if (source) {
    // Ensure the prototype has data-name/id templates set
    ensureTemplatesForPrototype(source);
    nuevo = source.cloneNode(true);
    nuevo.classList.remove('prototype');
    // mantener oculto hasta completar reindexado y limpieza para evitar parpadeos
    nuevo.style.display = 'none';
        limpiarControlesDentro(nuevo);
        // enable controls
        const controls = nuevo.querySelectorAll('input, select, textarea');
        controls.forEach(c => c.disabled = false);
    // determine new index before appending
    const newIndex = container.querySelectorAll('.branch-item:not(.prototype)').length;
    container.appendChild(nuevo);
    // apply templates to the new element (branch index), contacts will be set later
    applyTemplatesToElement(nuevo, { branch: newIndex });
    // Reindex immediately so branchIndex y data-branch-index estén actualizados
    reindexAllBranches();
        // Asegurar que la sucursal recién añadida (si es la primera) no tenga botón eliminar en el DOM
        try {
            const maybeFirst = container.querySelector('.branch-item:not(.prototype):last-child');
            if (maybeFirst) {
                const idx = parseInt(maybeFirst.getAttribute('data-branch-index')) || 0;
                if (idx === 0) {
                    const btnRem = maybeFirst.querySelector('.btn-remove-branch');
                    if (btnRem) btnRem.remove();
                    // también eliminar el botón eliminar del primer contacto si existe
                    const firstContactBtn = maybeFirst.querySelector('.contact-item:not(.prototype):first-child .btn-remove-contact');
                    if (firstContactBtn) firstContactBtn.remove();
                }
            }
        } catch (e) { /* ignore */ }
        // Asegurarnos de que la nueva sucursal tenga exactamente UN contacto real.
        try {
                const newIndex = branchIndex - 1; // índice del branch recién añadido
            const branchEl = document.querySelector(`.branch-item:not(.prototype)[data-branch-index="${newIndex}"]`);
            if (branchEl) {
                // Eliminar cualquier contacto real que haya venido en la plantilla/clon
                const realContacts = branchEl.querySelectorAll('.contact-item:not(.prototype)');
                realContacts.forEach(c => c.remove());
                // Añadir exactamente un contacto por defecto
                if (typeof agregarContacto === 'function') {
                    agregarContacto(newIndex);
                    // reindex y actualizar botones tras la adición
                    reindexAllBranches();
                    actualizarVisibilidadEliminarSucursales();
                    setTimeout(() => {
                        const input = branchEl.querySelector('input[name$=".contacts[0].nombre"], input[name$=".nombre"], input[id$=".nombre"]');
                        if (input) input.focus();
                    }, 60);
                    }
            }
        } catch (e) {
            console.warn('No se pudo asegurar un único contacto por defecto:', e);
        }
        // Mostrar la sucursal sólo cuando esté totalmente preparada
        try { nuevo.style.display = ''; } catch (e) {}
    }
}

window.eliminarSucursal = function eliminarSucursal(index) {
    // Prevent removing the first branch (index 0)
    if (parseInt(index) === 0) {
        alert('La primera sucursal no puede eliminarse.');
        return;
    }
    const sel = document.querySelector(`.branch-item:not(.prototype)[data-branch-index="${index}"]`);
    if (sel) {
        // confirm if there are any filled inputs
        const hasData = Array.from(sel.querySelectorAll('input,textarea,select')).some(c => {
            if (c.type === 'checkbox' || c.type === 'radio') return c.checked;
            return (c.value || '').toString().trim().length > 0;
        });
        if (hasData) {
            if (!confirm('Esta sucursal tiene datos. ¿Seguro que deseas eliminarla?')) return;
        }
        sel.remove();
        reindexAllBranches();
        actualizarVisibilidadEliminarSucursales();
    }
}

window.agregarContacto = function agregarContacto(branchIdx) {
    const branch = document.querySelector(`.branch-item:not(.prototype)[data-branch-index="${branchIdx}"]`);
    if (!branch) return;
    const container = branch.querySelector('.contacts-container');
    if (!container) return;
    const source = container.querySelector('.contact-item.prototype');
    let nuevo;
    if (source) {
        // ensure templates exist on the branch prototype
        const branchProto = branch.closest('#sucursales-container') ? branch.closest('#sucursales-container').querySelector('.branch-item.prototype') : null;
        if (branchProto) ensureTemplatesForPrototype(branchProto);
        nuevo = source.cloneNode(true);
        nuevo.classList.remove('prototype');
        // ocultar hasta reindexar para evitar parpadeos
        nuevo.style.display = 'none';
        limpiarControlesDentro(nuevo);
        const controls = nuevo.querySelectorAll('input, select, textarea');
        controls.forEach(c => c.disabled = false);
        // compute new contact index before append
        const newContactIndex = container.querySelectorAll('.contact-item:not(.prototype)').length;
        container.appendChild(nuevo);
        // apply templates for branch+contact indices
        applyTemplatesToElement(nuevo, { branch: branchIdx, contact: newContactIndex });
        reindexAllBranches(); // will reindex contacts as well
        actualizarVisibilidadEliminarSucursales();
        try { nuevo.style.display = ''; } catch (e) {}
    }
}

window.eliminarContacto = function eliminarContacto(branchIdx, contactIdx) {
    const branch = document.querySelector(`.branch-item:not(.prototype)[data-branch-index="${branchIdx}"]`);
    if (!branch) return;
    const contact = branch.querySelector(`.contact-item:not(.prototype)[data-contact-index="${contactIdx}"]`);
    if (contact) {
        const hasData = Array.from(contact.querySelectorAll('input,textarea,select')).some(c => {
            if (c.type === 'checkbox' || c.type === 'radio') return c.checked;
            return (c.value || '').toString().trim().length > 0;
        });
        if (hasData) {
            if (!confirm('Este contacto tiene datos. ¿Seguro que deseas eliminarlo?')) return;
        }
        contact.remove();
        reindexAllBranches();
        actualizarVisibilidadEliminarSucursales();
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
        // Ensure templates exist on product prototype
        if (source.classList.contains('prototype')) ensureTemplatesForPrototype(source);
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
        const newIndex = container.querySelectorAll('.producto-item:not(.prototype)').length;
        container.appendChild(nuevoProducto);
        applyTemplatesToElement(nuevoProducto, { product: newIndex });
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

// Oculta la sección de productos y elimina los productos reales añadidos (deja el prototipo intacto)
window.ocultarSeccionProductos = function ocultarSeccionProductos() {
    const seccion = document.getElementById('seccion-productos');
    const btn = document.getElementById('btn-mostrar-productos');
    const container = document.getElementById('productos-container');
    if (!seccion || !container) return;

    // Remove only real product items (not the prototype)
    const reales = container.querySelectorAll('.producto-item:not(.prototype)');
    reales.forEach(n => n.remove());

    // Reset index and reindex (will only find prototype)
    productoIndex = 0;
    reindexAllProducts();
    actualizarVisibilidadEliminar();

    // Hide section and re-enable the show button
    seccion.style.display = 'none';
    if (btn) btn.style.display = '';

    // Disable controls inside section (including prototype)
    const controls = seccion.querySelectorAll('input, select, textarea');
    controls.forEach(c => { c.disabled = true; });

    // Focus back to the boton mostrar
    if (btn) btn.focus();
}
