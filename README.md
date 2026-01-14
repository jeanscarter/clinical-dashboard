# Clinical Management System

Sistema de Gestión Clínica desarrollado en Java 17 con Swing.

## Características

- ✅ Gestión de Pacientes (CRUD completo)
- ✅ Historias Clínicas con adjuntos
- ✅ Sistema de Autenticación con roles
- ✅ Reportes en HTML y CSV
- ✅ Backup y restauración
- ✅ Import/Export de datos
- ✅ Tema moderno con FlatLaf

## Requisitos

- Java 17+
- Maven 3.6+

## Instalación

```bash
# Clonar el repositorio
git clone <repository-url>
cd clinical-dashboard

# Compilar
mvn clean compile

# Ejecutar
mvn exec:java -Dexec.mainClass="com.cms.Main"

# Crear JAR ejecutable
mvn package
java -jar target/clinical-dashboard-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Credenciales por Defecto

- **Usuario:** admin
- **Contraseña:** admin123

## Estructura del Proyecto

```
src/main/java/com/cms/
├── core/validation/    # Validadores
├── di/                 # Inyección de dependencias
├── domain/             # Entidades de dominio
├── infra/              # Infraestructura (DB, logging)
├── presenter/          # Presentadores MVP
├── repository/         # Persistencia
├── service/            # Lógica de negocio
├── ui/                 # Interfaz de usuario
│   ├── components/     # Componentes reutilizables
│   ├── dialogs/        # Diálogos
│   └── views/          # Vistas principales
└── util/               # Utilidades
```

## Atajos de Teclado

| Atajo    | Acción              |
|----------|---------------------|
| Ctrl+N   | Nuevo               |
| Ctrl+S   | Guardar             |
| Ctrl+F   | Buscar              |
| Ctrl+Q   | Cerrar sesión       |
| F1       | Ayuda               |
| Escape   | Cancelar/Cerrar     |

## Roles de Usuario

- **ADMIN**: Acceso completo
- **DOCTOR**: Gestión de pacientes e historias
- **RECEPCIONISTA**: Solo lectura de historias

## Base de Datos

SQLite con las siguientes tablas:
- `patients` - Pacientes
- `clinical_histories` - Historias clínicas
- `attachments` - Adjuntos
- `users` - Usuarios del sistema

## Licencia

MIT License
