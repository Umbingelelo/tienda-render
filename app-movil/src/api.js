// Todas las llamadas a la API viven en este archivo.
// La pantalla (App.js) nunca escribe una URL: solo llama a estas funciones.

// 👇 CAMBIA ESTA LÍNEA por la URL de TU servicio en Render (sin "/" al final).
//    La que viene es la API del profesor: sirve para probar, pero tu tienda debe usar la tuya.
export const API_URL = 'https://tienda-render-km7q.onrender.com';

// GET /api/health -> 200 {"estado":"ok"}. Sirve para saber si la API está despierta.
export async function salud() {
  const res = await fetch(`${API_URL}/api/health`);
  if (!res.ok) throw new Error(`Error ${res.status}`);
  return res.json();
}

// GET /api/productos -> 200 + lista
export async function listarProductos() {
  const res = await fetch(`${API_URL}/api/productos`);
  if (!res.ok) throw new Error(`Error ${res.status}`);
  return res.json();
}

// POST /api/productos -> 201 + producto creado, o 400 + { campos: {...} }
export async function crearProducto(producto) {
  const res = await fetch(`${API_URL}/api/productos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(producto),
  });
  const data = await res.json();
  if (res.status === 400) throw { status: 400, campos: data.campos ?? {}, mensaje: data.mensaje };
  if (!res.ok) throw new Error(`Error ${res.status}`);
  return data; // 201
}

// DELETE /api/productos/{id} -> 204 sin cuerpo (por eso NO se llama a res.json()), o 404
export async function eliminarProducto(id) {
  const res = await fetch(`${API_URL}/api/productos/${id}`, { method: 'DELETE' });
  if (res.status === 404) throw new Error(`El producto ${id} ya no existe (404)`);
  if (!res.ok) throw new Error(`Error ${res.status}`);
}
