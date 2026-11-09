import { StatusBar } from 'expo-status-bar';
import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Image,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { API_URL, crearProducto, eliminarProducto, listarProductos, salud } from './src/api';

const formularioVacio = { nombre: '', precio: '', stock: '' };
// El teclado numérico de iPhone no trae la tecla "-": este sí, para poder probar el 400 con -1.
const tecladoNumerico = Platform.OS === 'ios' ? 'numbers-and-punctuation' : 'numeric';

export default function App() {
  const [productos, setProductos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [estadoApi, setEstadoApi] = useState('revisando...');
  const [form, setForm] = useState(formularioVacio);
  const [errores, setErrores] = useState({}); // errores por campo que manda el 400
  const [aviso, setAviso] = useState(null);

  async function cargar() {
    setCargando(true);
    setError(null);
    try {
      setProductos(await listarProductos());
    } catch (e) {
      setError(e.message);
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    salud()
      .then((r) => setEstadoApi(r.estado === 'ok' ? 'despierta ✅' : 'respuesta rara'))
      .catch(() => setEstadoApi('no responde ❌'));
    cargar();
  }, []);

  async function guardar() {
    setErrores({});
    setAviso(null);
    // Los inputs entregan texto: el precio y el stock se envían como número.
    // Si el campo está vacío se envía null, y el backend responde 400 "obligatorio".
    const producto = {
      nombre: form.nombre,
      precio: form.precio === '' ? null : Number(form.precio),
      stock: form.stock === '' ? null : Number(form.stock),
    };
    try {
      const creado = await crearProducto(producto);
      setAviso(`201 Created: producto #${creado.id} guardado en Neon`);
      setForm(formularioVacio);
      cargar();
    } catch (e) {
      if (e.status === 400) {
        setErrores(e.campos);
        setAviso(`400 Bad Request: ${e.mensaje}`);
      } else {
        setAviso(e.message);
      }
    }
  }

  async function borrar(id) {
    setAviso(null);
    try {
      await eliminarProducto(id);
      setAviso(`204 No Content: producto #${id} eliminado`);
      cargar();
    } catch (e) {
      setAviso(e.message);
    }
  }

  function confirmarBorrado(item) {
    const pregunta = `¿Borrar "${item.nombre}" (#${item.id})?`;
    if (Platform.OS === 'web') {
      if (window.confirm(pregunta)) borrar(item.id);
      return;
    }
    Alert.alert('Borrar producto', pregunta, [
      { text: 'Cancelar', style: 'cancel' },
      { text: 'Borrar', style: 'destructive', onPress: () => borrar(item.id) },
    ]);
  }

  return (
    <View style={styles.pantalla}>
      <StatusBar style="dark" />
      <Text style={styles.titulo}>Tienda Render</Text>
      <Text style={styles.api} numberOfLines={1}>API: {API_URL}</Text>
      <Text style={styles.api}>Estado: {estadoApi}</Text>

      <View style={styles.tarjeta}>
        <Text style={styles.subtitulo}>Nuevo producto (POST)</Text>
        <TextInput
          style={styles.input}
          placeholder="Nombre"
          value={form.nombre}
          onChangeText={(nombre) => setForm({ ...form, nombre })}
        />
        {errores.nombre && <Text style={styles.errorCampo}>{errores.nombre}</Text>}
        <TextInput
          style={styles.input}
          placeholder="Precio"
          keyboardType={tecladoNumerico}
          value={form.precio}
          onChangeText={(precio) => setForm({ ...form, precio })}
        />
        {errores.precio && <Text style={styles.errorCampo}>{errores.precio}</Text>}
        <TextInput
          style={styles.input}
          placeholder="Stock"
          keyboardType={tecladoNumerico}
          value={form.stock}
          onChangeText={(stock) => setForm({ ...form, stock })}
        />
        {errores.stock && <Text style={styles.errorCampo}>{errores.stock}</Text>}
        <Pressable style={styles.boton} onPress={guardar} accessibilityRole="button">
          <Text style={styles.textoBoton}>Guardar</Text>
        </Pressable>
        {aviso && <Text style={styles.aviso}>{aviso}</Text>}
      </View>

      <View style={styles.filaTitulo}>
        <Text style={styles.subtitulo}>
          Productos (GET{!cargando && !error ? ` → 200 OK · ${productos.length}` : ''})
        </Text>
        <Pressable onPress={cargar} accessibilityRole="button">
          <Text style={styles.link}>Recargar</Text>
        </Pressable>
      </View>

      {cargando && (
        <View style={styles.centro}>
          <ActivityIndicator size="large" />
          <Text style={styles.ayuda}>Si la API estaba dormida, la primera respuesta puede tardar ~1 minuto.</Text>
        </View>
      )}
      {error && <Text style={styles.error}>No se pudo cargar: {error}</Text>}
      {!cargando && !error && (
        <FlatList
          data={productos}
          keyExtractor={(p) => String(p.id)}
          ListEmptyComponent={<Text style={styles.ayuda}>No hay productos todavía.</Text>}
          renderItem={({ item }) => (
            <View style={styles.producto}>
              {item.imagenUrl ? (
                <Image source={{ uri: item.imagenUrl }} style={styles.foto} />
              ) : (
                <View style={[styles.foto, styles.sinFoto]}>
                  <Text style={styles.ayuda}>sin foto</Text>
                </View>
              )}
              <View style={styles.datos}>
                <Text style={styles.nombre}>#{item.id} {item.nombre}</Text>
                <Text>${item.precio.toLocaleString('es-CL')} · stock {item.stock}</Text>
              </View>
              <Pressable onPress={() => confirmarBorrado(item)} accessibilityRole="button" accessibilityLabel={`Borrar ${item.nombre}`}>
                <Text style={styles.borrar}>Borrar</Text>
              </Pressable>
            </View>
          )}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  pantalla: { flex: 1, backgroundColor: '#f5f6f8', paddingHorizontal: 16, paddingTop: 48 },
  titulo: { fontSize: 26, fontWeight: '700' },
  api: { color: '#555', fontSize: 12 },
  tarjeta: { backgroundColor: '#fff', borderRadius: 10, padding: 12, marginVertical: 12 },
  subtitulo: { fontSize: 17, fontWeight: '600', marginBottom: 6 },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 6, padding: 8, marginTop: 6, backgroundColor: '#fff' },
  errorCampo: { color: '#c62828', fontSize: 13, marginTop: 2 },
  boton: { backgroundColor: '#1d4ed8', borderRadius: 6, padding: 10, marginTop: 10, alignItems: 'center' },
  textoBoton: { color: '#fff', fontWeight: '600' },
  aviso: { marginTop: 8, fontWeight: '600' },
  filaTitulo: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  link: { color: '#1d4ed8', fontWeight: '600' },
  centro: { alignItems: 'center', marginTop: 20 },
  ayuda: { color: '#777', fontSize: 12, textAlign: 'center' },
  error: { color: '#c62828', marginTop: 10 },
  producto: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#fff', borderRadius: 10, padding: 8, marginBottom: 8 },
  foto: { width: 56, height: 56, borderRadius: 6, marginRight: 10 },
  sinFoto: { backgroundColor: '#eee', justifyContent: 'center' },
  datos: { flex: 1 },
  nombre: { fontWeight: '600' },
  borrar: { color: '#c62828', fontWeight: '600' },
});
