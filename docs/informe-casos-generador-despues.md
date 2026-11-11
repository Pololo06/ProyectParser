# Informe de casos del generador PlantUML

**25/25 casos pasan la comparación de contenido.** Coinciden los tipos en
25/25 casos, los miembros en 25/25 y las relaciones en 25/25.
Se analizaron 112 archivos Java en las ejecuciones sin errores.
Hay 0/25 coincidencias textuales con saltos de línea normalizados.

La prueba usa `DiagramFacade.generatePlantUml` con las opciones predeterminadas y excluye
`DiagramaClases`, como hacen los proyectos de referencia. Un caso pasa si todos sus archivos
Java se analizan y coinciden sus tipos (nombre simple, categoría, genéricos y estereotipos),
sus miembros (tipos, firmas, visibilidad, modificadores mostrados e inicializadores) y sus
relaciones (extremos, flecha, multiplicidad y etiqueta). Se normalizan las dos sintaxis de
miembros, los estereotipos redundantes con la categoría, los nombres calificados de extremos,
el orden y los espacios de tipos. Los paquetes de presentación, el orden y los estilos visuales
se pueden revisar en los diffs textuales que deja el comando.

Desde la raíz del repositorio, ejecute lo siguiente con Python 3 y JDK 17 o posterior.

```sh
python3 proyect-parser-core/tests/verificar_casos.py /tmp/pruebas-parser
```

| Caso | Java analizados | Tipos | Miembros | Relaciones | Texto | Resultado |
|---|---:|---|---|---|---|---|
| caso_01 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_02 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_03 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_04 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_05 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_06 | 4/4 | Sí | Sí | Sí | No | Pasa |
| caso_07 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_08 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_09 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_10 | 5/5 | Sí | Sí | Sí | No | Pasa |
| caso_11 | 7/7 | Sí | Sí | Sí | No | Pasa |
| caso_12 | 4/4 | Sí | Sí | Sí | No | Pasa |
| caso_13 | 6/6 | Sí | Sí | Sí | No | Pasa |
| caso_14 | 6/6 | Sí | Sí | Sí | No | Pasa |
| caso_15 | 4/4 | Sí | Sí | Sí | No | Pasa |
| caso_16 | 5/5 | Sí | Sí | Sí | No | Pasa |
| caso_17 | 5/5 | Sí | Sí | Sí | No | Pasa |
| caso_18 | 5/5 | Sí | Sí | Sí | No | Pasa |
| caso_19 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_20 | 4/4 | Sí | Sí | Sí | No | Pasa |
| caso_21 | 4/4 | Sí | Sí | Sí | No | Pasa |
| caso_22 | 4/4 | Sí | Sí | Sí | No | Pasa |
| caso_23 | 3/3 | Sí | Sí | Sí | No | Pasa |
| caso_24 | 5/5 | Sí | Sí | Sí | No | Pasa |
| caso_25 | 14/14 | Sí | Sí | Sí | No | Pasa |
