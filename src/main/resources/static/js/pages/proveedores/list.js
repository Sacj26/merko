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

  // Selector de tamaño de página (placeholder - requiere backend con paginación)
  if (pageSizeSelector) {
    pageSizeSelector.addEventListener('change', () => {
      // Por ahora solo guarda la preferencia
      localStorage.setItem('proveedoresPageSize', pageSizeSelector.value);
      // Si hay soporte de paginación en el backend, aquí iría la lógica de recarga
      console.log('Tamaño de página cambiado a:', pageSizeSelector.value);
    });
    
    // Restaurar preferencia guardada
    const savedSize = localStorage.getItem('proveedoresPageSize');
    if (savedSize) {
      pageSizeSelector.value = savedSize;
    }
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
