(function(){
  const denseToggle = document.getElementById('toggleDenseProveedores');
  const table = document.querySelector('.table-admin');
  const pageSizeSelector = document.getElementById('pageSizeSelector');

  // Densidad compacta con persistencia
  const STORAGE_KEY = 'proveedoresTableDensity';
  const saved = localStorage.getItem(STORAGE_KEY);
  if (table && saved === 'dense') {
    table.classList.add('dense');
    if (denseToggle) denseToggle.checked = true;
  }
  if (denseToggle && table) {
    denseToggle.addEventListener('change', () => {
      table.classList.toggle('dense', denseToggle.checked);
      localStorage.setItem(STORAGE_KEY, denseToggle.checked ? 'dense' : 'normal');
    });
  }

  // Selector de tamaño de página con filtrado
  let pageSize = 10;
  if (pageSizeSelector) {
    // Restaurar preferencia guardada
    const savedSize = localStorage.getItem('proveedoresPageSize');
    if (savedSize) {
      pageSizeSelector.value = savedSize;
      pageSize = parseInt(savedSize);
    }
    
    pageSizeSelector.addEventListener('change', () => {
      pageSize = parseInt(pageSizeSelector.value);
      localStorage.setItem('proveedoresPageSize', pageSize);
      applyFilters();
    });
  }

  // Filtros en tiempo real
  const filterNombre = document.getElementById('filterNombre');
  const filterNit = document.getElementById('filterNit');
  const filterEstado = document.getElementById('filterEstado');
  const filterCiudad = document.getElementById('filterCiudad');
  const tbody = document.querySelector('.table-admin tbody');
  let allRows = [];
  
  if (tbody) {
    // Guardar todas las filas originales
    allRows = Array.from(tbody.querySelectorAll('tr')).filter(row => !row.querySelector('.empty-message'));
    
    function applyFilters() {
      const nombreValue = filterNombre ? filterNombre.value.toLowerCase().trim() : '';
      const nitValue = filterNit ? filterNit.value.toLowerCase().trim() : '';
      const estadoValue = filterEstado ? filterEstado.value.toLowerCase() : '';
      const ciudadValue = filterCiudad ? filterCiudad.value.toLowerCase().trim() : '';
      
      let filteredRows = allRows.filter(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length === 0) return false;
        
        const nombre = cells[1]?.textContent.toLowerCase() || '';
        const nit = cells[2]?.textContent.toLowerCase() || '';
        const ciudad = cells[5]?.textContent.toLowerCase() || '';
        const estado = cells[7]?.textContent.toLowerCase() || '';
        
        const matchNombre = !nombreValue || nombre.includes(nombreValue);
        const matchNit = !nitValue || nit.includes(nitValue);
        const matchCiudad = !ciudadValue || ciudad.includes(ciudadValue);
        const matchEstado = !estadoValue || estado.includes(estadoValue);
        
        return matchNombre && matchNit && matchCiudad && matchEstado;
      });
      
      // Limpiar tbody
      tbody.innerHTML = '';
      
      if (filteredRows.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="empty-message"><i class="fas fa-search"></i><br/>No se encontraron proveedores con los filtros aplicados.</td></tr>';
      } else {
        // Aplicar paginación
        const visibleRows = filteredRows.slice(0, pageSize);
        visibleRows.forEach(row => tbody.appendChild(row));
        
        // Mostrar mensaje si hay más resultados
        if (filteredRows.length > pageSize) {
          const infoRow = document.createElement('tr');
          infoRow.innerHTML = `<td colspan="9" class="empty-message" style="background:#f8f9fa; padding:0.5rem; font-size:0.85rem;"><i class="fas fa-info-circle"></i> Mostrando ${pageSize} de ${filteredRows.length} resultados. Cambia el tamaño de página para ver más.</td>`;
          tbody.appendChild(infoRow);
        }
      }
    }
    
    // Event listeners para filtros
    if (filterNombre) filterNombre.addEventListener('input', applyFilters);
    if (filterNit) filterNit.addEventListener('input', applyFilters);
    if (filterEstado) filterEstado.addEventListener('change', applyFilters);
    if (filterCiudad) filterCiudad.addEventListener('input', applyFilters);
    
    // Aplicar filtros iniciales con el pageSize cargado
    applyFilters();
  }
})();

// Crear sucursal: mostrar selector y redirigir
(function(){
  const btnShow = document.getElementById('btnShowCreateBranch');
  const box = document.getElementById('createBranchBox');
  const btnCreate = document.getElementById('btnCreateBranch');
  const btnCancel = document.getElementById('btnCancelCreateBranch');
  const select = document.getElementById('selectProveedorForBranch');

  if (btnShow && box) {
    btnShow.addEventListener('click', () => {
      box.style.display = (box.style.display === 'none' || box.style.display === '') ? 'block' : 'none';
    });
  }
  if (btnCancel && box) {
    btnCancel.addEventListener('click', () => { box.style.display = 'none'; select.value = ''; });
  }
  if (btnCreate && select) {
    btnCreate.addEventListener('click', () => {
      const id = select.value;
      if (!id) {
        alert('Seleccione un proveedor antes de crear la sucursal.');
        return;
      }
      // Navegar a la ruta de crear sucursal para el proveedor
      window.location.href = `/admin/proveedores/${id}/sucursales/nuevo`;
    });
  }
})();
