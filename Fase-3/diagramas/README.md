# Diagramas UML actualizados (Fase 3)

| Archivo | Diagrama |
|---|---|
| `clases.png` | Diagrama de clases (sección 5.2). Coincide con el código de `../tienda-virtual-poo` y con las tablas de la Fase 4. |
| `casos_uso_1.png` | Casos de uso (1/2), personal interno (sección 5.1). |

Los `.dot` son las fuentes de Graphviz. Para regenerar las imágenes desde esta carpeta:

```bash
dot -Tpng -Gdpi=150 clases.dot -o clases.png
dot -Tpng -Gdpi=130 casos_uso_1.dot -o casos_uso_1.png
```

Los demás diagramas (casos de uso 2/2, secuencia, actividades, estados, componentes y despliegue) no cambiaron y siguen en el documento.
