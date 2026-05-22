# LoreRuneTerra

**Aplicación de escritorio para la gestión del universo narrativo de League of Legends**

![Java](https://img.shields.io/badge/Java-23-orange?logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-25.0.2-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-336791?logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-3.9-red?logo=apachemaven)
![Gson](https://img.shields.io/badge/Gson-2.11.0-4285F4?logo=google)
![JDBC](https://img.shields.io/badge/JDBC-4.2-darkblue)
![Status](https://img.shields.io/badge/Status-En%20desarrollo-yellow)
![TFC](https://img.shields.io/badge/TFC-DAM%202024--2026-purple)

![Tests](https://github.com/franzcelta/LoreRuneTerra/actions/workflows/tests.yml/badge.svg)

Trabajo de Fin de Ciclo — Desarrollo de Aplicaciones Multiplataforma (DAM)  
Autor: Francisco Andrés Manzo Cabrera | Curso 2024–2026

## Descargar

[![Download](https://img.shields.io/badge/Download-v1.0-brightgreen)](https://github.com/franzcelta/LoreRuneTerra/releases/tag/v1.0)

> **Requisitos:** Windows 10/11 + PostgreSQL 18 instalado y configurado.

---

## ¿Qué es LoreRuneTerra?

LoreRuneTerra es una aplicación de escritorio Java que permite explorar, gestionar y personalizar el universo narrativo (lore) del videojuego League of Legends. Funciona **offline** con una base de datos PostgreSQL local, y permite sincronizar datos con la API pública DataDragon de Riot Games bajo demanda.

---

## Funcionalidades principales

- **Catálogo de campeones** — 172 campeones con imagen, clase y región. Filtros combinables por nombre, clase y región en tiempo real.
- **Libro del campeón** — Splashart HD con pasador de skins, badges de clase/región y 3 modalidades de biografía (corta, completa, primera persona).
- **CRUD completo** — Crear, editar y eliminar campeones y sus biografías con persistencia real en PostgreSQL.
- **Dashboard de estadísticas** — KPIs, gráfico de barras por clase y gráfico circular por región implementado con Arc de JavaFX.
- **Vista de Regiones** — 10 regiones de Runeterra con imagen, descripción y vista de detalle con los campeones de cada región.
- **Importación DataDragon** — Sincronización con la API REST oficial de Riot Games con log en tiempo real y barra de progreso.
- **Exportación PDF** — Genera una ficha completa del campeón con imagen y biografía usando iText 7.
- **Tests de integración** — 23 tests con JUnit 5 que cubren los 3 DAOs principales, ejecutados automáticamente en cada push mediante GitHub Actions.

---

## Tecnologías

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 23 | Lenguaje principal |
| JavaFX | 25.0.2 | Interfaz gráfica |
| PostgreSQL | 18 | Base de datos local |
| JDBC | 4.2 | Acceso a datos |
| Gson | 2.11.0 | Parseo JSON (DataDragon) |
| Maven | 3.9 | Gestión de dependencias |
| iText 7 | 7.2.5 | Exportación de fichas en PDF |
| JUnit 5 | 5.10.2 | Tests de integración |

---

## Arquitectura

El proyecto sigue el patrón **MVC + DAO**:

## Arquitectura

El proyecto sigue el patrón **MVC + DAO**:

    com.loreruneterra/
    ├── controller/     # MainController — navegación, CRUD, animaciones
    ├── db/             # ChampionDAO, CampeonPersonalDAO, PlacesDAO, DatabaseConnector
    ├── export/         # ChampionPDFExporter — exportación PDF con iText 7
    ├── importer/       # DataDragonImporter — API REST Riot Games
    ├── model/          # Campeon, Lugar
    ├── view/           # ChampionBookView, DashboardView, BiographyEditorDialog
    └── MainApp.java    # Punto de entrada

---

## Requisitos

- Java 23+
- PostgreSQL 18+
- Maven 3.9+

---

## Configuración

1. Clona el repositorio:
```bash
git clone https://github.com/franzcelta/LoreRuneTerra.git
```

2. Crea la base de datos en PostgreSQL:
```sql
CREATE DATABASE loreruneterra;
```

3. Crea el fichero de configuración en `src/main/resources/config.properties`:

```properties
db.host=localhost
db.port=5432
db.name=loreruneterra
db.user=tu_usuario
db.password=tu_contraseña
```

4. Ejecuta el DDL de la sección de arquitectura para crear las tablas.

5. Ejecuta desde IntelliJ IDEA con la clase principal `com.loreruneterra.MainApp`.

---

## Capturas

| Menú principal | Catálogo con filtros |
|---|---|
| ![Menu](docs/screen1.png) | ![Catalogo](docs/screen2.png) |

| Dashboard | Libro del campeón |
|---|---|
| ![Dashboard](docs/screen5.png) | ![Libro](docs/screen3a.png) |

---

## Licencia

Proyecto académico — uso educativo. Los datos de campeones pertenecen a Riot Games (DataDragon API).