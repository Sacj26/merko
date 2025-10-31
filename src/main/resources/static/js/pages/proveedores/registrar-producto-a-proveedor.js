/* Inicialización del formulario y manejo de productos dinámicos para registrar-producto-a-proveedor */
let productoIndex = 1; // Ya tenemos el producto 0

(function initFormBehavior(){
  document.addEventListener('DOMContentLoaded', function() {
      const selectProveedor = document.getElementById('proveedorId');
      const form = document.getElementById('form-productos');
      const btnSubmit = form?.querySelector('button[type="submit"]');

      if (btnSubmit) btnSubmit.disabled = true;

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

      if (selectProveedor) selectProveedor.addEventListener('change', actualizarAction);

      if (form) {
          form.addEventListener('submit', function(e) {
              if (!selectProveedor?.value) {
                  e.preventDefault();
                  alert('Por favor, seleccione un proveedor antes de guardar.');
                  selectProveedor?.focus();
              }
          });
      }
  });
})();

window.agregarProducto = function agregarProducto() {
  const container = document.getElementById('productos-container');
  const nuevoProducto = document.createElement('div');
  nuevoProducto.className = 'producto-item';
  nuevoProducto.setAttribute('data-producto-index', productoIndex);
  
  nuevoProducto.innerHTML = `
      <div class="producto-header">
          <h4>Producto #${productoIndex + 1}</h4>
          <button type="button" class="btn-remove-producto" onclick="eliminarProducto(${productoIndex})">
              <i class="fas fa-trash-alt"></i> Eliminar
          </button>
      </div>
      <div class="form-grid">
          <!-- Información Básica -->
          <div class="form-group">
              <label for="productos[${productoIndex}].nombre">Nombre del Producto: <span class="required">*</span></label>
              <input type="text" id="productos[${productoIndex}].nombre" name="productos[${productoIndex}].nombre" class="form-control" required />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].sku">SKU:</label>
              <input type="text" id="productos[${productoIndex}].sku" name="productos[${productoIndex}].sku" class="form-control" />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].codigoBarras">Código de Barras:</label>
              <input type="text" id="productos[${productoIndex}].codigoBarras" name="productos[${productoIndex}].codigoBarras" class="form-control" />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].marca">Marca:</label>
              <input type="text" id="productos[${productoIndex}].marca" name="productos[${productoIndex}].marca" class="form-control" />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].tipo">Tipo de Producto: <span class="required">*</span></label>
              <select id="productos[${productoIndex}].tipo" name="productos[${productoIndex}].tipo" class="form-control" required>
                  <option value="">Seleccione...</option>
                  <option value="MATERIA_PRIMA" selected>Materia prima</option>
                  <option value="PRODUCTO_TERMINADO">Producto terminado</option>
              </select>
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].estado">Estado: <span class="required">*</span></label>
              <select id="productos[${productoIndex}].estado" name="productos[${productoIndex}].estado" class="form-control" required>
                  <option value="">Seleccione...</option>
                  <option value="ACTIVO" selected>Activo</option>
                  <option value="INACTIVO">Inactivo</option>
              </select>
          </div>

          <div class="form-group" style="grid-column: 1 / -1;">
              <label for="productos[${productoIndex}].descripcion">Descripción:</label>
              <textarea id="productos[${productoIndex}].descripcion" name="productos[${productoIndex}].descripcion" class="form-control" rows="2"></textarea>
          </div>

          <!-- Presentación y Unidades -->
          <div class="form-group">
              <label for="productos[${productoIndex}].unidadMedida">Unidad de Medida:</label>
              <select id="productos[${productoIndex}].unidadMedida" name="productos[${productoIndex}].unidadMedida" class="form-control">
                  <option value="">Seleccione...</option>
                  <option value="KILOGRAMOS">Kilogramos</option>
                  <option value="GRAMOS">Gramos</option>
                  <option value="LITROS">Litros</option>
                  <option value="MILILITROS">Mililitros</option>
                  <option value="UNIDADES">Unidades</option>
                  <option value="CAJAS">Cajas</option>
                  <option value="PAQUETES">Paquetes</option>
                  <option value="PORCION">Porcion</option>
              </select>
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].contenidoNeto">Contenido Neto:</label>
              <input type="number" id="productos[${productoIndex}].contenidoNeto" name="productos[${productoIndex}].contenidoNeto" class="form-control" step="0.01" min="0" />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].contenidoUoM">Unidad del Contenido:</label>
              <select id="productos[${productoIndex}].contenidoUoM" name="productos[${productoIndex}].contenidoUoM" class="form-control">
                  <option value="">Seleccione...</option>
                  <option value="KILOGRAMOS">Kilogramos</option>
                  <option value="GRAMOS">Gramos</option>
                  <option value="LITROS">Litros</option>
                  <option value="MILILITROS">Mililitros</option>
                  <option value="UNIDADES">Unidades</option>
                  <option value="CAJAS">Cajas</option>
                  <option value="PAQUETES">Paquetes</option>
                  <option value="PORCION">Porcion</option>
              </select>
          </div>

          <!-- Precios e Inventario -->
          <div class="form-group">
              <label for="productos[${productoIndex}].precioCompra">Precio de Compra: <span class="required">*</span></label>
              <input type="number" id="productos[${productoIndex}].precioCompra" name="productos[${productoIndex}].precioCompra" class="form-control" step="0.01" min="0" required />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].precioVenta">Precio de Venta: <span class="required">*</span></label>
              <input type="number" id="productos[${productoIndex}].precioVenta" name="productos[${productoIndex}].precioVenta" class="form-control" step="0.01" min="0" required />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].stock">Stock Inicial: <span class="required">*</span></label>
              <input type="number" id="productos[${productoIndex}].stock" name="productos[${productoIndex}].stock" class="form-control" min="0" required />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].stockMinimo">Stock Mínimo:</label>
              <input type="number" id="productos[${productoIndex}].stockMinimo" name="productos[${productoIndex}].stockMinimo" class="form-control" min="0" />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].puntoReorden">Punto de Reorden:</label>
              <input type="number" id="productos[${productoIndex}].puntoReorden" name="productos[${productoIndex}].puntoReorden" class="form-control" min="0" />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].leadTimeDias">Lead Time (días):</label>
              <input type="number" id="productos[${productoIndex}].leadTimeDias" name="productos[${productoIndex}].leadTimeDias" class="form-control" min="0" />
          </div>

          <!-- Lotes y Vencimientos -->
          <div class="form-group" style="grid-column: 1 / -1;">
              <div style="display: flex; gap: 2rem; align-items: center; flex-wrap: wrap;">
                  <label style="margin: 0;">
                      <input type="checkbox" id="productos[${productoIndex}].gestionaLotes" name="productos[${productoIndex}].gestionaLotes" value="true" />
                      Gestiona Lotes
                  </label>
                  <label style="margin: 0;">
                      <input type="checkbox" id="productos[${productoIndex}].requiereVencimiento" name="productos[${productoIndex}].requiereVencimiento" value="true" />
                      Requiere Control de Vencimiento
                  </label>
              </div>
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].vidaUtilDias">Vida Útil (días):</label>
              <input type="number" id="productos[${productoIndex}].vidaUtilDias" name="productos[${productoIndex}].vidaUtilDias" class="form-control" min="0" />
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].almacenamiento">Almacenamiento:</label>
              <select id="productos[${productoIndex}].almacenamiento" name="productos[${productoIndex}].almacenamiento" class="form-control">
                  <option value="">Seleccione...</option>
                  <option value="AMBIENTE">Ambiente</option>
                  <option value="REFRIGERADO">Refrigerado</option>
                  <option value="CONGELADO">Congelado</option>
                  <option value="CONTROLADO">Controlado</option>
              </select>
          </div>

          <div class="form-group">
              <label for="productos[${productoIndex}].registroSanitario">Registro Sanitario:</label>
              <input type="text" id="productos[${productoIndex}].registroSanitario" name="productos[${productoIndex}].registroSanitario" class="form-control" />
          </div>

          <!-- Imagen -->
          <div class="form-group" style="grid-column: 1 / -1;">
              <label for="productos[${productoIndex}].imagenUrl">Imagen del Producto:</label>
              <input type="file" id="productos[${productoIndex}].imagenUrl" name="productos[${productoIndex}].imagenUrl" class="form-control" accept="image/*" />
          </div>
      </div>
  `;
  
  container.appendChild(nuevoProducto);
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
}
